/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;

import Layout.PamAxis;

/**
 *
 * @author Michael Oswald
 */
public class RoccaAxis extends JComponent {
    
    /**
     * The RoccaSpecPopUp Object object creating this instance
     */
    RoccaSpecPopUp rspu;

    /**
     * The PamAxis object
     */
    PamAxis pamAxis;

    /**
     * Valid orientations of the axis
     */
    public enum Orientation {
        HORIZONTAL, VERTICAL;
    }

    /**
     * orientation of the current axis
     */
    Orientation orientation;

    /**
     * Main Constructor
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param minVal
     * @param maxVal
     * @param aboveLeft
     * @param label
     * @param format
     * @param horizontal
     */
	public RoccaAxis(int x1, int y1, int x2, int y2, double minVal,
			double maxVal, boolean aboveLeft, String label, String format,
            Orientation o, RoccaSpecPopUp rspu) {
        super();
		pamAxis = new PamAxis(x1, y1, x2, y2, minVal, maxVal, aboveLeft, label, format);
        this.orientation = o;
        this.rspu = rspu;
	}

    @Override
	protected void paintComponent(Graphics g) {
        /* note: it would be faster to only draw the clipped area (see commented
         * drawAxis method calls, below) but to do that we would need
         * to calculate the min/max values that are currently being displayed
         */
        /* get the size of the clip */
        Rectangle drawHere = g.getClipBounds();

        /* get the dimensions of the scaled image (overall size, in pixels) */
        Dimension imageSize = rspu.getDataPanel().getScaledImageSize();


        /* set the range and draw the axis */
        if (orientation == Orientation.HORIZONTAL) {
            pamAxis.setRange(rspu.getTimeBins()[0], rspu.getTimeBins()[rspu.getTimeBins().length-1]);
            pamAxis.drawAxis(g, 0, drawHere.y, imageSize.width, drawHere.y);

        } else {

            /* if the max freq value is > 2000, display in kHz */
			double fScale = 1;
			if ( rspu.getFreqBins()[rspu.getFreqBins().length-1] > 2000) {
				fScale = 1000;
			}

            /* force the first freq value to be 0.0kHz */
            pamAxis.setForceFirstVal(true, 0.0);

            /* set the scale to the full range and draw the axis over the
             * whole image height.  Note that the scrollbar will only show
             * the portion of the axis that is visible in the window
             */
            pamAxis.setRange(rspu.getFreqBins()[0]/fScale,
                    rspu.getFreqBins()[rspu.getFreqBins().length-1]/fScale);
            pamAxis.drawAxis(g, drawHere.x+drawHere.width,
                    imageSize.height, drawHere.x+drawHere.width, 0);
        }
    }


}
