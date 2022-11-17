package Acquisition.sud;

import java.util.ArrayList;

import org.pamguard.x3.sud.Chunk;
import org.pamguard.x3.sud.SudAudioInputStream;
import org.pamguard.x3.sud.SudFileListener;
import org.pamguard.x3.sud.SudProgressListener;

/**
 * Class to handle appropriate notifications for SUD files, which go a bit
 * beyond what's handled in the chunk notifications. 
 * @author dg50
 *
 */
public class SUDNotificationManager implements SUDNotificationHandler {
	
	private ArrayList<SUDNotificationHandler> handlers = new ArrayList();
	
	public void addNotificationHandler(SUDNotificationHandler sudNotificationHandler) {
		if (handlers.contains(sudNotificationHandler)) {
			return;
		}
		handlers.add(sudNotificationHandler);
	}

	public boolean removeNotificationHandler(SUDNotificationHandler sudNotificationHandler) {
		return handlers.remove(sudNotificationHandler);
	}
	
	@Override
	public void newSudInputStream(SudAudioInputStream sudAudioInputStream) {
		for (SUDNotificationHandler handler : handlers) {
			handler.newSudInputStream(sudAudioInputStream);
		}
	}

	@Override
	public void sudStreamClosed() {
		for (SUDNotificationHandler handler : handlers) {
			handler.sudStreamClosed();
		}		
	}

	@Override
	public void progress(double arg0, int arg1, int arg2) {
		for (SUDNotificationHandler handler : handlers) {
			handler.progress(arg0, arg1, arg2);
		}
	}

	@Override
	public void chunkProcessed(int chunkId, Chunk sudChunk) {
		for (SUDNotificationHandler handler : handlers) {
			handler.chunkProcessed(chunkId, sudChunk);
		}
	}

	@Override
	public void interpretNewFile(String newFile, SudAudioInputStream sudAudioStream) {
		for (SUDNotificationHandler handler : handlers) {
		handler.interpretNewFile(newFile, sudAudioStream);
		}
	}

}
