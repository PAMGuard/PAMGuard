package Array.sensors;

import PamController.PamController;

public enum ArraySensorFieldType {
	
	HEIGHT, HEADING, PITCH, ROLL;

	@Override
	public String toString() {
		switch (this) {
		case HEIGHT:
			return PamController.getInstance().getGlobalMediumManager().getZString();
		case HEADING:
			return "True Heading";
		case PITCH:
			return "Pitch";
		case ROLL:
			return "Roll";
		default:
			break;
		
		}
		return super.toString();
	}
	
}
