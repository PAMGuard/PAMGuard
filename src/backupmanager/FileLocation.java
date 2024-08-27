package backupmanager;

import java.io.File;
import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * Information on a file path or folder path that can be used with the 
 * backup system 
 * @author dg50
 *
 */
public class FileLocation implements Serializable, Cloneable, ManagedParameters {
	
//	public static final int SEL_FOLDERS = 0x1;
//	
//	public static final int SEL_FILES = 0x2;

	public static final long serialVersionUID = 1L;

	public String path;
	
	public String mask;
	
//	public int fileOrFolders = SEL_FOLDERS;
	
	public boolean canEditPath = true;
	
	public boolean canEditMask = false; 

	@Override
	public FileLocation clone() {
		try {
			return (FileLocation) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}
	
	/**
	 * Get the amount of space at the specified path or null if the 
	 * path is not set, etc.
	 * @return free space in bytes
	 */
	public Long getFreeSpace() {
		if (path == null) {
			return null;
		}
		try {
			File fPath = new File(path);
			if (!fPath.exists()) {
				fPath = fPath.getParentFile();
			}
			if (!fPath.exists()) {
				return null;
			}
			return fPath.getFreeSpace();
		}
		catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Get the file system root. 
	 * @return
	 */
	public String getFileRoot() {
		if (path == null) {
			return null;
		}
		try {
			File fPath = new File(path);
			int firstSep = path.indexOf(File.separatorChar);
			if (firstSep > 0) {
				return path.substring(0, firstSep);
			}
			else {
				return path;
			}
		}
		catch (Exception e) {
			return null;
		}
		
	}

}
