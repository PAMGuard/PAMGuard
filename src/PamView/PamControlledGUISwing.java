package PamView;

import java.awt.Component;
import java.awt.Frame;

import javax.swing.JFrame;
import javax.swing.JMenuItem;

import PamController.PamControlledUnitGUI;
import PamController.PamController;
import PamController.PamGUIManager;

/**
 * Provides Swing GUI components for a PAMControlledUnit.
 * 
 * @author Jamie Macaulay
 *
 */
public class PamControlledGUISwing extends PamControlledUnitGUI {
	
	/**
	 * Reference to the PamView
	 */
	private PamView pamView;
	
	/**
	 * The side panel which sits on the left hiding pane 
	 */
	private PamSidePanel sidePanel;
	
	/**
	 * Component for the tool bar. 
	 */
	private Component toolbarComponent;
	
	/**
	 * The current GUI frame number
	 */
	private int frameNumber;
	
	/**
	 * The  tab panel; this is the main panel which sits in a new module specific tab. 
	 */
	private PamTabPanel tabPanel;
	
	/**
	 * The clip board copier to allow users to take screenshots of the tab pane. 
	 */
	private ClipboardCopier tabClipCopier;

	public PamControlledGUISwing() {
		
	}
	
	/**
	 * Gets a reference to a panel to be added to a view
	 * 
	 * @return reference to a PamTabPanel object
	 * @see PamTabPanel
	 * @see PamSidePanel
	 */
	public PamTabPanel getTabPanel() {
		return tabPanel;
	}
	
	/**
	 * Sets the side panel for the PamControlledUnit
	 * Side panels are shown down the left hand side of 
	 * the main Pamguard GUI and are always visible, irrespective
	 * of which tab is being viewed on the main tabbed display. 
	 * <p>
	 * Side panels are generally used to display summary information
	 * for the PamControlledUnit or to provide quick access controls. 
	 * @param sidePanel Reference to a PamSidePanel object
	 * @see PamSidePanel
	 */
	public void setSidePanel(PamSidePanel sidePanel) {
		this.sidePanel = sidePanel;
	}

	/**
	 * Sets the tab panel for the PamControlledUnit. 
	 * A tab panel may contain graphics or tables to display 
	 * information of any type.
	 * @param tabPanel
	 */
	public void setTabPanel(PamTabPanel tabPanel) {
		this.tabPanel = tabPanel;
		tabClipCopier = new ClipboardCopier(tabPanel.getPanel());
	}
	
	/**
	 * Get the number of the frame that side and tab panels
	 * for this module should sit on.  
	 * @return frame number. 
	 */
	public int getFrameNumber() {
		return frameNumber;
	}
	
	public void setFrameNumber(int frameNumber) {
		this.frameNumber = frameNumber;
	}
	
	/**
	 * Gets a reference to a small panel to be displayed along the 
	 * left hand edge of the main tab panel. Side panels should be small since 
	 * they are always visible and any space they take will be taken from the main 
	 * tab panel. 
	 * <p>
	 * It is possible for a PamControlled unit to have a side panel without having
	 * a pamTabPanel.
	 *
	 * @return a pamSidePanel object.
	 * @see PamSidePanel
	 * @see PamTabPanel
	 */
	public PamSidePanel getSidePanel() {
		return sidePanel;
	}
	
	/**
	 * Sets the toolbar component which will be incorporated into the top toolbar whenever
	 * this controlled unit's display is selected in the main tab panel
	 * @return An AWT component to include in the toolbar. 
	 */
	public Component getToolbarComponent() {
		return toolbarComponent;
	}
	
	/**
	 * Set the toolbar component which will be incorporated into the top toolbar 
	 * whenever this controlled unit's display is selected in the main tab panel
	 * @param toolbarComponent An AWT component to include in the toolbar.
	 */
	protected void setToolbarComponent(Component toolbarComponent) {
		this.toolbarComponent = toolbarComponent;
	}

	
	/**
	 * Create a JMenu object containing MenuItems associated with PamProcesses
	 * 
	 * @param parentFrame
	 *            The owner frame of the menu
	 * @return reference to a JMenu which can be added to an existing menu or
	 *         menu bar
	 *         <p>
	 *         Note that if multiple views are to use the same menu, then they
	 *         should each create a new menu (by setting Create to true) the
	 *         first time they call this method.
	 */
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		return null;
	}

	/**
	 * Create a JMenu object containing MenuItems associated with the view
	 * 
	 * @return reference to a JMenu which can be added to an existing menu or
	 *         menu bar
	 *         <p>
	 *         Note that if multiple views are to use the same menu, then they
	 *         should each create a new menu (by setting Create to true) the
	 *         first time they call this method.
	 */
	public JMenuItem createDisplayMenu(Frame parentFrame) {
		return null;
	}
	
	public JMenuItem createHelpMenu(Frame parentFrame) {
		// TODO Auto-generated method stub
		return null;
	}
	

	/**
	 * @param parentFrame parent frame for the menu
	 * @return the file menu item
	 */
	public JMenuItem createFileMenu(JFrame parentFrame) {
		return null;
	}
	
	public PamView getPamView() {
		return pamView;
	}
	
	public ClipboardCopier getTabClipCopier() {
		if (tabClipCopier == null && getTabPanel() != null) {
			tabClipCopier = new ClipboardCopier(getTabPanel().getPanel());
		}
		return tabClipCopier;
	}

	/**
	 * Get the main frame for the GUI. In some cases the view may not 
	 * have been created, so go straight to the main one. 
	 * @return frame. 
	 */
	public Frame getGuiFrame() {
		if (pamView != null) {
			return pamView.getGuiFrame();
		}
		else {
			PamController.getInstance();
			return PamController.getMainFrame();
		}
	}

	@Override
	public int getGUIFlag() {
		return PamGUIManager.SWING;
	}

}
