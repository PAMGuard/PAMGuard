package PamView.hidingpanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;

import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;

/**
 * This is similar to a tabbed pane but instead of tabs overlapping each other as in a JTabbedPane they expand and contract. You can view all tabs together or just one with
 * all others collapsed. 
 * @author Jamie Macaulay
 *
 */
public class TabbedHidingPane extends PamPanel{
	
	private static final long serialVersionUID = 1L;

	/**
	 * list of hiding panels which make up the tabs
	 */
	private ArrayList<TabHidingPanel> tabs;
	
	/**
	 * List of change listeners
	 */
	private ArrayList<ChangeListener> tabListeners;
	
	/**
	 * Background colour of the tabs and panel
	 */
	private Color background=PamColors.getInstance().getColor(PamColor.BACKGROUND_ALPHA);
	
	/**
	 * Highlight colour of tabs.
	 */
	private Color highlight=PamColors.getInstance().getColor(PamColor.HIGHLIGHT_ALPHA);
	
	/**
	 * Text colour of tabs
	 */
	private Color textColour=Color.WHITE;
	
	/**
	 * Border colour of tabs
	 */
	private Color borderColour=Color.GRAY;
		
	
	public TabbedHidingPane(){
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		tabs=new ArrayList<TabHidingPanel>();
		tabListeners=new ArrayList<ChangeListener>();
		this.setOpaque(false);
	}
		
	public void createPanel(ArrayList<TabHidingPanel> tabs){
		this.removeAll();
		//FIXME- why do we get an extra panel?
		for (int i=0; i<tabs.size(); i++){
			this.add(tabs.get(i));
			tabs.get(i).setOpaque(false);
			tabs.get(i).setAlignmentY(TOP_ALIGNMENT);
		}
		this.invalidate();
	}
	

	public void addTab(String title, Icon icon, Component component, boolean canScroll){
//		System.out.println("title: "+title+" icon "+icon+" component "+component);
		if (component==null) return;
		tabs.add(createNewHidingPanel(title, icon, component, canScroll));
		createPanel(tabs);
	}
	
	
	public void addTab(String title, Icon icon, Component component, boolean canScroll, int type){
//		System.out.println("title: "+title+" icon "+icon+" component "+component);
		if (component==null) return;
		tabs.add(createNewHidingPanel(title, icon, component, canScroll));
		createPanel(tabs);
	}
	
	
	/**
	 * Get the whole tab panel
	 * @param index
	 * @return
	 */
	public TabHidingPanel getTabPanel(int index){
		if (index<tabs.size()){
			return tabs.get(index);
		}
		return null; 
	}
	
	
	
	protected TabHidingPanel createNewHidingPanel(String title, Icon icon, Component component, boolean canScroll){
		return new TabHidingPanel(this, title, component ,icon, canScroll);
	}
	
	/**
	 * Create the showing tab for a panel. Override this to create custom tabs.  
	 * @param title - title to show in tab.
	 * @param icon - icon for ta. Can be null. 
	 * @return -Panel which acts as tab for hiding tab panel when panel is showing.
	 */
	public PamPanel createShowingTab(String tabTitle, Icon tabIcon){
		return new ShowingPanel(tabIcon,tabTitle);
	}
	
	/**
	 * Create the hiding tab for a panel. Override this to create custom tabs.  
	 * @param title - title to show in tab. Try to make very small fro hiding tab
	 * @param icon - icon for ta. Can be null. 
	 * @return -Panel which acts as tab for hiding tab panel when panle is hidden.
	 */
	public PamPanel createHidingTab(String tabTitle, Icon tabIcon){
		return new HidingTabPanel(tabIcon,"");
	}
	
	
	
	public class TabHidingPanel extends HidingPanel{

		private static final long serialVersionUID = 1L;
		
		//shows whether this tab is showing or not. 
		boolean isTabShowing=true;
	
		//The tab to show when the panel is hiding.  
		PamPanel hidingTab;
		
		//The tab which sits on top of the main component. 
		PamPanel showingTab;
		

		public TabHidingPanel(Component componentFrame, String tabTitle,
				Component mainComponent, Icon tabIcon, boolean canScroll) {
			super(componentFrame,mainComponent,HidingPanel.VERTICAL,canScroll);
			
			//remove the buttons
			this.remove(super.getEdgePanel());
			
			//create the tab hiding panel 
			hidingTab=createHidingTab(tabTitle, tabIcon);
			hidingTab.addMouseListener(new HidingHighlightListener(hidingTab));
			hidingTab.addMouseListener(new HidingTabListener(true));

			
			//create the tab hiding panel 
			showingTab=createShowingTab(tabTitle, tabIcon);
			showingTab.addMouseListener(new HidingHighlightListener(showingTab));
			showingTab.addMouseListener(new HidingTabListener(false));
			
			
			//set borders for both tabs
			((JComponent) mainComponent).setBorder(BorderFactory.createLineBorder(borderColour));
			hidingTab.setBorder(BorderFactory.createLineBorder(borderColour));
			
			//this class will set it's own highlighting. 
			setHighlight(false);
			
			showPanel(true);
		}
		
				
		/*
		 * Show or hide the panel
		 * @param state true = show, false = hide. 
		 */
		@Override
		public void showPanel(boolean state) {
			
			super.removeAll();
			
			if (showingTab==null || hidingTab==null) return; 
			
			if (state){
				addMainComponent(super.getMainComponent());
				super.add(BorderLayout.NORTH, showingTab);
			}
			else {
				super.add(BorderLayout.CENTER, hidingTab);
			}
			this.isTabShowing=state;
			
			super.invalidate();
		
			//notify the change listeners
			for (int i=0; i<tabListeners.size(); i++){
				tabListeners.get(i).stateChanged(null);
			}
		}
		
		
		/**
		 * Listens for mouse clicks on a hiding tab. 
		 * @author Jamie Macaulay
		 *
		 */
		class HidingTabListener implements MouseListener {
			
