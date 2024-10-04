/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package whistleDetector;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import PamUtils.Coordinate3d;
import PamView.GeneralProjector;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;

/**
 * @author Doug Gillespie
 * 
 * Implementation of PanelOverlayDraw for whistles so that the spectrogram can
 * draw whistles over the spectrogram data
 */
public class WhistleGraphics extends PamDetectionOverlayGraphics {

	private WhistleDetector whistleDetector;


	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_DIAMOND, 10, 10, true,
			Color.RED, Color.RED);



	Color[] whistleColours = { Color.RED, Color.GREEN, Color.CYAN,
			Color.ORANGE, Color.PINK, Color.MAGENTA };

//	int iCol = 0;

	public WhistleGraphics(WhistleDetector whistleDetector) {
		super(whistleDetector.whistleDataBlock, new PamSymbol(defaultSymbol));
		this.whistleDetector = whistleDetector;	
		setDefaultRange(1000);
//		if (getPamSymbol() == null) {
//			PamSymbol mapSymbol = new PamSymbol(PamSymbolType.SYMBOL_STAR, 8, 8, true,
//					Color.BLUE, Color.BLUE);
//			setPamSymbol(mapSymbol);
//		}
		//this.fftProcess = fftProcess;
	}

//	public boolean canDraw(GeneralProjector projector) {
//		// Map
//		if (projector.getParmeterType(0) == ParameterType.LONGITUDE
//				&& projector.getParmeterType(1) == ParameterType.LATITUDE)
//			return true;
//		// spectrogram
//		if (projector.getParmeterType(0) == ParameterType.TIME
//				&& projector.getParmeterType(1) == ParameterType.FREQUENCY)
//			return true;
//		// Radar plot
//		if (projector.getParmeterType(0) == ParameterType.BEARING &&
//				projector.getParmeterType(1) == ParameterType.AMPLITUDE) {
//			return true;
//		}
//		return false;
//	}

//	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit,
//			GeneralProjector projector) {
//		
//		ShapeDataUnit shapeDataUnit = (ShapeDataUnit) pamDataUnit;
//		
//		if (projector.getParmeterType(0) == ParameterType.LONGITUDE
//				&& projector.getParmeterType(1) == ParameterType.LATITUDE) {
//			return drawOnMap(g, shapeDataUnit, projector);
//		} else if (projector.getParmeterType(0) == ParameterType.TIME
//				&& projector.getParmeterType(1) == ParameterType.FREQUENCY) {
//			return drawWhistleShape(g, shapeDataUnit, projector);
//		} else if (projector.getParmeterType(0) == ParameterType.BEARING &&
//				projector.getParmeterType(1) == ParameterType.AMPLITUDE) {
//			return drawAmplitudeOnRadar(g, shapeDataUnit, projector);
////			return drawWhistleOnRadar(g, shapeDataUnit, projector);
//		}
//		return null;
//	}

