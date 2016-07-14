package org.dawnsci.prototype.nano.model.table;

import org.eclipse.dawnsci.analysis.api.dataset.Slice;

public class Dimension {
	
	private int dimension;
	private String description;
	private String[] axisOptions;
	private String axis;
	private int size = -1;
	private Slice slice;
	
	
	public Dimension(int dimension, int size) {
		this.dimension = dimension;
		this.size = size;
		slice = new Slice(0, 1, 1);
	}


	public Slice getSlice() {
		return slice;
	}
	
	public void setSlice(Slice slice) {
		this.slice = slice;
	}

	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDimensionWithSize() {
		if (size < 0) return Integer.toString(dimension);
		return Integer.toString(dimension) + " [" + Integer.toString(size) + "]";
	}
	
	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}


	public String getAxis() {
		return axis;
	}
	
	public int getDimension() {
		return dimension;
	}


	public void setAxis(String axis) {
		if (axis != null && !axis.isEmpty()) this.axis = axis;
	}


	public String[] getAxisOptions() {
		return axisOptions;
	}


	public void setAxisOptions(String[] axisOptions) {
		
		this.axisOptions = axisOptions;
	}

}
