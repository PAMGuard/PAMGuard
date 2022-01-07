package clickDetector.background;

import PamguardMVC.background.BackgroundBinaryWriter;
import PamguardMVC.background.BackgroundDataBlock;
import PamguardMVC.background.BackgroundManager;
import clickDetector.ClickBackgroundJSONDataSource;
import clickDetector.ClickDetector;

public class ClickBackgroundManager extends BackgroundManager<ClickBackgroundDataUnit> {

	private ClickBackgroundDataBlock clickBackgroundDataBlock;
	
	private ClickBackgroundBinaryWriter clickBackgroundBinaryWriter;
	
	public ClickBackgroundManager(ClickDetector ClickDetector) {
		super(ClickDetector, ClickDetector.getClickDataBlock());
		clickBackgroundDataBlock = new ClickBackgroundDataBlock(this);
		clickBackgroundDataBlock.setJSONDataSource(new ClickBackgroundJSONDataSource());
		clickBackgroundBinaryWriter = new ClickBackgroundBinaryWriter(this);
	}

	@Override
	public BackgroundDataBlock getBackgroundDataBlock() {
		return clickBackgroundDataBlock;
	}

	@Override
	public BackgroundBinaryWriter getBackgroundBinaryWriter() {
		return clickBackgroundBinaryWriter;
	}

}
