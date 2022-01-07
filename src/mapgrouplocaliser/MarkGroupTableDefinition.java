package mapgrouplocaliser;

import java.sql.Types;

import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;

public class MarkGroupTableDefinition extends PamTableDefinition {
	
	private PamTableItem markType, outlineX, outlineY, centX, centY;

	public MarkGroupTableDefinition(String tableName, int updatePolicy) {
		super(tableName, updatePolicy);
		// add other fields here - really may not be many since most of this 
		// will be handled with optional  / selected annotations
		addTableItem(markType = new PamTableItem("MarkType", Types.CHAR, 20));
		addTableItem(centX = new PamTableItem("CentralX", Types.REAL));
		addTableItem(centY = new PamTableItem("CentralY", Types.REAL));
		addTableItem(outlineX = new PamTableItem("OutlineX", Types.CHAR, 80));
		addTableItem(outlineY = new PamTableItem("OutlineY", Types.CHAR, 80));
	}

	/**
	 * @return the markType
	 */
	public PamTableItem getMarkType() {
		return markType;
	}

	/**
	 * @return the centX
	 */
	public PamTableItem getCentX() {
		return centX;
	}

	/**
	 * @return the centY
	 */
	public PamTableItem getCentY() {
		return centY;
	}

	/**
	 * @return the outlineX
	 */
	public PamTableItem getOutlineX() {
		return outlineX;
	}

	/**
	 * @return the outlineY
	 */
	public PamTableItem getOutlineY() {
		return outlineY;
	}

}
