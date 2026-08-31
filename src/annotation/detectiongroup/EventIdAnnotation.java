package annotation.detectiongroup;

import annotation.DataAnnotation;

/**
 * Annotation holding the event id for a detection group. Used by
 * {@link PamguardMVC.superdet.DetectionGroup} to carry the event id so that it
 * can be written to the database using standard annotation logging.
 *
 * @author Jamie Macaulay
 */
public class EventIdAnnotation extends DataAnnotation<EventIdAnnotationType> {

	private int eventId;

	public EventIdAnnotation(EventIdAnnotationType dataAnnotationType, int eventId) {
		super(dataAnnotationType);
		this.eventId = eventId;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	@Override
	public String toString() {
		return String.format("Event Id %d", eventId);
	}

}
