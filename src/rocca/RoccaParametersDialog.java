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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import GPS.GPSControl;
import GPS.GpsDataUnit;
import PamController.PamController;
import PamUtils.PamFileChooser;
import PamView.dialog.PamDialog;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;
import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import clickDetector.NoiseDataBlock;
import fftManager.FFTDataUnit;
import warnings.PamWarning;
import warnings.WarningSystem;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SerializationHelper;
import whistlesAndMoans.AbstractWhistleDataUnit;
import whistlesAndMoans.WhistleMoanControl;

/**
 * code copied from WorkshopParametersDialog.java
 * @author Michael Oswald
 *
 */
public class RoccaParametersDialog extends PamDialog implements ActionListener, ItemListener {

	/**
	 *  Version 6 - add button to load second stage classifier
	 *  
	 *  Version 7 - 2nd stage classifier now incorporated into single classifier file
	 *  instead of requiring a separate file.  Button and associated code for second
	 *  classifier commented out (essentially reverting back to Version 5)
	 *  
	 *  Version 8 - added a hidden button to run RoccaClassifyThis on a RoccaContourStats
	 *  file in order to reclassify the rows using a new classifier.  Had to do it through
	 *  Rocca because the classifier requires a RoccaProcess, and RoccaProcess can't be null.
	 *  Also added a hidden button to run RoccaFixParams on a series of csv contour files in
	 *  order to generate a new contour stats file.
	 *  
	 *  Version 9 - added Click Detector as potential source for audio data.
	 *  
	 *  Version 10 - added notes tab 
	 *  
	 *  Version 11 - added geographic location and channels text boxes to notes tab
	 *  
	 *  Version 12 - added Click Classifier
	 *  
	 *  Version 13 - show/hide possible sources based on radio buttons, and resize frame to match
	 */
	private static final long serialVersionUID = 13;

	
	static private RoccaParametersDialog singleInstance;
    private RoccaControl roccaControl;
	RoccaParameters roccaParameters;
	JPanel sourceSubPanel;
	SourcePanel fftSourcePanel;        // list avail FFT data sources
	JPanel fftSourceSubPanel;			// panel holding the SourcePanel fftSourcePanel
	SourcePanel clickSourcePanel;
	SourcePanel clickNoiseSourcePanel;
	JPanel clickSourceSubPanel;
	JLabel clickTypeNote;
	JButton clickTypeDialog;
	SourcePanel wmSourcePanel;		// list avail whistle & moan sources
	JPanel wmSourceSubPanel;			// panel holding the SourcePanel wmSourcePanel
	JPanel classifierPanel;			// serialVersionUID=24 2016/08/10 added
	SourcePanel gpsSourcePanel;		// list avail whistle & moan sources
	JPanel gpsSourceSubPanel;			// panel holding the SourcePanel wmSourcePanel
    JTabbedPane tabbedPane;
    JTextField noiseSensitivity, energyBinSize;
    JCheckBox ancCalcs4Clicks;	 	// serialVersionUID=22 2015/06/13 added
    JCheckBox ancCalcs4Whistles;	 	// serialVersionUID=22 2015/06/13 added
    JCheckBox trimWav;
    JLabel outputDirLbl;
    JTextField outputDirTxt;
    JButton outputDirectoryButton;
    JLabel outputContourStatsLbl;
    JLabel outputSightingStatsLbl;
    JTextField outputContourStatsTxt;
    JTextField outputSightingStatsTxt;
    JButton outputContourStatsFileButton;
    File outputDirectory;
    File outputContourStatsFile;
    File outputSightingStatsFile;
    JLabel classifierLbl;
    JTextField classifierTxt;
    JButton classifierButton;
    JButton clearClassifierButton;
    JTextArea classifierDesc;
    JScrollPane scrollPaneDesc;
    JTextField clickClassifierTxt;
    JButton clickClassifierButton;
    JButton clickClearClassifierButton;
    JTextArea clickClassifierDesc;
    JScrollPane clickScrollPanelDesc;    
    JTextField eventClassifierTxt;
    JButton eventClassifierButton;
    JButton eventClearClassifierButton;
    JTextArea eventClassifierDesc;
    JScrollPane eventScrollPanelDesc;    
    JTextField classifier2Txt;
    JButton classifier2Button;
    JButton recalcButton;
    JButton reclassifyButton;
    JButton trainThenTestButton;
    JButton clearClassifier;
    JComboBox<DefaultComboBoxModel<Vector<String>>> stage1Classes;
    DefaultComboBoxModel<Vector<String>> stage1ClassModel;
    Vector<String> stage1ClassList;
    File classifierFile;
    File clickClassifierFile;
    File eventClassifierFile;
    JLabel classDetLbl;
    JTextField classificationThreshold;
    JTextField sightingThreshold;
    JTextField filenameTemplate;
    JLabel filenameTemplateLbl;
    JLabel templateSymbols;
    JRadioButton fftButton;
    JRadioButton clickButton;
    JRadioButton wmButton;
    JRadioButton gpsButton;
    String fftButtonText = "Use FFT Source";
    String clickButtonText = "Use Click Detector Source";
    String wmButtonText = "Use Whistle & Moan Source";
    String gpsButtonText = "Use GPS Data";
    Frame parentFrame;
    String[] clickTypes; 
    int[] selectedClickTypes;
    //JTextField notesEncounter;	SerialVersionUID=23 2016/01/04 changed to JComboBox
    JComboBox<String> notesEncounter;
    JLabel notesEncounterLbl;
    //JTextField notesCruise;	SerialVersionUID=23 2016/01/04 changed to JComboBox
    JComboBox<String> notesCruise;
    JLabel notesCruiseLbl;
    //JTextField notesProvider;	SerialVersionUID=23 2016/01/04 changed to JComboBox
    JComboBox<String> notesProvider;
    JLabel notesProviderLbl;
    //JTextField notesSpecies;	SerialVersionUID=23 2016/01/04 changed to JComboBox
    JComboBox<String> notesSpecies;
    JLabel notesSpeciesLbl;
    //JTextField notesGeoLoc;	SerialVersionUID=23 2016/01/04 changed to JComboBox
    JComboBox<String> notesGeoLoc;
    JLabel notesGeoLocLbl;
    //JTextField notesChan;	SerialVersionUID=23 2016/01/04 changed to JComboBox
    JComboBox<String> notesChan;
    JLabel notesChanLbl;
    JButton loadNotes;		//	serialVersionUID=23 2016/01/04 added
    JLabel standardNotesFilename;	//	serialVersionUID=23 2016/01/04 added
    String noClassifier = "<no classifier selected>";
	private static PamWarning roccaWarning = new PamWarning("Rocca", "", 2);



	
	private RoccaParametersDialog(Frame parentFrame) {
		super(parentFrame, "Rocca Parameters", true);
		this.parentFrame=parentFrame;

		/*
		 * Use the Java layout manager to constructs nesting panels 
		 * of all the parameters. 
		 */
        tabbedPane = new JTabbedPane();
		JPanel mainPanel = new JPanel(new BorderLayout(0,5));
		mainPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
	 	/* serialVersionUID=24 2016/08/10 changed from BorderLayout to BoxLayout */
        //JPanel classifierPanel = new JPanel(new BorderLayout());
        classifierPanel = new JPanel();
        classifierPanel.setLayout(new BoxLayout(classifierPanel,BoxLayout.Y_AXIS));
        JPanel outputPanel = new JPanel(new BorderLayout());
        JPanel filenamePanel = new JPanel(new BorderLayout());
        JPanel notesPanel = new JPanel(new BorderLayout());
        tabbedPane.add("Source Data", mainPanel);
        tabbedPane.add("Contours/Classifier", classifierPanel);
        tabbedPane.add("Output", outputPanel);
        tabbedPane.add("Filename Template", filenamePanel);
        tabbedPane.add("Notes", notesPanel);
		
		/* 
		 * Radio buttons to allow user to select whether the source data comes from an FFT
		 * source, a Click Detector source or a whistle&moan detector source
		 */
		JPanel sourceSelection = new JPanel(new GridLayout(0, 2, 0, 0));
		sourceSelection.setBorder(new TitledBorder("Select Data source"));
		
		fftButton = new JRadioButton(fftButtonText, true);
        fftButton.setActionCommand(fftButtonText);
		clickButton = new JRadioButton(clickButtonText, false);
        clickButton.setActionCommand(clickButtonText);
        wmButton = new JRadioButton(wmButtonText, false);
        wmButton.setActionCommand(wmButtonText);
        gpsButton = new JRadioButton(gpsButtonText, false);
        gpsButton.setActionCommand(gpsButtonText);
        
        //Group the radio buttons
        // 2014-10-13 v14 get rid of the buttongroup, so that the user can select
        // multiple sources
        //ButtonGroup sourceGroup = new ButtonGroup();
        //sourceGroup.add(fftButton);
        //sourceGroup.add(clickButton);
        //sourceGroup.add(wmButton);
        
        //Register a listener for the radio buttons.         
        fftButton.addItemListener(this);
        clickButton.addItemListener(this);
        wmButton.addItemListener(this);
        gpsButton.addItemListener(this);
        
		sourceSelection.add(fftButton);
		sourceSelection.add(clickButton);
		sourceSelection.add(wmButton);
		sourceSelection.add(gpsButton);
		mainPanel.add(BorderLayout.PAGE_START, sourceSelection);
        
		/*
		 * create a source sub panel containing the FFT and whistle&moan detector selections
		 * serialVersionUID = 15 2014/11/12 change Layout Manager from GridLayout to BoxLayout
		 */
		//sourceSubPanel = new JPanel(new GridLayout(0,1));
		sourceSubPanel = new JPanel();
		sourceSubPanel.setLayout(new BoxLayout(sourceSubPanel,BoxLayout.PAGE_AXIS));
//		sourceSubPanel.setBorder(new EmptyBorder(5,10,5,10));
		
		/*
		 * FFT source subpanel
		 */
		fftSourcePanel = new SourcePanel(this, FFTDataUnit.class, true, false); // do not include subclasses - at the moment Rocca analyzes both fft and raw data, and Beamformer output (subclass of FFT) only has fft data
		fftSourceSubPanel = new JPanel();
		fftSourceSubPanel.setLayout(new BorderLayout());
		fftSourceSubPanel.setBorder(new TitledBorder("FFT Data source"));
		fftSourceSubPanel.add(BorderLayout.CENTER, fftSourcePanel.getPanel());
		sourceSubPanel.add(fftSourceSubPanel);
		sourceSubPanel.add(Box.createRigidArea(new Dimension(0,5)));
		
		
		/*
		 * Click source subpanel
		 */
		clickSourcePanel = new SourcePanel(this, ClickDetection.class, false, true); // 2017/11/29 remove channels, since we don't use them anyway
		ArrayList<PamDataBlock> sl = PamController.getInstance().getDataBlocks(ClickDetection.class, true);
		for (PamDataBlock aBlock : sl) {
			if (NoiseDataBlock.class.isAssignableFrom(aBlock.getClass())) {
				clickSourcePanel.excludeDataBlock(aBlock, true);
			}
		}

		// jump through some hoops here so that only the NoiseDataBlock is shown in the next source panel
		clickNoiseSourcePanel = new SourcePanel(this, ClickDetection.class, false, true); // 2017/11/29 remove channels, since we don't use them anyway
		for (PamDataBlock aBlock : sl) {
			if (!NoiseDataBlock.class.isAssignableFrom(aBlock.getClass())) {
				clickNoiseSourcePanel.excludeDataBlock(aBlock, true);
			}
		}
		
		clickSourceSubPanel = new JPanel();
		clickSourceSubPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 0);
		c.gridx=0;
		c.gridy=0;
		c.anchor=GridBagConstraints.LINE_START;
		c.fill=GridBagConstraints.NONE;
		clickSourceSubPanel.setBorder(new TitledBorder("Click Detector Data source"));
		clickSourceSubPanel.add(new JLabel("Click Detector Source", SwingConstants.LEFT),c);
		c.gridx++;
		c.fill=GridBagConstraints.HORIZONTAL;
		clickSourceSubPanel.add(clickSourcePanel.getPanel(),c);
		c.gridy++;
		c.gridx=0;
		c.fill=GridBagConstraints.NONE;
		clickSourceSubPanel.add(new JLabel("Click Detector Noise Source"),c);
		c.gridx++;
		c.fill=GridBagConstraints.HORIZONTAL;
		clickSourceSubPanel.add(clickNoiseSourcePanel.getPanel(),c);
		c.gridy++;
		c.gridx=0;
		c.gridwidth=2;
		clickTypeNote = new JLabel("<html><div WIDTH=300><strong>Warning</strong> - one or more Click Types must be defined " +
		" in the Click Classifier before you can use Rocca with the Click Detector</div></html>", SwingConstants.CENTER);
		clickSourceSubPanel.add(clickTypeNote,c);
		c.gridy++;
		clickTypeDialog = new JButton("Select Click Types to analyze");
		clickTypeDialog.addActionListener(this);
        clickTypeDialog.setVisible(true);
		clickSourceSubPanel.add(clickTypeDialog,c);
		sourceSubPanel.add(clickSourceSubPanel);
		sourceSubPanel.add(Box.createRigidArea(new Dimension(0,5)));
		
		
		/*
		 * Whistle & Moan Detector source subpanel
		 */
		wmSourcePanel = new SourcePanel(this, AbstractWhistleDataUnit.class, false, true);
		wmSourceSubPanel = new JPanel();
		wmSourceSubPanel.setLayout(new BorderLayout());
		wmSourceSubPanel.setBorder(new TitledBorder("Whistle & Moan Data source"));
		wmSourceSubPanel.add(BorderLayout.CENTER, wmSourcePanel.getPanel());
		sourceSubPanel.add(wmSourceSubPanel);
		sourceSubPanel.add(Box.createRigidArea(new Dimension(0,5)));
		
