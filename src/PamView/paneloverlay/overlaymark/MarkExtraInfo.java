package PamView.paneloverlay.overlaymark;

/**
 * Extra information for OverlayMarks which can be provided from 
 * the mark source. This can only be provided by the actual display
 * component (Swing or FX) that get's clicked on to make the mark
 * since it has to be pulled out of the source object in the mouse
 * event. 
 * @author dg50
 *
 */
public class MarkExtraInfo {
	
	private Integer channelMap;

	/**
	 * @return the channelMap
	 */
	public Integer getChannelMap() {
		return channelMap;
	}

	/**
	 * @param channelMap the channelMap to set
	 */
	public void setChannelMap(Integer channelMap) {
		this.channelMap = channelMap;
	}
	
}
