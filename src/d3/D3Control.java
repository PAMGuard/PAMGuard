package d3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import d3.calibration.CalFileReader;
import d3.calibration.CalibrationInfo;
import d3.calibration.CalibrationSet;
import d3.plots.D3DataPlotProvider;
import d3.plots.D3DataProviderFX;
import dataPlots.data.TDDataProviderRegister;
import dataPlotsFX.data.TDDataProviderRegisterFX;
import fileOfflineData.OfflineFileControl;
import fileOfflineData.OfflineFileMapPoint;
import fileOfflineData.OfflineFileProcess;
//import au.com.bytecode.opencsv.CSVReader;
import pamScrollSystem.ViewLoadObserver;
import userDisplay.UserDisplayControl;
import wavFiles.WavFileReader;
import wavFiles.WavHeader;

public class D3Control extends OfflineFileControl {

	public static final String unitType = "d3 Data";
	private D3DataBlock d3DataBlock;
	D3XMLFile firstXMLfile;
	private ArrayList<D3SensorInfo> d3SensorInfos = new ArrayList<>();
	private CalibrationSet calibrations;
	private D3DataPlotProvider d3DataPlotProvider;
	private float[] oldAccell = new float[3];
	private D3DataUnit previousJerkUnit;
	private D3DataProviderFX d3PlotProvider;


	public D3Control(String unitName) {
		super(unitType, unitName);

		TDDataProviderRegister.getInstance().registerDataInfo(d3DataPlotProvider = new D3DataPlotProvider(this, getD3DataBlock()));
		TDDataProviderRegisterFX.getInstance().registerDataInfo(d3PlotProvider = new D3DataProviderFX(this, getD3DataBlock()));
	}

	@Override
	protected void processMapFile(File aFile, int fileIndex) {
		D3FileTypes d3FileTypes = new D3FileTypes(aFile.getAbsolutePath());
		D3XMLFile xmlFile = D3XMLFile.openXMLFile(new File(d3FileTypes.getFileName("xml")));
		if (xmlFile == null) {
			return;
		}
		//		System.out.println(String.format("D3 Start %s, End %s", PamCalendar.formatDateTime(xmlFile.getStartTime()),
		//				PamCalendar.formatDateTime(xmlFile.getEndTime())));
		/**
		 * Unpacking sensor data files is a major pain in the arse. 
		 * First have to go through the xml file and find the list of sensors
		 * being read out, which will be something like:
		 * 4869, 6657, 4609, 4610, 4611, 4869, 5633, 4609, 4610, 4611, 4869, 5121, 4609, 4610, 4611, 4869, 5635, 4609, 4610, 4611
		 * Note that in this example there are 8 separate sensors, four sensors get read 4 times to the single readings of the other four 
		 * Also need to get the sample rate of the sensor reading, which is given in mHz, ie. 25000 for 25Hz. So the real sample rate for 
		 * the items that get read four times would be 100 Hz. 
		 * Typically are collecting about 1kByte per second of sensor data. (25*20*2) so an hours data would be 3.6Mbytes which ain't too bad:
		 * could probably load a days data quite happily without running out of memory. 
		 * 
		 * So unpack strategy needs to be
		 * 1) work on the xml file to see what on earth is in it - sample rate of sensor data, number of sensor channels
		 * and what the channel id's are, etc. 
		 * 2) work out how long the sensor data actually are from the size of the swv file - which may match something 
		 * in the xml file, but not sure what and may not match if the tag was suddenly switched off. 
		 * 3) we then have the times of a basic single map point
		 * 4) look to see if there is a tLog file and use that to break up the single map point into multiple map points. 
		 * Assume that for a deployment, all channel configs, etc are the same, so only need to store a single instance 
		 * of the actual configuration of the data - which can go into the d3datablock.
		 * 
		 *  When loading data, make one dataunit per second (say), which contains slightly unpacked data, i.e. do the de-interleaving
		 *  of channels so that there are typically 8 arrays of varying length. Dont' do any conversion to real units, but make
		 *  getters to convert to double units of depth, acceleration', jerk, etc as required. 
		 * 
		 */
		String sensorFile = d3FileTypes.getFileName("swv");
		WavFileReader wavFile = new WavFileReader(sensorFile);
		WavHeader wavHeader = wavFile.getWavHeader();
		if (fileIndex == 0 || d3SensorInfos.size() == 0) {
			firstXMLfile = xmlFile; // keep the first one !
			makeSensorInformation(xmlFile.getSensorList(), wavHeader);
		}
		
		int swvSampleRate = wavHeader.getSampleRate(); // is sample rate * 1000;
		double swvSeconds = (long) wavHeader.getDataSize() * 1000 / swvSampleRate / wavHeader.getBlockAlign();
		long swvEndTime = xmlFile.getStartTime() + (long) (swvSeconds * 1000.);
		long swvSamples = wavHeader.getDataSize()/wavHeader.getBlockAlign();
		wavFile.close();
		/*
		 * Now need to see if there is a tlog file in the same folder which will show the breakdown of
		 * the xmlFile with stop/start glider data.  
		 */
		String tlogName = d3FileTypes.getFileName("tlog");
		File tLogFile = new File(tlogName);
		if (!tLogFile.exists()) {
			/*
			 *  dead easy - just have to make a single data map point which is the length of 
			 *  the swv file. 
			 */
			D3DataMapPoint d3DataMapPoint = new D3DataMapPoint(xmlFile.getStartTime(), swvEndTime, 
					(int) (wavHeader.getDataSize()/wavHeader.getBlockAlign()), new File(sensorFile), 0, swvSamples);
			getOfflineFileDataMap().addDataPoint(d3DataMapPoint);
		}
		else {
			/**
			 * Now make several map points, each with an offset into the swv file. 
			 * Things to look for is lines with id == 4 - these are sample numbers in the
			 * swv file. 
			 */
			CSVReader reader;
			List<String[]> readList;
			try {
				reader = new CSVReader(new FileReader(tLogFile));
			} catch (FileNotFoundException e) {
				System.out.println("unable to open csv file " + tlogName);
				return;
			}
			try {
				readList = reader.readAll();
				int nSWVLines = 0;
				long[] tList = new long[readList.size()+1];
				long[] sampleList = new long[readList.size()+1];
				for (String[] aLine:readList) {
					TLogLine tLogLine = new TLogLine(aLine);
					if (tLogLine.id == TLogLine.ID_SENSOR) {
						tList[nSWVLines] = tLogLine.timeMillis;
						sampleList[nSWVLines] = tLogLine.samples;
						nSWVLines++;
					}
				}
				// last end time and sample count is not recorded, so add them to the list
				// but don't increment nSwVLines !
				tList[nSWVLines] = swvEndTime;
				sampleList[nSWVLines] = swvSamples;

				File sensFile = new File(sensorFile);
				for (int i = 0; i < nSWVLines; i++) {
					long nSamples = sampleList[i+1]-sampleList[i];
					long start = tList[i];
					long end = start + nSamples * 1000000 / swvSampleRate;
					D3DataMapPoint d3DataMapPoint = new D3DataMapPoint(start, end, (int) nSamples, 
							sensFile, sampleList[i], sampleList[i+1]);
					getOfflineFileDataMap().addDataPoint(d3DataMapPoint);

				}
			} catch (CsvException | IOException e) {
				System.out.println("unable to open csv file " + tlogName);
				return;
			}

		}

	}

