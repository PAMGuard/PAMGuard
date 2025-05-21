package Acquisition;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import PamController.PamSettings;
import PamUtils.XMLUtils;
import PamUtils.worker.filelist.WavFileType;

public class DCL5System extends FolderInputSystem implements PamSettings {


	//	public DCL5System() {
//		super();
//		PamSettingManager.getInstance().registerSettings(this);
//	}
	String xmlDocName = "C:\\DCL_5_2011\\Matlab\\SpeciesFiletimes2.xml";


	Document doc;


	public DCL5System(AcquisitionControl acquisitionControl) {
		super(acquisitionControl);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getSystemType() {
		return "DCL5 file folder or multiple files";
	}

	@Override
	public String getUnitName() {
		return "DCL5 Folder Analysis";
	}

	@Override
	public String getUnitType() {
		return "DCL5 Folder Acquisition System";
	}

	@Override
	public long getFileStartTime(File file) {
		if (doc == null) {
			doc = XMLUtils.createBlankDoc();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			try {

				//Using factory get an instance of document builder
				DocumentBuilder db = dbf.newDocumentBuilder();

				//parse using builder to get DOM representation of the XML file
				doc = db.parse(xmlDocName);
			}
			catch (Exception e) {
				e.printStackTrace();
				return 0;
			}

		}
		NodeList nl = doc.getElementsByTagName("files");
		Element fileEl = (Element) nl.item(0);
//		Element fileEl = doc.getElementById("files");
//		Element aFileEl;
//		aFileEl.getElementsByTagName(file.getName());
		NodeList nl2 = fileEl.getElementsByTagName(file.getName());
		if (nl2 == null) {
			System.out.println("Unble to find time for file " + file.getName());
			return 0;
		}
		Element e = (Element) nl2.item(0);
		if (e == null) {
			System.out.println("Unble to find time for file " + file.getName());
			return 0;
		}
		String timeString = e.getAttribute("time");
		return super.getFileStartTime(file);
		/*
		 * Get the file time from the XML document.
		 */
//		return super.getFileStartTime(file);
	}

}
