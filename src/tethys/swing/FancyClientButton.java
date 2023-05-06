package tethys.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;

import tethys.TethysControl;
import tethys.dbxml.DBXMLConnect;

/**
 * Fancy button which has a normal button AND a drop down 
 * for other web client pages. 
 * @author dg50
 *
 */
public class FancyClientButton extends JPanel {

	private JButton clientButton;
	private JButton dropButton;
	private JPopupMenu collectionsMenu;
	private TethysControl tethysControl;
	
	public FancyClientButton(TethysControl tethysControl) {
		this.tethysControl = tethysControl;
		setLayout(new GridBagLayout());
//		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		GridBagConstraints c = new GridBagConstraints();
		c.ipadx = c.ipady = 0;
		c.insets = new Insets(0,0,0,0);
		c.fill = GridBagConstraints.VERTICAL;
		clientButton = new JButton("Open Client");
		clientButton.setToolTipText("Open Tethys web client in default browser");
		ImageIcon arrowDown= null;
		try {
		arrowDown = new ImageIcon(ClassLoader
				.getSystemResource("Resources/SidePanelShowH.png"));
		}
		catch (Exception e) {
		}
		if (arrowDown != null) {
			dropButton = new JButton(arrowDown);
		}
		else {
			dropButton = new JButton("v");
		}
		dropButton.setToolTipText("Open Tethys collections pages in default browser");
		c.gridx = 0;
		add(clientButton, c);
		c.gridx++;
		add(dropButton, c);
		Insets dInsets = dropButton.getInsets();
		if (dInsets != null) {
			dInsets.left = dInsets.right = 4;
			dropButton.setBorder(new EmptyBorder(dInsets));
		}
		
		String[] collections = DBXMLConnect.collections;
		collectionsMenu = new JPopupMenu();
		for (int i = 0; i < collections.length; i++) {
			JMenuItem menuItem = new JMenuItem(collections[i]);
			menuItem.addActionListener(new OpenCollection(collections[i]));
			collectionsMenu.add(menuItem);
		}
		
		dropButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				collectionsMenu.show(dropButton, 0, 0);
			}
		});
	}
	
	public void addActionListener(ActionListener actionListener) {
		clientButton.addActionListener(actionListener);
	}
	
	private class OpenCollection implements ActionListener {

		private String collection;
		
		public OpenCollection(String collection) {
			super();
			this.collection = collection;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			tethysControl.openTethysCollection(collection);
		}
		
	}
	
	
}
