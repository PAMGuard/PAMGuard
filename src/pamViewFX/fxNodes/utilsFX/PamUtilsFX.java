package pamViewFX.fxNodes.utilsFX;

import java.awt.CheckboxMenuItem;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import PamView.PamSymbol;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.util.Duration;
import pamViewFX.fxNodes.PamSymbolFX;

/**
 * Useful classes for JavaFX library. 
 * @author Jamie Macaulay
 *
 */
public class PamUtilsFX {
	
	/**
	 * 
	 * @param awtColor
	 * @return
	 */
	public static Color awtToFXColor(java.awt.Color awtColor){
		if (awtColor==null) return Color.BLACK;
		int r = awtColor.getRed();
		int g = awtColor.getGreen();
		int b = awtColor.getBlue();
		int a = awtColor.getAlpha();
		double opacity = a / 255.0 ;
		return Color.rgb(r, g, b, opacity);
	}
	
	/**
	 * 
	 * @param fxColor
	 * @return
	 */
	public static java.awt.Color fxToAWTColor(Color fxColor) {
		return new java.awt.Color((float) fxColor.getRed(), (float) fxColor.getGreen(), (float) fxColor.getBlue(), (float) fxColor.getOpacity());
	}
	
	
	/**
	 * Converts a mouse event received on one node to mouse event received on another assuming that both nodes are in the same scene.
	 * The screen co-ordinates and all other variable are preserved  are preserved and the x and Y values converted to the new Node.  
	 * @param event - the MouseEvent. 
	 * @param newNode - the target node for the mouse event
	 * @return the new MouseEvent for the new Node. 
	 */
	public static MouseEvent mouseEvent2NewNode(MouseEvent event, Node newNode) {
		return mouseEvent2NewNode(event, newNode, 0., 0.); 
	}
	
	/**
	 * Converts a mouse event received on one node to mouse event received on another assuming that both nodes are in the same scene.
	 * The screen co-ordinates and all other variable are preserved  are preserved and the x and Y values converted to the new Node.  
	 * Note that the pick result is not preserved. 
	 * @param event - the MouseEvent. 
	 * @param newNode - the target node for the mouse event.
	 * @param xOffset - offset x in pixels.
	 * @param yOffset - offset y in pixels.
	 * @return the new MouseEvent for the new Node. 
	 */
	public static MouseEvent mouseEvent2NewNode(MouseEvent event, Node newNode, double xOffset, double yOffset) {
		Point2D localMainHoldr = newNode.screenToLocal(new Point2D(event.getScreenX(), event.getScreenY()));

		/**
		 * Note that preserving the pick result can alter the mouse co-ordinates which is not very useful in converting mouse events. 
		 * So pick result is not preserved and therefore null. 
		 */
		MouseEvent newEvent = new MouseEvent(event.getEventType(), localMainHoldr.getX()+xOffset, localMainHoldr.getY()+yOffset,
				event.getScreenX(), event.getScreenY(), event.getButton(), event.getClickCount(), event.isShiftDown(),
				event.isControlDown(), event.isAltDown(), event.isMetaDown(), event.isPrimaryButtonDown(),
				event.isMiddleButtonDown(), event.isSecondaryButtonDown(), event.isSynthesized(),
				event.isPopupTrigger(), event.isStillSincePress(), null);
		return newEvent;
	}

	/**
	 * Add transparancy to a JavaFX colour
	 * @param color
	 * @return
	 */
	public static Color addColorTransparancy(Color color, double opacity){
		if ( opacity<0 || opacity>1){
			System.err.println("Transparancy must be between 0 and 1");
			return null;
		}
		int r = (int) (color.getRed()*255);
		int g = (int) (color.getGreen()*255);
		int b = (int) (color.getBlue()*255);
		return Color.rgb(r, g, b, opacity);
	}
	
