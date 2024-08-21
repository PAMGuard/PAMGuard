package export.wavExport;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.swing.filechooser.FileSystemView;

import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamguardMVC.LoadObserver;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RawDataHolder;
import PamguardMVC.dataOffline.OfflineDataLoading;
import dataMap.OfflineDataMapPoint;
import detectiongrouplocaliser.DetectionGroupSummary;
import export.PamDataUnitExporter;
import javafx.scene.layout.Pane;
import wavFiles.Wav16AudioFormat;
import wavFiles.WavFileWriter;

/**
 * Writes data units and/or ordered raw data to a wav file. Has functions to
 * handle .wav file writing based on overlay marks and detection groups with
 * functions to make decisions based on what type of data unit is selected and
 * whether raw data is available.
 * <p>
 * There are two primary use cases; <br>
 * 1) Order raw data from an overlay mark and save as a wav file <br>
 * 2) Save a list of data units to wav files - either a single file with zero
 * pads, a concatenated file or separate files.
 * 
 * @author Jamie Macaulay
 *
 */
public class WavDetExportManager implements PamDataUnitExporter  {

	/**
	 * Options for exporting wav files. 
	 */
	private WavExportOptions wavFileoptions = new WavExportOptions(); 

	/**
	 * Settings panel for wav file exporting
	 */
	private WavOptionsPanel wavOptionsPanel; 

	/**
	 * Exporter of wav files. 
	 */
	private WavDetExport wavDetExport = new WavDetExport();

	public WavDetExportManager() {

	}

	@Override
	public boolean hasCompatibleUnits(Class dataUnitType) {
		//        boolean implementsInterface = Arrays.stream(dataUnitType.getInterfaces()).anyMatch(i -> i == RawDataHolder.class);
		if ( RawDataHolder.class.isAssignableFrom(dataUnitType)) return true;
		return false;
	}



	@Override
	public boolean exportData(File fileName,
			List<PamDataUnit> dataUnits, boolean append) {

		//make sure we have the latest options. 
		if (wavOptionsPanel!=null) {
			//means the options panel has been opened. 
			wavFileoptions = wavOptionsPanel.getParams(wavFileoptions);
		}

		//should we zeropad? 
		//saveDataUnitWav(dataUnits); 		
		switch (wavFileoptions.wavSaveChoice) {
		case WavExportOptions.SAVEWAV_CONCAT:
			wavDetExport.writeDataUnitWav(dataUnits, fileName, false);
			break;
		case WavExportOptions.SAVEWAV_INDIVIDUAL:
			//here the filename will not be used but the parent folder will be used instead to write
			//lots of wav files to. 
			wavDetExport.writeDataUnitWavs(dataUnits, fileName);
			break;
		case WavExportOptions.SAVEWAV_ZERO_PAD:
			wavDetExport.writeDataUnitWav(dataUnits, fileName, true);
			break;
		}
		
		return true; 
	}



	@Override
	public String getFileExtension() {
		return "wav";
	}



	@Override
	public String getIconString() {
		return "mdi2f-file-music";
	}



	@Override
	public String getName() {
		return "raw sound";
	}



	@Override
	public void close() {
		// TODO Auto-generated method stub
	}



	@Override
	public boolean isNeedsNewFile() {
		return false;
	}



	@Override
	public Component getOptionsPanel() {
		if (this.wavOptionsPanel==null) {
			this.wavOptionsPanel = new WavOptionsPanel(); 
		}
		wavOptionsPanel.setParams(this.wavFileoptions) ;
		return wavOptionsPanel;
	}



	@Override
	public Pane getOptionsPane() {
		// TODO - make FX version of settings. 
		return null;
	}





	//	hello(){
	//
	//
	//		if (mark==null) {
	//			start= foundDataUnits.getFirstTimeMillis();
	//			end= foundDataUnits.getLastTimeMillis(); 
	//		}
	//
	//		File folder = new File(currentFolder); 
	//
	//		//save a .wav file clip. 
	//		if (!folder.exists()){
	//			if (!folder.mkdir()){
	//				//TODO- warning message. 
	//				return;
	//			}
	//		}
	//
	//		String currentPath = PamCalendar.formatFileDateTime();
	//		//add data types to the filen,ae
	//		for (int i=0 ;i<mlData.size(); i++ ){
	//			currentPath=currentPath + "_" + mlData.get(i).getName(); 
	//		}
	//		//add correct file type.	
	//		currentPath = currentPath + ".mat";
	//		currentPath = currentFolder+"\\"+currentPath;
	//
	//
	//		if (append && clipControl.clipSettings.storageOption == ClipSettings.STORE_WAVFILES) {
	//			wavFile.append(rawData);
	//			lastClipDataUnit.setSampleDuration(rawEnd-lastClipDataUnit.getStartSample());
	//			clipDataBlock.updatePamData(lastClipDataUnit, dataUnit.getTimeMilliseconds());
	//			//			System.out.println(String.format("%d samples added to file", rawData[0].length));
	//		}
	//		else {
	//			ClipDataUnit clipDataUnit;
	//			long startMillis = dataUnit.getTimeMilliseconds() - (long) (clipGenSetting.preSeconds*1000.);
	//			if (clipControl.clipSettings.storageOption == ClipSettings.STORE_WAVFILES) {
	//				String folderName = getClipFileFolder(dataUnit.getTimeMilliseconds(), true);
	//				String fileName = getClipFileName(startMillis);
	//				AudioFormat af = new Wav16AudioFormat(getSampleRate(), rawData.length);
	//				wavFile = new WavFileWriter(folderName+fileName, af);
	//				wavFile.write(rawData);
	//				wavFile.close();
	//				// make a data unit to go with it. 
	//				clipDataUnit = new ClipDataUnit(startMillis, dataUnit.getTimeMilliseconds(), rawStart,
	//						(int)(rawEnd-rawStart), channelMap, fileName, dataBlock.getDataName(), rawData, getSampleRate());
	//			}
	//			else {
	//				clipDataUnit = new ClipDataUnit(startMillis, dataUnit.getTimeMilliseconds(), rawStart,
	//						(int)(rawEnd-rawStart), channelMap, "", dataBlock.getDataName(), rawData, getSampleRate());
	//			}
	//			clipDataUnit.setFrequency(dataUnit.getFrequency());
	//			lastClipDataUnit = clipDataUnit;
	//			if (bearingLocaliser != null) {
	//				localiseClip(clipDataUnit, bearingLocaliser, hydrophoneMap);
	//			}				
	//			clipDataBlock.addPamData(clipDataUnit);
	//		}
	//
	//		return 0; // no error. 
	//	}
	//
	//	}
	//	}

}
