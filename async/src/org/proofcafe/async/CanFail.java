package org.proofcafe.async;

public interface CanFail {
	
	public boolean isFailed(); 
	public String getErrorMessage();

}
