package IshmaelDetector;


import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import IshmaelDetector.IshDetFnDataUnit;
import PamUtils.complex.ComplexArray;

/* 
 * Process for the energy sum detector. 
 * 
 * @author Dave Mellinger and Hisham Qayum.
 * @author Jamie Macaulay (Heavily modified 2018/2019)
 */
@SuppressWarnings("rawtypes") 
public class EnergySumProcess extends IshDetFnProcess {
	
	private int savedGramHeight = -1;		//-1 forces recalculation
	
	private int loBin, hiBin;	//bin index to start/stop summing

	private boolean useDB; // the dB bin
	
	private int loBinRatio, hiBinRatio; //the hi and lo ratio bin

	/**
	 * Keeps a track of the adaptive noise floor
	 */
	private double noisefloor = -Double.MIN_VALUE;
	
	/**
	 * Keeps track of the last result for output smoothing. 
	 */
	private double smoothResult = -Double.MIN_VALUE; 
	
	public EnergySumProcess(EnergySumControl energySumControl, 
			PamDataBlock parentDataBlock) 
	{
		super(energySumControl, parentDataBlock);
	}
	
	@Override
	public String getLongName() {
		return "Energy sum detector data";
	}
	
	public String getNumberName() {
		EnergySumParams p = (EnergySumParams)ishDetControl.ishDetParams;
		return "Energy Sum: " + p.f0 + " to " + p.f1 + " Hz";
	}

    /** Return the rate at which detection samples arrive, which for this detector
     * is the FFT frame rate.  Abstractly declared in IshDetFnProcess.
     */
	@Override
	public float getDetSampleRate() {
		FFTDataBlock fftDataSource = (FFTDataBlock)getInputDataBlock();
		return sampleRate / fftDataSource.getFftHop();
	}
	
	@Override
	public Class inputDataClass() { return FFTDataUnit.class; }
	
	/*
	 * Calculate any subsidiary values needed for processing. These get
	 * recalculated(non-Javadoc) whenever the sample rate changes (via
	 * setSampleRate, which is also called after whenever the sample rate changes
	 * (via setSampleRate, which is also called after the params dialog box closes).
	 * Note that during initialization, this gets called with params.inputDataSource
	 * still null
	 * 
	 * @see IshmaelDetector.IshDetFnProcess#prepareMyParams()
	 */
	@Override
	protected void prepareMyParams() {
		EnergySumParams p = (EnergySumParams)ishDetControl.ishDetParams;
		PamDataBlock inputDataBlock = getInputDataBlock();		//might be null
		
		if (inputDataBlock != null && inputDataBlock.getUnitsCount() > 0) {
			savedGramHeight = ((FFTDataUnit)inputDataBlock.getLastUnit()).getFftData().length();	
			int len = savedGramHeight;
			//Should be max(1,...) here, but FFT bin 0 has 0's in it.
			loBin = Math.max(1,     (int) Math.floor(len * p.f0 / (sampleRate/2)));
			hiBin = Math.min(len-1, (int) Math.ceil (len * p.f1 / (sampleRate/2)));
			
			loBinRatio = Math.max(1,     (int) Math.floor(len * p.ratiof0 / (sampleRate/2)));
			hiBinRatio = Math.min(len-1, (int) Math.ceil (len * p.ratiof1 / (sampleRate/2)));
			
		} else {
			savedGramHeight = -1;	//special case: force recalculation later
		}
		useDB = p.useLog;
		setProcessName("Energy sum: " + p.f0 + " to " + p.f1);
	}
	
	/**
	 * Get the energy sum params. 
	 * @return the energy sum params
	 */
	private EnergySumParams getEnergySumParams() {
		return (EnergySumParams) ishDetControl.ishDetParams;
	}
	
	@Override
	public float getHiFreq() {
		return (float) getEnergySumParams().f1;
	}

