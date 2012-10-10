package org.proofcafe.async;

import android.content.Context;

public abstract class Background<A extends CanFail, Progress> extends Async<A> {

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

	protected abstract A doInBackground() throws Exception;
	
	@Override
	protected void execInternal(final Context context, final Object token, final Cont<A> cont, final Runnable ifFail) {
		if (Util.isInUiThread()) {
			// switch into background
			Util.runInBackground(new Runnable() {
				public void run() {
					doIt(context, token, cont, ifFail);
				}
			});
		} else {
			doIt(context, token, cont, ifFail);
		}
	}
	
	private final void doIt(final Context context, final Object token, final Cont<A> cont, final Runnable ifFail) {
		A a;
		Exception e = null;
		try {
			a = doInBackground();
		} catch (Exception e_) {
			e_.printStackTrace();
			a = null;
			e = e_;
		}
		final A result = a;
		final Exception exception = e;
		
		if(a==null) { // ネットワークエラー 再実行すべきかどうかユーザに問い合わせ
			Util.runInUiThread(new Runnable() {
				public void run() {
					Util.onErrorListener(context).onNetworkFailure(exception, new Cont<Boolean>() {
						@Override
						public void apply(Boolean retry) {
							if(retry) { 
								// 再実行
								Util.runInBackground(new Runnable() {
									@Override
									public void run() {
										doIt(context, token, cont, ifFail);
									}});
							} else {
								// 終了
								Util.listener(context).onAsyncEnd(token, Background.this);
								ifFail.run();
							}
						}
					}, Background.this);
				}
			});
			return;
		} else if(a.isFailed()) { // 業務エラー
			Util.runInUiThread(new Runnable() {
				@Override
				public void run() {
					// 終了
					Util.onErrorListener(context).onGeneralError(result, new Cont<Void>() {
						@Override
						public void apply(Void a) {
							Util.listener(context).onAsyncEnd(token, Background.this);
							ifFail.run();
						}
					}, Background.this);
				}
			});
			return;
		}
		cont.apply(result);
	}
}
