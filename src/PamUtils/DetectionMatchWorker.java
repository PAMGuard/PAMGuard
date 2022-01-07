package PamUtils;

/**
 * This is intended to be overridden by any class which wants to monitor the progress of a detection match. 
 * @author Jamie Macaulay
 */
public abstract class DetectionMatchWorker extends Thread{
	
	private int prog=0; 
	private boolean cancel=false;
	

	private boolean done=false; 

	public void setMatchProgress(int prog){
		this.prog=prog; 
	}
	
	public int getMatchProgress(){
		return prog; 
	}
	
	public void cancel(boolean cancel){
		this.cancel=cancel;
		this.done=cancel; 
	}
	
	public boolean isCancelled() {
		return cancel;
	}
	
	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	 
		
}