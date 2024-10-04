package clipgenerator;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamDetection.RawDataUnit;
import PamUtils.SelectFolder;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;

public class ClipDialog extends PamDialog {

	private static ClipDialog singleInstance;
	
	private ClipSettings clipSettings;

	private SourcePanel sourcePanel;
	private StoragePanel storagePanel;
	private ClipPanel clipPanel;

	private ClipControl clipControl;
	
	public ClipDialog(Window parentFrame, ClipControl clipControl) {
		super(parentFrame, "Clip generation settings", false);
		this.clipControl = clipControl;
		clipSettings = clipControl.clipSettings.clone();
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		sourcePanel = new SourcePanel(this, "Audio Data Source", RawDataUnit.class, false, true);
		storagePanel = new StoragePanel();
		clipPanel = new ClipPanel();
		mainPanel.add(sourcePanel.getPanel());
		mainPanel.add(storagePanel);
		mainPanel.add(clipPanel);
		setHelpPoint("sound_processing.ClipGenerator.docs.ClipGenerator");
		setDialogComponent(mainPanel);
	}
	
	public static ClipSettings showDialog(Window window, ClipControl clipControl) {
//		if (singleInstance == null || singleInstance.getOwner() != window || singleInstance.clipControl != clipControl) {
			singleInstance = new ClipDialog(window, clipControl);
//		}
//		singleInstance.clipSettings = clipControl.clipSettings.clone();
		if (singleInstance.clipSettings == null) {
			singleInstance.clipSettings = new ClipSettings();
		}
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.clipSettings;
	}

	@Override
	public void cancelButtonPressed() {
		clipSettings = null;
	}

	private void setParams() {
		sourcePanel.setSource(clipSettings.dataSourceName);
		storagePanel.setParams();
		clipPanel.setParams();
		enableControls();
	}

	@Override
	public boolean getParams() {
		clipSettings.dataSourceName = sourcePanel.getSource().getDataName();
		if (clipSettings.dataSourceName == null) {
			return showWarning("No data source");
		}
		if (!storagePanel.getParams()) return showWarning("Error in storage location");
		if (!clipPanel.getParams()) return showWarning("Error in clip generator settings");
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
	
	}
	
	
	class StoragePanel extends JPanel {

		private SelectFolder selectFolder;
		
		private JCheckBox dateSubFolders;
		
		private JRadioButton storeWavFiles, storeBinary;
//		private JRadioButton storeAnnotation;
		
		public StoragePanel() {
			this.setLayout(new BorderLayout());
			setBorder(new TitledBorder("Storage options"));
			JPanel topRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
			topRow.add(storeWavFiles = new JRadioButton("Store in wav files"));
			topRow.add(storeBinary = new JRadioButton("Store in binary files"));
//			topRow.add(storeAnnotation = new JRadioButton("Annotate data trigger"));
//			ButtonGroup bg = new ButtonGroup();
//			bg.add(storeBinary);
//			bg.add(storeWavFiles);
//			bg.add(storeAnnotation);
			StoreChanged sc = new StoreChanged();
			storeBinary.addActionListener(sc);
			storeWavFiles.addActionListener(sc);
			this.add(BorderLayout.NORTH, topRow);
			selectFolder = new SelectFolder("", 50, false);
			((JPanel) selectFolder.getFolderPanel()).setBorder(null);
			dateSubFolders = new JCheckBox("Store data in sub folders by date");
			this.add(BorderLayout.CENTER, selectFolder.getFolderPanel());
			this.add(BorderLayout.SOUTH, dateSubFolders);
		}
		
		public void setParams() {
			selectFolder.setFolderName(clipSettings.outputFolder);
			dateSubFolders.setSelected(clipSettings.datedSubFolders);
			storeWavFiles.setSelected((clipSettings.storageOption == ClipSettings.STORE_WAVFILES)
					|| (clipSettings.storageOption == ClipSettings.STORE_BOTH));
			storeBinary.setSelected((clipSettings.storageOption == ClipSettings.STORE_BINARY)
					|| (clipSettings.storageOption == ClipSettings.STORE_BOTH));
//			storeAnnotation.setSelected(clipSettings.storageOption == ClipSettings.STORE_ANNOTATION);
		}
		
		public boolean getParams() {
			boolean selectionMade = false;
			clipSettings.datedSubFolders = dateSubFolders.isSelected();
			// first get the folder name without the  check directory option. 
			clipSettings.outputFolder = selectFolder.getFolderName(false);
			if (storeWavFiles.isSelected()) {
				selectionMade = true;
				clipSettings.storageOption = ClipSettings.STORE_WAVFILES;
				if (clipSettings.outputFolder == null || clipSettings.outputFolder.length() == 0) {
					return showWarning("You must specify an output folder for created clips");
				}
				// now do a check with the create folder options
				clipSettings.outputFolder = selectFolder.getFolderName(true);
			}
			if (storeBinary.isSelected()) {
				selectionMade = true;
				clipSettings.storageOption = ClipSettings.STORE_BINARY;
			}
			if (storeWavFiles.isSelected() && storeBinary.isSelected()) {
				selectionMade = true;
				clipSettings.storageOption = ClipSettings.STORE_BOTH;
			}
//			else if (storeAnnotation.isSelected()) {
//				clipSettings.storageOption = ClipSettings.STORE_ANNOTATION;
//				WarnOnce.showWarning("Clip Annotation", "Note that this storage option can only be used with " +
//						" data stored in binary data files", WarnOnce.WARNING_MESSAGE, null);
//			}
			if (!selectionMade) {
				return showWarning("No storage option is selected");
			}
			return true;
		}

