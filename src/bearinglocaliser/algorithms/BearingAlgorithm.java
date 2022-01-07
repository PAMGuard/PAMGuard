package bearinglocaliser.algorithms;


import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import bearinglocaliser.BearingAlgorithmGroup;
import bearinglocaliser.display.BearingDataDisplay;

public interface BearingAlgorithm {

	public void setParams(BearingAlgorithmParams bearingAlgoParams);
	
	 public BearingAlgorithmParams getParams();
	 
	 public boolean prepare();
	 
	 public boolean process(PamDataUnit pamDataUnit, double sampleRate, BearingAlgorithmGroup bearingAlgoGroup);

	 public long getRequiredDataHistory(PamObservable o, Object arg);

	public BearingDataDisplay createDataDisplay();
}
