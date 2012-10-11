package org.proofcafe.async;

import android.util.Log;

public class AsyncState {
	private volatile boolean interrupted;
	public final boolean interruptible;

	AsyncState(boolean interruptible) {
		this.interruptible = interruptible;
	}

	public final boolean isInterrupted() {
		return interrupted;
	}
	
	public boolean interrupt() {
		interrupted = interruptible;
		Log.e("async", interrupted ? "interrupted" : "can't interrupt");
		return interrupted;
	}
}
