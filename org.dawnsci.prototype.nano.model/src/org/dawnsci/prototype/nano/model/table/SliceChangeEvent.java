package org.dawnsci.prototype.nano.model.table;

import java.util.EventObject;

import org.eclipse.dawnsci.analysis.api.dataset.SliceND;

public class SliceChangeEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;
	private SliceND slice;
	private Object[] options;
	private String[] axesNames;

	public SliceChangeEvent(SliceND slice, Object[] options, String[] axesNames) {
		super(slice);
		this.slice = slice;
		this.options = options;
		this.axesNames = axesNames;
	}

	public String[] getAxesNames() {
		return axesNames;
	}

	public Object[] getOptions() {
		return options;
	}

	public SliceND getSlice() {
		return slice;
	}

}
