package difar.calibration;

import java.awt.Window;

import javax.swing.JOptionPane;

import Array.ArrayManager;
import Array.PamArray;
import Array.Streamer;
import Array.StreamerDataBlock;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.dialog.PamDialog;
import annotation.string.StringAnnotation;
import difar.DIFARMessage;
import difar.DifarControl;
import difar.DifarProcess;
import difar.SonobuoyManager;
import generalDatabase.SQLLogging;

/**
 * Handle the calibration process for a single channel
 * @author Doug Gillespie
 *
 */
public class CalibrationProcess {

	private int channel;
	
	private DifarControl difarControl;
	
	private SonobuoyManager buoyManager;
	
	private DifarProcess difarProcess;

	private long nextBuoyCalibrationTime = Long.MAX_VALUE;
	private int nCalibrationsToDo = 0;
	private int nCalibrations;
	/**
	 * the last raw data unit sample number. 
	 */
	private long lastRawSampleNumber;

	private long calibrationStartTime;
	
	public CalibrationProcess(DifarControl difarControl, int channel) {
		super();
		this.difarControl = difarControl;
		buoyManager = difarControl.sonobuoyManager;
		this.channel = channel;
		difarProcess = difarControl.getDifarProcess();
	}
	

	/**
	 * Start a sequence of buoy angle calibrations. 
	 */
	public void startBuoyCalibration() {
		nextBuoyCalibrationTime = Math.max((long) (difarControl.getDifarParameters().vesselClipLength*
				difarProcess.getSampleRate()), lastRawSampleNumber);
		calibrationStartTime = 0;
		nCalibrations = nCalibrationsToDo = difarControl.getDifarParameters().vesselClipNumber;
		clearHistograms();
		buoyManager.showCalibrationDialog(channel);
	}
	
	private void clearHistograms() {
		difarProcess.getCalCorrectionHistogram(channel).clear();
		difarProcess.getCalTrueBearingHistogram(channel).clear();
	}

	/**
	 * Stop a sequence of buoy angle calibrations. 
	 */
	public void stopBuoyCalibration() {
		nextBuoyCalibrationTime = Long.MAX_VALUE;
		nCalibrationsToDo = 0;
	}

	/**
	 * @return the nextBuoyCalibrationTime
	 */
	public long getNextBuoyCalibrationTime() {
		return nextBuoyCalibrationTime;
	}

	/**
	 * @return the nCalibrationsToDo
	 */
	public int getnCalibrationsToDo() {
		return nCalibrationsToDo;
	}


	public void newRawData(RawDataUnit rawDataUnit) {

		lastRawSampleNumber = rawDataUnit.getStartSample();
		if (lastRawSampleNumber > nextBuoyCalibrationTime && 
				nCalibrationsToDo > 0 && 
				(rawDataUnit.getChannelBitmap() & 1<<channel) != 0) {
			// time to grab buoy calibration data. 
			Long calUnit = difarProcess.doBuoyCalibration(channel, lastRawSampleNumber);
			if (calibrationStartTime == 0 && calUnit != null) {
				calibrationStartTime = calUnit;
				nextBuoyCalibrationTime = lastRawSampleNumber;
			}
			nCalibrationsToDo--;
//			if (nCalibrationsToDo == 0) {
//				difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.StopBuoyCalibration));
//			}
			nextBuoyCalibrationTime += (long) (difarProcess.getSampleRate() * difarControl.getDifarParameters().vesselClipSeparation);
		}


		
	}

	/**
	 * 
	 * @return the channel number for this process. 
	 */
	public int getChannel() {
		return channel;
	}


	/**
	 * Get the total number of calibrations in the sequence
	 * @return the nCalibrations
	 */
	public int getnCalibrations() {
		return nCalibrations;
	}

	public String getStatusString() {
		if (nCalibrationsToDo == 0) {
			return "Calibration complete";
		}
		else {
			return String.format("%d of %d calibration clips remaining to process", nCalibrationsToDo, nCalibrations);
		}
	}


	/**
	 * Set the correction value. Give user the option of refusing !
	 * @param val correction value
	 * @return true if user selected OK. 
	 */
	public boolean setCorrectionValue(Window parent, Double val) {
		if (val == null) {
			return false;
		}
		
		double arrayHead = buoyManager.getCompassCorrection(channel, calibrationStartTime);
		
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray(); 
		int phoneNumber = channel;
		double newHead = arrayHead + val;
		int streamerInd = currentArray.getStreamerForPhone(phoneNumber);
		Streamer streamer = currentArray.getStreamer(streamerInd);
		String str = String.format("Update Buoy %d heading from %3.1f%s to %3.1f%s", 
				streamerInd, arrayHead, LatLong.deg, newHead, LatLong.deg);
		int ans = JOptionPane.showConfirmDialog(parent, str, "Buoy heading update", JOptionPane.OK_CANCEL_OPTION);
		if (ans == JOptionPane.CANCEL_OPTION) {
			return false;
		}
		
		double std = difarProcess.getCalCorrectionHistogram(channel).getSTD();
		int numClips = (int) difarProcess.getCalCorrectionHistogram(channel).getTotalContent();
		
		boolean correctionOk = buoyManager.updateCorrection(streamer, calibrationStartTime, newHead, std, numClips); 
		
		if (correctionOk)
			return true;
		else
			return PamDialog.showWarning(parent, "Error", "Unable to update Buoy heading");
	}



	
}
