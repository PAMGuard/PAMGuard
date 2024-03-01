package rawDeepLearningClassifier.dlClassification.archiveModel;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.zip.ZipException;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.DLStatus;

/**
 * Loads a zip file and checks for a saved model alongside a pamguard settings file
 * <p>
 * The model quickly pre checks zip files to make sure there is a settings file inside. 
 * This means that non compatible zip files are greyed out in the file importer. 
 * <p>
 * The model can accept PyTorch or Tensorflow models. 
 * 
 * @author Jamie Macaulay
 *
 */
public class PamZipModelClassifier extends ArchiveModelClassifier {

	private static final String MODEL_NAME = "PAMGuard model";
	
	/**
	 * The file extensions
	 */
	private String[] fileExtensions = new String[] {"*.zip"};

	public PamZipModelClassifier(DLControl dlControl) {
		super(dlControl);
	}
	
	@Override
	public String getName() {
		return MODEL_NAME;
	}
	
	
	@Override
	public DLStatus setModel(URI zipUri) {
		//will change the params if we do not clone. 
		//first check whether the zip file has the correct model. 
//		ZipUtils.
		
		//check that we have some kind of model that we can load here. Do not attempt to load the model if not. 
		String model;
		try {
			model = getZipFilePath( zipUri, ".py");

			if (model==null) model = getZipFilePath( zipUri, ".pb");
			if (model==null) return DLStatus.INCOMPATIBLE_ZIP;

			String settingsFile = getZipFilePath( zipUri, ".pdtf");
			if (settingsFile==null)  return DLStatus.INCOMPATIBLE_ZIP;
		} catch (ZipException e) {
			e.printStackTrace();
			return DLStatus.INCOMPATIBLE_ZIP;
		} catch (IOException e) {
			e.printStackTrace();
			return DLStatus.INCOMPATIBLE_ZIP;
		}
				
		return super.setModel(zipUri);
	}
	
	/**
	 * Find the first file within a zip folder that matches a pattern. This peaks into the zip file instead of decompressing it. 
	 * @param zipUri - uri to the zip file
	 * @param filePattern - the file pattern to match - the file must contain this string. 
	 * @return null if no file found and the file pqth if the file is founf
	 * @throws ZipException
	 * @throws IOException
	 */
	private static String getZipFilePath(URI zipUri, String filePattern) throws ZipException, IOException {
		return ArchiveModelWorker.getZipFilePath(new File(zipUri),  filePattern);
	}
	
	/**
	 * Zip test.
	 * @param args
	 */
	public static void main(String[] args) {
	
		String fileName = "D:/Dropbox/PAMGuard_dev/Deep_Learning/Gibbons/gibbon_model_shared/gibbon_model.zip";
		
		URI zipUri = new  File(fileName).toURI();
				
		try {
			String pbFile = getZipFilePath( zipUri, ".pb");
			String transformFile = getZipFilePath( zipUri, ".pdtf");
			
			System.out.println("pbFile: "  + pbFile + " transformFile: " + transformFile);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	 
	
}
	

