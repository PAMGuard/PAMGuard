package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

import java.util.ArrayList;
import java.util.BitSet;

import PamguardMVC.PamDataUnit;
import clickTrainDetector.clickTrainAlgorithms.mht.StandardMHTChi2Params;
import clickTrainDetector.layout.mht.IDIChi2Pane;
import clickTrainDetector.layout.mht.MHTVarSettingsPane;

/**
 * Calculate the chi2 value based on slowly changing inter-click-interval. 
 * <p>
 * The ICI value is a slightly special case becuase it's based ont he IDIManager calculations.
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public class IDIChi2 extends SimpleChi2Var {
	
	
	private double lastIDI=-1; 
	
	/**
	 * 
	 */
	public IDIChi2() {
		super(); 
		super.setSimpleChiVarParams(defaultSettings());
	}
	
	private SimpleChi2VarParams defaultSettings() {
		IDIChi2Params simpleChiVarParams = new IDIChi2Params(getName(), getUnits()); 
		//simpleChiVarParams.errLimits= new double[]{0, 2}; 
		simpleChiVarParams.error=0.2; 
		simpleChiVarParams.minError=0.0005; 
		simpleChiVarParams.errorScaleValue =  SimpleChi2VarParams.SCALE_FACTOR_ICI; 
		return simpleChiVarParams; 
	}
	
	@Override
	public void setSimpleChiVarParams(SimpleChi2VarParams params) {
		super.setSimpleChiVarParams(params);
//		//save a reference to params so we don;t have to keep casting. 
//		this.idiParams = (IDIChi2Params)  params; 
	}
	
	@Override
	public MHTVarSettingsPane<SimpleChi2VarParams> getSettingsPane() {
		if (this.settingsPane==null) this.settingsPane= new IDIChi2Pane(getSimpleChiVarParams(), new Millis2SecondsConverter()); 
		settingsPane.setParams(getSimpleChiVarParams());
		return settingsPane;
	}

	public String getName() {
		return "ICI";
	}

	@Override
	public double calcChi2(ArrayList<PamDataUnit> mhtDataUnits, IDIManager iciManager) {
		
		//the ICI is a special case because it's entirely time based		
		//printICISeries(iciSeries
		double lastICIChi2=iciManager.calcTime(mhtDataUnits.get(0), mhtDataUnits.get(1)); ; 
		double iciChi2=0; 
		double newICIChi2; 
		for (int i=2; i<mhtDataUnits.size(); i++) {
			newICIChi2=iciManager.calcTime(mhtDataUnits.get(i-1), mhtDataUnits.get(i));
			
			iciChi2+=calcICIChi2(lastICIChi2, newICIChi2);
			lastICIChi2=newICIChi2; 
		
		}
		
		iciChi2 = iciChi2/(mhtDataUnits.size()-2);
				
		//if (getVerbosity()>0) System.out.println("ICI Chi2: "+ String.format("%.3f", iciChi2)); 
		
		return iciChi2;
	}
	
	
	@Override
	public double updateChi2(PamDataUnit newdataUnit, BitSet bitSet, int bitcount, int kcount, IDIManager idiManager) {
		//have to do a slightly different calculation to update chi2 vales. 
		if (Double.isNaN(getChi2())) setChi2(0); 
		
		//do nothing if the data unit is not included in the track
		if (!bitSet.get(kcount-1)) {
			return getChi2()/bitcount; 
		}

		//first, do we have chi2 values 
		if (getLastDataUnit()==null || kcount<=1 || bitcount<2) {
			setLastDataUnit(newdataUnit); 
			setChi2(0.0);  
			return getChi2(); 
		}
		
		//have last data unit so can calculate chi2. 
		double newIDI = idiManager.calcTime(getLastDataUnit(), newdataUnit); 
		
		//do we have a last chi2 value
		if (lastIDI==-1) {
			setLastDataUnit(newdataUnit);
			lastIDI=newIDI; 
			return getChi2()/bitcount; 
		}
		

		//check whether the the current detection is in the track
		setChi2(getChi2()+ calcICIChi2(lastIDI, newIDI)); 
//		System.out.println("ICI chi2: " + String.format("%.3f ICI 1 %.4f ICI 2  %.4f val2 %.9f val1 %.9f", 
//				calcICIChi2(lastIDI, newIDI), lastIDI , newIDI, Math.pow((newIDI - lastIDI),2), 
//				Math.pow(Math.max(lastIDI*getSimpleChiVarParams().error, getMinCutValue()), 2))); 
		setLastDataUnit(newdataUnit); 
		lastIDI=newIDI; 
		

		//return the chi2 value divided by the number of data units. 
		return getChi2()/bitcount;
	}
	
	/**
	 * Calculate the chi2 value for two IDI measurements. 
	 * @return the chi2 value for two IDI measurements 
	 */
	private double calcICIChi2(double ici1, double ici2) {
		double ici; 
		if (ici2<((IDIChi2Params) this.getSimpleChiVarParams()).minIDI) return ici = StandardMHTChi2Params.JUNK_TRACK_PENALTY; 

		ici = (Math.pow((ici2 - ici1),2)/
				Math.pow(Math.max(ici1*getSimpleChiVarParams().error, getMinCutValue()), 2)); 

		return ici; 

	}
	
	@Override
	public double getDiffValue(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1) {
		// Not used
		return -1;
	}


	@Override
	public double getErrValue(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1) {
		return super.getSimpleChiVarParams().error;
	}


	@Override
	public String getUnits() {
		return "ms"; //Note that stored as seconds. 
	}
	
	@Override
	public void clear() {
		super.clear();
		lastIDI=-1;
	}
	
	/**
	 * Show IDI values as milliseconds in controls. 
	 * @author Jamie Macaulay
	 *
	 */
	public class Millis2SecondsConverter extends ResultConverter {
		
		/**
		 * Convert the value to the value to be shown in controls
		 * @param value
		 * @return
		 */
		public double convert2Control(double value) {
			return value*1000.; 
		}
		
		/**
		 * Convert the control value to the true value. 
		 * @param value - value form control
		 * @return
		 */
		public double convert2Value(double value) {
			return value/1000.; 
		}
		
	}

	


	

}