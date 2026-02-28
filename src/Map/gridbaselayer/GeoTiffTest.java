package Map.gridbaselayer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;
import org.geotools.api.data.DataSourceException;
import org.geotools.api.geometry.Bounds;
import org.geotools.api.geometry.MismatchedDimensionException;
import org.geotools.api.geometry.Position;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.NoSuchAuthorityCodeException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.util.factory.Hints;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import PamUtils.LatLong;
import ucar.nc2.geotiff.GeoTiff;

public class GeoTiffTest {

//	String fn = "C:\\ProjectData\\RobRiver\\y6\\RiverDee2.tif";
//	String fn = "C:\\PAMGuardTest\\SampleCharts\\UKHO Sample Data - Bathymetry.tif";
	String fn = "C:\\PAMGuardTest\\SampleCharts\\A00605.tif";
	public GeoTiffTest() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		GeoTiffTest gtt = new GeoTiffTest();
		//		gtt.runApache();
		gtt.runGoeTools();
	}

	private void runGoeTools() {
		File f = new File(fn);
		GeoTiff gtf = new GeoTiff(fn);
		String info = gtf.showInfo();
		System.out.println("GT Info: " + info);
		GeoTiffReader reader = null; 
		GridCoverage2D coverage = null;

		try {
			reader = new GeoTiffReader(f);
			coverage = reader.read(null);
		} catch (DataSourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			e.printStackTrace();
		} catch (FactoryException e) {
			e.printStackTrace();
		}

		GeometryFactory gf = new GeometryFactory();
		Point minPoint = gf.createPoint(new Coordinate(minLon, minLat));
		Point maxPoint = gf.createPoint(new Coordinate(maxLon, maxLat));
		//		sourcePoint = gf.createPoint(new Coordinate(lcC[0], lcC[1]));

		try {
			minPoint = (Point) JTS.transform(minPoint, transform);
			maxPoint = (Point) JTS.transform(maxPoint, transform);
		} catch (MismatchedDimensionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LatLong minLL = new LatLong(minPoint.getX(), minPoint.getY());
		LatLong maxLL = new LatLong(maxPoint.getX(), maxPoint.getY());
		System.out.printf("Bounds are %s to %s\n", minLL, maxLL);
		//		System.out.println("Coordinats system: " + refSystem);
		//		System.out.println("Min latlong = " + minLL);
		//		LatLong maxLL = new LatLong(maxLat,maxLon);
		//		System.out.printf("bounds are %s , %s x %s y %s\n", minLat, minLon, maxLat-minLat, maxLon-minLon);
		//		GeoTiffFormat gtf;
		//		
		//		  AbstractGridFormat format = GridFormatFinder.findFormat(f);
		//		  System.out.println("Format: " + format);
		//		  Hints hints = new Hints();
		////	        if (format instanceof GeoTiffFormat) {
		//	            hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
		////	        }
		//	        AbstractGridCoverage2DReader reader = format.getReader(f, hints); 
		////	        AbstractGridCoverage2DReader reader = format.getReader(parameters);
		//	         org.geotools.api.referencing.crs.CoordinateReferenceSystem crs = reader.getCoordinateReferenceSystem();
		//	        System.out.println(crs);

	}

	private void runApache() {
		File f = new File(fn);
		ImageMetadata metaData = null;
		try {
			metaData = Imaging.getMetadata(f);
		} catch (ImagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Meta: " + metaData);
		//		List<? extends ImageMetadataItem> items = metaData.getItems();
		//		for (ImageMetadataItem item : items) {
		//			System.out.println(item.toString());
		//		}
	}

}
