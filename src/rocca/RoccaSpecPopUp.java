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

//import com.csvreader.CsvReader;
import Acquisition.AcquisitionProcess;
import PamController.PamController;
import PamUtils.FileParts;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RawDataUnavailableException;
import Spectrogram.SpectrogramDisplay;
import fftManager.Complex;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;

import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import wavFiles.WavFile;
import wavFiles.WavFileWriter;

import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.JButton;

/**
 *
 * @author Michael Oswald
 */
public class RoccaSpecPopUp extends javax.swing.JPanel {

    /**
     * Constants
     */
    private static final float VZOOM_INCR = 2;
    private static final float HZOOM_INCR = 2;
    private static final float BRIGHT_INCR = 5;
    private static final double CONTRAST_INCR = .5;
    private static final float MIN_VZOOM = 0.03125f;
    private static final float MIN_HZOOM = 0.03125f;
    private static final float MAX_VZOOM = 16;
    private static final float MAX_HZOOM = 8;
    public static final int AXIS_SIZE = 30;

    /**
     * The number of detections captured since the program started
     */
    private static int numDetections = 0;

    /*
     * The detection count for this instance (set in the constructor)
     */
    private int thisDetection = 0;

    /**
     * reference to the current window frame
     */
    JFrame thisFrame;

    /**
     * reference to the RoccaProcess object calling this
     */
    RoccaProcess roccaProcess;

    /**
     * current Rocca parameters - a copy is made for this window
     */
    RoccaParameters roccaParameters;

    /**
     * FFT data block containing the whistle data
     */
    FFTDataBlock fftData;

    /**
     * The raw data (all channels) from which the FFTDataBlock containing the
     * whistle was created
     */
    PamRawDataBlock rawData;

    /**
     * List of time bins, in seconds
     */
    long[] timeBins;

    /**
     * List of frequency bins, in Hz
     */
    double[] freqBins;

    /**
     * starting frequency of the whistle
     */
    private double startFreq;

    /**
     * starting time of the whistle
     */
    private long startTime;

    /**
     * ending frequency of the whistle
     */
    private double endFreq;

    /**
     * the upper frequency that the user boxed on the scrolling spectrogram
     */
    private double boxTopFreq;

    /**
     * the lower frequency that the user boxed on the scrolling spectrogram
     */
    private double boxBotFreq;

    /**
     * ending time of the whistle
     */
    private long endTime;

    /**
     * link to the spectrogram display
     */
    SpectrogramDisplay display;

    /**
     * the input channel the contour was selected from
     */
    int channel;

    // Matrix of data to be displayed
    protected double[][] data;

    // RoccaContourDataBlock containing the contour information
    private RoccaContourDataBlock roccaContourDataBlock;

    // List of contour points (x = time bin, y=freq bin)
    protected Point[] contour;

    // A reference to the contour point being dragged to a new position
    private Point dragPoint;

    /* Save the original position of the last point moved */
    private Point lastMovedPoint;

    /* Save the contour index of the last point moved */
    private int lastMovedIndx;

    // title of the JFrame created by run()
    protected String windowTitle;

    // RoccaContour
    RoccaContour roccaContour;

    // GUI components and icons
    private SpectrogramPanel dataPanel;

    // states to keep track of
    private float currHZoom = 1.0f;
    private float currVZoom = 1.0f;
    private boolean hZoom = true;
    private boolean vZoom = true;

    /**
     * Whether or not to display the peaks of the contour
     */
    private boolean showPeaks = false;

    /**
     * Whether or not the starting point of the contour has been selected
     */
    private boolean startIsSet = false;

    /**
     * Whether or not the ending point of the contour has been selected
     */
    private boolean endIsSet = false;

    /**
     * Whether or not the user is currently selecting the contour start.  This
     * is set to true because the user needs to select a starting point
     * when the window opens
     */
    private boolean settingStart = true;

    /**
     * Whether or not the user is currently selecting the contour end
     */
    private boolean settingEnd = false;

    /**
     * Whether or not the user is currently selecting the lowpass filter frequency
     */
    private boolean settingLowpass = false;

    /**
     * Whether or not the user is currently selecting the highpass filter frequency
     */
    private boolean settingHighpass = false;

    /**
     * Whether the contour is currently visible or not
     */
    private boolean contourState = true;

    /**
     * constant indicating the contour should be turned off
     */
    public static final boolean CONTOUROFF = false;

    /**
     * constant indicating the contour should be turned on
     */
    public static final boolean CONTOURON = true;
    
    /**
     * constant indicating the getXPosition or getYPosition method should
     * return an int
     */
    public static final boolean RETURNINT = true;
    
    /**
     * constant indicating the getXPosition or getYPosition method should
     * return a float
     */
    public static final boolean RETURNFLOAT = false;
    
    /**
     * boolean indicating that the user is tracing out a contour
     */
    private boolean tracingNow = false;
    
    /**
     * array of points selected by user to trace out a contour
     */
    private ArrayList<Point> userPoints; 

    /**
     * The detection number
     */
    private String sNum;

    /**
     * The current highpass filter frequency
     */
    double highpassFreq = 0.0;

    /**
     * The current lowpass filter frequency
     */
    double lowpassFreq = 100000.0;

    /**
     * A String holding the previous highpass filter frequency
     */
    String oldHighPass = "0";

    /**
     * A String holding the previous lowpass filter frequency
     */
    String oldLowPass = ""+Double.MAX_VALUE;


    /**
     * The horizontal (time) axis
     */
//    RoccaAxis timeAxis;

    /**
     * The vertical (frequency) axis
     */
    RoccaAxis freqAxis;

    /**
     * Dummy constructor - only used to test display
     *
     * @param d
     * @param title
     */
    public RoccaSpecPopUp(double[][] d, String title) {
        super();
        this.data = d;
        this.windowTitle = title;

        this.initComponents();
    }

    /**
     *
     * @param roccaProcess
     * @param fftDataBlock
     * @param boxBotFreq
     * @param boxTopFreq
     * @param rawData
     * @param display the SpectrogramDisplay object that the user drew a box in, or null if it's from a TD Display
     * @param channel
     */
    public RoccaSpecPopUp(
            RoccaProcess roccaProcess,
            FFTDataBlock fftDataBlock,
            double boxBotFreq,
            double boxTopFreq,
            PamRawDataBlock rawData,
            SpectrogramDisplay display,
            int channel) {
        super();

        /* increment the detection count and set the window title */
        thisDetection = ++numDetections;
        this.windowTitle = "Encounter " + thisDetection;

        /* set the rest of the instance variables */
        this.roccaProcess = roccaProcess;
        this.fftData = fftDataBlock;
        this.startFreq = boxBotFreq;
        this.endFreq = boxTopFreq;
        this.boxBotFreq = boxBotFreq;
        this.boxTopFreq = boxTopFreq;
        this.rawData = rawData;
        this.display = display;
        this.channel = channel;
        this.roccaParameters = roccaProcess.roccaControl.roccaParameters.clone();
        this.sNum = roccaProcess.roccaControl.roccaSidePanel.getSightingNum();
//        this.windowTitle = "Detection Number " + this.sNum;
        roccaContour = new RoccaContour(roccaProcess);

        /* extract the data array from the FFT data block */
        this.data = this.getDataArray();

        /* comment out this section - we can no longer show the contour when the
         * window is initialized because we don't have the whistle start/end
         * points yet
         */
//
//        /* create a new contour datablock */
//        roccaContourDataBlock = roccaContour.generateContour(
//                fftData,
//                startFreq,
//                endFreq,
//                rawData,
//                channel,
//                roccaParameters);
//
//        /* extract the contour data from the contour data block */
//        this.contour = this.getContourData();

        /* create new instances of timeAxis and freqAxis.  Use dummy variables
         * for the x,y position - these will be reset when the axis actually
         * gets painted in the paint() method
         */
//        timeAxis = new RoccaAxis(10,0,200,0,
//                0,     // min value
//                100,    // max value
//                false,              // labels below
//                null,               // no title
//                "%d",               // display integer value
//                RoccaAxis.Orientation.HORIZONTAL,
//                this);              // reference to this rspu
        freqAxis = new RoccaAxis(0,200,0,10,
                0,     // min value
                24000,    // max value
                true,               // labels left
                null,               // no title
                null,               // display double value
                RoccaAxis.Orientation.VERTICAL,
                this);              // reference to this rspu
//		freqAxis.pamAxis.setFractionalScale(true);

        /* set the highpass and lowpass frequency filters */
        highpassFreq = 0.0;
        lowpassFreq = freqBins[freqBins.length-1];



        /* initialize the window, run it and set the scrollbar (we set the
         * scrollbar after the run() method because the params don't get set
         * properly until the window is displayed)
         */
        this.initComponents();
        this.run();
        this.setInitialPosition();
    }

    private double[][] getDataArray() {
        int numFreqBins = fftData.getFftLength()/2;   // number of freq. bins
        int numTimeBins = fftData.getUnitsCount();  // number of time bins
        int channelMap = fftData.getChannelMap();	// this needs to be channel map and not sequence map, so it can find the specific gains
        timeBins = new long[numTimeBins];
        freqBins = new double[numFreqBins];
        FFTDataUnit fftUnit = null;
        ComplexArray fftVals = null;
        double dB = 0.0;
        double rawAmp = 0.0;
        int fftLength = fftData.getFftLength();

        AcquisitionProcess daq = (AcquisitionProcess) fftData.getSourceProcess();

        /* note that the SpectrogramPanel object requires the data matrix to
         * have time bins in the first vector and frequency bins in the second
         */
        double[][] d = new double[numTimeBins][numFreqBins];
        for(int currentTimeBin = 0; currentTimeBin < numTimeBins; currentTimeBin++) {
            fftUnit = fftData.getDataUnit(currentTimeBin, FFTDataBlock.REFERENCE_CURRENT);
            fftVals = fftUnit.getFftData();
            timeBins[currentTimeBin] = fftUnit.getTimeMilliseconds();

            for(int currentFreqBin = 0; currentFreqBin < fftVals.length(); currentFreqBin++) {
                rawAmp = fftVals.magsq(currentFreqBin);
                dB = daq.fftAmplitude2dB(rawAmp,
                        PamUtils.getLowestChannel(channelMap),
                        fftData.getSampleRate(),
                        fftLength,
                        true,
                        true);
 //               dB = daqProcess.rawAmplitude2dB(fftVals[currentFreqBin].real, channel, false);
                d[currentTimeBin][currentFreqBin] = dB;

                // if this is the first time bin, also save the freq bin value
                if (currentTimeBin==0){
                    freqBins[currentFreqBin] =
                            (currentFreqBin+1) *
                            fftData.getSampleRate() /
                            fftData.getFftLength();
                }
            }
        }
        return d;
    }

