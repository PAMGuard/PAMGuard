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


package clickDetector;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageObserver;
import java.awt.image.ReplicateScaleFilter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.ColourArray;
import PamView.panel.JBufferedPanel;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import soundtrap.STClickControl;

/**
 * Creates a click ICI histogram display add-in
 *
 * @author Michael Oswald
 */
public class IDI_Display extends ClickDisplay implements PamObserver, PamSettings {

    /**
     * Store the counts in an int array for the high-resolution histogram.
     * The int[] cells will correspond to the IDI bins, and the ArrayList
     * elements will correspond to the time bins.
     */
    private ArrayList<int[]> highResHistograms = new ArrayList<int[]>();

    /**
     * index of the current int[] array in the histogram ArrayList
     */
    private int histTimeIdx;

    /**
     * Store the counts in an int array for the low-resolution histogram.
     * The int[] cells will correspond to the IDI bins, and the ArrayList
     * elements will correspond to the time bins.
     */
    private ArrayList<int[]> lowResHistograms = new ArrayList<int[]>();

    /**
     * {@link IDI_DisplayParams} object
     */
    private IDI_DisplayParams idiParams = new IDI_DisplayParams();

    /**
     * Timer to gather histogram data
     */
    private Timer timer;

    /**
     * Keep track of the time of the last click
     */
    private long prevClickTime = 0;

    /**
     * The outer IDI-Display panel, to be used for the ClickDisplay.setPlotPanel
     * method.  Contains the inner IDI-Display panel
     */
    private IDIOuterPanel idiOuterPanel;

    /**
     * The inner IDI-Display panel, containing the two histogram images.  Used
     * for the ClickDisplay.setLeft/RightPlotPanel methods
     */
    private IDIInnerPanel idiInnerPanel;
//    private IDIInnerPanel idiLeftPanel, idiRightPanel;

    /**
     * Low-resolution horizontal axis object
     */
    private PamAxis lowResAxis;

    /**
     * High-resolution horizontal axis object
     */
    private PamAxis highResAxis;

    /**
     * Time axis (vertical)
     */
    private PamAxis timeAxis;

    /**
     * Number of horizontal bins in the low-resolution histogram
     */
    private int nBinsLow;

    /**
     * Number of horizontal bins in the high-resolution histogram
     */
    private int nBinsHigh;

    /**
     * Number of vertical bins (time axis)
     */
    private int nTimeBins;

    /**
     * Main constructor
     *
     * @param clickControl
     * @param clickDisplayManager
     * @param clickDisplayInfo
     */
	public IDI_Display(ClickControl clickControl, ClickDisplayManager clickDisplayManager,
			ClickDisplayManager.ClickDisplayInfo clickDisplayInfo) {
		super(clickControl, clickDisplayManager, clickDisplayInfo);

        /* register with the settings manager so the parameters get saved */
        PamSettingManager.getInstance().registerSettings(this);

        /* create the panels in the display */
        setAxisPanel(idiOuterPanel = new IDIOuterPanel());
        setPlotPanel(idiInnerPanel = new IDIInnerPanel());
//        setEastPanel(colorBarPanel = new ColorBarPanel());
//        setLeftPlotPanel(idiLeftPanel = new IDIInnerPanel());
//        setRightPlotPanel(idiRightPanel = new IDIInnerPanel());

        /* create a time axis */
        timeAxis = new PamAxis(0, 0, 0, 100,   // x1, y1, x2, y2
                0, idiParams.getMaxTimeBin()/1000/60,        // minVal, maxVal (in minutes)
                true,                              // label to the left of axis
                "Elapsed Time (min)", "%d");                        // label and axis format
        idiOuterPanel.setWestAxis(timeAxis);

        /* clear the histogram images on the screen */
        idiInnerPanel.resetImages(100, 100, 100);
    }

	/**
	 * Constructor needed when creating the SoundTrap Click Detector - need to explicitly cast
	 * from STClickControl to ClickControl, or else constructor fails
	 * @param clickControl
	 * @param clickDisplayManager
	 * @param clickDisplayInfo
	 */
	public IDI_Display(STClickControl clickControl, ClickDisplayManager clickDisplayManager, 
			ClickDisplayManager.ClickDisplayInfo clickDisplayInfo) {
		this((ClickControl) clickControl, clickDisplayManager, clickDisplayInfo);
	}


