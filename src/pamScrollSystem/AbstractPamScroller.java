package pamScrollSystem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Timer;

import PamController.PamController;
import PamController.PamControllerInterface;
import PamUtils.PamCalendar;
import PamView.PamMenuParts;
import PamguardMVC.PamDataBlock;
import pamScrollSystem.coupling.CouplingParams;
import pamScrollSystem.coupling.ScrollerCoupling;

public abstract class AbstractPamScroller implements DataTimeLimits {

	/**
	 * reference to the global scroll manager. 
	 */
	protected AbstractScrollManager scrollManager;

	transient protected Vector<PamScrollObserver> observers = new Vector<PamScrollObserver>();

	transient protected Vector<PamDataBlock> usedDataBlocks = new Vector<PamDataBlock>();
	
	transient protected Vector<PamMenuParts> pamMenuParts = new Vector<PamMenuParts>();

	protected PamScrollerData scrollerData = new PamScrollerData();

	protected int orientation;

	protected boolean needsNotify;

	private ScrollerCoupling scrollerCoupling;

	private Timer playTimer;

	private Object playbackSynch = new Object();
	
	/*
	 * List of play speeds for a quick menu
	 */
	protected double[] playSpeeds = {.1, 0.25, .5, 1.0, 2, 5, 10};


	public AbstractPamScroller(String name, int orientation, int stepSizeMillis, long defaultLoadTime, boolean hasMenu){

		this.scrollerData.name = new String(name);
		this.scrollerData.setStepSizeMillis(stepSizeMillis);
		this.scrollerData.defaultLoadtime = defaultLoadTime;
		this.orientation=orientation;
		scrollManager = AbstractScrollManager.getScrollManager();
		scrollManager.addPamScroller(this);

	}


	public void pageForward() {
		long range = scrollerData.maximumMillis - scrollerData.minimumMillis;
		long step = range * scrollerData.pageStep / 100;
		long currVal = getValueMillis();
		long newMin = scrollerData.minimumMillis + step;
		long newMax = scrollerData.maximumMillis + step;
		newMin = scrollManager.checkGapPos(this, scrollerData.minimumMillis, scrollerData.maximumMillis, newMin, newMax, +1);
		scrollerData.minimumMillis = newMin;
		scrollerData.maximumMillis = newMin + range;

		scrollerData.maximumMillis = scrollManager.checkMaximumTime(scrollerData.maximumMillis);
		scrollerData.minimumMillis = scrollerData.maximumMillis - range;
		rangesChangedF(currVal);
	}

	public void pageBack() {
		long range = scrollerData.maximumMillis - scrollerData.minimumMillis;
		long step = range * scrollerData.pageStep / 100;
		long currVal = getValueMillis();
		long newMin = scrollerData.minimumMillis - step;
		long newMax = scrollerData.maximumMillis - step;
		newMin = scrollManager.checkGapPos(this, scrollerData.minimumMillis, scrollerData.maximumMillis, newMin, newMax, -1);
		scrollerData.minimumMillis = newMin;
		scrollerData.maximumMillis = newMin + range;

		scrollerData.minimumMillis = scrollManager.checkMinimumTime(scrollerData.minimumMillis);
		scrollerData.maximumMillis = scrollerData.minimumMillis + range;
		rangesChangedF(currVal);
	}

	public void destroyScroller() {
		scrollManager.removePamScroller(this);
	}

	/**
	 * Ad an observer that will receive notifications when the
	 * the scroller moves.  
	 * @param pamScrollObserver 
	 */
	public void addObserver(PamScrollObserver pamScrollObserver) {
		if (observers.indexOf(pamScrollObserver) < 0) {
			observers.add(pamScrollObserver);
		}
	}

