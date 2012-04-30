/**
 * 
 */
package org.twentygototen.pipeline.pipes;

import org.twentygototen.pipeline.Callbacks;
import org.twentygototen.pipeline.Pipeline;
import org.twentygototen.pipeline.Callbacks.Callback;
import org.twentygototen.pipeline.Pipeline.Sink;
import org.twentygototen.pipeline.Pipeline.Pipe;

public final class IdentityPipe<I> implements Pipe<I,I> {
	
	public I process(I input) {
		return input;
	}
	
	public void complete() {
		
	}

	public void processAsync(I input, Callbacks.Callback<? super I> output) {
		output.onSuccess(input);
	}

	public <O2> Pipe<I, O2> next(final Pipe<? super I, O2> pipe) {
		return new AsyncPipe<I,O2>() {
			@Override
			public void processAsync(I input, Callback<? super O2> callback) {
				pipe.processAsync(input, callback);
			}
		};
	}

	public Sink<I> next(final Sink<? super I> sink) {
		return new Sink<I>() {

			public void consume(I input) {
				sink.consume(input);
			}

			public void complete() {
				sink.complete();
			}
		};
	}

	public Pipe<I, I> tee(Callbacks.Callback<? super I> output) {
		return Pipeline.tee(this, output);
	}
}