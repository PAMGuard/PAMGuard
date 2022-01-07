package PamModel.parametermanager;

import java.lang.reflect.Field;

public abstract class FieldlessPamParameterData extends PamParameterData {

	private String fieldName;

	/**
	 * @param parentObject
	 * @param field
	 * @param shortName
	 * @param toolTip
	 */
	public FieldlessPamParameterData(Object parentObject, String fieldName, String shortName, String toolTip) {
		super(parentObject, null, shortName, toolTip);
		this.fieldName = fieldName;
	}

	/**
	 * @param parentObject
	 * @param field
	 */
	public FieldlessPamParameterData(Object parentObject, String fieldName) {
		super(parentObject, null);
		this.fieldName = fieldName;
	}

	/* (non-Javadoc)
	 * @see PamModel.parametermanager.PamParameterData#getFieldName()
	 */
	@Override
	public String getFieldName() {
		return fieldName;
	}

	/* (non-Javadoc)
	 * @see PamModel.parametermanager.PamParameterData#getDataClass()
	 */
	@Override
	public Class getDataClass() {
		return getParentObject().getClass();
	}

	/* (non-Javadoc)
	 * @see PamModel.parametermanager.PamParameterData#getField()
	 */
	@Override
	public Field getField() {
		// TODO Auto-generated method stub
		return super.getField();
	}

}
