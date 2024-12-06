package rawDeepLearningClassifier.dlClassification.delphinID;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;

import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import org.jamdev.jdl4pam.transforms.DLTransfromParams;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickDetection;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLClassiferModel;
import rawDeepLearningClassifier.dlClassification.PredictionResult;
import rawDeepLearningClassifier.dlClassification.StandardClassifierModel;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.delphinID.DelphinIDParams.DelphinIDDataType;
import rawDeepLearningClassifier.dlClassification.genericModel.DLModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;
import rawDeepLearningClassifier.layoutFX.DLCLassiferModelUI;
import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;
import whistlesAndMoans.ConnectedRegionDataBlock;
import whistlesAndMoans.ConnectedRegionDataUnit;

/**
 * A classifier based on the delphinID method which uses whistle contours to predict
 * dolphin species. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DelphinIDClassifier extends StandardClassifierModel {


	private DelphinIDParams delphinIDParams = new DelphinIDParams();


	private DelphinUI delphinUI;


	private DelphinIDWorker delphinIDWorker;

	private  ArrayList<Class> allowedDataTypes; 

	public DelphinIDClassifier(DLControl dlControl) {
		super(dlControl);

		allowedDataTypes = new ArrayList<Class>(); 
		//load the previous settings
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public boolean isModelType(URI model) {
		return false;
	}

	@Override
	public void prepModel() {
		super.prepModel();
		//set group detections to true. 
		getDLControl().setGroupDetections(true);
	}

	@Override
	public String getName() {
		return "delphinID";
	}

	@Override
	public DLCLassiferModelUI getModelUI() {
		if (delphinUI==null) {
			delphinUI = new DelphinUI(this); 
		}
		return delphinUI;
	}

	@Override
	public Serializable getDLModelSettings() {
		return delphinIDParams;
	}

	@Override
	public String getUnitName() {
		return dlControl.getUnitName()+"_" + getName(); 
	}

	@Override
	public String getUnitType() {
		return dlControl.getUnitType()+"_" + getName();
	}

	@Override
	public Serializable getSettingsReference() {
		if (delphinIDParams==null) {
			delphinIDParams = new DelphinIDParams(); 
		}

		ArrayList<DLTransfromParams> dlTransformParams = DLClassiferModel.getDLTransformParams(delphinIDParams.dlTransfroms);

		delphinIDParams.dlTransfromParams=dlTransformParams; 

		//System.out.println("SoundSpot have been saved. : " + soundSpotParmas.classNames); 
		return delphinIDParams;

	}

	@Override
	public ArrayList<? extends PredictionResult> runModel(ArrayList<? extends PamDataUnit> groupedRawData) {

		//add an extra test to see if the detection pre count has passed. 

		if (detectionPreFilter(groupedRawData)) {
			return super.runModel(groupedRawData);
		}
		else return null;
	}

	/**
	 * Check whether the delphinID model should run at all. 
	 * @param groupedRawData - the grouped raw data. 
	 * @return true if the model should run. 
	 */
	private boolean detectionPreFilter(ArrayList<? extends PamDataUnit> groupedRawData) {
		if (dlControl.getSegmenter().getParentDataBlock() instanceof  ConnectedRegionDataBlock) {
			return whistlePreFilter(groupedRawData);
		}
		else {
			return clickPreFilter(groupedRawData);
		}
	}


	private boolean clickPreFilter(ArrayList<? extends PamDataUnit> groupedRawData) {

		if (((SegmenterDetectionGroup) groupedRawData.get(0)).getSubDetectionsCount()>=delphinIDParams.minDetectionValue) {
			return true;
		}

		return true;
	}

	private boolean whistlePreFilter(ArrayList<? extends PamDataUnit> groupedRawData) {
		//TODO
		//		System.out.println("Check WHISTLE fragment density"); 
		//		
		//		3.	Within each time frame, the density of detection is calculated and used as a filter.
		//		-	Density 2
		//		-	where the length of frame = frame duration / mean time step across contour
		//		(time steps between time-frequency points in contour saved by ROCCA depend on FFT resolution but can vary slightly within contour)
		//
		//		4.	If a detection frame has less than 0.30 detection density, it is not used for classification
		//		double density = DelphinIDUtils.getDensity(null); 


		if (groupedRawData==null || groupedRawData.size()<1) {
			System.err.println("DelphinIDClassifier: + the grouped raw data is null or zero size:"); 
			return false;
		}

		System.out.println("Run DelphinID model: " +  groupedRawData.size() + " min density: " + delphinIDParams.minDetectionValue); 

		double density = DelphinIDUtils.getDensity((SegmenterDetectionGroup) groupedRawData.get(0)); 

		if (density>=delphinIDParams.minDetectionValue) {
			return true;
		}

		return true;
	}

	@Override
	public boolean isDecision(StandardPrediction modelResult, StandardModelParams modelParmas) {
		//TODO
		//DelphinID might end up using a different decision making process to most of the standard classifiers which just pass a binary threshold. 
		return super.isDecision(modelResult, modelParmas);
	}



	@Override
	public long getSettingsVersion() {
		return StandardModelParams.serialVersionUID;
	}


	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {

		DelphinIDParams newParameters = (DelphinIDParams) pamControlledUnitSettings.getSettings();
		if (newParameters!=null) {
			delphinIDParams = (DelphinIDParams) newParameters.clone();
			//			System.out.println("DELPHINID have been restored. : " + delphinIDParams.modelPath); 
			if (delphinIDParams.dlTransfromParams!=null) {
				delphinIDParams.dlTransfroms = DLTransformsFactory.makeDLTransforms((ArrayList<DLTransfromParams>) delphinIDParams.dlTransfromParams); 
			}
		}
		else delphinIDParams = new DelphinIDParams();
		return true; 
	}



	@Override
	public DLModelWorker<StandardPrediction> getDLWorker() {
		if (delphinIDWorker==null) {
			delphinIDWorker = new DelphinIDWorker();
		}
		return delphinIDWorker;
	}

	@Override
	public DelphinIDParams getDLParams() {
		return delphinIDParams;
	}

	public void setDLParams(DelphinIDParams params) {
		this.delphinIDParams=params;

	}


	@Override
	public ArrayList<Class> getAllowedDataTypes(){
		allowedDataTypes.clear();

		if (delphinIDParams.getDataType()==DelphinIDDataType.CLICKS) {
			allowedDataTypes.add(ClickDetection.class);
		}

		if (delphinIDParams.getDataType()==DelphinIDDataType.WHISTLES) {
			allowedDataTypes.add(ConnectedRegionDataUnit.class);
		}

		return allowedDataTypes;
	}


}