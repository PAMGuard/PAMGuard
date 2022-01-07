package Array.swing.sidepanel;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;

import Array.ArrayManager;
import Array.PamArray;
import Array.Streamer;
import Array.sensors.ArrayDisplayParamsProvider;
import Array.sensors.swing.HeadPitchRollComponent;
import GPS.GpsData;
import PamUtils.PamUtils;
import PamView.panel.PamPanel;

public class ArrayGraphicDisplay extends ArrayDisplay {

	public PamPanel mainPanel;
	
	private HeadPitchRollComponent hprComponents;

	private int numStreamers;

	private ArrayDisplayParamsProvider paramsProvider;

	public ArrayGraphicDisplay(ArrayDisplayParamsProvider paramsProvider) {
		this.paramsProvider = paramsProvider;
		mainPanel = new PamPanel();
	}

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}

	@Override
	public void updateData(int iStreamer, long timeMilliseconds, GpsData gpsData) {
		if (hprComponents == null) {
			return;
		}
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
//		int numStreamers = array.getNumStreamers();
//		int n = Math.min(numStreamers, hprComponents.length);
//		int iStreamer = PamUtils.getSingleChannel(streamerMap);
//		if (iStreamer >= 0 && iStreamer < hprComponents.length) {
			hprComponents.updateStreamer(iStreamer, gpsData);
//		}
		
	}

	@Override
	public void updateLayout(int streamerMap) {

		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		int numStreamers = array.getNumStreamers();
//		if (this.numStreamers == numStreamers) {
//			return;
//		}
		this.numStreamers = numStreamers;
		
		mainPanel.removeAll();
		mainPanel.setLayout(new GridLayout(1, 1));
		//		hprComponents = new HeadPitchRollComponent[numStreamers];
		//		for (int i = 0; i < numStreamers; i++) {
		//			Streamer aStreamer = array.getStreamer(i);
		hprComponents = new HeadPitchRollComponent(paramsProvider, PamUtils.makeChannelMap(numStreamers));
		PamPanel bP = new PamPanel(new BorderLayout());
		//			bP.setBorder(new TitledBorder(aStreamer.getStreamerName()));
		bP.add(BorderLayout.CENTER, hprComponents);
		mainPanel.add(bP);
		//		}
	}

}
