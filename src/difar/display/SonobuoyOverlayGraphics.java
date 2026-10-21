package difar.display;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import PamUtils.Coordinate3d;
import PamUtils.PamCalendar;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamColors;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PanelOverlayDraw;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;
import difar.DifarControl;
import difar.SonobuoyDataUnit;
import difar.SonobuoyRecord;

/**
 * Draws sonobuoy positions on the map.
 * <p>
 * Every deployment is drawn, so a whole voyage can be seen at once. The buoy in
 * force on each channel at the time being viewed is drawn large and named, and
 * the rest are drawn small, in the same way streamer positions are shown.
 * Positions come from the sonobuoy history, not from the array.
 */
public class SonobuoyOverlayGraphics extends PanelOverlayDraw {

	/** Size of the marker for a buoy in force. */
	private static final int IN_FORCE_SIZE = 12;

	/** Size of the marker for a buoy no longer, or not yet, in force. */
	private static final int OTHER_SIZE = 5;

	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_CIRCLE,
			IN_FORCE_SIZE, IN_FORCE_SIZE, true, Color.BLUE, Color.BLUE);

	private final DifarControl difarControl;

	public SonobuoyOverlayGraphics(DifarControl difarControl) {
		super(new PamSymbol(defaultSymbol));
		this.difarControl = difarControl;
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		if (!canDraw(generalProjector)) {
			return null;
		}
		SonobuoyRecord record = ((SonobuoyDataUnit) pamDataUnit).getRecord();
		if (!record.hasPosition()) {
			return null;
		}
		Coordinate3d position = generalProjector.getCoord3d(record.getLatitude(), record.getLongitude(), 0);
		boolean inForce = isInForce(record);
		PamSymbol symbol = getPamSymbol(pamDataUnit, generalProjector);
		Color colour = PamColors.getInstance().getChannelColor(record.getChannel());
		symbol.setLineColor(colour);
		symbol.setFillColor(colour);
		int size = inForce ? IN_FORCE_SIZE : OTHER_SIZE;
		symbol.setWidth(size);
		symbol.setHeight(size);
		Rectangle r = symbol.draw(g, position.getXYPoint());
		if (inForce && record.getName() != null) {
			g.setColor(colour);
			g.drawString(record.getName(), (int) position.x + size, (int) position.y);
		}
		if (isEndedWithoutReplacement(record)) {
			drawCross(g, position, OTHER_SIZE);
		}
		generalProjector.addHoverData(position, pamDataUnit);
		return r;
	}

	/**
	 * A buoy that has ended by the time being viewed, with nothing deployed on
	 * its channel afterwards. That channel has no buoy at the time shown, which
	 * is worth seeing on the map, so it is crossed out. A buoy that ends later
	 * than the time being viewed is still working, and is not crossed.
	 * @return true if this is the last record on its channel and its buoy had
	 * ended by the time being viewed.
	 */
	private boolean isEndedWithoutReplacement(SonobuoyRecord record) {
		if (!record.hasEndedBy(PamCalendar.getTimeInMillis())) {
			return false;
		}
		return record == difarControl.getSonobuoyHistory().getLastRecord(record.getChannel());
	}

	/**
	 * Draw a black cross over a marker.
	 */
	private void drawCross(Graphics g, Coordinate3d position, int size) {
		int x = (int) position.x, y = (int) position.y;
		g.setColor(Color.BLACK);
		g.drawLine(x - size, y - size, x + size, y + size);
		g.drawLine(x - size, y + size, x + size, y - size);
	}

	/**
	 * @return true if this record is the buoy in force on its channel at the
	 * time being viewed.
	 */
	private boolean isInForce(SonobuoyRecord record) {
		return record == difarControl.getSonobuoyHistory()
				.getRecordAt(record.getChannel(), PamCalendar.getTimeInMillis());
	}

	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		return parameterTypes[0] == GeneralProjector.ParameterType.LATITUDE
				&& parameterTypes[1] == GeneralProjector.ParameterType.LONGITUDE;
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector, int keyType) {
		return null;
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		SonobuoyRecord record = ((SonobuoyDataUnit) dataUnit).getRecord();
		String text = String.format("<html>Sonobuoy %s on channel %d<p>Deployed %s",
				record.getName(), record.getChannel(),
				PamCalendar.formatDateTime(record.getTimeMillis()));
		if (record.getEndTimeMillis() != null) {
			text += "<p>Ended " + PamCalendar.formatDateTime(record.getEndTimeMillis());
		}
		if (record.getHeading() != null) {
			text += String.format("<p>Calibration %3.1f\u00B0", record.getHeading());
		}
		if (isInForce(record)) {
			text += "<p>In force at the time shown";
		} else if (isEndedWithoutReplacement(record)) {
			text += "<p>Ended, and not replaced on this channel";
		}
		return text;
	}

	@Override
	public boolean hasOptionsDialog(GeneralProjector generalProjector) {
		return false;
	}
}
