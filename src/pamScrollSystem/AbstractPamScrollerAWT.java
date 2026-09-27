package pamScrollSystem;

import java.awt.Adjustable;
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
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.materialdesign2.MaterialDesignR;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;

import PamView.PamMenuParts;
import PamView.PamSymbol;
import PamView.component.PamFontIcon;
import PamView.component.PamFontIcon.IconColour;
import PamView.component.PamSettingsIconButton;

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

	/**
	 * Size of the glyphs in the scroller buttons. These used to be 12 pixel
	 * {@link PamSymbol} triangles in a fixed blue and dark grey, which stayed dark
	 * however the colour scheme was set. They are now Ikonli icons drawn through
	 * {@link PamFontIcon}: the paging buttons keep their blue through
	 * {@link IconColour#ACCENT}, which follows the scheme, and the rest take the
	 * colour of the button they sit on.
	 */
	private static final int ICON_SIZE = PamSettingsIconButton.NORMAL_SIZE;

	/**
	 * Icon size the buttons themselves are sized from, which is deliberately
	 * smaller than {@link #ICON_SIZE} - see {@link #buttonSize()}.
	 */
	private static final int BUTTON_ICON_SIZE = 16;

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
			pageForward = new JButton("", PamFontIcon.of(MaterialDesignF.FAST_FORWARD, ICON_SIZE, IconColour.ACCENT));
			//		pageForward = new JButton(new Character('\u21F0').toString());
			pageForward.addActionListener(new PageForwardAction());
			pageForward.setToolTipText("Move loaded data forward");

			pageBack = new JButton("", PamFontIcon.of(MaterialDesignR.REWIND, ICON_SIZE, IconColour.ACCENT));
			//		pageBack = new JButton(new Character('\u21e6').toString());
			pageBack.addActionListener(new PageBackAction());
			pageBack.setToolTipText("Move loaded data back");

			Dimension d = buttonSize();
			//		pageBack.setMinimumSize(d);
			pageForward.setPreferredSize(d);
			pageBack.setPreferredSize(d);

			//			Character c = '\u21b7';
			showMenu = new JButton("", PamFontIcon.of(MaterialDesignM.MENU_DOWN, ICON_SIZE));
			showMenu.setToolTipText("<html>Scroll and data loading options: "+
			"Left click for scroll time, Right for other options</html>");
			showMenu.addActionListener(new ShowMenuButtonPress());
			showMenu.addMouseListener(new MenuButtonMouse());
			showMenu.setPreferredSize(d);
			
			if (hasPlayback) {
				playIcon = PamFontIcon.of(MaterialDesignP.PLAY, ICON_SIZE);
				stopIcon = PamFontIcon.of(MaterialDesignS.STOP, ICON_SIZE);
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
	 * The square that every button in the scroller strip is set to.
	 * <p>
	 * MaterialDesign glyphs carry a good deal of padding inside their em square, so
	 * an icon big enough to have the same visual weight as the 12 pixel symbols
	 * these buttons used to hold would, if the buttons were sized from it, make the
	 * whole strip taller than the displays have always been laid out around. Size
	 * the buttons from a smaller icon instead and let the glyph sit slightly proud
	 * of the button's content box - it still has room to draw, since the ink is only
	 * about half the em square.
	 *
	 * @return the square to give each of the scroller buttons.
	 */
	private static Dimension buttonSize() {
		Dimension d = new JButton("", PamFontIcon.of(MaterialDesignR.REWIND, BUTTON_ICON_SIZE)).getMaximumSize();
		d.width = d.height;
		return d;
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
