/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import PamView.PamGui;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import clickDetector.offlineFuncs.OfflineEventDataBlock;
import clickDetector.offlineFuncs.OfflineEventDataUnit;


/**
 * Main controller for Rocca
 *
 * @author Michael Oswald
 */
public class RoccaControl extends PamControlledUnit implements PamSettings {

    protected RoccaControl roccaControl;
    protected RoccaParameters roccaParameters = new RoccaParameters();
    protected RoccaProcess roccaProcess;
    protected RoccaWhistleSelect roccaWhistleSelect;
    protected RoccaSidePanel roccaSidePanel;

    /**
     * Added 2014/09/11 - use in the constructor call to super class instead
     * of the hard-coded string
     */
	public static final String unitType = "Rocca";
	
	/*
	 * Max max data keep time to avoid memory overflows. Currently at 15 minutes. 
	 */
	private static final int MAXMAXDATAKEEPTIME = 900000;
	
	/**
	 * reference to the ClickControl module when it is loaded in Viewer mode
	 */
	private ClickControl clickControl;

	/**
	 * reference to offline datablock used in ClickDetector in Viewer mode
	 */
	private OfflineEventDataBlock offlineDataBlock;

	/**
	 * boolean indicating whether or not we are in Viewer mode
	 */
	private boolean viewerMode = false;
	
	/**
	 * Submenu in Click Detector menu, containing a list of events to run
	 * the Rocca measurement methods on
	 * Converted from JMenu (submenu) to JMenuItem
	 * serialVersionUID = 21
	 * 2015/05/31
	 */
	private JMenuItem roccaSubmenu;

	/**
	 * a copy of the menu bar - used to add
	 */
	JMenuBar clickTabMenu = null;
	
	/**
	 * menu item listener.  Records which click detector event to analyze
	 * when in Viewer Mode
	 */
	private SelectClickEvent eventListener;
	
	/**
	 * keep track of the parent frame
	 * serialVersionUID = 21 2015/05/31 added
	 */
	private Frame parentFrame;
	
	/**
	 * flag indicating whether initialization has been completed
	 */
	private boolean initialisationComplete = false;
	

	/**
	 * Constructor
	 * @param name
	 */
    public RoccaControl(String name){
        super(unitType, name);

        roccaControl = this;
        PamSettingManager.getInstance().registerSettings(this);
        addPamProcess(roccaProcess = new RoccaProcess(this));
        addPamProcess(roccaWhistleSelect = new RoccaWhistleSelect(this));
		setSidePanel(roccaSidePanel = new RoccaSidePanel(this));

		// check if a Click Detector module is loaded.
		this.clickControl = (ClickControl) PamController.getInstance().findControlledUnit("Click Detector");
		
		// check if we are in Viewer mode
		viewerMode = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;

    
    }

