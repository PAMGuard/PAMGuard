package clickDetector;

import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.ColourArray;
import PamView.ColourArray.ColourArrayType;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class ConcatenatedSpectrogramdialog  extends PamDialog {
	
	private JPanel mainPanel;
	private static ConcatenatedSpectrogramdialog singleInstance;
	private ConcatenatedSpectParams concatenatedSpectParams;
	private JComboBox colourList;
	private JCheckBox logScale;
	private JTextField logRange;
	private JCheckBox normalise;
	

	
	private ConcatenatedSpectrogramdialog(Window parentFrame, Point location) {
		super(parentFrame, "Concatenated Spectrogram Options", false);
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JPanel p=new JPanel();
		p.setBorder(new TitledBorder("Colour"));
		p.add( colourList = new JComboBox());
		ColourArrayType[] types = ColourArray.ColourArrayType.values();
		for (int i = 0; i < ColourArray.ColourArrayType.values().length; i++) {
			colourList.addItem(ColourArray.getName(types[i]));
		}
		colourList.addActionListener(new ColourSet());
	
		JPanel r = new JPanel(new GridBagLayout());
		PamGridBagContraints c = new PamGridBagContraints();
		r.setBorder(new TitledBorder("Amplitude Scale"));
		c.gridwidth = 3;
		addComponent(r, logScale = new JCheckBox("Log (Decibel) scale"), c);
		logScale.addActionListener(new Log3DScale());
		c.gridx = 0;
		c.gridwidth = 1;
		c.gridy++;
		addComponent(r, new JLabel("Scale range "), c);
		c.gridx++;
		addComponent(r, logRange = new JTextField(3), c);
		c.gridx++;
		addComponent(r, new JLabel(" dB"), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 3;
		addComponent(r, normalise = new JCheckBox("Normalise Amplitudes"), c);
		
//		JPanel q=new  JPanel(new GridBagLayout());
//		c = new PamGridBagContraints();
//		q.setBorder(new TitledBorder("Amplitudes"));
//		c.gridwidth = 3;
//		addComponent(q, normalise = new JCheckBox("Normalise Amplitudes"), c);
//		normalise.addActionListener(new Normalise());
//		c.gridy++;
		
		mainPanel.add(r);
		mainPanel.add(p);
//		mainPanel.add(q);

		normalise.setToolTipText("Normalise amplitude of all click spectra");
		logScale.setToolTipText("Use log (Decibel); amplitude scale");
		logRange.setToolTipText("Amplitude range for Decibel scale");
		
		setDialogComponent(mainPanel);
		setLocation(location);
		
	}
	
	public static ConcatenatedSpectParams showDialog(Window frame, Point pt,ConcatenatedSpectParams concatenatedSpectParams){
		
//			if (singleInstance == null || singleInstance.getOwner() != frame) {
				singleInstance = new ConcatenatedSpectrogramdialog(frame, pt);
//			}
			singleInstance.concatenatedSpectParams = concatenatedSpectParams.clone();
			singleInstance.setParams();
			singleInstance.setVisible(true);
			return singleInstance.concatenatedSpectParams;
	}
	
	public void setParams() {
		colourList.setSelectedIndex(concatenatedSpectParams.colourMap.ordinal());
		logScale.setSelected(concatenatedSpectParams.logVal);
		logRange.setText(String.format("%3.1f",concatenatedSpectParams.maxLogValS));
		logRange.setEnabled(logScale.isSelected());
		normalise.setSelected(concatenatedSpectParams.normaliseAll);
	}
	
	private class Log3DScale implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			concatenatedSpectParams.logVal=logScale.isSelected();
			logRange.setEnabled(logScale.isSelected());
		}
	}
	
	private class Normalise implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			concatenatedSpectParams.normaliseAll=normalise.isSelected();
		}
	}
	
	private class ColourSet implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			concatenatedSpectParams.setColourMap(ColourArrayType.values()[colourList.getSelectedIndex()]);
		}
	}

	@Override
	public boolean getParams() {
		
		try {
			concatenatedSpectParams.maxLogValS = Math.abs(Double.valueOf(logRange.getText()));
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid range value");
		}
		if (concatenatedSpectParams.maxLogValS <= 0) {
			return showWarning("The Scale range must be greater than zero");
		}
		
		concatenatedSpectParams.normaliseAll = normalise.isSelected();
		
		
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		concatenatedSpectParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}
	

	

	
}
