package fftManager;

import java.io.File;
import java.util.Arrays;

import javax.swing.JOptionPane;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

import wavFiles.WavFile;
import wavFiles.WavFileReader;
import wavFiles.WavHeader;

public class FileFFTTest {

		private String fileName;
		private int fftLength;
		private int fftHop;
		private WavFileReader wavFile;
		private int logFFTLen;
		
		private ChannelProcess[] channelProcesses;
		
		public FileFFTTest(String fileName, int fftLength, int fftHop) {
			this.fileName = fileName;
			this.fftLength = fftLength;
			this.fftHop = fftHop;
			logFFTLen = FastFFT.log2(fftLength);
		}
		
		public void run() {
			File aFile = new File(fileName);
			if (aFile.exists() == false) {
				reportError("Wav file does not exist");
			}
			wavFile = new WavFileReader(fileName);
			WavHeader wavHeader = wavFile.getWavHeader();
			int nChan = wavHeader.getNChannels();
			long dataSize = wavHeader.getDataSize();
			int blockAlign = wavHeader.getBlockAlign();
			long nSamples = dataSize / blockAlign;
			int sampleRate = wavHeader.getSampleRate();
			double fileSecs = (double) nSamples / (double) sampleRate;
			System.out.println(String.format("File %s has %d channels at %d Hz, total %d samples = %3.1f secs",
					aFile.getName(), nChan, sampleRate, nSamples, fileSecs));
			long startTime = System.currentTimeMillis();
			int sampleBlockSize = sampleRate/10;
			int samplesRead;
			int nReads = 0;
			long totalSamples = 0;
			double[][] soundData = new double[nChan][sampleBlockSize];
			channelProcesses = new ChannelProcess[nChan];
			for (int i = 0; i < nChan; i++) {
				channelProcesses[i] = new ChannelProcess(i);
			}
			
			while(true) {
				samplesRead = wavFile.readData(soundData);
				if (samplesRead > 0) {
					nReads++;
					totalSamples += samplesRead;
					for (int i = 0; i < nChan; i++) {
						channelProcesses[i].processData(soundData[i], samplesRead);
					}
				}
				else {
					break;
				}
			}
			long endTime = System.currentTimeMillis();
			System.out.println(String.format("Total samples read = %d in %d chunks", totalSamples, nReads));
			int totalFFTs = 0;
			for (int i = 0; i < nChan; i++) {
				totalFFTs += channelProcesses[i].totalFFTs;
			}
			double runSecs = (double)(endTime-startTime) / 1000;
			System.out.println(String.format("%d FFT's performed in %3.1f seconds = %3.1f * realtime",
					totalFFTs, runSecs, fileSecs/runSecs));
			
			wavFile.close();
		}
		private class ChannelProcess {
			
			private int iChan;

			private DoubleFFT_1D doubleFFT_1D;
			
			private FastFFT fastFFT;
			
			private int totalFFTs = 0;
			private Complex[] complexData;

			public ChannelProcess(int chan) {
				super();
				iChan = chan;
				doubleFFT_1D = new DoubleFFT_1D(fftLength);
				fastFFT = new FastFFT();
			}
			
			double[] dataToFFT;
			
			double[] copyToFFT;
			
			double[] dataCopy;
			
			int dataPointer = 0;
			
			public void processData(double[] soundData, int nSamples) {
				if (dataToFFT == null || dataToFFT.length != fftLength) {
					dataToFFT = new double[fftLength];
				}
				

				for (int i = 0; i < soundData.length; i++) {
					dataToFFT[dataPointer++] = soundData[i];
					if (dataPointer == fftLength) {
						dataCopy = Arrays.copyOf(dataToFFT, fftLength);
						copyToFFT = Arrays.copyOf(dataToFFT, fftLength);
						doubleFFT_1D.realForward(copyToFFT);
						int is = 0;
						complexData = Complex.allocateComplexArray(fftLength/2);
						for (int s = 0; s < fftLength/2; s+=2) {
							complexData[is++].assign(dataToFFT[s], dataToFFT[s+1]);
						}
//						fastFFT.rfft(dataToFFT, null, logFFTLen);
						totalFFTs++;
						// do anything with the FFTd data here.
						// then copy the hopped data back over in place. 
						dataPointer = 0;
						for (int j = fftHop; j < fftLength; j++) {
							dataToFFT[dataPointer++] = dataCopy[j];
						}
					}
				}
			}
			
		}
		
		private boolean reportError(String warningText) {
			JOptionPane.showMessageDialog(null, warningText, "Wav file", JOptionPane.ERROR_MESSAGE);
			return false;
		}
}
