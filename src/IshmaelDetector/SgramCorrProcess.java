package IshmaelDetector;


import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;


/** 
 * This implements the spectrogram correlation detector.
 * See Mellinger and Clark, J. Acoust. Soc. Am. 107(6):3518-3529 (2000).
 * 
 * @author Dave Mellinger and Hisham Qayum
 */
public class SgramCorrProcess extends IshDetFnProcess
{
	/* The fundamental operations here are to build a kernel (see makeKernel),
	 * to accumulate incoming data (in storedGram), and to calculate the
	 * dot-product of the kernel and the incoming data (see gramDotProd).
	 *
	 * There is one kernel (and its associated binOffset) that is shared by all 
	 * channels.  Also, the incoming gram height should be the same on all channels.
	 */
	double kernel[][];			    //corr kernel; time is first index, freq second
	int binOffset;					//offset from bottom bin (0 Hz) of kernel in gram
	int savedGramHeight = -1;		//height of incoming gram; -1 forces recalculation
	
	/** Information specific to each channel.
	 */
	public class PerChannelInfo {
		double storedGram[][];		//incoming samples stored here for correlating; a
									//  circular buffer of gram slices
		long nFramesIn;				//number of frames of data we've seen so far
		int sliceIx;				//where in storedGram to put the next slice
		public PerChannelInfo() {
			if (kernel != null)
				storedGram = new double[kernel.length][kernel[0].length];
			nFramesIn = 0;
			sliceIx = 0;
		}
	}
	PerChannelInfo perChannelInfo[] = new PerChannelInfo[PamConstants.MAX_CHANNELS];
	private double minF;
	private double maxF;
	
	public SgramCorrProcess(SgramCorrControl sgramCorrControl, 
			PamDataBlock parentDataBlock) 
	{
		super(sgramCorrControl, parentDataBlock);
	}
	
	@Override
	public String getLongName() {
		return "Spectrogram correlation detector data";
	}
	
	public String getNumberName() {
		SgramCorrParams p = (SgramCorrParams)ishDetControl.ishDetParams;
		return "Spectrogram correlation: " + p.segment.length + " segments";
	}

	@Override
	public Class inputDataClass() { return FFTDataUnit.class; }
	

	@Override
	public float getDetSampleRate() {
		FFTDataBlock fftDataSource = (FFTDataBlock)getInputDataBlock();
		return sampleRate / fftDataSource.getFftHop();
	}

	//Calculate any subsidiary values needed for processing.  These get recalculated
	//whenever the sample rate changes (via setSampleRate, which is also called after 
	//the params dialog box closes).
	//Note that during initialization, this gets called with params.inputDataSource
	//still null.
	@Override
	protected void prepareMyParams() {
		SgramCorrParams p = (SgramCorrParams)ishDetControl.ishDetParams;
		PamDataBlock inputDataBlock = getInputDataBlock();
		/*
		 *  get the unit. first, then check it's null. Due to multithreading it's 
		 *  possible that checking there are units and then asking for one,without
		 *  synchronization will crash if the unit is deleted between those two calls.  
		 */
		FFTDataUnit lastFFTUnit = ((FFTDataUnit)inputDataBlock.getLastUnit());
		if (lastFFTUnit != null) {
			savedGramHeight = lastFFTUnit.getFftData().length();
			/*
			 * fft information is now stored in an FFTDataBLock, so no need to get
			 * back to the process above it. 
			 */
			FFTDataBlock fftDataSource =
				(FFTDataBlock)inputDataBlock;
			double fRate = sampleRate / fftDataSource.getFftHop(); 
			makeKernel(p, sampleRate, fRate, savedGramHeight); //sets kernel, binOffset
			renewPerChannelInfo();
		} else {
			savedGramHeight = -1;	//special case: force recalculation later
		}
		setProcessName("Spectrogram correlation: " + p.segment.length);
	}
	
	/** Create a spectrogram correlation kernel.
	 * 
	 * @param SgramCorrParams p -- defines what the kernel is shaped like
	 * @param double sRate -- sample rate
	 * @param double fRate -- frame rate of the spectrogram (slices/s)
	 * @param int gramHeight -- in cells; equal to FFT size / 2 
	 * @return nothing, but sets SgramCorrProcess.kernel and .binOffset
	 */
	public void makeKernel(SgramCorrParams p, double sRate, double fRate, 
			int gramHeight) 
	{
		//Find duration and freq spans of the segments.
		double minT = Double.POSITIVE_INFINITY, maxT = Double.NEGATIVE_INFINITY;
		minF = minT;
		maxF = maxT;
		for (int i = 0; i < p.segment.length; i++) {
			minT = Math.min(minT, Math.min(p.segment[i][0], p.segment[i][2]));
			maxT = Math.max(maxT, Math.max(p.segment[i][0], p.segment[i][2]));
			minF = Math.min(minF, Math.min(p.segment[i][1], p.segment[i][3]));
			maxF = Math.max(maxF, Math.max(p.segment[i][1], p.segment[i][3]));
		}
		//Figure out what {max,min}{T,F} translate to in kernel pixels.  The
		//number 4 in these equations means that the kernel extends out 4
		//standard deviations from the center.
		double binBW = (sRate/2) / gramHeight;		//width of FFT bin, Hz/bin
		int minBin = (int)Math.floor((minF - p.spread*4) / binBW);
		int maxBin = (int)Math.ceil ((maxF + p.spread*4) / binBW);
		minBin = Math.max(minBin, 0);
		maxBin = Math.min(maxBin, gramHeight/2);
		int nBin = maxBin - minBin + 1;
		int durN = Math.max(1, (int)Math.ceil((maxT - minT) * fRate));

		//Make the new kernel and fill in its values.
		kernel = new double[durN][nBin];
		binOffset = minBin;
		for (int i = 0; i < durN; i++) {
			double t = i / fRate + minT;
			for (int j = 0; j < nBin; j++) {
				double f = (j + binOffset) * binBW; 
				for (int k = 0; k < p.segment.length; k++) {
					double[] seg = p.segment[k];
					if (t >= seg[0] && t <= seg[2]) {
						double axisF = 
							PamUtils.linterp(seg[0], seg[2], seg[1], seg[3], t);
						kernel[i][j] += hat((f - axisF) / p.spread);
					}
				}
			}
		}
	}
		
