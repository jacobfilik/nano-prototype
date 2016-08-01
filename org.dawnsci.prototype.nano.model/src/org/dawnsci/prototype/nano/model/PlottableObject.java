package org.dawnsci.prototype.nano.model;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.SliceND;

public class PlottableObject {

	private IPlotMode mode;
	private Object[] dimensionOptions;
	private SliceND slice;
	private String[] axesNames;
	private ITrace cachedTrace;
	
	public PlottableObject(IPlotMode mode, Object[] opt, SliceND slice, String[] axesNames) {
		this.mode = mode;
		this.dimensionOptions = opt;
		this.slice = slice;
		this.axesNames = axesNames;
	}
	
	public ITrace[] getTraces(DataOptions data, IPlottingSystem<?> system) {
		try {
			return mode.buildTraces(data.getData(), slice, dimensionOptions, system);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
