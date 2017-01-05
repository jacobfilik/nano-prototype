package org.dawnsci.prototype.nano.model.table;

import org.eclipse.january.dataset.Slice;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class DataConfigurationTable {

	private TableViewer       tableViewer;
	private TableViewerColumn options;
	private TableViewerColumn slice;
	private NDimensions nDimension;
	
//	private HashSet<ISliceChangeListener > listeners;
	
	public DataConfigurationTable() {
//		listeners = new HashSet<>();
	}
	
	public void createControl(Composite parent) {
		
		tableViewer = new TableViewer(parent, SWT.FULL_SELECTION |SWT.NO_FOCUS| SWT.BORDER);
		
		final TableViewerColumn dim   = new TableViewerColumn(tableViewer, SWT.LEFT, 0);
		dim.getColumn().setText("Dimension");
		dim.getColumn().setWidth(80);
		dim.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return nDimension.getDimensionWithSize((int)element);
			}
		});
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
			@Override
			public void dispose() {
			}
			
			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof NDimensions) {
					Integer[] vals = new Integer[((NDimensions)inputElement).getRank()];
					for (int i = 0; i < vals.length; i++) vals[i] = i;
					return vals;
				}
				return null;
			}
		});
		
		tableViewer.getTable().setHeaderVisible(true);
		
		options = new TableViewerColumn(tableViewer, SWT.CENTER, 1);
		options.getColumn().setText("Display");
		options.getColumn().setWidth(60);
		options.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String description = nDimension.getDescription((int)element);
			  return description == null ? "" : description;
			}
		});
		
		slice = new TableViewerColumn(tableViewer, SWT.CENTER, 2);
		slice.getColumn().setText("Start:Stop:Step");
		slice.getColumn().setWidth(120);
		slice.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
			  Slice slice = nDimension.getSlice((int)element);
			  return slice == null ? "" : slice.toString();
			}
		});
		
		slice.setEditingSupport(new SliceEditingSupport(tableViewer));

		final TableViewerColumn axis   = new TableViewerColumn(tableViewer, SWT.CENTER, 3);
		axis.getColumn().setText("Axes");
		axis.getColumn().setWidth(120);
		axis.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String axes = nDimension.getAxis((int)element);
				
			  return axes == null ? "" : axes;
			}
		});
		
		axis.setEditingSupport(new AxisEditSupport(tableViewer));
		
	}
	
	public void setLayoutData(Object layoutData) {
		tableViewer.getTable().setLayoutData(layoutData);
	}
	
	public void setInput(NDimensions ndims) {
		nDimension = ndims;
		options.setEditingSupport(new DimensionEditSupport(tableViewer,ndims));
		tableViewer.setInput(ndims);
		tableViewer.getTable().getParent().layout();
	}
	
	public void clearAll() {
		tableViewer.getTable().clearAll();
	}
	
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}

	public Control getControl() {
		return tableViewer.getControl();
	}
	
}
