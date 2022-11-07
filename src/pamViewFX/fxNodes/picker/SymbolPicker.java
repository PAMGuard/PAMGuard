package pamViewFX.fxNodes.picker;

import PamView.PamSymbol;
import PamView.PamSymbolType;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.PamSymbolFX;

/*
 * ComboBox which shows all available PamSymbolFX's. 
 * @author Jamie Macaulay 
 *
 */
public class SymbolPicker extends ComboBox<PamSymbolFX> {

	/**
	 * Fill colour for the symbol; 
	 */
	private Color fillColour= Color.PALEVIOLETRED; 

	/**
	 * line colour for the symbol
	 */
	private Color lineColour= Color.CYAN;
	
	/**
	 * The height of the symbol to display as an incon in the combo box
	 */
	public static int height=15;
	
	/**
	 * The width of the symbol to display as an incon in the combo box
	 */
	public static int width=15; 
	
	/**
	 * The total size of the canvas to display. 
	 */
	public static double canvasSize=18; 


	public SymbolPicker(){

		createSymbols();
        this.setButtonCell(new ImageListCell());
		this.setCellFactory(listView -> new ImageListCell());

	}

	class ImageListCell extends ListCell<PamSymbolFX> {
		private final Canvas canvas;

		ImageListCell() {
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			canvas= new Canvas(canvasSize,canvasSize); 
		}

		@Override protected void updateItem(PamSymbolFX item, boolean empty) {
			super.updateItem(item, empty);

			if (item == null || empty) {
				setGraphic(null);
			} else {
				canvas.getGraphicsContext2D().clearRect(0, 0, canvasSize, canvasSize);
				item.draw(canvas.getGraphicsContext2D(), new Point2D(canvasSize/2.,canvasSize/2.)); 
				setGraphic(canvas);
			}
		}

	}
	
	
	private void createSymbols(){
		
		int prevIndex=this.getSelectionModel().getSelectedIndex(); 
				
		//System.out.println("SymbolPicker: SelectedIndex: "+getSelectionModel().getSelectedIndex()); 

		this.getItems().clear(); 
		for (int i=0; i<PamSymbolType.values().length; i++){
			this.getItems().add(new PamSymbolFX(PamSymbolType.values()[i],height, width, true, fillColour, lineColour)); 
		}
		
		//System.out.println("SymbolPicker:  SelectedIndex: "+getSelectionModel().getSelectedIndex() +  " " + prevIndex); 

		this.getSelectionModel().select(prevIndex);
		
	}
	
	/**
	 * Get the current fill colour. 
	 * @return the fill colour. 
	 */
	public Color getFillColour() {
		return fillColour;
		
	}

	/**
	 * Set the current fill colour
	 * @param fillColour - the fill colour ot set. 
	 */
	public void setFillColour(Color fillColour) {
		this.fillColour = fillColour;
		createSymbols();
	}

	/**
	 * Get the current line colour. 
	 * @return the current line colour.
	 */
	public Color getLineColour() {
		return lineColour;
	}

	/**
	 * Set the line colour.
	 * @param lineColour - the line colour to set. 
	 */
	public void setLineColour(Color lineColour) {
		this.lineColour = lineColour;
		createSymbols(); 
	}

	/**
	 * Set the symbol with a symbol type. 
	 * @param symbol - the symbol type. 
	 */
	public void setValue(PamSymbolType symbol) {
		for (int i=0; i<getItems().size(); i++){
			if (getItems().get(i).getSymbol()==symbol){
				this.getSelectionModel().select(i);
			};
		} 
	}

	/**
	 * Set the symbol with a symbol type. Note, replicates setValue for convenience. 
	 * @param symbol - the symbol type. 
	 */
	public void setSymbol(PamSymbolType symbol) {
		setValue(symbol);
		
	}




}
