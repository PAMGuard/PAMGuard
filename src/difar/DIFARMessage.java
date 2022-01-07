package difar;



/**
 * has a case block at bottom of class to copy/paste into methods
 * @author 
 *
 */
public class DIFARMessage {

	/** message to grab time/gps/add to array manager with other perams required
	 * have channel data buoy type
	 */
	public static final int Deploy = 0;
	/**edit params box, this may have things like time to calibrate
	 */
	public static final int EditVesselBuoySettings = 1;
	/**Start a vessel calibration sequence, default to last buoy deployed but will need
	 * options to recalibrate other buoys
	 */
//	public static final int StartBuoyCalibration = 2;
//	/**stop all or stop specific one
//	 */
//	public static final int StopBuoyCalibration = 3;

	/**
	 * 
	 */
	public static final int NewDifarUnit = 2;
	
	
	/**
	 * Demux and difargram calculation complete. 
	 */
	public static final int DemuxComplete = 4;
	
	/**
	 * Delete a data unit from the pre-process queue display
	 */
	public static final int DeleteFromQueue = 100;
	
	/**
	 * A data unit in the pre-process queue should now be processed. 
	 */
	public static final int ProcessFromQueue = 101;
	/**
	 * Called when save button pressed or single click angle selection used. OR AUTOSAVES
	 */
	public static final int SaveDatagramUnit = 102; 
	

	/**
	 * Called when save button pressed or single click angle selection used. 
	 */
	public static final int SaveDatagramUnitWithoutRange = 107;
	
	/**
	 * Delete the current unit held in the datagram. 
	 */
	public static final int DeleteDatagramUnit = 103;
	
	/**
	 * Called when the datagram is clicked with the mouse. 
	 * Angle is added to the dataunit at this point, but it may 
	 * not actually get saved= delends on state of difarParameters.singleclickSave
	 */
	public static final int ClickDatagramUnit = 104;
	
	/**
	 * Passed around whenever any of the settings in the clip control panel change
	 */
	public static final int DisplaySettingsChange = 105;
	
	/**
	 * Return a demuxed data unit from the DIFARgram back to the queue. 
	 */
	public static final int ReturnToQueue = 106;
	
	public int message;
	
	public DifarDataUnit difarDataUnit;

	public DIFARMessage(int message) {
		super();
		this.message = message;
	}
	/**
	 * has a case block at bottom of class to copy/paste into methods
	 * @author 
	 *
	 */
	public DIFARMessage(int message, DifarDataUnit difarDataUnit) {
		super();
		this.message = message;
		this.difarDataUnit = difarDataUnit;
	}
	
	
//case DIFARMessage.Deploy:
//case DIFARMessage.EditVesselBuoySettings:
////Dont exist		case DIFARMessage.StartBuoyCalibration:
////Dont exist		case DIFARMessage.StopBuoyCalibration:
//case DIFARMessage.DemuxComplete:
//case DIFARMessage.DeleteFromQueue:
//case DIFARMessage.ProcessFromQueue:
//case DIFARMessage.SaveDatagramUnit:
//case DIFARMessage.DeleteDatagramUnit:
//case DIFARMessage.ClickDatagramUnit:
//case DIFARMessage.DisplaySettingsChange:
//case DIFARMessage.ReturnToQueue:

	
}
