package dataPlotsFX.overlaymark;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import PamView.paneloverlay.overlaymark.MarkKeyAction;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMarkObserver;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.layout.TDGraphFX;
import detectiongrouplocaliser.DetectionGroupSummary;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyEvent;

/**
 * Keyboard shortcuts for whatever is currently marked on a TDGraphFX, e.g. Ctrl+L
 * to label marked clicks as an event. This gives the FX time display the same
 * keyboard shortcuts that the click detector's bearing time display has always
 * had through its CtrlKeyManager.
 * <p>
 * Nothing in the FX displays is focus traversable and no node in them listens for
 * key presses, so a handler on any one node would never fire. Instead this
 * registers an event filter on the whole Scene, which sees key presses whatever
 * has focus within it. The graph only responds when the mouse is over it, so that
 * with several graphs open the shortcut goes to the one the user is pointing at.
 * <p>
 * In the Swing GUI the display sits inside a JFXPanel, which is focusable, takes
 * Swing focus on a left mouse press and forwards key events into the FX scene. So
 * having just clicked to make a mark, the panel has focus and the keys arrive.
 *
 * @author Jamie Macaulay
 */
public class MarkKeyHandlerFX {

	private TDGraphFX tdGraphFX;

	private EventHandler<KeyEvent> keyFilter;

	public MarkKeyHandlerFX(TDGraphFX tdGraphFX) {
		this.tdGraphFX = tdGraphFX;
		keyFilter = (keyEvent) -> keyPressed(keyEvent);
		/*
		 * The graph has no scene until it's been added to one, which is well after it's
		 * constructed, so follow the scene property rather than grabbing it now.
		 */
		tdGraphFX.sceneProperty().addListener((obsVal, oldScene, newScene) -> {
			setScene(oldScene, newScene);
		});
		setScene(null, tdGraphFX.getScene());
	}

	/**
	 * Move the key filter from one scene to another, either of which may be null.
	 * @param oldScene scene to stop listening to.
	 * @param newScene scene to start listening to.
	 */
	private void setScene(Scene oldScene, Scene newScene) {
		if (oldScene != null) {
			oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, keyFilter);
		}
		if (newScene != null) {
			newScene.addEventFilter(KeyEvent.KEY_PRESSED, keyFilter);
		}
	}

	/**
	 * Handle a key press from anywhere in the scene, running any mark action which
	 * matches it.
	 * @param keyEvent the key press.
	 */
	private void keyPressed(KeyEvent keyEvent) {
		if (!tdGraphFX.isHover()) {
			// the mouse is over some other graph or display, so this isn't for us.
			return;
		}
		if (keyEvent.getTarget() instanceof TextInputControl) {
			// don't steal Ctrl+A and friends from someone typing in a text field.
			return;
		}
		List<MarkKeyAction> keyActions = getMarkKeyActions();
		if (keyActions == null) {
			return;
		}
		for (MarkKeyAction keyAction : keyActions) {
			if (keyAction.getKeyCombination() != null && keyAction.getKeyCombination().match(keyEvent)) {
				runAction(keyAction);
				keyEvent.consume();
				return;
			}
		}
	}

	/**
	 * Run a mark action. Observers are Swing based and may open modal dialogs, which
	 * would hang the application if run on the JavaFX thread, so push the action to
	 * the Swing event dispatch thread. This is fire and forget: never wait on the
	 * EDT from the FX thread, since with the display inside a JFXPanel the two
	 * threads can deadlock on each other.
	 * @param keyAction action to run.
	 */
	private void runAction(MarkKeyAction keyAction) {
		if (keyAction.getAction() == null) {
			return;
		}
		SwingUtilities.invokeLater(() -> {
			keyAction.getAction().run();
			// data units may have changed colour, so repaint, back on the FX thread.
			Platform.runLater(() -> tdGraphFX.repaint(0));
		});
	}

	/**
	 * Gather the key actions offered by every observer of the current mark. Each
	 * observer is given the data units it would have been given for the popup menu,
	 * i.e. run through that observer's own mark data selector, so a shortcut does
	 * exactly what the equivalent menu item would have done.
	 * @return list of key actions, or null if there is no mark or no actions.
	 */
	private List<MarkKeyAction> getMarkKeyActions() {

		TDOverlayAdapter currentMarker = tdGraphFX.getOverlayMarkerManager().getCurrentMarker();
		if (currentMarker == null) {
			return null;
		}
		DetectionGroupSummary detectionGroup = currentMarker.getSelectedDetectionGroup();
		if (detectionGroup == null || detectionGroup.getNumDataUnits() == 0) {
			return null;
		}
		OverlayMarker overlayMarker = detectionGroup.getOverlayMarker();
		if (overlayMarker == null) {
			return null;
		}
		OverlayMark overlayMark = detectionGroup.getOverlayMark();
		if (overlayMark != null && overlayMark != overlayMarker.getCurrentMark()) {
			/*
			 * A detection group made by clicking on a single data unit holds whatever mark
			 * happened to exist at the time, which is then destroyed. Don't go back to a
			 * mark which is no longer the live one, or we'd act on the wrong data units.
			 */
			overlayMark = null;
		}

		List<MarkKeyAction> allActions = new ArrayList<>();
		for (OverlayMarkObserver observer : overlayMarker.getObservers()) {
			List<PamDataUnit> observerData = null;
			if (overlayMark != null) {
				observerData = overlayMarker.getSelectedMarkedDataUnits(overlayMark,
						observer.getMarkDataSelector(overlayMarker));
			}
			else {
				// a single clicked data unit, which has no mark of its own.
				observerData = detectionGroup.getDataList();
			}
			if (observerData == null || observerData.isEmpty()) {
				continue;
			}
			DetectionGroupSummary observerGroup = new DetectionGroupSummary(detectionGroup.getMouseEvent(),
					overlayMarker, overlayMark, observerData);
			List<MarkKeyAction> keyActions = observer.getMarkKeyActions(observerGroup);
			if (keyActions != null) {
				allActions.addAll(keyActions);
			}
		}
		return allActions.isEmpty() ? null : allActions;
	}
}
