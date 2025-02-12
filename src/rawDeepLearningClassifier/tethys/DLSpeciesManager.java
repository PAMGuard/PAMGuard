package rawDeepLearningClassifier.tethys;

import java.util.ArrayList;

import PamUtils.PamArrayUtils;
import PamguardMVC.PamDataBlock;
import annotation.DataAnnotation;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLClassName;
import rawDeepLearningClassifier.dlClassification.DLClassiferModel;
import rawDeepLearningClassifier.dlClassification.DLDetection;
import rawDeepLearningClassifier.dlClassification.PredictionResult;
import rawDeepLearningClassifier.logging.DLAnnotation;
import tethys.species.DataBlockSpeciesCodes;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.DataBlockSpeciesMap;
import tethys.species.SpeciesMapItem;

public class DLSpeciesManager extends DataBlockSpeciesManager<DLDetection> {

	private DLControl dlControl;
	
	private static final String unknown = "Unknown";

	public DLSpeciesManager(DLControl dlControl, PamDataBlock<DLDetection> dataBlock) {
		super(dataBlock);
		this.dlControl = dlControl;
		
		checkDefaultMap();
	}

	/**
	 * DL Classifiers may have default ITIS codes set, in which case it's possible 
	 * to make the species map immediately from the defaults, which will make 
	 * users happy - especially for the multispecies!
	 */
	private void checkDefaultMap() {
		DLClassName[] classNames = getClassNames();
		if (classNames == null) {
			return;
		}
		DataBlockSpeciesMap speciesMap = getDatablockSpeciesMap();
		if (speciesMap == null) {
			return;
		}
		String[] classStrings = getClassStrings(classNames);
		for (int i = 0; i < classNames.length; i++) {
			DLClassName className = classNames[i];
			Integer itis = className.itisCode;
			if (itis == null) {
//				can't set the default;
				continue;
			}
			// find the appropriate map item. 
			SpeciesMapItem mapItem = speciesMap.getItem(className.className);
			if (mapItem == null) {
				mapItem = new SpeciesMapItem(itis, className.className, className.className);
				speciesMap.putItem(className.className, mapItem);
			}			
		}
		
	}

	@Override
	public DataBlockSpeciesCodes getSpeciesCodes() {
		DLClassName[] classNames = getClassNames();
		String[] classStrings = getClassStrings(classNames);
		return new DataBlockSpeciesCodes(unknown, classStrings);
	}
	
	/**
	 * Get just the strings of the class names. 
	 * @param classNames
	 * @return
	 */
	private String[] getClassStrings(DLClassName[] classNames) {
		if (classNames == null) {
			return null;
		}
		String[] classStrings = new String[classNames.length];
		for (int i = 0; i < classStrings.length; i++) {
			classStrings[i] = classNames[i].className;
		}
		return classStrings;
	}
	
	/**
	 * Get the list of class names, or make a single unknown category. 
	 * @return
	 */
	private DLClassName[] getClassNames() {
		DLClassiferModel currentModel = dlControl.getDLModel();
		DLClassName[] classNames = null;
		if (currentModel != null) {
			classNames = currentModel.getClassNames();
		}
		if (classNames == null) {
			classNames = new DLClassName[1];
			classNames[0] = new DLClassName(unknown, (short) 0);
		}
		return classNames;
	}
	
	

	@Override
	public String getSpeciesCode(DLDetection dlDetection) {
		int nAnnot = dlDetection.getNumDataAnnotations();
		if (nAnnot == 0) {
			return unknown;
		}

		PredictionResult result = dlControl.getDLClassifyProcess().getBestModelResult(dlDetection);
		if (result == null) {
			return unknown;
		}
		int resInd = 0;
		float bestScore = 0;
		float[] scores = result.getPrediction();
		if (scores == null || scores.length == 0) {
			return unknown;
		}
		for (int i = 0; i < scores.length; i++) {
			if (scores[i] > bestScore) {
				bestScore = scores[i];
				resInd = i;
			}
		}
		String[] names = getClassStrings(getClassNames());
		if (resInd < names.length) {
			return names[resInd];
		}
		return unknown;
	}

}
