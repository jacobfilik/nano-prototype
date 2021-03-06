package org.dawnsci.prototype.nano.model;

import java.util.Arrays;
import java.util.Map;

import org.dawnsci.prototype.nano.model.table.NDimensions;
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
	private boolean selected;

	private PlottableObject plottableObject;
	
	public DataOptions(String name, LoadedFile parent) {
		this.name = name;
		this.parent = parent;
	}
	
	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Object[] getChildren() {
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
					for (int i = 0; i < axes.length ; i++) {
						ILazyDataset lzAxes = parent.getLazyDataset(axes[i]);
						if (lzAxes != null && !(lzAxes.getRank() ==1 || lzAxes.getRank() == local.getRank())) {
							int rank = local.getRank();
							int[] shape = local.getShape();
							int[] axShape = lzAxes.getShape();
							int axRank = lzAxes.getRank();
							int[] newShape = new int[local.getRank()];
							Arrays.fill(newShape, 1);

							int[] idx = new int[axRank];
							Arrays.fill(idx, -1);
							Boolean[] found = new Boolean[axRank];
							Arrays.fill(found, false);
							int max = rank;

							for (int j = axRank-1; j >= 0; j--) {
								int id = axShape[j];
								updateShape(i, max, shape, id, idx, found);

							}

							boolean allFound = !Arrays.asList(found).contains(false);

							if (!allFound) {
								continue;
							}

							for (int j = 0; j < axRank; j++) {
								newShape[idx[j]] = axShape[j];
							}
							
							lzAxes = lzAxes.getSliceView();
							lzAxes.setShape(newShape);
						}
						ax.setAxis(i, parent.getLazyDataset(axes[i]));
					}
					local.setMetadata(ax);
				} catch (MetadataException e) {
					e.printStackTrace();
				}
			}
			data = local;
		}
		return data;
	}
	
	private boolean updateShape(int i, int max, int[] shape, int id, int[] idx, Boolean[] found){
		
		int[] idxc = idx.clone();
		Arrays.sort(idxc);
		
		for (int j = max -1 ; j >= 0; j--) {

			if (id == shape[j] && Arrays.binarySearch(idxc, j) < 0) {
				idx[i] = j;
				found[i] = true;
				max = j;
				return true;
			}

		}
		
		return false;
	}
	
	public void setAxes(String[] axesNames) {
		data = null;
		this.axes = axesNames;
	}

	public PlottableObject getPlottableObject() {
		return plottableObject;
	}

	public void setPlottableObject(PlottableObject plottableObject) {
		this.plottableObject = plottableObject;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public NDimensions buildNDimensions() {
		NDimensions ndims = new NDimensions(getData().getShape());
		ndims.setUpAxes((String)null, getAllPossibleAxes(), getPrimaryAxes());
		return ndims;
	}
}
