package org.dawnsci.prototype.nano.model;

import java.util.EventListener;

public interface PlotModeChangeEventListener extends EventListener {

	public void plotModeChanged(PlotModeEvent event);
	
}
