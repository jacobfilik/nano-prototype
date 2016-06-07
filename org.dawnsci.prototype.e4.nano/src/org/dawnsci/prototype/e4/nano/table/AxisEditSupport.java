package org.dawnsci.prototype.e4.nano.table;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

public class AxisEditSupport extends EditingSupport {
	
	public AxisEditSupport(ColumnViewer viewer) {
		super(viewer);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		final ComboBoxViewerCellEditor axisEditor = new ComboBoxViewerCellEditor((Composite) getViewer().getControl(), SWT.READ_ONLY);
		axisEditor.setLabelProvider(new LabelProvider());
		axisEditor.setContentProvider(new ArrayContentProvider());
		axisEditor.setActivationStyle(ComboBoxViewerCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
		axisEditor.setInput(((Dimension)element).getAxisOptions());
		
//		axisEditor.getViewer().getCCombo().addSelectionListener(new SelectionAdapter() {
//
//			@Override
//			public void widgetSelected(SelectionEvent event) {
//				ColumnViewer viewer2 = getViewer();
////				ISelection selection = getViewer().getSelection();
////				if (!selection.isEmpty()) {
////					CCombo cCombo = axisEditor.getViewer().getCCombo();
////					String text = cCombo.getText();
////					AxisEditSupport.this.setValue(((StructuredSelection)selection).getFirstElement(), text);
////					getViewer().refresh();
////					
////				}
//			}
//		});
		
		return axisEditor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		Dimension dim = (Dimension)element;
		return dim.getAxis();
	}

	@Override
	protected void setValue(Object element, Object value) {
		Dimension dim = (Dimension)element;
		dim.setAxis((String)value);
		getViewer().refresh();

	}

}
