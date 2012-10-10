package org.proofcafe.async;

import android.content.Context;
import android.util.Log;
import static org.proofcafe.async.Util.*;

public abstract class Async<A> {

	protected Async() {
	}

	protected abstract void execInternal(Context context, Object token, Cont<A> cont, Runnable ifFail);

	public final void exec(Context context, final Cont<A> cont, final Runnable ifFail, final boolean withDialog) {
		final AsyncListener listener = listener(context);
		final Object token = new Object();
		
		Util.runInUiThread(new Runnable() {
			@Override
			public void run() {
				listener.onAsyncStart(token, Async.this, withDialog);
			}
		});
		
		runInternal(context, token, new Cont<A>() {
			public void apply(final A a) {
				Util.runInUiThread(new Runnable() {
					public void run() {
						cont.apply(a);
						listener.onAsyncEnd(token, Async.this);
					}
				});
			}
		}, ifFail);
	}
	
	public final void exec(Context context, Cont<A> cont, boolean showDialog) {
		exec(context, 
			cont, 
			new Runnable(){public void run() {}}, 
			showDialog);
	}
	
	public final void exec(Context context, boolean showDialog) {
		exec(context, 
			new Cont<A>(){public void apply(A a) {}}, 
			new Runnable(){public void run() {}}, 
			showDialog);
	}
	
	public final void exec(Context context, Runnable ifFail) {
		exec(context, new Cont<A>(){public void apply(A a) {}}, ifFail, true);
	}
	
	public final void exec(Context context, Cont<A> cont, Runnable ifFail) {
		exec(context, cont, ifFail, true);
	}
	
	public final void exec(Context context, Cont<A> cont) {
		exec(context, cont, new Runnable(){
			public void run() {
				// pass
			}}, true);
	}

	public final void exec(Context context) {
		exec(context, new Cont<A>() {
			public void apply(A a) {
				// pass
			}
		}, new Runnable() {
			public void run() {
				// pass
			}
		}, true);
	}

	public <B> Async<B> bind(final Async<B> that) {
		return new Bind<B>() {
			protected Async<B> f(A a) {
				return that;
			}
		}.get();
	}

	public <B> Async<B> bindInUiThread(final Async<B> that) {
		return new BindInUiThread<B>() {
			protected Async<B> f(A a) {
				return that;
			}
		}.get();
	}

	public <B> Async<B> bindBackground(final Async<B> that) {
		return new BindBackground<B>() {
			protected Async<B> f(A a) {
				return that;
			}
		}.get();
	}

	protected final void runInternal(Context listener, Object token, Cont<A> cont, Runnable ifFail) {
		execInternal(listener, token, cont, ifFail);
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
			private void doit(A a, Context listener, Object token, Cont<B> cont, Runnable ifFail) {
				Async<B> async;
				try {
					async = f(a);
				} catch (AsyncCancelledException e) {
					Log.d("Async", "Async cancelled at:" + Bind.this.getClass());
					return;
				}
				async.runInternal(listener, token, cont, ifFail);
			}

			@Override
			protected void execInternal(final Context listener, final Object token, final Cont<B> cont, final Runnable ifFail) {
				Async.this.runInternal(listener, null, new Cont<A>() {
					public void apply(final A a) {
						if (runInUiThread == null || runInUiThread.booleanValue() == Util.isInUiThread()) {
							// run in the current thread
							doit(a, listener, token, cont, ifFail);
						} else if (runInUiThread) {
							// run in the ui thread
							Util.runInUiThread(new Runnable() {
								public void run() {
									doit(a, listener, token, cont, ifFail);
								}
							});
						} else {
							// run in background
							Util.runInBackground(new Runnable() {
								public void run() {
									doit(a, listener, token, cont, ifFail);
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
