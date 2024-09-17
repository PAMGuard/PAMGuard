package Spectrogram;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import javax.swing.SwingConstants;

import PamView.ColourArray;
import PamView.ColourArray.ColourArrayType;
import PamView.sliders.PamRangeSlider;
import PamView.sliders.PamRangeSliderUI;

/**
 * This UI is designed to allow a user slide to thumbs defining the max and min amplitude for a colour scale. 
 * <p>
 * For anyone in the US please forgive the correct spelling of colour. 
 * <p>
 * @author Jamie Macaulay
 *
 */
public class ColourRangeSliderUI extends PamRangeSliderUI {
	
	/**
	 * Colour map for the slider bar. 
	 */
	private ColourArrayType colourMap;
	/**
	 * The colour array used to draw the image
	 */
	private double[][] colourArray;
	/**
	 * The image of the colour map.
	 */
	private BufferedImage amplitudeImage;
	
    /**
     * Default colour in case we have a null colourmap. 
     */
    private Color rangeColor = Color.GREEN;
    /**
     * Must keep a reference to the slider bar as it is an image observer. 
     */
	private PamRangeSlider b;


	public ColourRangeSliderUI(PamRangeSlider b) {
		super(b);
		this.b=b;
		setColourMap(ColourArrayType.HOT);
		// TODO Auto-generated constructor stub
	}

	
	/**
	 * Set the colour array type for the colour slider. 
	 * @param colourMap
	 */
	public void setColourMap(ColourArrayType colourMap){
		this.colourMap=colourMap;
		createColours();
		createColourMapImage();
	}
	
	/**
	 * Set the colour array for the slider. Use this for non standard colourmaps. e.g. if a standard colour map is reveresed. 
	 * @param colourMap
	 */
	public void setColourMap(ColourArray colourMap){
		createColours(colourMap);
		createColourMapImage();
	}
	
	
	private void createColourMapImage(){
        if (b.getOrientation() == SwingConstants.VERTICAL) {

	// now make a standard amplitude image
		if (colourArray != null && colourArray.length > 0) {
			amplitudeImage = new BufferedImage(1, colourArray.length,
					BufferedImage.TYPE_INT_RGB);
			WritableRaster raster = amplitudeImage.getRaster();
			for (int i = 0; i < colourArray.length; i++) {
				raster.setPixel(0, colourArray.length - i - 1, colourArray[i]);
			}
		}
        }
		else {
			amplitudeImage = new BufferedImage(colourArray.length, 1,
					BufferedImage.TYPE_INT_RGB);
			WritableRaster raster = amplitudeImage.getRaster();
			for (int i = 0; i < colourArray.length; i++) {
				raster.setPixel(i, 0,colourArray[i]);
			}
		}
	}
	
	
	private void createColours() {

		//colourArray = ColourArray.createHotArray(256);
		ColourArray colourArray = ColourArray.createStandardColourArray(256, colourMap);
		createColours(colourArray); 
	}
	
	private void createColours(ColourArray colourArray) {
		double[][] colorValues = new double[256][3];
		for (int i = 0; i < 256; i++) {
			colorValues[i][0] = colourArray.getColours()[i].getRed();
			colorValues[i][1] = colourArray.getColours()[i].getGreen();
			colorValues[i][2] = colourArray.getColours()[i].getBlue();
		}
		this.colourArray=colorValues;
	}
	
	
	/**
     * Paints the track.
     */
    @Override
    public void paintTrack(Graphics g) {
    	
        // Draw track.
        super.paintTrack(g);
        
        Rectangle trackBounds = trackRect;
        
        // Save colour and shift position.
        Color oldColor = g.getColor();
            
        int cx;

        if (b.getOrientation() == SwingConstants.VERTICAL) {
        	
            // Determine position of selected range by moving from the middle
            // of one thumb to the other.
            int lowerY = thumbRect.y + (thumbRect.width / 2);
            int upperY = getUpperThumbRect().y + (getUpperThumbRect().width / 2);
                
            // Determine track position.
             cx = (trackBounds.width / 2) - 2;

            g.translate(trackBounds.x + cx, trackBounds.y);
	        drawColourMapVert( g,  lowerY - trackBounds.y, upperY - trackBounds.y, -(getUpperThumbRect().width /4)-trackBounds.x,(getUpperThumbRect().width / 2)-trackBounds.x+(trackBounds.width / 4)+2);
      
	        cx = (trackBounds.width / 2) - 2;
	        g.translate(-(trackBounds.x + cx), -trackBounds.y);

        }
        else {
            // Determine position of selected range by moving from the middle
            // of one thumb to the other.
            int lowerX = thumbRect.x;
            int upperX = getUpperThumbRect().x;
                
            // Determine track position.
            cx = (trackBounds.width / 2) - 2;
//            -(getUpperThumbRect().height /4)-trackBounds.y, (getUpperThumbRect().height / 2)-trackBounds.y+(trackBounds.height / 4)+2
        	drawColourMapHorz(g, trackBounds.height/2-getUpperThumbRect().height/2, trackBounds.height/2+getUpperThumbRect().height/2, lowerX + getUpperThumbRect().width/2, upperX+ getUpperThumbRect().width/2);

        }
        // Restore position and colour.
        g.setColor(oldColor);
        
    }
    
