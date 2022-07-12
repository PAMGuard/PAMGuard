package clickDetector;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import clickDetector.offlineFuncs.OfflineEventDataUnit;

import Layout.PamInternalFrame;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamView.MenuItemEnabler;

public class ClickDisplayManager implements PamSettings {

	static public final int NO_MAX_COUNT = -1;

	int totalCount = 0;

	//private CreateMenuAction createMenuAction;

	private ClickTabPanelControl clickTabPanelControl;

	ClickDisplayManager clickDisplayManager;

	protected ArrayList<ClickDisplayInfo> displayInfoList = new ArrayList<ClickDisplayInfo>();

	transient protected ArrayList<ClickDisplay> windowList = new ArrayList<ClickDisplay>();

	ClickDisplayManagerParameters2 cdmp = null; 

	private boolean bAutoScroll = true;

	private ClickControl clickControl;

	public ClickDisplayManager(ClickControl clickControl, ClickTabPanelControl clickTabPanelControl) {
		this.clickControl = clickControl;
		this.clickTabPanelControl = clickTabPanelControl;
		clickDisplayManager = this;
		cdmp = new ClickDisplayManagerParameters2();
		PamSettingManager.getInstance().registerSettings(this);
		registerDisplay("clickDetector.ClickBTDisplay", "Time display", ClickDisplayManager.NO_MAX_COUNT, false);
		registerDisplay("clickDetector.ClickWaveform", "Waveform", 1, true);
		registerDisplay("clickDetector.ClickSpectrum", "Spectrum", 1, true);
		registerDisplay("clickDetector.ClickTrigger", "Triggers", 1, true);
		registerDisplay("clickDetector.WignerPlot", "Wigner Plot", 1, true);
		registerDisplay("clickDetector.IDI_Display", "IDI Histogram", 1, true);
		//Displays that are only available in offline mode.
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			registerDisplay("clickDetector.ConcatenatedSpectrogram", "Concatenated Spectrum", 1, true);	
		}
	}

	public ClickDisplayInfo registerDisplay(String className, String name, int maxCount, boolean isSmall) {

		Class c = null;
		try {
			c = Class.forName(className);
			//			System.out.println(c.getName());
		}
		catch (Exception Ex) {
			System.out.println("Can't find class " + className);
			return null;
		}
		Class stringClass = String.class;
		Class[] classlist = new Class[1];
		classlist[0] = stringClass;
		// check that it's a subclass of PamFramePlots
		Class superClass = c;
		while(true) {
			superClass = superClass.getSuperclass();
			if (superClass == null) return null;
			if (superClass.getSimpleName().equals("PamFramePlots")) {
				break;
			}
		}
		ClickDisplayInfo displayInfo = new ClickDisplayInfo(className, name, maxCount, c, isSmall);
		displayInfoList.add(displayInfo);
		return displayInfo;
	}

	class ClickDisplayInfo implements ComponentListener, ContainerListener, InternalFrameListener, Serializable, ManagedParameters {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4162942109694938380L;

		private int openCount = 0;

		private String name;

		private String className;

		private int maxCount = 1;

		private Class displayClass;

		private boolean smallWindow;

		transient private MenuItemEnabler menuItemEnabler;

		transient CreateMenuAction createMenuAction;

		transient ClickDisplayInfo clickDisplayInfo;

		private ClickDisplayInfo(String className, String name, int maxCount, Class displayClass, boolean isSmall) {
			clickDisplayInfo = this;
			this.className = className;
			this.name = name;
			this.maxCount = maxCount;
			this.displayClass = displayClass;
			this.smallWindow = isSmall;
			menuItemEnabler = new MenuItemEnabler();
			createMenuAction = new CreateMenuAction();

		}

		class CreateMenuAction implements ActionListener {

			public void actionPerformed(ActionEvent e) {

				clickDisplayInfo.createUnit();
				clickTabPanelControl.getClickPanel().arrangeWindows();
			}
		}

		public void createUnit() {
			ClickDisplay newClickDisplay;
			Class[] paramList = new Class[3];
			paramList[0] = clickTabPanelControl.clickControl.getClass(); 
			paramList[1] = ClickDisplayManager.class;
			paramList[2] = clickDisplayInfo.getClass();
			try {
				Constructor constructor = displayClass.getConstructor(paramList);
				newClickDisplay = (ClickDisplay) constructor.newInstance(clickTabPanelControl.clickControl, 
						clickDisplayManager, clickDisplayInfo);
			}
			catch (Exception Ex) {
				Ex.printStackTrace();
				return;
			}
			// if it gets here, the newFrame plot should exist ...
			PamInternalFrame pf;

			pf = new PamInternalFrame(newClickDisplay, false);

			if (!System.getProperty("os.name").equals("Linux")) {
				newClickDisplay.getFrame().setFrameIcon(new ImageIcon(ClassLoader
						.getSystemResource("Resources/pamguardIcon.png")));
			}

			newClickDisplay.getFrame().setClosable(true);
			newClickDisplay.getFrame().setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			newClickDisplay.getFrame().addComponentListener(clickDisplayInfo);
			newClickDisplay.getFrame().addContainerListener(clickDisplayInfo);
			newClickDisplay.getFrame().addInternalFrameListener(clickDisplayInfo);
			//			newClickDisplay.getFrame().addContainerListener(this);
			//			newClickDisplay.getFrame().addInternalFrameListener(this);
			clickTabPanelControl.clickPanel.add(pf);
			clickTabPanelControl.clickPanel.setPosition(pf, 0);
			newClickDisplay.created();
			openUnit(newClickDisplay);
		}

		public boolean isSmallWindow() {
			return smallWindow;
		}

		public void setSmallWindow(boolean smallWindow) {
			this.smallWindow = smallWindow;
		}

		void openUnit(ClickDisplay newDisplay) {
			openCount++;
			totalCount++;
			windowList.add(newDisplay);
			enableMenu();
		}

		void closeUnit(Object source) {
			openCount--;
			totalCount--;
			windowList.remove(findDisplay(source));
			enableMenu();
		}

		public int getMaxCount() {
			return maxCount;
		}

		public String getName() {
			return name;
		}

		public int getOpenCount() {
			return openCount;
		}

		private void enableMenu()
		{
			menuItemEnabler.enableItems(openCount < maxCount || maxCount == NO_MAX_COUNT);
		}

		@Override
		public String toString() {
			return getName();
		}

		public void componentHidden(ComponentEvent e) {
			//			System.out.println("componentHidden");
		}

		public void componentMoved(ComponentEvent e) {
			//System.out.println("componentMoved");
		}

		public void componentResized(ComponentEvent e) {
			//System.out.println("componentResized");
		}

		public void componentShown(ComponentEvent e) {
			//System.out.println("componentShown");
		}

		public void componentAdded(ContainerEvent e) {
			// TODO Auto-generated method stub
			//			System.out.println("componentAdded");

		}

		public void componentRemoved(ContainerEvent e) {
			// TODO Auto-generated method stub
			//			System.out.println("componentRemoved");

		}

		public void internalFrameActivated(InternalFrameEvent frameEvent) {
			JInternalFrame internalFrame = frameEvent.getInternalFrame();
			ClickDisplay clickDisplay = (ClickDisplay) ((PamInternalFrame) internalFrame).getFramePlots();

			//			System.out.println("internalFrameActivated : " + clickDisplay.getName());
			clickControl.displayActivated(clickDisplay);
		}

		public void internalFrameClosed(InternalFrameEvent e) {
			// TODO Auto-generated method stub
			//			System.out.println("internalFrameClosed");
			closeUnit(e.getSource());

		}

		public void internalFrameClosing(InternalFrameEvent arg0) {
			// TODO Auto-generated method stub
			//			System.out.println("internalFrameClosing");

		}

		public void internalFrameDeactivated(InternalFrameEvent arg0) {
			// TODO Auto-generated method stub
			//			System.out.println("internalFrameDeactivated");

		}

		public void internalFrameDeiconified(InternalFrameEvent arg0) {
			// TODO Auto-generated method stub
			//			System.out.println("internalFrameDeiconified");

		}

		public void internalFrameIconified(InternalFrameEvent arg0) {
			// TODO Auto-generated method stub
			//			System.out.println("internalFrameIconified");

		}

		public void internalFrameOpened(InternalFrameEvent arg0) {
			// TODO Auto-generated method stub
			//			System.out.println("internalFrameOpened");

		}
		
		@Override
		public PamParameterSet getParameterSet() {
			PamParameterSet ps = PamParameterSet.autoGenerate(this);
			try {
				Field field = this.getClass().getDeclaredField("className");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						return className;
					}
				});
			} catch (NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
			try {
				Field field = this.getClass().getDeclaredField("displayClass");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						return displayClass;
					}
				});
			} catch (NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
			return ps;
		}

	}

	public ClickDisplay findDisplay(Object displayFrame) {
		for (int i = 0; i < windowList.size(); i++) {
			if (windowList.get(i).getFrame() == displayFrame) return windowList.get(i);
		}
		return null;
	}

	public JMenu getModulesMenu() {
		JMenu modulesMenu = new JMenu("Add Display ...");
		JMenuItem menuItem;
		for (int i = 0; i < displayInfoList.size(); i++) {
			menuItem = new JMenuItem(displayInfoList.get(i).toString());
			menuItem.addActionListener(displayInfoList.get(i).createMenuAction);
			modulesMenu.add(menuItem);
			displayInfoList.get(i).menuItemEnabler.addMenuItem(menuItem);
		}
		return modulesMenu;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void createDisplays() {
		int runMode = PamController.getInstance().getRunMode();
		// create the number of windows specified in cdmp.
		for (int i = 0; i < Math.max(1, cdmp.getnBTDisplays()); i++) {
			displayInfoList.get(0).createUnit();
		}
		for (int i = 0; i < Math.max(1, cdmp.getnWaveDisplays()); i++) {
			displayInfoList.get(1).createUnit();
		}
		for (int i = 0; i < Math.max(1, cdmp.getnSpectrumDisplays()); i++) {
			displayInfoList.get(2).createUnit();
		}
		//		if (runMode == PamController.RUN_MIXEDMODE || runMode == PamController.RUN_NORMAL) {
		for (int i = 0; i < cdmp.getnTriggerDisplays(); i++) {
			displayInfoList.get(3).createUnit();
		}
		for (int i = 0; i < cdmp.getnWignerDisplays(); i++) {
			displayInfoList.get(4).createUnit();
		}
		for (int i = 0; i < cdmp.getnIDIHistogramDisplays(); i++) {
			displayInfoList.get(5).createUnit();
		}
		for (int i = 0; i < cdmp.getnConcatSpecDisplays(); i++) {
			displayInfoList.get(6).createUnit();
		}

		//		}
		//		else {
		//			displayInfoList.get(4).createUnit();
		//		}

		// activate the first bt display
		if (findFirstBTDisplay() != null) {
			clickControl.displayActivated(findFirstBTDisplay());
		}
	}
	public void createStandardDisplay() {
		// make one of everything ...
		for (int i = 0; i < displayInfoList.size(); i++) {
			displayInfoList.get(i).createUnit();
		}
	}

	public ArrayList<ClickDisplayInfo> getDisplayInfoList() {
		return displayInfoList;
	}

	public ArrayList<ClickDisplay> getWindowList() {
		return windowList;
	}

	public Serializable getSettingsReference() {
		cdmp.countEverything(this);
		return cdmp;
	}

	public int countDisplays(Class displayType) {
		int count = 0;
		ArrayList<ClickDisplay> dw = getWindowList();
		for (int i = 0; i < dw.size(); i++) {
			if (dw.get(i).clickDisplayInfo.displayClass == displayType) {
				count++;
			}
		}
		return count;
	}

	public long getSettingsVersion() {
		return ClickDisplayManagerParameters2.serialVersionUID;
	}

	public String getUnitName() {
		return clickTabPanelControl.clickControl.getUnitName() + "_Click Display Manager";
	}

	public String getUnitType() {
		return "Click Display Manager";
	}

	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		try {
			cdmp = ((ClickDisplayManagerParameters2) pamControlledUnitSettings.getSettings()).clone();
		} catch (ClassCastException e) {
			System.out.println("Reset click display windows");
			//			System.out.println(e.getMessage());
		}
		return true;
	}

	public boolean isBAutoScroll() {
		return bAutoScroll;
	}

	public void setBAutoScroll(boolean autoScroll) {
		bAutoScroll = autoScroll;
	}

	protected void clickedOnClick(ClickDetection click) {
		if (click == null) {
			return;
		}
		ArrayList<ClickDisplay> dw = getWindowList();
		for (int i = 0; i < dw.size(); i++) {
			dw.get(i).clickedOnClick(click);
		}
	}

	/**
	 * Scroll the time displays to a specific event. 
	 * @param event
	 * @param beforeTime 
	 */
	public void gotoEvent(OfflineEventDataUnit event, int beforeTime) {
		for (int i = 0; i < windowList.size(); i++) {
			if (windowList.get(i).getClass() == ClickBTDisplay.class) {
				((ClickBTDisplay)windowList.get(i)).gotoEvent(event, beforeTime);
			}
		}
	}

	public void playViewerData() {
		ClickBTDisplay btDisplay = findFirstBTDisplay();
		if (btDisplay != null) {
			btDisplay.playViewerData();
		}
	}

	public ClickBTDisplay findFirstBTDisplay() {
		for (int i = 0; i < windowList.size(); i++) {
			if (windowList.get(i).getClass() == ClickBTDisplay.class) {
				return (ClickBTDisplay) windowList.get(i);
			}
		}
		return null;
	}

	public void playClicks() {
		ClickBTDisplay btDisplay = findFirstBTDisplay();
		if (btDisplay != null) {
			btDisplay.playClicks();
		}
	}

	/**
	 * Set the default amplitude ranges for the BT display depending on the global 
	 * medium. 
	 */
	public void setDefaultAmplitudeScales() {
		// the default amplitude is dependent on the hydrophone sensitivity and clip level. 
		double[] amplitudeRanges = PamController.getInstance().getGlobalMediumManager().getDefaultAmplitudeScales(); 
		
		
		ClickBTDisplay btDisplay; 
		for (int i = 0; i < windowList.size(); i++) {
			if (windowList.get(i).getClass() == ClickBTDisplay.class) {
//				System.out.println("ClickDisplayManager: Set the click amplitudes: " + amplitudeRanges[0] + " "+ amplitudeRanges[1]); 
				btDisplay = ((ClickBTDisplay) windowList.get(i));

				btDisplay.getBtDisplayParameters().amplitudeRange=amplitudeRanges;
				
				//nasty piece of code to get the amplitude axis to change...
				if (btDisplay.getVScaleManager() != null) {
					btDisplay.getVScaleManager().setSelected();
				}
				btDisplay.getBtAxis().makeAxis();
				btDisplay.repaintBoth();
			}
		}
	}


}
