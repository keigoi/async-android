package org.proofcafe.async;

import android.content.Context;
import android.util.Log;

public abstract class InUiThread<A> extends Async<A> {
	protected abstract A doInUiThread() throws AsyncCancelledException;

	public InUiThread() {
	}

	@Override
	protected void execInternal(final Context context, final Object token, final Cont<A> cont, final Runnable ifFail) {
		if (Util.isInUiThread()) {
			try {
				cont.apply(doInUiThread());
			} catch (AsyncCancelledException e) {
				Log.d("Async", "Async is cancelled at:" + InUiThread.this.getClass());
				Util.listener(context).onAsyncEnd(token, InUiThread.this);
				ifFail.run();
			}
		} else {
			// switch into ui thread
			Util.runInUiThread(new Runnable() {
				public void run() {
					try {
						cont.apply(doInUiThread());
					} catch (AsyncCancelledException e) {
						Log.d("Async", "Async is cancelled at:" + InUiThread.this.getClass());
						Util.listener(context).onAsyncEnd(token, InUiThread.this);
						ifFail.run();
					}
				}
			});
		}
	}

}
