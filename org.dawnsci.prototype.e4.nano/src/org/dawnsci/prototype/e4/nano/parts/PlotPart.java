package org.dawnsci.prototype.e4.nano.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.dataset.impl.Random;
import org.eclipse.dawnsci.plotting.api.IPlotActionSystem;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Composite;

public class PlotPart {

	IPlottingSystem<Composite> system;
	
	@Inject
	ILoaderService lService;
	
	@Inject
	public PlotPart(IPlottingService service){
		
	}
	
	@PostConstruct
	public void createComposite(Composite parent, MPart part,IPlottingService servicep) {
		try {
			system = servicep.createPlottingSystem();

		} catch (Exception ne) {
			throw new RuntimeException(ne);
		}

			
		system.createPlotPart(parent, part.getLabel(), null, PlotType.IMAGE, null);
	}
	
	@Focus
	public void setFocus() {
		system.getPlotComposite().setFocus();
	}
	
}
