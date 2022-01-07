package difar.beamforming;

import java.util.Arrays;

import Array.ArrayManager;
import Array.PamArray;
import Array.SnapshotGeometry;
import Filters.Filter;
import Filters.FilterBand;
import Filters.FilterType;
import GPS.GPSControl;
import GPS.GpsData;
import GPS.GpsDataUnit;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.LatLong;
import PamUtils.PamUtils;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RequestCancellationObject;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import decimator.DecimatorControl;
import decimator.DecimatorProcess;
import difar.DemuxObserver;
import difar.DemuxWorkerMessage;
import difar.DifarControl;
import difar.DifarDataUnit;
import difar.DifarParameters;
import difar.DifarParameters.DifarDemuxTypes;
import difar.demux.AmmcDemux;
import difar.demux.DifarDemux;
import difar.demux.DifarResult;
import difar.demux.GreenridgeDemux;
import difar.demux.NativeDemux;
import fftManager.PamFFTControl;
import fftManager.PamFFTProcess;

public class BeamformProcess extends PamProcess{
	
//	DifarDemux difarDemux = new AmmcDemux();
	DifarDemux difarDemux;
		
	DifarDemuxTypes demuxType = DifarParameters.DifarDemuxTypes.AMMC_EXPERIMENTAL;
	float outputSampleRate = BeamformParameters.outputSampleRate;

	private BeamformControl beamformControl;

	/**
	 * Steering angle of beamformer
	 */

	private double[][] filteredData;
	private double[][] outputData;
	private int[] outputIndex;
	private long[] outputSampleNumber;

	private PamRawDataBlock outputDataBlock;

	private float sourceSampleRate = 1;
	
	private double Z0 = 1.0;

	private double[] x = new double[PamConstants.MAX_CHANNELS];
	private double[] b1 = new double[PamConstants.MAX_CHANNELS];
	private double[] b2 = new double[PamConstants.MAX_CHANNELS];

	/**
	 * step size between samples. 
	 */
	private float step;

	/**
	 * Number of samples in each output block. 
	 */
	private int outputBlockSize;

	private long[] outputDataStart = new long[PamConstants.MAX_CHANNELS];

	GPSControl gpsControl;
	
	public BeamformProcess(PamControlledUnit pamControlledUnit) {
		super(pamControlledUnit, null);
		beamformControl = (BeamformControl) pamControlledUnit;
		difarDemux = new AmmcDemux(); //new NativeDemux(beamformControl);
		gpsControl = GPSControl.getGpsControl();
		setSampleRate(outputSampleRate, false);
		
		addOutputDataBlock(outputDataBlock = new PamRawDataBlock(beamformControl.getUnitName() + " Beamformed Audio", this,
				0, outputSampleRate));
		
		newSettings();
 	}

	@Override
	public void newData(PamObservable obs, PamDataUnit pamDataUnit) {
		if (pamDataUnit.getClass().isAssignableFrom(RawDataUnit.class))
			newRawData(obs, (RawDataUnit) pamDataUnit);
		
		if (pamDataUnit.getClass().isAssignableFrom(GpsDataUnit.class))
			newGpsData(obs, (GpsDataUnit) pamDataUnit);
	}
	

	public void newRawData(PamObservable obs, RawDataUnit pamRawData){
		RawDataUnit rawDataUnit = (RawDataUnit) pamRawData;
		int chan = PamUtils.getSingleChannel(rawDataUnit.getChannelBitmap());

		// Check that this is a channel that we want
		if ((1<<chan & beamformControl.getBeamformParameters().channelMap) <= 0) return;
		double [][] demuxedDecimatedData = demuxDataUnit((RawDataUnit) pamRawData);
		outputBlockSize = demuxedDecimatedData[0].length;
		
		if (outputData[chan] == null || outputData[chan].length != outputBlockSize) {
			outputData[chan] = new double[outputBlockSize];
		}
		Double[] theta = beamformControl.getBeamformParameters().theta;
		for (int i = 0; i < outputBlockSize; i++){
			outputData[chan][i] = demuxedDecimatedData[0][i];
			if (theta[chan] != null){
				// Equation 1 from Thode et al 2016. JASA-EL 139 (4), ppEL105-EL111. 
				outputData[chan][i] -= Z0 * (demuxedDecimatedData[1][i]*Math.sin(theta[chan]) + demuxedDecimatedData[2][i]*Math.cos(theta[chan]));
			} 
		}
		outputDataStart[chan] = rawDataUnit.getTimeMilliseconds() + (long) (1000 / ((PamDataBlock<RawDataUnit>) obs).getSampleRate());
		RawDataUnit newDataUnit = new RawDataUnit(outputDataStart[chan], rawDataUnit.getChannelBitmap(), 
				outputSampleNumber[chan], outputBlockSize);
		newDataUnit.setRawData(outputData[chan], true);
		outputDataBlock.addPamData(newDataUnit);
		outputData[chan] = new double[outputBlockSize];
		outputIndex[chan] = 0;
		outputSampleNumber[chan] += outputBlockSize;
	}
	
