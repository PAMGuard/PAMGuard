package difar.dataSelector;

import generalDatabase.lookupTables.LookupItem;
import generalDatabase.lookupTables.LookupList;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Vector;

import difar.DifarControl;
import difar.DifarDataUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;

/**
 * Data selector for DIFAR data units (displayed on the map)
 * Initially just a copy of the Whistle & Moan Data Selector.
 * But now allows selection of DIFAR species too.
 */
public class DifarDataSelector extends DataSelector {

	private DifarControl difarControl;
	
	private DifarSelectPanel selectPanel;
	
	private DifarSelectParameters difarSelectParameters;

	public DifarDataSelector(DifarControl difarControl, PamDataBlock pamDataBlock, String selectorName,
			boolean allowScores) {
		super(pamDataBlock, selectorName, allowScores);
		this.difarControl = difarControl;
		LookupList speciesList = difarControl.getDifarParameters().getSpeciesList(difarControl);
		
		int channelBitmap = 0;
		PamDataBlock<PamDataUnit> sourceBlock = difarControl.getDifarProcess().getSourceDataBlock();
		if (sourceBlock != null) {
			channelBitmap = sourceBlock.getChannelMap();
		}
		difarSelectParameters = new DifarSelectParameters(speciesList, channelBitmap);
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		if (selectPanel == null) {
			selectPanel = new DifarSelectPanel(this);
		}
		updateSpecies();
		return selectPanel;
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
//		try {
			DifarDataUnit difarDataUnit = (DifarDataUnit) pamDataUnit;
			return (wantDifarUnit(difarDataUnit) ? 1: 0);
//		} catch(ClassCastException e) {
//			System.out.println("Wasn't a DifarDataUnit.");
//			return 0;
//		} finally{
//			return 0;
//		}
	}	
	
	private boolean wantDifarUnit(DifarDataUnit difarDataUnit) {
		
		//Filter by frequency
		double[] f = difarDataUnit.getFrequency();
		if (difarSelectParameters.minFreq > 0 && f[0] < difarSelectParameters.minFreq) {
			return false;
		}
		if (difarSelectParameters.maxFreq > 0 && f[1] > difarSelectParameters.maxFreq) {
			return false;
		}
		
		//Filter clicks less than some minimum amplitude
		if (difarDataUnit.getAmplitudeDB() < difarSelectParameters.minAmplitude) {
			return false;
		}
		
		//Filter by duration
		float sampleRate = difarDataUnit.getParentDataBlock().getParentProcess().getSampleRate();
		if (difarDataUnit.getSampleDuration() * 1000. / sampleRate < difarSelectParameters.minLengthMillis) {
			return false;
		}
		
		//Filter by click classification
		if ( shouldHideSpecies(difarDataUnit.getLutSpeciesItem()) ){
			return false;
		}
		
		//Filter by channel
		if (  shouldHideChannel( PamUtils.PamUtils.getSingleChannel(difarDataUnit.getChannelBitmap()) )  ){
			return false;
		}
		
		if (difarSelectParameters.showOnlyCrossBearings && difarDataUnit.getDifarCrossing()==null){
			return false;
		}
		
		return true;
	}

	/**
	 * For a given species, determine whether or not it should be shown.
	 * @param The species in question
	 * @return true if this species should be hidden by data selector
	 */
	private boolean shouldHideSpecies(LookupItem species){
		// Next 4 lines fix an obscure weird error in ACE2017 data 
		Vector<LookupItem> oldList = difarSelectParameters.speciesList.getList();
		if (difarSelectParameters.speciesEnabled.length < oldList.size()) {
			difarSelectParameters.speciesEnabled = Arrays.copyOf(difarSelectParameters.speciesEnabled, oldList.size());
		}
		for (int i = 0; i < difarSelectParameters.speciesList.getList().size(); i++){
			if (difarSelectParameters.speciesList.getLookupItem(i).equals(species) && 
					difarSelectParameters.speciesEnabled[i]==false) {
				return true;
			}
		}
		return false;
	}

	/**
	 * For a given species, determine whether or not it should be shown.
	 * @param The species in question
	 * @return true if this species should be hidden by data selector
	 */
	private boolean shouldHideChannel(int channel){
		if (difarSelectParameters.channelEnabled == null || difarSelectParameters.channelEnabled.length <= channel){
			return false;
		}
		
		if (difarSelectParameters.channelEnabled[channel]==false) {
			return true;
		}
		return false;
	}	
	
	/**
	 * @return the DifarSelectParameters
	 */
	public DifarSelectParameters getDifarSelectParameters() {
		return difarSelectParameters;
	}

	/**
	 * @param difarSelectParameters the DifarSelectParameters to set
	 */
	public void setDifarSelectParameters(DifarSelectParameters difarSelectParameters) {
		this.difarSelectParameters = difarSelectParameters;
	}
	
	/* (non-Javadoc)
	 * @see PamguardMVC.dataSelector.DataSelector#setParams(PamguardMVC.dataSelector.DataSelectParams)
	 */
	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		difarSelectParameters = (DifarSelectParameters) dataSelectParams;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.dataSelector.DataSelector#getParams()
	 */
	@Override
	public DataSelectParams getParams() {
		return difarSelectParameters;
	}

	/**
	 * Called every time the dialog is displayed, this checks if the list
	 * of species in the data selector is different than that in the DIFAR 
	 * module. If so, then a new data selector panel is created.
	 */
	private void updateSpecies(){
		LookupList difarSpecies = difarControl.getDifarParameters().getSpeciesList(difarControl);
		if (difarSelectParameters.speciesList != null){
			if (difarSpecies !=null){
				if (difarSelectParameters.speciesList.equals(difarSpecies)){
					return;
				}
			}
		}
//		difarSelectParameters.speciesList = difarSpecies;
//		difarSelectParameters.speciesEnabled = new boolean[difarSpecies.getSelectedList().size()];
//		for (int i = 0; i < difarSelectParameters.speciesEnabled.length; i++){
//			difarSelectParameters.speciesEnabled[i] = true;			
//		}
//		selectPanel = new DifarSelectPanel(this);
		
		// Next 4 lines fix an obscure weird error in ACE2017 data 
		Vector<LookupItem> oldList = difarSelectParameters.speciesList.getList();
		if (difarSelectParameters.speciesEnabled.length <oldList.size()) {
			difarSelectParameters.speciesEnabled = Arrays.copyOf(difarSelectParameters.speciesEnabled, oldList.size());
		}
		Vector<LookupItem> newList = difarSpecies.getList();
		boolean enabled[] = new boolean[newList.size()];
		for (int j = 0; j < newList.size(); j++){
			enabled[j] = true; // Newly added classifications should be shown
		}

		// Try to restore the state of existing classes
		for (int j = 0; j < newList.size(); j++){
			for (int i = 0; i < oldList.size(); i++){
				if(oldList.get(i).getText().equals(newList.get(j).getText())){
					enabled[j] = difarSelectParameters.speciesEnabled[i];
				}
			}	
		}
		
		difarSelectParameters.speciesList = difarSpecies;
		difarSelectParameters.speciesEnabled = enabled;
		selectPanel.updateSpeciesPanel();
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		return null;
	}
	
}
