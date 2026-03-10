package ltsa;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;
import fftManager.FFTDataUnit;

public class LtsaDialog extends PamDialog {

	private static LtsaDialog singleInstance;
	
	private LtsaParameters ltsaParameters;
	
	private SourcePanel sourcePanel;
	
	private JTextField interval;
	
	private LtsaControl ltsaControl;
	
	private LtsaDialog(Window parentFrame, LtsaControl ltsaControl) {
		super(parentFrame, "LTSA Settings", false);
		this.ltsaControl = ltsaControl;
		sourcePanel = new SourcePanel(this, "FFT Data source", FFTDataUnit.class, true, true);
		sourcePanel.excludeDataBlock(ltsaControl.ltsaProcess.getLtsaDataBlock(), true);
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.NORTH, sourcePanel.getPanel());
		
		JPanel southPanel = new JPanel(new GridBagLayout());
		southPanel.setBorder(new TitledBorder("Measurement"));
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(southPanel, new JLabel("Measurement interval ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(southPanel, interval = new JTextField(6), c);
		c.gridx++;
		addComponent(southPanel, new JLabel(" seconds", SwingConstants.LEFT), c);
		mainPanel.add(BorderLayout.SOUTH, southPanel);
		
		setDialogComponent(mainPanel);
		setHelpPoint("sound_processing.LTSA.Docs.LTSA");
	}
	
	public static LtsaParameters showDialog(Frame frame, LtsaControl ltsaControl, LtsaParameters ltsaParameters) {
		if (singleInstance == null || singleInstance.getOwner() != frame) {
			singleInstance = new LtsaDialog(frame, ltsaControl);
		}
		singleInstance.ltsaParameters = ltsaParameters.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.ltsaParameters;
	}

	private void setParams() {
		sourcePanel.setSource(ltsaParameters.dataSource);
		sourcePanel.setChannelList(ltsaParameters.channelMap);
		interval.setText(String.format("%d", ltsaParameters.intervalSeconds));
	}

	@Override
	public boolean getParams() {
		PamDataBlock dataBlock = sourcePanel.getSource();
		if (dataBlock != null) {
			ltsaParameters.dataSource = dataBlock.getLongDataName();
		}
		else {
			return showWarning("No data source selected");
		}
		ltsaParameters.channelMap = sourcePanel.getChannelList();
		try {
			ltsaParameters.intervalSeconds = Integer.valueOf(interval.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid interval");
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		ltsaParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
