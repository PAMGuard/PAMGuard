package difar.display;

import java.awt.Component;

import javax.swing.JComponent;

import difar.DifarControl;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayComponentAdapter;

public class DifarDisplayContainer2 extends UserDisplayComponentAdapter {

	private DifarControl difarControl;
	
	private JComponent component;

	public DifarDisplayContainer2(DifarControl difarControl) {
		this.difarControl = difarControl;
		component = new DisplayNorthPanel(difarControl);
	}

	@Override
	public Component getComponent() {
		return component;
	}

	@Override
	public void openComponent() {
	}

	@Override
	public void closeComponent() {

	}

	@Override
	public void notifyModelChanged(int changeType) {

	}

	@Override
	public String getFrameTitle() {
		return difarControl.getUnitName();
	}

}
