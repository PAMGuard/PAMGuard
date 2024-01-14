package PamguardMVC.datakeeper;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Hashtable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class DataKeeperSettings implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	private Hashtable<String, Integer> keepTimeData = new Hashtable<>();

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected DataKeeperSettings clone() {
		try {
			return (DataKeeperSettings) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Set how long we want to keep data for in seconds for a 
	 * named data block.<br>
	 * This is a minimum - other users of data may ask for longer. 
	 * @param dataLongName data block long name
	 * @return time in seconds. 
	 */
	public Integer getKeepTimeSeconds(String dataLongName) {
		return keepTimeData.get(dataLongName);
	}
	
	/**
	 * Set how long we want to keep data for in seconds for a 
	 * named data block.<br>
	 * This is a minimum - other users of data may ask for longer. 
	 * @param dataLongName data block long name
	 * @param seconds time in seconds
	 */
	public void setKeepTimeSeconds(String dataLongName, int seconds) {
		keepTimeData.put(dataLongName, seconds);
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("keepTimeData");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return keepTimeData;
				}

				@Override
				public boolean setData(Object data) throws IllegalArgumentException, IllegalAccessException {
					try {
						keepTimeData = (Hashtable<String, Integer>) data;
					}
					catch (Exception e) {
						throw new IllegalArgumentException(e.getMessage());
					}
					return true;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