		/*
		 * GPS Source subpanel
		 * 
		 */
		gpsSourcePanel = new SourcePanel(this, GpsDataUnit.class, false, true);
		gpsSourceSubPanel = new JPanel();
		gpsSourceSubPanel.setLayout(new BorderLayout());
		gpsSourceSubPanel.setBorder(new TitledBorder("GPS source"));
		gpsSourceSubPanel.add(BorderLayout.CENTER, gpsSourcePanel.getPanel());
		sourceSubPanel.add(gpsSourceSubPanel);
		mainPanel.add(BorderLayout.CENTER, sourceSubPanel);
		
		
        /* create the second tab, for the classifier parameters */
        /* top subpanel - classifier */
		JPanel classifierSubPanel = new JPanel();
		classifierSubPanel.setBorder(new TitledBorder("Whistle Classifier"));
        classifierTxt = new JTextField(30);
        classifierTxt.setEditable(false);
		classifierButton = new JButton("Select Whistle Classifier");
        classifierButton.addActionListener(this);
        clearClassifierButton = new JButton("Clear Whistle Classifier");
        clearClassifierButton.addActionListener(this);
        classifierDesc = new JTextArea("Classifier description not available",3,30);
        classifierDesc.setLineWrap(true);
        classifierDesc.setWrapStyleWord(true);
        classifierDesc.setEditable(false);
        scrollPaneDesc = new JScrollPane (classifierDesc);
        scrollPaneDesc.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        GroupLayout classPanelLayout = new GroupLayout(classifierSubPanel);
        classifierSubPanel.setLayout(classPanelLayout);
        classPanelLayout.setAutoCreateGaps(true);
        classPanelLayout.setAutoCreateContainerGaps(true);
        classPanelLayout.setHorizontalGroup(
        	classPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
              .addGroup(classPanelLayout.createSequentialGroup()
                  .addComponent(classifierTxt))
              .addGroup(classPanelLayout.createSequentialGroup()
//            	  .addComponent(scrollPaneDesc))
            	.addComponent(classifierButton)
            	.addComponent(clearClassifierButton))
        );
        classPanelLayout.setVerticalGroup(
        	classPanelLayout.createSequentialGroup()
              .addGroup(classPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                  .addComponent(classifierTxt))
              .addGroup(classPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                    .addComponent(scrollPaneDesc))
            		  .addComponent(classifierButton)
            		  .addComponent(clearClassifierButton))
        );
		classifierPanel.add(classifierSubPanel);


		/* Click Classifier selection */
	 	// serialVersionUID=24 2016/08/10 added
		JPanel clickClassifierSubPanel = new JPanel();
		clickClassifierSubPanel.setBorder(new TitledBorder("Click Classifier"));
        clickClassifierTxt = new JTextField(30);
        clickClassifierTxt.setEditable(false);
		clickClassifierButton = new JButton("Select Click Classifier");
		clickClassifierButton.addActionListener(this);
        clickClassifierDesc = new JTextArea("Classifier description not available",3,30);
        clickClassifierDesc.setLineWrap(true);
        clickClassifierDesc.setWrapStyleWord(true);
        clickClassifierDesc.setEditable(false);
        clickScrollPanelDesc = new JScrollPane (clickClassifierDesc);
        clickClearClassifierButton = new JButton("Clear Click Classifier");
        clickClearClassifierButton.addActionListener(this);
       clickScrollPanelDesc.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        GroupLayout clickPanelLayout = new GroupLayout(clickClassifierSubPanel);
        clickClassifierSubPanel.setLayout(clickPanelLayout);
        clickPanelLayout.setAutoCreateGaps(true);
        clickPanelLayout.setAutoCreateContainerGaps(true);
        clickPanelLayout.setHorizontalGroup(
        		clickPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
              .addGroup(clickPanelLayout.createSequentialGroup()
                  .addComponent(clickClassifierTxt))
              .addGroup(clickPanelLayout.createSequentialGroup()
//            	  .addComponent(clickScrollPanelDesc))
                      .addComponent(clickClassifierButton)
                      .addComponent(clickClearClassifierButton))
        );
        clickPanelLayout.setVerticalGroup(
        		clickPanelLayout.createSequentialGroup()
              .addGroup(clickPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                  .addComponent(clickClassifierTxt))
              .addGroup(clickPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                  .addComponent(clickScrollPanelDesc))
            		  .addComponent(clickClassifierButton)
            		  .addComponent(clickClearClassifierButton))
        );
		classifierPanel.add(clickClassifierSubPanel);

		/* Event Classifier selection */
	 	// serialVersionUID=24 2016/08/10 added
		JPanel eventClassifierSubPanel = new JPanel();
		eventClassifierSubPanel.setBorder(new TitledBorder("Encounter Classifier"));
        eventClassifierTxt = new JTextField(30);
        eventClassifierTxt.setEditable(false);
		eventClassifierButton = new JButton("Select Encounter Classifier");
		eventClassifierButton.addActionListener(this);
        eventClassifierDesc = new JTextArea("Classifier description not available",3,30);
        eventClassifierDesc.setLineWrap(true);
        eventClassifierDesc.setWrapStyleWord(true);
        eventClassifierDesc.setEditable(false);
        eventScrollPanelDesc = new JScrollPane (eventClassifierDesc);
        eventClearClassifierButton = new JButton("Clear Encounter Classifier");
        eventClearClassifierButton.addActionListener(this);
       eventScrollPanelDesc.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        GroupLayout eventPanelLayout = new GroupLayout(eventClassifierSubPanel);
        eventClassifierSubPanel.setLayout(eventPanelLayout);
        eventPanelLayout.setAutoCreateGaps(true);
        eventPanelLayout.setAutoCreateContainerGaps(true);
        eventPanelLayout.setHorizontalGroup(
        		eventPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
              .addGroup(eventPanelLayout.createSequentialGroup()
                  .addComponent(eventClassifierTxt))
              .addGroup(eventPanelLayout.createSequentialGroup()
                      .addComponent(eventClassifierButton)
                      .addComponent(eventClearClassifierButton))
        );
        eventPanelLayout.setVerticalGroup(
        		eventPanelLayout.createSequentialGroup()
              .addGroup(eventPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                  .addComponent(eventClassifierTxt))
              .addGroup(eventPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
            		  .addComponent(eventClassifierButton)
            		  .addComponent(eventClearClassifierButton))
        );
		classifierPanel.add(eventClassifierSubPanel);

		/* Extra buttons for recalculating/reclassifying */
	 	// serialVersionUID=24 2016/08/10 moved from the classifier subpanel into their own
        JPanel extraButtonsSubPanel = new JPanel();
        extraButtonsSubPanel.setBorder(new TitledBorder("Recalc/Reclassify")); 
		extraButtonsSubPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)); // serialVersionUID=24 2016/08/10 added 
        recalcButton = new JButton("Recalc/Reclassify Contours");
        recalcButton.addActionListener(this);
        recalcButton.setToolTipText("Recalculate all time-freq contour csv files in a folder, classify, and save info to contour stats output file");
        recalcButton.setVisible(true);
        reclassifyButton = new JButton("Reclassify Data");
        reclassifyButton.addActionListener(this);
        reclassifyButton.setToolTipText("Load the whistle data from the contour stats output file, and run it through the current Classifier");
        reclassifyButton.setVisible(true);
        trainThenTestButton = new JButton("Train then Test");
        trainThenTestButton.addActionListener(this);
        trainThenTestButton.setToolTipText("Train a classifier on a set of training data, then test it with a set of testing data");
        trainThenTestButton.setVisible(true);
        
        // ******** THIS LINES CONTROLS THE VISIBILITY ********
        if (RoccaDev.isEnabled()) {
        	extraButtonsSubPanel.setVisible(true);
        } else {
        	extraButtonsSubPanel.setVisible(false);
        }
        GroupLayout extraPanelLayout = new GroupLayout(extraButtonsSubPanel);
        extraButtonsSubPanel.setLayout(extraPanelLayout);
        extraPanelLayout.setAutoCreateGaps(true);
        extraPanelLayout.setAutoCreateContainerGaps(true);
        extraPanelLayout.setHorizontalGroup(
        		extraPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
        		.addGroup(extraPanelLayout.createSequentialGroup()
        				.addComponent(recalcButton)
        				.addComponent(reclassifyButton)
        				.addComponent(trainThenTestButton))
        		);
        extraPanelLayout.setVerticalGroup(
        		extraPanelLayout.createSequentialGroup()
        		.addGroup(extraPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        				.addComponent(recalcButton)
        				.addComponent(reclassifyButton)
        				.addComponent(trainThenTestButton))
        		);
		classifierPanel.add(extraButtonsSubPanel);
		        
        /* Classification Threshold subpanel */
		JPanel thresholdSubPanel = new JPanel();
		thresholdSubPanel.setBorder(new TitledBorder("Classification Thresholds"));
