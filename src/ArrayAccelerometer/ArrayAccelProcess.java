package ArrayAccelerometer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import mcc.MccJniInterface;
import mcc.mccjna.MCCException;
import mcc.mccjna.MCCUtils;
import Array.ArrayManager;
import Array.PamArray;
import Array.Streamer;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;


public class ArrayAccelProcess extends PamProcess {

	protected ArrayAccelDataBlock accelDataBlock;

	private Timer accelTimer;

	private ArrayAccelControl accelControl;

	private MccJniInterface mccJni;

	public ArrayAccelProcess(ArrayAccelControl accelControl) {
		super(accelControl, null);
		this.accelControl = accelControl;
		addOutputDataBlock(accelDataBlock = new ArrayAccelDataBlock("Array Acceleration Data", accelControl, this));
		accelDataBlock.SetLogging(new ArrayAccelLogging(accelControl, accelDataBlock));
		mccJni = new MccJniInterface();
		if (PamController.getInstance().getRunMode() == PamController.RUN_NORMAL) {
			accelTimer = new Timer(1000, new AccelTimerAction());
		}

	}

	@Override
	public void pamStart() {
		if (PamController.getInstance().getRunMode() == PamController.RUN_NORMAL) {
			accelTimer.setInitialDelay(0);
			accelTimer.setDelay((int) (accelControl.accelParams.readInterval * 1000));
			accelTimer.start();
		}
	}

	@Override
	public void pamStop() {
		if (PamController.getInstance().getRunMode() == PamController.RUN_NORMAL) {
			accelTimer.stop();
		}
	}

	private class AccelTimerAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			readSensor();
		}
	}

	public void readSensor() {
		ArrayAccelParams ap = accelControl.accelParams;
		int board = MCCUtils.boardIndexToNumber(ap.boardNumber);
		if (board < 0) {
			return;
		}
		Double[] v = new Double[ArrayAccelParams.NDIMENSIONS];
		int err;
		for (int i = 0; i < ArrayAccelParams.NDIMENSIONS; i++) {
			if (ap.dimChannel[i] >= 0) {
				try {
					v[i] = MCCUtils.readVoltage(board, ap.dimChannel[i], ap.boardRange);
				} catch (MCCException e) {
//					e.printStackTrace();
					System.out.println("Array Accel Error: " + e.getMessage());
					return;
				}
//				err = mccJni.getLastErrorCode();
//				if (err != 0) {
//					System.out.println("Array Accel Error: " + mccJni.getErrorString(err));
//					return;
//				}
			}
		}
		Double[] a;
		ArrayAccelDataUnit aadu = new ArrayAccelDataUnit(PamCalendar.getTimeInMillis(), accelControl, v, a = calcAccel(v));
		accelDataBlock.addPamData(aadu);
//		setArrayAngles(aadu);
		//		System.out.println(String.format("Accelerometer Voltages: %3.2f, %3.2f, %3.2fV Acel: %3.3f, %3.3f, %3.3f", 
		//				v[0], v[1], v[2], a[0], a[1], a[2]));
	}

	/**
	 * This no longer used since it's the job of the array to subscribe to the sensor. 
	 */
//	private void setArrayAngles(ArrayAccelDataUnit aadu) {
//		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
//		Streamer streamer = currentArray.getStreamer(accelControl.accelParams.streamerIndex);
//		if (currentArray == null) {
//			return;
//		}
//		Double roll = aadu.getRoll();
//		if (roll != null) {
//			streamer.setRoll(roll);
//		}
//		Double pitch = aadu.getPitch();
//		if (pitch != null) {
//			streamer.setPitch(pitch);
//		}
//		if (pitch != null || roll != null) {
//			streamer.makeStreamerDataUnit();
//		}
//	}

	public void setupTimer() {
		accelTimer.setDelay((int) accelControl.accelParams.readInterval * 1000);
	}

	private Double[] calcAccel(Double[] voltages) {
		ArrayAccelParams ap = accelControl.accelParams;
		Double[] a = new Double[voltages.length];
		for (int i = 0; i < voltages.length; i++) {
			if (voltages[i] != null) {
				a[i] = (voltages[i]-ap.zeroVolts[i])/ap.voltsPerG[i];
			}
		}
		return a;
	}

}
