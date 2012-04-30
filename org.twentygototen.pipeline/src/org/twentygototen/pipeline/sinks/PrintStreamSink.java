/**
 * 
 */
package org.twentygototen.pipeline.sinks;

import java.io.PrintStream;

import org.twentygototen.pipeline.Pipeline.Sink;


public class PrintStreamSink implements Sink<Object> {
	private final PrintStream out;

	public PrintStreamSink(PrintStream out) {
		this.out = out;
		
	}

	public void consume(Object input) {
		out.println(input);
	}

	public void complete() {
	}
	
}