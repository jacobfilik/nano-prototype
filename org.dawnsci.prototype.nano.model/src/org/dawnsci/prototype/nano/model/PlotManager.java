package org.dawnsci.prototype.nano.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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

/**
 * Controller class for the plotting system
 * 
 * On selection of a checked DataOption, for a checked loaded file, the default
 * plot mode, with its default slicing should be plotted.
 * 
 * if a different DataOption is checked in the same file, the default plot mode,
 * with its default slicing should be plotted. If the plot mode support multiple,
 * all traces of the same type should remain, if it doesn't the initial DataOption should be unchecked
 * and its trace removed. The a DataOption is added from a different file, and the plot mode
 * does not support multiple, the initial file should be uncheck and its trace removed.
 * 
 * On change of PlotMode all files except for the current should be deselected, and data options, and 
 * their traces removed from the plot.
 * 
 * When I file is unloaded all its traces should be removed.
 * 
 * When the slice changes, the traces should be updated or updated and added.
 * 
 * 
 * @author jacobfilik
 */
public class PlotManager {
	
	private IPlottingService pService;
	private IPlottingSystem system;

	private IPlotMode[] modes = new IPlotMode[]{new PlotModeXY(), new PlotModeImage(), new PlotModeSurface()};
	private IPlotMode currentMode;
	
	private FileController fileController = FileController.getInstance();
	
	private ISliceChangeListener sliceListener;
	
	private SliceForPlotJob job;
	
	public PlotManager (IPlottingSystem system) {
		this.system = system;
		init();
	}
	
	public PlotManager(IPlottingService p) {
		this.pService = p;
		this.currentMode = modes[0];
		init();
		
	}
	
	private void init(){
		
		job = new SliceForPlotJob();
		
		fileController.addStateListener(new FileControllerStateEventListener() {
			
			@Override
			public void stateChanged(FileControllerStateEvent event) {
				
				if (!event.isSelectedDataChanged() && !event.isSelectedFileChanged()) return;
				updateOnFileStateChange();	
			}
		});
		
		sliceListener = new ISliceChangeListener() {

			@Override
			public void sliceChanged(SliceChangeEvent event) {
				//respond to this happening elsewhere
				if (event.isOptionsChanged()) return;
				if (!fileController.getCurrentFile().isSelected()) return;
				if (!fileController.getCurrentDataOption().isSelected()) return;
				final List<DataStateObject> state = createImmutableFileState();
				
				updatePlotStateInJob(state, currentMode);	
			};
		};
	}
	
	private void updateOnFileStateChange() {
		if (getPlottingSystem() == null) return;
		
		DataOptions dOption = fileController.getCurrentDataOption();
		LoadedFile file = fileController.getCurrentFile();
		if (dOption == null) { 
			updatePlotStateInJob(null, currentMode);
			return;
		}
		
		boolean selected = file.isSelected() && dOption.isSelected();
//		if (!file.isSelected() || !dOption.isSelected()) return;
		
		PlottableObject plotObject = dOption.getPlottableObject();
		
		if (plotObject != null && plotObject.getPlotMode() != currentMode && selected) {
			currentMode = plotObject.getPlotMode();
		} else if (plotObject == null) {
			NDimensions nd = fileController.getNDimensions();
			IPlotMode[] plotModes = getPlotModes(nd.getRank());
			nd.setOptions(plotModes[0].getOptions());
			PlottableObject po = new PlottableObject(plotModes[0], nd);
			dOption.setPlottableObject(po);
			if (selected) currentMode = plotModes[0];
			
		}
		dOption.getPlottableObject().getNDimensions().addSliceListener(sliceListener);
		//update file state
		if (selected) updateFileState(file, dOption, currentMode);
		//make immutable state object
		final List<DataStateObject> state = createImmutableFileState();
		//update plot
		updatePlotStateInJob(state, currentMode);
		
	}
	
