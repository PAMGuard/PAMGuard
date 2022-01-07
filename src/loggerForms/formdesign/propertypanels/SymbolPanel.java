package loggerForms.formdesign.propertypanels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import PamView.PamColors;
import PamView.PamSymbol;
import PamView.PamSymbolSelector;
import PamView.PamSymbolType;
import loggerForms.ItemInformation;
import loggerForms.PropertyTypes;
import loggerForms.UDColName;
import loggerForms.formdesign.FormEditor;

public class SymbolPanel extends PropertyPanel {

	private PamSymbolSelector symbolSelector;
	
	public SymbolPanel(FormEditor formEditor, PropertyTypes propertyType) {
		super(formEditor, propertyType);
		addItem(symbolSelector = new PamSymbolSelector(null));
	}


	@Override
	public void propertyEnable(boolean enabled) {
		symbolSelector.setEnabled(getUseProperty().isSelected());
	}


	/* (non-Javadoc)
	 * @see loggerForms.formdesign.propertypanels.PropertyPanel#pushProperty(loggerForms.ItemInformation)
	 */
	@Override
	public void pushProperty(ItemInformation itemInformation) {
		super.pushProperty(itemInformation);
		if (itemInformation == null) {
			return;
		}
		PamSymbol currentSymbol = createSymbol(itemInformation);
		symbolSelector.setCurrentSymbol(currentSymbol);
	}


	/* (non-Javadoc)
	 * @see loggerForms.formdesign.propertypanels.PropertyPanel#fetchProperty(loggerForms.ItemInformation)
	 */
	@Override
	public ItemInformation fetchProperty(ItemInformation itemInformation) {
		ItemInformation itemInfo = super.fetchProperty(itemInformation);
		if (itemInfo == null) {
			return null;
		}
		PamSymbol selSymbol = symbolSelector.getCurrentSymbol();
		if (selSymbol == null) {
			return null;
		}
		char textCode = PamSymbol.getTextCode(selSymbol.getSymbol());
		String str = (new Character(textCode)).toString();
		itemInfo.setProperty(UDColName.Title.toString(), str);
		itemInfo.setProperty(UDColName.Plot.toString(), new Boolean(selSymbol.isFill()));
		itemInfo.setProperty(UDColName.Colour.toString(), PamColors.getLoggerColourString(selSymbol.getFillColor()));
		itemInfo.setProperty(UDColName.Topic.toString(), PamColors.getLoggerColourString(selSymbol.getLineColor()));
		itemInfo.setProperty(UDColName.Length.toString(), new Integer(selSymbol.getWidth()));
		itemInfo.setProperty(UDColName.Height.toString(), new Integer(selSymbol.getHeight()));
		
		return itemInfo;
	}
	
	/**
	 * Create a PAMSymbol from a Symbol item informatoin. 
	 * @param itemInformation Item information (a row from a UDF table)
	 * @return a PamSymbol 
	 */
	public static PamSymbol createSymbol(ItemInformation itemInformation) {
		if (itemInformation == null) {
			return null;
		}
		
		String type = itemInformation.getStringProperty(UDColName.Title.toString());
		PamSymbolType symbolType = PamSymbol.interpretTextCode(type);
		String colString = itemInformation.getStringProperty(UDColName.Colour.toString());
		Color fillColour = PamColors.interpretColourString(colString);
		if (fillColour == null) {
			fillColour = Color.BLACK;
		}
		colString = itemInformation.getStringProperty(UDColName.Topic.toString());
		Color lineColour = PamColors.interpretColourString(colString);
		if (lineColour == null) {
			lineColour = fillColour;
		}
		
		Boolean isFill = itemInformation.getBooleanProperty(UDColName.Plot.toString());
		if (isFill == null) {
			isFill = false;
		}
		Integer width = itemInformation.getIntegerProperty(UDColName.Length.toString());
		if (width == null) {
			width = 16;
		}
		Integer height = itemInformation.getIntegerProperty(UDColName.Height.toString());
		if (height == null) {
			height = width;
		}
		
		return new PamSymbol(symbolType, width, height, isFill, fillColour, lineColour);
	}

}
