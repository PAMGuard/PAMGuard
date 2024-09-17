/**
 * 
 */
package IshmaelLocator;


import java.awt.event.MouseEvent;

import Array.ArrayManager;
import Array.PamArray;
import Array.SnapshotGeometry;
import IshmaelDetector.IshDetControl;
import IshmaelDetector.IshDetection;
import PamController.PamController;
import PamDetection.PamDetection;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RawDataUnavailableException;
import Spectrogram.SpectrogramDisplay;
import Spectrogram.SpectrogramMarkObserver;
import Spectrogram.SpectrogramMarkObservers;
import dataPlotsFX.layout.TDGraphFX;
import fftManager.Complex;
import fftManager.FFTDataBlock;
import pamMaths.PamVector;
import warnings.PamWarning;
import warnings.WarningSystem;

/** Various location algorithms can fail to find a solution.  If so,
 * they just throw this Exception.
 * @author Dave Mellinger
 */
class noLocationFoundException extends Exception {
	static final long serialVersionUID = 0;
}


/** This is a superclass to a "real" localization class. It gathers the
 * necessary data (samples, phone positions, etc.) for the subclass, then calls
 * calcData(), which the subclass should define. Current children include
 * IshLocPairProcess and IshLocHyperbProcess.
 * 
 * @author Dave Mellinger
 */
abstract public class IshLocProcess extends PamProcess implements SpectrogramMarkObserver
{
	double[][] arraygeom;			//index different from hydlist; 2 or 3 cols
	double c;						//speed of sound
	public Complex[] inputData;
	int[] hydlist;					//indices of phones in incoming data 
	Complex[] v1, v2;
	IshLocControl ishLocControl;	//back-pointer for my control
	PamDataBlock<PamDataUnit> outputDataBlock;
	private static PamWarning IshWarning = new PamWarning("Ishmael Localiser", "", 2);
	//PamDataBlock outputDataBlock;
	
	//We don't really *need* a parent process here, as spectrogramNotification()
	//provides everything we need.  Keep the parent case we need it in the future. 
	//FFTDataSource parentFFTProcess;	
	
	public IshLocProcess(IshLocControl ishLocControl) {
		super(ishLocControl, null);
		this.ishLocControl = ishLocControl;
		outputDataBlock = new PamDataBlock<PamDataUnit>(PamDetection.class,
				this.getName(), this, 0);
		outputDataBlock.setOverlayDraw(new IshOverlayGraphics(outputDataBlock));
		StandardSymbolManager symbolManager = new StandardSymbolManager(outputDataBlock, IshOverlayGraphics.defaultSymbol, true);
		symbolManager.addSymbolOption(StandardSymbolManager.HAS_LINE_AND_LENGTH);
		outputDataBlock.setPamSymbolManager(symbolManager);
		addOutputDataBlock(outputDataBlock);
		IshLocSqlLogging ishLocSqlLogging = new IshLocSqlLogging(this.ishLocControl, outputDataBlock);
		outputDataBlock.SetLogging(ishLocSqlLogging);


		//Set it up so that whenever a spectrogram mark is made, 
		//this.spectrogramNotification() gets called.
		SpectrogramMarkObservers.addSpectrogramMarkObserver(this);
	}
	
	public abstract String getName();
	
	@Override
	public void setParentDataBlock(PamDataBlock newParentDataBlock) {
		super.setParentDataBlock(newParentDataBlock);
		//PamProcess proc = getParentProcess();
		//parentFFTProcess = (PamFFTProcess)proc;
	}

	/** An IshLocProcess has one input stream (data block).  Return it, or null 
	 * if it's not available.
	 */
	public PamDataBlock getInputDataBlock() {
		IshLocParams p = ishLocControl.ishLocParams;
		PamDataBlock inputDataBlock;
		
		if (!p.useDetector) {
			WarningSystem.getWarningSystem().removeWarning(IshWarning);
			return null;
		}
		if (p == null || p.inputDataSource == null) {
			WarningSystem.getWarningSystem().removeWarning(IshWarning);
			return getParentDataBlock();
		}
		else
			inputDataBlock = PamController.getInstance().getDataBlock(PamDetection.class, p.inputDataSource);
		
		// quick check here - if the source was derived from a beamformer, we cannot localize because the channel map is ambiguous.  In that case, throw a warning
		if (inputDataBlock.getSequenceMapObject()!=null) {
			String err = "Error: the selected Source Detector uses Beamformer output as a data source, and Beamformer output does not contain "
			+ "the link back to a single channel of raw audio data that is required for analysis.  Please either change the Source "
			+ "Detector's data source, or select a different Detector.";
			IshWarning.setWarningMessage(err);
			WarningSystem.getWarningSystem().addWarning(IshWarning);
			p.useDetector=false;
			return null;
		} else {
			WarningSystem.getWarningSystem().removeWarning(IshWarning);
			return inputDataBlock;
		}
	}
	
