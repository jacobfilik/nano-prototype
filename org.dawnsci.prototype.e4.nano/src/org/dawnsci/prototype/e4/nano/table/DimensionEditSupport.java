package org.dawnsci.prototype.e4.nano.table;


import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

public class DimensionEditSupport extends EditingSupport {

	private ComboBoxViewerCellEditor dimensionEditor = null;
	private boolean unique = true;
	private Dimension[] dimensions;
	
	public DimensionEditSupport(ColumnViewer viewer, String[] options, Dimension[] dimensions) {
		super(viewer);
		this.dimensions = dimensions;
		dimensionEditor = new ComboBoxViewerCellEditor((Composite) getViewer().getControl());
		dimensionEditor.setLabelProvider(new LabelProvider());
		dimensionEditor.setContentProvider(new ArrayContentProvider());
		dimensionEditor.setActivationStyle(ComboBoxViewerCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
		dimensionEditor.setInput(options);
		dimensionEditor.getViewer().getCCombo().addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = viewer.getSelection();
				if (!selection.isEmpty()) {
					CCombo cCombo = dimensionEditor.getViewer().getCCombo();
					String text = cCombo.getText();
					DimensionEditSupport.this.setValue(((StructuredSelection)selection).getFirstElement(), text);
					
				}
			}
		});
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return dimensionEditor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		Dimension dim = (Dimension)element;
		return dim.getDescription();
	}

	@Override
	protected void setValue(Object element, Object value) {
		Dimension dim = (Dimension)element;
		String strval = (String)value;
		String description = null;
		
		if (strval == null || strval.isEmpty()) {
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
		
		
		dim.setDescription(strval);
		
		if (unique) {
			for (Dimension d : dimensions) {
				if (d != dim && strval != null && strval.equals(d.getDescription())) {
					d.setDescription(null);
					d.setSlice(new Slice(1));
				}
			}
		}
		
		
		getViewer().refresh();
	}

}
