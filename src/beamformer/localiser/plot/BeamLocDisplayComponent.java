package beamformer.localiser.plot;

import java.awt.Component;

import PamUtils.SimpleObserver;
import beamformer.localiser.BeamFormLocaliserControl;
import beamformer.localiser.BeamLocaliserData;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
//import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;

public class BeamLocDisplayComponent implements UserDisplayComponent, SimpleObserver<BeamLocaliserData> {

	private BeamFormLocaliserControl bfLocaliserControl;
	private String uniqueDisplayName;

	private FXBeamLocDisplay fxLocDisplay;
	private JFXPanel jFXPanel;
	
	public BeamLocDisplayComponent(BeamLocDisplayProvider beamLocDisplayProvider,
			BeamFormLocaliserControl bfLocaliserControl, UserDisplayControl userDisplayControl,
			String uniqueDisplayName) {
		this.bfLocaliserControl = bfLocaliserControl;
		this.uniqueDisplayName = uniqueDisplayName;
		bfLocaliserControl.getBeamLocaliserObservable().addObserver(this);
		
		jFXPanel = new JFXPanel();
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				fxLocDisplay = new FXBeamLocDisplay(beamLocDisplayProvider, bfLocaliserControl, userDisplayControl, uniqueDisplayName);
				Scene fxMainScene = fxLocDisplay.getMainScene();
				jFXPanel.setScene(fxMainScene);
			}
		});
	}

	@Override
	public Component getComponent() {
		return jFXPanel;
	}

	@Override
	public void openComponent() {
		
	}

	@Override
	public void closeComponent() {
		bfLocaliserControl.getBeamLocaliserObservable().removeObserver(this);
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
		return bfLocaliserControl.getUnitName();
	}

	@Override
	public void update(BeamLocaliserData beamLocData) {
		if (fxLocDisplay == null) return;
		fxLocDisplay.update(beamLocData);
	}

	@Override
	public void updateSettings() {
		fxLocDisplay.updateSettings();
	}

}
