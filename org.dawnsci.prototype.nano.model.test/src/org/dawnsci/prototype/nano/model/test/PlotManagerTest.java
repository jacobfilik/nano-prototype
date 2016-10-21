package org.dawnsci.prototype.nano.model.test;

import static org.junit.Assert.*;

import org.dawnsci.prototype.nano.model.DataOptions;
import org.dawnsci.prototype.nano.model.FileController;
import org.dawnsci.prototype.nano.model.IPlotMode;
import org.dawnsci.prototype.nano.model.LoadedFile;
import org.dawnsci.prototype.nano.model.PlotManager;
import org.dawnsci.prototype.nano.model.ServiceManager;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.january.dataset.Slice;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderServiceImpl;

public class PlotManagerTest extends AbstractTestModel {
	
	private static PlotManager plotManager;
	private static FileController fileController;
	private static IPlottingSystem plottingSystem;
	
	@BeforeClass
	public static void buildData() throws Exception {
			AbstractTestModel.buildData();
			plottingSystem = new MockPlottingSystem();
			plotManager = new PlotManager(plottingSystem);
			ServiceManager.setLoaderService(new LoaderServiceImpl());
			fileController = FileController.getInstance();
	}

//	@Test
//	public void testGetPlotModes() {
//		assertNotNull(plotManager.getPlotModes());
//	}

	@Test
	public void testPlotModeXY() {
		fileController.loadFile(file.getAbsolutePath());
		LoadedFile lf = fileController.getLoadedFiles().getLoadedFile(file.getAbsolutePath());
		DataOptions dop = lf.getDataOptions().get(0);
		dop.setSelected(true);
		fileController.setCurrentFile(lf,true);
		fileController.setCurrentData(dop, true);
		assertNotNull(plotManager.getCurrentPlotModes());
		assertEquals(1, plotManager.getCurrentPlotModes().length);
		assertEquals(1, plottingSystem.getTraces().size());
		fileController.setCurrentData(dop,false);
		assertEquals(0, plottingSystem.getTraces().size());
		fileController.setCurrentData(dop,true);
		assertEquals(1, plottingSystem.getTraces().size());
		fileController.getNDimensions().setSlice(0, new Slice(5));
		assertEquals(1, plottingSystem.getTraces().size());
		fileController.setCurrentFile(lf,false);
		assertEquals(0, plottingSystem.getTraces().size());
		fileController.setCurrentFile(lf,true);
		assertEquals(1, plottingSystem.getTraces().size());
		fileController.unloadFile(lf);
		assertEquals(0, plottingSystem.getTraces().size());
		
		//TODO next check two lines plotted and unplotted from different datasets same file
	}
	
//	@Test
//	public void testPlotModeXYtoImage() {
//		fileController.loadFile(file.getAbsolutePath());
//		LoadedFile lf = fileController.getLoadedFiles().getLoadedFile(file.getAbsolutePath());
//		lf.setSelected(true);
//		DataOptions dop = lf.getDataOptions().get(1);
//		dop.setSelected(true);
//		fileController.setCurrentFile(lf);
//		fileController.setCurrentData(dop, true);
//		assertNotNull(plotManager.getCurrentPlotModes());
//		assertEquals(3, plotManager.getCurrentPlotModes().length);
//		assertEquals(1, plottingSystem.getTraces().size());
//		fileController.unloadFile(lf);
////		fileController.setCurrentData(dop,false);
////		assertEquals(0, plottingSystem.getTraces().size());
////		fileController.setCurrentData(dop,true);
////		assertEquals(1, plottingSystem.getTraces().size());
////		fileController.getNDimensions().setSlice(0, new Slice(5));
////		assertEquals(1, plottingSystem.getTraces().size());
//	}

//	@Test
//	public void testRemoveFromPlot() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSwitchPlotMode() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testAddToPlot() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetCurrentMode() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetCurrentMode() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testUpdatePlot() {
//		fail("Not yet implemented");
//	}

}
