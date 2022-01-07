package clickTrainDetector.clickTrainAlgorithms.mht.electricalNoiseFilter;

import clickTrainDetector.clickTrainAlgorithms.mht.StandardMHTChi2;
import clickTrainDetector.clickTrainAlgorithms.mht.TrackBitSet;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.MHTChi2Var;

/**
 * 
 * A simple electrical noise check which checks how low the chi2 value is. 
 * 
 * @author Jamie Macaulay
 *
 */
public class SimpleElectricalNoiseFilter implements ElectricalNoiseFilter {

	/*
	 * Reference to the StandardMHTChi2 this is associated with
	 */
	private StandardMHTChi2 standardMHTChi2;
	

	public SimpleElectricalNoiseFilter(StandardMHTChi2 standardMHTChi2) {
		this.standardMHTChi2=standardMHTChi2; 
	}

	@Override
	public double addChi2Penalties(double chi2, TrackBitSet<?> bitSet, int bitCount, int kcount, int nPruneback) {
		if (bitCount < getParams().nDataUnits) return chi2; 

		for (int i=0; i<this.standardMHTChi2.getMhtChi2Vars().size(); i++) {
			if (calcErrIndchi2(standardMHTChi2.getMhtChi2Vars().get(i), bitCount)<
					getParams().minChi2 && standardMHTChi2.getChi2Provider().getParams().enable[i]) {
				//System.out.println(String.format("This track is electrical NOISE!!!!!: %.10f  bitcount: %d i: %d", standardMHTChi2.getMhtChi2Vars().get(i).getChi2() ,bitCount , i));
				bitSet.flag=TrackBitSet.JUNK_TRACK;
				return chi2;
			}
		}
		return chi2;
	}
	
	/**
	 * Calculate an error independent chi2 value. This helps determine how uniform data is
	 * @param mhtChi2Var - the chi2 variable 
	 * @param bitcount - the number of detections. 
	 * @return the error independent chi" value. 
	 */
	private double calcErrIndchi2(MHTChi2Var mhtChi2Var, int bitcount) {
		return mhtChi2Var.getChi2()*Math.pow(mhtChi2Var.getError(),2)/bitcount; 
	}
	
	/**
	 * Get the electrical noise parameters. 
	 * @return the parameters
	 */
	private SimpleElectricalNoiseParams getParams() {
		return standardMHTChi2.getChi2Provider().getParams().electricalNoiseParams; 
	}

}