//	Rectangle drawWhistleOnMap(Graphics g, ShapeDataUnit pamDataUnit,
//			GeneralProjector projector) {
//
//		Rectangle r = new Rectangle();
//		GpsData gpsData;
//		Graphics2D g2 = (Graphics2D) g;
//		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
//		LatLong latLong = array.getHydrophoneLocator().getPhoneLatLong(pamDataUnit.getTimeMilliseconds(), 0);
//		Coordinate3d coord = projector.getCoord3d(latLong.getLatitude(),
//				latLong.getLongitude(), 0);
//
//		getPamSymbol().draw(g2, coord.getXYPoint());
//		
//		projector.addHoverData(coord, pamDataUnit);
//
//		return r;
//	}

	@Override
	protected Rectangle drawOnSpectrogram(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
//		super.drawOnSpectrogram(g, pamDataUnit, generalProjector);
		return drawWhistleShape(g, (ShapeDataUnit) pamDataUnit, generalProjector);
	}
	Rectangle drawWhistleShape(Graphics g, ShapeDataUnit pamDataUnit,
			GeneralProjector projector) {
		WhistleShape whistleShape = pamDataUnit.getWhistleShape();
		if (whistleShape.getSliceCount() < 2)
			return null;

		Graphics2D g2D = (Graphics2D) g;
		// Graphics g = image.getGraphics();
		int iCol = pamDataUnit.getAbsBlockIndex()%whistleColours.length;
		g2D.setColor(whistleColours[iCol]);
		g2D.setStroke(new BasicStroke(3));

		// x = overlayInfo.
		// int x1, x2, y1, y2;
		Coordinate3d p1, p2;
		int xmin, xmax, ymin, ymax;
		long lastTime = 0;

		WhistlePeak firstPeak = whistleShape.GetPeak(0);
		long startMillis = pamDataUnit.getTimeMilliseconds();
		long startSample = firstPeak.startSample;
		double millisPerSample = 1000./getParentDataBlock().getSampleRate();
		double sliceMillis;

		p1 = p2 = projector.getCoord3d(startMillis,
				whistleDetector.binsToHz(firstPeak.PeakFreq), 0);
		xmin = xmax = (int) p1.x;
		ymin = ymax = (int) p1.y;
		
		WhistlePeak peak;
		for (int i = 1; i < whistleShape.getSliceCount(); i++) {

			peak = whistleShape.GetPeak(i);

			sliceMillis = startMillis + (peak.startSample-startSample)*millisPerSample;
			p2 = projector.getCoord3d(sliceMillis, whistleDetector.binsToHz(peak.PeakFreq), 0);
			projector.addHoverData(p2, pamDataUnit);

			if (peak.startSample == lastTime) {
				System.out.println("Repeat slice in whistle");
			}
			lastTime = peak.startSample;

			if (p2.x > p1.x) {
				g.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
			}

			ymin = Math.min(ymin, (int) p2.y);
			ymax = Math.max(ymax, (int) p2.y);
			p1.assign(p2);
		}
		xmax = (int) p2.x;

//		firstPeak = whistleShape.GetPeak(0);
//		p1 = projector.getCoord3d(firstPeak.startSample,
//				whistleDetector.binsToHz(firstPeak.PeakFreq), 0);
//		String txt = String.format("%d", whistleShape.getLastPeak().getSliceNo());
//		g.drawString(txt,(int) p1.x - g.getFontMetrics().stringWidth(txt), (int) p1.y);
//		System.out.println(String.format(" start slice %d", firstPeak.getAbsoluteSliceNumber()));

		Rectangle rectangle = new Rectangle((int) p1.x, ymin,
				(int) (p2.x - p1.x), ymax - ymin);


		return rectangle;
	}
	/*Rectangle drawWhistleOnRadar(Graphics g, ShapeDataUnit pamDataUnit,
			GeneralProjector projector) {

//		Rectangle r = null;
//		
//		 param 0 = bearing
//		 param 1 = amplitude.
//		 Just draw a really simple dot for now. 
//		
		WhistleShape whistle = pamDataUnit.getWhistleShape();
		if (whistle.delay == WhistleLocaliser.WHISTLE_NODELAY) return null;
		double angle = whistle.delay;
		int channelList = pamDataUnit.getChannelBitmap();
//		channelList |= (1<<whistleDetector.whistleControl.whistleParameters.bearingChannel);
		
		AcquisitionProcess daqProcess = (AcquisitionProcess) whistleDetector.getSourceProcess();
		int hydrophoneList = daqProcess.getAcquisitionControl().ChannelsToHydrophones(channelList);
		double ang = (double) whistle.delay / whistleDetector.getSampleRate()
		/ ArrayManager.getArrayManager().getCurrentArray().getSeparationInMillis(hydrophoneList);
		ang = Math.min(1., Math.max(ang, -1.));
		ang =  Math.acos(ang) * 180. / Math.PI;
		double amplitude = whistle.getDBAmplitude();
		
		Coordinate3d coord = projector.getCoord3d(ang, amplitude, 0);
		if (coord == null) return null;
//		PamSymbol symbol = ClickBTDisplay.getClickSymbol(clickDetector.getClickIdentifier(), click);
		getPamSymbol().draw(g, coord.getXYPoint());
		projector.addHoverData(coord, pamDataUnit);
		return r;
	}*/
//
//	public PamKeyItem createKeyItem(GeneralProjector generalProjector, int keyType) {
//		// TODO Auto-generated method stub
//		return null;
//	}
////	
//	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit) {
//		String str =  "<html>Whistle";
//		str += String.format("<br> Channels: %s", PamUtils.getChannelList(dataUnit.getChannelBitmap()));
//		str += "</html>";
//		return str;
//	}


	@Override
	protected Rectangle drawOnMap(Graphics g, PamDataUnit pamDetection, GeneralProjector generalProjector) {
		// TODO Auto-generated method stub
		return super.drawOnMap(g, pamDetection, generalProjector);
	}

	@Override
	protected Rectangle drawAmplitudeOnRadar(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		// TODO Auto-generated method stub
		return super.drawAmplitudeOnRadar(g, pamDataUnit, generalProjector);
	}

	@Override
	protected Rectangle drawRangeOnRadar(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		// TODO Auto-generated method stub
		return super.drawRangeOnRadar(g, pamDataUnit, generalProjector);
	}

}
