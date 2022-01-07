package analoginput;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Hashtable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import analoginput.calibration.CalibrationData;

public class AnalogDeviceParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	private Hashtable<Integer, AnalogRangeData> channelRangeData = new Hashtable<>();
	
	private Hashtable<Integer, Integer> channelTable = new Hashtable<>();
	
	private Hashtable<Integer, CalibrationData> calibrationTable = new Hashtable<>();
	
	public AnalogRangeData getItemRange(int item) {
		if (channelRangeData == null) {
			channelRangeData = new Hashtable<>();
		}
		return channelRangeData.get(item);
	}
	
	public void setItemRange(int item, AnalogRangeData rangeData) {
		if (channelRangeData == null) {
			channelRangeData = new Hashtable<>();
		}
		channelRangeData.put(item, rangeData);
	}
	
	public Integer getItemChannel(int item) {
		if (channelTable == null) {
			channelTable = new Hashtable<>();
		}
		return channelTable.get(item);
	}
	
	public void setItemChannel(int item, int channel) {
		if (channelTable == null) {
			channelTable = new Hashtable<>();
		}
		channelTable.put(item, channel);
	}

	public CalibrationData getCalibration(int item) {
		if (calibrationTable == null) {
			calibrationTable = new Hashtable<>();
		}
		return calibrationTable.get(item);
	}
	
	public void setCalibration(int item, CalibrationData calibrationData) {
		if (calibrationTable == null) {
			calibrationTable = new Hashtable<>();
		}
		calibrationTable.put(item, calibrationData);
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("calibrationTable");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return calibrationTable;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("channelRangeData");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return channelRangeData;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("channelTable");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return channelTable;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
