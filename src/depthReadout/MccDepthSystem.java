package depthReadout;

import java.awt.Frame;
import java.io.Serializable;

import depthReadout.MccDepthParameters.MccSensorParameters;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;

import mcc.MccJniInterface;
import mcc.mccjna.MCCException;
import mcc.mccjna.MCCUtils;
import warnings.PamWarning;
import warnings.WarningSystem;

public class MccDepthSystem implements DepthSystem , PamSettings {

	DepthControl depthControl; 

	protected MccJniInterface mccJniInterface;

	protected MccDepthParameters mccDepthParameters = new MccDepthParameters();

	private double rawValue;

	private double depth;

	private int errorCode;

	private PamWarning depthWarning;

	public MccDepthSystem(DepthControl depthControl) {
		super();
		this.depthControl = depthControl;
		mccJniInterface = new MccJniInterface();
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public boolean canConfigure() {
		return true;
	}

	@Override
	public boolean configureSensor(Frame parentFrame) {
		MccDepthParameters newParams = MccDialog.showDialog(depthControl, this, parentFrame, mccDepthParameters);
		if (newParams != null) {
			mccDepthParameters = newParams.clone();
			return true;
		}
		return false;
	}


	@Override
	public boolean readSensor(int iSensor) {
		MccSensorParameters sp = mccDepthParameters.getSensorParameters(iSensor);
		if (sp == null) {
			return false;
		}
		try {
			int boardNum = MCCUtils.boardIndexToNumber(mccDepthParameters.iBoard);
			rawValue = MCCUtils.readVoltage(boardNum, sp.iChan, mccDepthParameters.range);
		} catch (MCCException e) {
			//			e.printStackTrace();
			setWarning("Error in depth readout : " + e.getMessage());
			return false;
		}
		setWarning(null);
		depth = rawValue * sp.scaleA + sp.scaleB;
		//		if (rawValue <= MccJniInterface.MCCERRORVALUE){
		//			errorCode = mccJniInterface.getLastErrorCode();
		//			String errorString = mccJniInterface.getErrorString(errorCode);
		//			System.out.println("Error in depth readout : " + errorString);
		//			return false;
		//		}
		return true;
	}

	private void setWarning(String msg) {
		if (msg == null) {
			if (depthWarning == null) {
				return;
			}
			else{
				WarningSystem.getWarningSystem().removeWarning(depthWarning);
				depthWarning = null;
			}
		}
		else {
			if (depthWarning == null) {
				depthWarning  = new PamWarning(depthControl.getUnitName(), msg, 2);
			}
			else {
				depthWarning.setWarningMessage(msg);
			}
			WarningSystem.getWarningSystem().addWarning(depthWarning);
		}
	}

	@Override
	public double getDepthRawData(int iSensor) {
		return rawValue;
	}

	@Override
	public double getDepth(int iSensor) {
		return depth;
	}

	public boolean shouldPoll() {
		// TODO Auto-generated method stub
		return false;
	}

	public Serializable getSettingsReference() {
		return mccDepthParameters;
	}

	public long getSettingsVersion() {
		return MccDepthParameters.serialVersionUID;
	}

	public String getUnitName() {
		return "MCC Depth system";
	}

	public String getUnitType() {
		return "MCC Depth system";
	}

	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		mccDepthParameters = ((MccDepthParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

}
