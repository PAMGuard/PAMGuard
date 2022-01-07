package IshmaelDetector;


//import clickDetector.ClickLogger;
//import PamDetection.RawDataUnit;
//import fftManager.FFTDataUnit;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
//import PamguardMVC.PamDataUnit;
import PamDetection.LocContents;
import PamguardMVC.PamProcess;

/**
 * This is the abstract superclass for all the "Ishmael detectors", i.e., the
 * detection PamProcesses that use a detection function and threshold in the
 * course of doing making detections. Currently this means it is the superclass
 * of EnergySumProcess, SgramCorrProcess, and MatchFiltProcess.
 * 
 * @author Dave Mellinger
 */ 
@SuppressWarnings("rawtypes")
public abstract class IshDetFnProcess extends PamProcess
{
	IshDetControl ishDetControl;
//	RecyclingDataBlock<IshDetFnDataUnit> outputDataBlock; 
//	RecyclingDataBlock<PamDataUnit> outputDataBlock;
	PamDataBlock outputDataBlock;
	
	
	/**
	 * The channel number 
	 */
	int channel;
	
	/**
	 * The binary data source. Records the raw detector output. 
	 */
	private IshmaelBinaryDataSource ishmealBinaryDataSource;
	
	/** Initialiser. 
	 * <p>IMPORTANT: The subclass initializer should construct the ishDetParams
	 * before calling this.
	 * 
	 * @param ishDetControl -- e.g., an EnergySumControl, SgramCorrControl, etc.
	 * @param parentDataBlock -- an FFTDataBlock or RawDataBlock
	 * @author Dave Mellinger
	 */
	public IshDetFnProcess(IshDetControl ishDetControl, PamDataBlock parentDataBlock)	{
		super(ishDetControl, null);
		this.channel = 0;         //we just fake the channel for now
		this.ishDetControl = ishDetControl;
		//this.ishDetParams = ishDetControl.ishDetParams;
		setParentDataBlock(parentDataBlock);

		//public IshLogger(IshDetControl ishDetControl, PamDataBlock<PamDataUnit> pamDataBlock) 

		outputDataBlock = new PamDataBlock(
				IshDetFnDataUnit.class, getLongName(), this, 1 << channel);
		
		
		ishmealBinaryDataSource = new IshmaelBinaryDataSource(this, outputDataBlock, "Ishmael_det_raw");
		outputDataBlock.setBinaryDataSource(ishmealBinaryDataSource);
		
//		outputDataBlock = new RecyclingDataBlock<IshDetFnDataUnit>(
//				IshDetFnDataUnit.class, getLongName(), this, 1 << channel);
		//Declare the possible types of localization (bearing, XY, etc.) for this detection.
		//The actual type of each specific localization is set in a subclass's calcData
		//(IshLocPairProcess.calcData or IshLocHyperbProcess.calcData or whatever).
		outputDataBlock.setLocalisationContents( 
				LocContents.HAS_BEARING | LocContents.HAS_AMBIGUITY | 
				LocContents.HAS_XY | LocContents.HAS_XYZ);
		//Set up data logging.
//		IshLogger ishLogger = new IshLogger(ishDetControl, outputDataBlock);		
//		outputDataBlock.SetLogging(ishLogger);
//		outputDataBlock.setOverlayDraw(new PamDetectionOverlayGraphics(outputDataBlock));
		addOutputDataBlock(outputDataBlock);
		
		setupConnections();
	}
	
	public abstract String getLongName();		//e.g., "Energy sum detector data"
	
	public abstract Class inputDataClass();		//e.g., FFTDataUnit.class
	
	/* This is DetSampleRate to distinguish it from sampleRate, which is usually
	 * the rate of audio samples.  This might be either the FFT rate (for, e.g.,
	 * spectrogram correlation) or the audio sample rate (for, e.g., matched filtering).
	 */
	public abstract float getDetSampleRate();
	
	public int getChannelMap() {
		return ishDetControl.ishDetParams.groupedSourceParmas.getChanOrSeqBitmap();
	}

	/** An IshDetFnProcess has one input stream.  Return it, or null if it's
	 * not available.
	 */
	public PamDataBlock getInputDataBlock() {
		IshDetParams p = ishDetControl.ishDetParams;
		
		return (p == null || p.groupedSourceParmas.getDataSource() == null) ? getParentDataBlock() :
			PamController.getInstance().getDataBlock(inputDataClass(), p.groupedSourceParmas.getDataSource());
	}
	
