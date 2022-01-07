package qa.generator;

import java.io.Serializable;

import PamUtils.LatLong;

public class ReceiverReference extends LatLong implements Serializable{

	public static final long serialVersionUID = 1L;
	
	private String referenceType;

	public ReceiverReference(LatLong receiverLatLong, String referenceType) {
		super(receiverLatLong);
		this.setReferenceType(referenceType);
	}

	/**
	 * @return the referenceType
	 */
	public String getReferenceType() {
		return referenceType;
	}

	/**
	 * @param referenceType the referenceType to set
	 */
	public void setReferenceType(String referenceType) {
		this.referenceType = referenceType;
	}

}
