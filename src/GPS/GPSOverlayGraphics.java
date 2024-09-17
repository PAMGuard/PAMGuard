package GPS;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

import Map.MapRectProjector;
import Map.Vessel;
import PamUtils.Coordinate3d;
import PamUtils.PamCalendar;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.ManagedSymbolInfo;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PanelOverlayDraw;
import PamView.SymbolKeyItem;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;

public class GPSOverlayGraphics extends PanelOverlayDraw {
	
	private Vessel vessel;
	
	private ManagedSymbolInfo symbolInfo;
	
	private GPSControl gpsControl;
	
	public static final SymbolData defSymbol = new SymbolData(PamSymbolType.SYMBOL_NONE, 1, 1, false, Color.WHITE, Color.WHITE); 
	
	public GPSOverlayGraphics(GPSControl gpsControl) {
		super(new PamSymbol(defSymbol));
		this.gpsControl = gpsControl;
		symbolInfo = new ManagedSymbolInfo(gpsControl.getUnitName());
//		PamOldSymbolManager.getInstance().addManagesSymbol(this);
		vessel = new Vessel(Color.GRAY);
	}
//
//	public ManagedSymbolInfo getSymbolInfo() {
//		return symbolInfo;
//	}

	private GpsDataUnit lastDrawnDataUnit = null;
	private Point lastCoord;
	
	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {
		if (canDraw(generalProjector)) {
			return drawOnMap(g, pamDataUnit, generalProjector);
		}
		
		return null;
	}

	private Rectangle drawOnMap(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		GpsDataUnit gpsDataUnit = (GpsDataUnit) pamDataUnit;
		GpsData gpsData = gpsDataUnit.getGpsData();
		long now = PamCalendar.getTimeInMillis();
		PamSymbol s = getPamSymbol(pamDataUnit, generalProjector);
		Coordinate3d coord = generalProjector.getCoord3d( gpsData.getLatitude(), gpsData.getLongitude(),gpsData.getHeight());
		Point p = coord.getXYPoint();
		if (lastDrawnDataUnit != null && Math.abs(lastDrawnDataUnit.getAbsBlockIndex()-gpsDataUnit.getAbsBlockIndex()) == 1) {
			if (!p.equals(lastCoord)) {
				g.setColor(s.getLineColor());
				g.drawLine(p.x, p.y, lastCoord.x, lastCoord.y);
			}
		}
		lastCoord = p;
		lastDrawnDataUnit = gpsDataUnit;
		GPSControl gpsControl = (GPSControl) gpsDataUnit.getParentDataBlock().getParentProcess().getPamControlledUnit(); 
		if (!gpsControl.isGpsMaster()){
			GpsData latestGps = ((GpsDataUnit) gpsDataUnit.getParentDataBlock().getClosestUnitMillis(now)).getGpsData();
			vessel.setShipGps(latestGps);
			vessel.setShipColor(s.getLineColor());
			vessel.drawShip((Graphics2D) g, (MapRectProjector) generalProjector);
			Coordinate3d hoverDataCoord = generalProjector.getCoord3d(latestGps.getLatitude(), latestGps.getLongitude(), 0);
			generalProjector.addHoverData(hoverDataCoord, pamDataUnit);
			
		}
		return null;
	}

	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		return (parameterTypes[0] == GeneralProjector.ParameterType.LATITUDE && 
				parameterTypes[1] == GeneralProjector.ParameterType.LONGITUDE);
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector,
			int keyType) {
		return new SymbolKeyItem(getDefaultSymbol(), gpsControl.getUnitName());
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector,
			PamDataUnit dataUnit, int iSide) {
		GPSControl gpsControl = (GPSControl) dataUnit.getParentDataBlock().getParentProcess().getPamControlledUnit(); 
		if (gpsControl.isGpsMaster()){
			return null;
		}
		GpsData gpsData = ((GpsDataUnit) dataUnit).getGpsData();
		return gpsData.summaryString() + "<br>" + gpsControl.getUnitName();
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
