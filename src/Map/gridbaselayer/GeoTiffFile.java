package Map.gridbaselayer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.geotools.api.data.DataSourceException;
import org.geotools.api.geometry.Bounds;
import org.geotools.api.geometry.MismatchedDimensionException;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.NoSuchAuthorityCodeException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import PamUtils.LatLong;
import ucar.nc2.geotiff.GeoTiff;

public class GeoTiffFile {

	private File tifFile;
	private LatLong minLatLong;
	private LatLong maxLatLong;
	private MapRasterImage rasterImage = null;

	private GeoTiffFile(File gtFile, LatLong minLatLong, LatLong maxLatLong) {
		this.tifFile = gtFile;
		this.minLatLong = minLatLong;
		this.maxLatLong = maxLatLong;
	}

	/**
	 * Do some basic unpacking, and check the tiff genuinely
	 * does have geo data in it. Return null if it's only 
	 * an image. 
	 * @param gtFile
	 * @return
	 */
	public static GeoTiffFile makeFile(File f) {
//		GeoTiff gtf = new GeoTiff(f.getAbsolutePath());
//		String info = gtf.showInfo();
//		System.out.println("GT Info: " + info);
		GeoTiffReader reader = null; 
		GridCoverage2D coverage = null;

		try {
			reader = new GeoTiffReader(f);
			coverage = reader.read(null);
		} catch (DataSourceException e) {
			System.out.println(e.getMessage());
			return null;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return null;
		}

		// Get the geographic bounding box
		Bounds envelope = coverage.getEnvelope();
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
	
	public MapRasterImage getImage() {
		if (rasterImage == null) {
			makeRasterImage();
		}
		
		return rasterImage;
	}

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
