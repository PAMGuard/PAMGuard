package noiseOneBand;


import Array.ArrayManager;
import Array.Streamer;
import PamguardMVC.PamDataUnit;
import jsonStorage.JSONObjectDataSource;


public class OneBandJsonDataSource extends JSONObjectDataSource<OneBandJsonData>{
	
	public OneBandJsonDataSource() {
		super();
		objectData = new OneBandJsonData();
	}
	
	protected OneBandJsonData initializeObjectData() {
		return new OneBandJsonData();

	}
	
	@Override
	protected void addClassSpecificFields(PamDataUnit pamDataUnit) {
		OneBandDataUnit noiseDu = (OneBandDataUnit) pamDataUnit;
		objectData.rms = noiseDu.getRms();
		objectData.peakpeak = noiseDu.getPeakPeak();
		objectData.sel = noiseDu.getIntegratedSEL();
		objectData.millis = noiseDu.getTimeMilliseconds();
		objectData.buoyId = getpbId(noiseDu.getChannelBitmap());
	}

	@Override
	protected void setObjectType(PamDataUnit pamDataUnit) {
		objectData.identifier = -2000;
		
	}
	
	private String getpbId(int channelBitmap) {
		int channelIdx = PamUtils.PamUtils.getLowestChannel(channelBitmap);
		int stremerIdx = ArrayManager.getArrayManager().getCurrentArray().getStreamerForPhone(channelIdx);
		Streamer streamer = ArrayManager.getArrayManager().getCurrentArray().getStreamer(stremerIdx);
		String streamerName = streamer.getStreamerName();
		String pbId = "pb"+streamerName.substring(streamerName.length()-3);
		return pbId;
	}

}
