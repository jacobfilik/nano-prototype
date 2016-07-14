package org.dawnsci.prototype.nano.model.table;

import java.util.EventListener;

public interface ISliceChangeListener extends EventListener {

	public void sliceChanged(SliceChangeEvent event);
	
}
