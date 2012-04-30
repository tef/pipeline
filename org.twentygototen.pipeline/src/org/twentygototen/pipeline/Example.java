/**
 * 
 */
package org.twentygototen.pipeline;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;

import org.twentygototen.pipeline.Pipeline.Block;
import org.twentygototen.pipeline.Pipeline.Pipe;
import org.twentygototen.pipeline.Pipeline.Source;
import org.twentygototen.pipeline.pipes.AsyncPipe;
import org.twentygototen.pipeline.pipes.FileReaderPipe;
import org.twentygototen.pipeline.pipes.PipeList;
import org.twentygototen.pipeline.pipes.SyncPipe;
import org.twentygototen.pipeline.pipes.AggregatePipe.Aggregate;
import org.twentygototen.pipeline.sources.DirectoryList;
import org.twentygototen.pipeline.util.Pair;

class Example {

	static class Mod extends SyncPipe<Integer, Integer> {
		private final int modulus;
		public Mod(int modulus) {
			this.modulus = modulus;
		}
		@Override
		public Integer process(Integer input) {
			return input % modulus;
		}

	}
	public static void main(String[] args) {
		Pipe<Integer, Integer> add1 = new SyncPipe<Integer,Integer>() {
			public Integer process(Integer input) {
				return input+1;
			}
		};

		Pipe<Integer,Integer> a = add1.next(add1).next(add1);

		System.out.println(a.process(1));

		AsyncPipe<Integer, String> tohex = new AsyncPipe<Integer, String>() {
			@Override
			public void processAsync(Integer input, Callbacks.Callback<? super String> callback) {
				callback.onSuccess(Integer.toHexString(input));

			}
		};

		System.out.println(add1.next(add1).next(tohex).process(12));


		PipeList<Integer,Integer> c = Pipeline.asList(a,a,a,a);


		List<Integer> run = c.process(Arrays.asList(1,2,3,4));

		for (int i: run) {
			System.out.println(i);
		}

		Block next = Pipeline.pour(1,2,3,4).next(c).next(c).next(Pipeline.sysout());
		next.run();

		Pipe<List<Integer>, List<Integer>> map = Pipeline.map(add1);
		
		Pipeline.pour(1,2,3,4).next(map).next(c).next(map).tee(Callbacks.syserr()).next(PipeList.tail(Pipeline.map(new Mod(2)))).next(PipeList.head(add1)).next(Pipeline.sysout()).run();

		Source<File> dirlisting = new DirectoryList(new File("."), new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".java");
			}
		});

		Pipe<File, Pair<File,List<String>>> readFile = new FileReaderPipe();

		SyncPipe<List<?>,Integer> wordCount = new SyncPipe<List<?>,Integer>() {
			@Override
			public Integer process(List<?> input) {
				return input.size();
			}
		};


		dirlisting.next(Pipeline.sysout()).run();

		Source<Integer> tee = dirlisting.next(readFile).tee(Callbacks.syserr()).next(Pair.<File,List<String>,Integer>second(wordCount)).tee(Callbacks.syserr()).next(
				Pipeline.aggregate(new Aggregate<Pair<File,Integer>, Integer>() {

					private int total;

					public void aggregate(Pair<File, Integer> input) {
						total += input.second;
					}

					public Integer result() {
						return total;
					}
				})
		).tee(Callbacks.syserr());

		tee.next(Pipeline.sysout()).run();

	}
}