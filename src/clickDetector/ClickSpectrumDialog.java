package clickDetector;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class ClickSpectrumDialog extends PamDialog {

	private static ClickSpectrumDialog singleInstance;
	
	private ClickSpectrumParams clickSpectrumParams;
	
	private ClickSpectrum clickSpectrum;
	
	private JCheckBox logScale;
	
	private JTextField logRange;
	
	private JCheckBox showEventInfo;

	private JComboBox channelChoice;

	private JCheckBox smoothData;
	
	private JRadioButton plotSpectrogram, plotCepstrum;
	
	private JTextField smoothBins;
	
	boolean isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
	
	private ClickSpectrumDialog(Window parentFrame, Point pt) {
		super(parentFrame, "Spectrogram Display", false);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		
		
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("Plot Type"));
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 3;
		addComponent(p, plotSpectrogram = new JRadioButton("Show Spectrogram"), c);
		c.gridy++;
		addComponent(p, plotCepstrum = new JRadioButton("Show Cepstrum"), c);
		ButtonGroup bg = new ButtonGroup();
		bg.add(plotCepstrum);
		bg.add(plotSpectrogram);
		mainPanel.add(p);
		
		p = new JPanel();
		p.setBorder(new TitledBorder("Scale"));
		p.setLayout(new GridBagLayout());
		c = new PamGridBagContraints();
		c.gridwidth = 3;
		addComponent(p, logScale = new JCheckBox("Log Scale"), c);
		logScale.addActionListener(new LogScale());
		c.gridx = 0;
		c.gridwidth = 1;
		c.gridy++;
		addComponent(p, new JLabel("Scale range "), c);
		c.gridx++;
		addComponent(p, logRange = new JTextField(3), c);
		c.gridx++;
		addComponent(p, new JLabel(" dB"), c);
		mainPanel.add(p);
		
		JPanel r = new JPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		r.setBorder(new TitledBorder("Event Info"));
		c.gridwidth = 3;
		addComponent(r, showEventInfo = new JCheckBox("Show Event Average"), c);
		showEventInfo.addActionListener(new ShowEventInfo());
		c.gridy++;
		if (isViewer==true){
		mainPanel.add(r);
		}
		
		JPanel q = new JPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		q.setBorder(new TitledBorder("Options"));
		addComponent(q, new JLabel("Channels "), c);
		c.gridx++;
		c.gridwidth = 4;
		addComponent(q, channelChoice = new JComboBox(), c);
		channelChoice.addItem("Show individual channels");
		channelChoice.addItem("Show overall means");
		c.gridx = 0;
		c.gridwidth = 2;
		c.gridy ++;
		addComponent(q, smoothData = new JCheckBox("Smooth data with"), c);
		smoothData.addActionListener(new SmoothPlot());
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		addComponent(q, smoothBins = new JTextField(2), c);
		c.gridx ++;
		addComponent(q, new JLabel(" bin filter"), c);
		mainPanel.add(q);
		
		
		setDialogComponent(mainPanel);
		if (pt != null) {
			setLocation(pt);
		}
	}
	
	public static ClickSpectrumParams showDialog(Window frame, Point pt, ClickSpectrum clickSpectrum, 
			ClickSpectrumParams clickSpectrumParams) {
		if (singleInstance == null || clickSpectrum != singleInstance.clickSpectrum) {
			singleInstance = new ClickSpectrumDialog(frame, pt);
		}
		singleInstance.clickSpectrumParams = clickSpectrumParams.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.clickSpectrumParams;
	}

	private void setParams() {
		plotSpectrogram.setSelected(clickSpectrumParams.plotCepstrum == false);
		plotCepstrum.setSelected(clickSpectrumParams.plotCepstrum == true);
		logScale.setSelected(clickSpectrumParams.logScale);
		logRange.setText(String.format("%3.1f", clickSpectrumParams.logRange));
		channelChoice.setSelectedIndex(clickSpectrumParams.channelChoice);
		smoothData.setSelected(clickSpectrumParams.smoothPlot);
		smoothBins.setText(String.format("%d", clickSpectrumParams.plotSmoothing));
		showEventInfo.setSelected(clickSpectrumParams.showEventInfo);
		enableControls();
		
	}

	@Override
	public boolean getParams() {
		clickSpectrumParams.plotCepstrum = plotCepstrum.isSelected();
		clickSpectrumParams.logScale = logScale.isSelected();
		try {
			clickSpectrumParams.logRange = Double.valueOf(logRange.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid range value");
		}
		clickSpectrumParams.logRange = Math.abs(clickSpectrumParams.logRange);
		if (clickSpectrumParams.logRange == 0) {
			return showWarning("The Scale range must be greater than zero");
		}
		
		clickSpectrumParams.channelChoice = channelChoice.getSelectedIndex();
		if (clickSpectrumParams.smoothPlot = smoothData.isSelected() == true) {
			try {
				clickSpectrumParams.plotSmoothing = Integer.valueOf(smoothBins.getText());
			}
			catch(NumberFormatException e) {
				return showWarning("Invalid smoothing constant");
			}
			if (clickSpectrumParams.plotSmoothing%2 == 0 || clickSpectrumParams.plotSmoothing <= 0) {
				return showWarning("The Smoothing constant must be a positive odd integer");
			}
		}
		
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		clickSpectrumParams = null;
	}

	private void enableControls() {
		logRange.setEnabled(logScale.isSelected());
		smoothBins.setEnabled(smoothData.isSelected());
	}
	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	private class LogScale implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}
	}
	
	private class ShowEventInfo implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			clickSpectrumParams.showEventInfo=showEventInfo.isSelected();
			
		}
	}
	private class SmoothPlot implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}
	}

}
