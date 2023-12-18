package videoRangePanel;

import java.util.List;
import java.awt.Point;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.ListIterator;

import javax.swing.JFrame;

import pamScrollSystem.AbstractPamScrollerAWT;
import pamScrollSystem.PamScroller;
import videoRangePanel.externalSensors.AngleListener;
import videoRangePanel.externalSensors.IMUListener;
import videoRangePanel.importTideData.TideManager;
import videoRangePanel.importTideData.TideSQLLogging;
import videoRangePanel.layoutAWT.VRParametersDialog;
import videoRangePanel.layoutAWT.VRTabPanelControl;
import videoRangePanel.layoutFX.VRDisplayFX2AWT;
import videoRangePanel.layoutFX.VRTabPanelControlFX;
import videoRangePanel.pamImage.ImageFileFilter;
import videoRangePanel.pamImage.ImageTimeParser;
import videoRangePanel.pamImage.PamImage;
import videoRangePanel.vrmethods.VRMethod;
import videoRangePanel.vrmethods.IMUMethod.IMUMethod;
import videoRangePanel.vrmethods.calibration.AddCalibrationMethod;
import videoRangePanel.vrmethods.horizonMethod.VRHorizonMethod;
import videoRangePanel.vrmethods.landMarkMethod.VRLandMarkMethod;
import videoRangePanel.vrmethods.shoreMethod.VRShoreMethod;
import Map.GebcoMapFile;
import Map.MapFileManager;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamModel.PamModel;
import PamModel.SMRUEnable;
import PamUtils.PamCalendar;
import PamView.importData.ImportDataSystem;
import dataPlotsFX.JamieDev;

/**
 * The videoRange module determines location information based on photographs of objects (photogrammetry). The ideal for determining the location of an animal is to know the heading, pitch, roll, focal length, GPS location and height of a camera along with the height of the tide.
 * <p>
 * However, not all these parameters are necessarily needed to gain some sort of location information. For example, tilt and pitch, focal length and height can determine the distance to animal without the need for heading information or a GPS location. 
 * <p>
 * There are various ways a user can determine required parameters, either directly from a photograph or external data. The simplest example is using the horizon to determine the pitch and tilt of the camera and manually inputting height data along with a calibration
 * (pixels per degree/rad) value. Users may want to use external IMU data, either in real time or for post processing. Geo-tagged photos are another option. 
 * <p>
 * This module is designed to provide a framework for photogrammetry. The core functionality allows users to open and view photographs/images along with any meta data. VRMethod classes are then used to provide the user interface for determining the l.ocation of an object
 * <p>
 * VRmethods use a JLayer over the photograph to allow users to select locations, draw lines etc. Each VRMethod has it's own side panel and ribbon panel to allow users to input information and select relevant. The VRMethod is responsible for calculating location information which is passed back to VRControl to be saved as a generic video range data unit. 
 *<p>
 *<p>
 *Generic Information Handled outside VRMethods (these are used by one or more VRMethods but handled solely within a VRMethod class)
 *<p>
 *GPS location of camera
 *<p>
 *Calibration Values
 *<p>
 *Landmark Locations
 *<p>
 *Tide Information
 *<p>
 *Imported Maps
 *<p>
 *External Sensor data
 *<p>
 *Photo meta data
 *
 * @author Doug Gillespie and Jamie Macaulay
 *
 */
public class VRControl extends PamControlledUnit implements PamSettings {

	/**
	 * Panel containing all gui components
	 */
	public VRTabPane vrTabPanelControl;

	/**
	 * Video range paramters. Stores settings. 
	 */
	protected VRParameters vrParameters = new VRParameters();

	/**
	 * Different methods for determining range from image measurements. 
	 */
	protected VRHorzMethods rangeMethods;

	/**
	 * PamProcess for the video range module
	 */
	protected VRProcess vrProcess;

	/**
	 * PamProcess to listen for heading angle measurements (AngleMeasurment)
	 */
	protected AngleListener angleListener;

