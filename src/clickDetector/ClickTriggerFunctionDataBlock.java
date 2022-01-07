package clickDetector;

import PamguardMVC.PamRawDataBlock;

public class ClickTriggerFunctionDataBlock extends PamRawDataBlock {

	private ClickDetector clickDetector;
	
	public ClickTriggerFunctionDataBlock(String name, ClickDetector clickDetector, int channelMap, float sampleRate) {
		super(name, clickDetector, channelMap, sampleRate, false);
		// TODO Auto-generated constructor stub
		this.clickDetector = clickDetector;
		new TriggerFunctionDisplay(this);
	}

	public ClickDetector getClickDetector() {
		return clickDetector;
	}

}
