package Acquisition.filetypes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import Acquisition.AcquisitionControl;
import PamUtils.worker.filelist.WavFileType;

/**
 * List of SoundFileType objects. 
 * @author dg50
 *
 */
public class SoundFileTypes {

	private AcquisitionControl acquisitionControl;
	
	private ArrayList<SoundFileType> availableTypes = new ArrayList<>();

	public SoundFileTypes(AcquisitionControl acquisitionControl) {
		this.acquisitionControl = acquisitionControl;
		availableTypes.add(new SUDFileType());
	}

	/**
	 * Get a list of used file types. Reaslistically this 
	 * can only return one value. 
	 * @param aFile
	 * @return
	 */
	public SoundFileType getFileType(File aFile) {
		ArrayList<SoundFileType> usedTypes = new ArrayList<>();
		for (SoundFileType aType : availableTypes) {
			if (aType.isFileType(aFile)) {
				return aType;
			}
		}
		return null;
	}
	
	/**
	 * Get a list of used file types. Reaslistically this 
	 * can only return one value but possibly it's useful to have it in the same
	 * format as the multiple file version. . 
	 * @param aFile
	 * @return
	 */
	public List<SoundFileType> getUsedTypes(File aFile) {
		ArrayList<SoundFileType> usedTypes = new ArrayList<>();
		for (SoundFileType aType : availableTypes) {
			if (aType.isFileType(aFile)) {
				usedTypes.add(aType);
			}
		}
		return usedTypes;
	}

	/**
	 * Get a list of used file types for the folder Input System. 
	 * @param aFile
	 * @return
	 */
	public List<SoundFileType> getUsedTypes(List<WavFileType> aFile) {
		ArrayList<SoundFileType> usedTypes = new ArrayList<>();
		for (SoundFileType aType : availableTypes) {
			if (aType.hasFileType(aFile)) {
				usedTypes.add(aType);
			}
		}
		return usedTypes;
	}

}
