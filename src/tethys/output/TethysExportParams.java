package tethys.output;

import java.io.Serializable;
import java.util.HashMap;

import PamguardMVC.PamDataBlock;

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
	
	private HashMap<String, StreamExportParams> streamParamsMap = new HashMap();

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

}
