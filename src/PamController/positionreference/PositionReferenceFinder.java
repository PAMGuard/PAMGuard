package PamController.positionreference;

import java.util.ArrayList;
import java.util.List;

import AirgunDisplay.AirgunControl;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamUtils.LatLong;


/**
 * Find a Position Reference, based on a preferred order. 
 * Options to take anything available if nothing in list exists.
 * @author Doug
 *
 */
public class PositionReferenceFinder {

	private List<Class> preferredList;
	
	private boolean acceptAnything;
	
	/**
	 * Construct a class to find and managed PositionReference Preferences 
	 * @param preferredList list or preferred reference types in preferred order
	 * @param acceptAnything accept anything if nothing in the preferred list exists. 
	 */
	public PositionReferenceFinder(List<Class> preferredList, boolean acceptAnything) {
		super();
		this.preferredList = preferredList;
		this.acceptAnything = acceptAnything;
	}
	/**
	 * Construct a class to find and managed PositionReference Preferences 
	 * @param preferredList list or preferred reference types in preferred order
	 * @param acceptAnything accept anything if nothing in the preferred list exists. 
	 */	
	public PositionReferenceFinder(Class[] preferredList, boolean acceptAnything) {
		super();
		if (preferredList != null) {
			this.preferredList = new ArrayList(preferredList.length);
			for (int i = 0; i < preferredList.length;i++)
				this.preferredList.add(preferredList[i]);
		}
		this.acceptAnything = acceptAnything;
	}

	/**
	 * Try to find a reference position based on the set rules. 
	 * @return
	 */
	public PositionReference findReference() {
		PositionReference prefReference = findPreferredReference();
		if (prefReference != null) {
			return prefReference;
		}
		if (acceptAnything) {
			return findAnyReference();
		}
		else{
			return null;
		}
	}
	
	/**
	 * Find any available PositionReference
	 * @return first available position reference
	 */
	public PositionReference findAnyReference() {
		return findReference(PositionReference.class);
	}

	/**
	 * Find the preferred position reference
	 * @return the first available position reference in the preferred list. 
	 */
	public PositionReference findPreferredReference() {
		if(preferredList == null) {
			return null;
		}
		for (Class aClass:preferredList) {
			PositionReference pcu = findReference(aClass);
			if (pcu != null) {
				return pcu;
			}
		}
		return null;
	}

	/**
	 * Get a list of all available position references
	 * @return list of available position references
	 */
	public List<PositionReference> findAllReferences() {
		ArrayList<PamControlledUnit> allRefs = PamController.getInstance().findControlledUnits(PositionReference.class);
		ArrayList<PositionReference> refList = new ArrayList<>();
		for (PamControlledUnit pcu:allRefs) {
			refList.add((PositionReference) pcu);
		}
		return refList;
	}
	
	/**
	 * Find the first available reference for a given class type
	 * @param referenceClass class of PositionReference
	 * @return position reference
	 */
	public PositionReference findReference(Class referenceClass) {
		return (PositionReference) PamController.getInstance().findControlledUnit(referenceClass, null);
	}
	
	/**
	 * @return the preferredList
	 */
	public List<Class> getPreferredList() {
		return preferredList;
	}
	/**
	 * @param preferredList the preferredList to set
	 */
	public void setPreferredList(List<Class> preferredList) {
		this.preferredList = preferredList;
	}
	/**
	 * @return the acceptAnything
	 */
	public boolean isAcceptAnything() {
		return acceptAnything;
	}
	/**
	 * @param acceptAnything the acceptAnything to set
	 */
	public void setAcceptAnything(boolean acceptAnything) {
		this.acceptAnything = acceptAnything;
	}

	/**
	 * Get the distance to the nearest Airgun array from 
	 * the given position. 2D distance only !
	 * @param sequencePos Position of generated sequence
	 * @param timeMillis Current time in milliseconds. 
	 * @return smallest distance or null.
	 */
	public Double distanceToAirgun(LatLong latLong, long timeMillis) {
		return distanceToNearest(latLong, AirgunControl.class, timeMillis);
	}

	/**
	 * Find the 2D distance to the nearest reference position of a given type. 
	 * @param latLong location of start point 
	 * @param referenceType Type of position reference
	 * @param timeMillis Millisecond time
	 * @return distance in metres, or null if no reference found.
	 */
	public Double distanceToNearest(LatLong latLong, Class<?> referenceType, long timeMillis) {
		ArrayList<PamControlledUnit> posList = PamController.getInstance().findControlledUnits(referenceType);
		if (posList == null || posList.size() == 0) {
			return null;
		}
		double minDist = Double.MAX_VALUE;
		double n = 0;
		for (PamControlledUnit pcu:posList) {
			if (pcu instanceof PositionReference) {
				PositionReference posRef = (PositionReference) pcu;
				LatLong pos = posRef.getReferencePosition(timeMillis);
				if (pos == null) continue;
				n++;
				double d = latLong.distanceToMetres(pos);
				minDist = Math.min(minDist, d);
			}
		}
		if (n == 0) {
			return null;
		}
		else {
			return minDist;
		}
	}

	/**
	 * Find the nearest reference position of a given type. 
	 * @param latLong location of start point 
	 * @param referenceType Type of position reference
	 * @param timeMillis Millisecond time
	 * @return distance in metres, or null if no reference found.
	 */
	public PositionReference findNearest(LatLong latLong, Class<?> referenceType, long timeMillis) {
		ArrayList<PamControlledUnit> posList = PamController.getInstance().findControlledUnits(referenceType);
		if (posList == null || posList.size() == 0) {
			return null;
		}
		PositionReference foundReference = null;
		double minDist = Double.MAX_VALUE;
		for (PamControlledUnit pcu:posList) {
			if (pcu instanceof PositionReference) {
				PositionReference posRef = (PositionReference) pcu;
				LatLong pos = posRef.getReferencePosition(timeMillis);
				if (pos == null) continue;
				double d = latLong.distanceToMetres(pos);
				if (d < minDist) {
					foundReference = posRef;
					minDist = d;
				}
			}
		}
		return foundReference;
	}
	
}
