package Filters;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import Filters.layoutFX.FilterGUIFX;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitGUI;
import PamController.PamControlledUnitSettings;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;

/**
 * Filters raw data using a specified filter. 
 * 
 * @author Doug Gillespie
 *
 */
public class FilterControl extends PamControlledUnit implements PamSettings {

	FilterParameters_2 filterParams = new FilterParameters_2();
	
	FilterProcess filterProcess;

	/**
	 * The filter FX GUI. 
	 */
	private FilterGUIFX filterGUIFX;
	
	public FilterControl(String unitName) {
		super("IIRF Filter", unitName);
		filterProcess = new FilterProcess(this);
		addPamProcess(filterProcess);
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenu menu = new JMenu(getUnitName());
		JMenuItem menuItem;
		
		menuItem = new JMenuItem("Data source...");
		menuItem.addActionListener(new DataSourceAction(parentFrame));
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Filter Settings...");
		menuItem.addActionListener(new DetectionMenuAction(parentFrame));
		menu.add(menuItem);
		
		return menu;
	}

	class DataSourceAction implements ActionListener {

		Frame parentFrame;
		
		public DataSourceAction(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			FilterParameters_2 newParams = FilterDataSourceDialog.showDialog(filterParams, parentFrame, filterProcess.outputData);
			
			if (newParams != null) {
				filterParams = newParams.clone();
				filterProcess.setupProcess();
			}
		}
		
	}
	class DetectionMenuAction implements ActionListener {

		Frame parentFrame;
		
		public DetectionMenuAction(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}
		@Override
		public void actionPerformed(ActionEvent e) {

			FilterParams newParams = FilterDialog.showDialog(parentFrame,
					filterParams.filterParams, filterProcess.getSampleRate());
			if (newParams != null) {
				filterParams.filterParams = newParams.clone();
				filterProcess.setupProcess();
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		//System.out.println("FFTControl: notifyModelChanged : " +changeType);
		super.notifyModelChanged(changeType);
		if (changeType == PamControllerInterface.INITIALIZATION_COMPLETE) {
			filterProcess.setupProcess();
		}
		if (filterGUIFX!=null) {
			filterGUIFX.notifyGUIChange(changeType);
		}
	}
	
	@Override
	public Serializable getSettingsReference() {
		return filterParams;
	}

	@Override
	public long getSettingsVersion() {
		return FilterParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		filterParams = ((FilterParameters_2) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	/**
	 * Get the filter process. 
	 * @return the filter process. 
	 */
	public FilterProcess getFilterProcess() {
		return this.filterProcess;
	}

	/**
	 * Get the filter parameters. 
	 * @return the filter parameters. 
	 */
	public FilterParameters_2 getFilterParams() {
		return this.filterParams;
	}

	/**
	 * Set the filter parameters. 
	 * @param newParams - the new filter parameters to set. 
	 */
	public void setFilterParams(FilterParameters_2 newParams) {
		this.filterParams=newParams;
	}
	
	@Override
	public PamControlledUnitGUI getGUI(int flag) {
		if (flag==PamGUIManager.FX) {
			if (filterGUIFX == null) {
				filterGUIFX= new FilterGUIFX(this);
			}
			return filterGUIFX;
		}
		//TODO swing
		return null;
	}

}
