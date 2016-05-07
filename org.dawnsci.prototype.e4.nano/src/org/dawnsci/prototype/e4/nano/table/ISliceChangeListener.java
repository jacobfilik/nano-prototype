package org.dawnsci.prototype.e4.nano.table;

import java.util.EventListener;

public interface ISliceChangeListener extends EventListener {

	public void sliceChanged(SliceChangeEvent event);
	
}