		public void enableControls() {
			selectFolder.setEnabled(storeWavFiles.isSelected());
			dateSubFolders.setEnabled(storeWavFiles.isSelected());
		}
	}
	
	class StoreChanged implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			enableControls();
		}
	}

	class ClipPanel extends JPanel {
	
		JLabel[] dataLabels;
		JCheckBox[] enableBoxes;
		JButton[] settingsButtons;
		public ClipPanel() {
			super();
			JPanel p = new JPanel();
			this.setLayout(new BorderLayout());
			int nSets = clipSettings.getNumClipGenerators();
			
			p.setBorder(new TitledBorder("Data Triggers"));
			dataLabels = new JLabel[nSets];
			enableBoxes = new JCheckBox[nSets];
			settingsButtons = new JButton[nSets];
			PamDataBlock aDataBlock;
			p.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			addComponent(p, new JLabel(" Data Name ", SwingConstants.RIGHT), c);
			c.gridx++;
			c.fill = GridBagConstraints.HORIZONTAL;
			addComponent(p, new JLabel(" Enabled "), c);
			for (int i = 0; i < nSets; i++) {
				ClipGenSetting genSet = clipSettings.getClipGenSetting(i);
				c.gridy++;
				c.gridx = 0;
				addComponent(p, dataLabels[i] = new JLabel(genSet.dataName + " ", SwingConstants.RIGHT), c);
				c.gridx++;
				c.anchor = GridBagConstraints.CENTER;
				addComponent(p, enableBoxes[i] = new JCheckBox(), c);
				enableBoxes[i].addActionListener(new ClipEnableButton(i));
				c.gridx ++;
				addComponent(p, settingsButtons[i] = new JButton("Settings"), c);
				settingsButtons[i].addActionListener(new ClipSettingsButton(i));
			}
			this.add(BorderLayout.NORTH, p);
		}
		
		public void setParams() {
			boolean b;
			ClipGenSetting cgs;
			for (int i = 0; i < clipSettings.getNumClipGenerators(); i++) {
				cgs = clipSettings.getClipGenSetting(i);
				enableBoxes[i].setSelected(cgs != null && cgs.enable);
				createToolTip(i);
			}
		}
		
		public boolean getParams() {
			for (int i = 0; i < clipSettings.getNumClipGenerators(); i++) {
				ClipGenSetting cgs = clipSettings.getClipGenSetting(i);
				cgs.enable = enableBoxes[i].isSelected();
			}
			return true;
		}
	
		void enableControls() {
			for (int i = 0; i < settingsButtons.length; i++) {
				settingsButtons[i].setEnabled(enableBoxes[i].isSelected());
			}
		}
		
		void fireSettings(int iDataStream) {
			ClipGenSetting oldSetting = clipSettings.getClipGenSetting(iDataStream);
			ClipGenSetting newSettings = ClipGenSettingDialog.showDialog(getOwner(), 
					oldSetting, storagePanel.storeWavFiles.isSelected());
			if (newSettings != null) {
				clipSettings.replace(oldSetting, newSettings);
			}
			createToolTip(iDataStream);
			enableControls();
		}
		
		private void createToolTip(int iDataStream) {
			String tipText;
			if (iDataStream == 0) {
				tipText = "<html>To use spectrogram marks you will also need to enable marking in the <br>"
						+ "Spectrogram display Settings / Mark Observer dialog.</html>";
			}
			else {
				tipText = "Check to enable clip generation";
			}
			dataLabels[iDataStream].setToolTipText(tipText);
			enableBoxes[iDataStream].setToolTipText(tipText);
			settingsButtons[iDataStream].setToolTipText(tipText);
		}
	
		class ClipEnableButton implements ActionListener {
			int iDataStream;
			/**
			 * @param iDataStream
			 */
			public ClipEnableButton(int iDataStream) {
				super();
				this.iDataStream = iDataStream;
			}
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				enableControls();
			}
			
		}
		class ClipSettingsButton implements ActionListener {
			int iDataStream;
			/**
			 * @param iDataStream
			 */
			public ClipSettingsButton(int iDataStream) {
				super();
				this.iDataStream = iDataStream;
			}
			@Override
			public void actionPerformed(ActionEvent arg0) {
				fireSettings(iDataStream);
			}
			
		}
		
	}

	public void enableControls() {
		storagePanel.enableControls();
		clipPanel.enableControls();
	}

}
