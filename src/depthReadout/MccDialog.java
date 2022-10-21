package depthReadout;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import depthReadout.MccDepthParameters.MccSensorParameters;

import mcc.MccJniInterface;
import mcc.mccjna.MCCBoardInfo;
import mcc.mccjna.MCCConstants;
import mcc.mccjna.MCCJNA;
import mcc.mccjna.MCCUtils;
import Array.ArrayManager;
import Array.PamArray;
import PamController.PamController;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.warn.WarnOnce;

public class MccDialog extends PamDialog {
	
	private MccDepthParameters mccDepthParameters;
	
	private static MccDialog singleInstance;
	
	private DepthControl depthControl;
	
	private MccDepthSystem mccDepthSystem;
	
	private MccJniInterface mccJniInterface;
	
	private DepthParameters depthParameters;
	
	private BoardPanel boardPanel;
	
	private SensorsPanel sensorsPanel;

	private MccDialog(Frame parentFrame) {
		super(parentFrame, "Measurement Computing Depth Readout", false);

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(BorderLayout.NORTH, boardPanel = new BoardPanel());
		p.add(BorderLayout.CENTER, sensorsPanel = new SensorsPanel());
		setHelpPoint("utilities.depthreadout.docs.depth_overview");
		setDialogComponent(p);
	}
	
