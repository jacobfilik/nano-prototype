package org.dawnsci.prototype.nano.model;

import java.util.Map;

import org.dawnsci.prototype.nano.model.table.NDimensions;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.SliceND;

public class PlottableObject {

	

	private IPlotMode mode;
	private Object[] dimensionOptions;
	private SliceND slice;
	private String[] axesNames;
	private ITrace[] cachedTraces;
	
	private NDimensions nDimensions;
	
//	public PlottableObject(IPlotMode mode, Object[] opt, SliceND slice, String[] axesNames) {
//		this.mode = mode;
//		this.dimensionOptions = opt;
//		this.slice = slice;
//		this.axesNames = axesNames;
//	}
	
	public PlottableObject(IPlotMode mode, NDimensions nDimensions) {
		this.mode = mode;
		this.nDimensions = nDimensions;
	}
	
//	public ITrace[] getTraces(DataOptions data, IPlottingSystem<?> system) {
//		try {
//			return mode.buildTraces(data.getData(), slice, dimensionOptions, system);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
	
	public NDimensions getNDimensions() {
		return nDimensions;
	}
	
	public ITrace[] getCachedTraces() {
		return cachedTraces;
	}

	public void setCachedTraces(ITrace[] cachedTraces) {
		this.cachedTraces = cachedTraces;
	}
	
	public IPlotMode getPlotMode(){
		return mode;
	}
}