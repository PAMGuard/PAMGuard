package loc3d_Thode;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import Acquisition.AcquisitionProcess;
//import loc3d_Thode.FindAnchorOverlayGraphics;
//import FindAnchor.FindAnchorSQLLogging;
import Array.ArrayManager;
import Array.PamArray;
import Array.SnapshotGeometry;
import Localiser.DelayMeasurementParams;
import Localiser.algorithms.Correlations;
import PamController.PamController;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamDetection.PamDetection;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.symbol.StandardSymbolManager;
//import PamguardMVC.DataType;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import clickDetector.ClickDetection;
import fftManager.FFT;

public class TowedArray3DProcess extends PamProcess{


	/**
	 * Refefence to PamContolledUnit for this module
	 */
	TowedArray3DController towedArray3DController;

	/**
	 * Reference to the data source data block
	 */
	PamDataBlock detectorEventDataBlock;

	/**
	 * Datablock for output data.
	 */
	PamDataBlock<TowedArray3DDataUnit> localizationDataBlock;

	TowedArray3DOverlayGraphics overlayGraphics;

	/**
	 * bitmap of channels in use. 
	 */
	int usedChannels; // bitmap of channels being analysed. 

	/**
	 * reference to a list of detectors handling data from a single channel each. 
	 */
	StationQueue[] stationDetectors;
	CrossStationMatcher crossStationMatcher;
	double direct_arrival_time[];
	PamArray array;
	double tdd,tdd_maxtime,subarray_maxtime, subarray_deck_distance;
	boolean fine_tune_tdd,fine_tune_tds;
	FileWriter output;

	private DelayMeasurementParams delayParams = new DelayMeasurementParams();

	/**
	 * At some point we'll need to get back to the original ADC data
	 * and hydrophone information in order to convert amplitude data
	 * to dB re 1 micropascal - so we'll need daqProcess. 
	 */
	AcquisitionProcess daqProcess;


	public TowedArray3DProcess(TowedArray3DController towedArray3DController) {
		// you must call the constructor from the super class. 
		super(towedArray3DController, null);

		// keep a reference to the PamControlledUnit contolling this process. 
		this.towedArray3DController = towedArray3DController;

		/* create an output data block and add to the output block list so that other detectors
		 * can see it. Also set up an overlay graphics class for use with the data block. 
		 */ 
		//groupDetDataBlock = new PamDataBlock(DataType.DETEVENT, towedArray3DController.getUnitName(), this, 0);
		localizationDataBlock = new PamDataBlock<TowedArray3DDataUnit>(TowedArray3DDataUnit.class, 
				towedArray3DController.getUnitName(), this, 0);
//		groupDetDataBlock.setOverlayDraw(new FindAnchorOverlayGraphics(towedArray3DController));

		/*
		 * tell the datablock that it's data units are going to have localisations with all sorts
		 * of position information
		 * and set a simple overlay graphics class so that they all draw nicely on the maps and other displays. 
		 */ 
		localizationDataBlock.setLocalisationContents(LocContents.HAS_BEARING | 
				LocContents.HAS_RANGE | LocContents.HAS_DEPTH | LocContents.HAS_AMBIGUITY);
		localizationDataBlock.addLocalisationContents(LocContents.HAS_LATLONG);
		overlayGraphics = new TowedArray3DOverlayGraphics(this);
		localizationDataBlock.setOverlayDraw(overlayGraphics);
		localizationDataBlock.setPamSymbolManager(new StandardSymbolManager(localizationDataBlock, TowedArray3DOverlayGraphics.defaultSymbol, true));

		localizationDataBlock.SetLogging(new TowedArray3DSQLLogging(towedArray3DController, localizationDataBlock));
		// use the standard logging class.
//		localizationDataBlock.SetLogging(new PamDetectionLogging(localizationDataBlock, SQLLogging.UPDATE_POLICY_OVERWRITE));

		addOutputDataBlock(localizationDataBlock);

		/*
		 * Create a datablock for background measurements, but don't register it - it will be found anyway 
		 * by the display plug in that uses it. 
		 */

	}

