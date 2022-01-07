package difar.display;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.border.TitledBorder;

import PamView.PamColors.PamColor;
import PamView.dialog.PamButton;
import PamView.dialog.PamCheckBox;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;
import difar.DIFARMessage;
import difar.DifarControl;

/**
 * A control panel which will most likely sit in the right hand 
 * edge of the DifarGram display, but keep separate in case 
 * we decide to put it somewhere else at a later date. 
 * @author Doug Gillepspie
 *
 */
public class DIFARUnitControlPanel implements DIFARDisplayUnit {

	private DifarControl difarControl;
		
	private PamPanel controlPanel;
	
	private PamButton saveButton, deleteButton, saveWithoutCrossButton;

	private PamCheckBox singleClick,autoSave;
	
	private PamCheckBox zoomFrequency;
	
	public DIFARUnitControlPanel(DifarControl difarControl) {
		super();
		this.difarControl = difarControl; 
		
		controlPanel = new PamPanel(PamColor.BORDER);
		controlPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		controlPanel.setBorder(new TitledBorder("Actions"));
		c.anchor = GridBagConstraints.SOUTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
	
		controlPanel.add(zoomFrequency = new PamCheckBox("Zoom freq'"),c);
		c.gridy++;
//		controlPanel.add(singleClick = new PamCheckBox("1 Click"),c);
//		c.gridy++;
		controlPanel.add(deleteButton = new PamButton("Delete"),c);
		c.gridy++;
//		controlPanel.add(autoSave = new PamCheckBox("Auto Save"),c);
//		c.gridy++;
		controlPanel.add(saveWithoutCrossButton = new PamButton("Save WITHOUT cross"),c);
		c.gridy++;
		controlPanel.add(saveButton = new PamButton("Save"),c);
		c.gridy++;

		
		zoomFrequency.setToolTipText("Zoom to selected  / chosen frequency limits");
//		singleClick.setToolTipText("Immediate save when DIFARGram is clicked using click angle location");
		deleteButton.setToolTipText("Delete/abandon this localisation");
		saveWithoutCrossButton.setToolTipText("Save the bearing only WITHOUT triangulation");
		
		//saveButton.setToolTipText("<html>Save the current max angle<p>Click on the image for a different choice</html>");


		zoomFrequency.addActionListener(new ZoomFrequency());
//		singleClick.addActionListener(new SingleClick());
//		autoSave.addActionListener(new ToggleAutoSave());
		deleteButton.addActionListener(new DeleteButton());
		saveButton.addActionListener(new SaveButton());
		saveWithoutCrossButton.addActionListener(new SaveWithoutCrossButton());
		if (difarControl.isViewer()) {
			String toolTip = "These controls are disabled in viewer operation";
			saveButton.setToolTipText(toolTip);
			deleteButton.setToolTipText(toolTip);
//			singleClick.setToolTipText(toolTip);
		}
		enableControls();
	}

	public void enableControls() {
		saveButton.setEnabled(difarControl.isSaveEnabled());
		saveWithoutCrossButton.setEnabled(difarControl.isSaveWithoutCrossEnabled());
		deleteButton.setEnabled(difarControl.isDeleteEnabled());	
		updateButtonLabels();
		
//		singleClick.setSelected(isViewer == false && difarControl.getDifarParameters().isSingleClickSave());
//		singleClick.setEnabled(!isViewer);
//		autoSave.setSelected(isViewer == false && difarControl.getDifarParameters().autoSaveDResult);
//		autoSave.setEnabled(!isViewer);
		zoomFrequency.setSelected(difarControl.getDifarParameters().zoomDifarFrequency);
	}
	public void updateButtonLabels(){
		saveButton.setText("Save" + appendParens(difarControl.getDifarParameters().saveKey));
		saveWithoutCrossButton.setText("Save W/O Cross"+ appendParens(difarControl.getDifarParameters().saveWithoutCrossKey));
		deleteButton.setText("Delete" + appendParens(difarControl.getDifarParameters().deleteKey));
		
	}
	private String appendParens(String s){
		if (s == null || s.isEmpty()) return "";
		return " (" + s + ")";
	}

	private class SaveButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			saveButton();
		}
	}
	private class SaveWithoutCrossButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			saveWithoutCrossButton();
		}
	}
	private class DeleteButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			deleteButton();
		}
	}
	private class SingleClick implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			singleClickButton();
		}
	}
	
	private class ToggleAutoSave implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			toggleAutoSave();
		}
	}
	private class ZoomFrequency implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			difarControl.getDifarParameters().zoomDifarFrequency = zoomFrequency.isSelected();
			difarControl.getDifarGram().zoomFrequency();
		}
	}
		

	private void toggleAutoSave(){
		difarControl.getDifarParameters().autoSaveDResult=autoSave.isSelected();//!difarControl.getDifarParameters().autoSaveDResult;
		//potentially set state here but should always correspond?? same for single click
	}
	
	private void singleClickButton() {
		difarControl.getDifarParameters().setSingleClickSave(singleClick.isSelected());
	}
	
	public void saveWithoutCrossButton() {
		difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.SaveDatagramUnitWithoutRange, difarControl.getCurrentDemuxedUnit()));
		enableControls();
	}
	
	public void saveButton() {
		difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.SaveDatagramUnit, difarControl.getCurrentDemuxedUnit()));
		enableControls();
	}
	
	public void deleteButton() {
		difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.DeleteDatagramUnit, difarControl.getCurrentDemuxedUnit()));
		enableControls();
	}
	@Override
	public String getName() {
		return "Unit Control";
	}

	@Override
	public Component getComponent() {
		return controlPanel;
	}

	@Override
	public int difarNotification(DIFARMessage difarMessage) {
		switch(difarMessage.message) {
		case DIFARMessage.DemuxComplete:
			break;
		}
		enableControls();
		return 0;
	}

}
