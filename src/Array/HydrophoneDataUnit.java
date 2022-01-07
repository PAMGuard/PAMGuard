package Array;

import PamguardMVC.PamDataUnit;

public class HydrophoneDataUnit extends PamDataUnit {
	
	 private Hydrophone hydrophones; 

	public HydrophoneDataUnit(Hydrophone hydrophone) {
		super(hydrophone.getTimeMillis());
		this.hydrophones=hydrophone;
	}
	
	public HydrophoneDataUnit(long timeNow, Hydrophone h) {
		super(timeNow);
		this.hydrophones = h.clone();
	}

	public Hydrophone getHydrophone(){
		return hydrophones;
	}
	
	public void setHydrophones(Hydrophone hydrophone){
		this.hydrophones=hydrophone;
	}
	
	public void setHydrophoneErrors(double[] errors){
		if (hydrophones!=null) hydrophones.setCoordinateErrors(errors);
	}


	public Double getTimeMillis() {
		return (double) super.getTimeMilliseconds();
	}


}
