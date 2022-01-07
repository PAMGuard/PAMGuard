package difar.calibration;

import java.sql.Connection;
import java.sql.Types;

import Array.streamerOrigin.HydrophoneOriginMethod;
import Array.streamerOrigin.HydrophoneOriginMethods;
import Array.streamerOrigin.HydrophoneOriginSystem;
import Array.streamerOrigin.OriginSettings;
import Array.streamerOrigin.StaticOriginSettings;
import GPS.GPSDataBlock;
import GPS.GpsData;
import GPS.GpsDataUnit;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import difar.DifarDataUnit;
import difar.DifarProcess;
import generalDatabase.PamConnection;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class CalibrationLogging extends SQLLogging {

	private DifarProcess difarProcess;

	private PamTableDefinition calibrationTable;

	private PamTableItem streamerUID, correction, stddev, n;

	private int basicItemCount;

	public CalibrationLogging(DifarProcess difarProcess, PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.difarProcess = difarProcess;
		calibrationTable = new PamTableDefinition("SonobuoyCalibration", UPDATE_POLICY_WRITENEW);
		calibrationTable.addTableItem(streamerUID = new PamTableItem("StreamerUID", Types.BIGINT));
		calibrationTable.addTableItem(correction = new PamTableItem("CompassCorrection", Types.DOUBLE));
		calibrationTable.addTableItem(stddev = new PamTableItem("StdDeviation", Types.DOUBLE));
		calibrationTable.addTableItem(n = new PamTableItem("NumClips", Types.INTEGER));

		basicItemCount = calibrationTable.getTableItemCount();

		setTableDefinition(calibrationTable);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		CalibrationDataUnit cdu = (CalibrationDataUnit) pamDataUnit;
		streamerUID.setValue(cdu.getStreamerUID());
		correction.setValue(cdu.getCompassCorrection());
		stddev.setValue(cdu.getCorrectionStdDev());
		n.setValue(cdu.getNumClips());
		// set all the extra ones to null before filling. 
		int itemCount = calibrationTable.getTableItemCount();
		for (int i = basicItemCount; i < itemCount; i++) {
			calibrationTable.getTableItem(i).setValue(null);
		}

	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#createDataUnit(long, int)
	 */
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds,
			int databaseIndex) {
		long streamerUID = this.streamerUID.getIntegerValue();
		double cor = correction.getDoubleValue();
		double std = stddev.getDoubleValue();
		int num = n.getIntegerValue();
		CalibrationDataUnit cdu = new CalibrationDataUnit(timeMilliseconds, streamerUID,
				cor, std, num);
		return cdu;
	}

}
