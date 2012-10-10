package org.proofcafe.async;

public interface AsyncListener {
	public void onAsyncStart(Object token, boolean showDialog);
	public void onAsyncEnd(Object token);
}