	/**
	 * 
	 * @param color
	 * @return
	 */
	public static String color2Hex(Color color){
        int r =  (int) (color.getRed() * 255);
        int g =  (int) (color.getGreen() * 255);
        int b =  (int) (color.getBlue() * 255);
        String str = String.format("#%02X%02X%02X;", r, g, b);
        return str; 
	}
	
	
	/**
	 * Extract info from a JpopUpMenu and return an array of MenuItemInfos which contain
	 * the information required to construct a JavaFX menu. 
	 * @param jPopupMenu - the JPopupMenu to deconstruct. 
	 * @return an array of MenuItemInfos containing info on the items in JPopupMenu. 
	 */
	public static ArrayList<MenuItemInfo> jPopMenu2FX(JPopupMenu jPopupMenu){
		
		ArrayList<MenuItemInfo> menuList= new ArrayList<MenuItemInfo>(); 
		
		Component component; 
		for (int i=0; i<jPopupMenu.getComponentCount(); i++){
			 component=jPopupMenu.getComponent(i); 
			 if (component instanceof JMenuItem){
				 menuList.add(new MenuItemInfo((JMenuItem) component)); 
			 }
		}
		
		return menuList;
	}
	
	/**
	 * Convert a FX context menu to a Swingpopup menu. 
	 * @param contextMenu FX Context menu
	 * @return Swing popup menu
	 */
	public static JPopupMenu contextMenuToSwing(ContextMenu contextMenu) {
		if (contextMenu == null) {
			return null;
		}
		JPopupMenu popMenu = new JPopupMenu();
		ObservableList<MenuItem> items = contextMenu.getItems();
		for (int i = 0; i < items.size(); i++) {
			popMenu.add(fxMenuItemToSwing(items.get(i)));
		}
		return popMenu;
	}
	
	/**
	 * Turn the items in a Swing menu into a list of FX menu items. 
	 * @param popMenu
	 * @return
	 */
	public static List<MenuItem> getSwingMenuItems(JPopupMenu popMenu) {
		if (popMenu == null) {
			return null;
		}
		List<MenuItem> itemList = new ArrayList<>();
		int n = popMenu.getComponentCount();
		for (int i = 0; i < n; i++) {
			Component item = popMenu.getComponent(i);
			if (JMenuItem.class.isAssignableFrom(item.getClass())) {
				itemList.add(swingMenuItemToFX( (JMenuItem) item));
			}
		}
		
		return itemList;
	}
	
	/**
	 * Convert an AWT menu item into an FX menu Item. 
	 * @param awtItem AWT menu item
	 * @return swing menu item. 
	 */
	public static MenuItem awtMenuItemToFX(java.awt.MenuItem awtItem) { 
		if (awtItem == null) {
			return null;
		}
		MenuItem fxItem = null;
		/**
		 * First possibility is that the menu item is a menu (this 
		 * is how sub menus are made) in this case make a 
		 * new sub menu to return and iterate through the content 
		 * of the awt component, calling recursively back into this
		 * function in the hope that items will be simple awt MenuItems. 
		 */
		if (awtItem.getClass() == java.awt.Menu.class) {
			java.awt.Menu awtMenu = (java.awt.Menu) awtItem;
			Menu fxMenu = new Menu(awtMenu.getLabel());
			fxItem = fxMenu;
			int nItems = awtMenu.getItemCount();
			for (int i = 0; i < nItems; i++) {
				java.awt.MenuItem subItem = awtMenu.getItem(i);
				fxMenu.getItems().add(awtMenuItemToFX(subItem));
			}
		}
		else if (awtItem.getClass() == java.awt.MenuItem.class) {
			fxItem = new MenuItem(awtItem.getLabel());
		}
		
		else if (awtItem.getClass() == CheckboxMenuItem.class) {
			CheckMenuItem checkItem;
			fxItem = checkItem = new CheckMenuItem(awtItem.getLabel());
			checkItem.setSelected(((CheckboxMenuItem) awtItem).getState());
			
		}
		if (fxItem == null) {
			return null;
		}
		// move over any action listeners from swing to fx. 
		fxItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ActionListener[] actions = awtItem.getActionListeners();
				for (int i = 0; i < actions.length; i++) {
					java.awt.event.ActionEvent awtEvent = new java.awt.event.ActionEvent(event.getSource(), 0, null);
					actions[i].actionPerformed(awtEvent);
				}
			}
		});
		// icons ? Need to repaint as fx Scene or some such. 
