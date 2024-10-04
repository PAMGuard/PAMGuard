package nmeaEmulator;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import AIS.AISControl;
import GPS.GPSControl;
import NMEA.NMEAControl;
import NMEA.NMEAParameters;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamSettingManager;
import PamguardMVC.PamDataBlock;
import generalDatabase.DBControl;

public class NMEAFrontEnd {

	private JFrame mainFrame;

	private JPanel mainPanel;

	private DBControl databaseControl;

	private SerialOutput serialOutput;

	private NMEAControl nmeaControl;

	private Frame parentFrame;

	private GPSControl gpsControl;

	private AISControl aisControl;

	private long[] timeLimits = new long[2];

	static public final int STATUS_IDLE = 0;
	static public final int STATUS_RUNNING = 1;

	protected NMEAEmulatorParams emulatorParams = new NMEAEmulatorParams();

	private ArrayList<PamControlledUnit> simulatedUnits = new ArrayList<PamControlledUnit>();
	private ArrayList<PamDataBlock> simulatedDataBlocks = new ArrayList<PamDataBlock>();
	private ArrayList<NMEAEmulator> emulatedDataBlocks = new ArrayList<NMEAEmulator>();

	private int status = STATUS_IDLE;

	public NMEAFrontEnd(NMEAControl nmeaControl, Frame parentFrame) {
		this.nmeaControl = nmeaControl;
		this.parentFrame = parentFrame;
		serialOutput = new SerialOutput("Serial Output");
		serialOutput.setSerialOutputParameters(nmeaControl.getNmeaParameters());
		gpsControl = (GPSControl) PamController.getInstance().findControlledUnit("GPS Acquisition");
		aisControl = (AISControl) PamController.getInstance().findControlledUnit("AIS Processing");
		if (gpsControl != null) {
			simulatedUnits.add(gpsControl);
			simulatedDataBlocks.add(gpsControl.getPamProcess(0).getOutputDataBlock(0));
			emulatedDataBlocks.add((NMEAEmulator) gpsControl.getPamProcess(0).getOutputDataBlock(0));
		}
		if (aisControl != null) {
			simulatedUnits.add(aisControl);
			simulatedDataBlocks.add(aisControl.getPamProcess(0).getOutputDataBlock(0));
			emulatedDataBlocks.add((NMEAEmulator) aisControl.getPamProcess(0).getOutputDataBlock(0));
		}

		setTimeLimits();

		NMEAOutdialog.showDialog(parentFrame, this);

	}

	// get the start and end of the loaded data
	private void setTimeLimits() {
		timeLimits = new long[2];
		PamDataBlock dataBlock;
		long viewStart, viewEnd;
		for (int i = 0; i < simulatedDataBlocks.size(); i++) {
			dataBlock = simulatedDataBlocks.get(i);
			viewStart = dataBlock.getCurrentViewDataStart();
			viewEnd = dataBlock.getCurrentViewDataEnd();
			if (timeLimits[0] == 0) {
				timeLimits[0] = viewStart;
			}
			else {
				timeLimits[0] = Math.min(timeLimits[0], viewStart);
			}
			if (timeLimits[1] == 0) {
				timeLimits[1] = viewEnd;
			}
			else {
				timeLimits[1] = Math.max(timeLimits[1], viewEnd);
			}
		}
	}

	public NMEAParameters getSerialParams() {
		return serialOutput.getSerialOutputParameters();
	}

	public int getStatus() {
		return status;
	}

	public String getStatusString() {
		switch(status) {
		case STATUS_IDLE:
			return "Idle";
		case STATUS_RUNNING:
			return "Running";
		default:
			return "Unknown";
		}
	}

