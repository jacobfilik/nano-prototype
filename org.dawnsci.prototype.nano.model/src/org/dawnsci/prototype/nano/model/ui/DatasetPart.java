package org.dawnsci.prototype.nano.model.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.dawnsci.prototype.nano.model.DataOptions;
import org.dawnsci.prototype.nano.model.IPlotMode;
import org.dawnsci.prototype.nano.model.LoadedFile;
import org.dawnsci.prototype.nano.model.PlotManager;
import org.dawnsci.prototype.nano.model.PlottableObject;
import org.dawnsci.prototype.nano.model.table.DataConfigurationTable;
import org.dawnsci.prototype.nano.model.table.ISliceChangeListener;
import org.dawnsci.prototype.nano.model.table.NDimensions;
import org.dawnsci.prototype.nano.model.table.SliceChangeEvent;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
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
	private LoadedFile currentFile;
	private DataOptions currentOptions;
	private ComboViewer optionsViewer;
	private PlotManager plotManager;
	private ISliceChangeListener listener;
	
	private CheckboxTableViewer viewer;
	
	@PostConstruct
	public void createComposite(Composite parent, IPlottingService pService, EventAdmin eventAdmin) {
		
		plotManager = new PlotManager(pService, eventAdmin);
		listener = getListener();

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
		
		optionsViewer.setInput(plotManager.getPlotModes());
		optionsViewer.getCombo().select(0);
		optionsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof StructuredSelection) {
					Object ob = ((StructuredSelection)selection).getFirstElement();
					if (ob instanceof IPlotMode) {
						updatePlotMode((IPlotMode)ob);
					}
				}
			}
		});
		
		
		table = new DataConfigurationTable();
		table.createControl(parent);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		
	}
	
	
	public ISliceChangeListener getListener() {

		return new ISliceChangeListener() {

			@Override
			public void sliceChanged(SliceChangeEvent event) {
				update(event.getSource());
				
			};
		};
	}
	
	private NDimensions buildNDimensions(DataOptions op) {
		NDimensions ndims = new NDimensions(op.getData().getShape());
		ndims.setUpAxes((String)null, op.getAllPossibleAxes(), op.getPrimaryAxes());
		if (op.isSelected()) ndims.addSliceListener(listener);
		ndims.setOptions(plotManager.getCurrentMode().getOptions());
		return ndims;
	}
	
	private void updatePlotMode(IPlotMode mode) {
		plotManager.setCurrentMode(mode);
		
		if (currentOptions.getPlottableObject() == null || currentOptions.getPlottableObject().getNDimensions() == null || !currentOptions.getPlottableObject().getPlotMode().equals(plotManager.getCurrentMode())) {
			table.setInput(buildNDimensions(currentOptions));
		} else {
			table.setInput(currentOptions.getPlottableObject().getNDimensions());
		}
		//update viewer to reflect selected options compatible with plot mode
		viewer.setCheckedElements(currentFile.getChecked().toArray());
		viewer.refresh();
	}
	
	private void updateOnSelectionChange(DataOptions op){
		boolean checked = false;
		for (Object o : viewer.getCheckedElements()) {
			if (op.equals(o)) {
				checked = true;
				break;
			}
		}
		op.setSelected(checked);
		
		plotManager.setCurrentData(op);
		currentOptions = op;
		NDimensions ndims = null;
		if (op.getPlottableObject() != null) {
			optionsViewer.setSelection(new StructuredSelection(op.getPlottableObject().getPlotMode()));
			ndims =op.getPlottableObject().getNDimensions();
			if (!checked || !currentFile.isSelected()) {
				plotManager.removeFromPlot(op.getPlottableObject());
				if (!checked && !plotManager.getCurrentMode().supportsMultiple() && op.getPlottableObject() != null && op.getPlottableObject().getCachedTraces() != null) {
					op.getPlottableObject().setCachedTraces(null);
					plotManager.setCurrentMode(plotManager.getCurrentMode());
				}
			} else {
				ndims.addSliceListener(listener);
				plotManager.addToPlot(op.getPlottableObject());
			}
		} else {
			ndims = buildNDimensions(op);
		}
		table.setInput(ndims);
	}
	
	private void update(NDimensions dimensions) {
//		currentOptions.setPlottableObject(new PlottableObject(plotManager.getCurrentMode(), dimensions));
		if (!currentFile.isSelected()) return;
		plotManager.updatePlot(dimensions,currentOptions);
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
				Object property = data.getProperty("file");
				
				if (property == null) {
					viewer.setInput(null);
					return;
				}
				
				currentFile = (LoadedFile)property;
				
				plotManager.setCurrentFile(currentFile);
				
				List<DataOptions> dataOptions = currentFile.getDataOptions();
				viewer.setInput(dataOptions.toArray());
				viewer.setCheckedElements(currentFile.getChecked().toArray());
				
				if (plotManager.getCurrentDataOption() != null) {
					DataOptions op = plotManager.getCurrentDataOption();
					viewer.setSelection(new StructuredSelection(op),true);
					if (op.getPlottableObject() != null) {
						PlottableObject po = op.getPlottableObject();
						po.getNDimensions().addSliceListener(listener);
						optionsViewer.setSelection(new StructuredSelection(po.getPlotMode()));
						table.setInput(po.getNDimensions());
					}
				}
				
				
				viewer.refresh();
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
