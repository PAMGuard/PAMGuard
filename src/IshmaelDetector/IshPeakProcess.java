package IshmaelDetector;

import java.awt.Color;

import PamUtils.PamUtils;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;

/**
 * Generic process which picks a peak from any of the Ishmael detectors, e.g. 
 * energy sum, spectral template etc. 
 * <p>
 * Note: Variables named <i>xx</i>N are time in units of number of FFT bins. Variables <i>xx</i>Sam are 
 * variables in units of raw samples. 
 * 
 * @author Dave Mellinger and Hisham Qayum 
 */
public class IshPeakProcess extends PamProcess
{

	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_DIAMOND, 10, 10, false, Color.GREEN, Color.GREEN);
	//public Complex[] inputData;
	//IshDetCalcIntfc ishDetCalcIntfc;
	//IshDetIoIntfc ishDetIoIntfc;
//	FFTDataSource parentProcess;
	IshDetControl ishDetControl;
	
	PamDataBlock<IshDetection> outputDataBlock;
	
	int savedFftLength = -1;		//-1 forces recalculation
	
	double[] outData;
	
	long minTimeN, maxTimeN, refactoryTimeSam;	//time values converted to slice numbers
	
	PerChannelInfo perChannelInfo[] = new PerChannelInfo[PamConstants.MAX_CHANNELS];
	
	/**
	 * The active channels i.e. the trigger channels. 
	 */
	private int activeChannels;
	
	/**
	 * The Ishmael binary data source. 
	 */
	private IshBinaryDataSource ishmealBinaryDataSource;
	
	
	public IshPeakProcess(IshDetControl ishDetControl, 
			PamDataBlock parentDataBlock) {
		super(ishDetControl, null);
		this.ishDetControl = ishDetControl;
		setParentDataBlock(parentDataBlock);
//		outputDataBlock = new PamDataBlock<IshDetection>(IshDetection.class, 
//				ishDetControl.getUnitName() + " events", this, parentDataBlock.getChannelMap());
		outputDataBlock = new PamDataBlock<IshDetection>(IshDetection.class, 
				ishDetControl.getUnitName() + " events", this, parentDataBlock.getSequenceMap());
		outputDataBlock.setCanClipGenerate(true);
		addOutputDataBlock(outputDataBlock);
		
		//set the binary data source. 
		ishmealBinaryDataSource = new IshBinaryDataSource(this, outputDataBlock, "Ishmael Detections");
		outputDataBlock.setBinaryDataSource(ishmealBinaryDataSource);

		//graphics for the map and spectrogram. 
		PamDetectionOverlayGraphics overlayGraphics = new PamDetectionOverlayGraphics(outputDataBlock, new PamSymbol(defaultSymbol));
		outputDataBlock.setOverlayDraw(overlayGraphics);
		StandardSymbolManager symbolManager = new StandardSymbolManager(outputDataBlock, defaultSymbol, true);
		symbolManager.addSymbolOption(StandardSymbolManager.HAS_LINE_AND_LENGTH);
		outputDataBlock.setPamSymbolManager(symbolManager);
		IshLogger ishLogger = new IshLogger(ishDetControl, outputDataBlock);		
		outputDataBlock.SetLogging(ishLogger);
		setupConnections();
	}
	
	@Override
	public void setParentDataBlock(PamDataBlock newParentDataBlock) {
		super.setParentDataBlock(newParentDataBlock);
	}
	
	/**
	 * Gte all channels used- these are NOT the actiuve channels
	 * @return the active channels. 
	 */
	public int getChannelMap() {
		return ishDetControl.ishDetParams.groupedSourceParmas.getChanOrSeqBitmap(); 
	}
	
	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		super.setSampleRate(sampleRate, notify);
		prepareMyParams();
	}
	
	public void setupConnections() {
		// Find the existing source data block and remove myself from observing it.
		// Then find the new one and subscribe to that instead. 
		//if (getParentDataBlock() != null) 
			//getParentDataBlock().deleteObserver(this);
		
		if (ishDetControl == null) 
			return;
		IshDetParams p = ishDetControl.ishDetParams;  //local reference
		//PamDataBlock detfnDataBlock = 
			//PamController.getInstance().getDetectorDataBlock(p.detfnDataSource);
		//setParentDataBlock(detfnDataBlock);

		prepareMyParams();
//		outputDataBlock.setChannelMap(p.channelList);
		PamDataBlock inputDataBlock = getParentDataBlock();
		outputDataBlock.sortOutputMaps(inputDataBlock.getChannelMap(), inputDataBlock.getSequenceMapObject(), p.groupedSourceParmas.getChanOrSeqBitmap());
		//setProcessName("Peak-picker: threshold " + p.thresh);
		//setSampleRate(sampleRate, true);	//set rate for outputDataBlock.
	}

	/** 
	 * Calculate any subsidiary values needed for processing.  These get recalculated
	 * whenever the sample rate changes (via setSampleRate, which is also called after 
	 * the params dialog box closes).
	 */
	protected void prepareMyParams() {
		IshDetParams p = ishDetControl.ishDetParams;  //local reference
		float dRate     = ishDetControl.ishDetFnProcess.getDetSampleRate();
		
		minTimeN        = Math.max(0, (long)(dRate * p.minTime));
		
		// if maxTime==0, it means we're not using it so set the param to infinity so that we are always under it
		if (p.maxTime==0) {
			maxTimeN = Long.MAX_VALUE-1;	// -1, because we test against maxTimeN+1 later; if we don't subtract 1 here, it rolls over to 0
		}
		else {
			maxTimeN        = Math.max(0, (long)(dRate * p.maxTime));
		}
		
		refactoryTimeSam = Math.max(0, (long)(this.getSampleRate() * p.refractoryTime));
		
		activeChannels = ishDetControl.getActiveChannels(); //no need to recalc this every iteration. 
	}

	public void prepareForRun() {
		//Refresh all the PerChannelInfo's.  This resets chan.nOverThresh to 0.
		for (int i = 0; i < perChannelInfo.length; i++)
			perChannelInfo[i] = new PerChannelInfo();
	}
	
	/* 
	 * PeakProcess uses recycled data blocks; the length of the data unit should
	 * correspond to the output of the detector function: Just one double.
	 */
	@Override
	public void newData(PamObservable o, PamDataUnit arg1) {  
		//called from PamProcess
		IshDetFnDataUnit ishFnDataUnit = (IshDetFnDataUnit)arg1;
		double[] inputData = ishFnDataUnit.getDetData()[0];

		//See if the channel is one we want before doing anything.
//		if ((arg.getChannelBitmap() & ishDetControl.ishDetParams.channelList) == 0)
		if ((activeChannels & ishFnDataUnit.getSequenceBitmap())==0) {
			return;
		}
//		int chanIx = PamUtils.getSingleChannel(arg.getChannelBitmap());
		
		int chanIx = PamUtils.getSingleChannel(ishFnDataUnit.getSequenceBitmap());
		PerChannelInfo chan = perChannelInfo[chanIx];
		
		//The actual peak-picking calculation. Check whether the peak is over calculation and 
		//that the peak has not exceeded the maximum time... 
		if (isIshOverThreshold(inputData[0]) && chan.nOverThresh <=maxTimeN+1) {
			if (chan.nOverThresh == 0) {
				chan.peakHeight = Double.NEGATIVE_INFINITY; 
			}
			chan.nOverThresh++;
//			System.out.println("above threshold, startSample= " + String.valueOf(ishFnDataUnit.getStartSample()) + ", inputData= " + String.valueOf(inputData[0]) + ", nOverThresh= "+ String.valueOf(chan.nOverThresh));
			if (inputData[0] >= chan.peakHeight) {
				chan.peakHeight = inputData[0];
				chan.peakTimeSam = ishFnDataUnit.getStartSample(); //
			}
		} 
		else {
//			System.out.println("below threshold, nOverThresh = "+ String.valueOf(chan.nOverThresh) + ", minTimeN= "+ String.valueOf(minTimeN));
			// Below threshold or over the maximum time allowed. 
			// Check to see if we were over enough times, and if so,
			//add an output unit. Data has to be less then the max time
			if (chan.nOverThresh > 0 && chan.nOverThresh >= minTimeN && chan.nOverThresh<=maxTimeN) {
				
				//Append the new data to the end of the data stream.
				long startSam = ishFnDataUnit.getStartSample();
				long durationSam = chan.nOverThresh;
				durationSam *= sampleRate / (ishDetControl.ishDetFnProcess.getDetSampleRate());
				long startMsec = absSamplesToMilliseconds(startSam - durationSam);
				long endMsec = absSamplesToMilliseconds(startSam) + 
				Math.round(1000.0 * (float)durationSam * ishDetControl.ishDetFnProcess.getDetSampleRate()); 
				float lowFreq = ishDetControl.ishDetFnProcess.getLoFreq();
				float highFreq = ishDetControl.ishDetFnProcess.getHiFreq();
			
				IshDetection iDet = outputDataBlock.getRecycledUnit();
				if (iDet != null) {                //refurbished
//					iDet.setInfo(startMsec, 1 << chanIx, startSam, durationSam, 
//							lowFreq, highFreq, chan.peakTimeSam, chan.peakHeight);
					iDet.setInfo(startMsec, arg1.getChannelBitmap(), startSam, durationSam, 
							lowFreq, highFreq, chan.peakTimeSam, chan.peakHeight);
					iDet.setSequenceBitmap(arg1.getSequenceBitmapObject());
				} else {                           //new
//					iDet = new IshDetection(startMsec, endMsec, lowFreq, highFreq, chan.peakTimeSam, 
//							chan.peakHeight, outputDataBlock, 1 << chanIx, startSam, durationSam);
					//11/03/2020 - major bug fix - start sample of Ishamel detector data unit was wrong - it was at the end of the data unit for some reason. 
					iDet = new IshDetection(startMsec, endMsec, lowFreq, highFreq, chan.peakTimeSam, 
							chan.peakHeight, outputDataBlock, arg1.getChannelBitmap(), startSam - durationSam, durationSam);
					
					iDet.setSequenceBitmap(arg1.getSequenceBitmapObject());
					iDet.setParentDataBlock(outputDataBlock);
				}
				
				//now check if the refactory time is OK

				
				double iDISam =Math.abs((startSam) - (chan.lastStartSam+durationSam)); 
				
//				System.out.println("Samplediff: " +  iDISam 
//				+ " maxTimeN: " + this.maxTimeN + " sR: " + ishDetControl.ishDetFnProcess.getDetSampleRate()); 
				if (chan.lastStartSam==-1 || startSam<chan.lastStartSam || iDISam>=refactoryTimeSam) {
					outputDataBlock.addPamData(iDet);
//					System.out.println("");
//					System.out.println("Ishmael data unit accepted because of refactory time: " 
//							+ startSam + "  " + chan.lastStartSam + " minIDI: (samples): " + refactoryTimeSam 
//							+ " minIDI (s) " + this.ishDetControl.ishDetParams.refractoryTime);
				}
				else {
//					System.out.println("");
//					System.out.println("Ishmael data unit REJECTED because of refactory time: " + startSam 
//							+ "  " + chan.lastStartSam + " minIDI: (samples): " + refactoryTimeSam 
//							+ " minIDI (s) " + this.ishDetControl.ishDetParams.refractoryTime);
				}
				//keep a reference to the last time and duration
				chan.lastStartSam = startSam; 
				chan.lastDurationSam = durationSam; 

			}
			chan.nOverThresh = 0;
		}
	}
	

	/**
	 * Checks whether a peak is over threshold...
	 * @param value - the current value of the detector
	 * @return true if value is above the peak threshold. 
	 */
	private boolean isIshOverThreshold(double value) {
		if (value > ishDetControl.ishDetParams.thresh) {
			return true;
		}
		return false; 
	} 
	
	private class PerChannelInfo {
		

		/**
		 * The sample time of the start of the last saved detection (samples)
		 */
		long lastStartSam = -1;	//samples
		long lastDurationSam = -1;	//samples
		int nOverThresh = 0;		//number of times we've been over threshold in units of FFT length
		double peakHeight;			//height of peak within the current event
		long peakTimeSam;			//sample number at which that peak occurred
	}

	@Override public void pamStart() {
		//This doesn't get called because we don't do addPamProcess(ishPeakProcess).
		//(Why not?  Then it shows up in the data model.)
	}
	
	//This keeps the compiler happy -- it's abstract in the superclass.
	@Override public void pamStop() { }

	public PamDataBlock getOutputDataBlock() {
		 return this.outputDataBlock;
	}
}
