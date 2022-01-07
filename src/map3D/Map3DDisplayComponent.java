package map3D;

import java.awt.Component;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import map3D.fx.DIY3DDisplay;
import map3D.fx.Map3DDisplayFX;
import map3D.fx.Test3DDisplayFX;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;

public class Map3DDisplayComponent implements UserDisplayComponent {

	private JFXPanel jFXPanel;
	private Map3DDisplayProvider map3dDisplayProvider;
	private Map3DControl map3dControl;
	private UserDisplayControl userDisplayControl;
	private String uniqueDisplayName;
//	private Test3DDisplayFX map3dDisplayFX;
	
	public Map3DDisplayComponent(Map3DDisplayProvider map3dDisplayProvider, Map3DControl map3dControl,
			UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		this.map3dDisplayProvider = map3dDisplayProvider;
		this.map3dControl = map3dControl;
		this.userDisplayControl = userDisplayControl;
		this.uniqueDisplayName = uniqueDisplayName;
		jFXPanel = new JFXPanel();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
//				map3dDisplayFX = new Test3DDisplayFX(Map3DDisplayComponent.this);
//				jFXPanel.setScene(map3dDisplayFX.getMainScene());
				
				jFXPanel.setScene(new DIY3DDisplay().getMainScene());
			}
		});
	}

	@Override
	public Component getComponent() {
		return jFXPanel;
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
		return uniqueDisplayName;
	}

	@Override
	public void setUniqueName(String uniqueName) {	
		this.uniqueDisplayName = uniqueName;
	}

	@Override
	public String getFrameTitle() {
		return uniqueDisplayName;
	}

}
