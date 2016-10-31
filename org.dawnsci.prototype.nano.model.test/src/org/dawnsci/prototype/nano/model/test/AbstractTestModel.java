package org.dawnsci.prototype.nano.model.test;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import org.dawnsci.prototype.nano.model.LoadedFile;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class AbstractTestModel {
	
	protected static File file;
	protected static LoadedFile loadedFile;
	protected static int numberOfDatasets = 3;
	protected static Map<String, int[]> nameShapeMap;
	
	protected static File file1;
	protected static File file2;
	protected static File file3;
	
	static {
		nameShapeMap = new TreeMap<String,int[]>();
		nameShapeMap.put("dataset0", new int[]{1});
		nameShapeMap.put("dataset1", new int[]{10});
		nameShapeMap.put("dataset2", new int[]{10,15});
		nameShapeMap.put("dataset3", new int[]{10,15,20});
	}
	
	@ClassRule
    public static TemporaryFolder testFolder = new TemporaryFolder();

	@BeforeClass
	public static void buildData() throws Exception {
		file = testFolder.newFile("file0.nxs");
		NanoModelTestUtils.makeHDF5File(file.getAbsolutePath(), nameShapeMap);
		
		Map<String, int[]> xyShapeMap = new TreeMap<String,int[]>();
		xyShapeMap.put("dataset0", new int[]{100});
		xyShapeMap.put("dataset1", new int[]{100});
		
		file1 = testFolder.newFile("file1.nxs");
		NanoModelTestUtils.makeHDF5File(file1.getAbsolutePath(), nameShapeMap);
		
		file2 = testFolder.newFile("file2.nxs");
		NanoModelTestUtils.makeHDF5File(file2.getAbsolutePath(), nameShapeMap);
		
		file3 = testFolder.newFile("file3.nxs");
		NanoModelTestUtils.makeHDF5File(file3.getAbsolutePath(), nameShapeMap);
		
		loadedFile = new LoadedFile(LoaderFactory.getData(file.getAbsolutePath()));
	}

}
