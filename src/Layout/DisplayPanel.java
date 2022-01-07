package Layout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;

import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;


/**
 * A standard display panel that can be incorporated into other displays, eg. the
 * Spectorgram window. Provides a JPanel and optionally axis to be drawn around it. 
 * <p>
 * The programmer must always implement the function containerNotification in order to 
 * clear the display panel ahead of where the spectrogram is currently drawing. 
 * <p>
 * There are two principle ways of drawing new data on the panel:
 * <ol>
 * <li>For data which are continuous in nature, such as waveforms or data calculated from
 * the spectrgram data itself, the new data can be drawn from within containerNotification.</li>
 * <li>For data which arrive intermittently, such as detected clicks, it is better to draw those data 
 * as and when they arrive by subscribing to the appropriate PamDataBlock and implementing code in the 
 * update function in the PamObserver interface. </li>
 * </ol>
 * 
 * 
 * @author Doug Gillespie
 * 
 * @see Layout.DisplayPanelContainer
 * @see Layout.DisplayPanelProvider
 * @see Layout.DisplayProviderList
 *
 */
abstract public class DisplayPanel {
	
	protected DisplayPanelProvider displayPanelProvider;
	
	protected DisplayPanelContainer displayPanelContainer;
	
	private JPanel outerPanel;
	
	private DisplayInnerPanel innerPanel;
	
	private BufferedImage displayImage;
	
	private Insets panelInsets;
	
	private int imageWidth, imageHeight;

	protected Color plotBackground;

	/**
	 * Standard Display panel constructor
	 * <p>
	 * Creates two J Panels, one nested inside the other with a
	 * small border. The inner panel also has a BufferedImage
	 * drawn in it which can be used for any graphics.
	 * @param displayPanelProvider
	 * @param displayPanelContainer
	 */
	public DisplayPanel(DisplayPanelProvider displayPanelProvider, DisplayPanelContainer displayPanelContainer) {
		super();
		this.displayPanelProvider = displayPanelProvider;
		this.displayPanelContainer = displayPanelContainer;
		outerPanel = new PamPanel();
		outerPanel.setLayout(new BorderLayout());
		setPanelBorder(new Insets(4, 0, 4, 0));
//		PamColors.getInstance().registerComponent(outerPanel, PamColor.BORDER);
		popupMenu = createPopupMenu();
		setInnerPanel(new DisplayInnerPanel(this));
	}
	
	/**
	 * Creates a buffered image for the actual drawing.
	 *
	 */
	private void createImage() {
		if (innerPanel == null){
			return;
		}
		imageHeight = innerPanel.getHeight();
		imageWidth = innerPanel.getWidth();
		if (imageHeight <= 0 || imageWidth <= 0) {
			return;
		}
		displayImage = new BufferedImage(imageWidth, 
				imageHeight, BufferedImage.TYPE_INT_RGB);
		plotBackground = PamColors.getInstance().getColor(PamColor.PlOTWINDOW);
		floodImage(plotBackground);
	}
	
	/**
	 * Fill the entire image with the same comour
	 * @param col
	 */
	private void floodImage(Color col) {
		if (displayImage == null) return;
		Graphics2D g2d = (Graphics2D) displayImage.getGraphics();
		g2d.setColor(PamColors.getInstance().getColor(PamColor.PlOTWINDOW));
		g2d.fillRect(0, 0, innerPanel.getWidth(), innerPanel.getHeight());		
	}
	
	private JPopupMenu popupMenu = null;
	/**
	 * Create a popup menu. <p>
	 * This should be overridden in any sub classes that want
	 * to present a menu for setting any display options. 
	 * @return reference to the popup menu for the display panel
	 */
	protected JPopupMenu createPopupMenu() {
		return null;
	}
//	
//	public JPopupMenu getPopupMenu() {
//		return popupMenu;
//	}
//
//	public void setPopupMenu(JPopupMenu popupMenu) {
//		this.popupMenu = popupMenu;
//	}

	/**
	 * Clear the entire image
	 *
	 */
	public void clearImage() {
		clearImage(0, imageWidth);
	}

	/**
	 * Clear part of the image between x1 and x2
	 * @param x1
	 * @param x2
	 */
	public void clearImage(int x1, int x2) {
		clearImage(x1, x2, false);
	}
	
	/**
	 * Clear part of the image and draw a line just to the 
	 * right of the cleared section
	 * @param x1 x position of start of clear
	 * @param x2 x position of end of clear
	 * @param drawLine draw a vertical line to show cleared region 
	 */
	public void clearImage(int x1, int x2, boolean drawLine) {
		if (displayImage == null) return;
		Graphics2D g2d = (Graphics2D) displayImage.getGraphics();
		g2d.setColor(PamColors.getInstance().getColor(PamColor.PlOTWINDOW));
		if (x1 <= x2) {
			g2d.fillRect(x1, 0, x2-x1+1, imageHeight);
			if (drawLine) {
				//			g2d.setColor(PamColors.getInstance().getColor(PamColor.PLAIN));
				g2d.setColor(Color.RED);
				g2d.drawLine(x2+1, 0, x2+1, imageHeight);
			}				
		}
		else {
			g2d.fillRect(x1, 0, imageWidth-x1+1, imageHeight);
			g2d.fillRect(0, 0, x2+1, imageHeight);
		}
	}

