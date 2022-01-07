package pamScrollSystem;

import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import PamView.PamMenuParts;
import PamView.PamSymbol;
import PamView.PamSymbolType;

abstract public class AbstractPamScrollerAWT extends AbstractPamScroller implements Serializable {

	private static final long serialVersionUID = 1L;

	static public final int HORIZONTAL = Adjustable.HORIZONTAL;
	static public final int VERTICAL = Adjustable.VERTICAL;

	/**
	 * @return the Swing component to go into the GUI. 
	 */
	public abstract JComponent getComponent();


	private JButton pageForward, pageBack;
	private JButton showMenu;
	private JPanel buttonPanel;
	private JButton playbackButton;
	
	private Icon playIcon, stopIcon;

	Color iconLine = Color.BLUE;
	Color iconFill = Color.BLUE;

	private int orientation;
	
	

	public AbstractPamScrollerAWT(String name, int orientation, int stepSizeMillis, long defaultLoadTime, boolean hasMenu) {
		super(name, orientation, stepSizeMillis, defaultLoadTime, hasMenu);		
		//create the swing components. 
		createScrollComponenent(hasMenu);
		
	}

	public void createScrollComponenent(boolean hasMenu){
		createScrollComponenent(hasMenu, true);
	}
	/**
	 * Create the wee component to go into the sorner of the scroll bar that 
	 * contains the paging arrows. 
	 * @param hasMenu true if the component is to include a popup menu. 
	 */
	public void createScrollComponenent(boolean hasMenu, boolean hasPlayback){
		buttonPanel = new JPanel();
		if (orientation == HORIZONTAL) {
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		}
		else {
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		}
//		buttonPanel.setLayout(new BorderLayout());
		//
		//		pageForward = new JButton("", new ImageIcon(ClassLoader
		//				.getSystemResource("Resources/doubleForwardArrow.png")));
		if (hasMenu) {
			PamSymbol ps;
			pageForward = new JButton("", ps = new PamSymbol(PamSymbolType.SYMBOL_DOUBLETRIANGLER, 12, 12, true, 
					iconFill, iconLine));
			ps.setIconHorizontalAlignment(PamSymbol.ICON_HORIZONTAL_CENTRE);
			//		pageForward = new JButton(new Character('\u21F0').toString());
			pageForward.addActionListener(new PageForwardAction());
			pageForward.setToolTipText("Move loaded data forward");

			pageBack = new JButton("", ps = new PamSymbol(PamSymbolType.SYMBOL_DOUBLETRIANGLEL, 12, 12, true, 
					iconFill, iconLine));
			ps.setIconHorizontalAlignment(PamSymbol.ICON_HORIZONTAL_CENTRE);
			//		pageBack = new JButton(new Character('\u21e6').toString());
			pageBack.addActionListener(new PageBackAction());
			pageBack.setToolTipText("Move loaded data back");

			Dimension d = pageBack.getMaximumSize();
			d.width = d.height;
			//		pageBack.setMinimumSize(d);
			pageForward.setPreferredSize(d);
			pageBack.setPreferredSize(d);

			//			Character c = '\u21b7';
			showMenu = new JButton("", ps = new PamSymbol(PamSymbolType.SYMBOL_TRIANGLED, 12, 12, true, 
					Color.DARK_GRAY, Color.DARK_GRAY));
			ps.setIconHorizontalAlignment(PamSymbol.ICON_HORIZONTAL_CENTRE);
			showMenu.setToolTipText("<html>Scroll and data loading options: "+
			"Left click for scroll time, Right for other options</html>");
			showMenu.addActionListener(new ShowMenuButtonPress());
			showMenu.addMouseListener(new MenuButtonMouse());
			showMenu.setPreferredSize(d);
			
			if (hasPlayback) {
				playIcon = new ImageIcon(ClassLoader.getSystemResource("Resources/playStart.png"));
				stopIcon = new ImageIcon(ClassLoader.getSystemResource("Resources/playbackStop.png"));
				playbackButton = new JButton("",playIcon);
				playbackButton.setToolTipText("Play scroller (right click for play speed)");
				playbackButton.addActionListener(new PlayButton());
				playbackButton.addMouseListener(new PlayButtonMouse());
				playbackButton.setPreferredSize(d);
				buttonPanel.add(playbackButton);
			}
//			if (orientation == HORIZONTAL) {
//				//			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
//				buttonPanel.add(BorderLayout.WEST, pageBack);
//				buttonPanel.add(BorderLayout.EAST, pageForward);
//			}
//			else {
//				//			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
//				buttonPanel.add(BorderLayout.SOUTH, pageBack);
//				buttonPanel.add(BorderLayout.NORTH, pageForward);
//			}
//			buttonPanel.add(BorderLayout.CENTER, showMenu);
			buttonPanel.add(pageBack);
			buttonPanel.add(showMenu);
			buttonPanel.add(pageForward);
		}
		
	}
	