    /**
     * Draw the colour map between the two thumbs in the slider bar. Colour the section above the top most thumb
     * and the section below the lower most thumb with the color map extremes. 
     * @param g- graphics
     * @param y1
     * @param y2
     * @param x1
     * @param x2
     */
    private void drawColourMapHorz(Graphics g, int y1, int y2, int x1, int x2){
    	
    	if (amplitudeImage == null) return;

		Graphics2D g2d = (Graphics2D) g;
		
		int width=Math.abs(x2-x1);
		int height=Math.abs(y2-y1);
		
//		System.out.println("Width: " + width + " " + height + " x1 " + x1);

		//calculate the distance between thumbs
		double ascaleX = width
		/ (double) amplitudeImage.getWidth(null);
		double ascaleY = height
		/ (double) amplitudeImage.getHeight(null);
		
		AffineTransform xform = new AffineTransform();
		// xform.translate(1, amplitudeImage.getWidth(null));
		xform.scale(ascaleX, ascaleY);
		//translate to the correct location;
		g2d.translate(x1, y1);
		//now translate back for the rest of the operations;
		g2d.drawImage(amplitudeImage, xform, b);
		
		//translate back to our original position. 
		g2d.translate(-x1, -y1);
		
		//go to the left of the lower thumb;
//		g2d.translate(0, height);
		g2d.setColor(new Color((int) colourArray[0][0],(int)colourArray[0][1],(int) colourArray[0][2]));
		for (int i=y1; i<y2; i++){
			g2d.drawLine(0,i,x1, i);
		}
	
		
		//color left of the thumb
		g2d.setColor(new Color((int) colourArray[colourArray.length-1][0],(int)colourArray[colourArray.length-1][1],(int) colourArray[colourArray.length-1][2]));
		for (int i=y1; i<y2; i++){
			g2d.drawLine(x2,i, trackRect.width + thumbRect.width/2, i);
		}
		
    }
    
    /**
     * Draw the colour map between the two thumbs in the slider bar. Colour the section above the top most thumb
     * and the section below the lower most thumb with the color map extremes. 
     * @param g- graphics
     * @param y1
     * @param y2
     * @param x1
     * @param x2
     */
    private void drawColourMapVert(Graphics g, int y1, int y2, int x1, int x2){
    	
    	if (amplitudeImage == null) return;

		Graphics2D g2d = (Graphics2D) g;
		
		int width=Math.abs(x2-x1);
		int height=Math.abs(y2-y1);

		//calculate the distance between thumbs
		double ascaleX = width
		/ (double) amplitudeImage.getWidth(null);
		double ascaleY = height
		/ (double) amplitudeImage.getHeight(null);
		
		AffineTransform xform = new AffineTransform();
		// xform.translate(1, amplitudeImage.getWidth(null));
		xform.scale(ascaleX, ascaleY);
		//translate to the correct location;
		g2d.translate(x1, y1-height);
		//now translate back for the rest of the operations;
		g2d.drawImage(amplitudeImage, xform, b);

		//now we have to fill in the rest with a solid colour determined by the ends of the colour map;
		g2d.translate(-x1, 0);		
		g2d.setColor(new Color((int) colourArray[colourArray.length-1][0],(int)colourArray[colourArray.length-1][1],(int) colourArray[colourArray.length-1][2]));
		for (int i=x1; i<x2; i++){
			g2d.drawLine(i,0,i,-getPixelsAboveUpperThumb());
		}
		//go to the lower thumb;
		g2d.translate(0, height);
		g2d.setColor(new Color((int) colourArray[0][0],(int)colourArray[0][1],(int) colourArray[0][2]));
		for (int i=x1; i<x2; i++){
			g2d.drawLine(i,0,i,getPixelsBelowLowerThumb());
		}
		
		//translate back to our original position. 
		g2d.translate(0, -y1);
		
    }
  
    
    //for vertical;
    private int getPixelsAboveUpperThumb(){
    	return getUpperThumbRect().y;
    }
    
    private int getPixelsBelowLowerThumb(){
    	return trackRect.height-thumbRect.y;
    }

	
	

	

}