	private double[][] demuxDataUnit(RawDataUnit rawDataUnit) {

		float sourceRate = rawDataUnit.getParentDataBlock().getSampleRate();
		double[] multiplexedData = rawDataUnit.getRawData();
		if (multiplexedData == null) {
			return null; // this will happen as normal behaviour in viewer mode. 
		}

		int decimationFactor = (int) (sourceRate / outputSampleRate);


		DifarResult difarResult = difarDemux.processClip(multiplexedData, sourceRate, decimationFactor, null, null);


		double[][] demuxedDecimatedData = difarResult.getDataArrays();

		if (demuxedDecimatedData[0] == null) {
			System.out.println("demuxed data null!! processing failed");
		}
		
		int nSamples = demuxedDecimatedData[0].length;
		long duration = (long) (nSamples/(sourceRate/decimationFactor));
		rawDataUnit.setSampleDuration(duration);
		duration = (long) (nSamples/outputSampleRate*sourceRate);
		rawDataUnit.setSampleDuration(duration);
		return demuxedDecimatedData;	
	}
	
	public void newGpsData(PamObservable obs, GpsDataUnit gpsData){
		updateNoiseSourceDirection(gpsData.getTimeMilliseconds());
	}

	public void updateNoiseSourceDirection(long timeMilliseconds){
		if (beamformControl.getBeamformParameters().useGpsNoiseSource == false) 
			return;
		double angleThreshold = Math.toRadians(5);
		int n = beamformControl.getBeamformParameters().getNumChannels();
		Double theta[] = beamformControl.getBeamformParameters().getTheta();
		Double noiseAngle[] = new Double[n];
		for (int i = 0; i<n; i++){
			if (getNoiseSourceDirection(timeMilliseconds, i)==null){
				continue;
			}
			noiseAngle[i] = Math.toRadians(getNoiseSourceDirection(timeMilliseconds, i));
			if (theta[i] == null){
				theta[i] = noiseAngle[i];
			}
			if (Math.abs(noiseAngle[i] - theta[i]) > angleThreshold){
				theta[i] = noiseAngle[i];
			}
		}
		beamformControl.getBeamformParameters().setTheta(theta);
		beamformControl.updateSidePanel();
	}
	
	@Override
	public void setupProcess() {
		super.setupProcess();
		newSettings();
	}
	
