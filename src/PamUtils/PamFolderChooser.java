package PamUtils;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

/**
 * Chooser for selecting folders, which can show the files in the folder, but will only 
 * save if the folder is selected not a file. 
 * If a single file is selected, it can manage to select the path that contains that single file. 
 * @author dg50
 *
 */
public class PamFolderChooser extends PamFileChooser {
	/*
	 * Using suggestions at https://stackoverflow.com/questions/2883447/jfilechooser-select-directory-but-show-files
	 * but it is struggling if you want to save when highlighting a folder, since it tends to open that folder rather
	 * then approve. May need to do some more overriding. 
	 * 
	 * Not really working unless we can override getApproveSelectionAction in the UI (WindowsFileChooserUI) so 
	 * that when the Approve button is clicked it will close the dialog, rather than opening that folder to 
	 * view contents, and also to allow it to OK the dialog when Approve is selected in any dialog is pressed in 
	 * and also setDirectorySelected(... to keep the text from changing t "Open"
	 * Might be very hard to do this for cross platform since we're diving into the UI. 
	 */

	private boolean checkParent = false;
	
	
	public PamFolderChooser() {
	}

	public PamFolderChooser(String currentDirectoryPath) {
		super(currentDirectoryPath);
		setup();
	}

	public PamFolderChooser(File currentDirectory) {
		super(currentDirectory);
		setup();
	}

	public PamFolderChooser(FileSystemView fsv) {
		super(fsv);
		setup();
	}

	public PamFolderChooser(File currentDirectory, FileSystemView fsv) {
		super(currentDirectory, fsv);
		setup();
	}

	public PamFolderChooser(String currentDirectoryPath, FileSystemView fsv) {
		super(currentDirectoryPath, fsv);
		setup();
	}
	
	private void setup() {
		setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	}
	
	

	@Override
	public void approveSelection() {
		File f = getSelectedFile();
		if (f == null) {
			return;
		}
		if (f.isFile()) {
			if (checkParent) {
				// go one up and see how that foes. 
				try {
					f = f.getParentFile();
					if (f.isDirectory()) {
						setSelectedFile(f);
						super.approveSelection();
					}
				}
				catch (Exception e) {
					return;
				}
			}
			// that didn't work, didn't find a folder parent, so .... 
			return;
		}
		super.approveSelection();
	}

	/**
	 * If this is set true, then even if a file is clicked on, the dialog will 
	 * return the path to the folder that's holding that file. 
	 * @return the checkParent
	 */
	public boolean isCheckParent() {
		return checkParent;
	}

	/**
	 * If this is set true, then even if a file is clicked on, the dialog will 
	 * return the path to the folder that's holding that file. 
	 * @param checkParent the checkParent to set
	 */
	public void setCheckParent(boolean checkParent) {
		this.checkParent = checkParent;
	}
	
	

}
