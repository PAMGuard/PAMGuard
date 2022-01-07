package analogarraysensor;


import Array.ArrayManager;
import Array.PamArray;
import Array.Streamer;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.PamProcess;
import analoginput.AnalogDeviceType;
import analoginput.AnalogReadException;
import analoginput.AnalogSensorData;

public class ArraySensorProcess extends PamProcess {

	private ArraySensorControl analogSensorControl;

	//	private Timer readTimer;
	/*
	 * Can't read in a timer since it just hogs the AWT thread if it can't open the 
	 * socket. Just run it in a different thread altogether. 
	 */


	private Thread readingThread;

	private AnalogDeviceType activeInputDevice;
	
	private AnalogArraySensorDataBlock sensorDataBlock;

	public ArraySensorProcess(ArraySensorControl analogSensorControl) {
		super(analogSensorControl, null);
		this.analogSensorControl = analogSensorControl;
		sensorDataBlock = new AnalogArraySensorDataBlock("Array Sensor Data", analogSensorControl, this);
		addOutputDataBlock(sensorDataBlock);
		sensorDataBlock.SetLogging(new ArraySensorLogging(analogSensorControl, sensorDataBlock));
	}


	private class ReadThread implements Runnable {

		@Override
		public void run() {
			while(true) {
				readData();
				try {
					Thread.sleep(analogSensorControl.getAnalogSensorParams().readIntervalMillis);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}


	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}
	
//	/**
//	 * Get a bitmap of streamers that have at least one parameter
//	 * set for readoug. 
//	 * @return
//	 */
//	protected int getActiveBitmap() {
//
//		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
//		int nStreamer = currentArray.getNumStreamers();
//		if (activeInputDevice == null) {
//			return 0;
//		}
//		for (int i = 0, j = 0; i < nStreamer; i++) {
//			for (int p = 0; p < 4; p++, j++) {
//				try {
//					AnalogSensorData sensorData = activeInputDevice.getDeviceParams()
//	}

	private void readData() {
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
		int nStreamer = currentArray.getNumStreamers();
		if (activeInputDevice == null) {
			return;
		}
		for (int i = 0, j = 0; i < nStreamer; i++) {
			Streamer aStreamer = currentArray.getStreamer(i);
			int updates = 0;
			AnalogSensorData[] sensorDatas = new AnalogSensorData[4];
			for (int p = 0; p < 4; p++, j++) {
				try {
					AnalogSensorData sensorData = activeInputDevice.readData(j);
					sensorDatas[p] = sensorData;
					updates++;
//					if (sensorData == null) {
//						continue;
//					}
//					double val = sensorData.getCalibratedValue();
//					switch(p) {
//					case 0:
//						aStreamer.setZ(-val);
//						updates++;
//						break;
//					case 1:
//						aStreamer.setPitch(val);
//						updates++;
//						break;
//					case 2:
//						aStreamer.setRoll(val);
//						updates++;
//						break;
//					case 3:
//						aStreamer.setHeading(val);
//						updates++;
//						break;
//					}
				}
				catch (AnalogReadException e) {
					//					System.out.println(e.getMessage());
				}
			}
			if (updates > 0) {
//				aStreamer.makeStreamerDataUnit();
//				if (analogSensorControl.getAnalogSensorParams().storeRawValues) {
					AnalogArraySensorDataUnit dataUnit = new AnalogArraySensorDataUnit(PamCalendar.getTimeInMillis(), i, sensorDatas);
					sensorDataBlock.addPamData(dataUnit);
//				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamController.INITIALIZATION_COMPLETE) {
			startSystem();
		}
	}

	private void startSystem() {
		if (PamController.getInstance().getRunMode() != PamController.RUN_NORMAL) {
			return;
		}
		readingThread = new Thread(new ReadThread());
		readingThread.start();		
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#setupProcess()
	 */
	@Override
	public void setupProcess() {
		super.setupProcess();
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
		int nStreamer = currentArray.getNumStreamers();
		sensorDataBlock.setChannelMap(PamUtils.makeChannelMap(nStreamer));
		prepareProcess();
	}

	@Override
	public void prepareProcess() {
		super.prepareProcess();
		analogSensorControl.getAnalogDevicesManager().prepareDevice();
		activeInputDevice = analogSensorControl.getAnalogDevicesManager().getActiveDevice();
	}

	/**
	 * @return the sensorDataBlock
	 */
	public AnalogArraySensorDataBlock getSensorDataBlock() {
		return sensorDataBlock;
	}






}
