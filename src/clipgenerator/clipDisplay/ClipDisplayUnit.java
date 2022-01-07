package clipgenerator.clipDisplay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;

import PamController.PamController;
import soundPlayback.ClipPlayback;
import PamUtils.FrequencyFormat;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.PamSymbol;
import PamView.panel.CornerLayout;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.PamPanel;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.modifier.SymbolModType;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clipgenerator.ClipDataUnit;

/**
 * Graphic component for a single clip display. 
 *
 *
 * @author Doug Gillespie
 *
 */
public class ClipDisplayUnit extends PamPanel {
	/*
	 * will need at least three nested panels with different borders to allow for the colouring 
	 * and things we'll want to do. 
	 */

//	private JLayeredPane layeredPanel;
	private PamPanel mainPanel;
	private ClipDisplayPanel clipDisplayPanel;
	private ClipDataUnit clipDataUnit;
	private PamDataUnit triggerDataUnit; // data unit that triggered the clip. 
//	private JPanel clipPanel;
	private JPanel axisPanel;
	private ImagePanel imagePanel;
	private BufferedImage image;
	private int fontHeight;
	private int borderSize;
	private int fontAscent;
	private FontMetrics fm;
	private ClipDisplayDecorations displayDecorations;
	private Color chanCol;
	private boolean highlight;
	private HighlightPane highlighPane;

	private static Color highlightCol = new Color(255,255,0,32);
	private static Color transparentCol = new Color(0,0,0,0);
	
	public JComponent getComponent() {
		return this;
	}
	/**
	 * @return the displayDecorations
	 */
	public ClipDisplayDecorations getDisplayDecorations() {
		return displayDecorations;
	}
	
	public void removeDisplayDecorations() {
		displayDecorations.removeDecoration();
	}

	public ClipDisplayUnit(ClipDisplayPanel clipDisplayPanel,
			ClipDataUnit clipDataUnit, PamDataUnit triggerDataUnit) {
		super();
		this.clipDisplayPanel = clipDisplayPanel;
		this.clipDataUnit = clipDataUnit;
		this.triggerDataUnit = triggerDataUnit;
//		layeredPanel = new JLayeredPane();
//		this.setLayout(new GridLayout(1,1));
		mainPanel = this;
//		this.add(mainPanel, Integer.valueOf(0));
		
		displayDecorations = clipDisplayPanel.getClipDisplayParent().getClipDecorations(this);
		mainPanel.setLayout(new BorderLayout());
		ImageMouse imageMouse = new ImageMouse();
		addMouseListener(imageMouse);
		axisPanel = new ClipAxisPanel(new BorderLayout());
//		axisPanel.addMouseListener(imageMouse);
//		axisPanel.addMouseMotionListener(imageMouse);
		imagePanel = new ImagePanel(new BorderLayout());
		imagePanel.addMouseListener(imageMouse);
//		imagePanel.addMouseListener(imageMouse);
//		imagePanel.addMouseMotionListener(imageMouse);
		JLayeredPane layeredPane = new LayeredPane();
//GridBagConstraints c = new GridBagConstraints();
		CornerLayoutContraint c = new CornerLayoutContraint();
		c.anchor = CornerLayoutContraint.FILL;
		layeredPane.setLayout(new CornerLayout(c));
		//		layeredPane.setLayout(new BorderLayout());
		layeredPane.add(axisPanel, c, 1);
		layeredPane.add(highlighPane = new HighlightPane(),c, 0);
//		layeredPane.setLayer(axisPanel, 0);
		mainPanel.add(BorderLayout.CENTER, layeredPane);
		axisPanel.add(BorderLayout.CENTER, imagePanel);
//		imagePanel.setBackground(Color.RED);
//		if (triggerDataUnit != null) {
//			imagePanel.setToolTipText(triggerDataUnit.getSummaryString());
//		}
//		else {
//			imagePanel.setToolTipText(clipDataUnit.getSummaryString());
//		}
		imagePanel.setToolTipText("Clip spectrogram");
		mainPanel.setBorder(new EmptyBorder(2,2,2,2));
		setBorderColour();
//		this.setBackground(Color.RED);
		
		displayDecorations.decorateDisplay();
		
//		axisPanel.setFont(this.clipDisplayPanel.clipFont);
		fm = axisPanel.getFontMetrics(axisPanel.getFont());
		fontHeight = fm.getAscent(); // the numbers used don't have any decent !
		borderSize = fontHeight + 1;
		fontAscent = fm.getAscent();
		axisPanel.setBorder(new EmptyBorder(borderSize, borderSize, borderSize, 2));
		
		createImage();
		setImageSize();
	}
	
