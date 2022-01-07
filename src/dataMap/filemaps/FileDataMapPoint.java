package dataMap.filemaps;

import java.io.File;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import dataMap.OfflineDataMapPoint;

/**
 * This was WavFileDataMapPoint, but can be used for any file types with OfflineFileServer. Won't rename
 * or move though since it's often serialised so 
 * @author dg50
 *
 */
public class FileDataMapPoint extends OfflineDataMapPoint implements ManagedParameters {
	
	private static final long serialVersionUID = 4955333805088379820L;
	
	private File soundFile;

	public FileDataMapPoint(File soundFile, long startTime, long endTime) {
		super(startTime, endTime, (int) Math.max(((endTime-startTime)/1000),1), 0);
		this.soundFile = soundFile;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
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

}
