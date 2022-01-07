package Map;

import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;

public class MapComment extends PamDataUnit {
	
	String comment;
	
	LatLong latLong;
	
	public static final int MAXCOMMENTLENGTH = 255;

	public MapComment(long timeMilliseconds, LatLong latLong, String comment) {
		super(timeMilliseconds);
		this.latLong = latLong;
		this.comment = comment;
	}

}
