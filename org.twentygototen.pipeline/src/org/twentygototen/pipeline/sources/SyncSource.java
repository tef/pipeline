/**
 * 
 */
package org.twentygototen.pipeline.sources;

import org.twentygototen.pipeline.Callbacks;
import org.twentygototen.pipeline.Pipeline;
import org.twentygototen.pipeline.Pipeline.Block;
import org.twentygototen.pipeline.Pipeline.Sink;
import org.twentygototen.pipeline.Pipeline.Pipe;
import org.twentygototen.pipeline.Pipeline.Source;

public abstract class SyncSource<O> implements Source<O> {
	abstract public O emit();
	
	public void emitAsync(Callbacks.Callback<? super O> output) {
		O value = null;
		while (true) {
		try {
			value = emit();
		} catch (RuntimeException e) {
			output.onFailure(e);
		}
		if (value == null) break;
			output.onSuccess(value);
		} 
	}
	public Block next(final Sink<? super O> sink) {
		return Pipeline.sink(this, sink);
	}


	public <O2> Source<O2> next(Pipe<? super O, O2> arrow) {
		return new SourceWithPipe<O,O2>(this, arrow);
	}
	final public Source<O> tee(Callbacks.Callback<? super O> output) {
		return new SourceTee<O>(this, output);
	}
	
}