	void newSettings() {
		// if the raw data source has changed ...
		PamRawDataBlock rawDataBlock = PamController.getInstance().
		getRawDataBlock(beamformControl.getBeamformParameters().rawDataName);
		if (rawDataBlock != getParentDataBlock() & rawDataBlock !=outputDataBlock) {
			setParentDataBlock(rawDataBlock);
		}
		if (getParentDataBlock() != null) {
			outputDataBlock.setChannelMap(beamformControl.getBeamformParameters().channelMap);
			this.setSampleRate(outputSampleRate, true);
			outputDataBlock.setSampleRate(outputSampleRate, true);
			sourceSampleRate = rawDataBlock.getSampleRate();
			int numChannels = beamformControl.getBeamformParameters().getNumChannels();
			Double [] oldTheta = beamformControl.getBeamformParameters().getTheta();
			if (oldTheta.length != numChannels){
				beamformControl.getBeamformParameters().setTheta( new Double[numChannels]);
			}
		}
		
		// if the GPS data source has changed
		gpsControl = GPSControl.getGpsControl();
		if (gpsControl != null) {
			gpsControl.getGpsDataBlock().deleteObserver(this);
			String gpsSourceName =  beamformControl.getBeamformParameters().noiseGpsSource;
			if (gpsControl.getUnitName() != gpsSourceName){
				gpsControl = (GPSControl) PamController.getInstance().findControlledUnit(GPSControl.class, gpsSourceName);
			}
			gpsControl.getGpsDataBlock().addObserver(this);
		}
		BeamformSidePanel sidePanel = (BeamformSidePanel) beamformControl.getSidePanel();
		if (sidePanel != null)
			sidePanel.enableControls();
		setupFilters();
	}

	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		sourceSampleRate = sampleRate;
		if (beamformControl != null) {
			super.setSampleRate(outputSampleRate, notify);
		}
	}
	
	@Override
	public void masterClockUpdate(long milliSeconds, long sampleNumber) {
		// Don't do anything. Update decimator output when data are added. 
		super.masterClockUpdate(milliSeconds, (long) (sampleNumber/step));
	}
	
	@Override
	public void prepareProcess() {
		// do nothing, especially NOT set the sample rate !!!!
		//super.prepareProcess();
		//startMilliseconds = PamCalendar.getTimeInMillis();
		// just zero all the indexes. 
		if (getParentDataBlock() == null) {
			return;
		}
		step = getParentDataBlock().getSampleRate() / getSampleRate();
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			x[i] = b1[i] = 0;
			b2[i] = step;
		}
		outputBlockSize = 0;
		outputData = new double[PamConstants.MAX_CHANNELS][];
		outputIndex = new int[PamConstants.MAX_CHANNELS];
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			outputIndex[i] = 0;//Math.round(step)-1;
		}
		outputSampleNumber = new long[PamConstants.MAX_CHANNELS];
//		System.out.println(getPamControlledUnit().getUnitName() + " prepared.");
		
	}
	
	
	@Override
	public void pamStart() {
		setupFilters();
		
	}

	void setupFilters(){
		// now create the filters.
		if (getParentDataBlock() == null) return;
		filteredData = new double[beamformControl.getBeamformParameters().getNumChannels()][];
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * @return the outputDataBlock
	 */
	public PamRawDataBlock getOutputDataBlock() {
		return outputDataBlock;
	}
	
	private Double getNoiseSourceDirection(long timeMilliseconds, int channel) {

		GpsDataUnit shipGpsUnit = gpsControl.getShipPosition(timeMilliseconds);
		if (shipGpsUnit == null) {
			return null;
		}
		GpsData shipGps = shipGpsUnit.getGpsData();
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray(); 
		// convert a channel to a hydrophones. 
		int phoneNumber = PamUtils.getSingleChannel(1<<channel);
		SnapshotGeometry geometry = ArrayManager.getArrayManager().getSnapshotGeometry(1<<phoneNumber, timeMilliseconds);
		LatLong hLatLong = geometry.getReferenceGPS();
		double arrayHead = geometry.getReferenceGPS().getHeading();
		if (hLatLong == null ||shipGps == null){
			return null;
		}
		double bearing = hLatLong.bearingTo(shipGps);
		double bearingCorr = bearing -  arrayHead;
		bearingCorr = PamUtils.constrainedAngle(bearingCorr, 180);
		return bearingCorr;
	}
	
	@Override
	public int getOfflineData(OfflineDataLoadInfo offlineLoadDataInfo) {
		setupProcess();
		prepareProcess();
		pamStart();
		updateNoiseSourceDirection((offlineLoadDataInfo.getStartMillis() + offlineLoadDataInfo.getEndMillis())/2);
		if (beamformControl.getOfflineFileServer() == null) {
			return PamDataBlock.REQUEST_NO_DATA;
		}
		/*
		 * if offline files are not requested, continue to ask up the chain - there may
		 * be data in an upstream process which can still be decimated as normal. 
		 */
		if (beamformControl.getOfflineFileServer().getOfflineFileParameters().enable == false) {
			return super.getOfflineData(offlineLoadDataInfo);
		}
		if (beamformControl.getOfflineFileServer().loadData(getOutputDataBlock(), offlineLoadDataInfo, null)) {
			return PamDataBlock.REQUEST_DATA_LOADED;
		}
		else {
			return PamDataBlock.REQUEST_NO_DATA;
		}
	}
}