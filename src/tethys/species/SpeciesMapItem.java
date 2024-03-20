package tethys.species;

import java.io.Serializable;

/**
 * Information linking a name within a PAMGuard datablock to a ITIS species code and call type. 
 * Also contains common and latin names for display purposes, though these are not essential. 
 * @author dg50
 *
 */
public class SpeciesMapItem implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;

	/**
	 * ITIS code. May be a real ITIS code or one of the -ve species that we're defining ourselves. 
	 */
	private int itisCode;
	
	/**
	 * Species code that was used within PAMGuard. Redundant information, but useful 
	 * for bookkeeping. 
	 */
	private String pamguardName;
	
	/**
	 * Latin name extracted from ITIS database. Can be null if unknown. 
	 * Probably not written to Tethys. 
	 */
	private String latinName;
	
	/**
	 * Common species name from ITIS database. Can be null if unknown. 
	 * Probably not written to Tethys. 
	 */
	private String commonName;
	
	/**
	 * Type of call. Descriptive name for type of sound, e.g. 'click', 'whistle', 'D-call',
	 * etc. to complement the itis species code. Will be written to Tethys.  
	 */
	private String callType;

	/**
	 * 
	 * @param itisCode
	 * @param callType
	 * @param pamguardName
	 * @param latinName
	 * @param commonName
	 */
	public SpeciesMapItem(int itisCode, String callType, String pamguardName, String latinName, String commonName) {
		super();
		this.itisCode = itisCode;
		this.callType = callType;
		this.pamguardName = pamguardName;
		this.latinName = latinName;
		this.commonName = commonName;
	}

	/**
	 * 
	 * @param itisCode
	 * @param callType
	 * @param pamguardName
	 */
	public SpeciesMapItem(int itisCode, String callType, String pamguardName) {
		super();
		this.itisCode = itisCode;
		this.callType = callType;
		this.pamguardName = pamguardName;
		this.latinName = null;
		this.commonName = null;
	}

	/**
	 * ITIS code. May be a real ITIS code or one of the -ve species that we're defining ourselves. 
	 * @return the itisCode
	 */
	public int getItisCode() {
		return itisCode;
	}

	/**
	 * Species code that was used within PAMGuard. Redundant information, but useful 
	 * @return the pamguardName
	 */
	public String getPamguardName() {
		return pamguardName;
	}

	/**
	 * Latin name extracted from ITIS database. Can be null if unknown. 
	 * @return the latinName
	 */
	public String getLatinName() {
		return latinName;
	}

	/**
	 * Common species name from ITIS database. Can be null if unknown. 
	 * @return the commonName
	 */
	public String getCommonName() {
		return commonName;
	}

	/**
	 * Type of call. Descriptive name for type of sound, e.g. 'click', 'whistle', 'D-call',
	 * @return the callType
	 */
	public String getCallType() {
		return callType;
	}
	
}
