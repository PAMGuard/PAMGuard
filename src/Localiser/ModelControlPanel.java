package Localiser;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamDataUnit;

/**
 * Generic pane which shows a list of localisation algorithms 
 * @author Jamie Macaulay 
 *
 */
public class ModelControlPanel {
	
	/**
	 * Image for setting icon
	 */
	public static ImageIcon settings = new ImageIcon(ClassLoader.getSystemResource("Resources/SettingsButtonSmall2.png"));

	
	private JPanel mainPanel;
	
	private JCheckBox[] enableModel;
	
	private JButton[] modelParams;

	/**
	 * List of model s
	 */
	private ArrayList<LocaliserModel> models;

	/**
	 * Panel which holds localiser models. 
	 */
	private JPanel modelPanel;
	
	public ModelControlPanel() {
		 createPanel();
	}

	/**
	 * @param targetMotionLocaliser
	 * @param targetMotionDialog
	 */
	public ModelControlPanel(ArrayList<LocaliserModel> loclaiserList) {
		super();
		this.models=loclaiserList;
		createPanel();
		populateModelPanel(loclaiserList);
	}

	/**
	 * Create the controls 
	 */
	private void createPanel(){
		mainPanel = new JPanel();
		//mainPanel.setBorder(new TitledBorder("Model Control"));
		mainPanel.setLayout(new BorderLayout());
	
		// pane whihc holds controls 
		modelPanel = new JPanel();
		modelPanel.setLayout(new GridBagLayout());
				
		mainPanel.add(BorderLayout.NORTH, modelPanel);
	}
	
	/**
	 * Populate the panel with a list of localisers 
	 * @param loclaiserList list of loclaisers. 
	 */
	public void populateModelPanel(ArrayList<LocaliserModel> loclaiserList){
		
		//swing is so clumsy 
		modelPanel.invalidate();
		mainPanel.remove(modelPanel);
		mainPanel.add(BorderLayout.NORTH, modelPanel=new JPanel());
		modelPanel.setLayout(new GridBagLayout());
		mainPanel.invalidate();
		models = loclaiserList;
		
		GridBagConstraints c = new PamGridBagContraints();

		int nModels = loclaiserList.size();
		enableModel = new JCheckBox[nModels];
		modelParams = new JButton[nModels];
		LocaliserModel<PamDataUnit> model;
		for (int i = 0; i < nModels; i++) {
			model = loclaiserList.get(i);
			enableModel[i] = new JCheckBox(model.getName());
			enableModel[i].addActionListener(new ModelEnable(model));
			modelParams[i] = new JButton(settings);
			modelParams[i].setVisible(model.hasParams());
			modelParams[i].addActionListener(new ModelParams(model));
			enableModel[i].setToolTipText(model.getToolTipText());
			modelParams[i].setToolTipText("Open settings for " +model.getName()+" localiser");
			c.gridx = 0;
			PamDialog.addComponent(modelPanel, enableModel[i], c);
			c.gridx++;
			PamDialog.addComponent(modelPanel, modelParams[i], c);
			c.gridy++;
		}
		modelPanel.validate();
		mainPanel.validate();
	}
		
	class ModelEnable implements ActionListener {
		LocaliserModel<PamDataUnit> model;

		public ModelEnable(LocaliserModel<PamDataUnit> model) {
			super();
			this.model = model;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			modelEnable();
//			System.out.println("Model list");
//			for (int j=0; j<enableModel.length; j++){
//				System.out.println("Model enabled: "+enableModel[j].isSelected());
//			}
		}
	}
	
	
	/**
	 * Called whenever a model is enabled. 
	 */
	public void modelEnable(){
		
	}
	
	
	
	class ModelParams implements ActionListener {
		LocaliserModel<PamDataUnit> model;

		public ModelParams(LocaliserModel<PamDataUnit> model2) {
			
			super();
			this.model = model2;
		}
		@Override
		//settings panel
		public void actionPerformed(ActionEvent arg0) {
			model.getAlgorithmSettingsPane();
			//AWT implementation. 
		}
	}
	
	/**
	 * @return the mainPanel
	 */
	public JPanel getPanel() {
		return mainPanel;
	}
	
	/**
	 * Enable the controls. 
	 */
	public void enableControls() {
		if (models==null) return; 
		int nModels = models.size();
		LocaliserModel<PamDataUnit> model;
		for (int i = 0; i < nModels; i++) {
			model = models.get(i);
			modelParams[i].setEnabled(enableModel[i].isSelected() && model.hasParams());
		}
	}
	
	/**
	 * 
	 * @param i model index
	 * @return true if a particular model is enabled
	 */
	public boolean isEnabled(int i) {
//		System.out.println("Enable list");
//		for (int j=0; j<enableModel.length; j++){
//			System.out.println("Model enabled: "+enableModel[j].isSelected());
//		}
		return enableModel[i].isSelected();
	}
	
	
	/**
	 * Enable or disable a model
	 * @param i model index to enable/disable
	 * @param enable - true to enable, false to disable.
	 */
	public void setEnable(int i, boolean enable) {
		if (i>enableModel.length-1) return;
		enableModel[i].setSelected(enable);
	}

	public int getNModels() {
		if (models==null) return 0; 
		return  models.size();
	}
	
	
}

