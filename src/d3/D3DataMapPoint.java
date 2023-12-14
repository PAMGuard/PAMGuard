package d3;

import java.io.File;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;
import fileOfflineData.OfflineFileMapPoint;

public class D3DataMapPoint extends OfflineFileMapPoint implements ManagedParameters {

	/**
	 * Offset of the data in this file from the start of the file. 
	 */
	long fileOffsetStart;
	
	/**
	 * Offset for the end of the data in this file from the start
	 * of the file - stops anything trying to read too far.
	 */
	long fileOffsetEnd;
	
	public D3DataMapPoint(long startTime, long endTime, int nDatas,
			File dataFile, long fileOffsetStart, long fileOffsetEnd) {
		super(startTime, endTime, nDatas, dataFile);
		this.fileOffsetStart = fileOffsetStart;
		this.fileOffsetEnd = fileOffsetEnd;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("fileOffsetStart");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return fileOffsetStart;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("fileOffsetEnd");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return fileOffsetEnd;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
