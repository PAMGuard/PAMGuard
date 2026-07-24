package NMEA.simulated;

import java.awt.Window;
import java.util.Date;
import java.util.Random;

import GPS.GpsData;
import NMEA.AISDataSet;
import NMEA.ChannelAISData;
import NMEA.NMEAControl;
import NMEA.NMEAParameters;
import NMEA.NMEAProvider;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.dialog.PamDialogPanel;
import geoMag.MagneticVariation;

public class SimulatedNMEAProvider extends NMEAProvider {
	
	public static final String name = "Simulated NMEA data";
	
	private volatile boolean keepGoing;
	
	private Thread runningThread;

	private NMEAControl nmeaControl;

	public SimulatedNMEAProvider(NMEAControl nmeaControl) {
		super(nmeaControl);
		this.nmeaControl = nmeaControl;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public PamDialogPanel getDialogPanel(Window frame) {
		return new SimulatedNMEAPanel(this);
	}

	@Override
	public boolean startAcquisition() {
		keepGoing = true;
		runningThread = new Thread(new Runnable() {
			@Override
			public void run() {
				simulateData();
			}
		});
		runningThread.start();
		return true;
	}

	protected void simulateData() {
		//System.out.println("********GPS SIM THREAD*********");
		GpsData gpsSim = new GpsData();
		int timeOnCurrentHeading;
		Date date = new Date();
		
		AISDataSet aisData = new ChannelAISData();
		
		MagneticVariation magneticVariation = MagneticVariation.getInstance();
		double magVar;

//		SimpleDateFormat gpsDateFormat = new SimpleDateFormat("ddMMyy");
//		SimpleDateFormat gpsTimeFormat = new SimpleDateFormat("hhmmss");
		// NumberFormat.
		NMEAParameters nmeaParameters = getNmeaControl().getNmeaParameters();
		nmeaParameters.simTimeInterval = 
			Math.max(nmeaParameters.simTimeInterval, .5);
		int simTimeInterval = (int) (nmeaParameters.simTimeInterval * 1000);
		boolean drunkCaptain = (nmeaParameters.drunkenness != 0);
		boolean continousChange = nmeaParameters.continousChange ;
		double courseChangeRate = 0;
		double courseRateRandom = nmeaParameters.drunkenness;
		double courseRateDecay = nmeaParameters.drunkenness / 3;
		double newCCR;
		Random random = new Random();
		double currentCourse;


		gpsSim.setLatitude(nmeaParameters.simStartLatitude);
		gpsSim.setLongitude(nmeaParameters.simStartLongitude);
		gpsSim.setSpeed(nmeaParameters.simStartSpeed);
		gpsSim.setCourseOverGround(nmeaParameters.simStartHeading);
		//gpsSim.setSpeed(0.0);
//		gpsSim.setTrueCourse(170.0);
//		gpsSim.setVariation(0.0);

		currentCourse = gpsSim.getCourseOverGround();
		double angle = Math.PI / 180 * (90 - currentCourse);
		double latStep = gpsSim.getSpeed() / 3600 * Math.sin(angle) / 60.;
		double longStep = gpsSim.getSpeed() / 3600 * Math.cos(angle)
		/ Math.cos(Math.abs(gpsSim.getLatitude()) * Math.PI / 180.)
		/ 60.;

		//System.out.println("gpsSim.getLongitude(): "  + gpsSim.getLongitude());

		long lastSimTime = PamCalendar.getTimeInMillis();
		long nowTime = 0;
		double stepTime;
		while (keepGoing) {
			nowTime = PamCalendar.getTimeInMillis();
			stepTime = (nowTime - lastSimTime) / 1000.;
			if (stepTime < 0) {
				/* 
				 * this will happen if a wav file is started from 
				 * some time ago - and will cause a very big backwards step
				 * so set it to zero and should be OK or at least better. 
				 */
				stepTime = 0;
			}
//			if (Math.abs(stepTime) > 10) stepTime = 1;
			lastSimTime = nowTime;

			angle = Math.PI / 180 * (90 - currentCourse);
			latStep = gpsSim.getSpeed() / 3600 * Math.sin(angle) / 60.;
			longStep = gpsSim.getSpeed() / 3600 * Math.cos(angle)
			/ Math.cos(Math.abs(gpsSim.getLatitude()) * Math.PI / 180.)
			/ 60.;
			gpsSim.setCourseOverGround(currentCourse);
			gpsSim.setLatitude(gpsSim.getLatitude() + latStep * stepTime);
			gpsSim.setLongitude(gpsSim.getLongitude() + longStep * stepTime);

			gpsSim.setTimeInMillis(PamCalendar.getTimeInMillis());

			StringBuffer sb = new StringBuffer(gpsSim.gpsDataToRMC(nmeaParameters.getLatLongDecimalPlaces()));
			getNMEAProcess().addNewString(sb);
			sb = new StringBuffer(gpsSim.gpsDataToGGA(nmeaParameters.getLatLongDecimalPlaces()));
			getNMEAProcess().addNewString(sb);
			
			if (nmeaParameters.simHeadingData == NMEAParameters.SIM_HEADING_MAGNETIC) {
				magVar = magneticVariation.getVariation(gpsSim);
				getNMEAProcess().addNewString(createMagneticNMEAString(gpsSim.getCourseOverGround(), magVar));
			}
			else if (nmeaParameters.simHeadingData == NMEAParameters.SIM_HEADING_TRUE) {
				getNMEAProcess().addNewString(createTrueNMEAString(gpsSim.getCourseOverGround()));
			}

			if (drunkCaptain) {
				//either a random change or a continous change.
				if (!continousChange){
					//random change in direction with a specified amgnitude. 
					newCCR = courseChangeRate + random.nextGaussian() * courseRateRandom * stepTime;
					newCCR *= Math.exp(-courseRateDecay * stepTime);
					courseChangeRate = newCCR;
				}
				else {
					//continous change in heading. 
					courseChangeRate=courseRateRandom;
				}
				
				currentCourse += courseChangeRate * stepTime;
				
				//wrap to 360.
				while (currentCourse >= 360) {
					currentCourse -= 360;
				}
				while (currentCourse < 0) {
					currentCourse += 360;
				}
				
				
			}
			
			if (nmeaParameters.generateAIS) {
				getNMEAProcess().addNewString(new StringBuffer(aisData.getNext()));
			}
			
			try {
				Thread.sleep(simTimeInterval);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	private StringBuffer createTrueNMEAString(double courseOverGround) {
		courseOverGround = PamUtils.constrainedAngle(courseOverGround);
		StringBuffer s = new StringBuffer(String.format("$GPHDT,%03.1f,T,", courseOverGround));
		int checkSum = createStringChecksum(s);
		s.append(String.format("*%02X", checkSum));
		return s;
	}

	private StringBuffer createMagneticNMEAString(double courseOverGround,
			double magVar) {
		courseOverGround = PamUtils.constrainedAngle(courseOverGround - magVar);
		StringBuffer s = new StringBuffer(String.format("$GPHDG,%03.1f,,,,", courseOverGround));
		int checkSum = createStringChecksum(s);
		s.append(String.format("*%02X", checkSum));
		return s;
	}

	@Override
	public void stopAcquisition() {
		keepGoing = false;
		if (runningThread != null) {
			try {
				runningThread.join(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
		}
		runningThread = null;
	}

}
