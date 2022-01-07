package classifier;

//import weka.classifiers.bayes.BayesNet;
//import weka.classifiers.functions.LinearRegression;


/**
 * Class to serve up classifier types to the Whistle classifier
 * @author Doug Gillespie
 *
 */
public class ClassifierTypes {
	
	static public final int CLASSIFIER_LINEAR = 0;
	
	static public final int CLASSIFIER_MAHALANOBIS = 1;
	
	static public final int CLASSIFIER_RANDOMFORREST = 2;

	static public final int CLASSIFIER_REGRESSIONTREE = 3;

	
	public static int getNumClassifiers() {
		return 4;
	}
	
//	private LinearClassifier linearClassifier;
//	private MahalanobisClassifier mahalanobisClassifier;
//	private WekaRandomForest wekaRandomForest;
//	private WekaRegressionTree wekaRegressionTree;
	
	public static Classifier createClassifier(int type) {
		switch (type) {
		case CLASSIFIER_LINEAR:
			return new LinearClassifier();
		case CLASSIFIER_MAHALANOBIS:
			return new MahalanobisClassifier();
		case CLASSIFIER_RANDOMFORREST:
			return new WekaRandomForest();
		case CLASSIFIER_REGRESSIONTREE:
			return new WekaRegressionTree();
		}
		return null;
	}
	
	public static String getClassifierName(int type) {
		switch (type) {
		case CLASSIFIER_LINEAR:
			return "Linear Discriminant Analysis";
		case CLASSIFIER_MAHALANOBIS:
			return "Mahalanobis Distances Classifier";		
		case CLASSIFIER_RANDOMFORREST:
			return "Random Forrest";
		case CLASSIFIER_REGRESSIONTREE:
			return "Regression Tree";
		}
		return null;		
	}

	
}
