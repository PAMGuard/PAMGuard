package IshmaelDetector;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import Acquisition.FileInputSystem;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamView.GroupedSourceParameters;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import fftManager.FastFFT;

public class MatchFiltProcess2 extends IshDetFnProcess {

	private MatchFiltControl matchFiltControl;
	
	private ChannelDetector[] channelDetectors = new ChannelDetector[PamConstants.MAX_CHANNELS];

	private double[] kernel;

	private int fftLength;

	private FastFFT fastFFT;

	private ComplexArray complexKernel;

	private int usefulSamples;

	private int logFFTLength;

	private double normConst;
	
	public MatchFiltProcess2(MatchFiltControl matchFiltControl, PamDataBlock parentDataBlock) {
		super(matchFiltControl, parentDataBlock);
		this.matchFiltControl = matchFiltControl;
	}
	

	//This is called when the user chooses Start Detection off the menu.

	@Override
	protected void prepareMyParams() {
		MatchFiltParams params = (MatchFiltParams) ishDetControl.ishDetParams;
		channelDetectors = new ChannelDetector[PamConstants.MAX_CHANNELS];
		GroupedSourceParameters groups = params.groupedSourceParmas;
		// want the first channel of each group ...
		int nGroup = groups.countChannelGroups();
		if (nGroup == 0) {
			// just try to do channel 0
			channelDetectors[0] = new ChannelDetector(0);
		}
		else {
			for (int i = 0; i < nGroup; i++) {
				int groupMap = groups.getGroupChannels(i);
				if (groupMap == 0) {
					continue;
				}
				int firstChan = PamUtils.getLowestChannel(groupMap);
				channelDetectors[firstChan] = new ChannelDetector(firstChan);
			}
		}
		
		boolean kernelOK = prepareKernel(params);
		
		
		prepareProcess();
	}
	
	private boolean prepareKernel(MatchFiltParams params) {
		ArrayList<String> kernelList = params.kernelFilenameList;
		if (kernelList.size() == 0) {
			WarnOnce.showWarning("Ish Matched Filter", "No Kernel file specified in configuration", WarnOnce.OK_OPTION);
			return false;
		}
		File kernelFile = new File(kernelList.get(0));
		if (kernelFile.exists() == false) {
			WarnOnce.showWarning("Ish Matched Filter", "Kernel file " + kernelFile.getAbsolutePath() + " doesn't exist", WarnOnce.OK_OPTION);
			return false;
		}
		
		// now get the kernel from the file.
		try {
			byte[] byteArray = new byte[(int)kernelFile.length()];  //longer than needed
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(kernelFile);
			int nBytesRead = audioStream.read(byteArray, 0, byteArray.length);
			AudioFormat audioFormat = audioStream.getFormat();
			long nBytesToDo = 
					nBytesRead - (nBytesRead % audioFormat.getFrameSize());
			kernel = FileInputSystem.bytesToSamples(byteArray, 
					nBytesToDo, 0, audioFormat);
			audioStream.close();
		} catch (Exception ex) {
			kernel = new double[0];
			WarnOnce.showWarning("Ish Matched Filter", "Unable to read Kernel file " + kernelFile.getAbsolutePath(), WarnOnce.OK_OPTION);
			return false;
		}
		if (kernel == null) {
			return false;
		}
		int bufLen = Math.max((int)Math.round(sampleRate * 0.1), kernel.length * 2);
		fftLength = FastFFT.nextBinaryExp(bufLen);
		logFFTLength = FastFFT.log2(fftLength);
		// pack it slightly differently so that the data are at the end. 
		double[] packedKernel = Arrays.copyOf(kernel, fftLength);
//		double[] packedKernel = new double[fftLength];
//		for (int i = 0, j = fftLength-packedKernel.length; i < kernel.length; i++, j++) {
//			packedKernel[j] = kernel[i];
//		}
		
		
		fastFFT = new FastFFT();
		// this will initialise the fastFFT for bufLen, to reuse in subsequent calls
		complexKernel = fastFFT.rfft(packedKernel, fftLength);
//		usefulSamples = fftLength/2;//-kernel.length;
		usefulSamples = fftLength-kernel.length;
		normConst = 0;
		for (int i = 0; i < kernel.length; i++) {
			normConst += Math.pow(kernel[i], 2);
		}

		return true;
	}