	/**
	 * PamProcess to listen for IMU measurments (IMUModule)
	 */
	protected IMUListener imuListener;

	/**
	 * Manages map information for VR Methods
	 */
	protected MapFileManager mapFileManager;
	
	/**
	 * Lst of available time parsers.
	 */
	private ArrayList<ImageTimeParser> imageTimeParser= PamImage.getImageTimeParsers();

	/**
	 * Manages TIDE information for VR Methods
	 */
	protected TideManager tideManager;

	/*
	 *update flags 
	 */
	/**
	 * Some settings in the vrparams may have changed
	 */
	public static final int SETTINGS_CHANGE = 0;
	/**
	 * The image has changed
	 */
	public static final int IMAGE_CHANGE=1; 
	/**
	 * The scale of the image has been changed.
	 */
	public static final int IMAGE_SCALE_CHANGE=2; 
	/**
	 * The time of the image has been changed.
	 */
	public static final int IMAGE_TIME_CHANGE=3; 
	/**
	 * The heading of the image has been changed (usually manually).
	 */
	public static final int HEADING_UPDATE=4;
	/**
	 * The pitch of the image has been changed (usually manually)
	 */
	public static final int PITCH_UPDATE=5; 
	/**
	 * The roll of the image has been changed (usually manually), note: roll is often referred to as tilt. 
	 */
	public static final int TILT_UPDATE=6; 
	/**
	 * The vrMethod has changed
	 */
	public static final int METHOD_CHANGED=7;
	/**
	 * A repaint has been called.
	 */
	public static final int REPAINT=8;


	/*
	 * flags for general status of what we're trying to do
	 */
	public static final int NOIMAGE = -1;
	
	/**
	 * The mouse has moved on the display. 
	 */
	public static final int MOUSEMOVED = 9;
	
	/**
	 * The landmark group selection has been changed
	 */
	public static final int LANDMARKGROUP_CHANGE = 10;

	/**
	 * The height has been changed
	 */
	public static final int HEIGHT_CHANGE =11;


	public static final int DBCOMMENTLENGTH = 50;
	
	


	private int vrSubStatus = -1;

	/**
	 * An array of animal measurments. This holds all measurments for the current image. When a new image is selected this is set to null. 
	 */
	private ArrayList<VRMeasurement> measuredAnimals;

	/**
	 * List of methods used to determine location information from an image. 
	 */
	private ArrayList<VRMethod> vrMethods;

	/**
	 * The currently selected method. 
	 */
	private VRMethod currentVRMethod;

	/**
	 * The currently selected image. Null if no image is currently loaded. 
	 */
	private PamImage currentImage;

	/**
	 * Manager for determining the location of the camera, deals with GPS data, image geo tags etc. 
	 */
	private LocationManager locationManager;

	private PamScroller vrScroller;

	/**
	 * True to use the JavaFX display. Default to false.
	 */
	private boolean useFX = false;

	private ImportDataSystem<String> tideImport;

	/**
	 * The load time for external data either side of the image in millis
	 */
	private static long loadTime=60*60*1000;


