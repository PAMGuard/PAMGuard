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

	@Override
	public Component getIcon(int keyType, int nComponent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumItems(int keyType) {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public String getText(int keyType, int nComponent) {
		return text;
	}

}
