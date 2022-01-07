package Array.swing.sidepanel;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.border.TitledBorder;

import Array.ArrayManager;
import Array.sensors.ArrayDisplayParamsProvider;
import PamUtils.PamUtils;
import PamView.PamSidePanel;
import PamView.panel.PamPanel;

public class ArraySidePanel implements PamSidePanel {
	
	private ArrayDisplayPanel arrayDisplayPanel;
	
	private PamPanel mainPanel;

	public ArraySidePanel(ArrayDisplayParamsProvider paramsProvider) {
		arrayDisplayPanel = new ArrayDisplayPanel(paramsProvider);
		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER, arrayDisplayPanel.getComponent());
		mainPanel.setBorder(new TitledBorder("Array Geometry"));
	}

	@Override
	public JComponent getPanel() {
		return mainPanel;
	}

	@Override
	public void rename(String arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void update() {
		int nStreamers = ArrayManager.getArrayManager().getCurrentArray().getNumStreamers();
		arrayDisplayPanel.updateLayout(PamUtils.makeChannelMap(nStreamers));
		arrayDisplayPanel.subscribeSensorData();
//		arrayDisplayPanel.updateData();
	}

	public void updateViewerTime(long timeInMillis) {
		int nStreamers = ArrayManager.getArrayManager().getCurrentArray().getNumStreamers();
		int arrayMap = PamUtils.makeChannelMap(nStreamers);
		arrayDisplayPanel.updateData(arrayMap, timeInMillis, null);
		
	}

}
