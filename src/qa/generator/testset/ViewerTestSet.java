package qa.generator.testset;

import PamUtils.LatLong;
import qa.generator.QASoundGenerator;
import qa.generator.clusters.QACluster;
import qa.generator.sequence.SoundSequence;

public class ViewerTestSet extends QATestSet {

	private int nSequences;
	private String version;

	public ViewerTestSet(String signalType, QACluster qaCluster, String version, int nSequences, Long endTime) {
		super(signalType, null, qaCluster, 0);
		this.version = version;
		this.nSequences = nSequences;
		setEndTime(endTime);
	}

	@Override
	public boolean isFinsihed(long currentSample) {
		return true;
	}

	@Override
	public SoundSequence getNextSequence(long startSample, LatLong currentLocation) {
		return null;
	}

	@Override
	public int getNumSequences() {
		return nSequences;
	}

	@Override
	public String getVersion() {
		return version;
	}

}
