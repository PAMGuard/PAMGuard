package Spectrogram;

import java.awt.Component;

import Layout.PamGraphLayout;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayComponentAdapter;
import userDisplay.UserDisplayControl;

/**
 * Wrap up the spectrogram display so that it can be displayed
 * using the more modern UserDisplayComponent system. 
 * @author dg50
 *
 */
public class SpectrogramDisplayComponent extends UserDisplayComponentAdapter {

	private SpectrogramDisplay spectrogramDisplay;
	
	private UserDisplayControl userDisplayControl;
	
	private PamGraphLayout graphLayout;
	
	/**
	 * @param userDisplayControl
	 * @param uniqueDisplayName 
	 */
	public SpectrogramDisplayComponent(UserDisplayControl userDisplayControl, 
			SpectrogramParameters spectrogramParameters, String uniqueDisplayName) {
		super();
		this.userDisplayControl = userDisplayControl;
		setUniqueName(uniqueDisplayName);
		spectrogramDisplay = new SpectrogramDisplay(userDisplayControl, this, spectrogramParameters);
		graphLayout = new PamGraphLayout(spectrogramDisplay);
	}

	@Override
	public Component getComponent() {
		if (graphLayout!=null) {
			return graphLayout.getMainComponent();
		}
		else return null;
	}

	@Override
	public void openComponent() {
		// TODO Auto-generated method stub
//		spectrogramDisplay.
		
	}

	@Override
	public void closeComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyModelChanged(int changeType) {
		spectrogramDisplay.notifyModelChanged(changeType);
	}

	@Override
	public String getFrameTitle() {
		String name =  getUniqueName();
		if (spectrogramDisplay != null) {
			name += ": " + spectrogramDisplay.getFrameTitle();
		}
		return name;
	}

}
