package tethys;

import java.util.ArrayList;

import org.w3c.dom.Document;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.settings.output.xml.PamguardXMLWriter;
import PamguardMVC.PamDataBlock;
import generalDatabase.DBSchemaWriter;
import generalDatabase.SQLLogging;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;

public class TethysExporter {

	private TethysControl tethysControl;
	private TethysExportParams tethysExportParams;

	public TethysExporter(TethysControl tethysControl, TethysExportParams tethysExportParams) {
		this.tethysControl = tethysControl;
		this.tethysExportParams = tethysExportParams;
	}
	
	/**
	 * Does the work. In reality this will need an awful lot of changing, for instance 
	 * to provide feedback to an observer class to show progress on the display. 
	 * @return OK if success. 
	 */
	public boolean doExport() {
		/*
		 * Call some general export function
		 */
		exportGeneralData(tethysExportParams);
		/*
		 * go through the export params and call something for every 
		 * data block that's enabled.
		 */
		ArrayList<PamDataBlock> allDataBlocks = PamController.getInstance().getDataBlocks();
		for (PamDataBlock aDataBlock : allDataBlocks) {
			StreamExportParams streamExportParams = tethysExportParams.getStreamParams(aDataBlock);
			if (streamExportParams == null || streamExportParams.selected == false) {
				continue; // not interested in this one. 
			}
			exportDataStream(aDataBlock, tethysExportParams, streamExportParams);
		}
		/*
		 * Then do whatever else is needed to complete the document. 
		 */
		
		return true;
	}

	/**
	 * No idea if we need this or not. May want to return something different to void, e.g. 
	 * a reference to the main object for a tethys export. I've no idea !
	 * @param tethysExportParams2
	 */
	private void exportGeneralData(TethysExportParams tethysExportParams) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Here is where we export data for a specific data stream to Tethys. 
	 * 
	 * @param aDataBlock
	 * @param tethysExportParams
	 * @param streamExportParams
	 */
	private void exportDataStream(PamDataBlock aDataBlock, TethysExportParams tethysExportParams,
			StreamExportParams streamExportParams) {
		/**
		 * This will probably need to be passed additional parameters and may also want to return something
		 * other than void in order to build a bigger Tethys document. 
		 */
		/*
		 * Some examples of how to do whatever is needed to get schema and data out of PAMGuard. 
		 */
		/*
		 * first we'll probably want a reference to the module containing the data.
		 * in principle this can't get null, since the datablock was found be searching in 
		 * the other direction. 
		 */
		PamControlledUnit pamControlledUnit = aDataBlock.getParentProcess().getPamControlledUnit();
		/*
		 * Get the XML settings for that datablock. 
		 */
		PamguardXMLWriter pamXMLWriter = PamguardXMLWriter.getXMLWriter();
		Document doc = pamXMLWriter.writeOneModule(pamXMLWriter, System.currentTimeMillis());
		String moduleXML = null;
		if (doc != null) {
			// this string should be XML of all the settings for the module controlling this datablock. 
			moduleXML = pamXMLWriter.getAsString(doc, true); // change to false to get smaller xml
			System.out.printf("Module settings for datablock %s are:\n", moduleXML);
			System.out.println(moduleXML);
		}
		/*
		 * This also should never be null, because we only selected datablocks that had a database
		 * interface. 
		 * Future versions may need to change this to use binary stores. This will require 
		 * the overriding datablock to return something different to SQLLogging - probably a TethysLogging 
		 * interface, which can probably by default just wrap the SQLLogging , but does allow the 
		 * option of modifying behaviour and of making something work for binary stores. 
		 */
		SQLLogging logging = aDataBlock.getLogging();
		if (logging == null) return;
		/**
		 * From the logging, it's possible to automatically generate a XML schema. This may not
		 * be entirely right, but will be easy to fix. 
		 */
		DBSchemaWriter schemaWriter = new DBSchemaWriter();
		Document schemaDoc = schemaWriter.generateDatabaseSchema(aDataBlock, logging, logging.getTableDefinition());
		String schemaXML = null;
		if (schemaDoc != null) {
			schemaXML = pamXMLWriter.getAsString(schemaDoc, true);
		}
		System.out.printf("Database schema for Module Type %s Name %s are:\n", pamControlledUnit.getUnitType(), pamControlledUnit.getUnitName());
		System.out.println(schemaXML);
		
		/**
		 * Now can go through the data. Probably, we'll want to go through all the data in 
		 * the project, but we can hold off on that for now and just go for data that 
		 * are in memory. We'll also have to think a lot about updating parts of the 
		 * database which have been reprocessed - what we want to do, should eventually all
		 * be options set in the dialog and available within TethysExportParams
		 * For now though, we're just going to export data that are in memory. 
		 */
		
	}

}
