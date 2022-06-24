package clickTrainDetector.layout.dataselector;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.dataSelector.DataSelectParams;
import clickTrainDetector.classification.CTClassifier;
import clickTrainDetector.classification.CTClassifierManager;
import clickTrainDetector.dataselector.CTDataSelector;
import clickTrainDetector.dataselector.CTSelectParams;
import javafx.scene.control.CheckBox;

/**
 * Specific data selector parameters panel for the click train detector. 
 * 
 * @author Jamie Macaulay
 *
 */
public class CTDataSelectPanel implements PamDialogPanel {

	/**
	 * The minimum time between sub detections field. 
	 */
	private JTextField minTimeField;

	/**
	 * The maximum time between sub detections field. 
	 */
	private JTextField maxAngleField;

	/**
	 * The minimum bearing change between sub detections. 
	 */
	private JTextField maxTimeField;

	/**
	 * The main panel. 
	 */
	private JPanel mainPanel;

	/**
	 * Reference to the data selector. 
	 */
	private CTDataSelector ctDataSelector;

	/**
	 * Reference to the last data select params
	 */
	private CTSelectParams currentParams;

	String toolTip ="Data is plotted either after a minimum time has passed <i>and</i>\n" 
			+"bearing change has exceeded maximum angle change or the the time since\n"
			+"last bearing exceeds maximum time.";

	/**
	 * Field for only plotting if has localisation
	 */
	private JCheckBox hasLoc;

	/**
	 * Minimum units before plotting field
	 */
	private JTextField minUnitsField;

	private JPanel classification;

	private JCheckBox[] classifierCheckBoxes;

	private JCheckBox unclassifiedCheckBox;

	private JPanel classificationFilter;

	public CTDataSelectPanel(CTDataSelector ctDataSelector, boolean allowScores) {
		this.ctDataSelector=ctDataSelector; 
		mainPanel = createPanel(); 
	}

	/**
	 * Create the panel for extra map options
	 * @return the panel with user controls. 
	 */
	private JPanel createPanel() {
		//panel for map options
		JPanel dataFilter=new JPanel();
		dataFilter.setBorder(new TitledBorder("General"));
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new PamGridBagContraints();
		dataFilter.setLayout(layout);

		//the minimum time between bearings
		constraints.gridy = 0;
		constraints.gridx = 0;
		constraints.gridwidth=3;
		PamDialog.addComponent(dataFilter,  hasLoc = new JCheckBox("Needs range localisation"), constraints);

		constraints.gridwidth=1;

		//next lines
		constraints.gridy++;

		//the minimum time between bearings
		constraints.gridx = 0;
		PamDialog.addComponent(dataFilter, new JLabel("Minimum sub detections"), constraints);

		constraints.gridx++;
		PamDialog.addComponent(dataFilter, minUnitsField = new JTextField(4), constraints);
		
		
		classificationFilter=new JPanel();
		classificationFilter.setBorder(new TitledBorder("Species classification"));
		layout = new GridBagLayout();
		constraints = new PamGridBagContraints();
		classificationFilter.setLayout(layout);
		constraints.gridy = 0;
		constraints.gridx = 0;
		constraints.gridwidth=3;
		PamDialog.addComponent(classificationFilter, classification = createClassificationPanel(), constraints);


		//panel for map options
		JPanel filterPanel=new JPanel(); 
		filterPanel.setBorder(new TitledBorder("Sub Detections"));
		layout = new GridBagLayout();
		constraints = new PamGridBagContraints();
		filterPanel.setLayout(layout);

		//the minimum time between bearings
		constraints.gridy = 0;
		constraints.gridx = 0;
		PamDialog.addComponent(filterPanel, new JLabel("Minimum time between bearings"), constraints);

		constraints.gridx++;
		PamDialog.addComponent(filterPanel, minTimeField = new JTextField(4), constraints);

		constraints.gridx++;
		PamDialog.addComponent(filterPanel, new JLabel("s"), constraints);

		minTimeField.setToolTipText(toolTip);

		//next lines
		constraints.gridy++;

		//the minimum time between bearings
		constraints.gridx = 0;
		PamDialog.addComponent(filterPanel, new JLabel("Maximum time between bearings"), constraints);

		constraints.gridx++;
		PamDialog.addComponent(filterPanel, maxTimeField = new JTextField(4), constraints);

		constraints.gridx++;
		PamDialog.addComponent(filterPanel, new JLabel("s"), constraints);

		maxTimeField.setToolTipText(toolTip);


		//next lines
		constraints.gridy++;

		constraints.gridx = 0;
		PamDialog.addComponent(filterPanel, new JLabel("Max bearing change"), constraints);

		constraints.gridx++;
		PamDialog.addComponent(filterPanel, maxAngleField = new JTextField(4), constraints);

		constraints.gridx++;
		PamDialog.addComponent(filterPanel, new JLabel("\u00B0"), constraints);

		maxAngleField.setToolTipText(toolTip);
		
		
		//make the main Panel
		JPanel mainPanel = new JPanel(); 
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(dataFilter, BorderLayout.NORTH); //preferred
		mainPanel.add(classificationFilter, BorderLayout.CENTER); //changes size. 
		mainPanel.add(filterPanel, BorderLayout.SOUTH); //preferred

		return mainPanel;
	}

	private JPanel createClassificationPanel() {
		JPanel panel = new JPanel(); 
		GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);

