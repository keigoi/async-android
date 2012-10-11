package org.proofcafe.async;

public class AsyncParam {
	public final boolean showDialog, interruptible;

	public AsyncParam() {
		this(true, false);
	}
	
	private AsyncParam(boolean showDialog, boolean interruptible) {
		this.showDialog = showDialog;
		this.interruptible = interruptible;
	}
	
	public AsyncParam hideDialog() {
		return new AsyncParam(false, this.interruptible);
	}
	
	public AsyncParam interruptible() {
		return new AsyncParam(this.showDialog, true);
	}

}
