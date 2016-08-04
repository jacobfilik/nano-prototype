package org.dawnsci.prototype.nano.model.table;

import java.util.EventObject;

import org.eclipse.january.dataset.SliceND;


public class SliceChangeEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;
	private NDimensions nDimension;

	public SliceChangeEvent(NDimensions nDimensions) {
		super(nDimensions);
		this.nDimension = nDimensions;
	}

	public String[] getAxesNames() {
		return nDimension.buildAxesNames();
	}

	public Object[] getOptions() {
		return nDimension.getOptions();
	}

	public SliceND getSlice() {
		return  nDimension.buildSliceND();
	}
	
	public NDimensions getSource() {
		return nDimension;
	}

}
