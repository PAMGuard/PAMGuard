package export;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import export.CSVExport.CSVExportManager;
import export.MLExport.MLDetectionsManager;
import export.RExport.RExportManager;
import export.wavExport.WavFileExportManager;

/**
 * Exports data to external files. Manages the file sizes and creates data buffers for 
 * offline processing. 
 */
public class PamExporterManager {

	/**
	 * The number of data units to save before saving. This prevents too much being stored in memory
	 */
	private static int BUFFER_SIZE = 10000; 

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
	
    // Creating date format
    public static SimpleDateFormat dataFormat = new SimpleDateFormat(
        "yyyy_MM_dd_HHmmss");

	public PamExporterManager() {
		
		pamExporters = new ArrayList<PamDataUnitExporter>();

		//add the MATLAB export
		pamExporters.add(new MLDetectionsManager());
		pamExporters.add(new RExportManager(this));
		pamExporters.add(new WavFileExportManager());
//		pamExporters.add(new CSVExportManager());
	}

	/** 
	 * Add a data unit to the export list. 
	 * @param force - true to force saving of data e.g. at the end of processing. 
	 */
	public boolean exportDataUnit(PamDataUnit<?, ?> dataUnit, boolean force) {
		boolean exportOK = true;
		
		//System.out.println("Add data unit " + dataUnit + " to: "+ currentFile); 
		
		if (dataUnit==null) {
			if (force) {
				//System.out.println("Write data 1!!" + dataUnitBuffer.size() ); 
				//finish off saving any buffered data
				exportOK = pamExporters.get(exportParams.exportChoice).exportData(currentFile, dataUnitBuffer, true);
				dataUnitBuffer.clear();
			}
			return true;
		}
		
		//if file is null or too large create another a file for saving. 
		if (currentFile == null || isNeedsNewFile(currentFile, pamExporters.get(exportParams.exportChoice))) {
			Date date = new Date(dataUnit.getTimeMilliseconds());
			
			String newFileName = "PAM_" + dataFormat.format(date) + "_" + dataUnit.getParentDataBlock().getDataName().replace(" ", "_");
			
			//create a new file - note each exporter is responsible for closing the file after writing
			//so previous files should already be closed
			String fileName = (exportParams.folder + File.separator + newFileName
			+ "." + pamExporters.get(exportParams.exportChoice).getFileExtension());

			currentFile = new File(fileName);
		}

		dataUnitBuffer.add(dataUnit);

		//System.out.println("Write data unit " + dataUnitBuffer.size() + " to: "+ currentFile); 
		
		if (dataUnitBuffer.size()>=BUFFER_SIZE || force) {
//			System.out.println("Write data 2!!" + dataUnitBuffer.size()); 
			exportOK = pamExporters.get(exportParams.exportChoice).exportData(currentFile, dataUnitBuffer, true);
			dataUnitBuffer.clear();
		}
		
		return exportOK;

	}
	
	public void close() {
		pamExporters.get(exportParams.exportChoice).close();
		
	}

	/**
	 * Check whether the current file is greater than the maximum allowed file size. 
	 * @param currentFile2 - the current file
	 * @param pamDataUnitExporter 
	 * @return true of greater than or equal to the maximum file size. 
	 */
	private boolean isNeedsNewFile(File currentFile2, PamDataUnitExporter pamDataUnitExporter) {
		if( getFileSizeMegaBytes(currentFile2) >= exportParams.maximumFileSize) {
			return true;
		};
		return pamDataUnitExporter.isNeedsNewFile();
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

	public void setCurrentFile(File file) {
		this.currentFile=file;
		
	}

	public ExportParams getExportParams() {
		return exportParams;
	}

	public void setExportParams(ExportParams currentParams) {
		exportParams=currentParams;
		
	}

	/**
	 * Get the currently selected exporter. 
	 * @return the currently selected exporter. 
	 */
	public PamDataUnitExporter getCurretnExporter() {
		return this.pamExporters.get(this.exportParams.exportChoice);
	}




}
