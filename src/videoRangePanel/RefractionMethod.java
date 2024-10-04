package videoRangePanel;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serializable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.SettingsPane;
import PamView.dialog.PamDialog;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;
import videoRangePanel.layoutAWT.RangeDialogPanel;
import videoRangePanel.layoutFX.RefractionMethodPane;

public class RefractionMethod extends RoundEarthMethod implements PamSettings {

	private static final double specificGasConstant = 286.9;
	private static final double airRefractiveIndex = 0.000226;
	private static final double kelvin = 273.15;
	
	private static final int MAXITERATIONS = 10;
	
	RefractionParameters refractionParameters = new RefractionParameters();
	
	RefractionDialogPanel refractionDialogPanel;
	
	/**
	 * FX settings pane for refraction parameters. 
	 */
	private DynamicSettingsPane<RefractionParameters> rangeSettingsPane;
	
	public RefractionMethod(VRControl vrControl) {
		super(vrControl);
		PamSettingManager.getInstance().registerSettings(this);
		refractionDialogPanel = new RefractionDialogPanel();
	}

	@Override
	String getName() {
		return "Refraction";
	}
	
	@Override
	public SettingsPane<RefractionParameters> getRangeMethodPane() {
		if (this.rangeSettingsPane==null) {
			rangeSettingsPane = new RefractionMethodPane(null); 
			rangeSettingsPane.addSettingsListener(()->{
				RefractionParameters params=rangeSettingsPane.getParams(refractionParameters); 
				if (params!=null) refractionParameters=params; 
			});
		}
		rangeSettingsPane.setParams(refractionParameters);
		return rangeSettingsPane;
	}
	

	@Override
	public double getRange(double height, double angle) {
		// get an initial starting range from the round earth method super class. 
		double roundRange =  super.getRange(height, angle);
		
		// then interate to correct
		// sort out a few parameters to get into same format as used by JG
		
		double cameraHeight = vrControl.getCurrentHeight();
		
		double rayRadius = getRayRadius();
		
		/*
		 * (JG VB code)
		 * refracteddiptohorizon = Atn(Sqr(2 * dblHeight * (1 / dblRadius - 1 / r)))
		 */
		double refractedDip = getHorizonAngle(cameraHeight); 
		
		/*
		 * (JG VB code)
		 *   theoreticaldiptohorizon = Atn(-(dblRadius / (dblRadius + dblHeight)) / 
		 *   Sqr(-(dblRadius / (dblRadius + dblHeight)) ^ 2 + 1)) + 2 * Atn(1)
//		 */
//		double theoreticalDip = Math.atan(-(earthRadius / (earthRadius + cameraHeight)) / 
//				Math.sqrt(1 - Math.pow(earthRadius / (earthRadius + cameraHeight),2) ) + Math.PI/2);
//		
		/*
		 * (JG VB code)
		 * psi = dblPi / 2 - dblTheta - theoreticaldiptohorizon
		 */
		double psi;// = Math.PI / 2 - angle - theoreticalDip;
		
		double range = roundRange;
		double newRange;
		int iterationCount = 0;
		double alpha;
		while (true) {
			/**
			 * (JG VB code)
    dblAlpha = d / (2 * r)
    psi = dblPi / 2 - dblTheta - dblAlpha - refracteddiptohorizon
    d = (dblRadius + dblHeight) * Cos(psi) - Sqr(((dblRadius + dblHeight) ^ 2 * 
    Cos(psi) ^ 2) - (dblRadius + dblHeight) ^ 2 + dblRadius ^ 2)
			 */
			alpha = getAlphaCorrection(range);
			psi = Math.PI / 2 - angle - alpha - refractedDip;
//			newRange = (earthRadius + cameraHeight) * Math.cos(psi) - 
//			Math.sqrt(Math.pow(earthRadius+cameraHeight,2) * Math.pow(Math.cos(psi),2) - 
//					Math.pow(earthRadius + cameraHeight, 2) + Math.pow(earthRadius, 2));
			
			newRange = rangeFromPsi(cameraHeight, psi);
//			System.out.println(String.format("Old range %.1f, new Range %.1f", range, newRange));
			
			if (Math.abs(range - newRange) < 1) { // stop iterating when correction is < 1m
				range = newRange;
				break;
			}
			range = newRange;
			
			if (++iterationCount > MAXITERATIONS) {
				break;
			}
		}
		
		return range;
	}
	
