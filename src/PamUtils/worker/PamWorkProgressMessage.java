package PamUtils.worker;

public class PamWorkProgressMessage {
	
	/**
	 * Create a message with a variable number of text arguments to be 
	 * displayed in the PamWorkDialog, one per line. Note that if you set
	 * a value to null, then it will be left as is. To clear a line set it
	 * to "". Similarly progress will be left as is if it's null.
	 * @param progress 0 to 100; -1 = indeterminate, null = keep old value. 
	 * @param txtLines lines of text. 
	 */
	public PamWorkProgressMessage(Integer progress, String ...txtLines) {
		this.progress = progress;
		this.textLines = txtLines;
	}

	public Integer progress;
	
	public String[] textLines;

}
