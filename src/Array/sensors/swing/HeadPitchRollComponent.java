package Array.sensors.swing;

import java.awt.BorderLayout;

import Array.Streamer;
import Array.sensors.ArrayDisplayParamsProvider;
import GPS.GpsData;
import PamController.SettingsNameProvider;
import PamguardMVC.debug.Debug;

/**
 * @author dg50
 *
 */
public class HeadPitchRollComponent extends ArrayDimComponent {
	/*
	 * 
	 * These things tend to get made and remade quite a lot, so best they don't hold their own settings
	 * instead, include settings in some kind of parent. 
	 */
	private PitchRollComponent prComponent;
	private HeadingComponent headComponent;
	private DepthComponent depthComponent;
	private ArrayDisplayParamsProvider paramsProvider;

	public HeadPitchRollComponent(ArrayDisplayParamsProvider paramsProvider, int streamerMap) {
		super(streamerMap);
		this.paramsProvider = paramsProvider;
		this.setLayout(new BorderLayout());
		prComponent = new PitchRollComponent(paramsProvider, streamerMap);
		headComponent = new HeadingComponent(paramsProvider, streamerMap);
		depthComponent = new DepthComponent(paramsProvider, streamerMap);
		add(BorderLayout.NORTH, headComponent);
		add(BorderLayout.CENTER, prComponent);
		add(BorderLayout.EAST, depthComponent);
	}

	public void updateStreamer(int iStreamer, GpsData gpsData) {
		//		if (gpsData == null) {
		//			return;
		//		}

		if (headComponent != null) {
			headComponent.setSensorData(iStreamer, gpsData);
		}

		if (prComponent != null) {
			prComponent.setSensorData(iStreamer, gpsData);
		}

		if (depthComponent != null) {
			depthComponent.setSensorData(iStreamer, gpsData);
		}
	}

}
