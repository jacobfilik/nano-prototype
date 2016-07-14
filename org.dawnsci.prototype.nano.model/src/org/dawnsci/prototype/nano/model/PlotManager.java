package org.dawnsci.prototype.nano.model;

import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;

public class PlotManager {
	
	IPlottingService pService;
	
	private DataOptions currentOptions; 
	private IPlotMode[] modes = new IPlotMode[]{new PlotModeXY(), new PlotModeImage(), new PlotModeSurface()};
	private IPlotMode currentMode;
	
	public PlotManager(IPlottingService p) {
		this.pService = p;
		setCurrentMode(modes[0]);
	}
	
	public void setDataOption(DataOptions dataOp) {
		this.currentOptions = dataOp;
	}
	
	public DataOptions getDataOption() {
		return currentOptions;
	}
	
	public IPlotMode[] getPlotModes() {
		return modes;
	}
	
	public IPlottingSystem getPlottingSystem() {
		return pService.getPlottingSystem("Plot");
	}

	public IPlotMode getCurrentMode() {
		return currentMode;
	}

	public void setCurrentMode(IPlotMode currentMode) {
		this.currentMode = currentMode;
	}

}
