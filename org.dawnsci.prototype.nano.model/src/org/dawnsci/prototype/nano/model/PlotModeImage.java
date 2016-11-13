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
import org.eclipse.swt.widgets.Display;

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
	
	public ITrace[] buildTraces(ILazyDataset lz, SliceND slice, Object[] options, IPlottingSystem ps) throws Exception {
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
		
		Collection<ITrace> traces = ps.getTraces(IImageTrace.class);
		IImageTrace trace = null;
		
		boolean empty = traces == null || traces.isEmpty();
		String name = MetadataPlotUtils.removeSquareBrackets(data.getName());
		data.setName(name);
//		ps.clear();
		if (!empty) {
			trace = (IImageTrace)traces.iterator().next();
			ps.renameTrace(trace, data.getName());
		} else {
	
		 trace = ps.createImageTrace(data.getName());
		}
		
		trace.setDataName(data.getName());
		if (true) {
			final IImageTrace f = trace;
			final List<IDataset> fax = ax;
			final IDataset fdata = data;
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					f.setData(fdata, fax, false);
					
				}
			});
			
		}
		
		
//		if (empty) {
//			ps.addTrace(trace);
//			ps.repaint();
//		}
		
		
		return new ITrace[]{trace};
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
	public boolean clearTracesOnRemoval() {
		return true;
	}

	@Override
	public int getMinimumRank() {
		return 2;
	}
	
	
}
