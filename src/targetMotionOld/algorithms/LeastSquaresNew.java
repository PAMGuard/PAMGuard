package targetMotionOld.algorithms;

import java.awt.Color;

import Localiser.LocaliserPane;
import Localiser.algorithms.genericLocaliser.leastSquares.LeastSquares;
import Localiser.detectionGroupLocaliser.DetectionGroupLocaliser;
import Localiser.detectionGroupLocaliser.DetectionGroupLocaliser2;
import Localiser.detectionGroupLocaliser.DetectionGroupOptions;
import Localiser.detectionGroupLocaliser.GroupDetection;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamDetection.LocContents;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;
import targetMotionOld.TargetMotionLocaliser;

/**
 * Implmentation of the least squares locaiser.  
 * @author Jamie Macaulay
 *
 * @param <T>
 */
public class LeastSquaresNew<T extends GroupDetection> extends AbstractTargetMotionModel<T> {
	
	/**
	 * The localiser. 
	 */
	public DetectionGroupLocaliser2<T> detectionGroupLocaliser;
	
	public LeastSquaresNew(TargetMotionLocaliser<T> targetMotionLocaliser) {
		detectionGroupLocaliser= new DetectionGroupLocaliser2<T>("Least Squares");
		detectionGroupLocaliser.setLocalisationAlgorithm(new LeastSquares());
	}

	@Override
	public String getName() {
		return "Least squares";
	}

	@Override
	public String getToolTipText() {
		return "The least squares algortihm is very fast but currently only supports 2D localisation and calculates basic errors";
	}

	@Override
	public GroupLocalisation runModel(T pamDetection, DetectionGroupOptions detectionGroupOptions, boolean run) {
//		System.out.println("Simpelx2D: New localiser attempting to localise some data units: " 
//	+ pamDetection.getSubDetectionsCount());
		
		GroupLocalisation groupLoc= (GroupLocalisation) detectionGroupLocaliser.runModel(pamDetection, detectionGroupOptions, false); 
		
		//target motion localiser needs to know the model for plot sysmbols etc.
		//Bit of a HACK but a lot of coding to replace?
		if (groupLoc != null) {
			for (int i=0; i<groupLoc.getGroupLocResults().length; i++){
				groupLoc.getGroupLocaResult(i).setModel(this);
			}
		}		
//		GroupLocResult[] oldResults=runModel(pamDetection);
//		for (int i=0; i<oldResults.length; i++){
//			System.out.println("Old least squares distance: "+oldResults[i].getPerpendicularDistance()); 
//		}
		
		
		return groupLoc;
	}

	@Override
	Color getSymbolColour() {
		return Color.BLACK;
	}

	@Override
	public LocContents getLocContents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LocaliserPane<?> getAlgorithmSettingsPane() {
		return detectionGroupLocaliser.getAlgorithmSettingsPane();
	}

	@Override
	public boolean hasParams() {
		return detectionGroupLocaliser.hasParams();
	}

	@Override
	public void notifyModelProgress(double progress) {		
		
	}
	
	/**Old least squares localiser**/
	
	private DetectionGroupLocaliser detectionGroupLocaliserold= new  DetectionGroupLocaliser(null); 

	@Deprecated
	public GroupLocResult[] runModel(T pamDetection) {
		GroupDetection groupDetection;

		int nSub = pamDetection.getSubDetectionsCount();
		if (nSub < 2) {
			return null;
		}
		groupDetection = new GroupDetection<PamDataUnit>(pamDetection.getSubDetection(0));
		for (int i = 1; i < nSub; i++) {
			groupDetection.addSubDetection(pamDetection.getSubDetection(i));
		}
		GroupLocResult[] results = new GroupLocResult[2];
		boolean[] sideOk = new boolean[2];
		LatLong ll;
		sideOk[0] = detectionGroupLocaliserold.localiseDetectionGroup(groupDetection, 1);
		if (sideOk[0]) {
			ll = detectionGroupLocaliserold.getDetectionLatLong();
			results[0] = new GroupLocResult(this, ll, 0, 0);
			results[0].setPerpendicularDistance(detectionGroupLocaliserold.getRange());
			//results[0].setPerpendicularDistanceError(detectionGroupLocaliser.getPerpendicularError());
			results[0].setReferenceHydrophones(pamDetection.getChannelBitmap());
//			results[0].setProbability(detectionGroupLocaliser.)
//			System.out.println(String.format("Fit lat long %d = %s, %s", 0, ll.formatLatitude(), ll.formatLongitude()));
		}
		sideOk[1] = detectionGroupLocaliserold.localiseDetectionGroup(groupDetection, -1);
		if (sideOk[1]) {
			ll = detectionGroupLocaliserold.getDetectionLatLong();
			results[1] = new GroupLocResult(this, ll, 1, 0);
			results[1].setPerpendicularDistance(detectionGroupLocaliserold.getRange());
			//results[1].setPerpendicularDistanceError(detectionGroupLocaliser.getPerpendicularError());
			results[1].setReferenceHydrophones(pamDetection.getChannelBitmap());
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

}
