package tethys.output;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import PamController.PamController;
import PamguardMVC.PamDataBlock;
import nilus.DescriptionType;
import nilus.Detections;
import nilus.GranularityEnumType;
import nilus.Helper;
import tethys.TethysControl;
import tethys.niluswraps.NilusSettingsWrapper;
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
	 * Map of selected species / call types for output. 
	 * Only selected species will be added to the Detections document 
	 * effort and only these species will be output whatever the granularity type. 
	 */
	private HashMap<String, Boolean> speciesSelection;

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
	 * Use the wrapped description since it's serializable. 
	 * I think this is the only element of the Detections document that
	 * we need since the others are all completed automatically. 
	 * Granularity has a few parameters included here since they go slightly beyond
	 * what's in the xml (max gap, separate channels, etc.)
	 * however, this doesn's serialise properly in the wrapper, so try with 
	 * a complete Detections document: just don't acually ever use it !
	 */
//	public WrappedDescriptionType detectionDescription;
	private NilusSettingsWrapper<Detections> wrappedDetections;

	public StreamExportParams(TethysControl tethysControl, PamDataBlock dataBlock) {
		super();
		this.longDataName = dataBlock.getLongDataName();
		autoFill(tethysControl, dataBlock);
	}
	
	/**
	 * Set if a species or call type is selected. 
	 * @param callName
	 * @param sel
	 */
	public void setSpeciesSelection(String callName, boolean sel) {
		getSpeciesSelection().put(callName, sel);
	}
	
	/**
	 * Get if a species or call type is selected. Return true by default. 
	 * @param callName
	 * @return
	 */
	public boolean getSpeciesSelection(String callName) {
		Boolean sel = getSpeciesSelection().get(callName);
		if (sel == null) {
			return true;
		}
		return sel;
	}

//	public WrappedDescriptionType getDetectionDescription() {
//		if (detectionDescription == null) {
//			detectionDescription = new WrappedDescriptionType();
//		}
////		if (detectionDescription.getMethod() == null) {
////			
////		}
//		return detectionDescription;
//	}

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
		DescriptionType desc = getDescription();
		if (desc == null) {
			setDescription(desc = new DescriptionType());
		}
		if (desc.getMethod() == null || desc.getMethod().length() == 0) {
			autoFill(tethysControl, dataBlock);
		}
	}
	
	public DescriptionType getDescription() {
		Detections detections = getDetections();
		DescriptionType desc = detections.getDescription();
		if (desc == null) {
			desc = new DescriptionType();

			try {
				Helper.createRequiredElements(desc);
			} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
				e.printStackTrace();
			}
			detections.setDescription(desc);
			setDetections(detections); // force reserialization
		}
		return desc;
	}
	
	public void setDescription(DescriptionType description) {
		Detections detections = getDetections();
		detections.setDescription(description);
		setDetections(detections); // force reserialization
	}
	
	public Detections getDetections() {
		NilusSettingsWrapper<Detections> wrapper = getWrappedDetections();
		Detections det = wrapper.getNilusObject(Detections.class);
		if (det == null ) {
			det = new Detections();
			wrapper.setNilusObject(det);
		}
		try {
			Helper.createRequiredElements(det);
		} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
		}
		return det;
	}
	
	public void setDetections(Detections det) {
		NilusSettingsWrapper<Detections> wrapper = getWrappedDetections();
		wrapper.setNilusObject(det);
	}
	
	private NilusSettingsWrapper<Detections> getWrappedDetections() {
		if (wrappedDetections == null) {
			wrappedDetections = new NilusSettingsWrapper<Detections>();
			Detections detections = new Detections();
			try {
				Helper.createRequiredElements(detections);
			} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
				e.printStackTrace();
			}
			wrappedDetections.setNilusObject(detections);
		}
		return wrappedDetections;
	}

	/**
	 * Try to put some information automatically into the Methods. 
	 * @param dataBlock2 
	 * @param tethysControl 
	 */
	private void autoFill(TethysControl tethysControl, PamDataBlock dataBlock) {
		// there should always be a data provider or we'd never have got this far. 
		DescriptionType desc = getDescription();
		TethysDataProvider dataProvider = dataBlock.getTethysDataProvider(tethysControl);
		desc.setMethod(dataProvider.getDetectionsMethod());
//		
//		TethysDataProvider dataProvider = dataBlock.getTethysDataProvider(tethysControl);
//		WrappedDescriptionType desc = getDetectionDescription();
//		desc.setMethod(dataProvider.getDetectionsMethod());
	}

	public void reSerialize() {
		getWrappedDetections().reSerialise();
	}

	/**
	 * @return the speciesSelection
	 */
	public HashMap<String, Boolean> getSpeciesSelection() {
		if (speciesSelection == null) {
			speciesSelection = new HashMap<String, Boolean>();
		}
		return speciesSelection;
	}
	
//	/**
//	 * Get the nilus detection description
//	 * @return
//	 */
//	public DescriptionType getNilusDetectionDescription() {
//		WrappedDescriptionType desc = getDetectionDescription();
//		if (desc == null) {
//			return null;
//		}
//		return desc.getDescription();
//	}

}
