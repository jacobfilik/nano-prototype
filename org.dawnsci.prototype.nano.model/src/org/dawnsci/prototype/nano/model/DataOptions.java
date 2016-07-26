package org.dawnsci.prototype.nano.model;

import java.util.Map;


import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;

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
		return parent.getLongName();
	}
	
	public Map<String, int[]> getAllPossibleAxes() {
		return parent.getDataShapes();
	}
	
	public String[] getPrimaryAxes(){
		ILazyDataset local = parent.getLazyDataset(name);
		AxesMetadata am = local.getFirstMetadata(AxesMetadata.class);
		if (am == null) return null;
		if (am.getAxes() == null) return null;
		String[] ax = new String[am.getAxes().length];
		ILazyDataset[] axes = am.getAxes();
		int index = name.lastIndexOf(Node.SEPARATOR);
		if (index < 0) return null;
		String sub = name.substring(0, index);
		for (int i = 0; i < axes.length; i++) {
			if (axes[i] != null) {
				String full =  sub + Node.SEPARATOR + axes[i].getName();
				ax[i] = parent.getDataShapes().containsKey(full) ? full : null;
			} else {
				ax[i] = null;
			}
		}
		return ax;
	}
	
	public ILazyDataset getData() {
		if (data == null) {
			ILazyDataset local = parent.getLazyDataset(name).getSliceView();
			if (axes != null) {
				AxesMetadata ax;
				try {
					ax = MetadataFactory.createMetadata(AxesMetadata.class, axes.length);
					for (int i = 0; i < axes.length ; i++) ax.setAxis(i, parent.getLazyDataset(axes[i]));
					local.setMetadata(ax);
				} catch (MetadataException e) {
					e.printStackTrace();
				}
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
