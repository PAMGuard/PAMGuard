/*
 * Copyright 1999-2004 Carnegie Mellon University.
 * Portions Copyright 2002-2004 Sun Microsystems, Inc.
 * Portions Copyright 2002-2004 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "README" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 */

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

/*
 * Adapted for use in Rocca/PAMGUARD by Michael Oswald June 2010
 *
 */


package rocca;

import PamView.ColourArray;
import PamView.ColourArray.ColourArrayType;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageObserver;
import java.awt.image.ReplicateScaleFilter;
import java.util.ArrayList;

import javax.swing.JPanel;



/**
 *
 * @author Michael Oswald
 */
/**
 * Creates a graphical representation from a matrix.  Like Matlab's
 * imagesc.
 */
public class SpectrogramPanel extends JPanel {

    /**
     * Reference to the pop-up window containing the spectrogram
     */
    private RoccaSpecPopUp roccaSpecPopUp;

    /**
     * The spectrogram image
     */
    private BufferedImage spectrogram = null;

    /**
     * A scaled version of the spectrogram image.
     */
    private Image scaledSpectrogram = null;

    /**
     * The filter used to scale the spectrogram image
     */
    private ImageFilter scaleFilter = null;

    /**
     * The zooming factor.
     */
    private float zoom = 1.0f;
    private float vzoom = 1.0f;

    /**
     * used to control the brightness of the spectrogram
     */
    private double colorOffset;

    /**
     * used to control the contrast of the spectrogram
     */
    private double colorScale;

    /**
     * The data matrix to be displayed
     */
    private double[][] data;

    /**
     * The matrix containing the contour overlay info;  each point consists of
     * an x-value (the time bin) and a y-value (the frequency bin).
     */
    private Point[] contour;

    /**
     * the width (number of columns, or time bins) of the spectrogram
     */
    private int width;

    /**
     * the height (number of rows, or frequency bins) of the spectrogram
     */
    private int height;

    /**
     * the number of colors to use when creating the color array
     */
    private static final int NUMOFCOLORS = 256;

    /**
     * the type of color array to create
     */
    private static final ColourArray.ColourArrayType COLOURARRAYTYPE =
            ColourArray.ColourArrayType.GREY;
    
    /**
     * the ColourArray
     */
    private ColourArray colourArray =
            ColourArray.createStandardColourArray(NUMOFCOLORS, COLOURARRAYTYPE );

    /**
     * minimum value of the FFT
     */
    private double minFFT;

    /**
     * maximum value of the FFT
     */
    private double maxFFT;

    /**
     * Creates a new SpectrogramPanel for the given data matrix
     */
    public SpectrogramPanel(RoccaSpecPopUp roccaSpecPopUp) {
        this.roccaSpecPopUp = roccaSpecPopUp;
        this.data = roccaSpecPopUp.getData();
        width = data.length;
        height = data[0].length;
        initialSetup();
    }

    /**
     * Initialize variables upon object creation
     */
    private void initialSetup() {
        maxFFT = 0;
        minFFT = Integer.MAX_VALUE;
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                if (data[x][y] > maxFFT) {
                    maxFFT = data[x][y];
                }
                if (data[x][y] < minFFT) {
                    minFFT = data[x][y];
                }
            }
        }

        /* set the preferred size of the spectrogram panel */
        Dimension d = new Dimension(width, height);
