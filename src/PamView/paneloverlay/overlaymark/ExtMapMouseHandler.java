package PamView.paneloverlay.overlaymark;

import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import PamguardMVC.debug.Debug;
import dataPlotsFX.layout.TDGraphFX.TDPlotPane;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

/**
 * Handle data from multiple external mouse handlers returning 
 * an OR of their response to the various functions. 
 * @author Doug Gillespie
 *
 */
public class ExtMapMouseHandler extends ExtMouseAdapter {

	private ArrayList<ExtMouseAdapter> extMouseAdapters = new ArrayList<>();	
	
	/**
	 * Handles pop up menu. 
	 */
	private ExtPopMenu popMenu = new ExtPopMenuSimple(); 


	/**
	 * The fxNode
	 */
	private Node fxNode;
	
	private Window awtWindow;
	
	/**
	 * Flag to say this should make and display composite popup menus
	 * from all extMouseAdapters. 
	 */
	private boolean compositePopupMenus;

	
	public ExtMapMouseHandler(Node fxNode, boolean compositePopupMenus) {
		this.fxNode = fxNode;
		this.compositePopupMenus = compositePopupMenus;
	}
	
	public ExtMapMouseHandler(Window awtWindow, boolean compositePopupMenus) {
		this.awtWindow = awtWindow;
		this.compositePopupMenus = compositePopupMenus;
	}
	
	public void addMouseHandler(ExtMouseAdapter mouseAdapter) {
		extMouseAdapters.add(mouseAdapter);
	}
	
	public boolean removeMouseHandler(ExtMouseAdapter mouseAdapter) {
		return extMouseAdapters.remove(mouseAdapter);
	}
	
	public void subscribeFXPanel(Node fxNode) {
		fxNode.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				mouseClicked(event);
			}
		});
		fxNode.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				mousePressed(event);
			}
		});
		fxNode.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				mouseReleased(event);
			}
		});
		fxNode.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				mouseMoved(event);
			}
		});
		fxNode.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				mouseDragged(event);
			}
		});
		fxNode.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				mouseExited(event);
			}
		});
	}


	@Override
	public boolean mouseDragged(javafx.scene.input.MouseEvent e) {
		boolean ans = false;
		for (ExtMouseAdapter ma:extMouseAdapters) {
			ans |= ma.mouseDragged(e);
		}
		return ans;
	}

	@Override
	public boolean mouseEntered(javafx.scene.input.MouseEvent e) {
		boolean ans = false;
		for (ExtMouseAdapter ma:extMouseAdapters) {
			ans |= ma.mouseEntered(e);
		}
		return ans;
	}

	@Override
	public boolean mouseExited(javafx.scene.input.MouseEvent e) {
		boolean ans = false;
		for (ExtMouseAdapter ma:extMouseAdapters) {
			ans |= ma.mouseExited(e);
		}
		return ans;
	}

	@Override
	public boolean mouseMoved(javafx.scene.input.MouseEvent e) {
		boolean ans = false;
		for (ExtMouseAdapter ma:extMouseAdapters) {
			ans |= ma.mouseMoved(e);
		}
		return ans;
	}
	
	@Override
	public boolean mouseClicked(javafx.scene.input.MouseEvent e) {
		boolean ans = false;
		for (ExtMouseAdapter ma:extMouseAdapters) {
			ans |= ma.mouseClicked(e);
		}
//		//pop up menus should appear on a click too?
//		if (e.isPopupTrigger()) {
//			if (popMenu.showPopupMenu(e, extMouseAdapters, fxNode)) {
//				return true;
//			}
//		}
		return ans;
	}


	@Override
	public boolean mousePressed(javafx.scene.input.MouseEvent e) {
		boolean ans = false;
		
		//System.out.println("Show pop up: 1 press: " + e.isPopupTrigger()); 
//		MarkRelationships links = MarkRelationships.getInstance();
//		MarkRelationshipsData params = links.getMarkRelationshipsData();


		for (ExtMouseAdapter ma:extMouseAdapters) {
			ans |= ma.mousePressed(e);
		}
		
		//pop up menus should appear on a press and click. 
		if (e.isPopupTrigger()) {
			if (popMenu!=null && popMenu.showPopupMenu(e, extMouseAdapters, fxNode)) {
				return true;
			}
		}
		
		//System.out.println("Show pop up: 2 press: " + e.isPopupTrigger()); 
		
		return ans;
	}

	@Override
	public boolean mouseReleased(javafx.scene.input.MouseEvent e) {
		boolean ans = false;

		//System.out.println("Show pop up: 2 release: " + e.isPopupTrigger()); 

		if (e.isPopupTrigger()) {
			if (popMenu.showPopupMenu(e, extMouseAdapters, fxNode)) {
				return true;
			}
		}
		
		for (ExtMouseAdapter ma:extMouseAdapters) {
			ans |= ma.mouseReleased(e);
		}

		//System.out.println("Show pop up: 2 release: " + e.isPopupTrigger());
		
		return ans;
	}


	@Override
	public boolean mouseWheelMoved(ScrollEvent e) {
		boolean ans = false;
		for (ExtMouseAdapter ma:extMouseAdapters) {
			ans |= ma.mouseWheelMoved(e);
		}
		return ans;
	}
	
	/**
	 * Get the pop up menu. 
	 * @return the pop up menu
	 */
	public ExtPopMenu getPopMenu() {
		return popMenu;
	}

	/**
	 * Set the pop up menu. Use this to set custom pop up menus
	 * @param popMenu - the pop up menu to set
	 */
	public void setPopMenu(ExtPopMenu popMenu) {
		this.popMenu = popMenu;
	}
	

	/**
	 * Get a complete list of all menu items. 
	 */
	public List<MenuItem> getPopupMenuItems(MouseEvent e) {		
		List<MenuItem> completeList=new ArrayList<MenuItem>(); 
		int n=0; 
		for (ExtMouseAdapter ma:extMouseAdapters) {
			List<MenuItem> adapMenuItems = ma.getPopupMenuItems(e);
			if (adapMenuItems != null) {
				//System.out.println("ExtMapMouseHandler: adpater " + ma.getPopupMenuItems(e).size());
				for (int  j=0; j<adapMenuItems.size(); j++) {
					//System.out.println("ExtMapMouseHandler: " + adapMenuItems.get(j).getText());
					completeList.add(adapMenuItems.get(j)); 
				}
				
				if (adapMenuItems.size()>0 && n<extMouseAdapters.size()-1) {
					//only add between items and if there is more than one menu item in a the list. 
					completeList.add(new SeparatorMenuItem());
				}
			}
			n++; 
		}
		return completeList; 
	}


}
