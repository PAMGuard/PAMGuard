package annotation.localise.targetmotion;

import Localiser.detectionGroupLocaliser.GroupLocalisation;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;

public class TMAnnotation extends DataAnnotation<DataAnnotationType> {

	private GroupLocalisation groupLocalisation;

	public TMAnnotation(DataAnnotationType dataAnnotationType, GroupLocalisation groupLocalisation) {
		super(dataAnnotationType);
		this.setGroupLocalisation(groupLocalisation);
	}

	/**
	 * @return the groupLocalisation
	 */
	public GroupLocalisation getGroupLocalisation() {
		return groupLocalisation;
	}

	/**
	 * @param groupLocalisation the groupLocalisation to set
	 */
	public void setGroupLocalisation(GroupLocalisation groupLocalisation) {
		this.groupLocalisation = groupLocalisation;
	}

	@Override
	public String toString() {
		if (groupLocalisation == null) {
			return "TMAnotation: No localisation information";
		}
		else {
			return groupLocalisation.toString();
		}
	}

}
