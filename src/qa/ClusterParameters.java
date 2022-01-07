package qa;

import java.io.Serializable;
import java.util.Hashtable;

import PamguardMVC.PamDataBlock;

/**
 * Parameters controlling tests of a single cluster type
 * @author dg50
 *
 */
public class ClusterParameters implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;

	/**
	 * Default Monitoring range in metres
	 */
	public double monitorRange = 500;
	
	/**
	 * include in immediate tests.
	 */
	public boolean runImmediate = false;
	
	/**
	 * Include in the more randomized tests
	 */
	public boolean runRandom = false;
	
	/**
	 * long name of primary detection block for this cluster. 
	 */
	public String primaryDetectionBlock;

	/**
	 * list of detectors we're interested in using with this cluster. 
	 */
	private Hashtable<String, Boolean> selectedDetectors;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ClusterParameters clone() {
		try {
			return (ClusterParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Set if this cluster is to be compared to the output of the given detector
	 * @param dataBlock  detection data block
	 * @param isSelected set if is selected. 
	 */
	public void setSelectedDetector(PamDataBlock dataBlock, boolean isSelected) {
		if (selectedDetectors == null) {
			selectedDetectors = new Hashtable<>();
		}
		selectedDetectors.put(dataBlock.getLongDataName(), isSelected);
	}
	
	/**
	 * Set if this cluster is to be compared to the output of the given detector
	 * @param dataBlock detection data block
	 * @return true if analysis should compare this detector with this cluster. 
	 */
	public boolean isSelectedDetector(PamDataBlock dataBlock) {
		if (dataBlock.getLongDataName().equals(primaryDetectionBlock)) {
			return true;
		}
		if (selectedDetectors == null) {
			return false;
		}
		Boolean isSel = selectedDetectors.get(dataBlock.getLongDataName());
		return isSel == null ? false : isSel;
	}

}
