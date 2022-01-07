package PamView;

import java.awt.Component;

public class TextKeyItem implements PamKeyItem {

	String text;
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public TextKeyItem(String text) {
		super();
		this.text = text;
	}

	public Component getIcon(int keyType, int nComponent) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNumItems(int keyType) {
		// TODO Auto-generated method stub
		return 1;
	}

	public String getText(int keyType, int nComponent) {
		return text;
	}

}
