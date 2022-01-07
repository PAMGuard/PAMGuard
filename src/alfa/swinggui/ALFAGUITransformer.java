package alfa.swinggui;

import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import PamController.PamController;
import PamView.FullScreen;
import PamView.GuiFrameManager;
import PamView.PamGui;
import PamView.panel.PamTabbedPane;
import PamguardMVC.debug.Debug;
import alfa.ALFAControl;

/**
 * Changes the PAMGuard <i>Swing<i> GUI to make it more touch friendly and remove some clutter so that
 * the UI is a little easier to use on tablets. 
 * 
 * @author Jamie Macaulay
 *
 */
public class ALFAGUITransformer  {

	/**
	 * The height of the main tabs
	 */

	public static final int TABHEIGHT = 65;
	
	public static final int TABWIDTH = 90;


	/**
	 * The main menu
	 */
	private JPopupMenu menu;

	/**
	 * Reference ot hte GUI frame manager. 
	 */
	private GuiFrameManager guiFrameManager;

	private Container glassPane; 

	public static ImageIcon settings = new ImageIcon(ClassLoader
			.getSystemResource("Resources/MenuButton.png"));

	public ALFAGUITransformer(ALFAControl alfaControl) {

		this.guiFrameManager = PamController.getInstance().getGuiFrameManager(); 

		//set the tab size. 

		guiFrameManager.getFrameGui(0).setTabsSize(new Dimension(TABWIDTH, TABHEIGHT));

		//guiFrameManager.getFrameGui(0).setTabFont(new Font(null, Font.BOLD, 16)); 

		//get rid of the top tool bar
		guiFrameManager.getFrameGui(0).setToolBarVisible(false);


		guiFrameManager.getFrameGui(0).getSidePanel().showPanel(true);
		guiFrameManager.getFrameGui(0).getSidePanel().disableShowButton(true);


		//total HACK to add button to tab pane
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				addButtonToPane(guiFrameManager.getFrameGui(0));
			}
		});


		//get rid of all those menus that are going to confuse folk. 
		guiFrameManager.getFrameGui(0).getGuiFrame().setJMenuBar(null);
		setMenuItems();
		FullScreen.setGoFullScreen(true);
	}

	/**
	 * Set the menu items
	 */
	private synchronized void setMenuItems() {
		
		guiFrameManager.getFrameGui(0).getGuiFrame().setJMenuBar(null);
		
		if (menu!=null) {
			menu.removeAll();
		}

		JMenuBar menuBar = guiFrameManager.getFrameGui(0).makeGuiMenu();	
		
//		for (int i=0; i<menuBar.getMenuCount(); i++) {
//			Debug.out.println("ALFA 2: MenuBar: " + menuBar.getMenu(i).getText() + " N: " + menuBar.getMenuCount()); 
//		}

		if (menu!=null && menuBar!=null) {
			final int count = menuBar.getMenuCount(); 
			ArrayList<JMenu> newMenuItems = new ArrayList<JMenu>(); 
			for (int i=0; i<count; i++) {
				//Debug.out.println("MenuBar: " + menuBar.getMenu(i).getText()+ " N: " + menuBar.getMenuCount() ); 

				newMenuItems.add(menuBar.getMenu(i));
			}
			
			//another total HACK. Don;t know why but simply adding one menu item from another cause all sorts of issues...
			//maybe should replace with while loops 
			for (int i=0; i<count; i++) {
				menu.add(newMenuItems.get(i));
			} 
		}
	}

	/**
	 * Add button to the tab pane with all menus etc. Again, pretty HACKY with a glass pane to put button on. 
	 * @param gui -the gui frame to add the button to. 
	 */
	private void addButtonToPane(PamGui gui) {

		PamTabbedPane tabbedPane = gui.getTabbedPane();
		tabbedPane.setTabsDrag(false); 
		//		Rectangle tabBounds = tabbedPane.getBoundsAt(0);

		glassPane = (Container) gui.getGuiFrame().getGlassPane();
		glassPane.setVisible(true);
		glassPane.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.NORTHEAST;

		JButton button = new JButton(settings);
		//navigation menu

		button.setPreferredSize(new Dimension(button.getPreferredSize().width,
				(int) TABHEIGHT+5));
		glassPane.add(button, gbc);

		menu = new JPopupMenu();
		menu.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				menu.show(button, 0, button.getBounds().height);
			}
		});

		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				glassPane.setVisible(true); //HACK
				setMenuItems();
			}
		};
		tabbedPane.addChangeListener(changeListener);
		//	      button.setPreferredSize(new Dimension(button.getPreferredSize().width,
		//	            (int) tabBounds.getHeight() - 2));
	}
	
	
	/**
	 * Notification passed from controller to the GUI transformer
	 * @param changeType - the change type flag. 
	 */
	public synchronized void notifyModelChanged(int changeType) {
		setMenuItems();
	}
}
