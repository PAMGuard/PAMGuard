package IshmaelLocator;

import Array.ArrayManager;
import PamDetection.AbstractLocalisation;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;

/** Whenever one of the IshLoc routines calculates a location
 * (bearing, X-Y position, etc.) it generates an IshLocation,
 * which is then passed to the rest of PAMGUARD via
 * IshLocProcess.outputDataBlock. 
 * @author Dave Mellinger
 */
public class IshLocalisation extends AbstractLocalisation {
	PamDataUnit pamDetection;  //a copy of AbstractLoc.pamDataUnit as a PamDetection
	
	//This is set for all bearing types.
	public long timeMsec;		//time the sound occurred
	
	//These are defined for ISHLOC_BEARING_1 and ISHLOC_BEARING_2.  Angle
	//here is 0 along positive X-axis, increasing anticlockwise, radians.
	public double theta0;		//defined for both BEARING_1 and _2
	public double theta1;		//defined only for BEARING_2
	public double theta0deg, theta1deg;		//same thing in degrees

	//These are defined for ISHLOC_POSITION_2 and ISHLOC_POSITION_3.
	public double x, y;			//defined for both POSITION_2 and _3
	public double z;			//defined only for POSITION_3
	
	/**
	 * Localisation data appended to Ishmael Detections. 
	 * @param pamDetection - IshDetection data unit containing acoustic data for localising
	 * @param locContents - Bitmap of localisation contents
	 * @param referencePhones - Bitmap of hydrophones used for localisation
	 */
	IshLocalisation(PamDataUnit pamDetection, int locContents, 
			int referencePhones) 
	{
		super(pamDetection, locContents, referencePhones);
		this.pamDetection = pamDetection;
	}

	
	@Override
	public LatLong getLatLong(int iSide) {
		//LatLong ll = pamDetection.getOriginLatLong(false);
		LatLong ll = ArrayManager.getArrayManager().getCurrentArray().getHydrophoneLocator().getStreamerLatLong(timeMsec);
		return ll.addDistanceMeters(x, y);
	}
	
	
}
