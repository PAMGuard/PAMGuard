package Array.sensors;

import java.awt.Window;

public interface ArrayDisplayParamsProvider {

	public ArrayDisplayParameters getDisplayParameters();
	
	public void setDisplayParameters(ArrayDisplayParameters displayParameters);
	
	public boolean showDisplayParamsDialog(Window window);
	
}