	@Override
	public void prepareProcess() {
		super.prepareProcess();
	}

	@Override
	public boolean prepareProcessOK() {
		return true;
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		if (arg instanceof RawDataUnit == false) {
			return;
		}
		int chan = PamUtils.getSingleChannel(arg.getSequenceBitmap());
		if (channelDetectors[chan] == null) {
			return;
		}
		channelDetectors[chan].newData((RawDataUnit) arg);
	}
	
	private class ChannelDetector {
		
		int iChannel;
		
		int bufferIndex = 0;
		
		long totalSamples = 0;
		
		double[] dataBuffer;
		
		private ChannelDetector(int iChan) {
			this.iChannel = iChan;
		}

		public void newData(RawDataUnit rawDataUnit) {
			double[] raw = rawDataUnit.getRawData();
			if (dataBuffer == null) {
				dataBuffer = new double[fftLength];
				bufferIndex = 0;
			}
			for (int i = 0; i < raw.length; i++) {
				dataBuffer[bufferIndex++] = raw[i];
				totalSamples++;
				if (bufferIndex == fftLength) {
					processBuffer();
					shuffleBuffer();
					bufferIndex -= usefulSamples;
				}
			}
		}

		/**
		 * Move data back to start of buffer by useful samples. 
		 */
		private void shuffleBuffer() {
			for (int i = 0, j = usefulSamples; j < fftLength; i++, j++) {
				dataBuffer[i] = dataBuffer[j];
			}
			
		}

		/**
		 * Process a completed data buffer
		 */
		private void processBuffer() {
			// do the xCorr here, it's as easy as in a separate class given the
			// functions in ComplexArray
			ComplexArray fftData = fastFFT.rfft(dataBuffer, fftLength);
			ComplexArray xCorrDat = fftData.conjTimes(complexKernel);
			// because it was a rfft, these data are only half of what we need for the inv FFt.
//			xCorrDat = xCorrDat.fillConjugateHalf();
			double[] xCorr = fastFFT.realInverse(xCorrDat);
			/**
			 * For normalisation, we need to sum the energy in the waveform for each sample of
			 * the correlation. So add the first block, the length of the kernel, then for 
			 * each sample add one in and take one out
			 */
			double norm2 = 0;
			for (int i = 0; i < kernel.length; i++) {
				norm2 += Math.pow(dataBuffer[i],2);
			}

			long startSamp = totalSamples - fftLength;
			long bufferStartMillis = absSamplesToMilliseconds(startSamp);

			double[] dataOut = new double[usefulSamples];
			for (int i = 0, j = kernel.length; i < usefulSamples; i++, j++) {
				dataOut[i] = xCorr[i]/Math.sqrt(normConst*norm2);
				norm2 -= Math.pow(dataBuffer[i],2); // remove first
				norm2 += Math.pow(dataBuffer[j],2); // add next
			}
			// now throw that at a new data unit ...
			IshDetFnDataUnit outData = new IshDetFnDataUnit(bufferStartMillis, 1<<iChannel, startSamp, usefulSamples, dataOut);
			
//			double maxOut = 0;
//			for (int i = 0; i < dataOut.length; i++) {
//				maxOut = Math.max(maxOut, dataOut[i]);
//			}
//			System.out.printf("MAx correlation value is %5.3f\n", maxOut);

			outputDataBlock.addPamData(outData);
		}
		
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

	@Override
	public float getDetSampleRate() {
		return sampleRate;
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
