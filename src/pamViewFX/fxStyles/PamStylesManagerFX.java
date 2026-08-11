/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */



package pamViewFX.fxStyles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;

import PamView.ColourScheme;
import PamView.PamColors;

/**
 * Holds the JavaFX style (i.e. the set of CSS style sheets) currently in use and
 * keeps track of the long lived scenes and nodes it has been applied to, so that
 * they can be restyled when the user changes colour scheme.
 * <p>
 * Dialogs don't need to register - they are built afresh each time they are
 * shown and so pick up whatever style is current. It is the displays which live
 * for the whole session (the time display, data map, detection displays...) that
 * would otherwise stay light after a switch to a dark scheme.
 *
 * @author mo55
 * @author Jamie Macaulay
 */
public class PamStylesManagerFX {

	/**
	 * Style category: the main GUI (displays and their controls).
	 */
	public static final int STYLE_GUI = 0;

	/**
	 * Style category: standard settings dialogs.
	 */
	public static final int STYLE_DIALOG = 1;

	/**
	 * Style category: the sliding / hiding settings panes.
	 */
	public static final int STYLE_SLIDING = 2;

	/**
	 * Style class added to nodes decorated with the GUI style. The GUI style
	 * sheets declare their colour palette on '.root, .gui-css-root' - '.root' only
	 * matches a scene root, so a node the sheet is attached to directly must carry
	 * this class for the looked up colours to resolve.
	 */
	public static final String GUI_CSS_ROOT_CLASS = "gui-css-root";

	/**
	 * Style class giving a node the plot window colour (PamColor.PlOTWINDOW) of the
	 * current colour scheme. For the areas data is drawn on.
	 */
	public static final String PLOT_PANE_STYLE_CLASS = "pam-plot-pane";

	/**
	 * Style class giving a node the window background colour of the current colour
	 * scheme. For the axis panes and the corner squares between them.
	 */
	public static final String AXIS_PANE_STYLE_CLASS = "pam-axis-pane";

	/**
	 * Singleton instance of the style manager
	 */
	private static PamStylesManagerFX singleInstance = null;

	/**
	 * The current style to use
	 */
	private PamDefaultStyle curStyle;

	/**
	 * The scenes and nodes which have been styled, and the style sheets which were
	 * added to each of them. Weakly keyed so that registering a display here never
	 * stops it being garbage collected once it's closed.
	 */
	private final Map<Object, StyledTarget> styledTargets =
			Collections.synchronizedMap(new WeakHashMap<Object, StyledTarget>());

	/**
	 * Notified when the colour scheme changes.
	 * <p>
	 * Style sheets cover anything drawn by JavaFX itself, but the displays draw a
	 * lot onto Canvases, where the colours are picked in Java rather than in CSS.
	 * Those need telling when the scheme changes.
	 */
	public interface ColourSchemeListener {

		/**
		 * The colour scheme has changed. Always called on the JavaFX application
		 * thread, after the style sheets have been swapped.
		 */
		void colourSchemeChanged();
	}

	/**
	 * Listeners to tell when the colour scheme changes. Weakly held, so registering
	 * never stops a display being garbage collected once it's closed - the listener
	 * is normally the display itself, which its parent holds for as long as it
	 * lives.
	 */
	private final Map<ColourSchemeListener, Boolean> schemeListeners =
			Collections.synchronizedMap(new WeakHashMap<ColourSchemeListener, Boolean>());

	/**
	 * Name of the colour scheme the listeners were last told about, so that they are
	 * only called when it really changes.
	 */
	private String lastSchemeName;

	/**
	 * Record of the style applied to one scene or node.
	 */
	private static class StyledTarget {

		/**
		 * Which of the three style categories was applied.
		 */
		private final int styleType;

		/**
		 * The style sheets we added last time round. Kept so that they can be removed
		 * again without disturbing any other style sheets on the same target.
		 */
		private List<String> appliedSheets = new ArrayList<>();

		private StyledTarget(int styleType) {
			this.styleType = styleType;
		}
	}

	/**
	 * private singleton constructor
	 */
	private PamStylesManagerFX() {
		this.setDefaultStyle();
	}

	/**
	 * Get the current style manager
	 * @return
	 */
	public static PamStylesManagerFX getPamStylesManagerFX() {
		if (singleInstance == null) {
			singleInstance = new PamStylesManagerFX();
		}
		return singleInstance;
	}
	
	/**
	 * Get the current style to use
	 * @return
	 */
	public PamDefaultStyle getCurStyle() {
		return curStyle;
	}
	
	/**
	 * Set the current style to use
	 * @param newStyle
	 */
	public void setCurStyle(PamDefaultStyle newStyle) {
		curStyle = newStyle;
	}
	
	/**
	 * Set the default style
	 */
	public void setDefaultStyle() {
		curStyle = new PamDefaultStyle();
	}

	/**
	 * Get the style sheets for one of the three style categories.
	 *
	 * @param styleType one of {@link #STYLE_GUI}, {@link #STYLE_DIALOG} or
	 *                  {@link #STYLE_SLIDING}
	 * @return the style sheets for that category with the current colour scheme.
	 */
	public ArrayList<String> getStyleSheets(int styleType) {
		switch (styleType) {
		case STYLE_DIALOG:
			return curStyle.getDialogCSS();
		case STYLE_SLIDING:
			return curStyle.getSlidingDialogCSS();
		case STYLE_GUI:
		default:
			return curStyle.getGUICSS();
		}
	}

