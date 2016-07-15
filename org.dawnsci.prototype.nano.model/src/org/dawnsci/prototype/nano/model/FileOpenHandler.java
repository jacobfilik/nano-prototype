package org.dawnsci.prototype.nano.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.Event;

public class FileOpenHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = Display.getDefault().getActiveShell();
		FileDialog dialog = new FileDialog(shell);
		String open = dialog.open();
		
		if (open != null) {
			Map<String,String> props = new HashMap<>();
			props.put("path", open);

			EventAdmin eventAdmin = ServiceManager.getEventAdmin();
			eventAdmin.sendEvent(new Event("org/dawnsci/events/file/OPEN", props));
			return null;
		}
		return null;
	}

}
