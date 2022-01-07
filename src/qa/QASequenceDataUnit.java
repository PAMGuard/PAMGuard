package qa;

import PamguardMVC.PamDataUnit;
import qa.generator.sequence.SoundSequence;

public class QASequenceDataUnit extends QADataUnit<QASoundDataUnit, QATestDataUnit> {

	private SoundSequence soundSequence;
	/**
	 * Distance to nearest airgun
	 */
	private Double distanceToAirgun;
	/**
	 * distance to nearest hydrophone
	 */
	private Double distanceToHydrophone;

	public QASequenceDataUnit(long timeMilliseconds, SoundSequence soundSequence) {
		super(timeMilliseconds);
		this.setSoundSequence(soundSequence);
		soundSequence.setSequenceDataUnit(this);
	}

	/**
	 * @return the soundSequence
	 */
	public SoundSequence getSoundSequence() {
		return soundSequence;
	}

	/**
	 * @param soundSequence the soundSequence to set
	 */
	public void setSoundSequence(SoundSequence soundSequence) {
		this.soundSequence = soundSequence;
	}
	/**
	 * @return the distanceToAirgun
	 */
	public Double getDistanceToAirgun() {
		return distanceToAirgun;
	}

	/**
	 * @param distanceToAirgun the distanceToAirgun to set
	 */
	public void setDistanceToAirgun(Double distanceToAirgun) {
		this.distanceToAirgun = distanceToAirgun;
	}

	/**
	 * @return the distanceToHydrophone
	 */
	public Double getDistanceToHydrophone() {
		return distanceToHydrophone;
	}

	/**
	 * @param distanceToHydrophone the distanceToHydrophone to set
	 */
	public void setDistanceToHydrophone(Double distanceToHydrophone) {
		this.distanceToHydrophone = distanceToHydrophone;
	}

	/**
	 * Calculate the percentage of QASounds that have been detected in this sequence
	 * 
	 * @return the percentage detected.  Note that if there are no sounds yet, a 0 is returned
	 */
	public double getFractionDetected() {
		int nS = getSubDetectionsCount();
		if (nS==0) {
			return 0;
		}
		double tot=0;
		for (int i = 0; i < nS; i++) {
			QASoundDataUnit aSound = getSubDetection(i);
			int nDets = aSound.getNumDetectorHits();
			if (nDets > 0) {
				tot += 1;
			}
		}
		System.out.printf("\n%3.0f sounds detected out of %d sounds",tot,nS);
		return tot/nS;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("QASoundSequence %s UId %d", soundSequence.getQaTestSet().getQaCluster().getSoundGenerator().getName(), getUID());
	}
	

}
