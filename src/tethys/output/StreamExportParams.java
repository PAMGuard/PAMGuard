package tethys.output;

import java.io.IOException;
import java.io.Serializable;

import PamController.PamController;
import PamguardMVC.PamDataBlock;
import nilus.DescriptionType;
import nilus.GranularityEnumType;
import tethys.TethysControl;
import tethys.niluswraps.PDescriptionType;
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
	 * reference so this object and serialise. 
	 */
	public String longDataName;
	
	public boolean selected;
	
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
	public PDescriptionType detectionDescription;

	public PDescriptionType getDetectionDescription() {
		if (detectionDescription == null) {
			detectionDescription = new PDescriptionType();
		}
		return detectionDescription;
	}

	public StreamExportParams(TethysControl tethysControl, PamDataBlock dataBlock, boolean selected) {
		super();
		this.longDataName = dataBlock.getLongDataName();
		this.selected = selected;
		autoFill(tethysControl, dataBlock);
	}

	/**
	 * Try to put some information automatically into the Methods. 
	 * @param dataBlock2 
	 * @param tethysControl 
	 */
	private void autoFill(TethysControl tethysControl, PamDataBlock dataBlock) {
		// there should always be a data provider or we'd never have got this far. 
		TethysDataProvider dataProvider = dataBlock.getTethysDataProvider(tethysControl);
		PDescriptionType desc = getDetectionDescription();
		desc.setMethod(dataProvider.getDetectionsMethod());
	}
	
	/**
	 * Get the nilus detection description
	 * @return
	 */
	public DescriptionType getNilusDetectionDescription() {
		return getDetectionDescription().getDescription();
	}

}
