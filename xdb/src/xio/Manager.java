package xio;

import com.goldhuman.Common.Marshal.OctetsStream;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import xdb.Executor;
import xdb.Trace;

import javax.management.*;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
/**
 * 
 * ���ӹ���
 * 
 * DynamicMBean �� XioConf ��open��ʱ��ע�ᵽMBeans�С�
 * 
 * @author lichenghua
 *
 */
public abstract class Manager implements DynamicMBean {
	// û���̰߳�ȫ���������˹����ʼ�����Ժ��Ƕ���
	String name;
	int maxSize;
	private Map<String, Creator> creators = new HashMap<String, Creator>();
	Manager.Coder coder; // Э������
	private XioConf conf;
	private AtomicLong newXioCount = new AtomicLong();

	/**
	 * Э�鴦���ɷ�����Э����뵽�̳߳��У��ɹ����̴߳�����Ӧ�ÿ����������������һЩ������ 
	 * <p><b>ע�⣬����������ز������е�Э�顣</b>�� Rpc �Ĵ�����һ����������·��
	 * ������������
	 * <ol>
	 * <li>ͬ������ʱ��Rpc.Result��ֱ�ӽ����ȴ����̡߳� 
	 * <li>��� Rpc.Result �����˴洢���̣�xio��ֱ�ӰѴ������� xdb.Procedure�����ڲ��Ѵ洢���̵Ľ�����ͻ�ȥ��
	 * </ol>
	 * @param p ��Ҫִ�е�Э�顣
	 */
	public void execute(Protocol p) {
		Executor.getInstance().execute(p);
	}

	/**
	 * ����Э��֮ǰ����
	 */
	protected void beforeSend(Protocol p, Xio to) {
	}

	// ��Ҫʵ�ֵ�Manager������
	/**
	 * ���ӹر�ʱ����
	 */
	protected abstract void removeXio(Xio xio, Throwable e);

	/**
	 * ���ӽ���ʱ����
	 */
	protected abstract void addXio(Xio xio);

	/**
	 * ��ǰ�������е���������
	 */
	public abstract int size();

	/**
	 * ��㷵��һ�����ӣ����ܷ���null�����ڲ���Ŀ��
	 */
	public abstract Xio get();

	/**
	 * close
	 */
	protected void close() {
		// close creator
		for (Creator c : creators.values())
			c.close();
		closeAllContexts();
	}


	/**
	 * ͨ�����������������ʵ��:
	 * * IP ���ơ�
	 * * ��ʼ��filter��
	 * * ���� interestOps
	 * * �ṩ�µ� Xio ʵ�֡�
	 * @param channel
	 * @return
	 */
	protected Xio newXio(Creator creator, SelectorThread selector, SocketChannel channel) {
		Xio.Input input = new Xio.Input();
		Xio.Output output = new Xio.Output(creator);
		coder.initFilterList(input, output);
		return new Xio(creator, selector, channel, Xio.OP_READ, input, output);
	}

	final long incrementAndGetNewXioCount() {
		return newXioCount.incrementAndGet();
	}

	final String getMBeanName() {
		return "XioConf." + conf.getName() + "/" + getClass().getName() + "." + getName();
	}

	protected void onConnectAbort(Connector c, Throwable e) {
		xdb.Trace.warn("Connect Abort: " + c, e);
	}

