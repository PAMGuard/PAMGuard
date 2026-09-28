package PamguardMVC.superdet;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import PamDetection.LocContents;
import PamUtils.avrgwaveform.AverageWaveform;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import clickTrainDetector.IDIInfo;

/**
 * A group of acoustic detections (clicks, whistles, etc.). Adds functionality
 * to the basic {@link DetectionGroup} which only makes sense when the sub
 * detections are acoustic: an average waveform and spectrum, inter-detection
 * interval statistics and a bearing (angle) range.
 * <p>
 * Average waveform calculation is off by default since it has a cost on every
 * added sub detection; subclasses which want it (e.g. click trains) should call
 * {@link #setCalculateAverages(boolean)} in their constructor.
 *
 * @author Jamie Macaulay, Doug Gillespie
 */
public class AcousticDetectionGroup<T extends PamDataUnit> extends DetectionGroup<T> {

	/**
	 * The default FFT length for the average templates.
	 */
	public static int defaultFFTLen = 2048;

	/**
	 * The average waveform. May be null if averages are not being calculated.
	 */
	protected AverageWaveform averageWaveform = null;

	/**
	 * True to update the average waveform as sub detections are added.
	 */
	private boolean calculateAverages = false;

	/**
	 * The current inter-detection interval info.
	 */
	private IDIInfo currentIDIInfo = new IDIInfo();

	/**
	 * The minimum bearing angle of all data units in RADIANS
	 */
	private double minAngle = 0;

	/**
	 * The maximum bearing angle of all bearing units in RADIANS
	 */
	private double maxAngle = 0;

	public AcousticDetectionGroup(T firstDetection) {
		super(firstDetection);
	}

	public AcousticDetectionGroup(long timeMilliseconds, int channelBitmap, long startSample, long duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
	}

	public AcousticDetectionGroup(long timeMilliseconds) {
		super(timeMilliseconds);
	}

	/**
	 * @return true if the average waveform is updated as sub detections are added.
	 */
	public boolean isCalculateAverages() {
		return calculateAverages;
	}

	/**
	 * Set whether the average waveform is updated as sub detections are added.
	 * @param calculateAverages true to calculate averages.
	 */
	public void setCalculateAverages(boolean calculateAverages) {
		this.calculateAverages = calculateAverages;
	}

	@Override
	public int addSubDetection(T subDetection) {
		if (calculateAverages) {
			addToAverageWaveform(subDetection);
		}
		return super.addSubDetection(subDetection);
	}

	/**
	 * Make sure average waveform and IDI info exist. Needed because sub detection
	 * lists can be added from super class constructors.
	 */
	public void checkAverageWaveformInfo() {
		if (averageWaveform==null || this.getSubDetections() ==null || this.getSubDetections().size()<1) averageWaveform = new AverageWaveform();
		if (currentIDIInfo==null  || this.getSubDetections() ==null || this.getSubDetections().size()<1) currentIDIInfo = new IDIInfo();
	}

	/**
	 * Adds to the average waveform if the data unit contains raw info, otherwise
	 * adds the data unit's frequency limits and amplitude to a frequency
	 * histogram.
	 * @param dataUnit - the data unit to add.
	 */
	public void addToAverageWaveform(PamDataUnit dataUnit) {
		if (averageWaveform == null) {
			averageWaveform = new AverageWaveform();
		}
		if (dataUnit instanceof RawDataHolder) {
			averageWaveform.addWaveform(((RawDataHolder) dataUnit).getWaveData()[0],
					dataUnit.getParentDataBlock().getSampleRate(), defaultFFTLen, false);
		}
		else {
			averageWaveform.addWaveform(dataUnit.getBasicData().getFrequency()[0],dataUnit.getBasicData().getFrequency()[1],
					dataUnit.getAmplitudeDB(),
					dataUnit.getParentDataBlock().getSampleRate(), defaultFFTLen);
		}
	}

	@Override
	public void removeAllSubDetections() {
		super.removeAllSubDetections();
		if (averageWaveform!=null) {
			averageWaveform.clearAvrgData();
			currentIDIInfo = new IDIInfo();
		}
	}

	public AverageWaveform averageWaveform() {
		return averageWaveform;
	}

	/**
	 * Get an average waveform for the data unit.
	 * @return the average waveform, or null if none has been calculated.
	 */
	public double[] getAverageWaveform() {
		if (averageWaveform==null) return null;
		return this.averageWaveform.getAverageWaveform();
	}

	/**
	 * Get an average spectrum for the data unit.
	 * @return the average spectrum, or null if none has been calculated.
	 */
	public double[] getAverageSpectra() {
		if (averageWaveform==null) return null;
		return this.averageWaveform.getAverageSpectra();
	}

	/**
	 * Set the average waveform.
	 * @param averageWaveform the average waveform to set.
	 */
	public void setAverageWaveform(AverageWaveform averageWaveform) {
		this.averageWaveform=averageWaveform;
	}

	/**
	 * Get the IDI info for the group, recalculating if the number of sub
	 * detections has changed since the last calculation.
	 * @return the IDIInfo for the group.
	 */
	public IDIInfo getIDIInfo() {
		if (currentIDIInfo == null) {
			currentIDIInfo = new IDIInfo();
		}
		if (currentIDIInfo.lastNumber != this.getSubDetectionsCount()) {
			currentIDIInfo.calcTimeSeriesData(this.getSubDetections());
		}
		return this.currentIDIInfo;
	}

	/**
	 * Force an update of the IDI calculation. This should be used if data unit
	 * information changes. The IDI is automatically updated when new data units
	 * are added.
	 */
	public void forceIDIUpdater() {
		if (currentIDIInfo != null) {
			currentIDIInfo.lastNumber=-1;
		}
	}

	/**
	 * Set the current IDI info.
	 * @param idiInfo - the IDI info.
	 */
	public void setIDIInfo(IDIInfo idiInfo) {
		this.currentIDIInfo=idiInfo;
	}

	/**
	 * Calculate the minimum and maximum angle of the whole group.
	 */
	public void calcMinMaxAng() {
		ArrayList<PamDataUnit<?,?>> subDet = this.getSubDetections();

		if (subDet==null || subDet.size() < 1 || subDet.get(0).getLocalisation()==null) {
			return;
		}

		ListIterator<PamDataUnit<?, ?>> iterator = subDet.listIterator();

		double maxAngle = -Double.MAX_VALUE;
		double minAngle = Double.MAX_VALUE;

		double lastAngle;
		PamDataUnit dataUnit;
		double angle;
		boolean hasBearing = subDet.get(0).getParentDataBlock().getLocalisationContents().hasLocContent(LocContents.HAS_BEARING);
		while (iterator.hasNext()) {
			dataUnit=iterator.next();
			if (dataUnit.getLocalisation() == null) {
				continue;
			}
			angle=dataUnit.getLocalisation().getAngles()[0];
			lastAngle= angle;
			if (hasBearing){
				if (lastAngle>maxAngle) maxAngle=lastAngle;
				if (lastAngle<minAngle) minAngle=lastAngle;
			}
		}

		if (maxAngle > -Double.MAX_VALUE) {
			this.minAngle=minAngle;
			this.maxAngle=maxAngle;
		}
	}

	/**
	 * Get the angle range in RADIANS.
	 * @return the angle range.
	 */
	public double getAngleRange() {
		return maxAngle-minAngle;
	}

}
