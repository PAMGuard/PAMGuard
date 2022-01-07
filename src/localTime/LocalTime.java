package localTime;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import PamController.PamControlledUnit;
import PamUtils.PamCalendar;
import PamView.PamSidePanel;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;

public class LocalTime extends PamControlledUnit {

	private LTSidePanel ltSidePanel;

	public LocalTime(String unitName) {
		super("Local Time", unitName);
		ltSidePanel = new LTSidePanel();
	}

	@Override
	public PamSidePanel getSidePanel() {
		return ltSidePanel;
	}

	private class LTSidePanel implements PamSidePanel {

		TitledBorder border;
		
		private JPanel mainPanel;
		
		private JLabel timeLabel;
		
		private JLabel zoneLabel;
		
		public LTSidePanel() {
			super();
			border = new TitledBorder(getUnitName());
			mainPanel = new PamPanel();
			mainPanel.setBorder(border);
//			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
			mainPanel.setLayout(new BorderLayout());
			timeLabel = new PamLabel("", JLabel.CENTER);
			mainPanel.add(BorderLayout.CENTER, timeLabel);
//			mainPanel.add(zoneLabel = new JLabel("", JLabel.CENTER));
			Timer timer = new Timer(500, new TimeAction());
			timer.start();
		}

		@Override
		public JComponent getPanel() {
			return mainPanel;
		}

		@Override
		public void rename(String newName) {
			mainPanel.setBorder(new TitledBorder(newName));
		}
		
		private void updateTimes() {
			long t = PamCalendar.getTimeInMillis();
			timeLabel.setText(PamCalendar.formatLocalDateTime(t));
		}
		
		private class TimeAction implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateTimes();
			}
			
		}
		
	}
}
