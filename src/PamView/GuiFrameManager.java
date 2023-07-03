package PamView;

import java.awt.Container;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLayer;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import PamController.NewModuleDialog;
import PamController.PAMControllerGUI;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamModel.PamModel;
import PamModel.PamModuleInfo;
import PamView.panel.PamTabbedPane;
import pamViewFX.pamTask.PamTaskUpdate;

/**
 * The GuiFrameManager manages multiple PamGui frames, each 
 * of which may contain multiple tabs of module information 
 * and a side panel.
 * <p>
 * The user can move tabs between the various frames 
 * and create new frames by right 
 * clicking on the tabs themselves. 
 * @see PamGui
 *  
 * @author Doug Gillespie
 *
 */
public class GuiFrameManager implements PamSettings, PAMControllerGUI {


	private PamController pamController;
	
	private java.util.List<Integer> frameList = new ArrayList<Integer>();

	private ArrayList<PamViewInterface> pamViewList = new ArrayList<PamViewInterface>();
	
	private int maxFrameNumber = 0;

	private GuiFrameSettings guiFrameSettings;

	private Image frameIcon;

	public GuiFrameManager(PamController pamController) {
		super();
		this.pamController = pamController;
		PamSettingManager.getInstance().registerSettings(this);

		String iconLoc;
		switch (PamController.getInstance().getRunMode()){
		case(PamController.RUN_NETWORKRECEIVER):
			iconLoc="Resources/pamguardIconNR.png";
			break;
		case(PamController.RUN_PAMVIEW):
			iconLoc="Resources/pamguardIconV.png";
			break;
		case(PamController.RUN_MIXEDMODE):
			iconLoc="Resources/pamguardIconM.png";
			break;
		default:
			iconLoc="Resources/pamguardIcon.png";
		}

		frameIcon = new ImageIcon(ClassLoader.getSystemResource(iconLoc)).getImage();
	}

	public java.util.List<Integer> getFrameList() {
		return frameList;
	}
	
	/**
	 * Make a list of Gui frames - that is all 
	 * PamView's with a frame number > 0 which indicates
	 * they are a GUI frame. Other frames, such as the model
	 * view, have a frame number < 0. 
	 */
	public void makeFrameList() {
		frameList.clear();
		int f;
		for (int i = 0; i < pamViewList.size(); i++) {
			f = pamViewList.get(i).getFrameNumber();
			if (f >= 0) {
				frameList.add(f);
			}
		}
	}
	
	/**
	 * Get the total number of frames
	 * @return number of frames.
	 */
	public int getNumFrames() {
		return Math.max(1, frameList.size());
	}
	
	/**
	 * Get a frame
	 * @param iFrame frame number
	 * @return frame.
	 */
	public JFrame getFrame(int iFrame) {
		if (iFrame >= pamViewList.size()) {
			return null;
		}
		return pamViewList.get(iFrame).getGuiFrame();
	}
		
	/**
	 * See if a frame exists in the current list. If it doens't, 
	 * then create it. 
	 * @param frameNumber Frame Number 
	 */
	private PamViewInterface createFrame(int frameNumber) {
		PamViewInterface frame;
		if ((frame = findFrame(frameNumber)) != null) {
			return frame;
		}
		PamGui newGui = new PamGui(pamController, pamController.getModelInterface(), frameNumber);
		addView(newGui);
		sortFrameTitles();
		return newGui;
	}
	
	/**
	 * Closes a PamGui FRame (not the main one)
	 * All tabs are first moved to the main frame (No. 0)
	 * then the frame is removed from the list of frames
	 * then the frame is closed. 
	 * @param pamGui reference to frame to close. 
	 */
	public void closeExtraFrame(PamGui pamGui) {
		int nUnits = pamController.getNumControlledUnits();
		int iFrame;
		PamControlledUnit pamControlledUnit;
		for (int i = 0; i < nUnits; i++) {
			pamControlledUnit = pamController.getControlledUnit(i);
			iFrame = pamControlledUnit.getFrameNumber();
			if (iFrame == pamGui.getFrameNumber()) {
				moveUnit(pamControlledUnit, pamGui.getFrameNumber(), 0);
			}
		}
		pamGui.frame.dispose();
		removeView(pamGui);
		sortFrameTitles();
	}
	
