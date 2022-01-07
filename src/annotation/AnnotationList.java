package annotation;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;

public class AnnotationList {

	private Hashtable<String, DataAnnotationType<?>> typesList;
	
	public AnnotationList() {
		typesList = new Hashtable<>();
	}
	
	/**
	 * Add a data annotation type to the list
	 * @param annotationType annotation type
	 * @return true unless there was a code conflict. 
	 */
	public boolean addAnnotationType(DataAnnotationType<?> annotationType) {
		String code = annotationType.getShortIdCode();
		// see if it already exists in the list. 
		DataAnnotationType<?> existing = typesList.get(code);
		if (existing != null) {
			if (existing.getClass() == annotationType.getClass()) {
				// that's OK - the same one is being put in the list twice.
				// replace the existing though incase it's been updated somehow. 
				typesList.put(code,  annotationType);
				return true;
			}
			else {
				String msg = String.format("Annotation type code \"%s\" is already used by Annotation Type %s and " + 
			"cannot be used by %s\nThe programmer must change the Annotations short id code.", 
			code, existing.getAnnotationName(), annotationType.getAnnotationName());
				WarnOnce.showWarning(null, "Annotation type error", 
						msg, WarnOnce.WARNING_MESSAGE);
				return false;
			}
		}
		else {
			typesList.put(code,  annotationType);
			return true;
		}
	}
	
	/**
	 * Get a specified annotation type from it's code.  
	 * @param code 4 character code identifying the annotation
	 * @return DataannotationType or null
	 */
	public DataAnnotationType<?> findTypeFromCode(String code) {
		return typesList.get(code);
	}

	/**
	 * Find additional annotators that are annotating this particular datablock. 
	 * @param pamDataBlock
	 */
	public ArrayList<DataAnnotationType> findAnnotators(PamDataBlock pamDataBlock) {
		ArrayList<DataAnnotationType> annotList = new ArrayList<>();
		Enumeration<DataAnnotationType<?>> elems = typesList.elements();
		while (elems.hasMoreElements()) {
			DataAnnotationType el = elems.nextElement();
			if (el.isAnnotating(pamDataBlock)) {
				annotList.add(el);
			}
		}
		
		return annotList;
		
	}

}
