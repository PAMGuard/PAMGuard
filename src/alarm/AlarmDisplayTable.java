package alarm;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayComponentAdapter;
import PamUtils.PamCalendar;
import PamView.PamTable;
import PamView.tables.SwingTableColumnWidths;
import PamView.tables.TableColumnWidthData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;

public class AlarmDisplayTable extends UserDisplayComponentAdapter {

	private JPanel alarmPanel;
	
	private AlarmControl alarmControl;
	
	private PamTable alarmTable;

	private AlarmDataBlock alarmDataBlock;
	
	private AlarmTableModel alarmTableModel;
	
	
	public AlarmDisplayTable(AlarmControl alarmControl) {
		super();
		this.alarmControl = alarmControl;
		alarmPanel = new JPanel(new BorderLayout());
		alarmDataBlock = alarmControl.getAlarmProcess().getAlarmDataBlock();
		alarmTableModel = new AlarmTableModel();
		alarmTable = new PamTable(alarmTableModel);
		
		JPanel tablePanel = new JPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(alarmTable, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		tablePanel.add(BorderLayout.CENTER, scrollPane);
		alarmPanel.add(BorderLayout.CENTER, tablePanel);
		
		new SwingTableColumnWidths("Alarm:"+alarmControl.getUnitName(), alarmTable);
		
		alarmDataBlock.addObserver(new DataObserver(), false);
	}

	public void updateTable() {
		alarmTableModel.fireTableDataChanged();
	}

	public JComponent getDisplayComponent() {
		return alarmPanel;
	}
	
	class DataObserver extends PamObserverAdapter {

		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return (long) alarmControl.alarmParameters.getHoldSeconds() * 1000L;
//			return 3600000L;
		}

		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
			updateTable();
		}

		@Override
		public String getObserverName() {
			return alarmControl.getUnitName() + " display table";
		}
		
	}
	
	class AlarmTableModel extends AbstractTableModel {

		String[] colNames = {"Current State (score)", "Highest State (score)", "Date", "Amber times", "Red times"};
		@Override
		public int getRowCount() {
			return alarmDataBlock.getUnitsCount();
		}

		@Override
		public int getColumnCount() {
			return colNames.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			synchronized (alarmDataBlock.getSynchLock()) {
				int nD = alarmDataBlock.getUnitsCount();
				int iD = nD-rowIndex-1;
				AlarmDataUnit alarmDataUnit = alarmDataBlock.getDataUnit(iD, PamDataBlock.REFERENCE_CURRENT);
				if (alarmDataUnit == null) {
					return null;
				}
				switch(columnIndex) {
				case 0:
					return formatState(alarmDataUnit.getCurrentStatus(), alarmDataUnit.getCurrentScore());
				case 1:
					return formatState(alarmDataUnit.getHighestStatus(), alarmDataUnit.getHighestScore());
				case 2:
					return PamCalendar.formatDate(alarmDataUnit.getTimeMilliseconds());
				case 3:
					return formatInterval(alarmDataUnit.getFirstStateTime()[1], alarmDataUnit.getLastStateTime()[1]);
				case 4:
					return formatInterval(alarmDataUnit.getFirstStateTime()[2], alarmDataUnit.getLastStateTime()[2]);
				}
			}
			return null;
		}

		private Object formatState(int currentStatus, double currentScore) {
			return String.format("%s (%3.1f)", AlarmParameters.sayLevel(currentStatus), currentScore);
		}

		private Object formatInterval(long start, long end) {
			if (start == 0 && end == 0) {
				return "-";
			}
			else if (start > 0 && end > 0) {
				return String.format("%s to %s", PamCalendar.formatTime(start), PamCalendar.formatTime(end));
			}
			else if (start > 0 && end == 0) {
				return String.format("%s ongoing", PamCalendar.formatTime(start));
			}
			
			return null;
		}

		@Override
		public String getColumnName(int column) {
			return colNames[column];
		}
		
	}

	@Override
	public Component getComponent() {
		return getDisplayComponent();
	}

	@Override
	public void openComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeComponent() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void notifyModelChanged(int changeType) {
		
	}

	@Override
	public String getFrameTitle() {
		// TODO Auto-generated method stub
		return alarmControl.getUnitName() + " history";
	}
}
