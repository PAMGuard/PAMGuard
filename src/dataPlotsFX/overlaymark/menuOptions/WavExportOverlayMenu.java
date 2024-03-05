package dataPlotsFX.overlaymark.menuOptions;

import java.io.File;

import PamController.PamController;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamRawDataBlock;
import detectiongrouplocaliser.DetectionGroupSummary;
import export.wavExport.WavFileExportManager;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorInput;
import javafx.scene.text.Text;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamButton;
import wavFiles.WavFileWriter;

/**
 * Export a .wav clip. 
 * @author 	Jamie Macaulay 
 *
 */
public class WavExportOverlayMenu  extends ExportOverlayMenu {

	private Text wavFileGlyph;

	/**
	 * The wav file write
	 */
	private WavFileWriter wavFile;

	private String defaultPath;

	private String currentFolder;

	private WavFileExportManager wavExportManager;

	private ColorInput shadow;

	private Timeline timline;

	private PamButton pamButton; 

	public WavExportOverlayMenu(){
//		wavFileGlyph=PamGlyphDude.createPamGlyph(MaterialDesignIcon.FILE_MUSIC, standardIconSize);
		wavFileGlyph=PamGlyphDude.createPamIcon("mdi2f-file-music", standardIconSize+14);
		wavExportManager= new WavFileExportManager(); 

		//set a callback when saving is finsihed. 
		wavExportManager.setOnWavSaved((saveFile, flag)->{
			Platform.runLater(()->{
				confirmSaved(saveFile); 
			});
		}); 

		pamButton = createButton();
		//		createAnimation();
	}

	/**
	 * Create button
	 * @return
	 */
	private PamButton createButton() {
//		wavFileGlyph=PamGlyphDude.createPamGlyph(MaterialDesignIcon.FILE_MUSIC, standardIconSize);
		wavFileGlyph=PamGlyphDude.createPamIcon("mdi2f-file-music", standardIconSize+7);
		PamButton button = new PamButton(); 
		button.setGraphic(wavFileGlyph);
		return button; 
	}


	@Override
	public Labeled menuAction(DetectionGroupSummary foundDataUnits, int selectedIndex, OverlayMark mark) {
		pamButton.setOnAction((action) -> {
			writeWavFile( foundDataUnits,  selectedIndex,  mark);

		});
		return pamButton; 
	}

	/**
	 * Confirm the wav file has been saved. 
	 * @param currentPath
	 */
	public void confirmSaved(String currentPath) {
		//now write the file
		super.showConfirmOverlay(currentPath, "Wav File");

		clipBoardFile(new File(currentPath)); 
	}


	//	/**
	//	 * Create an animation whihc can be used to animate the wav button
	//	 */
	//	private void  createAnimation() {
	//		shadow = new ColorInput();
	//		shadow.setPaint(Color.CYAN);
	//		
	//	
	//		timline = new Timeline(
	//				new KeyFrame(Duration.seconds(1), new KeyValue(shadow.paintProperty(), 0d)),
	//				new KeyFrame(Duration.seconds(1), new KeyValue(shadow.levelProperty(), 1d)));
	//		timline.setAutoReverse(true);
	//
	//	}	
	//	
	//	/**
	//	 * Animate the .wav button
	//	 */
	//	private void animateButton() {
	//		pamButton.setEffect(shadow); 
	//		timline.setCycleCount(Timeline.INDEFINITE);
	//		timline.play(); 
	//	}
	//	
	//	private void stopAnimation() {
	//		wavFileGlyph.setEffect(null); 
	//		timline.stop(); 
	//	}


	/**
	 * Write a wav file./ 
	 * @param foundDataUnits
	 * @param selectedIndex
	 * @param mark
	 */
	private void writeWavFile(DetectionGroupSummary foundDataUnits, int selectedIndex, OverlayMark mark) {
		//		 animateButton() ; //start button animation. 
		wavExportManager.dataUnits2Wav(foundDataUnits, selectedIndex, mark);
	}		


	@Override
	public boolean canBeUsed(DetectionGroupSummary foundDataUnits, int selectedIndex, OverlayMark mark) {
		//now search for the raw data block 
		//System.out.println("Can raw data be used?");
		PamRawDataBlock rawDataBlock;
		if (foundDataUnits==null || foundDataUnits.getDataList().size()<=0) {
			rawDataBlock=PamController.getInstance().getRawDataBlock(0);
		}
		else {
			rawDataBlock=foundDataUnits.getDataList().get(0).getParentDataBlock().getRawSourceDataBlock();
		}
		//is there available raw data
		if (mark!=null && WavFileExportManager.haveRawData(rawDataBlock, (long) mark.getLimits()[0], (long) mark.getLimits()[1])) {
			return true;
		} 


		if (foundDataUnits!=null) {
			//do any of the data units have raw snippets of data that could be used?
			for (PamDataUnit fnDataUnit: foundDataUnits.getDataList()){
				//System.out.println("Save detection wav." + foundDataUnits.getNumDataUnits());
				for (int i=0; i<wavExportManager.getWavDataUnitExporters().size(); i++) {
					if (wavExportManager.getWavDataUnitExporters().get(i).getUnitClass().isAssignableFrom(fnDataUnit.getClass())) {
						return true; 
					}
				}
			}
		}

		return false;
	}

	@Override
	public Tooltip getNodeToolTip() {
		return new Tooltip("Export the current selection into a .wav file. If a data unit is selected"
				+ " then the wav file is the start and stop time of the data unit");

	}

	@Override
	public int getSubMenuGroup() {
		return -1;
	}
}
