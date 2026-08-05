package binaryFileStorage.checker;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamUtils.SelectFolder;
import PamView.CancelObserver;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.WestAlignedPanel;
import PamguardMVC.PamDataBlock;

public class BinaryUpdateDialog extends PamDialog implements UpdateWorkObserver, CancelObserver {
	
	private static BinaryUpdateDialog singleInstance;
	
	private JCheckBox useSameFolder;
	
	private SelectFolder selectFolder;
	
	private JCheckBox[] dataBlocks;
	
	private JProgressBar blockProgress, fileProgress;
	private JLabel currentBlock, currentFile;

	private JPanel centPanel;

	private BinaryUpdater binaryUpdater;

	private List<PamDataBlock> blockList;

	private BinaryUpdateDialog(Window parentFrame, BinaryUpdater binaryUpdater) {
		super(parentFrame, "Binary File Update", false);
		this.binaryUpdater = binaryUpdater;
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel topPanel = new WestAlignedPanel(new GridBagLayout());
		centPanel = new JPanel(new GridBagLayout());
		JPanel botPanel = new JPanel(new GridBagLayout());
		JPanel wal = new WestAlignedPanel(centPanel);
		JPanel wap2 = new WestAlignedPanel(botPanel);
		
		topPanel.setBorder(new TitledBorder("Options"));
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 2;
		topPanel.add(useSameFolder = new JCheckBox("Save updated files in the same location"), c);
		c.gridy++;
		topPanel.add(new JLabel("New folder name"), c);
		c.gridy++;
		selectFolder = new SelectFolder(50);
		topPanel.add(selectFolder.getFolderPanel(), c);
		
		wal.setBorder(new TitledBorder("Data to convert"));
		
		useSameFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
		});		
		
		c = new PamGridBagContraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		wap2.setBorder(new TitledBorder("Progress"));
		botPanel.add(currentBlock = new JLabel(" "), c);
		c.gridy++;
		botPanel.add(blockProgress = new JProgressBar(), c);
		c.gridy++;
		botPanel.add(currentFile = new JLabel(" "), c);
		c.gridy++;
		botPanel.add(fileProgress = new JProgressBar(), c);
		
		getOkButton().setText("Start");
//		getOkButton().setText("Stop");
		
		mainPanel.add(BorderLayout.NORTH, topPanel);
		mainPanel.add(BorderLayout.CENTER, wal);
		mainPanel.add(BorderLayout.SOUTH, wap2);
		setResizable(true);
		setDialogComponent(mainPanel);
	}
	
	public static boolean showDialog(Window parent, BinaryUpdater binaryUpdater) {
		singleInstance = new BinaryUpdateDialog(parent, binaryUpdater);
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return true;
	}

	private void setParams() {
		String current = binaryUpdater.getCurrentFolder();
		BinaryUpdateParams params = binaryUpdater.getBinaryUpdateParams();
		useSameFolder.setSelected(params.isUseSameFolder());
		if (params.isUseSameFolder() || params.getNewFolderName() == null) {
			selectFolder.setFolderName(current);
		}
		else {
			selectFolder.setFolderName(params.getNewFolderName());
		}
		
		blockList = binaryUpdater.getBinaryDataBlocks();
		centPanel.removeAll();
		dataBlocks = new JCheckBox[blockList.size()];
		GridBagConstraints c = new PamGridBagContraints();
		int i = 0;
		BlockSelAction bsa = new BlockSelAction();
		for (PamDataBlock aBlock : blockList) {
			dataBlocks[i] = new JCheckBox(aBlock.getDataName());
			dataBlocks[i].setToolTipText(aBlock.getLongDataName());
			dataBlocks[i].addActionListener(bsa);
			centPanel.add(dataBlocks[i], c);
			c.gridy++;
			BinaryUpdateSet sp = params.getUpdateSet(aBlock);
			dataBlocks[i].setSelected(sp.update);
			
			i++;
		}
		
		pack();
		
		enableControls();
		updateButtons(false);
		
	}
	
	@Override
	public boolean getParams() {

		BinaryUpdateParams params = binaryUpdater.getBinaryUpdateParams();
		params.setUseSameFolder(useSameFolder.isSelected());
		params.setNewFolderName(selectFolder.getFolderName(true));
		String folderName = selectFolder.getFolderName(false);
		if (folderName == null) {
			return showWarning("No output folder selected");
		}
		File ff = new File(folderName);
		if (ff.exists() == false) {
			return showWarning("No output folder selected");
		}
		
		
		if (blockList == null) {
			return false;
		}
		for (int i = 0; i < blockList.size(); i++) {
			PamDataBlock aBlock = blockList.get(i); 
			BinaryUpdateSet ps = params.getUpdateSet(aBlock);
			ps.update = dataBlocks[i].isSelected();
			// don't need to put it back, since it's the same object. 
		}
		updateButtons(true);
		binaryUpdater.runUpdate(this);
		
		return false;
	}

	private class BlockSelAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			enableControls();
		}
		
	}

	private void enableControls() {
		selectFolder.setEnabled(useSameFolder.isSelected() == false);
		if (useSameFolder.isSelected()) {
			selectFolder.setFolderName(binaryUpdater.getCurrentFolder());
		}
	}
	
	private void updateButtons(boolean isRunning) {
		getOkButton().setEnabled(isRunning == false);
		getCancelButton().setText(isRunning ? "Stop" : "Close");
	}

	@Override
	public void update(UpdateWorkProgress updateWorkProgress) {
		currentBlock.setText(updateWorkProgress.getPamDataBlock().getLongDataName());
		int nBlock = updateWorkProgress.getnBlock();
		if (nBlock == 0) {
			blockProgress.setValue(0);
		}
		else {
			blockProgress.setMaximum(nBlock);
			blockProgress.setValue(updateWorkProgress.getiBlock());
		}
		int nFile = updateWorkProgress.getnFile();
		if (nFile <= 0) {
			currentFile.setText("Counting files ...");
			fileProgress.setIndeterminate(true);
		}
		else {
			fileProgress.setIndeterminate(false);
			fileProgress.setMaximum(nFile);
			fileProgress.setValue(updateWorkProgress.getiFile());
//			fileProgress.settoolTipText(String.format("%d/%d",updateWorkProgress.getiFile(),nFile));
			String fn = updateWorkProgress.getFileName();
			if (fn == null) {
				currentFile.setText("Complete");
			}
			else {
				currentFile.setText(fn);
			}
		}
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void done() {
		enableControls();
		updateButtons(false);
	}

	@Override
	public boolean cancelPressed() {

		boolean isRunning = binaryUpdater.isRunning();
		if (isRunning) {
			binaryUpdater.stopUpdate();
			return false;
		}
		return true;
	}

}
