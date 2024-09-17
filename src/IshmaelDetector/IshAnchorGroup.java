package IshmaelDetector;

import PamDetection.PamDetection;
import PamguardMVC.superdet.SuperDetection;

/**
 * Brings together groups of IshDetWrappers
 * @author Doug
 *
 */
public class IshAnchorGroup extends SuperDetection<IshDetWrapper> implements PamDetection {

	IshDetWrapper anchor;
	
	public IshAnchorGroup(long timeMilliseconds, int channelBitmap, long startSample, long duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
		// TODO Auto-generated constructor stub
		
		/*
		 * One all, or enough, IshDetWrappers have been added, create a PamLocation object (or a 
		 * subclass thereof) and fill in the relevent inforamtion. Shouldnot be necessary to have a 
		 * separate IshLocation class since the Location informatin will be held within the detection
		 * 
		 */
	}

	/**
	 * @return Returns the anchor.
	 */
	public IshDetWrapper getAnchor() {
		return anchor;
	}

	/**
	 * @param anchor The anchor to set.
	 */
	public void setAnchor(IshDetWrapper anchor) {
		this.anchor = anchor;
	}

}
