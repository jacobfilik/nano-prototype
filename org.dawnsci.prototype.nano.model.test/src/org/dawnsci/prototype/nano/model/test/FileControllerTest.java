package org.dawnsci.prototype.nano.model.test;

import static org.junit.Assert.*;

import org.dawnsci.prototype.nano.model.DataOptions;
import org.dawnsci.prototype.nano.model.FileController;
import org.dawnsci.prototype.nano.model.LoadedFile;
import org.dawnsci.prototype.nano.model.ServiceManager;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.LoaderServiceImpl;

public class FileControllerTest extends AbstractTestModel{
	
	private static FileController fileController;
	
	@BeforeClass
	public static void buildData() throws Exception {
		AbstractTestModel.buildData();
		ServiceManager.setLoaderService(new LoaderServiceImpl());
		fileController = FileController.getInstance();
		fileController.loadFile(file.getAbsolutePath());
	}

	@Test
	public void testGetInstance() {
		assertNotNull(FileController.getInstance());
	}

	@Test
	public void testGetLoadedFiles() {
		assertNotNull(fileController.getLoadedFiles());
	}

	@Test
	public void testSetCurrentFile() {
		assertNull(fileController.getCurrentFile());
		fileController.setCurrentFile(fileController.getLoadedFiles().getLoadedFile(file.getAbsolutePath()),false);
		assertNotNull(fileController.getCurrentFile());
		fileController.setCurrentFile(null,false);
		assertNull(fileController.getCurrentFile());
		
	}

	@Test
	public void testSetCurrentData() {
		assertNull(fileController.getCurrentFile());
		fileController.setCurrentFile(fileController.getLoadedFiles().getLoadedFile(file.getAbsolutePath()),false);
		assertNotNull(fileController.getCurrentFile());
		fileController.setCurrentData(fileController.getCurrentFile().getDataOptions().get(0));
		assertNotNull(fileController.getCurrentDataOption());
		fileController.setCurrentFile(null,false);
		assertNull(fileController.getCurrentFile());
		assertNull(fileController.getCurrentDataOption());
	}

	@Test
	public void testGetCurrentDataOption() {
		assertNull(fileController.getCurrentDataOption());
	}

	@Test
	public void testGetCurrentFile() {
		assertNull(fileController.getCurrentFile());
	}

	@Test
	public void testGetSelectedDataOptions() {
		LoadedFile lf = fileController.getLoadedFiles().getLoadedFile(file.getAbsolutePath());
		fileController.setCurrentFile(lf,false);
		DataOptions dataOptions = fileController.getCurrentFile().getDataOptions().get(0);		
		dataOptions.setSelected(true);
		assertEquals(dataOptions, fileController.getSelectedDataOptions().get(0));
		dataOptions.setSelected(false);
		fileController.setCurrentFile(null,false);
	}

}
