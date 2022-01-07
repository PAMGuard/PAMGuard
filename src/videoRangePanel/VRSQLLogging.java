package videoRangePanel;

import java.sql.Types;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

/**
 * 
 * @author Jamie Macaulay
 *
 */
public class VRSQLLogging extends SQLLogging {

	VRControl vrControl;

	VRProcess vrProcess;

	PamTableDefinition vrTable;

	/**
	 * That should be enough for ~15 8 decimal place latitiude/longitude values. Polygon larger than that may be an issue. 
	 */
	private final static int POLYGON_MAX_LENGTH = 166; 

	PamTableItem imageTime, range, rangeError, pixels, degrees, image, heightValue, heightName, 
	method, calibrationValue, calibrationName, imageAnimal, angleCorrection, animalBearing, comment,
	imageBearing, imagePitch, imageTilt,imageLat,imageLong,locLat,locLong,vrMethod, xpix, ypix, latpoly,
	longpoly, areapix, aream, perimpix, perimm;


	public VRSQLLogging(VRControl vrControl, VRProcess vrProcess) {
		super(vrProcess.getVrDataBlock());
		this.vrControl = vrControl;
		this.vrProcess = vrProcess;

		vrTable = new PamTableDefinition(vrControl.getUnitName(), SQLLogging.UPDATE_POLICY_WRITENEW);
		//when the image was taken
		vrTable.addTableItem(imageTime 			= new PamTableItem("Image_Time", Types.DOUBLE));
		//information on animal location
		vrTable.addTableItem(animalBearing 		= new PamTableItem("Heading", Types.DOUBLE));
		vrTable.addTableItem(angleCorrection 	= new PamTableItem("Heading_Correction", Types.DOUBLE));
		vrTable.addTableItem(range				= new PamTableItem("Range", Types.DOUBLE));
		vrTable.addTableItem(rangeError 		= new PamTableItem("Range_Error", Types.DOUBLE));
		vrTable.addTableItem(locLat				= new PamTableItem("latitude", Types.DOUBLE));
		vrTable.addTableItem(locLong 			= new PamTableItem("longitude", Types.DOUBLE));
		vrTable.addTableItem(imageAnimal 		= new PamTableItem("Animal_No.", Types.INTEGER));

		//information on image
		vrTable.addTableItem(image 				= new PamTableItem("Image_Name", Types.CHAR, 20));
		vrTable.addTableItem(imageBearing 		= new PamTableItem("Image_Bearing", Types.DOUBLE));
		vrTable.addTableItem(imagePitch 		= new PamTableItem("Image_Pitch", Types.DOUBLE));
		vrTable.addTableItem(imageTilt 			= new PamTableItem("image_Tilt", Types.DOUBLE));
		vrTable.addTableItem(imageLat 			= new PamTableItem("image_Lat", Types.DOUBLE));
		vrTable.addTableItem(imageLong 			= new PamTableItem("image_Long", Types.DOUBLE));
		vrTable.addTableItem(heightValue	 	= new PamTableItem("Height", Types.DOUBLE));
		vrTable.addTableItem(heightName 		= new PamTableItem("Height Name", Types.CHAR, 20));
		//calc methods
		vrTable.addTableItem(vrMethod 			= new PamTableItem("VR_Method", Types.CHAR, 20));
		vrTable.addTableItem(method 			= new PamTableItem("Calc_Method", Types.CHAR, 20));
		vrTable.addTableItem(calibrationValue 	= new PamTableItem("Calibration", Types.DOUBLE));
		vrTable.addTableItem(calibrationName 	= new PamTableItem("Calibration Name", Types.CHAR, 20));
		//random stuff
		//--
		//comment on this image
		vrTable.addTableItem(comment = new PamTableItem("Comment", Types.CHAR, 140));

		//polygon data 
		vrTable.addTableItem(xpix 				= new PamTableItem("x_Pix_Poly", Types.CHAR, POLYGON_MAX_LENGTH)); //all x pixel co-ordinates
		vrTable.addTableItem(ypix 				= new PamTableItem("y_Pix_Poly", Types.CHAR, POLYGON_MAX_LENGTH));  //all y pixel co-ordinates
		vrTable.addTableItem(latpoly 			= new PamTableItem("Latitiude_Poly", Types.CHAR, POLYGON_MAX_LENGTH));  //all lat pixel co-ordinates
		vrTable.addTableItem(longpoly 			= new PamTableItem("Longitude_Poly", Types.CHAR, POLYGON_MAX_LENGTH));  //all long pixel co-ordinates
		vrTable.addTableItem(areapix 			= new PamTableItem("Area_pix", Types.DOUBLE));  //area in pixels
		vrTable.addTableItem(aream 				= new PamTableItem("Area_m^2", Types.DOUBLE)); //area in m^2
		vrTable.addTableItem(perimpix 			= new PamTableItem("Perimeter_pix", Types.DOUBLE)); //perimeter of polygon in pixels
		vrTable.addTableItem(perimm 			= new PamTableItem("Perimeter_m", Types.DOUBLE));  //perimeter of polygon in meters

		setTableDefinition(vrTable);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {

		VRDataUnit vrDataUnit = (VRDataUnit) pamDataUnit;
		VRMeasurement vrm = vrDataUnit.getVrMeasurement();
//		System.out.println("VRSQLLogging:  time: " +  PamCalendar.formatDateTime(vrDataUnit.getTimeMilliseconds()));


		imageTime						.setValue(vrm.imageTime);
		angleCorrection					.setValue(vrm.angleCorrection);
		animalBearing					.setValue(vrm.locBearing);
		range							.setValue(vrm.locDistance);
		rangeError						.setValue(vrm.locDistanceError);
		if (vrm.imageOrigin!=null){
			locLat						.setValue(vrm.locLatLong.getLatitude());
			locLong						.setValue(vrm.locLatLong.getLongitude());
		}
		imageAnimal						.setValue(vrm.imageAnimal);
		image							.setValue(vrm.imageName);
		imageBearing					.setValue(vrm.imageBearing);
		imagePitch						.setValue(vrm.imagePitch);
		imageTilt						.setValue(vrm.imageTilt);
		if (vrm.imageOrigin!=null){
			imageLat					.setValue(vrm.imageOrigin.getLatitude());
			imageLong					.setValue(vrm.imageOrigin.getLongitude());
		}
		if (vrm.heightData!=null) {
			heightValue					.setValue(vrm.heightData.height);
			heightName					.setValue(vrm.heightData.name);
		}

		if (vrm.vrMethod!=null) {
			vrMethod					.setValue(vrm.vrMethod.getName());
			method						.setValue(vrm.rangeMethod.getName());
		}

		if (vrm.calibrationData!=null){
			calibrationValue			.setValue(vrm.calibrationData.degreesPerUnit);
		}

		angleCorrection					.setValue(vrm.angleCorrection);

		//polygon stuff
		if (vrm.polygonArea!=null){
			String xpixStr = arrayToString(VRUtils.points2Array(vrm.polygonArea, true));  
			String ypixStr = arrayToString(VRUtils.points2Array(vrm.polygonArea, false));  

			this.xpix					.setValue(xpixStr);
			this.ypix					.setValue(ypixStr);

			this.areapix				.setValue(vrm.polyAreaPix);
			this.perimpix				.setValue(vrm.polyPerimPix);
			//might be geo-referenced 
			if (vrm.polygonLatLon!=null) {
				String latStr = arrayToString(VRUtils.latLong2Array(vrm.polygonLatLon, true));  
				String longStr = arrayToString(VRUtils.latLong2Array(vrm.polygonLatLon, false));

				this.latpoly			.setValue(latStr);
				this.longpoly			.setValue(longStr);

				this.aream				.setValue(vrm.polyAreaM);
				this.perimm				.setValue(vrm.polyPerimM);
			}
		}

		comment.setValue(vrm.comment);
	}


	/**
	 * Convert an array to a comma delimited string. 
	 * @param array - the array to convert
	 * @return - string representaiton of the array. 
	 */
	private String arrayToString(double[] array) {
		String string=""; 
		for (int i=0; i<array.length; i++) {
			string+=(String.format("%.8f", array[i])+ ",");
		}
		return string;
	}


	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long dataTime, int iD) {

		//TODO. create the data unit from the database. 
		VRMeasurement vrm = new VRMeasurement();

		vrm.imageTime=(long) imageTime.getDoubleValue();

		VRDataUnit vrDataUnit=new VRDataUnit(dataTime,vrm);


		return vrDataUnit;
	}


}