	@Override
	public void pamStart() {
		//direct_arrival_time = new double[2][towedArray3DController.towedArray3DProcessParameters.minICINum];
		direct_arrival_time = new double[2];

		try {
			output = new FileWriter("ThreeD.txt");
		} catch (IOException ex){
			ex.printStackTrace();
		}

		for (int i = 0; i < 2; i++) {
			//if (((1<<i) & usedChannels) > 0) {
			stationDetectors[i].pamStart();
			//	}
		}
		this.array = ArrayManager.getArrayManager().getCurrentArray();
		int hydrophoneList = ((AcquisitionProcess) this.getSourceProcess()).getAcquisitionControl().ChannelsToHydrophones(usedChannels);
		SnapshotGeometry geom = ArrayManager.getArrayManager().getSnapshotGeometry(hydrophoneList, PamCalendar.getTimeInMillis());
		double maxSeparation = geom.getMaxSeparation();
//		this.subarray_maxtime=array.getHydrophoneLocator().getPairSeparation(0, 0, 1)/1.5;
//		this.tdd_maxtime=array.getHydrophoneLocator().getPairSeparation(0, 0, 2)/1.5;
		this.subarray_maxtime=maxSeparation/1.5;
		this.tdd_maxtime=maxSeparation/1.5;
		subarray_deck_distance = Math.abs(array.getHydrophoneCoordinate(0, 1, PamCalendar.getTimeInMillis()));
		//this.tdd_maxtime=ArrayManager.getArrayManager().getCurrentArray().getSeparationInMillis(hydrophoneList);

		crossStationMatcher= new CrossStationMatcher(1,this, this.tdd_maxtime,towedArray3DController.towedArray3DProcessParameters.percentICIMatchError/100.0);
		crossStationMatcher.setFocalStation(stationDetectors[1]); //Rear subarray will be focus for matching detections...
		crossStationMatcher.addQueueList(stationDetectors[0]);


	}

	@Override
	public void pamStop() {

		if (output != null) {
			try{
				output.close();
			} catch(IOException ex){
				ex.printStackTrace();
			}
		}
		// this method is abstract in the superclass and must therefore be overridden
		// even though it doesn't do anything. 

	}

