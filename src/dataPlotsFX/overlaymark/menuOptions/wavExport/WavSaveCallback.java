package dataPlotsFX.overlaymark.menuOptions.wavExport;

public interface WavSaveCallback {
	
	/**
	 * Called whenever a wav file has finished saving. 
	 * @param path - the save file. 
	 * @param flag - the falg. 
	 */
	public void wavSaved(String path, int flag); 

}
