package dbht;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class DbHtLogging extends SQLLogging {

	private DbHtControl dbHtControl;
	
	private PamTableDefinition tableDefinition;
	private PamTableItem channel, rms, zeroPeak, peakPeak;
	
	protected DbHtLogging(DbHtControl dbHtControl, PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.dbHtControl = dbHtControl;
		tableDefinition = new PamTableDefinition(pamDataBlock.getDataName(), UPDATE_POLICY_OVERWRITE);
		tableDefinition.addTableItem(channel = new PamTableItem("channel", Types.INTEGER));
		tableDefinition.addTableItem(rms = new PamTableItem("RMS", Types.FLOAT));
		tableDefinition.addTableItem(zeroPeak = new PamTableItem("ZeroPeak", Types.FLOAT));
		tableDefinition.addTableItem(peakPeak = new PamTableItem("PeakPeak", Types.FLOAT));
		tableDefinition.setUseCheatIndexing(true);
		
		setTableDefinition(tableDefinition);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		DbHtDataUnit dbHtDataUnit = (DbHtDataUnit) pamDataUnit;
		channel.setValue(dbHtDataUnit.getChannelBitmap());
		rms.setValue(new Float(dbHtDataUnit.getRms()));
		zeroPeak.setValue(new Float(dbHtDataUnit.getZeroPeak()));
		peakPeak.setValue(new Float(dbHtDataUnit.getPeakPeak()));
	}

}
