package pamScrollSystem;

import java.awt.BorderLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

import PamUtils.PamCalendar;
import PamView.component.PamScrollBar;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;

public class PamScroller extends AbstractPamScrollerAWT {
	
	private JPanel panel;
	private JScrollBar scrollBar;
	private PamPanel timePanel;
	private PamLabel startLabel, endLabel;
	/**
	 * small panel to go in the East or the South to hold additional 
	 * controls such as the navigation buttons and tools
	 * for setting the range. 
	 */
	private JPanel controlPanel;

	/**
	 * Construct a Pamguard scroll bar which contains 
	 * a main scroll bar bit and buttons for moving forward
	 * in large secScollbar name (used in scroll bar management)
	 * @param name Scroller name
	 * @param orientation AbstractPamScroller.VERTICAL or AbstractPamScroller.HORIZONTAL
	 * @param stepSizeMillis step size in milliseconds for scroller. 
	 * @param defaultLoadTime default amount of data to load.
	 * @param hasMenu true if menu options should be shown in navigation area. 
	 */
	public PamScroller(String name, int orientation, 
			int stepSizeMillis, long defaultLoadTime, boolean hasMenu) {
		super(name, orientation, stepSizeMillis, defaultLoadTime, hasMenu);
		
		panel = new JPanel(new BorderLayout());
		scrollBar = new TipScrollBar(orientation);
				
		scrollBar.addAdjustmentListener(new ScrollListener());
		controlPanel = new JPanel();
		
		if (orientation == HORIZONTAL) {
			controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
			panel.add(BorderLayout.CENTER, scrollBar);
			panel.add(BorderLayout.EAST, controlPanel);
			controlPanel.add(getButtonPanel());
		}
		else {
			controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
			panel.add(BorderLayout.CENTER, scrollBar);
			panel.add(BorderLayout.SOUTH, controlPanel);
			controlPanel.add(getButtonPanel());
		}
	}
	
	private class TipScrollBar extends PamScrollBar {

		private MouseEvent lastMouse;

		public TipScrollBar(int orientation) {
			super(orientation);
			setToolTipText("Scroll Bar");
		}
		
		@Override
		public String getToolTipText() {
			try {
				long t = getValueMillis();
				long t2 = getVisibleEnd();
				long mt = getMouseTime();
				if (t == 0) {
					return null;
				}
				else {
					String tip = String.format("<html>%s - %s<p> Mouse %s</html>", PamCalendar.formatDBDateTime(t), 
							PamCalendar.formatTime(t2), PamCalendar.formatDBDateTime(mt));
					
					return tip;
				}
			}
			catch (Exception e) {
				return null;
			}
		}
		
		public long getMouseTime() {
			if (lastMouse == null) {
				return -1;
			}
			return (getRangeMillis()*lastMouse.getPoint().x)/getWidth()+getMinimumMillis();
		}

		@Override
		public String getToolTipText(MouseEvent event) {
			lastMouse = event;
			return super.getToolTipText(event);
		}
		
		
	}
	
	public void setShowTimes(boolean showTimes) {
		if (!showTimes && timePanel != null) {
			panel.remove(timePanel);
			timePanel = null;
		}
		else if (showTimes) {
			timePanel = new PamPanel(new BorderLayout());
			startLabel = new PamLabel(" ");
			endLabel = new PamLabel(" ");
			timePanel.add(BorderLayout.WEST, startLabel);
			timePanel.add(BorderLayout.EAST, endLabel);
			panel.add(BorderLayout.SOUTH, timePanel);
			
		}
	}
	
	/**
	 * Add an additional control to the control panel which 
	 * will sit to the right of or just below the wee buttons
	 * used for scroll bar navigation. 
	 * @param component
	 */
	public void addControl(JComponent component) {
		controlPanel.add(component);
	}
	
	class ScrollListener implements AdjustmentListener {
		@Override
		public void adjustmentValueChanged(AdjustmentEvent arg0) {
			scrollMoved();
		}
	}
	
	@Override
	public void rangesChanged(long setValue) {
		scrollBar.setMinimum(0);
		scrollBar.setMaximum((int) ((scrollerData.maximumMillis-scrollerData.minimumMillis)/
				scrollerData.getStepSizeMillis()));
		sayTimes();
	}

