package tethys.pamdata;

import org.w3c.dom.Document;

/**
 * object for a Tethys Schema. This may just be a very simple 
 * wrapper around an XML string, or a JAXB object or something, 
 * but may get more sophisticated. TBD in discussions with SDSU
 */
@Deprecated
public class TethysSchema {

	private Document schemaDoc;

	public TethysSchema(Document doc) {
		this.setXsd(doc);
	}

	/**
	 * @return the xsd
	 */
	public Document getXsd() {
		return schemaDoc;
	}

	/**
	 * @param xsd the xsd to set
	 */
	public void setXsd(Document xsd) {
		this.schemaDoc = xsd;
	}

}
