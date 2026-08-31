package PamView.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;

import PamView.dialog.PamDialog;
import tethys.swing.export.ExportStreamInfoPanel;
import tethys.swing.export.ExportWizardCard;

abstract public class PamWizard extends PamDialog {

	private static final long serialVersionUID = 1L;

	private JPanel cardPanel;
		
	private CardLayout cardLayout;

	private JPanel mainPanel;

	private JButton prevButton;

	private ArrayList<PamWizardCard> wizardCards = new ArrayList();

	public PamWizard(Window parentFrame, String title) {
		super(parentFrame, title, false);

		cardLayout = new CardLayout();
		mainPanel = new JPanel(new BorderLayout());
		cardPanel = new JPanel(cardLayout);
		mainPanel.add(BorderLayout.CENTER, cardPanel);

		setDialogComponent(mainPanel);

		getOkButton().setText("Finish");
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

	public void addCard(PamWizardCard wizPanel) {
		cardPanel.add(wizPanel, wizPanel.getTitle());
		wizardCards.add(wizPanel);
	}
	
	/**
	 * Get the main panel. This is the main dialog panel and uses a borderlayout
	 * with the cards in the CENTER of the panel. Additional information panels
	 * (generally fixed and not changing with the dialog) can be added NORTH, SOUTH, WEST and EAST. 
	 * @return main Panel. 
	 */
	public JPanel getMainPanel() {
		return mainPanel;
	}

	/**
	 * Whether a card should be shown at all. Override to skip a page which is not
	 * relevant to what the user has chosen on an earlier one. All cards are shown
	 * unless a subclass says otherwise.
	 *
	 * @param wizardCard the card
	 * @return true if the card should be included in the sequence of pages.
	 */
	public boolean isCardEnabled(PamWizardCard wizardCard) {
		return true;
	}

	/**
	 * Find the next card to show in a given direction, skipping any that are not
	 * enabled.
	 *
	 * @param from  index to start looking from.
	 * @param step  +1 to look forwards, -1 to look backwards.
	 * @return index of the card to show, or -1 if there is not one.
	 */
	private int findEnabledCard(int from, int step) {
		for (int i = from; i >= 0 && i < wizardCards.size(); i += step) {
			if (isCardEnabled(wizardCards.get(i))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Show a particular card in the stack.
	 * @param iCard card index.
	 */
	private void showCard(int iCard) {
		cardLayout.show(cardPanel, wizardCards.get(iCard).getTitle());
	}

	/**
	 * Called when 'previous' button is clicked.
	 */
	protected void previousButton() {
		int iPrev = findEnabledCard(getCardIndex()-1, -1);
		if (iPrev >= 0) {
			showCard(iPrev);
		}
		enableControls();
	}

	public void enableControls() {
		int iCard = getCardIndex();
		boolean isFirst = findEnabledCard(iCard-1, -1) < 0;
		boolean isLast = findEnabledCard(iCard+1, +1) < 0;
		prevButton.setEnabled(!isFirst);
//		getOkButton().setEnabled(!isLast);
		getOkButton().setText(isLast ? "Finish" : "Next");
		getOkButton().setToolTipText(isLast ? "Check values and close" : "Next page");
		prevButton.setToolTipText(isFirst ? null : "Previous page");
	}
	

	private boolean checkCurrentCard() {
		int iCard = getCardIndex();
		if (iCard < 0) {
			return true;
		}
		return getCardParams(wizardCards.get(iCard));
	}
	
	private void selectNewCard() {
		int iCard = getCardIndex();
		if (iCard < 0) {
			return;
		}
		cardSelected(wizardCards.get(iCard));
	}
	
	/**
	 * Called when a card is selected in the stack. 
	 * @param pamWizardCard
	 */
	public void cardSelected(PamWizardCard pamWizardCard) {
	}

	abstract public void setCardParams(PamWizardCard wizardCard);
	
	abstract public boolean getCardParams(PamWizardCard wizardCard);
	
	public int getCardIndex() {
		for (int i = 0; i < cardPanel.getComponentCount(); i++) {
			Component component = cardPanel.getComponent(i);
			if (component.isVisible()) {
				return i;
			}
		}
		return -1;
	}
	
	public JButton getPreviousButton() {
		return prevButton;
	}

	public void setParams() {
		for (PamWizardCard wizCard : wizardCards) {
			setCardParams(wizCard);
		}
		enableControls();
	}
	
	@Override
	public boolean getParams() {
		/**
		 * This is the OK button, so we need to NOT return OK, which would close the 
		 * dialog until we're on the last card. 
		 */
		if (checkCurrentCard() == false) {
			return false;
		}
		int iNext = findEnabledCard(getCardIndex()+1, +1);
		if (iNext >= 0) {
			showCard(iNext);
			selectNewCard();
			enableControls();
			return false;
		}

		return true;
	}



	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Move to the first card in the stack
	 */
	public void moveFirst() {
		cardLayout.first(cardPanel);
	}

	/**
	 * Move to the last card in the stack
	 */
	public void moveLast() {
		cardLayout.last(cardPanel);
	}

}