	@Override
	public String getOfflineFileType() {
		return "swv";
	}

	@Override
	protected PamDataBlock createOfflineDataBlock(
			OfflineFileProcess offlineFileProcess) {
		return d3DataBlock = new D3DataBlock(offlineFileProcess, "D3 Sensor Data");
	}


	/* (non-Javadoc)
	 * @see fileOfflineData.OfflineFileControl#loadData(PamguardMVC.PamDataBlock, java.util.ArrayList, long, long, PamguardMVC.RequestCancellationObject, pamScrollSystem.ViewLoadObserver)
	 */
	@Override
	public boolean loadData(PamDataBlock dataBlock,
			ArrayList<OfflineFileMapPoint> usedMapPoints, 
			OfflineDataLoadInfo offlineDataLoadInfo,
			ViewLoadObserver loadObserver) {
		for (int i = 0; i < 3; i++) {
			oldAccell [i] = 0;
		}
		previousJerkUnit = null;
		boolean ok = true;
		for (OfflineFileMapPoint mapPoint:usedMapPoints) {
			ok &= loadData(dataBlock, (D3DataMapPoint) mapPoint, offlineDataLoadInfo.getStartMillis() , offlineDataLoadInfo.getEndMillis() );
			if (offlineDataLoadInfo.cancel) {
				break;
			}
		}
		return ok;
	}

