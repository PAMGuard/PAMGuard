package videoRangePanel;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import videoRangePanel.pamImage.ImageTimeOffset;
import videoRangePanel.pamImage.ImageTimeParser;
import videoRangePanel.pamImage.MetaDataTimeParser;
import videoRangePanel.pamImage.TimeOffset;
import videoRangePanel.vrmethods.landMarkMethod.LandMarkGroup;

public class VRParameters implements Serializable, Cloneable {

	public static final long serialVersionUID = 0;
	
	public static final int IMAGE_SCROLL = 0;
	public static final int IMAGE_CROP = 1;
	public static final int IMAGE_SHRINK = 2;
	public static final int IMAGE_STRETCH = 3;
	public static final int IMAGE_SHRINKORSTRETCH = 4;
	public static final int MOUSE_WHEEL_CONTROL=5;
	public int imageScaling = IMAGE_SCROLL;
	public static String[] scaleNames = {"Scroll", "Crop to window", "Shrink to fit"};
	public static String[] shortScaleNames = {"Scroll", "Crop", "Shrink"};


	
	/*************LandMark Method************/

	/**
	 * List of Land marks. 
	 */
	private ArrayList<LandMarkGroup> landMarks;
	private int currentLandMarkIndex=0; 
	
	
	/***********Image location*************/

	/**
	 * List of manually inputed GPS locations
	 */
	private LandMarkGroup manualGPS;

	/**
	 * 
	 * The currently selected GPS point. 
	 */
	private int currentManualGPSIndex=0;
	
	/**
	 * List of camera heights
	 */
	private ArrayList<VRHeightData> heightDatas;
	private int currentHeightIndex = 0;
	
	
	/***********General Method*************/
	
	/**
	 * List of camera calibration values
	 */
	private ArrayList<VRCalibrationData> calibrationDatas;
	private int currentCalibrationIndex = 0; 

	/**
	 * The type of method to use to get range cause the earth ain't flat. 
	 */
	public int rangeMethod = VRHorzMethods.METHOD_ROUND;

	
	/************Horizon Method**************/
	
	/**
	 * Draw a temporary 
	 */
	public boolean drawTempHorizon = true;

	/**
	 * True to measure angles. 
	 */
	public boolean measureAngles;

	/**
	 * The angle data block. 
	 */
	public String angleDataBlock;

	
	/************Shore method***************/
	
	/**
	 * file name for shore data (Gebco format)
	 */
	public File shoreFile;

	/**
	 * Ignore the closest segment (ie. if operating from on shore) 
	 */
	public boolean ignoreClosest = true;

	/** 
	 * draw the shore line on the map
	 */
	public boolean showShore = true;

	/**
	 * also highlight points on the map file (to check resolution). 
	 */
	public boolean showShorePoints = true;
	
	
	/*********Image Params**************/
	
	/**
	 * The current direcory of the image file
	 */
	public File imageDirectory;

	/*
	 * Mainatian the aspect ratio. Used in the AWT GUI
	 */
	public boolean maintainAspectRatio = true;
	
	/**
	 * file name for the current image. Null is pasted image. 
	 */
	public File currentImageFile;
	/**
	 * The current image time offset. 
	 */
	public boolean useTimeOffset = false; 
	
	/**
	 * The current image time parser index. 
	 */
	public int imageTimeParser = 0; 

	
	/**
	 * The current image time offset. 
	 */
	public TimeOffset timeOffset = new ImageTimeOffset(0); 

	/**
	 * The index of the last VR method used. Only used to get back to the vr method that 
	 * was last used. 
	 */
	public int methodIndex=0; 
	
	
	/**
	 * file name for tide data (POLPRED format)
	 */
	@Deprecated
	public File currTideFile;


	public VRCalibrationData getCurrentCalibrationData() {
		if (currentCalibrationIndex < 0) {
			return null;
		}
		if (calibrationDatas == null || calibrationDatas.size() <= currentCalibrationIndex) {
			return null;
		}
		return calibrationDatas.get(currentCalibrationIndex);
	}

	public void setCurrentCalibration(VRCalibrationData currentCalibration) {
		if (currentCalibration == null) {
			return;
		}
		addCalibrationData(currentCalibration);
		currentCalibrationIndex = calibrationDatas.indexOf(currentCalibration);
	}

