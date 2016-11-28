package org.dawnsci.prototype.nano.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawnsci.prototype.nano.model.table.ISliceChangeListener;
import org.dawnsci.prototype.nano.model.table.NDimensions;
import org.dawnsci.prototype.nano.model.table.SliceChangeEvent;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;

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
				List<DataStateObject> state = createImmutableFileState();
				System.out.println("slice");
				updatePlotState(state, currentMode);	
			};
		};
	}
	
//	private void removeAllTraces(LoadedFile removedFile) {
//		if (getPlottingSystem() == null) return;
//		
//		IPlottingSystem plottingSystem = getPlottingSystem();
//		
//		Collection<ITrace> ts = plottingSystem.getTraces();
//		
//		for (DataOptions op : removedFile.getDataOptions()) {
//			for (ITrace t : ts) {
//				if (op == t.getUserObject()) plottingSystem.removeTrace(t);
//			}
//		}
//		
//		plottingSystem.repaint();
//		
//	}
	
	private void updateOnFileStateChange() {
		if (getPlottingSystem() == null) return;
		
		DataOptions dOption = fileController.getCurrentDataOption();
		LoadedFile file = fileController.getCurrentFile();
		if (dOption == null) { 
			updatePlotState(null, currentMode);
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
			PlottableObject po = new PlottableObject(plotModes[0], nd);
			dOption.setPlottableObject(po);
			if (selected) currentMode = plotModes[0];
			
		}
		dOption.getPlottableObject().getNDimensions().addSliceListener(sliceListener);
		//update file state
		if (selected) updateFileState(file, dOption, currentMode);
		//make immutable state object
		List<DataStateObject> state = createImmutableFileState();
		//update plot
		updatePlotState(state, currentMode);
		
	}
	
	private void updatePlotState(List<DataStateObject> state, IPlotMode mode) {

		IPlottingSystem system = getPlottingSystem();

		Map<DataOptions, List<ITrace>> traceMap = collectTracesFromPlot();

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
		
		for (List<ITrace> traces : traceMap.values()) {
			for (ITrace t : traces) system.removeTrace(t);
		}
		
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
	
	private void updatePlottedData(DataStateObject stateObject, List<ITrace> traces, IPlotMode mode) {
		//remove traces if not the same as mode
		//update the data in the plot
		//TODO
		
		IPlottingSystem system = getPlottingSystem();
		
		if (traces != null) {
			for (ITrace t : traces) {
				//TODO update traces so dont remove all
				//			if (!mode.isThisMode(t)) system.removeTrace(t);
				system.removeTrace(t);
			}
		}
		
		PlottableObject plotObject = stateObject.getPlotObject();
		NDimensions nd = plotObject.getNDimensions();
		
		
		String[] axes = nd.buildAxesNames();
		SliceND slice= nd.buildSliceND();
		Object[] options = nd.getOptions();
		
		DataOptions dataOp = stateObject.getOption();
		
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
		
		if (t == null) return;		
		for (ITrace trace : t) {
			trace.setUserObject(dataOp);
			//	trace.getData().setMetadata(md);
			if (trace instanceof ISurfaceTrace) {
				system.setPlotType(PlotType.SURFACE);
			}
			system.addTrace(trace);
		}
		
		getPlottingSystem().repaint();
//		PlottableObject pO = dataOp.getPlottableObject();
//		if (pO != null && pO.getCachedTraces() != null && pO.getPlotMode().supportsMultiple()){
//			for (ITrace t  : pO.getCachedTraces())
//			getPlottingSystem().removeTrace(t);
//			
//			if (pO.getPlotMode().clearTracesOnRemoval()) pO.setCachedTraces(null);
//		}
//		
//		
//		dataOp.setAxes(axes);
////		
////		SourceInformation si = new SourceInformation(dataOp.getFileName(), dataOp.getName(), dataOp.getData());
////		SliceInformation s = new SliceInformation(slice, slice, new SliceND(dataOp.getData().getShape()), new int[]{0,1}, 1, 0);
////		SliceFromSeriesMetadata md = new SliceFromSeriesMetadata(si, s);
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
//		for (ITrace trace : t) {
//			trace.setUserObject(dataOp);
////		trace.getData().setMetadata(md);
//		if (trace instanceof ISurfaceTrace) {
//			getPlottingSystem().setPlotType(PlotType.SURFACE);
//		}
//		if (!getPlottingSystem().getTraces().contains(trace)) getPlottingSystem().addTrace(trace);
//	}
//		
//		
////		
////		if (currentMode.supportsMultiple()) {
////
////			for (ITrace trace : t) {
////				trace.getData().setMetadata(md);
////				if (trace instanceof ISurfaceTrace) {
////					getPlottingSystem().setPlotType(PlotType.SURFACE);
////				}
////				if (!getPlottingSystem().getTraces().contains(trace)) getPlottingSystem().addTrace(trace);
////			}
////		}
////		
////		
//		getPlottingSystem().repaint();
//		PlottableObject po = new PlottableObject(getCurrentMode(), nd);
//				
//		dataOp.setPlottableObject(po);
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
	
//	private void updateOnFileStateChange() {
//		if (getPlottingSystem() == null) return;
//		//update plot modes when changes happen
//		
////		if (!fileController.getCurrentFile().isSelected() || !fileController.getCurrentDataOption().isSelected()) return;
//		
//		DataOptions dOption = fileController.getCurrentDataOption();
//		if (dOption == null) return;
//		PlottableObject pObject = dOption.getPlottableObject();
//		boolean modeChange = false;
//		
//		//Test if selection leads to plot mode change
//		if (pObject != null) {
//			IPlotMode m = pObject.getPlotMode();
//			modeChange = m != currentMode;
//			currentMode = m;
//		} else {
//			IPlotMode m = getPlotModes(fileController.getSelectedDataRank())[0];
//			modeChange = m != currentMode;
//			currentMode = m;
//		}
//		
//		boolean selected = dOption.isSelected() && fileController.getCurrentFile().isSelected();
//		
//		if (!fileController.getCurrentFile().isSelected()) {
//			boolean allUnchecked = true;
//			for (LoadedFile f : fileController.getLoadedFiles()) {
//				if (f.isSelected()) allUnchecked = false;
//				break;
//			}
//			
//			if (allUnchecked) getPlottingSystem().clear();
//		}
//		
//		//If the data is selected and the mode has changed or doesnt support multiple
//		//un-check and remove others from the plot
//		if (selected && (modeChange || !currentMode.supportsMultiple())) {
//			List<DataOptions> dataOptions = fileController.getCurrentFile().getDataOptions();
//			for (DataOptions d : dataOptions) if (dOption != d) {
//				d.setSelected(false);
//				removeFromPlot(d);
//			}
//		}
//		
//		//for all files
//		// de-select and remove from plot any plotted
//		//unless support multiple and same type
//		if (selected) {
//			for (LoadedFile f : fileController.getLoadedFiles()) {
//				if (f.isSelected() && f != fileController.getCurrentFile()) {
//					List<DataOptions> dataOptions = f.getDataOptions();
//					for (DataOptions d : dataOptions) {
//						if (d.isSelected()) {
//							if (!(d.getPlottableObject() != null && d.getPlottableObject().getPlotMode() == currentMode && currentMode.supportsMultiple())) {
//								fileController.deselectFile(f);
//								removeFromPlot(d);
//							}
//						}
//					}
//				}
//			}
//		}
//
//		//check options set and set if not
//		NDimensions ndims = fileController.getNDimensions();
//		if (!ndims.areOptionsSet()) {
//			ndims.setOptions(currentMode.getOptions());
//			PlottableObject po = new PlottableObject(currentMode, ndims);
//			dOption.setPlottableObject(po);
//		}
//		
//		ndims.addSliceListener(sliceListener);
//		
//		if (!selected) {
//			if (!fileController.getCurrentFile().isSelected()) {
//				List<DataOptions> dataOptions = fileController.getCurrentFile().getDataOptions();
//				for (DataOptions d : dataOptions) removeFromPlot(d);
//			} else {
//				removeFromPlot(dOption);
//			}
//		} else {
//			
//			if (currentMode.supportsMultiple()) {
//				List<DataOptions> dataOptions = fileController.getCurrentFile().getDataOptions();
//				for (DataOptions d : dataOptions) if (d.isSelected()) addToPlot(d.getPlottableObject());
//			} else {
//				addToPlot(dOption.getPlottableObject());
//			}
//		}
//		
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
	
//	private void removeFromPlot(DataOptions data) {
//		if (data == null) return;
//		if (getPlottingSystem() == null) return;
//		IPlottingSystem s = getPlottingSystem();
//
//		Collection<ITrace> traces = s.getTraces();
//		for (ITrace t : traces) if (t.getUserObject() == data) s.removeTrace(t);
//
//		s.repaint();
//	}
	
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
		List<DataStateObject> state = createImmutableFileState();
		//update plot
		updatePlotState(state, currentMode);
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
	
	
//	public void switchPlotMode(IPlotMode mode) {
//		if (mode == currentMode) return;
//		
//		boolean selected =  fileController.getCurrentDataOption().isSelected() && fileController.getCurrentFile().isSelected();
//		
//		if (!selected) return;
//		
//		DataOptions option = fileController.getCurrentDataOption();
//		removeFromPlot(option);
//		
//		currentMode = mode;
////		removeAllOtherPlotted();
//		fileController.getNDimensions().setOptions(mode.getOptions());
//		
//	}
	


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
	
//	private void removeFromPlot(DataOptions option, IPlotMode currentMode) {
//		
//		Map<DataOptions, List<ITrace>> traceMap = collectTracesFromPlot();
//		IPlottingSystem system = getPlottingSystem();
//		
//		for (Entry<DataOptions,List<ITrace>> entry : traceMap.entrySet()) {
//			if (entry.getKey() == option) continue;
//			List<ITrace> value = entry.getValue();
//			for (ITrace t : value) {
//				if (!(currentMode.supportsMultiple() && currentMode.isThisMode(t))){
//					system.removeTrace(t);
//				}
//			}
//			
//		}
//		
//	}
	
//	private void removeAllOtherPlotted(){
//		
//		for (LoadedFile f : fileController.getLoadedFiles()) {
//			List<DataOptions> dataOptions = f.getDataOptions();
//			for (DataOptions d : dataOptions) {
//				if (d == fileController.getCurrentDataOption()) continue;
//				if (!(d.getPlottableObject() != null && d.getPlottableObject().getPlotMode() == currentMode && currentMode.supportsMultiple())) {
//					fileController.deselectFile(f);
//					removeFromPlot(d);
//				}
//
//			}
//		}
//	}
	
//	private void addToPlot(PlottableObject po) {
//		if (po == null) return;
//		if (getPlottingSystem() == null) return;
//		IPlottingSystem s = getPlottingSystem();
//		if (po.getCachedTraces() != null) {
//			
//			for (DataOptions dataOps : fileController.getCurrentFile().getDataOptions()) {
//				if (dataOps.getPlottableObject() == null || fileController.getCurrentDataOption().getPlottableObject().getPlotMode() != dataOps.getPlottableObject().getPlotMode()) {
//					dataOps.setSelected(false);
//					removeFromPlot(dataOps.getPlottableObject());
//				}
//			}
//			
//			Collection<ITrace> traces = s.getTraces();
//			ITrace[] cachedTraces = po.getCachedTraces();
//			for (ITrace t : cachedTraces) if (!s.getTraces().contains(t)) s.addTrace(t);
//			s.repaint();
//			
//		} else {
//			updatePlot(po.getNDimensions(), fileController.getCurrentDataOption());
//		}
//	}
	
	private IPlottingSystem getPlottingSystem() {
		if (system == null) {
			system = pService.getPlottingSystem("Plot");
		}
		return system;
	}
	

	public IPlotMode getCurrentMode() {
		return currentMode;
	}
	
//	private void updatePlot(NDimensions nd, DataOptions dataOp) {
//		
//		String[] axes = nd.buildAxesNames();
//		SliceND slice= nd.buildSliceND();
//		Object[] options = nd.getOptions();
//		PlottableObject pO = dataOp.getPlottableObject();
//		if (pO != null && pO.getCachedTraces() != null && pO.getPlotMode().supportsMultiple()){
//			for (ITrace t  : pO.getCachedTraces())
//			getPlottingSystem().removeTrace(t);
//			
//			if (pO.getPlotMode().clearTracesOnRemoval()) pO.setCachedTraces(null);
//		}
//		
//		
//		dataOp.setAxes(axes);
////		
////		SourceInformation si = new SourceInformation(dataOp.getFileName(), dataOp.getName(), dataOp.getData());
////		SliceInformation s = new SliceInformation(slice, slice, new SliceND(dataOp.getData().getShape()), new int[]{0,1}, 1, 0);
////		SliceFromSeriesMetadata md = new SliceFromSeriesMetadata(si, s);
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
//		for (ITrace trace : t) {
//			trace.setUserObject(dataOp);
////		trace.getData().setMetadata(md);
//		if (trace instanceof ISurfaceTrace) {
//			getPlottingSystem().setPlotType(PlotType.SURFACE);
//		}
//		if (!getPlottingSystem().getTraces().contains(trace)) getPlottingSystem().addTrace(trace);
//	}
//		
//		
////		
////		if (currentMode.supportsMultiple()) {
////
////			for (ITrace trace : t) {
////				trace.getData().setMetadata(md);
////				if (trace instanceof ISurfaceTrace) {
////					getPlottingSystem().setPlotType(PlotType.SURFACE);
////				}
////				if (!getPlottingSystem().getTraces().contains(trace)) getPlottingSystem().addTrace(trace);
////			}
////		}
////		
////		
//		getPlottingSystem().repaint();
//		PlottableObject po = new PlottableObject(getCurrentMode(), nd);
//				
//		dataOp.setPlottableObject(po);
//	}


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
}
