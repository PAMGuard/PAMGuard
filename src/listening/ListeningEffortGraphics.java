package listening;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Window;

import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PanelOverlayDraw;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;

public class ListeningEffortGraphics extends PanelOverlayDraw {

	private ListeningControl listeningControl;
	
	public static final SymbolData defSymbol = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 6, 6, true, Color.BLUE, Color.RED);

	public ListeningEffortGraphics(ListeningControl listeningControl) {
		super(new PamSymbol(defSymbol));
		this.listeningControl = listeningControl;
//		PamOldSymbolManager.getInstance().addManagesSymbol(this);
	}
	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		return (parameterTypes[0] == GeneralProjector.ParameterType.LATITUDE && 
				parameterTypes[1] == GeneralProjector.ParameterType.LONGITUDE);
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector,
			int keyType) {
		return getDefaultSymbol().makeKeyItem("Listening Effort");
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {
		if (generalProjector.getParmeterType(0) == GeneralProjector.ParameterType.LATITUDE && 
				generalProjector.getParmeterType(1) == GeneralProjector.ParameterType.LONGITUDE) {
			return drawOnMap(g, pamDataUnit, generalProjector);
		}
		return null;
	}
	
	public Rectangle drawOnMap(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {

		ListeningEffortData thing = (ListeningEffortData) pamDataUnit;
		if (thing == null) {
			return null;
		}
		Coordinate3d pos;
		LatLong oll = thing.getOriginLatLong(false);
		if (oll == null) {
			return null;
		}
		pos = generalProjector.getCoord3d(oll.getLatitude(),oll.getLongitude(), 0);
		Rectangle r = getPamSymbol(pamDataUnit, generalProjector).draw(g, pos.getXYPoint());
		generalProjector.addHoverData(pos, pamDataUnit);

		return r;
	}
	
	@Override
	public String getHoverText(GeneralProjector generalProjector,
			PamDataUnit dataUnit, int iSide) {

		ListeningEffortData thing = (ListeningEffortData) dataUnit;

		String str = "<html>Listening Effort at " + PamCalendar.formatTime(thing.getTimeMilliseconds());
		str += String.format("<br>%s", thing.getStatus());
		LatLong oll = thing.getOriginLatLong(false);
		if (oll != null) {
			str += String.format("<br>%s, %s", LatLong.formatLatitude(oll.getLatitude()),
					LatLong.formatLongitude(oll.getLongitude()));
		}
		str += "</html>";
		return str;
	}


	@Override
	public boolean hasOptionsDialog(GeneralProjector generalProjector) {
		return false;
	}

	@Override
	public boolean showOptions(Window parentWindow,
			GeneralProjector generalProjector) {
		return false;
	}


}