	/**
	 * Load data from a specific file. Note that a datamap point may 
	 * only refer to part of the file since d3 swv files can contain 
	 * data from multiple starts if it's on a duty cycle so only load data 
	 * from within those limits. 
	 * @param dataBlock data block
	 * @param mapPoint data map point
	 * @param dataStart data start time
	 * @param dataEnd data end time
	 */
	private boolean loadData(PamDataBlock dataBlock, D3DataMapPoint mapPoint,
			long dataStart, long dataEnd) {

		WavFileReader wavFile = new WavFileReader(mapPoint.getDataFile().getAbsolutePath());
		WavHeader wavHeader = wavFile.getWavHeader();
		int swvSampleRate = wavHeader.getSampleRate(); // is sample rate * 1000;
		//		double swvSeconds = (long) wavHeader.getDataSize() * 1000 / swvSampleRate / wavHeader.getBlockAlign();
		dataStart = Math.max(dataStart, mapPoint.getStartTime());
		dataEnd = Math.min(dataEnd, mapPoint.getEndTime());
		// dataStartSample and dataEndSample are absolute sample numbers in the file that we're going to read between. 
		long dataStartSample = mapPoint.fileOffsetStart;
		if (dataStart > mapPoint.getStartTime()) {
			dataStartSample += (dataStart - mapPoint.getStartTime()) * swvSampleRate / 1e6;
		}
		long dataEndSample = mapPoint.fileOffsetEnd;
		if (dataEnd < mapPoint.getEndTime()) {
			dataEndSample -= (mapPoint.getEndTime()-dataEnd) * swvSampleRate / 1e6;
		}

		int samplesPerChunk = Math.max(swvSampleRate / 1000, 1);
		long currentSample = dataStartSample;
		if (!wavFile.setPosition(currentSample)) {
			return false;
		}

		long now = dataStart;
		long totalSamplesRead = 0;
		while (currentSample < dataEndSample) {
			int samplesToRead = samplesPerChunk;
			if (currentSample + samplesToRead > dataEndSample) {
				samplesToRead = (int) (dataEndSample - currentSample);
			}
			// now read them !
			short[][] data = new short[wavHeader.getNChannels()][samplesToRead];
			int samplesRead = wavFile.readData(data);
//			System.out.println(String.format("%d of %d samples read from file %s", samplesRead, samplesToRead, mapPoint.getName()));
			if (samplesRead != samplesToRead) {
				break;
			}
			currentSample += samplesRead;
			long currentTime = dataStart + totalSamplesRead * 1000000L / swvSampleRate;
			totalSamplesRead += samplesRead;
			createDataUnit(currentTime, data, samplesRead, wavHeader.getSampleRate());
		}

		wavFile.close();
		return true;
	}

	/**
	 * Unpack the short data read back from the swv file and put it into a 
	 * data unit. 
	 * @param currentTime time at start of data unit
	 * @param data packed int16 data
	 * @param samplesRead number of samples (may be less than size of data). 
	 */
	private void createDataUnit(long currentTime, short[][] data,
			int samplesRead, int baseSampleRate) {
//		/*
//		 * For now, just try to extract the depth information. 
//		 * This is given code 
//		 */
//		int pressChan = 4869;
//		D3SensorInfo sensInfo = findSensorInfo(pressChan);
//		short[] sensData = repackData(data, sensInfo.getListIndexes()); 

		D3DataUnit d3DataUnit = new D3DataUnit(currentTime);
//		d3DataUnit.setDepthData(sensData);
		// pack all the data into the data unit. 
		for (D3SensorInfo sensInfo:d3SensorInfos) {
			float[] sensorData = repackData(data, sensInfo.getListIndexes());
//			if (sensInfo.getCalInfo() != null) {
//				sensorData = sensInfo.getCalInfo().apply_cal(sensorData);
//			}
			d3DataUnit.addSensorData(sensorData, sensInfo.getListIndexes().length*baseSampleRate);
		}
		
		float[] depth = calulateDepth(d3DataUnit);
		d3DataUnit.setDepth(depth);

		int ind = 0;
		int accelInd = 0;
		float[] jerk = null;
		for (D3SensorInfo sensInfo:d3SensorInfos) {
			float[] sensorData = d3DataUnit.getSensorData(ind++);
			if (sensInfo.getCalInfo() != null) {
				sensInfo.getCalInfo().apply_cal(sensorData);
			}
			
			if (sensInfo.getName().startsWith("ACC")) {
				if (accelInd == 0) {
					jerk = new float[sensorData.length];
				}
				for (int i = 0; i < sensorData.length; i++) {
					float diffAccel = sensorData[i]-oldAccell[accelInd];
					oldAccell[accelInd] = sensorData[i];
					jerk[i] += diffAccel*diffAccel;
				}
				accelInd++;
			}
		}
		/**
		 * If its a new data load or if there has been a gap in the data
		 * due to cycled sampling, it may need to ditch the first point (set to zero)
		 * since it won't correctly match. 
		 */
		if (previousJerkUnit == null || d3DataUnit.getTimeMilliseconds()-previousJerkUnit.getTimeMilliseconds() > 2000) {
			jerk[0] = 0;
		}
		d3DataUnit.setJerk(jerk);
		
		// now repack the d3 data again. 

		d3DataBlock.addPamData(previousJerkUnit = d3DataUnit);
	}
	