    @Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " Parameters");
		menuItem.addActionListener(new SetParameters(parentFrame));
		this.parentFrame=parentFrame;	// serialVersionUID = 21 2015/05/31 added
		return menuItem;
	}


    /**
     * If we are in Viewer mode and a Click Detector module has been loaded, find
     * the Click Detection menu item and add a Rocca Measurement submenu item to it
     */
	@Override
	public JMenuBar getTabSpecificMenuBar(Frame parentFrame, JMenuBar menuBar, PamGui pamGui) {
		if (viewerMode && clickControl != null) {
			for (int i = 0; i < menuBar.getMenuCount(); i++) {
				if (menuBar.getMenu(i).getText().equals("Click Detection")) {
					int numItems = menuBar.getMenu(i).getItemCount();
					for (int j=0; j<numItems; j++) {
						if (menuBar.getMenu(i).getItem(j).getText().equals("Target Motion Analysis ...")) {
							roccaControl.addDetectorMenuItems(clickControl, menuBar.getMenu(i),j);	
						}
					}
					break;
				}
			}
		}
		return menuBar;
	}

	/**
     * Adds a menu item when in viewer mode to the Click Detector menu, to
     * pass events to Rocca for measurement.  This is called from ClickControl.
     * Note that this method has been copied from TargetMotionLocaliser.
     * 
     * @param clickControl clickControl object calling this method
     * @param menu menu to add to
     * @param position where to add the new Rocca menu item
     * @return
     */
    public int addDetectorMenuItems(ClickControl clickControl,JMenu menu, int position) {
    	
    	// changed from MenuListener to ActionListener (MenuListener fires as soon as menu item
    	// is highlighted, and we want to wait until the user actually clicks on the item)
    	// serialVersionUID=21
    	// 2015/05/31
		roccaSubmenu = new JMenuItem("Rocca Measurement ...");
//		roccaSubmenu.addMenuListener(new CheckRoccaMenuItem());
		roccaSubmenu.addActionListener(new CheckRoccaMenuItem());
		menu.insert(roccaSubmenu,position);
		this.clickControl=clickControl;
		return 1;
	}
    
    /**
     * A bit of a fudge to deal with some old code which kept doubling the 
     * keep time of the raw data, which eventually led to raw data using too 
     * much memory and bringing down PG. I don't fully understand that code so 
     * have left as much of it as possible, but put this in as an absolute 
     * maximum which cannot be exceeded. 
     * @return max in milliseconds as int, which is what's used in PAMDataBlock. 
     */
    public int getMaxDataKeepTime() {
    	return MAXMAXDATAKEEPTIME;
    }
    /**
     * 
     * @param eventList
     */
    public void analyzeOfflineClicks(OfflineEventDataUnit eventList) {
    	
		// create a new RoccaContourDataBlock with the current click
    	int numClicks = eventList.getNClicks();
    	ClickDetection cd; 
    	
    	for (int i=0; i<numClicks; i++) {
    		
    		// create a RoccaContourDataBlock with the current detection
    		PamDataUnit pamDataUnit = eventList.getSubDetection(i);
    		if (ClickDetection.class.isAssignableFrom(pamDataUnit.getClass())) {
        		cd = (ClickDetection) pamDataUnit;
    		}
    		else {
    			continue;
    		}
    		cd.setOfflineEventID(eventList.getDatabaseIndex());
			RoccaContourDataBlock rcdb = roccaProcess.newClickDetectorData(cd);
			if (rcdb==null) {
				//return;
				continue;	// serialVersionUID=22 2015/06/23 don't break out of the loop completely, just skip to the next detection
			}
			
			// 2017/12/4 set the natural lifetime to Integer.Max, so that we definitely keep all of the data
			// units during this code block.  Set the lifetime back to 0 at the end of the block
			rcdb.setNaturalLifetimeMillis(Integer.MAX_VALUE);
			
			// pass the latest noise data unit to the rcdb
			rcdb.setClickNoise(null);
			
			// run statistics on click
			rcdb.calculateClickStats();
			
			// classify click, if user has selected a click classifier
			// serialVersionUID=25 2019/03/04 added
			//rcdb.setClassifiedAs(RoccaRFModel.AMBIG);
			
			// 2025/01/20 always try to classify, since we now allow Rocca to run without a classifier.  But only try to prune
			// if we are using a classifier
			// note that this has been in RoccaProcess.newData since 2021, but I missed it here
			if (roccaControl.roccaParameters.isClassifyClicks()) {
				
				// serialVersionUID=25 2017/03/28 added pruning params for ONR/LMR project N00014-14-1-0413
				// classifiers.  Check if the loaded classifier model filename matches one of the classifier
				// names created for the project.  If so, compare the click to the parameters used to prune
				// the datasets and exit if the click falls outside of the thresholds
				if (roccaParameters.roccaClickClassifierModelFilename.getName().equals("TemPacClick.model") &&
						(rcdb.getContour().get(RoccaContourStats.ParamIndx.SNR) > 35. ||
								rcdb.getContour().get(RoccaContourStats.ParamIndx.DURATION) < 0.005 ||
								rcdb.getContour().get(RoccaContourStats.ParamIndx.DURATION) > 0.6 )) {
					rcdb.setNaturalLifetimeMillis(0);
					return;
				}
				if (roccaParameters.roccaClickClassifierModelFilename.getName().equals("HIClick.model") &&
						(rcdb.getContour().get(RoccaContourStats.ParamIndx.SNR) > 40. ||
								rcdb.getContour().get(RoccaContourStats.ParamIndx.DURATION) < 0.01 ||
								rcdb.getContour().get(RoccaContourStats.ParamIndx.DURATION) > 0.6 )) {
					rcdb.setNaturalLifetimeMillis(0);
					return;
				}
				if (roccaParameters.roccaClickClassifierModelFilename.getName().equals("NWAtlClick.model") &&
						(rcdb.getContour().get(RoccaContourStats.ParamIndx.SNR) > 35. ||
								rcdb.getContour().get(RoccaContourStats.ParamIndx.DURATION) < 0.005 ||
								rcdb.getContour().get(RoccaContourStats.ParamIndx.DURATION) > 0.6 )) {
					rcdb.setNaturalLifetimeMillis(0);
					return;
				}
			}

			roccaProcess.roccaClassifier.classifyContour2(rcdb);
			

	        // check the side panel for a detection number.  If one has not yet been created,
	        // create a default one now.  The user can always rename it later
			String sNum = roccaControl.roccaSidePanel.getSightingNum();
	        if (sNum.equals(RoccaSightingDataUnit.NONE)) {
	            sNum = roccaControl.roccaSidePanel.sidePanel.addASighting("Clk001");
	        }
	        roccaProcess.updateSidePanel(rcdb, true);  // serialVersionUID=22 2015/06/13 added
	        roccaProcess.saveContourStats(rcdb, rcdb.getChannelMap(), i, sNum);
			rcdb.setNaturalLifetimeMillis(0);
    	}
    }


    // change this from MenuListener to ActionListener, because we've gotten rid of the submenu
    // and are using a seperate window instead.  Comment out the methods associated with a
    // MenuListener, and add the actionPerformed method
	// serialVersionUID=21
	// 2015/05/31
