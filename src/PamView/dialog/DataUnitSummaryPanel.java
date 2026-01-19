package PamView.dialog;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import PamUtils.FrequencyFormat;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * Swing based information panel giving information about a data unit
 * which can be incorporated into a larger dialog.
 * @author Doug Gillespie
 *
 */
public class DataUnitSummaryPanel {
	
	private JLabel unitType;
	
	private JLabel startTime;
	
	private JLabel duration;
	
	private JLabel frequency;
	
	private JPanel mainPanel;

	public DataUnitSummaryPanel() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(unitType = new JLabel(" "));
		mainPanel.add(startTime = new JLabel(" "));
		mainPanel.add(duration = new JLabel(" "));
		mainPanel.add(frequency = new JLabel(" "));
	}
	
	/**
	 * 
	 * @return Swing component to include in some larger display. 
	 */
	public JPanel getComponent() {
		return mainPanel;
	}
	
	/**
	 * Set a data unit for th edisplay of summary data. 
	 * @param dataUnit
	 */
	public void setData(PamDataUnit<PamDataUnit, PamDataUnit> dataUnit) {
		if (dataUnit == null) {
			clearAll();
			return;
		}
		PamDataBlock pBlock = dataUnit.getParentDataBlock();
		String name;
		if (pBlock != null) {
			name = pBlock.getDataName();
		}
		else {
			name = dataUnit.getClass().getName();
		}
		long uid = dataUnit.getUID();
		if (uid != 0) {
			name += " UID: " + uid;
		}
		unitType.setText(name);
		startTime.setText(PamCalendar.formatDateTime(dataUnit.getTimeMilliseconds()));
		Double dur = dataUnit.getDurationInMilliseconds();
		if (dur != null) {
			duration.setText(String.format("Duration: %3.3fs", dur/1000.));
		}
		else {
			duration.setText(null);
		}
		double[] f = dataUnit.getFrequency();
		if (f != null) {
			frequency.setText("Frequency: " + FrequencyFormat.formatFrequencyRange(f, true));
		}
		
	}

	/**
	 * Clear all labels. 
	 */
	private void clearAll() {
		unitType.setText("");
		startTime.setText("");
		duration.setText("");
		frequency.setText("");		
	}

}
