package dataMap.filemaps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import Acquisition.AcquisitionControl;
import Acquisition.CheckWavFileHeaders;
import Acquisition.DaqSystem;
import Acquisition.FolderInputParameters;
import Acquisition.FolderInputSystem;
import Acquisition.filedate.FileDate;
import Acquisition.filedate.FileDateDialogStrip;
import Acquisition.filedate.FileDateObserver;
import Acquisition.filedate.FileTimeData;
import Acquisition.offlineFuncs.OfflineWavFileServer;
import Acquisition.pamAudio.PamAudioFileFilter;
import PamController.OfflineFileDataStore;
import PamUtils.worker.PamWorker;
import PamUtils.worker.filelist.FileListData;
import PamUtils.worker.filelist.WavFileType;
import PamUtils.worker.filelist.WavListUser;
import PamUtils.worker.filelist.WavListWorker;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

/**
 * An extra panel that appears in the DAQ control when offline
 * so that user can point the DAQ at a set of wav of aif files
 * to use with the offline viewer. 
 * <p>
 * This mirrors the "folders and files" panel of the real time
 * {@link FolderInputSystem}: files as well as folders can be selected, the
 * files are catalogued as soon as the selection changes so that the file date
 * and the number of files found are shown straight away, and the file date
 * format can be set from here.
 * 
 * @author Doug Gillespie
 *
 */
public class OfflineFileDialogPanel implements WavListUser, FileDateObserver {

	private OfflineFileDataStore offlineRawDataStore;
	
	private PamDialog parentDialog;

	private JPanel outerPanel;
	
	private JCheckBox enableOffline;
	
	private JCheckBox subFolders;

	private JComboBox<String> selectionCombo;

	/**
	 * True while the combo box is being filled programmatically, so that its action
	 * listener can tell a real user selection from a rebuild of the list.
	 */
	private boolean fillingCombo;

	/**
	 * Recently used folders, most recent first.
	 */
	private ArrayList<String> recentFolders = new ArrayList<>();

	private JButton selectButton;

	private JLabel selectionLabel;

	private JButton checkFiles;

	/**
	 * File date strip, which also carries the label saying how many files were found.
	 * Null if the file server has no file date (i.e. isn't a sound file server).
	 */
	private FileDateDialogStrip fileDateStrip;

	/**
	 * Lists the sound files in the current selection in a background thread.
	 */
	private WavListWorker wavListWorker = new WavListWorker(this);

	/**
	 * The files and / or folders the user has selected. Null if the whole of
	 * folderName is to be used.
	 */
	private String[] selectedFiles;

	/**
	 * The folder the files are in. Used as the search root when nothing has been
	 * explicitly selected and as the place the file map is written to.
	 */
	private String folderName;

	/**
	 * The sound files found in the current selection.
	 */
	private ArrayList<WavFileType> currentFiles = new ArrayList<>();

