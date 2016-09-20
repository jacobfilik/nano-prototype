package org.dawnsci.prototype.nano.model;

import java.util.ArrayList;
import java.util.List;

public class FileController {

	private static final FileController instance = new FileController();
	
	private LoadedFiles loadedFiles;
	private LoadedFile currentFile;
	private DataOptions currentData;
	
	private FileController(){
		loadedFiles = new LoadedFiles();
	};
	
	public static FileController getInstance() {
		return instance;
	}
	
	public LoadedFiles getLoadedFiles() {
		return loadedFiles;
	}
	
	public void setCurrentFile(LoadedFile file) {
		currentFile = file;
		
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
	
	public void setCurrentData(DataOptions data) {
		currentData = data;
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
}
