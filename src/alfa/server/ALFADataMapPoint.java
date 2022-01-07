package alfa.server;

import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import dataMap.OfflineDataMapPoint;

public class ALFADataMapPoint extends OfflineDataMapPoint implements ManagedParameters {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String name = "ALFA Server";
	private Long lowUID, highUID; 
	public ALFADataMapPoint(long startTime, long endTime, int nDatas, long missingUIDs) {
		super(startTime, endTime, nDatas, missingUIDs);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public Long getLowestUID() {
		return lowUID;
	}

	@Override
	public void setLowestUID(Long uid) {
		lowUID = uid;
	}

	@Override
	public Long getHighestUID() {
		return highUID;
	}

	@Override
	public void setHighestUID(Long uid) {
		highUID = uid;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		try {
			Field field = this.getClass().getDeclaredField("highUID");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return highUID;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("lowUID");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return lowUID;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
