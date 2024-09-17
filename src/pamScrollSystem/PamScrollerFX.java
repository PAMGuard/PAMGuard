package pamScrollSystem;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamScrollBarFX;
import pamViewFX.fxNodes.pamScrollers.AbstractPamScrollerFX;

/**
 * Creates a scroll bar system that allows users to navigate through data in real time and viewer mode. 
 * @author Jamie Macaulay, Doug Gillespie
 *
 */
public class PamScrollerFX extends AbstractPamScrollerFX {
	
	/**
	 * Panel which holds buttons for navigating through data.
	 */
	protected Pane navigationPane;
	
	/**
	 * The scroll bar used to navigate through the current range of displayed data.
	 */
	private PamScrollBarFX scrollBar;
	
	/**
	 * Pane which holds the scroll bar and other controls.
	 */
	private PamBorderPane scrollPane;
	
	/**
	 * Construct a PAMGUARD scroll bar which contains 
	 * a main scroll bar bit and buttons for moving forward
	 * in large secScollbar name (used in scroll bar management)
	 * @param orientation AbstractPamScroller.VERTICAL or AbstractPamScroller.HORIZONTAL
	 * @param stepSizeMillis step size in milliseconds for scroller. 
	 * @param defaultLoadTime default amount of data to load.
	 * @param hasMenu true if menu options should be shown in navigation area. 
	 */
	public PamScrollerFX(String name, Orientation orientation, int stepSizeMillis,
			long defaultLoadTime, boolean hasMenu) {
		super(name, orientation, stepSizeMillis, defaultLoadTime, hasMenu);
		
		//create the scroll bar. 
		scrollBar = new PamScrollBarFX();
		
		//add a listener to the scroll bar.
		scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
			public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
                    scrollMoved();
            }
        });
		layoutScrollBarPane(orientation);
	}
	
	/**
	 * Get the pane which allows users to page through viewer data/ select the maximum amount of data to be loaded and start time. In real time mode simply allows selection 
	 * of the maximum data to be loaded and visible window size. 
	 * @param orientation-orientation of pane. 
	 * @return the navigation pane for scroll bar. 
	 */
	protected Pane getControlPane() {
		return navigationPane;
	}
	
	/**
	 * Layout the control pane containing scroll bar and buttons to navigate in time. 
	 * @param orientation- orientation of the time scroller. 
	 */
	public void layoutScrollBarPane(Orientation orientation){

		//create the holder for the scroll bar. 
		if (scrollPane==null)	scrollPane=new PamBorderPane();
		
		scrollPane.setCenter(null);
		scrollPane.setRight(null);
		scrollPane.setBottom(null);
		scrollBar.setOrientation(orientation);
		
		//create default control pane
		navigationPane=createNavigationPane(false, orientation);

		if (orientation == Orientation.HORIZONTAL) {
			scrollPane.setCenter(scrollBar);
			scrollPane.setRight(navigationPane);
		}
		else {
			scrollPane.setCenter(scrollBar);
			scrollPane.setBottom(navigationPane);
		}
		
		//add listeners to resize the scroll bar correctly.
		setResizeBinding(orientation);
		
	}
	
	/**
	 * Set orientation for the scroller. 
	 * @param orientation- orientation of the time scroller. 
	 */
	public void setOrientation(Orientation orientation){
		this.orientation=orientation;
		layoutScrollBarPane (orientation);
	} 
	
	/**
	 * Set binding so scroll bar dimensions are the same as the control pane. 
	 * @param orientation- orientation of scroll bar. 
	 */
	private void setResizeBinding(Orientation orientation ){
		scrollBar.prefWidthProperty().unbind();
		scrollBar.prefHeightProperty().unbind();
		scrollBar.prefWidthProperty().setValue(-1);
		scrollBar.prefHeightProperty().setValue(-1);
		
		if (orientation==Orientation.VERTICAL) scrollBar.prefWidthProperty().bind(getControlPane().widthProperty());

		if (orientation==Orientation.HORIZONTAL) scrollBar.prefHeightProperty().bind(getControlPane().heightProperty());
	}

	@Override
	public Pane getNode() {
		return scrollPane;
	}
	
	@Override
	public void rangesChanged(long setValue) {
		scrollBar.setMin(0);
		scrollBar.setMax((int) ((scrollerData.maximumMillis-scrollerData.minimumMillis)/
				scrollerData.getStepSizeMillis()));
	}
	
	public void scrollMoved() {
		AbstractScrollManager.getScrollManager().moveInnerScroller(this, getValueMillis());	
		notifyValueChange();
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
		double currVal = scrollBar.getModValue();//use modified value to keep with Swing scroll bar calcs; 
		double  visAmount = (visibleAmount/scrollerData.getStepSizeMillis());
		double maxVal = scrollBar.getMax()-visAmount;
		if (currVal > maxVal) {
			scrollBar.setModValue(maxVal);
			notifyValueChange();
		}
		scrollBar.setVisibleAmount(visAmount);
		setUnitIncrement(Math.max(1, visibleAmount/10));
		setBlockIncrement(Math.max(1, visibleAmount*8/10));
//		checkValueRange();
	}

	/**
	 * 
	 * @return The visible amount of the display in milliseconds. 
	 */
	@Override
	public long getVisibleAmount() {
		return (long) scrollBar.getVisibleAmount() * (long) scrollerData.getStepSizeMillis();
	}

	/**
	 * Called after setvisibleAmount to check
	 * that the value is now not higher than the 
	 * realistic maximum which is 
	 */
	private void checkValueRange() {
		double currVal = scrollBar.getModValue(); //use modified value to keep with swing calcs;
		double maxVal = scrollBar.getMax()-scrollBar.getVisibleAmount();
		if (currVal > maxVal) {
			scrollBar.setModValue(maxVal);
		}
	}

	@Override
	public void setUnitIncrement(long unitIncrement) {
		scrollBar.setUnitIncrement((int) (unitIncrement/scrollerData.getStepSizeMillis()));
	}

	@Override
	public long getValueMillis() {
//		System.out.println("PamScrollerFX: scrollerData.minimumMillis: "+PamCalendar.formatDateTime2(scrollerData.minimumMillis) + " scrollBar.getModValue() "+
//				scrollBar.getModValue());
		return (long) (scrollerData.minimumMillis + scrollBar.getModValue() * scrollerData.getStepSizeMillis());
	}

	@Override
	public void valueSetMillis(long valueMillis) {
		valueMillis = Math.max(scrollerData.minimumMillis, Math.min(scrollerData.maximumMillis, valueMillis));
		int val = (int) ((valueMillis - scrollerData.minimumMillis) /scrollerData. getStepSizeMillis());
		if (val >= scrollBar.getMin() && val <= scrollBar.getMax()) {
			scrollBar.setModValue(val);
		}
	}

	@Override
	public void anotherScrollerMovedInner(long newValue) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Get the scroll bar. 
	 * @return the scroll bar. 
	 */
	public PamScrollBarFX getScrollBar() {
		return scrollBar;
	}


}
