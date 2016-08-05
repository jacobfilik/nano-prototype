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
import org.eclipse.e4.ui.di.Focus;
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

public class DatasetPart {
	
	@Inject
	ESelectionService selectionService;
	
	private DataConfigurationTable table;
	private LoadedFile currentFile;
	private ComboViewer optionsViewer;
	private PlotManager plotManager;
	private ISliceChangeListener listener;
	
	private CheckboxTableViewer viewer;
	
	@PostConstruct
	public void createComposite(Composite parent, IPlottingService pService) {
		
		plotManager = new PlotManager(pService);
		listener = getListener();

		parent.setLayout(new GridLayout(1, true));
		
		viewer = CheckboxTableViewer.newCheckList(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new StyledCellLabelProvider() {
			@Override
		    public void update(ViewerCell cell) {
		      Object element = cell.getElement();
		      StyledString text = new StyledString();
		      text.append(((DataOptions)element).getName() + " " + Arrays.toString(((DataOptions)element).getData().getShape()));
		      cell.setText(text.toString());
		      super.update(cell);
			}
		});
		
		viewer.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				IStructuredSelection selection = viewer.getStructuredSelection();
				selectionService.setSelection(selection.getFirstElement());
				if (selection.getFirstElement() instanceof DataOptions) {
					DataOptions dOp = (DataOptions)selection.getFirstElement();
					dOp.setSelected(event.getChecked());
					if (event.getChecked()) {
							
							plotManager.setDataOption(dOp);
							NDimensions ndims = new NDimensions(dOp.getData().getShape());
							ndims.setUpAxes((String)null, dOp.getAllPossibleAxes(), dOp.getPrimaryAxes());
							plotManager.getPlottingSystem().reset();
							ndims.addSliceListener(listener);
							ndims.setOptions(plotManager.getCurrentMode().getOptions());
							table.setInput(ndims);

					}
//					if (currentFile.contains(name)) {
//						currentFile.addyDatasetName(name);
//						for (ISpectrumFile file : otherFiles) {
//							if (file.contains(name)) file.addyDatasetName(name);
//						}
//					}
				} else {
//					if (currentFile.contains(name)) {
//						currentFile.removeyDatasetName(name);
//						for (ISpectrumFile file : otherFiles) {
//							if (file.contains(name)) file.removeyDatasetName(name);
//						}
//					}
				}
			}
		});
		
		selectionService.addSelectionListener("org.dawnsci.prototype.nano.model.ui.LoadedFilePart", new ISelectionListener() {
			
			@Override
			public void selectionChanged(MPart part, Object selection) {
				if (selection instanceof LoadedFile) {
					currentFile = (LoadedFile)selection;
					List<DataOptions> dataOptions = ((LoadedFile)selection).getDataOptions();
					viewer.setInput(dataOptions.toArray());
					List<DataOptions> checked = new ArrayList<>();
					for (DataOptions op : dataOptions) {
						if (op.getPlottableObject() != null) {
							PlottableObject po = op.getPlottableObject();
							po.getNDimensions().addSliceListener(listener);
							table.setInput(po.getNDimensions());
							NDimensions nd = po.getNDimensions();
							plotManager.setDataOption(op);
							update(nd,nd.buildAxesNames(),nd.buildSliceND(),nd.getOptions());
							
						}
						if (op.isSelected()) {
							checked.add(op);
						}
					}
					viewer.setCheckedElements(checked.toArray());
					viewer.refresh();
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
					StructuredSelection ss = (StructuredSelection)selection;
					Object ob = ss.getFirstElement();
					if (ob instanceof IPlotMode) {
						plotManager.setCurrentMode((IPlotMode)ob);
						DataOptions dOp = plotManager.getDataOption();
						NDimensions ndims = new NDimensions(dOp.getData().getShape());
						ndims.setUpAxes((String)null, dOp.getAllPossibleAxes(), dOp.getPrimaryAxes());
						plotManager.getPlottingSystem().reset();
						ndims.addSliceListener(listener);
						ndims.setOptions(plotManager.getCurrentMode().getOptions());
						table.setInput(ndims);
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
				update(event.getSource(),event.getAxesNames(),event.getSlice(),event.getOptions());
				
			};
		};
	}
	
	private void update(NDimensions dimensions, String[] axes, SliceND slice, Object[] options) {
		if (!currentFile.isSelected()) return;
		PlottableObject pO = plotManager.getDataOption().getPlottableObject();
		if (pO != null)plotManager.getPlottingSystem().removeTrace(pO.getCachedTrace());
		plotManager.getDataOption().setAxes(axes);
		
		SourceInformation si = new SourceInformation(plotManager.getDataOption().getFileName(), plotManager.getDataOption().getName(), plotManager.getDataOption().getData());
		SliceInformation s = new SliceInformation(slice, slice, new SliceND(plotManager.getDataOption().getData().getShape()), new int[]{0,1}, 1, 0);
		SliceFromSeriesMetadata md = new SliceFromSeriesMetadata(si, s);
		ITrace[] t = null;
		try {
			t = plotManager.getCurrentMode().buildTraces(plotManager.getDataOption().getData(),
					slice, options, plotManager.getPlottingSystem());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (t == null) return;
		t[0].getData().setMetadata(md);
		if (t[0] instanceof ISurfaceTrace) {
			plotManager.getPlottingSystem().setPlotType(PlotType.SURFACE);
		}
		plotManager.getPlottingSystem().addTrace(t[0]);
		plotManager.getPlottingSystem().autoscaleAxes();
		PlottableObject po = new PlottableObject(plotManager.getCurrentMode(), dimensions);
		po.setCachedTrace(t[0]);
				
		plotManager.getDataOption().setPlottableObject(po);
	}
	
	@Focus
	public void setFocus() {
		if (viewer != null) viewer.getControl().setFocus();
	}
	
//	@Inject
//	public void setFile(@Optional 
//	    @Named(IServiceConstants.ACTIVE_SELECTION) LoadedFile todo) {
//	  if (todo != null) {
//	    // do something with the value
//	  }
//	} 
	
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
//			
//			if (parent instanceof Map<?,?>) {
//
//				return ((Map<?,?>)parent).keySet().toArray();
//			}
			
			return (Object[])parent;
		}
	}



}
