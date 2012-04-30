/**
 * 
 */
package org.twentygototen.pipeline.sources;

import org.twentygototen.pipeline.Callbacks;
import org.twentygototen.pipeline.Pipeline;
import org.twentygototen.pipeline.Callbacks.Callback;
import org.twentygototen.pipeline.Pipeline.Block;
import org.twentygototen.pipeline.Pipeline.Sink;
import org.twentygototen.pipeline.Pipeline.Pipe;
import org.twentygototen.pipeline.Pipeline.Source;

class SourceWithPipe<I,O> implements Source<O> {
	private final Pipe<? super I, O> pipe;
	private final Source<? extends I> source;

	public SourceWithPipe(Source<? extends I> source, Pipe<? super I, O> pipe) {
		this.source = source;
		this.pipe = pipe;
	}


	public O emit() {
		I produce = null;
		try {
			produce = source.emit();
		} catch (RuntimeException e) {
			throw e;
		}
		if (produce == null) {
			pipe.complete();
			return null;
		} else {
			return pipe.process(produce);
		}
	}
	public void emitAsync(final Callbacks.Callback<? super O> output) {
		source.emitAsync(new Callback<I>() {

			@Override
			public void onComplete() {
				pipe.complete();
				output.onComplete();
			}

			@Override
			public void onFailure(RuntimeException e) {
				output.onFailure(e);
			}

			@Override
			public void onSuccess(I input) {
				pipe.processAsync(input, output);
			}
		}
		);
	}

public <O2> Source<O2> next(Pipe<? super O, O2> arrow) {
	return new SourceWithPipe<I,O2>(source, this.pipe.next(arrow));
}

public Block next(Sink<? super O> sink) {
	return Pipeline.sink(source, pipe.next(sink));
}

public Source<O> tee(Callbacks.Callback<? super O> output) {
	return new SourceWithPipe<I,O>(source, Pipeline.tee(pipe, output));
}
}