package PamguardMVC.toad;

import java.awt.Window;
import java.util.List;

import Array.SnapshotGeometry;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import group3dlocaliser.algorithm.toadbase.TOADInformation;
import pamViewFX.fxNodes.pamDialogFX.ManagedSettingsPane;

/**
 * Class to handle TOAD (Time of Arrival Difference) calculations for a particular type of data. Different data types may
 * have to calculate TOAD's in quite different ways and may be able to present different options, e.g. the click
 * TOAD calculator can use click species information to select the analysis bandwidth and can have options
 * on using the leading edge of the click or the envelope, the Whistle calculator will 
 * probably use the sweep information and may have to do something cunning to match sounds far apart in time.   
 * @author Doug Gillespie
 *
 */
public interface TOADCalculator {

//	/**
//	 * Calculate a TOAD in seconds for specified channels within two data units. 
//	 * @param dataUnit1 first data unit
//	 * @param iChannel1 first channel (absolute index, not index within the data unit)
//	 * @param dataUnit2 second data unit
//	 * @param iChannel2 second channel (absolute index, not index within the data unit)
//	 * @param maxDelay max possible delay in seconds (based on hydrophone separation)
//	 * @return toad in seconds and an estimate of the error (also in seconds) or null if no answer (e.g. if channels don't exist) 
//	 * @throws TOADException Error, for example if a channel doesn't exist within a data unit. 
//	 */
//	public double[] getTOADSeconds(PamDataUnit dataUnit1, int iChannel1, PamDataUnit dataUnit2, int iChannel2, double maxDelay) throws TOADException;
	/**
	 * Generate TOAD information for a while load of data units (or just one - maybe 
	 * needs a simpler list if there is only one to avoid having to make lots of lists
	 * of just one data unit !)
	 * @param dtaUnits Data units, which are assumed to be of the same sound. If not, 
	 * then this function should be called separately for sounds considered part of the 
	 * same group. 
	 * @param channelMap wanted channels ? Do we need this parameter ? 
	 * @return
	 */
	public TOADInformation getTOADInformation(List<PamDataUnit> dataUnits, double sampleRate, int channelMap, SnapshotGeometry geometry);
	
	/**
	 * 
	 * @param detectionSource 
	 * @return A settings pane to configure any TOAD options. 
	 */
	public ManagedSettingsPane<?> getSettingsPane(Window parent, PamDataBlock<?> detectionSource);
	
	/**
	 * Flag to say there is a full settings dialog
	 * @return true if there is a dialog
	 */
	public boolean hasTOADDialog();
	
	/**
	 * Show the dialog,
	 * @param parentWindow AWT or FX parent.   
	 * @return true if OK was pressed and everything OK
	 */
	public boolean showTOADDialog(Object parentWindow);
	
}
