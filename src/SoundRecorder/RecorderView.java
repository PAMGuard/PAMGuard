package SoundRecorder;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public interface RecorderView {


	
	static public final int BUTTON_OFF = 1;
	static public final int BUTTON_AUTO = 2;
	static public final int BUTTON_START = 3;
	static public final int BUTTON_START_BUFFERED = 4; 
	
	public abstract void newParams();

	public abstract void newData(PamDataBlock dataBlock, PamDataUnit dataUnit);
	
	public abstract void setButtonStates(int command);
	
	public abstract void sayStatus(String status);
	
	public void enableRecording(boolean enable);
	
	public void enableRecordingControl(boolean enable);
	

}