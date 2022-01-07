package Array;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamView.dialog.DialogComponent;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

/**
 * Dialog component used by both the streamer and the hydrophone dialogs
 * @author Doug Gillespie
 *
 */
public class InterpolationDialogPanel implements DialogComponent {
	
	private PamDialog pamDialog;
	private JPanel mainPanel;
	private JRadioButton useLatest, usePrevious, useInterpolate;
	private int allowedValues = 0xFF; // bitmap of banned values !

	public InterpolationDialogPanel(PamDialog pamDialog) {
		this.pamDialog = pamDialog;
		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Interpolation"));
		GridBagConstraints c = new PamGridBagContraints();
		PamDialog.addComponent(mainPanel, useLatest = new JRadioButton("Use only the latest value"), c);
		c.gridy++;
		PamDialog.addComponent(mainPanel, useInterpolate = new JRadioButton("Interpolate between values"), c);
		c.gridy++;
		PamDialog.addComponent(mainPanel, usePrevious = new JRadioButton("Use the location for the time preceeding each data unit"), c);
		useLatest.setToolTipText("<html>"
				+ "Select this option if you have a simple static array in a single location for the entire data set</html>");
		useInterpolate.setToolTipText("<html>"
				+ "Select this option if you are storing multiple locations for slowely moving (i.e. not quite fixed) devices</html>");
		usePrevious.setToolTipText("<html>"
				+ "Select this option if you have devices which are periodically moved from one spot to another</html>");
		ButtonGroup bg = new ButtonGroup();
		bg.add(useLatest);
		bg.add(useInterpolate);
		bg.add(usePrevious);
	}
	
	@Override
	public JComponent getComponent(Window owner) {
		return mainPanel;
	}

	public void setSelection(int option) {
		useLatest.setSelected(option == PamArray.ORIGIN_USE_LATEST);
		useInterpolate.setSelected(option == PamArray.ORIGIN_INTERPOLATE);
		usePrevious.setSelected(option == PamArray.ORIGIN_USE_PRECEEDING);
	}
	
	public int getSelection() {
		int sel = getSelectedButton();
		if (((1<<sel) & allowedValues) == 0) {
			pamDialog.showWarning("The selected interpolation is not available with the selected reference position");
			return -1;
		}
		else {
			return sel;
		}
	}
	
	
	/**
	 * @return the allowedValues
	 */
	protected int getAllowedValues() {
		return allowedValues;
	}

	/**
	 * @param allowedValues the allowedValues to set
	 */
	protected void setAllowedValues(int allowedValues) {
		this.allowedValues = allowedValues;
		enableControls();
	}

	private void enableControls() {
		useLatest.setEnabled((allowedValues & (1<<PamArray.ORIGIN_USE_LATEST)) != 0);
		useInterpolate.setEnabled((allowedValues & (1<<PamArray.ORIGIN_INTERPOLATE)) != 0);
		usePrevious.setEnabled((allowedValues & (1<<PamArray.ORIGIN_USE_PRECEEDING)) != 0);
	}

	private int getSelectedButton() {
		if (useLatest.isSelected()) {
			return PamArray.ORIGIN_USE_LATEST;
		}
		else if (useInterpolate.isSelected()) {
			return PamArray.ORIGIN_INTERPOLATE;
		}
		else if (usePrevious.isSelected()) {
			return PamArray.ORIGIN_USE_PRECEEDING;
		}
		return -1;
	}
	
	@Override
	public void setParams() {
		
	}

	@Override
	public boolean getParams() {
		return getSelectedButton() >= 0;
	}

	
}
