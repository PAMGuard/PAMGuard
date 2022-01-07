package qa.generator.location;

import PamUtils.LatLong;
import qa.ClusterParameters;
import qa.QAControl;
import qa.generator.clusters.QACluster;
import qa.generator.sequence.SoundSequence;

/**
 * Create locations for the source at a preset list of distances from the receiver reference. 
 * @author dg50
 *
 */
public class SetDistanceGenerator extends QALocationGenerator {

	private double[] distances;
	
	private int distanceIndex = 0;
	
	private Double sourceDepth;

	/**
	 * 
	 * @param distances a list of distances to test. 
	 * @param rangeLimits 
	 */
	public SetDistanceGenerator(QAControl qaControl, QACluster qaCluster, double[] distances, double[] rangeLimits) {
		super(qaControl, qaCluster, rangeLimits);
		this.distances = distances;
	}

//	/**
//	 * @param distances a list of distances to test
//	 * @param sourceDepth depth of sound source
//	 */
//	public SetDistanceGenerator(QAControl qaControl, QACluster qaCluster, double[] distances, Double sourceDepth) {
//		super(qaControl, qaCluster);
//		this.distances = distances;
//		this.sourceDepth = sourceDepth;
//	}
	
	/**
	 * Make a stepped generator, starting at 1/3 nom range and working up to 3x nom range.
	 * @param cluster 
	 * @param qaControl 
	 * @param rangeLimits 
	 * @param defaultRange nominal range
	 * @param totalSequences total sequences to generate.
	 * @param rangeLimits min and max ranges. Must be two element array  
	 * @return stepped generator
	 */
	public static SetDistanceGenerator makeSteppedGenerator(QAControl qaControl, QACluster cluster, int totalSequences, double[] rangeLimits) {
//		ClusterParameters clusterParams = qaControl.getQaParameters().getClusterParameters(cluster);
//		double defaultRange =  clusterParams.monitorRange;
		double minRange = rangeLimits[0];
		double maxRange = rangeLimits[1];
		double step = Math.pow(maxRange/minRange, 1./(totalSequences-1));
		double r = minRange;
		double[] allSteps = new double[totalSequences];
		for (int i = 0; i < totalSequences; i++) {
			allSteps[i] = r;
			r *= step;
		}
		return new SetDistanceGenerator(qaControl, cluster, allSteps, rangeLimits);
	}

	@Override
	public LatLong getNextLocation(LatLong currentReference, SoundSequence previousSequence) {
		if (isFinished()) {
			return null;
		}
		double dist = distances[distanceIndex++];
		LatLong sourcePosition = currentReference.travelDistanceMeters(Math.random()*360., dist);
		if (sourceDepth != null) {
			sourcePosition.setHeight(-sourceDepth);
		}
		else {
			setLocationHeight(sourcePosition);
		}
		return sourcePosition;
	}

	@Override
	public boolean isFinished() {
		return (distanceIndex >= distances.length);
	}

	/**
	 * @return the sourceDepth
	 */
	public Double getSourceDepth() {
		return sourceDepth;
	}

	/**
	 * @param sourceDepth the sourceDepth to set
	 */
	public void setSourceDepth(Double sourceDepth) {
		this.sourceDepth = sourceDepth;
	}

}
