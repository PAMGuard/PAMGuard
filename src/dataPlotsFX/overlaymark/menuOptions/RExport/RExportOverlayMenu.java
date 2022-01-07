package dataPlotsFX.overlaymark.menuOptions.RExport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

import javax.swing.filechooser.FileSystemView;

import org.renjin.eval.Context;
import org.renjin.primitives.io.serialization.RDataWriter;
import PamUtils.PamCalendar;

import PamView.paneloverlay.overlaymark.OverlayMark;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.overlaymark.menuOptions.ExportOverlayMenu;
import dataPlotsFX.overlaymark.menuOptions.RExport.RExportManager.RData;
import detectiongrouplocaliser.DetectionGroupSummary;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import pamViewFX.fxGlyphs.PamSVGIcon;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;

public class RExportOverlayMenu extends ExportOverlayMenu {
	
	private RExportManager rExportManger;
	
	private PamButton buttonNode;

	private String defaultPath;

	private String currentFolder;

	private ImageView mlImage;

	/**
	 * Create the export overlay
	 */
	public RExportOverlayMenu(){
		rExportManger= new RExportManager(); 
		
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
//		Image image = new Image("Resources/prog_R_white.png", standardIconSize+7, standardIconSize+7, false, false);
//		mlImage = new ImageView(image); 
//		PamButton button = new PamButton(); 
//		button.setGraphic(mlImage);
//		return button; 
		
		PamButton button = new PamButton(); 

		PamSVGIcon svgsprite;
		try {
			
//			System.out.println("START SVG load R"); 

			PamSVGIcon svgIconMaker = new PamSVGIcon();

			svgsprite = svgIconMaker.create(getClass().getResource("/Resources/r-project.svg").toURI().toURL(), Color.WHITE);

//			svgsprite = PamSVGIcon.create(new File(getClass().getResource("/Resources/r-project.svg").toURI()), 
//					 "-fx-fill: white; -fx-stroke-width: 2;");
			svgsprite.setFitHeight(standardIconSize+7);
			svgsprite.setFitWidth(standardIconSize+7);		
			button.setGraphic(svgsprite.getSpriteNode());
			
//			System.out.println("END SVG load R"); 


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return button;
	}
	
	@Override
	public Labeled menuAction(DetectionGroupSummary foundDataUnits, int selectedIndex, OverlayMark mark) {
		buttonNode.setOnAction((action)->{
			//System.out.println("Matlab menu pressed: " + foundDataUnits);
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
		
		RData mlData=rExportManger.dataUnits2R(dataUnits);
		if (mlData==null ){
			//do nothing
			System.out.println("rOverlayMenu: no data units were converted to structs");
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
			//add data types to the filen,ae
			for (int i=0 ;i<mlData.rData.length(); i++ ){
				currentPath=currentPath + "_" + mlData.dataUnitTypes.get(i); 
			}
			//add correct file type.	
			currentPath = currentPath + ".RData";
			currentPath = currentFolder+"/"+currentPath;
							
			//now write the file
			try {
//				System.out.println("MLDATA size: "+ mlData.size());
//				System.out.println("---MLArray----");
//				for (int i=0; i<mlData.size(); i++) {
//					System.out.println(mlData.get(i));
//					System.out.println("-------");
//				}
//				System.out.println("--------------");

				Context context = Context.newTopLevelContext(); 
				FileOutputStream fos = new FileOutputStream(currentPath);
				GZIPOutputStream zos = new GZIPOutputStream(fos);
				RDataWriter writer = new RDataWriter(context, zos);		
				writer.save(mlData.rData.build());		
				zos.close();
				writer.close();
				super.showConfirmOverlay(currentPath, "R");
				
			} catch (IOException e1) {
				e1.printStackTrace();
				PamDialogFX.showWarning("There was an error saving the RData file: ");
				return;
			}
			clipBoardFile(new File(currentPath)); 
		}

	}

	@Override
	public boolean canBeUsed(DetectionGroupSummary foundDataUnits, int selectedIndex, OverlayMark mark) {
		if (rExportManger.hasCompatibleUnits(foundDataUnits.getDataList())) return true; 
		return false;
	}

	@Override
	public Tooltip getNodeToolTip() {
		return new Tooltip("Export selected data unit(s) into a n R list saved in a .RData file");
	}

	@Override
	public int getSubMenuGroup() {
		return -1;
	}


}
