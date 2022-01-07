//package targetMotionOld.algorithms.old;
//
//import java.awt.Color;
//
//import targetMotionOld.TargetMotionLocaliser;
//import targetMotionOld.algorithms.AbstractTargetMotionModel;
//import Localiser.LocaliserPane;
//import Localiser.detectionGroupLocaliser.DetectionGroupLocaliser;
//import Localiser.detectionGroupLocaliser.GroupDetection;
//import Localiser.detectionGroupLocaliser.GroupLocResult;
//import PamDetection.AbstractLocalisation;
//import PamDetection.LocContents;
//import PamDetection.PamDetection;
//import PamUtils.LatLong;
//
///**
// * Least squares localisation for Target motion analysis. 
// * Basically a wrapper around older least sq method developed for real time tracking. <p> 
// * Reinstated Target motion add-in as used by the click detector. Hope one day still to replace this
// * with Jamie's new one, but keep this one until Jamie's is working. 
// * @author Doug Gillespie 
// *
// * @param <T>
// */
//public class LeastSquares<T extends PamDetection> extends AbstractTargetMotionModel<T> {
//
//	private DetectionGroupLocaliser detectionGroupLocaliser;
//
//	public LeastSquares(TargetMotionLocaliser<T> targetMotionLocaliser) {
//		detectionGroupLocaliser = new DetectionGroupLocaliser(null);
//	}
//
//	@Override
//	public String getName() {
//		return "Least Squares";
//	}
//
//	@Override
//	public GroupLocResult[] runModel(T pamDetection) {
//		GroupDetection groupDetection;
//
//		int nSub = pamDetection.getSubDetectionsCount();
//		if (nSub < 2) {
//			return null;
//		}
//		groupDetection = new GroupDetection<PamDetection>(pamDetection.getSubDetection(0));
//		for (int i = 1; i < nSub; i++) {
//			groupDetection.addSubDetection(pamDetection.getSubDetection(i));
//		}
//		GroupLocResult[] results = new GroupLocResult[2];
//		boolean[] sideOk = new boolean[2];
//		LatLong ll;
//		sideOk[0] = detectionGroupLocaliser.localiseDetectionGroup(groupDetection, 1);
//		if (sideOk[0]) {
//			ll = detectionGroupLocaliser.getDetectionLatLong();
//			results[0] = new GroupLocResult(this, ll, 0, 0);
//			results[0].setPerpendicularDistance(detectionGroupLocaliser.getRange());
//			//results[0].setPerpendicularDistanceError(detectionGroupLocaliser.getPerpendicularError());
//			results[0].setReferenceHydrophones(pamDetection.getChannelBitmap());
////			results[0].setProbability(detectionGroupLocaliser.)
////			System.out.println(String.format("Fit lat long %d = %s, %s", 0, ll.formatLatitude(), ll.formatLongitude()));
//		}
//		sideOk[1] = detectionGroupLocaliser.localiseDetectionGroup(groupDetection, -1);
//		if (sideOk[1]) {
//			ll = detectionGroupLocaliser.getDetectionLatLong();
//			results[1] = new GroupLocResult(this, ll, 1, 0);
//			results[1].setPerpendicularDistance(detectionGroupLocaliser.getRange());
//			//results[1].setPerpendicularDistanceError(detectionGroupLocaliser.getPerpendicularError());
//			results[1].setReferenceHydrophones(pamDetection.getChannelBitmap());
////			System.out.println(String.format("Fit lat long %d = %s, %s", 1, ll.formatLatitude(), ll.formatLongitude()));
//		}
//		if (sideOk[0] == false && sideOk[1] == false) {
//			return null;
//		}
//		//		for (int i = 0; i < 2; i++) {
//		//			LatLong ll = detectionGroupLocaliser.getDetectionLatLong();
//		//			System.out.println(String.format("Fit lat long %d = %s, %s", i, ll.formatLatitude(), ll.formatLongitude()));
//		//		}
//		return results;
//	}
//
//	@Override
//	public String getToolTipText() {
//		return "<html>Least squares approximation - assumes vessel track is a straight line</html>";
//	}
//
//
//	@Override
//	public LocaliserPane<?> getSettingsPane() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public boolean hasParams() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//
//	@Override
//	public AbstractLocalisation runModel(T pamDataUnit, boolean addLoc) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void notifyModelProgress(double progress) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public LocContents getLocContents() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//
//
//	
//	
//
//	
//	
//	
///**public TransformGroup getPlotSymbol3D(Vector3f vector, Double[] sizeVector, double minSize){
//	
//	TransformGroup trg=new TransformGroup();
//
//	Transform3D posStretch=new Transform3D();
//	Appearance app=new Appearance();
//	
//	for (int i=0; i<sizeVector.length;i++){
//		if (sizeVector[i]<minSize){
//			sizeVector[i]=minSize;
//		}
//	}
//	
//	ColoringAttributes colour=new ColoringAttributes();
//	colour.setColor(new Color3f(0f,0f,0f));
//	app.setColoringAttributes(colour);
//
//	Sphere sphr=new Sphere(5f);
//	sphr.setAppearance(app);
//	trg.addChild(sphr);
//	
//	posStretch.setTranslation(vector);
//	posStretch.setScale(new Vector3d(sizeVector[0],sizeVector[1],sizeVector[2] ));
//	
//	trg.setTransform(posStretch);
//		
//		return trg;
//
//	};**/
//
//}
