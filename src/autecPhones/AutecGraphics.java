package autecPhones;

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
import PamView.PamColors;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PanelOverlayDraw;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamColors.PamColor;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;

public class AutecGraphics extends PanelOverlayDraw {



	private static final int SYMBOLSIZE = 10;
	
	public static final SymbolData defSymbol = new SymbolData(PamSymbolType.SYMBOL_DIAMOND, SYMBOLSIZE, SYMBOLSIZE, true, Color.RED, Color.BLUE);
	
	private PamSymbol symbol;

	public AutecGraphics() {
		super(new PamSymbol(defSymbol));
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		return (parameterTypes[0] == GeneralProjector.ParameterType.LATITUDE && 
				parameterTypes[1] == GeneralProjector.ParameterType.LONGITUDE);
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector,
			int keyType) {
		return new BasicKeyItem(getDefaultSymbol(), "Autec Hydrophones");
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {
		PamSymbol symbol = getPamSymbol(pamDataUnit, generalProjector);
		AutecDataUnit adu = (AutecDataUnit) pamDataUnit;
		Coordinate3d c3d = generalProjector.getCoord3d(adu.getLatLong().getLatitude(), 
				adu.getLatLong().getLongitude(), adu.getDepth());
		generalProjector.addHoverData(c3d, pamDataUnit);
		Rectangle r =  symbol.draw(g, c3d.getXYPoint());
		g.setColor(PamColors.getInstance().getColor(PamColor.AXIS));
		g.drawString(String.format("%d", adu.getID()), (int) c3d.x + symbol.getWidth()/2, 
				(int) c3d.y + symbol.getHeight()/2);
		return r;
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector,
			PamDataUnit dataUnit, int side) {
		AutecDataUnit adu = (AutecDataUnit) dataUnit;
		LatLong ll = adu.getLatLong();
		return String.format("<html>Hydropone %d (%s)<br>%s, %s<br>Depth %5.1f m</html>", 
				adu.getID(), adu.isActive() ? "Active" : "Inactive",
				LatLong.formatLatitude(ll.getLatitude()),
				LatLong.formatLongitude(ll.getLongitude()), adu.getDepth());
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
