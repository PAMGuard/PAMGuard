package binaryFileStorage;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamUtils.SelectFolder;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class BinaryStorageDialogPanel {

	private JPanel p;
	
	private SelectFolder storageLocation;

	private JCheckBox autoNewFiles, dateSubFolders, limitfileSize;

	private JTextField fileLength, fileSize;
	
	private JTextField channelOffset;
	
	private NoiseComboBox noiseStoreType;
	
	private String errorTitle = "Binary Storage Options";
	
	private Window owner;

	private boolean allowChannelOffsets;
	
	public BinaryStorageDialogPanel(Window owner, boolean allowChannelOffsets) {
		
		this.owner = owner;
		this.allowChannelOffsets = allowChannelOffsets;

		p = new JPanel(new BorderLayout());
//		p.setBorder(new TitledBorder("Binary storage options"));
		storageLocation = new SelectFolder("", 50, false);
		((JPanel) storageLocation.getFolderPanel()).setBorder(new TitledBorder("Binary file folder"));
//		storageLocation.getFolderPanel().setBorder(new TitledBorder("Binary file folder"));
		p.add(BorderLayout.NORTH, storageLocation.getFolderPanel());

		JPanel q = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		PamDialog.addComponent(q, dateSubFolders = new JCheckBox("Store data in sub folders by date"), c);
		c.gridy++;
		c.gridx = 0;
		q.add(new JLabel("Noise background storage location", SwingConstants.RIGHT), c);
		c.gridx++;
		c.gridwidth = 3;
		q.add(noiseStoreType = new NoiseComboBox(), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		PamDialog.addComponent(q, autoNewFiles = new JCheckBox("Automatically start new files every "), c);
		c.gridx++;
		PamDialog.addComponent(q, fileLength = new JTextField(6), c);
		c.gridx++;
		PamDialog.addComponent(q, new JLabel(" minutes"), c);
		c.gridy++;
		c.gridx = 0;
		PamDialog.addComponent(q, limitfileSize = new JCheckBox("Limit data file size to a maximum of "), c);
		c.gridx++;
		PamDialog.addComponent(q, fileSize = new JTextField(6), c);
		c.gridx++;
		PamDialog.addComponent(q, new JLabel(" Mega Bytes"), c);
		if (allowChannelOffsets) {
			c.gridx = 0;
			c.gridy++;
			q.add(new JLabel("Offset channel bitmaps by ", SwingConstants.RIGHT), c);
			c.gridx++;
			q.add(channelOffset = new JTextField(6), c);
			c.gridx++;
			q.add(new JLabel(" channels", SwingConstants.LEFT), c);
			channelOffset.setToolTipText("Used when combinging data from multiple recorders in viewer mode");
		}
		noiseStoreType.addItem(NoiseStoreType.PGDF);
		noiseStoreType.addItem(NoiseStoreType.PGNF);
		
		JPanel qb = new JPanel(new BorderLayout());
		qb.add(BorderLayout.WEST, q);
		qb.setBorder(new TitledBorder("Options"));
//		qb.setVisible(PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW);
		
		p.add(BorderLayout.CENTER, qb);

		autoNewFiles.addActionListener(new ChangeAction());
		limitfileSize.addActionListener(new ChangeAction());

	}
	
	private class NoiseComboBox extends JComboBox<NoiseStoreType> {

		public NoiseComboBox() {
			super();
			setToolTipText("Noise storage option");
		}

		@Override
		public String getToolTipText() {
			NoiseStoreType nt = (NoiseStoreType) getSelectedItem();
			if (nt == null) {
				return null;
			}
			else {
				return nt.getToolTip();
			}
		}
		
		
		
	}
	
	public boolean getParams(BinaryStoreSettings binaryStoreSettings) {
		if (binaryStoreSettings == null) {
			return false;
		}
		binaryStoreSettings.setStoreLocation(storageLocation.getFolderName(true));
		if (binaryStoreSettings.getStoreLocation()==null) {
			//cancel button pressed - return to dialog
			return false;
		}
		binaryStoreSettings.autoNewFiles = autoNewFiles.isSelected();
		binaryStoreSettings.datedSubFolders = dateSubFolders.isSelected();
		binaryStoreSettings.limitFileSize = limitfileSize.isSelected();

		if (allowChannelOffsets) {
			try {
				binaryStoreSettings.channelShift = Integer.valueOf(channelOffset.getText());
			}
			catch (NumberFormatException e) {
				return PamDialog.showWarning(owner, errorTitle, "Invalid channel offset number format");
			}
			if (binaryStoreSettings.channelShift < 0 || binaryStoreSettings.channelShift > 31) {
				return PamDialog.showWarning(owner, errorTitle, "Channel offset bust be between 0 and 31");
			}
		}
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			return true;
		}
		
		if (binaryStoreSettings.autoNewFiles) {
			try {
				binaryStoreSettings.fileSeconds = Integer.valueOf(fileLength.getText()) * 60;
				if (binaryStoreSettings.fileSeconds <= 0) {
					return PamDialog.showWarning(owner, errorTitle, "File length must be > 0");
				}
			}
			catch (NumberFormatException e) {
				return PamDialog.showWarning(owner, errorTitle, "Invalid file length data");
			}
		}
		if (binaryStoreSettings.limitFileSize) {
			try {
				binaryStoreSettings.maxFileSize = Integer.valueOf(fileSize.getText());
				if (binaryStoreSettings.maxFileSize <= 0) {
					return PamDialog.showWarning(owner, errorTitle, "File size must be > 0");
				}
			}
			catch (NumberFormatException e) {
				return PamDialog.showWarning(owner, errorTitle, "Invalid file size data");
			}
		}
		NoiseStoreType nst = (NoiseStoreType) noiseStoreType.getSelectedItem();
		if (nst == null) {
			return PamDialog.showWarning(owner, errorTitle, "You must select a noise storage type");
		}
		
		binaryStoreSettings.setNoiseStoreType(nst);
		
		return true;
	}
	
	public void setParams(BinaryStoreSettings binaryStoreSettings) {
		storageLocation.setFolderName(binaryStoreSettings.getStoreLocation());
		autoNewFiles.setSelected(binaryStoreSettings.autoNewFiles);
		dateSubFolders.setSelected(binaryStoreSettings.datedSubFolders);
		fileLength.setText(String.format("%d", binaryStoreSettings.fileSeconds/60));
		limitfileSize.setSelected(binaryStoreSettings.limitFileSize);
		fileSize.setText(String.format("%d", binaryStoreSettings.maxFileSize));
		if (allowChannelOffsets) {
			channelOffset.setText(String.format("%d", binaryStoreSettings.channelShift));
		}
		noiseStoreType.setSelectedItem(binaryStoreSettings.getNoiseStoreType());
		
		enableControls();
		owner.pack();
	}

	/**
	 * @return the panel
	 */
	public JPanel getPanel() {
		return p;
	}

	private class ChangeAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}

	}

	public void enableControls() {
		fileLength.setEnabled(autoNewFiles.isSelected());
		fileSize.setEnabled(limitfileSize.isSelected());
	}

}
