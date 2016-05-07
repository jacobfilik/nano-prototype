package org.dawnsci.prototype.e4.nano.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
			for (String key : found.keySet()) {
				DataOptions d = new DataOptions(key, this);
				
				dataOptions.add(d);
			}
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
	
	
}
