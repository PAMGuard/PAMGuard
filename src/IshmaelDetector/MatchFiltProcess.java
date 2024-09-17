package IshmaelDetector;


import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import Acquisition.FileInputSystem;
import PamDetection.RawDataUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import fftManager.FFT;


/* @author Dave Mellinger
 */
public class MatchFiltProcess extends IshDetFnProcess
{
	/**
	 * @author Dave Mellinger
	 */
	double kernel[] = null;
	FFT fftMgr = new FFT();					//need one of these to do FFTs
	int bufI = 0;
	double buffer[];
	
	public MatchFiltProcess(MatchFiltControl matchFiltControl, 
			PamDataBlock parentDataBlock) 
	{
		super(matchFiltControl, parentDataBlock);
	}
	
	@Override
	public String getLongName() {
		return "Matched filter detector data";
	}
	
	public String getNumberName() {
		MatchFiltParams p = (MatchFiltParams)ishDetControl.ishDetParams;
		return "Matched filter: " + p.getKernelFilename();
	}

	@Override
	public Class inputDataClass() { 
		return RawDataUnit.class; 
	}

	//Calculate any subsidiary values needed for processing.  These get recalculated
	//whenever the sample rate changes (via setSampleRate, which is also called after 
	//the params dialog box closes).
	//Note that during initialization, this gets called with params.inputDataSource
	//still null.
	static final int CHAN_0 = 0;
    
	/** Return the rate at which detection samples arrive, which for this detector
     * is the audio sample rate.  Abstractly declared in IshDetFnProcess.
     */
	@Override
	public float getDetSampleRate() {
		return sampleRate;
	}

	@Override
	protected void prepareMyParams() {
		MatchFiltParams p = (MatchFiltParams)ishDetControl.ishDetParams;

		//Read the matched filter kernel soundfile, if present, and store in 'kernel'.
		String fn = p.getKernelFilename();
		if (fn == null  || fn.length() == 0) {
			if (kernel == null || kernel.length > 0)
				kernel = new double[0];
		} else {
			try {
				File f = new File(fn);
				byte[] byteArray = new byte[(int)f.length()];  //longer than needed
				AudioInputStream audioStream = AudioSystem.getAudioInputStream(f);
				int nBytesRead = audioStream.read(byteArray, 0, byteArray.length);
				AudioFormat audioFormat = audioStream.getFormat();
				long nBytesToDo = 
					nBytesRead - (nBytesRead % audioFormat.getFrameSize());
				kernel = FileInputSystem.bytesToSamples(byteArray, 
						nBytesToDo, CHAN_0, audioFormat);
				audioStream.close();
			} catch (Exception ex) {
				kernel = new double[0];
				System.out.println("No kernel set for matched filter detector !");
			}
		}
		
		setProcessName("Matched filter: " + p.getKernelFilename());
	}
	
	//This is called when the user chooses Start Detection off the menu.
	@Override
	public void prepareProcess() {
		super.prepareProcess();
		bufI = 0;
		//Calculate the buffer size.  The longer the buffer, the more efficient
		//the FFTs, since we have to repeat the FFT or kernel.length samples each
		//time.  For instance, if buffer is exactly the length of the kernel, then
		//we end up doing one cross-correlation for every input sample (!).
		//But if buffer is too long, then the display gets jumpy.  The compromise
		//is to use the longer of kernel.length*2 (thus repeating roughly 
		//kernel.length samples in each FFT) and 1/10 second.
		int bufLen = Math.max((int)Math.round(sampleRate * 0.1), kernel.length * 2);
		bufLen = FFT.nextBinaryExp(bufLen);
		buffer = new double[bufLen];
	}
		
	/* MatchFiltProcess uses recycled data blocks.
	 */
	@Override
	public void newData(PamObservable o, PamDataUnit arg1) {  //called from PamProcess
		RawDataUnit arg = (RawDataUnit)arg1;
		double[] inputData = arg.getRawData();     //raw data is double[]

		//See if the channel is one we want before doing anything.
//		if ((arg.getChannelBitmap() & ishDetControl.ishDetParams.channelList) == 0)
//		if ((arg.getSequenceBitmap() & ishDetControl.ishDetParams.channelList) == 0)
		if ((arg.getSequenceBitmap() & ishDetControl.ishDetParams.groupedSourceParmas.getChanOrSeqBitmap()) == 0)
			return;
		
		if (kernel == null || kernel.length == 0) {
			return;
		}

		//Get a new or recycled unit.  Sets start sample, duration, channel bitmap from arg.
		IshDetFnDataUnit outputUnit = getOutputDataUnit(arg);

		//The basic idea: Copy as many input samples as possible into a buffer 
		//until the buffer is full or input samples run out.  If the buffer is
		//full, do a cross-correlation of the buffer and the kernel.
		//Extract the useful samples from the result and output them.  Repeat
		//until the input samples are used up.
		int nDone = 0;		//number of samples of inputData we're done with
		while (nDone < inputData.length) {
			int nNow = Math.min(buffer.length - bufI, inputData.length - nDone);
			//Copy nNow samples to buffer[bufI...], incrementing bufI as we go.
			for (int i = 0; i < nNow; i++, bufI++)
				buffer[bufI] = inputData[i];

			//If buffer is full, perform the actual cross-correlation.
			double dum;
			if (bufI == buffer.length) {
				double[] x = fftMgr.crossCorrelation(buffer, 0, buffer.length, 
						                             kernel, 0, kernel.length);

				//Extract the useful values from x into an output buffer.  The
				//non-useful values are those "polluted" by the circular nature
				//of FFT-based cross-correlation.
				double result[] = new double[buffer.length - 2*kernel.length + 1];
				//for (int i = 0, j = kernel.length; i < result.length; i++, j++)
				for (int i = 0, j = kernel.length; i < result.length; i++, j++) {
//					if (j >= x.length) {
//						System.out.println("J = " + j + " exceeds kernel length " + kernel.length);
//						continue;
//					}
//					System.out.println("J = " + j + " of " + kernel.length);
//					dum = x[j];
//					result[i] = dum;
					result[i] = x[j];
				}

				//Append the new data to the end of the data stream.
				outputUnit.detData[0] = result;
				outputDataBlock.addPamData(outputUnit);

				//Copy the samples to re-use down to the low indices of buffer.
				int i = result.length;
				bufI = 0;
				for ( ; i < buffer.length; i++, bufI++)
					buffer[bufI] = buffer[i];
				//bufI ends up with the correct value
			}
			nDone += nNow;
		}
	}
	
	@Override
	public float getHiFreq() {
		return sampleRate / 2;
	}

	@Override
	public float getLoFreq() {
		return 0;
	}

	//Keep the compiler happy -- these are abstract in the superclass.
	@Override public void pamStart() { }
	@Override public void pamStop() { }
}
