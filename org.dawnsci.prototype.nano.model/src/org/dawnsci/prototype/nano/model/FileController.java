package org.dawnsci.prototype.nano.model;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dawnsci.prototype.nano.model.table.NDimensions;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.january.dataset.ShapeUtils;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

public class FileController {

	private static final FileController instance = new FileController();
	
	private LoadedFiles loadedFiles;
	private LoadedFile currentFile;
	private DataOptions currentData;
	
	private Set<FileControllerStateEventListener> listeners = new HashSet<FileControllerStateEventListener>();
	
	private FileController(){
		loadedFiles = new LoadedFiles();
	};
	
	public static FileController getInstance() {
		return instance;
	}
	
	public void loadFiles(String[] paths) {
		
		FileLoadingRunnable runnable = new FileLoadingRunnable(paths);
		
		IProgressService service = (IProgressService) PlatformUI.getWorkbench().getService(IProgressService.class);
		
			try {
				service.busyCursorWhile(runnable);
			} catch (Exception e) {
				e.printStackTrace();
			} 
	}
	
	
	public void loadFile(String path) {
		
		loadFiles(new String[]{path});
//		IProgressService service = (IProgressService) PlatformUI.getWorkbench().getService(IProgressService.class);
//		
//			try {
//				service.busyCursorWhile(new IRunnableWithProgress() {
//
//					@Override
//					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//						// TODO Auto-generated method stub
//						
//					}
//					
//				});
//			} catch (Exception e) {
//				e.printStackTrace();
//			} 
					
					
		
//		
//		LoadedFile f = null;
//		try {
//			f = new LoadedFile(ServiceManager.getLoaderService().getData(path, null));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		if (f != null) {
//			loadedFiles.addFile(f);
//			fireStateChangeListeners(false,false);
//		}
	}
	
	public LoadedFiles getLoadedFiles() {
		return loadedFiles;
	}
	
	public void deselectFile(LoadedFile file) {
		file.setSelected(false);
		fireStateChangeListeners(false,false);
	}
	
	public void deselectOption(DataOptions option) {
		option.setSelected(false);
		fireStateChangeListeners(false,false);
	}
	
	public void deselectAllOthers() {
		List<DataOptions> dataOptions = currentFile.getDataOptions();
		for (DataOptions dop : dataOptions) {
			if (currentData != dop) dop.setSelected(false);
		}
		loadedFiles.deselectOthers(currentFile.getLongName());
		
		fireStateChangeListeners(false,false);
	}
	
	public void setCurrentFile(LoadedFile file, boolean selected) {
		currentFile = file;
		if (currentFile == null) {
			currentData = null;
			return;
		}
		
		
		file.setSelected(selected);
		
		DataOptions option = null;
		
		for (DataOptions op : file.getDataOptions()) {
			if (op.isSelected()) {
				option = op;
				break;
			}
		}
		
		if (option == null && file.getDataOptions().size() != 0) {
			option = file.getDataOptions().get(0);
		}
		
		if (option == null) return;
		
		setCurrentDataOnFileChange(option);

		
//		if (!set && file.getDataOptions().size() != 0) {
//			setCurrentDataOnFileChange(file.getDataOptions().get(0));
//		}
		
	}
	
	public void setCurrentDataOnFileChange(DataOptions data) {
		currentData = data;
		fireStateChangeListeners(true,true);
	}
	
	public void setCurrentData(DataOptions data, boolean selected) {
		if (currentData == data && data.isSelected() == selected) return;
		currentData = data;
		data.setSelected(selected);
		fireStateChangeListeners(false,true);
	}
	
	public void setCurrentData(DataOptions data) {
		if (currentData == data) return;
		currentData = data;
		fireStateChangeListeners(false,true);
	}
	
	public DataOptions getCurrentDataOption() {
		return currentData;
	}
	
	public void unloadFile(LoadedFile file){
		loadedFiles.unloadFile(file);
		if (currentFile == file)  {
			currentFile = null;
			currentData = null;
		}
		fireStateChangeListeners(true, true, file);
	}
	
	public LoadedFile getCurrentFile() {
		return currentFile;
	}
	
	public List<DataOptions> getSelectedDataOptions(){
		
		List<DataOptions> checked = new ArrayList<>();
		
		for (DataOptions op : currentFile.getDataOptions()) {
			if (op.isSelected()) checked.add(op);
		}
		return checked;
	}
	
	public List<LoadedFile> getSelectedFiles(){
		
		List<LoadedFile> checked = new ArrayList<>();
		
		for (LoadedFile f : loadedFiles) {
			if (f.isSelected()) checked.add(f);
		}
		return checked;
	}
	
	public int getSelectedDataRank() {
		if (currentData == null) return -1;
		int[] shape = currentData.getData().getShape();
		shape = ShapeUtils.squeezeShape(shape, false);
		int rank = shape.length;
		return rank;
	}
	
	public NDimensions getNDimensions(){
		if (currentData == null) return null;
		return getNDimensions(currentData);
	}
	
	private void fireStateChangeListeners(boolean file, boolean dataset, LoadedFile removed) {
		FileControllerStateEvent e = new FileControllerStateEvent(this, file, dataset, removed);
		for (FileControllerStateEventListener l : listeners) l.stateChanged(e);
	}
	private void fireStateChangeListeners(boolean file, boolean dataset) {
		FileControllerStateEvent e = new FileControllerStateEvent(this, file, dataset, null);
		for (FileControllerStateEventListener l : listeners) l.stateChanged(e);
	}
	
	public void addStateListener(FileControllerStateEventListener l) {
		listeners.add(l);
	}
	
	public void removeStateListener(FileControllerStateEventListener l) {
		listeners.remove(l);
	}
	
	private NDimensions getNDimensions(DataOptions dataOptions){
		if (dataOptions.getPlottableObject() != null) return dataOptions.getPlottableObject().getNDimensions();
		NDimensions ndims = new NDimensions(dataOptions.getData().getShape());
		ndims.setUpAxes((String)null, dataOptions.getAllPossibleAxes(), dataOptions.getPrimaryAxes());
		return ndims;
	}
	
	private class FileLoadingRunnable implements IRunnableWithProgress {

		String[] paths;
		
		public FileLoadingRunnable(String[] paths) {
			this.paths = paths;
		}
		
		@Override
		public void run(IProgressMonitor monitor) {
			
			List<LoadedFile> files = new ArrayList<>();
			
			for (String path : paths) {
				LoadedFile f = null;
				try {
					f = new LoadedFile(ServiceManager.getLoaderService().getData(path, null));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if (f != null) files.add(f);
				
			}
			
			loadedFiles.addFiles(files);
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					fireStateChangeListeners(false,false);
				}
			});
			
			
		}
		
	}
}
