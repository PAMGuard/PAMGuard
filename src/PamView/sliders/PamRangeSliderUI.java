package PamView.sliders;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 * UI delegate for the RangeSlider component.  RangeSliderUI paints two thumbs,
 * one for the lower value and one for the upper value.
 * 
 *<P> PamRangeSliderUI is based on code from Ernie Yu. 
 *<b>
 *http://ernienotes.wordpress.com/2010/12/27/creating-a-java-swing-range-slider/ (28/09/2013)
 *</b>
 */
public class PamRangeSliderUI extends PamSliderUI {

	/** Colour of selected range. */
	private Color rangeColor = Color.GREEN;

	private Color upperThumbFill=new Color(200,200,200);

	private Color upperThumbOutline=Color.GRAY;

	private Color lowerThumbFill=new Color(200,200,200);

	private Color lowerThumbOutline=Color.GRAY;


	/** Location and size of thumb for upper value. */
	private Rectangle upperThumbRect;

	/** Indicator that determines whether upper thumb is selected. */
	private boolean upperThumbSelected;

	/** Indicator that determines whether lower thumb is being dragged. */
	private transient boolean lowerDragging;
	/** Indicator that determines whether upper thumb is being dragged. */
	private transient boolean upperDragging;
	/** IndicatorS for direction track dragging*/
	private final static int DRAG_UP=0;
	private final static int DRAG_DOWN=1;
	private final static int DRAG_LEFT=2;
	private final static int DRAG_RIGHT=3;
	private final static int NO_DRAG=4;
	/**Indicator which indicates whether area between thumbs is being dragged*/
	private transient boolean bothDragging;
	/**Indicator for drag direction*/
	private  transient int dragDirection=NO_DRAG;
	/**Indicator that determines whether user can drag the track between thumbs (as well as the thumbs)*/
	private boolean trackDragging;

	/**
	 * Constructs a RangeSliderUI for the specified slider component.
	 * @param b RangeSlider
	 */
	public PamRangeSliderUI(PamRangeSlider b) {
		super(b);
	}

	/**
	 * Installs this UI delegate on the specified component. 
	 */
	@Override
	public void installUI(JComponent c) {
		upperThumbRect = new Rectangle();
		super.installUI(c);
	}

	/**
	 * Creates a listener to handle track events in the specified slider.
	 */
	@Override
	protected TrackListener createTrackListener(JSlider slider) {
		return new RangeTrackListener();
	}

	/**
	 * Creates a listener to handle change events in the specified slider.
	 */
	@Override
	protected ChangeListener createChangeListener(JSlider slider) {
		return new ChangeHandler();
	}

	/**
	 * Updates the dimensions for both thumbs. 
	 */
	@Override
	protected void calculateThumbSize() {
		// Call superclass method for lower thumb size.
		super.calculateThumbSize();

		// Set upper thumb size.
		upperThumbRect.setSize(thumbRect.width, thumbRect.height);
	}

