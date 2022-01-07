package PamView.dialog;

import java.awt.Window;
import java.io.File;

import javax.swing.JFileChooser;

import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;

/**
 * Some useful functions to open up a File Browser
 * @author JamieMacaulay
 *
 */
public class PamFileBrowser {
	
	public final static int OPEN_FILE=0; 
	
	public final static int SAVE_FILE=1; 
	/**
	 * Opens a file browser for .csv files. 
	 * @param parentFrame- the frame
	 * @param dir- directory the file browser begins with. 
	 * @param type. Type of file browser. Generally an PAMFileBrowser.OPEN_FILE or PAMFileBrowser.SAVE. 
	 * @return String pathname of selected file. 
	 */
	public static String csvFileBrowser(Window parentFrame, String dir, int type){
		
		return fileBrowser( parentFrame,  dir,  type,".csv");
		
	}
	
	/**
	 * Opens a file browser for a file. 
	 * @param parentFrame- the frame
	 * @param dir- directory the file browser begins with. 
	 * @param type. Type of file browser. Generally an PAMFileBrowser.OPEN_FILE or PAMFileBrowser.SAVE. 
	 * @param extension- file extension to use
	 * @return String pathname of selected file. 
	 * 
	 */
	public static String fileBrowser(Window parentFrame, String dir, int type, String extension){
		String[] extensions={extension};
		if (dir != null && dir.length() > 0) {
			try {
				File curFile = new File(dir);
				if (curFile.isDirectory() == false) {
					curFile = curFile.getParentFile();
					dir = curFile.getAbsolutePath();
				}
			}
			catch (Exception e) {

			}
		}
		return fileBrowser( parentFrame,  dir,  type, extensions);
	}
	
	/**
	 * Opens a file browser for a file. 
	 * @param parentFrame- the frame
	 * @param dir- directory the file browser begins with. 
	 * @param type. Type of file browser. Generally an PAMFileBrowser.OPEN_FILE or PAMFileBrowser.SAVE. 
	 * @param extension- file extension to use
	 * @param description File Description
	 * @return String pathname of selected file. 
	 * 
	 */
	public static String fileBrowser(Window parentFrame, String dir, int type, String extension, String description){
		String[] extensions={extension};
		return fileBrowser( parentFrame,  dir,  type, extensions, description);
	}
	
	
	/**
	 * Opens a file browser for a file. 
	 * @param parentFrame- the frame
	 * @param dir- directory the file browser begins with. 
	 * @param type. Type of file browser. Generally an PAMFileBrowser.OPEN_FILE or PAMFileBrowser.SAVE. 
	 * @param extensions- file extensions to use
	 * @return String pathname of selected file. 
	 * 
	 */ 
	public static String fileBrowser(Window parentFrame, String dir, int type, String[] fileExtensions){
		return fileBrowser(parentFrame, dir, type, fileExtensions, null);
	}
	
	/**
	 * Opens a file browser for a file. 
	 * @param parentFrame- the frame
	 * @param dir- directory the file browser begins with. 
	 * @param type. Type of file browser. Generally an PAMFileBrowser.OPEN_FILE or PAMFileBrowser.SAVE. 
	 * @param fileExtensions- file extension to use
	 * @param fileDescription- file description
	 * @return String pathname of selected file. 
	 * 
	 */ 
	public static String fileBrowser(Window parentFrame, String dir, int type, String[] fileExtensions, String fileDescription){
			JFileChooser fileChooser = new PamFileChooser();
			if (fileDescription == null) {
				fileDescription = fileExtensions[0] + " files";
			}
			PamFileFilter fileFilter = new PamFileFilter(fileDescription, fileExtensions[0]);		
			for (int i=1; i<fileExtensions.length; i++){
				fileFilter.addFileType(fileExtensions[i]);
			}
			fileChooser.setFileFilter(fileFilter);

			if (dir!=null){
				File directory=new File(dir);
				if (directory.isDirectory()) fileChooser.setCurrentDirectory(directory);
			}
			
			int state;
			if (type==SAVE_FILE) state = fileChooser.showSaveDialog(parentFrame);
			else state= fileChooser.showOpenDialog(parentFrame);
			
			if (state == JFileChooser.APPROVE_OPTION) {
				File currFile = fileChooser.getSelectedFile();
//				if (currFile.getAbsolutePath().endsWith(fileExtension)==false){
//					return (currFile.getAbsolutePath()+fileExtension);
//				}
//				else{
				return currFile.getAbsolutePath();
//				}
			}
			return null;
	}
	
}
