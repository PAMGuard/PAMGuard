package IshmaelLocator;

import java.util.*;

import IshmaelDetector.IshDetection;


/** This has information for a single IshDetection that indicates (1) whether
 * the detection is an 'anchor' detection within this channel, i.e., whether
 * it is the principal one of a group of multipath detections, and (2)
 * whether the detection is an anchor detection across channels, i.e.,
 * whether it is the anchor detection on a specially-designated reference
 * channel.
 * 
 * @author Aaron Thode and Dave Mellinger
 */
public class IshDetectionWrapper {
	IshDetection det;
	boolean isChanAnchor;		//is this a reference call WITHIN THIS channel?
	boolean isArrayAnchor;		//is this a reference call ACROSS ALL channels?
	double timeSinceAnchor;		//time since most recent chanAnchor
	double timeSinceArrayAnchor;//time since most recent arrayAnchor
	HashMap<String,Object> feature;
	
	public IshDetectionWrapper(IshDetection det) {
		this.det = det;
		feature = new HashMap<String,Object>();
	}
}
