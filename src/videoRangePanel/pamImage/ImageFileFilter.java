package videoRangePanel.pamImage;

import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;

public class ImageFileFilter  extends FileFilter {

	String[] fileTypes;
	public ImageFileFilter() {
		fileTypes = ImageIO.getReaderFileSuffixes();
	}
	
	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		String fname = f.getName().toLowerCase();
		
		if (fname == null)  {
			return false; 
		}
		for (int i = 0; i < fileTypes.length; i++) {
			if (fname.endsWith(fileTypes[i])) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getDescription() {
		String str = "Image Files (";
		for (int i = 0; i < fileTypes.length; i++) {
			if (i < fileTypes.length-1) {
				str += String.format("*.%s;", fileTypes[i]);
			}
			else {
				str += String.format("*.%s", fileTypes[i]);
			}
		}
		str += ")";
		return str;
	}
}
