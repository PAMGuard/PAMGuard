package clickDetector.basicalgorithm;

import PamUtils.PamUtils;
import clickDetector.ClickControl;
import clickDetector.ClickDetector;
import clickDetector.basicalgorithm.plot.TriggerDataProviderFX;
import dataPlotsFX.data.TDDataProviderRegisterFX;

/**
 * Class to stack up blocks of data about the trigger background level and store them 
 * into sensible sized data units, i.e. about a minutes data in each data unit in order
 * to avoid too much overhead from the headers. 
 * @author Doug Gillespie
 *
 */
public class TriggerBackgroundHandler {

	private ClickDetector clickDetector;
	private ClickControl clickControl;
	private int channelMap;
	private int nChan;
	private double[] values;
	private long nextDataStoreTime = 0;
	private int doneChannelMap;
	
	private TriggerBackgroundDataBlock triggerBackgroundDataBlock;
	private TriggerBackgroundLogging logging;

	public TriggerBackgroundHandler(ClickDetector clickDetector) {
		this.clickDetector = clickDetector;
		this.clickControl = clickDetector.getClickControl();
		triggerBackgroundDataBlock = new TriggerBackgroundDataBlock("Trigger Background", 
				clickDetector, clickControl.getClickParameters().getChannelBitmap());
//		clickDetector.addOutputDataBlock(triggerBackgroundDataBlock);
		triggerBackgroundDataBlock.setBinaryDataSource(new TriggerBackgroundBinarySource(this, triggerBackgroundDataBlock));
		triggerBackgroundDataBlock.SetLogging(logging = new TriggerBackgroundLogging(this, triggerBackgroundDataBlock));
		TDDataProviderRegisterFX.getInstance().registerDataInfo(new TriggerDataProviderFX(this, triggerBackgroundDataBlock));
	}
	
	public void prepare() {
		channelMap = clickControl.getClickParameters().getChannelBitmap();
		triggerBackgroundDataBlock.setChannelMap(channelMap);
		nChan = PamUtils.getNumChannels(channelMap);
		values = new double[nChan];
		nextDataStoreTime = 0;
		doneChannelMap = 0;
		boolean has = clickDetector.hasOutputDatablock(triggerBackgroundDataBlock);
		if (clickControl.getClickParameters().storeBackground && has==false) {
			clickDetector.addOutputDataBlock(triggerBackgroundDataBlock);
		}
		else if (clickControl.getClickParameters().storeBackground == false && has) {
			clickDetector.removeOutputDatablock(triggerBackgroundDataBlock);
		}
		logging.prepare();
	}

	/**
	 * Add data to the background handler
	 * @param timeMillis data time in millis
	 * @param iChan channel number
	 * @param backgroundValue background value (raw units, generally  0 < value < 1)
	 */
	public void newValue(long timeMillis, int iChan, double backgroundValue) {
		if (timeMillis < nextDataStoreTime) {
			return;
		}
		int channelIndex = PamUtils.getChannelPos(iChan, channelMap);
		doneChannelMap |= (1<<iChan);
		values[channelIndex] = backgroundValue;
		if (doneChannelMap == channelMap) {
			TriggerBackgroundDataUnit du = new TriggerBackgroundDataUnit(timeMillis, channelMap, values);
			triggerBackgroundDataBlock.addPamData(du);
			nextDataStoreTime = timeMillis + (long) clickControl.getClickParameters().backgroundIntervalMillis;
			doneChannelMap = 0;
		}
		
	}

	/**
	 * @return the clickControl
	 */
	public ClickControl getClickControl() {
		return clickControl;
	}

	/**
	 * @return the triggerBackgroundDataBlock
	 */
	public TriggerBackgroundDataBlock getTriggerBackgroundDataBlock() {
		return triggerBackgroundDataBlock;
	}
	

}
