package videoRangePanel.pamImage;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.TimeZone;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

/**
 * Time Parser which uses image metadat.  
 */
public class MetaDataTimeParser implements ImageTimeParser, Serializable, Cloneable {

	private Metadata imageMetaData;
	
	private Date date;

	@Override
	public String getName() {
		return "Image MetaData";
	}

	@Override
	public long getTime(File file) {

		try {
			imageMetaData = ImageMetadataReader.readMetadata(file);
			// Read Exif Data
			Directory directory = imageMetaData.getFirstDirectoryOfType( ExifSubIFDDirectory.class );
			if( directory != null ){
				// Read the date
				//we want PAMGUARD to assume any time in the meta data is UTC. 
				date=directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getTimeZone("UTC"));
				//	            	date=directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
				return date.getTime();
			}
			return -1;

		} catch (ImageProcessingException | IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.err.println("MataDataTimeParser: Could not retrieve image metadata");
			return -1; 
		}

	}

	
}
