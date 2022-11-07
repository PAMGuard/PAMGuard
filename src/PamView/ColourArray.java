package PamView;

import java.awt.Color;
import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * A series of functions for creating arrays of colours
 * <p>
 * Can be used for spectrogram colouring, contour colouring, etc.
 * @author Doug Gillespie
 *
 */
public class ColourArray implements Cloneable, Serializable, ManagedParameters {


	/**
	 * 
	 */
	private static final long serialVersionUID = -6356309519545235402L;

	private Color[] colours;
	
	private Color contrastingColor;
	
	private ColourArray() {
		
	}
	
//	public static final int GREY = 0;
//	public static final int REVERSEGREY = 1;
//	public static final int BLUE = 2;
//	public static final int RED = 3;
//	public static final int GREEN = 4;
//	public static final int HOT = 5;
	
	public static enum ColourArrayType{GREY, REVERSEGREY, BLUE,  GREEN, RED, HOT, HSV, FIRE, PATRIOTIC}
	
	public static String getName(ColourArrayType type) {
		switch (type){
		case GREY:
			return "Grey (black to white)";
		case REVERSEGREY:
			return "Grey (white to black)";
		case BLUE:
			return "Blue";
		case GREEN:
			return "Green";
		case RED:
			return "Red";
		case HOT:
			return "Rainbow (multicoloured)";
		case HSV:
			return "HSV (multicoloured)";
		case FIRE:
			return "Fire (multicoloured)";
		case PATRIOTIC:
			return "Red-White-Blue";
		default:
			return "Unknown";
		}
	}
	
	public static ColourArray createStandardColourArray(int nPoints, ColourArrayType type) {
		if (type == null) {
			type = ColourArrayType.GREY;
		}
		switch (type){
		case GREY:
			return createMergedArray(nPoints, Color.WHITE, Color.BLACK);
		case REVERSEGREY:
			return createMergedArray(nPoints, Color.BLACK, Color.WHITE);
		case BLUE:
			return createMergedArray(nPoints, Color.BLACK, Color.BLUE);
		case GREEN:
			return createMergedArray(nPoints, Color.BLACK, Color.GREEN);
		case RED:
			return createMergedArray(nPoints, Color.BLACK, Color.RED);
		case HOT:
			return createRainbowArray(nPoints);
		case HSV:
			return createHSVArray(nPoints);
		case FIRE:
			return createHotArray(nPoints);
		case PATRIOTIC:
			return createPatrioticArray(nPoints);
		default:
			return createMergedArray(nPoints, Color.GREEN, Color.RED);
		}
	}

	
	public static ColourArray createWhiteToBlackArray(int nPoints) {
		return createMergedArray(nPoints, Color.WHITE, Color.BLACK);
	}
	
	public static ColourArray createBlackToWhiteArray(int nPoints) {
		return createMergedArray(nPoints, Color.BLACK, Color.WHITE);
	}
	
	/**
	 * Color.BLACK, Color.BLUE, Color.CYAN,
				Color.GREEN, Color.ORANGE, Color.RED
	 * @param nPoints
	 * @return
	 */
	public static ColourArray createRainbowArray(int nPoints) {
		// go from black to blue to cyan to green to orange to red
		// that's five stages in total. 		
		return createMultiColouredArray(nPoints, Color.BLACK, Color.BLUE, Color.CYAN,
				Color.GREEN, Color.ORANGE, Color.RED);
	}
	
	/**
	 * Color.RED,
				Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, 
				Color.RED
	 * @param nPoints
	 * @return
	 */
	public static ColourArray createHSVArray(int nPoints) {
		// go from red to green to blue to red
		// that's five stages in total. 		
//		return createMultiColouredArray(nPoints, Color.RED, Color.ORANGE,
//				Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, 
//				Color.MAGENTA, Color.RED);
		return createMultiColouredArray(nPoints, Color.RED,
				Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, 
				Color.RED);
	}
	
	/**
	 * Colour array from black to white, via red, orange and yellow
	 * @param nPoints
	 * @return
	 */
	public static ColourArray createHotArray(int nPoints) {
		// go from black to red to orange to yellow to white
		// that's five stages in total. 		
		return createMultiColouredArray(nPoints, Color.BLACK, Color.RED, Color.ORANGE,
				Color.YELLOW, Color.WHITE);
	}
	
