package GPS;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.border.TitledBorder;

import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;

/**
 * Simple dialog which returns a path to get GPS data from;
 * @author spn1
 *
 */
public class ImportGPSDialog extends PamDialog{
	
	private	JComboBox<String> fileList; 
	private	JButton browseButton; 
	private	static ImportGPSDialog singleInstance; 
	private ImportGPSParams params;
	private Window frame;
	private ImportGPSData importGPSData; 
	private static int maxlistSize=40;

	
	public ImportGPSDialog(Window parentFrame, Point location){
		
		super(parentFrame, "Import GPS Data", false);

		PamGridBagContraints c=new PamGridBagContraints();

		PamPanel p= new PamPanel(new GridBagLayout());
		p.setBorder(new TitledBorder("Load Data"));
		c.gridx = 0;
		c.gridwidth = 3;
		addComponent(p, fileList = new JComboBox<String>(),c);
		fileList.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXX");
		fileList.setEditable(true);
		c.gridwidth = 1;
		c.gridx++;
		c.gridx++;
		c.gridx++;
		addComponent(p, browseButton = new JButton("Browse"),c);
		browseButton.addActionListener(new BrowseButtonAction());
		
//		c.gridy++;
//		c.gridx=0; 
//		checkBox=new JCheckBox("Delete previous data");
		
		PamPanel importGPS=new PamPanel(new BorderLayout());
		importGPS.add(BorderLayout.CENTER, p);
		setDialogComponent(importGPS);
		
		setLocation(location);
		
		
		
	}
	
	
	public static ImportGPSParams showDialog(Window frame, Point pt, ImportGPSParams params, ImportGPSData importGPSData ){
		if (singleInstance == null || singleInstance.getOwner() != frame) {
			singleInstance = new ImportGPSDialog(frame, pt);
		}
		singleInstance.params = params.clone();
		singleInstance.frame=frame;
		singleInstance.importGPSData=importGPSData;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.params;
	}

	
	public String openFileBrowser(Window parentFrame, String dir){
		
		try{
			PamFileFilter fileFilter = importGPSData.getGPSFileFilter();
			JFileChooser fileChooser = new PamFileChooser();
			fileChooser.setFileFilter(fileFilter);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			
			if (dir!=null){
				File directory=new File(dir);
				if (directory.isDirectory()) fileChooser.setCurrentDirectory(directory);
			}
			
			int state = fileChooser.showOpenDialog(parentFrame);
			if (state == JFileChooser.APPROVE_OPTION) {
				File currFile = fileChooser.getSelectedFile();
				//System.out.println(currFile);
				return currFile.getAbsolutePath();
			}
			return null;
		}
		catch (Exception e){
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	class BrowseButtonAction implements ActionListener {
		

		@Override
		public void actionPerformed(ActionEvent e) {
			
			File lastFile;
			String dir;
			if (params.path.size()>0){
				if (params.path!=null){
					lastFile=new File(params.path.get(0));
					dir=lastFile.getParent();
				}
				else dir=null;
			}
			else dir=null;
			
			String path=openFileBrowser(frame,dir);
			
			if (path==null){
				return;
			}
			
			params.path.add(0, path);
			
			if (params.path.size()>maxlistSize){
				params.path.remove(maxlistSize);
			}
			
			setPaths();
		}
	}
	
	private void setPaths(){
		fileList.removeAllItems();
		for (int i=0; i<params.path.size(); i++){
			fileList.addItem(params.path.get(i));
		}
	}
	
	public void setParams(){
		setPaths();
	}

	@Override
	public boolean getParams() {
		params.path.add(0,fileList.getSelectedItem().toString());
		return true;
	}

	@Override
	public void cancelButtonPressed() {		
	}

	@Override
	public void restoreDefaultSettings() {
		
	}
	
	

}
