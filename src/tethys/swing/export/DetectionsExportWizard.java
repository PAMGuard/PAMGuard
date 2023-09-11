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
import PamguardMVC.PamDataBlock;
import tethys.TethysControl;
import tethys.output.StreamExportParams;

public class DetectionsExportWizard extends PamDialog {

	private PamDataBlock dataBlock;
	private CardLayout cardLayout;
	private JPanel cardPanel;
	private GranularityCard granularityCard;
	private DescriptionCard descriptionCard;
	private JButton prevButton;
	private StreamExportParams streamExportParams;
	
	private ArrayList<ExportWizardCard> wizardCards = new ArrayList();
	private AlgorithmCard algorithmCard;
	private ExportWorkerCard exportWorkerCard;
	
	private DetectionsExportWizard(Window parentFrame,  TethysControl tethysControl, PamDataBlock dataBlock) {
		super(parentFrame, "Detections Export", false);
		this.dataBlock = dataBlock;
		
		streamExportParams = tethysControl.getTethysExportParams().getStreamParams(dataBlock);
		if (streamExportParams == null) {
			streamExportParams = new StreamExportParams(dataBlock.getLongDataName(), false);
		}
		
		cardLayout = new CardLayout();
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.NORTH, new ExportStreamInfoPanel(dataBlock));
		cardPanel = new JPanel(cardLayout);
		mainPanel.add(BorderLayout.CENTER, cardPanel);

		addCard(algorithmCard = new AlgorithmCard(this, tethysControl, dataBlock));
		addCard(granularityCard = new GranularityCard(this, tethysControl, dataBlock));
		addCard(descriptionCard = new DescriptionCard(this, tethysControl, dataBlock));
		addCard(exportWorkerCard = new ExportWorkerCard(this, tethysControl, dataBlock));
		
		cardLayout.first(cardPanel);
		
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
		
//		this.get
	}
	
	private void addCard(ExportWizardCard wizPanel) {
		cardPanel.add(wizPanel, wizPanel.getTitle());
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
		enableControls();
//		granularityCard.setParams(streamExportParams);
	}

	/**
	 * Called when 'previous' button is clicked. 
	 */
	protected void previousButton() {
		cardLayout.previous(cardPanel);
		enableControls();
	}
	
	public JButton getPreviousButton() {
		return prevButton;
	}

	@Override
	public boolean getParams() {
		int iCard = getCardIndex();
		if (iCard < wizardCards.size()-1) {
			if (checkCurrentCard()) {
				cardLayout.next(cardPanel);
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
		getOkButton().setEnabled(!isLast);
//		getOkButton().setText(isLast ? "Export" : "Next");
	}
	
	private boolean checkCurrentCard() {
		int iCard = getCardIndex();
		if (iCard < 0) {
			return true;
		}
		return wizardCards.get(iCard).getParams(streamExportParams);
	}
	
	private int getCardIndex() {
		for (int i = 0; i < cardPanel.getComponentCount(); i++) {
			Component component = cardPanel.getComponent(i);
			if (component.isVisible()) {
				return i;
			}
		}
		return -1;
	}
}
