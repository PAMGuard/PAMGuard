package tethys.swing.documents;

import java.awt.Window;

import PamView.dialog.PamDialog;
import tethys.Collection;
import tethys.TethysControl;

public class TethysDocumentsFrame extends PamDialog {
	
	private static final long serialVersionUID = 1L;

	private static TethysDocumentsFrame singleInstance;
	
	private TethysDocumentTable documentsTable;

	private TethysDocumentsFrame(Window parentFrame,TethysControl tethysControl) {
		super(parentFrame, "Tethys Documents", false);
		documentsTable = new TethysDocumentTable(tethysControl, null);
		setDialogComponent(documentsTable.getDialogComponent());
		setModalityType(ModalityType.MODELESS);
		setResizable(true);
		getOkButton().setVisible(false);
		getCancelButton().setText("Close");
	}
	
	public static void showTable(Window parentFrame, TethysControl tethysControl, Collection collection) {
		if (singleInstance == null) {
			singleInstance = new TethysDocumentsFrame(parentFrame, tethysControl);
		}
		singleInstance.documentsTable.setCollection(collection);
//		singleInstance.setTitle(collection.collectionName() + " Documents");
		singleInstance.setVisible(true);
	}
	

	@Override
	public boolean getParams() {
		return true;
	}

	@Override
	public void cancelButtonPressed() {
	}

	@Override
	public void restoreDefaultSettings() {
	}

}
