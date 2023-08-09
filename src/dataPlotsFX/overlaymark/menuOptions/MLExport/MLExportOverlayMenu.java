package dataPlotsFX.overlaymark.menuOptions.MLExport;

import java.io.File;
import java.util.ArrayList;

import detectiongrouplocaliser.DetectionGroupSummary;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import pamViewFX.fxGlyphs.PamSVGIcon;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;

import java.io.IOException;
import javax.swing.filechooser.FileSystemView;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;

import PamUtils.PamCalendar;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.overlaymark.menuOptions.ExportOverlayMenu;

/**
 * Export to MATLAB .mat files menu. 
 * @author Jamie Macaulay 
 *
 */
public class MLExportOverlayMenu extends ExportOverlayMenu {

	/**
	 * The detections manager
	 */
	private MLDetectionsManager mlDetectionsManager;

	/**
	 * Image of MATLAB symbol
	 */
	private ImageView mlImage;

	private String defaultPath; 

	private String currentFolder; 

	private PamButton buttonNode;

	private PamSVGIcon svgsprite; 

	/**
	 * Create the export overlay
	 */
	public MLExportOverlayMenu(){
		mlDetectionsManager= new MLDetectionsManager(); 

		buttonNode = createButton();

		defaultPath=FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
		defaultPath=defaultPath +  "/Pamguard Manual Export";

		currentFolder=defaultPath;

	}

	/**
	 * Create button
	 * @return
	 */
	private PamButton createButton() {		
		PamButton button = new PamButton("");

		if (svgsprite==null) {
			try {
				
//
//				// load the svg file
//			    InputStream svgFile = 
//			          getClass().getResourceAsStream("Resources/matlab_icon.svg");
//			    SvgLoader loader = new SvgLoader();
//			    //loader.setGradientTransformPolicy(GradientPolicy.DISCARD);
//			    Group svgImage = loader.loadSvg(svgFile);
//			    
//			    
//				button.setGraphic(svgImage);
				
				PamSVGIcon svgIconMaker = new PamSVGIcon();

			    
				svgsprite = svgIconMaker.create(getClass().getResource("/Resources/matlab_icon2.svg").toURI().toURL(), Color.WHITE, 1);
//						"-fx-fill: white; -fx-stroke-width: 2;");
				svgsprite.setFitHeight(standardIconSize+7);
				svgsprite.setFitWidth(standardIconSize+7);		
				button.setGraphic(svgsprite.getSpriteNode());

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return button; 
	}

	@Override
	public Labeled menuAction(DetectionGroupSummary foundDataUnits, int selectedIndex, OverlayMark mark) {
		buttonNode.setOnAction((action)->{
			//			System.out.println("Matlab menu pressed: " + foundDataUnits);
			menuPressed( foundDataUnits,  selectedIndex,  mark) ;
		});
		return buttonNode;
	}



	public void menuPressed(DetectionGroupSummary foundDataUnits, int selectedIndex, OverlayMark mark) {
		//A bit nasty but convert to a normal data list. 
		ArrayList<PamDataUnit> dataUnits= new ArrayList<PamDataUnit>();
		for (PamDataUnit fnDataUnit: foundDataUnits.getDataList()){
			dataUnits.add(fnDataUnit);
		}

		ArrayList<MLArray> mlData=mlDetectionsManager.dataUnits2MAT(dataUnits);
		if (mlData==null || mlData.size()==0){
			//do nothing
			System.out.println("MLExportOverlayMenu: no data units were converted to structs");
		}
		else{
			//save data. 
			File folder = new File(currentFolder); 
			//check the folder exists and if not create it. 
			if (!folder.exists()){
				if (!folder.mkdir()){
					//TODO- warning message.
					System.err.println("MLExportOverlayMenu: unable to make directory: " + folder);
					return;
				}
			}

			//create the filename; 
			long millisStart=foundDataUnits.getFirstTimeMillis();
			String currentPath = PamCalendar.formatFileDateTime(millisStart, false);
			//add data types to the filename
			for (int i=0 ;i<mlData.size(); i=i+2 ){//bit of a hack but every second name is the samplerate so leave that out of filename.  
				currentPath=currentPath + "_" + mlData.get(i).getName(); 
			}
			//add correct file type.	
			currentPath = currentPath + ".mat";
			currentPath = currentFolder+ File.separator +currentPath;

			//now write the file
			try {
				//				System.out.println("MLDATA size: "+ mlData.size());
				//				System.out.println("---MLArray----");
				//				for (int i=0; i<mlData.size(); i++) {
				//					System.out.println(mlData.get(i));
				//					System.out.println("-------");
				//				}
				//				System.out.println("--------------");

				MatFileWriter filewrite=new MatFileWriter(currentPath, mlData);
				super.showConfirmOverlay(currentPath, "MATLAB");

			} catch (IOException e1) {
				e1.printStackTrace();
				PamDialogFX.showWarning("There was an error saving the file: ");
				return;
			}

			clipBoardFile(new File(currentPath)); 

		}

	}

	@Override
	public int getSubMenuGroup() {
		return -1;
	}

	@Override
	public boolean canBeUsed(DetectionGroupSummary foundDataUnits, int selectedIndex, OverlayMark mark) {
		if (mlDetectionsManager.hasCompatibleUnits(foundDataUnits.getDataList())) return true; 
		return false;
	}

	@Override
	public Tooltip getNodeToolTip() {
		return new Tooltip("Export selected data unit(s) into a list of MATLAB structures");
	}




}
