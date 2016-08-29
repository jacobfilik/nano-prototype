package org.dawnsci.prototype.nano.model;

import org.dawnsci.prototype.nano.model.table.NDimensions;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.SliceND;

public class PlotManager {
	
	private IPlottingService pService;
	private IPlottingSystem system;
	
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
	
	private IPlottingSystem getPlottingSystem() {
		if (system == null) {
			system = pService.getPlottingSystem("Plot");
		}
		return system;
	}
	
	public void resetPlot(){
		getPlottingSystem().reset();
	}

	public IPlotMode getCurrentMode() {
		return currentMode;
	}

	public void setCurrentMode(IPlotMode currentMode) {
		this.currentMode = currentMode;
	}
	
	public void updatePlot(NDimensions nd) {
		String[] axes = nd.buildAxesNames();
		SliceND slice= nd.buildSliceND();
		Object[] options = nd.getOptions();
		PlottableObject pO = getDataOption().getPlottableObject();
		if (pO != null){
			for (ITrace t  : pO.getCachedTraces())
			getPlottingSystem().removeTrace(t);
		}
		getDataOption().setAxes(axes);
		
		SourceInformation si = new SourceInformation(getDataOption().getFileName(), getDataOption().getName(), getDataOption().getData());
		SliceInformation s = new SliceInformation(slice, slice, new SliceND(getDataOption().getData().getShape()), new int[]{0,1}, 1, 0);
		SliceFromSeriesMetadata md = new SliceFromSeriesMetadata(si, s);
		ITrace[] t = null;
		try {
			t = getCurrentMode().buildTraces(getDataOption().getData(),
					slice, options, getPlottingSystem());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (t == null) return;
		
		for (ITrace trace : t) {
			trace.getData().setMetadata(md);
			if (trace instanceof ISurfaceTrace) {
				getPlottingSystem().setPlotType(PlotType.SURFACE);
			}
			getPlottingSystem().addTrace(trace);
		}
		
		
		getPlottingSystem().repaint();
		PlottableObject po = new PlottableObject(getCurrentMode(), nd);
		po.setCachedTraces(t);
				
		getDataOption().setPlottableObject(po);
	}

}
