package videoRangePanel.pamImage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import javax.imageio.ImageIO;

import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;


/**
 * Class which holds photos or other images and reads metadata.
 * @author Jamie Macaulay
 *
 */
public class PamImage extends PamDataUnit {

	/**
	 * The location of the image file. Can be null if the image is pasted 
	 */
	private File imageFile;
	
	/**
	 * Metadata for the image
	 */
	private Metadata metadata;
	
	/**
	 * The image held in memory
	 */
	private BufferedImage image; 
	
	/**
	 * True if an image was loaded from file or pasted successfully
	 */
	private boolean imageOK=false; 

	/**
	 * An array of metadat strings
	 */
	private ArrayList<String> metaDataText=new ArrayList<String>();
	
	/**
	 * The current date of the image. Used for convenience as also stored in millis within the data unit. 
	 */
	private Date date;
	
	/**
	 * The latitude and longitude of the image. 
	 */
	private LatLong geoTag; 
	
	
	/**
	 * Open a photo and extract metadata information. 
	 * @param imageFile
	 * @throws ImageProcessingException
	 * @throws IOException
	 */
	public PamImage(File imageFile, ImageTimeParser timeParser){
		super(0);
		this.imageFile=imageFile; 
		//try and load the image
		try {
			long time0=System.currentTimeMillis();
			image = ImageIO.read(imageFile);
			long time1=System.currentTimeMillis();
//			System.out.println("PamImage: ImageIO.read(file): "+(time1-time0));

		}
		catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
		
		//set the image time. 
		setTimeMilliseconds(timeParser.getTime(imageFile));
		
		//try and load the meta data
		try {
			this.metadata = ImageMetadataReader.readMetadata(imageFile);
			processPhotoData();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		if (image!=null) imageOK=true; 
	}
	
	/**
	 * Create image from writable image and time stamp. 
	 * @param imageFX - the image 
	 * @param time - time stamp in millis. 
	 */
	public PamImage(Image imageFX, long time){
		super(0); 
		this.image= new BufferedImage((int) imageFX.getWidth(),(int) imageFX.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		image=SwingFXUtils.fromFXImage(imageFX, image);
		if (imageFX!=null) imageOK=true; 
		//now add time 
		this.setTimeMilliseconds(time);
		date = new Date(time); 
	}

	/**
	 * Sometimes a photo will have no metadata. In this case the constructor can just consist of the image. 
	 * @param bufferedImage
	 */
	public PamImage(BufferedImage bufferedImage) {
		super(0);
		this.image=bufferedImage;
		if (image!=null) imageOK=true; 
	}

	/**
	 * Find the relevant data within the photos meta data. 
	 */
	public void processPhotoData(){
		if (metadata==null) return; 
		try{
			//NOTE: time is processed by the image time parser. 
			
			// Read gps data
			GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
			if (gpsDirectory!=null){
				GeoLocation geoLocation = gpsDirectory.getGeoLocation();
				if (geoLocation == null || geoLocation.isZero()) geoTag=null;   
				else geoTag=new LatLong(geoLocation.getLatitude(), geoLocation.getLongitude());
			}

			//read all the metadata as text
			for (Directory direc : metadata.getDirectories()) {
				for (Tag tag : direc.getTags()) {
					metaDataText.add( "\t" + tag.getTagName() + " = " + tag.getDescription() );
				}
			}   

		}
		catch( Exception e ){	
			System.out.println( "Could not read metadata" );
			e.printStackTrace();
		}
	}

	/**
	 * Get the buffered image. Used to display in swing. 
	 * @return get the image. 
	 */
	public BufferedImage getImage(){
		return image;
	}
	
	
	/**
	 * Get the Image (JavaFX). Note that this converts from a buffered image on every call. 
	 * @return the FX version of the Bufferred image. 
	 */
	public WritableImage getImageFX(){
		WritableImage image = new WritableImage((int) this.image.getWidth(), (int) this.image.getHeight()); 
		return SwingFXUtils.toFXImage(this.image, image);
	}

	/**
	 * Check whether the image is Ok. This indicates the image 
	 * has been successfully loaded from a file 
	 * @return true if the image has been loaded properly 
	 */
	public boolean imageOK(){
		return imageOK;
	}

	/**
	 * Get the file of the image. This can null if the image was directly set. 
	 * @return the image file 
	 */
	public File getImageFile(){
		return imageFile;
	}
	

	/**
	 * Set the image file. 
	 * @param currentFile - the file of the image. 
	 */
	public void setImageFile(File currentFile) {
		this.imageFile=currentFile; 
	}
	

	/**
	 * Get an array of strings describing meta data for this photograph. 
	 * @return arraylist of strings containi9ng meta data. 
	 */
	public ArrayList<String> getMetaDataText() {
		return metaDataText;
	}

	///metadata///

	/**
	 * Get the name of the image 
	 * @return the name of the image 
	 */
	public String getName(){
		if (imageFile!=null) return imageFile.getName();
		else return image.toString();
	}

//	/**
//	 * Get the time string of the image. 
//	 * @return the time string. 
//	 */
//	public String getTimeString(){
//		if (date==null) return null; 
//		return (PamCalendar.formatDateTime2(this.getTimeMilliseconds(),"yyyy-MM-dd HH:mm:ss.SSS"));
//	}

	/**
	 * Get the geotag for this image contained in the metadata. Returns null if no geotag is avalaible. 
	 * @return the location of the image in latitude and longitude. 
	 */
	public LatLong getGeoTag() {
		return geoTag;
	}

	/**
	 * Get the date of the image 
	 * @return the image date 
	 */
	@Deprecated
	public Date getDate() {
		return date;
	}

	/**
	 * Set the date of the image 
	 * @param date - the date to set 
	 */
	public void setDate(Date date) {
		this.date=date;
		super.setTimeMilliseconds(date.getTime());
	}
	
	@Override
	public void setTimeMilliseconds(long time) {
		super.setTimeMilliseconds(time);
		date=new Date(time); 
	}

	
	/**
	 * Image time parsers. These can be used to get the filetime from filenames. 
	 * @return list of possible time parsers. 
	 */
	public static ArrayList<ImageTimeParser> getImageTimeParsers() {
		
		ArrayList<ImageTimeParser> imageTimeParsers = new ArrayList<ImageTimeParser>(); 
		imageTimeParsers.add(new MetaDataTimeParser()); 
		imageTimeParsers.add(new FileNameTimeParser(FileNameTimeParser.VLC_SNAPSHOT_1)); 

		
		return imageTimeParsers;
	}

	/**
	 * Recalculate the image time with a specified time parser.
	 * @param imageTimeParser - the time parser index
	 */
	public void recalcTime(ImageTimeParser imageTimeParser) {
		if (imageFile!=null) {
			this.setTimeMilliseconds(imageTimeParser.getTime(imageFile));
		}
		else System.err.println("PamImage: The image has no file to parse date from");
	}







}
