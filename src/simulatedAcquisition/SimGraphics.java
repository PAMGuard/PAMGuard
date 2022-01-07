package simulatedAcquisition;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Window;

import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamView.BasicKeyItem;
import PamView.GeneralProjector;
import PamView.ManagedSymbol;
import PamView.ManagedSymbolInfo;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamOldSymbolManager;
import PamView.PamSymbolType;
import PamView.PanelOverlayDraw;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;

public class SimGraphics extends PanelOverlayDraw {
	private PamSymbol pamSymbol;
	
	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_SQUARE, 8, 8, true, Color.GREEN, Color.RED);
	public SimGraphics() {
		super(new PamSymbol(defaultSymbol));
	}
	
	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {

		if (parameterTypes[0] == ParameterType.LATITUDE &&
				parameterTypes[1] == ParameterType.LONGITUDE) {
			return true;
		}
		return false;
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector,
			int keyType) {
		return new BasicKeyItem(getPamSymbol(), "Simulated Sources");
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {
		if (generalProjector.getParmeterType(0) == ParameterType.LATITUDE &&
				generalProjector.getParmeterType(1) == ParameterType.LONGITUDE) {
			return drawDataUnitOnMap(g, pamDataUnit, generalProjector);
		}
		return null;
	}
	public Rectangle drawDataUnitOnMap(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {
		SimObjectDataUnit sdu = (SimObjectDataUnit) pamDataUnit;
//		SimObject simObject = sdu.getSimObject();
		LatLong ll = sdu.getCurrentPosition();
//		System.out.println(String.format("Sim object at %s, %s", LatLong.formatLatitude(ll.getLatitude()), LatLong.formatLongitude(ll.getLongitude())));
		Coordinate3d c3d = generalProjector.getCoord3d(ll.getLatitude(), ll.getLongitude(), sdu.getCurrentHeight());
		getPamSymbol().draw(g, c3d.getXYPoint());
		generalProjector.addHoverData(c3d, pamDataUnit);
		return null;
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector,
			PamDataUnit dataUnit, int side) {
		SimObjectDataUnit sdu = (SimObjectDataUnit) dataUnit;
		SimObject simObject = sdu.getSimObject();
		String str = String.format("<html>Simulated Source: %s", simObject.name);
		str += String.format("<p>Depth = %3.1f m", -sdu.getCurrentHeight());
		str += String.format("<p>Course = %3.1f deg", simObject.course);
		str += String.format("<p>Speed = %3.1f m/s", simObject.speed);
		str += String.format("<p>Signal = %s", sdu.getSimSignal().getName());
		str += "</html>";
		return str;
	}

	public PamSymbol getPamSymbol() {
		if (pamSymbol == null) {
			pamSymbol = new PamSymbol(defaultSymbol);
		}
		return pamSymbol;
	}

	public ManagedSymbolInfo getSymbolInfo() {
		ManagedSymbolInfo msi = new ManagedSymbolInfo("Simulated Sources");
		return msi;
	}

	public void setPamSymbol(PamSymbol pamSymbol) {
		this.pamSymbol = pamSymbol;
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
