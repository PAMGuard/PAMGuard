package dataGram;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;

public class DatagramDataPoint implements Serializable, ManagedParameters {

	private static final long serialVersionUID = 1L;

	private Datagram datagram;
	private long startTime;
	private long endTime;
	private float[] data;
	private int nDataUnits;
	
	/**
	 * @param datagram
	 * @param startTime
	 * @param endTime
	 */
	public DatagramDataPoint(Datagram datagram, long startTime, long endTime, int dataLength) {
		super();
		this.datagram = datagram;
		this.startTime = startTime;
		this.endTime = endTime;
		data = new float[dataLength];
	}
	
	
	/**
	 * @param datagram
	 * @param startTime
	 * @param endTime
	 */
	public DatagramDataPoint(long startTime, long endTime, int dataLength) {
		super();
		this.startTime = startTime;
		this.endTime = endTime;
		data = new float[dataLength];
	}

	/**
	 * @return the datagram
	 */
	public Datagram getDatagram() {
		return datagram;
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @return the endTime
	 */
	public long getEndTime() {
		return endTime;
	}
	
	/**
	 * Set the end time of the data point
	 * @param endTime the end time
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	/**
	 * @return the data
	 */
	public float[] getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(float[] data, int nDataPoints) {
		this.data = data;
		this.nDataUnits = nDataPoints;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("nDataUnits");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return nDataUnits;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

	
}
