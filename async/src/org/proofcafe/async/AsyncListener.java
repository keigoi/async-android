package org.proofcafe.async;

public interface AsyncListener {
	public void onAsyncStart(AsyncState state, Async<?> toStart, AsyncParam param);
	public void onAsyncEnd(AsyncState state, Async<?> ended);
}
