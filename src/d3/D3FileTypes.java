package d3;

/**
 * Class that makes files of any type from 
 * a single starting point - strips of the bit
 * after the . and then returns all the 
 * oether ones. 
 * @author Doug Gillespie
 *
 */
public class D3FileTypes {

	private String baseName;

	public D3FileTypes(String baseName) {
		super();
		this.baseName = baseName;
		/**
		 * Now strip off everything after the '.'
		 */
		int lastDot = baseName.lastIndexOf('.');
		if (lastDot > 0) {
			this.baseName = baseName.substring(0, lastDot);
		}
		if (this.baseName.charAt(this.baseName.length()-1) != '.') {
			this.baseName += '.';
		}
	}

	public String getFileName(String endType) {
		// baseNAme already ends with a ., so make sure there isn't
		// one on endType
		if (endType.charAt(0) == '.') {
			return baseName + endType.substring(1);
		}
		else {
			return baseName + endType;
		}
	}

}
