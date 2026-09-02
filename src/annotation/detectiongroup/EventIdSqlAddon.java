package annotation.detectiongroup;

import java.sql.Types;

import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.DetectionGroup;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLoggingAddon;
import generalDatabase.SQLTypes;

/**
 * SQL logging addon which adds an EventId column to a detection group's super
 * detection table. The event id is not used to match sub detections (that is
 * done through the standard sub table) but is retained for backwards
 * compatibility with the historic click event numbering.
 *
 * @author Jamie Macaulay
 */
public class EventIdSqlAddon implements SQLLoggingAddon {

	private PamTableItem eventId;

	private EventIdAnnotationType eventIdAnnotationType;

	public EventIdSqlAddon(EventIdAnnotationType eventIdAnnotationType) {
		super();
		this.eventIdAnnotationType = eventIdAnnotationType;
		eventId = new PamTableItem("EventId", Types.INTEGER);
	}

	@Override
	public void addTableItems(EmptyTableDefinition pamTableDefinition) {
		pamTableDefinition.addTableItem(eventId);
	}

	@Override
	public boolean saveData(SQLTypes sqlTypes, EmptyTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		EventIdAnnotation annotation = (EventIdAnnotation) pamDataUnit.findDataAnnotation(EventIdAnnotation.class);
		if (annotation == null) {
			// fall back on the database index so that every group always has a number.
			eventId.setValue(pamDataUnit.getDatabaseIndex());
		}
		else {
			eventId.setValue(annotation.getEventId());
		}
		return true;
	}

	@Override
	public boolean loadData(SQLTypes sqlTypes, EmptyTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		Integer id = (Integer) eventId.getValue();
		if (id != null) {
			if (pamDataUnit instanceof DetectionGroup) {
				((DetectionGroup) pamDataUnit).setEventId(id);
			}
			else {
				pamDataUnit.addDataAnnotation(new EventIdAnnotation(eventIdAnnotationType, id));
			}
		}
		return true;
	}

	@Override
	public String getName() {
		return eventIdAnnotationType.getAnnotationName();
	}

}