//		thresholdSubPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, thresholdSubPanel.getMinimumSize().height)); // serialVersionUID=24 2016/08/10 added 
		thresholdSubPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)); // serialVersionUID=24 2016/08/10 added 
        JLabel whistleLbl = new JLabel("Whistle/Click Threshold");
        classificationThreshold = new JTextField(3);
        classificationThreshold.setMaximumSize(new Dimension(40, classificationThreshold.getHeight()));
        JLabel whistleUnits = new JLabel("%");
        JLabel schoolLbl = new JLabel("Encounter Threshold");
        sightingThreshold = new JTextField(3);
        sightingThreshold.setMaximumSize(new Dimension(40, sightingThreshold.getHeight()));
        JLabel schoolUnits = new JLabel("%");
        GroupLayout thresholdLayout = new GroupLayout(thresholdSubPanel);
        thresholdSubPanel.setLayout(thresholdLayout);
        thresholdLayout.setAutoCreateGaps(true);
        thresholdLayout.setAutoCreateContainerGaps(true);
        thresholdLayout.setHorizontalGroup(
            thresholdLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(thresholdLayout.createSequentialGroup()
                    .addComponent(whistleLbl)
                    .addComponent(classificationThreshold)
                    .addComponent(whistleUnits))
                .addGroup(thresholdLayout.createSequentialGroup()
                    .addComponent(schoolLbl)
                    .addComponent(sightingThreshold)
                    .addComponent(schoolUnits))
        );
        thresholdLayout.setVerticalGroup(
            thresholdLayout.createSequentialGroup()
                .addGroup(thresholdLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(whistleLbl)
                    .addComponent(classificationThreshold)
                    .addComponent(whistleUnits))
                .addGroup(thresholdLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(schoolLbl)
                    .addComponent(sightingThreshold)
                    .addComponent(schoolUnits))
        );
        thresholdLayout.linkSize(SwingConstants.HORIZONTAL, whistleLbl, schoolLbl);
		classifierPanel.add(thresholdSubPanel);

		// make another panel for the parameters.
		JPanel detPanel = new JPanel();
		detPanel.setBorder(new TitledBorder("Extraction parameters"));
		detPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)); // serialVersionUID=24 2016/08/10 added 
        JLabel noiseSensLbl = new JLabel("Noise Sensitivity");
        JLabel energyBinLbl = new JLabel("Energy Bin Calc Size");
        JLabel ancillaryCalcsCLbl = new JLabel("Calculate Click Encounter Stats ");
        JLabel ancillaryCalcsWLbl = new JLabel("Calculate Whistle Encounter Stats ");
        noiseSensitivity = new JTextField(4);
        noiseSensitivity.setMaximumSize(new Dimension(40, noiseSensitivity.getHeight()));
        energyBinSize = new JTextField(4);
        energyBinSize.setMaximumSize(new Dimension(40, energyBinSize.getHeight()));
        ancCalcs4Clicks = new JCheckBox();  	// serialVersionUID=22 2015/06/13 added
        ancCalcs4Whistles = new JCheckBox();  	// serialVersionUID=22 2015/06/13 added
        JLabel noiseSensUnits = new JLabel("%");
        JLabel energyBinUnits = new JLabel(" Hz");
        GroupLayout detPanelLayout = new GroupLayout(detPanel);
        detPanel.setLayout(detPanelLayout);
        detPanelLayout.setAutoCreateGaps(true);
        detPanelLayout.setAutoCreateContainerGaps(true);
        detPanelLayout.setHorizontalGroup(
            detPanelLayout.createSequentialGroup()
                .addGroup(detPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(noiseSensLbl)
                    .addComponent(energyBinLbl)
                    .addComponent(ancillaryCalcsWLbl)
                    .addComponent(ancillaryCalcsCLbl))
                .addGroup(detPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(noiseSensitivity)
                    .addComponent(energyBinSize)
                    .addComponent(ancCalcs4Whistles)
                    .addComponent(ancCalcs4Clicks))
                .addGroup(detPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(noiseSensUnits)
                    .addComponent(energyBinUnits))
        );
        detPanelLayout.setVerticalGroup(
            detPanelLayout.createSequentialGroup()
                .addGroup(detPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(noiseSensLbl)
                    .addComponent(noiseSensitivity)
                    .addComponent(noiseSensUnits))
                .addGroup(detPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(energyBinLbl)
                    .addComponent(energyBinSize)
                    .addComponent(energyBinUnits))
                .addGroup(detPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(ancillaryCalcsWLbl)
                        .addComponent(ancCalcs4Whistles))
                .addGroup(detPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(ancillaryCalcsCLbl)
                        .addComponent(ancCalcs4Clicks))
        );
		classifierPanel.add(detPanel);


        /* classifier details subpanel - not currently implemented */
