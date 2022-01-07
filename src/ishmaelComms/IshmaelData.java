package ishmaelComms;


public class IshmaelData {
	
	public enum IshmaelDataType {
		NEWPOSITION, NEWBEARING;
	}
	
	// Constants for array size. TODO can actually fix this 
	// now as we can add a makePhonLocs array method
	// to this class.
	int NUMPHONES = 8;
	int PHONEDIM = 3;
	
	protected IshmaelDataType ishmaelDataType; 
	

//	 Common Position/Bearing variables
	

	float start;

	float end;

	float lofreq;

	float hifreq;

	String input;

	// int [][] phonelocs ;
	int numphones;
	int phonedim;
	
	float[][] phonelocs = new float[NUMPHONES][PHONEDIM];

	int quality;

	float speed;

	String method;

	String tag_cert;

	// NewBearing variables

	float angle;

	int ambig;

	int bearoffset;

	// NewPosition variables
	int locdim = 0;

	float xpos = 0;

	float ypos = 0;

	float zpos = 0;

	int posoffset;


}
