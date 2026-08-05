package Map.gridbaselayer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.geotools.api.geometry.Bounds;
import org.geotools.api.geometry.MismatchedDimensionException;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.NoSuchAuthorityCodeException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import PamUtils.LatLong;

public class GeoTiffFile {

	private File tifFile;
	private LatLong minLatLong;
	private LatLong maxLatLong;
	private MapRasterImage rasterImage = null;

	private GeoTiffFile(File gtFile, LatLong minLatLong, LatLong maxLatLong) {
		this.tifFile = gtFile;
		this.minLatLong = minLatLong;
		this.maxLatLong = maxLatLong;
//		ImageN imN = null;
	}

	/**
	 * Do some basic unpacking, and check the tiff genuinely
	 * does have geo data in it. Return null if it's only 
	 * an image. 
	 * @param gtFile
	 * @return
	 */
	public static GeoTiffFile makeFile(File f) {

		GeoTiffReader reader = null; 
		Bounds envelope = null;

		try {
			reader = new GeoTiffReader(f);
			 envelope = reader.getOriginalEnvelope();
			 /**
			  * Several days of my life were lost to this function that I'll never ever get back. 
			  * Calling reader.read would invoke ImageN which is an eclipse library, so it would work 
			  * fine running from Eclipse, but proved impossible to get the correct Maven repositories 
			  * to include ImageN in the build. By using the above function to just get the envelope
			  * we're able to read the coordinates of the goetiff. The actual image itself can be
			  * read later with the much more robust and standard ImageIO.read function.
			  */
//			coverage = reader.read(null);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return null;
		}

		double minLon = envelope.getMinimum(0);
		double maxLon = envelope.getMaximum(0);
		double minLat = envelope.getMinimum(1);
		double maxLat = envelope.getMaximum(1);
		CoordinateReferenceSystem refSystem = envelope.getCoordinateReferenceSystem();
		MathTransform transform = null;
		try {
			CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");
			transform = CRS.findMathTransform(refSystem, targetCRS);
		} catch (NoSuchAuthorityCodeException e) {
			System.out.println(e.getMessage());
			return null;
		} catch (FactoryException e) {
			System.out.println(e.getMessage());
			return null;
		}

		GeometryFactory gf = new GeometryFactory();
		Point minPoint = gf.createPoint(new Coordinate(minLon, minLat));
		Point maxPoint = gf.createPoint(new Coordinate(maxLon, maxLat));

		try {
			minPoint = (Point) JTS.transform(minPoint, transform);
			maxPoint = (Point) JTS.transform(maxPoint, transform);
		} catch (MismatchedDimensionException e) {
			System.out.println(e.getMessage());
			return null;
		} catch (TransformException e) {
			System.out.println(e.getMessage());
			return null;
		}
		LatLong minLL = new LatLong(minPoint.getX(), minPoint.getY());
		LatLong maxLL = new LatLong(maxPoint.getX(), maxPoint.getY());
//		System.out.printf("Bounds are %s to %s\n", minLL, maxLL);
		return new GeoTiffFile(f, minLL, maxLL);
	}
	
	/**
	 * Get the image from the file. Will only read once, then store 
	 * in memory as a buffered image. 
	 * @return MapRasterImage that has coordinate bounds and the image data in the same object. 
	 */
	public MapRasterImage getImage() {
		if (rasterImage == null) {
			makeRasterImage();
		}
		
		return rasterImage;
	}

	/**
	 * Make the image. Called once. 
	 */
	private void makeRasterImage() {
		BufferedImage buffImage = null;
		try {
			buffImage = ImageIO.read(tifFile);
		}
		catch (IOException e) {
			return;
		}
		double[] latRange = {minLatLong.getLatitude(), maxLatLong.getLatitude()};
		double[] lonRange = {minLatLong.getLongitude(), maxLatLong.getLongitude()};
		rasterImage = new MapRasterImage(latRange, lonRange, buffImage);
	}

	/**
	 * @return the tifFile
	 */
	public File getTifFile() {
		return tifFile;
	}

	/**
	 * @return the minLatLong
	 */
	public LatLong getMinLatLong() {
		return minLatLong;
	}

	/**
	 * @return the maxLatLong
	 */
	public LatLong getMaxLatLong() {
		return maxLatLong;
	}
}
