package PamView.paneloverlay.overlaymark;

import java.awt.Component;
import java.awt.event.MouseWheelEvent;
import java.util.List;

import PamController.PamController;
import javafx.event.EventType;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

/**
 * This is an external mouse adapter class which should work with both Swing and Fx
 * Mouse events. Classes which implement these functions should only 
 * override the fx functions, leaving the swing ones in peace. 
 * @author dg50
 *
 */
public class ExtMouseAdapter {
	


	public boolean mouseClicked(MouseEvent e) {
		return false;
	}

	public boolean mouseDragged(MouseEvent e) {
		return false;
	}

	public boolean mouseEntered(MouseEvent e) {
		return false;
	}

	public boolean mouseExited(MouseEvent e) {
		return false;
	}

	public boolean mouseMoved(MouseEvent e) {
		return false;
	}

	public boolean mousePressed(MouseEvent e) {
		return false;
	}

	public boolean mouseReleased(MouseEvent e) {
		return false;
	}

	public boolean mouseWheelMoved(ScrollEvent e) {
		return false;
	}
	
	/**
	 * Now all the Swing versions of these functions ...
	 */

	final public boolean mouseClicked(java.awt.event.MouseEvent e) {
		return mouseClicked(fxMouse(e, null));
	}

	final public boolean mouseDragged(java.awt.event.MouseEvent e) {
		return mouseDragged(fxMouse(e, null));
	}

	final public boolean mouseEntered(java.awt.event.MouseEvent e) {
		return mouseEntered(fxMouse(e, null));
	}

	final public boolean mouseExited(java.awt.event.MouseEvent e) {
		return mouseExited(fxMouse(e, null));
	}

	final public boolean mouseMoved(java.awt.event.MouseEvent e) {
		return mouseMoved(fxMouse(e, null));
	}

	final public boolean mousePressed(java.awt.event.MouseEvent e) {
		return mousePressed(fxMouse(e, null));
	}

	final public boolean mouseReleased(java.awt.event.MouseEvent e) {
		return mouseReleased(fxMouse(e, null));
	}

	final public boolean mouseWheelMoved(MouseWheelEvent e) {
//		return mouseWheelMoved(fxScroll(e));
		return false;
	}

//	private MouseEvent swingMouse(javafx.scene.input.MouseEvent e) {
//		return new java.awt.event.MouseEvent(null, 0, System.currentTimeMillis(), 0, (int) e.getSceneX(), (int) e.getSceneY(), 
//				(int) e.getScreenX(), (int) e.getScreenY(), e.getClickCount(), e.isPopupTrigger(), 0);
//	}
	
	private ScrollEvent fxScroll(MouseWheelEvent e) {
		return new ScrollEvent(new EventType(""), e.getX(), e.getY(), e.getXOnScreen(), e.getYOnScreen(), 
				e.isShiftDown(), e.isControlDown(), e.isAltDown(), e.isMetaDown(), true, false, 
				0, e.getPreciseWheelRotation(), 0, 0, null, 0, null, 0, 0, null);
				/*
				 * public ScrollEvent(EventType<ScrollEvent> eventType,
                   double x,
                   double y,
                   double screenX,
                   double screenY,
                   boolean shiftDown,
                   boolean controlDown,
                   boolean altDown,
                   boolean metaDown,
                   boolean direct,
                   boolean inertia,
                   double deltaX,
                   double deltaY,
                   double totalDeltaX,
                   double totalDeltaY,
                   ScrollEvent.HorizontalTextScrollUnits textDeltaXUnits,
                   double textDeltaX,
                   ScrollEvent.VerticalTextScrollUnits textDeltaYUnits,
                   double textDeltaY,
                   int touchCount,
                   PickResult pickResult)

Constructs new ScrollEvent event with null source and target
Parameters:eventType - The type of the event.x - The x with respect to the scene.
y - The y with respect to the scene.screenX - The x coordinate relative to screen.
screenY - The y coordinate relative to screen.shiftDown - true if shift modifier was pressed.
controlDown - true if control modifier was pressed.altDown - true if alt modifier was pressed.
metaDown - true if meta modifier was pressed.
direct - true if the event was caused by direct input device. See GestureEvent.
isDirect()inertia - if represents inertia of an already finished gesture.
deltaX - horizontal scroll amount
deltaY - vertical scroll amount
totalDeltaX - cumulative horizontal scroll amount
totalDeltaY - cumulative vertical scroll amount
textDeltaXUnits - units for horizontal text-based scroll amount
textDeltaX - horizontal text-based scroll amount
textDeltaYUnits - units for vertical text-based scroll amounttextDeltaY - vertical text-based scroll amounttouchCount - number of touch pointspickResult - pick result. Can be null, in this case a 2D pick result without any further values is constructed based on the scene coordinatesSince:JavaFX 8.0
				 */
	}
	/**
	 * Convert a Swing mouse button id into an Fx one. 
	 * @param e Swing mouse event
	 * @return FX button
	 */
	private static MouseButton fxMouseButton(java.awt.event.MouseEvent e) {
		/**
		 * Button 3 is the secondary button (normally the right button)
		 * Button 2 is normally the middle one. 
		 */
		switch (e.getButton()) {
		case java.awt.event.MouseEvent.BUTTON1:
			return MouseButton.PRIMARY;
		case java.awt.event.MouseEvent.BUTTON2:
			return MouseButton.MIDDLE;
		case java.awt.event.MouseEvent.BUTTON3:
			return MouseButton.SECONDARY;
		case java.awt.event.MouseEvent.NOBUTTON:
			return MouseButton.NONE;
		}
		return MouseButton.NONE;
	}
	
