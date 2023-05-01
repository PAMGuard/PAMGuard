package tethys.output;

import java.io.IOException;
import java.io.Serializable;

import nilus.DescriptionType;
import nilus.GranularityEnumType;
import tethys.niluswraps.PDescriptionType;

/**
 * Parameters controlling export of a single stream. 
 * Starts just with a boolean 'selected', but may grow. 
 * These all contain data names rather than references to a Datablock so that 
 * they can be serialised. 
 * @author dg50
 *
 */
public class StreamExportParams implements Serializable {

	public static final long serialVersionUID = 1L;	

	public StreamExportParams(String longDataName, boolean selected) {
		super();
		this.longDataName = longDataName;
		this.selected = selected;
	}

	public String longDataName;
	
	public boolean selected;
	
	public GranularityEnumType granularity = GranularityEnumType.CALL;
	
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
	
	/**
	 * Get the nilus detection description
	 * @return
	 */
	public DescriptionType getNilusDetectionDescription() {
		return getDetectionDescription().getDescription();
	}

}