	private double getRayRadius() {

		double degKelvin = kelvin + refractionParameters.seaSurfactCelcius;
		//dblrho = dblPress * 100 * dblBeta / dblTemp  'density in kg m-3 (JG VB code)
		double airDensity = refractionParameters.atmosphericPressure * 100 / specificGasConstant / degKelvin;
		/*
		 * (JG VB code)
		 *   r = 1 / ((dblEpsi * dblrho) / 
		 *   ((1 + dblEpsi * dblrho) * dblTemp) * 
		 *   (dblDeltaT + dblGrav * dblBeta))  'radius of curvature of refracted ray
		 */
		double rayRadius = 1./((airRefractiveIndex * airDensity) / 
				((1. + airRefractiveIndex * airDensity) * degKelvin) *
				(refractionParameters.tempGradient + gravity / specificGasConstant));
		
		return rayRadius;
	}
	
	private double getAlphaCorrection(double range) {
		return range / (2 * getRayRadius());
	}
	
	private double getTanHorizonDip(double cameraHeight) {
		return Math.sqrt(2*cameraHeight * (1 / earthRadius - 1 / getRayRadius()));
	}
	

	@Override
	public double getHorizonDistance(double height) {
		/*
		 * Eq. 8 from Leaper and Gordon
		 */
		return 1./(1./earthRadius - 1./getRayRadius()) * getTanHorizonDip(height);
	}

	@Override
	public double getAngle(double height, double range) {
		if (range > getHorizonDistance(height)) {
			return -1;
		}
		return Math.PI/2 - getHorizonAngle(height) - psiFromRange(height, range) - getAlphaCorrection(range);
	}

	@Override
	protected double getHorizonAngle(double height) {
		return Math.atan(getTanHorizonDip(height));
	}

	@Override
	public RangeDialogPanel dialogPanel() {
		return refractionDialogPanel;
	}

	@Override
	public Serializable getSettingsReference() {
		return refractionParameters;
	}

	@Override
	public long getSettingsVersion() {
		return RefractionParameters.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		return vrControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Video Range Refraction";
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		refractionParameters = ((RefractionParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}
	
	class RefractionDialogPanel implements RangeDialogPanel  {
		
		JPanel panel;
		JTextField temp, tempGradient, pressure;
		RefractionDialogPanel() {
			panel = new JPanel();
			panel.setLayout(new GridBagLayout());
			panel.setBorder(new TitledBorder("Refraction parameters"));
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = c.gridy = 0;
			c.insets = new Insets(2,2,2,2);
			c.fill = GridBagConstraints.HORIZONTAL;
			PamDialog.addComponent(panel, new JLabel("Temperature "), c);
			c.gridx++;
			PamDialog.addComponent(panel, temp = new JTextField(6), c);
			c.gridx++;
			PamDialog.addComponent(panel, new JLabel(" \u00B0C"), c);
			c.gridx = 0;
			c.gridy++;
			PamDialog.addComponent(panel, new JLabel("Temperature Gradient"), c);
			c.gridx++;
			PamDialog.addComponent(panel, tempGradient = new JTextField(6), c);
			c.gridx++;
			PamDialog.addComponent(panel, new JLabel(" \u00B0C/m"), c);
			c.gridx = 0;
			c.gridy++;
			PamDialog.addComponent(panel, new JLabel("Atmospheric Pressure "), c);
			c.gridx++;
			PamDialog.addComponent(panel, pressure = new JTextField(6), c);
			c.gridx++;
			PamDialog.addComponent(panel, new JLabel(" milliBar"), c);
			c.gridx = 0;
			c.gridy++;
			
		}

		@Override
		public Component getPanel() {
			return panel;
		}

		@Override
		public boolean getParams() {
			try {
				refractionParameters.seaSurfactCelcius = Double.valueOf(temp.getText());
				refractionParameters.tempGradient = Double.valueOf(tempGradient.getText());
				refractionParameters.atmosphericPressure = Double.valueOf(pressure.getText());
			}
			catch (NumberFormatException ex) {
				return false;
			}
			return true;
		}

		@Override
		public void setParams() {
			
			temp.setText(String.format("%.1f", refractionParameters.seaSurfactCelcius));
			tempGradient.setText(String.format("%.4f", refractionParameters.tempGradient));
			pressure.setText(String.format("%.1f", refractionParameters.atmosphericPressure));
			
		}
		
	}

}
