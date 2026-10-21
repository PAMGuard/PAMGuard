package difar;

import PamguardMVC.PamDataUnit;

/**
 * One sonobuoy record, for display on the map.
 * <p>
 * These units exist only to be drawn. They are built from the sonobuoy history,
 * are never saved, and are rebuilt whenever the history changes. The record
 * itself holds everything worth showing.
 */
public class SonobuoyDataUnit extends PamDataUnit {

	private final SonobuoyRecord record;

	/**
	 * @param record the buoy record this unit shows.
	 */
	public SonobuoyDataUnit(SonobuoyRecord record) {
		super(record.getTimeMillis());
		this.record = record;
		setChannelBitmap(1 << record.getChannel());
	}

	/**
	 * @return the buoy record.
	 */
	public SonobuoyRecord getRecord() {
		return record;
	}
}
