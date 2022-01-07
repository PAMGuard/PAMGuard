package rawDeepLearningClassifier.dlClassification.dummyClassifier;

import rawDeepLearningClassifier.dlClassification.PredictionResult;

public class DummyModelResult implements PredictionResult {
	
	
	private float[] probability;

	public DummyModelResult(float probability) {
		this.probability = new float[] {probability};  
	}
	
	public DummyModelResult(float[] probability) {
		this.probability = probability;  
	}



	@Override
	public float[] getPrediction() {
		return probability;
	}

	@Override
	public boolean isBinaryClassification() {
		return probability[0]>0.95;
	}

	@Override
	public double getAnalysisTime() {
		return 0.0001;
	}

	@Override
	public String getResultString() {
		return "Dummy result: " + probability;
	}

	@Override
	public short[] getClassNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTimeMillis() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}