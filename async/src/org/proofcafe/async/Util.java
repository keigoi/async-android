package org.proofcafe.async;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class Util {
	private static Handler uiHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}
	};

	public static boolean isInUiThread() {
		return Looper.getMainLooper().getThread() == Thread.currentThread();
	}

	public static void runInBackground(final Runnable r) {
		// make use of the thread pool in AsyncTask
		new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... params) {
				r.run();
				return null;
			}
		}.execute();
	}

	public static void runInUiThread(Runnable r) {
		uiHandler.post(r);
	}

	private static final AsyncListener nullListener = new AsyncListener() {
		@Override
		public void onAsyncStart(AsyncState state, Async<?> toStart, AsyncParam param) {
		}

		@Override
		public void onAsyncEnd(AsyncState state, Async<?> ended) {
		}
	};

	public static AsyncListener listener(Object l) {
		return l instanceof AsyncListener ? (AsyncListener)l : nullListener;
	}
	
	public static OnError onErrorListener(Context context) {
		return context instanceof OnError ? (OnError)context : new OnError() {
			@Override
			public void onNetworkFailure(AsyncState state, Exception e, Cont<Boolean> cont, Async<?> extra) {
				cont.apply(false);
			}
			@Override
			public void onGeneralError(AsyncState state, CanFail res, Runnable ifFail, Async<?> extra) {
				ifFail.run();
			}
		};
	}
}