	/**
	 * Style a node and remember it, so that it gets restyled whenever the colour
	 * scheme changes.
	 * <p>
	 * Use this in preference to adding the style sheets directly for anything that
	 * lives longer than a single dialog.
	 *
	 * @param node      the node to style. Null is ignored.
	 * @param styleType one of {@link #STYLE_GUI}, {@link #STYLE_DIALOG} or
	 *                  {@link #STYLE_SLIDING}
	 */
	public void styleNode(Parent node, int styleType) {
		if (node == null) {
			return;
		}
		if (styleType == STYLE_GUI && !node.getStyleClass().contains(GUI_CSS_ROOT_CLASS)) {
			node.getStyleClass().add(GUI_CSS_ROOT_CLASS);
		}
		StyledTarget target = new StyledTarget(styleType);
		styledTargets.put(node, target);
		applyStyle(node.getStylesheets(), target);
	}

	/**
	 * Style a scene and remember it, so that it gets restyled whenever the colour
	 * scheme changes.
	 *
	 * @param scene     the scene to style. Null is ignored.
	 * @param styleType one of {@link #STYLE_GUI}, {@link #STYLE_DIALOG} or
	 *                  {@link #STYLE_SLIDING}
	 */
	public void styleScene(Scene scene, int styleType) {
		if (scene == null) {
			return;
		}
		StyledTarget target = new StyledTarget(styleType);
		styledTargets.put(scene, target);
		applyStyle(scene.getStylesheets(), target);
	}

	/**
	 * Swap the style sheets on one target: take off the ones added last time and
	 * put on the current ones. Anything else on the list (e.g. a display's own
	 * extra sheet) is left alone.
	 *
	 * @param stylesheets the target's style sheet list
	 * @param target      record of what was applied to it before
	 */
	private void applyStyle(ObservableList<String> stylesheets, StyledTarget target) {
		stylesheets.removeAll(target.appliedSheets);
		target.appliedSheets = getStyleSheets(target.styleType);
		stylesheets.addAll(target.appliedSheets);
	}

	/**
	 * Restyle every registered scene and node from the current style. Call this
	 * after the colour scheme has changed.
	 * <p>
	 * Safe to call from any thread - the work is marshalled onto the JavaFX
	 * application thread.
	 */
	public void updateStyles() {
		if (!Platform.isFxApplicationThread()) {
			try {
				Platform.runLater(this::updateStyles);
			}
			catch (IllegalStateException e) {
				// JavaFX toolkit not started - nothing to restyle.
			}
			return;
		}
		/*
		 * Copy the entries out before touching anything: applying a style can trigger
		 * layout, which mustn't run while the weak map is locked.
		 */
		ArrayList<Object> targets;
		synchronized (styledTargets) {
			targets = new ArrayList<Object>(styledTargets.keySet());
		}
		for (Object object : targets) {
			StyledTarget target = styledTargets.get(object);
			if (target == null) {
				continue;
			}
			if (object instanceof Scene) {
				applyStyle(((Scene) object).getStylesheets(), target);
			}
			else if (object instanceof Parent) {
				applyStyle(((Parent) object).getStylesheets(), target);
			}
		}

		/*
		 * Only tell the listeners when the scheme has really changed. updateStyles is
		 * called on every PamColors.setColors(), which happens whenever a module is
		 * added or any display setting changes; re-applying the style sheets then is
		 * harmless, but telling the listeners would throw away things the user has
		 * chosen since (e.g. a graph background colour) for no reason.
		 */
		ColourScheme scheme = PamColors.getInstance().getColourScheme();
		String schemeName = scheme == null ? null : scheme.getName();
		if (lastSchemeName != null && lastSchemeName.equals(schemeName)) {
			return;
		}
		lastSchemeName = schemeName;

		ArrayList<ColourSchemeListener> listeners;
		synchronized (schemeListeners) {
			listeners = new ArrayList<ColourSchemeListener>(schemeListeners.keySet());
		}
		for (ColourSchemeListener listener : listeners) {
			try {
				listener.colourSchemeChanged();
			}
			catch (Exception e) {
				// one display failing to recolour mustn't stop the rest
				System.out.printf("Error changing colour scheme of %s: %s\n",
						listener.getClass().getName(), e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Register to be told when the colour scheme changes, for anything which picks
	 * its colours in Java rather than in CSS (typically Canvas drawing).
	 * <p>
	 * The listener is held weakly, so there is no need to unregister it - but do
	 * make sure it is a long lived object, not a lambda whose only reference is the
	 * call to this method, or it will be collected straight away.
	 *
	 * @param listener the listener to add.
	 */
	public void addColourSchemeListener(ColourSchemeListener listener) {
		if (listener != null) {
			schemeListeners.put(listener, Boolean.TRUE);
		}
	}

	/**
	 * Stop being told about colour scheme changes.
	 *
	 * @param listener the listener to remove.
	 */
	public void removeColourSchemeListener(ColourSchemeListener listener) {
		schemeListeners.remove(listener);
	}

}
