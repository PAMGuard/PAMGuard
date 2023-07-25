package group3dlocaliser.algorithm;

import java.awt.Window;

import Array.ArrayManager;
import Localiser.LocaliserModel;
import PamController.SettingsPane;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;
import generalDatabase.SQLLoggingAddon;
import group3dlocaliser.grouper.DetectionGroupedSet;
import pamViewFX.fxNodes.pamDialogFX.ManagedSettingsPane;

abstract public class LocaliserAlgorithm3D implements LocaliserModel<PamDataUnit> {

	abstract public boolean prepare(PamDataBlock sourceBlock);
	
//	/**
//	 * Process a grouped set of data units to determine a localisation.
//	 * 
//	 * @param dataUnits list of data units to localise. 
//	 * @return group localisation (which may contain multiple results if there was ambiguity)
//	 */
//	abstract public GroupLocalisation process(List<PamDataUnit> dataUnits);
	
	/**
	 * Get SQL Logging Addon data to append to the basic database table
	 * @param arrayType type of array - which may affect how logging is organised. 
	 * @return SQL Logging information. 
	 */
	abstract public SQLLoggingAddon getSQLLoggingAddon(int arrayType);
	
	/**
	 * Option to pre-filter the localisation results. This can be useful when using algorithms that 
	 * internally handle detection match uncertainty. 
	 */
	public DetectionGroupedSet preFilterLoc(DetectionGroupedSet arrayType) {
		return arrayType; 
	}
	
	/**
	 * Configure parameters for this algorithm. Show a dialog with 
	 * anything you want ...
	 * <br>This may be Swing or FX for compatibility with old code. 
	 * @param parent parent frame / window, etc. 
	 * @param currentParams current algorithm params. These may be null
	 * the first time this gets called, so prepare for this and create new as 
	 * required
	 * @return null if the dialog cancelled, a new params object otherwise. 
	 */
	public LocaliserAlgorithmParams showAlgorithmDialog(Window parent, LocaliserAlgorithmParams currentParams) {
		return null;
	}
	
	/**
	 * Get a source settings pane. This will be inserted into the dialog beneath where the 
	 * detection source and algorithm have been selected. 
	 * <br> Can be null if the algorithm / data source have no source settings. May also 
	 * change should there be any changes to the data source type. 
	 * @param parent
	 * @return Pane to insert into the main 3D localiser dialog. 
	 */
	public ManagedSettingsPane<?> getSourceSettingsPane(Window parent, PamDataBlock<?> detectionSource) {
		return null;
	}
	
	/**
	 * @param pamDataBlock
	 * @return true if this algorithm can process this type of data. 
	 */
	abstract public boolean canLocalise(PamDataBlock pamDataBlock);	
	
	/**
	 * Ask if the algorithm can process this type of array data.
	 * @param arrayShape array shape. One of:	
	 * <br>	ArrayManager.ARRAY_TYPE_NONE
	 * <br>	ArrayManager.ARRAY_TYPE_POINT
	 * <br>	ArrayManager.ARRAY_TYPE_LINE
	 * <br>	ArrayManager.ARRAY_TYPE_PLANE
	 * <br>	ArrayManager.ARRAY_TYPE_VOLUME
	 * @return
	 */
	public boolean canArrayShape(int arrayShape) {
		return arrayShape >= ArrayManager.ARRAY_TYPE_LINE;
	}
	
	
	public int getGroupChannelMap(SuperDetection superDetection) {
		int groupMap = 0;
		int n = superDetection.getSubDetectionsCount();
		for (int i = 0; i < n; i++) {
			PamDataUnit subDet = superDetection.getSubDetection(i);
			groupMap |= subDet.getChannelBitmap();
		}
		return groupMap;
	}
	
//	private int[] getGroup

}
