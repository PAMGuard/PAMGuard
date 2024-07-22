package tethys.niluswraps;

import PamguardMVC.PamDataBlock;
import tethys.dbxml.DBXMLConnect;

public class NilusDocumentWrapper<T> {

	public T nilusObject;
	
	public String documentName;
	
	public NilusDocumentWrapper(T nilusDocument) {
		super();
		this.nilusObject = nilusDocument;
	}
	
	public String getDocumentId() {
		if (nilusObject == null) {
			return null;
		}
		return DBXMLConnect.getDocumentId(nilusObject);
	}
	
}
