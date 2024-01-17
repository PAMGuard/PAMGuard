package tethys.dbxml;

public class TethysException extends Exception {

	private static final long serialVersionUID = 1L;

	private String xmlError;

	public TethysException(String message, String xmlError) {
		super(message);
		this.xmlError = xmlError;
	}

	public String getXmlError() {
		return xmlError;
	}

}
