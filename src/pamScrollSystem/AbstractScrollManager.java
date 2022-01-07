package pamScrollSystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JPopupMenu;

import dataMap.OfflineDataMap;
import pamScrollSystem.coupling.ScrollerCoupling;
import PamController.PamController;
import PamguardMVC.PamDataBlock;

public abstract class AbstractScrollManager {

	protected Vector<AbstractPamScroller> pamScrollers = new Vector<AbstractPamScroller>();
	
	private Vector<ScrollerCoupling> scrollerCouplings = new Vector<ScrollerCoupling>();
	
	/**
	 * Some data blocks. e.g. the hydrophone datablock, are not associated with a scroller however still need to be loaded in viewer mode. 
	 * If a datablock is within specialDataBlocks it will be loaded regardless if associated with a
	 * scroller or not. 
	 */
	transient private ArrayList<SpecialDataBlockInfo> specialDataBlocks=new ArrayList<SpecialDataBlockInfo>();
	
	private static AbstractScrollManager singleInstance;
	
	protected AbstractScrollManager() {
		
	}
	
	public static synchronized AbstractScrollManager getScrollManager() {
		if (singleInstance == null) {
			int runMode = PamController.getInstance().getRunMode();
			
			switch(runMode) {
			case PamController.RUN_PAMVIEW:
				singleInstance = new ViewerScrollerManager();
				break;
			default:
				singleInstance = new RealTimeScrollManager();	
			}
		}
		return singleInstance;
	}
	
	/**
	 * Add a new scroller to the managed list. 
	 * @param abstractPamScroller scroller to add
	 */
	public void addPamScroller(AbstractPamScroller abstractPamScroller) {
		if (pamScrollers.indexOf(abstractPamScroller) < 0) {
			pamScrollers.add(abstractPamScroller);
		}
	}
	
	/**
	 * Remove a pam scroller from the managed list. 
	 * @param abstractPamScroller scroller to remove
	 */
	public void removePamScroller(AbstractPamScroller abstractPamScroller) {
		pamScrollers.remove(abstractPamScroller);
	}
	
	/**
	 * Move the scroll bar component of a scroller. This should not cause the
	 * reloading of any data, but other scroll bars should be notified of 
	 * any changes. 
	 * @param scroller scroller that moved
	 * @param newValue new value (time in milliseconds). 
	 */
	abstract public void moveInnerScroller(AbstractPamScroller scroller, long newValue);
	
	/**
	 * Move the data load component of a scroller. This should cause data to be 
	 * reloaded and will need to notify all other scrollers incase they also 
	 * need to shuffle along a bit. 
	 * @param scroller scroller that changed
	 * @param newMin new data min value in millis
	 * @param newMax new data max value in millis
	 */
	abstract public void moveOuterScroller(AbstractPamScroller scroller, long newMin, long newMax);

	/**
	 * Check the maximum time requested by a scroll bar doesn't go beyond the end of the data
	 * @param requestedTime requested time in millis.
	 * @return the minimum of the requested time and the actual end time of the data
	 */
	abstract public long checkMaximumTime(long requestedTime);

	/**
	 * Check the minimum time requested by a scroll bar doesn't go below the start of the data
	 * @param requestedTimerequested time in millis.
	 * @return the maximum of the requested time and the actual start time of the data
	 */
	abstract public long checkMinimumTime(long requestedTime);

	abstract public void notifyModelChanged(int changeType);

	/**
	 * Centre all data in all data blocks at the given time
	 * @param menuMouseTime time in milliseconds
	 */
	abstract public void centreDataAt(PamDataBlock dataBlock, long menuMouseTime);

	/**
	 * Start all data in all data blocks at the given time
	 * @param dataBlock
	 * @param menuMouseTime time in milliseconds
	 * @param immediateLoad load data immediately in current thread. Don't re-schedule for later. 
	 */
	abstract public void startDataAt(PamDataBlock dataBlock, long menuMouseTime, boolean immediateLoad);
	
	final public void startDataAt(PamDataBlock dataBlock, long menuMouseTime) {
		startDataAt(dataBlock, menuMouseTime, false);
	}
	
	

	/**
	 * Couple a scroller to another scroller so that both
	 * have exactly the same behaviour, load the same data period, 
	 * move their scrolls together, etc. 
	 * <p>
	 * Scollers are coupled by name so that they don't necessarily
	 * need to find references to each other in the code. These names 
	 * can be anything by measures should be taken to ensure that they
	 * are going to be unique, for example by using module names as
	 * part of the coupling name.  
	 * @param abstractPamScroller scroller to couple
	 * @param couplingName coupling name
	 * @return reference to the coupler
	 */
	public ScrollerCoupling coupleScroller(AbstractPamScroller abstractPamScroller, String couplingName) {
		ScrollerCoupling coupling = findCoupling(couplingName, true);
		coupling.addScroller(abstractPamScroller);
		return coupling;
	}

