package PamView;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import PamView.ColourArray.ColourArrayType;
import PamView.panel.PamPanel;

import javax.swing.*;


@SuppressWarnings("serial")
public class ColourComboBox extends PamPanel {

	
    private ImageIcon[] images;
    private int height=20;
    private int width=60;
    
    private String[] colourStrings;
    private ColourArrayType[] types;
	private JComboBox<Integer> colourBox;
        
    public ColourComboBox() {
        super(new BorderLayout());
        init( width, height);
    }
    
    public ColourComboBox(int width, int height) {
        super(new BorderLayout());
        this.width=width;
        this.height=height;
        init(width,  height);
    }
    
    public ColourComboBox(int width, int height, ColourArrayType[] types) {
        super(new BorderLayout());
        this.width=width;
        this.height=height;
        init(width,  height,types);
    }
    
    private void init(int width, int height){
    	this.types = ColourArray.ColourArrayType.values();
    	init();
    }
    
    private void init(int width, int height, ColourArrayType[] types){
    	this.types = types; 
    	init();
    }
    
    private void init(){
    	
    	colourStrings=new String[types.length];
    	images=new ImageIcon[types.length];
    	        
    	ColourArray colourArray;
    	int length=ColourArray.ColourArrayType.values().length;
    	Integer[] intArray = new Integer[length];
    	
    	for (int i = 0; i < ColourArray.ColourArrayType.values().length; i++) {
    		intArray[i] = new Integer(i);
    		colourStrings[i]=ColourArray.getName(types[i]);
    		colourArray=ColourArray.createStandardColourArray(256, ColourArray.ColourArrayType.values()[i]);
    		images[i]=new ImageIcon(createColourMapImage(colourArray,  height,  width));
    	}

    	//Create the combo box.
    	colourBox = new JComboBox<Integer>(intArray);
    	ComboBoxRenderer renderer= new ComboBoxRenderer();
    	renderer.setPreferredSize(new Dimension(width, height+height/10));
    	colourBox.setRenderer(renderer);
    	colourBox.setOpaque(false);
    	
    	//Lay out combo
    	add(colourBox, BorderLayout.PAGE_START);
    	setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

    	
    }
    
//    private void createImages() {
//    	for (int i = 0; i < ColourArray.ColourArrayType.values().length; i++) {
//    		intArray[i] = new Integer(i);
//    		colourStrings[i]=ColourArray.getName(types[i]);
//    		colourArray=ColourArray.createStandardColourArray(256, ColourArray.ColourArrayType.values()[i]);
//    		images[i]=new ImageIcon(createColourMapImage(colourArray,  height,  width));
//    	}
//    }
    
    public void addActionListener(ActionListener actionListener){
    	colourBox.addActionListener(actionListener);
    }
    
    public JComboBox getComboBox(){
    	return colourBox;
    }
    
    public void setSelectedColourMap(ColourArrayType type){
    	for (int i=0; i<types.length; i++){
    		if (types[i]==type){
    			colourBox.setSelectedIndex(i);
    			break; 
    		}
    	}
    }
    
    @Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		colourBox.setEnabled(enabled);
	}

	public ColourArrayType getSelectedColourMap(){
    	return types[colourBox.getSelectedIndex()];
    }
    
    
    private BufferedImage createColourMapImage(ColourArray colArray, int height, int width){
    	BufferedImage colourImage =
    			  new BufferedImage(width, height,
    			                    BufferedImage.TYPE_INT_ARGB);

    	Graphics2D g2 = colourImage.createGraphics();
		if (colArray == null) {
			return  null;
		}
		int nCols = colArray.getNumbColours();
		double scale = (double) nCols / (double) width;
		int y = height;
		for (int i = 0; i < width; i++) {
			int iCol = (int) Math.round(scale * i);
			iCol = Math.max(0, Math.min(iCol, nCols));
			g2.setColor(colArray.getColour(iCol));
			g2.drawLine(i, 0, i, y);
		}
		
		return colourImage;
    }

    class ComboBoxRenderer extends JLabel implements ListCellRenderer {

		public ComboBoxRenderer() {
			setOpaque(true);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
		}
		
		/*
		* This method finds the image and text corresponding
		* to the selected value and returns the label, set up
		* to display the text and image.
		*/
		public Component getListCellRendererComponent(
		                    JList list,
		                    Object value,
		                    int index,
		                    boolean isSelected,
			                    boolean cellHasFocus) {
			//Get the selected index. (The index param isn't
			//always valid, so just use the value.)
			if (width != getWidth() && getWidth() > 0) {
				width = getWidth();
			    ColourArray colourArray;
			    int length=ColourArray.ColourArrayType.values().length;
			    Integer[] intArray = new Integer[length];
			    	
			    for (int i = 0; i < ColourArray.ColourArrayType.values().length; i++) {
			    	intArray[i] = new Integer(i);
			    	colourStrings[i]=ColourArray.getName(types[i]);
			    	colourArray=ColourArray.createStandardColourArray(width, ColourArray.ColourArrayType.values()[i]);
			    	images[i]=new ImageIcon(createColourMapImage(colourArray,  height,  width));
			    }
			}
			
			int selectedIndex = ((Integer)value).intValue();
			
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} 
			else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			
			//Set the icon and text.  If icon was null, say so.
			ImageIcon icon = images[selectedIndex];
			setIcon(icon);
			
			return this;
		}
    }
}