	private static int swingMouseButton(MouseButton fxButton) {
		if (fxButton == null) return -1;
		switch (fxButton) {
		case MIDDLE:
			return java.awt.event.MouseEvent.BUTTON2;
		case NONE:
			return 0;
		case PRIMARY:
			return java.awt.event.MouseEvent.BUTTON1;
		case SECONDARY:
			return java.awt.event.MouseEvent.BUTTON3;
		default:
			break;
		}
		return 0;
	}
	
	public static javafx.scene.input.MouseEvent fxMouse(java.awt.event.MouseEvent e, EventType eventType) {
		int button = e.getButton();
		return new javafx.scene.input.MouseEvent(e.getSource(), null, eventType, e.getX(), e.getY(), e.getXOnScreen(), e.getYOnScreen(),
				fxMouseButton(e), e.getClickCount(), e.isShiftDown(), e.isControlDown(), e.isAltDown(), e.isMetaDown(),  
				button == java.awt.event.MouseEvent.BUTTON1, button == java.awt.event.MouseEvent.BUTTON2, 
				button == java.awt.event.MouseEvent.BUTTON3, true, e.isPopupTrigger(), false, null);
				/*
				 *  MouseEvent(Object source,
                  EventTarget target,
                  EventType<? extends MouseEvent> eventType,
                  double x,
                  double y,
                  double screenX,
                  double screenY,
                  MouseButton button,
                  int clickCount,
                  boolean shiftDown,
                  boolean controlDown,
                  boolean altDown,
                  boolean metaDown,
                  boolean primaryButtonDown,
                  boolean middleButtonDown,
                  boolean secondaryButtonDown,
                  boolean synthesized,
                  boolean popupTrigger,
                  boolean stillSincePress,
                  PickResult pickResult)

Constructs new MouseEvent event.
Parameters:source - the source of the event. Can be null.target - the target of the event. Can be null.eventType - The type of the event.x - The x with respect to the source. Should be in scene coordinates if source == null or source is not a Node.y - The y with respect to the source. Should be in scene coordinates if source == null or source is not a Node.screenX - The x coordinate relative to screen.screenY - The y coordinate relative to screen.button - the mouse button usedclickCount - number of click countsshiftDown - true if shift modifier was pressed.controlDown - true if control modifier was pressed.altDown - true if alt modifier was pressed.metaDown - true if meta modifier was pressed.primaryButtonDown - true if primary button was pressed.middleButtonDown - true if middle button was pressed.secondaryButtonDown - true if secondary button was pressed.synthesized - if this event was synthesizedpopupTrigger - whether this event denotes a popup trigger for current platformstillSincePress - see isStillSincePress()pickResult - pick result. Can be null, in this case a 2D pick result without any further values is constructed based on the scene coordinates and target
				 */
				
	}
	
	public static java.awt.event.MouseEvent swingMouse(MouseEvent fxMouse) {
		Component source = PamController.getMainFrame();
		if (fxMouse.getSource() != null) {
			if (Component.class.isAssignableFrom(fxMouse.getSource().getClass())) {
				source = (Component) fxMouse.getSource();
			}
		}
		return new java.awt.event.MouseEvent(source, 0, System.currentTimeMillis(), 0, (int) fxMouse.getX(), (int) fxMouse.getY(),
				(int) fxMouse.getScreenX(), (int) fxMouse.getScreenY(), fxMouse.getClickCount(), 
				fxMouse.isPopupTrigger(), swingMouseButton(fxMouse.getButton()));
		/*
		 * public MouseEvent(Component source,
                  int id,
                  long when,
                  int modifiers,
                  int x,
                  int y,
                  int xAbs,
                  int yAbs,
                  int clickCount,
                  boolean popupTrigger,
                  int button)
		 */
	}
	
	public List<MenuItem> getPopupMenuItems(MouseEvent e) {
		return null;
	}
	
}
