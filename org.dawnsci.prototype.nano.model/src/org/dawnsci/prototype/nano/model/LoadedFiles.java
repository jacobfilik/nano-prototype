package org.dawnsci.prototype.nano.model;

import java.util.ArrayList;
import java.util.List;

public class LoadedFiles implements SimpleTreeObject {

	private List<LoadedFile> fileList; 
	
	public LoadedFiles() {
		fileList = new ArrayList<LoadedFile>();
	}
	
	public void addFile(LoadedFile f){
		fileList.add(f);
	}

	@Override
	public boolean hasChildren() {
		return true;
	}

	@Override
	public Object[] getChildren() {
		return fileList.toArray();
	}

	@Override
	public String getName() {
		return "";
	}
	
	public void deselectOthers(String path) {
		for (LoadedFile file : fileList) if (!path.equals(file.getLongName())) file.setSelected(false);
	}
	
	public LoadedFile getLoadedFile(String path) {
		for (LoadedFile file : fileList) if (path.equals(file.getLongName())) return file;
		return null;
	}
	
	public void unloadFile(LoadedFile file) {
		fileList.remove(file);
	}
	
}
