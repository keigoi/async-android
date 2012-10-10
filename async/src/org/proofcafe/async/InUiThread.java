package org.proofcafe.async;

import android.content.Context;
import android.util.Log;

public abstract class InUiThread<A> extends Async<A> {
	protected abstract A doInUiThread() throws AsyncCancelledException;

	public InUiThread() {
	}

	@Override
	protected void execInternal(Context listener, Object token, final Cont<A> cont) {
		if (Util.isInUiThread()) {
			try {
				cont.apply(doInUiThread());
			} catch (AsyncCancelledException e) {
				Log.d("Async", "Async is cancelled at:" + InUiThread.this.getClass());
				// FIXME signal cancellation (hide progress dialog)
			}
		} else {
			// switch into ui thread
			Util.runInUiThread(new Runnable() {
				public void run() {
					try {
						cont.apply(doInUiThread());
					} catch (AsyncCancelledException e) {
						Log.d("Async", "Async is cancelled at:" + InUiThread.this.getClass());
						// FIXME signal cancellation (hide progress dialog)
					}
				}
			});
		}
	}

}
