package org.proofcafe.async;

import android.content.Context;
import android.util.Log;
import static org.proofcafe.async.Util.*;

public abstract class Async<A> {
	
	private volatile boolean interrupted = false;

	protected Async() {
	}

	protected abstract void execInternal(Context context, Object token, Cont<A> cont, Runnable ifFail);
	
	public void interrupt() {
		this.interrupted = true;
		Log.e("async", "interrupted");
	}
	
	protected void resetInterrupt() {
		this.interrupted = false;
	}
	
	public final boolean isInterrupted() {
		return interrupted;
	}

	protected final void runInternal(final Context context, final Object token, Cont<A> cont, final Runnable ifFail) {
		if(interrupted) {
			resetInterrupt();
			Util.runInUiThread(new Runnable(){
				public void run() {
					ifFail.run();
				}});
		} else {
			execInternal(context, token, cont, ifFail);
		}
	}

	public final void exec(final Context context, final Cont<A> cont, final Runnable ifFail, final boolean withDialog) {
		resetInterrupt();
		
		final AsyncListener listener = listener(context);
		final Object token = new Object();
		
		Util.runInUiThread(new Runnable() {
			@Override
			public void run() {
				listener.onAsyncStart(token, Async.this, withDialog);
				
				runInternal(context, token, new Cont<A>() {
					public void apply(final A a) {
						Util.runInUiThread(new Runnable() {
							public void run() {
								cont.apply(a);
								listener.onAsyncEnd(token, Async.this);
							}
						});
					}
				}, new Runnable() {
					public void run() {
						ifFail.run();
						listener(context).onAsyncEnd(token, Async.this);
					}});
			}
		});
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
			private void runBind(A a, Context context, Object token, Cont<B> cont, Runnable ifFail) {
				Async<B> next;
				try {
					next = f(a);
					if(interrupted) next.interrupt();
				} catch (AsyncCancelledException e) {
					Log.d("Async", "Async cancelled at:" + Bind.this.getClass());
					ifFail.run();
					return;
				}
				next.runInternal(context, token, cont, ifFail);
			}
			
			@Override
			public final void interrupt() {
				// recursively interrupt all Asyncs
				super.interrupt(); // the whole Bind composite
				Async.this.interrupt(); // enclosing Async (lhs of >>=)
			}
			
			@Override
			public final void resetInterrupt() {
				super.resetInterrupt();
				Async.this.resetInterrupt();
			}

			@Override
			protected void execInternal(final Context context, final Object token, final Cont<B> cont, final Runnable ifFail) {
				// run enclosing (aka left-hand side of >>=) Async, with a continuation that runs the next Async
				Async.this.runInternal(context, null, new Cont<A>() {
					public void apply(final A a) {
						// after enclosing Async ends, run Bind.f
						if (runInUiThread == null || runInUiThread.booleanValue() == Util.isInUiThread()) {
							// run in the current thread
							runBind(a, context, token, cont, ifFail);
						} else if (runInUiThread) {
							// run in the ui thread
							Util.runInUiThread(new Runnable() {
								public void run() {
									runBind(a, context, token, cont, ifFail);
								}
							});
						} else {
							// run in background
							Util.runInBackground(new Runnable() {
								public void run() {
									runBind(a, context, token, cont, ifFail);
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