	@Override
	public void setupProcess() {
		super.setupProcess();
		outputDataBlock.setNaturalLifetime(300);
	}
	
	@Override
	public void destroyProcess() {
		SpectrogramMarkObservers.removeSpectrogramMarkObserver(this);
		super.destroyProcess();
	}

	public void setupConnections() {
		// Find the existing source data block and remove myself from observing it.
		// Then find the new one and subscribe to that instead. 
		// 2020-01-28 removed - not req'd because setParentDataBlock does this on it's own, and putting it in here causes confusion later on because
		// the correct bookkeeping is not being done.
//		if (getParentDataBlock() != null) 
//			getParentDataBlock().deleteObserver(this);
		if (ishLocControl == null) 
			return;

		IshLocParams p = ishLocControl.ishLocParams;			//shorthand
		PamDataBlock inputDataBlock = getInputDataBlock();		//might be null
		setParentDataBlock(inputDataBlock);		//in case it wasn't parent already
		if (inputDataBlock != null) {

			// get rid of this next part - setParentDataBlock sets up this process as a threaded observer.  The
			// next line sets it up as a direct observer, which looks (to PamProcess) like a diff process and therefore
			// can be added in addition to the prev one.
//			inputDataBlock.addObserver(this);	//should happen in setParentDataBlock, but doesn't always
			prepareMyParams();
			//		outputDataBlock.setChannelMap(p.channelList);
			outputDataBlock.sortOutputMaps(inputDataBlock.getChannelMap(), inputDataBlock.getSequenceMapObject(), p.channelList);
		//setSampleRate(sampleRate, true);	//set rate for outputDataBlock
		}
	}

	//Calculate any subsidiary values needed for processing.  These get recalculated
	//whenever the sample rate changes (via setSampleRate, which is also called after 
	//the params dialog box closes).
	//Note that during initialization, this gets called with params.fftDataSource
	//still null.
	protected void prepareMyParams() {
	}
	
	/** Data for localization can arrive from either an upstream data source or
	 * from the user drawing a box on the spectrogram.  This is the routine for
	 * capturing user box-drawing.
	 * 
	 * @param display		spectrogram display; ignored
	 * @param downUp		mouse action (only MOUSE_UP events are used)
	 * @param channel		which channel was drawn on; ignored
	 * @param startMsec		in absolute msec (since 1970)
	 * @param durationMsec
	 * @param f0,f1			frequency range of the selection
	 */
	@Override
	public boolean spectrogramNotification(SpectrogramDisplay display, MouseEvent mouseEvent, int downUp, 
			int channel, long startMsec, long durationMsec, double f0, double f1, TDGraphFX tdDisplay) 	{		
		
		FFTDataBlock source = null;
		PamRawDataBlock daqBlock = null;
		
		// if this is from a Swing SpectrogramDisplay frame...
		if (display != null) {
			source = display.getSourceFFTDataBlock();
			daqBlock = display.getSourceRawDataBlock();
			
		// if this is from a TD Display frame...
		} else if (tdDisplay != null) {
			source = tdDisplay.getFFTDataBlock();
			if (source==null) return false;
			daqBlock = source.getRawSourceDataBlock2();
			
		// if neither, just exit
		} else {
			return false;
		}
		if (source==null || daqBlock==null) return false;

		if (source.getSequenceMapObject()!=null) {
			String err = "Error: this Spectrogram uses Beamformer data as it's source, and Beamformer output does not contain "
			+ "the link back to a single channel of raw audio data that Ishmael requires.  You will not be able to select detections "
			+ "until the source is changed";
			IshWarning.setWarningMessage(err);
			WarningSystem.getWarningSystem().addWarning(IshWarning);
			return false;
		} else {
			WarningSystem.getWarningSystem().removeWarning(IshWarning);
		}

		if (downUp != SpectrogramMarkObserver.MOUSE_UP)		//react only on mouse-up
			return false;
		
		long startSam, durationSam;
		PamProcess daqProc = daqBlock.getParentProcess();
		//If it crashes on this line, daqProc1 is not an AcquisitionProcess.
//		AcquisitionProcess daqProc = (AcquisitionProcess)daqProc1;
		startSam    = daqProc.absMillisecondsToSamples(startMsec);
		durationSam = daqProc.relMillisecondsToSamples(durationMsec);
		
		doLocalisation(startSam, durationSam, f0, f1, daqBlock);
		return false;
	}
	
	@Override
	public boolean canMark() {
		return (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW);
	}

