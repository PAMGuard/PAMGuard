package analoginput;

import java.util.List;

import PamView.dialog.PamDialogPanel;

public interface AnalogDeviceType {
	
	public String getDeviceType();
	
	public int getNumChannels();
	
	public List<AnalogRangeData> getAvailableRanges(int analogChan);
	
	public boolean setChannelRange(AnalogRangeData analogRange);
	
	public AnalogSensorData readData(int item) throws AnalogReadException;
	
	public PamDialogPanel getDevicePanel();
	
	public AnalogDeviceParams getDeviceParams();
	
	public void setDeviceParams(AnalogDeviceParams deviceParams);

	public void prepareDevice();
	
}