//		JPanel classDetSubPanel = new JPanel();
//		classDetSubPanel.setBorder(new TitledBorder("Classifier Details"));
//		classDetSubPanel.setLayout(new GridBagLayout());
//		GridBagConstraints classDetConstraints = new GridBagConstraints();
//		classDetConstraints.anchor = GridBagConstraints.LINE_START;
//		classDetConstraints.insets = new Insets(0,3,0,0);
//        classDetConstraints.fill = GridBagConstraints.NONE;
//        classDetConstraints.gridwidth = 2;
//		classDetConstraints.gridx = 0;
//		classDetConstraints.gridy = 0;
//        addComponent(classDetSubPanel, classDetLbl = new JLabel("Model details here..."), classDetConstraints);
//		classifierPanel.add(BorderLayout.PAGE_END, classDetSubPanel);

        /* create the third tab, for the output files */
		JPanel outputSubPanel = new JPanel();
		outputSubPanel.setBorder(new TitledBorder("Output File Details"));
        outputDirLbl = new JLabel("Output Directory");
        outputDirTxt = new JTextField(30);
        outputDirTxt.setEditable(false);
		outputDirectoryButton = new JButton("Select Directory");
        outputDirectoryButton.addActionListener(this);
        outputContourStatsLbl = new JLabel("Contour Stats Save File");
        outputContourStatsTxt = new JTextField(30);
        outputContourStatsTxt.setEditable(true);
        outputSightingStatsLbl = new JLabel("Encounter Stats Save File");
        outputSightingStatsTxt = new JTextField(30);
        outputSightingStatsTxt.setEditable(true);
        
        GroupLayout outputLayout = new GroupLayout(outputSubPanel);
        outputSubPanel.setLayout(outputLayout);
        outputLayout.setAutoCreateGaps(true);
        outputLayout.setAutoCreateContainerGaps(true);
        outputLayout.setHorizontalGroup(
            outputLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(outputDirLbl)
                .addComponent(outputDirTxt)
                .addComponent(outputDirectoryButton)
                .addComponent(outputContourStatsLbl)
                .addComponent(outputContourStatsTxt)
                .addComponent(outputSightingStatsLbl)
                .addComponent(outputSightingStatsTxt)
        );
        outputLayout.setVerticalGroup(
            outputLayout.createSequentialGroup()
                .addComponent(outputDirLbl)
                .addComponent(outputDirTxt)
                .addComponent(outputDirectoryButton)
                .addGap(20)
                .addComponent(outputContourStatsLbl)
                .addComponent(outputContourStatsTxt)
                .addGap(20)
                .addComponent(outputSightingStatsLbl)
                .addComponent(outputSightingStatsTxt)
       );
		outputPanel.add(BorderLayout.PAGE_START, outputSubPanel);
		
		JPanel trimPanel = new JPanel();
		trimPanel.setBorder(new TitledBorder("WAV Clip"));
		trimPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)); // serialVersionUID=24 2016/08/10 added 
        JLabel trimWavLbl = new JLabel("Trim wav clip to only include contour");
        trimWavLbl.setToolTipText("When checked, the wav clip will only contain the contour.  If unchecked, the clip will contain the entire area boxed by the user");
        trimWav = new JCheckBox();
        trimWav.setToolTipText("When checked, the wav clip will only contain the contour.  If unchecked, the clip will contain the entire area boxed by the user");
        GroupLayout trimPanelLayout = new GroupLayout(trimPanel);
        trimPanel.setLayout(trimPanelLayout);
        trimPanelLayout.setAutoCreateGaps(true);
        trimPanelLayout.setAutoCreateContainerGaps(true);
        trimPanelLayout.setHorizontalGroup(
    		trimPanelLayout.createSequentialGroup()
                    .addComponent(trimWavLbl)
                    .addComponent(trimWav)
        );
        trimPanelLayout.setVerticalGroup(
        	trimPanelLayout.createParallelGroup()
                .addComponent(trimWavLbl)
                .addComponent(trimWav)
        );
		outputPanel.add(BorderLayout.CENTER, trimPanel);

        // create the fourth tab, for the filename template
		JPanel filenameSubPanel = new JPanel();
		filenameSubPanel.setBorder(new EtchedBorder());
		filenameSubPanel.setLayout(new GridBagLayout());
		GridBagConstraints filenameConstraints = new GridBagConstraints();
		filenameConstraints.anchor = GridBagConstraints.PAGE_START;
		filenameConstraints.insets = new Insets(5,3,0,0);
        filenameConstraints.fill = GridBagConstraints.NONE;
        filenameConstraints.gridwidth = 2;
		filenameConstraints.gridx = 0;
		filenameConstraints.gridy = 0;
        addComponent(filenameSubPanel, filenameTemplateLbl = new JLabel("<html>" +
                "Rocca uses a template to create the file names for whistle<br>" +
                "clips and contour points.  You can enter the template<br>" +
                "in the textfield below, using any of the symbols shown<br>" +
                "at the bottom of the window.  When Rocca creates the file,<br>" +
                "it will substitute the actual values for the symbols.<br><br>" +
                "Filename Template<html>"), filenameConstraints);
        filenameConstraints.insets = new Insets(0,3,0,0);
		filenameConstraints.gridy++;
		addComponent(filenameSubPanel, filenameTemplate = new JTextField(35), filenameConstraints);
        filenameTemplate.setEditable(true);
        filenameConstraints.insets = new Insets(15,3,5,3);
        filenameConstraints.gridwidth = 1;
		filenameConstraints.gridx = 0;
		filenameConstraints.gridy++;
        addComponent(filenameSubPanel, templateSymbols = new JLabel("<html>" +
                "%f = name of source<br>" +
                "%n = encounter number<br>" +
                "%X = encounter tally<br>" +
                "%t = channel/track num<br>" +
                "%Y = year, 4 digits<br>" +
                "%y = year, 2 digits<br>" +
                "%M = month<br>" +
                "%D = day of month<br>" +
                "%J = day of year (3 digits)<html>"), filenameConstraints);
		filenameConstraints.gridx++;
        addComponent(filenameSubPanel, templateSymbols = new JLabel("<html>" +
                "%H = hour, 24-hour clock<br>" +
                "%h = hour, 12-hour clock<br>" +
                "%a = 'am' or 'pm'<br>" +
                "%m = minute<br>" +
                "%s = second<br>" +
                "%S = second of the day (5 digits)<br>" +
                "%d = tenths of a second<br>" +
                "%c = hundredths of a second<br>" +
                "%i = thousandths of a second<html>"), filenameConstraints);
		filenamePanel.add(BorderLayout.CENTER, filenameSubPanel);

        // create the fifth tab, for the filename template
		JPanel notesSubPanel = new JPanel();
		notesSubPanel.setBorder(new TitledBorder("Notes"));
		JLabel notesHeader = new JLabel("<html>" +
				"Use these text boxes to add notes to the contour stats output file.<br>" +
				"Do not include commas in the text.<html>");
        notesEncounterLbl = new JLabel("Encounter ID");
        //notesEncounter = new JTextField(30);	SerialVersionUID=23 2016/01/04 changed to JComboBox
        notesEncounter = new JComboBox<String>();
        notesEncounter.setEditable(true);
        notesEncounter.addActionListener(this);
        notesCruiseLbl = new JLabel("Cruise ID");
//        notesCruise = new JTextField(30);	SerialVersionUID=23 2016/01/04 changed to JComboBox
        notesCruise = new JComboBox<String>();
        notesCruise.setEditable(true);
        notesCruise.addActionListener(this);
        notesProviderLbl = new JLabel("Data Provider");
//        notesProvider = new JTextField(30);	SerialVersionUID=23 2016/01/04 changed to JComboBox
        notesProvider = new JComboBox<String>();
        notesProvider.setEditable(true);
        notesProvider.addActionListener(this);
        notesSpeciesLbl = new JLabel("Known Species");
//        notesSpecies = new JTextField(30);	SerialVersionUID=23 2016/01/04 changed to JComboBox
        notesSpecies = new JComboBox<String>();
        notesSpecies.setEditable(true);
        notesSpecies.addActionListener(this);
        notesGeoLocLbl = new JLabel("Geographic Location");
//        notesGeoLoc = new JTextField(30);	SerialVersionUID=23 2016/01/04 changed to JComboBox
        notesGeoLoc = new JComboBox<String>();
        notesGeoLoc.setEditable(true);
        notesGeoLoc.addActionListener(this);
        notesChanLbl = new JLabel("Number of Channels");