    /**
     * Sets the axes parameters, based on the current IDI_DisplayParams object.
     * This method is called from PamStart, and also when the Parameters Dialog
     * is closed.
     */
    public void setParameters() {
        /* calculate the number of bins in the low-resolution and high-resolution
         * histograms.  Use the Math.ceil method to round up any partial bins
         * to the next highest integer.  Note that in order to get partial bins,
         * we must first cast one of the parameters to a double (or else we
         * have int/int, and any decimal will automatically be dropped before
         * it can be rounded)
         */
        nBinsLow = (int) Math.ceil(((double) idiParams.getMaxLowResBin())/idiParams.getLowResIdiBinScale());
        nBinsHigh = (int) Math.ceil(((double) idiParams.getMaxHighResBin())/idiParams.getHighResIdiBinScale());
        nTimeBins = (int) Math.ceil(((double) idiParams.getMaxTimeBin())/idiParams.getTimeBinScale());

        /* create new, empty histogram arrays */
        lowResHistograms.clear();
        lowResHistograms.add(new int[nBinsLow]);
        highResHistograms.clear();
        highResHistograms.add(new int[nBinsHigh]);
        histTimeIdx=0;

        /* clear the histogram images on the screen */
        idiInnerPanel.resetImages(nTimeBins, nBinsLow, nBinsHigh);

        /* set the axes and color scales and repaint */
        timeAxis.setMaxVal(idiParams.getMaxTimeBin()/1000/60);
        highResAxis.setMaxVal(idiParams.getMaxHighResBin());
        lowResAxis.setMaxVal(idiParams.getMaxLowResBin());
        idiInnerPanel.updateColorScales();
        idiOuterPanel.repaint();
    }

    /**
     * Creates a new timer and sets the schedule.  If a timer already exists
     * when this method is called, it is canceled first
     */
    public void setTimer() {

        /* if the timer is running, stop it and reset the previous click time
         * counter.
         */
        if (timer!=null) {
            timer.cancel();
            prevClickTime=0;
        }

        /* create a new timer */
        timer = new Timer();

        /* schedule the timer for the timeBin parameter in IDI_DisplayParams */
        timer.scheduleAtFixedRate(new IDI_DisplayTimer(),
                idiParams.getTimeBinScale(),
                idiParams.getTimeBinScale());
    }

    /**
     * On audio start, start the timer and create a new histogram object in the
     * array.  Use the highest bin in the low-resolution display to set the
     * size of the histogram.
     */
    @Override
    public void pamStart() {

        /* troubleshooting */
//        System.out.println("Starting Time = " + System.currentTimeMillis());

        /* set up the axes based on the current parameters */
        setParameters();

        /* set up the timer */
        setTimer();

        /* Subscribe to ClickDetection data object */
        clickControl.clickDetector.getClickDataBlock().addObserver(this);
    }

    /**
     * called when user stops the audio capture
     */
    @Override
    public void pamStop() {
        /* stop the timer and reset the previous click time counter */
        if (timer!=null) {
            timer.cancel();
            timer.purge();
        }
        prevClickTime=0;
    }

