package soundtrap;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import Acquisition.pamAudio.WavFileInputStream;
import PamguardMVC.debug.Debug;
import soundtrap.xml.DWVInfo;
import wavFiles.ByteConverter;

/**
 * Reader for dwv files. These are basically wav files with 
 * the wrong ending and have multiple clicks packed up 
 * in them, each of the same fixed length. 
 * @author Douglas Gillespie. 
 *
 */
public class DWVReader {

	private DWVInfo dwvInfo;
	private File dwvFile;
	private WavFileInputStream ais;
	private long nSamples;
	private long nClicks;
	private long spareSamples;
	
	private ByteConverter byteConverter;

	public DWVReader(File dwvFile, DWVInfo dwvInfo) {
		this.dwvFile = dwvFile;
		this.dwvInfo = dwvInfo;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		closeDWV();
	}

	public boolean openDWV() {
		if (dwvFile == null || dwvFile.exists() == false) {
			return false;
		}
		closeDWV();
		// get the length of the dwv file in samples. 
		AudioFormat audioFormat = null;
		ais = null;
		try {
			ais = WavFileInputStream.openInputStream(dwvFile);
			audioFormat = ais.getFormat();
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
			return false;
		}
		nSamples = ais.getFrameLength();
		nClicks = nSamples / dwvInfo.dwvBlockLen;
		spareSamples = nSamples % dwvInfo.dwvBlockLen;
		if (spareSamples != 0) {
			Debug.out.printf("File %s has %d clicks and %d spare samples\n", dwvFile.getName(), (int) nClicks, (int) spareSamples);
		}
		
		byteConverter = ByteConverter.createByteConverter(audioFormat);
		
		return true;

	}
	
	public void closeDWV() {
		if (ais == null) {
			return;
		}
		try {
			ais.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ais = null;
	}
	
	/**
	 * Read the next click from the dwv file. Assume 16 bit. 
	 * @return scaled double array of audio data. 
	 */
	double[] readNextClick(double[] data) {
		if (ais == null) {
			openDWV();
		}
		if (ais == null) {
			return null;
		}
		if (data == null || data.length != dwvInfo.dwvBlockLen) {
			data = new double[dwvInfo.dwvBlockLen];
		}
		int nBytes = dwvInfo.dwvBlockLen * 2;
		byte[] byteData = new byte[nBytes];
		int bytesRead = 0;
		try {
			bytesRead = ais.read(byteData);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		if (bytesRead != nBytes) {
			return null;
		}
		
		double[][] multiChanData = {data};
		
		byteConverter.bytesToDouble(byteData, multiChanData, nBytes);
		
		return data;
	}
	
	int getNumDWV() {
		return (int) nClicks;
	}

}