	/**
	 * Goes through all open frames before they start closing
	 * and gets their parameters into the structure
	 * which will be written to the psf file. 
	 */
	public void getAllFrameParameters(){
		PamViewInterface view;
		for (int i = 0; i < pamViewList.size(); i++) {
			view = pamViewList.get(i);
			if (view.getFrameNumber() >= 0) {
				((PamGui) view).getGuiParameters();
			}
		}
	}

	/**
	 * See if a real frame exists. 
	 * @param iFrame
	 * @return true if a frame already exists. 
	 */
	private boolean frameExists(int iFrame) {
		return (findFrame(iFrame) != null);
	}
	
	/**
	 * Find the first free frame number that's not in the frame list
	 * @return an integer.
	 */
	private int firstFreeFrameNumber() {
		makeFrameList();
		int iFrame = 0;
		while (true) {
			if (frameExists(iFrame) == false) {
				return iFrame;
			}
			iFrame++;
		}
	}
	
	/**
	 * Get a frame GUI. This is a single frame whihc holds a GUI with tabs etc. 
	 * @param iFram e- the frame index to get. 
	 * @return the frame GUI to get.
	 */
	public PamGui getFrameGui(int iFrame) {
		if (iFrame >= pamViewList.size()) {
			return null;
		}
		return (PamGui) pamViewList.get(iFrame);
	}
	
	private PamViewInterface findFrame(int iFrame) {
		PamViewInterface v;
		for (int i = 0; i < pamViewList.size(); i++) {
			if ((v = pamViewList.get(i)).getFrameNumber() == iFrame) {
				return v;
			}
		}
		return null;
	}
	
	private boolean haveFrameInList(int iFrame) {
		if (frameList == null) {
			return false;
		}
		for (int i = 0; i < frameList.size(); i++) {
			if (frameList.get(i) == iFrame) {
				return true;
			}
		}
		return false;
		
	}
	
	
	private void moveUnit(PamControlledUnit pcu, int currentFrame, int destinationFrame) {
		/*
		 * First see if the destination frame exists, if not create it.  
		 */
		PamGui newGui = (PamGui) createFrame(destinationFrame);
		
		/*
		 * Then remove the unit from it's current frame
		 */
		PamGui currentGui = (PamGui) pcu.getPamView();
		if (currentGui != null) {
			currentGui.removeControlledUnit(pcu);
//			currentGui.ShowTabSpecificSettings();
		}
		
		/*
		 * Then give it a new frame index.
		 */
		pcu.setFrameNumber(destinationFrame);
		
		/*
		 * Then tell the new frame to add it. 
		 */
		newGui.addControlledUnit(pcu);
	}
	
	public JPopupMenu getTabPopupMenu(PamGui pamGui, int tabIndex) {
		JPopupMenu pm = new JPopupMenu();
		JMenuItem menuItem;
		makeFrameList();
		PamTabbedPane ptp = pamGui.getMainTab();
		String title = ptp.getTitleAt(tabIndex);
		PamControlledUnit pcu = pamGui.findControlledUnit(tabIndex);
		ClipboardCopier clipboardCopier;
		for (int i = 0; i < frameList.size(); i++) {
			if (frameList.get(i) == pamGui.getFrameNumber()) {
				continue;
			}
			menuItem = new JMenuItem(String.format("Move %s to %s", title, getMenuFrameName(frameList.get(i))));
			menuItem.addActionListener(new TabMover(pcu, pamGui.getFrameNumber(), frameList.get(i)));
			pm.add(menuItem);
		}
		menuItem = new JMenuItem(String.format("Move %s to a new frame", title));
		menuItem.addActionListener(new TabMover(pcu, pamGui.getFrameNumber(), firstFreeFrameNumber()));
		pm.add(menuItem);			
		clipboardCopier = pcu.getTabClipCopier();
		if (clipboardCopier != null) {
			pm.addSeparator();
			pm.add(clipboardCopier.getCopyMenuItem("Copy tab content to clipboard"));
		}
		return pm;
	}
	
	public String getMenuFrameName(int iFrame) {
		if (iFrame == 0) {
			return "the main frame";
		}
		else {
			return String.format("frame %d", iFrame);
		}
	}
	
	/**
	 * Get a name for a frame which can be used in their titles
	 * and in menus, etc. 
	 * @param iFrame frame Number
	 * @return names String
	 */
	public String getFrameName(int iFrame) {
		if (iFrame == 0) {
			return "Main Frame";
		}
		else {
			return String.format("Frame %d", iFrame);
		}
	}

