package PamView.hidingpanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.ColorManaged;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.panel.PamPanel;

/**
 * Class for a hiding, possibly sliding panel to hold things like
 * the side bar, top control panel of the clip display, etc. 
 * @author Doug Gillespie
 *
 */
public class HidingPanel extends PamPanel implements PamSettings {

	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;
	
	private Component mainComponent;
	private int direction; 
	private boolean canScroll;
	private PamPanel edgePanel;
	private JButton showButton, hideButton;
	private String showSymbol, hideSymbol;
	private JScrollPane scrollPanel;
	private Component thingToHide;

	private Component componentFrame; // frame to invalidate once thing is hidden. 
	private String title;
	private boolean dblClkClose;
	
	private HidingPanelParams hidingPanelParams = new HidingPanelParams();
	
	/**
	 * if true then highlights the hiding panel whenever mouse enters. 
	 */
	private boolean highlight=true;

	/**
	 * Highlight colour of tabs.
	 */
	private Color highlightColour=PamColors.getInstance().getColor(PamColor.HIGHLIGHT);
	private String displayName;
	private String settingsName;
	
	private static ImageIcon arrowDown=new ImageIcon(ClassLoader
			.getSystemResource("Resources/SidePanelShowH.png"));
	private static ImageIcon arrowUp=new ImageIcon(ClassLoader
			.getSystemResource("Resources/SidePanelHideH.png"));
	private static ImageIcon arrowRight=new ImageIcon(ClassLoader
			.getSystemResource("Resources/SidePanelHide.png"));
	private static ImageIcon arrowLeft=new ImageIcon(ClassLoader
			.getSystemResource("Resources/SidePanelShow.png"));
	/**
	 * Create a panel which can hide. 
	 * @param componentFrame the component where the hiding panel will sit. 
	 * @param mainComponent - the component which will be shown inside the hiding panel i.e. the component which can be hidden. 
	 * @param direction - direction: either HORIZONTAL or VERTICAL.
	 * @param canScroll - true if mainComponent is shown within a scroll pane.
	 */
	public HidingPanel(Component componentFrame, Component mainComponent, int direction, boolean canScroll) {
		this(componentFrame, mainComponent, direction, canScroll, null, null);
	}
	
	/**
	 * Create a panel which can hide. 
	 * @param componentFrame the component where the hiding panel will sit. 
	 * @param mainComponent - the component which will be shown inside the hiding panel i.e. the component which can be hidden. 
	 * @param direction - direction: either HORIZONTAL or VERTICAL.
	 * @param canScroll - true if mainComponent is shown within a scroll pane.
	 * @param displayName String to show in hide / show tips. 
	 * @param settingsName String unique name to store to hold position for next time. 
	 */
	public HidingPanel(Component componentFrame, Component mainComponent, int direction, boolean canScroll, String displayName, String settingsName) {
		super(new BorderLayout());
		this.componentFrame = componentFrame;
		this.mainComponent = mainComponent;
		this.direction = direction;
		this.canScroll = canScroll;
		this.displayName = displayName;
		this.settingsName = settingsName;
		edgePanel = new PamPanel(new BorderLayout());

		addMainComponent(mainComponent);
		
		if (direction == VERTICAL) {
			showButton = new JButton("", arrowDown);
			showButton.addActionListener(new ShowButton());
			showButton.setToolTipText(getShowTip());
			showButton.setMargin(new Insets(0, 0, 0, 0));
			hideButton = new JButton("", arrowUp);
			hideButton.addActionListener(new HideButton());
			hideButton.setToolTipText(getHideTip());
			hideButton.setMargin(new Insets(0, 0, 0, 0));
			edgePanel.add(BorderLayout.NORTH, showButton);
			edgePanel.add(BorderLayout.SOUTH, hideButton);
			this.add(BorderLayout.WEST, edgePanel);
			if (scrollPanel != null) {
				scrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
				scrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			}
		}
		else {
			showButton = new JButton("", arrowRight);
			showButton.addActionListener(new ShowButton());
			showButton.setToolTipText(getShowTip());
			showButton.setMargin(new Insets(0, 0, 0, 0));
			hideButton = new JButton("", arrowLeft);
			hideButton.addActionListener(new HideButton());
			hideButton.setToolTipText(getHideTip());
			hideButton.setMargin(new Insets(0, 0, 0, 0));
			edgePanel.add(BorderLayout.WEST, showButton);
			edgePanel.add(BorderLayout.EAST, hideButton);
			this.add(BorderLayout.NORTH, edgePanel);
			if (scrollPanel != null) {
				scrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				scrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			}
		}
		this.addMouseListener(new PanelMouse());
		edgePanel.addMouseListener(new PanelMouse());
		showPanel(true);
		
		if (settingsName != null) {
			PamSettingManager.getInstance().registerSettings(this);
		}
	}
	
	private String getShowTip() {
		if (displayName == null) {
			return "Show panel";
		}
		else {
			return "Show " + displayName;
		}
	}

	private String getHideTip() {
		if (displayName == null) {
			return "Hide panel";
		}
		else {
			return "Hide " + displayName;
		}
	}
	

