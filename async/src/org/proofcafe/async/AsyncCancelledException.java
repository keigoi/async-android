package org.proofcafe.async;

public class AsyncCancelledException extends Exception {

	private static final long serialVersionUID = -1853883135637381287L;

	public AsyncCancelledException() {
	}

	public AsyncCancelledException(String detailMessage) {
		super(detailMessage);
	}

	public AsyncCancelledException(Throwable throwable) {
		super(throwable);
	}

	public AsyncCancelledException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
