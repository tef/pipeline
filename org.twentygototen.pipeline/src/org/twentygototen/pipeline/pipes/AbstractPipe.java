package org.twentygototen.pipeline.pipes;


import org.twentygototen.pipeline.Callbacks;
import org.twentygototen.pipeline.Pipeline;
import org.twentygototen.pipeline.Callbacks.Callback;
import org.twentygototen.pipeline.Pipeline.Pipe;
import org.twentygototen.pipeline.Pipeline.Sink;



public abstract class AbstractPipe<I,O> implements Pipe<I,O> {
	final public <O2> Pipe<I,O2> next(final Pipe<? super O,O2> p) {
		return Pipeline.next(this, p);
	}
	
	final public Sink<I> next(final Sink<? super O> sink) {
		return new Sink<I>() {
			public void consume(I input) {
				AbstractPipe.this.processAsync(input, new Callback<O>() {

					@Override
					public void onComplete() {
						sink.complete();
					}

					@Override
					public void onFailure(RuntimeException e) {
						throw e;
					}

					@Override
					public void onSuccess(O input) {
						sink.consume(input);
					}
				});
			}

			public void complete() {
				AbstractPipe.this.complete();
				sink.complete();
			}
		};
	}
	public final Pipe<I,O> tee (Callbacks.Callback<? super O> callback) {
		return Pipeline.tee(this, callback);
	}
	

}
