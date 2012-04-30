/**
 * 
 */
package org.twentygototen.pipeline.pipes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.twentygototen.pipeline.util.Pair;

final public class FileReaderPipe extends SyncPipe<File, Pair<File, List<String>>> {
	@Override
	final public Pair<File,List<String>> process(File input) {
		List<String> out =  new ArrayList<String>();
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(input));
			String line = bufferedReader.readLine();
			while (line != null) {
				out.add(line);
				line = bufferedReader.readLine();
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return Pair.make(input,out);
	}
}