//        setMinimumSize(d);
//        setMaximumSize(d);
        setPreferredSize(d);

        /* Set the colourArrayType to be the same as currently being used on the
         * spectrogram.  Note that if this came from a TD Display instead of a Swing
         * display, then the ColourArray object is different and we will just default to
         * a grey colour scheme
         */
        if (roccaSpecPopUp.display!=null) {
	        colourArray =
	            ColourArray.createStandardColourArray(
	            NUMOFCOLORS,
	            roccaSpecPopUp.display.getSpectrogramParameters().getColourMap() );
        } else {
	        colourArray =
		            ColourArray.createStandardColourArray(
		            NUMOFCOLORS,
            ColourArrayType.GREY);
        }
               
        /* Set ColorScale to span the min/max of the FFT data.  Reset
         * ColorOffset
         */
        colorOffset = 0;
        colorScale = (((colourArray.getNumbColours()-1) +
                colorOffset) / (maxFFT-minFFT));

        /* create a new spectrogram image */
        spectrogram = new BufferedImage(width,
                                        height,
                                        BufferedImage.TYPE_INT_RGB);

        /* set the colors and scale, and create the spectrogram image to be
         * displayed
         */
        setColors();
        scaleFilter = new ReplicateScaleFilter(width, height);
        computeSpectrogram();
    }

    /**
     * Create the scaled spectrogram image
     */
    private void computeSpectrogram() {

        /* create the scaled spectrogram image */
        scaledSpectrogram =
            createImage(new FilteredImageSource(spectrogram.getSource(),
                                                scaleFilter));
        Dimension sz = getSize();
        revalidate();
        repaint();
//        repaint(0, 0, 0, sz.width - 1, sz.height - 1);
    }


    /**
     * Set the colors of the spectrogram, based on the FFT values
     */
    public void setColors() {
        int i;
        int j;
        int maxYIndex = height - 1;

        /* loop through the FFT data, setting the color for each pixel in the
         * spectrogram based on the value of the FFT
         */
        for (i = 0; i < width; i++) {
            for (j = maxYIndex; j >= 0; j--) {
                /* calculate the index in the colourArray based on the FFT value.
                 * Keep the value within the bounds of the colourArray
                 */
                int colorIndex = (int)((data[i][j] - minFFT) * colorScale
                                  - colorOffset);
                if (colorIndex<0) {
                    colorIndex=0;
                } else if (colorIndex>colourArray.getNumbColours()-1) {
                    colorIndex=colourArray.getNumbColours()-1;
                }
                //System.out.println(colorIndex);

                /* set the pixel color */
                spectrogram.setRGB(i,
                        maxYIndex - j,
                        colourArray.getColour(colorIndex).getRGB());
            }
        }
    }

    /**
     * Updates the offset factor used to calculate brightness.  Increasing this
     * variable increases the brightness of the spectrogram, while decreasing it
     * has the opposite effect
     *
     * @param colorOffset the offset factor used to calculate the brightness
     */
    public void setColorOffset(double colorOffset) {
        this.colorOffset = colorOffset;
        setColors();
        computeSpectrogram();
    }

    public double getColorOffset() {
        return this.colorOffset;
    }

    /**
     * Updates the scale factor used to calculate contrast.  Increasing this
     * variable increases the contrast of the spectrogram, while decreasing it
     * has the opposite effect
     *
     * @param colorScale the scaling factor used to calculate the contrast
     */
    public void setColorScale(double colorScale) {
        this.colorScale = colorScale;
        setColors();
        computeSpectrogram();
    }

    public double getColorScale() {
        return colorScale;
    }

    /**
     * Zoom the image in the vertical direction, preparing for new
     * display.
     */
    protected void vzoomSet(float vzoom) {
        this.vzoom = vzoom;
        zoom();
    }

    /**
     * Zoom the image in the horizontal direction, preparing for new
     * display.
     */
    protected void hzoomSet(float zoom) {
        zoomSet(zoom);
    }

    /**
     * Zoom the image in the horizontal direction, preparing for new
     * display.
     */
    protected void zoomSet(float zoom) {
        this.zoom = zoom;
        zoom();
    }

    private void zoom() {
        if (spectrogram != null) {

            int displayWidth = spectrogram.getWidth();
            int displayHeight = spectrogram.getHeight();

            // do the zooming
            displayWidth = (int) (zoom * displayWidth);
            displayHeight = (int)(vzoom*displayHeight);

            scaleFilter =
                new ReplicateScaleFilter(displayWidth, displayHeight);
            scaledSpectrogram =
                createImage(new FilteredImageSource(spectrogram.getSource(),
                                                    scaleFilter));

            // so ScrollPane gets notified of the new size:
            setPreferredSize(new Dimension(displayWidth, displayHeight));
            roccaSpecPopUp.getFreqAxis().setPreferredSize
                    (new Dimension(RoccaSpecPopUp.AXIS_SIZE, displayHeight));
//            roccaSpecPopUp.getTimeAxis().setPreferredSize
//                    (new Dimension(displayWidth, RoccaSpecPopUp.AXIS_SIZE));
            revalidate();

            repaint();
        }
    }

    public float getVZoom() {
        return vzoom;
    }

    public float getHZoom() {
        return zoom;
    }

    public double getData(int x, int y) {
        return data[x][y];
    }

    public int getDataWidth() {
        return width;
    }

    public int getDataHeight() {
        return height;
    }


    /**
     * Paint the component.  This will be called by AWT/Swing.
     *
     * @param g The <code>Graphics</code> to draw on.
     */
    @Override
    public void paintComponent(Graphics g) {
        /**
         * Fill in the whole image with white.
         */
        Dimension sz = getSize();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, sz.width - 1, sz.height - 1);

        if(spectrogram != null) {
            g.drawImage(scaledSpectrogram, 0, 0, (ImageObserver) null);
        }

        /**
         * draw the contour, unless the contour is turned off
         *
         */
        this.contour = roccaSpecPopUp.getContour();
        if (contour!=null && 
                roccaSpecPopUp.getContourState()==RoccaSpecPopUp.CONTOURON) {
            int maxYIndex = height - 1;
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(4.0f));
            g2.setColor(new Color(16777062));
            for (int i=0; i<contour.length-1; i++) {

                /* only draw a line if the contour is present in the current
                 * and next time slices.  If the value in the frequency bin is
                 * set to the constant NO_CONTOUR_HERE, it means we are either
                 * before or after the contour
                 */
                if ((contour[i].getY() != RoccaParameters.NO_CONTOUR_HERE) &&
                    (contour[i+1].getY() != RoccaParameters.NO_CONTOUR_HERE)) {
                    g2.draw(new Line2D.Double(
                        zoom*contour[i].getX(),
                        vzoom*(maxYIndex-contour[i].getY()),
                        zoom*contour[i+1].getX(),
                        vzoom*(maxYIndex-contour[i+1].getY())));
                }
            }

            /* draw the highpass and lowpass filter lines.  Figure out first
             * which bins are closest to the filter frequencies, then draw them
             */
            double[] freqBins = roccaSpecPopUp.getFreqBins();
            double lowpassFreq = roccaSpecPopUp.getLowPassFreq();
            double highpassFreq = roccaSpecPopUp.getHighPassFreq();
            double startX = contour[0].getX();
            double endX = contour[contour.length-1].getX();
            int lowpassBin=height-1;
            int highpassBin=0;

            for (int i=0; i<freqBins.length; i++) {
                if (freqBins[i]>=highpassFreq) {
                    highpassBin = i;
                    break;
                }
            }
            for (int i=freqBins.length-1; i>=0; i--) {
                if (freqBins[i]<=lowpassFreq) {
                    lowpassBin = i;
                    break;
                }
            }

            g2.setStroke(new BasicStroke(2.0f));
            g2.setColor(Color.RED);
            g2.draw(new Line2D.Double(
                zoom*startX,
                vzoom*(maxYIndex-lowpassBin),
                zoom*endX,
                vzoom*(maxYIndex-lowpassBin)));
            g2.draw(new Line2D.Double(
                zoom*startX,
                vzoom*(maxYIndex-highpassBin),
                zoom*endX,
                vzoom*(maxYIndex-highpassBin)));
        }

        /**
         * highlight the peak frequencies used to construct the contour
         */
        if (roccaSpecPopUp.showPeaks()) {
            int maxYIndex = height - 1;
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(13434777));
            for (int i=0; i<contour.length-1; i++) {
                g2.draw(new Ellipse2D.Double(
                        zoom*contour[i].getX()-2,
                        vzoom*(maxYIndex-contour[i].getY())-2,
                        6,
                        6));
            }
        }

        /**
         * if we're dragging a point, make it extra big
         */
        if (roccaSpecPopUp.getDragPoint()!=null) {
            int maxYIndex = height - 1;
            Point p = roccaSpecPopUp.getDragPoint();
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(16711680));
            g2.draw(new Ellipse2D.Double(
                    zoom*p.getX()-5,
                    vzoom*(maxYIndex-p.getY())-5,
                    14,
                    14));
        }
        
        /**
         * if we're tracing out a contour, plot the points
         */
        if (roccaSpecPopUp.tracingNow()) {
            int maxYIndex = height - 1;
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(13434777));
            ArrayList<Point> userPoints = roccaSpecPopUp.getUserPoints();
            for (int i=0; i<userPoints.size(); i++) {
                g2.draw(new Ellipse2D.Double(
                        zoom*userPoints.get(i).getX()-2,
                        vzoom*(maxYIndex-userPoints.get(i).getY())-2,
                        6,
                        6));
            }
        }
    }

    public Dimension getScaledImageSize() {
        return new Dimension(
                (int) (spectrogram.getWidth()*zoom),
                (int) (spectrogram.getHeight()*vzoom));
    }


}
