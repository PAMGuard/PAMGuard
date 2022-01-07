package Acquisition.rona;

import java.io.FileInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat.Encoding;

//import org.kc7bfi.jflac.PCMProcessor;
//import org.kc7bfi.jflac.metadata.StreamInfo;
//import org.kc7bfi.jflac.util.ByteData;
import org.jflac.PCMProcessor;
import org.jflac.metadata.StreamInfo;
import org.jflac.util.ByteData;

import PamDetection.RawDataUnit;
import wavFiles.ByteConverter;

public class RonaFlacProcessor implements PCMProcessor {
	
	private ByteConverter byteConverter;
	private FileInputStream fileStream;
	private int frameSize;
	private long totalSamples;
	private int channelOffset;
	private RonaGatherer ronaGatherer;
	private long fileStartTime;
	private float sampleRate;
	private long startTime, endTime;
	
	public RonaFlacProcessor(FileInputStream fileStream, long fileStartTime, float sampleRate, int channelIndex, RonaGatherer ronaGatherer) {
		this.fileStream = fileStream;
		this.fileStartTime = fileStartTime;
		this.sampleRate = sampleRate;
		this.channelOffset = channelIndex;
		this.ronaGatherer = ronaGatherer;
		byteConverter = ByteConverter.createByteConverter(2, false, Encoding.PCM_SIGNED);
		frameSize = 2;
	}
	
	public void setStartAndEnd(long startTime, long endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@Override
	public void processPCM(ByteData byteData) {
		if (ronaGatherer.shouldCancel()) {
			try {
				fileStream.close(); // will make the flac reader bomb out !
			}
			catch (IOException e) {
			}
			return;
		}

		int newSamples = byteData.getLen() / frameSize;
		long ms = fileStartTime + (long) (totalSamples * 1000L / sampleRate);
		long endMs = ms + (long) (newSamples * 1000L / sampleRate);
		if (endMs < startTime) {
			totalSamples += newSamples;
			return;
		}
		if (endTime != 0 && ms > endTime) {
			try {
				fileStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		double[][] doubleData = new double[1][newSamples];
		byteConverter.bytesToDouble(byteData.getData(), doubleData, byteData.getLen());
		
		RawDataUnit newDataUnit = new RawDataUnit(ms, 1<<channelOffset, totalSamples, newSamples);
		newDataUnit.setRawData(doubleData[0]);
		ronaGatherer.addRawData(newDataUnit, channelOffset);
		
		totalSamples += newSamples;
		
		while (ronaGatherer.shouldCancel() == false && ronaGatherer.waitingDataUnits(channelOffset)) {
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
//				e.printStackTrace();
			}
		}
	}

	@Override
	public void processStreamInfo(StreamInfo streamInfo) {
		sampleRate = streamInfo.getAudioFormat().getSampleRate();
	}

}
