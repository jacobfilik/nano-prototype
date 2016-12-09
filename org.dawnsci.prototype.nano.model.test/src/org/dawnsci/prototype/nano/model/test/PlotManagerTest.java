package org.dawnsci.prototype.nano.model.test;

import static org.junit.Assert.*;

import org.dawnsci.prototype.nano.model.DataOptions;
import org.dawnsci.prototype.nano.model.FileController;
import org.dawnsci.prototype.nano.model.IPlotMode;
import org.dawnsci.prototype.nano.model.LoadedFile;
import org.dawnsci.prototype.nano.model.PlotManager;
import org.dawnsci.prototype.nano.model.ServiceManager;
import org.dawnsci.prototype.nano.model.table.NDimensions;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.Slice;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
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
		plotManager.waitOnJob();
		assertNotNull(plotManager.getCurrentPlotModes());
		assertEquals(1, plotManager.getCurrentPlotModes().length);
		assertEquals(1, plottingSystem.getTraces().size());
		fileController.setCurrentData(dop,false);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		fileController.setCurrentData(dop,true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		fileController.getNDimensions().setSlice(0, new Slice(5));
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		fileController.setCurrentFile(lf,false);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		fileController.setCurrentFile(lf,true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		DataOptions dop1 = lf.getDataOptions().get(1);
		fileController.setCurrentData(dop1, true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		fileController.setCurrentFile(lf,false);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		fileController.setCurrentFile(lf,true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		fileController.setCurrentData(dop,false);
		fileController.setCurrentData(dop1,false);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		fileController.setCurrentData(dop,true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		fileController.setCurrentData(dop1,true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		fileController.setCurrentFile(lf,false);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		fileController.setCurrentFile(lf,true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		fileController.unloadFile(lf);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		
	}
	

	@Test
	public void testPlotModeImage() {
		//load file
		fileController.loadFile(file.getAbsolutePath());
		LoadedFile lf = fileController.getLoadedFiles().getLoadedFile(file.getAbsolutePath());
		DataOptions dop = lf.getDataOptions().get(1);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		//set data, check line trace plotted
		fileController.setCurrentFile(lf,true);
		fileController.setCurrentData(dop, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);
		//switch to image mode, check image is plotted
		IPlotMode[] modes = plotManager.getCurrentPlotModes();
		plotManager.switchPlotMode(modes[1]);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		//tick different data, check line trace plotted
		DataOptions dop1 = lf.getDataOptions().get(2);
		fileController.setCurrentData(dop1, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);
		//switch to image mode, check image plotted
		plotManager.switchPlotMode(modes[1]);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		//tick other data, check one image is plotted and dop1 not selected
		fileController.setCurrentData(dop, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		assertFalse(dop1.isSelected());;
		fileController.unloadFile(lf);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
	}
	
	@Test
	public void testPlotModeImageXYSwitch() {
		fileController.loadFile(file.getAbsolutePath());
		LoadedFile lf = fileController.getLoadedFiles().getLoadedFile(file.getAbsolutePath());
		DataOptions dop = lf.getDataOptions().get(1);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		dop.setSelected(true);
		fileController.setCurrentFile(lf,true);
		fileController.setCurrentData(dop, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);
		IPlotMode[] modes = plotManager.getCurrentPlotModes();
		plotManager.switchPlotMode(modes[1]);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		NDimensions nD = dop.getPlottableObject().getNDimensions();
		assertEquals(modes[1].getOptions()[0], nD.getDescription(1));
		assertEquals(modes[1].getOptions()[1], nD.getDescription(0));
		
		plotManager.switchPlotMode(modes[0]);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);
		
		fileController.unloadFile(lf);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
	}
	
	@Test
	public void testPlotModeImageXYSwitch2() {
		fileController.loadFile(file.getAbsolutePath());
		LoadedFile lf = fileController.getLoadedFiles().getLoadedFile(file.getAbsolutePath());
		DataOptions dop = lf.getDataOptions().get(1);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		fileController.setCurrentFile(lf,true);
		fileController.setCurrentData(dop, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);
		
		DataOptions dop2 = lf.getDataOptions().get(2);
		fileController.setCurrentData(dop2, true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		
		IPlotMode[] modes = plotManager.getCurrentPlotModes();
		plotManager.switchPlotMode(modes[1]);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		fileController.unloadFile(lf);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
	}
	
	@Test
	public void testPlotModeImageWithSlice() {
		fileController.loadFile(file.getAbsolutePath());
		LoadedFile lf = fileController.getLoadedFiles().getLoadedFile(file.getAbsolutePath());
		DataOptions dop = lf.getDataOptions().get(2);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		dop.setSelected(true);
		fileController.setCurrentFile(lf,true);
		fileController.setCurrentData(dop, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);
		IPlotMode[] modes = plotManager.getCurrentPlotModes();
		plotManager.switchPlotMode(modes[1]);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		NDimensions nD = dop.getPlottableObject().getNDimensions();
		nD.setSlice(0, new Slice(1,2,1));
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		fileController.unloadFile(lf);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
	}
	

	@Test
	public void testMultiFileXY() throws Exception{
		fileController.loadFile(file1.getAbsolutePath());
		fileController.loadFile(file2.getAbsolutePath());
		fileController.loadFile(file3.getAbsolutePath());
		LoadedFile lf1 = fileController.getLoadedFiles().getLoadedFile(file1.getAbsolutePath());
		LoadedFile lf2 = fileController.getLoadedFiles().getLoadedFile(file2.getAbsolutePath());
		LoadedFile lf3 = fileController.getLoadedFiles().getLoadedFile(file3.getAbsolutePath());
		DataOptions dop1 = lf1.getDataOptions().get(1);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		fileController.setCurrentFile(lf1,true);
		fileController.setCurrentData(dop1, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);
		DataOptions dop2 = lf2.getDataOptions().get(1);
		fileController.setCurrentFile(lf2,true);
		fileController.setCurrentData(dop2, true);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		DataOptions dop3 = lf3.getDataOptions().get(1);
		fileController.setCurrentFile(lf3,true);
		fileController.setCurrentData(dop3, true);
		plotManager.waitOnJob();
		assertEquals(3, plottingSystem.getTraces().size());
		fileController.setCurrentData(dop3, false);
		plotManager.waitOnJob();
		assertEquals(2, plottingSystem.getTraces().size());
		fileController.setCurrentData(dop2, false);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		fileController.setCurrentData(dop1, false);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		
		//clean up
		fileController.unloadFile(lf1);
		fileController.unloadFile(lf2);
		fileController.unloadFile(lf3);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		
	}
	
	@Test
	public void testMultiFileImage() throws Exception{
		fileController.loadFile(file1.getAbsolutePath());
		fileController.loadFile(file2.getAbsolutePath());
		fileController.loadFile(file2.getAbsolutePath());
		LoadedFile lf1 = fileController.getLoadedFiles().getLoadedFile(file1.getAbsolutePath());
		LoadedFile lf2 = fileController.getLoadedFiles().getLoadedFile(file2.getAbsolutePath());
		LoadedFile lf3 = fileController.getLoadedFiles().getLoadedFile(file3.getAbsolutePath());
		
		DataOptions dop1 = lf1.getDataOptions().get(1);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		fileController.setCurrentFile(lf1,false);
		fileController.setCurrentData(dop1, true);
		
		IPlotMode[] modes = plotManager.getCurrentPlotModes();
		plotManager.switchPlotMode(modes[1]);
		fileController.setCurrentFile(lf1,true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
//		DataOptions dop2 = lf2.getDataOptions().get(1);
//		fileController.setCurrentFile(lf2,true);
//		fileController.setCurrentData(dop2, true);
//		assertEquals(2, plottingSystem.getTraces().size());
//		DataOptions dop3 = lf3.getDataOptions().get(1);
//		fileController.setCurrentFile(lf3,true);
//		fileController.setCurrentData(dop3, true);
//		assertEquals(3, plottingSystem.getTraces().size());
//		fileController.setCurrentData(dop3, false);
//		assertEquals(2, plottingSystem.getTraces().size());
//		fileController.setCurrentData(dop2, false);
//		assertEquals(1, plottingSystem.getTraces().size());
//		fileController.setCurrentData(dop1, false);
//		assertEquals(0, plottingSystem.getTraces().size());
//		
		//clean up
		fileController.unloadFile(lf1);
		fileController.unloadFile(lf2);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		
	}
	
	@Test
	public void testMultiFileXYAndImage() throws Exception{
		fileController.loadFile(file.getAbsolutePath());
		fileController.loadFile(file1.getAbsolutePath());
//		fileController.loadFile(file2.getAbsolutePath());
//		fileController.loadFile(file3.getAbsolutePath());
		LoadedFile lf = fileController.getLoadedFiles().getLoadedFile(file.getAbsolutePath());
		LoadedFile lf1 = fileController.getLoadedFiles().getLoadedFile(file1.getAbsolutePath());
//		LoadedFile lf2 = fileController.getLoadedFiles().getLoadedFile(file2.getAbsolutePath());
//		LoadedFile lf3 = fileController.getLoadedFiles().getLoadedFile(file3.getAbsolutePath());
		DataOptions dop = lf.getDataOptions().get(1);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		//Select first file and some data, check plotted as line
		fileController.setCurrentFile(lf,true);
		fileController.setCurrentData(dop, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		ITrace next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);
		IPlotMode[] modes = plotManager.getCurrentPlotModes();
		//Switch to image mode, check image plotted
		plotManager.switchPlotMode(modes[1]);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		//change current file (not checked) and check data, make sure image still plotted
		DataOptions dop1 = lf1.getDataOptions().get(1);
		fileController.setCurrentFile(lf1,false);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		fileController.setCurrentData(dop1, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		plotManager.waitOnJob();
		fileController.setCurrentData(dop1, false);
	
		//check file (with data unchecked) make sure image still plotted
		fileController.setCurrentFile(lf1,true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		//check data, make sure plot switches to a line and first file is unchecked
		fileController.setCurrentData(dop1, true);
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof ILineTrace);
		
		//Need to make UI reflect the plot manager deselection
		assertFalse(lf.isSelected());
		//
		fileController.setCurrentFile(lf,true);
		assertFalse(lf1.isSelected());
		plotManager.waitOnJob();
		assertEquals(1, plottingSystem.getTraces().size());
		next = plottingSystem.getTraces().iterator().next();
		assertTrue(next instanceof IImageTrace);
		
		//clean up
		fileController.unloadFile(lf);
		fileController.unloadFile(lf1);
//		fileController.unloadFile(lf2);
//		fileController.unloadFile(lf3);
		plotManager.waitOnJob();
		assertEquals(0, plottingSystem.getTraces().size());
		
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
