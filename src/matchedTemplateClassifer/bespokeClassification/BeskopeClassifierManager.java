package matchedTemplateClassifer.bespokeClassification;

import java.util.function.Predicate;

import PamguardMVC.PamDataUnit;
import clickDetector.ClickControl;
import clickDetector.ClickDataBlock;
import clickDetector.ClickDetection;
import clickTrainDetector.CTDataUnit;
import clickTrainDetector.classification.CTClassification;
import clickTrainDetector.classification.CTClassifierType;
import matchedTemplateClassifer.MTClassifierControl;
import matchedTemplateClassifer.MTProcess;
import matchedTemplateClassifer.MatchedTemplateParams;

/**
 * Manages classification flags and/or any bespoke alteration to specific types of data units
 * after an MT Classification has been processed. 
 * <p>
 * For example, ClickDetections have a species flag which is used by many displays and saved 
 * in binary files. Although the Annotation framework the MTClassifier uses is generic across
 * all types of data unit it is still useful to add the flag to the ClickDetection. This helps
 * both simplify post analysis tasks as flags are saved to the binary file and also ensure that
 * classified clicks are coloured differently on many displays. 
 * 
 * @author Jamie Macaulay 
 *
 */
@SuppressWarnings("rawtypes")
public class BeskopeClassifierManager {

	/**
	 * Reference to the MTProcess the bespoke classifier manager is associated with. 
	 */
	@SuppressWarnings("unused")
	private MTProcess mtProcess;
	
	private MTClassifierControl mtControl;

	public BeskopeClassifierManager(MTProcess mtProcess) {
		this.mtProcess = mtProcess; 
		this.mtControl = mtProcess.getMTControl();
	}
	
	
	/**
	 * Called whenever the settings of the matched click classifier are updated. 
	 */
	public void updateSettings(MatchedTemplateParams newParams) {
		if (mtProcess.getParentDataBlock() instanceof ClickDataBlock) {
			ClickControl clickControl = ((ClickDataBlock) (mtProcess.getParentDataBlock())).getClickControl(); 
			//clickControl.getClassifierManager().
		}
	}

	/**
	 * Classifies specific data units if they have bespoke data unit specific
	 * species flags. Some data units will not be affected by this function.
	 * <p>
	 * Note: 26/08/2019 that until PG has a unified classification framework this is always going 
	 * to be a bit of a messy function.
	 * 
	 * @param pamDataUnit - the input data unit to add classification flags to.
	 * @param classify    - true if binary classifier test has been passed.
	 */
	public void bespokeDataUnitFlags(PamDataUnit pamDataUnit, boolean classify) {

		//Click Detections
		if (pamDataUnit instanceof ClickDetection) {
			//if a click detection then set some flags. 
			ClickDetection clickDetection = (ClickDetection) pamDataUnit; 
			
			//set classification flag. There is a bit of an issue here. What if the click has been classified by
			//the in built click classifier. If yes and classified by match classifier then override. If yes and 
			//not classified by match classifier leave type flag. If no and not classified leave unclassified. 
			
			//work out default type and set the click to that. 
			
			/**
			 * What if the user has changed the classification flag and then the click is re analysed. Then the classification flag will not be
			 * reset to zero...This is the issue with a simple flag- it's got no info to trace back. //TODO
			 */
			int defaultType=clickDetection.getClickType(); 
			if (defaultType == mtControl.getMTParams().type) defaultType=0; //should be reset to zero because it was classified by MT classifier

			if (classify) {
				clickDetection.setClickType(mtControl.getMTParams().type);
				//					 clickDetection.getDataUnitFileInformation().setNeedsUpdate(true);
			}
			else clickDetection.setClickType((byte) defaultType); 
		}
		//Click Train Detections
		else if (pamDataUnit instanceof CTDataUnit) {
			
			//first of all, remove any MT classifications which are associated with this classifier. 
			CTDataUnit ctDataUnit = (CTDataUnit) pamDataUnit; 
		
			if (ctDataUnit.getCtClassifications()!=null) {
				//use a stream to remove all matched click classifications
				Predicate<CTClassification> isQualified = item -> item.getClassifierType() == CTClassifierType.MATCHEDCLICK;
				ctDataUnit.getCtClassifications().removeIf(isQualified);
//				ctDataUnit.getCtClassifications().stream()
//				  .filter(isQualified)
//				  .forEach(item -> item.operate());
			}
			
			//if the matched click classification was positive, add a new classification the click train detector. 
			if (classify) {
				ctDataUnit.addCtClassification(new MTClkTrnClassification(mtControl.getMTParams().type));
			}

		}
		
		/***Add other bespoke data units here***/
	
	}
}
