package rawDeepLearningClassifier.logging;

import java.util.ArrayList;

import annotation.DataAnnotation;
import rawDeepLearningClassifier.dlClassification.PredictionResult;

/**
 * Deep learning results annotation. 
 * @author Jamie Macaulay. 
 *
 */
public class DLAnnotation extends DataAnnotation<DLAnnotationType> {

	/**
	 * The results of the DL model. 
	 */
	private ArrayList<PredictionResult> modelResults;

	public DLAnnotation(DLAnnotationType dlAnnotationType, ArrayList<PredictionResult> modelResults) {
		super(dlAnnotationType);
		//		System.out.println("DLAnnotation: " + modelResults.size()); 
		this.modelResults = modelResults; 
		//add to annotations. 
	}

	/**
	 * Get all the model results. 
	 * @return the model results. 
	 */
	public ArrayList<PredictionResult> getModelResults() {
		return modelResults;
	}


	@Override
	public String toString() {

		String results = "<html>"; 

		if (modelResults==null) {
			results += "WARNING: There are no model results associated with this data unit?"; 
		}
		else {
			for (int j=0; j<this.modelResults.get(0).getPrediction().length; j++) {
				results += "<p>"; 

				//System.out.println("Number class names: " + this.modelResults.get(0).getClassNames().length);

				if (this.modelResults.get(0).getClassNames()!=null && this.modelResults.get(0).getClassNames().length>j) {
					short ID = this.modelResults.get(0).getClassNames()[j];
					
//					System.out.println("Class ID: " + ID);

					String name = this.getDataAnnotationType().getDlControl().getClassNameManager().getClassName(ID);
					//System.out.println("Class ID: " + ID + " name: " + name);					
					if (name!=null) {
						results+= name + ": "; 
					}
					else {
						results += "Class " + j + ": "; 
					}
				}
				else {
					results += "Class " + j + ": "; 
				}

				//now grab all the probabilitiy segments for this class
				for (int i=0; i<this.modelResults.size(); i++) {

					if (i<this.modelResults.size()-1) {
						results += String.format(" %.2f,", modelResults.get(i).getPrediction()[j]);
					}
					else {
						//remove comma
						results += String.format(" %.2f", modelResults.get(i).getPrediction()[j]);
					}
				}
				//results += "</p>"; 
			}
		}

		//results += "</html>"; 

		return results;
	}

}
