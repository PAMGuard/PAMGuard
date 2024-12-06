package rawDeepLearningClassifier.dataPlotFX;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import PamguardMVC.PamDataBlock;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.generic.GenericDataPlotInfo;
import dataPlotsFX.data.generic.GenericSettingsPane;
import dataPlotsFX.layout.TDGraphFX;
import javafx.scene.Node;

import rawDeepLearningClassifier.DLControl;

public class DLGroupDetectionInfoFX extends GenericDataPlotInfo {
	

	public DLGroupDetectionInfoFX(TDDataProviderFX tdDataProvider, DLControl dlControlm, TDGraphFX tdGraph, PamDataBlock pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);

		try {
			URL iconURL = getClass().getResource(DLDetectionPlotInfoFX.iconResourcePath).toURI().toURL();
			Node showingIcon = DLDetectionPlotInfoFX.makeIcon(iconURL); 
			((GenericSettingsPane) this.getGraphSettingsPane()).setIcon(showingIcon);
		} catch (MalformedURLException | URISyntaxException e) {
			e.printStackTrace();
		}
		((GenericSettingsPane) this.getGraphSettingsPane()).setShowingName("Deep Learning Group Detections");
	}

}
