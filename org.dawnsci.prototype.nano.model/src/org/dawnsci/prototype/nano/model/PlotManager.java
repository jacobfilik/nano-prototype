package org.dawnsci.prototype.nano.model;

import java.util.ArrayList;
import java.util.Collection;
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
import org.eclipse.january.dataset.ILazyDataset;
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
	
	private FileController fileController = FileController.getInstance();
	
	public PlotManager(IPlottingService p, EventAdmin eventAdmin) {
		this.pService = p;
		this.eventAdmin = eventAdmin;
		setCurrentMode(modes[0]);
	}
	
	public IPlotMode[] getPlotModes() {
		return modes;
	}
	
	public void removeFromPlot(PlottableObject po) {
		if (po == null) return;
		if (getPlottingSystem() == null) return;
		IPlottingSystem s = getPlottingSystem();
//		if (!po.getPlotMode().supportsMultiple()) po.setCachedTraces(null);
		if (po.getCachedTraces() != null) {
			ITrace[] cachedTraces = po.getCachedTraces();
			for (ITrace t : cachedTraces) {
				Collection<ITrace> traces = s.getTraces();
				if (s.getTraces().contains(t)) s.removeTrace(t);
			}
			
			if (!currentMode.supportsMultiple()) po.setCachedTraces(null);
			
			s.repaint();
		}
	}
	
	public void addToPlot(PlottableObject po) {
		if (po == null) return;
		if (getPlottingSystem() == null) return;
		IPlottingSystem s = getPlottingSystem();
		if (po.getCachedTraces() != null) {
			
			
			for (DataOptions dataOps : fileController.getCurrentFile().getDataOptions()) {
				if (dataOps.getPlottableObject() == null || fileController.getCurrentDataOption().getPlottableObject().getPlotMode() != dataOps.getPlottableObject().getPlotMode()) {
					dataOps.setSelected(false);
					removeFromPlot(dataOps.getPlottableObject());
				}
				
			}
			
			Collection<ITrace> traces = s.getTraces();
			ITrace[] cachedTraces = po.getCachedTraces();
			for (ITrace t : cachedTraces) if (!s.getTraces().contains(t)) s.addTrace(t);
			s.repaint();
			
		} else {
			updatePlot(po.getNDimensions(), fileController.getCurrentDataOption());
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
		if (getPlottingSystem() != null)getPlottingSystem().clear();
	}
	
	public void updatePlot(NDimensions nd, DataOptions dataOp) {
		
		if (!currentMode.supportsMultiple()) {
			Map<String,String> props = new HashMap<String,String>();
			props.put("path", dataOp.getFileName());
			eventAdmin.sendEvent(new Event("orgdawnsciprototypeplotupdate", props));
			for (DataOptions dataOps : fileController.getCurrentFile().getDataOptions()) {
				if (dataOp != dataOps) dataOps.setSelected(false);
				
			}
		} else {
//			for (DataOptions dataOps : fileController.getCurrentFile().getDataOptions()) {
//				if (dataOps.getPlottableObject() == null || 
//						dataOp.getPlottableObject().getPlotMode() != dataOps.getPlottableObject().getPlotMode()) dataOps.setSelected(false);
//				
//			}
		}
		
//		String[] axes = nd.buildAxesNames();
//		SliceND slice= nd.buildSliceND();
//		Object[] options = nd.getOptions();
//		PlottableObject pO = dataOp.getPlottableObject();
//		if (pO != null && pO.getCachedTraces() != null && pO.getPlotMode().supportsMultiple()){
//			for (ITrace t  : pO.getCachedTraces())
//			getPlottingSystem().removeTrace(t);
//		}
//		dataOp.setAxes(axes);
//		
//		SourceInformation si = new SourceInformation(dataOp.getFileName(), dataOp.getName(), dataOp.getData());
//		SliceInformation s = new SliceInformation(slice, slice, new SliceND(dataOp.getData().getShape()), new int[]{0,1}, 1, 0);
//		SliceFromSeriesMetadata md = new SliceFromSeriesMetadata(si, s);
//		ITrace[] t = null;
//		try {
//			ILazyDataset view = dataOp.getData().getSliceView();
//			view.setName(fileController.getCurrentFile().getName() + ":" + fileController.getCurrentDataOption().getName());
//			
//			t = getCurrentMode().buildTraces(view,
//					slice, options, getPlottingSystem());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if (t == null) return;
//		
//		if (currentMode.supportsMultiple()) {
//
//			for (ITrace trace : t) {
//				trace.getData().setMetadata(md);
//				if (trace instanceof ISurfaceTrace) {
//					getPlottingSystem().setPlotType(PlotType.SURFACE);
//				}
//				if (!getPlottingSystem().getTraces().contains(trace)) getPlottingSystem().addTrace(trace);
//			}
//		}
//		
//		
//		getPlottingSystem().repaint();
//		PlottableObject po = new PlottableObject(getCurrentMode(), nd);
//		po.setCachedTraces(t);
//				
//		dataOp.setPlottableObject(po);
	}

}
