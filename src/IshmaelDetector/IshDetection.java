package IshmaelDetector;

import PamDetection.PamDetection;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

/** 
 * Basic detection on a single channel for the Ishmael det/loc.
 * <p>
 * Note that the Ishmael detection does not contain raw wav data - this is
 * intentional as the clip generator can be used to generate clips if required. 
 * As the Ishmael detector is a high false positive rate intial detection stage
 * then it may very well be beneficial not to store clips but simply pass the raw 
 * data to downstream processes. 
 *  
 * @author Doug Gillespie, Jamie Macaulay and Dave Mellinger 
 *
 */
public class IshDetection extends PamDataUnit<PamDataUnit, SuperDetection> implements PamDetection {

	static public final long serialVersionUID = 0;
	String name = "";				//not sure how used
	private double peakDelaySec; 	//Time delay of peak relative to start of detection 
	private double peakHeight;		//...and its height
	private double peakTimeSam; 	//Time of peak in samples
	protected String callType = "";

	public IshDetection(long startMsec, long endMsec,
			float lowFreq, float highFreq, long peakTimeSam, double peakHeight,
			PamDataBlock parentDataBlock, int channelBitmap, long startSam, long durationSam)
	{
		super(startMsec, channelBitmap, startSam, durationSam);
		peakDelaySec = (peakTimeSam - startSam)/parentDataBlock.getSampleRate(); 
		setInfo(startMsec, channelBitmap, startSam, durationSam, lowFreq, highFreq, peakTimeSam, peakHeight);
	}
	
	
	public IshDetection(DataUnitBaseData baseData, long peakTimeSample, double peakHeight) {
		super(baseData);
		setInfo(baseData.getTimeMilliseconds(), baseData.getChannelBitmap(), 
				baseData.getStartSample(), baseData.getSampleDuration(), (float) baseData.getFrequency()[0], 
				(float) baseData.getFrequency()[1], peakTimeSample, peakHeight);

	}


	public String getCallType() { return callType; }
	public void setCallType(String callType) { this.callType = callType; }
	public double getPeakDelaySec() { return peakDelaySec; }
	public double getPeakHeight() { return peakHeight; }
	public double getPeakTimeSam() {return peakTimeSam; }
	public void setPeakDelaySec(double peakDelaySec) {this.peakDelaySec = peakDelaySec; }
	public void setPeakHeight(double peakHeight) { this.peakHeight = peakHeight; }
	public void setPeakTimeSam(double peakTimeSam) { this.peakTimeSam = peakTimeSam; }

	/** Set various parameters.
	 * @param startMsec
	 * @param channelBitmap
	 * @param startSam          relative to start of PAMGUARD run
	 * @param durationSam			
	 * @param lowFreq			lower edge of call T/F box, Hz
	 * @param highFreq			upper edge of call T/F box, Hz
	 * @param peakTimeSam		relative to start of PAMGUARD run
	 * @param peakHeight		measure of detection quality; different detectors
	 * 							will scale it differently, so it's only comparable
	 * 							within a detector type
	 */  
	public void setInfo(long startMsec, int channelBitmap, long startSam, 
			long durationSam, float lowFreq, float highFreq, long peakTimeSam, 
			double peakHeight) {
		setInfo(startMsec, channelBitmap, startSam, durationSam);
		double[] frequency = new double[2];
		frequency[0] = lowFreq;
		frequency[1] = highFreq;
		setFrequency(frequency);
		setPeakHeight(peakHeight);
		setPeakTimeSam(peakTimeSam);     //relative to start of PAMGUARD run
	}


	@Override
	public String getSummaryString() {
		String str = super.getSummaryString();
		str += String.format("<br>Peak %5.3f", peakHeight);
		
		return str;
	}

}
