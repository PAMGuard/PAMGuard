package Array;

import java.awt.Frame;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JFrame;

import pamMaths.PamQuaternion;
import pamMaths.PamVector;
import userDisplay.UserDisplayControl;
import Array.importHydrophoneData.HydrophoneImport;
import Array.importHydrophoneData.StreamerImport;
import Array.layoutFX.ArrayGUIFX;
import Array.plot.ArrayPlotProviderFX;
import Array.sensors.swing.ArraySensorPanelProvider;
import GPS.GPSControl;
import GPS.GPSDataBlock;
import GPS.GpsData;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitGUI;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.positionreference.PositionReference;
import PamModel.PamModuleInfo;
import PamUtils.PamUtils;
import PamView.PamControlledGUISwing;
import PamView.WrapperControlledGUISwing;
import PamView.importData.ImportDataSystem;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import dataPlotsFX.data.TDDataProviderRegisterFX;

/**
 * Manager for different array configurations. Each array configuration is 
 * stored in it's own serialised file, which is separate from other Pam Settings
 * so that they can be stored, emailed around, etc. In it' main serialised file
 * controlled by PamSettingsManager, Pamguard keeps a list of recently used arrays.
 * <p>
 * The dialog ArrayDialog allows the user to load arrays from file, create new arrays,
 * edit arrays, etc.
 * 
 * @author Doug Gillespie
 * @see Array.PamArray
 *
 */
public class ArrayManager extends PamControlledUnit implements PamSettings, PamObserver, PositionReference {

	/**
	 * No array specified
	 */
	public static final int ARRAY_TYPE_NONE = 0;
	/**
	 * Point array (single phone or multiple phones at same point)
	 */
	public static final int ARRAY_TYPE_POINT = 1;
	/**
	 * Line array of two or more elements
	 */
	public static final int ARRAY_TYPE_LINE = 2;
	/**
	 * Three or more Hydrophones all in the same plane
	 */
	public static final int ARRAY_TYPE_PLANE = 3;
	/**
	 * Four or more hydrophones not in the same plane. 
	 */
	public static final int ARRAY_TYPE_VOLUME = 4;

	private static  ArrayManager singleInstance = null;

	private  HydrophoneProcess hydrophonesProcess;

	private static final String arrayFileType = "paf";

	ArrayList<PamArray> recentArrays;

//	private DepthControl depthControl;

	private ImportDataSystem<Hydrophone> hydrophoneImportManager;

	private ImportDataSystem<ArrayList<Double>> streamerImportManager;
	
	/**
	 * The FGX GUI for the array manager
	 */
	private ArrayGUIFX arrayGUIFX;
	
	/**
	 * The array manager. 
	 */
	public static String arrayManagerType = "Array Manager"; 
	
	/**
	 * The Swing GUI for the array manager,. 
	 */
	private PamControlledGUISwing arrayGUISwing;

	public static final double DEFAULT_HYDROPHONE_SENSITIVITY = -170;
	public static final double DEFAULT_PREAMP_GAIN = 0;


	private ArrayManager(String unitName) {
		super(arrayManagerType, "Array Manager");

		/* 
		 * set this immediately to stop it calling back into itself when the process is added
		 * causing the getArrayManager() to be called over and over. 
		 */
		singleInstance = this;


		/*
		 * Move these lines here from the getArrayManager() static function which was a daft place to have them
		 * and also make sure they are created before the arrays are loaded from memory.
		 */
		hydrophonesProcess=new HydrophoneProcess(this);
		addPamProcess(hydrophonesProcess);

		PamSettingManager.getInstance().registerSettings(this);

		//		// if it hasn't created an array by now, do so !
		if (recentArrays == null || recentArrays.size() == 0) {
			createDefaultArray();
		}

		//enable importing of time stamped hydrophone and streamer data if in viewer mode. 
		if (isViewer){
			hydrophoneImportManager= new ImportDataSystem<Hydrophone>(new HydrophoneImport(hydrophonesProcess.getHydrophoneDataBlock()));
			hydrophoneImportManager.setName("Hydrophone Data Import");
			streamerImportManager = new ImportDataSystem<ArrayList<Double>>(new StreamerImport(hydrophonesProcess.getStreamerDataBlock()));
			streamerImportManager.setName("Streamer Data Import");
		}
		// need to do a bit more to initialise the current array !
		setCurrentArray(recentArrays.get(0));
		
		UserDisplayControl.addUserDisplayProvider(new ArraySensorPanelProvider());

		TDDataProviderRegisterFX.getInstance().registerDataInfo(new ArrayPlotProviderFX(getStreamerDatabBlock()));
	}

	private void createDefaultArray() {
		addArray(PamArray.createSimpleArray("Basic Linear Array", 0, -5, 3, 2, DEFAULT_HYDROPHONE_SENSITIVITY, DEFAULT_PREAMP_GAIN, new double[] {0, 20000}));
	}


	@Override
	public PamObserver getObserverObject() {
		return this;
	}

	public static ArrayManager getArrayManager() {
		if (singleInstance == null) {
			singleInstance = new ArrayManager(null);
			singleInstance.addModuleInfo(); //needed for FX
			PamController.getInstance().addControlledUnit(singleInstance);			
		}
		return singleInstance;
	}

