package org.twentygototen.pipeline.sources;

import org.twentygototen.pipeline.Callbacks;
import org.twentygototen.pipeline.Pipeline;
import org.twentygototen.pipeline.Pipeline.Block;
import org.twentygototen.pipeline.Pipeline.Sink;
import org.twentygototen.pipeline.Pipeline.Pipe;
import org.twentygototen.pipeline.Pipeline.Source;


public class SourceTee<O> implements Source<O> {

	private final Source<O> source;
	private final Callbacks.Callback<? super O> callback;

	public SourceTee(Source<O> source, Callbacks.Callback<? super O> output) {
		this.source = source;
		this.callback = output;
	}

	public <O2> Source<O2> next(Pipe<? super O, O2> arrow) {
		return new SourceWithPipe<O,O2>(source, arrow);
	}

	public Block next(final Sink<? super O> sink) {
		return Pipeline.sink(this, sink);
	}

	public void emitAsync(final Callbacks.Callback<? super O> output) {
		source.emitAsync(new Callbacks.Callback<O>() {

			public void onSuccess(O input) {
				callback.onSuccess(input);
				output.onSuccess(input);
			}

			public void onFailure(RuntimeException e) {
				callback.onFailure(e);
				output.onFailure(e);
			}

			@Override
			public void onComplete() {
				callback.onComplete();
				output.onComplete();
			}
		});
	}

	public Source<O> tee(Callbacks.Callback<? super O> output) {
		return new SourceTee<O>(this, output);
	}

	public O emit() {
		O produce = null;
		try {
			produce = source.emit();
		} catch (RuntimeException e) {
			callback.onFailure(e);
			throw e;
		}
		if (produce == null) {
			callback.onComplete();
		} else {
			callback.onSuccess(produce);
		}
		return produce;
	}

}
