package angleVetoes;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;

/**
 * Class to support functions to handle vetoes on angles to clicks
 * <p>
 * This is all much more complicated than in RainbowClick since
 * there may be multiple channel groups and there may be multiple vetoes
 * looking at specific angles. For now though, it just handles min and max angles to veto, 
 * but you can have multiple vetoes. 
 * 
 * @author Douglas Gillespie
 *
 */
public class AngleVetoes extends Object implements PamSettings {

	private PamControlledUnit pamControlledUnit;
	
	private AngleVetoParameters angleVetoParameters = new AngleVetoParameters();
	
	private AngleVetoDisplay angleVetoDisplay;
	
	public AngleVetoes(PamControlledUnit pamControlledUnit) {
		super();
		this.pamControlledUnit = pamControlledUnit;
		

		if (PamGUIManager.getGUIType() != PamGUIManager.NOGUI) {
			angleVetoDisplay = new AngleVetoDisplay(this);
		}
		
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public Serializable getSettingsReference() {
		return angleVetoParameters;
	}

	@Override
	public long getSettingsVersion() {
		return AngleVetoParameters.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		return pamControlledUnit.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Detector Vetoes";
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		angleVetoParameters = ((AngleVetoParameters) pamControlledUnitSettings.getSettings()).clone();
		return (angleVetoParameters != null);
	}

	/**
	 * Get a menu item for inclusion in menus of detectors using the veto (i.e. add the output of this function to 
	 * the detection menu for the detector). 
	 * @param frame Frame holding the menu
	 * @return a menu item. 
	 */
	public JMenuItem getSettingsMenuItem(Frame frame) {
		JMenuItem menuItem = new JMenuItem("Angle Vetoes ...");
		menuItem.addActionListener(new SettingsAction(frame));
		return menuItem;
	}
	
	/**
	 * ActionListener for the getSettingsMenuItem() 
	 * @author Douglas Gillespie
	 *
	 */
	class SettingsAction implements ActionListener {
		
		private Frame frame;
		
		public SettingsAction(Frame frame) {
			super();
			this.frame = frame;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			getNewSettings(frame);
		}
	}
	
	private void getNewSettings(Frame frame) {
		AngleVetoParameters newParams = AngleVetoesDialog.showDialog(frame, this);	
		if (newParams != null) {
			angleVetoParameters = newParams.clone();
			if (angleVetoDisplay != null) {
				angleVetoDisplay.repaint();
			}
		}
	}

	/**
	 * Get a menu item for inclusion in menus of detectors using the veto (i.e. add the output of this function to 
	 * the display menu for the detector). 
	 * @param frame Frame holding the menu
	 * @return a menu item. 
	 */
	public JMenuItem getDisplayMenuItem(Frame frame) {
		JMenuItem menuItem = new JMenuItem("Angle Vetoes ...");
		menuItem.addActionListener(new DisplayAction(frame));
		return menuItem;
	}
	
	/**
	 * Action listener for the display menu
	 * @author Douglas Gillespie
	 *
	 */
	class DisplayAction implements ActionListener {
		
		private Frame frame;
		
		public DisplayAction(Frame frame) {
			super();
			this.frame = frame;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			displayAction(frame);
		}
	}
	
	private void displayAction(Frame frame) {
		if (angleVetoDisplay != null) {
			angleVetoDisplay.setVisible(true);
		}
	}

	/**
	 * Access to the veto parameters
	 * @return AngleVetoParameters
	 */
	public AngleVetoParameters getAngleVetoParameters() {
		return angleVetoParameters;
	}

	
	/**
	 * set angle veto parameters
	 * @param angleVetoParameters
	 */
	public void setAngleVetoParameters(AngleVetoParameters angleVetoParameters) {
		this.angleVetoParameters = angleVetoParameters;
	}
	
	/**
	 * Test to see if a given angle passes a particular veto
	 * @param vetoIndex index of veto
	 * @param angle angle in degrees
	 * @return tru if NOT vetoed, false if vetoed. 
	 */
	public boolean passVeto(int vetoIndex, double angle) {
		AngleVeto angleVeto = angleVetoParameters.getVeto(vetoIndex);
		return passVeto(angleVeto, angle);
	}

	/**
	 * Test to see if a given angle passes a particular veto
	 * @param angleVeto reference to veto
	 * @param angle angle in degrees
	 * @return tru if NOT vetoed, false if vetoed. 
	 */
	public boolean passVeto(AngleVeto angleVeto, double angle) {
		angle = Math.abs(angle);
		return (angle < angleVeto.startAngle || angle > angleVeto.endAngle);
	}
	
	/**
	 * Test a particular angle to see if it passes all veto tests. 
	 * <p>
	 * Optionally add data about the angle and whether or not it passed the
	 * tests to the display data histograms. 
	 * @param angle angle to test in degrees
	 * @param collectStats set to true if you want to collect stats for display purposes. 
	 * @return true if passes all tests. False if it ails one or more (true if there are no vetoes)
	 */
	public boolean passAllVetoes(double angle, boolean collectStats) {
		int count = angleVetoParameters.getVetoCount();
		boolean pass = true;
		for (int i = 0; i < count; i++) {
			if (!passVeto(i, angle)) {
				pass = false;
				break;
			}
		}
		if (collectStats) {
			addAngleData(angle);
			addPassData(pass);
		}
		
		return pass;
	}
	
	/**
	 * Add data to angle stats histogram for display
	 * @param angle angle in degrees
	 */
	public void addAngleData(double angle) {
		if (angleVetoDisplay != null) {
			angleVetoDisplay.newAngle(angle);
		}
	}
	
	/**
	 * Add data to pas / fail stats for display 
	 * @param pass true if there was a pass, false for a fail. 
	 */
	public void addPassData(boolean pass) {
		if (angleVetoDisplay != null) {
			angleVetoDisplay.newPassData(pass);
		}
	}
}
