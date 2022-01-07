//package targetMotionOld.algorithms.old;
//
//
//import java.awt.Color;
//import java.io.Serializable;
//import java.util.ArrayList;
//
//import javax.vecmath.Point3d;
//import javax.vecmath.Point3f;
//
//import clickDetector.ClickDetection;
//import clickDetector.ClickDetectionMatch;
//import pamMaths.PamVector;
//import targetMotionOld.AbstractTimeDelayLocaliser;
//import targetMotionOld.TargetMotionLocaliser;
//import Localiser.algorithms.genericLocaliser.MCMC.MCMC;
//import Localiser.algorithms.genericLocaliser.MCMC.old.MCMCLocaliser;
//import Localiser.algorithms.genericLocaliser.MCMC.old.MCMCParams;
//import Localiser.algorithms.genericLocaliser.MCMC.old.MCMCParamsDialog;
//import Localiser.algorithms.genericLocaliser.MCMC.old.MCMCTDResults;
//import Localiser.detectionGroupLocaliser.GroupDetection;
//import Localiser.detectionGroupLocaliser.GroupLocResult;
//import PamController.PamControlledUnitSettings;
//import PamController.PamSettingManager;
//import PamController.PamSettings;
//import PamDetection.PamDetection;
//import PamView.PamSymbol;
//import PamUtils.LatLong;
//
//
///****/
///** the idea is to visually represent each localisation three dimensional space to allow easy manual analysis of the likely location of a cetecean location.
// * @author Jamie Macaulay**/
//
///**
// *  this is a specific MCMC localiser designed to produce a probability distribution of the likely location of a cetecean using TARGET MOTION TECHNIQUES only.
// For each localisation the output will be a likely large array of x y z Co-Ordinates. Together these form a probability distribution. This array of points  can then be processed to give 
// results and corresponding errors. <p>
// the idea is to visually represent each localisation three dimensional space to allow easy manual analysis of the likely location of a cetecean location.
// * 
// * Reinstated Target motion add-in as used by the click detector. Hope one day still to replace this
// * with Jamie's new one, but keep this one until Jamie's is working. 
// * @author Jamie Macaulay
// *
// * @param <T>
// */
//public class MarkovChain<T extends GroupDetection>  extends AbstractTimeDelayLocaliser<T> implements PamSettings {
//	
//	
//	private TargetMotionLocaliser<T> targetMotionLocaliser;
//	
//
//	//MCMC Parameters
//	
//	private MCMCParams settings=new MCMCParams();
//	private MCMCLocaliser mCMC=new MCMCLocaliser(settings);
//	
//	//protected MCMCParams settings=mCMC.getSettings();
//	
//	ArrayList<Integer> indexM1;
//	
//	ArrayList<Integer> indexM2;
//	
//	//Time Delays
//	
//	double timeError;
//
//		
//	//MCMC Results
//	
//	ArrayList<ArrayList<Point3f>> Jumps;
//		
//	ArrayList<MCMCTDResults> finalResults;
//
//	public MarkovChain(TargetMotionLocaliser<T> targetMotionLocaliser) {
//		this.targetMotionLocaliser = targetMotionLocaliser;
//		PamSettingManager.getInstance().registerSettings(this);
//	}
//
//	
//	
////	public void GenerateTestData(){
////		
////		int nSubDet=3;
////		
////		PamVector[] subDetOrigins=new PamVector[3];
////		subDetOrigins[0]=new PamVector(0.15,0,0);
////		subDetOrigins[1]=new PamVector(29.85,0,0);
////		subDetOrigins[2]=new PamVector(45.15,0,0);
////		
////		PamVector[][] WorldVectors=new PamVector[3][2];
////		WorldVectors[0][0]=new PamVector(29.85,30,0);
////		WorldVectors[0][1]=new PamVector(29.85,-30,0);
////		
////		WorldVectors[1][0]=new PamVector(0.15,30,0);
////		WorldVectors[1][1]=new PamVector(0.15,-30,0);
////		
////		WorldVectors[2][0]=new PamVector(-15.15,30,0);
////		WorldVectors[2][1]=new PamVector(-15.15,-30,0);
////		
////		PamVector[] subDetHead=new PamVector[3];
////		subDetHead[0]=new PamVector(1,0.000001,0);
////		subDetHead[1]=new PamVector(1,0.000001,0);
////		subDetHead[2]=new PamVector(1,0.000001,0);
////		
////		this.nSubDetections = nSubDet;
////		this.detectionOrigins = subDetOrigins;
////		this.worldVectors = WorldVectors;
////		this.detectionHeadings =subDetHead;
////		this.speedOfSound = 1500;
////
////	}
////	
////	public void calcErrors(){
////		
////	}
////	
//	
//	
////	/***
////	 * 
////	 * @param arrayError Seperation Error
////	 * @param timedelay. Time delay  between the two hydrophones
////	 * @param Hseparation. Separation in meters between the two hydrophone elements
////	 * @return
////	 */
////	public Double calcTimeDelayError(PamVector arrayError){
////		getTimeDelayError(int H0, int H1, PamArray currentArray);
////		
////	}
//
//
//	@SuppressWarnings("unchecked")
//	@Override
//	/**
//	 * Run the MCMC localisation algorithm for target motion analysis.
//	 */
//	public GroupLocResult[] runModel(T pamDetection) {
//		
//		// If there is no data in pamDetection then  don't attempt to localise. 
//		if (pamDetection == null) return null;
//		if (pamDetection.getSubDetectionsCount()<2) return null;
//
//		getEventInfo(pamDetection);
//		
//		calcHydrophonePositions();
//		calculateTimeDelays();
//		calculateTimeDelayErrors();
//		
//		System.out.println(timeDelaysAll);
//		System.out.println(timeDelayErrorsAll);
//		System.out.println(hydrophonePos);
//		
//		mCMC.setTimeDelays(timeDelaysAll);
//		mCMC.setHydrophonePos(hydrophonePos);
//		mCMC.setSampleRate(sampleRate);
//		mCMC.setTimeDelaysErrors(timeDelayErrorsAll);
//		mCMC.setSoundSpeed(speedOfSound);
//		mCMC.runAlgorithm();
//		
//		this.finalResults=mCMC.getResults();//TODO
//		
//		if (finalResults==null) return null;
//
//		GroupLocResult[] tmResults = new GroupLocResult[finalResults.size()];
//
//		for (int i=0; i<finalResults.size(); i++){
//			
//		LatLong ll = eventRotator.metresToLatLong(new PamVector(finalResults.get(i).getLocation().getX(),finalResults.get(i).getLocation().getY(),finalResults.get(i).getLocation().getZ()),true);
//		
//		tmResults[i] = new GroupLocResult(this, ll, 0, finalResults.get(i).getChi());
//		tmResults[i].setErrorX(finalResults.get(i).getErrors().getX());
//		tmResults[i].setErrorY(finalResults.get(i).getErrors().getY());
//		tmResults[i].setErrorZ(finalResults.get(i).getErrors().getZ());
//		tmResults[i].setPerpendicularDistance(finalResults.get(i).getRange());
//		tmResults[i].setPerpendicularDistanceError(finalResults.get(i).getRangeError());
//		tmResults[i].setChi2(finalResults.get(i).getChi());
//		tmResults[i].setMCMCJumps(finalResults.get(i).getChainJumps());
//		//tmResults[0].setReferenceHydrophones(eventRotator.getReferenceHydrophones());
//		}
//		
//		return tmResults;
//	}
//	
//	
//	@Override
//	public String getName()		 	{
//		return "MCMC localisation";
//	}
//
//	@Override
//	public String getToolTipText() 	{
//		return "<html>MCMC</html>";
//	}
//
//	@Override
//	public boolean hasParameters() 	{
//		return true;
//	}
//
//	@Override
//	public boolean parametersDialog(){
//		//MarkovChainDialogue.showDialog(null, settings);
//		MCMCParamsDialog.showDialog(null, mCMC.getSettings());
//		return true;
//	}
//	
//	
//	private PamSymbol symbol = new PamSymbol(PamSymbol.SYMBOL_TRIANGLEU, 9, 9, true, Color.BLACK, Color.RED);
//
//	@Override
//	public PamSymbol getPlotSymbol(int iResult) {
//		// TODO Auto-generated method stub
//		return symbol;
//	}
//
//	@Override
//	Color getSymbolColour() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Serializable getSettingsReference() {
//		return mCMC.getSettings();
//	}
//
//	@Override
//	public long getSettingsVersion() {
//		return MCMCParams.serialVersionUID;
//	}
//
//	@Override
//	public String getUnitName() {
//		return targetMotionLocaliser.getLocaliserName();
//	}
//
//	@Override
//	public String getUnitType() {
//		return "MCMC Parameters";
//	}
//
//	@Override
//	public boolean restoreSettings(
//			PamControlledUnitSettings pamControlledUnitSettings) {
//		settings= ((MCMCParams) pamControlledUnitSettings.getSettings()).clone();
//    	return false;
//	}
//
//	
//	
//	
//	
//	
//	
//
//}
