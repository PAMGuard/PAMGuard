package loggerForms;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import PamUtils.XMLUtils;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;

public class FormsBinaryIO extends BinaryDataSource {

	private FormsDataBlock formsDataBlock;
	private FormsControl formsControl;
	
	private final int versionId = 1;

	public FormsBinaryIO(FormsControl formsControl, FormsDataBlock formsDataBlock) {
		super(formsDataBlock, false);
		this.formsControl = formsControl;
		this.formsDataBlock = formsDataBlock;
	}

	@Override
	public String getStreamName() {
		return formsDataBlock.getDataName();
	}

	@Override
	public int getStreamVersion() {
		return versionId;
	}

	@Override
	public int getModuleVersion() {
		return 0;
	}

	@Override
	public byte[] getModuleHeaderData() {
		return null;
	}

	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData,
			BinaryHeader bh, int moduleVersion) {
		String xmlString = new String(binaryObjectData.getData());
		Document doc = XMLUtils.getDocumentFromString(xmlString);
		/**
		 * Now have to get some data from the doc to identify the form.
		 */
		Element root = doc.getDocumentElement();
		if (root == null) {
			return null;
		}
//		root.get
//		NodeList info = root.getElementsByTagName("INFO");
//		if (info == null || info.getLength() == 0 ){
//			return -2;
//		}
		Integer v = XMLUtils.getIntegerValue(root, "Version");
		String formName = root.getAttribute("FormName");
		FormDescription formDesc = formsControl.findFormDescription(formName);
		if (formDesc == null) {
			return null;
		}
		return formDesc.createDataFromXML(doc);
		
	}

	@Override
	public ModuleHeader sinkModuleHeader(BinaryObjectData binaryObjectData,
			BinaryHeader bh) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ModuleFooter sinkModuleFooter(BinaryObjectData binaryObjectData,
			BinaryHeader bh, ModuleHeader moduleHeader) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		/**
		 * Pack the data from the data unit as xml, then write the xml text as
		 * a string object. 
		 * The packing is done within the data unit. 
		 */
		FormsDataUnit formsDataUnit = (FormsDataUnit) pamDataUnit;
		String xmlString = formsDataUnit.getFormDescription().getXMLData(formsDataUnit);
		if (xmlString == null) {
			return null;
		}
		return new BinaryObjectData(1, xmlString.getBytes());
	}

	@Override
	public void newFileOpened(File outputFile) {
		// TODO Auto-generated method stub
		
	}



}