	/**
	 * Red white blue colour array
	 * @param nPoints
	 * @return
	 */
	public static ColourArray createPatrioticArray(int nPoints) {
		// go from black to red to white to blue
		// that's three stages in total.  		
		return createMultiColouredArray(nPoints, Color.RED, Color.WHITE, Color.BLUE);
	}
	/**
	 * Create a multicoloured array of colours that merges in turn between each of
	 * the colours given in the list. 
	 * @param nPoints total number of colour points
	 * @param colourList variable number of colours. 
	 * @return a new ColourArray object. 
	 */
	public static ColourArray createMultiColouredArray(int nPoints, Color ... colourList) {

		if (colourList.length == 1) {
			return createMergedArray(nPoints, colourList[0], colourList[0]);
		}
		else if (colourList.length == 2) {
			return createMergedArray(nPoints, colourList[0], colourList[1]);
		}
		
		ColourArray ca = new ColourArray();
		ca.colours = new Color[nPoints];
		
		int nSegments = (colourList.length - 1);
		int segPoints = nPoints / nSegments-1;
		int lastPoints = nPoints - segPoints * (nSegments-1);
		int thisSegPoints;
		int pointsToCreate;
		int iPoint = 0;
		ColourArray subArray;
		for (int i = 0; i < nSegments-1; i++) {
			subArray = createMergedArray(segPoints+1, colourList[i], colourList[i+1]);
			for (int j = 0; j < segPoints; j++) {
				ca.colours[iPoint++] = subArray.colours[j];
			}
		}
		// now the last one
		subArray = createMergedArray(lastPoints, colourList[nSegments-1], colourList[nSegments]);
		for (int j = 0; j < lastPoints; j++) {
			ca.colours[iPoint++] = subArray.colours[j];
		}
		
		return ca;
	}
	
//	static ColourArray createHotArray(int nPoints) {
//		
//	}
	
	public static ColourArray createMergedArray(int nPoints, Color c1, Color c2) {
		ColourArray ca = new ColourArray();
		ca.colours = new Color[nPoints];
		float col1[] = new float[3];
		float col2[] = new float[3];
		float step[] = new float[3];
		col1 = c1.getColorComponents(null);
		col2 = c2.getColorComponents(null);
		if (nPoints == 1) {
			ca.colours[0] = new Color(col1[0], col1[1], col1[2]);
			return ca;
		}
		for (int i = 0; i < 3; i++) {
			step[i] = (col2[i]-col1[i]) / (nPoints-1);
		}
		for (int c = 0; c < nPoints; c++) {
			try {
		  ca.colours[c] = new Color(col1[0], col1[1], col1[2]);
			}
			catch (IllegalArgumentException ex) {
				System.out.println(String.format("Illegal colour arguments red %3.3f green %3.3f blue %3.3f",
						col1[0], col1[1], col1[2]));
			}
		  for (int i = 0; i < 3; i++) {
			  col1[i] += step[i];
			  col1[i] = Math.max(0, Math.min(1, col1[i]));
		  }
		}		
		return ca;
	}
	
	public Color[] getColours() {
		return colours;
	}
	
	/**
	 * Get a colour with index checking to make sure it's in the rangee
	 * 0 - nCol-1.
	 * @param iCol colour index
	 * @return colour
	 */
	public Color checkColour(int iCol) {
		return getColour(Math.max(0, Math.min(iCol, colours.length-1)));
	}
	
	/**
	 * Get the colour for the index. No checking so 
	 * can throw an indexoutofbounds error
	 * @param iCol colour index
	 * @return colour
	 */
	public Color getColour(int iCol) {
		return colours[iCol];
	}
	
	/**
	 * Get a three digit colour array
	 * @param iCol
	 * @return
	 */
	public int[] getIntColourArray(int iCol) {
		int[] rgb = new int[3];
		rgb[0] = colours[iCol].getRed();
		rgb[1] = colours[iCol].getGreen();
		rgb[2] = colours[iCol].getBlue();
		return rgb;
	}
	
