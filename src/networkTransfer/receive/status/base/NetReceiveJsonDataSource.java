package networkTransfer.receive.status.base;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import jsonStorage.JSONObjectDataSource;
import networkTransfer.receive.status.BuoyStatusDataUnit;

public class NetReceiveJsonDataSource extends JSONObjectDataSource<NetReceiveJsonData>{
	
	public NetReceiveJsonDataSource() {
		super(false);
		objectData = new NetReceiveJsonData();
	}

	@Override
	protected void addClassSpecificFields(PamDataUnit pamDataUnit) {
		NetReceiverStatusDataUnit netUnit = (NetReceiverStatusDataUnit) pamDataUnit;
		
		objectData.memoryUsedPercent = netUnit.getMemoryUsedPercent();
		objectData.nBuoysConnected = netUnit.getnBuoysConnected();
		objectData.nBuoysRegistered = netUnit.getnBuoysRegistered();
		objectData.pamguardRunState = netUnit.getPamguardRunState();
		objectData.pamguardUptimeMillis = netUnit.getPamguardUptimeMillis();
	}

	@Override
	protected void setObjectType(PamDataUnit pamDataUnit) {
		objectData.identifier = -1;
		
	}
	
	@Override
	protected String getAdditionalJson(PamDataUnit dataUnit) {
		
		NetReceiverStatusDataUnit netUnit = (NetReceiverStatusDataUnit) dataUnit;
		
		String extraJson = "\"buoySummaries\":[";
		
		for(BuoyStatusDataUnit buoyStatus:netUnit.getBuoyStatusData()) {
			String thisBuoyReport = "";
			long lastBuoyDataTime = buoyStatus.getLastDataTime();
			long lastBuoyCommPing = buoyStatus.getLastCommsPing();
			String buoyName = buoyStatus.getBuoyName();
			thisBuoyReport+="\"lastDataTime\":"+lastBuoyDataTime+",";
			thisBuoyReport+="\"lastComPingTime\":"+lastBuoyCommPing+",";
			thisBuoyReport+="\"buoyName\":\""+buoyName+"\",";
			String blockSummaries = "";
			for(PamDataBlock nextBlock:PamController.PamController.getInstance().getDetectorDataBlocks()) {
				blockSummaries+="\""+nextBlock.getDataName()+"\":"+buoyStatus.getBlockPacketCount(nextBlock)+",";
			}
			if (blockSummaries != null && blockSummaries.length() > 0) {
				blockSummaries = blockSummaries.substring(0, blockSummaries.length() - 1);
	        }
			blockSummaries = "{"+blockSummaries+"}";
			thisBuoyReport+="\"dataCounts\":"+blockSummaries;
			thisBuoyReport = "{"+thisBuoyReport+"}";
			extraJson += thisBuoyReport+",";
		}
		
		if (extraJson != null && extraJson.length() > 0) {
			extraJson = extraJson.substring(0, extraJson.length() - 1);
        }
		
		extraJson+="]";
		
		return extraJson;
	}

}
