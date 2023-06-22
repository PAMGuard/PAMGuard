package analogarraysensor;

import Array.sensors.ArraySensorFieldType;
import PamController.masterReference.MasterReferencePoint;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;
import analoginput.AnalogSensorData;
import geoMag.MagneticVariation;

public class AnalogArraySensorDataUnit extends PamDataUnit implements Array.sensors.ArraySensorDataUnit {

	private int streamer;
	private AnalogSensorData[] sensorData;

	public AnalogArraySensorDataUnit(long timeMilliseconds, int streamer, AnalogSensorData[] sensorData) {
		super(timeMilliseconds);
		this.streamer = streamer;
		setChannelBitmap(1<<streamer);
		this.sensorData = sensorData;
	}

	/**
	 * @return the streamer
	 */
	public int getStreamer() {
		return streamer;
	}

	/**
	 * @return the sensorData
	 */
	public AnalogSensorData[] getSensorData() {
		return sensorData;
	}


	@Override
	public Double getField(int streamer, ArraySensorFieldType fieldtype) {
		if (sensorData == null) {
			return null;
		}
		switch(fieldtype) {
		case HEADING:
			Double head = getFieldVal(3);
			if (head != null) {
				head += getMagDev();
			}
			return head;
		case HEIGHT:
			return -getFieldVal(0);
		case PITCH:
			return getFieldVal(1);
		case ROLL:
			return getFieldVal(2);
		default:
			break;
		
		}
		return null;
	}

	private Double getFieldVal(int iVal) {
		if (sensorData == null || sensorData.length <= iVal || iVal < 0) {
			return null;
		}
		AnalogSensorData sensDat = sensorData[iVal];
		if (sensDat == null) {
			return null;
		}
		else {
			return sensDat.getCalibratedValue();
		}
	}

	public double getMagDev() {
		LatLong latLong = MasterReferencePoint.getLatLong();
		if (latLong == null) {
			return 0;
		}
		return MagneticVariation.getInstance().getVariation(getTimeMilliseconds(), latLong.getLatitude(), latLong.getLongitude());
	}

}
