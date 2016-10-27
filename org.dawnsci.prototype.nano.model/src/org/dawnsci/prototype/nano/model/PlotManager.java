package org.dawnsci.prototype.nano.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawnsci.prototype.nano.model.table.ISliceChangeListener;
import org.dawnsci.prototype.nano.model.table.NDimensions;
import org.dawnsci.prototype.nano.model.table.SliceChangeEvent;
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

	private IPlotMode[] modes = new IPlotMode[]{new PlotModeXY(), new PlotModeImage(), new PlotModeSurface()};
	private IPlotMode currentMode;
	
	private FileController fileController = FileController.getInstance();
	
	private ISliceChangeListener sliceListener;
	
	public PlotManager (IPlottingSystem system) {
		this.system = system;
		init();
	}
	
	public PlotManager(IPlottingService p) {
		this.pService = p;
		setCurrentMode(modes[0]);
		init();
		
	}
	
	private void init(){
		fileController.addStateListener(new FileControllerStateEventListener() {
			
			@Override
			public void stateChanged(FileControllerStateEvent event) {
				
				if (event.getRemovedFile() != null) {
					removeAllTraces(event.getRemovedFile());
				}
				
				if (!event.isSelectedDataChanged() && !event.isSelectedFileChanged()) return;
				updateOnFileStateChange();	
			}
		});
		
		sliceListener = new ISliceChangeListener() {

			@Override
			public void sliceChanged(SliceChangeEvent event) {
				if (!fileController.getCurrentFile().isSelected()) return;
				if (!fileController.getCurrentDataOption().isSelected()) return;
				updatePlot(fileController.getNDimensions(),fileController.getCurrentDataOption());
				
			};
		};
	}
	
	private void removeAllTraces(LoadedFile removedFile) {
		if (getPlottingSystem() == null) return;
		
		IPlottingSystem plottingSystem = getPlottingSystem();
		
		for (DataOptions op : removedFile.getDataOptions()) {
			
			if (op.getPlottableObject() == null || op.getPlottableObject().getCachedTraces() == null) continue;
			
			ITrace[] cachedTraces = op.getPlottableObject().getCachedTraces();
			
			for (ITrace t : cachedTraces) plottingSystem.removeTrace(t);
		}
		
		plottingSystem.repaint();
		
	}
	
	private void updateOnFileStateChange() {
		if (getPlottingSystem() == null) return;
		//update plot modes when changes happen
		DataOptions dOption = fileController.getCurrentDataOption();
		if (dOption == null) return;
		PlottableObject pObject = dOption.getPlottableObject();
		boolean modeChange = false;
		if (pObject != null) {
			IPlotMode m = pObject.getPlotMode();
			modeChange = m != currentMode;
			currentMode = m;
		} else {
			IPlotMode m = getPlotModes(fileController.getSelectedDataRank())[0];
			modeChange = m != currentMode;
			currentMode = m;
		}
		
		if (modeChange || !currentMode.supportsMultiple()) {
			fileController.deselectAllOthers();
			List<DataOptions> dataOptions = fileController.getCurrentFile().getDataOptions();
			for (DataOptions d : dataOptions) if (dOption != d) {
				d.setSelected(false);
				removeFromPlot(d.getPlottableObject());
			}
		}

		
		NDimensions ndims = fileController.getNDimensions();
		if (!ndims.areOptionsSet()) {
			ndims.setOptions(currentMode.getOptions());
			PlottableObject po = new PlottableObject(currentMode, ndims);
			dOption.setPlottableObject(po);
		}
		
		ndims.addSliceListener(sliceListener);
		
		if (!dOption.isSelected() || !fileController.getCurrentFile().isSelected()) {
			if (!fileController.getCurrentFile().isSelected()) {
				List<DataOptions> dataOptions = fileController.getCurrentFile().getDataOptions();
				for (DataOptions d : dataOptions) removeFromPlot(d.getPlottableObject());
			} else {
				removeFromPlot(dOption.getPlottableObject());
			}
		} else {
			
			if (currentMode.supportsMultiple()) {
				List<DataOptions> dataOptions = fileController.getCurrentFile().getDataOptions();
				for (DataOptions d : dataOptions) if (d.isSelected())addToPlot(d.getPlottableObject());
			} else {
				addToPlot(dOption.getPlottableObject());
			}
		}
		
	}
	
//	public IPlotMode[] getPlotModes() {
//		return modes;
//	}
	
	public IPlotMode[] getCurrentPlotModes() {
		if (fileController.getCurrentDataOption() == null) return null;
		
		return getPlotModes(fileController.getSelectedDataRank());
	}
	
	private IPlotMode[] getPlotModes(int rank) {
		
		List<IPlotMode> m = new ArrayList<>();
		for (IPlotMode mode : modes) {
			if (mode.getMinimumRank() <= rank) m.add(mode);
		}
		
		return m.toArray(new IPlotMode[m.size()]);
		
	}
	
	private void removeFromPlot(PlottableObject po) {
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
			
			if (currentMode.clearTracesOnRemoval()) po.setCachedTraces(null);
			
			s.repaint();
		}
	}
	
	public void switchPlotMode(IPlotMode mode) {
		if (mode == currentMode) return;
		if (fileController.getCurrentDataOption().getPlottableObject() != null) removeFromPlot(fileController.getCurrentDataOption().getPlottableObject());
		currentMode = mode;
		fileController.getNDimensions().setOptions(mode.getOptions());
		if (!fileController.getCurrentDataOption().isSelected() || ! fileController.getCurrentFile().isSelected()) return;
		if (fileController.getCurrentDataOption().getPlottableObject() != null) {
			addToPlot(fileController.getCurrentDataOption().getPlottableObject());
		} else {
			updatePlot(fileController.getNDimensions(), fileController.getCurrentDataOption());
		}
		
	}
	
	private void addToPlot(PlottableObject po) {
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
	
	private void updatePlot(NDimensions nd, DataOptions dataOp) {
		
		String[] axes = nd.buildAxesNames();
		SliceND slice= nd.buildSliceND();
		Object[] options = nd.getOptions();
		PlottableObject pO = dataOp.getPlottableObject();
		if (pO != null && pO.getCachedTraces() != null && pO.getPlotMode().supportsMultiple()){
			for (ITrace t  : pO.getCachedTraces())
			getPlottingSystem().removeTrace(t);
		}
		dataOp.setAxes(axes);
//		
//		SourceInformation si = new SourceInformation(dataOp.getFileName(), dataOp.getName(), dataOp.getData());
//		SliceInformation s = new SliceInformation(slice, slice, new SliceND(dataOp.getData().getShape()), new int[]{0,1}, 1, 0);
//		SliceFromSeriesMetadata md = new SliceFromSeriesMetadata(si, s);
		ITrace[] t = null;
		try {
			ILazyDataset view = dataOp.getData().getSliceView();
			view.setName(fileController.getCurrentFile().getName() + ":" + fileController.getCurrentDataOption().getName());
			
			t = getCurrentMode().buildTraces(view,
					slice, options, getPlottingSystem());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (t == null) return;
		
		for (ITrace trace : t) {
//		trace.getData().setMetadata(md);
		if (trace instanceof ISurfaceTrace) {
			getPlottingSystem().setPlotType(PlotType.SURFACE);
		}
		if (!getPlottingSystem().getTraces().contains(trace)) getPlottingSystem().addTrace(trace);
	}
		
		
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
		getPlottingSystem().repaint();
		PlottableObject po = new PlottableObject(getCurrentMode(), nd);
		po.setCachedTraces(t);
				
		dataOp.setPlottableObject(po);
	}

}
