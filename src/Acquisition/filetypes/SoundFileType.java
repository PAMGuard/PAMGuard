package Acquisition.filetypes;

import java.io.File;
import java.util.List;

import Acquisition.FileInputSystem;
import Acquisition.pamAudio.PamAudioFileFilter;
import PamUtils.worker.filelist.WavFileType;

/**
 * Some functions for the File and Folder input systems to give a bit of 
 * extra functionality / help for different file types. Primarily introduced 
 * to give a couple of extras for sud files. 
 * @author dg50
 *
 */
public abstract class SoundFileType {

	private String fileType;
	
	private PamAudioFileFilter fileFilter = new PamAudioFileFilter();

	public SoundFileType(String fileType) {
		this.fileType = fileType.toLowerCase();
	}
	
	/**
	 * Work out if any files of this type are included in the current selection. 
	 * @param fileOrFolder this for a single file. 
	 * @param includeSubfolders used with folders. 
	 * @return true if any exist. 
	 */
	public boolean isFileType(File oneFile) {
		if (oneFile == null) {
			return false;
		}
		if (oneFile.isFile()) {
			return oneFile.getName().toLowerCase().endsWith(fileType);
		}
		
		return false;
	}

	/**
	 * Work out if any files of this type are included in the current selection. 
	 * @param fileOrFolder this for a single file. 
	 * @param includeSubfolders used with folders. 
	 * @return true if any exist. 
	 */
	public boolean hasFileType(List<WavFileType> files) {
		if (files == null) {
			return false;
		}
		for (File aFile : files) {
			if (isFileType(aFile)) {
				return true;
			}
		}
		
		return false;
	}

	public abstract void selected(FileInputSystem fileInputSystem);

}
