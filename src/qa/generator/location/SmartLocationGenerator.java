package qa.generator.location;

import PamUtils.LatLong;
import PamguardMVC.debug.Debug;
import Stats.LogRegWeka;
import qa.ClusterParameters;
import qa.QAControl;
import qa.QATestDataUnit;
import qa.generator.clusters.QACluster;
import qa.generator.sequence.SoundSequence;

/**
 * Smart location tester which processes previous sequences to see how many hits
 * they got and then dynamically tries to chose a sensible place for the next set.
 * @author dg50
 *
 */
public class SmartLocationGenerator extends RandomLocationGenerator {

	/** The fraction of sounds in a sequence that need to be detected in order for the sequence itself to be considered 'detected' */
	private static final double successfulDetection = 0.5;
	
	/** The probability of sequence detection to shoot for */
	private static final double desiredProbOfDetection = 0.5;
	
	/** flag indicating at least one of the sequences was successfully detected */
	private boolean someSuccess = false;
	
	/** flag indicating at least one of the sequences was not successfully detected */
	private boolean someFailure = false;

	private double initialRange;
	private double currentRange;
	private int currentTest;
	private int nLocations;

	public SmartLocationGenerator(QAControl qaControl, QACluster qaCluster, int nLocations, double[] rangeLimits) {
		super(qaControl, qaCluster, nLocations, rangeLimits);	
		this.initialRange = this.currentRange = getNominalRange();
		this.nLocations = nLocations;
		this.currentTest = 0;
	}

	@Override
	public LatLong getNextLocation(LatLong currentReference, SoundSequence previousSequence) {
		if (currentTest == nLocations) return null;
		currentRange = getNextRange(previousSequence);
		currentTest++;
		System.out.printf("Run next test %d of %d at range %3.0fm\n", currentTest, nLocations, currentRange);
		LatLong sourcePosition = currentReference.travelDistanceMeters(Math.random()*360., currentRange);
		setLocationHeight(sourcePosition);
//		if (sourceDepth != null) {
//			sourcePosition.setHeight(-sourceDepth);
//		}
		return sourcePosition;
	}
	
	private double getNextRange(SoundSequence previousSequence) {
		// if this is the first sequence, start with the initial range
		if (previousSequence == null) {
			return initialRange;
		}
		
		// Try to determine the new range using Logistic Regression only if:
		//		1) We have done at least 5 sequences
		//		2) Some of the sequences have been successfully detected
		//		3) Some of the sequences have not been successfully detected
		QATestDataUnit thisTest = previousSequence.getQaTestSet().getTestDataUnit();
		int nSeq = thisTest.getSubDetectionsCount();
		if (nSeq>5 && someSuccess && someFailure) {
			try {
				currentRange = getRangeFromAllSequences(previousSequence);
				return currentRange;
			} catch (Exception ex) {
				Debug.out.println("Error trying to determine new range with Logistic Regression");
			}
		}
		
		// if we get this far, it means one of the parameters in the If statement above is not true,
		// or the classifier failed.  Instead, use a simpler method to calculate the next range
		double lastF = getFractionDetected(previousSequence);
		Debug.out.printf(" %3.1f%% of sounds in sequence detected ", lastF*100.);
		// if it's approx a successfulDetection, then just make a random jump somewhere !
		if (lastF > (successfulDetection-0.05) && lastF < (successfulDetection+0.05)) {
			currentRange *= Math.pow(2., (Math.random()*2-1));
		}
		else {	
			double scale = (lastF-successfulDetection) * 2;
			currentRange *= Math.pow(2., scale);
		}

		// set the flags accordingly
		if (lastF > successfulDetection) {
			someSuccess = true;
			System.out.print(" (Success) ");
		} else {
			someFailure = true;
			System.out.print(" (Failed) ");
		}
		
		System.out.println("Setting new range to " + currentRange);
		return currentRange;
	}
	
	/**
	 * Return the fraction of QASounds detected in the previous Sequence
	 * @param soundSequence
	 * @return
	 */
	private double getFractionDetected(SoundSequence soundSequence) {
		return soundSequence.getSequenceDataUnit().getFractionDetected();
//		double tot = 0;
//		QASequenceDataUnit seqDU = soundSequence.getSequenceDataUnit();
//		synchronized (seqDU) {
//			int nS = seqDU.getSubDetectionsCount();
//			for (int i = 0; i < nS; i++) {
//				QASoundDataUnit aSound = seqDU.getSubDetection(i);
//				int nDets = aSound.getNumDetectorHits();
//				if (nDets > 0) {
//					tot += 1;
//				}
//			}
//			return tot/nS;
//		}
	}
	
	
	/**
	 * Use logistic regression to model the results of the previous sequences, and come up
	 * with a new range that has a probability of detection of desiredProbOfDetection.  Note
	 * that we can only train a classifier when we have both successful detections and unsuccessful
	 * detections.
	 * 
	 * @param soundSequence the previous sound sequence
	 * @return the new range
	 */
	private double getRangeFromAllSequences(SoundSequence soundSequence) {
		QATestDataUnit thisTest = soundSequence.getQaTestSet().getTestDataUnit();
		int nSeq = thisTest.getSubDetectionsCount();
		double[][] trainData = new double[nSeq][1];	// training data must be a 2d array, even if there is only 1 attribute
		double[] resultData = new double[nSeq];
		for (int i=0; i<nSeq; i++) {
			trainData[i][0] = thisTest.getSubDetection(i).getDistanceToHydrophone();
			resultData[i] = (thisTest.getSubDetection(i).getFractionDetected() > successfulDetection ? 1 : 0);
			double lastF = thisTest.getSubDetection(i).getFractionDetected();
			System.out.printf("\n%3.1f%% of sounds in sequence detected for range of %3.1f", lastF*100.,trainData[i][0]);

		}
		
		// create a logistic regression classifier, train it and then query it for an appropriate range
		LogRegWeka logRegClassifier = new LogRegWeka();
		logRegClassifier.setTrainingData(trainData, resultData);
		double range;
		try {
			range = logRegClassifier.getAttFromProb(desiredProbOfDetection);
		} catch (ArithmeticException ex) {
			range = 10.0;
		}
		range = Math.max(range, 10.0);
		System.out.println("Setting new range to " + range);
		return range;
	}

	@Override
	public boolean isFinished() {
		return (currentTest >= nLocations);
	}

}
