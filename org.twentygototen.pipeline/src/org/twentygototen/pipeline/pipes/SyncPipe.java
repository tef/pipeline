/**
 * 
 */
package org.twentygototen.pipeline.pipes;

import org.twentygototen.pipeline.Callbacks;

public abstract class SyncPipe<I,O> extends AbstractPipe<I,O>{

	public abstract O process(I input);	

	public void complete() {};
	
	public final void processAsync(final I input, final Callbacks.Callback<? super O> callback) {
		O value = null;
		try {
			value = process(input);
		} catch (RuntimeException e) {
			callback.onFailure(e);
			return;
		}
		if (value == null) {
			callback.onComplete();
		} else {
			callback.onSuccess(value);
		}
	} 
}


