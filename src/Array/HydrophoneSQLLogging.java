package Array;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class HydrophoneSQLLogging extends SQLLogging {
	

	private ImportHydrophoneTableDef table;
	private HydrophoneDataBlock hydrophoneDataBlock;

	protected HydrophoneSQLLogging(HydrophoneDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.hydrophoneDataBlock = pamDataBlock;
		
		setCanView(true);
		
		table=new ImportHydrophoneTableDef("Hydrophones");
		table.setUpdatePolicy(UPDATE_POLICY_OVERWRITE);
		table.setUseCheatIndexing(true);
		setTableDefinition(table);

	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		
		HydrophoneDataUnit importHydrophoneDataUnit=(HydrophoneDataUnit) pamDataUnit;
		
		table.findTableItem("timeMillis")		.setValue(Double.valueOf(importHydrophoneDataUnit.getTimeMillis().doubleValue()));
		table.findTableItem("xPos")				.setValue(importHydrophoneDataUnit.getHydrophone().getCoordinates()[0]);
		table.findTableItem("yPos")				.setValue(importHydrophoneDataUnit.getHydrophone().getCoordinates()[1]);
		table.findTableItem("zPos")				.setValue(importHydrophoneDataUnit.getHydrophone().getCoordinates()[2]);
		table.findTableItem("xErr")				.setValue(importHydrophoneDataUnit.getHydrophone().getCoordinateErrors()[0]);
		table.findTableItem("yErr")				.setValue(importHydrophoneDataUnit.getHydrophone().getCoordinateErrors()[1]);
		table.findTableItem("zErr")				.setValue(importHydrophoneDataUnit.getHydrophone().getCoordinateErrors()[2]);
		table.findTableItem("streamerID")		.setValue(importHydrophoneDataUnit.getHydrophone().getStreamerId());
		table.findTableItem("sensitivity")		.setValue(importHydrophoneDataUnit.getHydrophone().getSensitivity());
		table.findTableItem("gain")				.setValue(importHydrophoneDataUnit.getHydrophone().getPreampGain());
		table.findTableItem("hydrophoneN")		.setValue(importHydrophoneDataUnit.getHydrophone().getID());
		
	}
	

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long dataTime, int iD) {
		
//		System.out.println("Load Hydrophone Unit: "+iD);
		
		double xPos= table.xPos.getDoubleValue();
		double yPos= table.yPos.getDoubleValue();
		double zPos= table.zPos.getDoubleValue();
		double xErr= table.xErr.getDoubleValue();
		double yErr= table.yErr.getDoubleValue();
		double zErr= table.zErr.getDoubleValue();
		int streamerID= table.streamerID.getIntegerValue();
		double sensitivity= table.sensitivity.getDoubleValue();
		double gain= table.gain.getDoubleValue();
		int hydrophoneN= table.hydrophoneN.getIntegerValue();
		
		Hydrophone hydrophone=new Hydrophone(hydrophoneN);
		double[] cOrd={xPos, yPos, zPos};
		double[] cOrdErr={xErr, yErr, zErr};
		hydrophone.setCoordinate(cOrd);
		hydrophone.setCoordinateErrors(cOrdErr);
		hydrophone.setPreampGain(gain);
		hydrophone.setSensitivity(sensitivity);
		hydrophone.setStreamerId(streamerID);
		hydrophone.setTimeMillis(dataTime);
		double[] bandwidth={0 ,20000};
		hydrophone.setBandwidth(bandwidth);
		
		HydrophoneDataUnit dataunit=new HydrophoneDataUnit(dataTime, hydrophone);
		dataunit.setChannelBitmap(1<<hydrophoneN);
		
		dataunit.setDatabaseIndex(iD);
		
		//need to manually add to datablock
		((PamDataBlock<HydrophoneDataUnit>) getPamDataBlock()).addPamData(dataunit);
		
		return dataunit;
	}

}
