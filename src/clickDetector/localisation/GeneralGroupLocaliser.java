package clickDetector.localisation;

import java.util.ArrayList;

import Localiser.LocaliserModel;
import Localiser.LocaliserPane;
import Localiser.algorithms.genericLocaliser.leastSquares.LeastSquares;
import Localiser.algorithms.genericLocaliser.simplex.Simplex;
import Localiser.detectionGroupLocaliser.DetectionGroupLocaliser2;
import Localiser.detectionGroupLocaliser.DetectionGroupOptions;
import Localiser.detectionGroupLocaliser.GroupDetection;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamController.PamController;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import clickDetector.ClickDetection;
import warnings.PamWarning;
import warnings.WarningSystem;

abstract public class GeneralGroupLocaliser implements LocaliserModel<PamDataUnit> {

	private PamWarning locWarning;
	
	/**
	 * List of possible localiser (do not serialise)
	 */
	public transient ArrayList<LocaliserModel> locAlgorithmList;

	public GeneralGroupLocaliser() {
		//show a warning in PAMGuard. 
		generateLocList();
	}

	abstract public String getName();
	
	abstract public ClickLocParams getClickLocParams();
	
	/**
	 * Generate the list of available localisers. 
	 */
	public void generateLocList(){
		
		locAlgorithmList=new ArrayList<LocaliserModel>(); 
		//these are the default localisers
		//ADD NEW LOCLAISERS HERE//
		//least squares algorithm 
		locAlgorithmList.add(new DetectionGroupLocaliser2<GroupDetection<ClickDetection>>("Least Squares", new LeastSquares(), DetectionGroupLocaliser2.BEARINGS_GROUP, 2 )); 
		//2D simplex localiser
		locAlgorithmList.add(new DetectionGroupLocaliser2<GroupDetection<ClickDetection>>("2D Simplex Optimization", new Simplex(), DetectionGroupLocaliser2.BEARINGS_GROUP, 2 )); 
		//3D simplex localiser 
		locAlgorithmList.add(new DetectionGroupLocaliser2<GroupDetection<ClickDetection>>("3D Simplex Optimization", new Simplex(), DetectionGroupLocaliser2.BEARINGS_GROUP, 3 )); 
		
		//only add MCMC in viewer mode as way too processor intensive for real time operation
		if (PamController.getInstance().getRunMode()==PamController.RUN_PAMVIEW){
			//MCMC localiser
			locAlgorithmList.add(new DetectionGroupLocaliser2<GroupDetection<ClickDetection>>("MCMC", new Simplex(), DetectionGroupLocaliser2.TIMEDELAY_GROUP, 3 )); 
		}

//		ClickLocParams clickLocParams=getClickLocParams(); 
//		if (clickLocParams.isSelected == null || clickLocParams.isSelected.length != locAlgorithmList.size()) {
//			clickLocParams.isSelected=new boolean[locAlgorithmList.size()];
//			clickLocParams.isSelected[0]=true; //enable 2D simplex by default; 
//		}
	}
	
	/**
	 * Create the loc warning. 
	 * @return the loc warning. 
	 */
	private PamWarning generateWarning(){
		if (locWarning == null) {
			locWarning = new PamWarning(getName(), "localisation error", 2);
			locWarning.setWarningTip("Use a less processor intensive algortihm e.g. least squares or set the max time in the click loclaisation settings to a higher number.");
		}
		return locWarning;
	}

	@Override
	public String getToolTipText() {
		return "Loclaises groups of clicks";
	}


	@Override
	public LocaliserPane<?> getAlgorithmSettingsPane() {
		// no settings pane for this. Settings come from click control params 
		//and so are never called here. 
		return null;
	}
	
	/**
	 * Find an algorithm by name
	 * @param algorithmName
	 * @return
	 */
	public LocaliserModel findLocaliserAlgorithm(String algorithmName) {
		if (algorithmName == null) {
			return null;
		}
		for (LocaliserModel locAlgo:locAlgorithmList) {
			if (algorithmName.equals(locAlgo.getName())) {
				return locAlgo;
			}
		}
		return null;
	}

	@Override
	public boolean hasParams() {
		return false;
	}

	@Override
	public void notifyModelProgress(double progress) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Run the click group localiser. 
	 * @param pamDataUnit - the group detection to localise. 
	 * @return the best fit GroupLocalisation. 
	 */
	public GroupLocalisation runModel(PamDataUnit  pamDataUnit, DetectionGroupOptions detectionGroupOptions){
		 return runModel( pamDataUnit, detectionGroupOptions, true);
	}
	
