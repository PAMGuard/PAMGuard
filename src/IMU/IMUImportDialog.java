package IMU;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;

import PamView.dialog.PamDialog;
import PamView.dialog.PamFileBrowser;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;

public class IMUImportDialog extends PamDialog{
	
	private IMUParams imuParams; 
	private IMUControl imuControl;
	private IMUImportMananger importCSVMethod;
	
	static private IMUImportDialog singleInstance;
	
	public IMUImportDialog(Window parentFrame, IMUControl imuControl, IMUImportMananger importCSVMethod) {
		super(parentFrame, "Import IMU Data", false);
		this.imuControl=imuControl; 
		this.importCSVMethod=importCSVMethod; 
		setDialogComponent(getSettingsPanel());
	}
	
	public static IMUParams showDialog(Frame parentFrame, IMUControl imuControl, IMUImportMananger importCSVMethod) {
		if (singleInstance == null || parentFrame != singleInstance.getOwner()) {
			singleInstance = new IMUImportDialog(parentFrame, imuControl,importCSVMethod);
		}
		if (singleInstance.imuParams == null) {
			singleInstance.imuParams = new IMUParams();
		}
		else {
			singleInstance.imuParams = imuControl.getParams().clone();
		}
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.imuParams;
	}
	
	//components for panel
	private JButton importCSVB;
	private JComboBox<String> comboBox;
	
	public PamPanel getSettingsPanel() {
		
		importCSVB = new JButton("Browse...");
		importCSVB.addActionListener(new Browse());
		comboBox=new JComboBox<String>();

		PamPanel settingsPanel=new PamPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx=0;
		PamDialog.addComponent(settingsPanel, comboBox, c);
		comboBox.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXX");
		comboBox.setEditable(true);
		c.gridx=4;
		c.gridwidth = 1;
		PamDialog.addComponent(settingsPanel, importCSVB, c);
	
		return settingsPanel;
	}
	
	private class Browse implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			String path=openFileBrowser();
			if (path==null) return;
			//set the combo box to display the data
			setPaths();

		}
		
	}
	
	private String openFileBrowser(){
		
		String dir; 
		if (imuParams.lastCSVFiles.size()>0){
				File lastFile=new File(imuParams.lastCSVFiles.get(0));
				dir=lastFile.getParent();
		}
		else dir=null;
			
		String newFile=PamFileBrowser.csvFileBrowser(imuControl.getPamView().getGuiFrame(),dir,PamFileBrowser.OPEN_FILE);
		
		addNewFileToList( newFile);

		return newFile; 
	}
	
	private void addNewFileToList(String newFile){
		//if the new file path is the same as the latest file path in the list do nothing. 
		if (imuParams.lastCSVFiles.size()!=0 && (newFile==imuParams.lastCSVFiles.get(0) || newFile==null)) return; 
		else{
			imuParams.lastCSVFiles.add(0,newFile); 
			//remove some strings so that the number of saved file locations doesn't end up being infinite (not that that is very likely to happen)
			if (imuParams.lastCSVFiles.size()>30)  imuParams.lastCSVFiles.remove(30);
		}
	}
	
	private void setPaths(){
		comboBox.removeAllItems();
		for (int i=0; i<imuParams.lastCSVFiles.size(); i++){
			comboBox.addItem(imuParams.lastCSVFiles.get(i));
		}
	}
	

	private void setParams() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean getParams() {
		//the user may have manually changed the file path. If this is the case then take that filepath as the one to try and load
		if (comboBox.getSelectedItem()==null) return false; 
		addNewFileToList(comboBox.getSelectedItem().toString());
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		imuParams=null; 
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	

}