    private Point[] getContourData() {
        Point[] p = new Point[roccaContourDataBlock.getUnitsCount()];
        int timeBin;
        int freqBin;

        // read each roccaContourDataUnit peak freq and time, and convert to nearest time & freq bin
        for (int i=0; i < roccaContourDataBlock.getUnitsCount(); i++) {
            timeBin = timeBins.length;
            for (int j=0; j<timeBins.length; j++) {
                if (timeBins[j]>=roccaContourDataBlock.getDataUnit
                    (i, FFTDataBlock.REFERENCE_CURRENT).getTimeMilliseconds()) {
                    timeBin = j;
                    break;
                }
            }

            freqBin = freqBins.length;
            /* if we are outside of the range of the whistle, set the
             * frequency bin to the constant NO_CONTOUR_HERE
             */
            if (roccaContourDataBlock.getDataUnit
                (i, FFTDataBlock.REFERENCE_CURRENT).getPeakFreq()
                == RoccaContour.OUTOFRANGE) {
                freqBin = RoccaParameters.NO_CONTOUR_HERE;

            /* otherwise, find the frequency bin closest to the value */
            } else {
                for (int j=0; j<freqBins.length; j++) {

                    if (freqBins[j]>=roccaContourDataBlock.getDataUnit
                        (i, FFTDataBlock.REFERENCE_CURRENT).getPeakFreq()) {
                        freqBin = j;
                        break;
                    }
                }
            }
            p[i] = new Point(timeBin, freqBin);
        }
        return p;
    }


/** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        zoomIn = new javax.swing.JButton();
        classifierPanel = new javax.swing.JPanel();
        classify = new javax.swing.JButton();
        classifiedAsLbl = new javax.swing.JLabel();
        zoomOut = new javax.swing.JButton();
        reset = new javax.swing.JButton();
        brightDec = new javax.swing.JButton();
        brightInc = new javax.swing.JButton();
        contrastDec = new javax.swing.JButton();
        contrastInc = new javax.swing.JButton();
        /* create a new SpectrogramPanel */
        dataPanel = new SpectrogramPanel(this);
        /* get the dimensions of the scaled image (overall size, in pixels) */
        Dimension imageSize = dataPanel.getScaledImageSize();
        //timeAxis.setPreferredSize(new Dimension(imageSize.width, AXIS_SIZE));
        freqAxis.setPreferredSize(new Dimension(AXIS_SIZE, imageSize.height));
        scroller = new javax.swing.JScrollPane(dataPanel);
        //scroller.setColumnHeaderView(timeAxis);
        scroller.setRowHeaderView(freqAxis);
        saveExit = new javax.swing.JButton();
        discardExit = new javax.swing.JButton();
        statusBar = new javax.swing.JLabel();
        noiseSensSpinner = new javax.swing.JSpinner
        (new SpinnerNumberModel
            (roccaParameters.noiseSensitivity, 1, 21, 1));
        noiseSensLbl = new javax.swing.JLabel();
        resetContour = new javax.swing.JButton();
        saveNewNameExit = new javax.swing.JButton();
        saveWavOnly = new javax.swing.JButton();
        undoLastMove = new javax.swing.JButton();
        recalcContour = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        highpassLbl = new javax.swing.JLabel();
        highPass = new javax.swing.JTextField();
        highpassUnitsLbl = new javax.swing.JLabel();
        lowpassLbl = new javax.swing.JLabel();
        lowPass = new javax.swing.JTextField();
        lowpassUnitsLbl = new javax.swing.JLabel();
        resetFilters = new javax.swing.JButton();
        setLowpass = new javax.swing.JToggleButton();
        setHighpass = new javax.swing.JToggleButton();
        jPanel2 = new javax.swing.JPanel();
        selectStart = new javax.swing.JToggleButton();
        selectStart.setHorizontalTextPosition(SwingConstants.CENTER);
        selectEnd = new javax.swing.JToggleButton();
        toggleContour = new javax.swing.JButton();
        btnPickPoints = new javax.swing.JToggleButton();

        zoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Zoom-in 16.png"))); // NOI18N
        zoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInActionPerformed(evt);
            }
        });

        classifierPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        classify.setText("Classify");
        classify.setEnabled(false);
        classify.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        classify.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                classifyActionPerformed(evt);
            }
        });

        classifiedAsLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        classifiedAsLbl.setText("Not Classified");
        classifiedAsLbl.setEnabled(false);
        classifiedAsLbl.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout classifierPanelLayout = new javax.swing.GroupLayout(classifierPanel);
        classifierPanel.setLayout(classifierPanelLayout);
        classifierPanelLayout.setHorizontalGroup(
            classifierPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, classifierPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(classifiedAsLbl, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(classify)
                .addContainerGap())
        );
        classifierPanelLayout.setVerticalGroup(
            classifierPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, classifierPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(classifierPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(classifiedAsLbl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                    .addComponent(classify, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        zoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Zoom-out 16.png"))); // NOI18N
        zoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutActionPerformed(evt);
            }
        });

        reset.setText("Reset");
        reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetActionPerformed(evt);
            }
        });

        brightDec.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Brightness dec 16.png"))); // NOI18N
        brightDec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                brightDecActionPerformed(evt);
            }
        });

        brightInc.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Brightness inc 16.png"))); // NOI18N
        brightInc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                brightIncActionPerformed(evt);
            }
        });

        contrastDec.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/contrast dec 16.png"))); // NOI18N
        contrastDec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contrastDecActionPerformed(evt);
            }
        });

        contrastInc.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/contrast inc 16.png"))); // NOI18N
        contrastInc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contrastIncActionPerformed(evt);
            }
        });

        scroller.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                scrollerMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                scrollerMouseReleased(evt);
            }
        });
        scroller.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                scrollerMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                scrollerMouseMoved(evt);
            }
        });

        saveExit.setText("<html><font color=#808080>Save as encounter </font></html>");
        saveExit.setEnabled(false);
        saveExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveExitActionPerformed(evt);
            }
        });

        discardExit.setText("<html>Discard and Exit</html>");
        discardExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                discardExitActionPerformed(evt);
            }
        });

        statusBar.setText(windowTitle);

        noiseSensSpinner.setEnabled(false);
        noiseSensSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                noiseSensSpinnerStateChanged(evt);
            }
        });

        noiseSensLbl.setText("Noise Sensitivity");
        noiseSensLbl.setEnabled(false);

        resetContour.setText("Reset Contour");
        resetContour.setEnabled(false);
        resetContour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetContourActionPerformed(evt);
            }
        });

        saveNewNameExit.setText("<html><font color=#808080>Save as diff encounter</font></html>");
        saveNewNameExit.setEnabled(false);
        saveNewNameExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveNewNameExitActionPerformed(evt);
            }
        });

        saveWavOnly.setText("<html><font color=#808080>Save WAV discard class</font></html>");
        saveWavOnly.setEnabled(false);
        saveWavOnly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveWavOnlyActionPerformed(evt);
            }
        });

        undoLastMove.setText("Undo Last Move");
        undoLastMove.setEnabled(false);
        undoLastMove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoLastMoveActionPerformed(evt);
            }
        });

        recalcContour.setText("Recalc Contour");
        recalcContour.setEnabled(false);
        recalcContour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recalcContourActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        highpassLbl.setText("Highpass Filter");
        highpassLbl.setEnabled(false);

        highPass.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        highPass.setText("0");
        highPass.setEnabled(false);
        highPass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                highPassActionPerformed(evt);
            }
        });

        highpassUnitsLbl.setText("Hz");
        highpassUnitsLbl.setEnabled(false);

        lowpassLbl.setText("Lowpass Filter");
        lowpassLbl.setEnabled(false);

        lowPass.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        lowPass.setText(String.format("%5.0f", freqBins[freqBins.length-1]));
        lowPass.setEnabled(false);
        lowPass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lowPassActionPerformed(evt);
            }
        });

        lowpassUnitsLbl.setText("Hz");
        lowpassUnitsLbl.setEnabled(false);

        resetFilters.setText("Reset");
        resetFilters.setEnabled(false);
        resetFilters.setMargin(new java.awt.Insets(2, 10, 2, 10));
        resetFilters.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetFiltersActionPerformed(evt);
            }
        });

        setLowpass.setText("Set");
        setLowpass.setActionCommand("SetLow");
        setLowpass.setEnabled(false);
        setLowpass.setMargin(new java.awt.Insets(2, 10, 2, 10));
        setLowpass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setLowpassActionPerformed(evt);
            }
        });

        setHighpass.setText("Set");
        setHighpass.setActionCommand("SetHigh");
        setHighpass.setEnabled(false);
        setHighpass.setMargin(new java.awt.Insets(2, 10, 2, 10));
        setHighpass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setHighpassActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lowpassLbl)
                    .addComponent(highpassLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(lowPass, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(highPass, javax.swing.GroupLayout.Alignment.LEADING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(highpassUnitsLbl)
                    .addComponent(lowpassUnitsLbl))
                .addGap(7, 7, 7)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(setLowpass)
                    .addComponent(setHighpass))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(resetFilters)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(resetFilters, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lowPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lowpassUnitsLbl)
                            .addComponent(lowpassLbl)
                            .addComponent(setLowpass))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(highPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(highpassUnitsLbl)
                            .addComponent(highpassLbl)
                            .addComponent(setHighpass))))
                .addContainerGap())
        );

        oldHighPass = highPass.getText();
        oldLowPass = lowPass.getText();

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        selectStart.setSelected(true);
        selectStart.setText("<html>Select Contour Start</html>");
        selectStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectStartActionPerformed(evt);
            }
        });

        selectEnd.setText("<html>Select Contour End</html>");
        selectEnd.setEnabled(false);
        selectEnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectEndActionPerformed(evt);
            }
        });

        toggleContour.setText("<html>Turn Contour Off</html>");
        toggleContour.setActionCommand("Toggle Contour");
        toggleContour.setEnabled(false);
        toggleContour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleContourActionPerformed(evt);
            }
        });
        
        btnPickPoints.setText("Pick Points");
        btnPickPoints.setActionCommand("Pick Points");
        btnPickPoints.setHorizontalTextPosition(SwingConstants.CENTER);
        btnPickPoints.setPreferredSize(new Dimension(33, 9));
        btnPickPoints.setMinimumSize(new Dimension(33, 9));
        btnPickPoints.setMaximumSize(new Dimension(33, 9));
        btnPickPoints.setEnabled(false);
        btnPickPoints.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPickPointsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2Layout.setHorizontalGroup(
        	jPanel2Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel2Layout.createSequentialGroup()
        			.addContainerGap()
        			.addComponent(selectStart, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(selectEnd, GroupLayout.PREFERRED_SIZE, 107, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(toggleContour, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(btnPickPoints, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(24, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
        	jPanel2Layout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(jPanel2Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
        				.addComponent(btnPickPoints, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE)
        				.addGroup(jPanel2Layout.createParallelGroup(Alignment.TRAILING, false)
        					.addComponent(toggleContour, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
        					.addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE, false)
        						.addComponent(selectStart, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
        						.addComponent(selectEnd, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE))))
        			.addContainerGap())
        );
        jPanel2.setLayout(jPanel2Layout);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        layout.setHorizontalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(layout.createParallelGroup(Alignment.TRAILING)
        				.addComponent(scroller, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 754, Short.MAX_VALUE)
        				.addGroup(Alignment.LEADING, layout.createSequentialGroup()
        					.addComponent(zoomIn)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(zoomOut)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(reset)
        					.addPreferredGap(ComponentPlacement.UNRELATED)
        					.addComponent(brightDec)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(brightInc)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(contrastDec)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(contrastInc)
        					.addGap(12)
        					.addComponent(noiseSensLbl, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(noiseSensSpinner, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE))
        				.addGroup(Alignment.LEADING, layout.createSequentialGroup()
        					.addGroup(layout.createParallelGroup(Alignment.LEADING)
        						.addComponent(resetContour, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
        						.addComponent(recalcContour, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        						.addComponent(undoLastMove, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(classifierPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
        						.addGroup(layout.createSequentialGroup()
        							.addComponent(saveWavOnly, GroupLayout.PREFERRED_SIZE, 196, GroupLayout.PREFERRED_SIZE)
        							.addPreferredGap(ComponentPlacement.UNRELATED)
        							.addComponent(discardExit, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        						.addGroup(layout.createSequentialGroup()
        							.addComponent(saveExit, GroupLayout.PREFERRED_SIZE, 198, GroupLayout.PREFERRED_SIZE)
        							.addPreferredGap(ComponentPlacement.RELATED)
        							.addComponent(saveNewNameExit, GroupLayout.PREFERRED_SIZE, 175, GroupLayout.PREFERRED_SIZE)))
        					.addPreferredGap(ComponentPlacement.RELATED))
        				.addComponent(statusBar, Alignment.LEADING)
        				.addGroup(layout.createSequentialGroup()
        					.addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        					.addGap(18)
        					.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        			.addGap(205))
        );
        layout.setVerticalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        					.addComponent(zoomIn)
        					.addComponent(zoomOut)
        					.addComponent(reset, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
        				.addComponent(brightDec)
        				.addComponent(brightInc)
        				.addComponent(contrastDec)
        				.addComponent(contrastInc)
        				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        					.addComponent(noiseSensSpinner, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
        					.addComponent(noiseSensLbl, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)))
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(scroller, GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
        				.addComponent(jPanel2, 0, 0, Short.MAX_VALUE)
        				.addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
        				.addComponent(classifierPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        				.addGroup(layout.createSequentialGroup()
        					.addComponent(undoLastMove, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        					.addComponent(recalcContour, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(resetContour, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE))
        				.addGroup(layout.createSequentialGroup()
        					.addGroup(layout.createParallelGroup(Alignment.TRAILING)
        						.addComponent(saveNewNameExit, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
        						.addComponent(saveExit, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE))
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addGroup(layout.createParallelGroup(Alignment.LEADING)
        						.addComponent(saveWavOnly, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
        						.addComponent(discardExit, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE))))
        			.addGap(18)
        			.addComponent(statusBar)
        			.addContainerGap())
        );
        this.setLayout(layout);

        resetActionPerformed(new ActionEvent(reset, 0, "Reset"));
    }// </editor-fold>//GEN-END:initComponents

    private void zoomInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInActionPerformed
        zoomDataPanel(currHZoom*HZOOM_INCR, currVZoom*VZOOM_INCR);
}//GEN-LAST:event_zoomInActionPerformed

    private void scrollerMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_scrollerMouseMoved

        /* get the x position of the cursor; note that we need to add the value
         * of the horizontal scrollbar to this because the cursor value is only
         * what is currently on the screen, and we want to know relative to the
         * whole image.  We also need to subtract the width of the frequency
         * axis, otherwise the data is shifted
         */
        int x = getXPositionAsInt(evt);

        /* get the y position of the cursor; note that we need to add the value
         * of the horizontal scrollbar to this because the cursor value is only
         * what is currently on the screen, and we want to know relative to the
         * whole image.  Also, flip the number because java starts counting from
         * the top of the image, not the bottom.
         */
        int y = getYPositionAsInt(evt);

        /* if we're within the bounds of the spectrogram data, display the time
         * and frequency bins under the cursor
         */
        if(x < dataPanel.getDataWidth() && x >= 0
           && y < dataPanel.getDataHeight() && y >= 0) {
            statusBar.setText(timeBins[x] + " milliseconds, " +
                    freqBins[y] + " Hz ");
            /*
            statusBar.setText("Pos = " + evt.getX() +
                    " ("+
                    x +
                    "), " + evt.getY() +
                    " (" +
                    y +
                    "); HScroll = " +
                    scroller.getHorizontalScrollBar().getValue() +
                    " val, " +
                    scroller.getHorizontalScrollBar().getModel().getExtent() +
                    " ext; VScroll = " +
                    scroller.getVerticalScrollBar().getValue() +
                    " val, " +
                    scroller.getVerticalScrollBar().getModel().getExtent()
                    );
             */
        }
    }//GEN-LAST:event_scrollerMouseMoved

    private void scrollerMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_scrollerMousePressed

    	/* now, if we're NOT setting the start/end points, low/highpass filters, and not tracing out the contour, then see if there's
         * a contour point close to where the mouse was pressed
         */
    	if (!settingStart && !settingEnd && !settingLowpass && !settingHighpass && !tracingNow) {
            showPeaks = true;

            /* check if the current position is close to one of the contour points
             * if so, set that point to a square and change the cursor icon so the
             * user knows they're dragging a point
             */
            dragPoint = findTheClosestPoint(evt);
            if (dragPoint!=null) {
                setCursor(new Cursor(Cursor.MOVE_CURSOR));
            }

            // repaint the screen
            dataPanel.repaint();
        }
    }//GEN-LAST:event_scrollerMousePressed

    private Point findTheClosestPoint(java.awt.event.MouseEvent evt) {
        Point pointToReturn = null;

        /* get the time/freq bins under the cursor */
        float x = getXPositionAsFloat(evt);
        float y = getYPositionAsFloat(evt);

        Point2D pointSelected = new Point2D.Float(x,y);
        double dist = Double.POSITIVE_INFINITY;

        /* find the closest contour point to the cursor */
        for(Point p : contour) {
            if (p.distance(pointSelected) < dist) {
                dist=p.distance(pointSelected);
                pointToReturn=p;
            }
        }

        /* if we're too far away, just return a null */
        /*
        statusBar.setText("Cursor = " + pointSelected.getX() +
                ", " +
                pointSelected.getY() +
                " : Distance = " + dist +
                " to point x = " + pointToReturn.getX() +
                " y= " + pointToReturn.getY());
         */
        if (dist>6.0) {
            pointToReturn = null;
        }

        /* return the point */
        return pointToReturn;
    }

    private float getXPositionAsFloat(java.awt.event.MouseEvent evt) {
        /* get the time bin under the cursor
         * Note that we need to add the value of the horizontal scrollbar to
         * this because the cursor value is only what is currently on the
         * screen, and we want to know relative to the whole image.
         * We also need to subtract the width of the frequency axis, or else
         * everything will be shifted (because the scrollbar panel's zero is at
         * the edge of the panel, not at the edge of the spectrogram
         */
        return (evt.getX()-AXIS_SIZE+scroller.getHorizontalScrollBar().getValue())
                / currHZoom;
    }

    private int getXPositionAsInt(java.awt.event.MouseEvent evt) {
        /* get the time bin under the cursor
         * Note that we need to add the value of the horizontal scrollbar to
         * this because the cursor value is only what is currently on the
         * screen, and we want to know relative to the whole image.
         * We also need to subtract the width of the frequency axis, or else
         * everything will be shifted (because the scrollbar panel's zero is at
         * the edge of the panel, not at the edge of the spectrogram
         */
        return (int) ((evt.getX()-AXIS_SIZE+scroller.getHorizontalScrollBar().getValue())
                / currHZoom);
    }

    private float getYPositionAsFloat(java.awt.event.MouseEvent evt) {
        /* get the freq bin under the cursor; note that we need to add the value
         * of the horizontal scrollbar to this because the cursor value is only
         * what is currently on the screen, and we want to know relative to the
         * whole image.  Also, flip the number because java starts counting from
         * the top of the image, not the bottom
         */
        return dataPanel.getDataHeight()-
                (evt.getY()+scroller.getVerticalScrollBar().getValue())
                / currVZoom - 1;
    }
    
    private int getYPositionAsInt(java.awt.event.MouseEvent evt) {
        /* get the freq bin under the cursor; note that we need to add the value
         * of the horizontal scrollbar to this because the cursor value is only
         * what is currently on the screen, and we want to know relative to the
         * whole image.  Also, flip the number because java starts counting from
         * the top of the image, not the bottom
         */
        return (int) dataPanel.getDataHeight()-
                (int) ((evt.getY()+scroller.getVerticalScrollBar().getValue())
                / currVZoom) - 1;
   }
    
    private void scrollerMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_scrollerMouseReleased
        showPeaks = false;
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        /* have we been dragging a contour point?  If so, drop it at the
         * current XY position
         */
        if (dragPoint != null) {

            int indx = 0;

            /* get the time bin under the cursor; note that we need to add the value
             * of the horizontal scrollbar to this because the cursor value is only
             * what is currently on the screen, and we want to know relative to the
             * whole image
             */
            /* Next part is removed, because we want to limit the motion to
             * vertical only (so we're not putting 2 peak frequencies in the
             * same time slice)
             *
            float x = (evt.getX()+scroller.getHorizontalScrollBar().getValue())
                    / currHZoom;
             */


            /* get the freq bin under the cursor; note that we need to add the value
             * of the horizontal scrollbar to this because the cursor value is only
             * what is currently on the screen, and we want to know relative to the
             * whole image.  Also, flip the number because java starts counting from
             * the top of the image, not the bottom
             */
            float y = getYPositionAsFloat(evt);

            // find the contour point we're moving
            for(int i=0; i<contour.length; i++) {
                if (contour[i].equals(dragPoint)) {
                    indx = i;
                    break;
                }
            }

            /* save the position */
            lastMovedPoint = new Point
                    ((int) dragPoint.getX(), (int) dragPoint.getY());
            lastMovedIndx = indx;

            /* update the time and freq bins of the contour point */
            if (indx != 0) {
//                contour[indx].setLocation((int) x, (int) y);
                contour[indx].setLocation((int) dragPoint.getX(), (int) y);
            } else {
                System.out.println("Could not find point in contour");
            }

            /* update the time and freq of the roccaContourDataUnit */
            /* ERROR - FOR SOME REASON, TIME IS NOT GETTING UPDATED */

            /* DO NOT UPDATE TIME - we want to keep the new frequency selected
             * in the same time slice
             *
            roccaContourDataBlock.
                    getDataUnit(indx, FFTDataBlock.REFERENCE_CURRENT).
                    setTimeMilliseconds(timeBins[(int) x]);
             */
            roccaContourDataBlock.
                    getDataUnit(indx, FFTDataBlock.REFERENCE_CURRENT).
                    setPeakFreq(freqBins[(int) y]);
            roccaContourDataBlock.
                    getDataUnit(indx, FFTDataBlock.REFERENCE_CURRENT).
                    setUserChanged(true);

            // set the contourChanged flag
            roccaContour.setContourChanged(true);
            dragPoint = null;

            /* enable the undo button */
            undoLastMove.setEnabled(true);

        /* if we're setting the start or end point, save the position where the
         * user clicked */
        } else if (settingStart || settingEnd) {

            /* get the time bin under the cursor; note that we need to add the value
             * of the horizontal scrollbar to this because the cursor value is only
             * what is currently on the screen, and we want to know relative to the
             * whole image
             */
            int x = getXPositionAsInt(evt);

            /* get the freq bin under the cursor; note that we need to add the value
             * of the horizontal scrollbar to this because the cursor value is only
             * what is currently on the screen, and we want to know relative to the
             * whole image.  Also, flip the number because java starts counting from
             * the top of the image, not the bottom
             */
            int y = getYPositionAsInt(evt);

            /* check to see if we're somewhere on the spectrogram */
            if(x < dataPanel.getDataWidth() && x >= 0
               && y < dataPanel.getDataHeight() && y >= 0) {

                /* if we're currently setting the start point, save the cursor
                 * position.  Reset the button and the flags.  Check if the end
                 * point has been selected yet.  If it hasn't (ie. if the window
                 * has just opened), then press that button
                 */
                if (settingStart) {
                    startTime = timeBins[x];
                    startFreq = freqBins[y];
                    selectStart.setSelected(false);
                    settingStart = false;
                    startIsSet = true;

                    if (!endIsSet) {
                        settingEnd = true;
                        selectEnd.setSelected(true);
                    }

                /* if we're currently setting the end point, save the cursor
                 * position.  Reset the button and the flags.  We don't need
                 * to check the startIsSet flag, because the only way to get
                 * to this spot is if the starting point has already been set
                 */
                } else {
                    endTime = timeBins[x];
                    endFreq = freqBins[y];
                    selectEnd.setSelected(false);
                    settingEnd = false;
                    endIsSet = true;
                }

                /* enable/disable buttons on the display, depending on whether
                 * or not the SelectStart / SelectEnd buttons are pressed
                 */
                updateDisplayForStartEndSelect();

                /* finally, if both the start and end times have been selected
                 * then recalculate the contour.
                 */
                if (startIsSet && endIsSet) {
                    RoccaContourDataBlock newRCDB = roccaContour.regenerateContour(
                            roccaContourDataBlock,
                            fftData,
                            startTime,
                            startFreq,
                            endTime,
                            endFreq,
                            rawData,
                            channel,
                            roccaParameters);
                    newRCDB.setAsAWhistle(true); 	// serialVersionUID=24 2016/08/10 added

                    roccaContourDataBlock = newRCDB;

                    /* extract the contour data from the contour data block */
                    this.contour = this.getContourData();
                }
            }

        /* if we're setting the high/lowpass filters, save the position where the
         * user clicked */
        } else if (settingLowpass || settingHighpass) {

            /* get the time bin under the cursor; note that we need to add the value
             * of the horizontal scrollbar to this because the cursor value is only
             * what is currently on the screen, and we want to know relative to the
             * whole image
             */
            int x = getXPositionAsInt(evt);

            /* get the freq bin under the cursor; note that we need to add the value
             * of the horizontal scrollbar to this because the cursor value is only
             * what is currently on the screen, and we want to know relative to the
             * whole image.  Also, flip the number because java starts counting from
             * the top of the image, not the bottom
             */
            int y = getYPositionAsInt(evt);

            /* check to see if we're somewhere on the spectrogram */
            if(x < dataPanel.getDataWidth() && x >= 0
               && y < dataPanel.getDataHeight() && y >= 0) {

                /* if we're currently setting the lowpass, save the cursor
                 * position, the old filter value, and reset the buttons and the flags.
                 */
                if (settingLowpass) {
                    /* make sure the lowPass is greater than the highPass, but still less
                     * than the max freq bin
                     */
                    if (freqBins[y] > highpassFreq && freqBins[y] <= freqBins[freqBins.length-1]) {
                        /* enable the reset button */
                        resetFilters.setEnabled(true);
                        oldLowPass = "" + lowpassFreq;
                        lowpassFreq = freqBins[y];
                        setLowpass.setSelected(false);
                        settingLowpass = false;
                        lowPass.setText(String.format("%5.0f", lowpassFreq));

                    } else {
                        System.out.println("Error in RoccaSpecPopUp - invalid lowpass filter value");
                    }


                /* if we're currently setting the highpass, save the cursor
                 * position, the old filter value, and reset the buttons and the flags.
                 */
                } else {
                    /* make sure the highPass is less than the lowPass, but still greater
                     * than 0
                     */
                    if (freqBins[y] < lowpassFreq && freqBins[y] >= 0.0) {
                        /* enable the reset button */
                        resetFilters.setEnabled(true);
                        oldHighPass = "" + highpassFreq;
                        highpassFreq = freqBins[y];
                        setHighpass.setSelected(false);
                        settingHighpass = false;
                        highPass.setText(String.format("%5.0f", highpassFreq));

                    } else {
                        System.out.println("Error in RoccaSpecPopUp - invalid highpass filter value");
                        highPass.setText(oldHighPass);
                    }
                }


                /* enable/disable buttons on the display, depending on whether
                 * or not the SelectStart / SelectEnd buttons are pressed
                 */
                updateDisplayForFilterSelect();

                /* set the parameters in RoccaContour */
                roccaContour.setFilters(highpassFreq, lowpassFreq);

                /* recalc the contour */
                recalcContourActionPerformed(null);
            }

    	/* if the user is simply selecting points to trace out the contour, save the point in the list */
	    } else if (tracingNow) {
            /* get the time/freq bins under the cursor */
            int x = getXPositionAsInt(evt);
            int y = getYPositionAsInt(evt);
            checkPoint(x,y);
	    }
    	    	

        /* repaint the screen */
        dataPanel.repaint();
    }//GEN-LAST:event_scrollerMouseReleased
    
    /**
     * Compares the point the user has just selected to the previous selection.  The time bin 
     * of the current selection must be higher than the previous time bin.  If it isn't, ignore
     * the selection.  Note that this routine first checks to make sure we are within the spectrogram,
     * and that at least one user-selected data point already exists.
     * 
     * @param x time bin of the current selection
     * @param y frequency bin of the current selection
     */
    private void checkPoint(int x, int y) {
        if(x < dataPanel.getDataWidth() && x >= 0
                && y < dataPanel.getDataHeight() && y >= 0 ) {
        	if (userPoints.size()==0) {
        		userPoints.add(new Point(x,y));
        	} else if (userPoints.size()>0 && 
        			x>(userPoints.get(userPoints.size()-1)).x) {
	            userPoints.add(new Point(x,y));    		
	    	}
        }
    }

    /**
     * Toggle button for user-selected contour points.  Pressing the toggle switches
     * to user-selection mode, whereby each mouse click is logged.  Depressing the toggle
     * takes those points, sorts them by time, sets the start and end times, fills in any
     * missing time positions through linear interpolation, and creates a new contour point
     * list and displays the results
     */
    private void btnPickPointsActionPerformed(java.awt.event.ActionEvent ev) {
  	  JToggleButton tBtn = (JToggleButton) ev.getSource(); 
  	  if (tBtn.isSelected()) {
  		  
  		  /* hide the contour and set the boolean */
  		  contourState = CONTOUROFF;
  		  tracingNow = true;
  		  
  		  /* reset the user points array */
  		  userPoints = new ArrayList<Point>();
  		  
  		  /* disable the other buttons */
  		  updateDisplayForPickPoints();
  		  
          /* repaint the spectrogram */
          dataPanel.repaint();
  		  
  	  } else {
  		  contourState = CONTOURON;
  		  tracingNow = false;
  		  
  		  /* disable the other buttons */
  		  updateDisplayForPickPoints();
  		  
  		  /* Continue only if at least 2 points were selected */
  		  if (userPoints.size()>=2) {
  			  
  			  // set flag in RoccaContour
  			  roccaContour.setContourChanged(true);
  			  
  	  		  /* sort the data by time - SKIP FOR NOW */
  			  
  			  /* set the start and end times */
  			  int xStart = userPoints.get(0).x;
  			  int yStart = userPoints.get(0).y;
              startTime = timeBins[xStart];
              startFreq = freqBins[yStart];
  			  int xEnd = userPoints.get(userPoints.size()-1).x;
  			  int yEnd = userPoints.get(userPoints.size()-1).y;
              endTime = timeBins[xEnd];
              endFreq = freqBins[yEnd];
              
              /* go through the user points and copy the data into a new array,
               * interpolating any missing values */
              ArrayList<Point> tempPoints = new ArrayList<Point>();
              int x0;
              int y0;
              int x1;
              int y1;
              int yNew;
              tempPoints.add(userPoints.get(0));
              for (int i=1; i<userPoints.size(); i++) {
            	  if ((userPoints.get(i).x-tempPoints.get(tempPoints.size()-1).x)>1) {
            		  x0=tempPoints.get(tempPoints.size()-1).x;
            		  y0=tempPoints.get(tempPoints.size()-1).y;
            		  x1=userPoints.get(i).x;
            		  y1=userPoints.get(i).y;
            		  
            		  for (int xNew=x0+1; xNew<x1; xNew++) {
            			  yNew = y0 + (xNew-x0)*(y1-y0)/(x1-x0);
            			  tempPoints.add(new Point(xNew,yNew));
            		  }            		  
            	  }
            	  tempPoints.add(userPoints.get(i));
              }
            	  
              
              /* convert the contour points into RoccaContourDataUnits.  Any points that
               * are outside of the start or end have a frequency set to OUTOFRANGE.  Use
               * 0.0 for duty cycle, energy and windows RMS fields for now, and recalc later  */
              RoccaContourDataBlock newRCDB =
                      new RoccaContourDataBlock(roccaProcess,
                      roccaContourDataBlock.getChannelMap());    
              newRCDB.setAsAWhistle(true); 	// serialVersioUID=24 2016/08/10 added
              for (int i=0; i<timeBins.length; i++) {
                  RoccaContourDataUnit rcdu = new RoccaContourDataUnit (
                          timeBins[i],
                          roccaContourDataBlock.getChannelMap(),
                          fftData.getFirstUnitAfter(timeBins[i]).getStartSample(),
                          fftData.getFirstUnitAfter(timeBins[i]).getSampleDuration() );
                  rcdu.setTime(timeBins[i]);
            	  if (i<xStart | i>xEnd) {
            		  rcdu.setDutyCycle(0.0);
            		  rcdu.setEnergy(0.0);
            		  rcdu.setPeakFreq(RoccaContour.OUTOFRANGE);
            		  rcdu.setWindowRMS(0.0);
            	  } else {
            		  rcdu.setDutyCycle(0.0);
            		  rcdu.setEnergy(0.0);
            		  rcdu.setPeakFreq(freqBins[tempPoints.get(i-xStart).y]);
            		  rcdu.setWindowRMS(0.0);
            		  rcdu.setUserChanged(true);
            	  }
            	  newRCDB.addPamData(rcdu);
              }
              roccaContourDataBlock = newRCDB;

              /* extract the contour data from the contour data block */
              //Point[] oldContour = contour;
              this.contour = this.getContourData();
              
              // recalculate the params based on the new contour
              this.recalcContourActionPerformed(null);

              /* repaint the spectrogram */
              dataPanel.repaint();
  		  }
  		  
  	  }

    }
    
    
    private void updateDisplayForStartEndSelect() {
        if (settingStart) {
            classifiedAsLbl.setEnabled(false);
            classify.setEnabled(false);
            highPass.setEnabled(false);
            highpassLbl.setEnabled(false);
            highpassUnitsLbl.setEnabled(false);
            setHighpass.setEnabled(false);
            lowPass.setEnabled(false);
            lowpassLbl.setEnabled(false);
            lowpassUnitsLbl.setEnabled(false);
            setLowpass.setEnabled(false);
            noiseSensLbl.setEnabled(false);
            noiseSensSpinner.setEnabled(false);
            recalcContour.setEnabled(false);
            reset.setEnabled(false);
            resetContour.setEnabled(false);
            resetFilters.setEnabled(false);
            saveExit.setEnabled(false);
            saveNewNameExit.setEnabled(false);
            saveWavOnly.setEnabled(false);
            selectEnd.setEnabled(false);
            toggleContour.setEnabled(false);
            btnPickPoints.setEnabled(false);
            undoLastMove.setEnabled(false);
            disableClassify();

        } else if (settingEnd) {
            classifiedAsLbl.setEnabled(false);
            classify.setEnabled(false);
            highPass.setEnabled(false);
            highpassLbl.setEnabled(false);
            highpassUnitsLbl.setEnabled(false);
            setHighpass.setEnabled(false);
            lowPass.setEnabled(false);
            lowpassLbl.setEnabled(false);
            lowpassUnitsLbl.setEnabled(false);
            setLowpass.setEnabled(false);
            noiseSensLbl.setEnabled(false);
            noiseSensSpinner.setEnabled(false);
            recalcContour.setEnabled(false);
            reset.setEnabled(false);
            resetContour.setEnabled(false);
            resetFilters.setEnabled(false);
            saveExit.setEnabled(false);
            saveNewNameExit.setEnabled(false);
            saveWavOnly.setEnabled(false);
            selectStart.setEnabled(false);
            toggleContour.setEnabled(false);
            btnPickPoints.setEnabled(false);
            undoLastMove.setEnabled(false);
            disableClassify();

        } else {
            classify.setEnabled(true);
            highPass.setEnabled(true);
            highpassLbl.setEnabled(true);
            highpassUnitsLbl.setEnabled(true);
            setHighpass.setEnabled(true);
            lowPass.setEnabled(true);
            lowpassLbl.setEnabled(true);
            lowpassUnitsLbl.setEnabled(true);
            setLowpass.setEnabled(true);
            noiseSensLbl.setEnabled(true);
            noiseSensSpinner.setEnabled(true);
            recalcContour.setEnabled(true);
            reset.setEnabled(true);
            resetContour.setEnabled(true);
            resetFilters.setEnabled(true);
            selectStart.setEnabled(true);
            selectEnd.setEnabled(true);
            toggleContour.setEnabled(true);
            btnPickPoints.setEnabled(true);
            recalcContour.setEnabled(true);
            resetContour.setEnabled(true);

            if (lastMovedPoint==null) {
                undoLastMove.setEnabled(false);
            } else {
                undoLastMove.setEnabled(true);
            }
        }
    }

    private void updateDisplayForPickPoints() {
        if (tracingNow) {
            classifiedAsLbl.setEnabled(false);
            classify.setEnabled(false);
            highPass.setEnabled(false);
            highpassLbl.setEnabled(false);
            highpassUnitsLbl.setEnabled(false);
            setHighpass.setEnabled(false);
            lowPass.setEnabled(false);
            lowpassLbl.setEnabled(false);
            lowpassUnitsLbl.setEnabled(false);
            setLowpass.setEnabled(false);
            noiseSensLbl.setEnabled(false);
            noiseSensSpinner.setEnabled(false);
            recalcContour.setEnabled(false);
            reset.setEnabled(false);
            resetContour.setEnabled(false);
            resetFilters.setEnabled(false);
            saveExit.setEnabled(false);
            saveNewNameExit.setEnabled(false);
            saveWavOnly.setEnabled(false);
            selectStart.setEnabled(false);
            selectEnd.setEnabled(false);
            toggleContour.setEnabled(false);
            undoLastMove.setEnabled(false);
            disableClassify();

        } else {
            classify.setEnabled(true);
            highPass.setEnabled(true);
            highpassLbl.setEnabled(true);
            highpassUnitsLbl.setEnabled(true);
            setHighpass.setEnabled(true);
            lowPass.setEnabled(true);
            lowpassLbl.setEnabled(true);
            lowpassUnitsLbl.setEnabled(true);
            setLowpass.setEnabled(true);
            noiseSensLbl.setEnabled(true);
            noiseSensSpinner.setEnabled(true);
            recalcContour.setEnabled(true);
            reset.setEnabled(true);
            resetContour.setEnabled(true);
            resetFilters.setEnabled(true);
            selectStart.setEnabled(true);
            selectEnd.setEnabled(true);
            toggleContour.setEnabled(true);
            btnPickPoints.setEnabled(true);
            recalcContour.setEnabled(true);
            resetContour.setEnabled(true);

            if (lastMovedPoint==null) {
                undoLastMove.setEnabled(false);
            } else {
                undoLastMove.setEnabled(true);
            }
        }
    }

    private void zoomOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutActionPerformed
        zoomDataPanel(currHZoom/HZOOM_INCR, currVZoom/VZOOM_INCR);
    }//GEN-LAST:event_zoomOutActionPerformed

    private void brightDecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_brightDecActionPerformed
        double colorOffset = dataPanel.getColorOffset();
        colorOffset -= BRIGHT_INCR;
        dataPanel.setColorOffset(colorOffset);
    }//GEN-LAST:event_brightDecActionPerformed

    private void resetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetActionPerformed
        float h = (float)(this.getPreferredSize().getHeight()
                          - zoomIn.getPreferredSize().getHeight()
                          - scroller.getHorizontalScrollBar()
                                    .getPreferredSize().getHeight()
                          - saveExit.getPreferredSize().getHeight()
                          - statusBar.getPreferredSize().getHeight());
//                             - toolbarTop.getPreferredSize().getHeight());
        // no, this should not be hardcoded (the 3 especially), but I
        // don't know how else to properly set the initial zoom so
        // that no vertial scrollbar is required
        boolean vz = vZoom, hz = hZoom;
        vZoom = true;  hZoom = true;
//        zoomDataPanel(1, (h-3)/(float)dataPanel.getDataHeight());
        zoomDataPanel(1, 1);
        vZoom = vz;  hZoom = hz;
    }//GEN-LAST:event_resetActionPerformed

    private void brightIncActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_brightIncActionPerformed
        double colorOffset = dataPanel.getColorOffset();
        colorOffset += BRIGHT_INCR;
        dataPanel.setColorOffset(colorOffset);
    }//GEN-LAST:event_brightIncActionPerformed

    private void contrastDecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contrastDecActionPerformed
        double colorScale = dataPanel.getColorScale();
        colorScale -= CONTRAST_INCR;
        dataPanel.setColorScale(colorScale);
    }//GEN-LAST:event_contrastDecActionPerformed

    private void contrastIncActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contrastIncActionPerformed
        double colorScale = dataPanel.getColorScale();
        colorScale += CONTRAST_INCR;
        dataPanel.setColorScale(colorScale);
    }//GEN-LAST:event_contrastIncActionPerformed

    private void classifyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_classifyActionPerformed
    	// change this code, since we now allow the user to run Rocca without
    	// a classifier.  Trying to load a non-existent classifier for every
    	// whistle or click is just a waste of time - it should have been loaded
    	// during startup or when set in the dialog
//
        // if we haven't loaded the classifier yet, do that now
//        if (!roccaProcess.classifierLoaded) {
//            roccaProcess.setClassifierLoaded
//                    (roccaProcess.roccaClassifier.setUpClassifier());
//        }

        // calculate the statistics for the current contour
        roccaContourDataBlock.calculateStatistics();

        // call the classifier
        roccaProcess.roccaClassifier.classifyContour2(roccaContourDataBlock);
//        System.out.println(String.format("Whistle classified as %s ***", roccaContourDataBlock.getClassifiedAs()));

        // enable and update the label
        classifiedAsLbl.setText
                ("Classified As " + roccaContourDataBlock.getClassifiedAs());
        classifiedAsLbl.setEnabled(true);

        // enable (and set to black) the SaveAndExit button
        saveExit.setEnabled(true);
        saveNewNameExit.setEnabled(true);
        saveWavOnly.setEnabled(true);
//        saveExit.setText("<html><font color=#000000>Save and<br>Exit</font></html>");
        saveExit.setText("<html><font color=#000000>Save as encounter "
                + sNum + "</font></html>");
        saveNewNameExit.setText("<html><font color=#000000>Save as diff encounter</font></html>");
        saveWavOnly.setText("<html><font color=#000000>Save WAV only</font></html>");

        // change the classify button to say 'reclassify'
        classify.setText("Reclassify");
    }//GEN-LAST:event_classifyActionPerformed

    private void saveExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveExitActionPerformed
        /* check that the species list for the current classifier matches the
         * species list of the detection we're trying to save to.  If it
         * doesn't, force the user to create a new detection number.  This can
         * happen if the user captured a sound event after scrolling backwards
         * through the detection list to an older detection created with a
         * different classifier.
         * Note that this method also checks if the classifier species list
         * contains 'Ambig', and adds it if it doesn't.
         */
        String[] sNumList = roccaProcess.roccaControl.roccaSidePanel.rsdb.
                findDataUnitBySNum(sNum).getSpeciesAsString();
        String[] classifierList = RoccaSightingDataUnit.validateSpeciesList(
                roccaProcess.roccaClassifier.getClassifierSpList());
        if (!Arrays.equals(sNumList, classifierList)) {
            
            /* if the arrays are not equal, throw an error and force the user
             * to create a new detection
             */
            String message = "Classifier species list does not match the \n" +
                    "encounter number species list.  You must create\n" +
                    "a new encounter for this whistle.";
            int messageType = JOptionPane.ERROR_MESSAGE;
            JOptionPane.showMessageDialog
                    (null, message, "Species List conflict", messageType);
            saveNewNameExitActionPerformed(evt);

        } else {

            /* if the arrays are equal,
             * add the current tree votes to the tree vote tally on the
             * sidepanel
             */
            roccaProcess.roccaControl.roccaSidePanel.addSpeciesTreeVotes(sNum,
                    roccaContourDataBlock.getTreeVotes());

            // update the tab panel
            // serialVersionUID=15 2014/11/12 add start time to method call
            // serialVersionUID=22 2015/06/13 add duration and boolean to method call
            roccaProcess.roccaControl.roccaSidePanel.incSpeciesCount
                    (sNum,
                    roccaContourDataBlock.getClassifiedAs(), 
                    roccaContourDataBlock.getStartTime(),
                    roccaContourDataBlock.getDuration(),
                    false);

            // save the contour and the statistics
            boolean contourSaved = saveContour(false);
            boolean contourPointsSaved = saveContourPoints();
            boolean contourStatsSaved = saveContourStats();

            // close this window
            discardExitActionPerformed(null);
        }
    }//GEN-LAST:event_saveExitActionPerformed

    private void discardExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_discardExitActionPerformed
    	// 2017/12/4 set the natural lifetime back to 0
    	fftData.setNaturalLifetimeMillis(0);
    	rawData.setNaturalLifetimeMillis(0);
    	if (roccaContourDataBlock != null) {
        	roccaContourDataBlock.setNaturalLifetime(0);
    	}
    	
        /* clear the FFTDataBlock for garbage collection */
        fftData = null;
        rawData = null;
        roccaContourDataBlock = null;

        /* get rid of the pop-up frame */
        thisFrame.setVisible(false);
        thisFrame.dispose();
    }//GEN-LAST:event_discardExitActionPerformed

    private void noiseSensSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_noiseSensSpinnerStateChanged

        /* set the parameter to the new spinner value */
        roccaParameters.setNoiseSensitivity( (Double) noiseSensSpinner.getValue());

        /* recalculate the contour with the new parameter */
        roccaContourDataBlock = roccaContour.generateContour(
                fftData,
                startTime,
                startFreq,
                endTime,
                endFreq,
                rawData,
                channel,
                roccaParameters);
        roccaContourDataBlock.setAsAWhistle(true); 	// serialVersioUID=24 2016/08/10 added

        /* extract the contour data from the contour data block */
        this.contour = this.getContourData();

        /* update the spectrogram with the new contour */
        dataPanel.repaint();

        /* disable the classify button */
        disableClassify();
    }//GEN-LAST:event_noiseSensSpinnerStateChanged

    private void scrollerMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_scrollerMouseDragged

        /* get the x position of the cursor; note that we need to add the value
         * of the horizontal scrollbar to this because the cursor value is only
         * what is currently on the screen, and we want to know relative to the
         * whole image
         */
        int x = getXPositionAsInt(evt);

        /* get the y position of the cursor; note that we need to add the value
         * of the horizontal scrollbar to this because the cursor value is only
         * what is currently on the screen, and we want to know relative to the
         * whole image.  Also, flip the number because java starts counting from
         * the top of the image, not the bottom
         */
        int y = getYPositionAsInt(evt);
        
        /* if we're within the bounds of the spectrogram data, display the time
         * and frequency bins under the cursor
         */
        if(x < dataPanel.getDataWidth() && x >= 0
           && y < dataPanel.getDataHeight() && y >= 0) {
            statusBar.setText("Dragging contour point to " + timeBins[x] + " milliseconds, " +
                    freqBins[y] + " Hz ");
        }
        
        /* if we're tracing the contour, log the x,y point */
        if (tracingNow) {
        	checkPoint(x,y);
            /* repaint the screen */
            dataPanel.repaint();
        }
    }//GEN-LAST:event_scrollerMouseDragged

    /**
     * Reset the contour, clearing any user-changed points
     *
     * @param evt button event
     */
    private void resetContourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetContourActionPerformed
        /* clear old data and create a new contour datablock */
        roccaContour.clearOldDataBlock();
        roccaContour.setContourChanged(false);
        roccaContourDataBlock = roccaContour.generateContour(
                fftData,
                startTime,
                startFreq,
                endTime,
                endFreq,
                rawData,
                channel,
                roccaParameters);
        roccaContourDataBlock.setAsAWhistle(true); 	// serialVersioUID=24 2016/08/10 added

        /* extract the contour data from the contour data block */
        //Point[] oldContour = contour;
        this.contour = this.getContourData();

        /* repaint the spectorgram */
        dataPanel.repaint();

        /* reset the classify button */
        disableClassify();

        /* disable the undo button */
        undoLastMove.setEnabled(false);
}//GEN-LAST:event_resetContourActionPerformed

    private void disableClassify() {
        // disable the label
        classifiedAsLbl.setText
                ("Not Classified");
        classifiedAsLbl.setEnabled(false);

        // disable (and set to gray) the SaveAndExit buttons
        saveExit.setEnabled(false);
        saveNewNameExit.setEnabled(false);
        saveWavOnly.setEnabled(false);
        saveExit.setText("<html><font color=#808080>Save as encounter "
                + sNum + "</font></html>");
        saveNewNameExit.setText("<html><font color=#808080>Save as diff encounter</font></html>");
        saveWavOnly.setText("<html><font color=#808080>Save WAV only</font></html>");

        // change the classify button to say 'Classify'
        classify.setText("Classify");
    }

    private void saveNewNameExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveNewNameExitActionPerformed

        /* get the new sighting number.  If the user hit cancel, use the old
         * sighting number
         */
        String newSNum =
                roccaProcess.roccaControl.roccaSidePanel.sidePanel.addASighting(true);
        if (newSNum!=null) {
            sNum = newSNum;
        }

        saveExitActionPerformed(evt);
    }//GEN-LAST:event_saveNewNameExitActionPerformed

    private void saveWavOnlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveWavOnlyActionPerformed
        // save the contour
        boolean contourSaved = saveContour(true);

        // close this window
        discardExitActionPerformed(null);
    }//GEN-LAST:event_saveWavOnlyActionPerformed

   /**
     * Saves the contour points in the datablock in a csv file.  Saves the time
     * and peak frequency of each point, along with the duty cycle, energy, and
     * windows RMS
     * <p> Note that this method has now (as of 2012/08/12) been duplicated in RoccaProcess
     * in order to accommodate data from the Whistle and Moan detector.  It should be
     * deleted from RoccaSpecPopUp at some point in the future
     *
     * @return saveContourPoints boolean indicating success or failure
     */
    public boolean saveContourPoints() {

        // figure out the filename, based on the input filename and the time
        File contourFilename = getDataBlockFilename(roccaContourDataBlock, ".csv");

        // if this file already exists, warn the user that it will be overwritten
        if (contourFilename.exists()) {
  			String title = "Warning - Contour file already exists";
  			String msg = "<html><p>The file</p><b> " + 
  					contourFilename.getAbsolutePath() +
  					"</b><br><br><p>already exists and will be overwritten with the new contour data.  " +
  					"This will happen when re-analyzing data that has already been analyzed, but it may " +
  					"also indicate that your filename template is inadequate and returning duplicate filenames.</p></html>";
  			String help = null;
  			int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.WARNING_MESSAGE, help);
        }


        try {
//            System.out.println(String.format("writing contour points to ... %s",
//                    contourFilename));
            BufferedWriter writer = new BufferedWriter(new FileWriter(contourFilename));

            // Write the header line
            String hdr = new String("Time [ms], Peak Frequency [Hz], Duty Cycle, Energy, WindowRMS");
            writer.write(hdr);
            writer.newLine();

            /* Cycle through the contour points one at a time.  Save the points
             * that are within the range selected by the user (points outside
             * of the range will have a frequency equal to the constant
             * NO_CONTOUR_HERE
             */
            RoccaContourDataUnit rcdu = null;
            int numPoints = roccaContourDataBlock.getUnitsCount();
            for (int i = 0; i < numPoints; i++) {
                rcdu = roccaContourDataBlock.getDataUnit(i, RoccaContourDataBlock.REFERENCE_CURRENT);
                if (rcdu.getPeakFreq() != RoccaParameters.NO_CONTOUR_HERE) {
                    String contourPoints =   rcdu.getTime() + "," +
                            rcdu.getPeakFreq() + "," +
                            rcdu.getDutyCycle() + "," +
                            rcdu.getEnergy() + "," +
                            rcdu.getWindowRMS();
                    writer.write(contourPoints);
                    writer.newLine();
                }
            }
            writer.close();
        } catch(FileNotFoundException ex) {
            System.out.println("Could not find file");
            ex.printStackTrace();
            return false;
        } catch(IOException ex) {
            System.out.println("Could not write contour points to file");
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Generates a filename for the wav clip and the csv file containing the
     * contour points, based on the filename template stored in RoccaParameters
     *
     * @param rcdb The RoccaContourDataBlock containing the data to save
     * @param ext The filename extension (wav or csv)
     * @return A File
     */
    public File getDataBlockFilename(RoccaContourDataBlock rcdb, String ext) {
        File soundFile = rcdb.getWavFilename();
		FileParts fileParts = new FileParts(soundFile);
		String nameBit = fileParts.getFileName();
//		String fNameString =
//			String.format("%s\\%s-%s-Channel%d-Time%d%s",
//					roccaProcess.roccaControl.roccaParameters.roccaOutputDirectory.getAbsolutePath(),
//                    sNum,
//                    nameBit,
//                    channel,
//					rcdb.getFirstUnit().getTime(),
//                    ext);
//		String fNameString =
//			String.format("%s\\Detection%d-%s-Channel%d-",
//					roccaProcess.roccaControl.roccaParameters.roccaOutputDirectory.getAbsolutePath(),
//                    thisDetection,
//                    sNum,
//                    nameBit,
//                    channel);
//        String fullFilename = PamCalendar.createFileName
//                (rcdb.getFirstUnit().getTime(), fNameString, ext);
//        File fName = new File(fNameString);
//        File fName = new File(fullFilename);
 //       return fName;
        
        /* get the filename template */
        String filenameTemplate = roccaParameters.getFilenameTemplate();
        String filenameString = "";
        char c;

        /* step through the template, one character at a time, looking for codes */
        for ( int i=0,n=filenameTemplate.length(); i<n; i++ ) {
            c = filenameTemplate.charAt(i);

            /* if we've hit a symbolic code, determine which one it is and
             * substitute the correct text
             */
            if (c=='%') {
                if (i==n-1) break;
                i++;
                c = filenameTemplate.charAt(i);
                switch (c) {

                    /* source name */
                    case 'f':
//                        File soundFile = rcdb.getWavFilename();
//                        FileParts fileParts = new FileParts(soundFile);
//                        String nameBit = fileParts.getFileName();
                        filenameString = filenameString + nameBit;
                        break;

                    /* detection number */
                    case 'n':
                        filenameString = filenameString + sNum;
                        break;

                    /* detection tally */
                    case 'X':
                        filenameString = filenameString +
                                String.format("%d", thisDetection);
                        break;

                    /* channel/track number */
                    case 't':
                        filenameString = filenameString +
                                String.format("%d", channel);
                        break;

                    /* it has something to do with the date or time, so initialize
                     * some calendar variables
                     */
                    default:
                        long firstTime = rcdb.getFirstUnit().getTime();
                        Calendar cal = PamCalendar.getCalendarDate(firstTime);
                        DateFormat df = new SimpleDateFormat("yyyy");
                        DecimalFormat decf = new DecimalFormat(".#");
                        Double msAsDouble = 0.0;
                        int msAsInt = 0;

                        switch (c) {

                            /* year, 4 digits */
                            case 'Y':
                                df = new SimpleDateFormat("yyyy");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* year, 2 digits */
                            case 'y':
                                df = new SimpleDateFormat("yy");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* month */
                            case 'M':
                                df = new SimpleDateFormat("MM");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* day of the month */
                            case 'D':
                                df = new SimpleDateFormat("dd");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* day of the year (3 digits) */
                            case 'J':
                                df = new SimpleDateFormat("DDD");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* hour, 24-hour clock */
                            case 'H':
                                df = new SimpleDateFormat("kk");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* hour, 12-hour clock */
                            case 'h':
                                df = new SimpleDateFormat("hh");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* 'am' or 'pm' */
                            case 'a':
                                df = new SimpleDateFormat("a");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* minute */
                            case 'm':
                                df = new SimpleDateFormat("mm");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* second */
                            case 's':
                                df = new SimpleDateFormat("ss");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* second of the day (5 digits) */
                            case 'S':
                                int secOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 * 60 +
                                        cal.get(Calendar.MINUTE) * 60 +
                                        cal.get(Calendar.SECOND);
                                filenameString = filenameString +
                                        String.format("%05d", secOfDay);
                                break;

                            /* tenths of a second */
                            case 'd':
                                df = new SimpleDateFormat(".SSS");
                        		df.setTimeZone(cal.getTimeZone());
                                msAsDouble = Double.valueOf(df.format(cal.getTime()).toString());
                                decf = new DecimalFormat(".#");
                                msAsInt = (int) (Double.valueOf(decf.format(msAsDouble))*10);
                                filenameString = filenameString + String.format("%01d", msAsInt);
                                break;

                            /* hundredths of a second */
                            case 'c':
                                df = new SimpleDateFormat(".SSS");
                        		df.setTimeZone(cal.getTimeZone());
                                msAsDouble = Double.valueOf(df.format(cal.getTime()).toString());
                                decf = new DecimalFormat(".##");
                                msAsInt = (int) (Double.valueOf(decf.format(msAsDouble))*100);
                                filenameString = filenameString + String.format("%02d", msAsInt);
                                break;

                            /* thousandths of a second */
                            case 'i':
                                df = new SimpleDateFormat("SSS");
                        		df.setTimeZone(cal.getTimeZone());
                                filenameString = filenameString +
                                        df.format(cal.getTime());
                                break;

                            /* code not recognized; just use the % sign and character */
                            default:
                                filenameString = filenameString + "%" + c;
                                break;
                    }
                }

            /* if we haven't hit a code, just copy the character to the filename
             * string
             */
            } else {
               filenameString = filenameString + c;
            }
        }
        filenameString = filenameString + ext;
//       return fName;
       return new File(roccaParameters.roccaOutputDirectory.getAbsolutePath(),
               filenameString);
   }


    /**
     * Save the clip as a wav file.
     * <p> Note that this method has now (as of 2012/08/12) been duplicated in RoccaProcess
     * in order to accomodate data from the Whistle & Moan detector.  It should be
     * deleted from RoccaSpecPopUp at some point in the future
     *
     * @param markWavForRedo boolean indicating whether to mark the wav file
     * 'redo' or not
     * @return boolean indicating success
     */
    private boolean saveContour(boolean markWavForRedo) {

        if (rawData == null) {
            System.out.println("RoccaSpecPopUp: No source audio data found");
            return false;
        }
        int channelsToSave = roccaParameters.getChannelMap();
        long currentStartSample = rawData.getFirstUnit().getStartSample();
        long deltaMS = (rawData.getLastUnit().getTimeMilliseconds()-
                rawData.getFirstUnit().getTimeMilliseconds());
        float deltaS = ((float) deltaMS)/1000;
        float dur = deltaS*rawData.getSampleRate();
        int duration = (int) dur;

        /* get the raw data */
        double[][] rawDataValues = null;
        try {
        	if (roccaParameters.isTrimWav()) {
        		rawDataValues = rawData.getSamplesForMillis(startTime, endTime-startTime, channelsToSave);
        	}
        	else {
        		rawDataValues = rawData.getSamples(currentStartSample, duration, channelsToSave );
        	}
        }
        catch (RawDataUnavailableException e) {
        	System.out.println(e.getMessage());	
        }

        if (rawDataValues==null) {
            System.out.println(String.format
                    ("Error obtaining raw data during contour save; currentStartSample = %d",
                    currentStartSample));
        }

        /* figure out the filename, based on the input filename and the time */
        File contourFilename = getDataBlockFilename(roccaContourDataBlock, ".wav");
        if (markWavForRedo) {
            String thePath = contourFilename.getParent();
            String theName = contourFilename.getName();
            contourFilename = new File(
                    thePath,
                    "redo-" + theName);
        }
        
        // if this file already exists, warn the user that it will be overwritten
        if (contourFilename.exists()) {
  			String title = "Warning - Wav file already exists";
  			String msg = "<html><p>The file</p><b> " + 
  					contourFilename.getAbsolutePath() +
  					"</b><br><br><p>already exists and will be overwritten with the new audio data.  " +
  					"This will happen when re-analyzing data that has already been analyzed, but it may " +
  					"also indicate that your filename template is inadequate and returning duplicate filenames.</p></html>";
  			String help = null;
  			int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.WARNING_MESSAGE, help);
        }


        /*
         * Replace with new single call to wav file writer. DG. 21.2.2016 
         */
//        /* create a new WavFile object */
//        WavFile wavFile = new WavFile(contourFilename.getAbsolutePath(), "w");
//
//        /* save the data to a wav file */
////        System.out.println(String.format("writing contour  to ... %s",
////                contourFilename));
//        wavFile.write(rawData.getSampleRate(),
//                PamUtils.getNumChannels(channelsToSave),
//                rawDataValues );
        WavFileWriter.write(contourFilename.getAbsolutePath(), rawData.getSampleRate(), rawDataValues);
        return true;
    }

    /**
     * Saves all contour stats to the contour stats summary file, as defined in
     * the Rocca Parameters.  If the file does not exist, it is created and a
     * header line is written.  If it does exist, data is appended to the end.
     * Each contour is 1 row.
     *
     * Note that the last few columns contain the random forest votes, and so
     * the number of columns depends on how many species are in the classifier.
     * This won't be a problem if the same classifier is always used, but if a
     * different classifier with a different number of species is tried out the
     * number of columns from one row to the next will not match.  More
     * importantly, the order of the species from one row to the next might
     * not match and you would never know unless you remember.  For this
     * reason, instead of saving the species names as the column headers (which
     * would only be accurate for the classifier selected when the file had
     * been created), the species names are written in the row AFTER the tree
     * votes.  The classifier name is also saved, for reference.  While this
     * is awkward, at least results from different classifiers can be
     * interpreted properly without having to guess which vote is for which species
     *
     * <p> Note that this method has now (as of 2012/08/12) been duplicated in RoccaProcess
     * in order to accommodate data from the Whistle and Moan detector.  It should be
     * deleted from RoccaSpecPopUp at some point in the future
     *
     * @return boolean indicating whether or not the save was successful
     */
    public boolean saveContourStats() {
        // open file
        try {
            File fName = roccaParameters.roccaContourStatsOutputFilename;
//            System.out.println(String.format("writing contour stats to ... %s", fName.getAbsolutePath()));

            // write a header line if this is a new file
            if (!fName.exists()) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fName));
                String hdr = roccaContourDataBlock.createContourStatsHeader();
                writer.write(hdr);
                writer.newLine();
                writer.close();
            }

            /* create a String containing the contour stats and associated
             * variables (filename, species list, detection number, etc) we
             * want to save, and save it to the file.  This method will also
             * create and populate a RoccaLoggingDataUnit
             */
            BufferedWriter writer = new BufferedWriter(new FileWriter(fName, true));
