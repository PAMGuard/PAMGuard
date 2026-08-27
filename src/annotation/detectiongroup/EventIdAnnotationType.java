package annotation.detectiongroup;

import PamguardMVC.superdet.SuperDetection;
import annotation.CentralAnnotationsList;
import annotation.DataAnnotationType;
import generalDatabase.SQLLoggingAddon;

/**
 * Annotation type for event ids on detection groups. This is a mandatory
 * annotation for {@link PamguardMVC.superdet.DetectionGroup} data units - it is
 * never offered to the user in annotation selection dialogs, but is wired
 * directly into the group's database logging in the same way that the deep
 * learning classifier wires its annotations.
 *
 * @author Jamie Macaulay
 */
public class EventIdAnnotationType extends DataAnnotationType<EventIdAnnotation> {

	private static EventIdAnnotationType singleInstance;

	private EventIdSqlAddon eventIdSqlAddon;

	public EventIdAnnotationType() {
		super();
		eventIdSqlAddon = new EventIdSqlAddon(this);
		CentralAnnotationsList.addAnnotationType(this);
	}

	/**
	 * Get a shared instance of the annotation type.
	 * @return shared instance.
	 */
	public static synchronized EventIdAnnotationType getInstance() {
		if (singleInstance == null) {
			singleInstance = new EventIdAnnotationType();
		}
		return singleInstance;
	}

	@Override
	public String getAnnotationName() {
		return "Event Id";
	}

	@Override
	public Class getAnnotationClass() {
		return EventIdAnnotation.class;
	}

	@Override
	public boolean canAnnotate(Class dataUnitType) {
		return dataUnitType == null || SuperDetection.class.isAssignableFrom(dataUnitType);
	}

	@Override
	public SQLLoggingAddon getSQLLoggingAddon() {
		return eventIdSqlAddon;
	}

}
