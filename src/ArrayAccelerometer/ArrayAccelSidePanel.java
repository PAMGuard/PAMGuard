package ArrayAccelerometer;

import javax.swing.JComponent;
import javax.swing.border.TitledBorder;

import PamView.PamSidePanel;

public class ArrayAccelSidePanel implements PamSidePanel {
	
	private ArrayAccelPanel accelPanel;
	private ArrayAccelControl accelControl;
	private TitledBorder titBorder;

	public ArrayAccelSidePanel(ArrayAccelControl accelControl) {
		this.accelControl = accelControl;
		accelPanel = new ArrayAccelPanel(accelControl);
		titBorder = new TitledBorder(accelControl.getUnitName());
		accelPanel.getPanel().setBorder(titBorder);
	}

	@Override
	public JComponent getPanel() {
		return accelPanel.getPanel();
	}

	@Override
	public void rename(String newName) {
		titBorder.setTitle(newName);
	}

}
