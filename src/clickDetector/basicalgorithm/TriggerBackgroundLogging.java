package clickDetector.basicalgorithm;

import java.sql.ResultSet;
import java.sql.Types;

import Acquisition.AcquisitionProcess;
import PamController.PamViewParameters;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import clickDetector.ClickControl;
import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class TriggerBackgroundLogging extends SQLLogging {

	private TriggerBackgroundHandler triggerBackgroundHandler;
	private TriggerBackgroundDataBlock triggerBackgroundDataBlock;
	private ClickControl clickControl;
	private PamTableItem[] channelItems;
	private int checkedChannelMap;
	private double[] viewerCalibrations;
	
	public TriggerBackgroundLogging(TriggerBackgroundHandler triggerBackgroundHandler, 
			TriggerBackgroundDataBlock triggerBackgroundDataBlock) {
		super(triggerBackgroundDataBlock);
		this.triggerBackgroundHandler = triggerBackgroundHandler;
		this.triggerBackgroundDataBlock = triggerBackgroundDataBlock;
		this.clickControl = triggerBackgroundHandler.getClickControl();
		prepare();
	}

	public void prepare() {
		/*
		 * Remake the table definition with the correct channels. 
		 */
		PamTableDefinition tableDef = new PamTableDefinition(clickControl.getUnitName() + " Background", SQLLogging.UPDATE_POLICY_WRITENEW);
		int channelMap = clickControl.getClickParameters().getChannelBitmap();
		int nChan = PamUtils.getNumChannels(channelMap);
		channelItems = new PamTableItem[nChan];
		for (int i = 0; i < nChan; i++) {
			int chan = PamUtils.getNthChannel(i, channelMap);
			String name = String.format("Ch%d", chan);
			channelItems[i] = new PamTableItem(name, Types.REAL);
			tableDef.addTableItem(channelItems[i]);
		}
		setTableDefinition(tableDef);
		
		checkTable(channelMap);
		
		
	}
	
	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#createViewResultSet(generalDatabase.PamConnection, PamController.PamViewParameters)
	 */
	@Override
	protected ResultSet createViewResultSet(PamConnection con, PamViewParameters pamViewParameters) {
		getViewerCalibrations();
		return super.createViewResultSet(con, pamViewParameters);
	}

	private void checkTable(int channelMap) {
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			return;
		}
		PamConnection pamCon = DBControlUnit.findConnection();
		if (pamCon == null) {
			return;
		}
		if (channelMap == checkedChannelMap) {
			// don't bother doing again. 
			return;
		}
		boolean OK = dbControl.getDbProcess().checkTable(getTableDefinition());
		if (OK) {
			checkedChannelMap = channelMap;
		}
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		TriggerBackgroundDataUnit tbdu = (TriggerBackgroundDataUnit) pamDataUnit;
		double[] absData = tbdu.getAbsoluteAmplitudes();
		if (absData.length != channelItems.length) {
			for (int i = 0; i < channelItems.length; i++) {
				channelItems[i].setValue(null);
			}
		}
		else {
			for (int i = 0; i < channelItems.length; i++) {
				channelItems[i].setValue(new Float(absData[i]));
			}
		}
	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#createDataUnit(generalDatabase.SQLTypes, long, int)
	 */
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		if (channelItems == null) {
			return null;
		}
		int nChan = channelItems.length;
		Float absData;
		double[] rawData = new double[nChan];
		double[] absAmplitudes = new double[nChan];
		
		if (viewerCalibrations == null || viewerCalibrations.length != nChan) {
			getViewerCalibrations();
			if (viewerCalibrations == null || viewerCalibrations.length != nChan) {
				return null;
			}
		}

		int channelMap = clickControl.getClickParameters().getChannelBitmap();
		for (int i = 0; i < nChan; i++) {
			absData = (Float) channelItems[i].getValue();
			if (absData != null && Float.isFinite(absData)) {
				rawData[i] = Math.pow(10, (absData-viewerCalibrations[i]) / 20.);	
				absAmplitudes[i] = absData;
			}
			else {
				absAmplitudes[i] = Double.NaN;
			}
		}
		TriggerBackgroundDataUnit tbdu = new TriggerBackgroundDataUnit(timeMilliseconds, channelMap, rawData);	
		tbdu.setAbsoluteAmplitudes(absAmplitudes);
		return tbdu;
	}

	private void getViewerCalibrations() {
		PamProcess sourceProcess = triggerBackgroundDataBlock.getSourceProcess();
		if (sourceProcess instanceof AcquisitionProcess == false) {
			viewerCalibrations = null;
			return;
		}
		AcquisitionProcess daqProcess = (AcquisitionProcess) sourceProcess; 
		int channelMap = clickControl.getClickParameters().getChannelBitmap();
		int nChan = PamUtils.getNumChannels(channelMap);
		viewerCalibrations = new double[nChan];
		for (int i = 0; i < nChan; i++) {
			int chan = PamUtils.getNthChannel(i, channelMap);
			viewerCalibrations[i] = daqProcess.rawAmplitude2dB(1., chan, false);
		}
	}

}
