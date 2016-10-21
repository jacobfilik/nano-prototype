package org.dawnsci.prototype.nano.model.ui;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.dawnsci.prototype.nano.model.DataOptions;
import org.dawnsci.prototype.nano.model.FileController;
import org.dawnsci.prototype.nano.model.FileControllerStateEvent;
import org.dawnsci.prototype.nano.model.FileControllerStateEventListener;
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
import org.osgi.service.event.EventAdmin;

public class LoadedFilePart {

	private CheckboxTableViewer viewer;
	
	@Inject ILoaderService lService;
	@Inject ESelectionService selectionService;
	@Inject EventAdmin eventAdmin;

	@PostConstruct
	public void createComposite(Composite parent) {
		FillLayout fillLayout = new FillLayout();
		fillLayout.type = SWT.VERTICAL;
		parent.setLayout(fillLayout);
		
		LoadedFiles loadedFiles = FileController.getInstance().getLoadedFiles();
		FileController.getInstance().loadFile("/home/jacobfilik/Work/data/exampleFPA.nxs");
		
//		try {
//			LoadedFile f = new LoadedFile(lService.getData("/home/jacobfilik/Work/data/exampleFPA.nxs",null));
//			loadedFiles.addFile(f);
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

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
			    if (selection.getFirstElement() instanceof LoadedFile) {
			    	LoadedFile selected = (LoadedFile)selection.getFirstElement();
			    	boolean checked = false;
			    	for (Object o : viewer.getCheckedElements()) {
			    		if (selected.equals(o)) {
			    			checked = true;
			    			break;
			    		}
			    	}
//			    	selected.setSelected(checked);
			    	FileController.getInstance().setCurrentFile(selected, checked);
			    }
			    
			    
			    
//			    selectionService.setSelection(selection.getFirstElement());
//			    System.out.println("Selection");
//			    Map<String,Object> props = new HashMap<String,Object>();
//				props.put("file", selection.getFirstElement());
//				eventAdmin.sendEvent(new Event("org/dawnsci/prototype/file/update", props));
			  }
			});
		
		viewer.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
//				String name = event.getElement().toString();
//				IStructuredSelection selection = viewer.getStructuredSelection();
//				System.out.println("check");
//				
//			    if (selection.getFirstElement() instanceof LoadedFile) {
//			    	LoadedFile file = (LoadedFile)selection.getFirstElement();
//			    	file.setSelected(event.getChecked());
////			    	Map<String,Object> props = new HashMap<String,Object>();
////					props.put("file", selection.getFirstElement());
////					eventAdmin.sendEvent(new Event("org/dawnsci/prototype/file/update", props));
//			    }
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
								FileController.getInstance().unloadFile(f);
								viewer.refresh();
							}
						});

					}
					
				}
			}
		});
		menuMgr.setRemoveAllWhenShown(true);
		viewer.getControl().setMenu(menu);
		
		FileController.getInstance().addStateListener(new FileControllerStateEventListener() {
			
			@Override
			public void stateChanged(FileControllerStateEvent event) {
				viewer.refresh();
				
			}
		});
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
			FileController.getInstance().getLoadedFiles().addFile(f);
			viewer.refresh();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Inject
	@Optional
	private void subscribeFileOpen(@UIEventTopic("orgdawnsciprototypeplotupdate")  Event data) {
	  try {
			if (data.containsProperty("path")){
				String path = data.getProperty("path").toString();
				System.out.println(FileController.getInstance().getCurrentFile().isSelected());
				if (!FileController.getInstance().getCurrentFile().isSelected()) return;
				FileController.getInstance().getLoadedFiles().deselectOthers(path);
				viewer.setCheckedElements(new Object[]{FileController.getInstance().getLoadedFiles().getLoadedFile(path)});
				viewer.refresh();
			}
			
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
			  FileController.getInstance().getLoadedFiles().addFile(f);
		  }
			
			viewer.refresh();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	} 


}