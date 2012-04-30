package org.twentygototen.pipeline;

import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.twentygototen.pipeline.Pipeline.Sink;



public final class Callbacks {

	static public abstract class Callback<I> {
		abstract public void onSuccess(final I input);
		abstract public void onFailure(final RuntimeException e);
		abstract public void onComplete();
	}

	final public static Callback<Object> NOOP = new Callback<Object>(){

		@Override
		public void onFailure(RuntimeException e) {}

		@Override
		public void onSuccess(Object input) {}

		@Override
		public void onComplete() {
			
		}
		
	};
	static public final class FutureResult<O> extends Callback<O> {
		final BlockingQueue<O> queue = new LinkedBlockingQueue<O>();
		final AtomicReference<RuntimeException> error = new AtomicReference<RuntimeException>();
		final AtomicBoolean ended = new AtomicBoolean(false);

		public O get() {
			O result = null;
			while(result == null && !ended.get()) {
				try {
					result = queue.poll(100, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
				}
			}
			
			if (error.get() != null) throw error.get();
			return result;
		}		

		public void onSuccess(O input) {
			queue.add(input);
			synchronized (queue) {
				queue.notifyAll();
			}
		}

		public void onFailure(RuntimeException failure) {
			error.set(failure);
			ended.set(true);
			synchronized (queue) {
				queue.notifyAll();
			}

		}

		@Override
		public void onComplete() {
			ended.set(true);
			synchronized (queue) {
				queue.notifyAll();
			}
		}
	}
	
	static public final class SinkCallback<O> extends Callback<O> {

		private final Sink<? super O> sink;

		public SinkCallback(Sink<? super O> sink) {
			this.sink = sink;
		}

		@Override
		public void onFailure(RuntimeException e) {
			throw e;
		}

		@Override
		public void onSuccess(O input) {
			sink.consume(input);
		}

		@Override
		public void onComplete() {
			sink.complete();
		}
	}
	
	static public final class PrintCallback<O> extends Callback<O> {

		private PrintStream print;

		public PrintCallback(PrintStream out) {
			this.print = out;
		}

		@Override
		public void onFailure(RuntimeException e) {
			throw e;
		}

		@Override
		public void onSuccess(O input) {
			print.println(input);
		}

		@Override
		public void onComplete() {
			
		}
	}
	
	static <O> Callback<O> sysout() {
		return new PrintCallback<O>(System.out);
	}
	static <O> Callback<O> syserr() {
		return new PrintCallback<O>(System.err);
	}
		
}
