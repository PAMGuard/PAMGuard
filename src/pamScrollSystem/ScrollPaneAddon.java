package pamScrollSystem;

import java.awt.event.MouseWheelEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

/**
 * Small set of scroll controls which can be inserted into any window for 
 * controlling the loading of data in viewer mode. does not contain a
 * scroll bar, just the data navigation arrows. 
 * @author Doug Gillespie
 *
 */
public class ScrollPaneAddon extends AbstractPamScrollerAWT {

	private JScrollPane scrollPane;
	
//	private 
	
	public ScrollPaneAddon(JScrollPane scrollPane, String name, int orientation, int stepSizeMillis,
			long defaultLoadTime, boolean hasMenu) {
		super(name, orientation, stepSizeMillis, defaultLoadTime, hasMenu);
		this.scrollPane = scrollPane;
		JScrollBar vSB = scrollPane.getVerticalScrollBar();
	}
	
	private class DummyScrollBar extends JScrollBar {
		
	}

	@Override
	public JPanel getButtonPanel() {
		return super.getButtonPanel();
	}

	@Override
	public void anotherScrollerMovedInner(long newValue) {
	}

	@Override
	void doMouseWheelAction(MouseWheelEvent mouseWheelEvent) {
	}

	@Override
	public JComponent getComponent() {
		return null;
	}

	@Override
	public long getValueMillis() {
		return scrollerData.minimumMillis;
	}

	@Override
	public void rangesChanged(long setValue) {
		AbstractScrollManager.getScrollManager().moveOuterScroller(this, getMinimumMillis(), getMaximumMillis());
	}

	@Override
	public void valueSetMillis(long valueMillis) {
		
	}

	@Override
	public long getVisibleAmount() {
		return 0;
	}

}
