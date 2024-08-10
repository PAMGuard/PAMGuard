package effortmonitor;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import PamView.PamSidePanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.debug.Debug;
import effortmonitor.swing.EffortDataMapGraph;
import effortmonitor.swing.EffortDialog;
import effortmonitor.swing.EffortDisplayProvider;
import effortmonitor.swing.EffortSidePanel;
import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.AbstractScrollManager;
import pamScrollSystem.PamScroller;
import pamScrollSystem.PamScrollerData;
import userDisplay.UserDisplayControl;

/**
 * Record scroll effort. 
 * @author dg50
 *
 */
public class EffortControl extends PamControlledUnit implements PamSettings{

	public static String unitType = "Scroll Effort";
	private AbstractScrollManager scrollManager;
	private EffortDataBlock effortDataBlock;
	
	private EffortParams effortParams = new EffortParams();
	
	private boolean onEffort = false;
	
	private ArrayList<EffortObserver> effortObservers = new ArrayList<EffortObserver>();
	
	private EffortDataMapGraph dataMapGraph;
	
	private HashMap<AbstractPamScroller, PamScrollerData> lastOuterStarts = new HashMap<>();
	
	private EffortSidePanel effortSidePanel;
	
	public EffortControl(String unitName) {
		super("unitType", unitName);
		EffortProcess effortProc = new EffortProcess();
		this.addPamProcess(effortProc);
		effortDataBlock = new EffortDataBlock(this, effortProc);
		effortProc.addOutputDataBlock(effortDataBlock);
		effortDataBlock.SetLogging(new EffortLogging(this, effortDataBlock));
		dataMapGraph = new EffortDataMapGraph(this);
		
//		PamScroller

		UserDisplayControl.addUserDisplayProvider(new EffortDisplayProvider(this));
		
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public PamSidePanel getSidePanel() {
		if (effortSidePanel == null) {
			effortSidePanel = new EffortSidePanel(this);
		}
		return effortSidePanel;
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			scrollManager = AbstractScrollManager.getScrollManager();
			break;
		case PamController.NEW_SCROLL_TIME:
			updateScrollerInfo();
			break;
		case PamController.DATA_LOAD_COMPLETE:
			updateScrollerInfo();
			break;
		}
	}

	private void updateScrollerInfo() {
		if (onEffort == false) {
			return;
		}
		/**
		 * find out everything we can about every scroller that exists ...
		 */
		if (getPamController().isInitializationComplete() == false) {
			return;
		}
		scrollManager = AbstractScrollManager.getScrollManager();
		if (scrollManager == null) {
			return;
		}
		Vector<AbstractPamScroller> allScrollers = scrollManager.getPamScrollers();
//		Debug.out.println("\nScroller status at " + PamCalendar.formatTime(System.currentTimeMillis()));
		for (AbstractPamScroller scroller : allScrollers) {
			showScrollerData(scroller);
			updateScrollerData(scroller);
		}
	}

	private void updateScrollerData(AbstractPamScroller scroller) {
		if (scroller.isShowing() == false) {
			// this scroller is not visible on the screen, so no point in recording it
			return;
		}
		if (scroller.getValueMillis() <= 1) {
			/*
			 * Seem to get a notification as soon as scrollers are created and before they
			 * have set their first time, so get out
			 */
			return;
		}
		
		boolean newOuter = isNewOuter(scroller);
		if (effortParams.outserScrollOnly) {
			if (newOuter) {
				logOuterScroll(scroller);
			}
		}
		else {
			logInnerScroll(scroller);
		}
	}
	
	private void logOuterScroll(AbstractPamScroller scroller) {
		EffortDataUnit effortData = new EffortDataUnit(scroller, effortParams.getObserver(), effortParams.getObjective());
		effortData.setOuterLimits();
		effortDataBlock.addPamData(effortData);
	}

	private void logInnerScroll(AbstractPamScroller scroller) {
//		checkObserverName();
		
		EffortDataUnit effortData = effortDataBlock.findActiveUnit(scroller);
		if (effortData != null) {
			// old one exists
			if (effortData.isContinuous() == false) {
				// but it's needing closed because there is a gap
				effortData.isContinuous();
				effortData.setActive(false);
				effortDataBlock.updatePamData(effortData, System.currentTimeMillis());
				effortData = null;
			}
		}
		if (effortData == null) {
			// need a new one. so create and add
			effortData = new EffortDataUnit(scroller, effortParams.getObserver(), effortParams.getObjective());
			effortDataBlock.addPamData(effortData);
		} else { // update the existing one
			boolean changed = effortData.extendRecordedRange();
			if (changed) {
				// if it's changed, tell the data block it's updated.
				effortDataBlock.updatePamData(effortData, System.currentTimeMillis());
			}
		}
	}

