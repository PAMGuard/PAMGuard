package d3;

import java.util.ArrayList;

import PamguardMVC.PamDataUnit;

public class D3DataUnit extends PamDataUnit {

	private ArrayList<float[]> sensorData = new ArrayList<>();
	private ArrayList<Integer> sensorSampleRate = new ArrayList<>();
	private float[] depth;
	private float[] jerk;
	
	public D3DataUnit(long timeMilliseconds) {
		super(timeMilliseconds);
		// TODO Auto-generated constructor stub
	}

	public void addSensorData(float[] sensorData2, int sensorSampleRate) {
		sensorData.add(sensorData2);
		this.sensorSampleRate.add(sensorSampleRate);
	}

	/**
	 * @return the sensorData
	 */
	public float[] getSensorData(int sensorIndex) {
		if (sensorIndex < 0 || sensorIndex >= sensorData.size()) {
			return null;
		}
		return sensorData.get(sensorIndex);
	}

	/**
	 * @return the sensorSampleRate
	 */
	public int getSensorSampleRate(int sensorIndex) {
		if (sensorIndex < 0 || sensorIndex >= sensorData.size()) {
			return -1;
		}
		return sensorSampleRate.get(sensorIndex);
	}

	public void setDepth(float[] depth) {
		this.depth = depth;
	}
	
	/**
	 * @return the depthData
	 */
	public float[] getDepth() {
		return depth;
	}

	public void setJerk(float[] jerk) {
		this.jerk = jerk;
	}

	public float[] getJerk() {
		return jerk;
	}
//	/**
//	 * @param depthData the depthData to set
//	 */
//	public void setDepthData(short[] depthData) {
//		this.depthData = depthData;
//	}

}