	@Override
	public float getHiFreq() {
		return (float) maxF;
	}

	@Override
	public float getLoFreq() {
		// TODO Auto-generated method stub
		return (float) minF;
	}

	/** The derivative of the Gaussian function -- i.e., the 'Mexican hat'
	 * function -- with mean 0 and variance 1.
	 */
	public double hat(double x) {
		return (1 - x*x) * Math.exp(-x*x / 2);	//should divide by sqrt(2*pi)?
	}
	
	/* SgramCorrProcess uses recycled data blocks. The length of the data unit should
	 * correspond to the output of the detector function: Just one double.
	 */
	@Override
	public void newData(PamObservable o, PamDataUnit arg1) {  //called from PamProcess
		FFTDataUnit arg = (FFTDataUnit)arg1;
		SgramCorrParams p = (SgramCorrParams)ishDetControl.ishDetParams;
		ComplexArray inputData = arg.getFftData();        //data from FFT is Complex[]

		//See if the channel is one we want before doing anything.
//		if ((arg.getChannelBitmap() & ishDetControl.ishDetParams.channelList) == 0)
//		if ((arg.getSequenceBitmap() & ishDetControl.ishDetParams.channelList) == 0)
		if ((arg.getSequenceBitmap() & ishDetControl.ishDetParams.groupedSourceParmas.getChanOrSeqBitmap()) == 0)
			return;
//		int chanIx = PamUtils.getSingleChannel(arg.getChannelBitmap());
		int chanIx = PamUtils.getSingleChannel(arg.getSequenceBitmap());
		
		//See if loBin and hiBin need recalculating.
		if (inputData.length() != savedGramHeight)	//in theory, should never happen
			prepareMyParams();				//in practice, might happen on 1st unit
		
		IshDetFnDataUnit outputUnit = getOutputDataUnit(arg);
		
		//The actual spectrogram correlation calculation.  First copy sams.
		PerChannelInfo chan = perChannelInfo[chanIx];
		if (chan.storedGram == null) {
			return;
		}
		double[] slice = chan.storedGram[chan.sliceIx];
		for (int bin = 0, j = binOffset; bin < slice.length; bin++, j++) {
			double mag = inputData.magsq(j); 
			slice[bin] = p.useLog ? Math.log10(Math.max(1.0, mag)) : mag;
		}
		if (++chan.sliceIx >= chan.storedGram.length)     //circular buffer
			chan.sliceIx = 0;
		
		if (++chan.nFramesIn >= kernel.length) {
			double result = gramDotProd(kernel, chan.storedGram, chan.sliceIx);
			
			//Set up structure for depositing the result.  It's a vector of length 1.
			outputUnit.detData = new double[1][1];
			outputUnit.detData[0][0] = result;
			outputUnit.setChannelBitmap(arg.getChannelBitmap());
			outputUnit.setSequenceBitmap(arg.getSequenceBitmapObject());
			//Append the new data to the end of the data stream.
			outputDataBlock.addPamData(outputUnit);
		}
	}

	public double gramDotProd(double[][] ker, double[][] gram, int startI) {
		double sum = 0.0;
		int kerI = 0;
		for (int gramI = startI; gramI < gram.length; gramI++, kerI++)
			for (int j = 0; j < ker[0].length; j++)
				sum += ker[kerI][j] * gram[gramI][j];
		for (int gramI = 0; gramI < startI; gramI++, kerI++)
			for (int j = 0; j < ker[0].length; j++)
				sum += ker[kerI][j] * gram[gramI][j];
		return sum;
	}
	
	public void renewPerChannelInfo() {
		//Populate array of per-channel information.
		if (perChannelInfo == null) {
			return;
		}
		for (int i = 0; i < perChannelInfo.length; i++) {
			perChannelInfo[i] = new PerChannelInfo();
		}
	}
	
	@Override public void pamStart() {
		renewPerChannelInfo();
	}
}
