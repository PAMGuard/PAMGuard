package export;

import java.io.File;
import java.util.ArrayList;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import export.MLExport.MLDetectionsManager;
import export.RExport.RExportManager;
import export.layoutFX.ExportParams;
import export.wavExport.WavFileExportManager;

/**
 * Exports data to external files. Manages the file sizes and creates data buffers for 
 * offline processing. 
 */
public class PamExporterManager {

	/**
	 * The number of data units to save before saving. 
	 */
	private static int BUFFER_SIZE = 1000; 

	/**
	 * Keep the file size to around 1GB per file
	 */
	private static int MAX_FILE_SIZE_MB = 1024; 


	/**
	 * A buffer of data units so we are not opening a file everytime a new data is passed to the export. 
	 */
	private ArrayList<PamDataUnit> dataUnitBuffer = new ArrayList<PamDataUnit>();

	/**
	 * A list of the avilable exporters. 
	 */
	private ArrayList<PamDataUnitExporter> pamExporters;

	/**
	 * The current file. 
	 */
	private File currentFile;

	/**
	 * Reference to the current export paramters. 
	 */
	private ExportParams exportParams = new ExportParams();

	public PamExporterManager() {
		pamExporters = new ArrayList<PamDataUnitExporter>();

		//add the MATLAB export
		pamExporters.add(new MLDetectionsManager());
		pamExporters.add(new RExportManager());
		pamExporters.add(new WavFileExportManager());
	}

	/** 
	 * Add a data unit to the export list. 
	 */
	public boolean exportDataUnit(PamDataUnit<?, ?> dataUnit) {
		boolean exportOK = true;
		//if the data unit is null then save everything to the buffer.

		if (currentFile == null || isFileSizeMax(currentFile)) {
			//create a new file - note each exporter is responsible for closing the file after writing
			//so previous files should already be closed
			String fileName = (exportParams.folder + File.separator + PamCalendar.formatDate2(dataUnit.getTimeMilliseconds(), false) 
			+ "." + pamExporters.get(exportParams.exportChoice).getFileExtension());

			currentFile = new File(fileName);
		}

		dataUnitBuffer.add(dataUnit);

		if (BUFFER_SIZE>=BUFFER_SIZE) {
			exportOK = pamExporters.get(exportParams.exportChoice).exportData(currentFile, dataUnitBuffer, true);
			dataUnitBuffer.clear();
		}
		
		return exportOK;

	}

	/**
	 * Check whether the current file is greater than the maximum allowed file size. 
	 * @param currentFile2 - the current file
	 * @return true of greater than or equal to the maximum file size. 
	 */
	private boolean isFileSizeMax(File currentFile2) {
		return getFileSizeMegaBytes(currentFile2) >= MAX_FILE_SIZE_MB;
	}

	/**
	 * Get the file size in MegaBytes
	 * @param file
	 * @return
	 */
	private static double getFileSizeMegaBytes(File file) {
		return (double) file.length() / (1024 * 1024);
	}
	public boolean canExportDataBlock(PamDataBlock dataBlock) {
		for (PamDataUnitExporter exporter:pamExporters) {
			if (exporter.hasCompatibleUnits(dataBlock.getUnitClass())) return true;
		}

		return false;
	}

	/**
	 * Get the number of data exporters. e.g. MAT files, CSV, Wav etc. 
	 * @return the number of data exporters
	 */
	public int getNumExporters() {
		return pamExporters.size();
	}

	/**
	 * Get a specific exporter. 
	 * @param i - the index of the exporter. 
	 * @return the exporter. 
	 */
	public PamDataUnitExporter getExporter(int i) {
		return pamExporters.get(i);

	}


}
