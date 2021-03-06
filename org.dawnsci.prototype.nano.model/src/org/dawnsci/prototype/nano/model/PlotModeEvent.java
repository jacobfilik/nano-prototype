package org.dawnsci.prototype.nano.model;

import java.util.EventObject;

public class PlotModeEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	private IPlotMode mode;
	private IPlotMode[] possibleModes;

	public PlotModeEvent(PlotController source, IPlotMode mode, IPlotMode[] possibleModes) {
		super(source);
		
		this.mode = mode;
		this.possibleModes = possibleModes;

	}
	
	public IPlotMode getMode() {
		return mode;
	}

	public IPlotMode[] getPossibleModes() {
		return possibleModes;
	}

}
