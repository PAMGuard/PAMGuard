package tethys.output;

import java.io.Serializable;
import java.util.HashMap;
import PamguardMVC.PamDataBlock;
import generalDatabase.DBControlUnit;
import metadata.deployment.DeploymentData;


/**
 * Parameters for controlling export of Tethys data. 
 * @author dg50
 *
 */
public class TethysExportParams implements Serializable, Cloneable{

	public static final long serialVersionUID = 1L;
	
	/*
	 * Need to add lots of other parameters here, such as the connection detils
	 * for the tethys database. 
	 */
	public String serverName = "http://localhost";
	
	public int port = 9779;
	
	public String getFullServerName() {
		return serverName + ":" + port;			
	}
	
	private HashMap<String, StreamExportParams> streamParamsMap = new HashMap();

	private DeploymentData deploymentData;
	
	/**
	 * PAMGuard HAS to have a dataset name to link to data in Tethys, or it all gets
	 * very confusing. This will be used in Deployment and Detection document names. 
	 */
	private String datasetName;

	public boolean listDocsInPamguard;

	/**
	 * @return the datasetName
	 */
	public String getDatasetName() {
		if (datasetName == null) {
			datasetName = getDefaultDatasetName();
		}
		return datasetName;
	}
	
	private String getDefaultDatasetName() {
		// get the database name. It must exist in viewer mode !
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		String dbName = dbControl.getDatabaseName();
		// strip off trailing file type. 
		int dPos = dbName.lastIndexOf('.');
		if (dPos > 0) {
			dbName = dbName.substring(0, dPos);
		}
		/* 
		 * if the name ends in database, then remove that too (this is quite
		 * common since it's the default for batch output 
		 */
		if (dbName.toLowerCase().endsWith("database")) {
			dbName = dbName.substring(0, dbName.length()-"database".length());
		}
		if (dbName.endsWith("_")) {
			dbName = dbName.substring(0, dbName.length()-1);
		}
		return dbName;
	}

	/**
	 * @param datasetName the datasetName to set
	 */
	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	@Override
	public TethysExportParams clone() {
		try {
			return (TethysExportParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Set stream export parameters for a given datablock. 
	 * @param dataBlock
	 * @param exportParams
	 */
	public void setStreamParams(PamDataBlock dataBlock, StreamExportParams exportParams) {
		setStreamParams(dataBlock.getLongDataName(), exportParams);
	}
	/**
	 * Set stream export parameters for a given data name. 
	 * @param dataBlock
	 * @param exportParams
	 */
	public void setStreamParams(String longDataName, StreamExportParams exportParams) {
		streamParamsMap.put(longDataName, exportParams);
	}
	
	public StreamExportParams getStreamParams(PamDataBlock dataBlock) {
		return getStreamParams(dataBlock.getLongDataName());
	}

	private StreamExportParams getStreamParams(String longDataName) {
		return streamParamsMap.get(longDataName);
	}

	public DeploymentData getProjectData() {
		if (deploymentData == null) {
			deploymentData = new DeploymentData();
		}
		return deploymentData;
	}

}
