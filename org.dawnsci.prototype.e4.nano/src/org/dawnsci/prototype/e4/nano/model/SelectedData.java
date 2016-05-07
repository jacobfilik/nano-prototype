package org.dawnsci.prototype.e4.nano.model;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;

public class SelectedData implements SimpleTreeObject {

	private ILazyDataset lazy;

	public SelectedData(ILazyDataset lazy) {
		this.lazy = lazy;
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
		return lazy.getName();
	}
	
	

}
