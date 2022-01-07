package qa.swing;

import java.awt.Component;

import Layout.DisplayPanel;
import Layout.DisplayPanelContainer;
import Layout.DisplayPanelProvider;
import qa.QAControl;
import userDisplay.UserDisplayComponent;

public class QADisplayPanel implements UserDisplayComponent {

	private QAControl qaControl; 
	
	private QAMainPanel qaMainPanel;

	private String uniqueName;
	
	/**
	 * @param qaControl
	 */
	public QADisplayPanel(QAControl qaControl) {
		super();
		this.qaControl = qaControl;
		qaMainPanel = new QAMainPanel(qaControl);
	}

	@Override
	public Component getComponent() {
		return qaMainPanel.getComponent();
	}

	@Override
	public void openComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyModelChanged(int changeType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getUniqueName() {
		return uniqueName;
	}

	@Override
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;		
	}

	@Override
	public String getFrameTitle() {
		return qaControl.getUnitName() + " Display";
	}



}
