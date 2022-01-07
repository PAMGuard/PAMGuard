package whistleClassifier;

import classifier.Classifier;
import classifier.LinearClassifier;
import classifier.MahalanobisClassifier;

/**
 * Class to serve up classifier types to the Whistle classifier
 * @author Doug Gillespie
 *
 */
public class ClassifierTypes {
	
	static public final int CLASSIFIER_LINEAR = 0;
	
	static public final int CLASSIFIER_MAHALANOBIS = 1;
	
	public static int getNumClassifiers() {
		return 2;
	}
	
	public static Classifier createClassifier(int type) {
		switch (type) {
		case CLASSIFIER_LINEAR:
			return new LinearClassifier();
		case CLASSIFIER_MAHALANOBIS:
			return new MahalanobisClassifier();			
		}
		return null;
	}
	
	public static String getClassifierName(int type) {
		switch (type) {
		case CLASSIFIER_LINEAR:
			return "Linear Discriminant Analysis";
		case CLASSIFIER_MAHALANOBIS:
			return "Mahalanobis Distances Classifier";	
		}
		return null;		
	}

	
}
