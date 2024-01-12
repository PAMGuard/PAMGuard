package tethys.species;


/**
 * Class to hold and unpack a XML string returned from the ITIS_ranks document
 * in a Tethys database. 
 * @author dg50
 *
 */
public class TethysITISResult {

	private int itisCode;
	private String taxon_unit;
	private String latin;
	private String vernacular;

	/**
	 * Construct a ITIS object from XML data
	 * @param itisCode 
	 * @param xmlData
	 * @param vernacular 
	 * @param latin 
	 */
	public TethysITISResult(int itisCode, String taxon_unit, String latin, String vernacular) {

		this.itisCode = itisCode;
		this.taxon_unit = taxon_unit;
		this.latin = latin;
		this.vernacular = vernacular;
		
	}

	/**
	 * @return the itisCode
	 */
	public int getItisCode() {
		return itisCode;
	}

	/**
	 * @return the taxon_unit
	 */
	public String getTaxon_unit() {
		return taxon_unit;
	}

	/**
	 * @return the latin
	 */
	public String getLatin() {
		return latin;
	}

	/**
	 * @return the vernacular
	 */
	public String getVernacular() {
		return vernacular;
	}
}
