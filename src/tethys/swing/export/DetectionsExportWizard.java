package tethys.swing.export;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.wizard.PamWizard;
import PamView.wizard.PamWizardCard;
import PamguardMVC.PamDataBlock;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.pamdata.TethysDataProvider;

public class DetectionsExportWizard extends PamWizard {

	private PamDataBlock dataBlock;
	private CardLayout cardLayout;
	private GranularityCard granularityCard;
	private DescriptionCard descriptionCard;
	private StreamExportParams streamExportParams;
	
	private AlgorithmCard algorithmCard;
	private ExportWorkerCard exportWorkerCard;
	private TethysDataProvider tethysDataProvider;
	
	private DetectionsExportWizard(Window parentFrame,  TethysControl tethysControl, PamDataBlock dataBlock) {
		super(parentFrame, "Detections Export");
		this.dataBlock = dataBlock;
		
		streamExportParams = tethysControl.getTethysExportParams().getStreamParams(dataBlock);
		if (streamExportParams == null) {
			streamExportParams = new StreamExportParams(tethysControl, dataBlock, false);
		}
		tethysDataProvider = dataBlock.getTethysDataProvider(tethysControl);
		getMainPanel().add(BorderLayout.NORTH, new ExportStreamInfoPanel(dataBlock));
		
		addCard(algorithmCard = new AlgorithmCard(this, tethysControl, dataBlock));
		addCard(granularityCard = new GranularityCard(this, tethysControl, dataBlock));
		addCard(descriptionCard = new DescriptionCard(this, tethysControl));
		addCard(exportWorkerCard = new ExportWorkerCard(this, tethysControl, dataBlock));
		
		moveFirst();
		
		
		setResizable(true);
		
//		this.get
	}
	
	private void addCard(ExportWizardCard wizPanel) {
		if (tethysDataProvider.wantExportDialogCard(wizPanel)) {
			super.addCard(wizPanel);
		}
	}

	public static void showDialog(Window parentFrame, TethysControl tethysControl, PamDataBlock dataBlock) {
		DetectionsExportWizard wiz = new DetectionsExportWizard(parentFrame, tethysControl, dataBlock);
		wiz.setParams();
		wiz.setVisible(true);
	}
	

	@Override
	public void setCardParams(PamWizardCard wizardCard) {
		wizardCard.setParams(streamExportParams);
	}

	@Override
	public boolean getCardParams(PamWizardCard wizardCard) {
		return wizardCard.getParams(streamExportParams);
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
