package PamUtils.worker;

import java.awt.Window;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import PamController.PamGUIManager;

/**
 * Wrapper around a SwingWorker so it's easy to launch a background thread that will 
 * update a modal dialog with a progress bar and some text
 * 
 * @author Doug Gillespie
 *
 */
public class PamWorker<T> {


	private PamWorkWrapper<T> pamWorkWrapper;

	/**
	 * Swing dialog which shows progress
	 */
	private PamWorkDialog pamWorkDialog;
	
	/**
	 * Progress properties. 
	 */
	private PamWorkerProgressFX pamWorkProgress; 


	private BackgroundWorker backgroundWorker;

	private volatile boolean running = false;

	/**
	 * 
	 * @param pamWorkWrapper wrapper round callback and worker functions
	 * @param parentFrame parent frame of progress dialog
	 * @param nTextRows number of rows of text for progress messages
	 * @param title dialog title
	 */
	public PamWorker(PamWorkWrapper<T> pamWorkWrapper, Window parentFrame, int nTextRows, String title) {
		this.pamWorkWrapper = pamWorkWrapper;
		if (PamGUIManager.getGUIType() == PamGUIManager.NOGUI) {
			
		}
		else if (PamGUIManager.isFX()) {
			//TODO - make a node that can be extracted here. 
			pamWorkProgress = new PamWorkerProgressFX(); 
		}
		else {
			pamWorkDialog = new PamWorkDialog(parentFrame, nTextRows, title);
		}
		backgroundWorker = new BackgroundWorker();
		backgroundWorker.execute();
		if (pamWorkDialog!=null) pamWorkDialog.setVisible(true);
	}
	
	
	/**
	 * Create a PamWorker without a swing dialog. 
	 * @param pamWorkWrapper wrapper round callback and worker functions
	 * @param parentFrame parent frame of progress dialog
	 * @param nTextRows number of rows of text for progress messages
	 * @param title dialog title
	 */
	public PamWorker(PamWorkWrapper<T> pamWorkWrapper) {
		this.pamWorkWrapper = pamWorkWrapper;
		if (PamGUIManager.isFX()) {
			//TODO - make a node that can be extracted here. 
			pamWorkProgress = new PamWorkerProgressFX(); 
		}
		backgroundWorker = new BackgroundWorker();
	}

	/**
	 * Start the worker thread. 
	 * @return true of the thread is started. flase if the thread is done, cancelled or running. 
	 */
	public boolean start() {
		if (backgroundWorker.isCancelled() || backgroundWorker.isDone() || running) return false; 
		backgroundWorker.execute();
		return true; 
	}

	protected class BackgroundWorker extends SwingWorker<T, PamWorkProgressMessage> {

		@Override
		protected T doInBackground() throws Exception {
			running = true;
			T ans = pamWorkWrapper.runBackgroundTask(PamWorker.this);
			running = false;
			return ans;
		}

		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#done()
		 */
		@Override
		protected void done() {
			if (pamWorkDialog!=null) {
				pamWorkDialog.setVisible(false);
				pamWorkDialog.dispose();
			}
			if (pamWorkProgress!=null) {
				pamWorkProgress.updateProgress(1.0); 
			}
			T result = null;
			try {
				result = get();
			} catch (InterruptedException | ExecutionException e1) {
				e1.printStackTrace();
			}
			//System.out.println("Task finished: "); 

			pamWorkWrapper.taskFinished(result);
		}

		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#process(java.util.List)
		 */
		@Override
		protected void process(List<PamWorkProgressMessage> chunks) {
			for (PamWorkProgressMessage msg:chunks) {
				if (pamWorkDialog!=null) {
					pamWorkDialog.update(msg);
				}
				if (pamWorkProgress!=null) {
					pamWorkProgress.update(msg); 
				}
			}
		}

		public void ppublish(PamWorkProgressMessage progressMessage) {
			publish(progressMessage);
		}

	}



	public void update(PamWorkProgressMessage progressMessage) {
		backgroundWorker.ppublish(progressMessage);
	}
	
	
	/**
	 * Get the PAM progress property. 
	 * @return class with the progress properties of the worker. 
	 */
	public PamWorkerProgressFX getPamWorkProgress() {
		return pamWorkProgress;
	}

}
