package PamView;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.panel.PamPanel;

public class ImportLoadBar extends PamDialog {
	
	public static  ImportLoadBar singleInstance;
	
	private JProgressBar streamProgress;
	
	private JLabel textUpdate;
	
	public ImportLoadBar(Window parentFrame, String name) {
		super(parentFrame, name, false);
		
		PamPanel p = new PamPanel(new BorderLayout());
		p.setBorder(new TitledBorder(name));
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		p.add(BorderLayout.CENTER, streamProgress = new JProgressBar());
		p.add(BorderLayout.SOUTH, textUpdate = new JLabel(""));
		
		p.setPreferredSize(new Dimension(350,50));
		setDialogComponent(p);
		
		getOkButton().setVisible(false);
		getCancelButton().setVisible(true);
		getCancelButton().setText("Stop");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setModalityType(Dialog.ModalityType.MODELESS);

	}
	
	
	public static ImportLoadBar showDialog(Window parentFrame, String name) {
		
//		if (singleInstance == null || singleInstance.getOwner() == null ||
//				singleInstance.getOwner() != parentFrame) {
			singleInstance = new ImportLoadBar(parentFrame, name);
//		}
		
		singleInstance.setVisible(true);
		return singleInstance;
		
	}
	
	public void  setTextUpdate(String string){
		textUpdate.setText(string); 
	}
	
	@Override
	public JButton getCancelButton( ){
		return super.getCancelButton();
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (!visible) {
			closeLater();
		}
		else {
			super.setVisible(visible);
		}
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


	@Override
	public boolean getParams() {
		return false;
	}

	@Override
	public void cancelButtonPressed() {
		
	}

	@Override
	public void restoreDefaultSettings() {
		
	}

}