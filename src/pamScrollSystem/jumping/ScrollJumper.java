package pamScrollSystem.jumping;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.PamMenuParts;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.SimpleDataObserver;
import PamguardMVC.dataSelector.DataSelector;
import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.PamScroller;

/**
 * Scroll jumper allows the use of arrow keys to move to the next data unit. How it finds
 * that data unit is TBD, but might involve scanning all data blocks added to the current scroller
 * for the next data unit after a given time, or something like that. Can also use data filters
 * though need to think if it's using the same ones as on the display or different ones.  
 * @author dg50
 *
 */
public class ScrollJumper implements PamMenuParts, PamSettings {

	private AbstractPamScroller parentScroller;
	
	private ScrollJumpParams scrollJumpParams = new ScrollJumpParams();
	
	private PamDataUnit lastFoundDataUnit = null;
	
	private ArrayList<SimpleDataObserver> selectObservers = new ArrayList<SimpleDataObserver>();
	
	public ScrollJumper(AbstractPamScroller parentScroller, Component mainComponent) {
		this.parentScroller = parentScroller;
		if (mainComponent != null) {
			mainComponent.addKeyListener(new Keys());
		}
		parentScroller.addMenuParts(this);
		PamSettingManager.getInstance().registerSettings(this);
	}

	private class Keys extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
//			System.out.println("Key Pressed");
			if (e.isControlDown() == false) {
				return;
			}
			switch(e.getKeyCode()) {
			case KeyEvent.VK_RIGHT:
				rightArrow(e);
				break;
			case KeyEvent.VK_LEFT:
				leftArrow(e);
			}
		}
		
	}

	/**
	 * Move to the next loaded data unit or consider moving the
	 * outer scroller if no more data or we're scrollec to the end anyway
	 * @param e
	 */
	private void rightArrow(KeyEvent e) {
//		long currTime = parentScroller.getValueMillis();
//		long maxTime = parentScroller.getMaximumMillis()-parentScroller.getVisibleAmount()-parentScroller.getStepSizeMillis();
//		if (currTime >= maxTime) {
//			outerScrollForward(e);
//			return;
//		}
		
		moveNext(e);
		
		if (lastFoundDataUnit == null) {
			outerScrollForward(e);
		}

		notifySelectObservers();
	}
	
	/**
	 * Left arrow pressed, move to the previous loaded 
	 * unit or consider moving the outer scroller if no preceding dat or scroll is at start anyway 
	 * @param e
	 */
	private void leftArrow(KeyEvent e) {
//		long currTime = parentScroller.getValueMillis();
//		long minTime = parentScroller.getMinimumMillis();
//		if (currTime <= minTime) {
//			outerScrollBackward(e);
//			return;
//		}
		
		movePrevious(e);
		
		if (lastFoundDataUnit == null) {
			outerScrollBackward(e);
		}
		
		notifySelectObservers();
	}
	
	/**
	 * Move to the next loaded data unit
	 * @param e
	 */
	private void moveNext(KeyEvent e) {
		/*
		 * consider searching from the edge or center of the display ...
		 */
		long currTime = parentScroller.getValueMillis();
		long offset = 0;
		if (scrollJumpParams.alignment == ScrollJumpParams.ALIGN_AT_CENTRE) {
			/*
			 * if aligning at the centre, will need to start searching from the centre
			 * and will then need to set the scroll point an offset before that. 
			 */
			offset = parentScroller.getVisibleAmount()/2;
			currTime += offset;
		}
		/**
		 * But if the last found data unit is within the display, search from that instead. 
		 */
		if (lastFoundDataUnit != null) {
			if (lastFoundDataUnit.getTimeMilliseconds() >= parentScroller.getValueMillis() &&
					lastFoundDataUnit.getEndTimeInMilliseconds() <= parentScroller.getValueMillis() + parentScroller.getVisibleAmount()) {
				currTime = lastFoundDataUnit.getTimeMilliseconds();
			}
		}
		
		lastFoundDataUnit = findNextDataUnit(currTime+parentScroller.getStepSizeMillis());
		if (lastFoundDataUnit == null) {
			return;
		}
		long t = lastFoundDataUnit.getTimeMilliseconds()-offset;
		parentScroller.setValueMillis(t);
	}

	/**
	 * Move to the previous loaded data unit
	 * @param e
	 */
	private void movePrevious(KeyEvent e) {
		long currTime = parentScroller.getValueMillis() + parentScroller.getVisibleAmount();
		long offset = 0;
		if (scrollJumpParams.alignment == ScrollJumpParams.ALIGN_AT_CENTRE) {
			/*
			 * if aligning at the centre, will need to start searching from the centre
			 * and will then need to set the scroll point an offset before that. 
			 */
			offset = parentScroller.getVisibleAmount()/2;
			currTime -= offset;
		}
		/**
		 * But if the last found data unit is within the display, search from that instead. 
		 */
		if (lastFoundDataUnit != null) {
//			if (lastFoundDataUnit.getDurationInMilliseconds() != null) {
//				currTime -= Math.ceil(lastFoundDataUnit.getDurationInMilliseconds());
//			}
			if (lastFoundDataUnit.getTimeMilliseconds() >= parentScroller.getValueMillis() &&
					lastFoundDataUnit.getEndTimeInMilliseconds() <= parentScroller.getValueMillis() + parentScroller.getVisibleAmount()) {
				currTime = lastFoundDataUnit.getTimeMilliseconds();
			}
		}
		lastFoundDataUnit = findPrecedingDataUnit(currTime-parentScroller.getStepSizeMillis());
		if (lastFoundDataUnit == null) {
			return;
		}
		long t = lastFoundDataUnit.getTimeMilliseconds() - parentScroller.getVisibleAmount();		
		if (lastFoundDataUnit.getDurationInMilliseconds() != null) {
			t += Math.ceil(lastFoundDataUnit.getDurationInMilliseconds()); 
		}
		parentScroller.setValueMillis(t+offset);
	}

	private PamDataUnit findNextDataUnit(long searchTime) {
		PamDataUnit nextUnit = null;
		List<PamDataBlock> dataBlocks = getUsedDataBlocks();
		if (dataBlocks == null) {
			return null;
		}
		for (PamDataBlock dataBlock : dataBlocks) {
			DataSelector dataSelector = getDataSelector(dataBlock);
			PamDataUnit dataUnit = dataBlock.findFirstUnitAfter(searchTime, dataSelector);
			if (dataUnit == null) {
				continue;
			}
			if (nextUnit == null) {
				nextUnit = dataUnit;
			}
			else if (dataUnit.getTimeMilliseconds() < nextUnit.getTimeMilliseconds()) {
				nextUnit = dataUnit;
			}
		}
		/*
		 * Possible that data are in a different scoller too and more are loaded than
		 * are visible on this display. In this case we much return null and let 
		 * the outer scroller do its stuff.  
		 */
		if (nextUnit != null && nextUnit.getTimeMilliseconds() >= parentScroller.getMaximumMillis()) {
			return null;
		}
		return nextUnit;
	}
	
	private PamDataUnit findPrecedingDataUnit(long searchTime) {
		PamDataUnit nextUnit = null;
		List<PamDataBlock> dataBlocks = getUsedDataBlocks();
		if (dataBlocks == null) {
			return null;
		}
		for (PamDataBlock dataBlock : dataBlocks) {
			DataSelector dataSelector = getDataSelector(dataBlock);
			PamDataUnit dataUnit = dataBlock.findLastUnitBefore(searchTime, dataSelector);
			if (dataUnit == null) {
				continue;
			}
			if (nextUnit == null) {
				nextUnit = dataUnit;
			}
			else if (dataUnit.getTimeMilliseconds() > nextUnit.getTimeMilliseconds()) {
				nextUnit = dataUnit;
			}
		}
		return nextUnit;
	}
	
	/**
	 * Move the outer scroller forward and make sure it starts
	 * within a datamap point that has data in it. 
	 * @param e 
	 */
	private void outerScrollForward(KeyEvent e) {
		if (scrollJumpParams.allowOuterScroll == false) {
			return;
		}
		parentScroller.pageForward();
		moveNext(e);
	}

	/**
	 * Move the outer scroller backward and make sure it starts
	 * within a datamap point that has data in it. 
	 * @param e 
	 */
	private void outerScrollBackward(KeyEvent e) {
		if (scrollJumpParams.allowOuterScroll == false) {
			return;
		}
		parentScroller.pageBack();
		movePrevious(e);
	}

	/**
	 * Get a list of datablocks to search for the next or previous data unit
	 * By default this is the list of data blocks subscribed to the scroller, 
	 * but you may want to override this, e.g. in the spectrogram only
	 * return the list that's used by the display panel last clicked on. 
	 * @return list of datablocks. 
	 */
	public List<PamDataBlock> getUsedDataBlocks() {
		List<PamDataBlock> blockList = new ArrayList<PamDataBlock>();
		int nDataBlocks = parentScroller.getNumUsedDataBlocks();
		for (int i = 0; i < nDataBlocks; i++) {
			blockList.add(parentScroller.getUsedDataBlock(i));
		}
		return blockList;
	}
	
	/**
	 * Get a data selector for the datablock of interest. 
	 * by default this is null, but specific displays may want to 
	 * override this so that they can use the data selector 
	 * currently active on that display. 
	 * @param dataBlock
	 * @return data selector
	 */
	public DataSelector getDataSelector(PamDataBlock dataBlock) {
		return null;
	}

	@Override
	public int addMenuItems(JComponent parentComponent) {
		JMenuItem menuItem = new JMenuItem("Arrow scroll options ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showJumpOptions(e);
			}
		});
		parentComponent.add(menuItem);
		return 1;
	}

	protected void showJumpOptions(ActionEvent e) {
		JComponent parent = null;
		if (e.getSource() instanceof JComponent) {
			parent = (JComponent) e.getSource();
		}
		ScrollJumpParams newParams = ScrollJumpDialog.showDialog(PamController.getMainFrame(), parent, scrollJumpParams);
		if (newParams != null) {
			scrollJumpParams = newParams;
		}
	}

	@Override
	public String getUnitName() {
		// TODO Auto-generated method stub
		return parentScroller.getScrollerData().getName();
	}

	@Override
	public String getUnitType() {
		return "Scroll Jumper";
	}

	@Override
	public Serializable getSettingsReference() {
		return scrollJumpParams;
	}

	@Override
	public long getSettingsVersion() {
		return ScrollJumpParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		scrollJumpParams = ((ScrollJumpParams) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	/**
	 * @return the parentScroller
	 */
	public AbstractPamScroller getParentScroller() {
		return parentScroller;
	}

	/**
	 * @return the scrollJumpParams
	 */
	public ScrollJumpParams getScrollJumpParams() {
		return scrollJumpParams;
	}

	/**
	 * @return the lastFoundDataUnit
	 */
	public PamDataUnit getLastFoundDataUnit() {
		return lastFoundDataUnit;
	}
	
	/**
	 * Notify selection observers when a new data unit is selected
	 * May be sending out null's !
	 */
	public void notifySelectObservers() {
		for (SimpleDataObserver obs : selectObservers) {
			obs.update(lastFoundDataUnit);
		}
	}
	/**
	 * Add an observer which will get notifications whenever the selected data
	 * unit changes.
	 * @param simpleObserver
	 */
	public void addSelectObserver(SimpleDataObserver simpleObserver) {
		selectObservers.add(simpleObserver);
	}

	/**
	 * Remove an observer which got notifications whenever the selected data
	 * unit changes.
	 * @param simpleObserver
	 */
	public boolean removeSelectObserver(SimpleDataObserver simpleObserver) {
		return selectObservers.remove(simpleObserver);
	}
}
