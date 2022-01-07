package qa.generator.location;

import PamUtils.LatLong;
import qa.ClusterParameters;
import qa.QAControl;
import qa.generator.clusters.QACluster;
import qa.generator.distributions.QADistribution;
import qa.generator.sequence.SoundSequence;

/**
 * Class for generating locations at which sequences should be created. 
 * @author dg50
 *
 */
public abstract class QALocationGenerator {

	private QAControl qaControl;
	private QACluster qaCluster;
	private double[] rangeLimits;

	public QALocationGenerator(QAControl qaControl, QACluster qaCluster, double[] rangeLimits) {
		this.qaControl = qaControl;
		this.qaCluster = qaCluster;
		this.rangeLimits = rangeLimits;
	}
	
	/**
	 * Generate the next location for testing. 
	 * @param currentReference current receiver reference position (vessel, Array or Mitigation zone)
	 * @param previousSequence previous sequence (may want to check to see that how got on and move the source dynamically)
	 * @return new position for the source.
	 */
	public abstract LatLong getNextLocation(LatLong currentReference, SoundSequence previousSequence);
	
	public abstract boolean isFinished();

	/**
	 * @return the qaControl
	 */
	public QAControl getQaControl() {
		return qaControl;
	}

	/**
	 * @return the qaCluster
	 */
	public QACluster getQaCluster() {
		return qaCluster;
	}
	
	/**
	 * 
	 * @return The nominal range to centre the distribution of 
	 * distances around. 
	 */
	public double getNominalRange() {
		ClusterParameters clusterParams = getQaControl().getQaParameters().getClusterParameters(getQaCluster());
		return clusterParams.monitorRange;
	}

	/**
	 * @return the rangeLimits.  Two element array in metres.
	 */
	public double[] getRangeLimits() {
		return rangeLimits;
	}

	/**
	 * @param rangeLimits the rangeLimits to set. Two element array in metres. 
	 */
	public void setRangeLimits(double[] rangeLimits) {
		this.rangeLimits = rangeLimits;
	}
	
	/**
	 * 
	 * @return the minimum range
	 */
	public double getMinRange() {
		return rangeLimits[0];
	}
	
	/**
	 * 
	 * @return the maximum range
	 */
	public double getMaxRange() {
		return rangeLimits[1];
	}
	
	/**
	 * Set the location height based on the depth distribution. N.B depth 
	 * distributions are what they say and return a positive number for a diving
	 * animal, whereas the returned height will generally be negative. 
	 * @param location current location
	 * @return location height
	 */
	protected double setLocationHeight(LatLong location) {
		QADistribution depthDistribution = getQaCluster().getDepthDistribution();
		double height = 0;
		if (depthDistribution != null) {
			height = -depthDistribution.getValues(1)[0];
		}

		location.setHeight(height);
		return height;
	}

}
