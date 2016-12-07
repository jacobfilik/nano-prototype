package org.dawnsci.prototype.nano.model;

import org.eclipse.dawnsci.analysis.dataset.slicer.SliceViewIterator;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;

public class PlotModeXY implements IPlotMode {

	private static final String[] options =  new String[]{"X"};
	private long count = 0;

	public String[] getOptions() {
		return options;
	}

	@Override
	public String getName() {
		return "Line";
	}
	
	@Override
	public boolean supportsMultiple(){
		return true;
	}

	@Override
	public int getMinimumRank() {
		return 1;
	}

	@Override
	public boolean isThisMode(ITrace trace) {
		return trace instanceof ILineTrace;
	}

	@Override
	public IDataset[] sliceForPlot(ILazyDataset lz, SliceND slice, Object[] options) throws Exception {
		int[] dataDims = new int[1];
		for (int i = 0; i < options.length; i++) {
			if (PlotModeXY.options[0].equals(options[i])){
				dataDims[0] = i;
				break;
			}
		}
		
		SliceViewIterator it = new SliceViewIterator(lz, slice, dataDims);
		
		int total = it.getTotal();
		IDataset[] all = new IDataset[total];
		int count = 0;
		while (it.hasNext()) {
			ILazyDataset next = it.next();
			all[count++] = DatasetUtils.convertToDataset(next.getSlice()).squeeze();
			
		}
		return all;
	}

	@Override
	public void displayData(IDataset[] data, ITrace[] update, IPlottingSystem system, Object userObject)
			throws Exception {
		
		renameUpdates(update, system);
		int count = 0;
		for (IDataset d : data) {
			//TODO add update
			createSingleTrace(d, system, userObject, (update == null || count >= update.length) ? null:update[count++]);
		}
		if (update != null) for (; count < update.length; count++) {
			system.removeTrace(update[count]);
		}
		
		system.repaint();
	}
	
	private void renameUpdates(ITrace[] update, IPlottingSystem system) {
		
		if (update == null) return;
		
		for (ITrace t : update) {
			String name = "totally_amazing_unique_name_" + count++;
			t.setName(name);
//			try {
//				system.renameTrace(t, name);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}

	private void createSingleTrace(IDataset data, IPlottingSystem system, Object userObject, ITrace update) throws DatasetException {
		
		if (update != null && !(update instanceof ILineTrace)) system.removeTrace(update);
		
		AxesMetadata metadata = data.getFirstMetadata(AxesMetadata.class);
		IDataset ax = null;
		
		if (metadata != null) {
			ILazyDataset[] axes = metadata.getAxes();
			if (axes.length == 1 && axes[0] != null) ax = axes[0].getSlice();
		}
		
		ILineTrace trace = null;
		boolean canUpdate = false;
		if (update instanceof ILineTrace) {
			canUpdate = true;
//			String name = "totally_amazing_unique_name_" + count++;
			update.setName(data.getName());
			try {
//				system.renameTrace(update, data.getName());
				trace = (ILineTrace)update;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			trace  = system.createLineTrace(data.getName());
		}
		trace.setDataName(data.getName());
		trace.setData(ax, data);
		trace.setUserObject(userObject);
		if (!canUpdate)system.addTrace(trace);
	}
}
