package Spectrogram;

import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class SpectrogramDiplayProvider implements UserDisplayProvider {

	private SpectrogramParameters readyParameters;

	@Override
	public String getName() {
		return "Spectrogram Display";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		return createSpectrogramDisplay(userDisplayControl, readyParameters, uniqueDisplayName);
	}
	
	/**
	 * This can be used to recreate spectrogram displays from old configurations with their correct settings. 
	 * From now on though, all spectrograms will be responsible for doing their own settings. 
	 * @param userDisplayControl
	 * @param spectrogramParameters
	 * @param uniqueDisplayName 
	 * @return display component. 
	 */
	public SpectrogramDisplayComponent createSpectrogramDisplay(UserDisplayControl userDisplayControl, 
			SpectrogramParameters spectrogramParameters, String uniqueDisplayName) {
		SpectrogramDisplayComponent dc = new SpectrogramDisplayComponent(userDisplayControl, spectrogramParameters, uniqueDisplayName);
		readyParameters = null;
		return dc;
	}

	@Override
	public Class getComponentClass() {
		return SpectrogramDisplayComponent.class;
	}

	@Override
	public int getMaxDisplays() {
		return 0;
	}

	@Override
	public boolean canCreate() {
		return true;
	}

	@Override
	public void removeDisplay(UserDisplayComponent component) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Set parameters ready for the next constructed display. this is used
	 * as a bit of a fudge when converting old stle params storage 
	 * @param spectrogramParameters
	 */
	public void setReadyParameters(SpectrogramParameters spectrogramParameters) {
		this.readyParameters= spectrogramParameters;
	}


}
