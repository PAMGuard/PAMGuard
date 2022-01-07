package PamModel.parametermanager;

public class FieldNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public FieldNotFoundException(String fieldName) {
		super(fieldName + "Not Found in Parameter Set");
	}
	
	

}
