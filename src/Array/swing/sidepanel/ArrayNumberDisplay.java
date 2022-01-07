package Array.swing.sidepanel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import Array.ArrayManager;
import Array.PamArray;
import Array.Streamer;
import Array.sensors.ArrayDisplayParameters;
import Array.sensors.ArrayDisplayParamsProvider;
import Array.sensors.ArrayParameterType;
import Array.sensors.ArraySensorFieldType;
import GPS.GpsData;
import PamController.PamController;
import PamUtils.LatLong;
import PamUtils.PamUtils;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.panel.PamAlignmentPanel;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataBlock;

public class ArrayNumberDisplay extends ArrayDisplay {
	
	private PamAlignmentPanel mainPanel;
	private PamPanel innerPanel;
	private int numStreamers;
	private StreamerPanel[] streamerPanels;
	private ArrayDisplayParamsProvider paramsProvider;

	public ArrayNumberDisplay(ArrayDisplayParamsProvider paramsProvider) {
		this.paramsProvider = paramsProvider;
		innerPanel = new PamPanel(new BorderLayout());
		mainPanel = new PamAlignmentPanel(innerPanel, BorderLayout.WEST);
	}

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}

	@Override
	public synchronized void updateData(int iStreamer, long timeMilliseconds, GpsData gpsData) {
		if (streamerPanels == null) {
			return;
		}
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		int numStreamers = array.getNumStreamers();
//		int n = Math.min(numStreamers, streamerPanels.length);
//		int iStreamer = PamUtils.getSingleChannel(streamerMap);
//		if (iStreamer >= 0 && iStreamer < streamerPanels.length) {
//			Streamer streamer = array.getStreamer(i);
			streamerPanels[0].update(iStreamer, gpsData);
//		}
		
	}

	@Override
	public synchronized void updateLayout(int streamerMap) {
		int numStreamers = PamUtils.getNumChannels(streamerMap);
		if (this.numStreamers == numStreamers) {
			return;
		}
		this.numStreamers = numStreamers;
		
		innerPanel.removeAll();
		innerPanel.setLayout(new BorderLayout());
		streamerPanels = new StreamerPanel[1];
//		for (int i = 0; i < numStreamers; i++) {
			streamerPanels[0] = new StreamerPanel(streamerMap);
			innerPanel.add(BorderLayout.CENTER, streamerPanels[0]);
//		}
	}

	/**
	 * Constrain angle to selected range
	 * @param head
	 * @return
	 */
	private double constrianAngle(double head) {
		if (paramsProvider.getDisplayParameters().getHeadRange() == ArrayDisplayParameters.HEAD_0_360) {
			head = PamUtils.constrainedAngle(head, 360);
		}
		else {
			head = PamUtils.constrainedAngle(head, 180);
		}
		return head;
	}
	
	private class TippedTextField extends JTextField {
		
		private Streamer streamer;
		private ArraySensorFieldType fieldType;

		public TippedTextField(Streamer streamer, ArraySensorFieldType fieldType, int width) {
			super(width);
			this.streamer = streamer;
			this.fieldType = fieldType;
			setToolTipText(fieldType.toString());
		}

		@Override
		public String getToolTipText(MouseEvent e) {
			return makeTip();
		}
		
		private String makeTip() {
			if (streamer == null) {
				return null;
			}
			ArrayParameterType oType = streamer.getOrientationTypes(fieldType);
			switch (oType) {
			case DEFAULT:
				return "Using default from array location data";
			case FIXED:
				return "Using fixed value from array manager";
			case SENSOR:
				String sensDB = streamer.getSensorDataBlocks(fieldType);
				PamDataBlock dB = PamController.getInstance().getDataBlockByLongName(sensDB);
				if (dB != null) {
					return "Using sensor data from " + dB.getDataName();
				}
				else {
					// happens if display added before sensor module. 
					return "Using sensor data from " + sensDB;
				}
			default:
				return null;			
			}
		}
	}

	private class StreamerPanel extends PamPanel {
		
		private TippedTextField[] depth, heading, pitch, roll;
		private int streamerMap;
		private int nStreamers;
		private static final int fieldWidth = 4;

		public StreamerPanel(int streamerMap) {
			super(new GridBagLayout());
			this.streamerMap = streamerMap;
			nStreamers = PamUtils.getNumChannels(streamerMap);
//			if (nStreamers == 1) {
//				mainPanel.setBorder(new TitledBorder("Streamer " + PamUtils.getSingleChannel(streamerMap)));
//			}
//			else {
//				mainPanel.setBorder(new TitledBorder("Streamers"));
//			}
			GridBagConstraints c = new PamGridBagContraints();
			depth = new TippedTextField[nStreamers];
			heading = new TippedTextField[nStreamers];
			pitch = new TippedTextField[nStreamers];
			roll = new TippedTextField[nStreamers];
			if (nStreamers > 1) {
				c.gridx = 1;
				for (int i = 0; i < nStreamers; i++) {
					int ind = PamUtils.getNthChannel(i, streamerMap);
					JLabel lab;
					add(lab = new PamLabel("S"+i, JLabel.CENTER), c);
					String name = "";
					try {
						name = ArrayManager.getArrayManager().getCurrentArray().getStreamer(ind).getStreamerName();
					}
					catch (Exception e) {
						
					}
					name = String.format("Streamer %d: %s", ind, name);
					lab.setToolTipText(name);
					
					c.gridx++;
				}
				c.gridy++;
			}
			int y0 = c.gridy;
			// do all the left labels.
			c.gridx = 0;
			String zName = PamController.getInstance().getGlobalMediumManager().getZString();
			add(new PamLabel(zName, JLabel.RIGHT), c);
			c.gridy++;
			add(new PamLabel("Pitch ", JLabel.RIGHT), c);
			c.gridy++;
			add(new PamLabel("Roll ", JLabel.RIGHT), c);
			c.gridy++;
			add(new PamLabel("Head ", JLabel.RIGHT), c);
			for (int i = 0; i < nStreamers; i++) {
				Streamer streamer = ArrayManager.getArrayManager().getCurrentArray().getStreamer(i);
				c.gridy = y0;
				c.gridx++;
				add(depth[i] = new TippedTextField(streamer, ArraySensorFieldType.HEIGHT, fieldWidth), c);
				c.gridy++;
				add(pitch[i] = new TippedTextField(streamer, ArraySensorFieldType.PITCH, fieldWidth), c);
				c.gridy++;
				add(roll[i] = new TippedTextField(streamer, ArraySensorFieldType.ROLL, fieldWidth), c);
				c.gridy++;
				add(heading[i] = new TippedTextField(streamer, ArraySensorFieldType.HEADING, fieldWidth), c);
				c.gridy++;
				heading[i].setEditable(false);
				depth[i].setEditable(false);
				pitch[i].setEditable(false);
				roll[i].setEditable(false);
			}
			// and the units
			c.gridy = y0;
			c.gridx++;
			add(new PamLabel("m", JLabel.LEFT), c);
			c.gridy++;
			add(new PamLabel(LatLong.deg, JLabel.LEFT), c);
			c.gridy++;
			add(new PamLabel(LatLong.deg, JLabel.LEFT), c);
			c.gridy++;
			add(new PamLabel(LatLong.deg, JLabel.LEFT), c);
			

		}

		
		public void update(int iStreamer, GpsData gpsData) {
			if (iStreamer >= heading.length) {
				return;
			}
			updateField(heading[iStreamer], constrianAngle(gpsData.getHeading()));
			updateField(depth[iStreamer], -gpsData.getHeight());
			updateField(pitch[iStreamer], gpsData.getPitch());
			updateField(roll[iStreamer], gpsData.getRoll());
			
		}
		
		public void updateField(JTextField field, Double value) {
			if (value == null) {
				field.setText(" - ");
			}
			else {
				field.setText(String.format("%3.1f", value));
			}
		}
		
	}
}
