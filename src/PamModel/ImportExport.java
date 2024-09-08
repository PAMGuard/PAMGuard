package PamModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import PamController.settings.output.xml.PamguardXMLWriter;
import PamController.settings.output.xml.XMLImportData;
import PamController.settings.output.xml.XMLSettingsSwing;
import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;
import PamView.dialog.warn.WarnOnce;

/**
 * General PAMguard class for managing import and export of specific Java object types. 
 * @author Doug
 *
 * @param <IOClass>
 */
public class ImportExport {
	
	private JButton importButton, exportButton;
	private ImportExportUser ioUser;
	private PamFileFilter pamFileFilter;
	private String fileEnd;
	private String fileDescription;

	public ImportExport(String fileDescription, String fileEnd, ImportExportUser ioUser) {
		this.ioUser = ioUser;
		this.fileEnd = fileEnd;
		this.fileDescription = fileDescription;
		pamFileFilter = makeFileFilter();
		exportButton = new JButton("Export");
		importButton = new JButton("Import");
		exportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doExport();				
			}
		});
		importButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doImport();				
			}
		});
	}

	/**
	 * File filter for imports which may be one or two file types. 
	 * @return
	 */
	private PamFileFilter makeFileFilter() {
		PamFileFilter filter = null;
		if ((ioUser.getExportTypes() & ImportExportUser.EXPORT_SERIALIZED) != 0) {
			filter = new PamFileFilter(fileDescription, fileEnd);
		}
		if ((ioUser.getExportTypes() & ImportExportUser.EXPORT_XML) != 0) {
			if (filter == null) {
				filter = new PamFileFilter(fileDescription, ".xml");
			}
			else {
				filter.addFileType(".xml");
			}
		}
		return filter;
		
	}

	/**
	 * Export settings
	 */
    protected boolean doExport() {
		int types = ioUser.getExportTypes();
		if (types == ImportExportUser.EXPORT_SERIALIZED) {
			return doSerializedExport();
		}
		else if (types == ImportExportUser.EXPORT_XML) {
			return doXMLExport();
		}
		// else more than one type. 
		JPopupMenu popMenu = new JPopupMenu();
		if ((types & ImportExportUser.EXPORT_SERIALIZED) != 0) {
			JMenuItem menuItem = new JMenuItem("Export serialised Java object");
			menuItem.setToolTipText("Can only be read by Java programs");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					doSerializedExport();
				}
			});
			popMenu.add(menuItem);
		}
		if ((types & ImportExportUser.EXPORT_SERIALIZED) != 0) {
			JMenuItem menuItem = new JMenuItem("Export to XML file");
			menuItem.setToolTipText("Readable by humans and other software");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					doXMLExport();
				}
			});
			popMenu.add(menuItem);
		}
		popMenu.show(exportButton, exportButton.getWidth()/2, exportButton.getHeight()/2);
		
		return true;
	}
    
	private boolean doXMLExport() {
    	Serializable ioObject = ioUser.getExportObject();
    	if (ioObject == null) {
    		return false;
    	}
    	// dialog to find output file name and then export Java object. 

		File file = getExportFile(".xml");
		if (file == null) {
			return false;
		}
		PamguardXMLWriter xmlWriter = PamguardXMLWriter.getXMLWriter();
		return xmlWriter.writeOneModule(file, ioUser.getSettingsWrapper(), ioObject);
		
	}

	private boolean doSerializedExport() {
    	Serializable ioObject = ioUser.getExportObject();
    	if (ioObject == null) {
    		return false;
    	}
    	// dialog to find output file name and then export Java object. 

		File file = getExportFile(fileEnd);
		if (file == null) {
			return false;
		}
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(ioObject);
		} catch (Exception Ex) {
			System.out.println(Ex);
			return false;
		}
		return true;
	}
	
	private File getExportFile(String fileEnd) { 
		JFileChooser fileChooser = new PamFileChooser();
		fileChooser.setFileFilter(new PamFileFilter(fileDescription, fileEnd));
		fileChooser.setFileHidingEnabled(true);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int state = fileChooser.showSaveDialog(exportButton);
		if (state != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		File file = fileChooser.getSelectedFile();
		file = PamFileFilter.checkFileEnd(file, fileEnd, true);
		if (file.exists()) {
			String msg = String.format("Do you want to overwrite the file " + file.getAbsolutePath());
			int ans = WarnOnce.showWarning(null,  "File already exists", msg, WarnOnce.OK_CANCEL_OPTION);
			if (ans == WarnOnce.CANCEL_OPTION) {
				return null;
			}
		}
		return file;
	}

    /**
     * Import Settings.
     */
	protected boolean doImport() {
    	// dialog to find output file name and then export Java object. 
		JFileChooser fileChooser = new PamFileChooser();
		fileChooser.setFileFilter(pamFileFilter);
		fileChooser.setFileHidingEnabled(true);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int state = fileChooser.showOpenDialog(importButton);
		if (state != JFileChooser.APPROVE_OPTION) {
			return false;
		}
		File selFile = fileChooser.getSelectedFile();
		if (selFile == null) {
			return false;
		}
		if (selFile.getName().endsWith("xml")) {
			return importXML(selFile);
		}
		else if (selFile.getName().endsWith(fileEnd)) {
			return importSerialized(selFile);
		}
		return false;
	}
	
	private boolean importXML(File selFile) {
		XMLSettingsSwing xmlSwing = new XMLSettingsSwing();
		
		XMLImportData imported = xmlSwing.importXMLSettings(null, selFile, ioUser.getIOClass());
		if (imported == null || imported.getImportObject() == null) {
			return false;
		}
//		if (imported.getImportObject().getClass() instanceof ioUser.getIOClass() == false)
		try {
			ioUser.setImportObject((Serializable) imported.getImportObject());
		}
		catch (ClassCastException e) {
			System.out.printf("Imported obect class %s cannot be cast to correct type", imported.getImportObject().getClass());
		}
		
		return false;
	}

	private boolean importSerialized(File selFile) {
		Object impObject = null;

		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(selFile));
			impObject = ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (impObject == null) {
			return false;
		}
		try {
			ioUser.setImportObject((Serializable) impObject);
		}
		catch (ClassCastException e) {
			System.out.printf("Imported obect class %s cannot be cast to correct type", impObject.getClass());
		}
		return true;
	}


	/**
	 * @return the importButton
	 */
	public JButton getImportButton() {
		return importButton;
	}


	/**
	 * @return the exportButton
	 */
	public JButton getExportButton() {
		return exportButton;
	}

}
