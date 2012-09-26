package org.proofcafe.async;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

class Util {
	static Handler uiHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}
	};

	static boolean isInUiThread() {
		return Looper.getMainLooper().getThread() == Thread.currentThread();
	}

	static void runInBackground(final Runnable r) {
		// make use of the thread pool in AsyncTask
		new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... params) {
				r.run();
				return null;
			}
		}.execute();
	}

	static void runInUiThread(Runnable r) {
		uiHandler.post(r);
	}

	private static final Listener nullListener = new Listener() {
		public void onErrorRetry(Object token, Runnable retry) {
		}

		public void onBackgroundStart(Object token) {
		}

		public void onBackgroundEnd(Object token) {
		}

		public void onAsyncStart(Object token) {
		}

		public void onAsyncEnd(Object token) {
		}
	};

	static Listener listener(Object l) {
		return l instanceof Listener ? (Listener)l : nullListener;
	}

}