package pamViewFX.fxNodes.utilsFX;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenuItem;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;

/** 
 * Holds info allowing a swing menu item to be converted to JavaFX nodes.
 */
public class MenuItemInfo {

	/**
	 * Sub menu list
	 */
	private ArrayList<MenuItemInfo> subMenuList= new ArrayList<MenuItemInfo>(); 
	
	/**
	 * The label of the menu
	 */
	private String label; 
	
	/**
	 * The icon of the menu item
	 */
	private Node icon; 
	
	/**
	 * The action when the menu is pressed. 
	 */
	private EventHandler<ActionEvent> actionListener; 
	
	/**
	 * Constructor 
	 */
	public MenuItemInfo(JMenuItem jMenuItem){
		
		label=jMenuItem.getText();
		
		if (jMenuItem.getIcon()!=null){
			icon= PamUtilsFX.iconToFX(jMenuItem.getIcon());
		}
		
		//now search for sub menus. This is iterative so other menus will be contained in the 
		//new MenuItemInfo classes created. 
		Component component; 
		for (int i=0; i<jMenuItem.getComponentCount(); i++){
			 component=jMenuItem.getComponent(i); 
			 if (component instanceof JMenuItem){
				 subMenuList.add(new MenuItemInfo((JMenuItem) component)); 
			 }
		}	
		
		//System.out.println("Action listeners: "+jMenuItem.getActionListeners().length); 
		
		this.actionListener = new ActionEventFX(jMenuItem.getActionListeners()); 
	}	
	
	/**
	 * @return the subMenuList
	 */
	public ArrayList<MenuItemInfo> getSubMenuList() {
		return subMenuList;
	}

	/**
	 * @param subMenuList the subMenuList to set
	 */
	public void setSubMenuList(ArrayList<MenuItemInfo> subMenuList) {
		this.subMenuList = subMenuList;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the icon
	 */
	public Node getIcon() {
		return icon;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(Node icon) {
		this.icon = icon;
	}

	/**
	 * @return the actionListener
	 */
	public EventHandler<ActionEvent> getActionListener() {
		return actionListener;
	}

	/**
	 * @param actionListener the actionListener to set
	 */
	public void setActionListener(EventHandler<ActionEvent> actionListener) {
		this.actionListener = actionListener;
	}

	
	
	/**
	 * Calls swing action listeners
	 * @author Jamie Macaulay
	 */
	public class ActionEventFX implements EventHandler<ActionEvent>{
		
		private ActionListener[] listeners;

		public ActionEventFX(ActionListener[] listeners){
			this.listeners=listeners; 
		}

		@Override
		public void handle(ActionEvent event) {
//			System.out.println("Handle the action: ");
			for (int i=0; i<listeners.length; i++){
				listeners[i].actionPerformed(null);
			}
		}
	}
}