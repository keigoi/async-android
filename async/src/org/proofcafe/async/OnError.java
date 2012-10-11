package org.proofcafe.async;

/**
 * ネットワークエラー発生時のコールバック
 */
public interface OnError {
	public void onNetworkFailure(Exception e, Cont<Boolean> cont, Async<?> extra);
	public void onGeneralError(CanFail res, Runnable ifFail, Async<?> extra);
}
