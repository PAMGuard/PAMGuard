package PamUtils;

import PamUtils.PamFileFilter;

public class PamAudioFileFilter extends PamFileFilter {

	public PamAudioFileFilter() {
		/**
		 * PamfileFilter converts everything to lower case, so no need to have upper and lower versions of
		 * each ending.  
		 */
		super("Audio Files", ".wav");
//		addFileType(".WAV");
		addFileType(".aif");
		addFileType(".aiff");
//		addFileType(".AIF");
//		addFileType(".AIFF");
//		addFileType(".FLAC");
		addFileType(".flac");
		addFileType(".sud");
	}

}
