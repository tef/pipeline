package org.twentygototen.pipeline.util;

import org.twentygototen.pipeline.Callbacks.Callback;
import org.twentygototen.pipeline.Pipeline.Pipe;
import org.twentygototen.pipeline.pipes.AsyncPipe;


public class Pair<I,O> {
	public final I first;
	public final O second;

	private Pair(I first, O second) {
		this.first = first;
		this.second = second;
	}

	public final String toString() {
		return first + ", "+second;

	}

	final public Pair<O,I> swap() {
		return new Pair<O,I>(second,first);
	}

	public final static <I,O> Pair<I,O> make(I f, O s) {
		return new Pair<I,O>(f,s);
	}

	public final static <A,B,C> Pipe<? super Pair<A, B>,Pair<C, B>> first(final Pipe<? super A,C> arrow) {
		return new AsyncPipe<Pair<A,B>,Pair<C, B>>() {

			@Override
			public void processAsync(final Pair<A, B> inputPair,
					final Callback<? super Pair<C, B>> callback) {
				arrow.processAsync(inputPair.first,  new Callback<C>() {
					@Override
					public void onComplete() {
						arrow.complete();
						callback.onComplete();
					}

					@Override
					public void onFailure(RuntimeException e) {
						callback.onFailure(e);
					}

					@Override
					public void onSuccess(C input) {
						callback.onSuccess(new Pair<C,B>(input, inputPair.second));

					}

				});
			}
		};	
	}

	public final static <A,B,C> Pipe<? super Pair<A, B>,Pair<A, C>> second(final Pipe<? super B,C> arrow) {
		return new AsyncPipe<Pair<A, B>,Pair<A,C>>() {


			@Override
			public void processAsync(final Pair<A, B> inputPair,
					final Callback<? super Pair<A, C>> callback) {
				arrow.processAsync(inputPair.second,  new Callback<C>() {
					@Override
					public void onComplete() {
						arrow.complete();
						callback.onComplete();
					}

					@Override
					public void onFailure(RuntimeException e) {
						callback.onFailure(e);
					}

					@Override
					public void onSuccess(C input) {
						callback.onSuccess(new Pair<A,C>(inputPair.first, input));

					}

				});
			}

		};
	}

}