	/**
	 * @return the width in pixels of the image
	 */
	public int getInnerWidth() {
		return imageWidth;
	}
	
	/**
	 * @return the height in pixels of the image
	 */
	public int getInnerHeight() {
		return imageHeight;
	}

	/**
	 * 
	 * @return The display image
	 */
	public BufferedImage getDisplayImage() {
		return displayImage;
	}

	public Insets getPanelBorder() {
		return panelInsets;
	}

	public void setPanelBorder(Insets panelInsets) {
		this.panelInsets = panelInsets;
		outerPanel.setBorder(new EmptyBorder(panelInsets));
	}

	/**
	 * Each display panel must be able to provide a JPanel for incoropation
	 * into the display. 
	 * @return the outer panel
	 */
	public final JPanel getPanel() {
		return outerPanel;
	}
	
	public JPanel getInnerPanel() {
		return innerPanel;
	}
	
	/**
	 * repaints the display - needs to be called after any 
	 * changes to the image 
	 *
	 */
	public void repaint() {
		innerPanel.repaint();
		outerPanel.repaint();
	}

	/**
	 * 
	 * repaints the display - needs to be called after any 
	 * changes to the image 
	 * @param t time delay
	 */
	public void repaint(int t) {
		innerPanel.repaint(t);
		outerPanel.repaint(t);
	}

	private void setInnerPanel(DisplayInnerPanel innerPanel) {
		if (this.innerPanel != null) {
			outerPanel.remove(innerPanel);
		}
		this.innerPanel = innerPanel;
		outerPanel.add(BorderLayout.CENTER, this.innerPanel);
	}

	/**
	 * The displayPanelContainer should call destroyPanel
	 * when the panel is no longer required so that the 
	 * displayPanel can unsubscribe to any data it was
	 * observing.
	 *
	 */
	public abstract void destroyPanel();
	
	/**
	 * Called by the DisplayPanelContainer whenever the scales
	 * change - e.g. every time new data is drawn on the spectrogram.
	 * <p>
	 * The display panel can use this information in two ways:
	 * <p>
	 * Firstly, it should clear the region of the display just ahead of the current
	 * spectrogram x coordinate. 
	 * Secondly, it may draw additional data on the display.
	 * 
	 * @param displayContainer
	 * @param noteType
	 */
	public abstract void containerNotification(DisplayPanelContainer displayContainer, int noteType);
	
	public PamAxis getNorthAxis() {
		return null;
	}
	
	public PamAxis getSouthAxis() {
		return null;
	}
	
	public PamAxis getEastAxis() {
		return null;
	}
	
	public PamAxis getWestAxis() {
		return null;
	}

	public DisplayPanelContainer getDisplayPanelContainer() {
		return displayPanelContainer;
	}

	public DisplayPanelProvider getDisplayPanelProvider() {
		return displayPanelProvider;
	}
	
	class DisplayInnerPanel extends PamPanel {

		DisplayPanel displayPanel;
				
		public DisplayInnerPanel(DisplayPanel displayPanel) {
			super(PamColor.PlOTWINDOW);
			this.displayPanel = displayPanel;
			
			JLabel label = new PamLabel();
			label.setFont(PamColors.getInstance().getBoldFont());
			setLayout(new FlowLayout(FlowLayout.LEFT));
			add(label);
//			label.setBackground(Color.WHITE);
			label.setText(displayPanel.getDisplayPanelProvider().getDisplayPanelName());
			
//			PamColors.getInstance().registerComponent(this, PamColor.PlOTWINDOW);
			
			if (popupMenu != null) {
				addMouseListener(new PopupListener());
			}
		}
		
		@Override
		public void setBackground(Color bg) {
			plotBackground = bg;
			super.setBackground(plotBackground);
			floodImage(plotBackground);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (isShowing() == false) return;
			if (getWidth() != imageWidth || 
					getHeight() != imageHeight) {
				displayPanel.createImage();
			}
			if (displayPanel.displayImage == null) return;
			displayPanel.prepareImage();
			Graphics2D g2d = (Graphics2D) g;
			g2d.drawImage(displayPanel.displayImage, 0, 0, this);
		}
	}
	
	class PopupListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}
		
		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				if (popupMenu != null){
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		}
	}

	/**
	 * Called when the mouse is clicked or dragged on a spectrogram display during viewer operation. 
	 * @param chan channel for that panel
	 * @param point point on the screen (from the mouse adapter)
	 * @param mouseTime time of the mouse position in milliseconds
	 * @param mouseFreq frequency of the mouse position in Hz. 
	 */
	public void spectrogramMousePosition(int chan, Point point, long mouseTime,
			double mouseFreq) {
		
	}

	/**
	 * Called just before the image is drawn so that it can be drawn on. 
	 */
	public void prepareImage() {
		
	}
}
