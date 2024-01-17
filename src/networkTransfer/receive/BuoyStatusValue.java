package networkTransfer.receive;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamUtils.PamCalendar;

public class BuoyStatusValue implements Serializable, ManagedParameters {

	private static final long serialVersionUID = 2L;

	private long timeMillis;
	
	private Serializable data;


	public BuoyStatusValue(Serializable data) {
		this(System.currentTimeMillis(), data);
	}
	
	public BuoyStatusValue(long timeMillis, Serializable data) {
		super();
		this.timeMillis = timeMillis;
		this.data = data;
	}

	/**
	 * @return the data
	 */
	public Serializable getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(Serializable data) {
		this.data = data;
	}

	/**
	 * @return the datetime
	 */
	public long getTimemillis() {
		return timeMillis;
	}

	@Override
	public String toString() {
		return PamCalendar.formatDateTime(timeMillis) + data;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
