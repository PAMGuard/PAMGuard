package targetMotionModule;

import PamView.GeneralProjector;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

@SuppressWarnings("rawtypes")
public class TargetMotionDataBlock extends PamDataBlock<TargetMotionResult>{

	public TargetMotionDataBlock(Class unitClass, String dataName,
			PamProcess parentProcess, int channelMap){
		super(TargetMotionResult.class, dataName, parentProcess, channelMap);	
	}
	
	@Override
	public boolean canDraw (GeneralProjector genProjector){
		return super.canDraw(genProjector);
	}

	@Override
	public boolean shouldNotify() {
		return false;
	}

}
