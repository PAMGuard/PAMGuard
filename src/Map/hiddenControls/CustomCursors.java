package Map.hiddenControls;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.ImageIcon;


public class CustomCursors{
	
	private static Cursor lockCursor;
	
	public static Cursor getLockCursor() {
		if(lockCursor==null) {
			return makeLockCursor();
		}
		return lockCursor;
	}
	
	private static Cursor makeLockCursor() {
	    lockCursor = new CustomCursors("Resources/lockIcon.png").getCursor();
	    return lockCursor;
	}
	
	private String imagePath;
	
	public CustomCursors(String path){
		this.imagePath = path;
	}
	
	public Cursor getCursor() {
		ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource(imagePath));
    	Image cursorImage = icon.getImage();
	    if (cursorImage != null) {
	        Point hotSpot = new Point(0, 0);
	        return Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, hotSpot, "CustomCursor");
	    }else {
	    	System.out.println("Custom cursor is null");
	    	return null;
	    }
	}

}
