/**
 * 
 */
package org.twentygototen.pipeline.pipes;

import org.twentygototen.pipeline.Callbacks;
import org.twentygototen.pipeline.Callbacks.Callback;
import org.twentygototen.pipeline.Pipeline.Pipe;

final public class PipeTee<I,O> extends AsyncPipe<I, O> {

	private final Pipe<I, O> pipe;
	private final Callbacks.Callback<? super O> callback;

	public PipeTee(Pipe<I, O> pipe, Callbacks.Callback<? super O> callback) {
		this.pipe = pipe;
		this.callback = callback;
	}

	@Override
	public
	void complete() {
		pipe.complete();
	}

	@Override
	public void processAsync(I input, final Callback<? super O> call) {
		pipe.processAsync(input, new Callback<O>() {

			@Override
			public void onComplete() {
				callback.onComplete();
				call.onComplete();
			}

			@Override
			public void onFailure(RuntimeException e) {
				callback.onFailure(e);
				call.onFailure(e);
			}

			@Override
			public void onSuccess(O input) {
				callback.onSuccess(input);
				call.onSuccess(input);
			}
		});
	}

}