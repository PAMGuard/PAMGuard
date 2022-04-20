package cpod;

import PamguardMVC.PamProcess;

public class CPODProcess extends PamProcess {

	public CPODProcess(CPODControl2 pamControlledUnit) {
		super(pamControlledUnit, null);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public float getSampleRate() {
		return CPODClickDataBlock.CPOD_SR;
	}

}
