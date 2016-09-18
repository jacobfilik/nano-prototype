package org.dawnsci.prototype.nano.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.jface.viewers.StructuredSelection;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class PlotManager {
	
	private IPlottingService pService;
	private IPlottingSystem system;
	private EventAdmin eventAdmin;

	private IPlotMode[] modes = new IPlotMode[]{new PlotModeXY(), new PlotModeImage(), new PlotModeSurface()};
	private IPlotMode currentMode;
	
	private LoadedFile currentFile;
	private DataOptions currentOption;
	
	public PlotManager(IPlottingService p, EventAdmin eventAdmin) {
		this.pService = p;
		this.eventAdmin = eventAdmin;
		setCurrentMode(modes[0]);
	}
	
	public void setCurrentFile(LoadedFile file) {
		currentFile = file;
		switchFile();
	}
	
	public void setCurrentData(DataOptions data) {
		currentOption = data;
	}
	
//	public void setDataOption(DataOptions dataOp) {
//		this.currentOptions = dataOp;
//	}
//	
//	public DataOptions getDataOption() {
//		return currentOptions;
//	}
	
	public IPlotMode[] getPlotModes() {
		return modes;
	}
	
	public void removeFromPlot(PlottableObject po) {
		if (po == null) return;
		if (getPlottingSystem() == null) return;
		IPlottingSystem s = getPlottingSystem();
		if (po.getCachedTraces() != null) {
			ITrace[] cachedTraces = po.getCachedTraces();
			for (ITrace t : cachedTraces) s.removeTrace(t);
		}
	}
	
	public void addToPlot(PlottableObject po) {
		if (po == null) return;
		if (getPlottingSystem() == null) return;
		IPlottingSystem s = getPlottingSystem();
		if (po.getCachedTraces() != null) {
			ITrace[] cachedTraces = po.getCachedTraces();
			for (ITrace t : cachedTraces) s.addTrace(t);
			s.repaint();
		}
	}
	
	private IPlottingSystem getPlottingSystem() {
		if (system == null) {
			system = pService.getPlottingSystem("Plot");
		}
		return system;
	}
	

	public IPlotMode getCurrentMode() {
		return currentMode;
	}

	public void setCurrentMode(IPlotMode currentMode) {
		this.currentMode = currentMode;
	}
	
	public void updatePlot(NDimensions nd, DataOptions dataOp) {
		
		if (!currentMode.supportsMultiple()) {
			Map<String,String> props = new HashMap<String,String>();
			props.put("path", dataOp.getFileName());
			eventAdmin.sendEvent(new Event("orgdawnsciprototypeplotupdate", props));
		}
		
		String[] axes = nd.buildAxesNames();
		SliceND slice= nd.buildSliceND();
		Object[] options = nd.getOptions();
		PlottableObject pO = dataOp.getPlottableObject();
		if (pO != null){
			for (ITrace t  : pO.getCachedTraces())
			getPlottingSystem().removeTrace(t);
		}
		dataOp.setAxes(axes);
		
		SourceInformation si = new SourceInformation(dataOp.getFileName(), dataOp.getName(), dataOp.getData());
		SliceInformation s = new SliceInformation(slice, slice, new SliceND(dataOp.getData().getShape()), new int[]{0,1}, 1, 0);
		SliceFromSeriesMetadata md = new SliceFromSeriesMetadata(si, s);
		ITrace[] t = null;
		try {
			t = getCurrentMode().buildTraces(dataOp.getData(),
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
				
		dataOp.setPlottableObject(po);
	}
	
	public void switchFile(){
		
		List<DataOptions> dataOptions = currentFile.getDataOptions();
		
		if (!currentFile.isSelected()){
			for (DataOptions op : dataOptions) {
				removeFromPlot(op.getPlottableObject());
			}
		}
		
		PlottableObject selected = null;
		for (DataOptions op : dataOptions) {
			if (op.getPlottableObject() != null) {
				PlottableObject po = op.getPlottableObject();
				if (op.isSelected() && currentFile.isSelected() && po != null) {
					addToPlot(po);
				}
				
				if (selected == null) {
					currentOption = op;
//					NDimensions nd = po.getNDimensions();
//					optionsViewer.setSelection(new StructuredSelection(po.getPlotMode()));
//					table.setInput(po.getNDimensions());
					
//					update(nd);
				}
			}
		}
	}
	
	public DataOptions getCurrentDataOption(){
		return currentOption;
	}
	
	public List<DataOptions> getSelectedDataOptions(){
		
		List<DataOptions> checked = new ArrayList<>();
		
		for (DataOptions op : currentFile.getDataOptions()) {
			if (op.getPlottableObject() != null) {
				PlottableObject po = op.getPlottableObject();
				if (op.isSelected() && currentFile.isSelected() && po != null) {
					addToPlot(po);
				}
//				if (selected == null) {
//					selected = po;
//					po.getNDimensions().addSliceListener(listener);
			}
		}
		return checked;
	}

}
