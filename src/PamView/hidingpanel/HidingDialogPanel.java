package PamView.hidingpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import PamView.PamColors;
import PamView.ScreenSize;
import PamView.PamColors.PamColor;
import PamView.dialog.PamButtonAlpha;
import PamView.panel.CornerLayoutContraint;

/**
 * Something similar to the hiding panel, but doesn't get put into an existing 
 * panel, but rather floats over the top. In effect, it's it's own frame, but 
 * with no control bar so it can't be moved. Will be positioned relative to it's 
 * trigger window, but will move up or down left or right to ensure it stays 
 * within the screen coordinates. 
 * @author Doug Gillespie
 *
 */
public class HidingDialogPanel {

	private boolean modality = false;

	private int startLocation = CornerLayoutContraint.FIRST_LINE_END;

	private JButton showButton;

	private HidingDialog hidingDialog;

	private Timer hidingTimer;

	private int autoHideTime = 500;

	private boolean autoShow = true;

	private boolean autoHide = true;

	private HidingDialogComponent hidingDialogComponent;

	/**
	 * @return the hidingDialogComponent
	 */
	public HidingDialogComponent getHidingDialogComponent() {
		return hidingDialogComponent;
	}

	private float opacity = 0.75f;

	/**
	 * Component to try to fit the panel into. 
	 */
	private JComponent sizingComponent;

	public int outCount;

	private DialogParentListener parentListener;

	private JRootPane registeredRootPane;

	private Component registeredRoot;

	/**
	 * @return the sizingComponent
	 */
	public JComponent getSizingComponent() {
		return sizingComponent;
	}

	/**
	 * @param sizingComponent the sizingComponent to set
	 */
	public void setSizingComponent(JComponent sizingComponent) {
		this.sizingComponent = sizingComponent;
	}

	public HidingDialogPanel(int startLocation, HidingDialogComponent dialogComponent) {
		super();
		this.startLocation = startLocation;
		this.hidingDialogComponent = dialogComponent;

		showButton = new PamButtonAlpha("", getShowButtonImage(startLocation));
		showButton.setBackground(PamColors.getInstance().getColor(PamColor.BACKGROUND_ALPHA));
		showButton.setToolTipText("Show hidden dialog");
		showButton.setMargin(new Insets(0, 0, 0, 0));
		showButton.addActionListener(new ShowButton());
//		showButton.addMouseListener(new ShowButtonMouse());
		hidingTimer = new Timer(100, new MouseTimer());
		parentListener = new DialogParentListener();
	}

	ImageIcon getShowButtonImage(int location) {
		switch (startLocation) {
		case CornerLayoutContraint.FIRST_LINE_START:
		case CornerLayoutContraint.LAST_LINE_START:
			return new ImageIcon(ClassLoader
					.getSystemResource("Resources/SidePanelShow2.png"));
		case CornerLayoutContraint.FIRST_LINE_END:
		case CornerLayoutContraint.LAST_LINE_END:
			return new ImageIcon(ClassLoader
					.getSystemResource("Resources/SidePanelHide2.png"));
		}

		return new ImageIcon(ClassLoader
				.getSystemResource("Resources/SidePanelShow.png"));
	}

	/**
	 * Set tool tip text for the show button 
	 * @param tipText tool tip text for the show button. 
	 */
	public void setHiddenToolTipText(String tipText) {
		showButton.setToolTipText(tipText);
	}
	
