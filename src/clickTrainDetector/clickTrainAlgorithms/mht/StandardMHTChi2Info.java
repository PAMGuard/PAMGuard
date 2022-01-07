package clickTrainDetector.clickTrainAlgorithms.mht;

import java.util.ArrayList;

import PamguardMVC.PamDataUnit;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfoLogging;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTChi2ProviderManager.MHTChi2Type;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.MHTChi2Var;

/**
 * Some extra info on the chi2 algorithms
 */
public class StandardMHTChi2Info extends MHTAlgorithmInfo {

	/**
	 * String representation. 
	 */
	private String mhtStringInfo; 


	/**
	 * Standard logging for JSON strings. 
	 */
	private StandardMHTChi2InfoJSON standardMHTChi2InfoJSON = new StandardMHTChi2InfoJSON();

	/**
	 * MHT chi2 names. 
	 */
	private String[] mhtChi2Names;


	/**
	 * MHT chi2 values
	 */
	private double[] mhtChi2Chi2Vals;


	public StandardMHTChi2Info(ArrayList<MHTChi2Var<PamDataUnit>> mhtChi2Vars, StandardMHTChi2Params standardMHTChi2Params) {
		super(MHTChi2Type.STANDARD_MHT_CALC); 
		//do not create a reference as don;t want MHTChi2 vars to stay in memory. 
		//this.mhtChi2Vars=mhtChi2Vars; The info class might be passed through to 
		//click train data units. 

		int numVals=0; 
		for (int i=0; i< standardMHTChi2Params.enable.length; i++) {
			if (standardMHTChi2Params.enable[i]) numVals++;
		}

		double[] mhtChi2Chi2Vals= new double[numVals];
		String[] mhtChi2Names= new String[numVals];

		int count=0; 
		for (int i=0; i<standardMHTChi2Params.enable.length ; i++) {
			if (standardMHTChi2Params.enable[i]) {
				mhtChi2Chi2Vals[count] = mhtChi2Vars.get(i).getChi2(); 
				mhtChi2Names[count] = mhtChi2Vars.get(i).getName(); //not that 
				count++;
			}
		}

		this.mhtChi2Names=mhtChi2Names;
		this.mhtChi2Chi2Vals=mhtChi2Chi2Vals; 

		mhtStringInfo= generateStringInfo(mhtChi2Names, mhtChi2Chi2Vals); 

	}

	public StandardMHTChi2Info(String[] mhtChi2Names,double[] mhtChi2Chi2Vals) {
		super(MHTChi2Type.STANDARD_MHT_CALC); 
		//do not create a reference as don;t want MHTChi2 vars to stay in memory. 
		//this.mhtChi2Vars=mhtChi2Vars; The info class might be passed through to 
		//click train data units. 
		this.mhtChi2Names=mhtChi2Names;
		this.mhtChi2Chi2Vals=mhtChi2Chi2Vals; 

		mhtStringInfo= generateStringInfo(mhtChi2Names, mhtChi2Chi2Vals); 
	}

	/**
	 * Generate extra string information. 
	 * @param mhtChi2Vars - the MHTCHi2Vars
	 */
	private String generateStringInfo(String[] mhtChi2Names, double[] mhtChi2Chi2Vals) {
		String mhtStringInfo = "";  
		for (int i=0; i<mhtChi2Names.length; i++) {
			mhtStringInfo += String.format("%s X\u00b2: %.3f <p>", mhtChi2Names[i], mhtChi2Chi2Vals[i]);
		}
		return mhtStringInfo; 
	}


	public String[] getMhtChi2Names() {
		return mhtChi2Names;
	}

	public double[] getMhtChi2Chi2Vals() {
		return mhtChi2Chi2Vals;
	}

	@Override
	public String getInfoString() {
		return mhtStringInfo; 
	}

	@Override
	public CTAlgorithmInfoLogging getCTAlgorithmLogging() {
		return standardMHTChi2InfoJSON;
	}

	@Override
	public String getAlgorithmType() {
		return MHTClickTrainAlgorithm.MHT_NAME;
	}

}
