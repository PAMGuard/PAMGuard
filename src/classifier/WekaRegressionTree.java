package classifier;

import weka.classifiers.trees.REPTree;

public class WekaRegressionTree extends AbstractWekaClassifier {

	public WekaRegressionTree() {
		setWekaClassifier(new REPTree());
	}

	@Override
	public String getClassifierName() {
		return "Regression Tree";
	}

}