    /** On new data, add ICI variable to current histogram */
    @Override
	public void addData(PamObservable o, PamDataUnit arg) {
        ClickDetection click = (ClickDetection) arg;
        double ici = click.getTimeMilliseconds() - prevClickTime;

        /* troubleshooting - print out ici value */
//        System.out.println("Time, ICI = " + System.currentTimeMillis() + "," + ici);

        /* increase the count in the appropriate bin in the high-resolution
         * histogram.  We use the Math.ceil method to round up any partial bins
         * to the next integer.  If the ICI is > max bin, ignore it (we only
         * care about ICI that fall within the user-specified limits)
         */
        int binNumber = (int) Math.ceil(ici / idiParams.getHighResIdiBinScale()) - 1;

        /* if binNumber=-1, it means the ici=0 and should actually be counted
         * in the first bin
         */
        if (binNumber==-1) {
            binNumber=0;
        }
        if (binNumber < nBinsHigh && binNumber >= 0) {
            highResHistograms.get(histTimeIdx)[binNumber]++;
        }

        /* increase the count in the appropriate bin in the low-resolution
         * histogram.  We use the Math.ceil method to round up any partial bins
         * to the next integer.  If the ICI is > max bin, ignore it (we only
         * care about ICI that fall within the user-specified limits)
         */
        binNumber = (int) Math.ceil(ici / idiParams.getLowResIdiBinScale()) - 1;

        /* if binNumber=-1, it means the ici=0 and should actually be counted
         * in the first bin
         */
        if (binNumber==-1) {
            binNumber=0;
        }
        if (binNumber < nBinsLow && binNumber >= 0) {
            lowResHistograms.get(histTimeIdx)[binNumber]++;
        }

        /* save the current click time for comparison with the next click */
        prevClickTime = click.getTimeMilliseconds();
    }
    
	@Override
	public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
		
	}

    /**
     * Timer task - executes whenever the timer hits the user-specified time bin
     */
    class IDI_DisplayTimer extends TimerTask {
        @Override
		public void run() {

            /* troubleshooting - print out ici value */
//            System.out.println("   *** Time triggered at " + System.currentTimeMillis());
//            System.out.println("   *** High res values *****************");
//            for (int i = 0; i<nBinsHigh; i++) {
//                System.out.println("    High Res Bin, count = " + i + ", " +
//                        highResHistograms.get(highResHistograms.size()-1)[i]);
//            }
//            System.out.println("   *** Low res values *****************");
//            for (int i = 0; i<nBinsLow; i++) {
//                System.out.println("    Low Res Bin, count = " + i + ", " +
//                        lowResHistograms.get(lowResHistograms.size()-1)[i]);
//            }

            /* create new histogram object in the array and update the index */
            lowResHistograms.add(new int[nBinsLow]);
            highResHistograms.add(new int[nBinsHigh]);
            histTimeIdx++;

            /* update the histogram graphs with the latest histogram */
            idiInnerPanel.updateHistograms(histTimeIdx-1);

            /* if we're saving the data, call the appropriate method */
            if (idiParams.isOutputSaved()) {
                boolean success = saveOutput();
            }
        }
    }

    /**
     * Save the histogram data (high-res and low-res histograms, in that order),
     * to a csv file defined in the parameters object
     * 
     * @return boolean indicating success or failure
     */
    private boolean saveOutput() {

        /* try opening the file */
        try {
            File fName = idiParams.getOutputFilename();
//            System.out.println(String.format("writing data to ... %s", fName.getAbsolutePath()));

            // write a header line if this is a new file
            if (!fName.exists()) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fName));
                writer.write(getHeaderString());
                writer.newLine();
                writer.close();
            }

            /* create a String containing the histogram data we
             * want to save, and save it to the file.
             */
            BufferedWriter writer = new BufferedWriter(new FileWriter(fName, true));