	public static MccDepthParameters showDialog(DepthControl depthControl, MccDepthSystem mccDepthSystem, 
			Frame parentFrame, MccDepthParameters mccDepthParameters) {
		// always make a new panel.
//		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new MccDialog(parentFrame);
//		}
		singleInstance.depthControl = depthControl;
		singleInstance.depthParameters = depthControl.depthParameters;
		singleInstance.mccDepthParameters = mccDepthParameters.clone();
		singleInstance.mccDepthSystem = mccDepthSystem;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.mccDepthParameters;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	public void setParams() {
		mccJniInterface = mccDepthSystem.mccJniInterface;
		boardPanel.setParams();
		sensorsPanel.setParams();
		pack();
	}
	
	@Override
	public boolean getParams() {
		if (boardPanel.getParams() == false) {
			return false;
		}
		if (sensorsPanel.getParams() == false) {
			return false;
		}
		depthControl.depthParameters = depthParameters;
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	
	/**
	 * Container for many sub panels. 
	 * @author Douglas Gillespie
	 *
	 */
	class SensorsPanel extends JPanel {
		int nPanels = 0;
		SensorPanel[] sensorPanels;
		public SensorsPanel() {
			super();
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		}
		void setParams() {
			if (nPanels != boardPanel.getNumSensors()) {
				this.removeAll();
				nPanels = boardPanel.getNumSensors();
				sensorPanels = new SensorPanel[nPanels];
				for (int i = 0; i < nPanels; i++) {
					add(sensorPanels[i] = new SensorPanel(i));
				}
				pack();
			}
			for (int i = 0; i < nPanels; i++) {
				sensorPanels[i].setParams();
			}
		}
		boolean getParams() {
			if (nPanels == 0) {
				return true;
			}
			if (mccDepthParameters.mccSensorParameters == null ||nPanels >=  mccDepthParameters.mccSensorParameters.length) {
				mccDepthParameters.mccSensorParameters = new MccDepthParameters.MccSensorParameters[nPanels];
			}
			depthParameters.hydrophoneMaps = new int[nPanels];
//			depthParameters.hydrophoneY = new double[nPanels];
			for (int i = 0; i < nPanels; i++) {
				MccSensorParameters newP = sensorPanels[i].getParams();
				if (newP == null) {
					return false;
				}
				else {
					mccDepthParameters.mccSensorParameters[i] = newP;
				}
			}
			return true;
		}
	}
	
	/**
	 * one of these for every channel. 
	 * @author Douglas Gillespie
	 *
	 */
	class SensorPanel extends JPanel {
		int iSensor;
		JTextField channel;
		JTextField scaleA, scaleB;
//		JTextField yPosition;
		JCheckBox[] streamers;
		public SensorPanel(int iSensor) {
			super();
			this.iSensor = iSensor;
			setBorder(new TitledBorder("Sensor " + iSensor));
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridwidth = 2;
			c.gridx = c.gridy = 0;
			c.anchor = GridBagConstraints.WEST;
			c.fill = GridBagConstraints.HORIZONTAL;
			addComponent(this, new JLabel("Hardware Channel  "), c);
			c.gridx = c.gridwidth;
			c.gridwidth = 1;
			addComponent(this, channel = new JTextField(3), c);
			// y coordinate of sensor	
			c.gridx++;
			c.anchor = GridBagConstraints.EAST;
//			addComponent(this, new JLabel("   Cable pos"), c);
//			c.gridx++;
//			addComponent(this, yPosition = new JTextField(6), c);
//			c.gridx++;
//			addComponent(this, new JLabel(" m"), c);
			
			// hydrophones ...
			c.gridy++;
			c.gridx = 0;
			c.anchor = GridBagConstraints.WEST;
			addComponent(this, new JLabel("Streamers ", JLabel.RIGHT), c);
			ArrayManager arrayManager = ArrayManager.getArrayManager();
			PamArray array = arrayManager.getCurrentArray();
			int nS = array.getNumStreamers();
			JPanel hPanel = new JPanel();
//			hPanel.setBorder(new TitledBorder(""));
			hPanel.setLayout(new GridBagLayout());
			GridBagConstraints c2 = new GridBagConstraints();
			c2.gridx = c2.gridy = 0;
			c2.anchor = GridBagConstraints.CENTER;
			streamers = new JCheckBox[nS];
			for (int i = 0; i < nS; i++) {
				c2.gridy = 0;
				addComponent(hPanel, new JLabel(String.format("%d", i)), c2);	
				streamers[i] = new JCheckBox();
				c2.gridy = 1;
				addComponent(hPanel, streamers[i], c2);
				c2.gridx++;
			}
			c.fill = GridBagConstraints.NONE;
//			c.anchor = GridBagConstraints.WEST;
			c.gridwidth = 6;
			c.gridx = 1;
			addComponent(this, hPanel, c);
			// calibration
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 3;
//			addComponent(this, new JLabel(" "), c);
//			c.gridy++;
			addComponent(this, new JLabel("Calibration ..."), c);
			c.gridy++;
			c.gridwidth = 1;
			c.gridx = 0;
			addComponent(this, new JLabel("Depth (m) = "), c);
			c.gridx++;
			c.gridwidth = 2;
			addComponent(this, scaleA = new JTextField(6), c);
			c.gridx+=2;
			c.gridwidth = 1;
			addComponent(this, new JLabel(" * Voltage + "), c);
			c.gridx++;
			addComponent(this, scaleB = new JTextField(6), c);
			c.gridx++;
			addComponent(this, new JLabel(" offset "), c);
			
			channel.setToolTipText("Set the hardware (Measurement Computing input device) channel number");
			scaleA.setToolTipText("Set parameters for a linear transformation of measured voltage into depth");
			scaleB.setToolTipText("Set parameters for a linear transformation of measured voltage into depth");
//			yPosition.setToolTipText("Set the position of the sensor along the cable");
			if (streamers != null) for (int i = 0; i < streamers.length; i++) {
				streamers[i].setToolTipText("Select which streamer(s) use data from this sensor");
			}
		}
		
		void setParams() {
			MccSensorParameters sp;
			if (mccDepthParameters.mccSensorParameters != null && mccDepthParameters.mccSensorParameters.length > iSensor) {
				sp = mccDepthParameters.mccSensorParameters[iSensor];
			}
			else {
				sp = mccDepthParameters.new MccSensorParameters();
				sp.iChan = iSensor;
			}
			if (sp == null) {
				return;
			}
			channel.setText(String.format("%d", sp.iChan));

			if (depthParameters.hydrophoneMaps != null && depthParameters.hydrophoneMaps.length > iSensor) {
				for (int i = 0; i < streamers.length; i++) {
					streamers[i].setSelected((depthParameters.hydrophoneMaps[iSensor] & 1<<i) != 0);
				}
			}
			
			scaleA.setText(String.format("%.3f", sp.scaleA));
			scaleB.setText(String.format("%.3f", sp.scaleB));
//			if (depthParameters.hydrophoneY != null && depthParameters.hydrophoneY.length > iSensor) {
//				yPosition.setText(String.format("%.1f", depthParameters.hydrophoneY[iSensor]));
//			}
		}
		
		MccSensorParameters getParams() {
			MccSensorParameters sp = mccDepthParameters.new MccSensorParameters();
			try {
				sp.iChan = Integer.valueOf(channel.getText());
				sp.scaleA = Double.valueOf(scaleA.getText());
				sp.scaleB = Double.valueOf(scaleB.getText());
//				depthParameters.hydrophoneY[iSensor] = Double.valueOf(yPosition.getText());
			}
			catch (NumberFormatException e) {
				return null;
			}
			depthParameters.hydrophoneMaps[iSensor] = 0;
			for (int i = 0; i < streamers.length; i++) {
				if (streamers[i].isSelected()) {
					depthParameters.hydrophoneMaps[iSensor] += 1<<i;
				}
			}
			return sp;
		}
	}
	
	class BoardPanel extends JPanel {
		
		private JComboBox boardList;
		
		private JTextField nSensors, readoutInterval;
		
		private JComboBox rangeList;

		private ArrayList<MCCBoardInfo> boardInfos;

		public BoardPanel() {
			super();
			setBorder(new TitledBorder("Select Measurement Device"));
			setLayout(new BorderLayout());
			JPanel p = new JPanel();
			p.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			c.gridwidth = 2;
			c.gridx = c.gridy = 0;
			c.fill = GridBagConstraints.HORIZONTAL;
			addComponent(p, boardList = new JComboBox(), c);
			c.gridx+=2;
			c.gridwidth = 1;
			addComponent(p, new JLabel(" Range"), c);
			c.gridx++;
			addComponent(p, rangeList = new JComboBox() , c);
			int[] ranges = MCCConstants.bipolarRanges;
			for (int i = 0; i < ranges.length; i++) {
				rangeList.addItem(MCCUtils.sayBibolarRange(ranges[i]));
			}
			rangeList.setToolTipText("Ensure that the device you are using supports the selected range");
			
			
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 1;
			addComponent(p, new JLabel("Number of sensors "), c);
			c.gridx = 1;
			addComponent(p, nSensors = new JTextField(3), c);
			c.gridx++;
			c.gridwidth = 2;
			addComponent(p, new JLabel(" (hit enter to update)"), c);
			nSensors.addActionListener(new HitEnter());

			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 1;
			addComponent(p, new JLabel("Readout interval "), c);
			c.gridx = 1;
			addComponent(p, readoutInterval = new JTextField(3), c);
			c.gridx++;
			c.gridwidth = 2;
			addComponent(p, new JLabel(" seconds"), c);
			
			boardList.setToolTipText("Select Measurement Computing Analogue input device");
			String rangeTip = "<html>Select voltage measurement range" +
			"<p>Note that many ranges are only available if the device is set up for differential input mode" +
			"<p>To set the input mode, use the MeasurementComputing Instacal utility" +
			"</html>";
			rangeList.setToolTipText(rangeTip);
			nSensors.setToolTipText("Set the number of depth sensors and hit 'enter'");
			readoutInterval.setToolTipText("Set the readout interval");
			
			this.add(BorderLayout.CENTER, p);
//			nSensors.
		}
		
		int getNumSensors() {
			try {
				int n = Integer.valueOf(nSensors.getText());
				return n;
			}
			catch (NumberFormatException e) {
				return 0;
			}
		}
		
		void setParams() {
			boardList.removeAllItems();
			boardInfos = MCCJNA.getBoardInformation();
			if (boardInfos != null) {
				for (int i = 0; i < boardInfos.size(); i++) {
					boardList.addItem(boardInfos.get(i).getBoardName());
				}
			}
			else {
				String title = "Error - missing analog input device";
				String msg = "<html><p>PAMGuard cannot find an analog input device.  Please ensure that you have a " +
				"device plugged into your computer, and you have the correct drivers installed.</p></html>";
				String help = null;
				int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.WARNING_MESSAGE, help);
				return;
			}
			if (mccDepthParameters.iBoard >= 0 &&
					mccDepthParameters.iBoard < boardInfos.size()) {
				boardList.setSelectedIndex(mccDepthParameters.iBoard);
			}
			nSensors.setText(String.format("%d", depthParameters.nSensors));
			readoutInterval.setText(String.format("%3.1f", depthParameters.pollTime));
			int ind = MCCUtils.getBipolarRangeIndex(mccDepthParameters.range);
			if (ind >= 0) {
				rangeList.setSelectedIndex(ind);
			}
		}
		
		boolean getParams() {
			mccDepthParameters.iBoard = boardList.getSelectedIndex();
			if (mccDepthParameters.iBoard < 0) {
				JOptionPane.showMessageDialog(getOwner(), "No Measurement Computing DAQ device has been selected", 
						"Warning", JOptionPane.DEFAULT_OPTION);
			}
			depthParameters.nSensors = getNumSensors();
			try {
				depthParameters.pollTime = Double.valueOf(readoutInterval.getText());
			}
			catch (NumberFormatException e) {
				return false;
			}
			mccDepthParameters.range = MCCConstants.bipolarRanges[rangeList.getSelectedIndex()];
			return (mccDepthParameters.iBoard >= 0);
		}
		private class HitEnter implements ActionListener {

			public void actionPerformed(ActionEvent arg0) {
				int numSensors;
				try {
					numSensors = Integer.valueOf(nSensors.getText());
				}
				catch (NumberFormatException e) {
					return;
				}
				sensorsPanel.setParams();
			}
			
		}
		
	}

}