	@Override
	public float getLoFreq() {
		return (float) getEnergySumParams().f0;
	}

	
	/* 
	 * EnergySumProcess uses recycled data blocks. The length of the data unit should
	 * correspond to the output of the detector function: Just one double.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void newData(PamObservable o, PamDataUnit fftUnit) {  //called from PamProcess
		
		FFTDataUnit fftDataUnit = (FFTDataUnit) fftUnit;
		ComplexArray inputData = fftDataUnit.getFftData();     //data from FFT is Complex[]

		//See if the channel is one we want before doing anything.
//		if ((fftDataUnit.getChannelBitmap() & ishDetControl.ishDetParams.channelList) == 0)
		if ((fftDataUnit.getSequenceBitmap() & ishDetControl.ishDetParams.groupedSourceParmas.getChanOrSeqBitmap()) == 0)
			return;
		
		//See if loBin and hiBin need recalculating.
		if (inputData.length() != savedGramHeight) {	//in theory, should never happen
			prepareMyParams();				//in practice, might happen on 1st unit
		}
		
		IshDetFnDataUnit outputUnit = getOutputDataUnit(fftDataUnit);  //get a fresh unit

		//The actual sum calculation.
		double result = calcEnergyPeak(inputData,  loBin,  hiBin);
		
		//System.out.println("Energysum: " + fftDataUnit.getSequenceBitmap() +   "  energy: " + result + "  " + inputData.length() );
		if (getEnergySumParams().useRatio) {
			double resultRatio = calcEnergyPeak(inputData,  loBinRatio,  hiBinRatio);
			//if dB then look at threshold dB over other ratio band. '
			if (useDB) result=result-resultRatio; 
			//if linear then use ratio. 
			else result=result/resultRatio;
		}
		
		//result smoothing. 
		if (getEnergySumParams().outPutSmoothing) {
			if (smoothResult==-Double.MIN_VALUE) smoothResult = result; 
			else result = getEnergySumParams().shortFilter*result + (1-getEnergySumParams().shortFilter)*smoothResult; 
		}
				
		//if using an adaptive noise floor calculate the noise floor and see if the current noise is above that. 
		if (getEnergySumParams().adaptiveThreshold) {
			
			//System.out.println("Result dB: " + result);
			if (noisefloor==-Double.MIN_VALUE) noisefloor = result; 
			else noisefloor = getEnergySumParams().longFilter*result + (1-getEnergySumParams().longFilter)*noisefloor; 
			
			//if there is a very loud sound then the noise will take a long time to hit normal again
			if (noisefloor>getEnergySumParams().spikeDecay*result) noisefloor=0.5*noisefloor; //exponential decay 
			
			//Set up structure for depositing the result -- a vector of length 2 to also include noise floor data. 
			//and append the new data to the end of the data stream.
			outputUnit.detData = new double[3][1];
			outputUnit.detData[1][0]= noisefloor; //the smoothed average of detection data (add static threshold for absolute threshold)
			outputUnit.detData[2][0]= result; //the raw result
			
			//now the result is just the result minus the noise floor <- keeps all the Ishmael calculations in line with current code structure. 
			result = result - noisefloor; //the result minus noise floor i..e the difference - this allows Ishmael downstream modules to use the same code structure. 
		}
		else {
			//Set up structure for depositing the result -- a vector of length 1 --
			//and append the new data to the end of the data stream.
			outputUnit.detData = new double[1][1];
		}
				
		outputUnit.detData[0][0] = result;
		
//		System.out.println("Output data unit: " + outputUnit.getTimeMilliseconds());
//		System.out.println("OutputDataBlock_ID: " +outputDataBlock.hashCode() + " " + outputDataBlock.getPamObserver(0).hashCode() + 
//				"  " + outputDataBlock.getPamObserver(1).hashCode() +"  " + outputDataBlock.countObservers() + "  " +  outputDataBlock.getPamObserver(0).getObserverName());
		outputDataBlock.addPamData(outputUnit);
	}
	
	
	/**
	 * Do the actual energy sum calculation. 
	 * @param inputData - the input data. 
	 * @param lowBin - the low bin in bins
	 * @param highBin - the high bin in bins
	 * @return the energy sum value. 
	 */
	private double calcEnergyPeak(ComplexArray inputData, int lowBin, int highBin) {
		
		//The actual sum calculation.
		double sum = 0.0;
		if (useDB) {		//keep this test outside the loop for speed
			for (int i = lowBin; i <= highBin; i++) {
				double mag = inputData.magsq(i);
				sum += Math.log10(Math.max(mag, 1.0e-9)) + 5;   //apply floor- bit of a hack adding 100
			} 
		} 
		else {
			for (int i = lowBin; i <= highBin; i++)
				sum += inputData.magsq(i);
		}
		
		return sum / (highBin - lowBin + 1);
	} 
}
