package alfa.swinggui;

import java.awt.BorderLayout;
import java.util.Arrays;

import javax.swing.JComponent;

import GPS.GpsData;
import PamUtils.PamCalendar;
import PamView.component.DataBlockTableView;
import PamView.panel.PamPanel;
import alfa.ALFAControl;
import alfa.effortmonitor.AngleHistogram;
import alfa.effortmonitor.IntervalDataBlock;
import alfa.effortmonitor.IntervalDataUnit;

public class IntervalTablePanel {

	private IntervalDataBlock intervalDataBlock;
	
	private IntervalTable intervalTable;
	
	private PamPanel mainPanel;

	private ALFAControl alfaControl;
	
	private final  String[] baseColumnNames = {"Start", "Position", "End", "Postion", "% Effort", "Trains", "Clicks"};

	public IntervalTablePanel(ALFAControl alfaControl, IntervalDataBlock intervalDataBlock) {
		this.alfaControl = alfaControl;
		intervalTable = new IntervalTable(intervalDataBlock);
		mainPanel = new PamPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER, intervalTable.getComponent());
	}
	
	public JComponent getComponent() {
		return mainPanel;
	}


	private class IntervalTable extends DataBlockTableView<IntervalDataUnit>{
		public IntervalTable(IntervalDataBlock  intervalDataBlock) {
			super(intervalDataBlock, "Monitor intervals");
		}


		@Override
		public String[] getColumnNames() {
			int nHistos = alfaControl.getAlfaParameters().histosPerReportInterval; 
			String[] columnNames = Arrays.copyOf(baseColumnNames, baseColumnNames.length +nHistos);
			for (int i = 0; i < nHistos; i++) {
				columnNames[i+baseColumnNames.length] = "Interval_" + (i+1);
			}
			return columnNames;
		}

		@Override
		public Object getColumnData(IntervalDataUnit dataUnit, int column) {
			if (dataUnit == null) {
				return null;
			}
			switch (column) {
			case 0:
				return formatTime(dataUnit.getTimeMilliseconds());
			case 1:
				GpsData gpsData = dataUnit.getFirstGPSData();
				if (gpsData == null) {
					return "no GPS";
				}
				else {
					return gpsData.formatLatitude() + ", " + gpsData.formatLongitude();
				}
			case 2:
				return formatTime(dataUnit.getEndTimeInMilliseconds());
			case 3:
				GpsData endGpsData = dataUnit.getFirstGPSData();
				if (endGpsData == null) {
					return "no GPS";
				}
				else {
					return endGpsData.formatLatitude() + ", " + endGpsData.formatLongitude();
				}
			case 4:
				double duration = dataUnit.getDurationInMilliseconds();
				return String.format("%3.0f %% of %s", dataUnit.getPercentEffort(), PamCalendar.formatTime((long) duration));
			case 5:
				return dataUnit.getnClickTrains();
			case 6:
				return dataUnit.getnClicks();
			}
			int iHist = column-baseColumnNames.length;
			if (iHist < 0) return null;
			AngleHistogram angleHist = dataUnit.getAngleHistogram(iHist);
			if (angleHist == null) {
				return null;
			}
			double[] data = angleHist.getData();
			if (data == null || data.length == 0) {
				return null;
			}
			String str = String.format("(%.0f",data[0]);
			for (int i = 1; i < data.length; i++) {
				str += String.format(",%.0f", data[i]);
			}
			str += ")";
				
			return str;	
		}

		private String formatTime(long timeMilliseconds) {
			return PamCalendar.formatTodaysTime(timeMilliseconds, true);
		}
	}


}