	/**
	 * Has the outer scroll moved. 
	 * @param scroller
	 * @return true if the outer has moved. 
	 */
	private boolean isNewOuter(AbstractPamScroller scroller) {
		PamScrollerData lastData = lastOuterStarts.get(scroller);
		PamScrollerData nowVal = scroller.getScrollerData();
		if (nowVal == null) {
			return false; // shouldn't happen !
		}
		boolean changed = isChanged(lastData, nowVal);
		if (changed) {
			lastOuterStarts.put(scroller, nowVal.clone());
		}
		return changed;
	}

	private boolean isChanged(PamScrollerData lastData, PamScrollerData nowVal) {
		if (lastData == null || nowVal == null) {
			return true;
		}
		if (lastData.getMinimumMillis() != nowVal.getMinimumMillis()) {
			return true;
		}
		if (lastData.getMaximumMillis() != nowVal.getMaximumMillis()) {
			return true;
		}
		
		return false;
	}

	/**
	 * Quick and dirty to get observer name.
	 */
	private void checkObserverName() {
		if (effortParams.isSet == false) {
			while (effortParams.getObserver() == null) {
				showSettingsDialog(getGuiFrame(), null);
			}
		}
	}

	private void showScrollerData(AbstractPamScroller scroller) {
//		Debug.out.printf("%s\n", scroller.toString());
	}

	private class EffortProcess extends PamProcess {

		public EffortProcess() {
			super(EffortControl.this, null);
		}

		@Override
		public void pamStart() {
		}

		@Override
		public void pamStop() {
		}

	}

	public PamDataBlock<EffortDataUnit> getEffortDataBlock() {
		return effortDataBlock;
	}

	/**
	 * @return the effortParams
	 */
	public EffortParams getEffortParams() {
		return effortParams;
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenu menu = new JMenu(getUnitName());
		JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("Record Effort");
		menu.add(cbMenuItem);
		cbMenuItem.setSelected(isOnEffort());
		cbMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setOnEffort(cbMenuItem.isSelected());
			}
		});
		
		JMenuItem menuItem = new JMenuItem("Settings...");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showSettingsDialog(parentFrame, null);
			}
		});
		return menu;
	}

	public boolean showSettingsDialog(Frame parentFrame, EffortDataUnit existingUnit) {
		EffortParams newParams = EffortDialog.showDialog(parentFrame, this);
		if (newParams != null) {
			effortParams = newParams;
			if (existingUnit != null) {
				existingUnit.setObserver(newParams.getObserver());
				existingUnit.setObjective(newParams.getObjective());
			}
		}
		if (effortSidePanel != null) {
			effortSidePanel.updateSettings();
		}
		return newParams!=null;
	}

	public void goToTime(AbstractPamScroller scroller, long timeInMilliseconds) {
		if (scroller == null || scrollManager == null) {
			return;
		}
		// will first need to see if the data are loaded for that period. 
		if (scroller.getMinimumMillis() > timeInMilliseconds || scroller.getMaximumMillis() < timeInMilliseconds) {
			// need to load data ...
			long range = scroller.getRangeMillis();
			scroller.setRangeMillis(timeInMilliseconds, timeInMilliseconds+range, true);
		}
		scroller.setValueMillis(timeInMilliseconds);
		scroller.notifyRangeChange();
	}

	@Override
	public Serializable getSettingsReference() {
		return effortParams;
	}

	@Override
	public long getSettingsVersion() {
		return EffortParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		effortParams = ((EffortParams) pamControlledUnitSettings.getSettings());
		if (effortSidePanel != null) {
			effortSidePanel.updateSettings();
		}
		return true;
	}

	/**
	 * Add something that wants notificatins when status changes
	 * @param observer
	 */
	public void addObserver(EffortObserver observer) {
		effortObservers.add(observer);
	}
	
	/**
	 * Remove an effort observer
	 * @param observer
	 * @return
	 */
	public boolean removeObserver(EffortObserver observer) {
		return effortObservers.remove(observer);
	}
	
	public void notifyObservers() {
		for (EffortObserver obs : effortObservers) {
			obs.statusChange();
		}
	}

	/**
	 * Set that is recording effort. 
	 * @param selected
	 */
	public void setOnEffort(boolean selected) {
		if (onEffort == false && selected == true) {
			onEffort = showSettingsDialog(getGuiFrame(), null);
		}
		else {
			onEffort = selected;
		}
		notifyObservers();
		if (onEffort) {
			updateScrollerInfo();
		}
	}
	
	/**
	 * Is recording effort
	 * @return
	 */
	public boolean isOnEffort() {
		return onEffort;
	}

	/**
	 * @return the dataMapGraph
	 */
	public EffortDataMapGraph getDataMapGraph() {
		return dataMapGraph;
	}
}