	public VRControl(String unitName) {

		super("Video Range Measurement", unitName);

		locationManager=new LocationManager(this);

		PamSettingManager.getInstance().registerSettings(this);

		mapFileManager = new GebcoMapFile();

		vrMethods=createMethods();

		currentVRMethod=vrMethods.get(vrParameters.methodIndex);
		
		//System.out.println("The vr params: " + vrParameters.methodIndex);
		
		// for now, only set the FX display if we are using the SMRU or Jamie switches
		if (SMRUEnable.isEnable() || JamieDev.isEnabled()) {
			useFX = true;
		}

		if (useFX)
			vrTabPanelControl = new VRTabPanelControlFX(this);
		else{
			vrTabPanelControl = new VRTabPanelControl(this);
		}

		setTabPanel(vrTabPanelControl);

		//		setSidePanel(vrSidePanel = new VRSidePanel(this));

		rangeMethods = new VRHorzMethods(this);

		addPamProcess(vrProcess = new VRProcess(this));
		//TODO- need a better place to put this...

		///tide manager
		tideManager = new TideManager(this);
		tideManager.getTideDataBlock().SetLogging(new TideSQLLogging(tideManager.getTideDataBlock()));
		
		tideImport= new ImportDataSystem<String>(tideManager);
		tideImport.setName("Tide Data Import");
		
		vrProcess.addOutputDataBlock(tideManager.getTideDataBlock());

		//process for input of compass bearings
		addPamProcess(angleListener = new AngleListener(this));
		//process for input of IMU data
		addPamProcess(imuListener = new IMUListener(this));

		update(SETTINGS_CHANGE);
		
		setVRMethod(vrParameters.methodIndex);

		//load map file
		mapFileManager.readFileData(vrParameters.shoreFile, false);

		//load tide data
		//tideManager.importTideTxtFile(vrParameters.currTideFile);

		//create a scroller which we never display but helps loads viewer data. 
		vrScroller=new PamScroller("vrScroller", AbstractPamScrollerAWT.HORIZONTAL, 1000, 86400000, false);
		
		//this.vrTabPanelControl.openImageFile(vrParameters.currShoreFile);

	}

	
	/**
	 * This is where to define all the methods which can be used by the module.
	 * @return list of all the vr methods. 
	 */
	private ArrayList<VRMethod> createMethods(){
		ArrayList<VRMethod> vrMethods= new ArrayList<VRMethod>();
		vrMethods.add(new VRHorizonMethod(this));
		vrMethods.add(new VRShoreMethod(this));
		vrMethods.add(new AddCalibrationMethod(this));
		vrMethods.add(new VRLandMarkMethod(this));
		vrMethods.add(new IMUMethod(this));
		return vrMethods;	
	}

	/**
	 * Get an arraylist of all the vr methods currently available.
	 * @return
	 */
	public ArrayList<VRMethod> getMethods() {
		return vrMethods;
	}

	/**
	 * Set the current method. 
	 * @param method- integer specifying method position in vrMethods ArrayList. 
	 */
	public void setVRMethod(int method) {
		//System.out.println("The vr paramsf: " + vrParameters.methodIndex);
		this.currentVRMethod=vrMethods.get(method);
		this.vrParameters.methodIndex=method;
		update(METHOD_CHANGED); 
	}

	/**
	 * Get the current vr Method. 
	 * @return
	 */
	public VRMethod getCurrentMethod(){
		return currentVRMethod;
	}

	/**
	 * Get the calibration method. Some dialogs require this.
	 * @return the AddCalibration vrmethod
	 */
	public AddCalibrationMethod getCalibrationMethod() {
		if (getCurrentMethod() instanceof AddCalibrationMethod) return (AddCalibrationMethod) getCurrentMethod();
		else{
			for (int i=0; i<vrMethods.size(); i++ ){
				if (vrMethods.get(i) instanceof AddCalibrationMethod) return (AddCalibrationMethod) vrMethods.get(i);
			}
		}
		return null;
	}


	/**
	 * Opens the settings dialog and updates the module if new settings are saved. 
	 * @param frame
	 * @param tab- the tab to open the settings dialog on. 
	 */
	public void settingsButtonAWT(JFrame frame, int tab) {
		if (frame == null) {
			frame = getPamView().getGuiFrame();
		}
		VRParameters newParams = VRParametersDialog.showDialog(frame, this, tab);
		if (newParams != null) {
			vrParameters = newParams.clone();
			update(SETTINGS_CHANGE);
		}
	}
	
	/**
	 * Opens settings pane which updates module settings
	 * @param tab - which tab to open on. e.g. VRSettingsPane.HEIGHTTAB; 
	 */
	public void settingsButtonFX(int tab) {		
		((VRDisplayFX2AWT) vrTabPanelControl.getVRPane()).getPane().settingsButton(tab); 
	}

	
	/**
	 * 
	 * @param newParams
	 */
	public void setVRParams(VRParameters newParams) {
		vrParameters = newParams.clone();
	}

