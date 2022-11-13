package Acquisition.filetypes;

import Acquisition.FileInputSystem;
import PamView.dialog.warn.WarnOnce;

public class SUDFileType extends SoundFileType {

	private boolean isShown = false;

	private String sudInfoText = "<html>There are SoundTrap SUD files in your selection. PAMGuard can now process data from these " +
			"directly with no need to unpack them into WAV files. " + 
			"<p>If the SoundTrap was running with the click detector, then you should add a SoundTrap Click Detector module" +
			" and detected clicks will automatically be extracted to binary files while processing the wav data." +
			"<p>See the Click Detector help for further details";

	public SUDFileType() {
		super(".sud");
	}

	@Override
	public void selected(FileInputSystem fileInputSystem) {
		if (isShown) {
			return;
		}
		WarnOnce.showWarning("SoundTrap SUD Files", sudInfoText, WarnOnce.OK_OPTION);
		isShown = true;		
	}

}
