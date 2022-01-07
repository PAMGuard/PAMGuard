package IshmaelDetector;

import PamDetection.PamDetection;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

/**
 * IshTrack may not need to hold any location information itself - it mainly
 * holds a list of IshAnchorGroups, each of which has a PamLocation object, the
 * contents of which will make up a track. The class may want to hold data such as
 * heading and speed information for an animal, or information that will help 
 * decide if prospective IshAnchorGroups shold be added to this track.  
 * @author Doug
 *
 */
public class IshTrack extends PamDataUnit<IshAnchorGroup, SuperDetection> implements PamDetection {

	public IshTrack(long timeMilliseconds,  int channelBitmap, long startSample, long duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
		// TODO Auto-generated constructor stub
	}

}