	private class ShowButtonMouse extends MouseAdapter {
		@Override
		public void mouseEntered(MouseEvent arg0) {
			if (autoShow) {
				showHidingDialog(true);
			}
		}	
	}
	/**
	 * Listener for show button being pressed. 
	 * @author Doug Gillespie
	 *
	 */
	private class ShowButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			showHidingDialog(true);
		}
	}

	/**
	 * @return the startLocation
	 */
	public int getStartLocation() {
		return startLocation;
	}

	private class DialogParentListener extends ComponentAdapter implements HierarchyListener, InternalFrameListener {

		protected boolean wasShowing;
		private boolean intFrameActive = true;

		@Override
		synchronized public void componentMoved(ComponentEvent e) {
			checkDialogPosition();
		}

		@Override
		synchronized public void componentResized(ComponentEvent e) {
			checkDialogPosition();
		}

		synchronized private void checkDialogPosition() {
			if (hidingDialog!=null && hidingDialog.isVisible()) {
				setDialogPosition();
			}
		}

		@Override
		synchronized public void hierarchyChanged(HierarchyEvent hierarchyEvent) {
			//			System.out.println("HierarchyEvent: " + hierarchyEvent);
//			if (hidingDialog == null) {
//				return;
//			}
//			System.out.println(String.format("HierarchyEvent: %s flags %d, id %d, sbutton showing %s ", 
//					hierarchyEvent.getChanged().getName(), hierarchyEvent.getChangeFlags(), 
//					hierarchyEvent.getID(), new Boolean(showButton.isShowing()).toString()));
//			//			hierarchyEvent.
//			if (showButton.isShowing() == false && hidingDialog.isVisible()) {
//				wasShowing = true;
//				showHidingDialog(false);
//			}
//			//			else if (wasShowing) {
			//				showHidingDialog(true);
			//			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ComponentAdapter#componentHidden(java.awt.event.ComponentEvent)
		 */
		@Override
		synchronized public void componentHidden(ComponentEvent arg0) {
			shouldHide();
		}

		@Override
		 public void componentShown(ComponentEvent arg0) {
			mayShow();
		}

		@Override
		 public void internalFrameActivated(InternalFrameEvent arg0) {
			intFrameActive = true;
			mayShow();
		}

		@Override
		 public void internalFrameClosed(InternalFrameEvent arg0) {
			intFrameActive = false;
			shouldHide();
		}

		@Override
		 public void internalFrameClosing(InternalFrameEvent arg0) {
			intFrameActive = false;
			shouldHide();
		}

		@Override
		 public void internalFrameDeactivated(InternalFrameEvent arg0) {
			intFrameActive = false;
			shouldHide();
		}

		@Override
		 public void internalFrameDeiconified(InternalFrameEvent arg0) {
			intFrameActive = true;
			mayShow();
		}

		@Override
		public void internalFrameIconified(InternalFrameEvent arg0) {
			intFrameActive = false;
			shouldHide();
		}

		@Override
		public void internalFrameOpened(InternalFrameEvent arg0) {
			intFrameActive = true;
			mayShow();
		}

		synchronized private void shouldHide() {
			if (hidingDialog == null) {
				return;
			}
			if (hidingDialog.isVisible()) {
				wasShowing = true;
				showHidingDialog(false);
			}
		}
		synchronized private void mayShow() {
			if (hidingDialog == null) {
				return;
			}
			if (wasShowing && showButton.isShowing() && intFrameActive) {
				showHidingDialog(true);
			}
		}

	}
	/**
	 * show the hiding dialog
	 */
	public void showHidingDialog(boolean show) {
		
		
		JRootPane rootPane = SwingUtilities.getRootPane(showButton);
		Component root = SwingUtilities.getRoot(showButton);
		if (rootPane != registeredRootPane || root != registeredRoot){
			hidingDialog = null;
			if (registeredRootPane != null) {
				registeredRootPane.removeComponentListener(parentListener);
				registeredRootPane.removeHierarchyListener(parentListener);
				registeredRoot.removeComponentListener(parentListener);
				registeredRoot.removeHierarchyListener(parentListener);
			}
			registeredRootPane = rootPane;
			registeredRoot = root;
			/**
			 * Need to put an observer on both the frame it's in and also the main root
			 * window in-case any of them move. 
			 */
			Component c = showButton;
			c.addComponentListener(parentListener);
			while (c != null) {
				//			    if (RootPaneContainer.class.isAssignableFrom(c.getClass())) {
				c.removeComponentListener(parentListener);
				c.removeHierarchyListener(parentListener);
				c.addComponentListener(parentListener);
				c.addHierarchyListener(parentListener);
				//			    	RootPaneContainer rpc = (RootPaneContainer) c;
				//			    	rpc.
				//			    }
				if (JInternalFrame.class.isAssignableFrom(c.getClass())) {
					((JInternalFrame) c).addInternalFrameListener(parentListener);
				}
				c = c.getParent();
			}
			//			Component rootWin = findRootPane(showButton);
			//			if (rootWin != null) {
			//				rootWin.addComponentListener(new DialogParentListener());
			//			}
		}
		if (hidingDialog == null) {
			// 
			Window win = null;
			if (root!=null && Window.class.isAssignableFrom(root.getClass())) {
				win = (Window) root;
			}
			hidingDialog = new HidingDialog(win, this, "", false);
		}
		if (show) {
			if (autoHide) {
				outCount = 0;
				hidingTimer.restart();
				hidingTimer.start();
			}
			setDialogPosition();
			hidingDialogComponent.showComponent(true);
			hidingDialog.setVisible(true);
			parentListener.wasShowing = false;
		}
		else {
			if (hidingDialogComponent.canHide()) {
				hidingTimer.stop();
				hidingDialogComponent.showComponent(false);
				hidingDialog.setVisible(false);
			}
		}
		
		//make button translucent if the dialog is showing, otherwsie return to original colour. 
		if (show) showButton.setBackground(new Color(0,0,0,0));
		else showButton.setBackground(PamColors.getInstance().getColor(PamColor.BACKGROUND_ALPHA));
	}

	public HidingDialog getHidingDialog() {
		return hidingDialog;
	}

	private Component findRootPane(Component c) {
		if (c == null) {
			return JOptionPane.getRootFrame();
		}
		if (RootPaneContainer.class.isAssignableFrom(c.getClass())) {
			return c;
		} else {
			return findRootPane(c.getParent());
		}
	}

	private Window findWindow(Component c) {

		if (c == null) {
			return JOptionPane.getRootFrame();
		}
		//	    System.out.println("Next up = " + c.toString());
		if (Window.class.isAssignableFrom(c.getClass())) {
			return (Window) c;
		} else {
			return findWindow(c.getParent());
		}
	}

	public void setDialogPosition() {
		if (showButton.isShowing() == false) {
			return;
		}
		// still doesn't do anything
//		hidingDialog.repackDialog();
		Point dialogPos = showButton.getLocationOnScreen();
		int butHeight = showButton.getHeight();
		int butWidth = showButton.getWidth();
		Dimension dialogSize = hidingDialog.getSize();
		Dimension dialogPreferredSize = hidingDialog.getPreferredSize();
		dialogSize.height = Math.max(dialogSize.height, dialogPreferredSize.height);
		dialogSize.width = Math.max(dialogSize.width, dialogPreferredSize.width);
//		if (sizingComponent == null) System.out.println("Sizing Component is null");
		if (sizingComponent != null) {
//			System.out.println("Dialog size: "+hidingDialog.getSize().getHeight()+ " Preffered Dialog Size "+hidingDialog.getPreferredSize().getHeight()+
//					" Sizing Component: "+ sizingComponent.getHeight());
			switch (startLocation) {
			case CornerLayoutContraint.FIRST_LINE_START:
			case CornerLayoutContraint.FIRST_LINE_END:
			case CornerLayoutContraint.LAST_LINE_START:
			case CornerLayoutContraint.LAST_LINE_END:
				if (sizingComponent.getHeight()>dialogPreferredSize.height) dialogSize.height = sizingComponent.getHeight();
				else  dialogSize.height=dialogPreferredSize.height;
				break;
			case CornerLayoutContraint.NORTH:
				if (sizingComponent.getWidth()>dialogPreferredSize.width) dialogSize.width = sizingComponent.getWidth();
				else  dialogSize.width=dialogPreferredSize.width;
			}
		}

		int hPos = CornerLayoutContraint.getHorizontalAlign(startLocation);
		int vPos = CornerLayoutContraint.getVerticalAlign(startLocation);
		switch (hPos) {
		case -1:
			break;
		case 0:
			break;
		case 1:
			dialogPos.x -= (dialogSize.width-butWidth);
		}
		switch (vPos) {
		case -1:
			break;
		case 0:
			break;
		case 1:
			dialogPos.y -= (dialogSize.height-butHeight);
		}

		//		System.out.println(String.format("Dialog location = (%d,%d) size = (%d,%d)",
		//				bP.x, bP.y, dd.width, dd.height));
		Rectangle screenBounds = ScreenSize.getScreenBounds();
		if (screenBounds != null) {
			if (dialogPos.y+dialogSize.height > screenBounds.height+screenBounds.y) {
				dialogPos.y = screenBounds.height+screenBounds.y-dialogSize.height;
			}
			if (dialogPos.x+dialogSize.width > screenBounds.width+screenBounds.x) {
				dialogPos.x = screenBounds.width+screenBounds.x-dialogSize.width;
			}
			if (dialogPos.y < screenBounds.y) {
				dialogPos.y = screenBounds.y;
			}
			if (dialogPos.x < screenBounds.x) {
				dialogPos.x = screenBounds.x;
			}
		}
		//		now check it's still on the screen.
		//		Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		//		if (bP+dd.height > )
		hidingDialog.setSize(dialogSize);
		//		hidingDialog.setLocationRelativeTo(showButton);
		hidingDialog.setLocation(dialogPos);
		if (hidingDialog.isVisible()) {
			//			hidingDialog.pack();
			hidingDialog.repaint();
		}
		//		hidingDialog.setl
	}

	/**
	 * @param startLocation the startLocation to set
	 */
	public void setStartLocation(int startLocation) {
		this.startLocation = startLocation;
	}

	/**
	 * @return the showButton
	 */
	public JButton getShowButton() {
		return showButton;
	}

	private class MouseTimer implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (hidingDialog == null || hidingDialog.isVisible() == false) {
				return;
			}
			PointerInfo pointerInfo = MouseInfo.getPointerInfo();
			if (pointerInfo != null) {
				//				// print out the location !
				//				if (hidingDialog != null) {
				//					Dimension sz = hidingDialog.getSize();
				//					Dimension pz = hidingDialog.getPreferredSize();
				//					System.out.println(String.format("Hiding dialog size = (%d,%d) / (%s,%s)", sz.width,sz.height, pz.width,pz.height));
				//				}
				if (mouseOverPanel(pointerInfo.getLocation()) == false) {
					if (outCount++ > autoHideTime / 100 && hidingDialog.pinButton.isSelected() == false) {
						showHidingDialog(false);
					}
				}
				else {
					outCount = 0;
				}
			}
		}

	}

	private boolean mouseOverPanel(Point mousePoint) {
		if (hidingDialog == null) {
			return false;
		}
		Point loc = hidingDialog.getLocationOnScreen();
		int h = hidingDialog.getHeight();
		int w = hidingDialog.getWidth();
		if (mousePoint.x < loc.x || mousePoint.y < loc.y || mousePoint.x > loc.x+w || mousePoint.y > loc.y+h) {
			return false;
		}
		return true;
	}



	/**
	 * The time to wait before hiding the dialog after
	 * the mouse has moved off of it
	 * @return the autoHideTime
	 */
	public int getAutoHideTime() {
		return autoHideTime;
	}

	/**
	 * The time to wait before hiding the dialog after
	 * the mouse has moved off of it
	 * @param autoHideTime the autoHideTime to set in milliseconds
	 */
	public void setAutoHideTime(int autoHideTime) {
		this.autoHideTime = autoHideTime;
	}

	/**
	 * ? auto show the dialog as soon as the mouse moves over the showButton
	 * @return the autoShow
	 */
	public boolean isAutoShow() {
		return autoShow;
	}

	/**
	 * Set to auto show the dialog as soon as the mouse moves over the showButton
	 * @param autoShow the autoShow to set
	 */
	public void setAutoShow(boolean autoShow) {
		this.autoShow = autoShow;
	}

	/**
	 * Should the dialog automatically disappear when the mouse moves off it ? 
	 * @return the autoHide
	 */
	public boolean isAutoHide() {
		return autoHide;
	}

	/**
	 * Should the dialog automatically disappear when the mouse moves off it ? 
	 * @param autoHide the autoHide to set
	 */
	public void setAutoHide(boolean autoHide) {
		this.autoHide = autoHide;
	}

	/**
	 * @return the opacity
	 */
	public float getOpacity() {
		return opacity;
	}

	/**
	 * @param opacity the opacity to set
	 */
	public void setOpacity(float opacity) {
		this.opacity = opacity;
		if (hidingDialog != null) {
			hidingDialog.setOpacity(opacity);
			hidingDialog.setComponentOpaqueness(hidingDialogComponent.getComponent(), opacity >= 1.);
		}
	}

	/**
	 * @return the modality
	 */
	public boolean isModality() {
		return modality;
	}

	/**
	 * @param modality the modality to set
	 */
	public void setModality(boolean modality) {
		this.modality = modality;
	}

	public static Insets getDefaultButtonInsets() {
		return new Insets(1,1,1,1);
	}


}
