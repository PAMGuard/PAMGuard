package annotation.string;

import generalDatabase.SQLLoggingAddon;
import PamView.dialog.PamDialogPanel;
import annotation.AnnotationDialogPanel;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import annotation.handler.AnnotationOptions;

/**
 * Simple annotation type that does nothing by add a string text to 
 * a datat unit. 
 * @author Doug Gillespie
 *
 */
public class StringAnnotationType extends DataAnnotationType<StringAnnotation> {

	private String annotationName;
	private StringSQLLogging sqlAddon;
	private StringDialogPanel stringDialogPanel;
	private StringAnnotationOptions stringAnnotationOptions;

	public StringAnnotationType(String annotationName, int maxLength) {
		super();
		this.annotationName = annotationName;
		stringAnnotationOptions = new StringAnnotationOptions(annotationName);
		stringAnnotationOptions.setMaxLength(maxLength);
		sqlAddon = new StringSQLLogging(this);
		stringDialogPanel = new StringDialogPanel(this);
	}

	@Override
	public String getAnnotationName() {
		return annotationName;
	}

	@Override
	public boolean canAnnotate(Class dataUnitType) {
		return true; // can add this to absolutely anything !
	}



	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#canAutoAnnotate()
	 */
	@Override
	public boolean canAutoAnnotate() {
		return false;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getSQLLoggingAddon()
	 */
	@Override
	public SQLLoggingAddon getSQLLoggingAddon() {
		return sqlAddon;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getDialogPanel()
	 */
	@Override
	public AnnotationDialogPanel getDialogPanel() {
		return stringDialogPanel;
	}

	/**
	 * @return the maxLength
	 */
	public int getMaxLength() {
		return stringAnnotationOptions.getMaxLength();
	}

	@Override
	public AnnotationOptions getAnnotationOptions() {
		return stringAnnotationOptions;
	}

	@Override
	public void setAnnotationOptions(AnnotationOptions annotationOptions) {
		if (annotationOptions != null && StringAnnotationOptions.class.isAssignableFrom(annotationOptions.getClass())) {
			this.stringAnnotationOptions = (StringAnnotationOptions) annotationOptions;
		}
	}

	@Override
	public Class getAnnotationClass() {
		return StringAnnotation.class;
	}	
}