	public void setBorderColour() {
		PamSymbol symbol = getBorderSymbol();
		if (symbol == null) {
			return;
		}
		mainPanel.setBackground(symbol.getLineColor());
		if (symbol.isFill()) {
			axisPanel.setBackground(symbol.getFillColor());
			Color foreground = getforeGround(symbol.getFillColor());
			if (foreground != null) {
				axisPanel.setForeground(foreground);
			}
		}
		else {
			axisPanel.setBackground(PamColors.getInstance().getBorderColour());
			axisPanel.setForeground(PamColors.getInstance().getForegroudColor(PamColor.AXIS));
		}
	}
	
	/**
	 * Get a contrasting colour for the foreground of the display. 
	 * @param fillColor
	 * @return
	 */
	private Color getforeGround(Color fillColor) {
		if (fillColor == null) {
			return null;
		}
//		int sCol = fillColor.getRed()+fillColor.getBlue()+fillColor.getGreen();
		int mCol = Math.max(fillColor.getRed(), Math.max(fillColor.getGreen(), fillColor.getBlue()));
		// bigger is lighter so when it's above a certain value, switch to black. 
		return mCol > 128 ? Color.BLACK : Color.WHITE;
	}
	private PamSymbol getBorderSymbol() {
		PamSymbol symbol = clipDisplayPanel.getSymbolChooser().getPamSymbol(clipDisplayPanel.getClipDataProjector(), getClipDataUnit());
		return symbol;
	}
	
	public void layoutUnit(boolean needNewImage) {
		// get a new image and call repaint. 
		if (needNewImage) {
			createImage();
		}
		setImageSize();
		mainPanel.doLayout();
		axisPanel.doLayout();
	}
	
