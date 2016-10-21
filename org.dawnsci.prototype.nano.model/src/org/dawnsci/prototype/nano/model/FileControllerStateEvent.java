package org.dawnsci.prototype.nano.model;

import java.util.EventObject;

public class FileControllerStateEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	private boolean selectedFileChanged;
	private boolean selectedDataChanged;
	private LoadedFile removedFile;

	public FileControllerStateEvent(Object source, boolean selectedFileChanged,
			boolean selectedDataChanged, LoadedFile removed) {
		super(source);
		
		this.selectedDataChanged = selectedDataChanged;
		this.selectedFileChanged = selectedFileChanged;
		this.removedFile = removed;
	}
	
	public FileControllerStateEvent(Object source, boolean selectedFileChanged,
			boolean selectedDataChanged) {
		this(source, selectedFileChanged, selectedDataChanged, null);
	}

	public LoadedFile getRemovedFile() {
		return removedFile;
	}

	public boolean isSelectedFileChanged() {
		return selectedFileChanged;
	}

	public boolean isSelectedDataChanged() {
		return selectedDataChanged;
	}

}
