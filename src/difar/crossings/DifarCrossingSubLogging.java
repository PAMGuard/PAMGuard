package difar.crossings;

import java.sql.Types;

import Array.StreamerDataUnit;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import difar.DifarControl;
import difar.DifarDataUnit;
import generalDatabase.PamSubtableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

/**
 * Logs the clips of each DIFAR crossing, one row per clip.
 * <p>
 * PAMGuard fills the standard subtable columns: the crossing's UID, and the
 * clip's UID, time, data block and binary file. The extra columns here make
 * a crossing plottable from one query, without joining back to the clip
 * table or the buoy records.
 */
public class DifarCrossingSubLogging extends SQLLogging {

	/** Buoy names are typed by the operator; this is generous. */
	private static final int BUOY_NAME_LENGTH = 80;

	private final DifarControl difarControl;

	private PamTableItem channel, buoyName, trueBearing;

	/**
	 * @param tableName the subtable, named after the crossing table.
	 * @param difarControl the DIFAR module, for buoy names.
	 * @param dataBlock the crossings.
	 */
	public DifarCrossingSubLogging(String tableName, DifarControl difarControl, DifarCrossingDataBlock dataBlock) {
		super(dataBlock);
		this.difarControl = difarControl;
		PamSubtableDefinition tableDef = new PamSubtableDefinition(tableName);
		tableDef.addTableItem(channel = new PamTableItem("Channel", Types.INTEGER));
		tableDef.addTableItem(buoyName = new PamTableItem("BuoyName", Types.CHAR, BUOY_NAME_LENGTH));
		tableDef.addTableItem(trueBearing = new PamTableItem("TrueBearing", Types.DOUBLE));
		setTableDefinition(tableDef);
	}

	/**
	 * Called with each clip in turn, after PAMGuard has filled the standard
	 * subtable columns.
	 */
	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		DifarDataUnit clip = (DifarDataUnit) pamDataUnit;
		int chan = PamUtils.getSingleChannel(clip.getChannelBitmap());
		channel.setValue(chan);
		buoyName.setValue(findBuoyName(chan, clip.getTimeMilliseconds()));
		trueBearing.setValue(clip.getTrueAngle());
	}

	private String findBuoyName(int chan, long timeMillis) {
		if (difarControl.sonobuoyManager == null) {
			return null;
		}
		StreamerDataUnit record = difarControl.sonobuoyManager.findRecordUnitAt(chan, timeMillis);
		if (record == null || record.getStreamerData() == null) {
			return null;
		}
		return record.getStreamerData().getStreamerName();
	}
}
