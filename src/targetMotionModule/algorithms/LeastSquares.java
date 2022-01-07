package targetMotionModule.algorithms;

import java.awt.Color;

import targetMotionModule.TargetMotionInformation;
import targetMotionModule.TargetMotionLocaliser;
import targetMotionModule.TargetMotionResult;
import Localiser.detectionGroupLocaliser.DetectionGroupLocaliser;
import Localiser.detectionGroupLocaliser.GroupDetection;
import PamDetection.PamDetection;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;

/**
 * Least squares localisation for Target motion analysis. 
 * Basically a wrapper around older least sq method developed for real time tracking. 
 * @author Doug Gillespie
 *
 */
public class LeastSquares extends AbstractTargetMotionModel {

	private DetectionGroupLocaliser detectionGroupLocaliser;

	public LeastSquares(TargetMotionLocaliser targetMotionLocaliser) {
		detectionGroupLocaliser = new DetectionGroupLocaliser(null);
	}

	@Override
	public String getName() {
		return "Least Squares";
	}

	@Override
	public boolean hasParameters() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean parametersDialog() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TargetMotionResult[] runModel(TargetMotionInformation targetMotionInformation) {
		GroupDetection groupDetection;

		int nSub = targetMotionInformation.getNDetections();
		if (nSub < 2) {
			return null;
		}
		groupDetection = new GroupDetection<PamDataUnit>(targetMotionInformation.getCurrentDetections().get(0));
		for (int i = 1; i < nSub; i++) {
			groupDetection.addSubDetection(targetMotionInformation.getCurrentDetections().get(i));
		}
		TargetMotionResult[] results = new TargetMotionResult[2];
		boolean[] sideOk = new boolean[2];
		LatLong ll;
		sideOk[0] = detectionGroupLocaliser.localiseDetectionGroup(groupDetection, 1);
		if (sideOk[0]) {
			ll = detectionGroupLocaliser.getDetectionLatLong();
			results[0] = new TargetMotionResult(targetMotionInformation.getTimeMillis(), this, ll, 0, 0);
			results[0].setPerpendicularDistance(detectionGroupLocaliser.getRange());
			results[0].setPerpendicularDistanceError(detectionGroupLocaliser.getPerpendicularError());
			results[0].setReferenceHydrophones(targetMotionInformation.getCurrentDetections().get(0).getChannelBitmap());
//			results[0].setProbability(detectionGroupLocaliser.)
//			System.out.println(String.format("Fit lat long %d = %s, %s", 0, ll.formatLatitude(), ll.formatLongitude()));
		}
		sideOk[1] = detectionGroupLocaliser.localiseDetectionGroup(groupDetection, -1);
		if (sideOk[1]) {
			ll = detectionGroupLocaliser.getDetectionLatLong();
			results[1] = new TargetMotionResult(targetMotionInformation.getTimeMillis(), this, ll, 1, 0);
			results[1].setPerpendicularDistance(detectionGroupLocaliser.getRange());
			results[1].setPerpendicularDistanceError(detectionGroupLocaliser.getPerpendicularError());
			results[1].setReferenceHydrophones(targetMotionInformation.getCurrentDetections().get(0).getChannelBitmap());
//			System.out.println(String.format("Fit lat long %d = %s, %s", 1, ll.formatLatitude(), ll.formatLongitude()));
		}
		if (sideOk[0] == false && sideOk[1] == false) {
			return null;
		}
		//		for (int i = 0; i < 2; i++) {
		//			LatLong ll = detectionGroupLocaliser.getDetectionLatLong();
		//			System.out.println(String.format("Fit lat long %d = %s, %s", i, ll.formatLatitude(), ll.formatLongitude()));
		//		}
		return results;
	}

	@Override
	public String getToolTipText() {
		return "<html>Least squares approximation - assumes vessel track is a straight line</html>";
	}

	private Color symbolColour = new Color(0, 0, 0);

	@Override
	Color getSymbolColour() {
		return symbolColour;
	}

	

}
