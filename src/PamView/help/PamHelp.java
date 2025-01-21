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
package PamView.help;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.help.DefaultHelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.JHelpContentViewer;
import javax.help.JHelpIndexNavigator;
import javax.help.JHelpSearchNavigator;
import javax.help.JHelpTOCNavigator;
import javax.help.Map;
import javax.help.SwingHelpUtilities;
import javax.help.Map.ID;
import javax.help.event.HelpModelEvent;
import javax.help.event.HelpModelListener;
import javax.help.plaf.basic.BasicTOCNavigatorUI;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

//import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

import Acquisition.DaqSystemInterface;
import PamController.PamController;
import PamController.PamGUIManager;
import PamModel.PamModel;
import PamModel.PamPluginInterface;
import PamModel.SMRUEnable;
import PamView.GuiFrameManager;

/**
 * @author David McLaren, Paul Redmond Doug Gillespie
 * 
 * 
 */
public class PamHelp {

	private ArrayList<Component> componentList;
	private HelpSet hs;
	private JHelpContentViewer viewer;
	private JHelpSearchNavigator navSearch;
	private JHelpTOCNavigator navTOC;
	private JHelpIndexNavigator navIndex;
	private String currentID;
	private JButton backButton, forwardButton;
	private DefaultHelpBroker defHb;
	private BasicTOCNavigatorUI tocNav;
	private int idIndex = 0;
	private ArrayList<String> pageHistory = new ArrayList<String>();
	private boolean registerIdChange = true;
	
	/**
	 * Holds main content pane on right and 
	 * tabbed pane (help_pane) on left. 
	 */
	private JSplitPane splitPane;
	/**
	 * Holder for toc, index and search tabs
	 */
	private JTabbedPane help_pane;
	/**
	 * Main frame
	 */
	private JFrame helpFrame;
	private JPanel search, index, toc;
	private JPanel buttonBar;
	
	private final String defaultId = "overview.PamMasterHelp.docs.pamguardBackground";	
	private final String defaultViewerId = "overview.PamMasterHelp.docs.viewerMode";
	private final String defaultMixedId = "overview.PamMasterHelp.docs.mixedMode";
	
	private String helpLocURL;
	
	private String getDefaultId() {
		PamController pc = PamController.getInstance();
		if (pc == null) {
			return defaultId;
		}
		int mode = PamController.getInstance().getRunMode();
		switch(mode) {
		case PamController.RUN_MIXEDMODE:
			return defaultMixedId;
		case PamController.RUN_PAMVIEW:
			return defaultViewerId;
		default:
			return defaultId;
		}
	}
	
	static private PamHelp singleInstance;

	private PamHelp() {

		UIDefaults table = UIManager.getDefaults();
		Object[] uiDefaults = {
				"HelpContentViewerUI", "PamView.help.PamHelpContentViewerUI"
		};
		table.putDefaults(uiDefaults);
		
		SwingHelpUtilities.setContentViewerUI("PamView.help.PamHelpContentViewerUI");

		buildMasterHelpset();
		pageHistory.add(getDefaultId());
		checkButtonStates();
	}
	
	/**
	 * 
	 */
	private void checkButtonStates() {
		if((pageHistory.size()-idIndex)>1){
			backButton.setEnabled(true);
		}
		else{
			backButton.setEnabled(false);
		}
			
		if(idIndex>0){
			forwardButton.setEnabled(true);
		}
		else{
			forwardButton.setEnabled(false);
		}

	}


