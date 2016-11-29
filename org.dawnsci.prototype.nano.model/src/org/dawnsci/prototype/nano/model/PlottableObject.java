package org.dawnsci.prototype.nano.model;

import org.dawnsci.prototype.nano.model.table.NDimensions;

public class PlottableObject {

	private IPlotMode mode;
	private NDimensions nDimensions;
	
	public PlottableObject(IPlotMode mode, NDimensions nDimensions) {
		this.mode = mode;
		this.nDimensions = nDimensions;
	}
	
	public NDimensions getNDimensions() {
		return nDimensions;
	}
	
	public IPlotMode getPlotMode(){
		return mode;
	}
}
