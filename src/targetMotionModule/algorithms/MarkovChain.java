package targetMotionModule.algorithms;


import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;

import javax.vecmath.Point3f;

import pamMaths.PamVector;
import targetMotionModule.TargetMotionInformation;
import targetMotionModule.TargetMotionLocaliser;
import targetMotionModule.TargetMotionResult;
import Array.ArrayManager;
import GPS.GpsData;
import Localiser.algorithms.genericLocaliser.MCMC.MCMC;
import Localiser.algorithms.genericLocaliser.MCMC.old.MCMCLocaliser;
import Localiser.algorithms.genericLocaliser.MCMC.old.MCMCParams;
import Localiser.algorithms.genericLocaliser.MCMC.old.MCMCParamsDialog;
import Localiser.algorithms.genericLocaliser.MCMC.old.MCMCTDResults;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamguardMVC.debug.Debug;
import PamUtils.LatLong;


/*This is a specific MCMC localiser designed to produce a probability distribution of the likely location of a cetecean using TARGET MOTION TECHNIQUES only.
 For each localisation the output will be a likely large array of x y z Co-Ordinates. Together these form a probability distribution. This array of points  can then be processed to give 
 results and corresponding errors.**/
/** the idea is to visually represent each localisation three dimensional space to allow easy manual analysis of the likely location of a cetecean location.
 * @author Jamie Macaulay**/
public class MarkovChain  extends AbstractTargetMotionModel implements PamSettings {
	
	
	private TargetMotionLocaliser targetMotionLocaliser;
	
	//Array info
	
	private ArrayManager arrayManager;
	
	private double speedOfSound;
		
	private ArrayList<ArrayList<Point3f>> hydrophoneArray;

	ArrayList<PamVector> arrayError;
		
	//MCMC
	private MCMCParams settings=new MCMCParams();
	
	private MCMCLocaliser mCMC=new MCMCLocaliser(settings);
	
	//Time Delays
	
	float sampleRate;
	
	double timeError;

	private ArrayList<ArrayList<Double>> timeDelays;
	
	private ArrayList<ArrayList<Double>> timeDelaysErrors;
	
	//MCMC Results
			
	ArrayList<MCMCTDResults> finalResults;
	
	
	public MarkovChain(TargetMotionLocaliser targetMotionLocaliser) {
		this.targetMotionLocaliser = targetMotionLocaliser;
		arrayManager = ArrayManager.getArrayManager();
		PamSettingManager.getInstance().registerSettings(this);
	}


	/**
	 * Run the MCMC localisation algorithm for target motion analysis.
	 */
	@Override
	public TargetMotionResult[] runModel(TargetMotionInformation targetMotionInformation) {
		
		
		// If there is no data in pamDetection then  don't attempt to localise. 
		if (targetMotionInformation == null) return null;
		if (targetMotionInformation.getCurrentDetections().size() <=0) return null;
		
		
		timeDelays=targetMotionInformation.getTimeDelays();
		timeDelaysErrors=targetMotionInformation.getTimeDelayErrors();
		hydrophoneArray=targetMotionInformation.getHydrophonePos();
		sampleRate=targetMotionInformation.getCurrentDetections().get(0).getParentDataBlock().getSampleRate();
		speedOfSound=arrayManager.getCurrentArray().getSpeedOfSound();
				
		Debug.out.println("timeDelays: "+timeDelays);
		Debug.out.println("hydrophoneArray: "+hydrophoneArray);
		Debug.out.println("sampleRate: "+sampleRate);
		Debug.out.println("timeDelaysErrors: "+timeDelaysErrors);
		Debug.out.println("speedOfSound: "+speedOfSound);
		
		mCMC.setTimeDelays(timeDelays);
		mCMC.setHydrophonePos(hydrophoneArray);
		mCMC.setSampleRate(sampleRate);
		mCMC.setTimeDelaysErrors(timeDelaysErrors);
		mCMC.setSoundSpeed(speedOfSound);
		
		mCMC.runAlgorithm();
			
		this.finalResults=mCMC.getResults();//TODO
		
		if (finalResults==null) return null;

		TargetMotionResult[] tmResults = new TargetMotionResult[finalResults.size()];

		for (int i=0; i<finalResults.size(); i++){
			
		LatLong ll = targetMotionInformation.metresToLatLong(new PamVector(finalResults.get(i).getLocation().getX(),finalResults.get(i).getLocation().getY(),finalResults.get(i).getLocation().getZ()));
		
		tmResults[i] = new TargetMotionResult(targetMotionInformation.getTimeMillis(), this, ll, 0, finalResults.get(i).getChi());
		tmResults[i].setErrorX(finalResults.get(i).getErrors().getX());
		tmResults[i].setErrorY(finalResults.get(i).getErrors().getY());
		tmResults[i].setErrorZ(finalResults.get(i).getErrors().getZ());
		GpsData beamPos=targetMotionInformation.getBeamLatLong(ll);
		tmResults[i].setBeamLatLong(new GpsData(beamPos));
		tmResults[i].setBeamTime(targetMotionInformation.getBeamTime(beamPos));
		tmResults[i].setStartLatLong(targetMotionInformation.getCurrentDetections().get(0).getOriginLatLong(false));
		tmResults[i].setEndLatLong(targetMotionInformation.getCurrentDetections().get(targetMotionInformation.getCurrentDetections().size()-1).getOriginLatLong(false));
		tmResults[i].setPerpendicularDistance(ll.distanceToMetres(tmResults[i].getBeamLatLong()));
		tmResults[i].setPerpendicularDistanceError(finalResults.get(i).getRangeError());
		tmResults[i].setChi2(finalResults.get(i).getChi());
		tmResults[i].setMCMCJumps(finalResults.get(i).getChainJumps());
		tmResults[i].setBeamLatLong(new GpsData(targetMotionInformation.getBeamLatLong(ll)));
		tmResults[i].setReferenceHydrophones(targetMotionInformation.getReferenceHydrophones());
		
		}
		
		return tmResults;
	}
	
	
	@Override
	public String getName()		 	{
		return "MCMC localisation";
	}

	@Override
	public String getToolTipText() 	{
		return "<html>MCMC</html>";
	}

	@Override
	public boolean hasParameters() 	{
		return true;
	}

	@Override
	public boolean parametersDialog(){
		//MarkovChainDialogue.showDialog(null, settings);
		MCMCParamsDialog.showDialog(null, mCMC.getSettings());
		return true;
	}
	
	
	private PamSymbol symbol = new PamSymbol(PamSymbolType.SYMBOL_TRIANGLEU, 9, 9, true, Color.BLACK, Color.RED);

	@Override
	public PamSymbol getPlotSymbol(int iResult) {
		return symbol;
	}

	@Override
	Color getSymbolColour() {
		return null;
	}

	@Override
	public Serializable getSettingsReference() {
		return mCMC.getSettings();
	}

	@Override
	public long getSettingsVersion() {
		return MCMCParams.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		return targetMotionLocaliser.getLocaliserName();
	}

	@Override
	public String getUnitType() {
		return "MCMC Parameters";
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		settings= ((MCMCParams) pamControlledUnitSettings.getSettings()).clone();
    	return false;
	}
	
}