//	class CheckRoccaMenuItem implements MenuListener {
	class CheckRoccaMenuItem implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			createClickEventDialog();			
		}
		
		
//		@Override
//		public void menuSelected(MenuEvent arg0) {
////			populateDetectorSubmenu();
//			createClickEventDialog();
//		}
//
//		@Override
//		public void menuCanceled(MenuEvent arg0) {
//		}
//
//		@Override
//		public void menuDeselected(MenuEvent arg0) {
//		}

		public void populateDetectorSubmenu() {
			offlineDataBlock=clickControl.getClickDetector().getOfflineEventDataBlock();
			roccaSubmenu.removeAll();
			//int numUnits = offlineDataBlock.getUnitsCount();
			//ListIterator<OfflineEventDataUnit> it = offlineDataBlock.getListIterator(0);
			OfflineEventDataUnit dataUnit;
			eventListener = new SelectClickEvent();
			JMenuItem menuItem;
			for (int i=0; i<offlineDataBlock.getUnitsCount(); i++) {
				dataUnit = offlineDataBlock.getDataUnit(i, PamDataBlock.REFERENCE_ABSOLUTE);
				menuItem = new JMenuItem(String.format("Id %d; %s", dataUnit.getDatabaseIndex(), 
				PamCalendar.formatDateTime2(dataUnit.getTimeMilliseconds())));
				menuItem.putClientProperty("index", i);
				menuItem.addActionListener(eventListener);
				roccaSubmenu.add(menuItem);
			}
    
//			while (it.hasNext()) {
//				dataUnit = it.next();
//				menuItem = new JMenuItem(String.format("Id %d; %s", dataUnit.getDatabaseIndex(), 
//						PamCalendar.formatDateTime2(dataUnit.getTimeMilliseconds())));
//				menuItem.putClientProperty("index", dataUnit.getDatabaseIndex());
//				menuItem.addActionListener(eventListener);
//				roccaSubmenu.add(menuItem);
//			}			
		}
		
		/**
		 *  Creates a dialog listing all of the available events. User can select multiple
		 *  events, as well as a "Select All" button
		 * 	serialVersionUID = 21 
		 *  2015/05/31
		 */
	    private void createClickEventDialog() {
	    	// find the click detector and load the click types and names
			offlineDataBlock=clickControl.getClickDetector().getOfflineEventDataBlock();
			OfflineEventDataUnit dataUnit;
			String[] names = new String[offlineDataBlock.getUnitsCount()];
			for (int i=0; i<offlineDataBlock.getUnitsCount(); i++) {
				dataUnit = offlineDataBlock.getDataUnit(i, PamDataBlock.REFERENCE_ABSOLUTE);
				names[i] = String.format("Id %d; %s", dataUnit.getDatabaseIndex(), 
				PamCalendar.formatDateTime2(dataUnit.getTimeMilliseconds()));
			}
	    	
	    	// show the dialog    	
	        int[] selectedTypesNew = RoccaClickEventList.showDialog(
	                                parentFrame,
	                                roccaSubmenu,
	                                "Click Events to Analyze",
	                                names);
	        
        	// loop through the selected events, analyzing each one
	        if (selectedTypesNew != null && selectedTypesNew.length != 0) {	      
	        	OfflineEventDataUnit eventList;
		    	for (int i=0;i<selectedTypesNew.length;i++) {
					eventList = offlineDataBlock.getDataUnit(selectedTypesNew[i], PamDataBlock.REFERENCE_ABSOLUTE  ); // bug fix - corrected indexing; originally used [i] instead of selectedTypesNew[i]
					analyzeOfflineClicks(eventList);					    		
		    	}
	        }
	    }
	}
	
	class SelectClickEvent implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JMenuItem source = (JMenuItem)(e.getSource());
			int index = (int) source.getClientProperty("index");
			OfflineEventDataUnit eventList = offlineDataBlock.getDataUnit(index, PamDataBlock.REFERENCE_ABSOLUTE  );
			analyzeOfflineClicks(eventList);			
		}
	}


	class SetParameters implements ActionListener {

		Frame parentFrame;

		public SetParameters(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			RoccaParameters newParams = RoccaParametersDialog.showDialog
                    (parentFrame, roccaParameters, roccaControl);
			/*
			 * The dialog returns null if the cancel button was set. If it's
			 * not null, then clone the parameters onto the main parameters reference
			 * and call prepareProcess to make sure they get used !
			 */
			if (newParams != null) {
				
				// check first to see if any of the source configuration has changed.  If everything
				// is still the same, then set a flag so that we DON'T call prepareProcess.  Calling
				// prepareProcess will modify the Observers list, and if this happens while the
				// system is running (and detecting Clicks, or some other intensive activity) then
				// Pamguard could crash.
		        // serialVersionUID = 20 2015/05/20
				boolean sourceHasChanged = true;
				if (
						roccaParameters.weAreUsingClick()==newParams.weAreUsingClick() &&
						roccaParameters.weAreUsingFFT()==newParams.weAreUsingFFT() &&
						roccaParameters.weAreUsingWMD()==newParams.weAreUsingWMD() &&
						roccaParameters.getClickDataSource()==newParams.getClickDataSource() &&
						roccaParameters.getClickNoiseDataSource()==newParams.getClickNoiseDataSource() &&
						roccaParameters.getWmDataSource()==newParams.getWmDataSource() &&
						roccaParameters.getClickTypeList()==newParams.getClickTypeList()
						) {
					sourceHasChanged = false;					
				}
				
				// now clone the parameters
				roccaParameters = newParams.clone();

                // if the classifier model isn't loaded, load it now...
                if (!roccaProcess.isClassifierLoaded()) {
                    roccaProcess.setClassifierLoaded(
                            roccaProcess.roccaClassifier.setUpClassifier());
                }

                // IF the source has been modified, run prepareProcess
		        // serialVersionUID = 20 2015/05/20  
                if (sourceHasChanged) {
                	roccaProcess.prepareProcess();
                }
		        
		        // update the Encounter ID and Known Species ID in the Sidebar
		        // serialVersionUID = 20 2015/05/20
		        roccaSidePanel.setNewEncounterID();
		        roccaSidePanel.setNewSpeciesID();
			}
		}
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		
		/*
		 * This gets called every time a new module is added - make sure
		 * that the RoccaProcess get's a chance to look around and see
		 * if there is data it wants to subscribe to. 
		 */
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			initialisationComplete = true;
			break;
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
		case PamControllerInterface.REMOVE_CONTROLLEDUNIT:
		case PamControllerInterface.ADD_DATABLOCK:
		case PamControllerInterface.REMOVE_DATABLOCK:
			if (initialisationComplete) {
				roccaProcess.prepareProcess();
			}
		}
	}

// Serializable methods
    @Override
	public Serializable getSettingsReference() {
        return roccaParameters;
    }

    @Override
	public long getSettingsVersion() {
		return RoccaParameters.serialVersionUID;
    }

    @Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		this.roccaParameters = ((RoccaParameters) pamControlledUnitSettings
				.getSettings()).clone();
 		return true;
    }


}