	/**
	 * Uncouple a scroller. 
	 * @param abstractPamScroller scroller to uncouple
	 */
	public void uncoupleScroller(AbstractPamScroller abstractPamScroller) {
		ScrollerCoupling aCoupling = abstractPamScroller.getScrollerCoupling();
		if (aCoupling == null) {
			return;
		}
		aCoupling.removeScroller(abstractPamScroller);
		if (aCoupling.getScrollerCount() == 0) {
			scrollerCouplings.remove(aCoupling);
		}
	}	
	
	/**
	 * Find a scroller coupling with a given name
	 * @param name name of coupling
	 * @param autoCreate flag to automatically create a coupling if one isn't found. 
	 * @return the scroller coupling or null if none was found and the autoCreate flag was false. 
	 */
	public ScrollerCoupling findCoupling(String name, boolean autoCreate) {
		ScrollerCoupling aCoupling = null;
		for (int i = 0; i < scrollerCouplings.size(); i++) {
			aCoupling = scrollerCouplings.get(i);
			if (aCoupling.getName().equals(name)) {
				return aCoupling;
			}
		}
		// if it get's here, then no coupling was found
		if (autoCreate) {
			scrollerCouplings.add(aCoupling = new ScrollerCoupling(name));
		}
		return aCoupling;
	}
	
	/**
	 * Command telling manager to reload it's data. 
	 */
	public abstract void reLoad();


	/**
	 * Force all the scroller to move so that they bracket minTime -> maxTime.
	 * This is used for  modules which don't have their own scroller. 
	 * @param minTime
	 * @param maxTime
	 */
	public void moveAllScrollersTo(long minTime, long maxTime){
		AbstractPamScroller aScroller;
		for (int i = 0; i < pamScrollers.size(); i++) {
			aScroller = pamScrollers.get(i);
			aScroller.anotherScrollerMovedOuter(minTime, maxTime);
		}
	}
	
	/**
	 * Work out whether or not a particular time falls in the 
	 * gap between points in a datamap .
	 * @param dataBlock Pamguard data block
	 * @param timeMillis time in milliseconds
	 * @return true if the data are in a gap.
	 */
	public int isInGap(PamDataBlock dataBlock, long timeMillis) {
		OfflineDataMap dataMap = dataBlock.getPrimaryDataMap();
		if (dataMap == null) {
			return -1;
		}
		return dataMap.isInGap(timeMillis);
	}
	

	
	/**
	 * Check to see whether or not we are scrolling into a data gap. 
	 * Rules exist for stopping  / starting / jumping over gaps 
	 * depending on the current state and the new position of 
	 * the scroller. 
	 * @param abstractPamScroller PamScroller that moved
	 * @param oldMin old minimum time
	 * @param oldMax old maximum time
	 * @param newMin new minimum time
	 * @param newMax new maximum time
	 * @param direction direction of scroll +1 = forward, -1 = backward, 0 = plonked down by mouse on datamap. 
	 * @return new minimum position. Calling function must then work out the new maximum position. 
	 */
	public abstract long checkGapPos(AbstractPamScroller abstractPamScroller,
			long oldMin, long oldMax, long newMin, long newMax, int direction);
	
	/**
	 * Check if a data block is within the special data block list. 
	 * @param pamDataBlock
	 * @return true if the datablock is in the list. 
	 */
	public boolean isInSpecialList(PamDataBlock pamDataBlock){
		
		for (int i=0; i<specialDataBlocks.size(); i++){
			if (pamDataBlock == specialDataBlocks.get(i).pamDataBlock) return true;
			// should probably just compare the references rather than their toString() values !
//			if (pamDataBlock.equals(specialDataBlocks.get(i))) return true; 
		}
		
		return false;
	}
	
	/**
	 * Remove from special data block list.
	 * @param pamDataBlock
	 */
	public void removeFromSpecialDatablock(PamDataBlock pamDataBlock){
		
		Iterator<SpecialDataBlockInfo> it = specialDataBlocks.iterator();
		while(it.hasNext()) {
			if (it.next().pamDataBlock == pamDataBlock) {
				it.remove();
			}
		}
		// probably not comparing as strings as in this old code below. 
//		for (int i=0; i<specialDataBlocks.size(); i++){
//			if (pamDataBlock.equals(specialDataBlocks.get(i))){
//				specialDataBlocks.remove(i);
//				return; 
//			} 
//		}
	}
	
	/**
	 * Add a data block to the special data block list. The special block list always get's loaded in 
	 * viewer even if it's not displaying. this is used for any super detection datablocks, so that if 
	 * their sub detections ARE displaying, then their supers are also available. 
	 * @param pamDataBlock
	 */
	public void addToSpecialDatablock(PamDataBlock pamDataBlock){
		specialDataBlocks.add(new SpecialDataBlockInfo(pamDataBlock));
	}
	
