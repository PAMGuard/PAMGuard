package soundtrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import Acquisition.pamAudio.WavFileInputStream;
import soundtrap.xml.CDETInfo;
import soundtrap.xml.DWVInfo;
import soundtrap.xml.WAVInfo;

public class STGroupInfo {

	private File xmlFile, bclFile, dwvFile;

	private STXMLFile xmlFileInfo;

	private DWVInfo dwvInfo;

	private CDETInfo cdetInfo;

	private WAVInfo wavInfo;

	/**
	 * @param xmlFileInfo
	 */
	public STGroupInfo(STXMLFile xmlFileInfo) {
		super();
		this.xmlFileInfo = xmlFileInfo;
		xmlFile = xmlFileInfo.getXmlFile();
		String xmlName = xmlFile.getAbsolutePath();
		bclFile = new File(xmlName.replace(STToolsControl.xmlFileEnd, STToolsControl.bclFileEnd));
		dwvFile = new File(xmlName.replace(STToolsControl.xmlFileEnd, STToolsControl.dwvFileEnd));
		dwvInfo = xmlFileInfo.getDwvInfo();
		cdetInfo = xmlFileInfo.getCdetInfo();
		wavInfo = xmlFileInfo.getWavInfo();
		if (dwvInfo == null || cdetInfo == null) {
			System.out.printf("File %s does not contain any detector output information\n", xmlName);
		}
		else {
			DWVReader dwvReader = new DWVReader(dwvFile, dwvInfo);
			dwvReader.openDWV();
			System.out.printf("File %s has bcl %s and dwv %s files with %d clicks\n", xmlFile.getName(), 
					Boolean.toString(bclFile.exists()), Boolean.toString(dwvFile.exists()), dwvReader.getNumDWV());
//			double[] dwvData = new double[dwvInfo.dwvBlockLen];
//			while (dwvData != null) {
//				dwvData = dwvReader.readNextClick(dwvData);
//			}
		}
	}

	int getDwvSampleRate() {
		if (dwvInfo == null || dwvInfo.getFs() == null) {
			return -1;
		}
		return dwvInfo.getFs();
	}

	long getStartTimeMillis() {
		if (wavInfo != null && wavInfo.getTimeInfo() != null) {
			if (wavInfo.getTimeInfo().samplingStartTimeUTC > 0) {
				return wavInfo.getTimeInfo().samplingStartTimeUTC;
			}
		}
		if (dwvInfo != null && dwvInfo.getTimeInfo() != null) {
			return dwvInfo.getTimeInfo().samplingStopTimeUTC;
		}
		return -1;
	}

	long getStopTimeMillis() {
		if (wavInfo != null && wavInfo.getTimeInfo() != null) {
			if (wavInfo.getTimeInfo().samplingStopTimeUTC > 0) {
				return wavInfo.getTimeInfo().samplingStopTimeUTC;
			}
		}
		if (dwvInfo != null && dwvInfo.getTimeInfo() != null) {
			return dwvInfo.getTimeInfo().samplingStopTimeUTC;
		}
		return -1;
	}

	public boolean hasDWV() {
		return dwvFile.exists();
	}

	/**
	 * @return the xmlFile
	 */
	public File getXmlFile() {
		return xmlFile;
	}

	/**
	 * @return the bclFile
	 */
	public File getBclFile() {
		return bclFile;
	}

	/**
	 * @return the dwvFile
	 */
	public File getDwvFile() {
		return dwvFile;
	}

	/**
	 * @return the xmlFileInfo
	 */
	public STXMLFile getXmlFileInfo() {
		return xmlFileInfo;
	}

	/**
	 * @return the dwvInfo
	 */
	public DWVInfo getDwvInfo() {
		return dwvInfo;
	}

	/**
	 * @return the cdetInfo
	 */
	public CDETInfo getCdetInfo() {
		return cdetInfo;
	}

	/**
	 * @return the wavInfo
	 */
	public WAVInfo getWavInfo() {
		return wavInfo;
	}

	

}
