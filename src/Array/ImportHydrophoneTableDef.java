package Array;

import java.sql.Types;

import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;

public class ImportHydrophoneTableDef extends PamTableDefinition{
	
	//SAVABLE FIELDS
	PamTableItem timeMillis		=new PamTableItem("timeMillis",Types.DOUBLE);
	PamTableItem xPos			=new PamTableItem("xPos",Types.DOUBLE);
	PamTableItem yPos			=new PamTableItem("yPos",Types.DOUBLE);
	PamTableItem zPos			=new PamTableItem("zPos",Types.DOUBLE);
	PamTableItem xErr			=new PamTableItem("xErr",Types.DOUBLE);
	PamTableItem yErr			=new PamTableItem("yErr",Types.DOUBLE);
	PamTableItem zErr			=new PamTableItem("zErr",Types.DOUBLE);
	PamTableItem streamerID		=new PamTableItem("streamerID",Types.INTEGER);
	PamTableItem sensitivity	=new PamTableItem("sensitivity",Types.DOUBLE);
	PamTableItem gain			=new PamTableItem("gain",Types.DOUBLE);
	PamTableItem hydrophoneN	=new PamTableItem("hydrophoneN",Types.INTEGER);


	public ImportHydrophoneTableDef(String tableName) {
		super(tableName, SQLLogging.UPDATE_POLICY_WRITENEW);
		addTableItem(timeMillis);
		addTableItem(xPos);
		addTableItem(yPos);
		addTableItem(zPos);
		addTableItem(xErr);
		addTableItem(yErr);
		addTableItem(zErr);
		addTableItem(streamerID);
		addTableItem(sensitivity);
		addTableItem(gain);
		addTableItem(hydrophoneN);
	}
	
}