	/**
	 * @param offlineFileDataSource
	 * @param parentDialog
	 */
	public OfflineFileDialogPanel(OfflineFileDataStore offlineFileDataSource,
			PamDialog parentDialog) {
		super();
		this.offlineRawDataStore = offlineFileDataSource;
		this.parentDialog = parentDialog;

		enableOffline = new JCheckBox("Use offline files");
		selectionCombo = new JComboBox<String>();
		/*
		 * Folder names are long, so fix the width off a prototype rather than letting
		 * the longest path in the list set the width of the whole dialog. The full
		 * name is always in the tool tip.
		 */
		selectionCombo.setPrototypeDisplayValue("12345678901234567890123456789012345");
		selectionLabel = new JLabel(" ");
		selectButton = new JButton("Select Folder or Files");
		subFolders = new JCheckBox("Include sub folders");

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.NORTH, enableOffline);

		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]{100, 100, 10};
		JPanel p = new JPanel(layout);
		GridBagConstraints c = new PamGridBagContraints();
		c.insets = new Insets(2,2,2,2);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		p.add(selectionCombo, c);

		c.gridy++;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		p.add(subFolders, c);
		c.gridx = 2;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.EAST;
		p.add(selectButton, c);

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.WEST;
		p.add(selectionLabel, c);

		FileDate fileDate = findFileDate();
		if (fileDate != null) {
			c.gridy++;
			c.fill = GridBagConstraints.HORIZONTAL;
			fileDateStrip = new FileDateDialogStrip(fileDate, parentDialog);
			fileDateStrip.addObserver(this);
			p.add(fileDateStrip.getDialogComponent(), c);
			c.fill = GridBagConstraints.NONE;

			c.gridy++;
			c.gridwidth = 1;
			checkFiles = new JButton("Check File Headers...");
			checkFiles.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					checkFileHeaders();
				}
			});
			p.add(checkFiles, c);
		}

		mainPanel.add(BorderLayout.CENTER, p);
		outerPanel = new JPanel(new BorderLayout());
		outerPanel.add(BorderLayout.NORTH, mainPanel);
		outerPanel.setBorder(new TitledBorder("Offline file store"));

		enableOffline.addActionListener(new EnableButton());
		selectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectFilesOrFolder();
			}
		});
		subFolders.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listFiles();
			}
		});
		selectionCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				comboSelection();
			}
		});
		
		String folder = findOfflineFolderName(offlineRawDataStore.getOfflineFileServer().getOfflineFileParameters());
		setSelection(folder, null);
	}
	
	/**
	 * The file date used by this file store, or null if it doesn't have one.
	 * @return the file date.
	 */
	private FileDate findFileDate() {
		OfflineFileServer offlineFileServer = offlineRawDataStore.getOfflineFileServer();
		if (offlineFileServer instanceof OfflineWavFileServer) {
			return ((OfflineWavFileServer) offlineFileServer).getFileDate();
		}
		return null;
	}

	public Component getComponent() {
		return outerPanel;
	}
	
	private class EnableButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
			if (enableOffline.isSelected()) {
				listFiles();
			}
		}
	}
	
	private void enableControls() {
		boolean enabled = enableOffline.isSelected();
		selectionCombo.setEnabled(enabled);
		selectButton.setEnabled(enabled);
		selectionLabel.setEnabled(enabled);
		/*
		 * Sub folder searching is meaningless when the selection is a list of
		 * individual files, so grey it out to make it clear that only the listed files
		 * will be used.
		 */
		subFolders.setEnabled(enabled && !isFileSelection());
		if (fileDateStrip != null) {
			enableComponents(fileDateStrip.getDialogComponent(), enabled);
		}
		if (checkFiles != null) {
			checkFiles.setEnabled(enabled && currentFiles.size() > 0);
		}
	}

	/**
	 * Enable or disable a component and everything inside it.
	 * @param component component to change
	 * @param enable true to enable
	 */
	private void enableComponents(Component component, boolean enable) {
		component.setEnabled(enable);
		if (component instanceof Container) {
			Component[] children = ((Container) component).getComponents();
			for (int i = 0; i < children.length; i++) {
				enableComponents(children[i], enable);
			}
		}
	}

	/**
	 * Has the user selected individual files, rather than a folder ?
	 * @return true if at least one selected item isn't a folder.
	 */
	private boolean isFileSelection() {
		if (selectedFiles == null) {
			return false;
		}
		for (int i = 0; i < selectedFiles.length; i++) {
			if (new File(selectedFiles[i]).isDirectory() == false) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Select a folder, or a list of files and folders, in the same way as the real
	 * time folder input system does.
	 */
	private void selectFilesOrFolder() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setMultiSelectionEnabled(true);
		PamAudioFileFilter filter = new PamAudioFileFilter();
		filter.setAcceptFolders(true);
		fc.setFileFilter(filter);
		if (folderName != null) {
			fc.setCurrentDirectory(new File(folderName));
		}
		if (selectedFiles != null) {
			File[] files = new File[selectedFiles.length];
			for (int i = 0; i < selectedFiles.length; i++) {
				files[i] = new File(selectedFiles[i]);
			}
			fc.setSelectedFiles(files);
		}
		int ans = fc.showDialog(parentDialog, "Select files and folders");
		if (ans != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File[] files = fc.getSelectedFiles();
		if (files == null || files.length == 0) {
			return;
		}
		/*
		 * A single folder is stored as a plain folder name so that it behaves exactly
		 * as it always has. Anything else is kept as an explicit list, since only those
		 * files should be used.
		 */
		if (files.length == 1 && files[0].isDirectory()) {
			setSelection(files[0].getAbsolutePath(), null);
		}
		else {
			String[] names = new String[files.length];
			for (int i = 0; i < files.length; i++) {
				names[i] = files[i].getAbsolutePath();
			}
			setSelection(findRootFolder(files[0]), names);
		}
		listFiles();
	}

	/**
	 * The folder a selected file or folder sits in.
	 * @param file selected file or folder
	 * @return the folder itself if it's a folder, otherwise its parent.
	 */
	private String findRootFolder(File file) {
		if (file == null) {
			return null;
		}
		if (file.isDirectory()) {
			return file.getAbsolutePath();
		}
		File parent = file.getParentFile();
		return parent == null ? null : parent.getAbsolutePath();
	}

	/**
	 * Set the current selection and update all the descriptive text.
	 * @param folderName the root folder
	 * @param selectedFiles explicit list of files and folders, or null to use the whole folder.
	 */
	private void setSelection(String folderName, String[] selectedFiles) {
		this.folderName = folderName;
		this.selectedFiles = (selectedFiles == null || selectedFiles.length == 0) ? null : selectedFiles;
		if (folderName != null) {
			recentFolders.remove(folderName);
			recentFolders.add(0, folderName);
			while (recentFolders.size() > OfflineFileParameters.MAX_RECENT_FILES) {
				recentFolders.remove(recentFolders.size()-1);
			}
		}
		fillCombo();
		if (this.folderName == null && selectionCombo.getItemCount() > 0) {
			// nothing set yet, so fall back to the most recently used folder.
			this.folderName = (String) selectionCombo.getSelectedItem();
		}
		if (this.selectedFiles == null) {
			selectionCombo.setToolTipText(folderName);
			selectionLabel.setText("All sound files in this folder");
		}
		else {
			selectionCombo.setToolTipText(listToolTip(this.selectedFiles));
			selectionLabel.setText(String.format("Using %d selected file%s",
					this.selectedFiles.length, this.selectedFiles.length == 1 ? "" : "s"));
		}
		enableControls();
	}

	/**
	 * Fill the combo box with the recently used folders, most recent first.
	 */
	private void fillCombo() {
		fillingCombo = true;
		selectionCombo.removeAllItems();
		for (int i = 0; i < recentFolders.size(); i++) {
			String folder = recentFolders.get(i);
			if (folder != null && folder.length() > 0) {
				selectionCombo.addItem(folder);
			}
		}
		if (selectionCombo.getItemCount() > 0) {
			selectionCombo.setSelectedIndex(0);
		}
		fillingCombo = false;
	}

	/**
	 * Called when the user picks a folder from the drop down list. Picking a folder
	 * means the whole of that folder, so any list of individually selected files is
	 * dropped.
	 */
	private void comboSelection() {
		if (fillingCombo) {
			return;
		}
		String folder = (String) selectionCombo.getSelectedItem();
		if (folder == null || folder.equals(folderName)) {
			return;
		}
		setSelection(folder, null);
		listFiles();
	}

	/**
	 * Make an html tool tip listing the selection, truncated so that a big
	 * selection doesn't fill the screen.
	 * @param files selected file names
	 * @return html tool tip text
	 */
	private String listToolTip(String[] files) {
		int nShow = Math.min(files.length, 20);
		StringBuilder sb = new StringBuilder("<html>");
		for (int i = 0; i < nShow; i++) {
			sb.append(files[i]);
			sb.append("<br>");
		}
		if (nShow < files.length) {
			sb.append(String.format("... and %d more", files.length - nShow));
		}
		sb.append("</html>");
		return sb.toString();
	}

	/**
	 * Catalogue the sound files in the current selection in a background thread.
	 * The answer comes back in {@link #newFileList(FileListData)}.
	 */
	private void listFiles() {
		String[] searchList = getSearchList();
		if (searchList == null) {
			currentFiles.clear();
			sayFiles();
			return;
		}
		/*
		 * Run the search without a modal progress dialog, since this is called whenever
		 * the dialog opens as well as when the selection changes, and useOldIfPossible
		 * is true so that re-opening the dialog with an unchanged selection doesn't set
		 * a search running at all.
		 */
		if (fileDateStrip != null) {
			fileDateStrip.setNfiles("searching for sound files ...");
		}
		PamWorker<FileListData<WavFileType>> worker =
				wavListWorker.makeFileListProcess(searchList, subFolders.isSelected(), true);
		if (worker != null) {
			worker.start();
		}
	}

	/**
	 * The list of files and folders to search: the explicit selection if there is
	 * one, otherwise the folder.
	 * @return search roots, or null if nothing is set.
	 */
	private String[] getSearchList() {
		if (selectedFiles != null) {
			return selectedFiles;
		}
		if (folderName == null) {
			return null;
		}
		return new String[]{folderName};
	}

	@Override
	public void newFileList(FileListData<WavFileType> fileListData) {
		currentFiles.clear();
		if (fileListData != null) {
			currentFiles.addAll(fileListData.getListCopy());
		}
		sayFiles();
	}

	/**
	 * Update the file date and the number of files found.
	 */
	private void sayFiles() {
		if (fileDateStrip != null) {
			fileDateStrip.setNfiles(currentFiles);
			FileDate fileDate = findFileDate();
			if (fileDate != null) {
				fileDateStrip.setFormat(fileDate.getFormat());
				FileTimeData timeData = currentFiles.size() > 0 ?
						fileDate.getTimeFromFile(currentFiles.get(0)) : null;
				if (timeData == null) {
					fileDateStrip.clearDate();
				}
				else {
					fileDateStrip.setDate(timeData.getFileStart());
				}
			}
		}
		enableControls();
	}

	@Override
	public void fileDateChange(FileDate fileDate) {
		sayFiles();
	}

	/**
	 * Check the headers of the files in the current selection. This used to be done
	 * from the real time folder panel, which isn't available offline.
	 */
	private void checkFileHeaders() {
		List<File> files = new ArrayList<File>(currentFiles);
		String description = folderName;
		if (selectedFiles != null) {
			description = String.format("%d selected files in %s", files.size(), folderName);
		}
		else if (subFolders.isSelected()) {
			description = folderName + " + sub folders";
		}
		CheckWavFileHeaders.showDialog(parentDialog, description, files);
	}

	public void setParams() {
		OfflineFileParameters p = offlineRawDataStore.getOfflineFileServer().getOfflineFileParameters();
		enableOffline.setSelected(p.enable);
		subFolders.setSelected(p.includeSubFolders);
		recentFolders = new ArrayList<String>(p.getRecentFiles());
		String folder = p.folderName != null ? p.folderName : findOfflineFolderName(p);
		setSelection(folder, p.getSelectedFiles());
		enableControls();
		if (p.enable) {
			listFiles();
		}
	}
	
	public String findOfflineFolderName(OfflineFileParameters p) {
		if (p.folderName != null) {
			return p.folderName;
		}
		// otherwise take the folder name from the main daq parameters.
		if (AcquisitionControl.class.isAssignableFrom(offlineRawDataStore.getClass())) {
			AcquisitionControl daqControl = (AcquisitionControl) offlineRawDataStore;
			DaqSystem daqSystem = daqControl.findDaqSystem(null);
			if (daqSystem == null) {
				return null;
			}
			if (FolderInputSystem.class.isAssignableFrom(daqSystem.getClass())) {
				FolderInputSystem fis = (FolderInputSystem) daqSystem;
				FolderInputParameters fip = fis.getFolderInputParameters();
				return fip.getMostRecentFile();
			}
		}
		return null;
	}
	
	private boolean checkFolder(String file) {
		if (file == null) {
			return false;
		}
		File f = new File(file);
		if (f.exists() == false) {
			return false;
		}
		return true;
	}
	
	public OfflineFileParameters getParams() {
		OfflineFileParameters p = new OfflineFileParameters();
		p.enable = enableOffline.isSelected();
		p.includeSubFolders = subFolders.isSelected();
		p.folderName = folderName;
		p.setSelectedFiles(selectedFiles);
		p.getRecentFiles().addAll(recentFolders);
		if (p.enable) {
			if (p.folderName == null && selectedFiles == null) {
				parentDialog.showWarning("Error in file store", "No storage folder selected");
				return null;
			}
			if (selectedFiles == null) {
				if (checkFolder(p.folderName) == false) {
					String err = String.format("The folder %s does not exist", p.folderName);
					parentDialog.showWarning("Error in file store", err);
					return null;
				}
			}
			else {
				for (int i = 0; i < selectedFiles.length; i++) {
					if (checkFolder(selectedFiles[i]) == false) {
						String err = String.format("The selected file %s does not exist", selectedFiles[i]);
						parentDialog.showWarning("Error in file store", err);
						return null;
					}
				}
			}
		}
		return p;
	}
}
