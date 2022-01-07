package fileOfflineData;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import dataMap.filemaps.FileMapMakingdialog;
import dataMap.filemaps.OfflineFileServer;

public class OfflineFileProgressDialog extends PamDialog {

	
	
	private static OfflineFileProgressDialog singleInstance;
	private JLabel progressText;
	private JProgressBar progress;

	private OfflineFileProgressDialog(Window parentFrame) {
		super(parentFrame, "D3 Map Making", false);
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(new TitledBorder("Scan progress"));
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(panel, progressText = new JLabel(" "), c);
		c.gridy++;
		addComponent(panel, progress = new JProgressBar(), c);
		
		progressText.setPreferredSize(new Dimension(150, 10));
		

		setDialogComponent(panel);
		
		getOkButton().setVisible(false);
		getCancelButton().setVisible(false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setModalityType(Dialog.ModalityType.MODELESS);
	}

	public static OfflineFileProgressDialog showDialog(Window parent) {
		if (singleInstance == null || singleInstance.getOwner() != parent) {
			singleInstance = new OfflineFileProgressDialog(parent);
		}
		singleInstance.setVisible(true);
		return singleInstance;
	}
	
	public void setProgress(OfflineMapProgress mapProgress) {
		progress.setIndeterminate(mapProgress.stillCounting);
		if (!mapProgress.stillCounting) {
			progress.setMaximum(mapProgress.totalFiles);
			progress.setValue(mapProgress.fileIndex);
		}
		progressText.setText(String.format("Processing file %d of %d", mapProgress.fileIndex+1, mapProgress.totalFiles));
	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