	@Override
	/**
	 * PamProcess already implements PamObserver, enabling it to subscribe
	 * to PamDataBlocks and get notifications of new data. So that the 
	 * Pamguard Profiler can monitor CPU usage in each module, the function
	 * update(PamObservable o, PamDataUnit arg) from the PAmObserver interface
	 * is implemented in PamProcess. PamProcess.update handles profiling
	 * tasks and calls newData where the developer can process newly 
	 * arrived data. 
	 */
	public void newData(PamObservable o, PamDataUnit arg) {
		// see which channel it's from
		//Unpack particular type of data unit.  This line can be changed depending on the detector...
		float z[], tilt[], heading[];
		float fs;
		double bearing;
		int chan;
		long startSample, duration;
		z = new float[2];
		tilt = new float[2];
		heading = new float[2];
		//PamLocalisation ldu;
		TowedArray3DDataUnit ldu;

		/*Give an exit to suspend operation of this module using a checkbox option*/
		if(!towedArray3DController.towedArray3DProcessParameters.yes_process)
			return;

		PamDataBlock dataBlock = (PamDataBlock) o;
		String unitType=dataBlock.getParentProcess().getPamControlledUnit().getUnitName();
		double rawdata[];
		if (unitType.compareTo("Click Detector")>-1){
			ClickDetection click = (ClickDetection) arg;
			rawdata=click.getWaveData(0);
		}else
			rawdata = null;
		PamDataUnit pamDetection = arg;	// was originally casting PamDataUnit arg to PamDetection pamDetection 
		fs = dataBlock.getSampleRate();
		chan = PamUtils.getLowestChannel(arg.getChannelBitmap());
		startSample = pamDetection.getStartSample();
		duration = pamDetection.getSampleDuration();
		bearing = 0;
		AbstractLocalisation localisation = pamDetection.getLocalisation();
		if (localisation != null && localisation.hasLocContent(LocContents.HAS_BEARING)) {
			bearing = localisation.getBearing() * 180 / Math.PI;
		}

		int istation = (int)Math.floor(chan/2);  //A station is a collection of hydrophones that returns a bearing or other localization feature.
		//System.out.println("");
		//System.out.println("istation "+istation + " chan "+ chan + " stationDetectors.length "+ stationDetectors.length + " Bearing:" + bearing);
		// check that a detector has been instantiated for that detector
		if (stationDetectors == null || 
				stationDetectors.length < istation || 
				stationDetectors[istation] == null) {
			return;
		}

		SnapshotGeometry geometry = arg.getSnapshotGeometry();
		//Call the channel detector, having unwrapped all information needed from detection..
		//PamObservable o, int istation, float fs, long peakSample, double bearing, int Nstations) 

		int echo_flag = stationDetectors[istation].newData(towedArray3DController.towedArray3DProcessParameters,fs,Math.round(startSample+ 0.5*duration), bearing, rawdata);
		//Try to link latest detection to other station...

		switch (towedArray3DController.towedArray3DProcessParameters.algchoice) {
		case 0:
			//CROSS BEARING
			// Situation where we want to use cross-bearing technique even if no echo is detected...
			if (istation==1)
				crossStationMatcher.setLastFocalICIList();  //Set ICIlist
			if (crossStationMatcher.crossLink()){
				double bearing_for=crossStationMatcher.getLinked_bearings()[0];
				double bearing_rear=crossStationMatcher.getFocal_bearing();
				ldu =new TowedArray3DDataUnit(pamDetection,0,1,3,true,true,false);
				Crossbearing(tdd_maxtime*1.5,subarray_deck_distance,z,bearing_for,bearing_rear, ldu);
				localizationDataBlock.addPamData(ldu);
				break;
			} //crossLink()
			break;
		case 1:
			/*
			 * Case 1: Use relative TOA only, with simple TOA estimate
			 */
			for (int i=0; i<2; i++){
				direct_arrival_time[i]=stationDetectors[i].getLastTimeWithEcho();						
				z[i]= (float) geometry.getReferenceGPS().getHeight();
			}
			//System.out.println("direct_arrival_time[0]-direct_arrival_time[1] =" + (direct_arrival_time[1]-direct_arrival_time[0]));
			if (derive_simple_TOA(fs,stationDetectors[1].getLastIndexWithEcho(),stationDetectors[0].getLastIndexWithEcho())){ //if relatve TOA can be found and tdd assigned.
				double tds_f=stationDetectors[0].getLastEchoTime();
				double tds_r=stationDetectors[1].getLastEchoTime();
				ldu = new TowedArray3DDataUnit(pamDetection,0,1,3,true,true,true);
				ThreeD_TOA_only(z, tds_f, tds_r, tdd,tdd_maxtime*1.5, subarray_deck_distance, ldu);
				localizationDataBlock.addPamData(ldu);
				//overlayGraphics.setSymbolSize(ldu.getDepth());
				break;
			}
			break;

		case 2: 
			/*
			 * Case 2: Use relative TOA and rear bearing, with simple TOA estimate
			 */

			direct_arrival_time[0]=stationDetectors[0].getLastTime();
			direct_arrival_time[1]=stationDetectors[1].getLastTimeWithEcho();
			for (int i=0; i<2; i++){			
				z[i]= (float) geometry.getReferenceGPS().getHeight();
//				z[i]=(float) -array.getHydrophoneLocator().getPhoneHeight(arg.getTimeMilliseconds(), (2*i+1));
//				tilt[i] = (float) array.getHydrophoneLocator().getPhoneTilt(arg.getTimeMilliseconds(), (2*i+1));
				tilt[i] = (float) geometry.getReferenceGPS().getPitch();
			}
			//System.out.println("direct_arrival_time[0]-direct_arrival_time[1] =" + (direct_arrival_time[1]-direct_arrival_time[0]));
			if (derive_simple_TOA(fs,stationDetectors[1].getLastIndexWithEcho(),stationDetectors[0].getLastIndex())){ //if cross station
				double tds_r=stationDetectors[1].getLastEchoTime();
				double bearing_rear=stationDetectors[1].getLastBearingWithEcho();
				//Create a data unit.  To map, second argument "locContents" must be 0, 
				// and second argument is reference hydrophone, set to one for forward subarray.
				ldu = new TowedArray3DDataUnit(pamDetection,0,1,3,true,true,true);
				ThreeD_rear_bearing(z,tds_r,tdd,tdd_maxtime*1.5,subarray_deck_distance,bearing_rear,tilt, ldu);
				localizationDataBlock.addPamData(ldu);
				//overlayGraphics.setSymbolSize(ldu.getDepth());
			}
			break;

		case 3: 
			/*
			 * Case 3:Use relative TOA only, using TOA estimate using ICI.
			 */

			//if (stationDetectors[0].IsLastDetectionComplete()&stationDetectors[1].IsLastDetectionComplete()) {
			/* Logic:  If we are currently on rear subarray, and is now complete(has echo), then we match to cross channel.
			 * If we are currently on forward array, and have completed a detection (forward echo on array), then
			 * we want to match to cross channel to check whether we have a match with a previously unmatched rear array complete
			 * detection.
			 */
			if (istation==1)
				crossStationMatcher.setLastFocalWithEchoICIList();  //Set ICIlist
			if (crossStationMatcher.crossLinkWithEcho()){
				for (int i=0; i<2; i++){
//					z[i]=(float) -array.getHydrophoneLocator().getPhoneHeight(arg.getTimeMilliseconds(), (i*2+1));
					z[i] = (float) geometry.getReferenceGPS().getHeight();
				}
				double tds_f=crossStationMatcher.getLinked_echo_times()[0];
				double tds_r=crossStationMatcher.getFocal_echo_time();
				ldu = new TowedArray3DDataUnit(pamDetection,0,1,3,true,true,true);
				ThreeD_TOA_only(z, tds_f, tds_r, crossStationMatcher.getTOA(fine_tune_tdd)[0],tdd_maxtime*1.5, subarray_deck_distance, ldu);
				localizationDataBlock.addPamData(ldu);
				//overlayGraphics.setSymbolSize(ldu.getDepth());
				break;
			}
			break;
		case 4:
			/*
			 * Case 4: Use relative TOA and rear bearing, with TOA estimate using ICI
			 */
			//Use rear array bearing, do not need or use forward array echo time...
			if (istation==1)
				crossStationMatcher.setLastFocalWithEchoICIList();  //Set ICIlist
			if (crossStationMatcher.crossLink()) {
				for (int i=0; i<2; i++){
					z[i] = (float) geometry.getReferenceGPS().getHeight();
					tilt[i] = (float) geometry.getReferenceGPS().getPitch();
//					z[i]=(float) -array.getHydrophoneLocator().getPhoneHeight(arg.getTimeMilliseconds(), (2*i+1));
//					tilt[i] = (float) array.getHydrophoneLocator().getPhoneTilt(arg.getTimeMilliseconds(), (2*i+1));
				}
				//System.out.println("direct_arrival_time[0]-direct_arrival_time[1] =" + (direct_arrival_time[1]-direct_arrival_time[0]));
				double tds_r=crossStationMatcher.getFocal_echo_time();
				double bearing_rear=crossStationMatcher.getFocal_bearing();
				//Create a data unit.  To map, second argument "locContents" must be 0, 
				// and second argument is reference hydrophone, set to one for forward subarray.
				ldu = new TowedArray3DDataUnit(pamDetection,0,1,3,true,true,true);
				ThreeD_rear_bearing(z,tds_r,crossStationMatcher.getTOA(fine_tune_tdd)[0],tdd_maxtime*1.5,subarray_deck_distance,bearing_rear,tilt, ldu);
				localizationDataBlock.addPamData(ldu);
				//overlayGraphics.setSymbolSize(ldu.getDepth());
			}
			break;



		default:
			System.out.println("No tracking algorithm has been selected");
		}//switch
	}



