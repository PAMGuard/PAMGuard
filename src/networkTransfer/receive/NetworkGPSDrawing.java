package networkTransfer.receive;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.Vector;

import GPS.GpsData;
import GPS.GpsDataUnit;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
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

/**
 * Class for drawing buoy positions on the map. 
 * @author Doug Gillespie
 *
 */
public class NetworkGPSDrawing extends PanelOverlayDraw {
	
	private NetworkReceiver networkReceiver;

	private ManagedSymbolInfo managedSymbolInfo;
	
	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_DIAMOND, 12, 12, true, Color.darkGray, Color.GREEN);
	
	/**
	 * @param networkReceiver
	 */
	public NetworkGPSDrawing(NetworkReceiver networkReceiver) {
		super(new PamSymbol(defaultSymbol));
		this.networkReceiver = networkReceiver;
		managedSymbolInfo = new ManagedSymbolInfo("Network Buoy Localisations");
	}

	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		if (parameterTypes[0] == ParameterType.LATITUDE
				&& parameterTypes[1] == ParameterType.LONGITUDE) {
			return true;
		}
		return false;
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector,
			int keyType) {
		return new BasicKeyItem(getDefaultSymbol(), "Buoy Locations");
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {
		BuoyStatusDataUnit buoyStatusDataUnit = (BuoyStatusDataUnit) pamDataUnit;
		GpsData gpsData = buoyStatusDataUnit.getGpsData();
		if (gpsData == null) {
			return null;
		}
		Coordinate3d c3d = generalProjector.getCoord3d(gpsData.getLatitude(), gpsData.getLongitude(), 0);
		generalProjector.addHoverData(c3d, pamDataUnit);
		
		return getPamSymbol(pamDataUnit, generalProjector).draw(g, c3d.getXYPoint());
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector,
			PamDataUnit dataUnit, int iSide) {
		BuoyStatusDataUnit rxStats = (BuoyStatusDataUnit) dataUnit;
		GpsData gpsData = rxStats.getGpsData();
		String headString = "";
		if (gpsData.getMagneticHeading() != null) {
			headString = String.format("Buoy heading: %3.1f\u00B0", gpsData.getHeading());
			double[] compassData = rxStats.getCompassData();
			if (compassData == null || compassData.length < 3) {
				headString += "<p>";
			}
			else {
				headString += String.format(";    tilt %3.1f\u00B0/%3.1f\u00B0<p>", compassData[1], compassData[2]);
			}
		}
		return String.format("<html>Buoy %d(%d)<p>Software Channel: %d<p>%s,%s<p>%sLast data: %s</html>",
				rxStats.getBuoyId1(), rxStats.getBuoyId2(), rxStats.getLowestChannel(), LatLong.formatLatitude(gpsData.getLatitude()),
				LatLong.formatLongitude(gpsData.getLongitude()), headString, PamCalendar.formatDateTime(rxStats.getLastDataTime()));
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
