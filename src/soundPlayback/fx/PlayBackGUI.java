package soundPlayback.fx;

import java.util.ArrayList;

import PamController.SettingsPane;
import javafx.scene.layout.Pane;
import pamViewFX.PamControlledGUIFX;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackParameters;

/**
 * The FX GUI for the playback pane. 
 * @author Jamie Macaulay
 *
 */
public class PlayBackGUI extends PamControlledGUIFX {

	
	/**
	 * The main settings pane for the aquisition control. 
	 */
	private PlayBackPane playBackPane;
	
	/**
	 * Reference to the Sound Aquisition control. 
	 */
	private PlaybackControl playBackControl;
	
	/**
	 * The side panes 
	 */
	private ArrayList<Pane> sidePanes; 

	public PlayBackGUI(PlaybackControl aquisitionControl) {
		this.playBackControl=aquisitionControl; 
		sidePanes = new ArrayList<Pane> (); 
		sidePanes.add(new PlayBackSidePane(aquisitionControl));
		
	}
	
	@Override
	public ArrayList<Pane> getSidePanes(){
		return sidePanes;		
	}


	@Override
	public SettingsPane<PlaybackParameters> getSettingsPane(){
		if (playBackPane==null){
			playBackPane=new PlayBackPane(playBackControl);
		}
		playBackPane.setParams(playBackControl.getPlaybackParameters());
		return playBackPane;
	}
	

	/**
	 * This is called whenever a settings pane is closed. If a pamControlledUnit has
	 * settings pane then this should be used to update settings based on info input
	 * into settings pane.
	 */
	public void updateParams() {
		PlaybackParameters newParameters = playBackPane.getParams(playBackControl.getPlaybackParameters()); 
		if (newParameters != null) {
			playBackControl.setPlayBackParamters(newParameters);
		}
	}
	
	

}

