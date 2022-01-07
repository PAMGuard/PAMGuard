package PamController.settings.output.xml;

import java.io.File;

public class XMLImportData {

	private File importFile;
	private Object importObject;

	public XMLImportData(File importFile, Object importObject) {
		super();
		this.importFile = importFile;
		this.importObject = importObject;
	}

	/**
	 * @return the importFile
	 */
	public File getImportFile() {
		return importFile;
	}

	/**
	 * @param importFile the importFile to set
	 */
	public void setImportFile(File importFile) {
		this.importFile = importFile;
	}

	/**
	 * @return the importObject
	 */
	public Object getImportObject() {
		return importObject;
	}

	/**
	 * @param importObject the importObject to set
	 */
	public void setImportObject(Object importObject) {
		this.importObject = importObject;
	}

}
