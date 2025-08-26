package noiseMonitor;

import org.json.JSONObject;

import Array.ArrayManager;
import Array.Streamer;
import PamguardMVC.PamDataUnit;
import jsonStorage.JSONObjectDataSource;
import noiseBandMonitor.NoiseBandControl;

public class NoiseJsonDataSource extends JSONObjectDataSource<NoiseJsonData>{
	
	private NoiseDataBlock noiseBlock;
	private String[] bandNames;
	private String[] measurementNames;
	private NoiseBandControl noiseBandControl;
	
	public NoiseJsonDataSource(NoiseDataBlock noiseBlock,NoiseBandControl noiseControl) {
		super(false);
		this.noiseBandControl = noiseControl;
		this.noiseBlock = noiseBlock;
		bandNames=noiseBlock.getBandNames();
		measurementNames = noiseBlock.getUsedMeasureNames();
		objectData = new NoiseJsonData();
	}
	
	protected NoiseJsonData initializeObjectData() {
		return new NoiseJsonData();

	}
	
	@Override
	protected String getAdditionalJson(PamDataUnit dataUnit) {
		if(bandNames==null) {
			bandNames = noiseBandControl.getBandData().getBandNames();
		}
		NoiseDataUnit noiseDu = (NoiseDataUnit) dataUnit;
		JSONObject noiseJson = new JSONObject();
		double[][] noiseData = noiseDu.getNoiseBandData();
		for(int bandIdx=0;bandIdx<noiseData.length;bandIdx++) {
			JSONObject bandData = new JSONObject();
			for(int measurementIdx=0;measurementIdx<measurementNames.length;measurementIdx++) {
				bandData.put(measurementNames[measurementIdx], noiseData[bandIdx][measurementIdx]);
			}
			noiseJson.put(bandNames[bandIdx],bandData);
		}
		return "\"measurements\":"+noiseJson.toString();
	}

	@Override
	protected void addClassSpecificFields(PamDataUnit pamDataUnit) {
		objectData.buoyId = getpbId(pamDataUnit.getChannelBitmap());
		
	}

	@Override
	protected void setObjectType(PamDataUnit pamDataUnit) {
		objectData.identifier = -1999;
		
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
