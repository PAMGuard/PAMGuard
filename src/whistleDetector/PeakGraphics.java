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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.ArrayList;

import PamUtils.Coordinate3d;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PanelOverlayDraw;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;

/**
 * @author Doug Gillespie
 * 
 * Implementation of PanelOverlayDraw for whistle peaks so that the spectrogram
 * can draw peaks over the spectrogram data
 * 
 */
public class PeakGraphics extends PanelOverlayDraw {

	WhistleControl whistleControl;
	WhistleDetector whistleDetector;
	
	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 10, 10, false, Color.BLUE, Color.BLUE);

	public PeakGraphics(WhistleControl whistleControl, WhistleDetector whistleDetector) {
		super(new PamSymbol(defaultSymbol));
		this.whistleControl = whistleControl;
		this.whistleDetector = whistleDetector;
	}

	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		if (parameterTypes[0] == ParameterType.TIME
				&& parameterTypes[1] == ParameterType.FREQUENCY)
			return true;
		return false;
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector projector) {
		if (projector.getParmeterType(0) == ParameterType.TIME
				&& projector.getParmeterType(1) == ParameterType.FREQUENCY) {
			return drawPeakShape(g, (PeakDataUnit) pamDataUnit, projector);
		}
		return null;
	}

	Rectangle drawPeakShape(Graphics g, PeakDataUnit pamDataUnit,
			GeneralProjector projector) {
		Rectangle outerRectangle = null;

		// Graphics2D g = (Graphics2D) image.getGraphics();

		ArrayList<WhistlePeak> peaks = pamDataUnit.getWhistlePeaks();
		if (peaks.size() == 0) {
			return null;
		}

		Coordinate3d p1, p2;

		p1 = projector.getCoord3d(pamDataUnit.getTimeMilliseconds(),
				pamDataUnit.getFrequency()[0], 0);
		p2 = projector.getCoord3d(pamDataUnit.getTimeMilliseconds()
				+ pamDataUnit.getDurationInMilliseconds()*0, pamDataUnit.getFrequency()[1], 0);

		outerRectangle = new Rectangle((int) p1.x, (int) p1.y,
				(int) (p2.x - p1.x), (int) (p2.y - p1.y));
		Rectangle peakRectangle = new Rectangle((int) p1.x, 0,
				(int) (p2.x - p1.x), 10);
		WhistlePeak peak;
		/*
		 * Need to convert these frequenies from bins to Hz to work with the
		 * latest iteration of the projector. 
		 */

//		Wh parentFFTProcess = whistleControl.whistleDetector.getParentFFTDataSource();
		double bin2Hz = whistleDetector.getSampleRate() / whistleDetector.getFftLength();
		for (int i = 0; i < peaks.size(); i++) {
//			g.setColor(PamColors.getInstance().getWhaleColor(i));
			g.setColor(Color.BLUE);
			peak = peaks.get(i);
			peakRectangle.y = (int) projector.getCoord3d(0, peak.MaxFreq * bin2Hz, 0).y;
			peakRectangle.height = (int) projector.getCoord3d(0, peak.MinFreq * bin2Hz,
					0).y - peakRectangle.y;
			g.drawRect(peakRectangle.x, peakRectangle.y, peakRectangle.width,
					peakRectangle.height);
			
			peakRectangle.y = (int) projector.getCoord3d(0, peak.PeakFreq * bin2Hz, 0).y;
			peakRectangle.height = 1;
			g.setColor(Color.RED);
			g.drawRect(peakRectangle.x, peakRectangle.y, peakRectangle.width,
					peakRectangle.height);
		}

		return outerRectangle;
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector, int keyType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
//		PeakDataUnit peakDataUnit = (PeakDataUnit) dataUnit;
		return "Whistle Peak";
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