			private boolean open;

			HidingTabListener(boolean open){
				this.open=open; 
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				showPanel(open);
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				showPanel(open);
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		}
		
		/**
		 * Tab which shows when the panel is showing.
		 * @return
		 */
		public PamPanel getShowingTab(){
			return showingTab;
		}
		
		/**
		 * Tab which shows when the panel is hiding. Should be significantly smaller than showing tab
		 * @return tab which shows when the panel is hidden. 
		 */
		public PamPanel getHidingTab(){
			return hidingTab;
		}
		
	}
	
	/**
	 * Listens for mouse the mouse entering a tab. 
	 * @author Jamie Macaulay
	 */
	class HidingHighlightListener extends MouseAdapter {

		private Component component;

		HidingHighlightListener(Component panel){
			this.component=panel;
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
			component.repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			component.repaint();
		}
		
	}
	
	
	class HidingTabPanel extends PamPanel {
		
		private static final long serialVersionUID = 1L;

		HidingTabPanel(Icon tabIcon, String tabTitle){
			
			createPanel(tabIcon,tabTitle);
			
			//set the background
			this.setBackground(background);
			//allow for translucent colours
			this.setOpaque(false);
			//text colour
			this.setForeground(textColour);
			
		}
		
		public void createPanel(Icon tabIcon, String tabTitle){
			//create the tab hiding panel 
			this.setLayout(new GridBagLayout());
			PamGridBagContraints c=new PamGridBagContraints();
			c.fill = PamGridBagContraints.VERTICAL;
			PamDialog.addComponent(this, new JLabel(tabIcon), c);
			c.gridy++;
			c.weighty=1;
			PamDialog.addComponent(this, new JLabel(tabTitle), c);
		}
		
		
		@Override
		public void paintComponent(Graphics g) {
				extraPainting(g);
		        super.paintComponent(g);
		}
		
		
		public void extraPainting(Graphics g){
			if (mouseOverPanel(this)) g.setColor(highlight);
			else{
				g.setColor(background);
			}
	        Rectangle r = g.getClipBounds();
	        g.clearRect(r.x, r.y, r.width, r.height);
	        g.fillRect(r.x, r.y, r.width, r.height);
		}
		
		
	
	}
	
	/**
	 * Check whether the mouse is inside a component. 
	 * @return true if mouse is inside component. 
	 */
	public static boolean mouseOverPanel(Component component){
		if (component==null) return false;
		try {
			if(MouseInfo.getPointerInfo().getLocation().x >= component.getLocationOnScreen().x
					&& MouseInfo.getPointerInfo().getLocation().x <= component.getLocationOnScreen().x + component.getWidth()
					&& MouseInfo.getPointerInfo().getLocation().y >= component.getLocationOnScreen().y
					&& MouseInfo.getPointerInfo().getLocation().y <= component.getLocationOnScreen().y + component.getHeight())
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public class ShowingPanel extends HidingTabPanel{
		
		private static final long serialVersionUID = 1L;

		/**
		 * Button on tab to open settings dialog. 
		 */
		private JButton settingsButton;
		
		public PamGridBagContraints c;
		
		protected ShowingPanel(Icon tabIcon, String tabTitle) {
			super(tabIcon,tabTitle);
		}		

		public void createPanel(Icon tabIcon, String tabTitle){
			//create the tab showing panel 
			this.setLayout(new GridBagLayout());
			c=new PamGridBagContraints();
			c.insets=new Insets(0, 10, 0, 0);
			c.fill = PamGridBagContraints.HORIZONTAL;
			PamDialog.addComponent(this, new JLabel(tabIcon), c);
			c.insets=new Insets(0, 0, 0, 0);
			c.gridx++;
			c.weightx=1;
			PamDialog.addComponent(this, new JLabel(" "+tabTitle+" "), c);
			c.weightx=0;
			c.gridx++;
			c.insets=new Insets(0, 0, 0, 10);

		}

		
		public JButton getSettingsButton(){
			return settingsButton;
		}

	
	}
	
	public void addTabChangeListener(ChangeListener tabListener) {
		tabListeners.add(tabListener);
	}

	//TODO
	public int getSelectedIndex() {	

		return 0;
	}
	
	public Color getTabBackground() {
		return background;
	}

	public Color getTabHighlight() {
		return highlight;
	}

	public Color getTabTextColour() {
		return textColour;
	}

	public Color getBorderColour() {
		return borderColour;
	}

	public void setTabBackground(Color background) {
		this.background = background;
	}

	public void setTabHighlight(Color highlight) {
		this.highlight = highlight;
	}

	public void setTabTextColour(Color textColour) {
		this.textColour = textColour;
	}

	public void setPanelBorderColour(Color borderColour) {
		this.borderColour = borderColour;
	}
	

}
