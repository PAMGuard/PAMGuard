package PamView;

import java.awt.Component;

import PamView.dialog.IconPanel;

public class SymbolKeyItem implements PamKeyItem {

	private PamSymbol pamSymbol;
	
	private String text;
	
	public SymbolKeyItem(PamSymbol pamSymbol, String text) {
		super();
		this.pamSymbol = pamSymbol;
		this.text = text;
	}

	public Component getIcon(int keyType, int nComponent) {
//		JPanel p = new JPanel();
//		p.setPreferredSize(new Dimension(pamSymbol.getIconWidth(), pamSymbol.getIconHeight()));
//		pamSymbol.paintIcon(p, p.getGraphics(), pamSymbol.getIconWidth()/2, pamSymbol.getIconHeight()/2);
//		return p;
		return new IconPanel(pamSymbol);
	}

	public int getNumItems(int keyType) {
		return 1;
	}

	public String getText(int keyType, int nComponent) {
		// TODO Auto-generated method stub
		return text;
	}

}
