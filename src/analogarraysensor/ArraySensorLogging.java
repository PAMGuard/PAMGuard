package analogarraysensor;

import java.sql.Types;

import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import analoginput.AnalogSensorData;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class ArraySensorLogging extends SQLLogging {

	private SensorPair[] tablePairs;

	public ArraySensorLogging(ArraySensorControl arraySensorControl, AnalogArraySensorDataBlock pamDataBlock) {
		super(pamDataBlock);
		PamTableDefinition tableDef = new PamTableDefinition(arraySensorControl.getUnitName());
		String[] sensNames = arraySensorControl.getSensChannelNames();
		tablePairs = new SensorPair[sensNames.length];
		for (int i = 0; i < sensNames.length; i++) {
			tablePairs[i] = new SensorPair(sensNames[i]);
			tableDef.addTableItem(tablePairs[i].rawItem);
			tableDef.addTableItem(tablePairs[i].calItem);
		}
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		AnalogArraySensorDataUnit asdu = (AnalogArraySensorDataUnit) pamDataUnit;
		AnalogSensorData[] sensData = asdu.getSensorData();
		for (int i = 0; i < tablePairs.length; i++) {
			if (sensData != null && sensData[i] != null) {
				tablePairs[i].setData(sensData[i]);
			}
			else {
				tablePairs[i].setData(null);
			}
		}
	}

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		AnalogSensorData[] sensorData = new AnalogSensorData[tablePairs.length];
		for (int i = 0; i < tablePairs.length; i++) {
			double rawValue = tablePairs[i].rawItem.getDoubleValue();
			if (Double.isNaN(rawValue)) {
				rawValue = 0;
			}
			double calValue = tablePairs[i].calItem.getDoubleValue();
			if (Double.isNaN(calValue)) {
				calValue = 0;
			}
			AnalogSensorData aData = new AnalogSensorData(rawValue, calValue);
			sensorData[i] = aData;
		}
		int chanMap = getTableDefinition().getChannelBitmap().getIntegerValue();
		int streamer = PamUtils.getSingleChannel(chanMap);
		if (streamer < 0) streamer = 0;
		AnalogArraySensorDataUnit asdu = new AnalogArraySensorDataUnit(timeMilliseconds, streamer, sensorData);
		return asdu;
	}

	private class SensorPair {

		private PamTableItem rawItem, calItem;

		private SensorPair(String name) {
			rawItem = new PamTableItem(name+"_raw", Types.REAL);
			calItem = new PamTableItem(name+"_cal", Types.REAL);
		}

		public void setData(AnalogSensorData analogSensorData) {
			if (analogSensorData == null) {
				rawItem.setValue(null);
				calItem.setValue(null);
			}
			else {
				rawItem.setValue(new Float(analogSensorData.getRawValue()));
				calItem.setValue(new Float(analogSensorData.getCalibratedValue()));
			}
		}
	}

}
