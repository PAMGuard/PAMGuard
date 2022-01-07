package PamController.masterReference;

import Array.ArrayManager;
import Array.Streamer;
import Array.StreamerDataUnit;
import Array.streamerOrigin.HydrophoneOriginMethod;
import GPS.GpsDataUnit;
import PamUtils.LatLong;

public class ArrayReferenceSystem implements MasterReferenceSystem {

	/**
	 * Works by finding the first streamer in the array and getting whatever information 
	 * it can about it. 
	 */
	
	@Override
	public LatLong getLatLong() {
		Streamer streamer = findFirstStreamer();
		if (streamer == null) {
			return null;
		}
		HydrophoneOriginMethod ho = streamer.getHydrophoneOrigin();
		if (ho == null) {
			return null;
		}
		StreamerDataUnit lastGps = ho.getLastStreamerData();
		if (lastGps == null) {
			return null;
		}
		return lastGps.getGpsData();
	}

	@Override
	public Long getFixTime() {		
		Streamer streamer = findFirstStreamer();
		if (streamer == null) {
			return null;
		}
		HydrophoneOriginMethod ho = streamer.getHydrophoneOrigin();
		if (ho == null) {
			return null;
		}
		StreamerDataUnit lastGps = ho.getLastStreamerData();
		if (lastGps == null) {
			return null;
		}
		return lastGps.getTimeMilliseconds();
	}

	@Override
	public Double getCourse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getHeading() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private Streamer findFirstStreamer() {
		return ArrayManager.getArrayManager().getCurrentArray().getStreamer(0);
	}

	@Override
	public String getName() {
		return "Array pos";
	}

	@Override
	public Double getSpeed() {
		return null;
	}

	@Override
	public String getError() {
		if (getLatLong() != null) {
			return "No hydrophone origin location available - check array manager configuration";
		}
		else {
			return null;
		}
	}

	@Override
	public void setDisplayTime(long displayTime) {
		// TODO Auto-generated method stub
		
	}

}