	/** This is called when the sample rate changes.  It also gets called on
	 * other occasions, like when the model changes (or the FFT size changes??).
	 */
	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		super.setSampleRate(sampleRate, notify);
		prepareMyParams();  //if sample rate changes, recalculate local params
	}
	
	public void setupConnections() {
		// Find the existing source data block and remove myself from observing it.
		// Then find the new one and subscribe to that instead. 
		// 2020-01-28 removed - not req'd because setParentDataBlock does this on it's own, and putting it in here causes confusion later on because
		// the correct bookkeeping is not being done.
//		if (getParentDataBlock() != null) 
//			getParentDataBlock().deleteObserver(this);
		if (ishDetControl == null) 
			return;

		IshDetParams p = ishDetControl.ishDetParams;			//shorthand
		PamDataBlock inputDataBlock = getInputDataBlock();		//might be null
		setParentDataBlock(inputDataBlock);		//in case it wasn't parent already
		
		// get rid of this next part - setParentDataBlock sets up this process as a threaded observer.  The
		// next line sets it up as a direct observer, which looks (to PamProcess) like a diff process and therefore
		// can be added in addition to the prev one.  The result is that IshDetFnProcess.newData gets called
		// twice every time an FFT data unit comes in
//		if (inputDataBlock != null)
//			inputDataBlock.addObserver(this);	//should happen in setParentDataBlock, but doesn't always

		prepareMyParams();
//		outputDataBlock.setChannelMap(p.channelList);
		outputDataBlock.sortOutputMaps(inputDataBlock.getChannelMap(), inputDataBlock.getSequenceMapObject(), p.groupedSourceParmas.getChanOrSeqBitmap());
		//setSampleRate(sampleRate, true);	//set rate for outputDataBlock
	}
	
	/** Get a new IshDetDataUnit suitable for holding output data.
	 * This code was copied from PamFFTProcess.newData() and modified.
	 * 
	 * @param rawOrFftDataUnit   input to the detector process (EnergySumProcess, etc.).
	 * @return a fresh IshDetDataUnit, with time fields and channel bitmap set
	 */ 
//	public IshDetFnDataUnit getOutputDataUnit(FFTDataUnit fftDataUnit) {}
	public IshDetFnDataUnit getOutputDataUnit(PamDataUnit rawOrFftDataUnit) {
		long startSample    = rawOrFftDataUnit.getStartSample();
		long startMsec      = absSamplesToMilliseconds(startSample);
		long dur            = rawOrFftDataUnit.getSampleDuration();
		int bmap            = rawOrFftDataUnit.getChannelBitmap();

		IshDetFnDataUnit iddu = new IshDetFnDataUnit(startMsec, bmap, startSample, dur, null);
		iddu.setSequenceBitmap(rawOrFftDataUnit.getSequenceBitmapObject());
		
		//Old code from when recycledDataUnits were used:
//		IshDetFnDataUnit iddu = (IshDetFnDataUnit)outputDataBlock.getRecycledUnit();
//		if (iddu != null) {                //refurbished
//			iddu.setInfo(startMsec, bmap, startSample, dur);
//		} else {                           //new
//			iddu = new IshDetFnDataUnit(startMsec, bmap, startSample, dur, null);
//			iddu.setParentDataBlock(outputDataBlock);
//		}

		return iddu;
	}

	/** Get a new IshDetDataUnit suitable for holding output data.
	 * This code was copied from PamFFTProcess.newData() and modified.
	 * 
	 * @param rawDataUnit   input to the detector process (MatchFiltProcess, etc.).
	 * @return a fresh IshDetDataUnit, with time fields and channel bitmap set
	 */
	//Old code from when recycledDataUnits were used:
//	public IshDetFnDataUnit getOutputDataUnit(RawDataUnit rawDataUnit) {
//		long startSample    = rawDataUnit.getStartSample();
//		long startMsec      = absSamplesToMilliseconds(startSample);
//		long dur            = rawDataUnit.getDuration();
//		int bmap            = rawDataUnit.getChannelBitmap();
//
//		IshDetFnDataUnit iddu = new IshDetFnDataUnit(startMsec, bmap, startSample, dur, null);
//		
//		//Old code from when recycledDataUnits were used:
////		IshDetFnDataUnit iddu = (IshDetFnDataUnit)outputDataBlock.getRecycledUnit();
////		if (iddu != null)                 //refurbished
////			iddu.setInfo(startMsec, bmap, startSample, dur);
////		else {                             //new
////			iddu = new IshDetFnDataUnit(startMsec, bmap, startSample, dur, null);
////			iddu.setParentDataBlock(outputDataBlock);
////		}
//
//		return iddu;
//	}

	//This is called when the user chooses Start Detection off the menu.
	@Override
	public void prepareProcess() {
		ishDetControl.prepareNonDetProcesses();
	}
	
	//Calculate any subsidiary values needed for processing.  These get recalculated
	//whenever the sample rate changes (via setSampleRate, which is also called after 
	//the params dialog box closes).
	//Note: during initialization, this gets called with params.fftDataSource
	//still null.
	protected void prepareMyParams() {
//		IshDetParams p = ishDetControl.ishDetParams;			//shorthand
//		PamDataBlock inputDataBlock = 
//			PamController.getInstance().getDataBlock(null, p.inputDataSource);
	}
	
	//Keep the compiler happy -- these are abstract in the superclass.
	@Override public void pamStart() {
		//System.out.println("IshDetFnProcess: " +this.getSampleRate());
	} 
	
	
	@Override public void pamStop() { }


	/**
	 * 
	 * @return the lowest frequency of the detection - e.g. 0 or
	 * the lower bound of the detection kernel
	 */
	public abstract float getLoFreq();
	/**
	 * 
	 * @return the highest frequency of the detection - e.g. sampleRate/2 or
	 * the upper bound of the detection kernel
	 */
	public abstract float getHiFreq();
}
