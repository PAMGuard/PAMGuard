package PamView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import PamView.panel.PamPanel;

/**
 * More sophisticated side panel container which can 
 * close itself down to a very small strip to make 
 * more room for main display items. 
 * @author Doug Gillespie
 *
 */
public class HidingSidePanel {
	
	private JPanel outerPanel;
	
	private JPanel innerPanel;
	
	private JPanel topBar;
	
	private JPanel scrolledPanel;
	
	private JButton showButton, hideButton;
	
	private JScrollPane scrollPanel;
	
	private PamGui pamGui;

	/**
	 * New side panel which can be hidden using little buttons. 
	 */
	public HidingSidePanel(PamGui pamGui) {
		/*
		 * Order of panels is innerPanel (contains the actual PamSidePanels from modules)
		 *                    scrolledPanel (uses border layout to keep stuff in North)
		 *                    scrollPanel   (scrolls the scrolledPanel if it won't all fit)
		 *                    outerPanel    (container for scrollPanel and for topBar)
		 *                    
		 * When hidden, the visible panel is the outerPanel - could add some functionality to 
		 * display a small amount of info here ? 
		 */
		super();
		this.pamGui = pamGui;
		outerPanel = new PamPanel();
		outerPanel.setVisible(false);
		innerPanel = new PamPanel();
		scrolledPanel = new PamPanel();
		scrolledPanel.setLayout(new BorderLayout());
		scrolledPanel.add(BorderLayout.NORTH, innerPanel);
		scrollPanel = new HScrollPane(scrolledPanel);
		scrollPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		scrollPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
//		innerPanel.setBackground(Color.green);
//		outerPanel.setBackground(Color.red);
//		scrolledPanel.setBackground(Color.pink);
		outerPanel.setLayout(new BorderLayout());
		outerPanel.add(BorderLayout.CENTER, scrollPanel);
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
		
		topBar = new PamPanel();
		topBar.setLayout(new BorderLayout());
		showButton = new JButton("", new ImageIcon(ClassLoader
				.getSystemResource("Resources/SidePanelShow.png")));
		showButton.addActionListener(new ShowButton());
		showButton.setToolTipText("Show side panels");
//		showButton.setBorder(null);
		showButton.setMargin(new Insets(0, 0, 0, 0));
		hideButton = new JButton("", new ImageIcon(ClassLoader
				.getSystemResource("Resources/SidePanelHide.png")));
		hideButton.addActionListener(new HideButton());
		hideButton.setToolTipText("Hide side panels");
//		hideButton.setBorder(null);
		hideButton.setMargin(new Insets(0, 0, 0, 0));
		topBar.add(BorderLayout.WEST, showButton);
		topBar.add(BorderLayout.EAST, hideButton);
		outerPanel.add(BorderLayout.NORTH, topBar);
//		topBar.add(BorderLayout.CENTER, new JLabel("O"));
		topBar.addMouseListener(new TopBarMouse());
		topBar.setToolTipText("Click to hide side panels");
		
		outerPanel.setToolTipText("Click on the arrow above to show the side panels");
		outerPanel.addMouseListener(new OuterPanelMouse());
		
		showPanel(true);
		
	}
	
	/**
	 * Override JScrollPane to make it wider when the scroll bar is showing, 
	 * otherwise, the scroll bar takes up part of the actual display meaning
	 * that the right hand edge of the side panel components are obscured. 
	 * @author dg50
	 *
	 */
	private class HScrollPane extends JScrollPane {

		public HScrollPane(Component view) {
			super(view);
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension sz = super.getPreferredSize();
			if (getVerticalScrollBar() != null && getVerticalScrollBar().isVisible()) {
				sz.width += this.getVerticalScrollBar().getWidth();
			}
			return sz;
		}
		
	}
	
	/**
	 * Disable the show and hide button, keeping the side pane in current position. 
	 * @param disable - true to disable the button. False to re-enable. 
	 */
	public void disableShowButton(boolean disable) {
		outerPanel.remove(topBar);
		if (!disable) outerPanel.add(BorderLayout.NORTH, topBar);
		outerPanel.validate();
		outerPanel.repaint(); 
	}
	
	private class ShowButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			showPanel(true);
		}
	}
	private class HideButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			showPanel(false);
		}
	}
	
	private class TopBarMouse extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent arg0) {
			if (scrollPanel.isVisible()) {
				showPanel(false);
			}
		}
	}

	private class OuterPanelMouse extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent arg0) {
			if (!scrollPanel.isVisible()) {
				showPanel(true);
			}
		}
	}
	
	/**
	 * Show or side the active component of the side panel, 
	 * Some basic border from the outer panel will always 
	 * be visible unless the entire thing is hidden because there
	 * are no side panel controls available. 
	 * @param show
	 */
	public void showPanel(boolean show) {
		hideButton.setVisible(show);
		showButton.setVisible(!show);
		scrollPanel.setVisible(show);
		pamGui.guiParameters.hideSidePanel = !show;
	}

	
	/**
	 * Get the side panel outer window to include in the main GUI.
	 * @return gui component.
	 */
	public JPanel getSidePanel() {
		return outerPanel;
	}

	/**
	 * Remove a component from the side panel. 
	 * @param panel component to remove
	 */
	public void remove(JComponent panel) {
		innerPanel.remove(panel);
		showHide();
	}
	
	private void showHide() {
		boolean oldState = outerPanel.isVisible();
		boolean newState = innerPanel.getComponentCount() > 0;
		if (oldState != newState) {
			outerPanel.setVisible(newState);
			pamGui.getGuiFrame().validate();
//			pamGui.getGuiFrame().repaint();
			scrollPanel.invalidate();
		}
	}

	/**
	 * Add a component to the side panel
	 * @param panel component to add
	 */
	public void add(JComponent panel) {
		innerPanel.add(panel);
		showHide();
	}

	/**
	 * Remove all components from the side panel
	 */
	public void removeAll() {
		innerPanel.removeAll();
	}

	/**
	 * Get a count of the number of side panel components
	 * @return count of side panel components
	 */
	public int getComponentCount() {
		return innerPanel.getComponentCount();
	}
	
	

}
