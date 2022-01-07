package pamScrollSystem;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import PamUtils.PamCalendar;
import PamView.PamSlider;

public class PamScrollSlider extends AbstractPamScrollerAWT {
	
	private JSlider slider;
	
	private JPanel panel;

	public PamScrollSlider(String name, int orientation, int stepSizeMillis,
			long defaultLoadTime, boolean hasMenu) {
		super(name, orientation, stepSizeMillis, defaultLoadTime, hasMenu);
		
		panel = new JPanel(new BorderLayout());
		slider = new TipSlider(orientation);
		slider.addChangeListener(new SliderListener());
		addMouseWheelSource(panel);
		addMouseWheelSource(slider);
		if (orientation == HORIZONTAL) {
			panel.add(BorderLayout.CENTER, slider);
			panel.add(BorderLayout.EAST, getButtonPanel());
		}
		else {
			panel.add(BorderLayout.CENTER, slider);
			panel.add(BorderLayout.SOUTH, getButtonPanel());
		}
	}
	
	class SliderListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			scrollMoved();			
		}
	}
	
	private class TipSlider extends PamSlider {

		private MouseEvent lastMouse;

		public TipSlider(int orientation) {
			super(orientation);
			setToolTipText("Scroll slider");
		}

		@Override
		public String getToolTipText() {
			try {
				long t = getValueMillis();
				long mouseT = getMouseTime();
				if (t == 0) {
					return null;
				}
				else {
					return String.format("<html>Current %s<p> Mouse %s</html>", PamCalendar.formatDBDateTime(t), PamCalendar.formatDBDateTime(mouseT));
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
//			Insets ins = getInsets(); // all 0
//			Border bord = getBorder(); null
//			Rectangle bounds = getBounds(); // nothing useful
//			int ext = getExtent(); // 0
//			get
			return (getRangeMillis()*lastMouse.getPoint().x)/getWidth()+getMinimumMillis();
		}

		@Override
		public String getToolTipText(MouseEvent event) {
			lastMouse = event;
			return super.getToolTipText(event);
		}
		
	}

	@Override
	void doMouseWheelAction(MouseWheelEvent mouseWheelEvent) {
		int n = mouseWheelEvent.getWheelRotation();
		slider.setValue(slider.getValue() - n);
		scrollMoved();
	}
	public void scrollMoved() {
		AbstractScrollManager.getScrollManager().moveInnerScroller(this, getValueMillis());	
		notifyValueChange();
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
	public void rangesChanged(long setValue) {
		slider.setMinimum(0);
		slider.setMaximum((int) ((scrollerData.maximumMillis-scrollerData.minimumMillis)/
				scrollerData.getStepSizeMillis()));
//		setValueMillis(setValue);
//		AbstractScrollManager.getScrollManager().moveOuterScroller(this, getMinimumMillis(), getMaximumMillis());
	}

	@Override
	public long getValueMillis() {
//		int minVal = slider.getMinimum();
//		int maxVal = slider.getMaximum();
//		int val = slider.getValue();
//		System.out.println("Slider val = " + val);
		return scrollerData.minimumMillis + (long) slider.getValue() * (long) scrollerData.getStepSizeMillis();
	}

	@Override
	public void valueSetMillis(long valueMillis) {
		valueMillis = Math.max(scrollerData.minimumMillis, Math.min(scrollerData.maximumMillis, valueMillis));
		int val = (int) ((valueMillis - scrollerData.minimumMillis) / scrollerData.getStepSizeMillis());
		if (val >= slider.getMinimum() && val <= slider.getMaximum()) {
			slider.setValue(val);
		}
	}
	@Override
	public long getVisibleAmount() {
		return 0;
	}
	/* (non-Javadoc)
	 * @see pamScrollSystem.AbstractPamScroller#setRangeMillis(long, long, boolean)
	 */
	@Override
	public void setRangeMillis(long minimumMillis, long maximumMillis, boolean notify) {
		// TODO Auto-generated method stub
		super.setRangeMillis(minimumMillis, maximumMillis, notify);
	}
	/* (non-Javadoc)
	 * @see pamScrollSystem.AbstractPamScroller#setStepSizeMillis(int)
	 */
	@Override
	public void setStepSizeMillis(int stepSizeMillis) {
		// TODO Auto-generated method stub
		super.setStepSizeMillis(stepSizeMillis);
	}
	/* (non-Javadoc)
	 * @see pamScrollSystem.AbstractPamScroller#setUnitIncrement(long)
	 */
	@Override
	public void setUnitIncrement(long unitIncrement) {
		// TODO Auto-generated method stub
		super.setUnitIncrement(unitIncrement);
	}
	/* (non-Javadoc)
	 * @see pamScrollSystem.AbstractPamScroller#setPageStep(int)
	 */
	@Override
	public void setPageStep(int pageStep) {
		// TODO Auto-generated method stub
		super.setPageStep(pageStep);
	}

}
