package bearinglocaliser.display;

import java.awt.Component;

import PamUtils.SimpleObserver;
import beamformer.localiser.plot.FXBeamLocDisplay;
import bearinglocaliser.BearingLocaliserControl;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;

public class BearingDisplayComponent implements UserDisplayComponent {

	private BearingLocaliserControl bearingLocaliserControl;
	private UserDisplayControl userDisplayControl;
	private String uniqueDisplayName;

	private JFXPanel jFXPanel;
	private BearingDisplayProvider bearingDisplayProvider;
	
	private FXBearingDisplay fxBearingDisplay;
	
	public BearingDisplayComponent(BearingDisplayProvider bearingDisplayProvider, BearingLocaliserControl bearingLocaliserControl,
			UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		super();
		this.bearingDisplayProvider = bearingDisplayProvider;
		this.bearingLocaliserControl = bearingLocaliserControl;
		this.userDisplayControl = userDisplayControl;
		this.uniqueDisplayName = uniqueDisplayName;

		jFXPanel = new JFXPanel();
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				fxBearingDisplay = new FXBearingDisplay(bearingDisplayProvider, bearingLocaliserControl, userDisplayControl, uniqueDisplayName);
				Scene fxMainScene = fxBearingDisplay.getMainScene();
				jFXPanel.setScene(fxMainScene);
				bearingLocaliserControl.getConfigObservable().addObserver(fxBearingDisplay);
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
		return getUniqueName();
	}


	/**
	 * @return the fxBearingDisplay
	 */
	public FXBearingDisplay getFxBearingDisplay() {
		return fxBearingDisplay;
	}

}