	/**
	 * Get a four digit colour array with the alpha value set to 255 (opaque)
	 * @param iCol
	 * @return
	 */
	public int[] get4IntColourArray(int iCol) {
		int[] rgb = new int[4];
		rgb[0] = colours[iCol].getRed();
		rgb[1] = colours[iCol].getGreen();
		rgb[2] = colours[iCol].getBlue();
		rgb[3] = 255;
		return rgb;
	}
	
	public int getNumbColours() {
		if (colours == null) {
			return 0;
		}
		else {
			return colours.length;
		}
	}

	public void reverseArray() {
		Color[] newColours = new Color[colours.length];
		for (int i = 0; i < colours.length; i++) {
			newColours[i] = colours[colours.length-1-i];
		}
		colours = newColours;
	}

	@Override
	protected ColourArray clone() {
		try {
			ColourArray newArray = (ColourArray) super.clone();
			newArray.colours = this.colours.clone();
			return newArray;
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Return a contrasting colour which is different to ALL of the 
	 * colours in the array. This can be used to draw additional lines 
	 * over 3D plots made with the array colours. 
	 * @return the contrastingColor
	 */
	public Color getContrastingColour() {
		if (contrastingColor == null) {
			contrastingColor = createContrastingColour();
		}
		return contrastingColor;
	}
	
	/**
	 * Added just so that contrastingColor field would be included in getParameterSet method
	 * 
	 * @return
	 */
	public Color getContrastingColor() {
		return getContrastingColour();
	}
	
	public void setAlpha(int alpha) {
		for (int i = 0; i < colours.length; i++) {
			colours[i] = new Color(colours[i].getRed(), colours[i].getGreen(), colours[i].getBlue(),alpha); 
		}
	}

	/**
	 * Find a colour which is as distant as possible from all the other colours in 
	 * a colour array. 
	 * @return a contrasting colour. 
	 */
	private Color createContrastingColour() {
		Color[] tryCols = {Color.white, Color.red, Color.CYAN, Color.blue, Color.green, Color.black};
		if (colours == null) {
			return tryCols[0];
		}
		int furthest = 0;
		int furthestIndex = 0;
		for (int i = 0; i < tryCols.length; i++) {
			int closeness = getCloseness(colours, tryCols[i]);
			if (closeness > furthest) {
				furthest = closeness;
				furthestIndex = i;
			}
		}
		return tryCols[furthestIndex];
		
	}

	/**
	 * Work out the closest distance between all the colours in 
	 * colourArray and colour
	 * @param colourArray
	 * @param aColour
	 * @return closest distance found
	 */
	private int getCloseness(Color[] colourArray, Color aColour) {
		int r = aColour.getRed();
		int b = aColour.getBlue();
		int g = aColour.getGreen();
		int closeness = Integer.MAX_VALUE;
		int cl;
		for (int i = 0; i < colourArray.length; i++) {
			cl = Math.abs(r-colourArray[i].getRed()) +  Math.abs(b-colourArray[i].getBlue()) +  Math.abs(g-colourArray[i].getGreen());
			closeness = Math.min(cl, closeness);
		}
		return closeness;
	}

	/**	 
	 * Set a contrasting colour which is different to ALL of the 
	 * colours in the array. This can be used to draw additional lines 
	 * over 3D plots made with the array colours.
	 * @param contrastingColor the contrastingColor to set
	 */
	public void setContrastingColour(Color contrastingColor) {
		this.contrastingColor = contrastingColor;
	}

	/**
	 * Get a colour for a data value if the colourmap is between two data limits.  
	 * @param colValue - the data value to find colour for.
	 * @param min - the minimum data value of the colour array.
	 * @param max - the maximum data value of the colour array.
	 * @return colour representing the data value
	 */
	public Color getColour(double colValue, double min, double max) {
		double perc = (colValue-min)/(max-min);
		if (perc<=0) return getColour(0);
		if  (perc>=1.0) return getColour(getNumbColours()-1);
		
		int colIndex = (int) Math.round(getNumbColours()*perc);
		
		return colours[colIndex];
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
