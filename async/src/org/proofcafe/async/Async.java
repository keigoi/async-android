package org.proofcafe.async;

import android.content.Context;
import android.util.Log;
import static org.proofcafe.async.Util.*;

public abstract class Async<A> {

	protected Async() {
	}
	
	public static final AsyncParam HIDE_DIALOG = new AsyncParam().hideDialog();
	public static final AsyncParam INTERRUPTIBLE = new AsyncParam().interruptible();	

	protected abstract void execInternal(Context context, AsyncState state, Cont<A> cont, Runnable ifFail);

	protected final void runInternal(final Context context, AsyncState state, Cont<A> cont, final Runnable ifFail) {
		if(state.isInterrupted()) {			
			Util.runInUiThread(new Runnable(){
				public void run() {
					ifFail.run();
				}});
		} else {
			execInternal(context, state, cont, ifFail);
		}
	}

	public final void exec(final Context context, final AsyncParam param, final Cont<A> cont, final Runnable ifFail) {
		
		final AsyncListener listener = listener(context);
		final AsyncState state = new AsyncState(param.interruptible);
		
		Util.runInUiThread(new Runnable() {
			@Override
			public void run() {
				listener.onAsyncStart(state, Async.this, param);
				
				runInternal(context, state, new Cont<A>() {
					public void apply(final A a) {
						Util.runInUiThread(new Runnable() {
							public void run() {
								cont.apply(a);
								listener.onAsyncEnd(state, Async.this);
							}
						});
					}
				}, new Runnable() {
					public void run() {
						ifFail.run();
						listener.onAsyncEnd(state, Async.this);
					}});
			}
		});
	}
	
	public final void exec(Context context, AsyncParam param, Cont<A> cont) {
		exec(context,
			param,
			cont, 
			new Runnable(){public void run() {}});
	}
	
	public final void exec(Context context, AsyncParam param) {
		exec(context, 
			param,
			new Cont<A>(){public void apply(A a) {}}, 
			new Runnable(){public void run() {}});
	}
	
	public final void exec(Context context, AsyncParam param, Runnable ifFail) {
		exec(context, param, new Cont<A>(){public void apply(A a) {}}, ifFail);
	}
	
	public final void exec(Context context, Cont<A> cont, Runnable ifFail) {
		exec(context, new AsyncParam(), cont, ifFail);
	}
	
	public final void exec(Context context, Cont<A> cont) {
		exec(context
			, new AsyncParam()
			, cont
			, new Runnable(){
			public void run() {
				// pass
			}});
	}

	public final void exec(Context context) {
		exec(context
		, new AsyncParam() 
		, new Cont<A>() {
			public void apply(A a) {
				// pass
			}
		}, new Runnable() {
			public void run() {
				// pass
			}
		});
	}

	public final <B> Async<B> bind(final Async<B> that) {
		return new Bind<B>() {
			protected Async<B> f(A a) {
				return that;
			}
		}.get();
	}

	public final <B> Async<B> bindInUiThread(final Async<B> that) {
		return new BindInUiThread<B>() {
			protected Async<B> f(A a) {
				return that;
			}
		}.get();
	}

	public final <B> Async<B> bindBackground(final Async<B> that) {
		return new BindBackground<B>() {
			protected Async<B> f(A a) {
				return that;
			}
		}.get();
	}

	// 本来は extends Async<B> としたかったが、eclipseのコンパイラのバグのためできなかった．
	// (Oracle JDK / OpenJDKならコンパイル通る)
	// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=388903
	// そこで、このラッパーを getで返すようにした.
	public abstract class Bind<B> {

		private final Boolean runInUiThread;

		protected abstract Async<B> f(A a) throws AsyncCancelledException;

		public Bind() {
			this.runInUiThread = null; // don't care
		}

		protected Bind(boolean runInUiThread) {
			this.runInUiThread = runInUiThread;
		}

		private final Proxy proxy = new Proxy();

		public final Async<B> get() {
			return proxy;
		}

		private final class Proxy extends Async<B> {
			private void runBind(A a, Context context, AsyncState state, Cont<B> cont, Runnable ifFail) {
				Async<B> next;
				try {
					next = f(a);
				} catch (AsyncCancelledException e) {
					Log.d("Async", "Async cancelled at:" + Bind.this.getClass());
					ifFail.run();
					return;
				}
				next.runInternal(context, state, cont, ifFail);
			}
			
			@Override
			protected void execInternal(final Context context, final AsyncState state, final Cont<B> cont, final Runnable ifFail) {
				// run enclosing (aka left-hand side of >>=) Async, with a continuation that runs the next Async
				Async.this.runInternal(context, state, new Cont<A>() {
					public void apply(final A a) {
						// after enclosing Async ends, run Bind.f
						if (runInUiThread == null || runInUiThread.booleanValue() == Util.isInUiThread()) {
							// run in the current thread
							runBind(a, context, state, cont, ifFail);
						} else if (runInUiThread) {
							// run in the ui thread
							Util.runInUiThread(new Runnable() {
								public void run() {
									runBind(a, context, state, cont, ifFail);
								}
							});
						} else {
							// run in background
							Util.runInBackground(new Runnable() {
								public void run() {
									runBind(a, context, state, cont, ifFail);
								}
							});
						}
					}
				}, ifFail);
			}
		}
	}

	public abstract class BindInUiThread<B> extends Bind<B> {
		public BindInUiThread() {
			super(true);
		}
	}

	public abstract class BindBackground<B> extends Bind<B> {
		public BindBackground() {
			super(false);
		}
	}
}