		return panel;
	}

	/**
	 * Set up the classification pane with the right species check boxes etc. 
	 */
	private void setClassificationPane() {
		
		GridBagConstraints constraints = new PamGridBagContraints();
		constraints.gridy = 0;
		constraints.gridx = 0;
		constraints.gridwidth=3;
		
		classification.removeAll(); //clear all previous check boxes. 
		
		CTClassifierManager classifcationManager = ctDataSelector.getClickControl().getClassifierManager(); 
		
		if (classifcationManager.getCurrentClassifiers().size()<1) {
			//don't show the classification pane if there are no classiifcations. 
			this.classificationFilter.setVisible(false);
		}
		this.classificationFilter.setVisible(true);

		
		PamDialog.addComponent(classification,  unclassifiedCheckBox = new JCheckBox("All"), constraints);
		unclassifiedCheckBox.addActionListener((action)->{
			setCheckBoxEnable();
		});
		constraints.gridy++; 

		classifierCheckBoxes = new JCheckBox[classifcationManager.getCurrentClassifiers().size()]; 
		for (int i=0; i<classifcationManager.getCurrentClassifiers().size(); i++) {
			//System.out.println("Classifications: " + i); 
			PamDialog.addComponent(classification,  classifierCheckBoxes[i] = new JCheckBox(classifcationManager.getCurrentClassifiers().get(i).getParams().classifierName), constraints);
			classifierCheckBoxes[i].setSelected(true); //default should be selected
			constraints.gridy++; 
		}	
		classification.validate(); //make sure everything is laid out properly.
		
		setClassifierParams(); 
		
		setCheckBoxEnable(); 
	}
	
	private void setCheckBoxEnable() {
		if (classifierCheckBoxes!=null) {
			for (int i=0; i<classifierCheckBoxes.length; i++) {
				classifierCheckBoxes[i].setEnabled(!unclassifiedCheckBox.isSelected());
			}
		}
	}
	
	
	/**
	 * Set the classifier parameters. 
	 */
	private void setClassifierParams() {
		
		unclassifiedCheckBox.setSelected(currentParams.needsClassification ); 

		if (currentParams.classifier==null ) {
			for (int i=0; i<classifierCheckBoxes.length; i++) {
					classifierCheckBoxes[i].setSelected(true);
			}
			return; 
		}
		
		for (int i=0; i<classifierCheckBoxes.length; i++) {
			classifierCheckBoxes[i].setSelected(false);
			for (int j=0; j<currentParams.classifier.length; j++) {
				if (ctDataSelector.getClickControl().getClassifierManager().getCurrentClassifiers().get(i).getSpeciesID()==currentParams.classifier[j]) {
					classifierCheckBoxes[i].setSelected(true);
				}
			}
		}

	}
		
	
	/**
	 * Get the parameters for the classifiers to  use. 	
	 */
	private void getClassifierParams() {
			currentParams.needsClassification  = unclassifiedCheckBox.isSelected(); 
			
			int count = 0; 
			for (int i=0; i<classifierCheckBoxes.length; i++) {
				if (classifierCheckBoxes[i].isSelected()) {
					count++; 				
				}
			}
			
//			System.out.println("No. count: " + count);
			currentParams.classifier = new int[count];  
			ArrayList<CTClassifier> classifiers = ctDataSelector.getClickControl().getClassifierManager().getCurrentClassifiers();
			int used = 0;
			for (int i=0; i<classifierCheckBoxes.length; i++) {
				if (classifierCheckBoxes[i].isSelected()) {
					currentParams.classifier[used++] = classifiers.get(i).getSpeciesID(); 	
				}
			}
		
	}

	
	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		if (currentParams!=null) {

			//sub detection plots
			minTimeField.setText(String.format("%.2f", currentParams.minTime/1000.));
			maxAngleField.setText(String.format("%.2f", Math.toDegrees(currentParams.maxAngleChange)));
			maxTimeField.setText(String.format("%.2f", currentParams.maxTime/1000.));

			//click train plotting
			minUnitsField.setText(String.format("%d", currentParams.minSubDetections));
			hasLoc.setSelected(currentParams.needsLoc);
			
//			System.out.println("No. set classiifers: " + currentParams.classifier.length);
			
			setClassificationPane(); 
			
		}
		
		
	}

	/**
	 * Get the parameters from controls. 
	 * @param dataSelectParams - the params to set fields from controls. 
	 * @return the input params with changed fields (not cloned)
	 */
	public CTSelectParams getParams(CTSelectParams dataSelectParams){
		this.currentParams  = dataSelectParams;
		getParams();
		return currentParams;
	}

	@Override
	public boolean getParams() {
		if (currentParams == null) {
			currentParams = new CTSelectParams();
		}
		try {
			//System.out.println("Get Params: ");

			Double minTime  =  (Double.valueOf(minTimeField.getText())*1000.); //millis
			Double maxTime  =  (Double.valueOf(maxTimeField.getText())*1000.); //millis
			Double maxAngleChange  =  Double.valueOf(maxAngleField.getText()); 

			currentParams.minTime = minTime.longValue();
			currentParams.maxTime = maxTime.longValue();
			currentParams.maxAngleChange = Math.toRadians(maxAngleChange.doubleValue());

			//click train plotting
			currentParams.minSubDetections = Integer.valueOf(minUnitsField.getText());
			currentParams.needsLoc = hasLoc.isSelected();
			
			// get the classifier parameters.
			getClassifierParams();
			
			//System.out.println("No. getParams classifiers: " + currentParams.classifier.length);

			return true; 
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void setParams(CTSelectParams dataSelectParams) {
		this.currentParams  = dataSelectParams; 
		if (currentParams == null) {
			currentParams = new CTSelectParams();
		}
		setParams(); 

	}

}
