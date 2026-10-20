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
 * The peak picking itself is in {@link IshPeakPicker}, one per channel, which can be tested
 * on its own; this class feeds it the detection function and turns its events into
 * IshDetections.
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

	IshDataBlock outputDataBlock;

	int savedFftLength = -1;		//-1 forces recalculation

	double[] outData;

	/** One peak picker per channel, made when the channel first has data. */
	private IshPeakPicker[] pickers = new IshPeakPicker[PamConstants.MAX_CHANNELS];

	/** Settings for the pickers: threshold, min, max and refractory times (s), audio and detection rates. */
	private double[] pickerParams;

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
		outputDataBlock = new IshDataBlock(ishDetControl.getUnitName() + " events", 
				this, parentDataBlock.getSequenceMap());
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
		/*
		 * The detection function's rate (dRate) is the audio rate for the matched filter,
		 * and the FFT slice rate for the two spectrogram detectors. The picker converts
		 * each time to the units it is compared in (see IshPeakPicker).
		 */
		double audioRate = ishDetControl.ishDetFnProcess.getSampleRate();
		double dRate = ishDetControl.ishDetFnProcess.getDetSampleRate();
		pickerParams = new double[] {p.thresh, p.minTime, p.maxTime, p.refractoryTime, audioRate, dRate};
		for (IshPeakPicker picker : pickers) {
			if (picker != null) {
				picker.setParams(p.thresh, p.minTime, p.maxTime, p.refractoryTime, audioRate, dRate);
			}
		}

		activeChannels = ishDetControl.getActiveChannels(); //no need to recalc this every iteration. 
	}

	public void prepareForRun() {
		// start every channel afresh
		pickers = new IshPeakPicker[PamConstants.MAX_CHANNELS];
	}

	private IshPeakPicker getPicker(int chanIx) {
		if (pickers[chanIx] == null) {
			if (pickerParams == null) {
				prepareMyParams();
			}
			double[] q = pickerParams;
			pickers[chanIx] = new IshPeakPicker(q[0], q[1], q[2], q[3], q[4], q[5]);
		}
		return pickers[chanIx];
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
		IshPeakPicker picker = getPicker(chanIx);
		double audioRate = ishDetControl.ishDetFnProcess.getSampleRate();

		int nSamples = inputData.length;
		for (int iSamp = 0; iSamp < nSamples; iSamp++) {
			IshPeakPicker.Event event = picker.add(ishFnDataUnit.getStartSample() + iSamp, inputData[iSamp]);
			if (event == null) {
				continue;
			}
			long startMsec = absSamplesToMilliseconds(event.startSample);
			long endMsec = startMsec + Math.round(1000.0 * event.durationSamples / audioRate);
			float lowFreq = ishDetControl.ishDetFnProcess.getLoFreq();
			float highFreq = ishDetControl.ishDetFnProcess.getHiFreq();
			IshDetection iDet = new IshDetection(startMsec, endMsec, lowFreq, highFreq, event.peakSample,
					event.peakHeight, outputDataBlock, arg1.getChannelBitmap(), event.startSample,
					event.durationSamples);
			iDet.setSequenceBitmap(arg1.getSequenceBitmapObject());
			iDet.setParentDataBlock(outputDataBlock);
			outputDataBlock.addPamData(iDet);
		}
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
