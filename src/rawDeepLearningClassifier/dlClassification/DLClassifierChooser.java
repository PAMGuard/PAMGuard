package rawDeepLearningClassifier.dlClassification;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.codehaus.plexus.util.FileUtils;
import org.jamdev.jdl4pam.transforms.jsonfile.DLTransformsParser;
import org.json.JSONObject;

import ai.djl.repository.FilenameUtils;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.DLZipUtils;

/**
 * Selects which type of DL classifier to use. 
 * 
 * @author Jamie Macaulay
 */
public class DLClassifierChooser {

	private DLControl dlControl;


	public DLClassifierChooser(DLControl dlControl) {
		this.dlControl = dlControl; 
	}

	/**
	 * Select the correct classifier model to use from a URI. 
	 * @param modelURI - the URI
	 * @return the classifier model that runs the file. 
	 */
	public DLClassiferModel selectClassiferModel(URI modelURI) {

		String extension = FileUtils.getExtension(modelURI.getPath());


		if (extension.equals("zip")) {
			//If the model is a zip file it may contain a model and a metadata file. Want to check 
			//this metadata file in case it points to a particular framework.
			try {
				String settingsFile = DLZipUtils.getZipFilePath(modelURI, ".pdtf");
				System.out.println("Settings file: " +settingsFile);
				if (settingsFile!=null) {
					//there's a settings file - does it contain a metadata field describing which
					//type of classifier it belongs to. 
					String outFolder = System.getProperty("user.home") +  File.separator + "PAMGuard_temp";
					new File(outFolder).mkdir();

					File file = DLZipUtils.extractFile(modelURI, settingsFile, outFolder);
					if (file!=null) {
						//now we need to open the file and get a specific JSON field which describes the model. 
						//read the JSON string from the the file. 
						String jsonString  = DLTransformsParser.readJSONString(file);
						JSONObject object = new JSONObject(jsonString); 

						JSONObject frameworkObject = object.getJSONObject("framework_info");
						String frameworkString = frameworkObject.getString("framework");

						for (DLClassiferModel model: dlControl.getDLModels()) {
							System.out.println("frameworkString: " + frameworkString + "  " + model.getName());
							if (model.getName().toLowerCase().equals(frameworkString.trim().toLowerCase())) return model;
						}
						
					} 
					else {
						System.err.println("DLClassifierChooser: Unable to extract the metadata file even though it exists in the zip archive:??"); 
					}
				}
				else {
					System.err.println("DLClassifierChooser: The zip file does not contain a metadata  file for the deep learning model"); 
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Check for model compatibility. This usually means checking for a file
		// extension. Note that a koogu .kgu model. for example can cxontain a metadata
		// file without the correct @koogu@ field. In this instance the model is loaded
		// properly because of the file extension but would not have been loaded by
		// checking the metadata as above
		for (DLClassiferModel model: dlControl.getDLModels()) {
			if (model.isModelType(modelURI)) return model; 
		}

		//return the generic model as default
		return dlControl.getDLModels().get(dlControl.getDLModels().size()-1); 
	}

}