	public int findCalibrationData(VRCalibrationData aCalibration) {
		if (calibrationDatas == null) {
			return -1;
		}
		return calibrationDatas.indexOf(aCalibration);
	}

	public void addCalibrationData(VRCalibrationData aCalibration) {
		if (aCalibration == null) {
			return;
		}
		if (calibrationDatas == null) {
			calibrationDatas = new ArrayList<VRCalibrationData>();
		}
		if (findCalibrationData(aCalibration) < 0) {
			calibrationDatas.add(aCalibration);
		}
	}

	public void removeCalibrationData(VRCalibrationData aCalibration) {
		if (calibrationDatas == null) {
			return;
		}
		VRCalibrationData curData = getCurrentCalibrationData();
		calibrationDatas.remove(aCalibration);
		if (findCalibrationData(curData) < 0) {
			// have just deleted the current calibration !
			currentCalibrationIndex = 0;
		}
	}

	public double getCameraHeight() {
		if (heightDatas == null || heightDatas.size() == 0) {
			return 0;
		}
		if (currentHeightIndex < 0 || currentHeightIndex >= heightDatas.size()) {
			return 0;
		}
		return heightDatas.get(currentHeightIndex).height;
	}

	@Override
	public VRParameters clone() {
		try {
			VRParameters newParams = (VRParameters) super.clone();
			if (calibrationDatas != null) {
				newParams.calibrationDatas = (ArrayList<VRCalibrationData>) calibrationDatas.clone();
			}
			if (heightDatas != null) {
				newParams.heightDatas = (ArrayList<VRHeightData>) heightDatas.clone();
			}
			return newParams;
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ArrayList<VRCalibrationData> getCalibrationDatas() {
		return calibrationDatas;
	}

	public void setCalibrationDatas(ArrayList<VRCalibrationData> calibrationDatas) {
		this.calibrationDatas = calibrationDatas;
	}

	public int getCurrentCalibrationIndex() {
		return currentCalibrationIndex;
	}

	public void setCurrentCalibrationIndex(int currentCalibrationIndex) {
		this.currentCalibrationIndex = currentCalibrationIndex;
	}

	public ArrayList<VRHeightData> getHeightDatas() {
		if (heightDatas == null) {
			heightDatas = new ArrayList<VRHeightData>();
		}
		return heightDatas;
	}

	public void setHeightDatas(ArrayList<VRHeightData> heightDatas) {
		if (heightDatas == null) {
			heightDatas = new ArrayList<VRHeightData>();
		}
		this.heightDatas = heightDatas;
	}

	public int getCurrentHeightIndex() {
		return currentHeightIndex;
	}

	public void setCurrentHeightIndex(int currentHeightIndex) {
		this.currentHeightIndex = currentHeightIndex;
	}

	public VRHeightData getCurrentheightData() {
		if (heightDatas == null) {
			return null;
		}
		if (currentHeightIndex < 0 || currentHeightIndex >= heightDatas.size()) {
			return null;
		}
		return heightDatas.get(currentHeightIndex);
	}

	public boolean getShowShore() {
		return showShore;
	}

	public ArrayList<LandMarkGroup> getLandMarkDatas() {
		return landMarks;
	}

	public void  setLandMarkDatas( ArrayList<LandMarkGroup> landMarks) {
		this.landMarks=landMarks;
	}

	public void setCurrentLandMarkGroupIndex(int currentlySelected) {
//		System.out.println("Set the current landmark index to " + currentlySelected); 
		this.currentLandMarkIndex=currentlySelected;		
	}

	public int getSelectedLandMarkGroup() {
		return currentLandMarkIndex;
	}

	public LandMarkGroup getManualGPSDatas() {
		return manualGPS;
	}

	public int getCurrentManualGPSIndex() {
		return currentManualGPSIndex;
	}

	public void setGPSLocData(LandMarkGroup localGPSMarkList) {
		this.manualGPS=localGPSMarkList;
	}

	public void setGPSLocDataSelIndex(int currentlySelected) {
		this.currentManualGPSIndex=currentlySelected;
	}

}
