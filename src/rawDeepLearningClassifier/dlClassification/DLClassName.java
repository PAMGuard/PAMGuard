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
	 * The class name. 
	 * @param name - the name of the class. . 
	 * @param ID - the ID. 
	 */
	public DLClassName(String name, short ID) {
		this.className = name; 
		this.ID=ID; 
	}
	

}
