package PamView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.materialdesign2.MaterialDesignR;
import org.kordamp.ikonli.swing.FontIcon;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.RawInputControlledUnit;
import PamUtils.PamCalendar;
import PamView.PamColors.PamColor;
import PamView.component.PamSettingsIconButton;
import PamView.dialog.PamButton;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import warnings.SingleLineWarningDisplay;

public class TopToolBar extends PamToolBar implements ColorManaged {

	//	static private TopToolBar topToolBar;

	private JLabel timeUTC;
	private PamGui pamGUI;
	private Component currentPCUComponent = null;
	private PamControlledUnit currentControlledUnit = null;
	PamController pamController;
	private JButton startButton, stopButton;
	private JPanel moduleBit;
//	private WarningLabel lastWarning;
	private SingleLineWarningDisplay lastWarning;

	static private MenuItemEnabler startEnabler = new MenuItemEnabler(false);
	static private MenuItemEnabler stopEnabler = new MenuItemEnabler(false);

	public TopToolBar (PamGui pamGUI) {

		super("Pamguard");
		add(timeUTC = new PamLabel("Time UTC"));
		timeUTC.setFont(PamColors.getInstance().getBoldFont());
		this.addSeparator(new Dimension(10, 0));

		pamController = PamController.getInstance();
		if (pamController.getRunMode() == PamController.RUN_PAMVIEW) {
			add(startButton = new PamButton(FontIcon.of(MaterialDesignP.PLAY, PamSettingsIconButton.NORMAL_SIZE, Color.DARK_GRAY)));
			startButton.setDisabledIcon(FontIcon.of(MaterialDesignP.PLAY, PamSettingsIconButton.NORMAL_SIZE, Color.LIGHT_GRAY));
			startButton.setToolTipText("Start sound playback");
			add(stopButton = new PamButton(FontIcon.of(MaterialDesignP.PAUSE, PamSettingsIconButton.NORMAL_SIZE, Color.DARK_GRAY)));
			stopButton.setDisabledIcon(FontIcon.of(MaterialDesignP.PAUSE, PamSettingsIconButton.NORMAL_SIZE, Color.LIGHT_GRAY));

			stopButton.setToolTipText("Stop sound playback");
		}
		else {
			add(startButton = new PamButton(FontIcon.of(MaterialDesignR.RECORD_CIRCLE, PamSettingsIconButton.NORMAL_SIZE, Color.RED)));
			startButton.setDisabledIcon(FontIcon.of(MaterialDesignR.RECORD_CIRCLE, PamSettingsIconButton.NORMAL_SIZE, Color.LIGHT_GRAY));
			startButton.setToolTipText("Start processing");
			startButton.addMouseListener(new StartButtonMouse());
			add(stopButton = new PamButton(FontIcon.of(MaterialDesignP.PAUSE, PamSettingsIconButton.NORMAL_SIZE, Color.DARK_GRAY)));
			stopButton.setDisabledIcon(FontIcon.of(MaterialDesignP.PAUSE, PamSettingsIconButton.NORMAL_SIZE, Color.LIGHT_GRAY));
			stopButton.setToolTipText("Stop processing");
		}
		startButton.addActionListener(new StartButton());
		checkStartTip();
		stopButton.addActionListener(new StopButton());
		startEnabler.addMenuItem(startButton);
		stopEnabler.addMenuItem(stopButton);
		
		PamPanel flexiArea = new PamPanel(new BorderLayout());
		moduleBit = new PamPanel();
		flexiArea.add(BorderLayout.WEST, moduleBit);
		add(flexiArea);
		lastWarning = new SingleLineWarningDisplay();
		flexiArea.add(BorderLayout.EAST, lastWarning.getComponent());
		barTimer.start();
	}

	private class StartButtonMouse extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				doStartPopup(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				doStartPopup(e);
			}
		}
		
	}
	private PamColor defaultColor = PamColor.BORDER;

	public PamColor getDefaultColor() {
		return defaultColor;
	}

	/**
	 * popup menu actions for start button. 
	 * @param e
	 */
	public void doStartPopup(MouseEvent e) {
		if (startButton.isEnabled() == false) {
			return;
		}
		// find the first unit that's a RawInputcontrolledUnit
		ArrayList<PamControlledUnit> rawinputs = PamController.getInstance().findControlledUnits(RawInputControlledUnit.class, true);
		if (rawinputs == null || rawinputs.size() == 0) {
			return;
		}
		RawInputControlledUnit rawinput = (RawInputControlledUnit) rawinputs.get(0);
		rawinput.startButtonXtraActions(startButton, e);
	}

	public void setDefaultColor(PamColor defaultColor) {
		this.defaultColor = defaultColor;
	}

	@Override
	public PamColor getColorId() {
		return defaultColor;
	}



	Timer barTimer = new Timer(500, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent evt) {
			tellTime();
		}
	});

	class StartButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (pamController.getRunMode() == PamController.RUN_PAMVIEW) {
				currentControlledUnit.playViewerSound();
			}
			else {
				pamController.toolBarStartButton(currentControlledUnit);			
			}
		}
	}

	class StopButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (pamController.getRunMode() == PamController.RUN_PAMVIEW) {
				currentControlledUnit.stopViewerSound();
			}