	/** Data for localization can arrive from either an upstream data source or
	 * from the user drawing a box on the spectrogram.  This is the routine for
	 * data arriving from upstream.
	 * 
	 * @param arg1	data arriving from upstream; type must be PamDataUnit (or
	 * 				a subclass of it)
	 */
	@Override
	public void newData(PamObservable o, PamDataUnit arg1) {  //called from PamProcess
		
		// quick check here - if the PamDataUnit has sequence numbers then we can't use it for localisation.  Warn the user and exit
		if (!ishLocControl.ishLocParams.useDetector || arg1.getSequenceBitmapObject()!=null) {
			String err = "Error: the selected Source Detector uses Beamformer output as a data source, and Beamformer output does not contain "
			+ "the link back to a single channel of raw audio data that is required for analysis.  Please either change the Source "
			+ "Detector's data source, or select a different Detector.";
			IshWarning.setWarningMessage(err);
			WarningSystem.getWarningSystem().addWarning(IshWarning);
			ishLocControl.ishLocParams.useDetector=false;
			return;
		} else {
			WarningSystem.getWarningSystem().removeWarning(IshWarning);
		}
		
		PamDataUnit det = arg1;	// originally this was casting from PamDataUnit to PamDetection
		IshLocParams p = ishLocControl.ishLocParams;			//shorthand
		PamRawDataBlock daqBlock = null;		
		PamProcess daqProc = null;
		IshDetControl ishControl = (IshDetControl) getParentProcess().getPamControlledUnit();
//		String ishRawDataSource = ishControl.getIshDetectionParams().inputDataSource; 
		String ishRawDataSource = ishControl.getIshDetectionParams().groupedSourceParmas.getDataSource();
		PamDataBlock dataBlock = PamController.getInstance().
										getDataBlock(PamDataUnit.class, ishRawDataSource);

		PamDataBlock parentDB = dataBlock;
		while(true) {
			daqProc = parentDB.getParentProcess();
			if (daqProc==null) {
				break;
			}
			if (PamRawDataBlock.class.isAssignableFrom(parentDB.getClass())) {
				daqBlock = (PamRawDataBlock) parentDB;
				daqProc = daqBlock.getParentProcess();
				break;
			}
			PamDataBlock prevDB = daqProc.getParentDataBlock();
			if (prevDB==null) {
				break;
			} else {
				parentDB = prevDB;
			}
			
		}
				
		float sRate = daqProc.getSampleRate();
		//float sRate = ishLocControl.getProcess().getSampleRate();
 
//		long t0Sam = det.getStartSample() - (long)(p.tBefore * sRate);
//		long durSam = det.getDuration() + (long)((p.tBefore + p.tAfter) * sRate);
		long startTime = dataBlock.getParentProcess().absSamplesToMilliseconds(det.getStartSample());
		long t0Sam = daqProc.absMillisecondsToSamples(det.getTimeMilliseconds());
		
		long durSam = det.getSampleDuration();
//		long startSam    = daqProc.absMillisecondsToSamples(det.getTimeMilliseconds());
//		long durationSam = daqProc.relMillisecondsToSamples(det.getDuration());
		
		
		double[] freq = det.getFrequency();
		doLocalisation(t0Sam, durSam, freq[0], freq[1], daqBlock);
	}

