package org.dawnsci.prototype.nano.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dawnsci.prototype.nano.model.table.NDimensions;
import org.eclipse.january.dataset.ShapeUtils;

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
	
	public void loadFile(String path) {
		LoadedFile f = null;
		try {
			f = new LoadedFile(ServiceManager.getLoaderService().getData(path, null));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (f != null) {
			loadedFiles.addFile(f);
			fireStateChangeListeners();
		}
	}
	
	public LoadedFiles getLoadedFiles() {
		return loadedFiles;
	}
	
	public void deselectAllOthers() {
		List<DataOptions> dataOptions = currentFile.getDataOptions();
		for (DataOptions dop : dataOptions) {
			if (currentData != dop) dop.setSelected(false);
		}
		loadedFiles.deselectOthers(currentFile.getLongName());
		
		fireStateChangeListeners();
	}
	
	public void setCurrentFile(LoadedFile file) {
		currentFile = file;
		if (currentFile == null) {
			currentData = null;
			return;
		}
		
		if (file.getDataOptions().size() != 0) {
			setCurrentData(file.getDataOptions().get(0));
		}
		
		for (DataOptions op : file.getDataOptions()) {
			if (op.isSelected()) {
				setCurrentData(op);
				break;
			}
		}
	}
	
	public void setCurrentData(DataOptions data, boolean selected) {
		if (currentData == data && data.isSelected() == selected) return;
		currentData = data;
		data.setSelected(selected);
		fireStateChangeListeners();
	}
	
	public void setCurrentData(DataOptions data) {
		if (currentData == data) return;
		currentData = data;
		fireStateChangeListeners();
	}
	
	public DataOptions getCurrentDataOption() {
		return currentData;
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
	
	private void fireStateChangeListeners() {
		for (FileControllerStateEventListener l : listeners) l.stateChanged();
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
}