//			else {
				pamController.toolBarStopButton(currentControlledUnit);
//			}
		}
	}

	private void tellTime() {
		timeUTC.setText(PamCalendar.formatDateTime(PamCalendar.getTimeInMillis(), true));
		checkStartTip();
	}

	/**
	 * Only display component from the pam controlled unit which is currently
	 * on view. 
	 * @param pamControlledUnit
	 */
	public void setActiveControlledUnit(PamControlledUnit pamControlledUnit) {

		checkStartTip();
		
		/** 
		 * Enable items in the main part of the toolbar
		 */
		if (pamController.getRunMode() == PamController.RUN_PAMVIEW) {
			if (pamControlledUnit != null) {
				startEnabler.enableItems(pamControlledUnit.canPlayViewerSound());
			}
			else {
				startEnabler.enableItems(false);
			}
		}
		
		/**
		 * Set module specific part of toolbar. 
		 */
		currentControlledUnit = pamControlledUnit;
		Component newComponent = null;
		if (pamControlledUnit != null) {
			newComponent = pamControlledUnit.getToolbarComponent();
		}
		if (currentPCUComponent == newComponent) {
			return;
		}
		if (currentPCUComponent != null) {
			moduleBit.remove(currentPCUComponent);
			currentPCUComponent = null;
		}
		if (newComponent != null) {
			refreshComponentTheme(newComponent);
			moduleBit.add(newComponent);
			currentPCUComponent = newComponent;
		}
	}

	/**
	 * Colour scheme version each module tool bar component was last styled at.
	 * Weakly keyed so a module being removed doesn't leave an entry behind.
	 */
	private static final Map<Component, Integer> styledVersions =
			Collections.synchronizedMap(new WeakHashMap<Component, Integer>());

	/**
	 * Bring a module's tool bar component up to date with the current colour scheme.
	 * <p>
	 * These components are only in the window while their own tab is showing - they
	 * are added and removed by {@link #setActiveControlledUnit}. A component which
	 * was detached when the user changed colour scheme missed both the look and feel
	 * update and the recolouring, so it would otherwise come back still wearing the
	 * old scheme. Nothing happens if it is already up to date.
	 *
	 * @param component the tool bar component about to be shown.
	 */
	private void refreshComponentTheme(Component component) {
		PamColors pamColors = PamColors.getInstance();
		int version = pamColors.getColourSchemeVersion();
		Integer styledAt = styledVersions.get(component);
		if (styledAt != null && styledAt.intValue() == version) {
			return;
		}
		styledVersions.put(component, version);
		try {
			SwingUtilities.updateComponentTreeUI(component);
			if (component instanceof Container) {
				pamColors.notifyContianer((Container) component);
			}
			component.invalidate();
			component.repaint();
		}
		catch (Exception e) {
			// a tool bar which can't be restyled mustn't stop the tab being shown
			System.out.printf("Error restyling tool bar component %s: %s\n",
					component.getClass().getName(), e.getMessage());
			e.printStackTrace();
		}
	}
	/**
	 * Enable all start buttons in all top menu bars. 
	 * In normal mode, this is generally controlled
	 * by PamController. In Viewer mode, this is more
	 * dependent on the topmost tab and whether or not
	 * it contains components which support sound playback
	 * @param enable
	 */
	public static void enableStartButton(boolean enable) {
		startEnabler.enableItems(enable);
	}
	/**
	 * Enable all stop buttons in all top menu bars. 
	 * In normal mode, this is generally controlled
	 * by PamController. In Viewer mode, this is more
	 * dependent on the topmost tab and whether or not
	 * it contains components which support sound playback
	 * @param enable
	 */
	public static void enableStopButton(boolean enable) {
		stopEnabler.enableItems(enable);
	}

	private void checkStartTip() {

		ArrayList<PamControlledUnit> rawinputs = PamController.getInstance().findControlledUnits(RawInputControlledUnit.class, true);
		if (rawinputs == null || rawinputs.size() == 0) {
			return;
		}
		RawInputControlledUnit rawinput = (RawInputControlledUnit) rawinputs.get(0);
		String tip = rawinput.getStartButtonToolTip();
		if (tip != null) {
			startButton.setToolTipText(tip);
		}
		else if (pamController.getRunMode() == PamController.RUN_PAMVIEW) {
			startButton.setToolTipText("Start sound playback");
		}
		else {
			startButton.setToolTipText("Start processing");
		}
	}

}