	/** Do the localization for the call delineated by startMsec, durationMsec, f0,
	 * and f1.
	 * 
	 * @param startSam		in absolute msec (since 1970)
	 * @param durationSam
	 * @param f0,f1			frequency range to use in calculating the loc
	 * @param daqProcess	the PamProcess producing raw audio data, needed for getting
	 * 						hydrophone info
	 */
	public void doLocalisation(long startSam, long durationSam, double f0, double f1,
			PamRawDataBlock daqBlock)    //AcquisitionProcess daqProcess)
	{	
		//Need the geometry -- the location of the hydrophone elements 
		//relative to the ship.  First find the available hydrophones (i.e., the
		//ones whose signals are captured by software) get the hydrophone list
		//for this digitizing PamProcess --should give actual phone element numbers
		//versus channels.  We will send the hydrophone list to subclass
		//processes: user chooses phones; Pam finds channels.
		//AcquisitionProcess daqProcess = (AcquisitionProcess)outputDataBlock.getSourceProcess();
		PamProcess daqProcess = daqBlock.getParentProcess();
//		PamControlledUnit ctrl = daqProcess.getPamControlledUnit();
		
		//I don't think hydlist is used anymore.  See channelMap below.
//		hydlist = ctrl.getHydrophoneList();
//		int nPhones = ctrl.acquisitionParameters.getNChannels();
//		
//		if (hydlist == null || nPhones < 2) {
//			//For some reason hydlist is bad. Assume that channels are phones 0..n-1.
//			hydlist = new int[ctrl.acquisitionParameters.getNChannels()];
//			for (int i = 0; i < nPhones; i++)
//				hydlist[i] = i;
//		}

		//Get coordinates of hydrophones on this list for the subclass.
		int channelMap = daqBlock.getChannelMap();
		
		//Get the array geometry that was in effect at the time of this selection.
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
//		for (int i = 0; i < nPhones; i++)
//			arraygeom[i] = array.getHydrophone(hydlist[i]).getCoordinate();
		SnapshotGeometry snapshotGeom = ArrayManager.getArrayManager().getSnapshotGeometry(channelMap, daqProcess.absSamplesToMilliseconds(startSam));
		PamVector[] geom = snapshotGeom.getGeometry();
		arraygeom = new double[geom.length][];
		for (int i = 0; i < geom.length; i++) {
			arraygeom[i] = geom[i].getVector();
		}
		
		// if we have failed to get the array geometry, just exit
		if (arraygeom==null) return;

		c = array.getSpeedOfSound();
		
		/**
		 * Doug.
		 * If the locator is downstream of a decimator, this doesn't work since the 
		 * sample rate will be the acquisition sample rate, not the sample rate in the 
		 * incoming data. Or does a decimator output RawDataUnits, and getAncestorDataBlock 
		 * will find it correctly?  I think it should all work OK if sample rate, etc. are 
		 * all just taken from the process just before the FFT. 
		 */
		double[][] selectionSams = null;
		try {
			selectionSams =	daqBlock.getSamples(startSam, (int)durationSam, channelMap);
		}
		catch (RawDataUnavailableException e) {
			System.out.println("RawDataUnavailableException in IshLocProcess:" + e.getMessage());

			// if the raw data has already been discarded, adjust the natural lifetime to try and avoid this in the future
			int newTime;
			int oldTime = daqBlock.getNaturalLifetimeMillis();
			if (oldTime == 0) {
				newTime = 1000; // set the natural lifetime to 1 second
			} else if (oldTime> Integer.MAX_VALUE/2) {
				newTime = Integer.MAX_VALUE;
			} else {
				newTime = oldTime*2;
			}
			/*
			 * Above causes same error as in Rocca and needs fixing
			 */
			newTime = Math.min(newTime, 600000); // don't let this exceed 10 minutes. 
//			System.out.println("Adjusting raw data natural lifetime from " + oldTime + " ms to " + newTime + " ms");
			daqBlock.setNaturalLifetimeMillis(newTime); // increase the lifetime to try and prevent this from happening again
			return;		
		}
		if (selectionSams == null) {
			System.out.println("ishLocProcess: Unable to find source audio data.");
			return;
		}
		//PamProcess rawProcess = getRawDataProcess();
		long startMsec = daqProcess.absSamplesToMilliseconds(startSam); 
		long endMsec   = daqProcess.absSamplesToMilliseconds(startSam + durationSam);
		long midSam = startSam + durationSam / 2;

		//RecycledDataUnit outputUnit = getOutputDataBlock(0).getNewUnit(startSample,
		//		durationInSams, channelMap);
		PamDataUnit outputUnit = outputDataBlock.getRecycledUnit();
		if (outputUnit != null)                 //refurbished outputUnit
			outputUnit.setInfo(startSam, channelMap, startSam, durationSam);
		else {                                  //new outputUnit
			outputUnit = new IshDetection(startMsec, endMsec, (float)f0, (float)f1, 
					midSam, 1.0, outputDataBlock, channelMap, startSam, durationSam); //TODO - need to add ra data. 
		}

		////////////////////////////////// Do it! ////////////////////////////////////////
		//Here's where we call the subclass to run the loc algorithm.
		//The order of entries in arraygeom matches the order in selectionSams.
		//The result is installed in outputUnit.
		try {
			IshLocalisation iLoc = calcData(outputUnit, outputDataBlock.getChannelMap(), // note that we actually need the channel map here, because calcData uses it for the hydrophone locations
					selectionSams, daqProcess.getSampleRate(), f0, f1);
			 outputUnit.setLocalisation(iLoc);
		} catch (noLocationFoundException ex) {
			return;				//error; just ignore the loc
		}
		////////////////////////////////// Done! ////////////////////////////////////////

		//Produce an output PamDataUnit with the result.
		outputDataBlock.addPamData(outputUnit);
	}
	
	@Override
	public String getMarkObserverName() {
		return getProcessName();
	}

	/** calcData, which is declared here but defined only in subclasses,
	 * uses the selectionSams to calculate a location, which is returned.
	 * Note that the class variables arraygeom[][] and c are available
	 * for use by the subclass.
	 */
	abstract IshLocalisation calcData(PamDataUnit outputUnit, int referencePhones,
			double[][] selectionSams, double rawSampleRate, double f0, double f1)
		throws noLocationFoundException;

	@Override
	public void pamStart() {
		outputDataBlock.setNaturalLifetime(300);
	}

	@Override
	public void pamStop() {}

	@Override
	public void prepareProcess() {
		super.prepareProcess();
		
	}
}
