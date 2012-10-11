package org.proofcafe.async;

import android.content.Context;
import android.util.Log;

public abstract class InUiThread<A> extends Async<A> {
	protected abstract A doInUiThread() throws AsyncCancelledException;

	public InUiThread() {
	}

	@Override
	protected void execInternal(final Context context, final AsyncState state, final Cont<A> cont, final Runnable ifFail) {
		if (Util.isInUiThread()) {
			doIt(cont, ifFail);
		} else {
			// switch into ui thread
			Util.runInUiThread(new Runnable() {
				public void run() {
					doIt(cont, ifFail);
				}
			});
		}
	}

	private void doIt(final Cont<A> cont, final Runnable ifFail) {
		A result;
		try {
			result = doInUiThread();
		} catch (AsyncCancelledException e) {
			Log.d("Async", "Async is cancelled at:" + InUiThread.this.getClass());
			ifFail.run();
			return;
		}
		cont.apply(result);
	}

}
