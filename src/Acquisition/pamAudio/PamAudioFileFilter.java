package Acquisition.pamAudio;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import PamUtils.PamFileFilter;

/**
 * The file filter for audio files compatible with PAMGuard. 
 * <p>
 * All file extensions from all PamAudioFileLoaders in PamAudioManager are
 * accepted by the file filter. The file filter will aslo discard compressed 
 * audio files if there exists a raw audio file of the same name. 
 * 
 * @author Jamie Macaulay
 *
 */
public class PamAudioFileFilter extends PamFileFilter {

	private PamAudioFileManager pamAudioFileManager;

	public PamAudioFileFilter(PamAudioFileManager pamAudioFileManager) {
		
		/**
		 * PamfileFilter converts everything to lower case, so no need to have upper and lower versions of
		 * each ending.  
		 */
		super("Audio Files", ".wav");
		
		this.pamAudioFileManager = pamAudioFileManager; 

		//add all the file types. 
		for (int i=0; i<pamAudioFileManager.getAudioFileLoaders().size(); i++) {
			for (int j=0; j<pamAudioFileManager.getAudioFileLoaders().get(i).getFileExtensions().size(); j++) {
				if (!pamAudioFileManager.getAudioFileLoaders().get(i).getFileExtensions().get(j).equals(".wav")){
					// already added .wav but add all other file extensions. 
					addFileType(pamAudioFileManager.getAudioFileLoaders().get(i).getFileExtensions().get(j)); 
				}
					
			}; 
		}
	}

	public PamAudioFileFilter() {
		this(PamAudioFileManager.getInstance());
	}
	
	@Override
	public boolean accept(File f) {
		//System.out.println("Accept?: " + super.accept(f) + "  " + f.getName() ); 
		if (super.accept(f)) {
			//need to do an extra check here. Is there a .wav file or other raw wav file
			//that has the same name as another file type? If so we do not want both a compressed
			//and uncompressed file to be read - instead ignore the compressed file and only accept
			//the wav file.  
			
			if (pamAudioFileManager.isExtension(f, pamAudioFileManager.getRawFileLoader()) || f.isDirectory()) {
				//if a raw file then all OK. 
				//System.out.println("Accepted file 1: " + f.getName());
				return true; 
			}
			
			String blankFileName = FilenameUtils.removeExtension(f.getAbsolutePath());
			//System.out.println("Blank file 1: " + blankFileName);

			
			File rawfile; 
			for (int i=0; i<pamAudioFileManager.getRawFileLoader().getFileExtensions().size(); i++) {
				rawfile = new File(blankFileName + pamAudioFileManager.getRawFileLoader().getFileExtensions().get(i)); 
				//System.out.println("Raw file 1: " + blankFileName);

				if (rawfile.exists()) {
					//the raw file will have been picked up by the file filter already or subsequently. So do not
					//use the other file. 
					return false; 
				}
			}
			//System.out.println("Accepted file 2: " + f.getName());
			return true; 
			
		}
		else return false; 
	}


}