	/**
	 * Add a component to the scrollers mouse wheel listener. 
	 * All mouse wheel actions over that component will then be sent 
	 * to the scroller for processing. 
	 * @param component component
	 */
	public void addMouseWheelSource(Component component) {
	  component.addMouseWheelListener(new MouseWheel());
	}
	
	class MouseWheel implements MouseWheelListener {
		@Override
		public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
			doMouseWheelAction(mouseWheelEvent);
		}
	}
	
	abstract void doMouseWheelAction(MouseWheelEvent mouseWheelEvent);

	/**
	 * @return the buttonPanel
	 */
	protected JPanel getButtonPanel() {
		return buttonPanel;
	}

	class PageForwardAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			pageForward();
		}
	}
	class PageBackAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			pageBack();
		}
	}
	class ShowMenuButtonPress implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			menuButtonPress();
		}
	}

	private class MenuButtonMouse extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent me) {
			if (me.isPopupTrigger()) {
				showMenuButtonPopup(me);
			}
		}

		@Override
		public void mouseReleased(MouseEvent me) {
			if (me.isPopupTrigger()) {
				showMenuButtonPopup(me);
			}
		}

	}
	
	/**
	 * Show standard menu for mouse right click on 
	 * the middle options button. 
	 * <p>Concrete instances of the scroller can either 
	 * override this or add to the standard menu
	 * @param me Mouse event
	 */
	void showMenuButtonPopup(MouseEvent me) {
		JPopupMenu menu = getStandardOptionsMenu(this);
		if (menu != null) {
			menu.show(showMenu, me.getX(), me.getY());
		}
	}
	
	public JPopupMenu getStandardOptionsMenu(AbstractPamScrollerAWT pamScroller) {
		JPopupMenu popMenu = null;
		if (pamScroller != null && pamScroller.scrollManager != null) {
			 popMenu = pamScroller.scrollManager.getStandardOptionsMenu(pamScroller);
		}
		if (popMenu == null) {
			popMenu = new JPopupMenu();
		}
		Vector<PamMenuParts> menuParts = getPamMenuParts();		
		int added = 0;
		for (PamMenuParts menuPart : menuParts) {
			added += menuPart.addMenuItems(popMenu);
		}
		if (popMenu.getComponentCount() == 0) {
			return null;
		}
		return popMenu;
	}


	public void menuButtonPress() {
		PamScrollerData newData = LoadOptionsDialog.showDialog(null, this, showMenu);
		if (newData != null) {
			scrollerData = newData;
			rangesChangedF(getValueMillis());
		}
	}


	//	/**
	//	 * @return the visibleAmount
	//	 */
	//	public int getVisibleAmount() {
	//		return visibleAmount;
	//	}


	/**
	 * Set the visibility of the scroll bar component. 
	 * @param b
	 */
	public void setVisible(boolean b) {
		if (getComponent() == null) {
			return;
		}
		getComponent().setVisible(b);
	}

	private class PlayButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (playbackButton.getIcon() == playIcon) {
				startPlayback();
			}
			else {
				stopPlayback();
			}
		}
	}
	
	private class PlayButtonMouse extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPlaybackMenu(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPlaybackMenu(e);
			}
		}
		
	}


	@Override
	public void playbackStopped() {
		playbackButton.setIcon(playIcon);
	}

	/**
	 * Show dialog to enter playback speed. 
	 * @param e 
	 */
	public void showPlaybackMenu(MouseEvent e) {
		JPopupMenu popMenu = new JPopupMenu();
		JMenuItem menuItem;
		DecimalFormat df = new DecimalFormat("x#.##");
		double currSpeed = getScrollerData().getPlaySpeed();
		for (int i = 0; i < playSpeeds.length; i++) {
			menuItem = new JCheckBoxMenuItem(df.format(playSpeeds[i]));
			menuItem.setSelected(currSpeed == playSpeeds[i]);
			menuItem.addActionListener(new SetPlaySpeed(playSpeeds[i]));
			popMenu.add(menuItem);
		}
		popMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	private class SetPlaySpeed implements ActionListener {
		private double playSpeed;
		public SetPlaySpeed(double playSpeed) {
			this.playSpeed = playSpeed;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			getScrollerData().setPlaySpeed(playSpeed);
		}
	}
	/* (non-Javadoc)
	 * @see pamScrollSystem.AbstractPamScroller#playbackStarted()
	 */
	@Override
	public void playbackStarted() {
		playbackButton.setIcon(stopIcon);
	}

	@Override
	public boolean isShowing() {
		if (getComponent() == null) {
			return false;
		}
		else {
			return getComponent().isShowing();
		}
	}
	
	
		
}
