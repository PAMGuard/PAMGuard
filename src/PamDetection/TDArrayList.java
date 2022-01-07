package PamDetection;

import java.util.ArrayList;

import PamguardMVC.PamDataUnit;


/**
 * Wrapper which holds a list of time delays and the corresponding detections used to calculate those delays. 
 * This allows detections to be matched back with time delays. 
 * Difficult to integrate this into a dataunit as time delays can be calculated from multiple data units. 
 * @author Jamie Macaulay
 * @param <T>
 *
 */
@SuppressWarnings("rawtypes")
public class TDArrayList<T> extends ArrayList<T> {

	/**
	 * A list of data units used to create the time delays.
	 */
	public ArrayList<PamDataUnit> dataUnits=new ArrayList<PamDataUnit>();
		
	private static final long serialVersionUID = 1L;

	public ArrayList<PamDataUnit> getDataUnits() {
		return dataUnits;
	}
	
	public void addDataUnit(PamDataUnit unit) {
		dataUnits.add(unit);
	}

	public void setDataUnits(ArrayList<PamDataUnit> dataUnits) {
		this.dataUnits = dataUnits;
	}

}