//            String contourStats = roccaContourDataBlock.createContourStatsString();
            String contourStats =
                    roccaContourDataBlock.createContourStatsString
                    ((getDataBlockFilename(roccaContourDataBlock, ".wav")).getAbsolutePath(),
                    thisDetection);
            writer.write(contourStats);
            writer.newLine();
            writer.close();

        } catch(FileNotFoundException ex) {
            System.out.println("Could not find file");
            ex.printStackTrace();
            return false;
        } catch(IOException ex) {
            System.out.println("Could not write contour statistics to file");
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private void undoLastMoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoLastMoveActionPerformed
        contour[lastMovedIndx].setLocation((int) lastMovedPoint.getX(), (int) lastMovedPoint.getY());

        /* do not change the time, so that the freq always stays in the same
         * time slice
        roccaContourDataBlock.
                getDataUnit(lastMovedIndx, FFTDataBlock.REFERENCE_CURRENT).
                setTimeMilliseconds(timeBins[(int) lastMovedPoint.getX()]);
         */
        roccaContourDataBlock.
                getDataUnit(lastMovedIndx, FFTDataBlock.REFERENCE_CURRENT).
                setPeakFreq(freqBins[(int) lastMovedPoint.getY()]);

        /* clear the last point fields */
        lastMovedIndx = -1;
        lastMovedPoint = null;

        /* disable the undo button */
        undoLastMove.setEnabled(false);

        /* repaint the screen */
        dataPanel.repaint();
    }//GEN-LAST:event_undoLastMoveActionPerformed

    /**
     * Recalculate the contour, keeping any user-changed points where they are
     *
     * @param evt button event
     */
    private void recalcContourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recalcContourActionPerformed
        /* create a new contour datablock */
        RoccaContourDataBlock newRCDB = roccaContour.regenerateContour(
                roccaContourDataBlock,
                fftData,
                startTime,
                startFreq,
                endTime,
                endFreq,
                rawData,
                channel,
                roccaParameters);
        newRCDB.setAsAWhistle(true); 	// serialVersioUID=24 2016/08/10 added
        roccaContourDataBlock = newRCDB; 	// serialVersionUID=24 2016/08/10

        /* extract the contour data from the contour data block */
        //Point[] oldContour = contour;
        this.contour = this.getContourData();

        /* repaint the spectrogram */
        dataPanel.repaint();
    }//GEN-LAST:event_recalcContourActionPerformed

    private void resetFiltersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetFiltersActionPerformed
        /* get the min/max freq values */
        highpassFreq = 0.0;
        lowpassFreq = freqBins[freqBins.length-1];

        /* set the values in the text fields */
        highPass.setText("0");
        lowPass.setText(String.format("%5.0f", lowpassFreq));

        /* set the parameters in RoccaContour */
        roccaContour.setFilters(highpassFreq, lowpassFreq);

        /* disable the reset button */
        resetFilters.setEnabled(false);
        
        /* recalc the contour */
        recalcContourActionPerformed(evt);
    }//GEN-LAST:event_resetFiltersActionPerformed

    private void highPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_highPassActionPerformed
        /* read the values in the text fields */
        double hiFreq = Double.valueOf(highPass.getText()).doubleValue();
        lowpassFreq = Double.valueOf(lowPass.getText()).doubleValue();

        /* make sure the highPass is less than the lowPass, but still greater
         * than 0
         */
        if (hiFreq < lowpassFreq && hiFreq >= 0.0) {
            /* enable the reset button */
            resetFilters.setEnabled(true);

            /* save the frequency */
            oldHighPass = String.format("%5.0f", highpassFreq);
            highpassFreq = hiFreq;
            highPass.setText(String.format("%5.0f", hiFreq));

            /* set the parameters in RoccaContour */
            roccaContour.setFilters(highpassFreq, lowpassFreq);

            /* recalc the contour */
            recalcContourActionPerformed(evt);

        } else {
            System.out.println("Error in RoccaSpecPopUp - invalid highpass filter value");
            highPass.setText(String.format("%5.0f", highpassFreq));
        }
    }//GEN-LAST:event_highPassActionPerformed

    private void lowPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lowPassActionPerformed
        /* read the values in the text fields */
        highpassFreq = Double.valueOf(highPass.getText()).doubleValue();
        double loFreq = Double.valueOf(lowPass.getText()).doubleValue();

        /* make sure the lowPass is greater than the highPass, but still lower
         * than the max frequency
         */
        if (loFreq > highpassFreq && loFreq <= freqBins[freqBins.length-1]) {
            /* enable the reset button */
            resetFilters.setEnabled(true);

            /* save the frequency */
            oldHighPass = String.format("%5.0f", lowpassFreq);
            lowpassFreq = loFreq;
            lowPass.setText(String.format("%5.0f", loFreq));

            /* set the parameters in RoccaContour */
            roccaContour.setFilters(highpassFreq, lowpassFreq);

            /* recalc the contour */
            recalcContourActionPerformed(evt);

        } else {
            System.out.println("Error in RoccaSpecPopUp - invalid lowpass filter value");
            lowPass.setText(String.format("%5.0f", lowpassFreq));
        }
    }//GEN-LAST:event_lowPassActionPerformed

    private void selectStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectStartActionPerformed
        /* set boolean */
        settingStart = true;

        /* disable buttons */
        updateDisplayForStartEndSelect();

    }//GEN-LAST:event_selectStartActionPerformed

    private void selectEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectEndActionPerformed
        /* set boolean */
        settingEnd = true;

        /* disable buttons */
        updateDisplayForStartEndSelect();
    }//GEN-LAST:event_selectEndActionPerformed

    private void toggleContourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleContourActionPerformed
       toggleTheContour();
    }//GEN-LAST:event_toggleContourActionPerformed

    private void setLowpassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setLowpassActionPerformed
        /* set boolean */
        settingLowpass = true;

        /* disable buttons */
        updateDisplayForFilterSelect();

    }//GEN-LAST:event_setLowpassActionPerformed

    private void setHighpassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setHighpassActionPerformed
        /* set boolean */
        settingHighpass = true;

        /* disable buttons */
        updateDisplayForFilterSelect();

    }//GEN-LAST:event_setHighpassActionPerformed

    /**
     * Enables/disables the GUI buttons while the user is selecting a filter
     * frequency
     */
    private void updateDisplayForFilterSelect() {
        if (settingLowpass) {
            classifiedAsLbl.setEnabled(false);
            classify.setEnabled(false);
            highPass.setEnabled(false);
            highpassLbl.setEnabled(false);
            highpassUnitsLbl.setEnabled(false);
            setHighpass.setEnabled(false);
            lowPass.setEnabled(false);
            lowpassLbl.setEnabled(false);
            lowpassUnitsLbl.setEnabled(false);
            noiseSensLbl.setEnabled(false);
            noiseSensSpinner.setEnabled(false);
            recalcContour.setEnabled(false);
            reset.setEnabled(false);
            resetContour.setEnabled(false);
            resetFilters.setEnabled(false);
            saveExit.setEnabled(false);
            saveNewNameExit.setEnabled(false);
            saveWavOnly.setEnabled(false);
            selectStart.setEnabled(false);
            selectEnd.setEnabled(false);
            toggleContour.setEnabled(false);
            undoLastMove.setEnabled(false);
            disableClassify();

        } else if (settingHighpass) {
            classifiedAsLbl.setEnabled(false);
            classify.setEnabled(false);
            highPass.setEnabled(false);
            highpassLbl.setEnabled(false);
            highpassUnitsLbl.setEnabled(false);
            lowPass.setEnabled(false);
            lowpassLbl.setEnabled(false);
            lowpassUnitsLbl.setEnabled(false);
            setLowpass.setEnabled(false);
            noiseSensLbl.setEnabled(false);
            noiseSensSpinner.setEnabled(false);
            recalcContour.setEnabled(false);
            reset.setEnabled(false);
            resetContour.setEnabled(false);
            resetFilters.setEnabled(false);
            saveExit.setEnabled(false);
            saveNewNameExit.setEnabled(false);
            saveWavOnly.setEnabled(false);
            selectStart.setEnabled(false);
            selectEnd.setEnabled(false);
            toggleContour.setEnabled(false);
            undoLastMove.setEnabled(false);
            disableClassify();

        } else {
            classify.setEnabled(true);
            highPass.setEnabled(true);
            highpassLbl.setEnabled(true);
            highpassUnitsLbl.setEnabled(true);
            setHighpass.setEnabled(true);
            lowPass.setEnabled(true);
            lowpassLbl.setEnabled(true);
            lowpassUnitsLbl.setEnabled(true);
            setLowpass.setEnabled(true);
            noiseSensLbl.setEnabled(true);
            noiseSensSpinner.setEnabled(true);
            recalcContour.setEnabled(true);
            reset.setEnabled(true);
            resetContour.setEnabled(true);
            resetFilters.setEnabled(true);
            selectStart.setEnabled(true);
            selectEnd.setEnabled(true);
            toggleContour.setEnabled(true);
            recalcContour.setEnabled(true);
            resetContour.setEnabled(true);

            if (lastMovedPoint==null) {
                undoLastMove.setEnabled(false);
            } else {
                undoLastMove.setEnabled(true);
            }
        }
    }

    /**
     * Toggle the contour points on/off
     */
    private void toggleTheContour() {
        if (contourState == CONTOUROFF) {
            contourState = CONTOURON;
            toggleContour.setText("Turn Contour Off");
        } else {
            contourState = CONTOUROFF;
            toggleContour.setText("Turn Contour On");
        }

        /* repaint the spectrogram */
        dataPanel.repaint();
    }

    private void zoomDataPanel(float h, float v) {
        if(hZoom)
        {
            if(h > MAX_HZOOM)
                h = MAX_HZOOM;
            else if(h < MIN_HZOOM)
                h = MIN_HZOOM;

            currHZoom = h;
            dataPanel.hzoomSet(currHZoom);
        }
        if(vZoom)
        {
            if(v > MAX_VZOOM)
                v = MAX_VZOOM;
            else if(v < MIN_VZOOM)
                v = MIN_VZOOM;

            currVZoom = v;
            dataPanel.vzoomSet(currVZoom);
        }

        setInitialPosition();
    }

    /**
     * Launch this RoccaSpecPopUp in its own JFrame.
     */
    public void run()
     {
         thisFrame = new JFrame(windowTitle);
         thisFrame.setContentPane(this);
         thisFrame.pack();
         thisFrame.setVisible(true);
         thisFrame.setState(JFrame.ICONIFIED);
    }

    /**
     * Sets the initial position of the vertical scrollbar to center the user-
     * selected area in the display window
     */
    public void setInitialPosition() {
        JScrollBar verScroll = scroller.getVerticalScrollBar();
        
        /* calculate the middle frequency between the start and end frequncies */
        double midFreq = (boxBotFreq+boxTopFreq)/2;
        
        /* figure out which frequency bin is closest to the midFreq */
        int fBin;
        for (fBin=0; fBin<freqBins.length; fBin++) {
            if (freqBins[fBin]>midFreq) {
                break;
            }
        }

        /* get the scrollbar position fields */
        int vis = verScroll.getVisibleAmount();
        int maxVal = verScroll.getMaximum();
        int minVal = verScroll.getMinimum();

        /* calculate the scrollbar value desired.  Note that we need to subtract
         * fBin from maxVal because the frequency bins start counting from the
         * bottom of the image, but the scrollbar starts counting from the top
         */
        int valWanted = (int) (((freqBins.length - fBin) * (currVZoom))-vis/2);

         /* make sure we're not too high or too low */
        if (valWanted < minVal) {
            valWanted = minVal;
        } else if (valWanted > (freqBins.length*currVZoom)-vis) {
            valWanted = (int) (freqBins.length*currVZoom)-vis;
        }

        /* set the vertical scrollbar position */
        verScroll.setValue(valWanted);
    }

    public boolean showPeaks() {
        return showPeaks;
    }

    public Point[] getContour() {
        return contour;
    }

    public double[][] getData() {
        return data;
    }

    public Point getDragPoint() {
        return dragPoint;
    }
    
    public boolean tracingNow() {
    	return tracingNow;
    }
    
    /**
	 * @return the userPoints
	 */
	public ArrayList<Point> getUserPoints() {
		return userPoints;
	}

	public boolean getContourState() {
        return contourState;
    }

    public static int getNumDetections() {
        return numDetections;
    }

    public static void setNumDetections(int numDetections) {
        RoccaSpecPopUp.numDetections = numDetections;
    }

    public static void incNumDetections() {
        ++numDetections;
    }

    public double[] getFreqBins() {
        return freqBins;
    }

    public long[] getTimeBins() {
        return timeBins;
    }

    public SpectrogramPanel getDataPanel() {
        return dataPanel;
    }

    public RoccaAxis getFreqAxis() {
        return freqAxis;
    }

    public double getHighPassFreq() {
        return highpassFreq;
    }

    public double getLowPassFreq() {
        return lowpassFreq;
    }

