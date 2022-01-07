package PamView;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.Icon;

import PamView.dialog.IconPanel;

public class BasicKeyItem implements PamKeyItem {

	ArrayList<Icon> icons;
	ArrayList<String> texts;
	ArrayList<Component> components;

	public BasicKeyItem() {
		
	}
	
	public BasicKeyItem(Icon icon, String text) {
		addIcon(icon, text);
	}
	
	public void addIcon(Icon icon, String text) {
		if (setupArrays(true) == false) return;
		icons.add(icon);
		texts.add(text);
	}
	
	public BasicKeyItem(Component component, String text) {
		addIcon(component, text);
	}
	
	public void addIcon(Component component, String text) {
		if (setupArrays(false) == false) return;
		components.add(component);
		texts.add(text);
	}
	
	private boolean setupArrays(boolean isIcons) {
		if (texts == null) {
			// nothing set up yet, so just get on with it.
			texts = new ArrayList<String>();
			if (isIcons) {
				icons = new ArrayList<Icon>();
			}
			else {
				components = new ArrayList<Component>();
			}
			return true;
		}
		else if (icons == null && isIcons) {
			System.out.println("You cannot add icons to a BasicKeyITem configured for awt.Components");
			return false;
		}
		else if (components == null && !isIcons) {
			System.out.println("You cannot add components to a BasicKeyItem configured for Icons");	
			return false;
		}
		return true;
	}

	public Component getIcon(int keyType, int nComponent) {
		if (texts == null) return null;
		if (nComponent >= 0 && nComponent < texts.size()) {
			if (icons != null) {
				return new IconPanel(icons.get(nComponent));
			}
			else {
				return components.get(nComponent);
			}
		}
		return null;
	}

	public int getNumItems(int keyType) {
		return texts.size();
	}

	public String getText(int keyType, int nComponent) {
		if (nComponent >= 0 && nComponent < texts.size()) {
			return texts.get(nComponent);
		}
		return null;
	}

}
