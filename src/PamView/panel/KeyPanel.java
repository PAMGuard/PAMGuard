package PamView.panel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.ColorManaged;
import PamView.PamKeyItem;
import PamView.PamColors.PamColor;
import PamView.dialog.PamDialog;
import PamView.dialog.PamLabel;

/**
 * Class to generate standard panels for plot keys.
 * Basically, it's a container with a simple 
 * layout that get's a load of other componenets
 * added to it.
 * @author Doug
 *
 */
public class KeyPanel  {

	private String title;
	
	private ArrayList<PamKeyItem> keyItems = new ArrayList<PamKeyItem>();
	
	private JPanel panel;
	
	private TitledBorder titledBorder;
	
	private int keyType = PamKeyItem.KEY_VERBOSE;
	
		
	public KeyPanel (String title, int keyType) {
		super();
		this.title = title;
	}


	public void add(PamKeyItem pamKeyItem) {
//		if (true) {
//			return;
//		}
		if (pamKeyItem == null) return;
		keyItems.add(pamKeyItem);
		if (panel != null) { 
			fillPanel();
		}
	}
	
	public void remove(PamKeyItem pamKeyItem) {
		keyItems.remove(pamKeyItem);
		if (panel != null) { 
			fillPanel();
		}
	}
	
	public JPanel getPanel() {
		if (panel == null) {
			createPanel();
			fillPanel();
		}
		return panel;
	}
	
	private void createPanel() {
		if (panel != null) {
			destroy();
		}
		panel = new BPanel();
		panel.setLayout(new GridBagLayout());
//		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(titledBorder = new TitledBorder(title));
//		PamColors.getInstance().registerComponent(panel, PamColor.PlOTWINDOW);
	}
	
	class BPanel extends PamPanel implements ColorManaged {

		public BPanel() {
			super(PamColor.PlOTWINDOW);
		}
		

		@Override
		public void setForeground(Color fg) {
			super.setForeground(fg);
			if (titledBorder != null) {
				titledBorder.setTitleColor(fg);
			}
		}
		
		@Override
		protected void paintComponent(Graphics arg0) {
				super.paintComponent(arg0);
		}
		
	}
	private void fillPanel() {
		JPanel panel = this.panel;
		if (panel == null) {
			return;
		}
		panel.removeAll();
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
//		if (title != null) {
//			c.gridwidth = 2;
//			PamDialog.addComponent(panel, new PamLabel(title), c);
//		}
		c.gridwidth = 1;
		c.ipadx = 5;
		Component icon;
		String text;
		PamKeyItem keyItem;
		for (int k = 0; k < keyItems.size(); k++) {
//			c.gridy++;
			keyItem = keyItems.get(k);
			for (int i = 0; i < keyItem.getNumItems(keyType); i++) {
				c.gridy++;
				icon = keyItem.getIcon(keyType, i);
				text = keyItem.getText(keyType, i);
				c.gridx = 0;
				if (icon != null) {
					c.gridwidth = 1;
					c.anchor = GridBagConstraints.EAST;
					PamDialog.addComponent(panel, icon, c);
					c.gridx = 1;
				}
				else {
					c.gridwidth = 2;
				}
				c.anchor = GridBagConstraints.WEST;
				PamDialog.addComponent(panel, new PamLabel(text), c);
			}
		}
	}
	
	public void clear() {
		keyItems.clear();
		if (panel != null) { 
			fillPanel();
		}
	}

	public void destroy() {
		
//		PamColors.getInstance().removeComponent(this.panel);
		panel = null;
		
	}

	public int getKeyType() {
		return keyType;
	}

	public void setKeyType(int keyType) {
		if (this.keyType != keyType) {
			this.keyType = keyType;
			createPanel();
			fillPanel();
		}
	}

	public TitledBorder getTitledBorder() {
		return titledBorder;
	}
}