	private boolean initComplete = false;
	public void notifyModelChanged(int changeType) {
		if (changeType == PamController.INITIALIZATION_COMPLETE) {
			initComplete = true;
		}
		getCurrentArray().notifyModelChanged(changeType, initComplete);

		if (changeType == PamController.INITIALIZATION_COMPLETE) {
			// create data units and save - is this needed in the viewer ? 
			//			if (PamController.getInstance().getRunMode() == PamController.RUN_NORMAL) {
			hydrophonesProcess.createArrayData();
			//			}
		}

		if (changeType == PamController.OFFLINE_DATA_LOADED){
			if (isViewer) {
				getHydrophoneDataBlock().clearChannelIterators();
			}
		}

		if (changeType == PamController.HYDROPHONE_ARRAY_CHANGED){
			if (isViewer) {
				getHydrophoneDataBlock().clearChannelIterators();
			}
		}
		
		if (changeType == PamController.GLOBAL_MEDIUM_UPDATE){
			this.getCurrentArray().setSpeedOfSound(this.getPamController().getGlobalMediumManager().getDefaultSoundSpeed());
			this.getCurrentArray().setDefaultSensitivity(this.getPamController().getGlobalMediumManager().getDefaultRecieverSens()); 
		}

	}

	/**
	 * @return Returns the arrayFileType.
	 */
	public static String getArrayFileType() {
		return arrayFileType;
	}

	/**
	 * @return Returns the recentArrays.
	 */
	public ArrayList<PamArray> getRecentArrays() {
		return recentArrays;
	}

	public int getArrayCount() {
		if (recentArrays == null || recentArrays.size() == 0) return 0;
		return recentArrays.size();
	}

	public void showArrayDialog(Frame parentFrame) {
		PamArray selectedArray = ArrayDialog.showDialog(parentFrame, singleInstance);

		//WARNING: need to make sure that the notify model changed is at the end of this function. It must be called after all hydrophone data loading 
		//and saving has occured, otherwise we don't clear channel iterators from the hydrophone dateblock and end up with concurrent modification exceptions in 
		//localiser modules. 
		if (selectedArray != null) {
			hydrophonesProcess.createArrayData();
			// need to tell all modules that the array may have changed. 
			PamController.getInstance().notifyModelChanged(PamController.HYDROPHONE_ARRAY_CHANGED);
		}

	}

	public void addArray(PamArray newArray) {
		if (recentArrays == null) {
			recentArrays = new ArrayList<PamArray>();
		}
		recentArrays.add(newArray);
	}

	public void setCurrentArray(PamArray array) {
		if (array == null) return;
		recentArrays.remove(array);
		array.setArrayShape(this.getArrayShape(array));
		array.getHydrophoneLocator();
		recentArrays.add(0, array);
	}

