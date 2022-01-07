package matchedTemplateClassifer;

import java.util.ArrayList;
import java.util.Arrays;

import PamController.PamController;
import PamUtils.complex.ComplexArray;
import PamView.symbol.PamSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamInstantProcess;
import PamguardMVC.PamObservable;
import PamguardMVC.RawDataHolder;
import Spectrogram.WindowFunction;
import clickDetector.ClickLength;
import clickDetector.ClickClassifiers.basicSweep.SweepClassifierSet;
import fftManager.FastFFT;
import matchedTemplateClassifer.annotation.MatchedClickAnnotation;
import matchedTemplateClassifer.annotation.MatchedClickAnnotationType;
import matchedTemplateClassifer.bespokeClassification.BeskopeClassifierManager;

/**
 * Calculates a correlation difference coefficient between a match and reject click template 
 * and classifies a data unit based on a threshold value for the coefficient. Input data units
 * must implement RawDataHolder, i.e. must have a raw waveform available. 
 * <p>
 * The MTClassifier annotates data units with results from the matched template. It also 
 * adds data unit specific flags e.g. species flags to click detections. 
 *
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public class MTProcess extends PamInstantProcess {

	/**
	 * Rference ot the controlled unit. 
	 */
	private MTClassifierControl mtControl;

	/**
	 * Reference to the click data block 
	 */
	private PamDataBlock sourceDataBlock;

	/**
	 * The channel map
	 */
	int channelMap;

	/**
	 * Annotation type for click classification 
	 */
	private MatchedClickAnnotationType mtAnnotationType;

	//for picking click peaks 
	private double[] window;
	
