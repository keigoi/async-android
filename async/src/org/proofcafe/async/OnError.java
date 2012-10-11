package org.proofcafe.async;

/**
 * ネットワークエラー発生時のコールバック
 */
public interface OnError {
	public void onNetworkFailure(AsyncState state, Exception e, Cont<Boolean> cont, Async<?> extra);
	public void onGeneralError(AsyncState state, CanFail res, Runnable ifFail, Async<?> extra);
}
