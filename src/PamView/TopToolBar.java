package PamView;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.Timer;

import warnings.PamWarning;
import warnings.SingleLineWarningDisplay;
import warnings.WarningDisplay;
import warnings.WarningLabel;
import warnings.WarningSystem;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.PamColors.PamColor;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;

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
			add(startButton = new JButton(new ImageIcon(ClassLoader
					.getSystemResource("Resources/playStart.png"))));
			startButton.setToolTipText("Start sound playback");
			add(stopButton = new JButton(new ImageIcon(ClassLoader
					.getSystemResource("Resources/playPause.png"))));
			stopButton.setToolTipText("Stop sound playback");
		}
		else {
			add(startButton = new JButton(new ImageIcon(ClassLoader
					.getSystemResource("Resources/recordStart.png"))));
			startButton.setToolTipText("Start PAM processing");
			add(stopButton = new JButton(new ImageIcon(ClassLoader
					.getSystemResource("Resources/playPause.png"))));
			stopButton.setToolTipText("Stop PAM processing");
		}
		startButton.addActionListener(new StartButton());
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

	private PamColor defaultColor = PamColor.BORDER;

	public PamColor getDefaultColor() {
		return defaultColor;
	}

	public void setDefaultColor(PamColor defaultColor) {
		this.defaultColor = defaultColor;
	}

	@Override
	public PamColor getColorId() {
		return defaultColor;
	}



	Timer barTimer = new Timer(500, new ActionListener() {
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
	}

	/**
	 * Only display component from the pam controlled unit which is currently
	 * on view. 
	 * @param pamControlledUnit
	 */
	public void setActiveControlledUnit(PamControlledUnit pamControlledUnit) {

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
			moduleBit.add(newComponent);
			currentPCUComponent = newComponent;
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

}
