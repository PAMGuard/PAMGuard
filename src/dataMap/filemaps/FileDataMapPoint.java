package dataMap.filemaps;

import java.io.File;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import dataGram.Datagram;
import dataGram.DatagramPoint;
import dataMap.OfflineDataMapPoint;

/**
 * This was WavFileDataMapPoint, but can be used for any file types with OfflineFileServer. Won't rename
 * or move though since it's often serialised so leave alone. 
 * @author dg50
 *
 */
public class FileDataMapPoint extends OfflineDataMapPoint implements ManagedParameters, DatagramPoint {
	
	private static final long serialVersionUID = 4955333805088379820L;
	
	private File soundFile;
	
	private FileSubSection fileSubSection;

	/**
	 * Waveform summary of this sound file (rms and peak), used by the data map. Created
	 * on demand by SoundFileDatagramManager and saved along with the rest of the map, so
	 * it only has to be calculated once. Null until it's been made.
	 */
	private Datagram datagram;

	/**
	 * Long term spectral average of this sound file, calculated in the same pass as the
	 * waveform summary so that the user can flip between the two. Null until it's been
	 * made.
	 */
	private Datagram ltsaDatagram;


	public FileDataMapPoint(File soundFile, long startTime, long endTime) {
		super(startTime, endTime, (int) Math.max(((endTime-startTime)/1000),1), 0);
		this.soundFile = soundFile;
	}

	@Override
	public String getName() {
		return soundFile.getName();
	}

	/**
	 * @return the soundFile
	 */
	public File getSoundFile() {
		return soundFile;
	}

	/* (non-Javadoc)
	 * @see dataMap.OfflineDataMapPoint#setEndTime(long)
	 */
	@Override
	public void setEndTime(long endTime) {
		super.setEndTime(endTime);
	}

	/**
	 * @param soundFile the soundFile to set
	 */
	public void setSoundFile(File soundFile) {
		this.soundFile = soundFile;
	}

	/**
	 * The DatagramPoint interface only allows for a single datagram, so this returns the
	 * waveform one. Code which needs to pick between the two goes through
	 * {@link SoundFileDatagramManager#getDatagram}.
	 */
	@Override
	public Datagram getDatagram() {
		return datagram;
	}

	@Override
	public void setDatagram(Datagram datagram) {
		this.datagram = datagram;
	}

	/**
	 * @return the waveform (rms and peak) datagram, or null if it hasn't been made.
	 */
	public Datagram getWaveformDatagram() {
		return datagram;
	}

	/**
	 * @param datagram the waveform (rms and peak) datagram.
	 */
	public void setWaveformDatagram(Datagram datagram) {
		this.datagram = datagram;
	}

	/**
	 * @return the long term spectral average datagram, or null if it hasn't been made.
	 */
	public Datagram getLtsaDatagram() {
		return ltsaDatagram;
	}

	/**
	 * @param ltsaDatagram the long term spectral average datagram.
	 */
	public void setLtsaDatagram(Datagram ltsaDatagram) {
		this.ltsaDatagram = ltsaDatagram;
	}

	@Override
	public Long getLowestUID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLowestUID(Long uid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Long getHighestUID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHighestUID(Long uid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}

	/**
	 * @return the fileSubSection
	 */
	public FileSubSection getFileSubSection() {
		return fileSubSection;
	}

	/**
	 * @param fileSubSection the fileSubSection to set
	 */
	public void setFileSubSection(FileSubSection fileSubSection) {
		this.fileSubSection = fileSubSection;
	}

	@Override
	public String toString() {
		String str = super.toString();
		if (soundFile != null) {
			str += "<br>File " + soundFile.getName();
//			if (fileSubSection != null) {
//				str += " (subsection)"; 
//			}
		}
		return str;
	}

}