	public boolean removeArray(PamArray newArray) {
		if (recentArrays == null) return false;
		return recentArrays.remove(newArray);
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#GetSettingsReference()
	 */
	public Serializable getSettingsReference() {
		/*
		 * Save the entire array classes in the serialised file
		 * but there will still be facilities for storing individual
		 * arrays in their own files for backup and export
		 */
		if (recentArrays == null) {
			return null;
		}
		for (PamArray array:recentArrays) {
			array.prepareToSerialize();
		}
		return new ArrayParameters(recentArrays);
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#GetSettingsVersion()
	 */
	public long getSettingsVersion() {
		return ArrayParameters.serialVersionUID;
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#GetUnitName()
	 */
	public String getUnitName() {
		return "Array Manager";
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#getUnitType()
	 */
	public String getUnitType() {
		return "Array Manager";
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#RestoreSettings(PamController.PamControlledUnitSettings)
	 */
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		try {
			if (pamControlledUnitSettings.getSettings() != null) {
				ArrayList<PamArray> oldArrays;
				Object settings = pamControlledUnitSettings.getSettings();
				if (settings instanceof ArrayParameters) {
					oldArrays = ((ArrayParameters) settings).getArrayList();
				}
				else {
					oldArrays =  (ArrayList<PamArray>) pamControlledUnitSettings.getSettings();
				}
				/**
				 * Need to make a hard copy so that the cloning initiates the depth inversion fix !
				 */
				recentArrays = new ArrayList<PamArray>();
				for (int i = 0; i < oldArrays.size(); i++) {
					recentArrays.add(oldArrays.get(i).clone());
				}

				for (PamArray array:recentArrays) {
					array.arrayDeserialized();
				}
			}
		}
		catch (Exception Ex) {
			Ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @return Returns the currentArray.
	 */
	public  PamArray getCurrentArray() {
		if (recentArrays == null || recentArrays.size() == 0) {
			createDefaultArray();
			if (recentArrays == null || recentArrays.size() == 0) {
				return null;
			}
		}
		return recentArrays.get(0);
	}

	static public PamArray loadArrayFromFile(String fileName) {
		try {
			ObjectInputStream file =  new ObjectInputStream(new FileInputStream(fileName));
			PamArray array = (PamArray) file.readObject();
			return array;
		} catch (Exception Ex) {
			Ex.printStackTrace();
			return null;
		}
	}

	static public boolean saveArrayToFile(PamArray array) {

		// should put some stuff in so that if arrayFile is null, it asks for a name
		if (array.getArrayFile() == null) return false;
		try {
			ObjectOutputStream file = new ObjectOutputStream(new FileOutputStream(array.getArrayFile()));
			file.writeObject(array);
		} catch (Exception Ex) {
			System.out.println(Ex);
			return false;
		}
		return true;
	}

//	/**
//	 * @return the depthControl
//	 */
//	public DepthControl getDepthControl() {
//		return depthControl;
//	}
//
//	/**
//	 * @param depthControl the depthControl to set
//	 */
//	public void setDepthControl(DepthControl depthControl) {
//		if (this.depthControl != null && this.depthControl != depthControl) {
//			this.depthControl.getDepthDataBlock().deleteObserver(this);
//		}
//		this.depthControl = depthControl;
//		if (depthControl != null) {
//			depthControl.getDepthDataBlock().addObserver(this);
//		}
//	}

	public String getObserverName() {
		return "Array Manager";
	}

	public long getRequiredDataHistory(PamObservable o, Object arg) {
		// would be better to work out how long the Gps data are being kept for and do the same
		return 3600*1000;
	}

	public void noteNewSettings() {

	}

	public void removeObservable(PamObservable o) {

	}

	public void setSampleRate(float sampleRate, boolean notify) {
		// TODO Auto-generated method stub

	}

	@Override
	public void masterClockUpdate(long milliSeconds, long sampleNumber) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addData(PamObservable o, PamDataUnit arg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateData(PamObservable o, PamDataUnit arg) {

	}

	/**
	 * @return the type of the current array (point, planar, line, etc) 
	 */
	public int getArrayType() {
		return getArrayShape(getCurrentArray());
	}

	/**
	 * 
	 * @param phones
	 * @return the type of a sub array of the current array (point, planar, line, etc) 
	 */
	public int getArrayType(int phones) {
		return getArrayShape(getCurrentArray(), phones);
	}

	/**
	 * 
	 * @param array a PAMGUARD array
	 * @return the type of an array (point, plane, etc)
	 */
	public int getArrayShape(PamArray array) {
		if (array == null) {
			return ARRAY_TYPE_NONE;
		}
		int nPhones = array.getHydrophoneCount();
		int phones = PamUtils.makeChannelMap(nPhones);
		return getArrayShape(array, phones);
	}

	/**
	 * 
	 * @param array a PAMGUARD array
	 * @param phones a sub array of phones in the pam array. 
	 * @return the type of an array (point, plane, etc)
	 */
	public int getArrayShape(PamArray array, int phones) {
		if (array == null || phones == 0) {
			return ARRAY_TYPE_NONE;
		}
		phones = getSpatiallyUniquePhones(array, phones);
		int nPhones = PamUtils.getNumChannels(phones);
		if (nPhones <= 1) {
			return ARRAY_TYPE_POINT;
		}
		/**
		 * Need an array of phones, but need to exclude any which 
		 * are at the exact same place as existing ones. So for
		 * each phone, look back through the list and see if 
		 * its really unique.  
		 */
		PamVector phoneVec;
		PamVector[] phoneVectors = new PamVector[nPhones];
		int iPhone, jPhone, iStreamer, jStreamer;
		int uniquePhones = 0;
		boolean matches;
		for (int i = 0; i < nPhones; i++) {
			iPhone = PamUtils.getNthChannel(i, phones);
			iStreamer = array.getStreamerForPhone(iPhone);
			phoneVec = array.getAbsHydrophoneVector(iPhone,0);
			matches = false;
			for (int j = 0; j < uniquePhones; j++) {
				jPhone = PamUtils.getNthChannel(j, phones);
				jStreamer = array.getStreamerForPhone(jPhone);
				if (phoneVec.equals(phoneVectors[j]) & jStreamer == iStreamer) {
					matches = true;
					break;
				}
			}
			if (matches) {
				continue;
			}
			phoneVectors[uniquePhones++] = phoneVec;
		}
		nPhones = uniquePhones;
		phoneVectors = Arrays.copyOf(phoneVectors, nPhones);

		int nPairs = nPhones * (nPhones-1) / 2;
		int zeroPoints = 0;
		int iPair = 0;
		PamVector[] pairVectors = new PamVector[nPairs];
		for (int i = 0; i < nPhones; i++) {
			for (int j = i+1; j < nPhones; j++) {
				pairVectors[iPair++] = phoneVectors[j].sub(phoneVectors[i]);
			}
		}
		// now test for the various types. 
		if (nPhones == 2) {
			return ARRAY_TYPE_LINE;
		}
		// will also be a line if all interpair vectors
		// are in a line. 
		if (areInLine(pairVectors)) {
			return ARRAY_TYPE_LINE;
		}
		/*
		 * May be a plane !
		 */
		if (nPhones == 3) {
			return ARRAY_TYPE_PLANE;
		}
		if (areOnPlane(pairVectors)) {
			return ARRAY_TYPE_PLANE;
		}
		return ARRAY_TYPE_VOLUME;
	}

	/**
	 * Take a list of phones and return a list of ones
	 * which are at unique positions. 
	 * @param array hydrophone array
	 * @param phones bitmap of hydrophone numbers 
	 * @return bitmap of hydrophones which have a unique position. 
	 */
	public int getSpatiallyUniquePhones(PamArray array, int phones) {
		int nPhones = PamUtils.getNumChannels(phones);
		int iPhone, jPhone, iStream, jStream;
		int uniqueList = 0;
		PamVector iVec, jVec;
		boolean unique;
		for (int i = 0; i < nPhones; i++) {
			unique = true;
			iPhone = PamUtils.getNthChannel(i, phones);
			iStream = array.getStreamerForPhone(iPhone);
			iVec = array.getAbsHydrophoneVector(iPhone,-1);
			if (iVec == null) continue;
			for (int j = i+1; j < nPhones; j++) {
				jPhone = PamUtils.getNthChannel(j, phones);
				jStream = array.getStreamerForPhone(jPhone);
				jVec = array.getAbsHydrophoneVector(jPhone,-1);
				if (jVec == null || jStream != iStream) continue;
				if (iVec.equals(jVec)) {
					unique = false;
				}
			}
			if (unique) {
				uniqueList |= (1<<iPhone);
			}
		}
		return uniqueList;
	}

	/**
	 * Get the principle direction vectors for the current array
	 * @return Array of vectors pointing along the array principle axes. 
	 */
	public PamVector[] getArrayDirections() {
		PamArray array = getCurrentArray();
		return getArrayDirections(array);
	}

	/**
	 * Get the principle direction vectors for a sub set of hydrophones
	 * within the current array
	 * @param phones bitmap of hydrophones
	 * @return  Array of vectors pointing along the array principle axes. 
	 */
	public PamVector[] getArrayDirection(int phones) {
		PamArray array = getCurrentArray();
		return getArrayDirections(array, phones);
	}

	/**
	 * Get a set of vectors which define the principle coordinates of an array
	 * @param array array
	 * @return array of vectors 
	 * 
	 */
	public PamVector[] getArrayDirections(PamArray array) {
		int nPhones = array.getHydrophoneCount();
		int phones = PamUtils.makeChannelMap(nPhones);
		return getArrayDirections(array, phones);
	}
	/**
	 * Get a set of vectors which define the principle components of an array. 
	 * <p>For a point array, null is returned
	 * <p>For a line array a vector pointing along the array in the direction closest
	 * to the y axis
	 * <p>For a planar array a vector pointing as closely as possible to the y axis
	 * and a second vector, perpendicular to the first, obeying a right hand rule, in 
	 * the plane will be returned. 
	 * <p>For a volumetric array, the above + the third vector (vec product of the other two). 
	 *    
	 *  
	 * @param array Pamguard array
	 * @param phones phones included in a sub array. 
	 * @return up to three vectors defining the components of the array. 
	 */
	public PamVector[] getArrayDirections(PamArray array, int phones) {
		phones = getSpatiallyUniquePhones(array, phones);
		int nPhones = PamUtils.getNumChannels(phones);
		if (nPhones <= 0) {
			return null;
		}
		PamVector[] arrayVectors = getArrayVectors(array, phones);
		int arrayType = getArrayShape(array, phones);
		switch(arrayType) {
		case ARRAY_TYPE_POINT:
			return null;
		case ARRAY_TYPE_LINE:
			return getLineArrayVector(arrayVectors);
		case ARRAY_TYPE_PLANE:
			return getPlaneArrayVectors(arrayVectors);
		case ARRAY_TYPE_VOLUME:
			return getVolumeArrayVectors(arrayVectors);
		}
		return null;
	}

	private PamVector[] getLineArrayVector(PamVector[] arrayVectors) {
		// we know they are in a line, so just take the first two. 
		PamVector[] vectors = new PamVector[1];
		vectors[0] = arrayVectors[1].sub(arrayVectors[0]);
		// want to line up along the positive direction of an axis if at all
		// possible. 
		int ax = vectors[0].getPrincipleAxis();
		if (vectors[0].dotProd(PamVector.getCartesianAxes(ax)) < 0) {
			vectors[0] = vectors[0].times(-1);
		}
		vectors[0] = vectors[0].getUnitVector();
		return vectors;
	}

	private PamVector[] getPlaneArrayVectors(PamVector[] arrayVectors) {
		PamVector[] vectors = new PamVector[2];
		PamVector[] vectorPairs = PamVector.getVectorPairs(arrayVectors);
		int nPairs = vectorPairs.length;
		int[] closestAxis = new int[nPairs];
		double[] closestAngle = new double[nPairs];
		for (int i = 0; i < nPairs; i++) {
			closestAxis[i] = vectorPairs[i].getPrincipleAxis();
			closestAngle[i] = vectorPairs[i].absAngle(PamVector.getCartesianAxes(closestAxis[i]));
		}
		// need to find the direction vector for the plane. 
		// can do this by finding any non zero vector product.
		PamVector planePerpendicular = null;;
		for (int i = 0; i < nPairs; i++) {
			for (int j = (i+1); j < nPairs; j++) {
				if (vectorPairs[i].isParallel(vectorPairs[j]) == false) {
					planePerpendicular = vectorPairs[i].vecProd(vectorPairs[j]);
					break;
				}
			}
			if (planePerpendicular == null) {
				break;
			}
		}
		if (planePerpendicular == null) {
			planePerpendicular = PamVector.getZAxis().clone();			
		}

		//		find the closest pair to each of the three axis. 
		int[] closestPair = new int[3];		
		PamVector axis;
		double closest;
		double ang;
		for (int ax = 0; ax < 3; ax++) {
			closestPair[ax] = -1;
			axis = PamVector.getCartesianAxes(ax);
			closest = Double.MAX_VALUE;
			for (int i = 0; i < nPairs; i++) {
				if (closestAxis[i] != ax) {
					continue;
				}
				if (closestAngle[i] < closest) {
					closest = closestAngle[i];
					closestPair[ax] = i;
				}
			}
		}
		// try to line up the first vector on the y axis, then x, then z
		int startPair = -1;
		if (closestPair[1] >= 0) {
			startPair = closestPair[1];
		}
		else if (closestPair[0] >= 0) {
			startPair = closestPair[0];
		}
		else {
			startPair = closestPair[2];
		}
		if (startPair < 0) {
			return null;
		}
		vectors[0] = vectorPairs[startPair];
		if (vectors[0].angle(PamVector.getCartesianAxes(closestAxis[startPair])) > Math.PI/2) {
			vectors[0] = vectors[0].times(-1);
		}
		// second vector must be perpendicular to first one and also 
		// to the plane perpendicular.
		vectors[1] = vectors[0].vecProd(planePerpendicular);
		int closestAx = vectors[1].getPrincipleAxis();
		if (vectors[1].angle(PamVector.getCartesianAxes(closestAx)) > Math.PI/2) {
			vectors[1] = vectors[1].times(-1);
		}
		vectors[0] = vectors[0].getUnitVector();
		vectors[1] = vectors[1].getUnitVector();


		return vectors;
	}


	private PamVector[] getVolumeArrayVectors(PamVector[] arrayVectors) {
		PamVector[] vectors = new PamVector[3];
		for (int i = 0; i < 3; i++) {
			vectors[i] = PamVector.getCartesianAxes(i).clone();
		}
		return vectors;
	}

	public PamVector[] getArrayVectors(PamArray array, int phones) {
		int nPhones = PamUtils.getNumChannels(phones);
		PamVector[] arrayVectors = new PamVector[nPhones];
		int iPhone;
		for (int i = 0; i < nPhones; i++) {
			iPhone = PamUtils.getNthChannel(i, phones);
			arrayVectors[i] = array.getAbsHydrophoneVector(iPhone,0);
		}
		return arrayVectors;
	}

	public static String getArrayTypeString(int arrayType) {
		switch (arrayType) {
		case ARRAY_TYPE_NONE:
			return "No Hydrophones";
		case ARRAY_TYPE_POINT:
			return "Point Array";
		case ARRAY_TYPE_LINE:
			return "Line Array";
		case ARRAY_TYPE_PLANE:
			return "Planar Array";
		case ARRAY_TYPE_VOLUME:
			return "Volumetric Array";
		}
		return null;
	}

	/**
	 * Test to see if a load of inter-pair vectors are in line or not. 
	 * @param pvs inter-hydrophone vectors. 
	 * @return true if they all line up. 
	 */
	private boolean areInLine(PamVector[] pvs) {
		int nPairs = pvs.length;
		for (int i = 0; i < nPairs; i++) {
			for (int j = i+1; j < nPairs; j++) {
				if (pvs[i].isInLine(pvs[j]) == false) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Check to see if inter pair vectors define a volume
	 * or just a plane. 
	 * @param pvs array of inter phone vectors
	 * @return true if there is significant volume. 
	 */
	private boolean areOnPlane(PamVector[] pvs) {
		double vol = getMaxVolume(pvs);
		return (vol == 0);
	}

	private double getMaxVolume(PamVector[] pvs) {
		int nPairs = pvs.length;
		double maxVol = 0;
		double vol;
		for (int i = 0; i < nPairs; i++) {
			for (int j = i+1; j < nPairs; j++) {
				for (int k = j+1; k < nPairs; k++) {
					vol = pvs[i].tripleDotProduct(pvs[j], pvs[k]);
					maxVol = Math.max(maxVol, vol);
				}
			}
		}
		return maxVol;
	}

	/**
	 * Called from the network receiver to create and assign some specific hydrophone streamer within an
	 * array and to clone a streamer to make a new one if necessary 
	 * @param buoyStats 
	 */
	//	@Deprecated
	//	public void checkBuoyHydropneStreamer(BuoyStatusDataUnit buoyStats) {
	//		if (buoyStats.getHydrophoneStreamer() != null) {
	//			// no need to do anything if it' already found it's streamer. 
	//			return;
	//		}
	//		// check that we're using the correct hydrophone locator. 
	//		PamArray currentArray = getCurrentArray();
	//		HydrophoneLocator locator = currentArray.getHydrophoneLocator();
	////		if (locator == null || locator.getClass() != NetworkHydrophoneLocator.class) {
	////			currentArray.setArrayType(PamArray.ARRAY_TYPE_TOWED);
	////			currentArray.setArrayLocator(2);
	////		}
	//		
	//		// now try to find a streamer with that buoy id. 
	//		Streamer streamer = currentArray.findStreamer(buoyStats.getBuoyName());
	//		if (streamer != null) {
	//			buoyStats.setHydrophoneStreamer(streamer);
	//			return;
	//		}
	//		
	//		// try the first streamer - see if it's already assigned to any buoy, it not grab it. 
	//		Streamer firstStreamer = currentArray.getStreamer(0);
	//		if (firstStreamer != null && firstStreamer.getBuoyId1() == null) {
	//			buoyStats.setHydrophoneStreamer(firstStreamer);
	//			return;
	//		}
	//		// else will have to clone that first streamer, hydrophones and all !!!
	//		int nStreamers = currentArray.getNumStreamers();
	//		if (firstStreamer != null) {
	//			streamer = new Streamer(firstStreamer, nStreamers, buoyStats.getBuoyId1());
	//		}
	//		else {
	//			streamer = new Streamer(nStreamers);
	//		}
	//		int streamerId = currentArray.addStreamer(streamer);
	//		buoyStats.setHydrophoneStreamer(streamer);
	//		
	//		// clone the hydrophones attached to that streamer. 
	//		Hydrophone oldPhone, newPhone;
	//		int n = currentArray.getHydrophoneCount();
	//		for (int i = 0; i < n; i++) {
	//			oldPhone = currentArray.getHydrophone(i);
	//			if (oldPhone.getStreamerId() != 0) {
	//				continue;
	//			}
	//			newPhone = new Hydrophone(currentArray.getHydrophoneCount(), oldPhone.getX() + 10*streamerId, oldPhone.getY(), oldPhone.getZ(),
	//					oldPhone.getType(), oldPhone.getSensitivity(), oldPhone.getBandwidth().clone(), oldPhone.getPreampGain());
	//			newPhone.setdX(oldPhone.getdX());
	//			newPhone.setdY(oldPhone.getdY());
	//			newPhone.setdZ(oldPhone.getdZ());
	//			newPhone.setStreamerId(streamerId);
	//			currentArray.addHydrophone(newPhone);
	//		}
	//	}

	//	public ArrayDialog getArrayDialog() {
	//		System.out.println("static array dialog: "+singleInstance.arrayDialog);
	//		System.out.println("this: "+ arrayDialog);
	//		return arrayDialog;
	//	}

	public HydrophoneDataBlock getHydrophoneDataBlock() {
		return hydrophonesProcess.getHydrophoneDataBlock();
	}

	public StreamerDataBlock getStreamerDatabBlock() {
		return hydrophonesProcess.getStreamerDataBlock();
	}

	public HydrophoneSQLLogging getHydrophoneSQLLogging() {
		return hydrophonesProcess.getHydrophoneSQLlogging();
	}

	//	public void setArrayDialog(ArrayDialog arrayDialog) {
	//		this.arrayDialog=arrayDialog;
	//	}
	/**
	 * Gets the GPS data block, if there is one. If no datablock is present then returns null; 
	 * @return reference to the gps data block (if there is one)
	 */
	public static GPSDataBlock getGPSDataBlock() {
		// go right to the main gps data block for this, 
		GPSControl gpsControl = (GPSControl) PamController.getInstance().findControlledUnit(GPSControl.gpsUnitType);
		if (gpsControl == null) {
			return null;
		}
		return gpsControl.getGpsDataBlock();
	}

	public void showHydrophoneImportDialog(JFrame guiFrame) {
		hydrophoneImportManager.showImportDialog();	
	}

	public void showStreamerImportDialog(JFrame guiFrame) {
		streamerImportManager.showImportDialog();	
	}

//	/**
//	 * Get the current geometry for a data unit's hydrophones. 
//	 * @param dataUnits list of data units 
//	 * @return current geometry. 
//	 */
//	public SnapshotGeometry getSnapshotGeometry(PamDataUnit dataUnit) {
//		/**
//		 * first work out the total number of channels involved
//		 * and try to individually link data units back to the 
//		 * acquisition process, just in case this ever gets used
//		 * with different daq's (I don't think this is even possible
//		 * but might as well allow for it). 
//		 * Aiming to end up with an array of channels and an array
//		 * of hydrophone numbers. 
//		 * PhoneNo's will end up with a -1 if no hydrophone is assigned
//		 * to a channel (again, this should never happen). 
//		 */
//		int nChan =  PamUtils.getNumChannels(dataUnit.getChannelBitmap());
//		int[] phoneNos = new int[nChan];
//		int[] chanNos = new int[nChan];
//		int iC = 0;
//		int chMap = dataUnit.getChannelBitmap();
//		int phoneMap = chMap;
//		PamProcess sourceProcess = dataUnit.getParentDataBlock().getSourceProcess();
//		if (sourceProcess instanceof AcquisitionProcess) {
//			AcquisitionProcess ap = (AcquisitionProcess) sourceProcess;
//			phoneMap = ap.getAcquisitionControl().ChannelsToHydrophones(chMap);
//		}
//		int uChans = PamUtils.getNumChannels(chMap);
//		for (int c = 0; c < uChans; c++) {
//			chanNos[iC] = PamUtils.getNthChannel(c, chMap);
//			phoneNos[iC] = PamUtils.getNthChannel(c, phoneMap);
//			iC++;
//		}
//		long now = dataUnit.getTimeMilliseconds();
//		/*
//		 * Now get the array geometry for that list of hydrophones.
//		 * Annoyingly, a lot of the geometry functions are based around
//		 * phone bitmaps. however we need to be a bit wary of them since there
//		 * is a small chance that a hydrophone may get repeated somehow.   
//		 */
//		PamArray currentArray = getCurrentArray();
//		double[][] geometry = new double[nChan][];
//		double[] centre = new double[3];
//		int nGood = 0;
//		GpsData referenceLatLong = currentArray.getHydrophoneLocator().getReferenceLatLong(now);
//
//		//		arrayHeading = currentArray.getHydrophoneLocator().get
//		for (int i = 0; i < nChan; i++) {
//			if (phoneNos[i] >= 0) {
//				geometry[i] = currentArray.getAbsHydrophoneVector(phoneNos[i], now).getVector();
//				if (geometry[i] != null) {
//					nGood++;
//					for (int p = 0; p < 3; p++) {
//						centre[p] += geometry[i][p];
//					}
//				}
//			}
//		}
//		if (nGood > 0) {
//			for (int p = 0; p < 3; p++) {
//				centre[p] /= nGood;
//			}
//		}
//
//		SnapshotGeometry sg = new SnapshotGeometry(currentArray, now, chanNos, phoneNos, referenceLatLong, centre, geometry);
//		return sg;
//	}

	/**
	 * Make an ordered set of geometry data for the given hydrophones. 
	 * data rows will be null if a hydrophone isn't used. 
	 * 
	 * Geometry is all returned in cartesian coordinates relative to 
	 * the reference point which is the position of the first hydrophone streamer. 
	 * 
	 * All individual hydrophones have their pitch, roll and everything else added 
	 * within this function. 
	 *  
	 * @param hydrophoneMap bitmap of used hydrophones
	 * @return geometry data. 
	 */
	public SnapshotGeometry getSnapshotGeometry(int hydrophoneMap, long timeMillis) {
		PamArray currentArray = getCurrentArray();
		if (currentArray == null) {
			return null;
		}
		boolean getSpacingErrors = true;
		int nPhones = PamUtils.getNumChannels(hydrophoneMap);
		int maxPhone = PamUtils.getHighestChannel(hydrophoneMap);
		int[] hydrophoneList = new int[nPhones];
		int[] streamerList = new int[maxPhone+1];
		PamVector[] geometry = new PamVector[maxPhone+1];
		PamVector[] streamerError = new PamVector[maxPhone+1];
		PamVector[] hydrophoneError = new PamVector[maxPhone+1];
		double[] centre = new double[3];
//		GpsData referenceLatLong = currentArray.getHydrophoneLocator().getReferenceLatLong(timeMillis);
		int nGood = 0;
		int lastStreamerId = -1;
		GpsData firstStreamerPos = null, streamerPos = null;
		PamQuaternion streamerQuaternion = null;
		double streamerHead = 0, streamerPitch = 0, streamerRoll = 0;
		PamVector streamerOffestFromFirst = null;
		PamVector streamerErrorVec = null;
		for (int i = 0; i <= maxPhone; i++) {
			if ((1<<i & hydrophoneMap) == 0) {
				continue;
			}
			int streamerId = currentArray.getStreamerForPhone(i);
			if (streamerId != lastStreamerId) {
				lastStreamerId = streamerId;
				Streamer streamer = currentArray.getStreamer(streamerId);
				streamerPos = streamer.getHydrophoneLocator().getStreamerLatLong(timeMillis); // getReferenceLatLong(timeMillis);
				if (streamerPos == null) {
					return null;
				}
				streamerErrorVec = streamer.getErrorVector();
				streamerHead = streamerPos.getHeading();
				streamerPitch = streamerPos.getPitch();
				streamerRoll = streamerPos.getRoll();
//				System.out.printf("HPR = %5.1f, %5.1f, %5.1f\n", streamerHead, streamerPitch, streamerRoll);
				if (streamerPitch != 0. || streamerRoll != 0.) {
					streamerQuaternion = new PamQuaternion(Math.toRadians(streamerHead), Math.toRadians(streamerPitch), Math.toRadians(streamerRoll));
					streamerErrorVec = PamVector.rotateVector(streamerErrorVec, streamerQuaternion);
				}
				else {
					streamerQuaternion = null;
					streamerErrorVec = streamerErrorVec.rotate(-Math.toRadians(streamerHead));
				}
				if (firstStreamerPos == null) {
					firstStreamerPos = streamerPos;
				}
				else {
					// work out the difference in position from this streamer to the 
					// first one and use that as an offset. ....
					streamerOffestFromFirst = new PamVector(firstStreamerPos.distanceTo(streamerPos));
				}
			}
			/**
			 * Rotate the hydrophone about the centre of it's streamer. 
			 */
			Hydrophone hydrophone = currentArray.getHiddenHydrophone(i);
			if (hydrophone == null) {
				continue;
			}
			PamVector hydrophoneVec = hydrophone.getVector();
			PamVector hydrophoneErrorVec = hydrophone.getErrorVector();
			if (streamerQuaternion != null) {
				hydrophoneVec = PamVector.rotateVector(hydrophoneVec, streamerQuaternion);
				hydrophoneErrorVec = PamVector.rotateVector(hydrophoneErrorVec, streamerQuaternion);
			}
			else if (streamerHead != 0) { 
				hydrophoneVec = hydrophoneVec.rotate(-Math.toRadians(streamerHead));
				hydrophoneErrorVec = hydrophoneErrorVec.rotate(-Math.toRadians(streamerHead));
			}
			if (streamerOffestFromFirst != null) {
				hydrophoneVec = hydrophoneVec.add(streamerOffestFromFirst);
			}
			
			geometry[i] = hydrophoneVec;
			hydrophoneList[nGood] = i;
			streamerList[i] = lastStreamerId;
			hydrophoneError[i] = hydrophoneErrorVec;
			streamerError[i] = streamerErrorVec;
			nGood++;
			for (int p = 0; p < 3; p++) {
				centre[p] += geometry[i].getCoordinate(p);
			}
		}

		if (nGood > 0) for (int p = 0; p < 3; p++) {
			centre[p] /= nGood;
		}
		
		return new SnapshotGeometry(currentArray, timeMillis, streamerList, hydrophoneList, firstStreamerPos,
				new PamVector(centre), geometry, streamerError, hydrophoneError);
		
	}
//	
//	public SnapshotGeometry getSubDetectionGeometry(PamDataUnit superDataUnit) {
//		/**
//		 * first work out the total number of channels involved
//		 * and try to individually link data units back to the 
//		 * acquisition process, just in case this ever gets used
//		 * with different daq's (I don't think this is even possible
//		 * but might as well allow for it). 
//		 * Aiming to end up with an array of channels and an array
//		 * of hydrophone numbers. 
//		 * PhoneNo's will end up with a -1 if no hydrophone is assigned
//		 * to a channel (again, this should never happen). 
//		 * 
//		 * It can get a bit complicated having the hydrophones in an odd order
//		 * but this can .  
//		 */
//		int nChan = 0;
//		int nSubs = superDataUnit.getSubDetectionsCount();
//		for (int i = 0; i < nSubs; i++) {
//			PamDataUnit dataUnit = superDataUnit.getSubDetection(i);
//			nChan += PamUtils.getNumChannels(dataUnit.getChannelBitmap());
//		}
//		int[] phoneNos = new int[nChan];
//		int[] chanNos = new int[nChan];
//		int iC = 0;
//		for (int i = 0; i < nSubs; i++) {
//			PamDataUnit dataUnit = superDataUnit.getSubDetection(i);
//			int chMap = dataUnit.getChannelBitmap();
//			int phoneMap = chMap;
//			PamProcess sourceProcess = dataUnit.getParentDataBlock().getSourceProcess();
//			if (sourceProcess instanceof AcquisitionProcess) {
//				AcquisitionProcess ap = (AcquisitionProcess) sourceProcess;
//				phoneMap = ap.getAcquisitionControl().ChannelsToHydrophones(chMap);
//			}
//			int uChans = PamUtils.getNumChannels(chMap);
//			for (int c = 0; c < uChans; c++) {
//				chanNos[iC] = PamUtils.getNthChannel(c, chMap);
//				phoneNos[iC] = PamUtils.getNthChannel(c, phoneMap);
//				iC++;
//			}
//		}
//		long now = superDataUnit.getTimeMilliseconds();
//		/*
//		 * Now get the array geometry for that list of hydrophones.
//		 * Annoyingly, a lot of the geometry functions are based around
//		 * phone bitmaps. however we need to be a bit wary of them since there
//		 * is a small chance that a hydrophone may get repeated somehow.   
//		 */
//		PamArray currentArray = getCurrentArray();
//		double[][] geometry = new double[nChan][];
//		double[] centre = new double[3];
//		int nGood = 0;
//		GpsData referenceLatLong = currentArray.getHydrophoneLocator().getReferenceLatLong(now);
//
//		//		arrayHeading = currentArray.getHydrophoneLocator().get
//		for (int i = 0; i < nChan; i++) {
//			if (phoneNos[i] >= 0) {
//				geometry[i] = currentArray.getAbsHydrophoneVector(phoneNos[i], now).getVector();
//				if (geometry[i] != null) {
//					nGood++;
//					for (int p = 0; p < 3; p++) {
//						centre[p] += geometry[i][p];
//					}
//				}
//			}
//		}
//		if (nGood > 0) {
//			for (int p = 0; p < 3; p++) {
//				centre[p] /= nGood;
//			}
//		}
//
//		SnapshotGeometry sg = new SnapshotGeometry(currentArray, now, chanNos, phoneNos, referenceLatLong, centre, geometry);
//		return sg;
//	}
	
	/**
	 * Add module info to the array manager. Need to do this to add icon which is used in data model. 
	 */
	private void addModuleInfo(){
		//need to add module info due to fact array manager is a special case
		PamModuleInfo arrayModuleInfo=new PamModuleInfo("ArrayManager", "Array Manager", ArrayManager.class); 
		arrayModuleInfo.setCoreModule(true);
		this.setPamModuleInfo(arrayModuleInfo);
	}
	
	@Override
	public PamControlledUnitGUI getGUI(int flag) {
		if (flag==PamGUIManager.FX) {
			if (arrayGUIFX == null) {
				arrayGUIFX= new ArrayGUIFX(this);
			}
			return arrayGUIFX;
		}
		if (flag==PamGUIManager.SWING) {
			if (arrayGUISwing == null) {
				arrayGUISwing= new WrapperControlledGUISwing(this);
			}
			return arrayGUISwing;
		}
		return null;
	}

	@Override
	public GpsData getReferencePosition(long timeMillis) {
		int hMap = PamUtils.makeChannelMap(getCurrentArray().getHydrophoneCount());
		SnapshotGeometry sg = getSnapshotGeometry(hMap, timeMillis);
		return sg.getReferenceGPS();
	}

	@Override
	public String getReferenceName() {
		return getUnitName();
	}
	
	@Override
	public void receiveSourceNotification(int type, Object object) {
		// don't do anything by default
	}



}
