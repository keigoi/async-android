package org.proofcafe.async;

/**
 * Continuation (or what-to-do-next) object. Normally you should call apply() exactly once.
 *  
 * @author keigoi
 *
 * @param <A>
 */
public interface Cont<A> {
	/**
	 * Fire the continuation.
	 * @param a The result that is passed to this continuation.
	 */
	abstract void apply(A a);
}