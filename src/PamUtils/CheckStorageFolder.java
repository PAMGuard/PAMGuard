package PamUtils;

import java.io.File;

import javax.swing.JOptionPane;

/**
 * Class for checking a storage folder exists and optionally creating it
 * @author Doug Gillespie
 *
 */
public class CheckStorageFolder {

	private String moduleName;

	public CheckStorageFolder(String moduleName) {
		super();
		this.moduleName = moduleName;
	}

	public boolean checkPath(String pathName, boolean autoCreate) {

		if (pathName == null) {
			pathName = "";
		}
		File file = new File(pathName);
		if (file.exists() && file.isDirectory()) {
			return true;
		} 
		else if (file.exists() && !file.isDirectory()) {
			JOptionPane
			.showMessageDialog(null, pathName
					+ " is a file, not a folder \nCreate a new folder for data storage",
					moduleName, JOptionPane.WARNING_MESSAGE);
			return false;
		}
		else if (!file.exists() & autoCreate) {
			int ans = JOptionPane.showOptionDialog(null, "Folder " + pathName
					+ " does not exist. \nWould you like to create it ?",
					moduleName,
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
					null, null, null);
			if (ans == JOptionPane.NO_OPTION) {
				return false;
			}
			if (ans == JOptionPane.YES_OPTION) {
				try {
					if (!file.mkdirs()) {
						return checkPath(pathName, false);
					}
					FileFunctions.setNonIndexingBit(file);
				} catch (SecurityException ex) {
					ex.printStackTrace();
				}
			}
		} 
		else {
			return false;
		}

		return true;
	}
}
