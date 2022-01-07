package qa.generator.sequence;

public class SequenceData implements Comparable<SequenceData> {

	private long startSample;
	
	private double amplitude;

	private SoundSequence soundSequence;

	private int sequencePosition;

	/**
	 * @param startSample
	 * @param amplitude
	 */
	public SequenceData(SoundSequence soundSequence, int sequencePosition, long startSample, double amplitude) {
		super();
		this.setSoundSequence(soundSequence);
		this.setSequencePosition(sequencePosition);
		this.startSample = startSample;
		this.amplitude = amplitude;
	}

	@Override
	public int compareTo(SequenceData other) {
		return (int) (this.startSample - other.startSample);
	}

	/**
	 * @return the startSample
	 */
	public long getStartSample() {
		return startSample;
	}

	/**
	 * @param startSample the startSample to set
	 */
	public void setStartSample(long startSample) {
		this.startSample = startSample;
	}

	/**
	 * @return the amplitude
	 */
	public double getAmplitude() {
		return amplitude;
	}

	/**
	 * @param amplitude the amplitude to set
	 */
	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
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
	 * @return the sequencePosition
	 */
	public int getSequencePosition() {
		return sequencePosition;
	}

	/**
	 * @param sequencePosition the sequencePosition to set
	 */
	public void setSequencePosition(int sequencePosition) {
		this.sequencePosition = sequencePosition;
	}

}
