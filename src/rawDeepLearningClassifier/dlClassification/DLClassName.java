package rawDeepLearningClassifier.dlClassification;

import java.io.Serializable;

/**
 * A class name along with a unique identifier number. This allows class names to be
 * efficiently stored in data. 
 * 
 * @author Jamie Macaulay. 
 *
 */
public class DLClassName  implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The class name; 
	 */
	public String className;
	
	/**
	 * Unique ID number for the class name; 
	 */
	public short ID; 
	
	/**
	 * optional ITIS code. 
	 */
	public Integer itisCode;

	/**
	 * 
	 * @param className Class name. Can be a species name or call type. 
	 * @param iD id within the model
	 * @param itisCode ITIS species code from https://itis.gov/ (optional). Get's used by the
	 * Tethys module and saves users from having to manually enter all the codes. User can still 
	 * overwrite them.  
	 */
	public DLClassName(String className, short iD, Integer itisCode) {
		super();
		this.className = className;
		ID = iD;
		this.itisCode = itisCode;
	}


	/**
	 * The class name. 
	 * @param name - the name of the class. . 
	 * @param ID - the ID. 
	 */
	public DLClassName(String name, short ID) {
		this(name, ID, null);
	}
	

}
