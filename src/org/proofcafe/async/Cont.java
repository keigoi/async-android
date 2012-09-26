package org.proofcafe.async;

public interface Cont<A> {
	abstract void apply(A a);
}