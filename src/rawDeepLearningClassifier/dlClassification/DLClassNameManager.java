package rawDeepLearningClassifier.dlClassification;

import java.util.Optional;

import rawDeepLearningClassifier.DLControl;

/**
 * Manages assigning class names and unique identifier numbers for each class type.
 * @author Jamie Macaulay
 *
 */
public class DLClassNameManager {
	
	private DLControl dlControl;


	/**
	 * Constructor for the class name mamanger. 
	 * @param dlControl - the dl control. 
	 */
	public DLClassNameManager(DLControl dlControl){
		this.dlControl = dlControl; 
	}

	/**
	 * Creates an array of DLClassNames names from a an array of String class names.
	 * The DLClassName contains a unique ID number that is used for saving data.
	 * 
	 * @param dlParams - the
	 * @return a corresponding array of class names.
	 */
	public DLClassName[] makeClassNames(String[] classNames) {
		DLClassName[] dlClassNames = new DLClassName[classNames.length]; 
		int n=0; 
		for (String className: classNames) {
			dlClassNames[n] =  makeClassNames(className);
			n++; 
		}
		return dlClassNames; 
	}
	
	/**
	 * Get a classname from a class name string. Applies a unique identifier number
	 * for a class name.
	 * 
	 * @param className - the class name string.
	 * @return a corresponding array of class names.
	 */
	public DLClassName makeClassNames(String className) {
		//first check if the sname already exists. 
		Optional<DLClassName> hasClassName = dlControl.getDLParams().classNameMap
				.stream().filter(n -> className.equals(n.className)).findAny(); 
		
		//already have this class name.
		if (!hasClassName.isEmpty()) return hasClassName.get();
		
		DLClassName dLClassName = new DLClassName(className, dlControl.getDLParams().classNameIndex);
		
		
		dlControl.getDLParams().classNameMap.add(dLClassName); 
		//increase the unique identifier number. 
		dlControl.getDLParams().classNameIndex++;
		
		return dLClassName; 
	}

	/**
	 * Get the class name. 
	 * @param iD - the unique identifier number. 
	 * @return - the name of the class. 
	 */
	public String getClassName(short iD) {
		
//		System.out.println("ID in manager 1: " + dlControl.getDLParams().classNameMap.size()); 
		
		//first check if the sname already exists. 
		Optional<DLClassName> hasClassName = dlControl.getDLParams().classNameMap
						.stream().filter(n -> iD == n.ID).findAny(); 
//		
//		for (int i=0; i<dlControl.getDLParams().classNameMap.size(); i++) {
//			System.out.println("ID in manager 2: " + dlControl.getDLParams().classNameMap.get(i).ID); 
//		}
		
		if (hasClassName.isEmpty()) return null; 
		
		return hasClassName.get().className; 
	}


}
