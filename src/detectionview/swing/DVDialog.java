package detectionview.swing;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamDetection.PamDetection;
import PamDetection.RawDataUnit;
import PamView.dialog.PamDialog;
import PamView.dialog.SourcePanel;
import PamView.panel.WestAlignedPanel;
import detectionview.DVControl;
import detectionview.DVParameters;

public class DVDialog extends PamDialog {
	
	private static final long serialVersionUID = 1L;

	private DVParameters dvParameters;
	
	private DVControl dvControl;
	
	private static DVDialog singleinstance;
	
	private JCheckBox useDefault;
	private SourcePanel detectorSource, rawSource;

	private DVDialog(DVControl dvControl) {
		super(dvControl.getGuiFrame(), dvControl.getUnitName() + " settings", false);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		detectorSource = new SourcePanel(this, "Detector", PamDetection.class, false, true);
		rawSource = new SourcePanel(this, RawDataUnit.class, false, true);
		useDefault = new JCheckBox("Use Default");
		JPanel sSub = new JPanel(new BorderLayout());
		sSub.add(BorderLayout.NORTH, new WestAlignedPanel(useDefault));
		sSub.add(BorderLayout.CENTER, rawSource.getPanel());
		sSub.setBorder(new TitledBorder("Raw data source"));
		
		mainPanel.add(detectorSource.getPanel());
		mainPanel.add(sSub);
		
		useDefault.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				useDefaultPressed();
			}
		});
		
		setDialogComponent(mainPanel);
	}
	
	protected void useDefaultPressed() {
		boolean useDef = useDefault.isSelected();
		if (useDef) {
			findDefaultSource();
		}
		enableControls();
	}

	private void enableControls() {
		rawSource.setEnabled(useDefault.isSelected() == false);
	}

	/**
	 * find the first raw data source upstream of the detector. 
	 * and set it as the selected input block. 
	 */
	private void findDefaultSource() {
		// TODO Auto-generated method stub
		
	}

	public static DVParameters showDialog(DVControl dvControl) {
		if (singleinstance == null || singleinstance.dvControl != dvControl) {
			singleinstance = new DVDialog(dvControl); 
		}
		singleinstance.setParams(dvControl.getDvParameters());
		singleinstance.setVisible(true);
		
		return singleinstance.dvParameters;
	}

	private void setParams(DVParameters dvParameters) {
		this.dvParameters = dvParameters;
		
		
		enableControls();
	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cancelButtonPressed() {
		dvParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

}
