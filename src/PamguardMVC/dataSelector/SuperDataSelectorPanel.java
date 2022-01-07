package PamguardMVC.dataSelector;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

import PamView.dialog.PamDialogPanel;

public class SuperDataSelectorPanel implements PamDialogPanel {

	private SuperDetDataSelector superDetDataSelector;
	
	private JCheckBox allowNoSuperDet;

	private PamDialogPanel dialogPanel;

	private JPanel mainPanel;

	public SuperDataSelectorPanel(SuperDetDataSelector superDetDataSelector, PamDialogPanel dialogPanel) {
		this.superDetDataSelector = superDetDataSelector;
		this.dialogPanel = dialogPanel;
		mainPanel = new JPanel(new BorderLayout());
		// steal any border
		Border border = dialogPanel.getDialogComponent().getBorder();
		if (border != null) {
			dialogPanel.getDialogComponent().setBorder(null);
			mainPanel.setBorder(border);
		}
		mainPanel.add(allowNoSuperDet = new JCheckBox("Allow no super detection"), BorderLayout.NORTH);
		mainPanel.add(dialogPanel.getDialogComponent(), BorderLayout.CENTER);
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		SuperDataSelectParams params = superDetDataSelector.getParams();
		allowNoSuperDet.setSelected(params.isUseUnassigned());
		dialogPanel.setParams();
	}

	@Override
	public boolean getParams() {
		SuperDataSelectParams params = superDetDataSelector.getParams();
		params.setUseUnassigned(allowNoSuperDet.isSelected());
		return dialogPanel.getParams();
	}

}
