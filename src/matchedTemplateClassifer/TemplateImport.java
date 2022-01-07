package matchedTemplateClassifer;

import java.io.File;

/**
 * Interface fro importing a template 
 * @author Jamie Macaulay
 *
 */
public interface TemplateImport {
	
	public final static int NO_ERROR = 0;

	
	/**
	 * The length of the waveform is too small. 
	 */
	public final static int ERROR_WAVEFORM_LENGTH = 1;
	
	/**
	 *The file format is somehow incorrect. 
	 * 
	 */
	public final static int INCORRECT_FILE_FORMAT = 2;


	/**
	 * The minimum allowed waveform length
	 */
	public static final int MIN_WAVEFORM_LENGTH = 5;

	
	/**
	 * Import template 
	 * @param filePath - the file path
	 * @return - the match template.
	 */
	public MatchTemplate importTemplate(File filePath);
	
	/**
	 * File extensions which can be used with this. 
	 * @return the file extensions. 
	 */
	public String[] getExtension(); 
	
	/**
	 * Get the error code flag. 0 if there was no error. 
	 * @return the error code flag. 
	 */
	public int getErrorCode();

}
