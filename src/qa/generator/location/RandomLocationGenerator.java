package qa.generator.location;

import PamUtils.LatLong;
import qa.ClusterParameters;
import qa.QAControl;
import qa.generator.clusters.QACluster;
import qa.generator.distributions.QADistribution;
import qa.generator.sequence.SoundSequence;

public class RandomLocationGenerator extends QALocationGenerator {

	public double logMinRange, logMaxRange;
	private int nDone;
	private int totalSequences;
	
	public RandomLocationGenerator(QAControl qaControl, QACluster qaCluster, int totalSequences, double[] rangeLimits) {
		super(qaControl, qaCluster, rangeLimits);
		this.totalSequences = totalSequences;
		nDone = 0;
	}

	@Override
	public LatLong getNextLocation(LatLong currentReference, SoundSequence previousSequence) {
		if (nDone >= totalSequences) {
			return null;
		}
		double r0 = getNominalRange();
		logMinRange = Math.log(getMinRange());
		logMaxRange = Math.log(getMaxRange());
		double r  = logMinRange + Math.random()*(logMaxRange-logMinRange);
		r = Math.exp(r);

		LatLong sourcePosition = currentReference.travelDistanceMeters(Math.random()*360., r);
		setLocationHeight(sourcePosition);
		
		nDone++;
		return sourcePosition;
	}
	

	@Override
	public boolean isFinished() {
		return (nDone >= totalSequences);
	}

}
