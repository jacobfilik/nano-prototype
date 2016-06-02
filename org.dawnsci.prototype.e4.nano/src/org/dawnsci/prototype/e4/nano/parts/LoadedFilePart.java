/*******************************************************************************
 * Copyright (c) 2010 - 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <lars.Vogel@gmail.com> - Bug 419770
 *******************************************************************************/
package org.dawnsci.prototype.e4.nano.parts;


import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.dawnsci.prototype.e4.nano.model.DataOptions;
import org.dawnsci.prototype.e4.nano.model.FileTreeContentProvider;
import org.dawnsci.prototype.e4.nano.model.FileTreeLabelProvider;
import org.dawnsci.prototype.e4.nano.model.IPlotMode;
import org.dawnsci.prototype.e4.nano.model.LoadedFile;
import org.dawnsci.prototype.e4.nano.model.LoadedFiles;
import org.dawnsci.prototype.e4.nano.model.PlotManager;
import org.dawnsci.prototype.e4.nano.model.PlotModeImage;
import org.dawnsci.prototype.e4.nano.model.PlotModeXY;
import org.dawnsci.prototype.e4.nano.table.DataConfigurationTable;
import org.dawnsci.prototype.e4.nano.table.ISliceChangeListener;
import org.dawnsci.prototype.e4.nano.table.SliceChangeEvent;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

public class LoadedFilePart {

	private DataConfigurationTable table;
	private TreeViewer viewer;
	private ComboViewer optionsViewer;
	private PlotManager plotManager;
	private LoadedFiles loadedFiles;
	
	@Inject ILoaderService lService;

	@PostConstruct
	public void createComposite(Composite parent, IPlottingService pService) {
		
		loadedFiles = new LoadedFiles();
		plotManager = new PlotManager(pService);
		
		try {
			LoadedFile f = new LoadedFile(lService.getData("/home/jacobfilik/Work/data/exampleFPA.nxs",null));
			loadedFiles.addFile(f);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		viewer = new TreeViewer(parent);
		viewer.setContentProvider(new FileTreeContentProvider());
		viewer.setLabelProvider(new FileTreeLabelProvider());
		ColumnViewerToolTipSupport.enableFor(viewer);
		viewer.setInput(loadedFiles);
		
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ISelection s = event.getSelection();
				if (s instanceof StructuredSelection) {
					Object e = ((StructuredSelection)s).getFirstElement();
					if (e instanceof DataOptions) {
						DataOptions dOp = (DataOptions)e;
						plotManager.setDataOption(dOp);
						table.setInput(dOp.getData().getShape(), plotManager.getCurrentMode().getOptions(),dOp.getAllPossibleAxes(),(String)null);
					}
				}
			}
		});
		
		//Comment
		optionsViewer = new ComboViewer(parent);
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
						table.setInput(dOp.getData().getShape(), plotManager.getCurrentMode().getOptions(),dOp.getAllPossibleAxes(),(String)null);
					}
				}
			}
		});
		
		
		table = new DataConfigurationTable();
		table.createControl(parent);
		//TODO update table lazy
		table.addSliceListener(new ISliceChangeListener() {
			
			@Override
			public void sliceChanged(SliceChangeEvent event) {
				plotManager.getDataOption().setAxes(event.getAxesNames());
				plotManager.getPlottingSystem().clearTraces();
				Object[] options = event.getOptions();
				ITrace t = plotManager.getCurrentMode().buildTraces(plotManager.getDataOption().getData(),
						event.getSlice(), event.getOptions(), plotManager.getPlottingSystem())[0];
				if (t == null) return;
				
				plotManager.getPlottingSystem().addTrace(t);
//				plotManager.getPlottingSystem().repaint();

				//				boolean transpose = false;
//				for (int i = 0; i < options.length; i++) {
//					if (options[i] != null && !((String)options[i]).isEmpty()) {
//						if (options[i].equals("Y")) {
//							transpose = false;
//							break;
//						} else {
//							transpose = true;
//							break;
//						}
//					}
//				}
//				plotManager.plotData(event.getSlice(), transpose);
				
			}
		});
	}
	

	@Focus
	public void setFocus() {
		table.setFocus();
	}
	
	@Inject
	@Optional
	private void subscribeFileOpen(@UIEventTopic("orgdawnsciprototypee4nano") String path) {
	  try {
			LoadedFile f = new LoadedFile(lService.getData(path,null));
			loadedFiles.addFile(f);
			viewer.refresh();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	} 


}