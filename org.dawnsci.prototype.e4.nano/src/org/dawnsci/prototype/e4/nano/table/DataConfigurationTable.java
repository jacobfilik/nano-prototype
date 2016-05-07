package org.dawnsci.prototype.e4.nano.table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class DataConfigurationTable {

	private TableViewer       tableViewer;
	private TableViewerColumn options;
	private TableViewerColumn slice;
	
	private HashSet<ISliceChangeListener > listeners;
	
	public DataConfigurationTable() {
		listeners = new HashSet<>();
	}
	
	public void createControl(Composite parent) {
		
		tableViewer = new TableViewer(parent, SWT.FULL_SELECTION |SWT.NO_FOCUS);
		
		final TableViewerColumn dim   = new TableViewerColumn(tableViewer, SWT.LEFT, 0);
		dim.getColumn().setText("Dimension");
		dim.getColumn().setWidth(80);
		dim.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Dimension dim = (Dimension)element;
			  return dim.getDimensionWithSize();
			}
		});
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.getTable().setHeaderVisible(true);
		
		options = new TableViewerColumn(tableViewer, SWT.CENTER, 1);
		options.getColumn().setText("Display");
		options.getColumn().setWidth(60);
		options.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Dimension dim = (Dimension)element;
			  return dim.getDescription() == null ? "" : dim.getDescription();
			}
		});
		
		slice = new TableViewerColumn(tableViewer, SWT.CENTER, 2);
		slice.getColumn().setText("Start:Stop:Step");
		slice.getColumn().setWidth(120);
		slice.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Dimension dim = (Dimension)element;
			  return dim.getSlice() == null ? "" : dim.getSlice().toString();
			}
		});
		
		slice.setEditingSupport(new SliceEditingSupport(tableViewer));

		final TableViewerColumn axis   = new TableViewerColumn(tableViewer, SWT.CENTER, 3);
		axis.getColumn().setText("Axes");
		axis.getColumn().setWidth(120);
		axis.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Dimension dim = (Dimension)element;
			  return dim.getAxis() == null ? "" : dim.getAxis();
			}
		});
		
		axis.setEditingSupport(new AxisEditSupport(tableViewer));
		
		
	}
	
	public void setLayout(Object layoutData) {
		tableViewer.getTable().setLayoutData(layoutData);
	}
	
	public void setInput(String[] opt,Dimension[] dims) {
		
		int c = 0;
		for (int i = dims.length -1 ; i >=0 ; i-- ) {
			if (c >= opt.length) break;
			dims[i].setDescription(opt[c++]);
			dims[i].setSlice(new Slice(0,dims[i].getSize()-1));
		}
		
		for (Dimension d : dims) d.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				onUpdate();
			}
		});
		
		options.setEditingSupport(new DimensionEditSupport(tableViewer,opt,dims));
		tableViewer.setInput(dims);
		tableViewer.getTable().getParent().layout();
	}
	
	public void setInput(int[] shape, String[] options, Map<String,int[]> axes, String name) {
		Dimension[] d = new Dimension[shape.length];
		for (int i = 0; i < d.length; i++) d[i] = new Dimension(i, shape[i]);
		
		setUpAxes(d, axes, name);
		
		setInput(options, d);
	}
	
	private void setUpAxes(Dimension[] dims, Map<String,int[]> axes, String name) {
		
		List<String>[] options = new List[dims.length];
		for (int i = 0 ; i < options.length; i++) {
			options[i] = new ArrayList<String>();
			options[i].add("");
		}
		
		for (Entry<String,int[]> e : axes.entrySet()) {
			for (Integer i : e.getValue()) {
				for (int j = 0; j < dims.length ; j++) {
					if (dims[j].getSize() == i && !e.getKey().equals(name)) options[j].add(e.getKey());
				}	
			}	
		}
		
		for (int i = 0 ; i < options.length; i++) {
			options[i].add("");
		}
		
		for (int i = 0 ; i < dims.length ; i++) {
			dims[i].setAxisOptions(options[i].toArray(new String[options[i].size()]));
		}
		
	}
	
	public void clearAll() {
		tableViewer.getTable().clearAll();
	}
	
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}
	
	private void onUpdate() {
		Dimension[] input = (Dimension[])tableViewer.getInput();
		if (input == null) return;
		SliceND s = buildSliceND(input);
		Object[] object = new Object[input.length];
		for (int i = 0; i < input.length; i++) object[i] = input[i].getDescription();
		
		fireFileLoadedListeners(new SliceChangeEvent(s, object, buildAxesNames(input)));
		
		
	}
	
	private SliceND buildSliceND(Dimension[] dims) {
		int[] shape = new int[dims.length];
		for (int i = 0; i < dims.length;i++) shape[i] = dims[i].getSize();
		SliceND slice = new SliceND(shape);
		for (int i = 0; i < dims.length;i++) {
			Slice s = dims[i].getSlice();
			slice.setSlice(i, s.getStart(), s.getStop(), s.getStep());
		}
		return slice;
	}
	
	private String[] buildAxesNames(Dimension[] dims) {
		String[] names = new String[dims.length];
		for (int i = 0; i < dims.length;i++) names[i] = dims[i].getAxis();

		return names;
	}
	
	public void addSliceListener(ISliceChangeListener listener) {
		listeners.add(listener);
	}

	public void removeFileListener(ISliceChangeListener listener) {
		listeners.remove(listener);
	}

	private void fireFileLoadedListeners(SliceChangeEvent event) {
		for (ISliceChangeListener listener : listeners)
			listener.sliceChanged(event);
	}
	
}
