package difar.display;

import PamUtils.PamCalendar;
import PamView.component.DataBlockTableView;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import difar.DifarProcess;
import difar.calibration.CalibrationDataUnit;

public class CalibrationTableView extends DataBlockTableView<CalibrationDataUnit> {

	/**
	 * Seem to have to make this static so it get's made before the super class 
	 * constructor is called, otherwise the calls to getColumnNames will return 
	 * null on the first couple of calls while the table is being constructed
	 * and will mess everything up. 
	 */
	private static String[] colNames = {"Calibration Started", "StreamerUID", "Correction", "Std Dev", "n"};

	public CalibrationTableView(DifarProcess difarProcess) {
		super(difarProcess.getCalibrationDataBlock(), difarProcess.getProcessName() + " Calibration Data table");
	}

	@Override
	public String[] getColumnNames() {
		return colNames;
	}


	@Override
	public Object getColumnData(CalibrationDataUnit dataUnit, int columnIndex) {
		if (dataUnit == null) {
			return null;
		}
		switch (columnIndex) {
		case 0:
			return PamCalendar.formatTodaysTime(dataUnit.getTimeMilliseconds());
		case 1:
			return dataUnit.getStreamerUID();
		case 2:
			return dataUnit.getCompassCorrection();
		case 3:
			return dataUnit.getCorrectionStdDev();
		case 4:
			return dataUnit.getNumClips();
		}
		return null;
	}
	
	
}

