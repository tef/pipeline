/**
 * 
 */
package org.twentygototen.pipeline.pipes;

import org.twentygototen.pipeline.Callbacks.Callback;

final public class AggregatePipe<I,O> extends
		AsyncPipe<I, O> {
	private Callback<? super O> callback;
	private final Aggregate<I, O> agg;

	public interface Aggregate<I,O> {
		void aggregate(I input);
		O result();
	}

	public AggregatePipe(Aggregate<I, O> agg) {
		this.agg = agg;
	}

	public void complete() {
		callback.onSuccess(agg.result());
		callback.onComplete();
	}
	@Override
	public void processAsync(I input, Callback<? super O> callback) {
		agg.aggregate(input);
		this.callback=callback;
		
	}
}