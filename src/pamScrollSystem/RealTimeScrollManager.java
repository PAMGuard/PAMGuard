package pamScrollSystem;

import PamguardMVC.PamDataBlock;

public class RealTimeScrollManager extends AbstractScrollManager {

	@Override
	public long checkMaximumTime(long requestedTime) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long checkMinimumTime(long requestedTime) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void moveInnerScroller(AbstractPamScroller scroller, long newValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveOuterScroller(AbstractPamScroller scroller, long newMin,
			long newMax) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyModelChanged(int changeType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startDataAt(PamDataBlock dataBlock, long menuMouseTime, boolean immediateLoad) {
		// TODO Auto-generated method stub

	}

	@Override
	public void centreDataAt(PamDataBlock dataBlock, long menuMouseTime) {
		// TODO Auto-generated method stub
	
	}

	@Override
	public void reLoad() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long checkGapPos(AbstractPamScroller abstractPamScroller,
			long oldMin, long oldMax, long newMin, long newMax, int i) {
		return newMin;
	}

	@Override
	public void scrollToTime(PamDataBlock dataBlock, long menuMouseTime) {
		// TODO Auto-generated method stub
		
	}

}
