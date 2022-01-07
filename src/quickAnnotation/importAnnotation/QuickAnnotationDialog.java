package quickAnnotation.importAnnotation;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;

import Array.ArrayManager;
import Array.HydrophoneLocators;
import Array.streamerOrigin.HydrophoneOriginMethods;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;


public class QuickAnnotationDialog extends PamDialog {

	private static final long serialVersionUID = 1L;

	//combo boxes
	private JComboBox<String> streamerOriginMethods;
	private JComboBox<String> hydrophoneLocatorMethods;
	private JComboBox<Integer> streamerID;

	public QuickAnnotationDialog(Window parentFrame, QuickAnnotationImportParams params) {
		super(parentFrame, "QuickAnnotation Import Settings", false);
		this.params=params;
		setDialogComponent(getSettingsPanel());
	}

	private PamPanel getSettingsPanel() {
		
		streamerID=new JComboBox<Integer>();
		
		//create the combo boxes to select streamer methods. 
		streamerOriginMethods=new JComboBox<String>();
		for (int i=0; i<HydrophoneOriginMethods.getInstance().getCount(); i++){
			streamerOriginMethods.addItem(HydrophoneOriginMethods.getInstance().getMethod(i).getName());
		}
		
		hydrophoneLocatorMethods=new JComboBox<String>();
		for (int i=0; i<HydrophoneLocators.getInstance().getCount(); i++){
			hydrophoneLocatorMethods.addItem(HydrophoneLocators.getInstance().getSystem(i).getName());
		}
		

		PamPanel settingsPanel=new PamPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		
		PamDialog.addComponent(settingsPanel, new JLabel("Streamer ID"), c);
		c.gridy++;
		PamDialog.addComponent(settingsPanel, streamerID, c);
		c.gridy++;
		c.gridx=0;
		PamDialog.addComponent(settingsPanel, new JLabel("Reference Position"), c);
		c.gridy++;
		PamDialog.addComponent(settingsPanel, streamerOriginMethods, c);
		streamerOriginMethods.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXX");
		streamerOriginMethods.setEditable(false);
		c.insets=new Insets(10, 0, 0 ,0 );
		c.gridy++;
		PamDialog.addComponent(settingsPanel, new JLabel("Hydrophone Locator Method"), c);
		c.insets=new Insets(0, 0, 0 ,0 );
		c.gridy++;
		c.gridwidth = 1;
		PamDialog.addComponent(settingsPanel, hydrophoneLocatorMethods, c);
		hydrophoneLocatorMethods.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXX");
		
		settingsPanel.setBorder(new TitledBorder("Streamer Locations"));

		return settingsPanel;
		
	}

	private static QuickAnnotationDialog singleInstance;
	
	private QuickAnnotationImportParams params;
	

	public static QuickAnnotationImportParams showDialog(Frame parentFrame, QuickAnnotationImportParams params) {
		if (singleInstance == null || parentFrame != singleInstance.getOwner()) {
			singleInstance = new QuickAnnotationDialog(parentFrame,params);
		}
		if (singleInstance.params == null) {
			singleInstance.params = new QuickAnnotationImportParams();
		}
		else {
			singleInstance.params = params.clone();
		}
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.params;
	}


	private void setParams() {
		int streamerCount=ArrayManager.getArrayManager().getCurrentArray().getStreamerCount();
		streamerID.removeAllItems();
		for (Integer i=0; i<streamerCount; i++){
			streamerID.addItem(i);
		}
	}
	

	@Override
	public boolean getParams() {
//		
//		params.hydropheonLocator=HydrophoneLocators.getInstance().getSystem(hydrophoneLocatorMethods.getSelectedIndex());
//		params.hydropheonOrigin=HydrophoneOriginMethods.getInstance().getMethod(streamerOriginMethods.getSelectedIndex());
//		params.streamerIndex=(int) streamerID.getSelectedItem();

		return true;
	}

	
	@Override
	public void cancelButtonPressed() {
		params=null;
		singleInstance.setVisible(false);
	}
	

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

}
