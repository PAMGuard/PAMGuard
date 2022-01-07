package pamViewFX.fxNodes.comboBox;

import pamViewFX.fxNodes.utilsFX.ColourArray;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;

/**
 * Creates a combo box which shows a selection of colours or colour gradients.
 * @author Jamie Macaulay
 *
 */
public class ColorComboBox extends ComboBox<String>{
	
	ColorComboBox colourComboBox;

	/**
	 * Colour box- contains a selection of standardcolours. 
	 */
	public static final int COLOUR_BOX=0;
	
	/**
	 * A set of pastel shades. 
	 */
	public static final int PASTEL_COLOUR_BOX=1;

	
	/**
	 * Colour array box- contains a selection of colour arrays.
	 */
	public static final int COLOUR_ARRAY_BOX=2;
	
	/**
	 * Width of the colour rectangle; 
	 */
//	private static int rectWidth=120;
	
	/**
	 * The colour list- contains a string reference to colours or colour arrays. 
	 */
	ObservableList<String> colourList;
	
	
	/**
	 * Pre defined default list of single colours for the colour combo box
	 */
	ObservableList<String> colourDataPastel = FXCollections.observableArrayList(
			"chocolate", "salmon", "gold", "coral", "darkorchid",
			"darkgoldenrod", "lightsalmon", "black", "rosybrown", "blue",
			"blueviolet", "brown");
	
	ObservableList<String> colourData = FXCollections.observableArrayList(
			"red", "orange", "yellow", "green", "cyan",
			"blue", "black", "white");


	public ColorComboBox() {
		//set the list
		colourList=setupColourList(COLOUR_BOX);
		setUpComboBox();
		colourComboBox=this;
		
	}
	

	public ColorComboBox(int type) {
		//set the list
		colourList=setupColourList(type);
		setUpComboBox();
		colourComboBox=this;
	}

	/**
	 * Set up the colour combo box.
	 */
	private void setUpComboBox(){
		this.setItems(colourList);
		Callback<ListView<String>, ListCell<String>> factory = new Callback<ListView<String>, ListCell<String>>() {
			@Override
			public ListCell<String> call(ListView<String> list) {
				return new ColorRectCell();
			}
		};
		//set so that colour cells are shown in the list and current colour is shown on combo box
		this.setCellFactory(factory);
		this.setButtonCell(factory.call(null));
		//must set first value so combo box shows something
		this.setValue(colourList.get(0));
	}

	
	/**
	 * Creates a string array containing names of colour arrays. 
	 * @return list of names representing different colour arrays. 
	 */
	private ObservableList<String> createColourArrayList(){
		ObservableList<String> colourData=FXCollections.observableArrayList();
		for (int i=0; i<ColourArrayType.values().length; i++ ){
			colourData.add(ColourArray.getName(ColourArrayType.values()[i]));
		}
		return colourData;
	}
	
	/**
	 * Creates the colour list- override this to create custom list of colours. 
	 */
	private ObservableList<String>  setupColourList(int type){
		switch (type){
		case COLOUR_BOX:
			return colourData;
		case PASTEL_COLOUR_BOX:
			return colourDataPastel;
		case COLOUR_ARRAY_BOX:
			return createColourArrayList();
		}
		return colourData;
	}


	protected  class ColorRectCell extends ListCell<String>{
		@Override
		public void updateItem(String item, boolean empty){
			super.updateItem(item, empty);
			double rectWidth=colourComboBox.getPrefWidth()-30; //need the colour rect to be smaller than the combobox...
			Rectangle rect = new Rectangle(rectWidth,18);
			if(item != null){
				//check to see if the string name corresponds to gradient
				String name;
				boolean isGradient=false;
				for (int i=0; i<ColourArrayType.values().length; i++){
					name=ColourArray.getName(ColourArrayType.values()[i]);
					if (item==name){
						isGradient=true;
						rect.setFill(ColourArray.getLinerGradient(Orientation.HORIZONTAL, rectWidth, ColourArrayType.values()[i]));
					}
				}
				if (!isGradient) rect.setFill(Color.web(item));
				setGraphic(rect);
			}
		}
	}


	/**
	 * Set the value to the current colour array. Note this will not work if 
	 * the colour box is not a colour array box
	 * @param freqColourArray - the colour array vlaue to set the combo box to. 
	 */
	public void setValue(ColourArrayType freqColourArray) {
		this.setValue(ColourArray.getName(freqColourArray)); 
	}


}
