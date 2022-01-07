package Array.swing.sidepanel;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import Array.ArrayManager;
import Array.PamArray;
import Array.Streamer;
import Array.sensors.ArrayDisplayParamsProvider;
import Array.sensors.ArrayParameterType;
import Array.sensors.ArraySensorFieldType;
import GPS.GpsData;
import PamController.PamController;
import PamUtils.PamUtils;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.debug.Debug;

public class ArrayDisplayPanel extends ArrayDisplay {

	private PamPanel mainPanel;
	private ArrayNumberDisplay textPane;
	private ArrayGraphicDisplay graphPane;
	private ArrayLineGraphs lineGraphs;
	private StreamerObserver streamerObserver;
	private ArrayList subscribedBlocks;

	public ArrayDisplayPanel(ArrayDisplayParamsProvider paramsProvider) {
		mainPanel = new PamPanel(new BorderLayout());
		JTabbedPane tabPane = new JTabbedPane();
		textPane = new ArrayNumberDisplay(paramsProvider);
		graphPane = new ArrayGraphicDisplay(paramsProvider);
		lineGraphs = new ArrayLineGraphs();
		mainPanel.add(BorderLayout.CENTER, tabPane);
		tabPane.add("Data", textPane.getComponent());
		tabPane.addTab("Graphs",  graphPane.getComponent());
//		tabPane.addTab("Lines", lineGraphs.getComponent());

		int nStreamers = ArrayManager.getArrayManager().getCurrentArray().getNumStreamers();
		updateLayout(PamUtils.makeChannelMap(nStreamers));
		//		PamController.getInstance().ad
		// no point in this, since streamer block no longer updates with new sensor data. Need to 
		// observer incoming data units and get HPR from them. 
		streamerObserver = new StreamerObserver();
		subscribedBlocks = new ArrayList<>();
		if (PamController.getInstance().isInitializationComplete()) {
			subscribeSensorData();
		}
		//		ArrayManager.getArrayManager().getStreamerDatabBlock().addObserver(new StreamerObserver());
	}


	@Override
	public JComponent getComponent() {
		return mainPanel;
	}

	@Override
	public void updateData(int streamerMap, long timeMilliseconds, GpsData gpsData) {
		// get the gps data representing the latest value of the orientation. 
//		int[] chans = PamUtils.getChannelArray(streamerMap);
		PamArray currArray = ArrayManager.getArrayManager().getCurrentArray();
		int nStreamer = currArray.getNumStreamers();
		for (int i = 0; i < nStreamer; i++) {
			Streamer aStreamer = currArray.getStreamer(i);	
			if (aStreamer == null) {
				continue;
			}
			GpsData streamerPos = aStreamer.getHydrophoneLocator().getStreamerLatLong(timeMilliseconds);
			if (streamerPos != null) {
				textPane.updateData(i, timeMilliseconds, streamerPos);
				graphPane.updateData(i, timeMilliseconds, streamerPos);
			}
		}
	}

	@Override
	public void updateLayout(int streamerMap) {
		textPane.updateLayout(streamerMap);
		graphPane.updateLayout(streamerMap);
	}

	/**
	 * Subscribe to sensor data blocks. 
	 * @return
	 */
	public void subscribeSensorData() {
		//		ArrayList<PamDataBlock> sensorBlocks = PamController.getInstance().getDataBlocks(ArraySensorDataBlock.class, true);
		PamArray currArray = ArrayManager.getArrayManager().getCurrentArray();
		int nStreamers = currArray.getNumStreamers();
		ArraySensorFieldType[] fields = ArraySensorFieldType.values();
		for (int i = 0; i < nStreamers; i++) {
			Streamer streamer = currArray.getStreamer(i);
			for (int s = 0; s < fields.length; s++) {
				String dBlock = streamer.getSensorDataBlocks(fields[s]);
				ArrayParameterType oType = streamer.getOrientationTypes(fields[s]);
				if (dBlock == null || oType != ArrayParameterType.SENSOR) {
					continue;
				}
				PamDataBlock dataBlock = PamController.getInstance().getDataBlockByLongName(dBlock);
				if (dataBlock == null) {
					continue;
				}
				if (subscribedBlocks.contains(dataBlock) == false) {
					subscribedBlocks.add(dataBlock);
					dataBlock.addObserver(streamerObserver);
				}
			}
		}
	}

	private class StreamerObserver extends PamObserverAdapter {

		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
			ArrayDisplayPanel.this.updateData(arg.getChannelBitmap(), arg.getTimeMilliseconds(), null);
		}

		@Override
		public String getObserverName() {
			return null;
		}

		@Override
		public PamObserver getObserverObject() {
			return null;
		}
	}
}
