package rawDeepLearningClassifier.dlClassification.archiveModel;

import rawDeepLearningClassifier.DLControl;

public class ZipModelClassifier extends ArchiveModelClassifier {


	public static String MODEL_NAME = "Zip";
	
	/**
	 * The file extensions
	 */
	private String[] fileExtensions = new String[] {"zip"};


	public ZipModelClassifier(DLControl dlControl) {
		super(dlControl);
	}
	
	@Override
	public String[] getFileExtensions() {
		return fileExtensions;
	}
	
	@Override
	public String getName() {
		return MODEL_NAME;
	}
}