	class MenuExit implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			prepareToClose();
		}
	}

	private boolean prepareToClose() {

		if (!PamController.getInstance().canClose()) {
			return false;
		}
		serialOutput.closeSerialPort();

		//		int pamStatus = PamController.getInstance().getPamStatus();
		//		if (pamStatus != PamController.PAM_IDLE) {
		//			int ans = JOptionPane.showConfirmDialog(frame,  
		//					"Are you sure you want to stop and exit",
		//					"PAMGUARD is busy",
		//					JOptionPane.YES_NO_OPTION);
		//			if (ans == JOptionPane.NO_OPTION) {
		//				return false;
		//			}
		//		}

		// finally save all settings just before PAMGUARD closes. 
		PamSettingManager.getInstance().saveFinalSettings();

		//		System.exit(0);
		mainFrame.dispose();
		//		mainFrame.setVisible(false);

		return true;
	}


	class MenuStart implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			//			menuTimes();
		}
	}

	class MenuStop implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev){
			//			menuTimes();
		}
	}

	class WindowEvents extends  WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			prepareToClose();			
		}		
	}

	private int testCount = 0;

	public long[] getTimeLimits() {
		return timeLimits;
	}

	private SimWorker simWorker;
	public void startSim(NMEAOutdialog nmeaOutdialog) {
		simWorker = new SimWorker(nmeaOutdialog);
		simWorker.execute();
	}

	public void stopSim() {
		// TODO Auto-generated method stub
		simWorker.stop();

	}

	class SimWorker extends SwingWorker<Integer, EmulationProgress>{

		private NMEAOutdialog nmeaOutdialog;
		private volatile boolean stopNow;
		public SimWorker(NMEAOutdialog nmeaOutdialog) {
			super();
			this.nmeaOutdialog = nmeaOutdialog;
		}

		protected void stop() {
			stopNow = true;
		}

		@Override
		protected Integer doInBackground() throws Exception {
			try {
			int nBlocks = simulatedDataBlocks.size();
			int haveCount;
			EmulationProgress emulationProgress;
			EmulatedData emulatedData[] = new EmulatedData[nBlocks];
			boolean dataPrepared[] = new boolean[nBlocks];
			String outString;
			publish(new EmulationProgress("Initialise serial port ..."));
			if (!serialOutput.openSerialPort()) {
				publish(new EmulationProgress("Unable to open serial port ..."));
				return null;
			}
			publish(new EmulationProgress("Starting data emulation ..."));
			while(true) { // repeat if repeat button selected in dialog. 
				long simStartTime = System.currentTimeMillis();
				long currentRealTime = simStartTime;
				long currentDataTime = timeLimits[0];
				long dataLength = timeLimits[1] - timeLimits[0];
				long timePassed;
				if (nBlocks == 0) {
					return null;
				}

				for (int i = 0; i < nBlocks; i++) {
					publish(new EmulationProgress("Prepare data source " + simulatedDataBlocks.get(i).getDataName()));
					dataPrepared[i] = emulatedDataBlocks.get(i).
					prepareDataSource(timeLimits, currentRealTime - currentDataTime);
					if (dataPrepared[i]) {
						emulatedData[i] = emulatedDataBlocks.get(i).getNextData();
					}
				}
				while (!stopNow) {
					currentRealTime = System.currentTimeMillis();
					timePassed = currentRealTime - simStartTime;
					currentDataTime = timeLimits[0] + timePassed;
					for (int i = 0; i < nBlocks; i++) {
						while (emulatedData[i] != null) {
							if (emulatedData[i].dataTime > currentDataTime) {
								break;
							}
							// simulate the data
							//							outString = "Simulate data from " + 
							//							simulatedDataBlocks.get(i) + " at " + 
							//							PamCalendar.formatDBDateTime(nextUnits[i].getTimeMilliseconds());
							//							publish(new EmulationProgress(outString));
							//							serialOutput.sendSerialString(outString);
							outString = emulatedData[i].dataString; 
							if (outString != null) {
								publish(new EmulationProgress(outString));
								serialOutput.sendSerialString(outString);
							}

							emulatedData[i] = emulatedDataBlocks.get(i).getNextData();
						}
					}
					haveCount = 0;
					for (int i = 0; i < nBlocks; i++) {
						if (emulatedData[i] != null) {
							haveCount++;
						}
					}
					if (haveCount == 0) {
						break;
					}
					Thread.sleep(10);

					publish(new EmulationProgress(currentRealTime, currentDataTime, 
							(int) (timePassed * 100 / dataLength)));
				}
				if (stopNow || !nmeaOutdialog.isRepeat()) {
					break;
				}
			}
			publish(new EmulationProgress());
			serialOutput.closeSerialPort();
			}
			catch (Exception e){
				System.out.println("Error in NMEA Emulator SwingWorker thread");
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void done() {
			// TODO Auto-generated method stub
			super.done();
		}

		@Override
		protected void process(List<EmulationProgress> chunks) {
			// TODO Auto-generated method stub
			super.process(chunks);
			for (int i = 0; i < chunks.size(); i++) {
				nmeaOutdialog.setProgress(chunks.get(i));
			}
		}


	}
}
