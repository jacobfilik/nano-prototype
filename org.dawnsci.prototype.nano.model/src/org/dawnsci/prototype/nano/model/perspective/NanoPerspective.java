package org.dawnsci.prototype.nano.model.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

public class NanoPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		
		IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.LEFT, 0.2f, IPageLayout.ID_EDITOR_AREA);
		folderLayout.addView("org.dawnsci.prototype.nano.model.ui.LoadedFilePart");
		IViewLayout vLayout = layout.getViewLayout("org.dawnsci.prototype.nano.model.ui.LoadedFilePart");
		vLayout.setCloseable(false);

		folderLayout = layout.createFolder("folder_1", IPageLayout.LEFT, 0.6f, IPageLayout.ID_EDITOR_AREA);
		folderLayout.addView("org.dawnsci.prototype.nano.model.Plot");
		vLayout = layout.getViewLayout("org.dawnsci.prototype.nano.model.Plot");
		vLayout.setCloseable(false);



		folderLayout = layout.createFolder("folder_2", IPageLayout.TOP, 0.7f, IPageLayout.ID_EDITOR_AREA);
		folderLayout.addView("org.dawnsci.prototype.nano.model.ui.DatasetPart");
		vLayout = layout.getViewLayout("org.dawnsci.prototype.nano.model.ui.DatasetPart");
		vLayout.setCloseable(false);
	}

}