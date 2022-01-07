package backupmanager.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamFolders;
import PamUtils.FileFunctions;
import PamUtils.FolderChangeListener;
import PamUtils.PamFileChooser;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamTextDisplay;
import PamView.dialog.warn.WarnOnce;
import backupmanager.FileLocation;

/**
 * Swing component that can be used to set a file location for source or destination files. 
 * Allows selection by files OR folders, (or both?)
 * @author dg50
 *
 */
public class FileLocationComponent {

	public enum LocationType {SOURCE, DESTINATION};
	
	public static final int DEFAULT_PATH_LEN = 70;
	
	private JPanel mainPanel;
	
	private LocationType type;

	private Window owner;

//	private boolean allowFiles;
//
//	private boolean allowFolders;
	
	private PamTextDisplay pathName;
	
	private JTextField maskField;

	private boolean allowPath;

	private boolean showMask;

	public FileLocationComponent(Window owner, LocationType type, boolean allowPath, boolean allowMask) {
		this(owner, type, defaultTitle(type), DEFAULT_PATH_LEN, allowPath, allowMask);
	}
	
	public FileLocationComponent(Window owner, LocationType type, String borderTitle, int charWidth, boolean allowPath, boolean showMask) {
		this.owner = owner;
		this.type = type;
		this.allowPath = allowPath || type == LocationType.DESTINATION;
		this.showMask = showMask;
//		this.allowFiles = allowFiles;
//		this.allowFolders = allowFolders;
		mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		if (borderTitle != null) {
			mainPanel.setBorder(new TitledBorder(borderTitle));
		}
		c.gridwidth = 3;
		pathName = new PamTextDisplay(charWidth);
		mainPanel.add(pathName, c);
		JButton browse = new JButton("Browse...");
		browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				browseButton();
			}
		});
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		mainPanel.add(browse, c);
		if (type == LocationType.SOURCE && showMask) {
			c.gridx = 0;
			c.gridy++;
			mainPanel.add(new JLabel("File mask "), c);
			c.gridx++;
			mainPanel.add(maskField = new JTextField(30), c);
			c.gridx = 0;
			c.gridy++;
			c.gridwidth = 3;
			mainPanel.add(new JLabel(" (use ; to separate multiple types, leave blank for all file types, supports wildcards, e.g. *.pgdf;*.pgdx;*.wav, etc)"), c);
			maskField.setEnabled(showMask);
//			mainPanel.add(selFiles = new JRadioButton("Backup individual files"), c);
//			c.gridy++;
//			mainPanel.add(selFolders = new JRadioButton("Backup by folder"), c);
//			ButtonGroup bg = new ButtonGroup();
//			bg.add(selFiles);
//			bg.add(selFolders);
//			selFiles.setEnabled(allowFiles);
//			selFolders.setEnabled(allowFolders);
		}
		
		pathName.setEnabled(this.allowPath);
		browse.setEnabled(this.allowPath);
	}
	
	private static String defaultTitle(LocationType type) {
		switch (type) {
		case DESTINATION:
			return "Destination folder";
		case SOURCE:
			return "Source folder";
		default:
			break;
		}
		return null;
	}
	
	public JPanel getComponent() {
		return mainPanel;
	}

	private void browseButton() {
		// whether files or folders are selected, this is only ever going to select the folder, 
		// though it may still show the files. 
		JFileChooser fc = new PamFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		fc.setCurrentDirectory(new File(pathName.getText()));
		
		String tit = getSelectionTitle(type);
		
		int ans = fc.showDialog(owner, tit);

		if (ans == JFileChooser.APPROVE_OPTION) {
			pathName.setText(fc.getSelectedFile().toString());
			if (checkPath(pathName.getText()) == false) {
				String msg = String.format("The file or directory %s does not exist, do you want to create it?", pathName.getText());
				ans = WarnOnce.showWarning(getSelectionTitle(type), msg, WarnOnce.OK_CANCEL_OPTION, null);
				if (ans == WarnOnce.OK_OPTION) {
					File aFolder = FileFunctions.createNonIndexedFolder(pathName.getText());
				}
			}
		}
	}
	
	private String getSelectionTitle(LocationType type) {
		switch (type) {
		case DESTINATION:
			return "Select destination folder";
		case SOURCE:
			return "Select source folder";
		default:
			return "Select folder";
		}
	}

	public void setParams(FileLocation fileLocation) {
		if (fileLocation == null) {
			return;
		}
		pathName.setText(fileLocation.path);
		
		if (showMask) {
			maskField.setText(fileLocation.mask);
		}
//		if (selFiles != null) {
//			selFiles.setSelected((fileLocation.fileOrFolders & FileLocation.SEL_FILES) != 0);
//		}
//		if (selFolders != null) {
//			selFolders.setSelected((fileLocation.fileOrFolders & FileLocation.SEL_FOLDERS) != 0);
//		}
	}
	
	public FileLocation getParams(FileLocation fileLocation) {
		if (fileLocation == null) {
			fileLocation = new FileLocation();
		}
		else {
			fileLocation = fileLocation.clone();
		}
		if (allowPath) {
			fileLocation.path = pathName.getText();
		}
		if (showMask) {
			fileLocation.mask = maskField.getText();
		}
		if (checkPath(fileLocation.path)) {
			if (this.type == LocationType.DESTINATION) {
				if (checkPath(fileLocation.path) == false) {
					String msg = String.format("The file or directory %s does not exist, do you want to create it?", fileLocation.path);
					int ans = WarnOnce.showWarning(getSelectionTitle(type), msg, WarnOnce.OK_CANCEL_OPTION, null);
					if (ans == WarnOnce.OK_OPTION) {
						File aFolder = new File(pathName.getText());
						boolean made = aFolder.mkdirs() == false;
						if (made) {
							FileFunctions.setNonIndexingBit(aFolder);
						}
						else {
							String msg2 = String.format("Unable to create the directory %s. Check your drives!", fileLocation.path);
							WarnOnce.showWarning(getSelectionTitle(type), msg, WarnOnce.WARNING_MESSAGE, null);
						}
					}
				}
			}
		}
//		fileLocation.fileOrFolders = 0;
//		if (selFiles != null && selFiles.isSelected()) {
//			fileLocation.fileOrFolders |= FileLocation.SEL_FILES;
//		}
//		if (selFolders != null && selFolders.isSelected()) {
//			fileLocation.fileOrFolders |= FileLocation.SEL_FOLDERS;
//		}
		
		return fileLocation;
	}
	
	private boolean checkPath(String path) {
		if (path == null || path.length() == 0) {
			return false;
		}
		File filePath = new File(path);
		return filePath.exists();
	}
}
