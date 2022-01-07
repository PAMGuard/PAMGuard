package clickTrainDetector.clickTrainAlgorithms.mht;

import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.BearingChi2VarParams;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.BearingChi2VarParams.BearingJumpDrctn;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.CorrelationChi2Params;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.IDIChi2Params;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.SimpleChi2VarParams;

/**
 * Default parameters for different species for the MHT click train detector. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class DefaultMHTParams {
	
	public enum DefaultMHTSpecies {DOLPHIN, SPERM_WHALE, PORPOISE, PORPOISE_BUZZES}
	
	
	/**
	 * Get the tool-tip for a particular species
	 * @param defaultSpecies - the default species. 
	 * @return description of the species. 
	 */
	public static String getDefaultSpeciesTooltip(DefaultMHTSpecies defaultSpecies) {
		String speciesTooltip = "";
		switch (defaultSpecies) {
		case DOLPHIN:
			speciesTooltip = "Small dolphin species, e.g. Common Dolphin"; 
			break;
		case PORPOISE:
			speciesTooltip = "Click trains from NBHF species such as Harbour or Dalls Porpoise";
			break;
		case PORPOISE_BUZZES:
			speciesTooltip = "Buzzes from NBHF species such as Harbour or Dall Porpoise. \n"
								+ " Buzzes are defined as click trains with a median ICI below 13ms"; 
			break;
		case SPERM_WHALE:
			speciesTooltip = "Click trains from sperm whales"; 
			break;
		default:
			break;
		}
		return speciesTooltip;
	}


	/**
	 * Get the string name for the enum DefaultMHTSpecies 
	 * @param defaultSpecies - the species
	 * @return string name of the species. 
	 */
	public static String getDefaultSpeciesName(DefaultMHTSpecies defaultSpecies) {
		String speciesName = "";
		switch (defaultSpecies) {
		case DOLPHIN:
			speciesName = "Dolphins"; 
			break;
		case PORPOISE:
			speciesName = "Porpoise (NBHF)";
			break;
		case PORPOISE_BUZZES:
			speciesName = "Porpoise buzzes"; 
			break;
		case SPERM_WHALE:
			speciesName = "Sperm Whale"; 
			break;
		default:
			break;
		}
		return speciesName; 
	}


	/**
	 * Get default MHT parameters for a particular default species. 
	 * 
	 * @return the default MHT parameters for giver DefaultMHTSpecies. 
	 */
	public static MHTParams getDefaultMHTParams(DefaultMHTSpecies defaultSpecies) {

		MHTParams mhtParams = new MHTParams();

		StandardMHTChi2Params simpleChi2Params = new StandardMHTChi2Params(); 
		MHTKernelParams mhtKernelParams = new MHTKernelParams(); 


		//create individual settings. 
		Object[] chi2Settings = new Object[4]; 
		
		
		simpleChi2Params.maxICI = 0.5; 
		simpleChi2Params.coastPenalty = 5; 
		simpleChi2Params.newTrackPenalty = 50; 
		simpleChi2Params.newTrackN = 2;
		simpleChi2Params.longTrackExponent = 0.01; 
		simpleChi2Params.lowICIExponent = 0.5; 

		switch (defaultSpecies) {
		case DOLPHIN:

			/********MHT PARAMS*******
			maxICI: 0.5
			newtrackpenalty: 50.0
			coastpenatly: 5.0
			maxICImultiplierxICI: 1.0
			/******X^2 Variables*******
			ICI Error: 0.200 Min Error: 0.00500 
			Amplitude (dB) Error: 30.000 Min Error: 1.00000
			Bearing ° Error: 0.070 Min Error: 0.00349
			Correlation Error: 0.200 Min Error: 0.00100
			/*************************
			N Prune Back: 6
			N Prune Back Start: 12
			Max Coasts: 5
			N Hold: 15
			 ****************************/

			chi2Settings[0]= new IDIChi2Params("ICI", "s", 0.1, 0.00001, SimpleChi2VarParams.SCALE_FACTOR_ICI);
			chi2Settings[1]= new SimpleChi2VarParams("Amplitude", "dB", 30, 0.003, SimpleChi2VarParams.SCALE_FACTOR_AMPLITUDE);
			chi2Settings[2]= new BearingChi2VarParams("Bearing Delta", "\u00b0", Math.toRadians(8),  Math.toRadians(0.0008), 
					SimpleChi2VarParams.SCALE_FACTOR_BEARING); //RADIANS
			chi2Settings[3]= new CorrelationChi2Params("Correlation"); 

			simpleChi2Params.chi2Settings = chi2Settings;

			simpleChi2Params.enable = new boolean[] {true, true, true, false, false, false}; 

			simpleChi2Params.maxICI = 0.5; 
			simpleChi2Params.coastPenalty = 5; 
			simpleChi2Params.newTrackPenalty = 50; 
			simpleChi2Params.coastPenalty = 10; 
			simpleChi2Params.longTrackBonus = 1.0; 
			simpleChi2Params.lowTrackICIBonus = 1.0; 

			//the MHT kernel params
			mhtKernelParams.maxCoast = 7; 
			mhtKernelParams.nHold = 40; 
			mhtKernelParams.nPruneBackStart = 15;
			mhtKernelParams.nPruneback = 7;

			//set the  two main parameter classes. 
			mhtParams.chi2Params = simpleChi2Params; 
			mhtParams.mhtKernal = mhtKernelParams; 


			break;
		case PORPOISE:

			chi2Settings[0]= new IDIChi2Params("ICI", "s", 0.2, 0.0002, SimpleChi2VarParams.SCALE_FACTOR_ICI);
			chi2Settings[1]= new SimpleChi2VarParams("Amplitude", "dB", 30, 1, SimpleChi2VarParams.SCALE_FACTOR_AMPLITUDE);
			chi2Settings[2]= new BearingChi2VarParams("Bearing Delta", "\u00b0", Math.toRadians(0.2),  Math.toRadians(0.01), 
					SimpleChi2VarParams.SCALE_FACTOR_BEARING); //RADIANS
			chi2Settings[3]= new CorrelationChi2Params("Correlation"); 

			simpleChi2Params.chi2Settings = chi2Settings;

			simpleChi2Params.enable = new boolean[] {true, true, true, false, false, false}; 



			//the MHT kernel params
			mhtKernelParams.maxCoast = 7; 
			mhtKernelParams.nHold = 40; 
			mhtKernelParams.nPruneBackStart = 15;
			mhtKernelParams.nPruneback = 7;

			//set the  two main parameter classes. 
			mhtParams.chi2Params = simpleChi2Params; 
			mhtParams.mhtKernal = mhtKernelParams; 

			break;

		case SPERM_WHALE:

			chi2Settings[0]= new IDIChi2Params("ICI", "s", 0.1, 0.005, SimpleChi2VarParams.SCALE_FACTOR_ICI);
			chi2Settings[1]= new SimpleChi2VarParams("Amplitude", "dB", 5, 0.5, SimpleChi2VarParams.SCALE_FACTOR_AMPLITUDE);
			chi2Settings[2]= new BearingChi2VarParams("Bearing Delta", "\u00b0", Math.toRadians(0.2),  Math.toRadians(0.01), 
					SimpleChi2VarParams.SCALE_FACTOR_BEARING); //RADIANS
			((BearingChi2VarParams) chi2Settings[2]).bearingJumpDrctn = BearingJumpDrctn.POSITIVE; 
			((BearingChi2VarParams) chi2Settings[2]).bearingJumpEnable = true;
			((BearingChi2VarParams) chi2Settings[2]).maxBearingJump = Math.toRadians(20); 
			chi2Settings[3]= new CorrelationChi2Params("Correlation"); 

			simpleChi2Params.chi2Settings = chi2Settings;
			simpleChi2Params.useElectricNoiseFilter=false; //sperm whales can be too like electrical noise sometimes. 

			simpleChi2Params.enable = new boolean[] {true, true, true, false, false, false}; 

			simpleChi2Params.maxICI = 1.5; 
			simpleChi2Params.coastPenalty = 5; 
			simpleChi2Params.newTrackPenalty = 50.0; 
			simpleChi2Params.longTrackBonus = 1.0; 
			simpleChi2Params.lowTrackICIBonus = 1.0; 

			//the MHT kernel params
			mhtKernelParams.maxCoast = 7; 
			mhtKernelParams.nHold = 20; 
			mhtKernelParams.nPruneBackStart = 10;
			mhtKernelParams.nPruneback = 6;

			//set the  two main parameter classes. 
			mhtParams.chi2Params = simpleChi2Params; 
			mhtParams.mhtKernal = mhtKernelParams; 

			break;
		default:
			break;
		}

		//do not use electrical noise filter. 
		simpleChi2Params.useElectricNoiseFilter = false; 
		
		return mhtParams; 

	}

}
