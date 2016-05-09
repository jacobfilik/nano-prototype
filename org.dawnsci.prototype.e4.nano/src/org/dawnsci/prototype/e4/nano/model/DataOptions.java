package org.dawnsci.prototype.e4.nano.model;

import java.util.Map;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.dataset.metadata.AxesMetadataImpl;

public class DataOptions implements SimpleTreeObject {

	private String name;
	private LoadedFile parent;
	private String[] axes;
	private ILazyDataset data;
	
	public DataOptions(String name, LoadedFile parent) {
		this.name = name;
		this.parent = parent;
	}
	
	@Override
	public boolean hasChildren() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object[] getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public String getFileName(){
		return parent.getName();
	}
	
	public Map<String, int[]> getAllPossibleAxes() {
		return parent.getDataShapes();
	}
	
	public ILazyDataset getData() {
		if (data == null) {
			ILazyDataset local = parent.getLazyDataset(name);
			if (axes != null) {
				AxesMetadataImpl ax = new AxesMetadataImpl(axes.length);
				for (int i = 0; i < axes.length ; i++) ax.setAxis(i, parent.getLazyDataset(axes[i]));
				local.setMetadata(ax);
			}
			data = local;
		}
		return data;
	}
	
	public void setAxes(String[] axesNames) {
		data = null;
		this.axes = axesNames;
	}

}