	private void updatePlotState(List<DataStateObject> state, IPlotMode mode) {

		IPlottingSystem system = getPlottingSystem();

		final Map<DataOptions, List<ITrace>> traceMap = collectTracesFromPlot();

		if (state == null) state = new ArrayList<DataStateObject>();
		
		if (mode != null && !mode.supportsMultiple()) {
			system.clear();
		}
		
		Map<DataOptions, List<ITrace>> updateMap = new HashMap<>();
		//have to do multiple iterations so image traces arent removed after correct
		// one added
		for (DataStateObject object : state) {
			updateMap.put(object.getOption(), traceMap.remove(object.getOption()));	
		}
		
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				for (List<ITrace> traces : traceMap.values()) {
					for (ITrace t : traces) system.removeTrace(t);
				}
			}
		});
		
		
		for (DataStateObject object : state) {

			List<ITrace> list = updateMap.remove(object.getOption());
			
			if (list == null) list = new ArrayList<ITrace>();

			if (!object.isChecked() && !list.isEmpty()) {
				for (ITrace t : list){
					system.removeTrace(t);
				}
			} else if (object.isChecked()) {
				updatePlottedData(object, list, currentMode);
			}
		}
	}
	
	private void updatePlottedData(DataStateObject stateObject,final List<ITrace> traces, IPlotMode mode) {
		//remove traces if not the same as mode
		//update the data in the plot
		//TODO
		
		IPlottingSystem system = getPlottingSystem();
		
		PlottableObject plotObject = stateObject.getPlotObject();
		NDimensions nd = plotObject.getNDimensions();
		
		
		String[] axes = nd.buildAxesNames();
		SliceND slice= nd.buildSliceND();
		Object[] options = nd.getOptions();
		
		DataOptions dataOp = stateObject.getOption();
		dataOp.setAxes(axes);
		
		ITrace[] t = null;
		try {
			ILazyDataset view = dataOp.getData().getSliceView();
			view.setName(dataOp.getFileName() + ":" + dataOp.getName());

			t = mode.buildTraces(view,
					slice, options, system);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		SourceInformation si = new SourceInformation(dataOp.getFileName(), dataOp.getName(), dataOp.getData());
//		SliceInformation s = new SliceInformation(slice, slice, new SliceND(dataOp.getData().getShape()), new int[]{0,1}, 1, 0);
//		SliceFromSeriesMetadata md = new SliceFromSeriesMetadata(si, s);
		
		if (t == null) return;	
	
		final ITrace[] finalTraces = t;
		
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				
				//do update if number of traces the same
				boolean update = true;
				if (traces != null && finalTraces.length != traces.size()) {
					update = false;
					for (ITrace t : traces) {
						system.removeTrace(t);
					}
				}
				
				int count = 0;
				for (ITrace trace : finalTraces) {
					if (update) {
						mode.updateTrace(traces.get(count++),trace);

					} else {
						trace.setUserObject(dataOp);
						//	trace.getData().setMetadata(md);
						if (trace instanceof ISurfaceTrace) {
							system.setPlotType(PlotType.SURFACE);
						}
						system.addTrace(trace);
					}
				}
				
				if (!update) getPlottingSystem().repaint();
			}
		});
		
	}
	
	private List<DataStateObject> createImmutableFileState() {
		
		List<DataStateObject> list = new ArrayList<DataStateObject>();
		
		for (LoadedFile f : fileController.getLoadedFiles()) {
			for (DataOptions d : f.getDataOptions()) {
				
				PlottableObject plotObject = null; 
				
				if (d.getPlottableObject() != null) {
					PlottableObject p = d.getPlottableObject();
					plotObject = new PlottableObject(p.getPlotMode(), new NDimensions(p.getNDimensions()));
				} 
				if (f.isSelected() && d.isSelected()) {
					DataStateObject dso = new DataStateObject(d, f.isSelected() && d.isSelected(), plotObject);
					
					list.add(dso);
				}
				
			}
		}
		
		return list;
	}
	
	
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
	
	public void switchPlotMode(IPlotMode mode) {
		if (mode == currentMode) return;
		
		DataOptions dOption = fileController.getCurrentDataOption();
		
		boolean selected =  dOption.isSelected() && fileController.getCurrentDataOption().isSelected();
		if (!selected) return;
		
		currentMode = mode;
		NDimensions nd = fileController.getNDimensions();
		nd.setOptions(mode.getOptions());
		
		dOption.setPlottableObject(new PlottableObject(currentMode, nd));
		
		updateFileState(fileController.getCurrentFile(), fileController.getCurrentDataOption(),currentMode);
		final List<DataStateObject> state = createImmutableFileState();
		//update plot
		updatePlotStateInJob(state, currentMode);
	}
	
	private void updatePlotStateInJob(List<DataStateObject> state, IPlotMode mode){
		
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				updatePlotState(state, mode);
			}
		};
		job.setRunnable(r);
		job.schedule();
	}
	
	private void updateFileState(LoadedFile file, DataOptions option, IPlotMode mode) {
		
		for (LoadedFile f : fileController.getLoadedFiles()) {
			if (!f.isSelected()) continue;
			
			boolean thisFile = f == file;
			
			for (DataOptions o : f.getDataOptions()) {
				if (!o.isSelected()) continue;
				if (option == o) continue;
				if (o.getPlottableObject() == null) continue;
				if (!mode.supportsMultiple() || o.getPlottableObject().getPlotMode() != mode) {
					if (thisFile) {
						fileController.deselectOption(o);
					} else {
						fileController.deselectFile(f);
					}
				}
			}	
		}
	}

	private Map<DataOptions, List<ITrace>> collectTracesFromPlot() {
		
		IPlottingSystem system = getPlottingSystem();
		
		Collection<ITrace> traces = system.getTraces();
		
		Map<DataOptions, List<ITrace>> optionTraceMap = new HashMap<>();
		
		for (ITrace t : traces) {
			if (t.getUserObject() instanceof DataOptions) {
				DataOptions option = (DataOptions)t.getUserObject();
				if (optionTraceMap.containsKey(option)) {
					optionTraceMap.get(option).add(t);
				} else {
					List<ITrace> list = new ArrayList<ITrace>();
					list.add(t);
					optionTraceMap.put(option, list);
				}
			}
		}
		
		return optionTraceMap;
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
	
	private class DataStateObject {
		
		private boolean checked;
		private DataOptions option;
		private PlottableObject plotObject;

		public DataStateObject(DataOptions option, boolean checked, PlottableObject plotObject) {
			
			this.option = option;
			this.checked = checked;
			this.plotObject = plotObject;
		}

		public boolean isChecked() {
			return checked;
		}

		public DataOptions getOption() {
			return option;
		}

		public PlottableObject getPlotObject() {
			return plotObject;
		}
		
	}
	
	private class SliceForPlotJob extends Job {

		private Runnable runnable;
		
		public SliceForPlotJob() {
			super("Slice for plot");
		}
		
		public void setRunnable(Runnable runnable) {
			this.runnable = runnable;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			Runnable local = runnable;
			local.run();
			return Status.OK_STATUS;
		}
		
	}
}