	public boolean derive_simple_TOA(float fs, int i_focal, int i_other){

		tdd=direct_arrival_time[1]-direct_arrival_time[0];

		if (fine_tune_tdd&i_focal>-1&i_other>-1) {
			double [] focal_data=stationDetectors[1].getRawData(i_focal);
			double[] other_data=stationDetectors[0].getRawData(i_other);
			Correlations correlations = new Correlations();
			int nFFT = 2 * FFT.nextBinaryExp(Math.max(other_data.length,focal_data.length));
			double lagSams = correlations.getDelay(other_data, 
					focal_data, delayParams, getSampleRate(), nFFT).getDelay();
			double lagSec = 1000*lagSams / fs;
			tdd+=lagSec;
		}
		if (Math.abs(tdd) <tdd_maxtime)
			return true;
		else
			return false;
	}

	public void ThreeD_rear_bearing(float z[], double tds_r, double tdd, double L, double La, 
			double eta, float tilt0[], TowedArray3DDataUnit ldu)
	{
		final double c =1.5;
		double cosnd_r=Math.cos(eta*Math.PI/180);
		double tilt=tilt0[1]*Math.PI/180;
		double tilt_global=Math.asin((z[1]-z[0])/L);  //global tilt of cable...
		double Lhoriz = Math.sqrt(Math.pow(L,2)-Math.pow(z[1]-z[0], 2));
		La = Math.sqrt(Math.pow(La,2)-Math.pow(z[0], 2));
		//double term1=Math.cos(tilt_global)/Math.cos(tilt);
		//double term2=Math.sin(tilt_global-tilt)/Math.cos(tilt);

		double testt=Math.cos(tilt)*c*tdd-cosnd_r*Lhoriz*Math.cos(tilt_global);
		double Pd_f=Lhoriz*c*tdd*cosnd_r*Math.cos(tilt_global);
		Pd_f = Pd_f -0.5*Math.cos(tilt)*(Math.pow(Lhoriz,2)+Math.pow(c*tdd,2))+ 
		Lhoriz*Math.sin(tilt_global-tilt)*(z[1]-0.25*c*tds_r*c*(2*tdd+tds_r)/z[1]);
		Pd_f=Pd_f/(Math.cos(tilt)*c*tdd-cosnd_r*Lhoriz*Math.cos(tilt_global)+0.5*Lhoriz*c*tds_r*Math.sin(tilt_global-tilt)/z[1]);

		//%S=(Pdd-L.*cosnd_r);
		//%Pd_f=0.5*(2*L.*Pdd.*cosnd_r-L.^2-Pdd.^2)./S;

		double Pd_r=Pd_f+c*tdd;
		double depth=0.25*c*tds_r*(2*Pd_r+c*tds_r)/z[1];
		double range_r=Math.sqrt(Math.pow(Pd_r,2)-Math.pow(z[1]-depth,2));
		double range_f=Math.sqrt(Math.pow(Pd_f,2)-Math.pow(z[0]-depth,2));
		double sin_el_r=range_r/Pd_r;
		double cos_el_r=(z[1]-depth)/Pd_r;
		double cos_azimuth_rear=(cosnd_r-cos_el_r*Math.sin(tilt))/(Math.cos(tilt)*sin_el_r);
		double azimuth_rear=Math.acos(cos_azimuth_rear);
		double azimuth_forward=Math.asin(range_r*Math.sin(azimuth_rear)/range_f);
		double test = Math.pow(range_f,2)-Math.pow(range_r,2)+Math.pow(Lhoriz,2);  //Is forward angle acute?
		/*
		 * The sine of azimuth_rear will always be positive, since azi_rear lies between 0 and pi.
		 * 	Thus result azimuth_forward will always be between 0 and 90 degrees.
		 */
		if (test>=0){// if original forward angle obtuse
			azimuth_forward=Math.PI-azimuth_forward; //Convert angle to origin relative to bow
		}

		double range_boat = Math.sqrt(Math.pow(Lhoriz+La,2)+Math.pow(range_r,2)-2*(Lhoriz+La)*range_r*Math.cos(azimuth_rear));
		double azimuth_boat =Math.PI-Math.acos((Math.pow(Lhoriz+La,2)+Math.pow(range_boat,2)-Math.pow(range_r, 2))/(2*(Lhoriz+La)*range_boat));


		ldu.setDepth(depth);
		ldu.setRanges(range_r,1);
		ldu.setRanges(range_f,0);
		ldu.setRanges(range_boat, 2);
		ldu.setAngle(azimuth_forward*1,0);
		ldu.setAngle(azimuth_rear*1,1);
		ldu.setAngle(azimuth_boat*1,2);
		//ldu.getLocalisation().s(latlong)
		if (range_r<10E6){
			ldu.getLocalisation().getLocContents().setLocContent(LocContents.HAS_BEARING | LocContents.HAS_RANGE|LocContents.HAS_DEPTH);
			ldu.getLocalisation().addLocContents(LocContents.HAS_LATLONG);
			ldu.setLatLong();
		}
		else
			ldu.getLocalisation().getLocContents().setLocContent(0);


		try {
			//output.write("depth: " + depth + " Range forward: " + range_f
			//		+ " Range rear: " + range_r + " Time " + cal.getTime() + " zf: "
			//		+ z[0] + " zr:" + z[1] + "\n");
			Calendar cal = Calendar.getInstance();
			output.write(String.format("\nThreeD_TOAonly  tds_r: %6.4f tdd: %6.4f rear bearing: %6.4f\n",
					tds_r,tdd,eta));
			output.write(String.format("depth: %6.2f Range boat: %6.2f Range forward: %6.2f Range rear: %6.2f \n azimuth boat: %6.2f, azimuth forward: %6.2f azimuth rear: %6.2f \n z[0]: %6.2f z[1]: %6.2f tilt[0]: %6.2f tilt[1]:%6.2f Time: %tc %x\n",
					depth,range_boat, range_f,range_r,azimuth_boat*180/Math.PI,azimuth_forward*180/Math.PI,azimuth_rear*180/Math.PI,z[0],z[1],tilt0[0],
					tilt0[1],cal.getTime(),cal.getTimeInMillis()));

		} catch (Exception e) {
			e.printStackTrace();
		}	

	}
	public void Crossbearing(double L, double La, float z[], double bearing_f, 
			double bearing_r, TowedArray3DDataUnit ldu)
	{
		bearing_f=bearing_f*Math.PI/180;
		bearing_r=bearing_r*Math.PI/180;
		La=Math.sqrt(Math.pow(La,2)-Math.pow(z[0],2));
		L=Math.sqrt(Math.pow(L,2)-Math.pow(z[1]-z[0],2));
		double bearing_source=bearing_f-bearing_r; //Third angle of triangle... 180-(180-bearing_f)-bearing_r
		//double range_r = L/(Math.sin(bearing_r)/Math.tan(bearing_f)+Math.cos(bearing_r));
		double range_r=L*Math.sin(bearing_f)/Math.sin(bearing_source);
		double range_f=range_r*Math.sin(bearing_r)/Math.sin(bearing_f);
		double range_boat = Math.sqrt(Math.pow(L+La,2)+Math.pow(range_r,2)-2*(L+La)*range_r*Math.cos(bearing_r));
		double bearing_boat =Math.PI-Math.acos((Math.pow(L+La,2)+Math.pow(range_boat,2)-Math.pow(range_r, 2))/(2*(L+La)*range_boat));

		ldu.setRanges(range_f,0);
		ldu.setRanges(range_r,1);
		ldu.setRanges(range_boat, 2);
		ldu.setAngle(bearing_f*1,0);
		ldu.setAngle(bearing_r*1,1);
		ldu.setAngle(bearing_boat*1,2);
		//Example of bitwise manipulation, the '|' is bitwise OR operator, sets locContents variable to 6...
		ldu.getLocalisation().getLocContents().setLocContent(LocContents.HAS_BEARING | LocContents.HAS_RANGE);
		ldu.getLocalisation().addLocContents(LocContents.HAS_LATLONG);
		ldu.setLatLong();

		try {
			//output.write("depth: " + depth + " Range forward: " + range_f
			Calendar cal = Calendar.getInstance();

			output.write(String.format("Cross bearing: Range boat: %6.2f Range forward: %6.2f Range rear: %6.2f \n ship bearing: %6.2f forward bearing: %6.2f rear bearing: %6.2f Time: %tc %x\n",
					range_boat, range_f,range_r,bearing_boat*180/Math.PI,bearing_f*180/Math.PI,bearing_r*180/Math.PI,
					cal.getTime(),cal.getTimeInMillis()));

		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public void ThreeD_TOA_only(float z[], double tds_f, double tds_r, double tdd, double L, double La, TowedArray3DDataUnit ldu)
	{
		final double c = 1.5;
		La=Math.sqrt(Math.pow(La,2)-Math.pow(z[0],2));
		L=Math.sqrt(Math.pow(L,2)-Math.pow(z[1]-z[0],2));

		double Phat=tds_f/tds_r;
		double zhat = z[0]/z[1];
		double S = zhat/Phat;
		double Q = (2*tdd+tds_r)*c;
		double slant_range_f= 0.5*(tds_f*c-Q*S)/(S-1);
		double slant_range_r=slant_range_f+tdd*c;
		double depth = 0.25*(tds_f*c*(2*slant_range_f+tds_f*c))/z[0];
		ldu.setDepth(depth);
		ldu.setRanges(Math.sqrt(Math.pow(slant_range_f,2)-Math.pow(depth-z[0],2)),0);
		ldu.setRanges(Math.sqrt(Math.pow(slant_range_r,2)-Math.pow(depth-z[1],2)),1);


		double range[] = ldu.getRanges();
		double test = Math.pow(range[1],2)+Math.pow(L,2)-Math.pow(range[0],2);
		double azimuth_rear = Math.acos(test/(2*L*range[1]));
		/*
		 * Two cases: if test is positive then forward range^2 is smaller than RA, azimuth_rear is acute.  No change needed
		 * 	If test negative then aziuth_rear is obtuse, and acos returns a value greater than 90 degrees.  All good
		 */
		double azimuth_forward = Math.asin(Math.sin(azimuth_rear)*range[1]/range[0]);
		test = Math.pow(range[0],2)-Math.pow(range[1],2)+Math.pow(L,2);  //Is forward angle acute?

		/*
		 * The sine of azimuth_rear will always be positive, since azi_rear lies between 0 and pi.
		 * 	Thus result azimuth_forward will always be between 0 and 90 degrees.
		 */
		if (test>=0){// if original forward angle obtuse
			azimuth_forward=Math.PI-azimuth_forward; //Convert angle to origin relative to bow
		}

		//azimuth_rear = azimuth_rear*180/Math.PI;

		double range_boat = Math.sqrt(Math.pow(L+La,2)+Math.pow(range[1],2)-2*(L+La)*range[1]*Math.cos(azimuth_rear));
		double bearing_boat =Math.PI-Math.acos((Math.pow(L+La,2)+Math.pow(range_boat,2)-Math.pow(range[1], 2))/(2*(L+La)*range_boat));


		ldu.setAngle(azimuth_forward,0);
		ldu.setAngle(azimuth_rear,1);
		ldu.setAngle(bearing_boat,2);
		ldu.setRanges(range_boat,2);
		if (depth>0&range_boat>0)
		{
			ldu.getLocalisation().getLocContents().setLocContent(LocContents.HAS_BEARING | LocContents.HAS_RANGE|LocContents.HAS_DEPTH);
			ldu.getLocalisation().addLocContents(LocContents.HAS_LATLONG);
			ldu.setLatLong();
		}
		else
			ldu.getLocalisation().getLocContents().setLocContent(0);


		try {
			//output.write("depth: " + depth + " Range forward: " + range_f
			//		+ " Range rear: " + range_r + " Time " + cal.getTime() + " zf: "
			//		+ z[0] + " zr:" + z[1] + "\n");
			Calendar cal = Calendar.getInstance();
			output.write(String.format("\nThreeD_TOAonly tds_f: %6.4f tds_r: %6.4f tdd: %6.4f \n",
					tds_f,tds_r,tdd));
			output.write(String.format("depth: %6.2f\n Range ship: %6.2f Range forward: %6.2f Range rear: %6.2f \n ship bearing: %6.2f forward bearing: %6.2f rear bearing: %6.2f \n z[0]: %6.2f z[1]: %6.2f Time: %tc %x\n",
					depth,range_boat,range[0],range[1],bearing_boat*180/Math.PI,azimuth_forward*180/Math.PI,azimuth_rear*180/Math.PI,
					z[0],z[1],cal.getTime(),cal.getTimeInMillis()));

		} catch (Exception e) {
			e.printStackTrace();
		}	


	}
	@Override
	/**
	 * One of the 'annoyances' of the very flexible Pamguard model is that 
	 * it's impossible to say at development time exactly what order 
	 * pamguard models will be generated in. This example is dependent on 
	 * FFT data but it's possible (although unlikely) that the FFT producing 
	 * module will be created after this module. Therefore there isn't much point in 
	 * looking for the FFT data in this modules constructor. When new modules 
	 * are added, a notification is sent to all PamControlledUnits. I've arranged 
	 * for TowedArray3DController to call prepareProcess() when this happens so 
	 * that we can look around and try to find an Detector data block. 
	 */
	public void prepareProcess() {
		super.prepareProcess();
		/*
		 * Need to hunt aroudn now in the Pamguard model and try to find the Detector
		 * data block we want and then subscribe to it. 
		 * First get a list of all PamDataBlocks holding FFT data from the PamController
		 * 
		 * Good location to get reference to parent data block...
		 * 
		 */
		//ArrayList<PamDataBlock> detectorEventDataBlocks = PamController.getInstance().getDetectorEventDataBlocks();

		ArrayList<PamDataBlock> detectorEventDataBlocks = PamController.getInstance().getDataBlocks(PamDetection.class,true);

		if (detectorEventDataBlocks == null || 
				detectorEventDataBlocks.size() <= towedArray3DController.towedArray3DProcessParameters.detectorDataBlock) {
			setParentDataBlock(null);
			return;
		}
		/*
		 * If the detector data blocks exist, subscribe to the one in the parameters. 
		 */
		detectorEventDataBlock = detectorEventDataBlocks.get(towedArray3DController.towedArray3DProcessParameters.detectorDataBlock);
		setParentDataBlock(detectorEventDataBlock);
		/*
		 * Then find the process that created this datablock. This should have implemented 
		 * the FFTDataSource interface which will enable us to get information we need
		 * on fft parameters. 
		 * AARON: For an event processor I see no need at present to access detection parameters, so will comment out for now.
		 */
		//detectorDataSource = (FFTDataSource) detectorEventDataBlock.getParentProcess();

		/*
		 * usedChannels will be a combination of what we want and what's availble.
		 */
		usedChannels = detectorEventDataBlock.getChannelMap() & towedArray3DController.towedArray3DProcessParameters.channelList;

		/*
		 * Tell the output data block which channels data may come from.
		 */
		localizationDataBlock.setChannelMap(usedChannels);

		/**
		 * allocate references to a list of detectors - one for each channel used. 
		 */
		//int istation=(int) Math.floor(PamUtils.getHighestChannel(usedChannels)/2);
		stationDetectors = new StationQueue[2];
		for (int i = 0; i < 2; i++) {
			//if (((1<<i) & usedChannels) > 0) {
			stationDetectors[i] = new StationQueue(i, this);
			stationDetectors[i].setMinICI(towedArray3DController.towedArray3DProcessParameters.minICINum); //Time in msec to remove
			stationDetectors[i].setQueue_maxtime(towedArray3DController.towedArray3DProcessParameters.maxTimeQueue); //Time in msec to remove
			stationDetectors[i].setTds_max(towedArray3DController.towedArray3DProcessParameters.maxTimeDelay);
			stationDetectors[i].setPercentICIErrorTOL(towedArray3DController.towedArray3DProcessParameters.percentErrorAngle/100.0);
			//stationDetectors[i].setPercentErrorEchoTime(towedArray3DController.towedArray3DProcessParameters.percentErrorEchoTime/100.0);

		}
		fine_tune_tdd=towedArray3DController.towedArray3DProcessParameters.fine_tune_tdd;
		fine_tune_tds=towedArray3DController.towedArray3DProcessParameters.fine_tune_tds;

		/*
		 * This should always go back to an Acquisition process by working
		 * back along the chain of data blocks and pamprocesses. 
		 */
		daqProcess = (AcquisitionProcess) getSourceProcess();

	}

}


