package clickDetector;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import fftManager.FFTLengthModel;
import fftManager.FFTLengthModeled;

public class WignerPlotdialog extends PamDialog implements FFTLengthModeled {

	private static WignerPlotdialog singleInstance;
	private WignerPlotOptions wignerPlotOptions;
	
	JCheckBox limitLength;
	private FFTLengthModel fftLengthModel;
	private JSpinner fftLengthSpinner;
	private JTextField fftLengthData;
	
	private WignerPlotdialog(Window parentFrame, Point location) {
		super(parentFrame, "Wigner Plot Options", false);
		
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		p.setBorder(new TitledBorder("Transform Length"));
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 3;
		addComponent(p, limitLength = new JCheckBox("Limit transform length around peak"), c);
		limitLength.addActionListener(new LimitListener());
		
		c.gridwidth = 1;
		c.gridy++;
		c.gridx = 0;
		addComponent(p, new JLabel("Transform length  "), c);
		fftLengthModel = new FFTLengthModel(this);
		fftLengthSpinner = new JSpinner(fftLengthModel);
		fftLengthData = new JTextField(4);
		fftLengthSpinner.setEditor(fftLengthData);
		c.gridx++;
		addComponent(p, fftLengthSpinner, c);
		c.gridx++;
		addComponent(p, new JLabel(" bins"), c);
		setDialogComponent(p);
		setLocation(location);
	}
	
	public static WignerPlotOptions showDialog(Frame frame, Point location, WignerPlotOptions wignerPlotOptions) {
		if (singleInstance == null || singleInstance.getOwner() != frame) {
			singleInstance = new WignerPlotdialog(frame, location);
		}
		singleInstance.wignerPlotOptions = wignerPlotOptions.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.wignerPlotOptions;
	}

	private void setParams() {
		limitLength.setSelected(wignerPlotOptions.limitLength);
		setFFTLength(wignerPlotOptions.manualLength);
		enableControls();
	}
	
	private void enableControls() {
		fftLengthSpinner.setEnabled(limitLength.isSelected());
	}
	
	class LimitListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}
	}

	@Override
	public int getFFTLength() {
		try {
			return Integer.valueOf(fftLengthData.getText());
		}
		catch (NumberFormatException e) {
			return 1;
		}
	}

	@Override
	public void setFFTLength(int fftLength) {
		fftLengthData.setText(String.format("%d",fftLength));
	}

	@Override
	public void cancelButtonPressed() {
		wignerPlotOptions = null;
	}

	@Override
	public boolean getParams() {
		wignerPlotOptions.limitLength = limitLength.isSelected();
		wignerPlotOptions.manualLength = getFFTLength();
		return wignerPlotOptions.manualLength > 1;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