//        notesChan = new JTextField(30);	SerialVersionUID=23 2016/01/04 changed to JComboBox
        notesChan = new JComboBox<String>();
        notesChan.setEditable(true);
        notesChan.addActionListener(this);
		loadNotes = new JButton("Load Standard Notes");
		loadNotes.addActionListener(this);
		standardNotesFilename = new JLabel("(No File Loaded)");
        GroupLayout notesLayout = new GroupLayout(notesSubPanel);
        notesSubPanel.setLayout(notesLayout);
        notesLayout.setAutoCreateGaps(true);
        notesLayout.setAutoCreateContainerGaps(true);
        notesLayout.setHorizontalGroup(
            notesLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            	.addComponent(notesHeader)
                .addComponent(notesEncounterLbl)
                .addComponent(notesEncounter)
                .addComponent(notesCruiseLbl)
                .addComponent(notesCruise)
                .addComponent(notesProviderLbl)
                .addComponent(notesProvider)
                .addComponent(notesSpeciesLbl)
                .addComponent(notesSpecies)
                .addComponent(notesGeoLocLbl)
                .addComponent(notesGeoLoc)
                .addComponent(notesChanLbl)
                .addComponent(notesChan)
                .addComponent(loadNotes)
                .addComponent(standardNotesFilename) 
        );
        notesLayout.setVerticalGroup(
            notesLayout.createSequentialGroup()
            	.addComponent(notesHeader)
            	.addGap(20)
                .addComponent(notesEncounterLbl)
                .addComponent(notesEncounter)
                .addGap(20)
                .addComponent(notesCruiseLbl)
                .addComponent(notesCruise)
                .addGap(20)
                .addComponent(notesProviderLbl)
                .addComponent(notesProvider)
                .addGap(20)
                .addComponent(notesSpeciesLbl)
                .addComponent(notesSpecies)
                .addGap(20)
                .addComponent(notesGeoLocLbl)
                .addComponent(notesGeoLoc)
                .addGap(20)
                .addComponent(notesChanLbl)
                .addComponent(notesChan)
                .addGap(20)
        		.addComponent(loadNotes)
        		.addComponent(standardNotesFilename)
        );
		notesPanel.add(BorderLayout.PAGE_START, notesSubPanel);

		
		
        // set the dialog component focus
		setDialogComponent(tabbedPane);
	}

    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == outputDirectoryButton) {
            selectDirectory();
        } else if (e.getSource() == outputContourStatsFileButton) {
            selectContourStatsFile();
        } else if (e.getSource() == classifierButton ) {
        	//selectClassifier(); serialVersionUID=24 2016/08/10 replaced with overloaded method
            classifierFile=selectClassifier(classifierTxt, classifierFile, classifierDesc);
            roccaParameters.setClassifyWhistles(true);
        } else if (e.getSource() == clearClassifierButton) {
        	classifierTxt.setText(noClassifier);
        	classifierFile=null;
        	roccaParameters.setClassifyWhistles(false);
            roccaControl.roccaProcess.setClassifierLoaded(false);
        } else if (e.getSource() == clickClassifierButton ) {
        	//selectClassifier(); serialVersionUID=24 2016/08/10 replaced with overloaded method
            clickClassifierFile=selectClassifier(clickClassifierTxt, clickClassifierFile, clickClassifierDesc);
            roccaParameters.setClassifyClicks(true);
        } else if (e.getSource() == clickClearClassifierButton) {
        	clickClassifierTxt.setText(noClassifier);
        	clickClassifierFile=null;
        	roccaParameters.setClassifyClicks(false);
            roccaControl.roccaProcess.setClassifierLoaded(false);
        } else if (e.getSource() == eventClassifierButton ) {
            eventClassifierFile=selectClassifier(eventClassifierTxt, eventClassifierFile, eventClassifierDesc);
            roccaParameters.setClassifyEvents(true);
        } else if (e.getSource() == eventClearClassifierButton) {
        	eventClassifierTxt.setText(noClassifier);
        	eventClassifierFile=null;
        	roccaParameters.setClassifyEvents(false);
        } else if (e.getSource() == recalcButton) {
        	RoccaFixParams recalc = new RoccaFixParams(roccaControl.roccaProcess);
        } else if (e.getSource() == reclassifyButton) {
        	RoccaClassifyThis reclassify = new RoccaClassifyThis(roccaControl.roccaProcess);
        } else if (e.getSource() == trainThenTestButton) {
        	RoccaTrainThenTest trainThenTest = new RoccaTrainThenTest(roccaControl.roccaProcess);
        } else if (e.getSource() == fftButton) {
        	roccaParameters.setUseFFT(true);
        	this.enableTheCorrectSource();
        } else if (e.getSource() == clickButton) {
        	roccaParameters.setUseClick(true);
        	this.enableTheCorrectSource();
        } else if (e.getSource() == wmButton) {
        	roccaParameters.setUseWMD(true);
        	this.enableTheCorrectSource();
        } else if (e.getSource() == gpsButton) {
        	roccaParameters.setUseGPS(true);
        	this.enableTheCorrectSource();
        } else if (e.getSource() == clickTypeDialog) {
        	createClickTypeDialog();
        } else if (e.getSource() == loadNotes ) {	// 	serialVersionUID=23 2016/01/04 added
        	ArrayList<String> sb=loadStandardNotes();
        	String[] array = (String[]) sb.toArray(new String[sb.size()]);
    	    for (String str : array) {
     	       populateComboBox(notesEncounter, str);
    	       populateComboBox(notesChan, str);
    	       populateComboBox(notesCruise, str);
    	       populateComboBox(notesGeoLoc, str);
    	       populateComboBox(notesProvider, str);
    	       populateComboBox(notesSpecies, str);
    	       }
    	    
    	// 	SerialVersionUID=23 2016/01/04 
    	// added these lines so that if the user manually types something into the combo box, it will
    	// be added to the list
        } else if (e.getSource() == notesEncounter ) {
        	String dummy = (String) notesEncounter.getSelectedItem();
            populateComboBox(notesEncounter, dummy);
        } else if (e.getSource() == notesChan ) {
        	String dummy = (String) notesChan.getSelectedItem();
            populateComboBox(notesChan, dummy);
        } else if (e.getSource() == notesCruise ) {
        	String dummy = (String) notesCruise.getSelectedItem();
            populateComboBox(notesCruise, dummy);
        } else if (e.getSource() == notesGeoLoc ) {
        	String dummy = (String) notesGeoLoc.getSelectedItem();
            populateComboBox(notesGeoLoc, dummy);
        } else if (e.getSource() == notesProvider ) {
        	String dummy = (String) notesProvider.getSelectedItem();
            populateComboBox(notesProvider, dummy);
        } else if (e.getSource() == notesSpecies ) {
        	String dummy = (String) notesSpecies.getSelectedItem();
            populateComboBox(notesSpecies, dummy);
        }
    }

    private void createClickTypeDialog() {
    	// find the click detector and load the click types and names
		ClickControl clickControl = (ClickControl) PamController.getInstance().findControlledUnit(ClickControl.UNITTYPE);
		String[] names = clickControl.getClickIdentifier().getSpeciesList();
		int[] clickTypeList = clickControl.getClickIdentifier().getCodeList();
		
		// loop through all the click types and check if any of them are on the 'selected' list.  If so, save
		// that index so that it gets highlighted in the dialog
		ArrayList<Integer> selected = null;
		int[] selectedTypes=null;
		if (selectedClickTypes != null) {
			selected = new ArrayList<Integer>();
	    	for (int i=0;i<clickTypeList.length;i++) {
	    		for (int j=0;j<selectedClickTypes.length;j++) {
	    			if (clickTypeList[i]==selectedClickTypes[j]) {
	    				selected.add(i);
	    			}
	    		}
	    	}
	
	    	// convert the arraylist to an int array for use by the dialog
			selectedTypes = new int[selected.size()];
			for (int i=0;i<selectedTypes.length;i++) {
				selectedTypes[i]=selected.get(i).intValue();
			}
		}
    	
    	// show the dialog    	
        int[] selectedTypesNew = RoccaClickTypeDialog.showDialog(
                                parentFrame,
                                clickTypeDialog,
                                "Click Types to Analyze",
                                names,
                                selectedTypes);
        
		// loop through the new list of selected clicks and save the corresponding click types
        if (selectedTypesNew == null) {
        	selectedClickTypes=null;
        } else {
			selected = new ArrayList<Integer>();
	    	for (int i=0;i<selectedTypesNew.length;i++) {
	    		selected.add(clickTypeList[selectedTypesNew[i]]);
	    	}
	    	
	    	// convert the arraylist to an int array for to save
	    	selectedClickTypes = new int[selected.size()];
			for (int i=0;i<selectedClickTypes.length;i++) {
				selectedClickTypes[i]=selected.get(i).intValue();
			}
        }
    }

    
    protected void selectDirectory() {
        String currDir = outputDirTxt.getText();
		JFileChooser fileChooser = new PamFileChooser();
        fileChooser.setDialogTitle("Select output directory...");
        fileChooser.setFileHidingEnabled(true);
        fileChooser.setApproveButtonText("Select");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (currDir != null) fileChooser.setSelectedFile(new File(currDir));
		int state = fileChooser.showOpenDialog(outputDirTxt);
		if (state == JFileChooser.APPROVE_OPTION) {
			currDir = fileChooser.getSelectedFile().getAbsolutePath();
            outputDirectory = fileChooser.getSelectedFile();
			//System.out.println(currFile);
		}
        outputDirTxt.setText(currDir);
    }
	
    protected void selectContourStatsFile() {
        String currFile = outputContourStatsTxt.getText();
		JFileChooser fileChooser = new PamFileChooser();
        fileChooser.setDialogTitle("Select contour stats output file...");
        fileChooser.setFileHidingEnabled(true);
        fileChooser.setApproveButtonText("Select");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (currFile != null) fileChooser.setSelectedFile(new File(currFile));
		int state = fileChooser.showOpenDialog(outputDirTxt);
		if (state == JFileChooser.APPROVE_OPTION) {
			currFile = fileChooser.getSelectedFile().getAbsolutePath();
            outputContourStatsFile = fileChooser.getSelectedFile();
			//System.out.println(currFile);
		}
        outputContourStatsTxt.setText(currFile);
    }

    /**
     * Ask the user to choose the model file for the classifier.  Load the classifier
     * description and display the text in the window
     */
    protected void selectClassifier() {
        String currFile = classifierTxt.getText();
		JFileChooser fileChooser = new PamFileChooser();
        fileChooser.setDialogTitle("Select classifier model...");
        fileChooser.setFileHidingEnabled(true);
        fileChooser.setApproveButtonText("Select");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (currFile != null) fileChooser.setSelectedFile(new File(currFile));
		int state = fileChooser.showOpenDialog(outputDirTxt);
		if (state == JFileChooser.APPROVE_OPTION) {
			
			// set the classifier parameters
			currFile = fileChooser.getSelectedFile().getAbsolutePath();
            classifierFile = fileChooser.getSelectedFile();
            roccaControl.roccaProcess.setClassifierLoaded(false);
            
            // get the classifier description and display
            String desc = getClassifierDesc();
            classifierDesc.setText(desc);
            classifierDesc.setCaretPosition(0);
            classifierDesc.repaint();
		}
        classifierTxt.setText(currFile);
    }
    
    /**
     * Ask the user to choose the model file for the classifier.  Load the classifier
     * description and display the text in the window
     * Overloaded method to allow it to handle both whistle and click classifier selection
     * serialVersionUID=24 2016/08/10
     */
    protected File selectClassifier(JTextField classTxt, File classFile, JTextArea classDesc) {
        String currFile = classTxt.getText();
		JFileChooser fileChooser = new PamFileChooser();
        fileChooser.setDialogTitle("Select classifier model...");
        fileChooser.setFileHidingEnabled(true);
        fileChooser.setApproveButtonText("Select");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Classifier Models", "model"));

        if (currFile != null) fileChooser.setSelectedFile(new File(currFile));
		int state = fileChooser.showOpenDialog(outputDirTxt);
		if (state == JFileChooser.APPROVE_OPTION) {
			
			// set the classifier parameters
			currFile = fileChooser.getSelectedFile().getAbsolutePath();
			classFile = fileChooser.getSelectedFile();
            roccaControl.roccaProcess.setClassifierLoaded(false);
            
            // get the classifier description and display
            String desc = getClassifierDesc(classFile);
            classDesc.setText(desc);
            classDesc.setCaretPosition(0);
            classDesc.repaint();
		}
		classTxt.setText(currFile);
		return classFile;
    }
    
    
    /**
     * Ask the user to choose the text file containing standard notes
     */
    protected ArrayList<String> loadStandardNotes() {
		JFileChooser fileChooser = new PamFileChooser();
        fileChooser.setDialogTitle("Select text file containing standard notes");
        fileChooser.setFileHidingEnabled(true);
        fileChooser.setApproveButtonText("Select");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    ArrayList<String> sb = new ArrayList<String>();

 		int state = fileChooser.showOpenDialog(outputDirTxt);
		if (state == JFileChooser.APPROVE_OPTION) {

		    // load the file
            File notesFile = fileChooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(notesFile))) {
	    	    String line = br.readLine();

	    	    while (line != null) {
	    	        sb.add(line);
	    	        line = br.readLine();
	    	    }
	    	} catch (Exception e) {
	            System.out.println("RoccaParametersDialog: Error loading standard notes");
				e.printStackTrace();
				notesFile = null;
			}
    		standardNotesFilename.setText(notesFile.getName());
    		standardNotesFilename.repaint();
		}
		return sb;
    }
    
