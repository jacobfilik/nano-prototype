package org.dawnsci.prototype.nano.model.ui;

import java.util.Arrays;

import org.dawnsci.prototype.nano.model.DataOptions;
import org.dawnsci.prototype.nano.model.FileController;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class DataOptionTableViewer {

	private TableViewer       tableViewer;
	private Image ticked;
	private Image unticked;
	private Composite tableComposite;
	
	public DataOptionTableViewer(){
		
	}
	
	public void dispose(){
		ticked.dispose();
		unticked.dispose();
	}
	
	public Control getControl() {
		return tableComposite;
	}
	
	public Table getTable() {
		return tableViewer.getTable();
	}
	
	public void addSelectionChangedListener(ISelectionChangedListener listener){
		tableViewer.addSelectionChangedListener(listener);
	}
	
	public IStructuredSelection getStructuredSelection() {
		return tableViewer.getStructuredSelection();
	}
	
	public void setInput(Object input){
		tableViewer.setInput(input);
	}
	
	public void refresh(){
		tableViewer.refresh();
	}
	
	public void setSelection(ISelection selection, boolean reveal){
		tableViewer.setSelection(selection, reveal);
	}
	
	public void createControl(Composite parent) {
		tableComposite = new Composite(parent, SWT.None);
		tableViewer = new TableViewer(tableComposite, SWT.FULL_SELECTION | SWT.BORDER);
		tableViewer.getTable().setHeaderVisible(true);
		
		ticked = AbstractUIPlugin.imageDescriptorFromPlugin("org.dawnsci.prototype.nano.model", "icons/ticked.png").createImage();
		unticked = AbstractUIPlugin.imageDescriptorFromPlugin("org.dawnsci.prototype.nano.model", "icons/unticked.gif").createImage();
		
		tableViewer.setContentProvider(new ArrayContentProvider());
		
		TableViewerColumn check   = new TableViewerColumn(tableViewer, SWT.CENTER, 0);
		check.setEditingSupport(new CheckBoxEditSupport(tableViewer));
		check.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return "";
			}
			
			@Override
			public Image getImage(Object element) {
				return ((DataOptions)element).isSelected() ? ticked : unticked;
			}
			
		});

		check.getColumn().setWidth(28);
		
		TableViewerColumn name = new TableViewerColumn(tableViewer, SWT.LEFT);
		name.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((DataOptions)element).getName();
			}
		});
		
		name.getColumn().setText("Dataset Name");
		name.getColumn().setWidth(200);
		
		TableViewerColumn shape = new TableViewerColumn(tableViewer, SWT.CENTER);
		shape.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return Arrays.toString(((DataOptions)element).getData().getShape());
			}
		});
		
		shape.getColumn().setText("Shape");
		shape.getColumn().setWidth(200);
		
		TableColumnLayout columnLayout = new TableColumnLayout();
	    columnLayout.setColumnData(check.getColumn(), new ColumnPixelData(24));
	    columnLayout.setColumnData(name.getColumn(), new ColumnWeightData(70,20));
	    columnLayout.setColumnData(shape.getColumn(), new ColumnWeightData(30,20));
	    
	    tableComposite.setLayout(columnLayout);
	}
	
	private class CheckBoxEditSupport extends EditingSupport {

		public CheckBoxEditSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			CheckboxCellEditor edit = new CheckboxCellEditor(getTable());
			edit.setValue(((DataOptions)element).isSelected());
			return edit;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof DataOptions) return ((DataOptions)element).isSelected();
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (element instanceof DataOptions && value instanceof Boolean){
				FileController.getInstance().setCurrentData((DataOptions)element, (Boolean)value);
			}
//			FileController.getInstance().setCurrentData((DataOptions)element, (Boolean)value);
		}
		
	}
	
}
