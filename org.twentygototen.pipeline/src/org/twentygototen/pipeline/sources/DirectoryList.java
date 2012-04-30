/**
 * 
 */
package org.twentygototen.pipeline.sources;

import java.io.File;
import java.io.FileFilter;

import org.twentygototen.pipeline.Callbacks.Callback;

final public class DirectoryList extends AsyncSource<File> {
	private final File dir;
	private final FileFilter fileFilter;

	public DirectoryList(File dir) {
		this.dir = dir;
		fileFilter = null;
	}

	public DirectoryList(File dir, FileFilter fileFilter) {
		this.dir = dir;
		this.fileFilter = fileFilter;
	}

	public void emitAsync(Callback<? super File> output) {
		if (dir.isDirectory()) {
			walkTree(dir, output);					
		} else {
			output.onSuccess(dir);
		}
		output.onComplete();
		
	}

	public void walkTree(File dir, Callback<? super File> output) {
		for (File file: dir.listFiles()) {
			if (file.isDirectory()) {
				walkTree(file, output);
			} else {
				if (fileFilter.accept(file)) {
					output.onSuccess(file);					
				}
			}
		}
	}
}