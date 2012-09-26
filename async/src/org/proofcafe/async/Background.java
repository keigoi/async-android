package org.proofcafe.async;

public abstract class Background<A, Progress> extends Async<A> {

	protected abstract A doInBackground() throws Exception;

	public Background() {
	}

	protected final void publishProgress(final Progress... values) {
		Util.runInUiThread(new Runnable() {
			public void run() {
				onProgressUpdate(values);
			}
		});
	}

	protected void onProgressUpdate(Progress... progress) {
	}

	@Override
	void execInternal(Object listener, Object token, final Cont<A> cont) {
		if (Util.isInUiThread()) {
			// switch into background
			Util.runInBackground(new Runnable() {
				public void run() {
					A a;
					try {
						a = Background.this.doInBackground();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
					cont.apply(a);
				}
			});
		} else {
			A a;
			try {
				a = doInBackground();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			cont.apply(a);
		}
	}
}