	public void sortFrameTitles() {
		PamViewInterface view;
		makeFrameList();
		int nFrames = frameList.size();
		int frameNo;
		for (int i = 0; i < pamViewList.size(); i++) {
			view = pamViewList.get(i);
			frameNo = view.getFrameNumber();
			if (frameNo < 0) {
				continue;
			}
			view.setTitle(getFrameTitle(frameNo, nFrames));
		}
	}
	
	private String getFrameTitle(int frameNo, int nFrames) {
		String tit = "PAMGUARD";
		switch(pamController.getRunMode()) {
		case PamController.RUN_NORMAL:
			tit = "PAMGUARD";
			break;
		case PamController.RUN_NETWORKRECEIVER:
			tit = "PAMGUARD Network Receiver";
			break;
		case PamController.RUN_MIXEDMODE:
			tit = "PAMGUARD Mixed Mode";
			break;
		case PamController.RUN_PAMVIEW:
			tit = "PAMGUARD Viewer";
			break;
		case PamController.RUN_REMOTE:
			tit = "PAMGUARD Remote";
			break;
		}
		if (nFrames <= 1) {
//			tit = "PAMGUARD";
		}
		else if (frameNo == 0) {
			tit += " Main";
		}
		else {
			tit = String.format("%s Frame %d", tit, frameNo);
		}
		// now append the name of the psf or database.
		String ctrlName = pamController.getPSFName();
		if (ctrlName != null) {
			tit += " - " + ctrlName;
		}
		return tit;
	}
	
	class TabMover implements ActionListener {

		PamControlledUnit pamControlledUnit;
		int currentFrame;
		int destinationFrame;
		
		public TabMover(PamControlledUnit pamControlledUnit, int currentFrame, 
				int destinationFrame) {
			super();
			this.pamControlledUnit = pamControlledUnit;
			this.currentFrame = currentFrame;
			this.destinationFrame = destinationFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			moveUnit(pamControlledUnit, currentFrame, destinationFrame);
		}
		
	}

	public void addControlledUnit(PamControlledUnit controlledUnit) {
		for (int i = 0; i < pamViewList.size(); i++) {
			pamViewList.get(i).addControlledUnit(controlledUnit);
		}
	}

	@Override
	public void removeControlledUnit(PamControlledUnit controlledUnit) {
		for (int i = 0; i < pamViewList.size(); i++) {
			pamViewList.get(i).removeControlledUnit(controlledUnit);
		}
	}

	public void addView(PamViewInterface newView) {
		if (pamViewList == null) {
			pamViewList = new ArrayList<PamViewInterface>();
		}
		pamViewList.add(newView);
	}
	
	public void removeView(PamViewInterface oldView) {
		if (pamViewList == null) {
			pamViewList = new ArrayList<PamViewInterface>();
		}
		pamViewList.remove(oldView);
	}

	public void showControlledUnit(PamControlledUnit unit) {
		for (int i = 0; i < pamViewList.size(); i++) {
			pamViewList.get(i).showControlledUnit(unit);
		}
	}
	
	public void pamStart() {
		for (int i = 0; i < pamViewList.size(); i++) {
			pamViewList.get(i).pamStarted();
		}
	}
	
	public void pamStop() {
		for (int i = 0; i < pamViewList.size(); i++) {
			pamViewList.get(i).pamEnded();
		}
	}

	public void notifyModelChanged(int changeType) {
		for (int i = 0; i < pamViewList.size(); i++) {
			pamViewList.get(i).modelChanged(changeType);
		}
		if (changeType == PamController.INITIALIZATION_COMPLETE) {
			intialiseFrames();
		}
		if (changeType==PamController.ADD_CONTROLLEDUNIT) {
			if (this.getFrame(0)!=null)	this.getFrame(0).revalidate();
		}
		if (changeType==PamControllerInterface.REMOVE_CONTROLLEDUNIT) {
			if (this.getFrame(0)!=null)	 this.getFrame(0).revalidate();
		}
	}

	/**
	 * Called at start up to move various controlled unit panels to different frames of choice. 
	 */
	private void intialiseFrames() {
		if (guiFrameSettings == null) {
			return;
		}
		int nUnits = pamController.getNumControlledUnits();
		PamControlledUnit pamControlledUnit;
		for (int i = 0; i < nUnits; i++) {
			pamControlledUnit = pamController.getControlledUnit(i);
			int iFrame = guiFrameSettings.getUnitFrameNumber(pamControlledUnit);
			if (iFrame > 0) {
				moveUnit(pamControlledUnit, 0, iFrame);
			}
		}
		
	}

