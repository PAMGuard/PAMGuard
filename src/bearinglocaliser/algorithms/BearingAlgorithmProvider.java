package bearinglocaliser.algorithms;

import java.awt.Window;

import PamController.SettingsPane;
import bearinglocaliser.BearingLocaliserParams;
import bearinglocaliser.BearingProcess;
import bearinglocaliser.display.BearingDataDisplay;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX2AWT;

public abstract class BearingAlgorithmProvider {

	abstract public StaticAlgorithmProperties getStaticProperties();

	abstract public SettingsPane<?> getSettingsPane(Window awtWindow, BearingLocaliserParams blParams, BearingAlgorithmParams algoParams);

	abstract public BearingAlgorithm createAlgorithm(BearingProcess bearingProcess, BearingAlgorithmParams algorithmParams, int groupindex);

	abstract public BearingAlgorithmParams createNewParams(int groupList, int groupChanMap);

	public BearingAlgorithmParams showConfigDialog(Window awtWindow, BearingLocaliserParams params,
			BearingAlgorithmParams groupParams) {
		SettingsPane<BearingAlgorithmParams> theSettingsPane = 
				(SettingsPane<BearingAlgorithmParams>) getSettingsPane(awtWindow, params, groupParams);
		if (theSettingsPane == null) {
			System.out.println("No available settings pane for bearing algorithm " + getStaticProperties().getName());
			return null;
		}

		PamDialogFX2AWT<BearingAlgorithmParams> algDialog = new PamDialogFX2AWT<BearingAlgorithmParams>(awtWindow, theSettingsPane, false);
		BearingAlgorithmParams newParams = algDialog.showDialog(groupParams);
		return newParams;
	}


}
