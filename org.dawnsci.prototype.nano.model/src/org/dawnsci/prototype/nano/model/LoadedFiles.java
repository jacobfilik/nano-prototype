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
	
}
