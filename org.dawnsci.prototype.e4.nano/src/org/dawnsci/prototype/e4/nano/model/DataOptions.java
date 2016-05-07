package org.dawnsci.prototype.e4.nano.model;

public class DataOptions implements SimpleTreeObject {

	private String name;
	private LoadedFile parent;
	
	public DataOptions(String name, LoadedFile parent) {
		this.name = name;
		this.parent = parent;
	}
	
	@Override
	public boolean hasChildren() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object[] getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public String getFileName(){
		return parent.getName();
	}

}
