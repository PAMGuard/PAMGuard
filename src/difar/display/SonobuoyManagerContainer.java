package difar.display;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;

import PamView.PamTable;
import difar.DifarControl;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayComponentAdapter;

public class SonobuoyManagerContainer extends UserDisplayComponentAdapter {

	private DifarControl difarControl;
	
	private JComponent component;

	public SonobuoyManagerContainer(DifarControl difarControl) {
		this.difarControl = difarControl;
		component = new SonobuoyManagerPanel(difarControl);
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

	public PamTable getSonobuoyTable(){
		return ((SonobuoyManagerPanel) component).sonobuoyTable;
	}

	@Override
	public String getFrameTitle() {
		return difarControl.getUnitName() + " buoy manager";
	}
	
}
