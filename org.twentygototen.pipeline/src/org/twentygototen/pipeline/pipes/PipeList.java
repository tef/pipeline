/**
 * 
 */
package org.twentygototen.pipeline.pipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.twentygototen.pipeline.Callbacks;
import org.twentygototen.pipeline.Callbacks.Callback;
import org.twentygototen.pipeline.Pipeline.Pipe;

public class PipeList<I,O> extends AsyncPipe<List<I>, List<O>>  implements List<Pipe<I, O>> {
	private final List<Pipe<I,O>> pipes = new ArrayList<Pipe<I,O>>();

	public final static <A> Pipe<List<A>,List<A>> head(final Pipe<? super A, A> pipe) {
		return new AsyncPipe<List<A>, List<A>>() {
			@Override
			public void processAsync(final List<A> input,
					final Callback<? super List<A>> callback) {
				
				pipe.processAsync(input.get(0), new Callback<A>(){
					@Override
					public void onComplete() {
						pipe.complete();
						callback.onComplete();
					}

					@Override
					public void onFailure(RuntimeException e) {
						callback.onFailure(e);
					}

					@Override
					public void onSuccess(A i) {
						ArrayList<A> out = new ArrayList<A>();
						out.add(i);
						out.addAll(input.subList(1, input.size()));
						callback.onSuccess(out);
					}});
			}

		};
		
	}

	
	public final static <A> Pipe<List<A>,List<A>> tail(final Pipe<List<A>, List<A>> pipe) {
		return new AsyncPipe<List<A>, List<A>>() {

			@Override
			public void processAsync(final List<A> input,
					final Callback<? super List<A>> callback) {
				
				pipe.processAsync(input.subList(1, input.size()), new Callback<List<A>>(){
					@Override
					public void onComplete() {
						pipe.complete();
						callback.onComplete();
					}

					@Override
					public void onFailure(RuntimeException e) {
						callback.onFailure(e);
					}

					@Override
					public void onSuccess(List<A> i) {
						ArrayList<A> out = new ArrayList<A>();
						out.add(input.get(0));
						out.addAll(i);
						callback.onSuccess(out);
					}});
			}
		};
		
	}
	@Override public void processAsync(List<I> input, final Callbacks.Callback<? super List<O>> callback) {
		if (input.size() != pipes.size()) throw new IndexOutOfBoundsException();
		
		final AtomicInteger count = new AtomicInteger(pipes.size());
		final ArrayList<O> output = new ArrayList<O>();


		for (int i = 0; i < pipes.size(); i++ ){
			output.add(null);
			final int j = i;
			pipes.get(i).processAsync(input.get(j), new Callbacks.Callback<O>() {
				public void onFailure(RuntimeException failure) {
					count.set(-1);
					callback.onFailure(failure);
				}

				public void onSuccess(O input) {
					int c = count.decrementAndGet();
					output.set(j, input);
					if (c == 0) callback.onSuccess(output); 
				}

				@Override
				public void onComplete() {
					throw new RuntimeException();
				}

			});
		}
	}

	public void complete() {
		for(Pipe<I, O> p : pipes) {
			p.complete();
		}
	}
	public boolean add(Pipe<I, O> o) {	return pipes.add(o);}
	public void add(int index, Pipe<I, O> element) { pipes.add(index, element);}
	public boolean addAll(Collection<? extends Pipe<I, O>> c) { return addAll(c);}
	public boolean addAll(int index, Collection<? extends Pipe<I, O>> c) { return addAll(index,c);}

	public void clear() {pipes.clear();}

	public boolean contains(Object o) { return pipes.contains(o);}

	public boolean containsAll(Collection<?> c) { return pipes.contains(c);}

	public Pipe<I, O> get(int index) {return pipes.get(index);}

	public int indexOf(Object o) {return pipes.indexOf(o);}

	public boolean isEmpty() {
		return pipes.isEmpty();
	}

	public Iterator<Pipe<I, O>> iterator() {
		return pipes.iterator();
	}

	public int lastIndexOf(Object o) {
		return pipes.lastIndexOf(o);
	}

	public ListIterator<Pipe<I, O>> listIterator() {
		return pipes.listIterator();
	}

	public ListIterator<Pipe<I, O>> listIterator(int index) {
		return pipes.listIterator(index);
	}

	public boolean remove(Object o) {
		return pipes.remove(o);
	}

	public Pipe<I, O> remove(int index) {
		return pipes.remove(index);
	}

	public boolean removeAll(Collection<?> c) {
		return pipes.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return pipes.retainAll(c);
	}

	public Pipe<I, O> set(int index, Pipe<I, O> element) {
		return pipes.set(index, element);
	}

	public int size() {
		return pipes.size();
	}

	public List<Pipe<I, O>> subList(int fromIndex, int toIndex) {
		return pipes.subList(fromIndex, toIndex);
	}

	public Object[] toArray() {
		return pipes.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return pipes.toArray(a);
	}
}