package export;

import java.util.ArrayList;

import export.MLExport.MLDetectionsManager;

/**
 * Exports data to external files. 
 */
public class PamExporter {
	
	/**
	 * A list of the avilable exporters. 
	 */
	private ArrayList<PamExportManager> pamExporters;
	
	
	public PamExporter() {
		pamExporters = new ArrayList<PamExportManager>();
		
		//add the MATLAB export
		pamExporters.add(new MLDetectionsManager());
		
	}

}
