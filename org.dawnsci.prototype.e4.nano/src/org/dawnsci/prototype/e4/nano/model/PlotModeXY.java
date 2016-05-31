package org.dawnsci.prototype.e4.nano.model;

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
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;

public class PlotModeXY implements IPlotMode {

	private static final String[] options =  new String[]{"X"};

	public String[] getOptions() {
		return options;
	}
	
	public ITrace[] buildTraces(ILazyDataset lz, SliceND slice, Object[] options, IPlottingSystem ps) {
		Dataset data = DatasetUtils.convertToDataset(lz.getSlice(slice));
		data.squeeze();
//		if (data.getRank() != 2) return null;
		
		AxesMetadata metadata = data.getFirstMetadata(AxesMetadata.class);
		IDataset ax = null;
		
		if (metadata != null) {
			ILazyDataset[] axes = metadata.getAxes();
			if (axes[0] != null) ax = axes[0].getSlice();
		}
		
		ILineTrace trace = ps.createLineTrace(data.getName());
		trace.setData(ax, data);
		trace.setDataName(data.getName());
		
		return new ITrace[]{trace};
	}

	@Override
	public String getName() {
		return "Line";
	}
}
