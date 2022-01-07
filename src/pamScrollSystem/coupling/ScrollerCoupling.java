package pamScrollSystem.coupling;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.GeneralProjector;
import PamView.paneloverlay.overlaymark.OverlayMark;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import pamScrollSystem.AbstractPamScroller;

/**
 * A class for coupling two or more scrollers. 
 * <p>
 * Coupled scrollers will move together - when one moves, the
 * others move. 
 * @author Doug Gillespie
 *
 */
public class ScrollerCoupling implements PamSettings {

	private String couplerName;
	
	private Vector<AbstractPamScroller> scrollers = new Vector<AbstractPamScroller>();
	
	private CouplingParams couplingParams = new CouplingParams();

	public CouplingParams getCouplingParams() {
		return couplingParams;
	}

	public ScrollerCoupling(String name) {
		super();
		this.couplerName = name;
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	public String getName() {
		return couplerName;
	}
	
	public JMenuItem getSwingOptionsMenu(Window frame) {
		JMenuItem mi = new JMenuItem("Scroller Coupling");
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				CouplingParams newParams = CouplerDialog.showDialog(frame, couplerName, couplingParams);
				if (newParams != null) {
					couplingParams = newParams;
				}
			}
		});
		mi.setToolTipText("Control how scrollers on different displays interact");
		return mi;
	}

	/**
	 * Add a new scroller to the coupling
	 * @param aScroller
	 */
	public void addScroller(AbstractPamScroller aScroller) {
		if (scrollers.indexOf(aScroller) < 0) {
			scrollers.add(aScroller);
		}
		/*
		 *  now need to immediately pick up settings from
		 *  one of the existing scrollers - may not be the first if 
		 *  this one was already there.  
		 */
		for (int i = 0; i < scrollers.size(); i++) {
			if (scrollers.get(i) == aScroller) {
				continue;
			}
			else {
				aScroller.coupledScrollerChanged(this, scrollers.get(i));
			}
			break;
		}
	}
	
	/**
	 * lock to stop looping of calls to the notifyOthers function.
	 */
	private boolean notificationLock = false;
	/**
	 * Notify other scrollers in the set that a scroller has
	 * changed then pass them a reference to the changed
	 * scroller so that they can copy information from it. 
	 * <p>
	 * This function holds a lock  since as soon as another 
	 * scroller is changed, it's likely to call back to this same
	 * function and set up an infinite loop. The lock will exit 
	 * the function if set to avoid this situation. 
	 * @param scroller scroller which changes. 
	 */
	public void notifyOthers(AbstractPamScroller scroller) {
		if (notificationLock) {
			return;
		}
		notificationLock = true;
		AbstractPamScroller aScroller;
		for (int i = 0; i < scrollers.size(); i++) {
			aScroller = scrollers.get(i);
			if (aScroller == scroller) {
				continue; // no need to notify itself !
			}
			aScroller.coupledScrollerChanged(this, scroller);
		}
		notificationLock = false;
	}
	
	/**
	 * Remove a scroller form a coupling
	 * @param aScroller
	 * @return true if the scroller wwas present. 
	 */
	public boolean removeScroller(AbstractPamScroller aScroller) {
		return scrollers.remove(aScroller);
	}
	
	public int getScrollerCount() {
		return scrollers.size();
	}

	/**
	 * Get a list of menu commands that can be inserted into a popup menu
	 * when a display is clicked, which may or may not have an overlay mark
	 * on it. If overlayMark and mouseTime are both null, null will be returned
	 * @param overlayMark Overlay mark, may be null;
	 * @param mouseTime Mouse time in milliseconds, can also be null 
	 * @return popup menu item list. 
	 */
	public List<MenuItem> getPopupMenuItems(AbstractPamScroller hostScroller, OverlayMark overlayMark, Long mouseTime) {
		if (overlayMark == null && mouseTime == null) {
			return null; // probably should never happen ...
		}
		if (getScrollerCount() < 2) {
			return null; // no other displays to do anything to !
		}
		ArrayList<MenuItem> menuItems = new ArrayList<>();
		MenuItem menuItem;
		if (overlayMark != null) {
			int timeInd = overlayMark.findParameterIndex(GeneralProjector.ParameterType.TIME);
			double[] markRange = null;
			if (timeInd >= 0) {
				double[] limits = overlayMark.getLimits();
				if (timeInd == 0) {
					markRange = Arrays.copyOf(limits, 2);
				}
				else if (timeInd == 1) {
					markRange = Arrays.copyOfRange(limits, 2, 4);
				}
			}
			if (markRange != null) {
				double dur = (markRange[1]-markRange[0])/1000.;
				String ms = String.format("Zoom linked displays to box %s-%s(%3.1fs)", PamUtils.PamCalendar.formatTime((long)markRange[0],3),
						PamUtils.PamCalendar.formatTime((long)markRange[1],3), dur);
				menuItem = new MenuItem(ms);
				menuItem.setOnAction(new ZoomRangeHandler(hostScroller, markRange));
				menuItems.add(menuItem);
			}
		}
		if (mouseTime != null) {
			String ms2 = String.format("Start linked displays at mouse position %s", PamUtils.PamCalendar.formatTime(mouseTime));
			menuItem = new MenuItem(ms2);
			menuItem.setOnAction(new MouseStartHandler(hostScroller, mouseTime));
			menuItems.add(menuItem);

			ms2 = String.format("Centre linked displays at mouse position %s", PamUtils.PamCalendar.formatTime(mouseTime));
			menuItem = new MenuItem(ms2);
			menuItem.setOnAction(new MouseCentreHandler(hostScroller, mouseTime));
			menuItems.add(menuItem);
		}
		
		return menuItems;
	}
	
	private class ZoomRangeHandler implements EventHandler {
		private AbstractPamScroller hostScroller;
		private double[] markRange;

		/**
		 * @param hostScroller
		 * @param markRange
		 */
		public ZoomRangeHandler(AbstractPamScroller hostScroller, double[] markRange) {
			super();
			this.hostScroller = hostScroller;
			this.markRange = markRange;
		}

		@Override
		public void handle(Event event) {
			zoomToRange(hostScroller, markRange);
		}
		
	}
	protected void zoomToRange(AbstractPamScroller hostScroller, double[] markRange) {
		for (AbstractPamScroller scroller:scrollers) {
			if (scroller == hostScroller) {
				continue;
			}
			scroller.setVisibleMillis((long) (markRange[1]-markRange[0]));
			scroller.setValueMillis((long) markRange[0]);
			scroller.notifyRangeChange();
		}
	}

	private class MouseCentreHandler implements EventHandler {
		private AbstractPamScroller hostScroller;
		private long mouseTime;

		/**
		 * @param hostScroller
		 * @param markRange
		 */
		public MouseCentreHandler(AbstractPamScroller hostScroller, long mouseTime) {
			super();
			this.hostScroller = hostScroller;
			this.mouseTime = mouseTime;
		}

		@Override
		public void handle(Event event) {
			centreTime(hostScroller, mouseTime);
		}
		
	}

	/**
	 * Centre other displays at this mouse time ...
	 * @param hostScroller
	 * @param mouseTime
	 */
	public void centreTime(AbstractPamScroller hostScroller, long mouseTime) {
		for (AbstractPamScroller scroller:scrollers) {
			if (scroller == hostScroller) {
				continue;
			}
			long visMillis = scroller.getVisibleAmount();
			scroller.setValueMillis(mouseTime - visMillis/2);
			scroller.notifyRangeChange();
		}
	}

	private class MouseStartHandler implements EventHandler {
		private AbstractPamScroller hostScroller;
		private long mouseTime;

		/**
		 * @param hostScroller
		 * @param markRange
		 */
		public MouseStartHandler(AbstractPamScroller hostScroller, long mouseTime) {
			super();
			this.hostScroller = hostScroller;
			this.mouseTime = mouseTime;
		}

		@Override
		public void handle(Event event) {
			startTime(hostScroller, mouseTime);
		}
		
	}

	/**
	 * Start other displays at this mouse time ...
	 * @param hostScroller
	 * @param mouseTime
	 */
	public void startTime(AbstractPamScroller hostScroller, long mouseTime) {
		for (AbstractPamScroller scroller:scrollers) {
			if (scroller == hostScroller) {
				continue;
			}
			scroller.setValueMillis(mouseTime);
			scroller.notifyRangeChange();
		}
	}

	@Override
	public String getUnitName() {
		return getName();
	}

	@Override
	public String getUnitType() {
		return "Scroll Coupler";
	}

	@Override
	public Serializable getSettingsReference() {
		return couplingParams;
	}

	@Override
	public long getSettingsVersion() {
		return CouplingParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		couplingParams = ((CouplingParams) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}
	
	
}
