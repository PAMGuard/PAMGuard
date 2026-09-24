package rawDeepLearningClassifier.dlClassification.yolo;

import PamController.SettingsPane;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelUI;

/**
 * UI for a YOLO classifier.
 * 
 * @author Jamie Macaulay
 */
public class YoloUI extends ArchiveModelUI  {

	
	public YoloUI(YoloClassifier archiveClassifier) {
		super(archiveClassifier);
	}


	@Override
	public SettingsPane<StandardModelParams> getSettingsPane() {
		if (standardSettingsPane==null) {
			standardSettingsPane = new  YoloPane(getYoloClassifier()); 
		}
		return standardSettingsPane;
	}

	private YoloClassifier getYoloClassifier() {
		return (YoloClassifier) this.getArchiveModelClassifier();
	}


}