	/**
	 * Repaint both the border and the image panel. 
	 */
	public void repaintUnit() {
//		axisPanel.setBackground(PamColors.getInstance().getColor(PamColor.BORDER));
		setBorderColour();
		mainPanel.repaint();
		axisPanel.repaint();
		imagePanel.repaint();
		if (highlighPane != null) {
			highlighPane.setHighlight();
			highlighPane.repaint();
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		displayDecorations.drawOnClipBorder(g);
	}

	private void setImageSize() {		
		if (image != null) {
			ClipDisplayParameters clipParams = clipDisplayPanel.clipDisplayParameters;
			imagePanel.setPreferredSize(new Dimension((int) (image.getWidth()*clipParams.imageHScale), 
					(int) (image.getHeight()*clipParams.imageVScale)));
		}
		else {
			imagePanel.setPreferredSize(new Dimension(60,60));
		}
		axisPanel.doLayout();
		imagePanel.invalidate();
	}
	
	private void createImage() {
		ClipDisplayParameters clipParams = clipDisplayPanel.clipDisplayParameters;
		int fftLen = 1<<clipParams.getLogFFTLength();
		int iChan = PamUtils.getLowestChannel(clipDataUnit.getChannelBitmap());
		image = clipDataUnit.getClipImage(iChan, fftLen, fftLen/2, clipParams.amlitudeMinVal, 
				clipParams.amlitudeMinVal + clipParams.amplitudeRangeVal, clipDisplayPanel.getColourArray().getColours());
	}

	private JPopupMenu createPopupMenu(MouseEvent e) {
		JPopupMenu popupMenu = new JPopupMenu();
//		JMenuItem menuItem;
//		menuItem = new JMenuItem("Play clip");
//		popupMenu.add(menuItem);
//		menuItem.addActionListener(new PlayClip());
		popupMenu = displayDecorations.addDisplayMenuItems(popupMenu);
		PamDataBlock dataBlock = this.clipDataUnit.getParentDataBlock();
		if (dataBlock != null) {
			List<JMenuItem> menuItems = dataBlock.getDataUnitMenuItems(null, null, clipDataUnit);
			if (menuItems != null) {
				for (JMenuItem item : menuItems) {
					popupMenu.add(item);
				}
			}
		}
		
		clipDisplayPanel.addPopupMenuItems(popupMenu, e);
		
		PamSymbolChooser symbolChooser = clipDisplayPanel.getSymbolChooser();
		if (symbolChooser instanceof StandardSymbolChooser) {
			StandardSymbolChooser standardChooser = (StandardSymbolChooser) symbolChooser;
			ArrayList<JMenuItem> menuItems = standardChooser.getQuickMenuItems(PamController.getMainFrame(), clipDisplayPanel, "Border colour:", 
					SymbolModType.LINECOLOUR, true);
			if (menuItems != null && menuItems.size() > 0) {
				popupMenu.addSeparator();
				for (JMenuItem mi : menuItems) {
					popupMenu.add(mi);
				}
			}
		}
		
		return popupMenu;		
	}
	
	private class ClipAxisPanel extends PamPanel {

		public ClipAxisPanel(LayoutManager layout) {
			super(layout);
		}
		public void paint(Graphics g) {
			super.paint(g);
			String tStr = PamCalendar.formatTime(clipDataUnit.getTimeMilliseconds(), true);
			g.drawString(tStr, borderSize, fontAscent);
			
			
			String lenString = String.format("%3.2fs", (float)clipDataUnit.getSampleDuration() / clipDisplayPanel.getSampleRate());
			Rectangle2D strSize = fm.getStringBounds(lenString, g);
			g.drawString(lenString, (int) (getWidth()-strSize.getWidth()), getHeight()-fm.getDescent());

			int iChan = PamUtils.getLowestChannel(clipDataUnit.getChannelBitmap());
			String chString = String.format("Ch%d", iChan);
			g.drawString(chString, fm.charWidth('c')/2, getHeight()-fm.getDescent());

			double f = clipDataUnit.getDisplaySampleRate()/2.*clipDisplayPanel.clipDisplayParameters.frequencyScale;
			String fStr = FrequencyFormat.formatFrequency(f, true);
			strSize = fm.getStringBounds(fStr, g);
			int x = fontAscent;
			int y = (int) (strSize.getWidth() + borderSize);
			Graphics2D g2d = (Graphics2D) g;
			g2d.translate(x, y);
			g2d.rotate(-Math.PI/2.);
			g2d.drawString(fStr, 0,0);
			g2d.rotate(+Math.PI/2.);
			g2d.translate(-x, -y);
			
			displayDecorations.drawOnClipAxis(g);
		}
		
		/* (non-Javadoc)
		 * @see PamView.PamPanel#setBackground(java.awt.Color)
		 */
		@Override
		public void setBackground(Color bg) {
			/**
			 * Ideally, the information in the displayDecorations would take the 
			 * form of a SymbolModifier, but that's quite a faff to sort out, so 
			 * leave this bodge in place for now. 
			 */
			Color bgCol = displayDecorations.getClipBackground();
			if (bgCol != null){
				super.setBackground(bgCol);
			}
			else {
				super.setBackground(bg);
			}
		}
		
		
	}
	
	private class LayeredPane extends JLayeredPane {

		@Override
		public Dimension getPreferredSize() {
			return axisPanel.getPreferredSize();
		}

		@Override
		public Dimension getMaximumSize() {
			return axisPanel.getMaximumSize();
		}

		@Override
		public Dimension getMinimumSize() {
			return axisPanel.getMinimumSize();
		}
		
	}
	
	private class ImagePanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ImagePanel(BorderLayout borderLayout) {
			super(borderLayout);;
//			this.setComponentPopupMenu(createPopupMenu());
		}

//		/* (non-Javadoc)
//		 * @see javax.swing.JComponent#getComponentPopupMenu()
//		 */
//		@Override
//		public JPopupMenu getComponentPopupMenu() {
//			return createPopupMenu();
//		}
		
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (image != null) {
				int imageYStart = 0;
				double miss = (1.-clipDisplayPanel.clipDisplayParameters.frequencyScale);
				imageYStart = (int) (image.getHeight() * miss);
				imageYStart = Math.max(imageYStart, 1);
				g.drawImage(image, 0, 0, getWidth(), getHeight(), 0, imageYStart, image.getWidth(), image.getHeight(), null);
				if (clipDisplayPanel.clipDisplayParameters.showTriggerOverlay) {
					drawTriggerDataUnit(g);
				}
//				return;
			}
			else {
				String str = "no image";
				int x = (getWidth()-getFontMetrics(getFont()).stringWidth(str)) / 2;
				g.drawString(str, x, getHeight()/2);
			}
		}

		private void drawTriggerDataUnit(Graphics g) {
			if (triggerDataUnit == null) {
				return;
			}
			ClipDataProjector proj = clipDisplayPanel.getClipDataProjector();
			PamDataBlock dataBlock = triggerDataUnit.getParentDataBlock();
			if (dataBlock == null || dataBlock.canDraw(proj) == false) {
				return;
			}
			proj.setClipStart(clipDataUnit.getTimeMilliseconds());
			dataBlock.drawDataUnit(g, triggerDataUnit, proj);
			
		}

//		@Override
//		public String getToolTipText() {
////			if (triggerDataUnit != null) {
////				return triggerDataUnit.getSummaryString();
////			}
////			if (clipDataUnit != null) {
////				return clipDataUnit.getSummaryString();
////			}
//			return "Clip Spectrogram";
//		}

