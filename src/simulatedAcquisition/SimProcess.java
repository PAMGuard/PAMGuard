package simulatedAcquisition;

import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionDialog;
import Acquisition.AudioDataQueue;
import Acquisition.DaqSystem;
import Array.ArrayManager;
import Array.PamArray;
import Map.MapController;
import Map.MapPanel;
import Map.MapRectProjector;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import pamMaths.PamVector;
import propagation.PropagationModel;
import propagation.SphericalPropagation;
import propagation.SurfaceEcho;
import simulatedAcquisition.sounds.SimSignals;

/**
 * Simulation of simulated sound. 
 * <p>Sound simulated on a time at approximately 10x real time
 * <p>Slow it down with a sound playback module if needs be. 
 * <p>
 * Controls creation and movement of simulated objects
 * @author Doug Gillespie
 *
 */
public class SimProcess extends DaqSystem implements PamSettings {

	//	private SimControl simControl;

	public static final String daqType =  "Simulated Data DAQ";

	public static final String sysType = "Simulated Sources";

	//	private PamRawDataBlock outputData;
	private AcquisitionControl daqControl;

	private SimObjectsDataBlock simObjectsDataBlock;
	
	private SimSoundDataBlock simSoundsDataBlock;

	private PamArray currentArray;

	private Random random = new Random();

	private volatile long totalSamples;

	private volatile long startTimeMillis;

	SimParameters simParameters = new SimParameters(sysType);

	private SimDialogPanel simdialogPanel;

	private SimGraphics simGraphics;

	private volatile boolean dontStop;

	private volatile boolean stillRunning;

	private GenerationThread genThread;

	private Thread theThread;

	protected AudioDataQueue newDataUnits;

	private SimMouseAdapter simMouseAdapter;

	protected SimSignals simSignals;
	
	protected ArrayList<PropagationModel> propagationModels = new ArrayList<PropagationModel>();
	
	private PropagationModel propagationModel;

	public SimProcess(AcquisitionControl daqControl) {
		this.daqControl = daqControl;
		setArray();
		propagationModels.add(new SphericalPropagation());
		propagationModels.add(new SurfaceEcho(new SphericalPropagation()));
		simSignals = new SimSignals(this);
		PamSettingManager.getInstance().registerSettings(this);
		simMouseAdapter = new SimMouseAdapter();
		sortMapMice();
		setupObjects();
		setupSim();
	}

	private void sortMapMice() {
		if (simMouseAdapter == null) {
			return;
		}
		PamControlledUnit pamControlledUnit;
		MapController mapController;
		int n = PamController.getInstance().getNumControlledUnits();
		for (int i = 0; i < n; i++) {
			pamControlledUnit = PamController.getInstance().getControlledUnit(i);
			if (pamControlledUnit.getClass() == MapController.class) {
				mapController = (MapController) pamControlledUnit;
				mapController.addMouseAdapterToMapPanel(simMouseAdapter);
			}
		}
	}

	private boolean wasSelected;

	private int dataUnitSamples;
	
	@Override
	public void setSelected(boolean select) {
		if (simObjectsDataBlock == null) {
			simObjectsDataBlock = new SimObjectsDataBlock(daqControl.getAcquisitionProcess());
			simObjectsDataBlock.setOverlayDraw(simGraphics = new SimGraphics());
			simObjectsDataBlock.setPamSymbolManager(new StandardSymbolManager(simObjectsDataBlock, SimGraphics.defaultSymbol, true));
		}
		if (simSoundsDataBlock == null) {
			simSoundsDataBlock = new SimSoundDataBlock("Simulated Sounds", daqControl.getAcquisitionProcess(), 0);
		}
		if (wasSelected && !select) {
			daqControl.getAcquisitionProcess().removeOutputDatablock(simObjectsDataBlock);
			daqControl.getAcquisitionProcess().removeOutputDatablock(simSoundsDataBlock);
			//			PamController.getInstance().notifyModelChanged(PamControllerInterface.REMOVE_DATABLOCK);
		}
		else if (!wasSelected && select) {
			daqControl.getAcquisitionProcess().addOutputDataBlock(simObjectsDataBlock);
			daqControl.getAcquisitionProcess().addOutputDataBlock(simSoundsDataBlock);
			setupObjects();
			//			PamController.getInstance().notifyModelChanged(PamControllerInterface.ADD_DATABLOCK);
		}
		wasSelected = select;
	}

