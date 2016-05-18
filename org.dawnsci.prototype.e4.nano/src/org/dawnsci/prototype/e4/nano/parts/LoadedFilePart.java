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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.dawnsci.prototype.e4.nano.model.DataOptions;
import org.dawnsci.prototype.e4.nano.model.FileTreeContentProvider;
import org.dawnsci.prototype.e4.nano.model.FileTreeLabelProvider;
import org.dawnsci.prototype.e4.nano.model.LoadedFile;
import org.dawnsci.prototype.e4.nano.model.LoadedFiles;
import org.dawnsci.prototype.e4.nano.model.PlotManager;
import org.dawnsci.prototype.e4.nano.table.DataConfigurationTable;
import org.dawnsci.prototype.e4.nano.table.Dimension;
import org.dawnsci.prototype.e4.nano.table.ISliceChangeListener;
import org.dawnsci.prototype.e4.nano.table.SliceChangeEvent;
import org.eclipse.core.internal.content.LazyReader;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class LoadedFilePart {

	private DataConfigurationTable table;
	private TreeViewer viewer;
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
						table.setInput(dOp.getData().getShape(), new String[]{"X","Y"},dOp.getAllPossibleAxes(),(String)null);
//						updateTable(dOp.getData(), dOp.getAllPossibleAxes());
						
//						
//						DataOptions d = (DataOptions)e;
//						String name = d.getName();
//						String fileName = d.getFileName();
//						name = "/" +name + "/data";
//						try {
//							IDataHolder dh = LoaderFactory.getData(fileName);
//							ILazyDataset lz = dh.getLazyDataset(name);
//							Map<String, int[]> axMapping = dh.getMetadata().getDataShapes();
//							updateTable(lz, axMapping);
//						} catch (Exception ex) {
//							// TODO Auto-generated catch block
//							ex.printStackTrace();
//						}
						
					}
				}
				event.toString();
				
			}
		});
		
		ILazyDataset lz = null;
		Map<String, int[]> axMapping= null;
		
		try {
			IDataHolder dh = LoaderFactory.getData("/home/jacobfilik/Work/data/exampleFPA.nxs");
			lz = dh.getLazyDataset("/ftir1/absorbance/data");
			axMapping = dh.getMetadata().getDataShapes();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		final ILazyDataset lazy = lz;
		
		table = new DataConfigurationTable();
		table.createControl(parent);
		//TODO update table lazy
		table.addSliceListener(new ISliceChangeListener() {
			
			@Override
			public void sliceChanged(SliceChangeEvent event) {
				plotManager.getDataOption().setAxes(event.getAxesNames());
				Object[] options = event.getOptions();
				boolean transpose = false;
				for (int i = 0; i < options.length; i++) {
					if (options[i] != null && !((String)options[i]).isEmpty()) {
						if (options[i].equals("Y")) {
							transpose = false;
							break;
						} else {
							transpose = true;
							break;
						}
					}
				}
				plotManager.plotData(event.getSlice(), transpose);
//				
//				
//				System.out.println(event.getSlice().toString());
//				IPlottingSystem<Object> ps = pService.getPlottingSystem("Plot");
//				if (ps == null) return;
//				IDataset s = lazy.getSlice(event.getSlice());
//				if (s == null) return;
//				s.squeeze();
//				
//				Map<Integer, String> map = new HashMap<Integer, String>();
//				for (Integer i = 0 ; i < event.getAxesNames().length; i++) {
//					map.put(i+1, event.getAxesNames()[i]);
//				}
//				
//				AxesMetadata fam = null;
//				try {
//					fam = lService.getAxesMetadata(lazy, "/home/jacobfilik/Work/data/exampleFPA.nxs", map);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				List<IDataset> ax = null;
//				if (fam != null) {
//					ax = new ArrayList<IDataset>();
//					ILazyDataset[] axes = fam.getAxes();
//					if (axes != null) {
//						for (ILazyDataset a : axes) {
//							ax.add(a == null ? null : a.getSlice().squeeze());
//						}
//						Collections.reverse(ax);
//					}
//				}
//				
//				ps.createPlot2D(s,ax,null);
				
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
//	  Object object = data.get(IEventBroker.DATA);
//	  String path = object.toString();
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