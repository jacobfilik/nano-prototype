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

		parent.setLayout(new GridLayout(1, true));
		
		viewer = CheckboxTableViewer.newCheckList(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
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
		
	optionsViewer = new ComboViewer(parent);
	optionsViewer.getCombo().setLayoutData(new GridData());
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
		
		
		table = new DataConfigurationTable();
		table.createControl(parent);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		
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
		IPlotMode[] suitableModes = plotManager.getCurrentPlotModes();
		optionsViewer.setInput(suitableModes);
		optionsViewer.setSelection(new StructuredSelection(plotManager.getCurrentMode()));
	}
	
	@Focus
	public void setFocus() {
		if (viewer != null) viewer.getControl().setFocus();
	}
	
	@Inject
	@Optional
	private void subscribeFileEvent(@UIEventTopic("org/dawnsci/prototype/file/update")  Event data) {
	  try {
			if (data != null && data.containsProperty("file")) {
//				Object property = data.getProperty("file");
//				
//				if (property == null) {
//					viewer.setInput(null);
//					return;
//				}
//				
//				LoadedFile currentFile = (LoadedFile)property;
//				
//				FileController.getInstance().setCurrentFile(currentFile);
//				
//				List<DataOptions> dataOptions = currentFile.getDataOptions();
//				viewer.setInput(dataOptions.toArray());
//				viewer.setCheckedElements(currentFile.getChecked().toArray());
////				
//				if (FileController.getInstance().getCurrentDataOption() != null) {
//					DataOptions op = FileController.getInstance().getCurrentDataOption();
//					viewer.setSelection(new StructuredSelection(op),true);
//				}
//				
//				
//				viewer.refresh();
			}
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
