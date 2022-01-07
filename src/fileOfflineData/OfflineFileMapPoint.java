package fileOfflineData;

import java.io.File;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import dataMap.OfflineDataMapPoint;

public class OfflineFileMapPoint extends OfflineDataMapPoint implements ManagedParameters {

	private static final long serialVersionUID = 1L;

	private File dataFile;

	public OfflineFileMapPoint(long startTime, long endTime, int nDatas, File dataFile) {
		super(startTime, endTime, nDatas, 0);
		this.dataFile = dataFile;
	}

	@Override
	public String getName() {
		return dataFile.getName();
	}

	/**
	 * @return the dataFile
	 */
	public File getDataFile() {
		return dataFile;
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
