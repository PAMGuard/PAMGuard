package annotation.handler;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;

/**
 * Class to handle choice of annotator and any other 
 * associated options.  This can be used to control which 
 * annotation(s) is/are added to data and options controlling
 * how the annotations work. 
 * @author Doug Gillespie
 *
 */
public class AnnotationChoices implements Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	private Hashtable<String, AnnotationOptions> annotationOptions = new Hashtable<>();
	
	private boolean allowMany = true;

	/**
	 * Names of the annotation types shown in the list the user selects from, for handlers
	 * which let the user add types to the list (see
	 * {@link AnnotationChoiceHandler#isEditableList()}). Null until the user first edits the
	 * list, in which case the handler's defaults are listed.
	 */
	private ArrayList<String> listedAnnotations;

	/**
	 * Is an annotation type shown in the list the user selects from ?
	 * @param annotationName annotation type name.
	 * @param defaultListed true if the type is listed when the user hasn't edited the list.
	 * @return true if listed.
	 */
	public boolean isListed(String annotationName, boolean defaultListed) {
		if (listedAnnotations == null) {
			return defaultListed;
		}
		return listedAnnotations.contains(annotationName);
	}

	/**
	 * @return names of the listed annotation types, or null if the list has never been edited.
	 */
	public ArrayList<String> getListedAnnotations() {
		return listedAnnotations;
	}

	/**
	 * @param listedAnnotations names of the annotation types to show in the list.
	 */
	public void setListedAnnotations(Collection<String> listedAnnotations) {
		this.listedAnnotations = listedAnnotations == null ? null : new ArrayList<>(listedAnnotations);
	}

	/**
	 * Set the options for an annotation type
	 * @param annotationName annotation type name
	 * @param annotationOptions options
	 * @param setSelected set the selected option true or false. 
	 */
	public void setAnnotionOption(String annotationName, AnnotationOptions annotationOptions, boolean setSelected) {
		annotationOptions.setSelected(setSelected);
		setAnnotionOption(annotationName, annotationOptions);
	}
	/**
	 * Set the options for an annotation type
	 * @param annotationName annotation type name
	 * @param annotationOptions options
	 */
	public void setAnnotionOption(String annotationName, AnnotationOptions annotationOption) {
		this.annotationOptions.put(annotationName, annotationOption);
		if (isAllowMany() == false && annotationOption.isIsSelected()) {
			// deselect all the others. 
			Collection<AnnotationOptions> values = this.annotationOptions.values();
			Iterator<AnnotationOptions> it = values.iterator();
			while (it.hasNext()) {
				AnnotationOptions anOpt = it.next();
				if (anOpt == annotationOption) continue;
				anOpt.setSelected(false);
			}
		}
	}
	
	/**
	 * Get an annotation options, or null if nothin in list. 
	 * @param annotationName annotation type name
	 * @return annotation options. 
	 */
	public AnnotationOptions getAnnotationOptions(String annotationName) {
		return annotationOptions.get(annotationName);
	}
	/**
	 * @return the allowMany
	 */
	public boolean isAllowMany() {
		return true;
	}
	/**
	 * @param allowMany the allowMany to set
	 */
	public void setAllowMany(boolean allowMany) {
		this.allowMany = allowMany;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("annotationOptions");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return annotationOptions;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
