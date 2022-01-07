package annotation.calcs.wav;

import annotation.DataAnnotation;
import annotation.DataAnnotationType;

public class WavAnnotation extends DataAnnotation<DataAnnotationType> {

	private String wavFolder;
	private String wavPrefix;
	private String exportedWavFileName;
	
	public WavAnnotation(DataAnnotationType dataAnnotationType) {
		super(dataAnnotationType);
	}

	/**
	 * @return the full path of the wav file
	 */
	public String getWavFolderName() {
		if (wavFolder == null)
			return "..";
		return wavFolder;
	}

	/**
	 * @param String containing full path of the wav file for this annotation
	 */
	public void setWavFolderName(String folder) {
		this.wavFolder = folder;
	}

	/**
	 * @return the prefix of the wav file name
	 */
	public String getWavPrefix() {
		if (wavPrefix == null)
			return "";
		return wavPrefix;
	}

	/**
	 * @param String containing the prefix of the wav file for this annotation
	 */
	public void setWavPrefix(String prefix) {
		wavPrefix = prefix;
	}

	/**
	 * @return the full path of the wav file
	 */
	public String getExportedWavFileName() {
		if (exportedWavFileName == null)
			return "";
		return exportedWavFileName;
	}

	/**
	 * @param String containing full path of the wav file for this annotation
	 */
	public void setExportedWavFileName(String fn) {
		exportedWavFileName = fn;
		
	}
	

}
