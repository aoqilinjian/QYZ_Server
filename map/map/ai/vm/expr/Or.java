package map.ai.vm.expr;

import map.ai.vm.VM;

public final class Or extends BooleanExpr {
	private final BooleanExpr left, right;
	public Or(BooleanExpr left, BooleanExpr right) {
		this.left = left;
		this.right = right;
	}
	
	@Override
	public boolean comput() {
		return left.comput() || right.comput();
	}

	@Override
	protected void onInit(VM vm) {
		this.left.init(vm);
		this.right.init(vm);
	}

}