	float[] calulateDepth(D3DataUnit dataUnit) {
		
		// find the pressure calibration and make a depth line. 
		if (calibrations == null) {
			return null;
		}
		CalibrationInfo depthCal = calibrations.findCalibrationInfo("press");
		CalibrationInfo tempCal = calibrations.findCalibrationInfo("press:bridge");
		int pressInd = findSensorIndex("press");
		if (pressInd < 0 || depthCal == null) {
			System.err.println("Unable to find pressure sensor and calibration information");
			return null;
		}
		D3SensorInfo pressureInfo = d3SensorInfos.get(pressInd);
		float[] depth = dataUnit.getSensorData(pressInd).clone();
		depthCal.apply_cal(depth);
		
		return depth;
	}

	/**
	 * Repack data, using the selected channel indexes. 
	 * @param data
	 * @param inds
	 * @return
	 */
	private float[] repackData(short[][] data, int[] inds) {
		int nSamps = data[0].length;
		int newSamps = inds.length * nSamps;
		float[] newData = new float[newSamps];
		int iSamp = 0;
		for (int i = 0; i < nSamps; i++) {
			for (int j = 0; j < inds.length; j++) {
				newData[iSamp] = data[inds[j]][i];
				if (newData[iSamp] < 0) {
					newData[iSamp] += 65536; // numbers are unsigned. 
				}
				newData[iSamp] /= 65536.; // read on scale of 0 - 1.
				iSamp++;
			}
		}
		return newData;
	}


	@Override
	public boolean saveData(PamDataBlock dataBlock) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return the d3DataBlock
	 */
	public D3DataBlock getD3DataBlock() {
		return d3DataBlock;
	}

	/**
	 * Called for the first xml to be unpacked during the data mapping stage in viewer to 
	 * make a list of sensors, get their names and cal information, etc.
	 * @param sensorList
	 * @return
	 */
	private boolean makeSensorInformation(int[] sensorList, WavHeader swvHeader) {
		if (sensorList == null) {
			return false;
		}
		
//		List<String[]> sensorDefs = readSensorCSVFile();


		String fn = getFileParams().offlineFolder + "\\d418.xml";
		File calFile = new File(fn);
		if (!calFile.exists()) {
			System.out.println("D3 cal file cannot be found at " + fn);
//			return false;
			calibrations = null;
		}
		else {
			CalFileReader calFileReader = CalFileReader.openCalFile(calFile);
			calibrations = calFileReader.readEverything();
		}

		for (int i = 0; i < sensorList.length; i++) {
			if (findSensorInfo(sensorList[i]) == null) {
				D3SensorInfo sensorInfo = new D3SensorInfo(sensorList[i], sensorList);
				d3SensorInfos.add(sensorInfo);
				System.out.println("D Sensor info: " + sensorInfo);
				if (calibrations != null) {
					CalibrationInfo calInfo = calibrations.findCalibrationInfo(sensorInfo.getCal().trim());
					sensorInfo.setCalInfo(calInfo);
				}
			}
		}
//		
//		D3SensorInfo pressureInfo = findSensorInfo("press");
//		if (pressureInfo != null) {
//		}
		

		return d3SensorInfos.size() > 0;
	}

	/**
	 * Find sensor information corresponding to a specific id. 
	 * @param sensorId integer sensor id
	 * @return sensor information. 
	 */
	public D3SensorInfo findSensorInfo(int sensorId) {
		for (D3SensorInfo info:d3SensorInfos) {
			if (info.getSensorId() == sensorId) {
				return info;
			}
		}
		return null;
	}
	/**
	 * Find sensor information corresponding to a specific id. 
	 * @param sensorId integer sensor id
	 * @return sensor information. 
	 */
	public D3SensorInfo findSensorInfo(String calName) {
		int infoIndex = findSensorIndex(calName);
		if (infoIndex < 0) return null;
		return d3SensorInfos.get(infoIndex);
	}
	/**
	 * Find sensor information corresponding to a specific id. 
	 * @param sensorId integer sensor id
	 * @return sensor index. 
	 */
	public int findSensorIndex(int sensorId) {
		int ind = 0;
		for (D3SensorInfo info:d3SensorInfos) {
			if (info.getSensorId() == sensorId) {
				return ind;
			}
			ind++;
		}
		return -1;
	}
	
	/**
	 * Find a sensor by the name of it's cal field. 
	 * @param sensorName
	 * @return
	 */
	public int findSensorIndex(String sensorName) {
		int ind = 0;
		for (D3SensorInfo info:d3SensorInfos) {
			if (info.getCal().trim().equals(sensorName)) {
				return ind;
			}
			ind++;
		}
		return -1;
	}

	/**
	 * @return the d3SensorInfos
	 */
	public ArrayList<D3SensorInfo> getD3SensorInfos() {
		return d3SensorInfos;
	}

	/**
	 * @return the d3DataPlotProvider
	 */
	public D3DataPlotProvider getD3DataPlotProvider() {
		return d3DataPlotProvider;
	}

}
