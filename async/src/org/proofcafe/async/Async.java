package org.proofcafe.async;

import android.util.Log;
import static org.proofcafe.async.Util.*;

public abstract class Async<A> {

	protected Async() {
	}

	abstract void execInternal(Object listener, Object token, Cont<A> cont);

	public final void exec(Object l, final Cont<A> cont) {
		final Listener listener = listener(l);
		final Object token = new Object();
		
		listener.onAsyncStart(token);
		
		runInternal(l, token, new Cont<A>() {
			public void apply(final A a) {
				Util.runInUiThread(new Runnable() {
					public void run() {
						cont.apply(a);
						listener.onAsyncEnd(token);
					}
				});
			}
		});
	}

	public final void exec(Object listener) {
		exec(listener, new Cont<A>() {
			public void apply(A a) {
				// pass
			}
		});
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

	protected final void runInternal(Object listener, Object token, Cont<A> cont) {
		execInternal(listener, token, cont);
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
			private void doit(A a, Object listener, Object token, Cont<B> cont) {
				Async<B> async;
				try {
					async = f(a);
				} catch (AsyncCancelledException e) {
					Log.d("Async", "Async cancelled at:" + Bind.this.getClass());
					// FIXME
					return;
				}
				async.runInternal(listener, token, cont);
			}

			@Override
			void execInternal(final Object listener, final Object token, final Cont<B> cont) {
				Async.this.runInternal(listener, null, new Cont<A>() {
					public void apply(final A a) {
						if (runInUiThread == null || runInUiThread.booleanValue() == Util.isInUiThread()) {
							// run in the current thread
							doit(a, listener, token, cont);
						} else if (runInUiThread) {
							// run in the ui thread
							Util.runInUiThread(new Runnable() {
								public void run() {
									doit(a, listener, token, cont);
								}
							});
						} else {
							// run in background
							Util.runInBackground(new Runnable() {
								public void run() {
									doit(a, listener, token, cont);
								}
							});
						}
					}
				});
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