	public void destroyModel() {
		for (int i = 0; i < pamViewList.size(); i++) {
			pamViewList.get(i).modelChanged(PamControllerInterface.DESTROY_EVERYTHING);
		}
		pamViewList = null;
	}


	@Override
	public Serializable getSettingsReference() {
		GuiFrameSettings guiFrameSettings = new GuiFrameSettings();
		int nUnits = pamController.getNumControlledUnits();
		PamControlledUnit pamControlledUnit;
		for (int i = 0; i < nUnits; i++) {
			pamControlledUnit = pamController.getControlledUnit(i);
			guiFrameSettings.addUnitInfo(pamControlledUnit);
		}
		return guiFrameSettings;
	}

	@Override
	public long getSettingsVersion() {
		return GuiFrameSettings.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		return "GUI Frame Settings";
	}

	@Override
	public String getUnitType() {
		return "GUI Frame Settings";
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		this.guiFrameSettings = ((GuiFrameSettings)(pamControlledUnitSettings.getSettings())).clone();
		return true;
	}

	public void enableGUIControl(boolean enable) {
		for (int i = 0; i < pamViewList.size(); i++) {
//			pamViewList.get(i).enableGUIControl(enable);
			Container component = pamViewList.get(i).getGuiFrame().getContentPane();
			if (JLayer.class.isAssignableFrom(component.getClass())) {
				JLayer layer = (JLayer) component;
			}
			pamViewList.get(i).getGuiFrame().setEnabled(enable);
		}		
	}

	public JMenuItem getCollapseMenuItem() {
		JMenuItem mi = new JMenuItem("Collapse multi-frames");
		mi.setToolTipText("If using multiple frames, collapse all secondary frames into the main display");
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				collapseAllFrames();
			}
		});
		int nFrames = countGuiFrames();
		mi.setEnabled(nFrames > 1);
		return mi;
	}
	
	private int countGuiFrames() {
		int n = 0;
		for (PamViewInterface pv:pamViewList) {
			if (PamGui.class.isAssignableFrom(pv.getClass())) {
				n++;
			}
		}
		return n;
	}

	protected void collapseAllFrames() {
		int nView = pamViewList.size();
		for (int iV = nView-1; iV > 0; iV--) {
			PamViewInterface pv = pamViewList.get(iV);
			if (PamGui.class.isAssignableFrom(pv.getClass())) {
				PamGui pamGui = (PamGui) pv;
				if (pamGui.getFrameNumber() > 0) {
					closeExtraFrame(pamGui);
				}
			}
		}
	}

	@Override
	public void notifyLoadProgress(PamTaskUpdate progress) {
		// TODO Auto-generated method stub
	}

	@Override
	public PamSettings getInitialSettings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasCallBack() {
		return false;
	}

	@Override
	public PamViewInterface initPrimaryView(PamController pamController, PamModel pamModelInterface ) {
		return new PamGui(pamController, pamModelInterface, 0);
	}

	@Override
	public String getModuleName(Object parentFrame, PamModuleInfo moduleInfo) {
		return 	NewModuleDialog.showDialog(parentFrame==null ? null : (Frame) parentFrame ,moduleInfo,null);
	}

	@Override
	public void pamStarted() {
		pamStart();
	}

	@Override
	public void pamEnded() {
		pamStop();
	}

	@Override
	public void modelChanged(int changeType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getFrameNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public JFrame getGuiFrame() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDefaultFrameIcon(Image frameIcon) {
		if (frameIcon != null) {
			this.frameIcon = frameIcon;
			for (PamViewInterface vi : pamViewList) {
				JFrame frame = vi.getGuiFrame();
				if (frame != null) {
					frame.setIconImage(frameIcon);
				}
			}
		}		
	}
	
	public Image getDefaultFrameIcon() {
		return frameIcon;
	}

	/**
	 * Function that can move GUI frames back onto the main window. 
	 * Can be used to recover a GUI if it's on a monitor that is not present. 
	 */
	public void findGUI() {
		if (pamViewList == null) {
			return;
		}
		int loc = 10;
		for (PamViewInterface view : pamViewList) {
			JFrame frame = view.getGuiFrame();
			if (frame == null) {
				continue;
			}
			frame.setLocation(loc, loc);
			frame.setState(JFrame.NORMAL);
			frame.setVisible(true);
			loc += 20;
		}
	}


}
