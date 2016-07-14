package org.dawnsci.prototype.nano.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;

public class PlotModeSurface implements IPlotMode {

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
	
	public ITrace[] buildTraces(ILazyDataset lz, SliceND slice, Object[] options, IPlottingSystem ps) {
		Dataset data = DatasetUtils.convertToDataset(lz.getSlice(slice));
		data.squeeze();
		if (data.getRank() != 2) return null;
		if (transposeNeeded(options)) data = data.getTransposedView(null);
		
		AxesMetadata metadata = data.getFirstMetadata(AxesMetadata.class);
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
		
		ISurfaceTrace trace = ps.createSurfaceTrace(data.getName());
		trace.setData(data, ax);
		trace.setDataName(data.getName());
		
		return new ITrace[]{trace};
	}

	@Override
	public String getName() {
		return "Surface";
	}
}