//            String contourStats = roccaContourDataBlock.createContourStatsString();
            writer.write(getDataString());
            writer.newLine();
            writer.close();

        } catch(FileNotFoundException ex) {
            System.out.println("Could not find file");
            ex.printStackTrace();
            return false;
        } catch(IOException ex) {
            System.out.println("Could not write histogram data to file");
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Creates a header string for the output file, based on the current high-
     * and low-res bin values (high-res histogram first, then low).
     * 
     * @return the header string
     */
    public String getHeaderString() {
        String header = "Time";
        for (int i=idiParams.getHighResIdiBinScale();
                i<=idiParams.getMaxHighResBin();
                i+=idiParams.getHighResIdiBinScale()) {
            header = header + ", " + i;
        }
        for (int i=idiParams.getLowResIdiBinScale();
                i<=idiParams.getMaxLowResBin();
                i+=idiParams.getLowResIdiBinScale()) {
            header = header + ", " + i;
        }
        return header;
    }

    /**
     * Creates a string of counts from the high-res and low-res histogram bins
     * (high-res histogram first, then low).
     *
     * @return the data string
     */
    public String getDataString() {
        /* tie together all the bin values into one long string, separated by commas */
        String dataString = ""+System.currentTimeMillis();
        for (int i=0; i<nBinsHigh;i++) {
            dataString = dataString + ", " +
                    highResHistograms.get(histTimeIdx-1)[i];
        }
        for (int i=0; i<nBinsLow;i++) {
            dataString = dataString + ", " +
                    lowResHistograms.get(histTimeIdx-1)[i];
        }
        return dataString;


    }


    /**
     * The outer IDI-Display panel, to hold the two histograms as well as the
     * color bar
     */
    class IDIOuterPanel extends PamAxisPanel implements ComponentListener {

		int axisExtent = 0;

        public IDIOuterPanel() {

            /* call the AxisPanel constructor */
            super();
            
            /* create a high-resolution axis */
            highResAxis = new PamAxis(10, 10, 300, 10,   // x1, y1, x2, y2
                    0, idiParams.getMaxHighResBin(),        // minVal, maxVal
                    false,                              // label below axis
                    "High Resolution Histogram", "%d");                        // label and axis format

            /* create a low-resoultion axis */
            lowResAxis = new PamAxis(10, 10, 200, 10,   // x1, y1, x2, y2
                    0, idiParams.getMaxLowResBin(),        // minVal, maxVal
                    false,                              // label below axis
                    "Low Resolution Histogram", "%d");                        // label and axis format

            /* ensure there's enough room for the axes; the left (50) and bottom
             * (40) values were derived by trial and error.  If this statement
             * is removed, the horizontal axis disappears and the vertical axis
             * gets cut off
             */
			this.SetBorderMins(10, 50, 40, 20);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

			// just draw the axes along the bottom of the splitpane
			Rectangle r = getBounds();
			Insets insets = getInsets();
			Insets plotInsets = idiInnerPanel.getInsets();
			int newExtent = highResAxis.getExtent(g, "0");
			if (newExtent != axisExtent) {
				setBorder(new EmptyBorder(insets.top, insets.left, newExtent,
						insets.right));
				axisExtent = newExtent;
			}

			highResAxis.drawAxis(g,
                    insets.left + plotInsets.left,
                    r.height - insets.bottom,
                    insets.left + plotInsets.left + idiInnerPanel.getHighResImage().getWidth(),
					r.height - insets.bottom);

			lowResAxis.drawAxis(g,
                    insets.left + plotInsets.left + idiInnerPanel.getLowResImage().getX(),
                    r.height - insets.bottom,
                    r.width - insets.right - plotInsets.right,
					r.height - insets.bottom);
        }

        @Override
		public void componentMoved(ComponentEvent e) {
            idiInnerPanel.revalidate();
        }

        @Override
		public void componentResized(ComponentEvent e) {
            idiInnerPanel.revalidate();
        }

        @Override
		public void componentShown(ComponentEvent e) {}
        @Override
		public void componentHidden(ComponentEvent e) {}
    }

    /**
     * The inner IDI-Display panel.  This panel holds the two histograms and
     * their corresponding x-axis
     */
	class IDIInnerPanel extends JBufferedPanel {

        /**
         * The splitpanel which makes up the inner panel
         */
        private JSplitPane splitPane;

        /**
         * the high-resolution histogram image
         */
        private IDIHistogramImage highResImage;
        
        /**
         * the low-resolution histogram image
         */
        private IDIHistogramImage lowResImage;

        /**
         * Constructor
         */
        public IDIInnerPanel() {
            super();

            /* create the high-res and low-res image objects */
//            calculateSize();
            highResImage = new IDIHistogramImage(highResHistograms,
                    idiParams.getMaxHighCount());
            lowResImage = new IDIHistogramImage(lowResHistograms,
                    idiParams.getMaxLowCount());

            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                       highResImage, lowResImage);
            splitPane.setOneTouchExpandable(true);
            splitPane.setResizeWeight(0.5);

            /* set the minimum sizes of the histograms to 0 - allows user
             * to push the divider all the way to either side
             */
            Dimension minimumSize = new Dimension(0, 0);
            highResImage.setMinimumSize(minimumSize);
            lowResImage.setMinimumSize(minimumSize);

            //Provide a preferred size for the split pane.
            splitPane.setPreferredSize(new Dimension(300, 300));

            // add the split pane to the panel
            this.add(splitPane);
        }

        /**
         * Updates the color scales for the two images, based on the current
         * values in the IDI_DisplayParams object
         */
        public void updateColorScales() {
            highResImage.setColorScale(idiParams.getMaxHighCount());
            lowResImage.setColorScale(idiParams.getMaxLowCount());
        }


        /**
         * Resets the high and low resolution images
         * @param nTimeBins
         * @param nLowBins
         * @param nHighBins
         */
        public void resetImages(int nTimeBins, int nLowBins, int nHighBins) {
            highResImage.resetImage(nTimeBins, nHighBins);
            lowResImage.resetImage(nTimeBins, nLowBins);
        }

        /*
         * Adds the last row of data to the histograms
         */
        public void updateHistograms(int idx) {
            highResImage.shiftImageByOne(highResHistograms.get(idx));
            lowResImage.shiftImageByOne(lowResHistograms.get(idx));
        }

//        @Override
//        public void paint(Graphics g) {
//            super.paint(g);
//            paintPanel(g, null);
//        }
//
//
        @Override
        public void paintPanel(Graphics g, Rectangle clipRectangle) {

            /* if we just moved the divider, reset the splitpane's preferred
             * size to the size of the panel and repaint the axis panel so that
             * the axes get updated
             */
            if (splitPane.getDividerLocation() != splitPane.getLastDividerLocation()) {
                splitPane.setPreferredSize(this.getSize());
                idiOuterPanel.repaint();
            }
            highResImage.repaint();
            lowResImage.repaint();
        }

        public JSplitPane getSplitPane() {
            return splitPane;
        }

        public IDIHistogramImage getHighResImage() {
            return highResImage;
        }

        public IDIHistogramImage getLowResImage() {
            return lowResImage;
        }

	}
    
    /**
     * The actual histogram image
     */
    class IDIHistogramImage extends JPanel {

        /**
         * Store the counts in an int array for the histogram.
         * The int[] cells will correspond to the IDI bins, and the ArrayList
         * elements will correspond to the time bins.
         */
        private ArrayList<int[]> histogramData;

        /**
         * Highest count on the color bar - defined by the user and stored
         * in the IDI parameters
         */
        int maxCount;
        
        /**
         * the number of timeBins in the histogram
         */
        private int timeBins;

        /**
         * the number of IDIBins in the histogram
         */
        private int IDIBins;

        /**
         * the number of colors to use when creating the color array
         */
        private static final int NUMOFCOLORS = 256;

        /**
         * the type of color array to create
         */
        private final ColourArray.ColourArrayType COLOURARRAYTYPE =
                ColourArray.ColourArrayType.HOT;

        /**
         * the ColourArray for the histograms
         */
        private ColourArray colourArray =
                ColourArray.createStandardColourArray(NUMOFCOLORS, COLOURARRAYTYPE );

        /**
         * used to control the contrast of the image
         */
        private double colorScale;

        /**
         * The histogram image
         */
        private BufferedImage histImage = null;

        /**
         * A scaled version of the histogram image.
         */
        private Image scaledHistImage = null;

        /**
         * The filter used to scale the histogram image
         */
        private ImageFilter scaleFilter = null;

        /**
         * Constructor
         * 
         * @param histogramData the histogram data that this image is based on
         */
        public IDIHistogramImage(ArrayList<int[]> histogramData, int maxCount) {
            this.histogramData = histogramData;

            /* Set ColorScale to span the user-defined max count */
            setColorScale(maxCount);
            
            /* add the mouse listener */
			addMouseListener(new MouseFuncs());
        }

        /**
         * Sets the highest value in the colorbar, and adjusts the colorscale
         * accordingly
         */
        public void setColorScale(int maxCount) {
            colorScale = (colourArray.getNumbColours()-1) / maxCount;
        }

//        /**
//         * Sets the size and scaleFilter of the image
//         */
//        private void setSize(int width) {
//            graphWidth = width;
//            /* set the preferred size of the histogram panel */
//            Dimension d = new Dimension(graphWidth, graphHeight);
//            setPreferredSize(d);
//            /* set the scale */
//            scaleFilter = new ReplicateScaleFilter(graphWidth, graphHeight);
//        }

//        /**
//         * Resizes the image, based on the graphWidth and graphHeight parameters
//         *
//         * @param width The width of this image
//         */
//        private void resizeImage(int width) {
//            graphWidth = width;
////            setSize();
//
//            /* if we've already created an image, redraw it */
//            resizePane();
//            revalidate();
//        }

        /**
         * Create the scaled spectrogram image
         */
        private void resizePane() {

            /* create the scaled spectrogram image, if it exists */
            if (histImage != null) {
                scaleFilter = new ReplicateScaleFilter(this.getWidth(), this.getHeight());
                scaledHistImage =
                    createImage(new FilteredImageSource(histImage.getSource(),
                                                        scaleFilter));
            }

    //        Dimension sz = getSize();
//            revalidate();
//            repaint();
    //        repaint(0, 0, 0, sz.width - 1, sz.height - 1);
        }

        /**
         * Resets the image, clearing any previous data that might exist and
         * setting the image size to the latest parameter values
         */
        private void resetImage(int timeBins, int IDIBins) {

            /* save the size of the image */
            this.timeBins = timeBins;
            this.IDIBins = IDIBins;

            /* create a new scale filter, based on the current JPanel size */
            /* commented out - it seemed to be causing errors, though I don't
             * know why.  Need to find a place to reset the scale...
             */
//            scaleFilter = new ReplicateScaleFilter(graphWidth, graphHeight);

            /* create a new image, clear it and display it on the screen */
            histImage = new BufferedImage(IDIBins,
                    timeBins,
                    BufferedImage.TYPE_INT_RGB);
            clearImage();
//            resizePane();
            repaint();
        }


        /**
         * Set the colors of the entire image, based on the counts
         */
        public void setColors() {

            /* loop through the histogram data, setting the color for each pixel in the
             * histogram based on the number of counts in the bin
             */
            for (int i = 0; i < timeBins; i++) {
                for (int j = 0; j< IDIBins; j++) {
                    /* calculate the index in the colourArray based on the counts
                     * Keep the value within the bounds of the colourArray
                     */
                    int colorIndex = (int)(histogramData.get(i)[j] * colorScale);
                    if (colorIndex<0) {
                        colorIndex=0;
                    } else if (colorIndex>colourArray.getNumbColours()-1) {
                        colorIndex=colourArray.getNumbColours()-1;
                    }
                    //System.out.println(colorIndex);

                    /* set the pixel color */
                    histImage.setRGB(i,
                            j,
                            colourArray.getColour(colorIndex).getRGB());
                }
            }
        }

        /**
         * Paints the histogram image completely white
         */
        public void clearImage() {
            for (int i = 0; i < histImage.getWidth(); i++) {
                for (int j = 0; j< histImage.getHeight(); j++) {
                    /* set the pixel color */
                    histImage.setRGB(i,
                            j,
                            Color.WHITE.getRGB());
                }
            }
        }

        /**
         * Set the colors of 1 line in the image, based on the counts.  Returns
         * an int array with the colors
         */
        public int[] setColors(int[] singleRow) {

            int[] newColors = new int[singleRow.length];

            /* make sure we were passed correct data; the size of the array
             * should match the IDIBins parameter
             */
            if (singleRow.length != histImage.getWidth()) {
                System.out.println("Error - incorrect histogram data passed");
                return null;
            }

            /* loop through the histogram data, setting the color for each pixel in the
             * histogram based on the number of counts in the bin
             */
            for (int j = 0; j< singleRow.length; j++) {
                /* calculate the index in the colourArray based on the counts
                 * Keep the value within the bounds of the colourArray
                 */
                int colorIndex = (int)(singleRow[j] * colorScale);
                if (colorIndex<0) {
                    colorIndex=0;
                } else if (colorIndex>colourArray.getNumbColours()-1) {
                    colorIndex=colourArray.getNumbColours()-1;
                }
                //System.out.println(colorIndex);

                /* set the pixel color */
                newColors[j] = colourArray.getColour(colorIndex).getRGB();
            }
            return newColors;
        }

        /**
         * Shifts the image colors up by 1 row, and adds the passed row of data
         * to the bottom
         *
         * @param singleRow the row of counts to be added
         */
        public void shiftImageByOne(int[] singleRow) {
            int w = histImage.getWidth();
            int h = histImage.getHeight();
            int[] oldImage = histImage.getRGB(0, 1, w, h-1, null, 0, w);
            int[] newLine = setColors(singleRow);
            histImage.setRGB(0, 0, w, h-1, oldImage, 0, w);
            histImage.setRGB(0, h-1, w, 1, newLine, 0, w);
//            resizePane();
            repaint();
        }

        /**
         * Adds a row of data to the bottom of the current image
         *
         * @param singleRow  the row of counts to be added
         */
        public void addOneToImage(int[] singleRow) {

            int w = histImage.getWidth();
            int h = histImage.getHeight();
            int[] newLine = setColors(singleRow);
            histImage.setRGB(0, h-1, w, 1, newLine, 0, w);
//            resizePane();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            /* recalculate the scale, in case the size has changed */
            resizePane();

            /* draw the histogram image */
            g.drawImage(scaledHistImage, 0, 0, (ImageObserver) null);
        }
    }

    
	class MouseFuncs extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {}

		@Override
		public void mouseReleased(MouseEvent e) {
			showMenu(e);
		}

		private void showMenu(MouseEvent e) {
			if (e.isPopupTrigger()) {
				JPopupMenu menu = new JPopupMenu();
				JMenuItem menuItem = new JMenuItem("Plot options ...");
				menuItem.addActionListener(new PlotOptions());
				menu.add(menuItem);
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	private class PlotOptions implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Point pt = new Point();
			pt = idiInnerPanel.getLocationOnScreen();
			pt.y -= 10;
			pt.x += 10;
			IDI_DisplayParams newParams = IDI_DisplayDialog.showDialog(
					clickControl.getGuiFrame(), pt, idiParams);
			if (newParams != null) {
				idiParams = newParams.clone();
                setParameters();
			}
		}
	}


    
    /**
     * Class containing the axis, histogram and color bar
     */
