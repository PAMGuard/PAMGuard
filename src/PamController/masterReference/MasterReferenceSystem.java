package PamController.masterReference;

import PamUtils.LatLong;

/**
 * System for master references used by various bits of PAMGuard. 
 * 
 * @author Doug Gillespie
 *
 */

public interface MasterReferenceSystem {

	public LatLong getLatLong();
	
	public Long getFixTime();
	
	public Double getCourse();
	
	public Double getHeading();
	
	public String getName();

	public Double getSpeed();

	public String getError();

	public void setDisplayTime(long displayTime);
}
