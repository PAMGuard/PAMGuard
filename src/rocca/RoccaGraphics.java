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
package rocca;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import PamView.GeneralProjector;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;

/**
 * @author Michael Oswald
 * 
 * THIS OBJECT IS OUTDATED AND NOT CURRENTLY USED.  METHODS HAVE BEEN COMMMENTED OUT
 * <p>
 * Shamelessly copied from WhistleGraphics.java
 */
public class RoccaGraphics extends PamDetectionOverlayGraphics {

	RoccaProcess roccaProcess;
	
	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_STAR, 8, 8, true,
					Color.BLUE, Color.BLUE);

	Color[] contourColours = { Color.RED, Color.GREEN, Color.CYAN,
			Color.ORANGE, Color.PINK, Color.MAGENTA };

	int iCol = 0;

	public RoccaGraphics(RoccaProcess roccaProcess, RoccaLoggingDataBlock rldb) {
		super(rldb, new PamSymbol(defaultSymbol));
		this.roccaProcess = roccaProcess;
//		if (getPamSymbol() == null) {
//			PamSymbol mapSymbol = new PamSymbol(PamSymbolType.SYMBOL_STAR, 8, 8, true,
//					Color.BLUE, Color.BLUE);
//			setPamSymbol(mapSymbol);
//		}
	}

	@Override
	protected Rectangle drawOnSpectrogram(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		return super.drawOnSpectrogram(g, pamDataUnit, generalProjector);
	}

//
//	Rectangle drawContourShape(Graphics g, RoccaSightingDataUnit rsdu,
//			GeneralProjector projector) {
//
//        // get the contour block from the sighting dataunit that was passed
//        RoccaContourDataBlock rcdb = rsdu.getContour();
//
//		Graphics2D g2D = (Graphics2D) g;
//
//        // cycle to the next color
//        g2D.setColor(contourColours[iCol]);
//		if (++iCol >= contourColours.length) {
//			iCol = 0;
//		}
//		g2D.setStroke(new BasicStroke(3));
//
//		// x = overlayInfo.
//		// int x1, x2, y1, y2;
//		Coordinate3d p1, p2;
//		int xmin, xmax, ymin, ymax;
//		double lastTime = 0;
//
//		p1 = p2 = projector.getCoord3d(rcdb.getFirstUnit().getStartSample(),
//				rcdb.getFirstUnit().getPeakFreq(), 0);
//		xmin = xmax = (int) p1.x;
//		ymin = ymax = (int) p1.y;
//
//		RoccaContourDataUnit peak;
//		for (int i = 1; i < rcdb.getUnitsCount(); i++) {
//
//			peak = rcdb.getDataUnit(i, RoccaContourDataBlock.REFERENCE_CURRENT);
//
//			p2 = projector.getCoord3d(peak.getStartSample(), peak.getPeakFreq(), 0);
//
//            /*
//             * what does this next line do?  Is rsdu the correct field?
//             */
//			projector.addHoverData(p2, rsdu);
//
//			if (peak.getMeanTime() == lastTime) {
//				System.out.println("Repeat slice in contour");
//			}
//			lastTime = peak.getMeanTime();
//
//			if (p2.x > p1.x) {
//				g.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
//			}
//
//			ymin = Math.min(ymin, (int) p2.y);
//			ymax = Math.max(ymax, (int) p2.y);
//			p1.assign(p2);
//		}
//		xmax = (int) p2.x;
//
//		Rectangle rectangle = new Rectangle((int) p1.x, ymin,
//				(int) (p2.x - p1.x), ymax - ymin);
//
//
//		return rectangle;
//	}
}
