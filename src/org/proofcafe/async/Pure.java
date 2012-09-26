package org.proofcafe.async;


/**
 * 何もせずコンストラクタに渡された引数を次のAsyncに渡す．
 * 
 * @see org.proofcafe.async.Async
 */
public class Pure<A> extends Async<A> {
	private final A result;

	public Pure(A result) {
		this.result = result;
	}

	@Override
	final void execInternal(Object context, Object token, final Cont<A> cont) {
		cont.apply(result);
	}
	
	public static <A> Pure<A> pure(A x) {
	    return new Pure<A>(x);
	}
}