	/**
	 * Pastes an image from the computers clipboard. 
	 */
	public void pasteButton() {
		if (vrTabPanelControl.pasteImage()) {
			update(IMAGE_CHANGE);
		}
	}


	/**
	 * Find the next file which is approved by the Image file filter in the folder. Returns null if no file is found.  
	 * @param file- current file selected in the directory. If null the first file in the directory is chosen. 
	 * @param filefilter - the file filter. 
	 * @param forward- true move forward in the folder, false move backward in the folder. 
	 * @return the image file
	 */
	public File findNextFile(File file, ImageFileFilter filefilter, boolean forward){

		File directory=new File(file.getParent());
		if (!directory.isDirectory()) return null; //something seriously wrong

		File[] files=directory.listFiles();
		List<File> fileList = Arrays.asList(files);
		ListIterator<File> listIterator = fileList.listIterator();
		
		boolean fileFound=false; //find the file position; 
		for (int i=0; i<files.length; i++){
			if (files[i].getAbsolutePath().equals(file.getAbsolutePath())){
				fileFound=true;
				break;
			}
			listIterator.next();
		}
		
//		System.out.println("The file has been found ");

		if (!fileFound) return null;
		if (forward && !listIterator.hasNext()) {
			listIterator=fileList.listIterator();
			//reset the iterator
			if (!listIterator.hasNext()) return null;
		}
		if (!forward && !listIterator.hasPrevious()) {
			listIterator=fileList.listIterator(fileList.size());
			if (!listIterator.hasPrevious()) return null;
		}
		
		
//		System.out.println("Iterating to next file: ");
		//now we have index of file and list iterator in correct position; 
		File listFile=null; 
		File newFile=null; 
		boolean iterate=true;

		while (iterate==true){
			if (forward) {
				listFile=listIterator.next();
				iterate=listIterator.hasNext();
			}
			else {
				listFile=listIterator.previous();
				iterate=listIterator.hasPrevious();
			}

//			System.out.println("List file: " + listFile.getName());

			if (filefilter.accept(listFile) && !listFile.getAbsolutePath().equals(file.getAbsolutePath()) && !listFile.isDirectory()){
//				System.out.println("file accepted: "+listFile.getAbsoluteFile() +"  "+ file.getAbsolutePath() );
				newFile=listFile;
				iterate=false;
			}

		}
		return newFile;
	}		

	/**
	 * Force the video range tab to be selected.
	 */
	void showVRTab() {
		PamController.getInstance().showControlledUnit(this);
	}

	/**
	 * Set the current selected height
	 * @param heightIndex
	 */
	public void selectHeight(int heightIndex) {
		vrParameters.setCurrentHeightIndex(heightIndex);
		update(SETTINGS_CHANGE);
	}

	/**
	 * Get the  current height of the camera from sea level. Includes any offset calulated by the tide manager. 
	 * @return the current height oif the camera from sea level- (this is fiorm the current sea level)
	 */
	public double getCurrentHeight(){
		//System.out.println("The time of the current image is: " + PamCalendar.formatDateTime(currentImage.getTimeMilliseconds()));
		if (currentImage==null) return vrParameters.getCameraHeight();
		if (currentImage.getTimeMilliseconds()==0) return vrParameters.getCameraHeight();
		return vrParameters.getCameraHeight()+tideManager.getHeightOffset(currentImage.getTimeMilliseconds());
	}

	//	/**
	//	 * Set the image brightness
	//	 * @param brightness
	//	 * @param contrast
	//	 */
	//	public void setImageBrightness(float brightness, float contrast) {
	//		vrTabPanelControl.getVRPane().setImageBrightness(brightness, contrast);
	//	}

