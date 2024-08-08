package tethys.output;

import java.io.IOException;
import java.io.Serializable;

import PamController.PamController;
import PamguardMVC.PamDataBlock;
import nilus.DescriptionType;
import nilus.GranularityEnumType;
import tethys.TethysControl;
import tethys.niluswraps.WrappedDescriptionType;
import tethys.pamdata.TethysDataProvider;

/**
 * Parameters controlling export of a single stream. 
 * Starts just with a boolean 'selected', but may grow. 
 * These all contain data names rather than references to a Datablock so that 
 * they can be serialised. However, created with TethysControl and datablock
 * so that some stuff canbe automatically initialised. 
 * @author dg50
 *
 */
public class StreamExportParams implements Serializable {

	public static final long serialVersionUID = 1L;	

	/**
	 * Datablock long data name (used instead of datablock
	 * reference so this object is serialise. 
	 */
	public String longDataName;
	
	/**
	 * Have selected export of detections. 
	 */
	public boolean exportDetections;
	
	/**
	 * Have selected export of localisations. 
	 */
	public boolean exportLocalisations;
	
	/**
	 * Granularity type, binned, call, encounter, grouped. 
	 */
	public GranularityEnumType granularity = GranularityEnumType.CALL;
	
	/**
	 * Bin duration, seconds. 
	 */
	public double binDurationS = 60;
	
	/**
	 * Minimum encounter gap, seconds
	 */
	public double encounterGapS = 60;
	
	/**
	 * Minimum count for a bin to be retained. 
	 */
	public int minBinCount = 1;
	
	/**
	 * Minimum count for an encounter to be retained. 
	 */
	public int minEncounterCount = 1;
	
	/**
	 * Keep channels separate when using binned data. 
	 */
	public boolean separateChannels = true;
	
	/*
	 * Can't have this here since it isn't serializable. 
	 */
	public WrappedDescriptionType detectionDescription;

	public WrappedDescriptionType getDetectionDescription() {
		if (detectionDescription == null) {
			detectionDescription = new WrappedDescriptionType();
		}
//		if (detectionDescription.getMethod() == null) {
//			
//		}
		return detectionDescription;
	}

	public StreamExportParams(TethysControl tethysControl, PamDataBlock dataBlock) {
		super();
		this.longDataName = dataBlock.getLongDataName();
		autoFill(tethysControl, dataBlock);
	}
	
	/**
	 * Used to get the description data back in again if it's changes
	 * as PAMGuard updates. This object can't store references to the 
	 * TethysControl or the datablock since they aren't serializable. 
	 * Normally, the description is auto filled at constructoin, but once
	 * serialized, this can no longer happen, so can call this function to 
	 * sort it all out.  
	 */
	public void checkDescription() {
		TethysControl tethysControl = (TethysControl) PamController.getInstance().findControlledUnit(TethysControl.class, null);
		PamDataBlock dataBlock = PamController.getInstance().getDataBlockByLongName(longDataName);
		if (tethysControl == null || dataBlock == null) {
			return; // probably impossible for this to happen, but just in case.  
		}
		DescriptionType desc = getNilusDetectionDescription();
		if (desc == null) {
			detectionDescription.setDescription(desc = new DescriptionType());
		}
		if (desc.getMethod() == null || desc.getMethod().length() == 0) {
			autoFill(tethysControl, dataBlock);
		}
	}

	/**
	 * Try to put some information automatically into the Methods. 
	 * @param dataBlock2 
	 * @param tethysControl 
	 */
	private void autoFill(TethysControl tethysControl, PamDataBlock dataBlock) {
		// there should always be a data provider or we'd never have got this far. 
		TethysDataProvider dataProvider = dataBlock.getTethysDataProvider(tethysControl);
		WrappedDescriptionType desc = getDetectionDescription();
		desc.setMethod(dataProvider.getDetectionsMethod());
	}
	
	/**
	 * Get the nilus detection description
	 * @return
	 */
	public DescriptionType getNilusDetectionDescription() {
		WrappedDescriptionType desc = getDetectionDescription();
		if (desc == null) {
			return null;
		}
		return desc.getDescription();
	}

}
