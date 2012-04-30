/**
 * 
 */
package org.twentygototen.pipeline.pipes;

import org.twentygototen.pipeline.Callbacks.Callback;
import org.twentygototen.pipeline.Pipeline.Pipe;

final public class JoinedPipe<I, O2, O> extends AsyncPipe<I, O2> {
	private final Pipe<I, O> first;
	private final Pipe<? super O, O2> second;

	public JoinedPipe(Pipe<I, O> first, Pipe<? super O, O2> second) {
		this.first = first;
		this.second = second;
	}

	@Override public void processAsync(I input, final Callback<? super O2> callback) {
		first.processAsync(input,new Callback<O>() {
			public void onSuccess(O input) {
				second.processAsync(input, callback);
			}

			public void onFailure(RuntimeException e) {
				callback.onFailure(e);
			}

			@Override
			public void onComplete() {
				second.complete();
			}
		});
	}

	@Override
	public void complete() {
		first.complete();
		second.complete();
	}
}