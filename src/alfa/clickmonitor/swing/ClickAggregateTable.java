package alfa.clickmonitor.swing;

import PamUtils.PamCalendar;
import PamView.component.DataBlockTableView;
import alfa.clickmonitor.ClickMonitorProcess;
import alfa.clickmonitor.eventaggregator.ClickEventAggregate;

public class ClickAggregateTable extends DataBlockTableView<ClickEventAggregate>{
	
	/**
	 * Seem to have to make this static so it get's made before the super class 
	 * constructor is called, otherwise the calls to getColumnNames will return 
	 * null on the first couple of calls while the table is being constructed
	 * and will mess everything up. 
	 */
	private String[] colNames = {"UID", "Date Time", "N", "TX"};

	public ClickAggregateTable(ClickMonitorProcess clickMonitorProcess) {
		super(clickMonitorProcess.getClickAggregateDataBlock(), clickMonitorProcess.getProcessName() + " Data table");
	}
	
	@Override
	public String[] getColumnNames() {
		return colNames;
	}


	@Override
	public Object getColumnData(ClickEventAggregate dataUnit, int columnIndex) {
		if (dataUnit == null) {
			return null;
		}
		switch (columnIndex) {
		case 0:
			return dataUnit.getUID();
		case 1:
			return PamCalendar.formatTodaysTime(dataUnit.getTimeMilliseconds());
		case 2:
			return dataUnit.getSubDetectionsCount();
		case 3:
			return false;
			
		}
		return null;
	}
}
