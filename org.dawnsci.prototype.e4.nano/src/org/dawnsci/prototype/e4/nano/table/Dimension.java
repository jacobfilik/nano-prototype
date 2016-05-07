package org.dawnsci.prototype.e4.nano.table;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.dawnsci.analysis.api.dataset.Slice;

public class Dimension {

	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	
	private int dimension;
	private String description;
	private String[] axisOptions;
	private String axis;
	private String secondaryAxis; //used for data that needs to be remapped
	private int size = -1;
	private Slice slice;
	
	
	public Dimension(int dimension) {
		this.dimension = dimension;
	}
	
	public Dimension(int dimension, int size) {
		this(dimension);
		this.size = size;
		slice = new Slice(0, 1, 1);
	}


	public Slice getSlice() {
		return slice;
	}

	public void setSlice(Slice slice) {
		firePropertyChange("slice", this.slice, this.slice = slice);
	}

	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		firePropertyChange("description", this.description, this.description = description);
	}
	
	public String getDimensionWithSize() {
		if (size < 0) return Integer.toString(dimension);
		return Integer.toString(dimension) + " [" + Integer.toString(size) + "]";
	}
	
	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		firePropertyChange("size", this.size, this.size = size);
	}


	public String getAxis() {
		return axis;
	}
	
	public int getDimension() {
		return dimension;
	}


	public void setAxis(String axis) {
		if (axis != null && axis.isEmpty()) axis = null;
		firePropertyChange("axis", this.axis, this.axis = axis);
	}


	public String[] getAxisOptions() {
		return axisOptions;
	}


	public void setAxisOptions(String[] axisOptions) {
		
		this.axisOptions = axisOptions;
	}


	public String getSecondaryAxis() {
		return secondaryAxis;
	}


	public void setSecondaryAxis(String secondaryAxis) {
		this.secondaryAxis = secondaryAxis;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName,
				listener);
	}

	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		propertyChangeSupport.firePropertyChange(propertyName, oldValue,
				newValue);
	}
	
}
