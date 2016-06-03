package org.dawnsci.prototype.e4.nano.handlers;

import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class PlotAutoscaleHandler {

	@CanExecute
	public boolean canExecute(IPlottingService plotService, EPartService partService, IEclipseContext context){
		
		IPlottingSystem<Object> ps = plotService.getPlottingSystem(partService.getActivePart().getLabel());
		
		return ps != null;
	}
	
	
	@Execute
	public void execute(IPlottingService plotService, EPartService partService, IEclipseContext context){
		
		IPlottingSystem<Object> ps = plotService.getPlottingSystem(partService.getActivePart().getLabel());
		
		if (ps != null) ps.autoscaleAxes();

	}
	
}
