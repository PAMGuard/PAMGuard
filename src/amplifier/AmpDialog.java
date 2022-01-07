package amplifier;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamDetection.RawDataUnit;
import PamView.dialog.ChannelListScroller;
import PamView.dialog.PamDialog;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;

public class AmpDialog extends PamDialog {

	private static AmpDialog singleInstance;
	
	AmpParameters ampParameters;
	
	AmpControl ampControl;
	
	SourcePanel sourcePanel;
	
	JTextField[] ampGain = new JTextField[PamConstants.MAX_CHANNELS];
	
	JCheckBox[] invert = new JCheckBox[PamConstants.MAX_CHANNELS];
	
	JLabel[] labels = new JLabel[PamConstants.MAX_CHANNELS];
	
	private AmpDialog(Frame parentFrame) {
		super(parentFrame, "Amplifier Settings", true);
		
		sourcePanel = new SourcePanel(this, "Raw Data input", RawDataUnit.class, false, true);
		
		JPanel mainWindow = new JPanel();
		mainWindow.setLayout(new BorderLayout());
		mainWindow.add(BorderLayout.NORTH, sourcePanel.getPanel());
		
		JPanel channelWindow = new JPanel();
		channelWindow.setBorder(new TitledBorder("Channel Gains"));
		channelWindow.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.ipadx = 10;
		addComponent(channelWindow, new JLabel("   Channel   "), c);
		c.gridx++;
		addComponent(channelWindow, new JLabel("   Gain (dB)   "), c);
		c.gridx++;
		addComponent(channelWindow, new JLabel("   invert   "), c);
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			c.gridy++;
			c.gridx = 0;
			addComponent(channelWindow, labels[i] = new JLabel(String.format(" %d ", i)), c);
			c.gridx++;
			addComponent(channelWindow, ampGain[i] = new JTextField(5), c);
			c.gridx++;
			addComponent(channelWindow, invert[i] = new JCheckBox(), c);
		}
		
		mainWindow.add(BorderLayout.CENTER, new ChannelListScroller(channelWindow));
		
		setDialogComponent(mainWindow);
	}

	public static AmpParameters showDialog(Frame parentFrame, 
			AmpParameters ampParameters, AmpControl ampControl) {
		
		if (singleInstance == null || parentFrame != singleInstance.getOwner()) {
			singleInstance = new AmpDialog(parentFrame);
		}
		singleInstance.ampParameters = ampParameters.clone();
		singleInstance.ampControl = ampControl;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.ampParameters;
	}
	
	@Override
	public void cancelButtonPressed() {

		ampParameters = null;

	}
	
	private void setParams() {
		
		PamDataBlock sourceData = PamController.getInstance().getRawDataBlock(ampParameters.rawDataSource);
		sourcePanel.clearExcludeList();
		sourcePanel.excludeDataBlock(ampControl.ampProcess.getOutputDataBlock(0), true);
		sourcePanel.setSource(sourceData);
		
		double absVal;
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			absVal = Math.abs(ampParameters.gain[i]);
			if (absVal == 0) absVal = 1;
			ampGain[i].setText(String.format("%.1f", 20 * Math.log10(absVal)));
			invert[i].setSelected(ampParameters.gain[i] < 0);
		}
		showRows();
	}

	@Override
	public boolean getParams() {

		PamDataBlock inputData = sourcePanel.getSource();
		ArrayList<PamDataBlock> rawDatas = PamController.getInstance().getRawDataBlocks();
		ampParameters.rawDataSource = rawDatas.indexOf(inputData);
		
		double dbVal, linVal;
		try {
			for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
				dbVal = Double.valueOf(ampGain[i].getText());
				linVal = Math.pow(10., dbVal / 20.);
				if (invert[i].isSelected()) {
					linVal *= -1;
				}
				ampParameters.gain[i] = linVal;
			}
		}
		catch (NumberFormatException ex) {
			return false;
		}
		return true;
	}

	private void showRows() {

		int availableChannels = 3;
		PamDataBlock sourceData = sourcePanel.getSource();
		if (sourceData != null) {
			availableChannels = sourceData.getChannelMap();
		}
		boolean e;
		for (int in = 0; in < PamConstants.MAX_CHANNELS; in++) {
			e = ((1<<in & availableChannels) != 0);
			labels[in].setVisible(e);
			ampGain[in].setVisible(e);
			invert[in].setVisible(e);
		}
		invalidate();
		pack();
	}
	
	@Override
	public void restoreDefaultSettings() {

		ampParameters = new AmpParameters();
		
		setParams();

	}

}
