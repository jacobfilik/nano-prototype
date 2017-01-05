package org.dawnsci.prototype.nano.model.ui;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.dawnsci.prototype.nano.model.DataOptions;
import org.dawnsci.prototype.nano.model.FileController;
import org.dawnsci.prototype.nano.model.FileControllerStateEvent;
import org.dawnsci.prototype.nano.model.FileControllerStateEventListener;
import org.dawnsci.prototype.nano.model.IPlotMode;
import org.dawnsci.prototype.nano.model.LoadedFile;
import org.dawnsci.prototype.nano.model.PlotManager;
import org.dawnsci.prototype.nano.model.PlotModeChangeEventListener;
import org.dawnsci.prototype.nano.model.PlotModeEvent;
import org.dawnsci.prototype.nano.model.table.DataConfigurationTable;
import org.dawnsci.prototype.nano.model.table.ISliceChangeListener;
import org.dawnsci.prototype.nano.model.table.NDimensions;
import org.dawnsci.prototype.nano.model.table.SliceChangeEvent;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class DatasetPart {
	
	@Inject
	ESelectionService selectionService;
	
	private DataConfigurationTable table;
	private ComboViewer optionsViewer;
	private PlotManager plotManager;
	
	private CheckboxTableViewer viewer;
	
	@PostConstruct
	public void createComposite(Composite parent, IPlottingService pService) {
		
		plotManager = new PlotManager(pService);

		parent.setLayout(new FormLayout());
		FormData checkForm = new FormData();
		checkForm.top = new FormAttachment(0,0);
		checkForm.left = new FormAttachment(0,0);
		checkForm.right = new FormAttachment(100,0);
		checkForm.bottom = new FormAttachment(75,0);
		viewer = CheckboxTableViewer.newCheckList(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.getTable().setLayoutData(checkForm);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new ViewLabelLabelProvider());
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = viewer.getStructuredSelection();
				selectionService.setSelection(selection.getFirstElement());
				if (selection.getFirstElement() instanceof DataOptions) {
					DataOptions op = (DataOptions)selection.getFirstElement();
					updateOnSelectionChange(op);
				}
				
			}
		});
	FormData comboForm = new FormData();
	comboForm.top = new FormAttachment(viewer.getControl());
	comboForm.left = new FormAttachment(0,0);
	comboForm.right = new FormAttachment(100,0);
	
	optionsViewer = new ComboViewer(parent);
	table = new DataConfigurationTable();
	table.createControl(parent);
	
	optionsViewer.getCombo().setLayoutData(comboForm);
	optionsViewer.setContentProvider(new ArrayContentProvider());
	optionsViewer.setLabelProvider(new LabelProvider() {
		@Override
		public String getText(Object element) {
			String name = "";
			
			if (element instanceof IPlotMode) {
				name = ((IPlotMode)element).getName();
			}
			
			return name;
		}
	});
		 
		optionsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof StructuredSelection) {
					Object ob = ((StructuredSelection)selection).getFirstElement();
					if (ob instanceof IPlotMode) {
						plotManager.switchPlotMode((IPlotMode)ob);
						NDimensions nd = FileController.getInstance().getNDimensions();
						nd.setOptions(((IPlotMode)ob).getOptions());
						table.setInput(nd);
						viewer.setCheckedElements(FileController.getInstance().getCurrentFile().getChecked().toArray());
						viewer.refresh();

					}
				}
			}
		});
		
		
		FormData tableForm = new FormData();
		tableForm.top = new FormAttachment(optionsViewer.getCombo());
		tableForm.left = new FormAttachment(0,0);
		tableForm.right = new FormAttachment(100,0);
		tableForm.bottom = new FormAttachment(100,0);
		
		table.setLayoutData(tableForm);
		
		FileController.getInstance().addStateListener(new FileControllerStateEventListener() {
			
			@Override
			public void stateChanged(FileControllerStateEvent event) {
			
				if (event.isSelectedFileChanged()) {
					LoadedFile currentFile = FileController.getInstance().getCurrentFile();
					if (currentFile == null) {
						viewer.setInput(null);
						table.setInput(null);
						optionsViewer.setInput(null);
						return;
					}
					List<DataOptions> dataOptions = currentFile.getDataOptions();
					viewer.setInput(dataOptions.toArray());
					viewer.setCheckedElements(currentFile.getChecked().toArray());
//					
					if (FileController.getInstance().getCurrentDataOption() != null) {
						DataOptions op = FileController.getInstance().getCurrentDataOption();
						viewer.setSelection(new StructuredSelection(op),true);
					}
					
					
					viewer.refresh();
				}
				
			}
		});
		
		plotManager.addPlotModeListener(new PlotModeChangeEventListener() {
			
			@Override
			public void plotModeChanged(PlotModeEvent event) {
				viewer.setCheckedElements(FileController.getInstance().getCurrentFile().getChecked().toArray());
				viewer.refresh();
				IPlotMode[] suitableModes = event.getPossibleModes();
				optionsViewer.setInput(suitableModes);
				optionsViewer.setSelection(new StructuredSelection(event.getMode()));
				
			}
		});
		
	}
	
	private void updateOnSelectionChange(DataOptions op){
		boolean checked = false;
		for (Object o : viewer.getCheckedElements()) {
			if (op.equals(o)) {
				checked = true;
				break;
			}
		}
		FileController.getInstance().setCurrentData(op,checked);
	}
	
	@Focus
	public void setFocus() {
		if (viewer != null) viewer.getControl().setFocus();
	}
	
	class ViewLabelLabelProvider extends StyledCellLabelProvider {
		
		@Override
	    public void update(ViewerCell cell) {
	      Object element = cell.getElement();
	      StyledString text = new StyledString();
	      text.append(((DataOptions)element).getName() + " " + Arrays.toString(((DataOptions)element).getData().getShape()));
	      cell.setText(text.toString());
	      super.update(cell);
		}
	}


}
