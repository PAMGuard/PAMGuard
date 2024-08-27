package PamUtils;

import java.io.File;
import java.io.IOException;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT;

/**
 * Functions connected with storing data files. 
 * @author Doug Gillespie
 *
 */
public class FileFunctions {

	/**
	 * Get and create a folder for output file storage. 
	 * @param base base name for folder system
	 * @param timeMillis current time in millis
	 * @param datedSubFolders whether the returned name should include a sub folder
	 * @param autoCreate whether the folder should eb created if it doesn't exist. 
	 * @return new folder name, or null if autocreate is set and the folder couldn't be created. 
	 */
	public static File getStorageFileFolder(String base, long timeMillis, boolean datedSubFolders, boolean autoCreate) {
		File folder;
		if (datedSubFolders) {
			folder = new File(base + File.separator + PamCalendar.formatFileDate(timeMillis));
		}
		else {
			folder = new File(base);
		}
		if (autoCreate) {
			if (!folder.exists()) {
				folder.mkdirs();
				if (!folder.exists()) {
					return null;
				}
			}
		}
		setNonIndexingBit(folder);
		return folder;
	}

	/**
	 * Create a folder that is not indexed by the Windows indexing system.
	 * @param path the full path of the folder to create
	 * @return the new folder, or null if something went wrong with the folder creation.  Note that if
	 * setting the non-indexing bit did not work, the new folder will still be returned
	 */
	public static File createNonIndexedFolder(String path) {
		File folder = new File(path);
		if (!folder.exists()) {
			folder.mkdirs();
			if (!folder.exists()) {
				return null;
			}
		}

		if (isWindows()) {
			//breaks non windows without check
			setNonIndexingBit(folder);
		}
		return folder;
	}


	public static boolean isWindows() {
		return (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0);
	}

	/**
	 * Tell Windows not to index the file/folder passed to this method.  This uses jna functions to
	 * access the file/folder attributes.  Thanks to StackOverflow user Boann for the basic jna code he posted here:</br>
	 * https://stackoverflow.com/questions/19401889/java-ntfs-compression-attribute</br>
	 * Like compression, indexing is also one of the NTFS attributes and not visible through the java.nio file/folder methods.
	 * <p>Note that if this method fails, there is no 
	 * @param file the file or folder to set the bit on
	 * @return whether or not the operation was a success
	 */
	public static boolean setNonIndexingBit(File file) {
		if (!isWindows() || true) {
			return false; 
		}
		boolean success = false;
		try {
			int theAttrib = FileFunctions.getAttributes(file);
			DWORD attribWithSetBit = new DWORD (theAttrib | WinNT.FILE_ATTRIBUTE_NOT_CONTENT_INDEXED);
			FileFunctions.setAttributes(file, attribWithSetBit);
			success = true;
		} catch (Exception e) {
			System.out.println("Warning - was not able to disable Windows indexing on " + file.getAbsolutePath());
			e.printStackTrace();
		}
		return success;
	}

	private static String pathString(File file) {
		// "\\?\" is a Windows API thing that enables paths longer than 260 chars
		return "\\\\?\\" + file.getAbsolutePath();
	}

	private static int getAttributes(File file) throws Exception {
		try {
		int attrib = Kernel32.INSTANCE.GetFileAttributes(pathString(file));
		if (attrib == WinBase.INVALID_FILE_ATTRIBUTES) {
			throw new IOException("Unable to read file attributes of " + file);
		}
		return attrib;
		}
		catch (Error e) {
			System.out.println("Error in FileFunctions.getAttributes: " + e.getMessage());
			return 0;
		}
	}

	private static void setAttributes(File file, DWORD attrib) {
		Kernel32.INSTANCE.SetFileAttributes(pathString(file), attrib);
	}

	public static boolean isNotIndexed(File file) throws Exception {
		return (getAttributes(file) & WinNT.FILE_ATTRIBUTE_NOT_CONTENT_INDEXED) != 0;
	}

}
