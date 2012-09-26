package org.proofcafe.async;

public interface Listener {
	public void onAsyncStart(Object token);
	public void onBackgroundStart(Object token);
	public void onBackgroundEnd(Object token);
	public void onErrorRetry(Object token, Runnable retry);
	public void onAsyncEnd(Object token);
}