//    pubic RoccaAxis getTimeAxis() {
//        return timeAxis;
//    }



    
    /**
     * Method for testing RoccaSpecPopUp
     * <p>
     * Requires a csv file containing the FFT data to be displayed, with no
     * header.  Size of file is hard-coded in the specData variable.  Note that
     * this is commented out, along with the Csvreader import, because it was
     * throwing an error when compiled.  To use this test, uncomment the reader
     * section and the csvreader import at the top.
     *
     */
    public static void main(String[] args)
    {
         double[][] specData = new double[113][513];
         String filename = "C:\\Users\\Michael\\Documents\\Work\\Java\\test_whistleFFT.csv";

         /*
          * UNCOMMENT THIS SECTION, ALONG WITH THE CSVREADER IMPORT, IN ORDER
          * TO RUN THE MAIN METHOD
          *
         try {
             CsvReader reader = new CsvReader(filename);
             int row = -1;
             while (reader.readRecord()) {
                 row++;
                 for(int col=0; col<reader.getColumnCount(); col++) {
                     specData[col][row]=Double.valueOf(reader.get(col));
                 }
             }
         } catch (Exception ex) {

         }

         RoccaSpecPopUp p = new RoccaSpecPopUp(specData, filename);
         p.run();
          */
     }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JButton brightDec;
    javax.swing.JButton brightInc;
    javax.swing.JLabel classifiedAsLbl;
    javax.swing.JPanel classifierPanel;
    javax.swing.JButton classify;
    javax.swing.JButton contrastDec;
    javax.swing.JButton contrastInc;
    javax.swing.JButton discardExit;
    javax.swing.JTextField highPass;
    javax.swing.JLabel highpassLbl;
    javax.swing.JLabel highpassUnitsLbl;
    javax.swing.JPanel jPanel1;
    javax.swing.JPanel jPanel2;
    javax.swing.JTextField lowPass;
    javax.swing.JLabel lowpassLbl;
    javax.swing.JLabel lowpassUnitsLbl;
    javax.swing.JLabel noiseSensLbl;
    javax.swing.JSpinner noiseSensSpinner;
    javax.swing.JButton recalcContour;
    javax.swing.JButton reset;
    javax.swing.JButton resetContour;
    javax.swing.JButton resetFilters;
    javax.swing.JButton saveExit;
    javax.swing.JButton saveNewNameExit;
    javax.swing.JButton saveWavOnly;
    private javax.swing.JScrollPane scroller;
    javax.swing.JToggleButton selectEnd;
    javax.swing.JToggleButton selectStart;
    javax.swing.JToggleButton setHighpass;
    javax.swing.JToggleButton setLowpass;
    javax.swing.JLabel statusBar;
    javax.swing.JButton toggleContour;
    javax.swing.JToggleButton btnPickPoints;
    javax.swing.JButton undoLastMove;
    javax.swing.JButton zoomIn;
    javax.swing.JButton zoomOut;
}
