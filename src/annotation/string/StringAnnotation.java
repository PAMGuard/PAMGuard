package annotation.string;

import annotation.DataAnnotation;
import annotation.DataAnnotationType;

public class StringAnnotation<T extends DataAnnotationType<?>> extends DataAnnotation<T> {

	private String string; 
	
	public StringAnnotation(T dataAnnotationType) {
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