//    class ColorBarPanel {
//        /* taken from SpectrogramDisplay, line 358 */
//
//
//
//		// now make a standard amplitude image
//		if (colorValues != null && colorValues.length > 0) {
//			//			synchronized (spectrogramDisplay) {
//			amplitudeImage = new BufferedImage(1, colorValues.length,
//					BufferedImage.TYPE_INT_RGB);
//			WritableRaster raster = amplitudeImage.getRaster();
//			for (int i = 0; i < colorValues.length; i++) {
//				raster.setPixel(0, colorValues.length - i - 1, colorValues[i]);
//			}
//			//			}
//		}
//    }







    /*                       */
    /* PamObserver Methods   */
    /*                       */
    @Override
    public void noteNewSettings() {}

	@Override
    public String getObserverName() {
        return getName();
    }

	@Override
    public PamObserver getObserverObject() {
        return this;
    }

	@Override
    public long getRequiredDataHistory(PamObservable o, Object arg) {
        return 0;
    }

	@Override
    public void masterClockUpdate(long milliSeconds, long sampleNumber) {}

	@Override
    public void removeObservable(PamObservable o) {}

	@Override
    public void setSampleRate(float sampleRate, boolean notify) {}

	@Override
	public void receiveSourceNotification(int type, Object object) {
		// don't do anything by default
	}

    @Override
    public String getName() {
        return "IDI Display";
    }

    /*                       */
    /* Serialization Methods */
    /*                       */
    @Override
	public Serializable getSettingsReference() {
        return idiParams;
    }

    @Override
	public long getSettingsVersion() {
        return IDI_DisplayParams.getSerialVersionUID();
    }

    @Override
	public String getUnitName() {
        return getName();
    }

    @Override
	public String getUnitType() {
        return getName();
    }

    @Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		idiParams = ((IDI_DisplayParams) pamControlledUnitSettings
				.getSettings()).clone();
 		return true;
    }

}