	@Override
	public void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    //only highlight if the panel is hiding and highlighting has been allowed.
		if (!isExpanded() && highlight) extraPainting(g);
	}
		
	/**
	 * Extra painting to highlight panel if mouse is inside. 
	 * @param g- graphics handle. 
	 */
	public void extraPainting(Graphics g){
		if (TabbedHidingPane.mouseOverPanel(this)) g.setColor(highlightColour);
		else{
			g.setColor(getBackground());
		}
	    Rectangle r = g.getClipBounds();
	    g.fillRect(r.x, r.y, r.width, r.height);
	}
		
	
	
	
	/**
	 * Swaps the hide and show buttons around. Generally use this when a panel is on left hand side or bottom of screen. 
	 */
	public void reverseShowButton(){
		if (direction == HORIZONTAL) {
			hideButton.setIcon(arrowDown);
			showButton.setIcon(arrowUp);
			edgePanel.remove(hideButton); edgePanel.remove(showButton);
			edgePanel.add(BorderLayout.SOUTH, showButton); edgePanel.add(BorderLayout.NORTH, hideButton);
			
		}
		else{
			hideButton.setIcon( arrowRight);
			showButton.setIcon(arrowLeft);
			edgePanel.remove(hideButton); edgePanel.remove(showButton);
			edgePanel.add(BorderLayout.EAST, showButton); edgePanel.add(BorderLayout.WEST, hideButton);
		}
	}
	
	public void setTitle(String title) {
		this.title = title;
		setToolTips();
	}
	
	private void setToolTips() {
		if (title != null) {
			showButton.setToolTipText("Click to show " + title);
			hideButton.setToolTipText("Click to hide " + title);
			edgePanel.setToolTipText("Click");
			edgePanel.setToolTipText(thingToHide.isVisible() ? hideButton.getToolTipText() : showButton.getToolTipText());
			this.setToolTipText(thingToHide.isVisible() ? hideButton.getToolTipText() : showButton.getToolTipText());
		}
		
	}

	class ShowButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			showPanel(true);
		}

	}

	class HideButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			showPanel(false);
		}
	}
	
	private class PanelMouse extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent arg0) {
			showPanel(!thingToHide.isVisible());
//			if (dblClkClose && arg0.getClickCount()>=2){
//				showPanel(false);
//			}
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
			repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			repaint();
		}
	}

	/**
	 * Show or hide the panel
	 * @param state true = show, false = hide. 
	 */
	public void showPanel(boolean state) {
		thingToHide.setVisible(state);
		showButton.setVisible(!state);
		hideButton.setVisible(state);
		setToolTips();
		if (mainComponent != null) {
			mainComponent.invalidate();
		}
		if (state) {
			edgePanel.setToolTipText(getHideTip());
			this.setToolTipText(getHideTip());
		}
		else {
			edgePanel.setToolTipText(getShowTip());
			this.setToolTipText(getShowTip());
		}
	}
	
	/**
	 * Return true if the panel is in it's expanded state. 
	 * @return whether the panel is showing or not. 
	 */
	public boolean isExpanded() {
		return thingToHide.isVisible();
	}
	
	@Override
	public void setOpaque(boolean isOpaque){
		super.setOpaque(isOpaque);
//		if (edgePanel!=null) edgePanel.setOpaque(isOpaque);
		if (scrollPanel!=null){
			scrollPanel.setOpaque(isOpaque);
			scrollPanel.getViewport().setOpaque(isOpaque);
		}

	}
	
	private class PanelScrollPane extends JScrollPane {
		
		public PanelScrollPane(Component mainComponent) {
			super(mainComponent);
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension d = super.getPreferredSize();
			Dimension tD = this.getSize();
			if (getVerticalScrollBar().isVisible()){// || d.width > tD.width) {
				d.width += getVerticalScrollBar().getWidth();
			}
			if (getHorizontalScrollBar().isVisible()) {
				d.height += getHorizontalScrollBar().getHeight();
			}
			return d;
		}
	}
	
	/**
	 * Remove the mainComponentPanel. 
	 */
	public void removeMainComponent(){
		this.remove(thingToHide);
	}
	
	/**
	 * Add the mainComponent to the hiding panel
	 * @param mainComponent
	 */
	public void addMainComponent(Component mainComponent){
		if (canScroll) {
			scrollPanel = new PanelScrollPane(mainComponent);
			thingToHide = scrollPanel;
		}
		else {
			thingToHide = mainComponent;
		}
		this.add(BorderLayout.CENTER, thingToHide);
		this.validate();
	}
	
	
//	public void setDoubleClickClose(boolean close){
//		this.dblClkClose=close;
//	}
	/**
	 * Get the button which controls hide behaviour. Can be used to add additional listeners. 
	 * @return HidePanel button. 
	 */
	public JButton getHideButton(){
		return hideButton;
	}
	
	public Component getMainComponent() {
		return thingToHide;
	}
	
	/**
	 * Get the panel which contains the show and hide buttons
	 * @return panel which contaisn the show and hide buttons
	 */
	public PamPanel getEdgePanel() {
		return edgePanel;
	}

	public void setEdgePanel(PamPanel edgePanel) {
		this.edgePanel = edgePanel;
	}
	

	/**
	 * Check whether the hiding panel is highlighted when the mouse enters. Only highlights the panle, not top level components. 
	 * @return true of panel is set to highlight. 
	 */
	public boolean isHighlight() {
		return highlight;
	}


	/**
	 * Set whether the hiding panel is highlighted when the mouse enters. Only highlights the panle, not top level components. 
	 */
	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
	}

	@Override
	public String getUnitName() {
		if (settingsName != null) {
			return settingsName;
		}
		else {
			return "HidingPanel";
		}
	}

	@Override
	public String getUnitType() {
		return "HidingPanel";
	}

	@Override
	public Serializable getSettingsReference() {
		hidingPanelParams.isExpanded = isExpanded();
		return hidingPanelParams;
	}

	@Override
	public long getSettingsVersion() {
		return HidingPanelParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		hidingPanelParams = ((HidingPanelParams) pamControlledUnitSettings.getSettings()).clone();
		if (hidingPanelParams != null) {
			if (hidingPanelParams.isExpanded != null) {
				showPanel(hidingPanelParams.isExpanded);
				return true;
			}
		}
		return false;
	}

	
	

	
}
