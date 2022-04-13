package PamView.dialog;

import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class GenericSwingDialog extends PamDialog {
	
	private boolean allOk;
	
	private PamDialogPanel[] dialogPanels;

	private GenericSwingDialog(Window parentFrame, String title, PamDialogPanel ...dialogPanels) {
		super(parentFrame, title, false);
		this.dialogPanels = dialogPanels;
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		for (int i = 0; i < dialogPanels.length; i++) {
			JComponent comp = dialogPanels[i].getDialogComponent();
			if (comp.getBorder() == null) {
				comp.setBorder(new EmptyBorder(5, 5, 5, 5));
			}
			mainPanel.add(comp);
		}
		
		setDialogComponent(mainPanel);
	}
	
	public static boolean showDialog(Window parentFrame, String title, PamDialogPanel ...dialogPanels) {

		GenericSwingDialog swingDialog = new GenericSwingDialog(parentFrame, title, dialogPanels);
		swingDialog.setParams();
		swingDialog.pack();
		swingDialog.setVisible(true);
		return swingDialog.allOk;
	}

	private void setParams() {
		for (int i = 0; i < dialogPanels.length; i++) {
			dialogPanels[i].setParams();
		}		
	}

	@Override
	public boolean getParams() {
		allOk = true;
		for (int i = 0; i < dialogPanels.length; i++) {
			allOk &= dialogPanels[i].getParams();
		}
		return allOk;
	}

	@Override
	public void cancelButtonPressed() {
		allOk = false;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