//		awtIcon = awtItem.get
		return fxItem;
	}
	
	/**
	 * Convert a swing menu item to an FX MenuItem. Sub menu's
	 * will be preserved. 
	 * @param swingItem Swing menu item. 
	 * @return FX Menu Item
	 */
	public static MenuItem swingMenuItemToFX(JMenuItem swingItem) { 
		if (swingItem == null) {
			return null;
		}
		MenuItem fxItem = null;
		/**
		 * First possibility is that the menu item is a menu (this 
		 * is how sub menus are made) in this case make a 
		 * new sub menu to return and iterate through the content 
		 * of the awt component, calling recursively back into this
		 * function in the hope that items will be simple awt MenuItems. 
		 */
		if (swingItem.getClass() == JMenu.class) {
			JMenu swingMenu =  (JMenu) swingItem;
			Menu fxMenu = new Menu(swingMenu.getText());
			fxItem = fxMenu;
			int nItems = swingMenu.getItemCount();
			for (int i = 0; i < nItems; i++) {
				JMenuItem subItem = swingMenu.getItem(i);
				fxMenu.getItems().add(swingMenuItemToFX(subItem));
			}
		}
		else if (swingItem.getClass() == JMenuItem.class) {
			fxItem = new MenuItem(swingItem.getText());
		}
		else if (swingItem.getClass() == JCheckBoxMenuItem.class) {
			CheckMenuItem checkItem;
			fxItem = checkItem = new CheckMenuItem(swingItem.getText());
			checkItem.setSelected(((JCheckBoxMenuItem) swingItem).isSelected());
		}
		else if (swingItem.getClass() == JRadioButtonMenuItem.class) {
			RadioMenuItem radioItem;
			fxItem = radioItem = new RadioMenuItem(swingItem.getText());
			radioItem.setSelected(((JRadioButtonMenuItem) swingItem).isSelected());
			
		}
		if (fxItem == null) {
			return null;
		}
		// move over any action listeners from swing to fx. 
		fxItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				/**
				 * If the underlying swing control was a button, will also need 
				 * to set the button so that if the thing that created the button 
				 * needs to read it's state, it's reading the changed value. 
				 * Do this before sending out the action event.
				 */
				if (event.getSource() instanceof CheckMenuItem) {
					CheckMenuItem cm = (CheckMenuItem) event.getSource();
					((JCheckBoxMenuItem) swingItem).setSelected(cm.isSelected());
				}
				if (event.getSource() instanceof RadioMenuItem) {
					CheckMenuItem cm = (CheckMenuItem) event.getSource();
					((JRadioButtonMenuItem) swingItem).setSelected(cm.isSelected());
				}
				/**
				 * Now send out action events to all the listeners of the menu item ....
				 */
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						ActionListener[] actions = swingItem.getActionListeners();
						for (int i = 0; i < actions.length; i++) {
							java.awt.event.ActionEvent awtEvent = new java.awt.event.ActionEvent(event.getSource(), 0, null);
							actions[i].actionPerformed(awtEvent);
						}
					}
				});
			}
		});
		// icons ? Need to repaint as fx Scene or some such. 
		javax.swing.Icon swingIcon = swingItem.getIcon();
		if (swingIcon != null) {
			fxItem.setGraphic(iconToFX(swingIcon));
		}
