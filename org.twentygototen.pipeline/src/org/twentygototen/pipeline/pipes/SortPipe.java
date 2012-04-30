package org.twentygototen.pipeline.pipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortPipe<I extends Comparable<I>> extends SyncPipe<List<I>,List<I>> {

	private final Comparator<I> comp;
	public SortPipe() {
		comp =null;
	}

	public SortPipe(Comparator<I> comp) {
		this.comp = comp;
	}
	@Override
	public List<I> process(List<I> input) {
		ArrayList<I> arrayList = new ArrayList<I>();
		arrayList.addAll(input);
		if (comp == null) {
			Collections.sort(arrayList);
		} else {
			Collections.sort(arrayList, comp);
			
		}
		return arrayList;
	}

}
