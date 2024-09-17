package PamView;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

import PamView.panel.PamPanel;

public class PamLoadBar  {

	private JProgressBar streamProgress;
	private JLabel textUpdate;
	private PamPanel panel;

	public PamLoadBar (String name){
		
		panel = new PamPanel(new BorderLayout());
		panel.setBorder(new TitledBorder(name));
		panel.add(BorderLayout.CENTER, streamProgress = new JProgressBar());
		panel.add(BorderLayout.SOUTH, textUpdate = new JLabel(""));
		
	}
	
	public PamLoadBar (){
		
		panel = new PamPanel(new BorderLayout());
		panel.add(BorderLayout.CENTER, streamProgress = new JProgressBar());
		panel.add(BorderLayout.SOUTH, textUpdate = new JLabel(""));
		
	}
	
	public 	PamPanel getPanel(){
		return panel; 
	}
	
	
	public void setPanelSize(int width, int height){
		panel.setPreferredSize(new Dimension(width, height)); 
	}
	
	public void  setTextUpdate(String string){
		textUpdate.setText(string); 
	}
	
	public void setProgress(int progress) {
		streamProgress.setValue(progress);
	}
	
	public void setIntermediate(boolean intermediate) {
		streamProgress.setIndeterminate(intermediate);
	}
	
	public boolean isIntermediate() {
		return streamProgress.isIndeterminate();
	}
	
	
	public void setMax(int max) {
		streamProgress.setMaximum(max);
	}



}