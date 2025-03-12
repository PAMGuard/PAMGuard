package annotation;

import annotation.calcs.snr.SNRAnnotationType;
import annotation.calcs.spl.SPLAnnotationType;
import annotation.calcs.wav.WavAnnotationType;
import annotation.dummy.DummyAnnotationType;
import annotation.localise.targetmotion.TMAnnotationType;
import annotation.userforms.UserFormAnnotationType;

/**
 * Singleton centralised annotations types list. 
 * @author Doug Gillespie
 *
 */
public class CentralAnnotationsList {

	private volatile static AnnotationList singleInstance = null;
	
	private static DummyAnnotationType dummyAnnotationType = new DummyAnnotationType();

	/**
	 * 
	 * @return centralised annotations list. 
	 */
	public static AnnotationList getList() {
		if (singleInstance == null) {
			synchronized (CentralAnnotationsList.class) {
				// use double locking as per p182 of design patterns book
				if (singleInstance == null) {
					singleInstance = new AnnotationList();
					singleInstance.addAnnotationType(new SNRAnnotationType());
					singleInstance.addAnnotationType(new SPLAnnotationType());
					singleInstance.addAnnotationType(new TMAnnotationType());
					singleInstance.addAnnotationType(new UserFormAnnotationType(null));
					singleInstance.addAnnotationType(new WavAnnotationType());
				}
			}
		}
		return singleInstance;
	}

	/**
	 * convenience wrapper round underlying singleton functions. 
	 * Add a data annotation type to the list
	 * @param annotationType annotation type
	 * @return true unless there was a code conflict. 
	 */
	public static boolean addAnnotationType(DataAnnotationType<?> annotationType) {
		return getList().addAnnotationType(annotationType);
	}
	
	/**
	 * Convenience method to get a type more easily.
	 * @param code 4 character code identifying the annotation
	 * @return DataannotationType or null
	 */
	public static DataAnnotationType<?> findTypeFromCode(String code) {
		return getList().findTypeFromCode(code);
	}

	/**
	 * @return the dummyAnnotationType
	 */
	public static DummyAnnotationType getDummyAnnotationType() {
		return dummyAnnotationType;
	}

}
