package PamView.menu;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * A component that looks very much like a normal popup menu, but the 
 * show(...) function doesn't return until any menu actions are complete. 
 * This can be useful in popups where you need to act after one of the 
 * dialogs in the system has been opened and closed. <br>
 * All the location setting code has been shamelessly copied from JPopupMenu
 * @author dg50
 *
 */
public class ModalPopupMenu extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPanel mainPanel;
	private GridBagConstraints c;
	private ArrayList<AbstractButton> buttons = new ArrayList<>();
	boolean inAction = false; // currently in an action, so don't close if focus lost. 
	boolean actionTaken = false; // flag for any action taken
	private Component invoker;
	
	/**
	 * Create a modal popup menu. This behaves in almost exactly the
	 * same way as JPopupMenu, except that the show(...) function will 
	 * not return until whatever actions are fired by the menu items have
	 * completed. 
	 */
	public ModalPopupMenu() {
		mainPanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = c.gridy = 0;

		setContentPane(mainPanel);
		
		setModal(true);
		setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
		
		setUndecorated(true);
        setFocusTraversalKeysEnabled(false);

        enableEvents(AWTEvent.MOUSE_EVENT_MASK);

        
        
		pack();
		
		addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
//				System.out.println("Lost focus");
				if (!inAction) {
					setVisible(false);
				}
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	@Override
	public Component add(Component menuItem) {

		if (menuItem instanceof AbstractButton) {
			buttons.add((AbstractButton) menuItem);
		}
		mainPanel.add(menuItem, c);
		c.gridy++;
		return menuItem;
	}

	/**
	 * Displays the popup menu at the position x,y in the coordinate
     * space of the component invoker.<br>
     * Then blocks using a DOCUMENT_MODAL
	 * dialog until one of the menu items is selected AND the action called by
	 * the menu item has been completed. Would need to override to get an actual result 
	 * from any action, but at least the caller knows something may have been done. 
	 * @param invoker
	 * @param x
	 * @param y
	 * @return True if a menu was actioned, false if closed without doing anything. 
	 */
	public boolean show(Component invoker, int x, int y) {
		setInvoker(invoker);
		pack();
		hijackActions();
		setLocation(invoker, x, y);
		setVisible(true);
//		System.out.println("Modeless menu return");
		return actionTaken;
	}

	/**
	 * Take the actions from the buttons and replace them with wrappers that
	 * close the dialog when they are complete. 
	 */
	private void hijackActions() {
		for (AbstractButton button : buttons) {
			ActionListener[] als = button.getActionListeners();
			if (als == null)  continue;
			for (int i = 0; i < als.length; i++) {
				button.addActionListener(new HyperAction(als[i]));
				button.removeActionListener(als[i]);
			}
		}
		
	}
	
	/**
	 * Wrapper around an existing action listener to a) prevent
	 * the menu closing when it loses the focus to a window opened
	 * during the action event and then does close the menu when 
	 * the action is complete.  
	 * @author dg50
	 *
	 */
	private class HyperAction implements ActionListener {

		private ActionListener realAction;
		
		public HyperAction(ActionListener actionListener) {
			realAction = actionListener;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			inAction = true;
			actionTaken = true;
			try {
				realAction.actionPerformed(e);
				//			System.out.println("Real action complete");
			}
			catch (Exception x) {};
			inAction = false;
			setVisible(false);
		}
		
	}

	private void setLocation(Component invoker, int x, int y) {

		 Point invokerOrigin;
		 
	        if (invoker != null) {
	            invokerOrigin = invoker.getLocationOnScreen();

	            // To avoid integer overflow
	            long lx, ly;
	            lx = ((long) invokerOrigin.x) +
	                 ((long) x);
	            ly = ((long) invokerOrigin.y) +
	                 ((long) y);
	            if(lx > Integer.MAX_VALUE) lx = Integer.MAX_VALUE;
	            if(lx < Integer.MIN_VALUE) lx = Integer.MIN_VALUE;
	            if(ly > Integer.MAX_VALUE) ly = Integer.MAX_VALUE;
	            if(ly < Integer.MIN_VALUE) ly = Integer.MIN_VALUE;

	            setLocation((int) lx, (int) ly);
	        } else {
	            setLocation(x, y);
	        }
	}
	/**
     * Returns an point which has been adjusted to take into account the
     * desktop bounds, taskbar and multi-monitor configuration.
     * <p>
     * This adjustment may be cancelled by invoking the application with
     * -Djavax.swing.adjustPopupLocationToFit=false
     */
    Point adjustPopupLocationToFitScreen(int xPosition, int yPosition) {
        Point popupLocation = new Point(xPosition, yPosition);

//        if(popupPostionFixDisabled == true || GraphicsEnvironment.isHeadless()) {
//            return popupLocation;
//        }

        // Get screen bounds
        GraphicsConfiguration gc = getCurrentGraphicsConfiguration(popupLocation);
        if (gc == null) {
            // If we don't have GraphicsConfiguration use primary screen
            gc = GraphicsEnvironment.getLocalGraphicsEnvironment().
                            getDefaultScreenDevice().getDefaultConfiguration();
        }
        Rectangle scrBounds = gc.getBounds();

        // Calculate the screen size that popup should fit
        Dimension popupSize = this.getPreferredSize();
        long popupRightX = (long)popupLocation.x + (long)popupSize.width;
        long popupBottomY = (long)popupLocation.y + (long)popupSize.height;
        int scrWidth = scrBounds.width;
        int scrHeight = scrBounds.height;

        if (!canPopupOverlapTaskBar()) {
            // Insets include the task bar. Take them into account.
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Insets scrInsets = toolkit.getScreenInsets(gc);
            scrBounds.x += scrInsets.left;
            scrBounds.y += scrInsets.top;
            scrWidth -= scrInsets.left + scrInsets.right;
            scrHeight -= scrInsets.top + scrInsets.bottom;
        }
        int scrRightX = scrBounds.x + scrWidth;
        int scrBottomY = scrBounds.y + scrHeight;

        // Ensure that popup menu fits the screen
        if (popupRightX > (long) scrRightX) {
            popupLocation.x = scrRightX - popupSize.width;
        }

        if (popupBottomY > (long) scrBottomY) {
            popupLocation.y = scrBottomY - popupSize.height;
        }

        if (popupLocation.x < scrBounds.x) {
            popupLocation.x = scrBounds.x;
        }

        if (popupLocation.y < scrBounds.y) {
            popupLocation.y = scrBounds.y;
        }

        return popupLocation;
    }

    /**
     * Tries to find GraphicsConfiguration
     * that contains the mouse cursor position.
     * Can return null.
     */
    private GraphicsConfiguration getCurrentGraphicsConfiguration(
            Point popupLocation) {
        GraphicsConfiguration gc = null;
        GraphicsEnvironment ge =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        for(int i = 0; i < gd.length; i++) {
            if(gd[i].getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
                GraphicsConfiguration dgc =
                    gd[i].getDefaultConfiguration();
                if(dgc.getBounds().contains(popupLocation)) {
                    gc = dgc;
                    break;
                }
            }
        }
        // If not found and we have invoker, ask invoker about his gc
        if(gc == null && getInvoker() != null) {
            gc = getInvoker().getGraphicsConfiguration();
        }
        return gc;
    }

    /**
     * Returns whether popup is allowed to be shown above the task bar.
     */
    static boolean canPopupOverlapTaskBar() {
//        boolean result = true;
//
//        Toolkit tk = Toolkit.getDefaultToolkit();
//        if (tk instanceof SunToolkit) {
//            result = ((SunToolkit)tk).canPopupOverlapTaskBar();
//        }
//
//        return result;
    	return true;
    }

	/**
	 * @return the invoker
	 */
	public Component getInvoker() {
		return invoker;
	}

	/**
	 * @param invoker the invoker to set
	 */
	public void setInvoker(Component invoker) {
		this.invoker = invoker;
	}

	@Override
	public void setLocation(int x, int y) {
		Point p = adjustPopupLocationToFitScreen(x, y);
		super.setLocation(p.x, p.y);
	}

	@Override
	protected void processKeyEvent(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			setVisible(false);
		}
		super.processKeyEvent(e);
	}

}
