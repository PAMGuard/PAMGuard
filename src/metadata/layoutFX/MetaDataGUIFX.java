package metadata.layoutFX;

import PamController.PamController;
import PamController.SettingsPane;
import metadata.MetaDataContol;
import metadata.PamguardMetaData;
import pamViewFX.PamControlledGUIFX;

/**
 * JavaFX GUI for the MetaDataContol module.
 * Provides a settings pane that replicates the Swing MetaDataDialog.
 * 
 * @author PAMGuard
 */
public class MetaDataGUIFX extends PamControlledGUIFX {

	/**
	 * Reference to the metadata controller.
	 */
	private MetaDataContol metaDataContol;

	/**
	 * The settings pane for metadata.
	 */
	private MetaDataSettingsPane settingsPane;

	public MetaDataGUIFX(MetaDataContol metaDataContol) {
		this.metaDataContol = metaDataContol;
	}

	@Override
	public SettingsPane<PamguardMetaData> getSettingsPane() {
		if (settingsPane == null) {
			settingsPane = new MetaDataSettingsPane(null);
		}
		settingsPane.setParams(metaDataContol.getMetaData());
		return settingsPane;
	}

	@Override
	public void updateParams() {
		if (settingsPane == null) {
			return;
		}
		PamguardMetaData newData = settingsPane.getParams(metaDataContol.getMetaData());
		if (newData != null) {
			metaDataContol.setMetaData(newData);
			newData.setLastModified(System.currentTimeMillis());
			PamController.getInstance().notifyModelChanged(PamController.PROJECT_META_UPDATE);
		}
	}
}
