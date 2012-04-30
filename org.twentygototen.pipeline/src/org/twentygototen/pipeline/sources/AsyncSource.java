/**
 * 
 */
package org.twentygototen.pipeline.sources;

import org.twentygototen.pipeline.Callbacks;
import org.twentygototen.pipeline.Pipeline;
import org.twentygototen.pipeline.Callbacks.FutureResult;
import org.twentygototen.pipeline.Pipeline.Block;
import org.twentygototen.pipeline.Pipeline.Pipe;
import org.twentygototen.pipeline.Pipeline.Sink;
import org.twentygototen.pipeline.Pipeline.Source;

public abstract class AsyncSource<O> implements Source<O> {
	abstract public void emitAsync(Callbacks.Callback<? super O> output);

	public O emit() {
		FutureResult<O> result = new FutureResult<O>();
		emitAsync(result);
		return result.get();
	}
	public Block next(final Sink<? super O> sink) {
		return Pipeline.sink(this,sink);
	}

	public <O2> Source<O2> next(Pipe<? super O, O2> arrow) {
		return new SourceWithPipe<O,O2>(this, arrow);
	}
	final public Source<O> tee(Callbacks.Callback<? super O> output) {
		return new SourceTee<O>(this, output);
	}
	
}