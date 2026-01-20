package analoginput.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.WestAlignedPanel;
import analoginput.AnalogDeviceParams;
import analoginput.AnalogRangeData;
import analoginput.SensorChannelInfo;
import analoginput.calibration.CalibrationData;

/**
 * Construct a dialog panel with range information for a list of named channels
 * @author dg50
 *
 */
public class AnalogChannelPanel implements PamDialogPanel {

	private SensorChannelInfo[] sensorChannelInfos;
	private int maxChannel;
	private List<AnalogRangeData> rangeList;

	private JComboBox<String>[] channelNumber;

	private JComboBox<AnalogRangeData>[] channelRange;

	private JTextField[][] calConstants;

	private JPanel channelPanel, calPanel;
	private JPanel leftPanel;
	private boolean allowNones;
	private JLabel[] channelLabel;
	
	private int nCalConstants = 3;

	public AnalogChannelPanel(SensorChannelInfo[] sensorChannelInfos, int maxChannel, List<AnalogRangeData> rangeList, boolean allowNones) {
		this.sensorChannelInfos = sensorChannelInfos;
		this.maxChannel = maxChannel;
		this.rangeList = rangeList;
		this.allowNones = allowNones;
		channelPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		int nChan = sensorChannelInfos.length;
		channelNumber = new JComboBox[nChan];
		channelRange = new JComboBox[nChan];
		channelLabel = new JLabel[nChan];
		//		mainPanel.add(new JLabel(" "), c);
		//		c.gridy++;
		//		c.gridwidth = 3;
		//		channelPanel.add(new JLabel("Channel mapping..."), c);
		//		c.gridwidth = 1;
		//		c.gridy++;
		channelPanel.setBorder(new TitledBorder("Channel Mapping"));
		channelPanel.add(new JLabel(" Input ", JLabel.CENTER), c);
		c.gridx++;
		channelPanel.add(new JLabel(" Channel ", JLabel.CENTER), c);
		c.gridx++;
		channelPanel.add(new JLabel(" Range ", JLabel.CENTER), c);
		for (int i = 0; i < nChan; i++) {
			channelNumber[i] = new JComboBox<>();
			if (allowNones) {
				channelNumber[i].addItem("-");
			}
			for (int j = 0; j < maxChannel; j++) {
				channelNumber[i].addItem(getChannelName(j));
			}
			channelRange[i] = new JComboBox<>();
			for (int j = 0; j < rangeList.size(); j++) {
				channelRange[i].addItem(rangeList.get(j));
			}
			c.gridy++;
			c.gridx = 0;
			channelPanel.add(channelLabel[i] = new JLabel(sensorChannelInfos[i].getName(), JLabel.RIGHT), c);
			c.gridx++;
			channelPanel.add(channelNumber[i], c);
			c.gridx++;
			channelPanel.add(channelRange[i], c);
			
			channelNumber[i].addActionListener(new ChannelNumberChange(i));
			channelLabel[i].setToolTipText(sensorChannelInfos[i].getToolTip());
		}
		calPanel = new JPanel(new GridBagLayout());
		calPanel.setBorder(new TitledBorder("Data Conversion"));
		c = new PamGridBagContraints();
		calConstants = new JTextField[nChan][nCalConstants];
		int calFieldLen = 6;
		calPanel.add(new JLabel(" Input ", JLabel.CENTER), c);
		c.gridwidth = 5;
		c.gridx++;
		calPanel.add(new JLabel(" Calibration Constants ", JLabel.CENTER), c);
		c.gridwidth = 1;
		JComboBox stDrop = new JComboBox<>(); // make same height as dropdowns to look nice. 
		Dimension prefSz = stDrop.getPreferredSize();
		for (int i = 0; i < nChan; i++) {
			for (int j = 0; j < nCalConstants; j++) {
				calConstants[i][j] = new JTextField(calFieldLen);
				Dimension sz = calConstants[i][j].getPreferredSize();
				sz.height = Math.max(sz.height, prefSz.height);
				calConstants[i][j].setPreferredSize(sz);
			}
			c.gridx=0;
			c.gridy++;
			JLabel label;
			calPanel.add(label = new JLabel(sensorChannelInfos[i].getName() + " = (Input+", JLabel.RIGHT), c);
			label.setToolTipText(sensorChannelInfos[i].getToolTip());
			c.gridx++;
			calPanel.add(calConstants[i][0], c);
			c.gridx++;
			calPanel.add(new JLabel(")*"),c);
			c.gridx++;
			calPanel.add(calConstants[i][1], c);
			c.gridx++;
			calPanel.add(new JLabel("+"),c);
			c.gridx++;
			calPanel.add(calConstants[i][2], c);
		}

		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab("Channels", new WestAlignedPanel(channelPanel, true));
		tabPane.addTab("Calibration", calPanel);
		//		leftPanel = new WestAlignedPanel(tabPane);
		leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(BorderLayout.CENTER, tabPane);
	}
	