	/**
	 * Load an image file
	 * @param file- image file. 
	 */
	public void loadFile(File file) {
		long time1=System.currentTimeMillis();
		if (vrTabPanelControl.openImageFile(file)) {
			//now set the scroller to move to to the correct imu or angle data;
			if (currentImage.getTimeMilliseconds()!=0 && !checkViewLoadTime(currentImage.getTimeMilliseconds())){
				vrScroller.setRangeMillis(currentImage.getTimeMilliseconds()-loadTime, currentImage.getTimeMilliseconds()+loadTime, true);
			}
		}
		long time2=System.currentTimeMillis();
//		System.out.println("vrControl.loadFile: time to loadfile: "+(time2-time1));
	}
	
//	/**
//	 * Set the scroller to a new position if an image is loaded which is not within the the current scroll range.
//	 * @param currentImage
//	 */
//	private void checkScrollPos(PamImage currentImage) {
//		if (currentImage!=null && this.isViewer){
//			long millis= currentImage.getTimeMilliseconds();
//			//check if millis is in scroll bar range. If not moive scroll bar. 
//			if (this.vrScroller.getMinimumMillis()<millis || millis>this.vrScroller.getMaximumMillis()){
//				// set the scrollert to a new position. 
//				vrScroller.setValueMillis(millis-vrScroller.getRangeMillis()/2);
//			}
//		}
//	}


	/**
	 * Check that the IMU or angle data is within the load limits. 
	 */
	private boolean checkViewLoadTime(long timeMillisPic){
		if (timeMillisPic==0) return true;  
		if (imuListener.getIMUDataBlock()!=null){
			if (timeMillisPic>imuListener.getIMUDataBlock().getCurrentViewDataStart() && timeMillisPic<imuListener.getIMUDataBlock().getCurrentViewDataEnd() ){
				return true; 
			}
		}
		if (angleListener.getAngleDataBlock()!=null){
			if (timeMillisPic>angleListener.getAngleDataBlock().getCurrentViewDataStart() && timeMillisPic<angleListener.getAngleDataBlock().getCurrentViewDataEnd() ){
				return true; 
			}
		}
		return false;
	}

	public Serializable getSettingsReference() {
		return vrParameters;
	}

