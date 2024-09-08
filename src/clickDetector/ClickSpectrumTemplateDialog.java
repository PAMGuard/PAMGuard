package clickDetector;


import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;
import PamUtils.SelectFolder;
import PamView.dialog.PamDialog;

public class ClickSpectrumTemplateDialog  extends PamDialog {
	
	private static ClickSpectrumTemplateDialog singleInstance;
	
	private ClickSpectrumTemplateParams clickSpectrumTemplateParams;
	
	private SelectFolder templateFileLocation; 
	
	private ArrayList<String> previousFiles;
	
	ClickTemplate clickTemplate;
	
	JButton browseButton;
	
	JButton createButton;
	
	JComboBox templateList;
	
	Window parentFrame;
	
	File clickTemplateFile;
	

	public ClickSpectrumTemplateDialog(Window parentFrame, Point pt) {
		super(parentFrame, "Click Template Display", false);
		this.parentFrame=parentFrame;
		JPanel mainPanel = new JPanel();
		
		JPanel q = new JPanel();
		q.setBorder(new TitledBorder("Load Template"));
		q.setLayout(new BorderLayout());
		q.add(BorderLayout.NORTH, templateList = new JComboBox());
		templateList.setEditable(true);
		q.add(BorderLayout.EAST, browseButton = new JButton("Browse"));
			
		browseButton.addActionListener(new BrowseButtonAction());
		
		mainPanel.add(BorderLayout.CENTER,q);
		
		setDialogComponent(mainPanel);
		if (pt != null) {
			setLocation(pt);
		}
	}
	
	public static ClickSpectrumTemplateParams showDialog(Window parentFrame, Point pt,  ClickSpectrum clickSpectrum, ClickSpectrumTemplateParams clickSpectrumTemplateParams){
		if (singleInstance == null ) {
			singleInstance = new ClickSpectrumTemplateDialog(parentFrame, pt);
		}
		singleInstance.clickSpectrumTemplateParams = clickSpectrumTemplateParams.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.clickSpectrumTemplateParams;
	}
	
	public void setParams(){
		previousFiles=clickSpectrumTemplateParams.previousFiles;
	}
	
	class BrowseButtonAction implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			String template=openFileBrowser();
			
			if (template==null){
				return;
			}
			clickSpectrumTemplateParams.templateFile = new File(template);
//			System.out.println(clickTemplateFile);
			templateList.setSelectedItem(template);
		}
	}
	
	public void loadTemplate(){
		String template=(String) templateList.getSelectedItem();
		ClickTemplate clkTemplate=ClickTemplate.getCSVResults(template);
		if (clkTemplate!=null){
		clickSpectrumTemplateParams.clickTemplateArray.add(clkTemplate);
		clickSpectrumTemplateParams.clickTempVisible.add(true);
		}
	}
	

	
	public String openFileBrowser(){
		
		PamFileFilter fileFilter = new PamFileFilter("Click Template", ".csv");
		fileFilter.addFileType(".txt");
		JFileChooser fileChooser = new PamFileChooser();
		fileChooser.setFileFilter(fileFilter);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int state = fileChooser.showOpenDialog(parentFrame);
		if (state == JFileChooser.APPROVE_OPTION) {
			File currFile = fileChooser.getSelectedFile();
			//System.out.println(currFile);
			return currFile.getAbsolutePath();
		}
		return null;
	}
	

	@Override
	public boolean getParams() {
		loadTemplate();
	return true;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
	}
	

}
