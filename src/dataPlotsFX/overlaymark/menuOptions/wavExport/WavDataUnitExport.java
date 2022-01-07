package dataPlotsFX.overlaymark.menuOptions.wavExport;

import PamView.paneloverlay.overlaymark.OverlayMark;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickDetection;
import wavFiles.WavFileWriter;

/**
 * Handles writing of .wav files. Generally wav files from overlay marks will just be the raw data. But there may be cases were an artifical .wav files 
 * needs to be created. e.g. concatenated clicks if there is no raw data available. 
 */
public abstract class WavDataUnitExport<T extends PamDataUnit<?, ?>> {
	
	/**
	 * Write the .wav file.
	 * @param dataUnits - list of selected data units. 
	 * @param mark - the overlay mark. Can be null.
	 */
	public void writeWavFile(WavFileWriter wavWriter, T dataUnit, OverlayMark mark) {
		if (wavWriter.append(getWavClip(dataUnit))) {
			
		}
	}
	
	
	/**
	 * Get a wav file clip from a data unit
	 * @param dataUnit - the data unit. 
	 * @return the wav file clip. 
	 */
	public abstract double[][] getWavClip(T dataUnit);
	
	/**
	 * Get the data unit class whihc uses this .wav export function. 
	 * @return the class of the type of the data unit which uses this class. 
	 */
	public abstract Class<?> getUnitClass(); 
			
			

}