	/**
	 * Get the name of a channel for display in the ComboBox
	 * @param j channel index
	 * @return channel name
	 */
	public String getChannelName(int j) {
		return String.format("%d", j); 
	}

	protected void channelNumberChange(int i) {
		// TODO Auto-generated method stub
		
	}

	public boolean setItemRange(int iItem, AnalogRangeData rangeData) {
		channelRange[iItem].setSelectedItem(rangeData);
		return channelRange[iItem].getSelectedItem() != null;
	}

	public AnalogRangeData getItemRange(int iItem) {
		return (AnalogRangeData) channelRange[iItem].getSelectedItem();
	}

	public void setDeviceParams(AnalogDeviceParams deviceParams) {
		for (int i = 0; i < sensorChannelInfos.length; i++) {
			setItemRange(i, deviceParams.getItemRange(i));
			Integer itemChan = deviceParams.getItemChannel(i);
			if (itemChan != null) {
				if (allowNones) itemChan++;
				channelNumber[i].setSelectedIndex(itemChan);
			}
			CalibrationData calData = deviceParams.getCalibration(i);
			setItemCalibration(i, calData);
			enableItem(i);
		}
	}

	public void setItemCalibration(int item, CalibrationData calData) {
		if (calData == null) {
			return;
		}
		double[] params = calData.getParams();
		JTextField[] fields = calConstants[item];
		int n = Math.min(fields.length, params.length);
		DecimalFormat df = new DecimalFormat("####.#######");
		for (int i = 0; i < n; i++) {
			fields[i].setText(df.format(params[i]));
		}
	}

	public CalibrationData getItemCalibration(int item) {
		JTextField[] fields = calConstants[item];
		double[] params = new double[nCalConstants];
		for (int i = 0; i < fields.length; i++) {
			try {
				params[i] = Double.valueOf(fields[i].getText());
			}
			catch (NumberFormatException e) {
				return null;
			}
		}
		return new CalibrationData(params);
	}

	public AnalogDeviceParams getDeviceParams(AnalogDeviceParams deviceParams) {
		for (int i = 0; i < sensorChannelInfos.length; i++) {
			int chan = (Integer) channelNumber[i].getSelectedIndex();
			if (allowNones) {
				chan--;
			}
			deviceParams.setItemChannel(i, chan);
			if (chan < 0) {
				continue; // nothing wrong with not selecting a channel. 
			}
			AnalogRangeData itemRange = getItemRange(i);
			if (itemRange == null) {
				return null;
			}
			deviceParams.setItemRange(i, itemRange);
			CalibrationData calData = getItemCalibration(i);
			if (calData == null) {
				return null;
			}
			deviceParams.setCalibration(i, calData);
		}
		return deviceParams;
	}



	@Override
	public JComponent getDialogComponent() {
		return leftPanel;
	}

	@Override
	public void setParams() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private class ChannelNumberChange implements ActionListener {
		
		private int item;

		/**
		 * @param iChan
		 */
		public ChannelNumberChange(int item) {
			super();
			this.item = item;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			enableItem(item);
		}
		
	}

	public void enableItem(int item) {
		boolean enable = true;
		if (allowNones && channelNumber[item].getSelectedIndex() <= 0) {
			enable = false;
		}
		channelRange[item].setEnabled(enable);
		JTextField[] calFields = calConstants[item];
		for (int i = 0; i < calFields.length; i++) {
			calFields[i].setEnabled(enable);
		}
	}

}
