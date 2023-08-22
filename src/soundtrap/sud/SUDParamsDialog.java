package soundtrap.sud;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.pamguard.x3.sud.SUDClickDetectorInfo;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import soundtrap.STClickControl;

/**
 * Display ST detection parameters. These cannot be changed so all boxes are disabled. 
 * @author dg50
 *
 */
public class SUDParamsDialog extends PamDialog {

	private STClickControl stClickControl;
	
	private JTextField sampleRate, channels, threshold, blanking, preSamps, postSamps, len;
	
	private static SUDParamsDialog singleInstance;

	private SUDParamsDialog(Window parentFrame, STClickControl stClickControl) {
		super(parentFrame, stClickControl.getUnitName(), false);
		this.stClickControl = stClickControl;
		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("SoundTrap Detector Configuration"));
		GridBagConstraints c = new PamGridBagContraints();
		sampleRate = addThing(mainPanel, "Sample Rate", "Hz", c);
		channels = addThing(mainPanel, "Num Channels", null, c);
		threshold = addThing(mainPanel, "Threshold", "dB", c);
		blanking = addThing(mainPanel, "Blanking", "samples", c);
		preSamps = addThing(mainPanel, "Pre Samples", "samples", c);
		postSamps = addThing(mainPanel, "Post Samples", "samples", c);
		len = addThing(mainPanel, "Length", "samples", c);
		
		setDialogComponent(mainPanel);
	}
	
	public static void showDialog(Window parent, STClickControl stClickControl) {
//		if (singleInstance == null) {
			singleInstance = new SUDParamsDialog(parent, stClickControl);
//		}
		singleInstance.setParams(stClickControl.getSudClickDetectorInfo());
		singleInstance.setVisible(true);
		return;
	}

	private void setParams(SUDClickDetectorInfo sudClickDetectorInfo) {
		sampleRate.setText(String.format("%d", sudClickDetectorInfo.sampleRate));
		channels.setText(String.format("%d", sudClickDetectorInfo.nChan));
		threshold.setText(String.format("%3.1f", sudClickDetectorInfo.detThr));
		blanking.setText(String.format("%d", sudClickDetectorInfo.blankingSamples));
		preSamps.setText(String.format("%d", sudClickDetectorInfo.preSamples));
		postSamps.setText(String.format("%d", sudClickDetectorInfo.postSamples));
		len.setText(String.format("%d", sudClickDetectorInfo.lenSamples));
	}

	private JTextField addThing(JPanel mainPanel, String title, String postTit, GridBagConstraints c) {
		c.gridx = 0;
		mainPanel.add(new JLabel(title, JLabel.RIGHT), c);
		c.gridx++;
		STField field = new STField(5);
		mainPanel.add(field, c);
		if (postTit != null) {
			c.gridx++;
			mainPanel.add(new JLabel(postTit, JLabel.LEFT), c);
		}
		c.gridy++;
		return field;
	}

	@Override
	public boolean getParams() {
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	private class STField extends JTextField {

		public STField(int columns) {
			super(columns);
			setEditable(false);
		}
		
	}

}