	/**
	 * Called from constructor to create the panels 
	 * for the help frame. 
	 */
	private void buildMasterHelpset() {
		createHelpSet();
		createFrameAndWindows();
		linkHelpToWindows();
		
		backButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				goBack(evt);
			}
			private void goBack(ActionEvent evt) {
				registerIdChange = false;
				idIndex++;
				setCurrentID(pageHistory.get(idIndex));	
				
				registerIdChange = true;
				checkButtonStates();
			}
		});
		
		forwardButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				goForward(evt);
			}
			private void goForward(ActionEvent evt) {
				registerIdChange = false;
				idIndex--;
				setCurrentID(pageHistory.get(idIndex));	
//				showHelp();
				registerIdChange = true;
				checkButtonStates();
			}
		});
		
	}
	
	
	/**
	 * Adds the right components to the main frame. 
	 */
	public void createFrameAndWindows(){
		helpFrame = new JFrame("PAMGUARD Help");
		helpFrame.setIconImage(new ImageIcon(ClassLoader
				.getSystemResource("Resources/redHelpIcon.png")).getImage());

		buttonBar = new JPanel();
		helpFrame.setSize(new Dimension(1000, 730));
		setFramePosition(helpFrame);
		
		helpFrame.setLayout(new BorderLayout());

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setDividerLocation(300);

		help_pane=new JTabbedPane(SwingConstants.TOP);

		search = new JPanel(new BorderLayout());
		index = new JPanel(new BorderLayout());
		toc = new JPanel(new BorderLayout());
		help_pane.addTab("Contents", toc);
		help_pane.addTab("Index", index);
		help_pane.addTab("Search", search);
		help_pane.setAutoscrolls(true);
		
		viewer = new JHelpContentViewer(hs);
//		viewer = new PamHelpContentViewer(hs);
		

		splitPane.add(help_pane);
		splitPane.add(viewer);
		
		helpFrame.add(buttonBar, BorderLayout.NORTH);	
		helpFrame.add(splitPane, BorderLayout.CENTER);
		
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		buttonBar.setLayout(flowLayout);

		buttonBar.add(backButton = new JButton("<"));
		buttonBar.add(forwardButton = new JButton(">"));
	}

	/**
	 * Place the help window just offset from the main PAMGuard window
	 * @param helpFrame helpframe object to position. 
	 * @return true if gui found and position obtained, otherwise false. 
	 */
	private boolean setFramePosition(JFrame helpFrame) {
		GuiFrameManager guiManager = PamController.getInstance().getGuiFrameManager();
		if (guiManager == null) {
			return false;
		}
		JFrame mainFrame = guiManager.getFrame(0);
		if (mainFrame == null) {
			return false;
		}
		Point framePos = mainFrame.getLocation();
		if (framePos == null) {
			return false;
		}
		helpFrame.setLocation(framePos.x+10, framePos.y+10);
		return true;
	}

	public void displayHelp(){
		
		helpFrame.setVisible(true);

		int state = helpFrame.getExtendedState();
		if ((state & Frame.ICONIFIED) != 0) {
			helpFrame.setExtendedState(Frame.NORMAL);
		}
		helpFrame.requestFocusInWindow();

	}
	
	public void displayContextSensitiveHelp(String helpTarget){
		
		displayHelp();
		
		setCurrentID(helpTarget);	
	}

	
	static public PamHelp getInstance() {
		if (singleInstance == null) {
			singleInstance = new PamHelp();
		}
		return singleInstance;
	}