		@Override
		public String getToolTipText(MouseEvent event) {
			// TODO Auto-generated method stub
		return clipDataUnit.getSummaryString();
//			return "Boo";
		}
		
		
	}
	
	private class HighlightPane extends JPanel {
		

		public HighlightPane() {
			super();
			setHighlight();
		}

		public void setHighlight() {
			setBackground(isHighlight() ? highlightCol : transparentCol);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
		}
		
	}
	
	private class PlayClip implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			ClipPlayback.getInstance().playClip(clipDataUnit.getRawData(), clipDataUnit.getParentDataBlock().getSampleRate(), true);
		}		
	}

	/**
	 * @return the clipDataUnit
	 */
	public ClipDataUnit getClipDataUnit() {
		return clipDataUnit;
	}
	
	/**
	 * Add a component to the display unit to be inserted into the 
	 * North of a BorderLayout
	 * @param northComponent component to add.
	 */
	public void setNorthComponent(JComponent northComponent) {
		mainPanel.add(BorderLayout.NORTH, northComponent);
	}
	
	/**
	 * Add a component to the display unit to be inserted into the 
	 * South of a BorderLayout
	 * @param northComponent component to add.
	 */
	public void setSouthComponent(JComponent southComponent) {
		mainPanel.add(BorderLayout.SOUTH, southComponent);
	}
	
	/**
	 * Add a component to the display unit to be inserted into the 
	 * East of a BorderLayout
	 * @param eastComponent component to add.
	 */
	public void setEastComponent(JComponent eastComponent) {
		mainPanel.add(BorderLayout.EAST, eastComponent);
	}
	
	/**
	 * Add a component to the display unit to be inserted into the 
	 * West of a BorderLayout
	 * @param northComponent component to add.
	 */
	public void setWestComponent(JComponent westComponent) {
		mainPanel.add(BorderLayout.WEST, westComponent);
	}
	
	/**
	 * 
	 * @return a reference to the central image panel (so you can add a dropdown menu, etc.). 
	 */
	public JPanel getImagePanel() {
		return imagePanel;
	}
	
	/**
	 * 
	 * @return a reference to the main axis panel (so you can add a dropdown menu, etc.). 
	 */
	public JPanel getAxisPanel() {
		return axisPanel;
	}

	/**
	 * @return the clipDisplayPanel
	 */
	public JPanel getimagePanel() {
		return imagePanel;
	}
	
	
	/**
	 * Added this class as the first step in being able to select clips to a Group Detection
	 * Localiser group, or something similar.  Had it mostly completed, just tweaking what
	 * happens if you click on a clip versus holding the button down versus dragging across
	 * a clip.  The main code was in ClipDisplayPanel; the code below basically just caught
	 * mouse events that occurred over a clip image and passed them back to the ClipDisplayPanel
	 * for processing.
	 * Going to abandon this for now, as adding Group Detection Localiser functionality will entail
	 * a lot more work than we can do at the moment.  But I wanted to leave this code in case we ever
	 * come back to it.  To restore it, don't forget to uncomment the lines adding actionlisteners
	 * in the ClipDisplayUnit constructor.
	 * 
	 * @author mo55
	 *
	 */
	class ImageMouse extends MouseAdapter {
		
		public ImageMouse() {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
				return;
			}
			clipDisplayPanel.mousePressed(e, ClipDisplayUnit.this);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
				return;
			}
			clipDisplayPanel.mouseReleased(e, ClipDisplayUnit.this);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
				return;
			}
			if (e.getButton() == MouseEvent.BUTTON1) {
				clipDisplayPanel.mouseClicked(e, ClipDisplayUnit.this);
			}
//			toggleHighlight();
		}
		
//		@Override
//		public void mouseDragged(MouseEvent e) {
//		}

//		@Override
//		public void mouseEntered(MouseEvent e) {
////			clipDisplayPanel.selectClip(theUnit);
//		}

//		@Override
//		public void mouseMoved(MouseEvent e) {
//		}
	}

//	/**
//	 * @param red
//	 */
//	public void changeColor(Color color) {
//		if (color==null) color=chanCol;
//		mainPanel.setBackground(color);
//	}

	public void showPopupMenu(MouseEvent e) {
		JPopupMenu popMenu = createPopupMenu(e);
		if (popMenu != null) {
			popMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
	/**
	 * @return the highlight
	 */
	public boolean isHighlight() {
		return highlight;
	}

	/**
	 * @param highlight the highlight to set
	 */
	public void setHighlight(boolean highlight) {
		if (this.highlight == highlight) {
			return;
		}
		this.highlight = highlight;
		repaintUnit();
	}
	
	/**
	 * Flip the highlight state. 
	 * @return
	 */
	public boolean toggleHighlight() {
		setHighlight(!isHighlight());
		return isHighlight();
	}
}
