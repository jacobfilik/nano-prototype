package org.dawnsci.prototype.e4.nano.model;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;

public class FileTreeLabelProvider extends StyledCellLabelProvider {

	@Override
    public void update(ViewerCell cell) {
      Object element = cell.getElement();
      StyledString text = new StyledString();
      text.append(((SimpleTreeObject)element).getName());
      cell.setText(text.toString());
      super.update(cell);
	}
	
}
