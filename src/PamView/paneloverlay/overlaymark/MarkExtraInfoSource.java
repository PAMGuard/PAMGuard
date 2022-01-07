package PamView.paneloverlay.overlaymark;

public interface MarkExtraInfoSource {

	/**
	 * @return extra information about the mark being made from 
	 * the panel (FX or Swing) that is making it. 
	 */
	public MarkExtraInfo getExtraInfo();
	
}
