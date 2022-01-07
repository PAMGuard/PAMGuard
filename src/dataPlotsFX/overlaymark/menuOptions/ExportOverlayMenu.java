package dataPlotsFX.overlaymark.menuOptions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.controlsfx.control.Notifications;

import dataPlotsFX.overlaymark.FoundDataUnitFX;
import javafx.geometry.Pos;
import javafx.util.Duration;


/**
 * Some functions useful for exporting manually annotated files. 
 * 
 * @author Jamie Macaulay
 *
 */
public abstract class ExportOverlayMenu implements OverlayMenuItem {


	public static int standardIconSize=30; 
	
	/**
	 * Save the file to clipboard.  
	 * @param file - the filepath to save. 
	 */
	protected void clipBoardFile(File file){
		List<File> listOfFiles = new ArrayList<File>();
		listOfFiles.add(file);
		FileTransferable ft = new FileTransferable(listOfFiles);

		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ft, new ClipboardOwner() {
			@Override
			public void lostOwnership(Clipboard clipboard, Transferable contents) {
				System.out.println("Lost ownership");
			}
		});
	}

	/**
	 * Find the earliest data unit in the list. Should normally be 
	 * @param foundDataUnits - a list of data units to search
	 * @return the unit with the earliest time stamp. 
	 */
	protected FoundDataUnitFX getEarliestDataUnit(ArrayList<FoundDataUnitFX> foundDataUnits){
		Long millis = Long.MAX_VALUE;
		int index=-1; 

		for (int i=0; i<foundDataUnits.size(); i++){
			if (foundDataUnits.get(i).dataUnit.getTimeMilliseconds()<millis){
				millis=foundDataUnits.get(i).dataUnit.getTimeMilliseconds();
				index=i; 
			}
		}

		return foundDataUnits.get(index); 
	}


	/**
	 * Allows files to be copied ot clipboard. 
	 *
	 */
	public static class FileTransferable implements Transferable {

		private List listOfFiles;

		public FileTransferable(List listOfFiles) {
			this.listOfFiles = listOfFiles;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{DataFlavor.javaFileListFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.javaFileListFlavor.equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			return listOfFiles;
		}
	}


	public void showConfirmOverlay(String fileURl, String dataName){
		Notifications notification = Notifications.create().title("Manual Data Export");
		notification.darkStyle();

		notification.text(dataName +  " data has been saved to " + fileURl + ". The file has also been saved to the clipboard");

		notification.hideAfter(Duration.millis(10000));
		notification.position(Pos.BOTTOM_RIGHT);

		notification.show();

	}
	
	@Override
	public int getFlag() {
		return ExportOverlayMenu.EXPORT_GROUP;
	}
	
}
