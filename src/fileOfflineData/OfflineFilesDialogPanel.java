package fileOfflineData;

import javax.swing.JComponent;
import javax.swing.JPanel;

import PamView.dialog.PamDialogPanel;

public class OfflineFilesDialogPanel implements PamDialogPanel {

	private OfflineFileControl offlineFileControl;
	
	private JPanel mainPanel;

	public OfflineFilesDialogPanel(OfflineFileControl offlineFileControl) {
		this.offlineFileControl = offlineFileControl;
	}

	@Override
	public JComponent getDialogComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParams() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

}