	protected void setupObjects() {
		if (simObjectsDataBlock == null) {
			return;
		}
		simObjectsDataBlock.clearOldData();
		int n = simParameters.getNumObjects();
		SimObject s;
		SimObjectDataUnit sdu;
		for (int i = 0; i < n; i++) {
			s = simParameters.getObject(i);
			sdu = new SimObjectDataUnit(this, s, 1000);
			sdu.lastUpdateTime = 0;
			simObjectsDataBlock.addPamData(sdu);
		}
	}

	protected void setupSim() {
		propagationModel = getPropagationModel(simParameters.propagationModel);
		sortMapMice();
		//		outputData.setSampleRate(simControl.simParameters.sampleRate, true);
		//		super.setSampleRate(simControl.simParameters.sampleRate, false);
	}

	public PropagationModel getPropagationModel() {
		if (propagationModel == null) {
			return getPropagationModel(simParameters.propagationModel);
		}
		return propagationModel;
	}
	
	public PropagationModel getPropagationModel(String propName) {
		for (int i = 0; i < propagationModels.size(); i++) {
			if (propagationModels.get(i).getName().equals(propName)) {
				return propagationModels.get(i);
			}
		}
		if (propagationModel == null) {
			propagationModel = propagationModels.get(0);
		}
		return propagationModel;
	}

	public void setPropagationModel(PropagationModel propagationModel) {
		this.propagationModel = propagationModel;
		if (propagationModel != null) {
			simParameters.propagationModel = propagationModel.getName();
		}
	}
	

	private void updateObjectPositions(long timeMilliseconds) {
		int n = simObjectsDataBlock.getUnitsCount();
		SimObjectDataUnit sdu;
		ListIterator<SimObjectDataUnit> li = simObjectsDataBlock.getListIterator(0);
		while (li.hasNext()) {
			sdu = li.next();
//			updateObjectPosition(timeMilliseconds, sdu);
		}
	}
//	private void updateObjectPosition(long timeMilliseconds, SimObjectDataUnit simObjectDataUnit) {
//		SimObject s = simObjectDataUnit.getSimObject();
//		if (simObjectDataUnit.lastUpdateTime == 0) {
//			simObjectDataUnit.lastUpdateTime = timeMilliseconds;
//			return;
//		}
//		long updateInterval = timeMilliseconds - simObjectDataUnit.lastUpdateTime;
//		simObjectDataUnit.lastUpdateTime = timeMilliseconds;
//		double dist = (double) updateInterval / 1000 * s.speed;
//		simObjectDataUnit.setCurrentPosition(simObjectDataUnit.getCurrentPosition().travelDistanceMeters(s.course, dist));
//		simObjectsDataBlock.updatePamData(simObjectDataUnit, timeMilliseconds);
//	}

	//	@Override
	//	public void masterClockUpdate(long timeMilliseconds, long sampleNumber) {
	//		super.masterClockUpdate(timeMilliseconds, sampleNumber);
	//		updateObjectPositions(timeMilliseconds);
	//	}

	public void notifyArrayChanged() {
		setArray();
	}

	public float getSampleRate() {
		return daqControl.getAcquisitionParameters().sampleRate;
	}

	private void setArray() {
		currentArray = ArrayManager.getArrayManager().getCurrentArray();
		int nChan = currentArray.getHydrophoneCount();
		//		outputData.setChannelMap(PamUtils.makeChannelMap(nChan));
	}

	/**
	 * Generates data as fast as it can. If
	 * we want to slow it down, then it's necessary 
	 * to add a sound playback module or something.
	 * @author Doug
	 *
	 */
	class GenerationThread implements Runnable {

