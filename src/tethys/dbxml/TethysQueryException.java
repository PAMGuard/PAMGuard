package tethys.dbxml;

public class TethysQueryException extends TethysException {


	private static final long serialVersionUID = 1L;

	private String queryString;

	public TethysQueryException(String message, String queryString) {
		super(message, null);
		this.queryString = queryString;
	}

	public String getQueryString() {
		return queryString;
	}

}