	public void scrollMoved() {
		AbstractScrollManager.getScrollManager().moveInnerScroller(this, getValueMillis());	
		notifyValueChange();
	}
	
	
	@Override
	void doMouseWheelAction(MouseWheelEvent mouseWheelEvent) {
		int n = mouseWheelEvent.getWheelRotation();
		scrollBar.setValue(scrollBar.getValue() - scrollBar.getUnitIncrement()*n);
		scrollMoved();
	}

	private void sayTimes() {
		if (startLabel != null) {
			startLabel.setText(PamCalendar.formatDateTime(getMinimumMillis()));
			endLabel.setText(PamCalendar.formatDateTime(getMaximumMillis()));
		}
	}

	@Override
	public void anotherScrollerMovedInner(long newValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public JComponent getComponent() {
		return panel;
	}

	@Override
	public void setBlockIncrement(long blockIncrement) {
		scrollBar.setBlockIncrement((int) (blockIncrement/scrollerData.getStepSizeMillis()));
	}
	
	public long getBlockIncrement() {
		return (long) scrollBar.getBlockIncrement() * (long) scrollerData.getStepSizeMillis();
	}

	@Override
	public void setVisibleMillis(long visibleAmount) {
		int currVal = scrollBar.getValue();
		int visAmount = (int)(visibleAmount/scrollerData.getStepSizeMillis());
		int maxVal = scrollBar.getMaximum()-visAmount;
		if (currVal > maxVal) {
			scrollBar.setValue(maxVal);
			notifyValueChange();
		}
		scrollBar.setVisibleAmount(visAmount);
		setUnitIncrement(Math.max(1, visibleAmount/10));
		setBlockIncrement(Math.max(1, visibleAmount*8/10));
//		if (Platform.isFxApplicationThread()) {
//			SwingUtilities.invokeLater(new Runnable() {
//				@Override
//				public void run() {
//					notifyRangeChange();
//				}
//			});
//		}
//		else {
//			notifyRangeChange();
//		}
//		checkValueRange();
	}
	
	/**
	 * 
	 * @return The visible amount of the display in milliseconds. 
	 */
	@Override
	public long getVisibleAmount() {
		int scrollBarVal=0;
		if (scrollBar!=null) {
			scrollBarVal=scrollBar.getVisibleAmount();
		}
		return (long) scrollBarVal * (long) scrollerData.getStepSizeMillis();
	}
	
	/**
	 * Called after setvisibleAmount to check
	 * that the value is now not higher than the 
	 * realistic maximum which is 
	 */
	private void checkValueRange() {
		int currVal = scrollBar.getValue();
		int maxVal = scrollBar.getMaximum()-scrollBar.getVisibleAmount();
		if (currVal > maxVal) {
			scrollBar.setValue(maxVal);
		}
	}

	@Override
	public void setUnitIncrement(long unitIncrement) {
		scrollBar.setUnitIncrement((int) (unitIncrement/scrollerData.getStepSizeMillis()));
	}

	@Override
	public long getValueMillis() {
		int scrollBarVal=0;
		if (scrollBar!=null) {
			scrollBarVal=scrollBar.getValue();
		}
		return scrollerData.minimumMillis + scrollBarVal * scrollerData.getStepSizeMillis();
	}

	@Override
	public void valueSetMillis(long valueMillis) {
		valueMillis = Math.max(scrollerData.minimumMillis, Math.min(scrollerData.maximumMillis, valueMillis));
		int val = (int) ((valueMillis - scrollerData.minimumMillis) /scrollerData. getStepSizeMillis());
		if (val >= scrollBar.getMinimum() && val <= scrollBar.getMaximum()) {
			scrollBar.setValue(val);
		}
	}

	@Override
	public String toString() {
		return super.toString();
//		return String.format("Scroller %s Start %s End %s Value %s len %ds, visible %ds",
//				scrollerData.name, PamCalendar.formatDateTime(getMinimumMillis()),
//				PamCalendar.formatDateTime(getMaximumMillis()),
//				PamCalendar.formatDateTime(getValueMillis()), 
//				(getMaximumMillis()-getMinimumMillis())/1000,
//				getVisibleAmount()/1000);
	}

	public JScrollBar getScrollBar() {
		return scrollBar;		
	}

}
