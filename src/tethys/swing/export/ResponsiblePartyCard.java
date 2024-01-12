package tethys.swing.export;

import java.awt.BorderLayout;

import PamView.panel.PamNorthPanel;
import PamView.wizard.PamWizard;
import PamView.wizard.PamWizardCard;
import nilus.ResponsibleParty;

public class ResponsiblePartyCard extends PamWizardCard<ResponsibleParty> {

	private ResponsiblePartyPanel responsiblePartyPanel;

	public ResponsiblePartyCard(PamWizard pamWizard, String title) {
		super(pamWizard, title);
		responsiblePartyPanel = new ResponsiblePartyPanel("Responsible Party");
		this.setLayout(new BorderLayout());
		this.add(BorderLayout.CENTER, new PamNorthPanel(responsiblePartyPanel.getMainPanel()));
	}

	@Override
	public boolean getParams(ResponsibleParty cardParams) {
		return responsiblePartyPanel.getParams(cardParams);
	}

	@Override
	public void setParams(ResponsibleParty cardParams) {
		responsiblePartyPanel.setParams(cardParams);
	}

}
