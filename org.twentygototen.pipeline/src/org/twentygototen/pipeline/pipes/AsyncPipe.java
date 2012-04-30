/**
 * 
 */
package org.twentygototen.pipeline.pipes;

import org.twentygototen.pipeline.Callbacks;
import org.twentygototen.pipeline.Callbacks.FutureResult;

public abstract class AsyncPipe<I,O> extends AbstractPipe<I,O>{
	final public O process(final I input) {
		final FutureResult<O> output = new FutureResult<O>();
		processAsync(input, output);			
		return output.get();
	}

	public abstract void processAsync(I input, Callbacks.Callback<? super O> callback);
	public void complete() {};
}