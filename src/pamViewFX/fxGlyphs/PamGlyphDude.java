package pamViewFX.fxGlyphs;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import pamViewFX.fxNodes.connectionPane.StandardConnectionNode;

/**
 * Handles glyphs from various libraries. 
 * @author Jamie Macaulay
 *
 */
public class PamGlyphDude {
	
	/**
	 * Create a standard Glyph from any Icon library. 
	 * @param icon - the icon,
	 * @return the icon glyph as a Text node. 
	 */
//	public static Text createPamGlyph(GlyphIcons icon){
//		Text text2 = null; 
//		if (icon instanceof FontAwesomeIcon){
//			return text2 = FontAwesomeIconFactory.get().createIcon(icon); 
//		}
//		if (icon instanceof MaterialDesignIcon){
//			return text2 = MaterialDesignIconFactory.get().createIcon(icon); 
//		}
//		if (icon instanceof MaterialIcon){
//			return text2 = MaterialIconFactory.get().createIcon(icon); 
//		}
//		if (icon instanceof OctIcon){
//			return text2 = OctIconFactory.get().createIcon(icon); 
//		}
//		if (icon instanceof Icons525){
//			return text2 = Icon525Factory.get().createIcon(icon); 
//		}
//		if (icon instanceof WeatherIcon){
//			return text2 = WeatherIconFactory.get().createIcon(icon); 
//		}
//		return text2; 
//	}
	
	
	/**
	 * Create an Ikonli icon which can be used as graphics for various controls. 
	 * @param iconStr - the icon name to add
	 * @return the icon in a form to add to a control. 
	 */
	public static Text createPamIcon(String iconStr) {
		FontIcon icon2 = new FontIcon(iconStr);
		return icon2;
	}
	
	/**
	 * 
	 * Create a Glyph icon which can be used as graphics for various controls. 
	 * @param icon - the icon to add
	 * @param color - colour to set the icon to
	 * @param size - the size of the icon in font size.
	 * @return the icon in a form to add to a control. 
	 */
//	public static Text createPamGlyph(GlyphIcons icon, Color color, int size){
//		Text text2 = createPamGlyph(icon);
//		text2.getStyleClass().add("glyph-icon");
//		text2.setStyle(String.format("-fx-font-family: %s; -fx-font-size: %dpt; -fx-fill: %s;", icon.fontFamily(), size, colourToHex(color)));
//		return text2;
//	}
	
	/**
	 * Create an Ikonli icon which can be used as graphics for various controls. 
	 * @param iconStr - the icon name to add
	 * @param color - colour to set the icon to
	 * @param size - the width of the icon in pixels
	 * @return the icon in a form to add to a control. 
	 */
	public static Text createPamIcon(String iconStr, Color color, int size) {
		//Note - the setIconCOlor dows not seem to work so had to use a different way to create the icon using CSS. 
		FontIcon icon2 =new FontIcon(); 
//		icon2.setIconColor(color);
//		icon2.setIconLiteral(iconStr);
//		icon2.setIconColor(color);
		//do not use above.
		String style = "-fx-icon-code: \"" + iconStr + "\";" +  " -fx-icon-color: " + colourToHex(color)+ ";" + " -fx-icon-size: " + size + ";";
		icon2.setStyle(style);
		return icon2;
	}
	
	/**
	 * 
	 * Create a Glyph icon which can be used as graphics for various controls. 
	 * @param icon - the icon to add
	 * @param size - the size of the icon in font size.
	 * @return the icon in a form to add to a control. 
	 */
//	public static Text createPamGlyph(GlyphIcons icon, int size){
//		Text text2 = createPamGlyph(icon);
//		text2.getStyleClass().add("glyph-icon");
//		text2.setStyle(String.format("-fx-font-family: %s; -fx-font-size: %dpt; -fx-fill: -fx-icon_col;", icon.fontFamily(), size));
//		return text2;
//	}
	
	/**
	 * Create an Ikonli icon which can be used as graphics for various controls. 
	 * @param iconStr - the icon name to add
	 * @param size - the width of the icon in pixels
	 * @return the icon in a form to add to a control. 
	 */
	public static Text createPamIcon(String iconStr, int size) {
//		FontIcon icon2 = (FontIcon) createPamIcon(iconStr);
//		icon2.setIconSize(size);
		
		FontIcon icon2 =new FontIcon(); 
		String style = "-fx-icon-code: \"" + iconStr + "\";" + " -fx-icon-size: " + size + ";";
		icon2.setStyle(style);

		return icon2;
	}
	
	/**
	 * Create a Glyph for modules with a default size and look. 
	 * @param icon - the icon
	 * @return the module glyph. 
	 */
//	public static Node createModuleGlyph(GlyphIcons icon){
//		Text text2 = createPamGlyph(icon);
//		text2.getStyleClass().add("glyph-icon");
//		text2.setStyle(String.format("-fx-font-family: %s; -fx-font-size: 80; -fx-fill: black;", icon.fontFamily()));
//		
//		text2.setTextAlignment(TextAlignment.CENTER);
//		
//		StackPane stackPane = new StackPane(text2); 
//		stackPane.setAlignment(Pos.CENTER);
//		stackPane.setPrefSize(StandardConnectionNode.DEFUALT_PREF_WIDTH, StandardConnectionNode.DEFUALT_PREF_HEIGHT);
////		stackPane.setStyle("-fx-background-color: red;");
//
//		return stackPane;
//	}

	/**
	 * Create an Ikonli icon for modules with a default size and look. 
	 * @param iconStr - the icon name to add
	 * @return the module glyph. 
	 */
	public static Node createModuleIcon(String iconStr){
		FontIcon text2 = (FontIcon) createPamIcon(iconStr, (int) StandardConnectionNode.DEFUALT_PREF_WIDTH);
		//text2.setStyle( "-fx-icon-color:  black;");
		//text2.setId("module-pane");
		
		text2.setTextAlignment(TextAlignment.CENTER);
		
		StackPane stackPane = new StackPane(text2); 
		stackPane.setAlignment(Pos.CENTER);
		stackPane.setPrefSize(StandardConnectionNode.DEFUALT_PREF_WIDTH, StandardConnectionNode.DEFUALT_PREF_HEIGHT);
		stackPane.setMaxSize(StandardConnectionNode.DEFUALT_PREF_WIDTH, StandardConnectionNode.DEFUALT_PREF_HEIGHT);

		//stackPane.getStyleClass().add(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getSlidingDialogCSS());
		stackPane.setId("module-pane");

		return stackPane;
	}

	/**
	 * Convert a colour object to a hex code
	 * @param color
	 * @return the colour hex code. 
	 */
	public static String colourToHex(Color color){

		int r= (int) (255*color.getRed());
		int g=(int) (255*color.getGreen());
		int b=(int) (255*color.getBlue());

		StringBuilder sb = new StringBuilder("#");

		if (r < 16) sb.append('0');
		sb.append(Integer.toHexString(r));

		if (g < 16) sb.append('0');
		sb.append(Integer.toHexString(g));

		if (b < 16) sb.append('0');
		sb.append(Integer.toHexString(b));

		//another method
//		Formatter f = new Formatter(new StringBuffer("#"));
//		int r=(int) color.getRed();
//		int g=(int) color.getGreen();
//		int b=(int) color.getBlue();
//
//		f.format("%02X", r );
//		f.format("%02X", g);
//		f.format("%02X", b);
//		
//		String hex=f.toString();
//		f.close();
		return sb.toString();
	}



}
