package noiseOneBand;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;

public class OneBandDisplayDialog extends PamDialog {

	private OneBandControl dbhtControl;
	private static OneBandDisplayDialog singleInstance;
	private OneBandDisplayParams params;
	
	private JCheckBox autoScale;
	private JTextField minAmp, maxAmp;
	private JCheckBox showGrid;
	
	private JCheckBox drawLines;
	private JCheckBox colourByChannel;
	private JTextField symbolSize;
	private JCheckBox[] showMeasure = new JCheckBox[OneBandControl.NMEASURES];
	private JCheckBox[] showChannel = new JCheckBox[PamConstants.MAX_CHANNELS];
	
	
	private OneBandDisplayDialog(Window parentFrame, OneBandControl dbhtControl) {
		super(parentFrame, dbhtControl.getUnitName() + " Display Options", true);
		this.dbhtControl = dbhtControl;
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		JTabbedPane tabPane = new JTabbedPane();
		mainPanel.add(tabPane);
		
		JPanel scalePanel = new JPanel(new GridBagLayout());
		scalePanel.setBorder(new TitledBorder("Amlitude Scale"));
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 3;
		addComponent(scalePanel, showGrid = new JCheckBox("Show Grid"), c);
		c.gridy++;
		addComponent(scalePanel, autoScale = new JCheckBox("Auto Scale"), c);
		autoScale.addActionListener(new EnableAction());
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy++;
		addComponent(scalePanel, new JLabel("Min ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(scalePanel, minAmp = new JTextField(5), c);
		c.gridx++;
		addComponent(scalePanel, new JLabel(" dB re 1\u03BCPa ", SwingConstants.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(scalePanel, new JLabel("Max ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(scalePanel, maxAmp = new JTextField(5), c);
		c.gridx++;
		addComponent(scalePanel, new JLabel(" dB re 1\u03BCPa ", SwingConstants.LEFT), c);
		
		tabPane.add(scalePanel, "Scale");
		
		JPanel symPanel = new JPanel(new GridBagLayout());
		symPanel.setBorder(new TitledBorder("Symbols"));
		c = new PamGridBagContraints();
		c.gridwidth = 3;
		addComponent(symPanel, drawLines = new JCheckBox("Draw Lines"), c);
		c.gridy++;
		addComponent(symPanel, colourByChannel = new JCheckBox("Colour By Channel"), c);
		c.gridwidth = 1;
		c.gridy++;
		addComponent(symPanel, new JLabel("Symbol Size ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(symPanel, symbolSize = new JTextField(5), c);
		c.gridx++;
		addComponent(symPanel, new JLabel(" pixels ", SwingConstants.LEFT), c);
		c.gridwidth = 3;
		for (int i = 0; i < OneBandControl.NMEASURES; i++) {
			c.gridx = 0;
			c.gridy++;
			addComponent(symPanel, showMeasure[i] = new JCheckBox("Show " + OneBandControl.measureNames[i]), c);
		}
		tabPane.add(symPanel, "Symbols");
		
		JPanel channelPanel = new JPanel();
		channelPanel.setLayout(new BoxLayout(channelPanel, BoxLayout.Y_AXIS));
		channelPanel.setBorder(new TitledBorder("Channels"));
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			showChannel[i] = new JCheckBox("Channel " + i);
			channelPanel.add(showChannel[i]);
		}
		tabPane.add(channelPanel, "Channels");
		
		
		setDialogComponent(mainPanel);
	}
	
	public static OneBandDisplayParams showDialog(Window frame, OneBandControl oneBandControl, OneBandDisplayParams oneBandDisplayParams) {
		if (singleInstance == null || singleInstance.getOwner() != frame || singleInstance.dbhtControl != oneBandControl) {
			singleInstance = new OneBandDisplayDialog(frame, oneBandControl);
		}
		singleInstance.params = oneBandDisplayParams.clone();
		singleInstance.setParams();
		singleInstance.pack();
		singleInstance.setVisible(true);
		return singleInstance.params;
	}

	@Override
	public void cancelButtonPressed() {
		params = null;
	}

	private void enableControls() {
		boolean e = (autoScale.isSelected() == false);
		minAmp.setEnabled(e);
		maxAmp.setEnabled(e);
		if (PamController.getInstance().getRunMode() == PamController.RUN_NETWORKRECEIVER) {
			drawLines.setEnabled(false);
			drawLines.setSelected(false);
		}
	}

	private void setParams() {
		showGrid.setSelected(params.showGrid);
		autoScale.setSelected(params.autoScale);
		minAmp.setText(new Double(params.minAmplitude).toString());
		maxAmp.setText(new Double(params.maxAmplitude).toString());
		drawLines.setSelected(params.drawLine);
		colourByChannel.setSelected(params.colourByChannel);
		symbolSize.setText(new Integer(params.symbolSize).toString());
		for (int i = 0; i < OneBandControl.NMEASURES; i++) {
			showMeasure[i].setSelected(((1<<i) & params.showWhat) != 0);
		}
		PamDataBlock source = dbhtControl.getOneBandProcess().getSourceDataBlock();
		int availChan = 0;
		if (source != null) {
			availChan = source.getChannelMap();
		}
		int selChan = params.getDisplayChannels(availChan);
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			showChannel[i].setVisible((1<<i & availChan) != 0);
			showChannel[i].setSelected((1<<i & selChan) != 0);
		}
		
		enableControls();
	}

	@Override
	public boolean getParams() {
		params.showGrid = showGrid.isSelected();
		params.autoScale = autoScale.isSelected();
		params.drawLine = drawLines.isSelected();
		params.colourByChannel = colourByChannel.isSelected();
		params.showWhat = 0;
		for (int i = 0; i < OneBandControl.NMEASURES; i++) {
			if (showMeasure[i].isSelected()) {
				params.showWhat |= 1<<i;
			}
		}
		try {
			params.minAmplitude = Double.valueOf(minAmp.getText());
			params.maxAmplitude = Double.valueOf(maxAmp.getText());
			params.symbolSize = Integer.valueOf(symbolSize.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Number format error");
		}
		int selChan = 0;
		for (int i = 0; i < showChannel.length; i++) {
			if (showChannel[i].isVisible() == false) {
				continue;
			}
			if (showChannel[i].isSelected()) {
				selChan |= (1<<i);
			}
		}
		params.setDisplayChannels(selChan);
		
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		params = new OneBandDisplayParams();
		setParams();
	}

	/**
	 * @return the dbhtControl
	 */
	public OneBandControl getDbhtControl() {
		return dbhtControl;
	}

	private class EnableAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}
	}
}