	/**
	 * Updates the locations for both thumbs.
	 */
	@Override
	protected void calculateThumbLocation() {
		// Call superclass method for lower thumb location.
		super.calculateThumbLocation();

		// Adjust upper value to snap to ticks if necessary.
		if (slider.getSnapToTicks()) {
			int upperValue = slider.getValue() + slider.getExtent();
			int snappedValue = upperValue; 
			int majorTickSpacing = slider.getMajorTickSpacing();
			int minorTickSpacing = slider.getMinorTickSpacing();
			int tickSpacing = 0;

			if (minorTickSpacing > 0) {
				tickSpacing = minorTickSpacing;
			} else if (majorTickSpacing > 0) {
				tickSpacing = majorTickSpacing;
			}

			if (tickSpacing != 0) {
				// If it's not on a tick, change the value
				if ((upperValue - slider.getMinimum()) % tickSpacing != 0) {
					float temp = (float)(upperValue - slider.getMinimum()) / (float)tickSpacing;
					int whichTick = Math.round(temp);
					snappedValue = slider.getMinimum() + (whichTick * tickSpacing);
				}

				if (snappedValue != upperValue) { 
					slider.setExtent(snappedValue - slider.getValue());
				}
			}
		}

		// Calculate upper thumb location.  The thumb is centered over its 
		// value on the track.
		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			int upperPosition = xPositionForValue(slider.getValue() + slider.getExtent());
			upperThumbRect.x = upperPosition - (upperThumbRect.width / 2);
			upperThumbRect.y = trackRect.y;

		} else {
			int upperPosition = yPositionForValue(slider.getValue() + slider.getExtent());
			upperThumbRect.x = trackRect.x;
			upperThumbRect.y = upperPosition - (upperThumbRect.height / 2);
		}
	}



	/**
	 * Paints the slider.  The selected thumb is always painted on top of the
	 * other thumb.
	 */
	@Override
	public void paint(Graphics g, JComponent c) {
		super.paint(g, c);

		Rectangle clipRect = g.getClipBounds();
		if (upperThumbSelected) {
			// Paint lower thumb first, then upper thumb.
			if (clipRect.intersects(thumbRect)) {
				paintThumb(g, thumbRect, lowerThumbFill, lowerThumbOutline);
			}
			if (clipRect.intersects(upperThumbRect)) {
				paintThumb(g, upperThumbRect, upperThumbFill, upperThumbOutline);
			}

		} else {
			// Paint upper thumb first, then lower thumb.
			if (clipRect.intersects(upperThumbRect)) {
				paintThumb(g, upperThumbRect, upperThumbFill, upperThumbOutline);
			}
			if (clipRect.intersects(thumbRect)) {
				paintThumb(g, thumbRect, lowerThumbFill, lowerThumbOutline);
			}
		}
	}

	/**
	 * Paints the track.
	 */
	@Override
	public void paintTrack(Graphics g) {
		// Draw track.
		super.paintTrack(g);

		Rectangle trackBounds = trackRect;

		if (slider.getOrientation() == JSlider.HORIZONTAL) {
			// Determine position of selected range by moving from the middle
			// of one thumb to the other.
			int lowerX = thumbRect.x + (thumbRect.width / 2);
			int upperX = upperThumbRect.x + (upperThumbRect.width / 2);

			// Determine track position.
			int cy = (trackBounds.height / 2) - 2;

			// Save color and shift position.
			Color oldColor = g.getColor();
			g.translate(trackBounds.x, trackBounds.y + cy);

			// Draw selected range.
			g.setColor(rangeColor);
			for (int y = 0; y <= 3; y++) {
				g.drawLine(lowerX - trackBounds.x, y, upperX - trackBounds.x, y);
			}

			// Restore position and color.
			g.translate(-trackBounds.x, -(trackBounds.y + cy));
			g.setColor(oldColor);

		} 
		else {
			// Determine position of selected range by moving from the middle
			// of one thumb to the other.
			int lowerY = thumbRect.y + (thumbRect.width / 2);
			int upperY = upperThumbRect.y + (upperThumbRect.width / 2);

			// Determine track position.
			int cx = (trackBounds.width / 2) - 2;

			// Save color and shift position.
			Color oldColor = g.getColor();
			g.translate(trackBounds.x + cx, trackBounds.y);

			// Draw selected range.
			g.setColor(rangeColor);
			for (int x = 0; x <= 3; x++) {
				g.drawLine(x, lowerY - trackBounds.y, x, upperY - trackBounds.y);
			}

			// Restore position and color.
			g.translate(-(trackBounds.x + cx), -trackBounds.y);
			g.setColor(oldColor);
		}
	}

	/**
	 * Overrides superclass method to do nothing.  Thumb painting is handled
	 * within the <code>paint()</code> method.
	 */
	@Override
	public void paintThumb(Graphics g) {
		// Do nothing.
	}

