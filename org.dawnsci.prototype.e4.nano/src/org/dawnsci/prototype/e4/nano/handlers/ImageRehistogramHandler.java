package org.dawnsci.prototype.e4.nano.handlers;

import java.util.Collection;

import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

public class ImageRehistogramHandler {
	
	@CanExecute
	public boolean canExecute(IPlottingService plotService, EPartService partService, IEclipseContext context){
		
		IPlottingSystem<Object> ps = plotService.getPlottingSystem(partService.getActivePart().getLabel());
		
		if (ps == null) return false;
		
		Collection<ITrace> traces = ps.getTraces(IImageTrace.class);
		
		if (traces == null || traces.isEmpty()) return false;
		
		return true;
	}
	
	@Execute
	public void execute(IPlottingService plotService, EPartService partService, IEclipseContext context){
		
		IPlottingSystem<Object> ps = plotService.getPlottingSystem(partService.getActivePart().getLabel());
		
		if (ps == null) return;
		
		Collection<ITrace> traces = ps.getTraces(IImageTrace.class);
		
		if (traces == null || traces.isEmpty()) return;
		
		((IImageTrace)traces.iterator().next()).rehistogram();

	}

}
