package noiseOneBand;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class OneBandLogging extends SQLLogging {

	private OneBandControl oneBandControl;
	
	private PamTableDefinition tableDefinition;
	private PamTableItem channel, rms, zeroPeak, peakPeak, sel, selSecs;
	
	protected OneBandLogging(OneBandControl oneBandControl, PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.oneBandControl = oneBandControl;
		tableDefinition = new PamTableDefinition(pamDataBlock.getDataName(), UPDATE_POLICY_OVERWRITE);
		tableDefinition.addTableItem(channel = new PamTableItem("channel", Types.INTEGER));
		tableDefinition.addTableItem(rms = new PamTableItem("RMS", Types.REAL));
		tableDefinition.addTableItem(zeroPeak = new PamTableItem("ZeroPeak", Types.REAL));
		tableDefinition.addTableItem(peakPeak = new PamTableItem("PeakPeak", Types.REAL));
		tableDefinition.addTableItem(sel = new PamTableItem("SEL", Types.REAL));
		tableDefinition.addTableItem(selSecs = new PamTableItem("SEL Secs", Types.REAL));
		tableDefinition.setUseCheatIndexing(true);
		
		setTableDefinition(tableDefinition);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		OneBandDataUnit oneBandDataUnit = (OneBandDataUnit) pamDataUnit;
		channel.setValue(oneBandDataUnit.getChannelBitmap());
		rms.setValue(new Float(oneBandDataUnit.getRms()));
		zeroPeak.setValue(new Float(oneBandDataUnit.getZeroPeak()));
		peakPeak.setValue(new Float(oneBandDataUnit.getPeakPeak()));
		Double d = oneBandDataUnit.getIntegratedSEL();
		if (d == null) {
			sel.setValue(null);
			selSecs.setValue(null);
		}
		else {
			sel.setValue(new Float(d));
			selSecs.setValue(new Float(oneBandDataUnit.getSelIntegationTime()));
		}
	}

}