	public Manager.Coder getCoder() {
		return coder;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public int getCreatorCount() {
		return creators.size();
	}

	public Creator getCreator(InetSocketAddress address) {
		for (Creator c : this.creators.values())
			if (address.equals(c.getAddress()))
				return c;
		return null;
	}

	public Creator getCreator(String name) {
		return creators.get(name);
	}

	void open() {
		for (Creator c : creators.values()) c.open();
	}

	/////////////////////////////////////////////////////////////////////
	// ϵ�кż������Ĺ���.
	private int serialId = 0; // 1~0x7fffffff. see Rpc
	private Map<Integer, java.io.Closeable> contexts = new HashMap<Integer, java.io.Closeable>();

	private final void closeAllContexts() {
		synchronized (contexts) {
			for (java.io.Closeable c : contexts.values())
				try { c.close(); } catch (Throwable e) { }
				contexts.clear();
		}
	}

	public final java.io.Closeable getContext(int sid) {
		synchronized (contexts) {
			return contexts.get(sid);
		}
	}

	public final <T> T getContext(int sid, T hint) {
		synchronized (contexts) {
			@SuppressWarnings("unchecked")
			T a = (T)contexts.get(sid);
			return a;
		}
	}

	public final java.io.Closeable removeContext(int sid) {
		synchronized (contexts) {
			return contexts.remove(sid);
		}
	}

	public final <T> T removeContext(int sid, T hint) {
		synchronized (contexts) {
			@SuppressWarnings("unchecked")
			T a = (T)contexts.remove(sid);
			return a;
		}
	}

	public final int addContext(java.io.Closeable obj) {
		synchronized (contexts) {
			do {
				++ serialId;
				if (serialId <= 0) // ��λ������rpc�����������Ƿ�����. ���Ᵽ�� 0��
					serialId = 1;
				if (false == contexts.containsKey(serialId)) {
					contexts.put(serialId, obj);
					return serialId;
				}
			} while (true);
		}
	}
	/////////////////////////////////////////////////////////////////
	public static abstract class Coder {
		public abstract void parse(Manager manager, Element self) throws Exception;
		public abstract void initFilterList(Filter.List input, Filter.List output);
		public abstract void dispatch(int type, OctetsStream os, Xio from, Object context) throws Exception;
		public abstract void checkSend(Protocol p, int psize);

		static Coder create(Manager manager, Element self) throws Exception {
			String clsName = self.getAttribute("class");
			Coder coder = clsName.isEmpty() ? new Protocol.Coder() :
				(Coder)Class.forName(clsName).newInstance();
			coder.parse(manager, self);
			return coder;
		}
	}

	private void add(Creator c) {
        Trace.debug("addCreator:{}", c);
        if (null != creators.put(c.getName(), c))
			throw new RuntimeException("Creator duplicate! " + c);
	}

	void add(Manager.Coder coder) {
		if (null != this.coder)
			throw new RuntimeException("too many Coder");
		this.coder = coder;
	}

	/**
	 * �������ã�����ʼ�����á�
	 * ��������������������������������Զ��� Manager �����á�����ʱ������� super.parse �Գ�ʼ���������á�
	 * @param self
	 * @throws Exception
	 */
	protected void parse(Element self) throws Exception {
		this.name = self.getAttribute("name");
		if (this.name.isEmpty())
			throw new RuntimeException("Manager need a name");

		String tmp;
		this.maxSize = (tmp = self.getAttribute("maxSize")).isEmpty() ? 0 : Integer.parseInt(tmp);

		NodeList childnodes = self.getChildNodes();
		for (int i = 0; i < childnodes.getLength(); ++i) {
			Node node = childnodes.item(i);
			if (Node.ELEMENT_NODE != node.getNodeType())
				continue;

			Element e = (Element) node;
			String n = e.getNodeName();
			if (n.equals("Connector"))     add(Connector.create(this, e));
			else if (n.equals("Acceptor")) add(Acceptor.create(this, e));
			else if (n.equals("Coder"))    add(Manager.Coder.create(this, e));
            else if (n.equals("DynamicMultiConnector")) add (DynamicMultiConnector.create(this, e));
			else
				throw new RuntimeException("Unkown! node=" + n + " parent=" + self.getNodeName() + "," + name);
		}
	}

	public final XioConf getConf() {
		return conf;
	}

	public static Manager create(XioConf conf, Element self) throws Exception {
		String clsName = self.getAttribute("class");
		Manager manager = clsName.isEmpty() ? new ManagerSimple() :
			(Manager)Class.forName(clsName).newInstance();
		manager.parse(self);
		manager.conf = conf;
		return manager;
	}

	protected Manager() {
	}

	////////////////////////////////////////////////////////////////
	// DynamicMBean implement
	@Override
	public Object getAttribute(String attribute) {
		if (attribute.startsWith("count."))
			return this.newXioCount.get(); // Ŀǰֻ��һ����������
		return "<- see this";
	}

	@Override
	public AttributeList getAttributes(String[] attributes) {
		AttributeList alist = new AttributeList();
		for (int i = 0; i < attributes.length; ++i) {
			Object value = getAttribute(attributes[i]);
			if (null != value)
				alist.add(new Attribute(attributes[i], value));
		}
		return alist;
	}

	@Override
	public MBeanInfo getMBeanInfo() {
		MBeanAttributeInfoBuilder builder = new MBeanAttributeInfoBuilder();
		this.buildMBeanInfo(builder);
		return new MBeanInfo(this.getClass().getName(), "Manager",
				builder.toArray(), null, null, null);
	}

	public static final class MBeanAttributeInfoBuilder {
		private Map<String, MBeanAttributeInfo> attrs = new HashMap<String, MBeanAttributeInfo>();

		public void add(MBeanAttributeInfo minfo) {
			if (attrs.put(minfo.getName(), minfo) != null)
				throw new RuntimeException("duplicate MBeanAttributeInfo. name=" + minfo.getName());
		}

		public MBeanAttributeInfo[] toArray() {
			return attrs.values().toArray(new MBeanAttributeInfo[attrs.size()]);
		}
	}

	/**
	 * �����������أ����������Ķ�̬ MBean ���ԡ�
	 * ����ʵ�ֱ������ super �ķ����������������Իᱻ��ʧ��
	 * ��������붯̬����ʱ��Ҳ��Ҫ���� getAttribute ��������ֵ��
	 * @param attrs
	 */
	protected void buildMBeanInfo(MBeanAttributeInfoBuilder attrs) {
		for (Creator c : creators.values()) {
			StringBuilder sb = new StringBuilder();
			if (c instanceof Acceptor)
				sb.append("acceptor.");
			else if (c instanceof Connector)
				sb.append("connector.");
			else
				sb.append("creator.");
			sb.append(c.getName());
			attrs.add(new MBeanAttributeInfo(sb.toString(),
					"java.lang.String", "creator", true, false, false));
		}
		attrs.add(new MBeanAttributeInfo("count.newXioCount",
				"java.lang.Long", "count of xio created", true, false, false));
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException {
		return null;
	}

	@Override
	public void setAttribute(Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException {
	}

	@Override
	public AttributeList setAttributes(AttributeList attributes) {
		return null;
	}
}