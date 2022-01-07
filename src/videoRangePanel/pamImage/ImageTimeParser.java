package videoRangePanel.pamImage;

import java.io.File;
import java.io.Serializable;

/**
 * Interface for reading image timr from filename.
 * @author Jamie Macaulay 
 *
 */
public interface ImageTimeParser {
	
	/**
	 * For displaying in options menu. 
	 */
	public String getName();
	
	/**
	 * 
	 * @param filename
	 * @return
	 */
	public long getTime(File file);

}
