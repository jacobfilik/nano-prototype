package org.dawnsci.prototype.e4.nano.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;

public class PlotManager {
	
	IPlottingService pService;
	
	private DataOptions currentOptions; 
	
	public PlotManager(IPlottingService p) {
		this.pService = p;
	}
	
	public void setDataOption(DataOptions dataOp) {
		this.currentOptions = dataOp;
	}
	
	public DataOptions getDataOption() {
		return currentOptions;
	}
	
	public void plotData(SliceND slice, boolean transpose) {
		Dataset data = DatasetUtils.convertToDataset(currentOptions.getData().getSlice(slice));
		data.squeeze();
		if (transpose) data = data.getTransposedView(null);
		IPlottingSystem<Object> ps = pService.getPlottingSystem("Plot");
		
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
		
		ps.createPlot2D(data,ax,null);
	}

}