	/**
	 * Send notification to all observers of this scroll bar to say
	 * that the value set by the slider in the scroll bar has changed. 
	 */
	protected void notifyValueChange() {
		/*
		 * always set the calendar position to that of the latest
		 * scroll bar to move !
		 */
		PamCalendar.setViewPosition(getValueMillis());
		for (int i = 0; i < observers.size(); i++) {
			observers.get(i).scrollValueChanged(this);
		}
		notifyCoupledScrollers();
		/*
		 *  only put this out in viewer mode since it's causing trouble in real time
		 *  with modules which were lazily written and reset themselves everytime
		 *  ANY notification arrives. 
		 */
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			PamController.getInstance().notifyModelChanged(PamControllerInterface.NEW_SCROLL_TIME);
		}
	}

	/**
	 * Remove an observer which no longer requires notifications when
	 * the scroller moves. 
	 * @param pamScrollObserver
	 */
	public void removeObserver(PamScrollObserver pamScrollObserver) {
		observers.remove(pamScrollObserver);
	}

	/**
	 * Add a datablock to the list for this scroller. 
	 * <p>
	 * When the scroller is moved, data from data blocks in 
	 * this list will re read from the database and binary stores. 
	 * Other data will not be read. 
	 * @param dataBlock a PamDataBlock
	 */
	public void addDataBlock(PamDataBlock dataBlock) {
		if (dataBlock == null) return;
		if (usedDataBlocks.indexOf(dataBlock) < 0) {
			usedDataBlocks.add(dataBlock);
		}
	}

	/**
	 * Remove a datablock from the viewed list. 
	 * @param dataBlock a PamDataBlock
	 */
	public void removeDataBlock(PamDataBlock dataBlock) {
		if (dataBlock == null) return;
		usedDataBlocks.remove(dataBlock);
	}

	/**
	 * Remove all datablocks from the viewed list. 
	 */
	public void removeAllDataBlocks() {
		usedDataBlocks.removeAllElements();
	}

	/**
	 * See if this scroller is using a particular data block
	 * @param dataBlock a Pamguard data block
	 * @return true if it's being used. 
	 */
	public final boolean isDataBlockUsed(PamDataBlock dataBlock) {
		return (usedDataBlocks.indexOf(dataBlock) >= 0);
	}
	/**
	 * Another managed scroller moved it's position
	 * @param newValue new value in millis
	 */
	abstract public void anotherScrollerMovedInner(long newValue);

	/**
	 * Another managed scroller moved its outer position - will cause
	 * new data to be loaded. 
	 * @param newMin
	 * @param newMax
	 */
	public void anotherScrollerMovedOuter(long newMin, long newMax) {
		// basically, this scroller has to remain around 
		// or within the one which has just moved. 
		long thatLen = newMax-newMin;
		long thisLen = scrollerData.getLength();
		if (thisLen == thatLen && scrollerData.minimumMillis != newMin) {
			setRangeMillis(newMin, newMax, false);
		}
		else if (thisLen > thatLen) {
			if (scrollerData.minimumMillis > newMin) {
				setRangeMillis(newMin, newMin+thisLen, false);
			}
			else if (scrollerData.maximumMillis < newMax) {
				setRangeMillis(newMax-thisLen, newMax, false);
			}
		}
		else {
			if (scrollerData.minimumMillis < newMin) {
				setRangeMillis(newMin, newMin+thisLen, false);
			}
			else if (scrollerData.maximumMillis > newMax) {
				setRangeMillis(newMax-thisLen, newMax, false);
			}			
		}
	}

	/**
	 * Send a notification to all observers of this scroller to say
	 * that the range of data loaded has changed. 
	 */
	public void notifyRangeChange() {
		if (PamCalendar.getTimeInMillis() <= 0) {
			PamCalendar.setViewPosition(getValueMillis());
		}
		notifyValueChange();
		for (int i = 0; i < observers.size(); i++) {
			observers.get(i).scrollRangeChanged(this);
		}
	}
	/**
	 * @return the minimumMillis - the minimum of loaded data
	 */
	public long getMinimumMillis() {
		return scrollerData.minimumMillis;
	}

	/**
	 * @return the maximumMillis - the maximum of loaded data
	 */
	public long getMaximumMillis() {
		return scrollerData.maximumMillis;
	}

	/**
	 * @return the difference between getMaximumMills and getMinimumMillis
	 */
	public long getRangeMillis() {
		return scrollerData.maximumMillis-scrollerData.minimumMillis;
	}

	/**
	 * Set the range of the currently loaded data and optionally notify other
	 * scrollers. 
	 * @param minimumMillis minimum time in milliseconds
	 * @param maximumMillis maximum time in milliseconds
	 * @param notify notify the rangesChanged function. 
	 */
	public void setRangeMillis(long minimumMillis, long maximumMillis, boolean notify) {
		//		System.out.println("AbstrractPamScroller: setRangeMillis(): " + minimumMillis +  " "+maximumMillis);
		scrollerData.minimumMillis = minimumMillis;
		scrollerData.maximumMillis = maximumMillis;
		//		if (getValueMillis() < minimumMillis) {
		//			setValueMillis(scrollerData.minimumMillis);
		//		}
		if (notify) {
			rangesChangedF(getValueMillis());
		}
		else {
			needsNotify = true;
		}
	}

	/**
	 * called when the set range is changed with a flag to send out a
	 * notification. Does some things that must be done, but also 
	 * calls an abstract setRanges in order that specific scrollers
	 * can update their scrolling component. 
	 * @param setValue scroller position in milliseconds. 
	 */
	protected final void rangesChangedF(long setValue) {
		rangesChanged(setValue);
		setValueMillis(setValue);
		notifyCoupledScrollers();
		scrollManager.moveOuterScroller(this, getMinimumMillis(), getMaximumMillis());
	}

	/**
	 * @param visibleAmount the visibleAmount to set in millis
	 */
	public void setVisibleMillis(long visibleAmount) {
	}

	/**
	 * Called when ranges have been changed and tells 
	 * scroller to go to a particular absolute value. 
	 * @param setValue
	 */
	abstract public void rangesChanged(long setValue);

	/**
	 * Command passed through the the scroll manager telling it reload data. 
	 */
	public void reLoad() {
		scrollManager.reLoad();
	}

	//	/**
	//	 * Set the maximum of the range of the scroller in milliseconds. 
	//	 * @param millis milliseconds (using standard Jva millisecond time)
	//	 */
	//	public void setMaximumMillis(long millis) {
	//		scrollerData.maximumMillis = millis;
	//	}
	//	
	//	/**
	//	 * Set the maximum of the range of the scroller in milliseconds. 
	//	 * @param millis milliseconds (using standard Jva millisecond time)
	//	 */
	//	public void setMinimumMillis(long millis) {
	//		scrollerData.minimumMillis = millis;
	//	}

	/**
	 * The start time of the scroller in milliseconds. This is the current position 
	 * of the scroller NOT the start time of loaded data. 
	 * @return the valueMillis
	 */
	abstract public long getValueMillis();

	/**
	 * This will be none for sliders, such as on the map. For other scrollers it
	 * should be the same as the time displayed in to the navigaiton buttons of the scroller. 
	 * @return the visible display time in milliseconds. 
	 */
	abstract public long getVisibleAmount();
	
	/**
	 * The end of the current visible screen which is 
	 * getValueMillis() + getVisibleAmount();
	 * @return end of visible time
	 */
	public long getVisibleEnd() {
		return getValueMillis() + getVisibleAmount();
	}

	/**
	 * @param valueMillis the valueMillis to set
	 */
	final public void setValueMillis(long valueMillis) {
		valueSetMillis(valueMillis);
		notifyCoupledScrollers();
	}

	/**
	 * Called when a new position has been set
	 * @param valueMillis new scroll value in milliseconds
	 */
	abstract public void valueSetMillis(long valueMillis) ;

	/**
	 * stepSizeMillis is the resolution of the scroller in milliseconds. 
	 * <p>For displays which will only ever display a short amount of data
	 * this can be one, however for longer displays this should be 1000 (a second)
	 * or more to avoid wrap around of the 32 bit integers used to control the 
	 * actual scroll bar. 
	 * @return the stepSizeMillis
	 */
	public int getStepSizeMillis() {
		return scrollerData.getStepSizeMillis();
	}

	/**
	 * @param stepSizeMillis the stepSizeMillis to set
	 */
	public void setStepSizeMillis(int stepSizeMillis) {
		scrollerData.setStepSizeMillis(Math.max(1, stepSizeMillis));
	}

	//	/**
	//	 * @return the blockIncrement
	//	 */
	//	public int getBlockIncrement() {
	//		return 1;
	//	}

	/**
	 * @param blockIncrement the blockIncrement to set in millis
	 */
	public void setBlockIncrement(long blockIncrement) {
	}
	//
	//	/**
	//	 * @return the unitIncrement
	//	 */
	//	public long getUnitIncrement()

	/**
	 * @param unitIncrement the unitIncrement to set in millis
	 */
	public void setUnitIncrement(long unitIncrement) {
	}

	/**
	 * @return the observers
	 */
	public Vector<PamScrollObserver> getObservers() {
		return observers;
	}

	/**
	 * @return the pageStep - the size to move when loading forward or backward. 
	 */
	public int getPageStep() {
		return scrollerData.pageStep;
	}

	/**
	 * @param pageStep the pageStep to set
	 */
	public void setPageStep(int pageStep) {
		scrollerData.pageStep = pageStep;
	}

	public long getDefaultLoadtime() {
		return scrollerData.defaultLoadtime;
	}

	protected void setDefaultLoadtime(long defaultLoadtime) {
		scrollerData.defaultLoadtime = defaultLoadtime;
	}

	/**
	 * @return the scrollManager
	 */
	public AbstractScrollManager getScrollManager() {
		return scrollManager;
	}

	/**
	 * Couple this scroller to another scroller so that both
	 * have exactly the same behaviour, load the same data period, 
	 * move their scrolls together, etc. 
	 * <p>
	 * Scollers are coupled by name so that they don't necessarily
	 * need to find references to each other in the code. These names 
	 * can be anything by measures should be taken to ensure that they
	 * are going to be unique, for example by using module names as
	 * part of the coupling name.  
	 * @param couplingName name of the coupling
	 * @return number of other scrollers in this coupling
	 */
	public ScrollerCoupling coupleScroller(String couplingName) {
		uncoupleScroller();
		return scrollerCoupling = scrollManager.coupleScroller(this, couplingName);
	}

	/**
	 * Remove the scroller from it's coupling. 
	 */
	public void uncoupleScroller() {
		scrollManager.uncoupleScroller(this);
		scrollerCoupling = null;
	}

	public ScrollerCoupling getScrollerCoupling() {
		return scrollerCoupling;
	}

	public void setScrollerCoupling(ScrollerCoupling scrollerCoupling) {
		this.scrollerCoupling = scrollerCoupling;
	}

	/**
	 * Called when a scroller which is coupled to this scroller changes
	 * in any way. 
	 * @param scrollerCoupling 
	 * @param scroller coupled scroller which changes. 
	 */
	public void coupledScrollerChanged(ScrollerCoupling scrollerCoupling, AbstractPamScroller scroller) {
		CouplingParams params = scrollerCoupling.getCouplingParams();
		int type = params.couplingType;
		switch(type) {
		case CouplingParams.COUPLING_NONE:
			return;
		case CouplingParams.COUPLING_START:
			scrollerData = scroller.scrollerData.clone();
			setValueMillis(scroller.getValueMillis());
			//			rangesChanged(scroller.getValueMillis());
			break;
		case CouplingParams.COUPLING_MIDDLE:
			scrollerData = scroller.scrollerData.clone();
			long start = scroller.getValueMillis();
			long page = scroller.getVisibleAmount();
			long middle = start + page/2;
			long thisPage = getVisibleAmount();
			long thisStart = middle-thisPage/2;
			thisStart = correctStartTime(thisStart);
			setValueMillis(thisStart);
			break;
		case CouplingParams.COUPLING_RANGE:
			coupleScrollRange(scrollerCoupling, scroller);
			break;
		}
	}

	/**
	 * More complex situation, so keep the shortes display overlapped with the longer one. 
	 * Have to think of some rules !
	 * @param scrollerCoupling
	 * @param scroller
	 */
	private void coupleScrollRange(ScrollerCoupling scrollerCoupling, AbstractPamScroller scroller) {
		CouplingParams params = scrollerCoupling.getCouplingParams();
		long otherStart = scroller.getValueMillis();
		long otherEnd = scroller.getValueMillis()+scroller.getVisibleAmount();
		long thisStart = getValueMillis();
		long thisEnd = getValueMillis()+getVisibleAmount();
		if (thisStart >= otherStart && thisEnd <= otherEnd) {
			/*
			 * This is contained within other, so no need to do anything. 
			 */
			//			System.out.println("This contained in other");
			return;
		}
		else if (otherStart > thisStart && otherEnd < thisEnd) {
			/**
			 * Other is contained within this, so still no need to 
			 * do anything
			 */
			//			System.out.println("Other contained in this");
			return;
		}
		else if (otherEnd > thisEnd) {
			/*
			 * Not contained, and the other one starts after this, which means
			 * the other one is moving forwards, so line up the starts ...
			 */
			//			System.out.println("OtherEnd >thisEnd");
			long newStart = Math.min(otherStart, otherEnd-this.getVisibleAmount());
			setValueMillis(correctStartTime(newStart));
			return;
		}
		else if (otherStart < thisStart) {
			//			System.out.println("thisStart < otherStart");
			// must have moved back in time. 
			long newStart = Math.max(otherStart, otherEnd-this.getVisibleAmount());
			setValueMillis(correctStartTime(newStart));
			return;
		}
		else {
			//			System.out.println("No logic");
		}
	}

	private long correctStartTime(long thisStart) {
		long dataStart = scrollerData.minimumMillis;
		long dataEnd = scrollerData.maximumMillis - getVisibleAmount();
		thisStart = Math.max(dataStart, thisStart);
		thisStart = Math.min(thisStart, dataEnd);
		return thisStart;
	}


	/**
	 * Tell other scrollers coupled to this one that there has been a change 
	 */
	private void notifyCoupledScrollers() {
		if (scrollerCoupling != null) {
			scrollerCoupling.notifyOthers(this);
		}
	}

	/**
	 * @return the number of data blocks observed by this scroller
	 */
	public int getNumUsedDataBlocks() {
		return usedDataBlocks.size();
	}

	/**
	 * Get a specific data block observed by this scroller. 
	 * @param iBlock block index
	 * @return reference to an observed datablock 
	 */
	public PamDataBlock getUsedDataBlock(int iBlock) {
		return usedDataBlocks.get(iBlock);
	}

	/**
	 * Check if a data block is within the special data block list. The special data
	 * block list is a list of data blocks that load data regardless of a
	 * subscription to a scroller.
	 * 
	 * @param pamDataBlock - the data block to test
	 * @return true if in the special list.
	 */
	public boolean isInSpecialList(PamDataBlock pamDataBlock){
		return scrollManager.isInSpecialList(pamDataBlock);
	}

	/**
	 * Get the load times for a special data block based on current load times.
	 * Unless specifically set when added to special data block list the default is
	 * Long.MIN_VALUE and Long.MAX_VALUE.
	 * 
	 * @param pamDataBlock - the data block to grab. 
	 * @param minimumMillis - the minimum time of the current scroller.
	 * @param maximumMillis - the maximum time of the current scroller
	 * @return the load time of the special data block.
	 */
	public long[] getSpecialLoadTimes(PamDataBlock pamDataBlock, long minimumMillis, long maximumMillis) {
		return scrollManager.getSpecialLoadTimes(pamDataBlock, minimumMillis,  maximumMillis);
	}

	/**
	 * Get scroller data. 
	 * @return the current scroller data. 
	 */
	public PamScrollerData getScrollerData() {
		return this.scrollerData;
	}

	public void startPlayback() {
		synchronized(playbackSynch) {
			if (playTimer != null && playTimer.isRunning()) {
				return;
			}
			int step = Math.max((int) (this.getStepSizeMillis() / getScrollerData().getPlaySpeed()), 1);
			playTimer = new Timer(step, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					playTimerAction();
				}
			});
			playTimer.start();
			playbackStarted();
		}
	}



	public void stopPlayback() {
		synchronized(playbackSynch) {
			if (playTimer == null) {
				return;
			}
			playTimer.stop();
			playTimer= null;
			playbackStopped();
		}
	}


	public void playbackStopped() {
		// TODO Auto-generated method stub

	}

	public void playbackStarted() {
		// TODO Auto-generated method stub

	}


	protected void playTimerAction() {
		synchronized(playbackSynch) {
			if (playTimer == null) {
				return;
			}
			int timerInterval = playTimer.getDelay();
			long pos = getValueMillis();
			if (pos >= getMaximumMillis()-getVisibleAmount()) {
				stopPlayback();
				return;
			}
			long step = Math.max((long) (timerInterval * scrollerData.getPlaySpeed()), 1);
			setValueMillis(pos+step);
			if (pos == getValueMillis()) {
				playTimer.setDelay(playTimer.getDelay()*2);
			}
		}
	}

	/**
	 * Add menuparts, items that will be added to the menus
	 * little popup menu that appears from the middle of the 
	 * scroller controls. 
	 * @param menuParts
	 */
	public void addMenuParts(PamMenuParts menuParts) {
		this.pamMenuParts.add(menuParts);
	}
	/**
	 * Get menu parts to add to the menu from the scroller right click
	 * @return the pamMenuParts
	 */
	public Vector<PamMenuParts> getPamMenuParts() {
		return pamMenuParts;
	}

	public boolean isShowing() {
		return false;
	}


	@Override
	public String toString() {
		String str = String.format("Scroller '%s' is %s, load %s-%s visible %s-%s", getScrollerData().name, isShowing() ? "Showing" : "Hidden",
				PamCalendar.formatTime(getMinimumMillis()), PamCalendar.formatTime(getMaximumMillis()), 
				PamCalendar.formatTime(getValueMillis()), PamCalendar.formatTime(getValueMillis()+getVisibleAmount()));
		return str;
	}

}
