package tethys.output;

import java.io.Serializable;
import java.util.HashMap;
import PamguardMVC.PamDataBlock;
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
