package clickTrainDetector.clickTrainAlgorithms.mht;

import java.io.Serializable;
import java.util.ArrayList;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamguardMVC.PamDataUnit;
import clickTrainDetector.clickTrainAlgorithms.mht.electricalNoiseFilter.SimpleElectricalNoiseParams;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.MHTChi2Var;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.SimpleChi2VarParams;

/**
 * Parameters for the PamMHTChi2 class.
 * <p>
 * Note on penalty factors. Once the chi^2 is calculated based on the MHT track
 * variables, then the track is given a series of penalties. A penalty in the
 * variable name means that the higher the value the lower the higher the chi^2.
 * A bonus means the higher the value the lower the chi^2 value,
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public class StandardMHTChi2Params extends MHTChi2Params  implements Serializable, ManagedParameters {

	/**
	 * 
	 */
	private static final long serialVersionUID = 12L;

	/****General*****/

	/**
	 * True to use cross correlation to calculate ICI values. 
	 */
	public boolean useCorrelation = false;


	/****Track penalty and bonus factors *****/

	/**
	 * The penalty factor for a single coast.
	 */
	public double coastPenalty=10;

	/**
	 * The penalty factor for starting a new track. 
	 */
	public double newTrackPenalty=50;

	/***
	 * Exponent for the long track bonus - no point in using a multiplier because then
	 * just multiplying all tracks by the same number. Thus the higher this is the more 
	 * the more weight is given to longer tracks. Make zero to remove bonus
	 */
	public double longTrackExponent = 0.1;

	/***
	 * Exponent for the low ICI track bonus - no point in using a multiplier because then
	 * just multiplying all tracks by the same number. Thus the higher this is the more 
	 * the more weight is given to loweer ICI tracks. Make zero to remove bonus. 
	 */
	public double lowICIExponent = 0.1;


	/**
	 * The bonus factor for having a longer track. 
	 */
	@Deprecated
	public double longTrackBonus=1;

	/**
	 * Bonus factor for a track with lower ICI. 
	 */
	@Deprecated
	public double lowTrackICIBonus=1;


	/**
	 * The maximum multiple of the median ICI of a track that maximum ICI of the track can be. 
	 * Deprecated. No longer used. 
	 */
	@Deprecated
	public double maxICIMultipler = 1; 

	/**
	 * The maximum number of detections after which a track is no longer considered a new track. 
	 */
	public int newTrackN=3; //this is an important parameter for stopping harmonic tracks

	/**
	 * Penalty which is added when a track is considered junk (i.e. with a median
	 * IDI value well over the maximum allowed) but not yet tagged for removal from
	 * the probability mix. Essentially this is designed to put the track to the
	 * back of the queue.
	 */
	public static double JUNK_TRACK_PENALTY = 20000000;

	/***************Track Descriptor Variables**********************/

	/**
	 *Simply stores bespoke settings class for mhtChi2Vars; Only used because 
	 *ArrayList<MHTChi2Var<PamDataUnit>> is not serializable. 
	 */
	public Object[] chi2Settings; 

	/**
	 * List of enabled params. 
	 */
	public boolean[] enable;


	/****Electrical Noise Filter*****/

	/**
	 * True to add a test as to whether data is too consistent, i.e. likely to be electrical noise.. 
	 */
	public boolean useElectricNoiseFilter = false;

	/**
	 * Simple electrical noise parameters. 
	 */
	public SimpleElectricalNoiseParams electricalNoiseParams = new SimpleElectricalNoiseParams(); 


	public StandardMHTChi2Params() {
		createMH2Chi2VarSettings(StandardMHTChi2.createChi2Vars()); 
	}

	public StandardMHTChi2Params(ArrayList<MHTChi2Var<PamDataUnit>> arrayList) {
		createMH2Chi2VarSettings(arrayList); 
	}


	/**
	 * Create settings objects for the chi2 variables. These are important for serilaizing the settings. 
	 * @param mhtChi2Vars - the chi2varibales.
	 */
	private void createMH2Chi2VarSettings(ArrayList<MHTChi2Var<PamDataUnit>> mhtChi2Vars) {

		//which settings are enabled
		enable=new boolean[mhtChi2Vars.size()];
		for (int i=0; i<enable.length; i++) {
			enable[i]=true;
		}

		//chi^2 settings object
		chi2Settings= new Object[mhtChi2Vars.size()];

		//set the chi^2 settings. 
		for (int i=0; i<this.chi2Settings.length; i++) {
			chi2Settings[i]=mhtChi2Vars.get(i).getSettingsObject(); 
		}
	}



	@Override
	public StandardMHTChi2Params clone() {
		//StandardMHTChi2Params(StandardMHTChi2Params) super.clone();
		return (StandardMHTChi2Params) super.clone();
	}

	@Override
	public void restoreSettings() {

		//		if (mhtChi2Vars==null) {
		//			mhtChi2Vars = new ArrayList<MHTChi2Var<PamDataUnit>>(); 
		//			/******Add new chi2 vars here*******/
		//			mhtChi2Vars.add(new ICIChi2()); 
		//			mhtChi2Vars.add(new AmplitudeChi2()); 
		//			mhtChi2Vars.add(new BearingChi2()); 
		//			mhtChi2Vars.add(new CorrelationChi2()); 
		//			/**********************************/
		//
		////			//which settings are enabled
		////			enable=new boolean[mhtChi2Vars.size()];
		//
		//			//chi^2 settings
		//			for (int i=0; i<this.chi2Settings.length; i++) {
		//				mhtChi2Vars.get(i).setSettingsObject(chi2Settings[i]); 
		//			}
		//		}
	}

	/**
	 * Restore settings of chi2 vars from the saved settings chi2settings objects. 
	 * @param mhtChi2Vars - the chi2 vars to restore settings for. 
	 */
	public void restoreSettings(ArrayList<MHTChi2Var<PamDataUnit>> mhtChi2Vars) {
		
		
		//only used if a new mhtchi2var has been added and and old settings file  has been loaded. 
		if (this.chi2Settings.length!=mhtChi2Vars.size()) {
			Object[] chi2Settings = new Object[mhtChi2Vars.size()]; 
			for (int i=0; i<chi2Settings.length; i++) {
				if (i<this.chi2Settings.length) {
					chi2Settings[i]=this.chi2Settings[i]; 
				}
				else {
					chi2Settings[i]=mhtChi2Vars.get(i).getSettingsObject(); 
				}
			}
			this.chi2Settings=chi2Settings; 
		}


		//set the chi2 var settings
		for (int i=0; i<this.chi2Settings.length; i++) {
			mhtChi2Vars.get(i).setSettingsObject(chi2Settings[i]); 
		}
		
		
		//only used if a new mhtchi2var has been added and and old settings file has been loaded. 
		if (this.enable.length!=mhtChi2Vars.size()) {
			boolean[] enable = new boolean[mhtChi2Vars.size()]; 
			for (int i=0; i<enable.length; i++) {
				if (i<this.enable.length) {
					enable[i]=this.enable[i]; 
				}
			}
			this.enable=enable; 
		}
		
		//System.out.println("RESTORE SETTINGS: " + enable.length + "  " + chi2Settings.length); 
	}

	/**
	 * Print the class types for each of MHT variable settings.  
	 */
	public void printMHTVarClass() {
		SimpleChi2VarParams mhtVarParams; 
		for (int i=0; i<chi2Settings.length; i++) {
			mhtVarParams = ((SimpleChi2VarParams) chi2Settings[i]);
			System.out.println(mhtVarParams.getClass());
		}
	}


	/**
	 * Print the settings for the params class. 
	 */
	public void printSettings() {

		System.out.println("maxICI: " + this.maxICI);
		System.out.println("newtrackpenalty: " + this.newTrackPenalty);
		System.out.println("coastpenatly: " + this.coastPenalty);
		System.out.println("longtrackExponent: " + this.longTrackExponent);
		System.out.println("lowtrackICIExponent: " + this.lowICIExponent);
		System.out.println("junktrackpenalty: " + StandardMHTChi2Params.JUNK_TRACK_PENALTY);
		System.out.println("ntrack: " + this.newTrackN);

		System.out.println("/******X^2 Variables*******/");
		SimpleChi2VarParams mhtVarParams; 
		for (int i=0; i<chi2Settings.length; i++) {
			mhtVarParams = ((SimpleChi2VarParams) chi2Settings[i]);
			System.out.println(mhtVarParams.toString() + " Enabled: " + enable[i] );
		}

		System.out.println("/******Electrical Noise filter*******/");
		System.out.println("Electrical Noise Filter Enabled: " + this.useElectricNoiseFilter );
		System.out.println("minChi2: " + this.electricalNoiseParams.minChi2);
		System.out.println("ndataunits: " + this.electricalNoiseParams.nDataUnits);

		System.out.println("/*************************/");

	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}


}
