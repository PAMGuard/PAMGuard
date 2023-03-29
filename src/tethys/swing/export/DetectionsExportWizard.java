package tethys.swing.export;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamguardMVC.PamDataBlock;
import tethys.TethysControl;
import tethys.output.StreamExportParams;

public class DetectionsExportWizard extends PamDialog {

	private PamDataBlock dataBlock;
	private CardLayout cardLayout;
	private JPanel mainPanel;
	private GranularityCard granularityCard;
	private DescriptionCard descriptionCard;
	private JButton prevButton;
	private StreamExportParams streamExportParams;
	
	private ArrayList<ExportWizardCard> wizardCards = new ArrayList();
	
	private DetectionsExportWizard(Window parentFrame,  TethysControl tethysControl, PamDataBlock dataBlock) {
		super(parentFrame, "Detections Export", false);
		this.dataBlock = dataBlock;
		cardLayout = new CardLayout();
		mainPanel = new JPanel(cardLayout);
		
		addCard(granularityCard = new GranularityCard(tethysControl, dataBlock));
		addCard(descriptionCard = new DescriptionCard(tethysControl, dataBlock));
	
		streamExportParams = tethysControl.getTethysExportParams().getStreamParams(dataBlock);
		
		cardLayout.first(mainPanel);
		
		setDialogComponent(mainPanel);
		
		getOkButton().setText("Next");
		prevButton = new JButton("Previous");
		getButtonPanel().add(prevButton, 0);
		prevButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				previousButton();
			}
		});
		
		setResizable(true);
	}
	
	private void addCard(ExportWizardCard wizPanel) {
		mainPanel.add(wizPanel, wizPanel.getTitle());
		wizardCards.add(wizPanel);
	}

	public static void showDilaog(Window parentFrame, TethysControl tethysControl, PamDataBlock dataBlock) {
		DetectionsExportWizard wiz = new DetectionsExportWizard(parentFrame, tethysControl, dataBlock);
		wiz.setParams();
		wiz.setVisible(true);
	}
	
	private void setParams() {
		for (ExportWizardCard wizCard : wizardCards) {
			wizCard.setParams(streamExportParams);
		}
//		granularityCard.setParams(streamExportParams);
	}

	/**
	 * Called when 'previous' button is clicked. 
	 */
	protected void previousButton() {
		cardLayout.previous(mainPanel);
		enableControls();
	}

	@Override
	public boolean getParams() {
		int iCard = getCardIndex();
		if (iCard < wizardCards.size()-1) {
			if (checkCurrentCard()) {
				cardLayout.next(mainPanel);
				enableControls();
			}
			return false;
		}
		
//		if (cardLayout.)
//		cardLayout.next(mainPanel);
//		System.out.println(mainPanel.getComponent(0).isShowing());
		/*
		 *  there seems to be no obvious way of knowing which card is showing except 
		 *  to go through and see which one has isShowing() == true, then test for first and 
		 *  last, etc. 
		 */
		enableControls();
		return false;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	private void enableControls() {
		int iCard = getCardIndex();
		prevButton.setEnabled(iCard > 0);
		boolean isLast = iCard == wizardCards.size()-1;
		getOkButton().setText(isLast ? "Export" : "Next");
	}
	
	private boolean checkCurrentCard() {
		int iCard = getCardIndex();
		if (iCard < 0) {
			return true;
		}
		return wizardCards.get(iCard).getParams(streamExportParams);
	}
	
	private int getCardIndex() {
		for (int i = 0; i < mainPanel.getComponentCount(); i++) {
			Component component = mainPanel.getComponent(i);
			if (component.isVisible()) {
				return i;
			}
		}
		return -1;
	}
}