//    protected void enableFFT() {
//    	fftSourceSubPanel.setEnabled(true);
//    	fftSourcePanel.setEnabledWithChannels(true);
//    	wmSourceSubPanel.setEnabled(false);
//    	wmSourcePanel.setEnabled(false);
//    	clickSourceSubPanel.setEnabled(false);
//    	clickSourcePanel.setEnabled(false);
//    	clickNoiseSourcePanel.setEnabled(false);
//    }
//    
//    protected void enableWM() {
//    	fftSourceSubPanel.setEnabled(false);
//    	fftSourcePanel.setEnabledWithChannels(false);
//    	wmSourceSubPanel.setEnabled(true);
//    	wmSourcePanel.setEnabled(true);
//    	clickSourceSubPanel.setEnabled(false);
//    	clickSourcePanel.setEnabled(false);
//    	clickNoiseSourcePanel.setEnabled(false);
//    }
//
//    protected void enableClick() {
//    	fftSourceSubPanel.setEnabled(false);
//    	fftSourcePanel.setEnabledWithChannels(false);
//    	wmSourceSubPanel.setEnabled(false);
//    	wmSourcePanel.setEnabled(false);
//    	clickSourceSubPanel.setEnabled(true);
//    	clickSourcePanel.setEnabled(true);
//    	clickNoiseSourcePanel.setEnabled(true);
//    }
    
	public static RoccaParameters showDialog(Frame parentFrame, 
            RoccaParameters roccaParameters,
            RoccaControl roccaControl) {
		if (singleInstance == null || singleInstance.getParent() != parentFrame) {
			singleInstance = new RoccaParametersDialog(parentFrame);
		}
        singleInstance.roccaControl = roccaControl;
        singleInstance.roccaParameters = roccaParameters.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.roccaParameters;
	}

	/**
	 * Enables/disables the FFT, Click Detector and Whistle and Moan sources based on the value found
	 * in the current roccaParameters object 
	 * 
	 * 2014/10/13 allow multiple sources.  enable/disable source panels based on radio button selection
	 */
	public void enableTheCorrectSource() {
		
		// enable/disable radio buttons depending on whether or not modules are loaded
		if (PamController.getInstance().findControlledUnit(ClickControl.UNITTYPE)==null) {
			roccaParameters.setUseClick(false);
			clickButton.setEnabled(false);
		} else {
			clickButton.setEnabled(true);
		}
		if (PamController.getInstance().findControlledUnit(WhistleMoanControl.UNITTYPE)==null) {
			roccaParameters.setUseWMD(false);
			wmButton.setEnabled(false);
		} else {
			wmButton.setEnabled(true);
		}
		if (PamController.getInstance().findControlledUnit(GPSControl.gpsUnitType)==null) {
			roccaParameters.setUseGPS(false);
			gpsButton.setEnabled(false);
		} else {
			gpsButton.setEnabled(true);
		}
		
		
		if (roccaParameters.weAreUsingFFT()) {
//			enableFFT();
			fftButton.setSelected(true);
			fftSourceSubPanel.setVisible(true);
	    	fftSourceSubPanel.setEnabled(true);
	    	fftSourcePanel.setEnabledWithChannels(true);
//			wmButton.setSelected(false);
//			clickButton.setSelected(false);
		} else {
			fftButton.setSelected(false);
			fftSourceSubPanel.setVisible(false);
	    	fftSourceSubPanel.setEnabled(false);
	    	fftSourcePanel.setEnabledWithChannels(false);
		} 
		
		if (roccaParameters.weAreUsingClick()) {
//			enableClick();
//			fftButton.setSelected(false);
//			wmButton.setSelected(false);
			clickButton.setSelected(true);	
			clickSourceSubPanel.setVisible(true);
	    	clickSourceSubPanel.setEnabled(true);
	    	clickSourcePanel.setEnabled(true);
	    	clickNoiseSourcePanel.setEnabled(true);
	    	
	    	// now check if any click types have been created
			ClickControl clickControl = (ClickControl) PamController.getInstance().findControlledUnit(ClickControl.UNITTYPE);
			if (clickControl.getClickIdentifier().getSpeciesList() == null ||
					clickControl.getClickIdentifier().getSpeciesList().length==0 ) {
				clickTypeNote.setVisible(true);
				clickTypeDialog.setEnabled(false);
			} else {
				clickTypeNote.setVisible(false);
				clickTypeDialog.setEnabled(true);
			}

		} else {
			clickButton.setSelected(false);			
			clickSourceSubPanel.setVisible(false);
	    	clickSourceSubPanel.setEnabled(false);
	    	clickSourcePanel.setEnabled(false);
	    	clickNoiseSourcePanel.setEnabled(false);
		}
		
		if (roccaParameters.weAreUsingWMD()) {
//			enableWM();
			wmButton.setSelected(true);
			wmSourceSubPanel.setVisible(true);
	    	wmSourceSubPanel.setEnabled(true);
	    	wmSourcePanel.setEnabled(true);
//			clickButton.setSelected(false);
//			fftButton.setSelected(false);
		} else {
			wmButton.setSelected(false);
			wmSourceSubPanel.setVisible(false);
	    	wmSourceSubPanel.setEnabled(false);
	    	wmSourcePanel.setEnabled(false);
		}
		
		if (roccaParameters.weAreUsingGPS()) {
			gpsButton.setSelected(true);
			gpsSourceSubPanel.setVisible(true);
			gpsSourceSubPanel.setEnabled(true);
			gpsSourcePanel.setEnabled(true);
		} else {
			gpsButton.setSelected(false);
			gpsSourceSubPanel.setVisible(false);
			gpsSourceSubPanel.setEnabled(false);
			gpsSourcePanel.setEnabled(false);
		}
		singleInstance.pack();
	}

	/**
     * sets up the labels to be shown in the dialog box, based on the current
     * roccaParameters object
     */
	public void setParams() {
		ArrayList<PamDataBlock> fftSources = PamController.getInstance().getFFTDataBlocks();
		fftSourcePanel.setSource(fftSources.get(roccaParameters.fftDataBlock));
		fftSourcePanel.setChannelList(roccaParameters.getChannelMap());
		clickSourcePanel.setSourceList();
		clickSourcePanel.setSource(roccaParameters.getClickDataSource());
		clickNoiseSourcePanel.setSourceList();
		clickNoiseSourcePanel.setSource(roccaParameters.getClickNoiseDataSource());
		selectedClickTypes = roccaParameters.getClickTypeList();
		wmSourcePanel.setSourceList();
		wmSourcePanel.setSource(roccaParameters.getWmDataSource());
		this.enableTheCorrectSource();
		gpsSourcePanel.setSourceList();
		gpsSourcePanel.setSource(roccaParameters.getGpsSource());
		
        noiseSensitivity.setText(String.format("%.1f",
                roccaParameters.getNoiseSensitivity()));
		energyBinSize.setText(String.format("%d", 
                roccaParameters.getEnergyBinSize()));
		ancCalcs4Clicks.setSelected(roccaParameters.runAncCalcs4Clicks); 	// serialVersionUID=22 2015/06/13 added
		ancCalcs4Whistles.setSelected(roccaParameters.runAncCalcs4Whistles); 	// serialVersionUID=22 2015/06/13 added
		trimWav.setSelected(roccaParameters.isTrimWav());
        classificationThreshold.setText(String.format("%d",
                roccaParameters.getClassificationThreshold()));
        sightingThreshold.setText(String.format("%d",
                roccaParameters.getSightingThreshold()));
        filenameTemplate.setText(roccaParameters.getFilenameTemplate());
        //notesEncounter.setText(roccaParameters.getNotesEncounterID());		serialVersionUID=23 2016/01/04 changed to JComboBox
        //notesCruise.setText(roccaParameters.getNotesCruiseID());		serialVersionUID=23 2016/01/04 changed to JComboBox
        //notesProvider.setText(roccaParameters.getNotesDataProvider());		serialVersionUID=23 2016/01/04 changed to JComboBox
        //notesSpecies.setText(roccaParameters.getNotesKnownSpecies());		serialVersionUID=23 2016/01/04 changed to JComboBox
        //notesGeoLoc.setText(roccaParameters.getNotesGeoLoc());		serialVersionUID=23 2016/01/04 changed to JComboBox
        //notesChan.setText(roccaParameters.getNotesChan());		serialVersionUID=23 2016/01/04 changed to JComboBox
        populateComboBox(notesEncounter, roccaParameters.getNotesEncounterID());
        populateComboBox(notesCruise, roccaParameters.getNotesCruiseID());
        populateComboBox(notesProvider, roccaParameters.getNotesDataProvider());
        populateComboBox(notesSpecies, roccaParameters.getNotesKnownSpecies());
        populateComboBox(notesGeoLoc, roccaParameters.getNotesGeoLoc());
        populateComboBox(notesChan, roccaParameters.getNotesChan());
        
        try {
            outputDirectory = roccaParameters.roccaOutputDirectory;
            String dirString = outputDirectory.getAbsolutePath();
//            int dirLen = dirString.length();
//            String dirSubString;
//            if (dirLen > 30) {
//                dirSubString = "..." + dirString.substring(dirString.length()-30);
//            } else {
//                dirSubString = dirString;
//            }
//            outputDirTxt.setText(dirSubString);
            outputDirTxt.setText(dirString);
        } catch (NullPointerException ex) {
            outputDirectory = new File("C:\\");
            outputDirTxt.setText("C:\\");
        }
        try {
            outputContourStatsFile = roccaParameters.roccaContourStatsOutputFilename;
            outputContourStatsTxt.setText(
                    roccaParameters.roccaContourStatsOutputFilename.getName());
        } catch (NullPointerException ex) {
            outputContourStatsFile = new File("C:\\RoccaContourStats.csv");
            outputContourStatsTxt.setText("RoccaContourStats.csv");
        }
        try {
            outputSightingStatsFile = roccaParameters.roccaSightingStatsOutputFilename;
            outputSightingStatsTxt.setText(
                    roccaParameters.roccaSightingStatsOutputFilename.getName());
        } catch (NullPointerException ex) {
            outputSightingStatsFile = new File("C:\\RoccaSightingStats.csv");
            outputSightingStatsTxt.setText("RoccaSightingStats.csv");
        }
        if (!roccaParameters.isClassifyWhistles()) {
            classifierTxt.setText(noClassifier);
            classifierFile = null;
        } else {
	        try {
	            classifierFile = roccaParameters.getRoccaClassifierModelFilename();
	            String dirString = classifierFile.getAbsolutePath();
	            classifierTxt.setText(dirString);
	            
	            // get the classifier description and display
	            String desc = getClassifierDesc(classifierFile);	// serialVersionUID=24 2016/08/10 changed from getClassifierDesc method
	            classifierDesc.setText(desc);
	            classifierDesc.setCaretPosition(0);
	            classifierDesc.repaint();
	        } catch (NullPointerException ex) {
	            classifierFile = null;
	            classifierTxt.setText(noClassifier);
	            roccaParameters.setClassifyWhistles(false);
	        }
        }
        if (!roccaParameters.isClassifyClicks()) {
        	clickClassifierTxt.setText(noClassifier);
        	clickClassifierFile = null;
        } else {
	        try {
	            clickClassifierFile = roccaParameters.getRoccaClickClassifierModelFilename();
	            String dirString = clickClassifierFile.getAbsolutePath();
	            clickClassifierTxt.setText(dirString);
	            
	            // get the classifier description and display
	            String desc = getClassifierDesc(clickClassifierFile);
	            clickClassifierDesc.setText(desc);
	            clickClassifierDesc.setCaretPosition(0);
	            clickClassifierDesc.repaint();
	        } catch (NullPointerException ex) {
	            clickClassifierFile = null;
	            clickClassifierTxt.setText(noClassifier);
	            roccaParameters.setClassifyClicks(false);
	        }
        }
        if (!roccaParameters.isClassifyEvents()) {
        	eventClassifierTxt.setText(noClassifier);
        	eventClassifierFile = null;
        } else {
	        try {
	        	eventClassifierFile = roccaParameters.getRoccaEventClassifierModelFilename();
	            String dirString = eventClassifierFile.getAbsolutePath();
	            eventClassifierTxt.setText(dirString);
	            
	            // get the classifier description and display
	            String desc = getClassifierDesc(eventClassifierFile);
	            eventClassifierDesc.setText(desc);
	            eventClassifierDesc.setCaretPosition(0);
	            eventClassifierDesc.repaint();
	        } catch (NullPointerException ex) {
	        	eventClassifierFile = null;
	        	eventClassifierTxt.setText(noClassifier);
	            roccaParameters.setClassifyEvents(false);
	        }
        }
    }
	
	
	/**
	 * Adds the passed item to the passed combo box, but only if the item is
	 * not a duplicate
	 * SerialVersionUID=23 2016/01/04
	 * 
	 * @param list
	 * @param item
	 */
	public void populateComboBox(JComboBox<String> list, String item) {
    	boolean foundit = false;
    	int temp=list.getItemCount();
    	for (int i=0; i<list.getItemCount(); i++) {
    		if (list.getItemAt(i).equals(item)) {
    			foundit = true;
    			break;
    		}
    	}
    	if (!foundit && item != "" && item != null) {	// serialVersionUID=24 2016/08/10 added check for null
    		list.addItem(item);
    	}

	}
	
	/**
	 * Loads the classifier description and displays in the dialog.  Note that prior to the
	 * introduction of 2-stage classifiers, model files did not contain descriptions.  If this
	 * is the case, return a default string
	 * 
	 * @return String containing the classifier description
	 */
	public String getClassifierDesc() {
		// default return String
		String desc = new String("Classifier description not available");
		
	    // load the model file
	    String fname = classifierFile.getAbsolutePath();
	    try {
	        BufferedInputStream input = new BufferedInputStream(
	                (new ProgressMonitorInputStream(null, "Loading Classifier - Please wait",
	                new FileInputStream(fname))));
	        Object[] modelParams = SerializationHelper.readAll(input);
	
	        // there are 2 different styles of model file, the original version and the version
	        // developed for the 2-stage classifier.  Both files contain 2 objects.
	        // The original version contained the classifier and the training dataset.  The newer
	        // version contains a String description and the classifier.  Test the first object;
	        // if it's a String, then this is the newer version and the String is the description.
	        // If it's not a String, assume this is the old version and return the default desc.
	        if (modelParams[0] instanceof String) {
	        	desc = (String) modelParams[0];
	        }
	        
	    } catch (Exception ex) {
	        System.err.println("Deserialization failed: " + ex.getMessage());
	        return null;
	    }	
		return desc;
	}
	
	
	/**
	 * Loads the classifier description and displays in the dialog.  Note that prior to the
	 * introduction of 2-stage classifiers, model files did not contain descriptions.  If this
	 * is the case, return a default string
	 * Overloaded method to make it work with both whistle and click classifier files
	 * serialVersionUID=24 2016/08/10
	 * ...later that same rev...
	 * Commented out entire method - while it's nice to display a description of the classifier, it takes 
	 * too long to load the classifier just for the sake of reading the string.  Especially when the classifier
	 * could be >400Mb.
	 * 
	 * @return String containing the classifier description
	 */
	public String getClassifierDesc(File classFile) {
		// default return String
		String desc = new String("Classifier description not available");
		
/*	    // load the model file
	    String fname = classFile.getAbsolutePath();
	    try {
	        BufferedInputStream input = new BufferedInputStream(
	                (new ProgressMonitorInputStream(null, "Loading Classifier - Please wait",
	                new FileInputStream(fname))));
	        Object[] modelParams = SerializationHelper.readAll(input);
	
	        // there are 2 different styles of model file, the original version and the version
	        // developed for the 2-stage classifier.  Both files contain 2 objects.
	        // The original version contained the classifier and the training dataset.  The newer
	        // version contains a String description and the classifier.  Test the first object;
	        // if it's a String, then this is the newer version and the String is the description.
	        // If it's not a String, assume this is the old version and return the default desc.
	        if (modelParams[0] instanceof String) {
	        	desc = (String) modelParams[0];
	        }
	        
	    } catch (Exception ex) {
	        System.err.println("Deserialization failed: " + ex.getMessage());
	        return null;
	    }	
*/		return desc;
	}
	
	
	/**
	 * Loads the stage 1 classifier and populates the drop-down box with the classes
	 */
	public void showClasses() {
		
		// load the classifier and get the classes
    	//System.err.println("Getting the stage1 classes");
		String[] classes = getStage1Classes();
		stage1ClassList.removeAllElements();
		if (classes!=null) {
        	// System.err.println("populating the class list");
			for (int i=0; i<classes.length;i++) {
				stage1ClassList.addElement(classes[i]);
			}
			
			// populate the combobox with the classes and set the combobox to the first class in the list
			stage1ClassModel = new DefaultComboBoxModel(stage1ClassList);
			stage1Classes.setSelectedIndex(0);	
			stage1Classes.setEnabled(true);
		} else {
        	System.err.println("error getting stage 1 classes");
            stage1ClassList.addElement((String) "<none>");
			stage1ClassModel = new DefaultComboBoxModel(stage1ClassList);
			stage1Classes.setEnabled(false);
		}
		stage1Classes.repaint();
	}
	
	public String[] getStage1Classes() {
	    // load the stage 1 classification tree
	    String fname = classifierFile.getAbsolutePath();
	    try {
	        BufferedInputStream input = new BufferedInputStream(
	                (new ProgressMonitorInputStream(null, "Loading 1st Stage Classifier - Please wait",
	                new FileInputStream(fname))));
	      Object[] modelParams = SerializationHelper.readAll(input);
	
	        // separate the classifier model from the training dataset info
	        Instances trainedDataset = (Instances) modelParams[1];
	        
		    // update the species list
		    String[] speciesList = new String[trainedDataset.numClasses()];
		    Attribute classAttribute = trainedDataset.classAttribute();
		
		    for (int i=0; i<trainedDataset.numClasses(); i++) {
		        speciesList[i] = classAttribute.value(i);
		    }
		    return speciesList;
		    
	    } catch (Exception ex) {
	        System.err.println("Deserialization failed: " + ex.getMessage());
	        return null;
	    }	
	}

	

	@Override
	public void cancelButtonPressed() {
		roccaParameters = null;
	}

	@Override
	/**
	 * takes the values of the labels in the dialog box and sets the
     * roccaParameter object's fields
	 */
	public boolean getParams() {
		/*
		 * get the source parameters
		 */
		// change 'else if' to 'if' so that multiple sources are possible
		// 2014/10/13 MO
		if (roccaParameters.weAreUsingFFT()) {
			roccaParameters.fftDataBlock = fftSourcePanel.getSourceIndex();
			roccaParameters.setChannelMap(fftSourcePanel.getChannelList());
			if (roccaParameters.getChannelMap() == 0) {
				return false;
			}
		// } else if (roccaParameters.weAreUsingClick()) {
		} 
		if (roccaParameters.weAreUsingClick()) {
			roccaParameters.setClickDataSource(clickSourcePanel.getSourceName());
			roccaParameters.setClickNoiseDataSource(clickNoiseSourcePanel.getSourceName());
			roccaParameters.setClickTypeList(selectedClickTypes);
		// } else {
		} 
		if (roccaParameters.weAreUsingWMD()) {
			
			// need to check if the WMD data block is detecting on FFT data or Beamformer data.  Beamformer data lacks a link
			// to the raw audio, which Rocca requires for analysis.  If that's the case here, warn the user and exit
			PamDataBlock wmdSource = PamController.getInstance().getDataBlock(AbstractWhistleDataUnit.class, wmSourcePanel.getSourceName());
			if (wmdSource.getSequenceMapObject()==null) {
				roccaParameters.setWmDataSource(wmSourcePanel.getSourceName());
				WarningSystem.getWarningSystem().removeWarning(roccaWarning);
			}
			else {
				String err = "Error: the selected Whistle & Moan Detector uses Beamformer output as a data source, and Beamformer output does not contain "
				+ "the link back to a single channel of raw audio data that Rocca requires for analysis.  Please either change the Whistle & Moan "
				+ "Detector source, or change the Rocca source.";
				roccaWarning.setWarningMessage(err);
				WarningSystem.getWarningSystem().addWarning(roccaWarning);
				return false;
			}
		}
		
		if (roccaParameters.weAreUsingGPS()) {
			roccaParameters.setGpsSource(gpsSourcePanel.getSourceName());
		}
		
		
		// will throw an exception if the number format of any of the parameters is invalid, 
		// so catch the exception and return false to prevent exit from the dialog. 
		try {
			roccaParameters.noiseSensitivity = Double.valueOf(noiseSensitivity.getText());
			roccaParameters.energyBinSize = Integer.valueOf(energyBinSize.getText());
			roccaParameters.setClickAncCalcs(ancCalcs4Clicks.isSelected()); 	// serialVersionUID=22 2015/06/13 added
			roccaParameters.setWhistleAncCalcs(ancCalcs4Whistles.isSelected()); 	// serialVersionUID=22 2015/06/13 added
			roccaParameters.setTrimWav(trimWav.isSelected());
            roccaParameters.setClassificationThreshold
                    (Integer.valueOf(classificationThreshold.getText()));
            roccaParameters.setSightingThreshold
                    (Integer.valueOf(sightingThreshold.getText()));

            // set the directory and filename fields
            roccaParameters.roccaOutputDirectory = outputDirectory;
            roccaParameters.roccaContourStatsOutputFilename = new File
                    (outputDirectory,
                    outputContourStatsTxt.getText());
            roccaParameters.roccaSightingStatsOutputFilename = new File
                    (outputDirectory,
                    outputSightingStatsTxt.getText());

            // if a new model has been selected, set the flag to indicate it
            // hasn't been loaded yet
            if (!Objects.equals(classifierFile, roccaParameters.roccaClassifierModelFilename) ||
            		!Objects.equals(clickClassifierFile, roccaParameters.roccaClickClassifierModelFilename) ||
            		!Objects.equals(eventClassifierFile, roccaParameters.roccaEventClassifierModelFilename)) {
              roccaControl.roccaProcess.setClassifierLoaded(false);
            }
            roccaParameters.setRoccaClassifierModelFilename(classifierFile);
            roccaParameters.setRoccaClickClassifierModelFilename(clickClassifierFile); // serialVersionUID=24 2016/08/10 added
            roccaParameters.setRoccaEventClassifierModelFilename(eventClassifierFile); // serialVersionUID=24 2016/08/10 added
            roccaParameters.setFilenameTemplate(filenameTemplate.getText());
            //roccaParameters.setNotesEncounterID(notesEncounter.getText());
            //roccaParameters.setNotesCruiseID(notesCruise.getText());
            //roccaParameters.setNotesDataProvider(notesProvider.getText());
            //roccaParameters.setNotesKnownSpecies(notesSpecies.getText());
            //roccaParameters.setNotesGeoLoc(notesGeoLoc.getText());
            //roccaParameters.setNotesChan(notesChan.getText());
            roccaParameters.setNotesEncounterID((String) notesEncounter.getSelectedItem());
            roccaParameters.setNotesCruiseID((String) notesCruise.getSelectedItem());
            roccaParameters.setNotesDataProvider((String) notesProvider.getSelectedItem());
            roccaParameters.setNotesKnownSpecies((String) notesSpecies.getSelectedItem());
            roccaParameters.setNotesGeoLoc((String) notesGeoLoc.getSelectedItem());
            roccaParameters.setNotesChan((String) notesChan.getSelectedItem());
		}
		catch (NumberFormatException ex) {
			return false;
		}		
		return true;
	}

	@Override
	public void restoreDefaultSettings() {

		roccaParameters = new RoccaParameters();
		setParams();
		
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
	    if (e.getSource() == fftButton) {
	    	if (e.getStateChange()==ItemEvent.SELECTED) {
	        	roccaParameters.setUseFFT(true);
	    	} else {
	        	roccaParameters.setUseFFT(false);
	    	}
	    } else if (e.getSource() == clickButton) {
	    	if (e.getStateChange()==ItemEvent.SELECTED) {
		    	roccaParameters.setUseClick(true);
	    	} else {
		    	roccaParameters.setUseClick(false);
	    	}
	    } else if (e.getSource() == wmButton) {
	    	if (e.getStateChange()==ItemEvent.SELECTED) {
		    	roccaParameters.setUseWMD(true);
	    	} else {
		    	roccaParameters.setUseWMD(false);
	    	}
	    } else if (e.getSource() == gpsButton) {
	    	if (e.getStateChange()==ItemEvent.SELECTED) {
		    	roccaParameters.setUseGPS(true);
	    	} else {
		    	roccaParameters.setUseGPS(false);
	    	}
		}
	    this.enableTheCorrectSource();
	}
}
