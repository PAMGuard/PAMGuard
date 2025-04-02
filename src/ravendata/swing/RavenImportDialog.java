package ravendata.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamAlignmentPanel;
import ravendata.RavenParameters;

public class RavenImportDialog extends PamDialog {
	
	private static RavenImportDialog singleInstance;
	private RavenParameters ravenParameters;
	
	private JTextField ravenFile;
	
	private JButton chooseButton;
	
	private JTextField timeOffset;


	private RavenImportDialog(Window parentFrame) {
		super(parentFrame, "Import Raven Data", false);
		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Choose Raven selection table"));
		GridBagConstraints c = new PamGridBagContraints();
		ravenFile = new JTextField(80);
		ravenFile.setEditable(false);
		chooseButton = new JButton("Select ...");
		
		c.gridwidth = 2;
		c.gridx = c.gridy = 0;
		mainPanel.add(ravenFile, c);
		
		JPanel p2 = new JPanel(new GridBagLayout());
		GridBagConstraints c2 = new PamGridBagContraints();
		c2.gridx = 1;
		c2.gridwidth = 1;
		p2.add(chooseButton, c2);
		
		c2.gridx = 0;
		c2.gridy++;
		c2.gridwidth = 1;
		p2.add(new JLabel("Time offset (s) ", JLabel.RIGHT), c2);
		c2.gridx++;
		p2.add(timeOffset = new JTextField(7), c2);
		String tip = "Added to data as it's read from file";
		timeOffset.setToolTipText(tip);
		
		c.gridwidth = 2;
		c.gridy++;
		mainPanel.add(new PamAlignmentPanel(p2, BorderLayout.EAST), c);
		
		
		chooseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chooseFile(e);
			}
		});
		setDialogComponent(mainPanel);
	}

	protected void chooseFile(ActionEvent e) {
		PamFileFilter fileFilter = new PamFileFilter("Raven files", ".txt");
//		fileFilter.
		PamFileChooser chooser = new PamFileChooser(ravenParameters.importFile);
		chooser.setFileFilter(fileFilter);
		int ans = chooser.showDialog(this, "Select ...");
		if (ans == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			if (f != null) {
				ravenFile.setText(f.getAbsolutePath());
			}
		}
	}

	public static RavenParameters showDialog(Window parentFrame, RavenParameters ravenParameters) {
//		if (singleInstance == null) {
			singleInstance = new RavenImportDialog(parentFrame);
//		}
		singleInstance.setParams(ravenParameters);
		singleInstance.setVisible(true);
		return singleInstance.ravenParameters;
	}
	
	private void setParams(RavenParameters ravenParameters) {
		this.ravenParameters = ravenParameters;
		ravenFile.setText(ravenParameters.importFile);
		timeOffset.setText(String.format("%5.3f", ravenParameters.timeOffsetSeconds));
	}

	@Override
	public boolean getParams() {
		String fn = ravenFile.getText();
		if (fn == null) {
			return showWarning("Error - No file selected");
		}
		File f = new File(fn);
		if (f.exists() == false) {
			String str = String.format("The file %s does not exist", fn);
			return showWarning(str);
		}
		ravenParameters.importFile = fn;
		try {
			ravenParameters.timeOffsetSeconds = Double.valueOf(timeOffset.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid time offset value. Must be a number");
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		this.ravenParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
