package Acquisition.sud;

import org.pamguard.x3.sud.Chunk;
import org.pamguard.x3.sud.SudAudioInputStream;
import org.pamguard.x3.sud.SudFileListener;
import org.pamguard.x3.sud.SudProgressListener;

public interface SUDNotificationHandler extends SudFileListener, SudProgressListener {

	/**
	 * A new SUD file input stream has been opened. 
	 * @param sudAudioInputStream
	 */
	public void newSudInputStream(SudAudioInputStream sudAudioInputStream);
	
	/**
	 * SUD stream has closed. 
	 */
	public void sudStreamClosed();
			
	@Override
	public void progress(double arg0, int arg1, int arg2);

	@Override
	public void chunkProcessed(int chunkId, Chunk sudChunk);

	/**
	 * Notification that a new file or folder is selected. This is called when a file
	 * or folder is selected in the dialog, NOT when acquisition starts, so is a good 
	 * opportunity for the SUD Click Detector to work out channel maps and sample rates. 
	 * @param newFile
	 * @param sudAudioStream
	 */
	public void interpretNewFile(String newFile, SudAudioInputStream sudAudioStream);

}
