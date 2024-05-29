package clickTrainDetector.layout.localisation;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.PamAWTUtils;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import annotation.handler.AnnotationOptions;
import annotation.localise.targetmotion.TMSettingsPanel;
import clickDetector.localisation.GeneralGroupLocaliser;
import clickTrainDetector.localisation.CTLocParams;
import clickTrainDetector.localisation.CTMAnntoationType;

/**
 * Settings panel with some additional settings. 
 * 
 * @author Jamie Macaulay
 *
 */
public class CTMSettingsPanel extends TMSettingsPanel {

	/**
	 * The minimum number of sub detections field
	 */
	private JTextField minSubDetField;
	
	/**
	 * The minimum angle range field. 
	 */
	private JTextField minAngleRange;

	/**
	 * clickTreainControl;
	 */
	private CTMAnntoationType clickTrainControl;

	/**
	 * Check box tp indicate whether click trian loclaisation should be performed
	 */
	private JCheckBox useLocCheckBox;



	public CTMSettingsPanel(CTMAnntoationType clickAnnotationType, GeneralGroupLocaliser tmGroupLocaliser) {
		super(tmGroupLocaliser);
		this.clickTrainControl=clickAnnotationType; 
//		
//		GridBagConstraints constraints = new GridBagConstraints(); 
//		
//		constraints.gridy=4;
//		constraints.gridwidth=3; 
		
		JPanel useLocPanel=new JPanel(); 
		useLocPanel.setBorder(new TitledBorder("Run Click Train Localisation"));
		useLocCheckBox = new JCheckBox("Localise click trains");
		useLocCheckBox.addActionListener(a->{
			setPanelEnabled(useLocCheckBox.isSelected());
		});
		useLocPanel.add(useLocCheckBox);
		
		super.getDialogComponent().add(useLocPanel, 0); 
		
		
		getFilterPanel().add(createMapLocPanel(), 0);

	}
	
	/**
	 * Set the entire localisation panel enabled or disabled including all low level components. 
	 * @param enable - true to enable. 
	 */
	private void setPanelEnabled(boolean enable) {
		PamAWTUtils.setPanelEnabled(getFilterPanel(), enable);
		PamAWTUtils.setPanelEnabled(getLocPanel(), enable);
	}


	/**
	 * Create the map plot panel. 
	 * @return the component. 
	 */
	private Component createMapLocPanel() {

		//panel for map options
		JPanel filterPanel=new JPanel(); 
		filterPanel.setBorder(new TitledBorder("Pre Localisation Filter"));
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new PamGridBagContraints();
		filterPanel.setLayout(layout);

		//the minimum time between bearings
		constraints.gridy = 0;
		constraints.gridx = 0;
		PamDialog.addComponent(filterPanel, new JLabel("Minimum sub detections"), constraints);

		constraints.gridx++;
		PamDialog.addComponent(filterPanel, minSubDetField = new JTextField(4), constraints);

//		constraints.gridx++;
//		PamDialog.addComponent(filterPanel, new JLabel("s"), constraints);

		//next lines
		constraints.gridy++;

		constraints.gridx = 0;
		PamDialog.addComponent(filterPanel, new JLabel("Min Angle Range"), constraints);

		constraints.gridx++;
		PamDialog.addComponent(filterPanel, minAngleRange = new JTextField(4), constraints);

		constraints.gridx++;
		PamDialog.addComponent(filterPanel, new JLabel("\u00B0"), constraints);

		return filterPanel;
	}
	
//	@Override
//	public void setParams() {
//		super.setParams();
//		
//		minSubDetField.setText(String.format("%d", this.clickTrainControl.get));
//		
//		minAngleRange.setText(String.format("%.2f", this.clickTrainControl.getClickTrainParams().ctLocParams.minAngleRange));
//		
//	}
	
	@Override
	public void setSettings(AnnotationOptions annotationOptions) {
		super.setSettings(annotationOptions);
		minSubDetField.setText(String.format("%d", ((CTLocParams) getAnnotationOptions()).minDataUnits));
		minAngleRange.setText(String.format("%.1f", Math.toDegrees(((CTLocParams) getAnnotationOptions()).minAngleRange)));
		useLocCheckBox.setSelected(((CTLocParams) getAnnotationOptions()).shouldloc);
		setPanelEnabled(useLocCheckBox.isSelected());
	}

	
	@Override
	public AnnotationOptions getSettings() {
		try { 
			((CTLocParams) getAnnotationOptions()).minDataUnits = Integer.valueOf(minSubDetField.getText()); 
			((CTLocParams) getAnnotationOptions()).minAngleRange = Math.toRadians(Double.valueOf(minAngleRange.getText())); 
			((CTLocParams) getAnnotationOptions()).shouldloc = useLocCheckBox.isSelected();
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null; 
		}
		return super.getSettings();
	}

}
