package rawDeepLearningClassifier.dlClassification.deepAcoustics;

import PamController.SettingsPane;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelUI;

public class DeepAcousticsUI extends ArchiveModelUI  {

	
	public DeepAcousticsUI(DeepAcousticsClassifier archiveClassifier) {
		super(archiveClassifier);
	}


	@Override
	public SettingsPane<StandardModelParams> getSettingsPane() {
		if (standardSettingsPane==null) {
			standardSettingsPane = new  DeepAcousticsPane(getDeepAcousticsClassifier()); 
		}
		return standardSettingsPane;
	}

	private DeepAcousticsClassifier getDeepAcousticsClassifier() {
		return (DeepAcousticsClassifier) this.getArchiveModelClassifier();
	}


}
