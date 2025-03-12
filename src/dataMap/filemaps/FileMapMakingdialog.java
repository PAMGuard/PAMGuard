package dataMap.filemaps;

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

public class FileMapMakingdialog extends PamDialog {
	
	private JProgressBar progress;
	
	private JLabel fileName;
	
	private static FileMapMakingdialog singleInstance;
	
	private OfflineFileServer offlineFileServer;
	
	private static Object synchObject = new Object();

	private FileMapMakingdialog(Window parentFrame) {
		super(parentFrame, "Map raw sound files", false);
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(new TitledBorder("Scan progress"));
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(panel, fileName = new JLabel("   "), c);
		c.gridy++;
		addComponent(panel, progress = new JProgressBar(), c);
		
		fileName.setPreferredSize(new Dimension(350, 10));
		

		setDialogComponent(panel);
		
		getOkButton().setVisible(false);
		getCancelButton().setVisible(false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setModalityType(Dialog.ModalityType.MODELESS);
		
	}
	
	public static FileMapMakingdialog showDialog(Window parent, OfflineFileServer fileServer) {
		// copy the reference, just in case it's deleted in a call to setVisible(false) 
		FileMapMakingdialog anInstance = singleInstance;
		synchronized (synchObject) {
			if (singleInstance == null || singleInstance.getOwner() != parent) {
				anInstance = singleInstance = new FileMapMakingdialog(parent);
			}
		}
		anInstance.offlineFileServer = fileServer;
		anInstance.progress.setIndeterminate(true);
		anInstance.fileName.setText(" ");
		anInstance.setVisible(true);
		return anInstance;
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible == false) {
			synchronized(synchObject) {
				super.dispose();
				singleInstance = null;
			}
		}
	}

	public void setProgress(FileMapProgress mapProgress) {
		switch (mapProgress.countingState) {
		case FileMapProgress.STATE_LOADINGMAP:
			fileName.setText("Loading serialised file map");
			progress.setIndeterminate(true);
			break;
		case FileMapProgress.STATE_SAVINGMAP:
			fileName.setText("Loading serialised file map");
			progress.setIndeterminate(true);
			break;
		case FileMapProgress.STATE_COUNTINGFILES:
			fileName.setText(String.format("Counting files: %d", mapProgress.totalFiles));
			progress.setIndeterminate(true);
			break;
		case FileMapProgress.STATE_DONECOUNTINGFILES:
			progress.setIndeterminate(false);
			progress.setMaximum(mapProgress.totalFiles);
			break;
		case FileMapProgress.STATE_CHECKINGFILES:
			progress.setValue(mapProgress.openedFiles);
			fileName.setText(mapProgress.fileName);
			break;
		}
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
