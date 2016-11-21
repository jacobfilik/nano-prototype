package org.dawnsci.prototype.nano.model.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;

public class NDimensions {

	private static final String INDICES = "indices";
	
	private Dimension[] dimensions;
	private HashSet<ISliceChangeListener > listeners;
	private Object[] options = null;
	
	public NDimensions(int[] shape) {
		dimensions = new Dimension[shape.length];
		for (int i = 0; i < dimensions.length; i++) dimensions[i] = new Dimension(i, shape[i]);
		listeners = new HashSet<>();
	}
	
	public NDimensions(NDimensions toCopy) {
		this.dimensions = new Dimension[toCopy.dimensions.length];
		for (int i = 0; i < dimensions.length; i++) {
			dimensions[i] = new Dimension(toCopy.dimensions[i]);
		}
		this.options = toCopy.options != null ? toCopy.options.clone() : null;
		listeners = new HashSet<>();
	}
	
	public void setOptions(Object[] options) {
		this.options = options;
		int c = 0;
		for (int i = 0; i < dimensions.length; i++) {
			dimensions[i].setSlice(new Slice(0, 1, 1));
			dimensions[i].setDescription(null);
		}
		
		for (int i = dimensions.length-1 ; i >=0 ; i-- ) {
			if (c >= options.length) break;
			dimensions[i].setDescription(options[c++].toString());
			dimensions[i].setSlice(new Slice(0,dimensions[i].getSize()));
		}
		update(true);
	}
	
	public boolean areOptionsSet(){
		return options != null;
	}
	
	public String[] getDimensionOptions(){
		String[] o = new String[options.length+1];
		for (int i = 0; i < options.length; i++) o[i] = options[i].toString();
		o[options.length] = "";
		return o;
	}
	
	public int getRank() {
		return dimensions.length;
	}
	
	public Slice getSlice(int i) {
		return dimensions[i].getSlice();
	}
	
	public void setSlice(int i, Slice slice) {
		dimensions[i].setSlice(slice);
		update(false);
	}

	public String getDescription(int i) {
		return dimensions[i].getDescription();
	}
	
	public int getSize(int i) {
		return dimensions[i].getSize();
	}

	public String getAxis(int i) {
		return dimensions[i].getAxis();
	}
	
	public void setAxis(int i, String axis) {
		dimensions[i].setAxis(axis);
		update(false);
	}
	
	public String[] getAxisOptions(int i) {
		return dimensions[i].getAxisOptions();
	}

	public void setDescription(int i, String description) {
		updateDescription(dimensions[i], description);
//		dimensions[i].setDescription(description);
		update(false);
	}
	
	public void updateDimension(int i, String description, Slice slice) {
		dimensions[i].setDescription(description);
		dimensions[i].setSlice(slice);
		update(false);
		
	}
	
	public String getDimensionWithSize(int i) {
		return dimensions[i].getDimensionWithSize();
	}
	
	private void update(boolean optionChange) {
		SliceChangeEvent sliceChangeEvent = new SliceChangeEvent(new NDimensions(this), optionChange);
		fireSliceListeners(sliceChangeEvent);
	}
	
	private void updateDescription(Dimension dim, String desc) {
		String description = null;
		
		if (desc == null || desc.isEmpty()) {
			description = dim.getDescription();
			dim.setDescription(null);
			dim.setSlice(new Slice(1));
		} else if (dim.getDescription() ==null || dim.getDescription().isEmpty()) {
			dim.setSlice(new Slice(dim.getSize()));
		} else {
			description = dim.getDescription();
			for (int i = dimensions.length-1; i >= 0 ; i--) {
				if (dimensions[i].getDescription() == null || ((String)dimensions[i].getDescription()).isEmpty()) {
					if (dimensions[i] == dim) continue;
					else {
						dimensions[i].setDescription(description);
						dimensions[i].setSlice(new Slice(dimensions[i].getSize()));
						break;
					}
				}
			}
		}
		
		dim.setDescription(desc);

		for (Dimension d : dimensions) {
			if (d != dim && desc != null && desc.equals(d.getDescription())) {
				d.setDescription(null);
				d.setSlice(new Slice(1));
			}
		}
		
//		update();

	}
	
	private boolean validOptions(Object[] opts){
		
		List<Object> list = Arrays.asList(opts);
		
		boolean valid = true;
		
		for (Object o : options) {
			if (!list.contains(o)) {
				valid = false;
				break;
			}
			
			if (Collections.frequency(list, o) != 1) {
				valid = false;
				break;
			}
		}
		
		
		return valid;
	}
	
	public void setUpAxes(String name, Map<String,int[]> axes, String[] primary) {
		
		List<String>[] options = new List[dimensions.length];
		for (int i = 0 ; i < options.length; i++) {
			options[i] = new ArrayList<String>();
			if (primary != null && primary[i] != null) {
				options[i].add(primary[i]);
				dimensions[i].setAxis(primary[i]);
			} else {
				dimensions[i].setAxis(INDICES);
			}
			options[i].add(INDICES);
		}
		
		for (Entry<String,int[]> e : axes.entrySet()) {
			for (Integer i : e.getValue()) {
				for (int j = 0; j < dimensions.length ; j++) {
					if (dimensions[j].getSize() == i && !e.getKey().equals(name)) options[j].add(e.getKey());
				}	
			}	
		}
		
		for (int i = 0 ; i < dimensions.length ; i++) {
			dimensions[i].setAxisOptions(options[i].toArray(new String[options[i].size()]));
		}
		
	}
	
	public SliceND buildSliceND() {
		int[] shape = new int[dimensions.length];
		for (int i = 0; i < dimensions.length;i++) shape[i] = dimensions[i].getSize();
		SliceND slice = new SliceND(shape);
		for (int i = 0; i < dimensions.length;i++) {
			Slice s = dimensions[i].getSlice();
			slice.setSlice(i, s.getStart(), s.getStop(), s.getStep());
		}
		return slice;
	}
	
	public void addSliceListener(ISliceChangeListener listener) {
		listeners.add(listener);
	}

	public void removeSliceListener(ISliceChangeListener listener) {
		listeners.remove(listener);
	}

	private void fireSliceListeners(SliceChangeEvent event) {
		for (ISliceChangeListener listener : listeners)
			listener.sliceChanged(event);
	}
	
	public Object[] getOptions(){
		Object[] object = new Object[dimensions.length];

		for (int i = 0; i < dimensions.length; i++) {
			object[i] = dimensions[i].getDescription();
		}

		return object;
	}
	
	public String[] buildAxesNames() {
		String[] names = new String[dimensions.length];
		for (int i = 0; i < dimensions.length;i++) {
			String name = dimensions[i].getAxis();
			if (INDICES.equals(name)) name = "";
			names[i] = name;
		}

		return names;
	}

}
