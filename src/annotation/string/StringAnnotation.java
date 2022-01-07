package annotation.string;

import annotation.DataAnnotation;
import annotation.DataAnnotationType;

public class StringAnnotation extends DataAnnotation {

	private String string; 
	
	public StringAnnotation(DataAnnotationType dataAnnotationType) {
		super(dataAnnotationType);
	}

	/**
	 * @return the string
	 */
	public String getString() {
		return string;
	}

	/**
	 * @param string the string to set
	 */
	public void setString(String string) {
		this.string = string;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return string;
	}

}