//	/**
//	 * Paints the thumb for the lower value using the specified graphics object.
//	 */
//	private void paintLowerThumb(Graphics g) {
//		Rectangle knobBounds = thumbRect;
//		int w = knobBounds.width;
//		int h = knobBounds.height;      
//
//		// Create graphics copy.
//		Graphics2D g2d = (Graphics2D) g.create();
//
//		// Create default thumb shape.
//		Shape thumbShape = createThumbShape(w - 1, h - 1);
//
//		// Draw thumb.
//		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//				RenderingHints.VALUE_ANTIALIAS_ON);
//		g2d.translate(knobBounds.x, knobBounds.y);
//
//		g2d.setColor(lowerThumbFill);
//		g2d.fill(thumbShape);
//
//		g2d.setColor(lowerThumbOutline);
//		g2d.draw(thumbShape);
//
//		// Dispose graphics.
//		g2d.dispose();
//	}
//
//	/**
//	 * Paints the thumb for the upper value using the specified graphics object.
//	 */
//	private void paintUpperThumb(Graphics g) {
//		Rectangle knobBounds = upperThumbRect;
//		int w = knobBounds.width;
//		int h = knobBounds.height;      
//
//		// Create graphics copy.
//		Graphics2D g2d = (Graphics2D) g.create();
//
//		// Create default thumb shape.
//		Shape thumbShape = createThumbShape(w - 1, h - 1);
//
//		// Draw thumb.
//		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//				RenderingHints.VALUE_ANTIALIAS_ON);
//		g2d.translate(knobBounds.x, knobBounds.y);
//
//		g2d.setColor(upperThumbFill);
//		g2d.fill(thumbShape);
//
//		g2d.setColor(upperThumbOutline);
//		g2d.draw(thumbShape);
//
//		// Dispose graphics.
//		g2d.dispose();
//	}


	/** 
	 * Sets the location of the upper thumb, and repaints the slider.  This is
	 * called when the upper thumb is dragged to repaint the slider.  The
	 * <code>setThumbLocation()</code> method performs the same task for the
	 * lower thumb.
	 */
	private void setUpperThumbLocation(int x, int y) {
		Rectangle upperUnionRect = new Rectangle();
		upperUnionRect.setBounds(upperThumbRect);

		upperThumbRect.setLocation(x, y);

		SwingUtilities.computeUnion(upperThumbRect.x, upperThumbRect.y, upperThumbRect.width, upperThumbRect.height, upperUnionRect);
		slider.repaint(upperUnionRect.x, upperUnionRect.y, upperUnionRect.width, upperUnionRect.height);
	}

	/**
	 * Moves the selected thumb in the specified direction by a block increment.
	 * This method is called when the user presses the Page Up or Down keys.
	 */
	public void scrollByBlock(int direction) {
		synchronized (slider) {
			int blockIncrement = (slider.getMaximum() - slider.getMinimum()) / 10;
			if (blockIncrement <= 0 && slider.getMaximum() > slider.getMinimum()) {
				blockIncrement = 1;
			}
			int delta = blockIncrement * ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);

			if (upperThumbSelected) {
				int oldValue = ((PamRangeSlider) slider).getUpperValue();
				((PamRangeSlider) slider).setUpperValue(oldValue + delta);
			} else {
				int oldValue = slider.getValue();
				slider.setValue(oldValue + delta);
			}
		}
	}

	/**
	 * Moves the selected thumb in the specified direction by a unit increment.
	 * This method is called when the user presses one of the arrow keys.
	 */
	public void scrollByUnit(int direction) {
		synchronized (slider) {
			int delta = 1 * ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);

			if (upperThumbSelected) {
				int oldValue = ((PamRangeSlider) slider).getUpperValue();
				((PamRangeSlider) slider).setUpperValue(oldValue + delta);
			} else {
				int oldValue = slider.getValue();
				slider.setValue(oldValue + delta);
			}
		}       
	}

	/**
	 * Listener to handle model change events.  This calculates the thumb 
	 * locations and repaints the slider if the value change is not caused by
	 * dragging a thumb.
	 */
	public class ChangeHandler implements ChangeListener {
		public void stateChanged(ChangeEvent arg0) {
			if (!lowerDragging && !upperDragging && !bothDragging) {
				calculateThumbLocation();
				//                slider.repaint();
			}
			//TODO- might need to 
			slider.repaint();
		}
	}

	/**
	 * Check which direction of the last mouse drag. 
	 * @return the direction of the current mouse drag.
	 */
	private int getDragDirection(int oldMouseX, int oldMouseY, int newMouseX, int newMouseY){
		int direction=NO_DRAG;
		switch (slider.getOrientation()) {
		case JSlider.VERTICAL:  
			if (oldMouseY>newMouseY) direction=DRAG_UP;
			if (oldMouseY<newMouseY) direction=DRAG_DOWN;
			break;
		case JSlider.HORIZONTAL:   
			if (oldMouseX>newMouseX) direction=DRAG_LEFT;
			if (oldMouseY<newMouseY) direction=DRAG_RIGHT;
			break;
		}

		return direction;

	}

	/**
	 * Listener to handle mouse movements in the slider track.
	 */
	public class RangeTrackListener extends TrackListener {

		@Override
		public void mousePressed(MouseEvent e) {
			if (!slider.isEnabled()) {
				return;
			}

			currentMouseX = e.getX();
			currentMouseY = e.getY();

			if (slider.isRequestFocusEnabled()) {
				slider.requestFocus();
			}

			// Determine which thumb is pressed.  If the upper thumb is 
			// selected (last one dragged), then check its position first;
			// otherwise check the position of the lower thumb first.
			boolean lowerPressed = false;
			boolean upperPressed = false;
			boolean bothPressed=false;

			if (upperThumbSelected) {
				if (upperThumbRect.contains(currentMouseX, currentMouseY)) {
					upperPressed = true;
				} else if (thumbRect.contains(currentMouseX, currentMouseY)) {
					lowerPressed = true;
				}
			} 
			else{
				if (thumbRect.contains(currentMouseX, currentMouseY)) {
					lowerPressed = true;
				} else if (upperThumbRect.contains(currentMouseX, currentMouseY)) {
					upperPressed = true;
				}
			}


			if (!upperPressed && !lowerPressed && trackDragging){
				if (getSliderRangeRect().contains(currentMouseX, currentMouseY)){
					bothPressed=true;
				}
			}


			// Handle lower thumb pressed.
			if (lowerPressed) {
				switch (slider.getOrientation()) {
				case JSlider.VERTICAL:
					offset = currentMouseY - thumbRect.y;
					break;
				case JSlider.HORIZONTAL:
					offset = currentMouseX - thumbRect.x;
					break;
				}
				upperThumbSelected = false;
				lowerDragging = true;
				return;
			}
			lowerDragging = false;


			// Handle upper thumb pressed.
			if (upperPressed) {
				switch (slider.getOrientation()) {
				case JSlider.VERTICAL:
					offset = currentMouseY - upperThumbRect.y;
					break;
				case JSlider.HORIZONTAL:
					offset = currentMouseX - upperThumbRect.x;
					break;
				}
				upperThumbSelected = true;
				upperDragging = true;
				return;
			}
			upperDragging = false;

			// Handle both dragging
			if (bothPressed){
				bothDragging=true;
				switch (slider.getOrientation()) {
				case JSlider.VERTICAL:
					if (lastDragY!=null){
						offset=currentMouseY-lastDragY;
					}
				case JSlider.HORIZONTAL:
					if (lastDragX!=null){
						offset=currentMouseX-lastDragX;
					}
				}
				return;
			}
			bothDragging=false;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			lowerDragging = false;
			upperDragging = false;
			bothDragging=false;
			lastDragX=null;
			lastDragY=null;
			slider.setValueIsAdjusting(false);
			super.mouseReleased(e);
		}

		Integer lastDragX=null;
		Integer lastDragY=null;
		@Override
		public void mouseDragged(MouseEvent e) {
			if (!slider.isEnabled()) {
				return;
			}

			dragDirection=getDragDirection(currentMouseX,currentMouseY,e.getX(),e.getY());

			currentMouseX = e.getX();
			currentMouseY = e.getY();

			if (lowerDragging) {
				slider.setValueIsAdjusting(true);
				moveLowerThumb();

			} else if (upperDragging) {
				slider.setValueIsAdjusting(true);
				moveUpperThumb();

			} else if (bothDragging){

				if (checkTrackDragCanMove()){

					slider.setValueIsAdjusting(true);

					if (lastDragX!=null && lastDragY!=null){
						offset=lastDragY-currentMouseY;
						//need to trick the algorithm into thinking it is moving thumbs so save true mouse location. 
						int oldcurrentY=currentMouseY;
						int oldcurrentX=currentMouseX;
						//now set the current mouse location to thumb location and add offset

						currentMouseY=upperThumbRect.y;
						currentMouseX=upperThumbRect.x;
						moveUpperThumb();
						currentMouseY=thumbRect.y;
						currentMouseX=thumbRect.x;
						moveLowerThumb();

						//reset the old mouse location. 
						currentMouseY=oldcurrentY;
						currentMouseX=oldcurrentX;
					}
				}

				lastDragX=currentMouseX;
				lastDragY=currentMouseY;
			}

		}

		@Override
		public boolean shouldScroll(int direction) {
			return false;
		}

		/**
		 * Moves the location of the lower thumb, and sets its corresponding 
		 * value in the slider.
		 */
		private void moveLowerThumb() {
			int thumbMiddle = 0;

			switch (slider.getOrientation()) {
			case JSlider.VERTICAL:      
				int halfThumbHeight = thumbRect.height / 2;
				int thumbTop = currentMouseY - offset;
				int trackTop = trackRect.y;
				int trackBottom = trackRect.y + (trackRect.height - 1);
				int vMax = yPositionForValue(slider.getValue() + slider.getExtent());

				// Apply bounds to thumb position.
				if (drawInverted()) {
					trackBottom = vMax;
				} else {
					trackTop = vMax;
				}
				//make sure we do not go below or above slider track
				thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
				thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

				setThumbLocation(thumbRect.x, thumbTop);

				// Update slider value.
				thumbMiddle = thumbTop + halfThumbHeight;
				slider.setValue(valueForYPosition(thumbMiddle));
				break;

			case JSlider.HORIZONTAL:
				int halfThumbWidth = thumbRect.width / 2;
				int thumbLeft = currentMouseX - offset;
				int trackLeft = trackRect.x;
				int trackRight = trackRect.x + (trackRect.width - 1);
				int hMax = xPositionForValue(slider.getValue() + slider.getExtent());

				// Apply bounds to thumb position.
				if (drawInverted()) {
					trackLeft = hMax;
				} else {
					trackRight = hMax;
				}
				thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
				thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

				setThumbLocation(thumbLeft, thumbRect.y);

				// Update slider value.
				thumbMiddle = thumbLeft + halfThumbWidth;
				slider.setValue(valueForXPosition(thumbMiddle));
				break;

			default:
				return;
			}
		}

		/**
		 * Checks that upper thumb is not at the end of the track and the user is dragging up or that the lower thumb is at the lower end of the track and the user is attempting to drag down. 
		 * Same with horizontal but up down is left right. 
		 */
		private boolean checkTrackDragCanMove(){
			///TODO-wrte this function properly and implement.
			int thumbTop;
			int thumbBottom;
			int trackTop;
			int trackBottom;
			boolean atEnd=false; 

			switch (slider.getOrientation()) {
			case JSlider.VERTICAL:   
				thumbTop = upperThumbRect.y;
				thumbBottom = thumbRect.y+thumbRect.height;
				trackTop = trackRect.y;
				trackBottom=trackRect.y+trackRect.height;
				if (trackTop>thumbTop && dragDirection==DRAG_UP) atEnd=true;
				if (trackBottom<thumbBottom && dragDirection==DRAG_DOWN) atEnd=true;
				break;
			case JSlider.HORIZONTAL:  
				thumbTop = upperThumbRect.x;
				thumbBottom = thumbRect.x+thumbRect.width
						;
				//left
				trackTop = trackRect.x;
				//right
				trackBottom=trackRect.x+trackRect.width;
				if (trackTop>thumbTop && dragDirection==DRAG_LEFT) atEnd=true;
				if (trackBottom<thumbBottom && dragDirection==DRAG_RIGHT) atEnd=true;
				break;
			}

			if (dragDirection==NO_DRAG) atEnd=true;
			//        	System.out.println("At End; "+atEnd);
			return !atEnd;
		}


		/**
		 * Get the rectangle for the area between the slider thumbs. 
		 * @return rectangle defined by the area between the two thumbs. height or width of rectangle defined by height or width of thumbs. width or height of rectangle defined by distnace between tweo thumbs. 
		 */
		public Rectangle getSliderRangeRect(){
			Rectangle rect=new Rectangle();
			//vertical
			switch (slider.getOrientation()) {
			case JSlider.VERTICAL:      
				rect.setRect(upperThumbRect.x, upperThumbRect.y+upperThumbRect.height, upperThumbRect.width, thumbRect.y-upperThumbRect.y-upperThumbRect.height);
				break;
			case (JSlider.HORIZONTAL):
				rect.setRect(upperThumbRect.x+upperThumbRect.width,upperThumbRect.y , thumbRect.x-upperThumbRect.x+upperThumbRect.width, upperThumbRect.height);
			break;
			}
			return rect;
		}


		/**
		 * Moves the location of the upper thumb, and sets its corresponding 
		 * value in the slider.
		 */
		private void moveUpperThumb() {
			int thumbMiddle = 0;

			switch (slider.getOrientation()) {
			case JSlider.VERTICAL:      
				int halfThumbHeight = thumbRect.height / 2;
				int thumbTop = currentMouseY - offset;
				int trackTop = trackRect.y;
				int trackBottom = trackRect.y + (trackRect.height - 1);
				int vMin = yPositionForValue(slider.getValue());

				// Apply bounds to thumb position.
				if (drawInverted()) {
					trackTop = vMin;
				} else {
					trackBottom = vMin;
				}
				thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
				thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

				setUpperThumbLocation(thumbRect.x, thumbTop);

				// Update slider extent.
				thumbMiddle = thumbTop + halfThumbHeight;
				slider.setExtent(valueForYPosition(thumbMiddle) - slider.getValue());
				break;

			case JSlider.HORIZONTAL:
				int halfThumbWidth = thumbRect.width / 2;
				int thumbLeft = currentMouseX - offset;
				int trackLeft = trackRect.x;
				int trackRight = trackRect.x + (trackRect.width - 1);
				int hMin = xPositionForValue(slider.getValue());

				// Apply bounds to thumb position.
				if (drawInverted()) {
					trackRight = hMin;
				} else {
					trackLeft = hMin;
				}
				thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
				thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

				setUpperThumbLocation(thumbLeft, thumbRect.y);

				// Update slider extent.
				thumbMiddle = thumbLeft + halfThumbWidth;
				slider.setExtent(valueForXPosition(thumbMiddle) - slider.getValue());
				break;

			default:
				return;
			}
		}

	}

	public Rectangle getUpperThumbRect() {
		return upperThumbRect;
	}

	public void setUpperThumbRect(Rectangle upperThumbRect) {
		this.upperThumbRect = upperThumbRect;
	}




	public Color getRangeSliderColour() {
		return rangeColor;
	}

	public void setRangeSliderColour(Color rangeColor) {
		this.rangeColor = rangeColor;
	}

	public Color getUpperThumbFill() {
		return upperThumbFill;
	}

	public Color getUpperThumbOutline() {
		return upperThumbOutline;
	}

	public boolean isUpperThumbSelected() {
		return upperThumbSelected;
	}

	public boolean isUpperDragging() {
		return upperDragging;
	}

	public void setUpperThumbFill(Color upperThumbFill) {
		this.upperThumbFill = upperThumbFill;
	}

	public void setUpperThumbOutline(Color upperThumbOutline) {
		this.upperThumbOutline = upperThumbOutline;
	}

	public void setUpperThumbSelected(boolean upperThumbSelected) {
		this.upperThumbSelected = upperThumbSelected;
	}

	public void setUpperDragging(boolean upperDragging) {
		this.upperDragging = upperDragging;
	}

	public void setTrackDragging(boolean trackDragging) {
		this.trackDragging=trackDragging;

	}


}