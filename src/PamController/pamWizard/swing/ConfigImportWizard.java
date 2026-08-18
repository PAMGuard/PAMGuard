package PamController.pamWizard.swing;

import java.awt.Window;
import java.util.List;

import javax.swing.JOptionPane;

import PamController.pamWizard.configurations.ConfigWizardData;
import PamView.wizard.PamWizard;
import PamView.wizard.PamWizardCard;

/**
 * The Swing import wizard: takes the user from a set of dropped files to a
 * working PAMGuard configuration.
 * <p>
 * Four pages - what was imported, which configuration to use, where to put the
 * data, and a summary of what will be built. The storage page is skipped for
 * configurations which have nothing to store.
 * <p>
 * Nothing is changed until Finish is pressed, so the user can back out at any
 * point.
 *
 * @author Jamie Macaulay
 */
public class ConfigImportWizard extends PamWizard {

	private static final long serialVersionUID = 1L;

	private final ConfigWizardData wizardData;

	private final ScanSummaryCard scanCard;
	private final ConfigSelectionCard selectionCard;
	private final StoragePathsCard storageCard;
	private final ConfigSummaryCard summaryCard;

	private boolean finished = false;

	private ConfigImportWizard(Window parentFrame, ConfigWizardData wizardData) {
		super(parentFrame, "Set up PAMGuard from imported files");
		this.wizardData = wizardData;

		scanCard = new ScanSummaryCard(this);
		selectionCard = new ConfigSelectionCard(this);
		storageCard = new StoragePathsCard(this);
		summaryCard = new ConfigSummaryCard(this);

		addCard(scanCard);
		addCard(selectionCard);
		addCard(storageCard);
		addCard(summaryCard);

		setParams();
		moveFirst();
		enableControls();
		pack();
	}

	/**
	 * Show the wizard and, if the user completes it, build the configuration they
	 * chose.
	 *
	 * @param parentFrame the parent window.
	 * @param wizardData  what was imported and what can be built from it.
	 * @return true if a configuration was built.
	 */
	public static boolean showWizard(Window parentFrame, ConfigWizardData wizardData) {
		if (wizardData == null || wizardData.getAvailableConfigs().isEmpty()) {
			return false;
		}
		ConfigImportWizard wizard = new ConfigImportWizard(parentFrame, wizardData);
		wizard.setVisible(true);

		if (!wizard.finished) {
			return false;
		}

		boolean built = wizardData.applySelected();
		if (built) {
			showWarnings(parentFrame, wizardData);
		}
		return built;
	}

	/**
	 * Tell the user about anything that did not go entirely to plan - a module whose
	 * input could not be reconnected, a folder that could not be created. Saying so
	 * is much better than letting it fail once processing starts.
	 */
	private static void showWarnings(Window parentFrame, ConfigWizardData wizardData) {
		List<String> warnings = wizardData.getApplyContext().getWarnings();
		if (warnings.isEmpty()) {
			return;
		}
		StringBuilder text = new StringBuilder("<html>The configuration was created, but please check "
				+ "the following before you start processing:<br><br>");
		for (String warning : warnings) {
			text.append("&bull; ").append(warning).append("<br>");
		}
		text.append("</html>");
		JOptionPane.showMessageDialog(parentFrame, text.toString(),
				"Configuration created with warnings", JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * The storage page is only of use to a configuration which has a binary store or
	 * a database.
	 */
	@Override
	public boolean isCardEnabled(PamWizardCard wizardCard) {
		if (wizardCard == storageCard) {
			return wizardData.needsStoragePaths();
		}
		return true;
	}

	/**
	 * Refresh a page as it is shown, since what it needs to display depends on
	 * choices made on the pages before it.
	 */
	@Override
	public void cardSelected(PamWizardCard pamWizardCard) {
		setCardParams(pamWizardCard);
	}

	@Override
	protected void previousButton() {
		super.previousButton();
		// cardSelected is only called when moving forwards, so refresh here too.
		int iCard = getCardIndex();
		if (iCard >= 0) {
			setCardParams(cardAt(iCard));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCardParams(PamWizardCard wizardCard) {
		if (wizardCard != null) {
			wizardCard.setParams(wizardData);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean getCardParams(PamWizardCard wizardCard) {
		return wizardCard == null || wizardCard.getParams(wizardData);
	}

	@Override
	public boolean getParams() {
		boolean done = super.getParams();
		if (done) {
			finished = true;
		}
		return done;
	}

	@Override
	public void cancelButtonPressed() {
		finished = false;
	}

	/**
	 * The card at a given index in the stack.
	 */
	private PamWizardCard cardAt(int index) {
		switch (index) {
		case 0:
			return scanCard;
		case 1:
			return selectionCard;
		case 2:
			return storageCard;
		case 3:
			return summaryCard;
		default:
			return null;
		}
	}
}
