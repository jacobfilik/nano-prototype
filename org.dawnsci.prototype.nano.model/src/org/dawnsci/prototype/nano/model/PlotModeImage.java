package org.dawnsci.prototype.nano.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;

public class PlotModeImage implements IPlotMode {

	private static final String[] options =  new String[]{"X","Y"};

	public String[] getOptions() {
		return options;
	}
	
	public static boolean transposeNeeded(Object[] options){
		
		boolean transpose = false;
		for (int i = 0; i < options.length; i++) {
			if (options[i] != null && !((String)options[i]).isEmpty()) {
				if (options[i].equals("Y")) {
					transpose = false;
					break;
				} else {
					transpose = true;
					break;
				}
			}
		}
		
		return transpose;
	}
	
	public IDataset[] sliceForPlot(ILazyDataset lz, SliceND slice, Object[] options) throws Exception {
		Dataset data = DatasetUtils.convertToDataset(lz.getSlice(slice));
		data.squeeze();
		if (data.getRank() != 2) return null;
		if (transposeNeeded(options)) data = data.getTransposedView(null);
		return new IDataset[]{data};
	}
	
	public void displayData(IDataset[] data, ITrace[] update, IPlottingSystem system, Object userObject) throws Exception {
		IDataset d = data[0];
		AxesMetadata metadata = d.getFirstMetadata(AxesMetadata.class);
		List<IDataset> ax = null;
		
		if (metadata != null) {
			ax = new ArrayList<IDataset>();
			ILazyDataset[] axes = metadata.getAxes();
			if (axes != null) {
				for (ILazyDataset a : axes) {
					ax.add(a == null ? null : a.getSlice().squeeze());
				}
				Collections.reverse(ax);
			}
		}
		
		IImageTrace trace = null;
		
//		String name = MetadataPlotUtils.removeSquareBrackets(d.getName());
//		d.setName(name);
		//deal with updates
		boolean isUpdate = false;
		if (update == null) {
			trace = system.createImageTrace(d.getName());
			trace.setDataName(d.getName());
		} else {
			if (update[0] instanceof IImageTrace) {
				trace = (IImageTrace) update[0];
				isUpdate = true;
			}
			
			for (int i = 0; i < update.length; i++) {
				if (i==0 && update[i] instanceof IImageTrace) {
					continue;
				}
				system.removeTrace(update[i]);
			}
		}
		
		
		trace.setData(d, ax, false);
		trace.setUserObject(userObject);
		system.setTitle(d.getName());
		if (!isUpdate)system.addTrace(trace);
		
	}

	@Override
	public String getName() {
		return "Image";
	}
	
	@Override
	public boolean supportsMultiple(){
		return false;
	}

	@Override
	public int getMinimumRank() {
		return 2;
	}

	@Override
	public boolean isThisMode(ITrace trace) {
		return trace instanceof IImageTrace;
	}
	
	@Override
	public int[] getDataDimensions(Object[] currentOptions) {
		int[] dataDims = new int[2];
		int count = 0;
		for (int i = 0; i < currentOptions.length && count < 2; i++) {
			if (currentOptions[i] != null && !currentOptions[i].toString().isEmpty() && (options[0].equals(currentOptions[i].toString()) || options[1].equals(currentOptions[i].toString()))) {
				dataDims[count++] = i;
			}
		}
		return dataDims;
	}
	
}
