package targetMotionModule.panels;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;
import targetMotionModule.TargetMotionControl;
import targetMotionModule.TargetMotionLocaliser;
import targetMotionModule.algorithms.TargetMotionModel;


public class ModelControlPanel implements TMDialogComponent {

	private TargetMotionMainPanel targetMotionDialog;
	
	private TargetMotionLocaliser targetMotionLocaliser;
	
	private PamPanel mainPanel;
	
	private JCheckBox[] enableModel;
	
	private JButton[] modelParams;

	/**
	 * @param targetMotionLocaliser
	 * @param targetMotionDialog
	 */
	public ModelControlPanel(TargetMotionLocaliser targetMotionLocaliser,
			TargetMotionMainPanel targetMotionDialog) {
		
		super();
		this.targetMotionLocaliser = targetMotionLocaliser;
		this.targetMotionDialog = targetMotionDialog;
		
		mainPanel = new PamPanel();
		mainPanel.setBorder(new TitledBorder("Model Control"));
		
		mainPanel.setLayout(new BorderLayout());
		
		PamPanel modelPanel = new PamPanel();
		modelPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		ArrayList<TargetMotionModel> models = targetMotionLocaliser.getModels();
		int nModels = models.size();
		enableModel = new JCheckBox[nModels];
		modelParams = new JButton[nModels];
		TargetMotionModel model;
		for (int i = 0; i < nModels; i++) {
			model = models.get(i);
			enableModel[i] = new JCheckBox(model.getName());
			enableModel[i].addActionListener(new ModelEnable(model));
			modelParams[i] = new JButton(targetMotionDialog.settings);
			modelParams[i].setVisible(model.hasParameters());
			modelParams[i].addActionListener(new ModelParams(model));
			enableModel[i].setToolTipText(model.getToolTipText());
			modelParams[i].setToolTipText(model.getToolTipText());
			c.gridx = 0;
			PamDialog.addComponent(modelPanel, enableModel[i], c);
			c.gridx++;
			PamDialog.addComponent(modelPanel, modelParams[i], c);
			c.gridy++;
		}
		
		mainPanel.add(BorderLayout.NORTH, modelPanel);
		
	}
	
	

	class ModelEnable implements ActionListener {
		TargetMotionModel model;

		public ModelEnable(TargetMotionModel model) {
			super();
			this.model = model;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
		targetMotionDialog.update(TargetMotionControl.ALGORITHM_SELECTION_CHANGED);
			for (int i=0; i<enableModel.length; i++){
					modelParams[i].setEnabled(enableModel[i].isSelected());
			}
		}
		
	}
	
	
	
	class ModelParams implements ActionListener {
		TargetMotionModel model;

		public ModelParams(TargetMotionModel model) {
			super();
			this.model = model;
		}
		@Override
		//settings panel
		public void actionPerformed(ActionEvent arg0) {
			model.parametersDialog();
		
		}
	}
	
//	@Override
//	public void setCurrentEventIndex(int eventIndex, Object sender) {
//		if (sender == this) return;
//	}
	/**
	 * @return the mainPanel
	 */
	public JPanel getPanel() {
		return mainPanel;
	}
	
	@Override
	public boolean canRun() {
		for (int i = 0; i < enableModel.length; i++) {
			if (enableModel[i].isSelected()) {
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public void enableControls() {

		ArrayList<TargetMotionModel> models = targetMotionLocaliser.getModels();
		int nModels = models.size();
		TargetMotionModel model;
		for (int i = 0; i < nModels; i++) {
			model = models.get(i);
			modelParams[i].setEnabled(enableModel[i].isSelected() && model.hasParameters());
		}
		
	}
	/**
	 * 
	 * @param i model index
	 * @return true if a particular model is enabled
	 */
	public boolean isEnabled(int i) {
		return enableModel[i].isSelected();
	}
	@Override
	public void update(int flag) {
		// TODO Auto-generated method stub
		
	}
	
	
}
