package Filters.layoutFX;

import Filters.FilterControl;
import Filters.FilterParameters_2;
import PamController.PamControllerInterface;
import PamController.SettingsPane;
import pamViewFX.PamControlledGUIFX;

public class FilterGUIFX extends PamControlledGUIFX{

	/**
	 * The filter control. 
	 */
	private FilterControl filterControl;

	/**
	 * The filter settings pane 
	 */
	private FilterSettingsPaneFX fftPane;

	public FilterGUIFX(FilterControl filterControl) {
		this.filterControl=filterControl; 
	}

	public SettingsPane<FilterParameters_2> getSettingsPane(){
		if (fftPane==null) fftPane= new FilterSettingsPaneFX(filterControl); 
		fftPane.setParams(filterControl.getFilterParams());
		return fftPane;
	}

	@Override
	public void updateParams() {
		FilterParameters_2 newParams=fftPane.getParams(filterControl.getFilterParams()); 
		if (newParams!=null) filterControl.setFilterParams(newParams); 
		//setup the controlled unit. 
		filterControl.setupControlledUnit(); 
	}

	@Override
	public void notifyGUIChange(int changeType) {
		switch (changeType) {
		case PamControllerInterface.CHANGED_PROCESS_SETTINGS:
			//			data source may have potentially changed. e.g. by a datamodelfx Need to set in params.
			//			System.out.println("FFTControl: CHANGED_PROCESS_SETTINGS : " +fftProcess.getParentDataBlock());
			if (filterControl.getFilterProcess().getParentDataBlock()!=null){
				filterControl.getFilterParams().rawDataSource=filterControl.getFilterProcess().getParentDataBlock().getDataName();
			}
			else filterControl.getFilterParams().rawDataSource="";
			break;
		}
	}


}
