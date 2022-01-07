package PamView;

import java.awt.Color;
import java.awt.Component;

import PamView.dialog.IconPanel;

public class LineKeyItem implements PamKeyItem {

	private Color color;
	
	private String text;
	
	private PamSymbol symbol;
	
	public LineKeyItem(Color color, String text) {
		super();
		this.color = color;
		this.text = text;
		symbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 12, 1, true, color, color);
		symbol.setIconStyle(PamSymbol.ICON_STYLE_LINE);
	}

	@Override
	public Component getIcon(int keyType, int component) {
		return new IconPanel(symbol);
	}

	@Override
	public int getNumItems(int keyType) {
		return 1;
	}

	@Override
	public String getText(int keyType, int component) {
		return text;
	}

}
