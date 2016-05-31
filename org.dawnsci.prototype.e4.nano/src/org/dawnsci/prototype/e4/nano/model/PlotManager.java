package org.dawnsci.prototype.e4.nano.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;

public class PlotManager {
	
	IPlottingService pService;
	
	private DataOptions currentOptions; 
	private IPlotMode[] modes = new IPlotMode[]{new PlotModeXY(), new PlotModeImage()};
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
