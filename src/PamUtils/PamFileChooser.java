/**
 * 
 */
package PamUtils;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import PamController.PamFolders;

/**
 * 
 * Modified file/folder chooser for use throughout PAMGuard which by default will open 
 * in the folder containing the psfx or database file, rather than user.documents
 * 
 * @author dg50
 *
 */
public class PamFileChooser extends JFileChooser {

	private static final long serialVersionUID = 1L;

	{
		setAcceptAllFileFilterUsed(false);
		setCurrentDirectory(null);
	}
	/**
	 * 
	 */
	public PamFileChooser() {	
		super();
	}

	/**
	 * @param currentDirectoryPath
	 */
	public PamFileChooser(String currentDirectoryPath) {
		super(currentDirectoryPath);
	}

	/**
	 * @param currentDirectory
	 */
	public PamFileChooser(File currentDirectory) {
		super(currentDirectory);
	}

	/**
	 * @param fsv
	 */
	public PamFileChooser(FileSystemView fsv) {
		super(fsv);
	}

	/**
	 * @param currentDirectory
	 * @param fsv
	 */
	public PamFileChooser(File currentDirectory, FileSystemView fsv) {
		super(currentDirectory, fsv);
	}

	/**
	 * @param currentDirectoryPath
	 * @param fsv
	 */
	public PamFileChooser(String currentDirectoryPath, FileSystemView fsv) {
		super(currentDirectoryPath, fsv);//		setAcceptAllFileFilterUsed(false);
	}

	/**
	 * Override this so that when given a file, if the file doesn't exist, it tries 
	 * to at least start in the same folder, or if it's  a new config and 
	 * file is null, it starts in the folder containing the psfx, not user.home 
	 */
	@Override
	public void setSelectedFile(File file) {
		if (isShowing()) {
			super.setSelectedFile(file);
			return;
		}
		/**
		 * Modified behaviour for when the dialog is called before it's shown. 
		 * Has to have normal behaviour once it's visible or it's impossible to 
		 * create new folders and files 
		 */
		File startPlan = PamFolders.getFileChooserPath(file);
		if (startPlan != null) {
			if (startPlan.isDirectory()) {
				super.setCurrentDirectory(startPlan);
			}
			else {
				super.setSelectedFile(startPlan);
			}
		}
	}

	@Override
	public void setCurrentDirectory(File dir) {
		if (isShowing()) {
			super.setCurrentDirectory(dir);
			return;
		}
		/**
		 * Modified behaviour for when the dialog is called before it's shown. 
		 * Has to have normal behaviour once it's visible or it's impossible to 
		 * create new folders and files 
		 */
		if (dir != null && dir.isFile()) {
			dir = dir.getParentFile();
		}
		File startPlan = PamFolders.getFileChooserPath(dir);
		if (startPlan != null) {
			if (startPlan.isDirectory()) {
				super.setCurrentDirectory(startPlan);
			}
		}
	}

}
