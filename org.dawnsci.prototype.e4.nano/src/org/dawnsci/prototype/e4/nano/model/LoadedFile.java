package org.dawnsci.prototype.e4.nano.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.tree.IFindInTree;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeUtils;

public class LoadedFile implements SimpleTreeObject {

	private IDataHolder dataHolder;
	private List<DataOptions> dataOptions;
	
	public LoadedFile(IDataHolder dataHolder) {
		this.dataHolder = dataHolder;		
		dataOptions = new ArrayList<DataOptions>();
		
		if (dataHolder.getTree() != null) {
			//Find NX Datas
			Tree t = dataHolder.getTree();
			
			IFindInTree findNXData = new IFindInTree() {
				
				@Override
				public boolean found(NodeLink node) {
					Node n = node.getDestination();
					if (n.containsAttribute("signal")) {
						return true;
					}
					return false;
				}
			};
			
			Map<String, NodeLink> found = TreeUtils.treeBreadthFirstSearch(t.getGroupNode(), findNXData, false, null);
			Tree tree = dataHolder.getTree();
			for (String key : found.keySet()) {
				String path = Node.SEPARATOR + key;
				NodeLink nl = tree.findNodeLink(path);
				Node dest = nl.getDestination();
				String signal = dest.getAttribute("signal").getFirstElement();
				DataOptions d = new DataOptions(path+Node.SEPARATOR+signal, this);
	
				dataOptions.add(d);
			}
			
			if (found.size() > 0) return;
		}
		
		String[] names = dataHolder.getNames();
		for (String n : names) {
			DataOptions d = new DataOptions(n, this);
			dataOptions.add(d);
		}
		
		
	}

	@Override
	public boolean hasChildren() {
		return true;
	}

	@Override
	public Object[] getChildren() {
		return dataOptions.toArray();
	}

	@Override
	public String getName() {
		return dataHolder.getFilePath();
	}
	
	public ILazyDataset getLazyDataset(String name){
		return dataHolder.getLazyDataset(name);
	}
	
	public Map<String, int[]> getDataShapes(){
		return dataHolder.getMetadata().getDataShapes();
	}
	
}
