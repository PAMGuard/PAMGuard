package Array.swing.sidepanel;

import java.awt.GridLayout;

import javax.swing.JComponent;

import Array.sensors.ArraySensorFieldType;
import GPS.GpsData;
import PamView.panel.PamPanel;

public class ArrayLineGraphs extends ArrayDisplay {

	private PamPanel mainPanel;
	
	private SmallAngleGraph[] smallGraphs;
	
	public ArrayLineGraphs() {
		super();
		mainPanel = new PamPanel();
		mainPanel.setLayout(new GridLayout(4, 1));
		ArraySensorFieldType[] fieldTypes = ArraySensorFieldType.values();
		double[] aRange = {-90., 90.}; 
		smallGraphs = new SmallAngleGraph[fieldTypes.length];
		for (int i = 0; i < fieldTypes.length; i++) {
			smallGraphs[i] = new SmallAngleGraph(fieldTypes[i].name(), aRange);
			mainPanel.add(smallGraphs[i].getMainPanel());
		}
		
	}

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}

	@Override
	public void updateData(int streamerMap, long timeMilliseconds, GpsData gpsData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateLayout(int streamerMap) {
		// TODO Auto-generated method stub
		
	}
	
	

}
