/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package clickDetector.dialogs;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamUtils.PamFileChooser;
import PamView.dialog.PamDialog;
import clickDetector.ClickParameters;
import clickDetector.RainbowFile;

public class ClickStorageOptionsDialog extends PamDialog implements
		ActionListener {

	private ClickParameters clickParameters;

	private static ClickStorageOptionsDialog singleInstance;

	JCheckBox createRCFile, rcAutoNewFile;

	JTextField storageDirectory, storageInitials, rcFileLength;

	JButton browseDirectory;

	JFileChooser fc;
	
//	JCheckBox saveToDatabase;

	private ClickStorageOptionsDialog(Frame parentFrame) {
		
		super(parentFrame, "Click Storage Options", true);
		
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		
//		p.add(BorderLayout.NORTH, new DBPanel());
		p.add(BorderLayout.CENTER, new RainbowFilePanel());
		
		setDialogComponent(p);

		browseDirectory.addActionListener(this);
		createRCFile.addActionListener(this);
		rcAutoNewFile.addActionListener(this);
		
		setHelpPoint("detectors.clickDetectorHelp.docs.ClickDetector_clickStorageOptions");

	}

	public static ClickParameters showDialog(Frame parentFrame, ClickParameters clickParameters) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new ClickStorageOptionsDialog(parentFrame);
		}

		singleInstance.setParams(clickParameters);
		singleInstance.enableControls();
		singleInstance.setVisible(true);

		return singleInstance.clickParameters;
	}

	@Override
	public void cancelButtonPressed() {
		clickParameters = null;
	}
	

	// public void SetParams(ClickParameters clickParameters) {
	// this.clickParameters = new ClickParameters(clickParameters);
	// }

	public void setParams(ClickParameters clickParameters) {
		// JCheckBox createRCFile, rcAutoNewFile;
		// JTextField storageDirectory, storageInitials, rcFileLength;
		// JButton browseDirectory;
		this.clickParameters = clickParameters.clone();
		createRCFile.setSelected(clickParameters.createRCFile);
		rcAutoNewFile.setSelected(clickParameters.rcAutoNewFile);
		storageDirectory.setText(clickParameters.storageDirectory);
		storageInitials.setText(clickParameters.storageInitials);
		rcFileLength
				.setText(String.format("%3f", clickParameters.rcFileLength));
		
//		saveToDatabase.setSelected(clickParameters.saveAllClicksToDB);
	}

	@Override
	public void restoreDefaultSettings() {

		setParams(new ClickParameters());
		
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == browseDirectory) {
			browseDirectories();
		} else if (e.getSource() == createRCFile) {
			enableControls();
		} else if (e.getSource() == rcAutoNewFile) {
			enableControls();
		}
	}

	public void browseDirectories() {
		if (fc == null) {
			fc = new PamFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}

		fc.setCurrentDirectory(new File(storageDirectory.getText()));

		int ans = fc.showDialog(this, "Select storage folder");

		if (ans == JFileChooser.APPROVE_OPTION) {
			storageDirectory.setText(fc.getSelectedFile().toString());
		}

	}

	private void enableControls() {
		boolean makeFile = createRCFile.isSelected();
		boolean autoNew = rcAutoNewFile.isSelected();
		browseDirectory.setEnabled(makeFile);
		storageDirectory.setEnabled(makeFile);
		storageInitials.setEnabled(makeFile);
		rcAutoNewFile.setEnabled(makeFile);
		rcFileLength.setEnabled(makeFile && autoNew);
	}

	@Override
	public boolean getParams() {
		// clickParameters = new ClickParameters(this.clickParameters);
		clickParameters.createRCFile = createRCFile.isSelected();
		clickParameters.rcAutoNewFile = rcAutoNewFile.isSelected();
		clickParameters.storageDirectory = new String(storageDirectory
				.getText());
		/*
		 * Check the directory exists and try to create it otherwise.
		 * 
		 */
		// DirectoryManager.;
		clickParameters.storageInitials = new String(storageInitials.getText());
		try {
			clickParameters.rcFileLength = Float.valueOf(rcFileLength
					.getText());
		} catch (NumberFormatException ex) {
			return false;
		}

//		clickParameters.saveAllClicksToDB = saveToDatabase.isSelected();
		
		// now check the directory is there and don't allow to exit if it
		// is not created. 
		if (clickParameters.createRCFile) {
			if (RainbowFile.checkStorage(clickParameters.storageDirectory) == false) {
				return false;
			}
		}
		
		return true;
	}

	private class RainbowFilePanel extends JPanel {

		public RainbowFilePanel() {
			super();
			// JCheckBox createRCFile, rcAutoNewFile;
			// JTextField storageDirectory, storageInitials, rcFileLength;
			// JButton browseDirectory;
			JPanel p;
			setBorder(new TitledBorder("RainbowClick compatible files"));
			setLayout(new GridLayout(6, 1));
			add(createRCFile = new JCheckBox("Create RainbowClick File(s)"));
			p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
			p.add(new JLabel("Output Directory   "));
			p.add(browseDirectory = new JButton("Browse ..."));
			add(p);
			add(storageDirectory = new JTextField(50));
			p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
			p.add(new JLabel("File Initials   "));
			p.add(storageInitials = new JTextField(20));
			p.add(new JLabel("                              "));
			add(p);
			add(rcAutoNewFile = new JCheckBox("Start new files automatically"));
			p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
			p.add(new JLabel("File length  "));
			p.add(rcFileLength = new JTextField(10));
			p.add(new JLabel("  hours                       "));
			add(p);
		}
	}
	
//	private class DBPanel extends JPanel {
//
//		public DBPanel() {
//			setLayout(new BorderLayout());
//			setBorder(new TitledBorder("Database Options"));
////			saveToDatabase = new JCheckBox("Save all clicks to database");
////			add(BorderLayout.WEST, saveToDatabase);
//		}
//	}
}