//		fxItem.set
		return fxItem;
	}
	
	/**
	 * Convert an FX menu item into a Swing menu item. 
	 * Sub menu's will be preserved. 
	 * @param fxItem FX Menu item
	 * @return Swing menu item. 
	 */
	public static JMenuItem fxMenuItemToSwing(MenuItem fxItem) {
		if (fxItem == null) {
			return null;
		}
		JMenuItem jItem = null;
		/**
		 * First possibility is that the menu item is a menu (this 
		 * is how sub menus are made) in this case make a 
		 * new sub menu to return and iterate through the content 
		 * of the  component, calling recursively back into this
		 * function in the hope that items will be simple awt MenuItems. 
		 */
		if (fxItem.getClass() == Menu.class) {
			Menu fxMenu =  (Menu) fxItem;
			JMenu jMenu = new JMenu(fxItem.getText());
			jItem = jMenu;
			int nItems = fxMenu.getItems().size();
			for (int i = 0; i < nItems; i++) {
				MenuItem subItem = fxMenu.getItems().get(i);
				jMenu.add(fxMenuItemToSwing(subItem));
			}
		}
		else if (fxItem.getClass() == MenuItem.class) {
			jItem = new JMenuItem(fxItem.getText());
		}
		else if (fxItem.getClass() == CheckMenuItem.class) {
			JCheckBoxMenuItem checkItem;
			jItem = checkItem = new JCheckBoxMenuItem(fxItem.getText());
			checkItem.setSelected(((CheckMenuItem) fxItem).isSelected());			
		}
		if (jItem == null) {
			return null;
		}
		jItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				EventHandler<ActionEvent> eventHandler = fxItem.getOnAction();
				if (eventHandler != null) {
					ActionEvent event = new ActionEvent(e.getSource(), null);
					eventHandler.handle(event);
				}
			}
		});
		Node fxGraphic = fxItem.getGraphic();
		if (fxGraphic != null) {
			jItem.setIcon(iconToSwing(fxGraphic));
		}
		
		return jItem;
	}
	
	/**
	 * Convert an icon to an FX node. 
	 * @param icon - the icon to convert
	 * @return a node containing the image of the icon. 
	 */
	public static Node iconToFX(Icon icon){
		if (icon == null) {
			return null;
		}
		Canvas canvas; 
		/**
		 * this function seems to work at converting a swing icon into an FX one. 
		 */
		if (icon instanceof PamSymbol){
			PamSymbolFX symbol= new PamSymbolFX((PamSymbol) icon); 
			canvas= new Canvas(symbol.getWidth(), symbol.getHeight());
			symbol.draw(canvas.getGraphicsContext2D(), new Point2D(symbol.getWidth()/2.,symbol.getHeight()/2)); 
			canvas.setVisible(true);
		}
		else{
			ImageIcon swingImageIcon = (ImageIcon) icon;
			BufferedImage swingImage = toBufferedImage( swingImageIcon.getImage()); 
			WritableImage writableImage = new WritableImage(swingImage.getWidth(), swingImage.getHeight()); 
			writableImage = SwingFXUtils.toFXImage(swingImage, writableImage); 
			canvas= new Canvas(writableImage.getWidth(), writableImage.getHeight());
			canvas.getGraphicsContext2D().drawImage(writableImage, 0, 0);
		}
		return canvas; 
	}
	
	/**
	 * Convert an fx node to a Swing icon. 
	 * @param canvas fx Node
	 * @return Swing icon. 
	 */
	public static Icon iconToSwing(Node node) {
		if (node == null) {
			return null;
		}
		/**
		 * This doesn't work !
		 */
		if (node instanceof Canvas) {
			Canvas fxCanvas = (Canvas) node;
			WritableImage fxImage;// = new WritableImage((int)fxCanvas.getWidth(), (int) fxCanvas.getHeight()/2);  
			SnapshotParameters param = new SnapshotParameters();
		    param.setDepthBuffer(true);
		    param.setTransform(Transform.scale(1, 1));
//		    param.set
			fxImage = fxCanvas.snapshot(param, null);
			BufferedImage swingImage = new BufferedImage((int)fxCanvas.getWidth(), (int) fxCanvas.getHeight(), BufferedImage.TYPE_INT_RGB); 
			swingImage = SwingFXUtils.fromFXImage(fxImage, null);
			Icon swingIcon = new ImageIcon(swingImage);
			return swingIcon;
		}
		return null;
	}
	
	
	/**
	 * Converts a given Image into a BufferedImage
	 * http://stackoverflow.com/questions/13605248/java-converting-image-to-bufferedimage
	 *
	 * @param img The Image to be converted
	 * @return The converted BufferedImage
	 */
	public static BufferedImage toBufferedImage(java.awt.Image img)
	{
	    if (img instanceof BufferedImage)
	    {
	        return (BufferedImage) img;
	    }

	    // Create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

	    // Draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}
	
	
	/**
	 * Animates a node with a flash
	 * @param node - the node for the effect
	 * @param col - the colour of the flash
	 * @param radius - the raioud of the flash
	 * @param duration - the duration of the flash in seconds
	 */
	public static void nodeFlashEffect(Node node, Color col, double radius, double duration ){
		//			ColorInput effect = new ColorInput(0, 0, textBox.getWidth(), textBox.getHeight(), Paint.valueOf("#FFDDDD"));
		//			Timeline flash = new Timeline(
		//					  new KeyFrame(Duration.seconds(0.4), new KeyValue(effect.paintProperty(), Color.RED)),
		//					  new KeyFrame(Duration.seconds(0.8), new KeyValue(effect.paintProperty(), Paint.valueOf("#E0DDDD"))),
		//					  new KeyFrame(Duration.seconds(1.0), new KeyValue(effect.paintProperty(), Paint.valueOf("#DDDDDD"))));

		DropShadow shadow = new DropShadow();
		shadow.setColor(col);
		shadow.setSpread(0.5);

		Timeline shadowAnimation = getFlashTimeLine( shadow,  radius, duration);

		node.setEffect(shadow);
		shadowAnimation.setOnFinished(e -> node.setEffect(null));
		shadowAnimation.play();
	}
	
	/**
	 * Get node flash timeline. This can be used to make  a node flash. 
	 * @param node - the node for the effect
	 * @param col - the colour of the flash
	 * @param radius - the raioud of the flash
	 * @return flash timeline
	 */
	public static Timeline getFlashTimeLine(DropShadow shadow, double radius, double duration) {


		Timeline shadowAnimation = new Timeline(
				new KeyFrame(Duration.ZERO, new KeyValue(shadow.radiusProperty(), 0d)),
				new KeyFrame(Duration.seconds(duration), new KeyValue(shadow.radiusProperty(), radius)));
		shadowAnimation.setAutoReverse(true);
		
		return shadowAnimation;
	}
	



	/**
	 * Convert an html string to normal text. JavaFX labels do not support html
	 * @param htmlString - an html formatted string
	 * @return a string with basic formatting corresponding to html. 
	 */
	public static String htmlToNormal(String htmlString) {
			if (htmlString == null) return null;
			String str = htmlString.replaceAll("<p>", "\n");
			str = str.replaceAll("</p>", "");

			str = str.replaceAll("<br>", "\n");
			str = str.replaceAll("<html>", "");
			str = str.replaceAll("</html>", "");
			return str;
	}
	
	
	/**
	 * Get the average colour of an image.
	 * @param srcImage - the image 
	 * @return - the average colour of the image. 
	 */
	public static Color getDominantColor(Image srcImage) {
		if (null == srcImage) return Color.TRANSPARENT;

		int redBucket = 0;
		int greenBucket = 0;
		int blueBucket = 0;

		int xbin= (int) Math.floor(srcImage.getWidth()/512); 
		int ybin= (int) Math.floor(srcImage.getWidth()/512); 

		PixelReader r = srcImage.getPixelReader();
		//PixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbInstance();

		int pixelCount=0; 
		for (int y = 0; y<srcImage.getHeight(); y=y+ybin) {
			for (int x = 0; x<srcImage.getWidth(); x=x+xbin)
			{
				int color = r.getArgb(x, y); 
				redBucket += (color >> 16) & 0xFF; // Color.red
				greenBucket += (color >> 8) & 0xFF; // Color.greed
				blueBucket += (color & 0xFF); // Color.blue
				pixelCount++; 
			}
		}

		return Color.rgb(
				redBucket / pixelCount,
				greenBucket / pixelCount,
				blueBucket / pixelCount);
	}
	
	/**
	 * Scale an image. 
	 * @param source - the source image
	 * @param targetWidth - the target image width.
	 * @param targetHeight - the target image height.
	 * @param preserveRatio - true to preserve ratio. 
	 * @return the scaled image. 
	 */
	public static Image scale(Image source, int targetWidth, int targetHeight, boolean preserveRatio) {
	    ImageView imageView = new ImageView(source);
	    imageView.setPreserveRatio(preserveRatio);
	    imageView.setFitWidth(targetWidth);
	    imageView.setFitHeight(targetHeight);
	    return imageView.snapshot(null, null);
	}
	
	
	/**
	 * 
	 * Get the hex code form a color
	 * @param color - the color. 
	 * @return the color. 
	 */
	 public static String toRGBCode( Color color )
	    {
	        return String.format( "#%02X%02X%02X",
	            (int)( color.getRed() * 255 ),
	            (int)( color.getGreen() * 255 ),
	            (int)( color.getBlue() * 255 ) );
	    }
	

}
