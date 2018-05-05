
package map.msg;

import com.goldhuman.Common.Marshal.OctetsStream;
import com.goldhuman.Common.Marshal.MarshalException;

// {{{ RPCGEN_IMPORT_BEGIN
// {{{ DO NOT EDIT THIS

abstract class __MEndArenaLogger__ extends xio.Protocol { }

// DO NOT EDIT THIS }}}
// RPCGEN_IMPORT_END }}}

public class MEndArenaLogger extends __MEndArenaLogger__ {
	@Override
	protected void process() {
		// protocol handle
	}

	// {{{ RPCGEN_DEFINE_BEGIN
	// {{{ DO NOT EDIT THIS
	public static final int PROTOCOL_TYPE = 6710123;

	public int getType() {
		return 6710123;
	}

	public int ectypeid;
	public int result;
	public long time;
	public long enemyroleid;

	public MEndArenaLogger() {
	}

	public MEndArenaLogger(int _ectypeid_, int _result_, long _time_, long _enemyroleid_) {
		this.ectypeid = _ectypeid_;
		this.result = _result_;
		this.time = _time_;
		this.enemyroleid = _enemyroleid_;
	}

	public final boolean _validator_() {
		return true;
	}

	public OctetsStream marshal(OctetsStream _os_) {
		_os_.marshal(ectypeid);
		_os_.marshal(result);
		_os_.marshal(time);
		_os_.marshal(enemyroleid);
		return _os_;
	}

	public OctetsStream unmarshal(OctetsStream _os_) throws MarshalException {
		ectypeid = _os_.unmarshal_int();
		result = _os_.unmarshal_int();
		time = _os_.unmarshal_long();
		enemyroleid = _os_.unmarshal_long();
		return _os_;
	}

	public boolean equals(Object _o1_) {
		if (_o1_ == this) return true;
		if (_o1_ instanceof MEndArenaLogger) {
			MEndArenaLogger _o_ = (MEndArenaLogger)_o1_;
			if (ectypeid != _o_.ectypeid) return false;
			if (result != _o_.result) return false;
			if (time != _o_.time) return false;
			if (enemyroleid != _o_.enemyroleid) return false;
			return true;
		}
		return false;
	}

	public int hashCode() {
		int _h_ = 0;
		_h_ += ectypeid;
		_h_ += result;
		_h_ += (int)time;
		_h_ += (int)enemyroleid;
		return _h_;
	}

	public String toString() {
		StringBuilder _sb_ = new StringBuilder();
		_sb_.append("(");
		_sb_.append(ectypeid).append(",");
		_sb_.append(result).append(",");
		_sb_.append(time).append(",");
		_sb_.append(enemyroleid).append(",");
		_sb_.append(")");
		return _sb_.toString();
	}

	public int compareTo(MEndArenaLogger _o_) {
		if (_o_ == this) return 0;
		int _c_ = 0;
		_c_ = ectypeid - _o_.ectypeid;
		if (0 != _c_) return _c_;
		_c_ = result - _o_.result;
		if (0 != _c_) return _c_;
		_c_ = Long.signum(time - _o_.time);
		if (0 != _c_) return _c_;
		_c_ = Long.signum(enemyroleid - _o_.enemyroleid);
		if (0 != _c_) return _c_;
		return _c_;
	}

	// DO NOT EDIT THIS }}}
	// RPCGEN_DEFINE_END }}}

}
