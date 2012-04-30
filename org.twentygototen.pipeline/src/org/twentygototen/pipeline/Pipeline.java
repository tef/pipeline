package org.twentygototen.pipeline;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.twentygototen.pipeline.Callbacks.Callback;
import org.twentygototen.pipeline.pipes.AggregatePipe;
import org.twentygototen.pipeline.pipes.IdentityPipe;
import org.twentygototen.pipeline.pipes.JoinedPipe;
import org.twentygototen.pipeline.pipes.PipeList;
import org.twentygototen.pipeline.pipes.PipeTee;
import org.twentygototen.pipeline.pipes.SyncPipe;
import org.twentygototen.pipeline.pipes.AggregatePipe.Aggregate;
import org.twentygototen.pipeline.sinks.PrintStreamSink;
import org.twentygototen.pipeline.sources.SyncSource;
import org.twentygototen.pipeline.util.Pair;


public final class Pipeline {

	
	/* arrows take an input and an output
	 * and can be combined to form processing chains.
	 */
	
	public interface Pipe<I,O> {
		// it can be run normally
		O process(I input);
		// or run with a sink to call with the result
		void processAsync(I input, Callback<? super O> callback);
		
		// if you combine it with an arrow, you get a new arrow with the outer types
		<O2> Pipe<I,O2> next(final Pipe<? super O,O2> pipe);		
		
		// if you combine it with a sink, you get a new sink that takes the arrows input
		Sink<I> next(final Sink<? super O> sink);	
		
		// you can also tee (copy) the output to a callback
		Pipe<I,O> tee(Callback<? super O> callback);
		
		// and you can signal the end of the source.
		void complete();
	}
	
	public interface Source<O> {
		// it can be run to produce a value, returning null on finish.
		O emit();
		// or run with a sink to write to
		void emitAsync(Callback<? super O> callback);
		// when combined with an arrow, it produces a new source
		<O2> Source<O2> next(final Pipe<? super O,O2> pipe);		
		// and with a sink it makes a complete runnable.
		Block next(final Sink<? super O> sink);
		
		Source<O> tee(Callback<? super O> callback);
	}

	
	public interface Sink<I> {
		public void consume(final I input);
		public void complete();
	}
	
	public interface Block extends Runnable {
	};
	
	
	
	public static <I> Pipe<I,I> identity() {
		return new IdentityPipe<I>();
	}
	
	static <O> Source<List<O>> pour(final O... object) {
		return pour(Arrays.asList(object));
	}
	
	
	static <O> Source<O> pour(final O object) {
		return new SyncSource<O>() {
			AtomicBoolean done = new AtomicBoolean(false);
			@Override
			public O emit() {
				if (done.compareAndSet(false, true)) {
					return object;					
				}
				return null;
			}
			
		};
	}
	
	
	@SuppressWarnings("unchecked")
	static <I,O> PipeList<I,O> asList(Pipe... arrows) {
		PipeList<I,O> arrowList = new PipeList<I, O>();
		for (Pipe<I, O> arrow: arrows) {
			arrowList.add(arrow);
		}
		return arrowList;
	}
	static <I,O> PipeList<I,O> asList(List<? extends Pipe<I, O>> arrows) {
		PipeList<I, O> arrowList = new PipeList<I, O>();
		arrowList.addAll(arrows);
		return arrowList;
	}

	
	
	public static Sink<Object> sysout() {
		return new PrintStreamSink(System.out);
	}

	public static Sink<Object> syserr() {
		return new PrintStreamSink(System.err);
	}
	
	public static <I,O,O2> Pipe<I, O2> next(final Pipe<I, O> first, final Pipe<? super O, O2> second) {
		return new JoinedPipe<I, O2, O>(first, second);
	}

	public static final <I,O> Pipe<I,O> tee(Pipe<I,O> arrow, final Callback<? super O> callback) {
		return new PipeTee<I,O>(arrow, callback);
	}

	public static final <I,O> Pipe<List<I>,List<O>> map(final Pipe<I,O> pipe) {
		return new SyncPipe<List<I>,List<O>>() {

			@Override
			public List<O> process(List<I> input) {
				List<O> out = new ArrayList<O>();
				for (I i: input) {
					out.add(pipe.process(i));
				}
				return out;
			}
		};
	}

	public static <O> Block sink(final Source<O> source, final Sink<? super O> sink) {
		return new Block() {
			public void run() {
				source.emitAsync(new Callbacks.SinkCallback<O>(sink));
			}
		};
	}

	public static <I,O> Pipe<? super Pair<I,O>,Pair<O,I>> swap() {
		return new SyncPipe<Pair<I,O>,Pair<O,I>>() {

			@Override
			public Pair<O, I> process(Pair<I, O> input) {
				return  input.swap();
			}};
	}
	static <I,O> Pipe<I,O> aggregate(Aggregate<I, O> agg) {
		return new AggregatePipe<I, O>(agg);
	}

}
