package classifier;

import weka.classifiers.trees.RandomForest;

public class WekaRandomForest extends AbstractWekaClassifier {

	public WekaRandomForest() {
		super();
		RandomForest randomForest = new RandomForest();
		setWekaClassifier(randomForest);
        String[] options = new String[2];
        options[0] = "-I";
        options[1] = "10";
        try {
			randomForest.setOptions(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getClassifierName() {
		return "Random Forrest";
	}

}
