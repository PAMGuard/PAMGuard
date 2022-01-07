package generalDatabase.lookupTables;

import java.awt.Color;
import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamView.PamSymbol;
import PamView.PamSymbolType;

/**
 * hold data for a single item from the lookup list. 
 * @author Doug Gillespie
 *
 */
public class LookupItem implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	private int databaseId;

	private int order;

	private String topic; 

	private String code;

	private String text;

	private boolean selectable;

	private Color borderColour;

	private Color fillColour;

	private String symbolType;

	private int resultSetRow;

	/**
	 * @param databaseId
	 * @param topic
	 * @param order
	 * @param code
	 * @param text
	 * @param selectable
	 * @param fillCol
	 * @param borderCol
	 * @param symbolType
	 */
	public LookupItem(int databaseId, int row, String topic, int order, String code,
			String text, boolean selectable, Color fillCol, Color borderCol, String symbolType) {
		super();
		this.databaseId = databaseId;
		this.resultSetRow = row;
		this.topic = topic;
		this.order = order;
		this.code = code;
		this.text = text;
		this.selectable = selectable;
		this.borderColour = borderCol;
		this.fillColour = fillCol;
		this.symbolType = symbolType;
	}

	/**
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * @return the topic
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * @param topic the topic to set
	 */
	public void setTopic(String topic) {
		this.topic = topic;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the selectable
	 */
	public boolean isSelectable() {
		return selectable;
	}

	/**
	 * @param selectable the selectable to set
	 */
	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

	/**
	 * @return the colour
	 */
	public Color getBorderColour() {
		return borderColour;
	}

	/**
	 * @param colour the colour to set
	 */
	public void setBorderColour(Color colour) {
		this.borderColour = colour;
	}

	/**
	 * @return the colour
	 */
	public Color getFillColour() {
		return fillColour;
	}

	/**
	 * @param colour the colour to set
	 */
	public void setFillColour(Color colour) {
		this.fillColour = colour;
	}

	/**
	 * @param databaseId the databaseId to set
	 */
	public void setDatabaseId(int databaseId) {
		this.databaseId = databaseId;
	}

	/**
	 * @return the databaseId
	 */
	public int getDatabaseId() {
		return databaseId;
	}

	/**
	 * @return the symbolType
	 */
	public String getSymbolType() {
		return symbolType;
	}

	@Override
	public String toString() {
		//		return String.format("Lookup Item row %d, topic %s, Code %s, Text %s", resultSetRow, topic, code, text);
		return String.format("%s; %s", code, text);
	}

	/**
	 * @param symbolType the symbolType to set
	 */
	public void setSymbolType(String symbolType) {
		this.symbolType = symbolType;
	}

	public PamSymbol getSymbol() {
		PamSymbolType symbolType = PamSymbol.interpretTextCode(this.symbolType);
		if (symbolType == null) {
			return null;
		}
		if (borderColour == null) {
			borderColour = Color.black;
		}
		if (fillColour == null) {
			fillColour = Color.black;
		}
		return new PamSymbol(symbolType,10, 10, true, fillColour, borderColour);
	}

	/**
	 * @return the resultSetRow
	 */
	public int getResultSetRow() {
		return resultSetRow;
	}

	/**
	 * Check the item has at least something in code and something in text.
	 * @return true if OK. 
	 */
	public boolean checkItem() {
		if (code == null || text == null) {
			return false;
		}
		if (code.length() == 0 || text.length() == 0) {
			return false;
		}
		return true;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
