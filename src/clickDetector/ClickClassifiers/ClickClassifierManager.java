package clickDetector.ClickClassifiers;

import clickDetector.ClickControl;
import clickDetector.ClickClassifiers.basic.BasicClickIdentifier;
import clickDetector.ClickClassifiers.basicSweep.SweepClassifier;

public class ClickClassifierManager {

//	static public final int CLASSIFY_NONE = 0;
	static public final int CLASSIFY_BASIC = 0;
	static public final int CLASSIFY_BETTER = 1;
//	static public final int CLASSIFY_MULTIVARIATE = 2;
	
	private ClickControl clickControl;
	
	ClickIdentifier basicIdentifier, sweepIdentifier;
	
//	private ClickIdentifier currentIdentifier; 
	
	public ClickClassifierManager(ClickControl clickControl) {
		this.clickControl = clickControl;
		basicIdentifier = new BasicClickIdentifier(clickControl);
		sweepIdentifier = new SweepClassifier(clickControl);
	}
	
	public int getNumClassifiers() {
		return 3;
	}
	
	public ClickIdentifier getClassifier(int type) {
		switch(type) {
//		case CLASSIFY_NONE:
//			return new NullClassifier();
		case CLASSIFY_BASIC:
			return basicIdentifier;
		case CLASSIFY_BETTER:
			return sweepIdentifier;
		}
		return new NullClassifier();
	}
	
	public String getClassifierName(int type) {
		switch(type) {
//		case CLASSIFY_NONE:
//			return "none";
		case CLASSIFY_BASIC:
			return "Basic Click Classifier";
		case CLASSIFY_BETTER:
			return "Classifier with frequency sweep";
		}
		return "None";
	}
	
	public int getClassifierIndex(ClickIdentifier clickClassifier) {
		if (clickClassifier == null) {
			return CLASSIFY_BASIC;
		}
//		if (clickClassifier.getClass() == NullClassifier.class) {
//			return CLASSIFY_NONE;		
//		}
		if (clickClassifier.getClass() == BasicClickIdentifier.class) {
			return CLASSIFY_BASIC;		
		}
		else if (clickClassifier.getClass() == SweepClassifier.class) {
			return CLASSIFY_BETTER;		
		}
		return CLASSIFY_BASIC;
	}
	
}