//	/**
//	 * FFT calculation
//	 */
//	private FastFFT fastFFT = new FastFFT();

	/**
	 * Click length calculation
	 */
	private ClickLength clickLength = new ClickLength(); 

	/**
	 * The length data of the last click in channels and start sample, end sample. 
	 */
	private int[][] lengthData;

	private BeskopeClassifierManager bespokeClassifierManager; 



	/**
	 * The MTProcess 
	 * @param pamControlledUnit - the pam controlled unit. 
	 */
	public MTProcess(MTClassifierControl pamControlledUnit) {
		super(pamControlledUnit);
		this.mtControl=pamControlledUnit; 
		this.bespokeClassifierManager = new BeskopeClassifierManager(this);
		this.mtAnnotationType= new MatchedClickAnnotationType(pamControlledUnit);
		
	}


	@Override
	public void setupProcess() {
		super.setupProcess();
		sourceDataBlock = PamController.getInstance().getDataBlock(PamDataUnit.class, 
				getMTParams().dataSourceName);
		if (sourceDataBlock!=null) System.out.println("Matched template process: Source data" +   sourceDataBlock.getDataName());
		else System.out.println("The source data is null: " + getMTParams().dataSourceName); 
		
		setParentDataBlock(sourceDataBlock);
		
		if (sourceDataBlock != null) {
			channelMap = getParentDataBlock().getChannelMap();
			//must check that the annotation has been added to the parent data block. Binary annotation is automatic. 
			//note the addOnFunction deals with existing add ons. 
			sourceDataBlock.getLogging().addAddOn(mtAnnotationType.getSQLLoggingAddon());	
			
			// sort out symbol modifier
			sourceDataBlock.addDataAnnotationType(mtAnnotationType);
			PamSymbolManager symbolManager = sourceDataBlock.getPamSymbolManager();
			if (symbolManager != null) {
				PamSymbolManager.globalClear();
			}
		}
		

		//also need to make sure that the click classifier has been set up.
		//This is a bit of hack for now. 
		mtControl.setupClickClassifier(); 

		prepareProcess();
	}

	/**
	 * Get the click settings. 
	 * @return the click settings. 
	 */
	private MatchedTemplateParams getMTParams() {
		return mtControl.getMTParams();
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
			if (o == sourceDataBlock) {
				//must be an instance of raw data holder. 
				newClickData(arg);
			}
	}
	

	/**
	 * A new click has arrived . It needs to be classified
	 * @param clickDetection - the incoming click.
	 */
	public void newClickData(PamDataUnit clickDetection) {
		
		//System.out.println("clickDetection: " + clickDetection.getUID());

		boolean[] channelClassify;
		boolean classify = false;
		int classifyCount = 0;

		//this is the master result which holds all results from the various templates
		MatchedTemplateResult[] matchedClickResult= new  MatchedTemplateResult[mtControl.getMTParams().classifiers.size()]; 

//		//generate length data if needed
//		this.mtControl.getMTParams().peakSearch=true; 
//		this.mtControl.getMTParams().peakSmoothing=5;
//		this.mtControl.getMTParams().lengthdB=6;

		if (this.mtControl.getMTParams().peakSearch) {
			calcLengthData(clickDetection);
			//					//Print out the length data
			//					System.out.println("Generate length data: ");
			//					for (int i=0; i<this.lengthData.length; i++) {
			//						System.out.println(" Length: " +  i + " ");
			//						for (int j=0; j<this.lengthData[i].length; j++) {
			//							System.out.print (" " + lengthData[i][j]);
			//						}
			//						System.out.println("");
			//					}
		}
		
		//Get the raw wave data from the click. 
		double[][] waveData = ((RawDataHolder) clickDetection).getWaveData();
		
		double[] waveDataChan;
		ArrayList<MatchedTemplateResult> aResult; 

		for (int i=0; i<waveData.length; i++) {

			waveDataChan = getWaveData(((RawDataHolder) clickDetection),  i);

			aResult= classifyDetection(waveDataChan, clickDetection.getParentDataBlock().getSampleRate());

			//reset the classification array. 
			channelClassify = new boolean[mtControl.getMTParams().classifiers.size()];
			
			//now for each template combination we find the results which has the highest threshold 
			for (int j=0; j<aResult.size(); j++) {
				if (matchedClickResult[j]==null) matchedClickResult[j]=aResult.get(j); 

				//set max threshold as the threshold value. 
				if (aResult.get(j).threshold>matchedClickResult[j].threshold) {
					matchedClickResult[j]=aResult.get(j); 
				}
	
				//check whether classification has occurred. 
				channelClassify[j]=isClassified(aResult.get(j), j); 
			}
			
			//now check whether classification on one channel is OK and if not record the classification result 
			//for multi-channel classification. 
			if (this.getMTParams().channelClassification==SweepClassifierSet.CHANNELS_REQUIRE_ONE
					&& containsATrue(channelClassify)) {
				classify=true;
				break;
			}
			else if (containsATrue(channelClassify)) {
				classifyCount++;
			}
			//					System.out.println(String.format("The waveform value is %.2f thresh: %.3f ",
			//							classifyCorr, pamControlledUnit.getMTParams().classifiers.get(0).thresholdToAccept)
			//							+ " classify: " +classify + " channel classification: " + getMTParams().channelClassification);
		}

		//if requires all channels check all channels were classified. 
		if (classifyCount==waveData.length 
				&& this.getMTParams().channelClassification==SweepClassifierSet.CHANNELS_REQUIRE_ALL) {
			classify=true;
		}
		

		//set bespoke data unit specific classification flags. 
		bespokeClassifierManager.bespokeDataUnitFlags(clickDetection,  classify);
	
		//System.out.println("Add annotation: "); 
		// add the matched click classifier annotation. 
		clickDetection.addDataAnnotation(new MatchedClickAnnotation(mtAnnotationType, Arrays.asList(matchedClickResult)));
		
		
	}
	

	/**
	 * Check whether a boolean array contains at least one true
	 * @return true if the contains at least one true. 
	 */
	private boolean containsATrue(boolean[] array) {
		for (boolean value:array) {
			if (value) return true; 
		}
		return false;
	}

	/**
	 * Creates a 2D array of length data[channels][start/end]
	 * <p>
	 * Better to call getLengthData which will only call this if it
	 * really has to. 
	 * @param click click
	 * @param scs classifier settings
	 */
	private synchronized void calcLengthData(PamDataUnit click) {

		int[][] tempLengthData;
//		if (click instanceof ClickDetection) {
//			ClickDetection clickDet = (ClickDetection) click; 
//			//the click specific method is much faster
//			//bit messy here using static function from sweep classifier. This should be in some sort of click utils class. 
//			tempLengthData = SweepClassifierWorker.createLengthData(clickDet,  clickDet.getNChan(), this.mtControl.getMTParams().lengthdB, 
//					this.mtControl.getMTParams().peakSmoothing);
//		}
//		else {
			//generic calculation of length for a RawDataHolder
			tempLengthData=clickLength.lengthData(click, this.mtControl.getMTParams().lengthdB, 
					this.mtControl.getMTParams().peakSmoothing); 
//		}
		
//		System.out.println("----------------------");
//		System.out.println("LengthData1");
//		PamArrayUtils.printArray(tempLengthData);
//		System.out.println("LengthData2");
//		PamArrayUtils.printArray(tempLengthData2);
//		System.out.println("----------------------");

		lengthData = tempLengthData;
	}

	/**
	 * Get wave data for a click- handles sampling around peaks to get rid of white noise. 
	 * @param clickDetection - the click detection 
	 * @param i - the index of the wave data 
	 * @return the wave data 
	 */
	@SuppressWarnings("unused")
	private double[] getWaveData(RawDataHolder clickDetection, int i) {
		double[] waveform; 
		if (this.getMTParams().peakSearch) {
			waveform = createRestrictedLenghtWave(clickDetection,  i, lengthData[i],
					this.getMTParams().restrictedBins); 
//			System.out.println("LEN RESTRICTED WAVEFORM");
//			PamArrayUtils.printArrayRaw(waveform);
		}
		else {
			waveform=clickDetection.getWaveData()[i]; 
//			System.out.println("NORMAL WAVEFORM");
//			PamArrayUtils.printArrayRaw(waveform);
		}
		
//		PamArrayUtils.printArray(clickWaveform);
//		double[] clickWaveformNorm = null; 
//		
//		System.out.println("Normalisation type: " + mtControl.getMTParams().normalisationType + " " + 	this.getMTParams().restrictedBins); 
//		
//		switch(mtControl.getMTParams().normalisationType) {
//		case MatchedTemplateParams.NORMALIZATION_NONE:
//			clickWaveformNorm = waveform;
//			break; 
//		case MatchedTemplateParams.NORMALIZATION_PEAK:
//			clickWaveformNorm =PamUtils.PamArrayUtils.divide(waveform, PamUtils.PamArrayUtils.max(waveform));
//			break;
//		case MatchedTemplateParams.NORMALIZATION_RMS:
//			clickWaveformNorm =PamUtils.PamArrayUtils.normalise(waveform);
//			break;
//		}
		
		double[] clickWaveformNorm  = MTClassifier.normaliseWaveform(waveform, 
				mtControl.getMTParams().normalisationType);
				
		return clickWaveformNorm;
	}

	/**
	 * Create restricted waveform. 
	 * @param click - the input data unit
	 * @param chan - the channel to use 
	 * @param lengthPoints - the restricted start/end points. 
	 * @param restrictedBins - the number of bins to restrict the waveform to.
	 * @param win - the window to use e.g. WindowFunction.hann(restrictedBins)
	 * @return the restricted waveform.
	 */
	public static double[] createRestrictedLenghtWave(RawDataHolder click, int chan, int[] lengthPoints,
			int restrictedBins, double[] win) {
		
		double[] waveData = click.getWaveData()[chan]; 

		return createRestrictedLenghtWave( waveData, lengthPoints,
				restrictedBins, win); 
	}
	
	/**
	 * Create restricted waveform. 
	 * @param waveData - the wave data to process
	 * @param lengthPoints - the restricted start/end points. 
	 * @param restrictedBins - the number of bins to restrict the waveform to.
	 * @param win - the window to use e.g. WindowFunction.hann(restrictedBins)
	 * @return the restricted waveform.
	 */
	public static double[] createRestrictedLenghtWave(double[] waveData, int[] lengthPoints,
			int restrictedBins, double[] win) {
		
		int startBin = (lengthPoints[0] + lengthPoints[1] - restrictedBins)/2;
		startBin = Math.max(0, startBin);
		int endBin = startBin + restrictedBins;
		endBin = Math.min(endBin, waveData.length);
		waveData = Arrays.copyOfRange(waveData, startBin, endBin);
		if (waveData.length < restrictedBins) {
			waveData = Arrays.copyOf(waveData, restrictedBins);
		}
		//double[] win = getWindow(restrictedBins);
		for (int i = 0; i < restrictedBins; i++) {
			waveData[i]*=win[i];
		}
		return waveData; 
	}
	
	/**
	 * Create restricted waveform. 
	 * @param click - the input data unit
	 * @param chan - the channel to use 
	 * @param lengthPoints - the restricted start/end points. 
	 * @param scs - the restricted bins
	 * @return the restricted waveform.
	 */
	private double[] createRestrictedLenghtWave(RawDataHolder click, int chan, int[] lengthPoints,
			int restrictedBins) {
		return createRestrictedLenghtWave(click, chan, lengthPoints, restrictedBins,  getWindow(restrictedBins)); 
	}


	private double[] getWindow(int len) {
		if (window == null ||window.length != len) {
			window = WindowFunction.hann(len);
		}
		return window;
	}


	/**
	 * Performs the binary classification on a result form the matched template classifier.
	 * @param aResult - the result from the MTClassifer. 
	 * @param i - the classifier index.
	 * @return true if classified and false if not. 
	 */
	private boolean isClassified(MatchedTemplateResult aResult, int i) {
		return aResult.threshold>this.mtControl.getMTParams().classifiers.get(i).thresholdToAccept; 
	}

	//the difference in correlation vales between the reject and match templates. 


	ComplexArray clickFFT; 
	/**
	 * Classify a click. 
	 * @param clickWaveform  - the waveform to correlate with reject and match templates. 
	 * @return an array containing results from all match template classifiers 
	 */
	private ArrayList<MatchedTemplateResult> classifyDetection(double[] clickWaveform, float sR) {

		ArrayList<MatchedTemplateResult> results = new ArrayList<MatchedTemplateResult>();
		
		//System.out.println("Click waveform max: " + PamArrayUtils.max(clickWaveform) + " sample rate: " + sR); 
		
		//normalisation and picking peak has already been performed

		//multiple templates means that we have multiple results. 
		MTClassifier classifier;
		ComplexArray matchClick;
		MatchedTemplateResult matchResult;
		FastFFT fft;
		//int fftSize;
		for (int i=0; i<mtControl.getMTParams().classifiers.size(); i++) {

			classifier = mtControl.getMTParams().classifiers.get(i);
			//		int fftLength = classifier.getFFTLength(sR);
			//if the template's FFT size is smaller then might only be grabbing a non peak section 
			//of the click 
			//		if (fftLength<clickWaveform.length) {
			//			clickWaveform=MTClassifier.conditionClickLength(clickWaveform, fftLength, pamControlledUnit.getMTParams().peakSmoothing);
			//		}

			//calculate the click FFT. 
			fft = new FastFFT(); 
			//int fftSize = FastFFT.nextBinaryExp(testWaveform.length/2);

			//fftSize = classifier.getFFTLength(sR); 
			matchClick = fft.rfft(clickWaveform, clickWaveform.length);
			
			//System.out.println("clickWaveform: " +clickWaveform.length + " " + matchClick.length()); 

			//calculate the correlation coefficient.
			matchResult = classifier.calcCorrelationMatch(matchClick, sR);

			results.add(matchResult); 
		}

		return results; 
		//		//TESTING ONLY. Randomly classify 50% of clicks. 
		//		if (Math.random()<0.5) {
		//			return 0; 
		//		}
		//		else {
		//			return 101; 
		//		}
	}



	@Override
	public void pamStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}


	/**
	 * Get the Matched Click Classifier Controller for this process. 
	 * @return the matched click classifier controller. 
	 */
	public MTClassifierControl getMTControl() {
		return this.mtControl;
	}
	

	/**
	 * Get the manager for bespoke data units. This handles adding custom flags to different ypes of data units. 
	 * @return the bespoke classifier manager
	 */
	public BeskopeClassifierManager getBespokeClassifierManager() {
		return bespokeClassifierManager;
	}

//	/**
//	 * Get the parent click data block.
//	 * @return the click data block which is the parent of the process. 
//	 */
//	public ClickDataBlock getClickDataBlock() {
//		return (ClickDataBlock) this.sourceDataBlock;
//	}

}
