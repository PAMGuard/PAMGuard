package Acquisition.sud;

import javax.swing.JLabel;

import Acquisition.pamAudio.PamAudioSettingsPane;
import Acquisition.pamAudio.SudAudioFile;
import PamView.panel.PamPanel;
import javafx.scene.layout.Pane;

public class SudAudioSettingsPane implements PamAudioSettingsPane {
	
	private SudAudioFile sudAudioFile;
	
	private PamPanel sudAudioPanel;

	public SudAudioSettingsPane(SudAudioFile sudAudioFile) {
		this.sudAudioFile=sudAudioFile; 
	}

	@Override
	public Pane getAudioLoaderPane() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PamPanel getAudioLoaderPanel() {
		if (sudAudioPanel==null) {
			createSudAudioPanel(); 
		}
		return sudAudioPanel;
	}

	private void createSudAudioPanel() {
		sudAudioPanel = new PamPanel(); 
		
		sudAudioPanel.add(new JLabel("Hello"));
		
	}

}
