
package lx.gs.family.msg;

import com.goldhuman.Common.Marshal.OctetsStream;
import com.goldhuman.Common.Marshal.MarshalException;

// {{{ RPCGEN_IMPORT_BEGIN
// {{{ DO NOT EDIT THIS

abstract class __SUpLevelMallNotify__ extends xio.Protocol { }

/** 升级商店通知，通知全族人员
*/
// DO NOT EDIT THIS }}}
// RPCGEN_IMPORT_END }}}

public class SUpLevelMallNotify extends __SUpLevelMallNotify__ {
	@Override
	protected void process() {
		// protocol handle
	}

	// {{{ RPCGEN_DEFINE_BEGIN
	// {{{ DO NOT EDIT THIS
	public static final int PROTOCOL_TYPE = 6571041;

	public int getType() {
		return 6571041;
	}

	public lx.gs.family.msg.FamilyBasicInfo family;

	public SUpLevelMallNotify() {
		family = new lx.gs.family.msg.FamilyBasicInfo();
	}

	public SUpLevelMallNotify(lx.gs.family.msg.FamilyBasicInfo _family_) {
		this.family = _family_;
	}

	public final boolean _validator_() {
		if (!family._validator_()) return false;
		return true;
	}

	public OctetsStream marshal(OctetsStream _os_) {
		_os_.marshal(family);
		return _os_;
	}

	public OctetsStream unmarshal(OctetsStream _os_) throws MarshalException {
		family.unmarshal(_os_);
		return _os_;
	}

	public boolean equals(Object _o1_) {
		if (_o1_ == this) return true;
		if (_o1_ instanceof SUpLevelMallNotify) {
			SUpLevelMallNotify _o_ = (SUpLevelMallNotify)_o1_;
			if (!family.equals(_o_.family)) return false;
			return true;
		}
		return false;
	}

	public int hashCode() {
		int _h_ = 0;
		_h_ += family.hashCode();
		return _h_;
	}

	public String toString() {
		StringBuilder _sb_ = new StringBuilder();
		_sb_.append("(");
		_sb_.append(family).append(",");
		_sb_.append(")");
		return _sb_.toString();
	}

	// DO NOT EDIT THIS }}}
	// RPCGEN_DEFINE_END }}}

}

