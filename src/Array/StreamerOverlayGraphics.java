package Array;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Window;

import Array.streamerOrigin.HydrophoneOriginMethod;
import GPS.GpsData;
import GPS.GpsDataUnit;
import PamUtils.Coordinate3d;
import PamUtils.PamCalendar;
import PamView.GeneralProjector;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PanelOverlayDraw;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;

public class StreamerOverlayGraphics extends PanelOverlayDraw {
	


	public static final SymbolData streamerSymbol = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 4, 4, true, Color.BLUE, Color.RED);
	
	public StreamerOverlayGraphics() {
		super(new PamSymbol(streamerSymbol));
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {
		if (canDraw(generalProjector)) {
			return drawOnMap(g, pamDataUnit, generalProjector);
		}
		return null;
	}
	
	public Rectangle drawOnMap(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {

		/**
		 * Note that this currently gets null for gps origin streamers. 
		 */
		GpsData gpsData = extractGpsData((StreamerDataUnit) pamDataUnit, pamDataUnit.getTimeMilliseconds());
		if (gpsData == null) {
			return null;
		}
		Coordinate3d pos = generalProjector.getCoord3d(gpsData.getLatitude(), gpsData.getLongitude(), gpsData.getHeight());
		Rectangle r =getPamSymbol(pamDataUnit, generalProjector).draw(g, pos.getXYPoint());
		generalProjector.addHoverData(pos, pamDataUnit);
		return r;
	}
	
	private GpsData extractGpsData(StreamerDataUnit streamerDataUnit, long timeMillis) {
		return streamerDataUnit.getGpsData();
//		Streamer streamer = streamerDataUnit.getStreamerData();
//		if (streamer == null) {
//			return null;
//		}
//		HydrophoneOriginMethod hOrigin = streamer.getHydrophoneOrigin();
//		if (hOrigin == null) {
//			return null;
//		}
//		GpsDataUnit gpsDataUnit = hOrigin.getGpsData(streamerDataUnit.getTimeMilliseconds());
//		if (gpsDataUnit == null) {
//			return null;
//		}
//		return gpsDataUnit.getGpsData();
	}

	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		return (parameterTypes[0] == GeneralProjector.ParameterType.LATITUDE && 
				parameterTypes[1] == GeneralProjector.ParameterType.LONGITUDE);
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector,
			int keyType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector,
			PamDataUnit dataUnit, int iSide) {
		StreamerDataUnit sdu = (StreamerDataUnit) dataUnit;
		Streamer streamer = sdu.getStreamerData();
		String str = String.format("<html>Streamer %s, ind %d<p>%s<p>Locator: %s<p>Origin: %s",
				streamer.getStreamerName(), streamer.getStreamerIndex(), PamCalendar.formatDateTime(sdu.getTimeMilliseconds()),
				streamer.getHydrophoneLocator().getName(), streamer.getHydrophoneOrigin().getName());
//		if (streamer.isEnableOrientation()) {
			if (streamer.getHeading() != null) {
				str += String.format("<p>Heading: %s\u00B0", formatAngle(streamer.getHeading())); 
			}
			if (streamer.getPitch() != null) {
				str += String.format("<p>Pitch: %s\u00B0", formatAngle(streamer.getPitch())); 
			}
			if (streamer.getRoll() != null) {
				str += String.format("<p>Roll: %s\u00B0", formatAngle(streamer.getRoll())); 
			}
//		}
//		str += "<\\html>";
		return str;
	}
	
	private String formatAngle(Double angle) {
		if (angle == null) {
			return "null";
		}
		else {
			return String.format("%3.1f", angle);
		}
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
