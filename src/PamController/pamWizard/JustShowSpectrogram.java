package PamController.pamWizard;

import PamController.soundMedium.GlobalMedium.SoundMedium;

public class JustShowSpectrogram implements PamAutoConfig {

	@Override
	public boolean isConfigValid(PamFileImport importHandler) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getConfigDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getSpeciesList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getConfigName() {
		return "Just show me a spectrgram";
	}

	@Override
	public SoundMedium getGlobalMediumSettings() {
		//spectrgrams don't care whether air or water. 
		return null;
	}

}
