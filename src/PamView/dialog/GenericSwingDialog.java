package PamView.dialog;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
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
		return showDialog(parentFrame, title, null, dialogPanels);
	}

	/**
	 * Show dialog at a specific location on the screen. 
	 * @param parentFrame
	 * @param title
	 * @param screenPoint
	 * @param dialogPanels
	 * @return
	 */
	public static boolean showDialog(Window parentFrame, String title, Point screenPoint, PamDialogPanel ...dialogPanels) {
		GenericSwingDialog swingDialog = new GenericSwingDialog(parentFrame, title, dialogPanels);
		swingDialog.setParams();
		swingDialog.pack();
		if (screenPoint != null) {
			try {
				// check we're not going too far off the screen. 
				Dimension sz = swingDialog.getPreferredSize();
				Dimension screen = null;
				if (parentFrame != null) {
					screen = parentFrame.getSize();
				}
				else {
					screen = Toolkit.getDefaultToolkit().getScreenSize();
				}
				screenPoint.y = Math.min(screenPoint.y, screen.height-sz.height-10);
				screenPoint.y = Math.max(screenPoint.y, 0);
				screenPoint.x = Math.min(screenPoint.x, screen.width-sz.width-10);
				screenPoint.x = Math.max(screenPoint.x, 0);

				swingDialog.setLocation(screenPoint);
			}
			catch (Exception e) {
				// shouldn't happen, but if it does, it doesn't matter much
			}
		}
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
