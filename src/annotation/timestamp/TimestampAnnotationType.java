package annotation.timestamp;

import generalDatabase.SQLLoggingAddon;
import PamView.dialog.PamDialogPanel;
import annotation.AnnotationDialogPanel;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;

/**
 * Simple annotation type that does nothing by add a string text to 
 * a datat unit. 
 * @author Doug Gillespie
 *
 */
public class TimestampAnnotationType extends DataAnnotationType<TimestampAnnotation> {

	private String annotationName;
	private TimestampSQLLogging sqlAddon;
//	private TimestampDialogPanel stringDialogPanel;

	public TimestampAnnotationType(String annotationName) {
		super();
		this.annotationName = annotationName;
		sqlAddon = new TimestampSQLLogging(this);
//		stringDialogPanel = new TimestampDialogPanel(this);
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
//		return stringDialogPanel;
		return null;
	}

	@Override
	public Class getAnnotationClass() {
		return TimestampAnnotation.class;
	}	

}
