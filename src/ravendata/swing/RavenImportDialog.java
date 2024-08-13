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
	


	private RavenImportDialog(Window parentFrame) {
		super(parentFrame, "Import Raven Data", false);
		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Choose Raven selection table"));
		GridBagConstraints c = new PamGridBagContraints();
		ravenFile = new JTextField(80);
		ravenFile.setEditable(false);
		chooseButton = new JButton("Select ...");
		c.gridwidth = 2;
		mainPanel.add(ravenFile, c);
		c.gridy++;
		mainPanel.add(new PamAlignmentPanel(chooseButton, BorderLayout.EAST), c);
		
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
