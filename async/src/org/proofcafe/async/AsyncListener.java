package org.proofcafe.async;

public interface AsyncListener {
	public void onAsyncStart(Object token, Async<?> toStart, boolean showDialog);
	public void onAsyncEnd(Object token, Async<?> ended);
}