//
//	public String getUnitName() {
//		return "Pam Help Manager";
//	}
//
//	public String getUnitType() {
//		return "Pam Help Manager";
//	}

	/**
	 * Create the master HelpSet object
	 * from the help/PAMGUARD.hs file. 
	 * <p>
	 * Then optionally merge in helpsets from
	 * other modules (since Feb 2009, this is little
	 * used with most help sets being combined into
	 * a single hs so that indexing and searching work and 
	 * so that a more sensible layout of modules within
	 * the TOC can be acheived. 
	 */
	public void createHelpSet(){
		String masterHelpFile = "help/PAMGUARD.hs";
		String mergedHelpFile = "";
		File tempDir = new File(".");
		//FileFinder findHelpFiles = new FileFinder();
	
		
		URL helpURL = ClassLoader.getSystemResource(masterHelpFile);
		if(helpURL == null){
			System.out.println("masterHelpFile URL is null:");
			return;
		} 		
		
		// save the helpset location for use by the PamHelpContentViewerUI class to direct plugin help files
		// trying to link to the master helpset
		String tempURL = helpURL.toString();
		int idx = tempURL.lastIndexOf("/");
		helpLocURL = tempURL.substring(0, idx+1);
		
		try {
			hs = new HelpSet(PamHelp.class.getClassLoader(), helpURL);
		} catch (HelpSetException e2) {
			e2.printStackTrace();
			System.out.println(e2.getLocalizedMessage());
		}
	
		
		// TODO: (PR) This is a 'workaround' fix to allow the helpsets to be loaded from the deployment JAR.
		// Its a hardcoded solution and needs correcting.
//		addHelpSet("radarDisplayHelp/radarDisplayHelp_.hs");
//		addHelpSet("mapHelp/Map Help.hs");
//		addHelpSet("userDisplayHelp/userDisplayHelp.hs");
//		addHelpSet("spectrogramDisplayHelp/spectrogramDisplayHelp.hs");
//		addHelpSet("whistleDetectorHelp/whistleDetector_.hs");
//		addHelpSet("fftManagerHelp/FFT Engine Help.hs");
//		addHelpSet("clickDetectorHelp/Click Detector Help.hs");
//		addHelpSet("hydrophoneArrayManagerHelp/hydrophoneArrayManager.hs");
//		addHelpSet("Pam3DHelp/Pam3DHelp.hs");
//		addHelpSet("AsioHelp/AsioHelp.hs");
//		addHelpSet("FiltersHelp/IIRF Filter Help.hs");
//		addHelpSet("decimatorHelp/DecimatorHelp.hs");
//		addHelpSet("generalDatabaseHelp/DatabaseHelp.hs");
//		addHelpSet("soundPlaybackHelp/SoundOutputHelp.hs");
//		addHelpSet("videoRangeHelp/VideoRangeHelp.hs");
		
		// get a list of plugins and add their helpsets
		List<PamPluginInterface> pluginList = ((PamModel) PamController.getInstance().getModelInterface()).getPluginList();
		
		// only continue if there were plugins
		if (!pluginList.isEmpty()) {
			for (int i=0; i<pluginList.size(); i++) {
				
				// if the plugin does not have a helpset, continue to the next plugin
				if (pluginList.get(i).getHelpSetName()==null) {
					continue;
				}
	
				// set up a URL to point to the helpset file inside the jar
				try {
			        //URL helpSetLoc = new URL("jar:file:\\C:\\Users\\SCANS\\workspace\\Pamguard_MikeBranch\\plugins\\TestPlugin.jar!/TestPlugin/PluginHelp.hs"); this one worked - passed argument should match
					String pluginJar = pluginList.get(i).getJarFile();
					URL helpSetLoc;
					if (pluginJar != null) {
						helpSetLoc = new URL("jar:file:" + File.separator + pluginList.get(i).getJarFile()+"!/"+pluginList.get(i).getHelpSetName());
						addPluginHelpSet(helpSetLoc);
					}
					else {
						// this used when developing plugins and they are not actually in a jar file yet, rather they
						// have just been manually added to the list of available plugins. 
						String hsN = pluginList.get(i).getHelpSetName();
						addHelpSet(hsN);
					}
				} catch (MalformedURLException e) {
					System.err.println("Error generating URL for help set " + pluginList.get(i).getHelpSetName());
					System.err.println(e.getMessage());
				}
			}
		}
		
		// get a list of external daq systems and add their helpsets
		List<DaqSystemInterface> daqList = ((PamModel) PamController.getInstance().getModelInterface()).getDaqList();
		
		// only continue if there were external daq systems
		if (!daqList.isEmpty()) {
			for (int i=0; i<daqList.size(); i++) {
				
				// if the plugin does not have a helpset, continue to the next plugin
				if (daqList.get(i).getHelpSetName()==null) {
					continue;
				}
	
				// set up a URL to point to the helpset file inside the jar
				try {
			        //URL helpSetLoc = new URL("jar:file:\\C:\\Users\\SCANS\\workspace\\Pamguard_MikeBranch\\plugins\\TestPlugin.jar!/TestPlugin/PluginHelp.hs"); this one worked - passed argument should match
					URL helpSetLoc = new URL("jar:file:" + File.separator + daqList.get(i).getJarFile()+"!/"+daqList.get(i).getHelpSetName());
					addPluginHelpSet(helpSetLoc);
				} catch (MalformedURLException e) {
					System.err.println("Error generating URL for help set " + daqList.get(i).getHelpSetName());
					System.err.println(e.getMessage());
				}
			}
		}
	}
	
	/**
	 * Add a plugin module helpset to the master help.  This method differs from addHelpSet in
	 * that it is not limited to searching for the help file on the search path used to load classes.
	 * 
	 * @param helpsetLoc URL path to the HelpSet file inside the jar
	 * @return true if successful.
	 * 
	 */
	private boolean addPluginHelpSet(URL helpSetLoc) {
		if (hs == null) {
			return false;
		}
		try {
			HelpSet dum = new HelpSet();
			dum.setTitle("Test Item");
			dum.add(new HelpSet(PamHelp.class.getClassLoader(), helpSetLoc));
			hs.add(dum);
		}catch (HelpSetException e) {
			System.err.println("Error loading help set " + helpSetLoc.toString());
			System.err.println(e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Add a module helpset to the master help
	 * @param helpsetName
	 * @return true if successful.
	 */
	private boolean addHelpSet(String helpsetName) {
		if (hs == null) {
			return false;
		}
		try {
			HelpSet dum = new HelpSet();
			dum.setTitle("Test Item");
			dum.add(new HelpSet(PamHelp.class.getClassLoader(),ClassLoader.getSystemResource(helpsetName)));
			hs.add(dum);
		}catch (HelpSetException e) {
			System.err.println("Error loading help set " + helpsetName);
			return false;
		}
		return true;
	}

	private boolean linkHelpToWindows() {

		setCurrentID(getDefaultId());
		
		MyHelpModelListener myListener = new MyHelpModelListener();
		viewer.addHelpModelListener(myListener);
		
		PamHelpContentViewerUI pcvui = new PamHelpContentViewerUI(viewer, this);
		viewer.setUI(pcvui);

		navTOC = (JHelpTOCNavigator)hs.getNavigatorView("TOC").createNavigator(viewer.getModel());
		navTOC.addHelpModelListener(myListener);

		tocNav = new BasicTOCNavigatorUI(navTOC);
		
		toc.add(navTOC,BorderLayout.CENTER);
		
		navIndex = (JHelpIndexNavigator)hs.getNavigatorView("Index").createNavigator(viewer.getModel());
		navIndex.addHelpModelListener(myListener);
		index.add(navIndex,BorderLayout.CENTER);

		navSearch = (JHelpSearchNavigator)hs.getNavigatorView("Search").createNavigator(viewer.getModel());	
		search.add(navSearch,BorderLayout.CENTER);
		
		defHb = new PamHelpBroker(hs);
		defHb.setHelpSet(hs);
				
		
		return true;
	}
	
	private boolean setCurrentID(String id) {
//		if (SMRUEnable.isDevEnable()) {
//			System.out.println("Opening help page: " + id);
//		}
		currentID = id;
		if (currentID != null && viewer != null) {
			try {
				viewer.setCurrentID(currentID);
			}
			catch (Exception ex) {
				//			viewer.
//				ex.printStackTrace();
				System.out.println("Can't find " + currentID + " in help file");
				if (!defaultId.equals(id)) {
					viewer.setCurrentID(defaultId);
				}
				return false;
			}
		}
		return true;
	}
	
	class MyHelpModelListener implements HelpModelListener {
		MyHelpModelListener(){
			
		}
        public void idChanged(HelpModelEvent e) {
        	ID id = e.getID();

        	/*
        	 * Always store any new page navigated to
        	 */
        	if(id != null && !id.getIDString().equals(pageHistory.get(0))){
        		pageHistory.add(0,id.getIDString());
        		/*
        		 * if registerIdChange is true, then 
        		 * the page was navigated to from one of the 
        		 * main help TOC / Index / Search pages, so it's
        		 * Definitely the last page we're visiting and
        		 * we not that we're at the end of the list.
        		 * Otherwise, if false, then we're navigating
        		 * using the back / forward buttons, so increase
        		 * the idIndex by one to keep it pointing at the
        		 * right place - note that new entries are added
        		 * at the start of the list.  
        		 */
        		if (registerIdChange) {
        			idIndex = 0;
        		}
        		else {
        			idIndex++;
        		}
        	}
			
        	checkButtonStates();
        }
    }

	
	
	public String getHelpLocURL() {
		return helpLocURL;
	}

	public void setHelpLocURL(String helpLocURL) {
		this.helpLocURL = helpLocURL;
	}

//	/**
//	 * Quick main function 
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		} catch (Exception e) { }
//		PamHelp.getInstance().displayHelp();
//	}


}
