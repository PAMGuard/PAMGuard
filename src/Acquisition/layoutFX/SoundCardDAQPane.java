package Acquisition.layoutFX;

import java.util.ArrayList;

import Acquisition.SoundCardSystem;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamVBox;

/**
 * SpundCard settings controls for the DAQ pane. 
 * @author Jamie Macaulay
 *
 */
public class SoundCardDAQPane extends DAQSettingsPane {

	/**
	 * The main pane. 
	 */
	PamBorderPane mainPane;

	private ComboBox<String> audioDevices;

	/**
	 * The sound card system. 
	 */
	private SoundCardSystem soundCardSystem; 
	
	private DAQStatusPaneFactory statusBarPaneFactory = new SimpleStatusPaneFactory("This is  sound card DAQ");

	public SoundCardDAQPane(SoundCardSystem soundCardSystem) {

		this.soundCardSystem=soundCardSystem; 

		mainPane = new PamBorderPane(); 

		PamVBox holder = new PamVBox(); 
		holder.setSpacing(5);

		Label title = new Label("Select audio line"); 
		PamGuiManagerFX.titleFont2style(title);

		audioDevices = new ComboBox<String>(); 
		audioDevices.setMaxWidth(Double.MAX_VALUE);
		
		holder.getChildren().addAll(title, audioDevices);
		mainPane.setCenter(holder);

		//			
		//			JPanel p = new JPanel();
		//			
		//			p.setBorder(new TitledBorder("Select audio line"));
		//			p.setLayout(new BorderLayout());
		//			p.add(BorderLayout.CENTER, audioDevices = new JComboBox());
		//			

	}



	@Override
	public void setParams() {


		// do a quick check to see if the system type is stored in the parameters.  This field was added
		// to the SoundCardParameters class on 23/11/2020, so any psfx created before this time
		// would hold a null.  The system type is used by the getParameterSet method to decide
		// whether or not to include the parameters in the XML output
		if (soundCardSystem.getSoundCardParameters().systemType==null) 
			soundCardSystem.getSoundCardParameters().systemType=soundCardSystem.getSystemType();


		ArrayList<String> devices = soundCardSystem.getDevicesList();

		audioDevices.getItems().clear();
		for (int i = 0; i < devices.size(); i++) {
			//System.out.println("Adding to audio device:"+devices.get(i));
			audioDevices.getItems().add(devices.get(i));
		}

		soundCardSystem.getSoundCardParameters().deviceNumber = Math.max(Math.min(devices.size()-1, soundCardSystem.getSoundCardParameters().deviceNumber), 0);
		if (devices.size() > 0) {
			audioDevices.getSelectionModel().select(soundCardSystem.getSoundCardParameters().deviceNumber);
		}

	}

	@Override
	public boolean getParams() {
		//System.out.println("soundCardParameters: " + soundCardParameters + "audioDevices: " + audioDevices);
		if (audioDevices!=null) soundCardSystem.getSoundCardParameters().deviceNumber = audioDevices.getSelectionModel().getSelectedIndex();
		return true;
	}

	@Override
	public Object getParams(Object currParams) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParams(Object input) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return "Sound card params";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub

	}

	@Override
	public DAQStatusPaneFactory getStatusBarFactory() {
		return statusBarPaneFactory;
	}

}
