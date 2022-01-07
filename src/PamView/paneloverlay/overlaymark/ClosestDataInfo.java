package PamView.paneloverlay.overlaymark;

import PamguardMVC.PamDataUnit;

public class ClosestDataInfo {

	public PamDataUnit dataUnit;
	
	public double distance;

	public ClosestDataInfo(PamDataUnit dataUnit, double distance) {
		super();
		this.dataUnit = dataUnit;
		this.distance = distance;
	}
}
