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
import networkTransfer.receive.status.BuoyStatusDataUnit;

/**
 * Class for drawing buoy positions on the map. 
 * @author Doug Gillespie
 *
 */
public class NetworkGPSDrawing extends PanelOverlayDraw {
	
	private NetworkReceiver networkReceiver;

	private ManagedSymbolInfo managedSymbolInfo;
	
	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_CAB, 25, 25, true, Color.YELLOW, Color.black);
	
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
		int[] buoyChannels = PamUtils.getChannelArray(rxStats.getChannelBitmap());
		String swChannelList = "";
		for(int channel:buoyChannels) {
			swChannelList+=channel+", ";
		}
		swChannelList = swChannelList.substring(0, swChannelList.length()-2);
		
		String headerData = String.format("<html><strong>%s - pb%d</strong><p>Software Channels: %s<p>%s,%s<p>%sLast data received: %s",
				rxStats.getSiteName(),rxStats.getBuoyId1(), swChannelList, LatLong.formatLatitude(gpsData.getLatitude()),
				LatLong.formatLongitude(gpsData.getLongitude()), headString, PamCalendar.formatDateTime(rxStats.getLastDataTime()));
		
		String buoyStatus = getExtendedStatus(rxStats);//"";//"<div id=\"myCanvas\"></div>";//getSVG();//String.format("<hr width=\"5\" size=\"10\" noshade><hr width=\"5\" size=\"5\" noshade>");
		
		
		String fullHoverText = headerData+buoyStatus+"</html>";
		return fullHoverText;
	}
	
	private String getDrawingScript() {
		String tooltip =
			    "<table cellpadding='1' cellspacing='2' height='16'>" +
			    "<tr height='16'>" +
			    "  <td bgcolor='#4CAF50' width='6' height='1px'></td>" +
			    "  <td bgcolor='#4CAF50' width='6' height='2px'></td>" +
			    "  <td bgcolor='#4CAF50' width='6' height='3px'></td>" +
			    "  <td bgcolor='#4CAF50' width='6' height='0px'></td>" +
			    "</tr>" +
			    "</table>";

		return tooltip;
	}
	
	private String getNetworkStatusHTML(BuoyStatusDataUnit buoyStatus) {
		String lastCommPing = PamCalendar.formatDateTime(buoyStatus.getLastCommsPing());
		if(buoyStatus.getLastCommsPing()==0) {
			lastCommPing = "Unknown";
		}
		String buoyIpAddr = buoyStatus.getIPAddr();
		if(buoyIpAddr==null) {
			buoyIpAddr = "Unknown";
		}
		String signalStrengthdB = buoyStatus.getCommunicationsStrength()+" dBm";
		String networkStatusHTML = String.format("<p>Last Signal from CAB: %s</p><p>Current CAB IP Address: %s</p><p>Signal Strength: %s</p>",lastCommPing,buoyIpAddr,signalStrengthdB);
		return networkStatusHTML;
	}
	
	private String getHousingStatusHTML(BuoyStatusDataUnit buoyStatus) {
		
		
		double temp = buoyStatus.getBuoyStatusData().getTemp();
		double voltage = buoyStatus.getBuoyStatusData().getVoltage();
		double hum = buoyStatus.getBuoyStatusData().getHumidity();
		double power = buoyStatus.getBuoyStatusData().getPower();

		
		String housingStatusHTML = String.format("<p>Buoy Housing power draw: %s W</p><p>Buoy Housing battery voltage: %s V</p><p>Buoy Housing temperature: %s °C</p><p>Buoy Housing humidity: %s %%</p>",power,voltage,temp,hum);
		return housingStatusHTML;
	}
	
	private String getExtendedStatus(BuoyStatusDataUnit buoyStatus) {
		
		
		String networkStatusHTML = getNetworkStatusHTML(buoyStatus);
		
		String housingStatusHTML = getHousingStatusHTML(buoyStatus);
		
		String cabStatusHTML = "<br><br><strong>Buoy Housing</strong>"+housingStatusHTML+"<strong>Communications</strong>"+networkStatusHTML;
		
		return cabStatusHTML;
		
		
		/*return String.format("<table width=\"100%%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\r\n"
		+ "  <tr>\r\n"
		+ "    <!-- Left Bar -->\r\n"
		+ "    <td width=\"50%%\" bgcolor=\"#f1f1f1\" valign=\"top\">\r\n"
		+ "      <h3>Buoy Housing</h3>\r\n"
		+ "      %s\r\n"
		+ "      <br><br><br><br><br><br><br>\r\n"
		+ "    </td>\r\n"
		+ "    \r\n"
		+ "    <!-- Right Bar -->\r\n"
		+ "    <td width=\"50%%\" bgcolor=\"#cccccc\" valign=\"top\">\r\n"
		+ "      <h3>Communications</h3>\r\n"
		+ "      %s\r\n"
		+ "      <br><br><br><br><br><br><br>\r\n"
		+ "    </td>\r\n"
		+ "  </tr>\r\n"
		+ "</table>",housingStatusHTML,networkStatusHTML);*/
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