		@Override
		public void run() {
			stillRunning = true;
			while (dontStop) {
				generateData();
				/*
				 * this is the point we wait at for the other thread to
				 * get it's act together on a timer and use this data
				 * unit, then set it's reference to zero.
				 */
				while (newDataUnits.getQueueSize() > daqControl.acquisitionParameters.nChannels*2) {
					if (!dontStop) break;
					try {
						Thread.sleep(2);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			stillRunning = false;
		}

	}

	/**
	 * Main generation function, called from within a continuous
	 * loop. Generates random data for each channel for each data unit
	 * and then overlays the individual objects data on top of each block of 
	 * raw data. 
	 */
	private void generateData() {
		RawDataUnit rdu;
		int nChan = daqControl.acquisitionParameters.nChannels;
		int phone;
		int nSource = simParameters.getNumObjects();
		int nSamples = (int) daqControl.acquisitionParameters.sampleRate / 10;
		double nse;
		double[] channelData;
		long currentTimeMillis = startTimeMillis + totalSamples / 1000;
		SimObject simObject;
		for (int i = 0; i < nChan; i++) {
			channelData = new double[nSamples];
			/* simulate the noise, need to divide fs by 2 to only generate for 0 to Nyquist to get correct values out.
			 * that match the results of the noise band monitor modules.  
			 */
			double dbNse = simParameters.backgroundNoise + 10*Math.log10(getSampleRate()/2.);
			nse = daqControl.getDaqProcess().dbMicropascalToSignal(i, dbNse);
			generateNoise(channelData, nse);
//			generateNoiseQuickly(channelData, nse, i);
			phone = daqControl.acquisitionParameters.getHydrophone(i);
			// then add the simulated data for each object.
			for (int o = 0; o < nSource; o++) {
				simObject = simParameters.getObject(o);
				generateSignals(simObject.simObjectDataUnit, channelData,
						phone, totalSamples);
			}
			// then create a dataunit
			rdu = new RawDataUnit(currentTimeMillis, 1<<i,totalSamples,nSamples);
			rdu.setRawData(channelData, true);
			newDataUnits.addNewData(rdu, i);
		}
		//				PamCalendar.set(currentTimeMillis);
		PamCalendar.setSoundFileTimeInMillis(totalSamples * 1000L / (long)getSampleRate());
		updateObjectPositions(currentTimeMillis);
		//		outputData.masterClockUpdate(currentTimeMillis, totalSamples);
		//		masterClockUpdate(currentTimeMillis, totalSamples);
		for (int o = 0; o < nSource; o++) {
			simObject = simParameters.getObject(o);
			simObject.simObjectDataUnit.clearOldSounds(totalSamples + nSamples);
			int nNew = simObject.simObjectDataUnit.createNewSounds(totalSamples + nSamples*2);
//			if (nNew >0) {
//				StreamerDataUnit lastStreamerData = ArrayManager.getArrayManager().getCurrentArray().getStreamer(0).getHydrophoneOrigin().getLastStreamerData();
//				if (lastStreamerData != null) {
//					GpsData arrayLatLong = lastStreamerData.getGpsData();
//					LatLong simPos = simObject.simObjectDataUnit.getCurrentPosition();
//					double x = arrayLatLong.distanceToMetresX(simPos);
//					double y = arrayLatLong.distanceToMetresY(simPos);
//				}
//				
////				Debug.out.printf("Generate %d at xy %3.1f,%3.1f at %d\n", nNew, x,y,totalSamples);
//			}
		}
		totalSamples += nSamples;
	}
	
	/**
	 * Adds data from a single simulated object to a block of raw data. 
	 * @param sdu simulated object
	 * @param data raw data to add to 
	 * @param phone hydrophone number
	 * @param startSample start sample of data[]
	 */
	private void generateSignals(SimObjectDataUnit sdu, double[] data, int phone, long startSample) {
		sdu.takeSignals(data, phone, startSample);
	}

	/**
	 * Generate random noise on a data channel
	 * @param data data array to fill
	 * @param noise noise level 
	 */
	private void generateNoise(double[] data, double noise) {
		for (int i = 0; i < data.length; i++) {
			data[i] = random.nextGaussian() * noise;
		}
	}
	/**
	 * Generate random noise on a data channel. cheats somewhat by 
	 * setting up a group of randomated buffers, then selects randomly from within them 
	 * so that the same noise gets reused, but should be quicker. 
	 * @param data data array to fill
	 * @param noise noise level 
	 * @param iChan channel number. 
	 */
	private void generateNoiseQuickly(double[] data, double noise, int iChan) {
		int nChan = daqControl.acquisitionParameters.nChannels;
		if (noiseSets == null || noiseSets.length != nChan * 2 || noiseSets[0].length != data.length) {
			// generate a load of random noise for every channel. 
			noiseSets = new double[nChan*2][data.length];
			for (int i = 0; i < nChan*2; i++) {
				for (int j = 0; j < data.length; j++) {
					noiseSets[i][j] = random.nextGaussian();
				}
			}
		}
		if (iChan == 0) {
			permutateNoiseSets();
		}
		double[] noiseSet = noiseSets[channelPermutation.get(iChan)];
		for (int i = 0; i < data.length; i++) {
			data[i] = noiseSet[i] * noise;
		}
	}
	/*
	 * Called for each channel set to repermutate channel numbers for 
	 * noise. 
	 */
	private void permutateNoiseSets() {
		int nChan = daqControl.acquisitionParameters.nChannels;
		if (channelPermutation == null || channelPermutation.size() != nChan*2) {
			channelPermutation = new ArrayList<Integer>();
			for (int i = 0; i < nChan*2; i++) {
				channelPermutation.add(i);
			}
		}
		Collections.shuffle(channelPermutation);
	}
	private double[][] noiseSets = null;
	private ArrayList<Integer> channelPermutation;

	@Override
	public boolean canPlayBack(float sampleRate) {
		return true;
	}

	@Override
	public void daqHasEnded() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean dialogGetParams() {
		boolean ok = simdialogPanel.getParams();
		if (ok) {
			setupObjects();
		}
		return ok;
	}

	@Override
	public void dialogSetParams() {
		// do a quick check to see if the system type is stored in the parameters.  This field was added
		// to the SoundCardParameters class on 23/11/2020, so any psfx created before this time
		// would hold a null.  The system type is used by the getParameterSet method to decide
		// whether or not to include the parameters in the XML output
		if (simParameters.systemType==null) simParameters.systemType=getSystemType();

		simdialogPanel.setParams(simParameters);
	}

	@Override
	public JComponent getDaqSpecificDialogComponent(
			AcquisitionDialog acquisitionDialog) {
		Window parentFrame = PamController.getMainFrame();
		if (acquisitionDialog != null) {
			parentFrame = acquisitionDialog.getOwner();
		}
		if (simdialogPanel == null) {
			simdialogPanel = new SimDialogPanel(parentFrame, this);
		}
		return simdialogPanel.dialogPanel;
	}

	@Override
	public int getMaxChannels() {
		return 0;
	}

	@Override
	public int getMaxSampleRate() {
		return 0;
	}

	@Override
	public double getPeak2PeakVoltage(int swChannel) {
		return DaqSystem.PARAMETER_UNKNOWN;
	}

	@Override
	public String getSystemName() {
		return sysType;
	}

	@Override
	public String getSystemType() {
		return sysType;
	}

	@Override
	public boolean isRealTime() {
		return false;
	}

	@Override
	public boolean prepareSystem(AcquisitionControl daqControl) {
		newDataUnits = daqControl.getDaqProcess().getNewDataQueue();
		genThread = new GenerationThread();
		theThread = new Thread(genThread);
		startTimeMillis = System.currentTimeMillis();
		totalSamples = 0;
		dataUnitSamples = (int) (daqControl.acquisitionParameters.sampleRate/10);
		setupSim();
		PamCalendar.setSoundFile(true);
		PamCalendar.setSoundFileTimeInMillis(0);
		PamCalendar.setSessionStartTime(startTimeMillis);
		for (int i = 0; i < simParameters.getNumObjects(); i++) {
			simParameters.getObject(i).simObjectDataUnit.prepareSimulation();
		}
		return true;
	}

	@Override
	public int getDataUnitSamples() {
		return dataUnitSamples;
	}


	@Override
	public boolean startSystem(AcquisitionControl daqControl) {

		dontStop = true;

		theThread.start();

		setStreamStatus(STREAM_RUNNING);

		return true;
	}

	@Override
	public void stopSystem(AcquisitionControl daqControl) {

		dontStop = false;

	}

	@Override
	public Serializable getSettingsReference() {
		return simParameters;
	}

	@Override
	public long getSettingsVersion() {
		return SimParameters.serialVersionUID;
	}

	@Override
	public String getUnitName() {
//		return "Simulated Data Sources";
		return daqControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return daqType;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		simParameters = ((SimParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;		
	}

	private class SimMouseAdapter extends MouseAdapter {
		private Point mouseClickPoint;
		private MapPanel mapPanel;
		private SimObjectDataUnit sodu;
		MapRectProjector mapProjector;

		@Override
		public void mousePressed(MouseEvent mouseEvent) {
			super.mouseClicked(mouseEvent);
			mapPanel = (MapPanel) mouseEvent.getSource();
			mapProjector = mapPanel.getRectProj();
			PamDataUnit dataUnit = mapProjector.getHoveredDataUnit();
			if (dataUnit == null) {
				sodu = null;
				return;
			}
			else if (dataUnit.getClass() == SimObjectDataUnit.class) {
				sodu = (SimObjectDataUnit) dataUnit;
			}
			mouseClickPoint = mouseEvent.getPoint();
		}

		@Override
		public void mouseDragged(MouseEvent mouseEvent) {
			if (sodu == null) {
				return;
			}
			mouseClickPoint = mouseEvent.getPoint();
			LatLong latLong = mapProjector.panel2LL(new Coordinate3d(mouseClickPoint.getX(), 
					mouseClickPoint.getY(), 0.0));
			SimObject so = sodu.getSimObject();
			setPosition(PamCalendar.getTimeInMillis(), latLong, sodu.getCurrentHeight(), sodu.getCurrentHeading());
		}

		@Override
		public void mouseReleased(MouseEvent mouseEvent) {
			if (sodu == null) {
				return;
			}
			LatLong latLong = mapProjector.panel2LL(new Coordinate3d(mouseClickPoint.getX(), 
					mouseClickPoint.getY(), 0.0));
			SimObject so = sodu.getSimObject();
			setPosition(PamCalendar.getTimeInMillis(), latLong, sodu.getCurrentHeight(), sodu.getCurrentHeading());
		}

		/**
		 * Sets the position both of the current data unit and of 
		 * the underlying SimObject so that the position is held for 
		 * the next run. 
		 * @param timeInMillis
		 * @param latLong
		 * @param currentHeight
		 * @param currentHeading
		 */
		private void setPosition(long timeInMillis, LatLong latLong,
				double currentHeight, PamVector currentHeading) {
			SimObject simObject = sodu.getSimObject();
			simObject.startPosition = latLong;
			sodu.setCurrentPosition(PamCalendar.getTimeInMillis(), latLong, sodu.getCurrentHeight(), sodu.getCurrentHeading());
			sodu.resetSounds(PamCalendar.getTimeInMillis());
		}
		
		

	}



	public AcquisitionControl getDaqControl() {
		return daqControl;
	}

	@Override
	public String getDeviceName() {
		return null;
	}
	
	/**
	 * Get the speed of sound to use in simulations. 
	 * this may eventually be different from the speed of sound used to 
	 * reconstruct positions as part of error investigation. 
	 * @return currently used speed of sound
	 */
	public double getSimulatorSpeedOfSound() {
		return ArrayManager.getArrayManager().getCurrentArray().getSpeedOfSound();
	}

	/**
	 * Get the datablock for individual sounds (not the objects creating the sounds)
	 * @return the simSoundsDataBlock
	 */
	public SimSoundDataBlock getSimSoundsDataBlock() {
		return simSoundsDataBlock;
	}

	public PamDataBlock getSimObjectsDataBlock() {
		return simObjectsDataBlock;
	}

	/* (non-Javadoc)
	 * @see Acquisition.DaqSystem#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamControllerInterface.INITIALIZATION_COMPLETE) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					// might get this to happen late enough that it actually works !
					sortMapMice(); 
				}
			});
		}
	}
}
