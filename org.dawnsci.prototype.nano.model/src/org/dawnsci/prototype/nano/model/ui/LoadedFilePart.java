package org.dawnsci.prototype.nano.model.ui;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.dawnsci.prototype.nano.model.DataOptions;
import org.dawnsci.prototype.nano.model.FileTreeContentProvider;
import org.dawnsci.prototype.nano.model.FileTreeLabelProvider;
import org.dawnsci.prototype.nano.model.LoadedFile;
import org.dawnsci.prototype.nano.model.LoadedFiles;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.osgi.service.event.Event;

public class LoadedFilePart {

	private CheckboxTableViewer viewer;
	private LoadedFiles loadedFiles;
	
	@Inject ILoaderService lService;
	@Inject
	ESelectionService selectionService;

	@PostConstruct
	public void createComposite(Composite parent) {
		FillLayout fillLayout = new FillLayout();
		fillLayout.type = SWT.VERTICAL;
		parent.setLayout(fillLayout);
		
		loadedFiles = new LoadedFiles();
		
		try {
			LoadedFile f = new LoadedFile(lService.getData("/dls/science/groups/das/ExampleData/OpusData/Nexus/MappingNexus/exampleFPA.nxs",null));
			loadedFiles.addFile(f);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		viewer = CheckboxTableViewer.newCheckList(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(new FileTreeContentProvider());
		viewer.setLabelProvider(new FileTreeLabelProvider());
		ColumnViewerToolTipSupport.enableFor(viewer);
		viewer.setInput(loadedFiles);
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			  @Override
			  public void selectionChanged(SelectionChangedEvent event) {
			    IStructuredSelection selection = viewer.getStructuredSelection();
			    selectionService.setSelection(selection.getFirstElement());
			  }
			});
		
		viewer.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				String name = event.getElement().toString();
				IStructuredSelection selection = viewer.getStructuredSelection();
			    if (selection.getFirstElement() instanceof LoadedFile) {
			    	LoadedFile file = (LoadedFile)selection.getFirstElement();
			    	file.setSelected(event.getChecked());
			    }
			    
				if (event.getChecked()) {
				    if (selection.getFirstElement() instanceof LoadedFile) {
				    	LoadedFile file = (LoadedFile)selection.getFirstElement();
				    }
				    
				    selectionService.setSelection(selection.getFirstElement());
				}
			}
		});
		
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				if (viewer.getSelection().isEmpty())
					return;
				if (viewer.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					Iterator<?> it = selection.iterator();
			
					
					if (selection.size() == 1 && selection.getFirstElement() instanceof LoadedFile) {

						final LoadedFile f = (LoadedFile)selection.getFirstElement();
						manager.add(new Action("Unload") {
							@Override
							public void run() {
								loadedFiles.unloadFile(f);
								viewer.refresh();
							}
						});

					}
					
				}
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		viewer.getControl().setMenu(menu);
	}
	

	@Focus
	public void setFocus() {
		viewer.getControl().setFocus();
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
	
	@Inject
	@Optional
	private void subscribeFileOpenE3(@UIEventTopic("org/dawnsci/events/file/OPEN") Event data ) {
		String[] paths = (String[])data.getProperty("paths");

	  try {
		  for (String path : paths) {
			  LoadedFile f = new LoadedFile(lService.getData(path,null));
			loadedFiles.addFile(f);
		  }
			
			viewer.refresh();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	} 


}