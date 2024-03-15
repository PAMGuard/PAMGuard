package export;

import java.util.ArrayList;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import export.MLExport.MLDetectionsManager;
import export.RExport.RExportManager;
import export.wavExport.WavFileExportManager;

/**
 * Exports data to external files. Manages the file sizes and creates data buffers for 
 * offline processing. 
 */
public class PamExporterManager {
	
	/**
	 * A list of the avilable exporters. 
	 */
	private ArrayList<PamDataUnitExporter> pamExporters;
	
	
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
	public void exportDataUnit(PamDataUnit<?, ?> dataUnit) {
		// TODO Auto-generated method stub
		
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