	/**
	 * Filter results before localisation is selected based on AIC value. If the localisation contains multiple ambiguities, then it 
	 * will pass as long as one of the ambiguous results passes the filter. 
	 * @param groupLocalisation - the localisation result to test. 
	 * @return true if localisation passes the filter tests. 
	 */
	public boolean resultsFilterOK(GroupLocalisation groupLocalisation){
		ClickLocParams clickLocParams=getClickLocParams(); 
		GroupLocResult[] results = groupLocalisation.getGroupLocResults();
		if (results == null) {
			return false;
		}
		for (int j=0; j<results.length; j++){
			if (groupLocalisation.getGroupLocaResult(j).getPerpendicularDistance()<clickLocParams.maxRange &&
					groupLocalisation.getHeight(j)>clickLocParams.minHeight && groupLocalisation.getHeight(j)<clickLocParams.maxHeight){
				//even if one ambiguity is OK then the whole localisation passes the filter. 
				return true; 
			}
		}
		return false;
	}
	
	/**
	 * Create a warning message for localisation algorithms taking over the maximum set time to process. 
	 * @param clickLocParams - the localisation params for this localiser.
	 * @param timetaken  - the time taken to localise in millis.  
	 * @return a warning message. 
	 */
	private String createWarningMessage(ClickLocParams clickLocParams, long timetaken){
		//show a warning in PAMGuard. 
		String warning=""; 
		for (int i=0; i<locAlgorithmList.size(); i++){
			if (clickLocParams.getIsSelected(i)) warning+=locAlgorithmList.get(i).getName();
			if (locAlgorithmList.size()!=1){
				if (i==locAlgorithmList.size()-1) warning+=" and ";
				else warning+=", ";
			}
		}
		if (locAlgorithmList.size()!=1) warning+="algorithm has";
		else warning+="algorithms have ";
		warning+=(" taken "+ String.format("%d", timetaken) +"ms to process");
		
		return warning; 
	}
	
	@Override
	public GroupLocalisation runModel(PamDataUnit  pamDataUnit, DetectionGroupOptions detectionGroupOptions, boolean addLoc) {
		ClickLocParams clickLocParams=getClickLocParams(); 		

//		Debug.out.println("GroupLocalisation: Localising:  "+ locAlgorithmList.size());

		
		if (locAlgorithmList==null) generateLocList(); //this should never be called but might be if serialization issue. 
		if (locAlgorithmList.size()==0) return null; 
		
		GroupLocalisation groupLoc; 
		ArrayList<GroupLocalisation> locResults=new ArrayList<GroupLocalisation>(); 
		long time0=System.currentTimeMillis();
		for (int i=0; i<locAlgorithmList.size(); i++){
			if (clickLocParams.getIsSelected(i)){
				//notify localisation progress; 
//				Debug.out.println("ClickGroupLocaliser: Localising:  "+ locAlgorithmList.get(i).getName());
				notifyModelProgress(((double) i)/locAlgorithmList.size()); 
				//add to a list of results - note: do not add the new localisation to the data unit...yet.
				LocaliserModel algo = locAlgorithmList.get(i);
				AbstractLocalisation newLoc = algo.runModel(pamDataUnit, detectionGroupOptions, false);
				groupLoc= ((GroupLocalisation) newLoc);
				if (groupLoc!=null) locResults.add(groupLoc); 
			}
		}
		long time1=System.currentTimeMillis();

		long timeTaken=time1-time0; 
		
		//show a warning. 
//		Debug.out.println("ClickGroupLocaliser: time taken: "+ timeTaken + " max time: :" +clickLocParams.maxTime);
		generateWarning();
		if (timeTaken>clickLocParams.maxTime) {
			//show a warning in PAMGuard. 
			String warning= createWarningMessage(clickLocParams, timeTaken);
			locWarning.setWarningMessage(warning);
			locWarning.setEndOfLife(PamCalendar.getTimeInMillis() + 5*1000 );
			WarningSystem.getWarningSystem().addWarning(locWarning);
		}
		else WarningSystem.getWarningSystem().removeWarning(locWarning);
		
		//now work out which localisation is best based on chi2 value
		//can add some more test here later. 
		double minAic=Double.MAX_VALUE; 
		int minIndex=-1; 
		for (int i=0; i<locResults.size(); i++){
			if (resultsFilterOK(locResults.get(i))){
				for (int j=0; j<locResults.get(i).getGroupLocResults().length; j++){
					double aic=locResults.get(i).getGroupLocaResult(j).getAic(); 
					if (aic<minAic){
						minAic=aic;
						minIndex=i;
					}
				}
			}
		}
		
		if (minIndex==-1){
			//something has gone wrong
//			System.err.println(getName() +": No loc results");
			locWarning.setWarningMessage(getName() +": No localisation results");
			WarningSystem.getWarningSystem().addWarning(locWarning);
			return null; 
		}
		
		if (addLoc) pamDataUnit.setLocalisation(locResults.get(minIndex));
		
		return locResults.get(minIndex);
	}

	@Override
	public LocContents getLocContents() {
		// TODO Auto-generated method stub
		return null;
	}

}
