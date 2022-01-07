package PamView.importData;

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

/**
 * Generic Dialog for opening a file. 
 * @author Jamie Macaulay;
 */
@SuppressWarnings("serial")
public class ImportDataDialog extends PamDialog{
	
	private ImportDataDialog singleInstance;
	private FileImportParams params;
	private Window parentFrame;
	String[] extensions;
	
	public ImportDataDialog(Window parentFrame, String name, FileImportParams params, String[] extension) {
		super(parentFrame, name, false);
		this.params=params;
		this.parentFrame=parentFrame; 
		setDialogComponent(getSettingsPanel());
	}
	
	//TODO- need to clean up this function as was originally static but due to this essentially being an abstract dialog that doens't work very well. 
	public FileImportParams showDialog(Frame parentFrame, String name, FileImportParams params, String[] extensions) {
		if (singleInstance == null || parentFrame != singleInstance.getOwner()) {
			singleInstance = new ImportDataDialog(parentFrame,name,params,extensions);
		}
		if (singleInstance.params == null) {
			singleInstance.params = new FileImportParams();
		}
		else {
			singleInstance.params = params.clone();
		}
		singleInstance.extensions=extensions;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.params;
	}
	
	//components for panel
	private JButton browse;
	private JComboBox<String> comboBox;
	
	public PamPanel getSettingsPanel() {
		
		browse = new JButton("Browse...");
		browse.addActionListener(new Browse());
		comboBox=new JComboBox<String>();

		PamPanel settingsPanel=new PamPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx=0;
		PamDialog.addComponent(settingsPanel, comboBox, c);
		comboBox.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXX");
		comboBox.setEditable(true);
		c.gridx=4;
		c.gridwidth = 1;
		PamDialog.addComponent(settingsPanel, browse, c);
	
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
		if (params.lastFiles.size()>0 && params.lastFiles.get(0)!=null){
				File lastFile=new File(params.lastFiles.get(0));
				dir=lastFile.getParent();
		}
		else dir=null;
			
		String newFile=PamFileBrowser.fileBrowser(parentFrame,dir,PamFileBrowser.OPEN_FILE, extensions);
		
		addNewFileToList( newFile);

		
		return newFile; 
	}
	
	private void addNewFileToList(String newFile){
		//if the new file path is the same as the latest file path in the list do nothing. 
		if (params.lastFiles.size()!=0 && (newFile==params.lastFiles.get(0) || newFile==null)) return; 
		else{
			params.lastFiles.add(0,newFile); 
			//remove some strings so that the number of saved file locations doesn't end up being infinite (not that that is very likely to happen)
			if (params.lastFiles.size()>30)  params.lastFiles.remove(30);
		}
	}
	
	private void setPaths(){
		comboBox.removeAllItems();
		for (int i=0; i<params.lastFiles.size(); i++){
			comboBox.addItem(params.lastFiles.get(i));
		}
	}
	

	private void setParams() {
		setPaths();
	}

	@Override
	public boolean getParams() {
		//the user may have manually changed the file path. If this is the case then take that filepath as the one to try and load
		addNewFileToList(comboBox.getSelectedItem().toString());
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		params=null; 
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}
	

}