	public long getSettingsVersion() {
		return VRParameters.serialVersionUID;
	}

	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {	
		vrParameters = ((VRParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	/**
	 * Updates the VRControl depending on flag and passes update flag to the VRTabPanel (gui) and the VRMethods. 
	 * @param updateType
	 */
	public void update(int updateType) {

		switch (updateType){
		case SETTINGS_CHANGE:
			rangeMethods.setCurrentMethodId(vrParameters.rangeMethod);
			sortExtAngleSource();
			if (currentImage!=null)	this.currentImage.recalcTime(getImageTimeParser()); 
			//angleListener.sortAngleMeasurement();
			break;
		case METHOD_CHANGED:
			measuredAnimals=null;
			break;
		case IMAGE_CHANGE:
			//currentVRMethod.clearOverlay(); 
			if (currentImage!=null && currentImage.getImage() != null) {
				showVRTab();
				if (!currentImage.imageOK()) currentImage=null;
			}
			break;
		}
		

		//update other panels. 
		vrTabPanelControl.update(updateType);

		//update methods
		for (int i=0; i<vrMethods.size(); i++){
			vrMethods.get(i).update(updateType);
		}
	}


	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamControllerInterface.ADD_CONTROLLEDUNIT || changeType == PamControllerInterface.REMOVE_CONTROLLEDUNIT) {
			angleListener.sortAngleMeasurement();
			imuListener.sortIMUMeasurement();
		}
		if (changeType == PamControllerInterface.HYDROPHONE_ARRAY_CHANGED) {
			update(SETTINGS_CHANGE);
		}
		if (changeType == PamControllerInterface.DATA_LOAD_COMPLETE) {
			update(SETTINGS_CHANGE);
		}
		if (changeType == PamControllerInterface.INITIALIZATION_COMPLETE ){
			vrScroller.addDataBlock(imuListener.getIMUDataBlock());
			vrScroller.addDataBlock(angleListener.getAngleDataBlock());
			//need to make sure tide data is loaded up
			vrScroller.addDataBlock(this.getTideManager().getTideDataBlock());

		}
	}

	/**
	 * Sorts out which listener to currently use. Depends on the current datablock stored in vrParams. 
	 */
	private void sortExtAngleSource(){
		angleListener.sortAngleMeasurement();
		imuListener.sortIMUMeasurement();
	}

	public int getVrSubStatus() {
		return vrSubStatus;
	}

	public void newMousePoint(Point mousePoint) {
		// called from mouse move.
		vrTabPanelControl.newMousePoint(mousePoint);
	}

	/**
	 * 
	 * @return
	 */
	public String getImageName() {
		return currentImage.getName();
	}

	/**
	 * Get the time of the current image. 
	 * @return the image date and time string. 
	 */
	public String getImageTimeString() {
		if (getImageTime()==0 || getImageTime()==-1) return "No image time stamp"; 
		else return PamCalendar.formatDateTime2(getImageTime(),"yyyy-MM-dd HH:mm:ss.SSS", false);
	}

	/**
	 * Get a list of the current measurements on the image
	 * @return the current measurements on the image 
	 */
	public ArrayList<VRMeasurement> getMeasuredAnimals() {
		return measuredAnimals;
	}

	/**
	 * Get the VR paramters class. 
	 * @return the vr params. 
	 */
	public VRParameters getVRParams() {
		return vrParameters;
	}

	/**
	 * Get the vr panel. 
	 * @return
	 */
	public VRPane getVRPanel() {
		return vrTabPanelControl.getVRPane();
	}

	/**
	 * Get the VRProcess. 
	 * @return the vr process. 
	 */
	public VRProcess getVRProcess() {
		return vrProcess;
	}

	public void setMeasuredAnimals(ArrayList<VRMeasurement> measuredAnimals) {
		this.measuredAnimals=measuredAnimals;
	}

	public VRHorzMethods getRangeMethods() {
		return rangeMethods;
	}


	public void setRangeMethods(VRHorzMethods rangeMethods) {
		this.rangeMethods = rangeMethods;
	}


	/**
	 * Get the file manager for shore based method maps. 
	 * @return the map manager
	 */
	public MapFileManager getMapFileManager() {
		return mapFileManager;
	}

	/**
	 * Get the location manager. This handles the location of the images.
	 * @return the location manager
	 */
	public LocationManager getLocationManager(){
		return locationManager;
	}

	/**
	 * Get the tide manager. This handles tide imports. 
	 * @return tide imports. 
	 */
	public TideManager getTideManager() {
		return tideManager;
	}

	/**
	 * Set the current image. 
	 * @param image - the current displayed image. 
	 */
	public void setCurrentImage(PamImage image) {
		this.currentImage=image; 
		if (currentImage!=null){
			vrParameters.currentImageFile=currentImage.getImageFile();
		}
		else {
			vrParameters.currentImageFile=null;
		}
	}

	/**
	 * Get the current pam image.
	 * @return the current image. 
	 */
	public PamImage getCurrentImage() {
		return currentImage;
	}

	
	public VRTabPane getVRTabPanel(){
		return vrTabPanelControl;
	}

	/**
	 * Get the current image time in millis. 
	 * @return the current image time. 
	 */
	public long getImageTime() {
		if (currentImage==null) return -1; 
		if (vrParameters.useTimeOffset) return vrParameters.timeOffset.getOffsetTime(currentImage.getTimeMilliseconds());
		else return (currentImage.getTimeMilliseconds());
	}

	/**
	 * Get the date of the image. 
	 * @return the date of the image 
	 */
	public Date getImageDate() {
		if (currentImage==null) return null; 
		return new Date(getImageTime());
	}

	
	public  IMUListener getIMUListener() {
		return imuListener;
	}

	/**
	 * Check whether the GUI is FX or not. 
	 * @return
	 */
	public boolean isFX() {
		return useFX;
	}
	
	/**
	 * Get the import system for tide data. 
	 * @return the tideImport
	 */
	public ImportDataSystem<String> getTideImport() {
		return tideImport;
	}

	/**
	 * Get the current image time aprser
	 * @return
	 */
	public ImageTimeParser getImageTimeParser() {
		return imageTimeParser.get(this.vrParameters.imageTimeParser);
	}





}