	/**
	 * Add a data block to the special data block list. The special block list always get's loaded in 
	 * viewer even if it's not displaying. this is used for any super detection datablocks, so that if 
	 * their sub detections ARE displaying, then their supers are also available. 
	 * @param pamDataBlock
	 * @param timeBefore
	 * @param timeAfter
	 */
	public void addToSpecialDatablock(PamDataBlock pamDataBlock, long timeBefore, long timeAfter){
		specialDataBlocks.add(new SpecialDataBlockInfo(pamDataBlock, timeBefore, timeAfter));
	}

	public JPopupMenu getStandardOptionsMenu(AbstractPamScroller pamScroller) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Holds a reference to a data block and some extra information on how that data block should be loaded. 
	 * <p>
	 * Special data blocks are data blocks which are loaded regardless of their subscription to a scroller, usually
	 * this for super detections in which there are relatively very few data units. 
	 * <p>
	 * In some cases there may be a type of data unit which has a medium numbder of data units i.e. they should not all
	 * be loaded but a large section should be loaded. If this is the case then the min and max time functions can be used to 
	 * figure out what the load times should be. 
	 * 
	 * @author Jamie Macualay
	 *
	 */
	public class SpecialDataBlockInfo {
		
		/**
		 * Create a SpecialDataBlockInfo with default load times to load all data from data block. 
		 * @param pamDataBlock
		 */
		public SpecialDataBlockInfo(PamDataBlock pamDataBlock) {
			this.pamDataBlock = pamDataBlock;
		}

		/**
		 * Create a SpecialDataBlockInfo with defined load times
		 * @param pamDataBlock - the data block to be special 
		 * @param timeBefore - time to load before the scroller. 
		 * @param timeAfter - time to load after the scroller. 
		 */
		public SpecialDataBlockInfo(PamDataBlock pamDataBlock, long timeBefore, long timeAfter) {
			this.pamDataBlock = pamDataBlock;
			this.minTime = timeBefore; 
			this.maxTime = timeAfter; 
		}

		/**
		 * The data block that is special. 
		 */
		public PamDataBlock pamDataBlock;
		
		/**
		 * The time to load before scroller. 
		 */
		public long minTime = Long.MAX_VALUE;
		
		/**
		 * The time to load after scroller.
		 */
		public long maxTime = Long.MAX_VALUE;
		
	}

	/**
	 * 
	 * Get the load times for a special data block based on current load times.
	 * Unless specifically set when added to special data block list the default is
	 * Long.MIN_VALUE and Long.MAX_VALUE.
	 * 
	 * @param minimumMillis - the minimum time of a current scroller.
	 * @param maximumMillis - the maximum time of a current scroller
	 * @return the load time of the special data block.
	 */
	public long[] getSpecialLoadTimes(PamDataBlock pamDataBlock, long minimumMillis, long maximumMillis) {
		
		for (int i=0; i<specialDataBlocks.size(); i++){
			if (pamDataBlock == specialDataBlocks.get(i).pamDataBlock) {
				//have to be careful with number limits here. 
				long minTime; 
				long maxTime; 

				// A bit clunky. Technically we could get a number over/underflow error if someone adds
				//a very very big number. 
				if (specialDataBlocks.get(i).minTime == Long.MAX_VALUE) {
					// set value to 1 instead of Long.MIN_VALUE, because otherwise ViewerScrollerManager.loadDataQueueItem
					// will not load (nothing with a start time <= 0 gets loaded)
//					minTime= Long.MIN_VALUE;
					minTime = 1L;
				}
				else {
					minTime = minimumMillis - specialDataBlocks.get(i).minTime; 
				}
				
				if (specialDataBlocks.get(i).maxTime == Long.MAX_VALUE) {
					maxTime= Long.MAX_VALUE; 
				}
				else {
					maxTime = maximumMillis + specialDataBlocks.get(i).maxTime; 
				}
					
//				Debug.out.println("Special Data Block: millis in: " + pamDataBlock.getDataName() + "  "+ PamCalendar.formatDateTime(minimumMillis) +  " maxTime: " + PamCalendar.formatDateTime(maximumMillis));
//				Debug.out.println("Special Data Block: minTime: " + pamDataBlock.getDataName() + "  "+ minTime +  " maxTime: " + maxTime);
				long[] minmaxtimes = {minTime, maxTime};
				
				return minmaxtimes; 
			}
		}
		return null; //want to throw an error if the data block is not in the special list . 
	}

	/**
	 * @return the pamScrollers
	 */
	public Vector<AbstractPamScroller> getPamScrollers() {
		return pamScrollers;
	}
		
	/**
	 * Find a scroller with a given name. 
	 * @param scrollerName
	 * @return
	 */
	public AbstractPamScroller findScroller(String scrollerName) {
		if (pamScrollers == null) return null;
		for (AbstractPamScroller scroller : pamScrollers) {
			if (scroller.getScrollerData().getName().equals(scrollerName)) {
				return scroller;
			}
		}
		return null;
	}
}
