package depthReadout;

import java.sql.Types;

import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class DepthSQLLogging extends SQLLogging {

	private DepthControl depthControl;
	
	private PamTableItem[] rawDepthItems;
	private PamTableItem[] depthItems;
	
	private int nSensors;
	
	private PamTableDefinition tableDefinition;
	
	public DepthSQLLogging(DepthControl depthControl, DepthDataBlock depthDataBlock) {
		
		super(depthDataBlock);
		
		setCanView(true);
		
		this.depthControl = depthControl;
		
		tableDefinition = new PamTableDefinition(depthDataBlock.getDataName(), 
				SQLLogging.UPDATE_POLICY_WRITENEW);
		nSensors = depthControl.depthParameters.nSensors;
		rawDepthItems = new PamTableItem[nSensors];
		depthItems = new PamTableItem[nSensors];
		for (int i = 0; i < nSensors; i++) {
			tableDefinition.addTableItem(rawDepthItems[i] = new PamTableItem(String.format("Sensor_%d_Raw",i), Types.DOUBLE));
			tableDefinition.addTableItem(depthItems[i] = new PamTableItem(String.format("Sensor_%d_Depth",i), Types.DOUBLE));
			tableDefinition.setUseCheatIndexing(true);
		}

		setTableDefinition(tableDefinition);
	}

//	@Override
//	public PamTableDefinition getTableDefinition() {
//		return tableDefinition;
//	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {

		DepthDataUnit ddu = (DepthDataUnit) pamDataUnit;
		double[] depthData = ddu.getDepthData();
		double[] rawData = ddu.getRawDepthData();
		int n;
		if (depthData != null) {
			n = Math.min(nSensors, depthData.length);
			for (int i = 0; i < n; i++) {
				depthItems[i].setValue(depthData[i]);
			}
		}
		if (rawData != null) {
			n = Math.min(nSensors, rawData.length);
			for (int i = 0; i < n; i++) {
				rawDepthItems[i].setValue(rawData[i]);
			}
		}

	}

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		
		DepthDataUnit ddu = new DepthDataUnit(depthControl, timeMilliseconds);	

		ddu.setDatabaseIndex((Integer) tableDefinition.getIndexItem().getValue());
		ddu.setDatabaseIndex(databaseIndex);
//		Timestamp ts = (Timestamp) tableDefinition.getTimeStampItem().getValue();
//		long t = PamCalendar.millisFromTimeStamp(ts);
		
		double[] rawData = new double[nSensors];
		double[] depthData = new double[nSensors];
		
		for (int i = 0; i < nSensors; i++) {
			rawData[i] = (Double) rawDepthItems[i].getValue();
			depthData[i] = (Double) depthItems[i].getValue(); 
		}
		ddu.setDepthData(depthData);
		ddu.setRawDepthData(rawData);
		
		depthControl.getDepthDataBlock().addPamData(ddu);
		
		return ddu;
		
	}

}
