package pamViewFX.pamTask;

import PamUtils.worker.PamWorkProgressMessage;

/**
 * Simple implementation of PAMTaskUpdate which returns 100% by default. 
 * @author Jamie Macaulay
 *
 */
public class SimplePamTaskUpdate extends PamTaskUpdate {
	
	private String name = "task";
	
	private int status =  PamTaskUpdate.STATUS_DONE;
	
	private double progress = 1; 
	
	public SimplePamTaskUpdate(String name, int status) {
		this.name=name;
		this.status=status;
	}
	
	
	public SimplePamTaskUpdate(PamWorkProgressMessage message) {
		if (message.textLines!=null && message.textLines.length>0) this.name=message.textLines[0];
		this.status=status;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public double getProgress() {
		return progress;
	}
	
	/**
	 * Set the progress of the task. 
	 * @param progress - progress from 0 to 1 where 1 is 100%
	 */
	public void setProgress(double progress) {
		this.progress=progress;
	}

}
