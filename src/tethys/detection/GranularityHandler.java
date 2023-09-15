package tethys.detection;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import nilus.Detection;
import nilus.GranularityEnumType;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.species.DataBlockSpeciesManager;

public abstract class GranularityHandler {

	protected TethysControl tethysControl;
	
	protected PamDataBlock dataBlock;
	
	protected TethysExportParams tethysExportParams;
	
	protected StreamExportParams streamExportParams;

	private DataBlockSpeciesManager speciesManager;

	/**
	 * @param tethysControl
	 * @param dataBlock
	 * @param tethysExportParams
	 * @param streamExportParams
	 */
	public GranularityHandler(TethysControl tethysControl, PamDataBlock dataBlock,
			TethysExportParams tethysExportParams, StreamExportParams streamExportParams) {
		this.tethysControl = tethysControl;
		this.dataBlock = dataBlock;
		this.tethysExportParams = tethysExportParams;
		this.streamExportParams = streamExportParams;
		speciesManager = dataBlock.getDatablockSpeciesManager();
	}
	
	/**
	 * Prepare to start, passing the start time of the effort 
	 * or of the first time bin for binned granularity types. 
	 * @param timeMillis
	 */
	public abstract void prepare(long timeMillis);
	
	/**
	 * Put a data unit into a Detection object. for Call granularity
	 * this will probably return every time. For binned and encounter
	 * types this will only return at the end of a bin / encounter
	 * @param dataUnit
	 * @return Detection object, but only when ready to be added to Detections
	 */
	public abstract Detection[] addDataUnit(PamDataUnit dataUnit);
	
	/**
	 * Get a grouping name for the call. This may just be the calls species code, 
	 * or it may be appended with the channel number. This is used to find bin and 
	 * encounter data in HashMaps in 
	 * @param dataUnit
	 * @return
	 */
	public String getCallGroupName(PamDataUnit dataUnit) {
		String groupName = speciesManager.getSpeciesCode(dataUnit);
		if (groupName == null) {
			groupName = "NullSpecies";
		}
		if (streamExportParams.separateChannels) {
			groupName += String.format("Chan%d", dataUnit.getChannelBitmap());
		}
		return groupName;
	}
	/**
	 * Called after end end of all data units to get the last bin / encounter. <p>
	 * 
	 * @param timeMillis end time of effort or last bin in milliseconds. 
	 * @return null for Call granularity, otherwise may be non null for binned or encounter. 
	 */
	public abstract Detection[] cleanup(long timeMillis);
	
	/**
	 * Convert a single detection to a one element array since that's what' 
	 * most functions need to return. 
	 * @param det
	 * @return
	 */
	protected Detection[] toDetectionArray(Detection det) {
		if (det == null) {
			return null;
		}
		Detection[] dets = new Detection[1];
		dets[0] = det;
		return dets;
	}
	
	/**
	 * Create the correct type of granularity handler to put individual data units into 
	 * Detection objects. 
	 * @param granularity
	 * @param tethysControl
	 * @param dataBlock
	 * @param tethysExportParams
	 * @param streamExportParams
	 * @return
	 */
	public static GranularityHandler getHandler(GranularityEnumType granularity, TethysControl tethysControl, PamDataBlock dataBlock,
			TethysExportParams tethysExportParams, StreamExportParams streamExportParams) {
		switch (granularity) {
		case BINNED:
			return new BinnedGranularityHandler(tethysControl, dataBlock, tethysExportParams, streamExportParams);
		case CALL:
			return new CallGranularityHandler(tethysControl, dataBlock, tethysExportParams, streamExportParams);
		case ENCOUNTER:
			return new EncounterGranularityHandler(tethysControl, dataBlock, tethysExportParams, streamExportParams);
		case GROUPED:
			return new GroupedGranularityHandler(tethysControl, dataBlock, tethysExportParams, streamExportParams);
		default:
			break;
		}
		return null;
	}
}
