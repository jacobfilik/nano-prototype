package org.dawnsci.prototype.nano.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawnsci.prototype.nano.model.table.ISliceChangeListener;
import org.dawnsci.prototype.nano.model.table.NDimensions;
import org.dawnsci.prototype.nano.model.table.SliceChangeEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.swt.widgets.Display;

public class PlotManager {
	
	private IPlottingService pService;
	private IPlottingSystem system;

	private IPlotMode[] modes = new IPlotMode[]{new PlotModeXY(), new PlotModeImage(), new PlotModeSurface()};
	private IPlotMode currentMode;
	
	private FileController fileController = FileController.getInstance();
	
	private ISliceChangeListener sliceListener;
	
	private UpdatePlotJob job = new UpdatePlotJob();
	
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
//				updateOnFileStateChange();	
				job.setFullUpdate(true);
				job.schedule();
			}
		});
		
		sliceListener = new ISliceChangeListener() {

			@Override
			public void sliceChanged(SliceChangeEvent event) {
				if (!fileController.getCurrentFile().isSelected()) return;
				if (!fileController.getCurrentDataOption().isSelected()) return;
//				updatePlot(fileController.getNDimensions(),fileController.getCurrentDataOption());
				job.setFullUpdate(false);
				job.schedule();
				
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
			
			if (op.getPlottableObject().getPlotMode().clearTracesOnRemoval()) op.getPlottableObject().setCachedTraces(null);
		}
		
		plottingSystem.repaint();
		
	}
	
	private void updateOnFileStateChange() {
		if (getPlottingSystem() == null) return;
		//update plot modes when changes happen
		
//		if (!fileController.getCurrentFile().isSelected() || !fileController.getCurrentDataOption().isSelected()) return;
		
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
		
		if (dOption.isSelected() && fileController.getCurrentFile().isSelected() && (modeChange || !currentMode.supportsMultiple())) {
//			fileController.deselectAllOthers();
			List<DataOptions> dataOptions = fileController.getCurrentFile().getDataOptions();
			for (DataOptions d : dataOptions) if (dOption != d) {
				d.setSelected(false);
				removeFromPlot(d.getPlottableObject());
			}
		}
		
		if (dOption.isSelected() && fileController.getCurrentFile().isSelected()) {

			for (LoadedFile f : fileController.getLoadedFiles()) {
				if (f.isSelected() && f != fileController.getCurrentFile()) {
					List<DataOptions> dataOptions = f.getDataOptions();
					for (DataOptions d : dataOptions) {
						if (d.isSelected()) {
							if (!(d.getPlottableObject() != null && d.getPlottableObject().getPlotMode() == currentMode && currentMode.supportsMultiple())) {

								fileController.deselectFile(f);
								removeFromPlot(d.getPlottableObject());
							}
						}
					}
				}
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
	
	private void removeFromPlot(final PlottableObject po) {
		if (po == null) return;
		if (getPlottingSystem() == null) return;
		
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				IPlottingSystem s = getPlottingSystem();
//				if (!po.getPlotMode().supportsMultiple()) po.setCachedTraces(null);
				if (po.getCachedTraces() != null) {
					ITrace[] cachedTraces = po.getCachedTraces();
					for (ITrace t : cachedTraces) {
						Collection<ITrace> traces = s.getTraces();
						if (s.getTraces().contains(t)) s.removeTrace(t);
					}
					
					if (po.getPlotMode().clearTracesOnRemoval()) po.setCachedTraces(null);
					
					s.repaint();
				}
			}
		});
		
		
	}
	
	public void switchPlotMode(IPlotMode mode) {
		if (mode == currentMode) return;
		currentMode = mode;
		fileController.getNDimensions().setOptions(mode.getOptions());
		
	}
	
	private void addToPlot(PlottableObject po) {
		if (po == null) return;
		if (getPlottingSystem() == null) return;
		IPlottingSystem s = getPlottingSystem();
//		if (po.getCachedTraces() != null && !po.getPlotMode().clearTracesOnRemoval()) {
		if (po.getCachedTraces() != null) {
			
			for (DataOptions dataOps : fileController.getCurrentFile().getDataOptions()) {
				if (dataOps.getPlottableObject() == null || fileController.getCurrentDataOption().getPlottableObject().getPlotMode() != dataOps.getPlottableObject().getPlotMode()) {
					dataOps.setSelected(false);
					removeFromPlot(dataOps.getPlottableObject());
				}
			}
			
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					Collection<ITrace> traces = s.getTraces();
					ITrace[] cachedTraces = po.getCachedTraces();
					for (ITrace t : cachedTraces) if (!s.getTraces().contains(t)) s.addTrace(t);
					s.repaint();
				}
			});
			
			
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
		
		if (dataOp.getPlottableObject() != null) {
			if (dataOp.getPlottableObject().getPlotMode() != currentMode)
			removeFromPlot(dataOp.getPlottableObject());
		}
		
		String[] axes = nd.buildAxesNames();
		SliceND slice= nd.buildSliceND();
		Object[] options = nd.getOptions();
		PlottableObject pO = dataOp.getPlottableObject();
		if (pO != null && pO.getCachedTraces() != null && pO.getPlotMode().supportsMultiple()){
			removeFromPlot(pO);
//			for (ITrace t  : pO.getCachedTraces())
//			getPlottingSystem().removeTrace(t);
			
//			if (pO.getPlotMode().clearTracesOnRemoval()) pO.setCachedTraces(null);
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
		final 
		ITrace[] ft = t;
Display.getDefault().syncExec(new Runnable() {
	
	@Override
	public void run() {
		for (ITrace trace : ft) {
			//		trace.getData().setMetadata(md);
			if (trace instanceof ISurfaceTrace) {
				getPlottingSystem().setPlotType(PlotType.SURFACE);
			}
			if (!getPlottingSystem().getTraces().contains(trace)) getPlottingSystem().addTrace(trace);
		}
	}
});
		

		
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
	
	private class UpdatePlotJob extends Job {
		
		private boolean fullUpdate = true;

		public UpdatePlotJob() {
			super("Update plot job");
		}
		
		public void setFullUpdate(boolean update) {
			this.fullUpdate = update;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (fullUpdate) {
				updateOnFileStateChange();
			} else {
				updatePlot(fileController.getNDimensions(),fileController.getCurrentDataOption());
			}
			return Status.OK_STATUS;
		}
		
	}

}
