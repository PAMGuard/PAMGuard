package tethys.localization;

import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;

import Array.ArrayManager;
import Array.PamArray;
import Array.Streamer;
import Array.streamerOrigin.OriginSettings;
import Array.streamerOrigin.StaticOriginSettings;
import GPS.GpsData;
import GPS.GpsDataUnit;
import Localiser.LocalisationAlgorithm;
import Localiser.LocalisationAlgorithmInfo;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamDetection.LocalisationInfo;
import PamUtils.LatLong;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.DataUnitFileInformation;
import nilus.AlgorithmType;
import nilus.AngularCoordinateType;
import nilus.BearingType;
import nilus.DataSourceType;
import nilus.Detections;
import nilus.Helper;
import nilus.LocalizationType;
import nilus.Localize;
import nilus.SpeciesIDType;
import nilus.WGS84CoordinateType;
import nilus.AlgorithmType.SupportSoftware;
import nilus.LocalizationType.Angular;
import nilus.LocalizationType.Bearing;
import nilus.LocalizationType.Parameters;
import nilus.LocalizationType.References;
import nilus.LocalizationType.WGS84;
import nilus.LocalizationType.Parameters.TargetMotionAnalysis;
import nilus.LocalizationType.Parameters.UserDefined;
import nilus.LocalizationType.References.Reference;
import nilus.Localize.Effort;
import nilus.Localize.Effort.CoordinateReferenceSystem;
import nilus.Localize.Effort.ReferencedDocuments;
import nilus.Localize.Effort.CoordinateReferenceSystem.ReferenceFrame;
import nilus.Localize.Effort.ReferencedDocuments.Document;
import pamMaths.PamVector;
import tethys.Collection;
import tethys.TethysControl;
import tethys.TethysTimeFuncs;
import tethys.niluswraps.PDeployment;
import tethys.output.StreamExportParams;
import tethys.pamdata.AutoTethysProvider;
import tethys.pamdata.TethysDataProvider;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.SpeciesMapItem;

/**
 * Class to build a single localisation document during export. 
 * Works hand in had with the global LocalisationHandler and also with 
 * the LocalizationCreator that should be part of each localisation algorithm.
 * <p>To understand the difference, this contains most of the required functionality
 * but by having the interface on specific algorithms, it's easy for them to override
 * that on a localiser specific basis.  
 * algorithm was used. 
 * @author dg50
 *
 */
public class LocalizationBuilder {

	private PDeployment deployment;
	private Detections detectionsDocument;
	private PamDataBlock dataBlock;
	private StreamExportParams streamExportParams;
	private TethysControl tethysControl;
	private LocalizationHandler localisationHandler;
	private LocalisationAlgorithm localisationAlgorithm;
	
	private Localize currentDocument;
	private LocalizationCreator localisationCreator;
	private TethysDataProvider dataProvider;
	
	private Helper helper;

	public LocalizationBuilder(TethysControl tethysControl, PDeployment deployment, Detections detectionsDocument, PamDataBlock dataBlock,
			StreamExportParams exportParams) {
		this.deployment = deployment;
		this.detectionsDocument = detectionsDocument;
		this.dataBlock = dataBlock;
		this.streamExportParams = exportParams;
		this.tethysControl = tethysControl;
		try {
			helper = new Helper();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		dataProvider = dataBlock.getTethysDataProvider(tethysControl);
		localisationHandler = tethysControl.getLocalizationHandler();
		localisationAlgorithm = dataBlock.getLocalisationAlgorithm();
		if (localisationAlgorithm != null) {
			localisationCreator = localisationAlgorithm.getTethysCreator();
		}
		currentDocument = startLocalisationDocument(deployment, detectionsDocument, dataBlock, exportParams);
	}

	public Localize startLocalisationDocument(PDeployment deployment, Detections detectionsDocument, PamDataBlock dataBlock,
			StreamExportParams exportParams) {
		currentDocument = new Localize();
		try {
			Helper.createRequiredElements(currentDocument);
		} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
			return null;
		}		
		Effort eff = currentDocument.getEffort();
		if (eff == null) {
			eff = new Effort();
			try {
				Helper.createRequiredElements(eff);
			} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
				e.printStackTrace();
				return null;
			}		
			currentDocument.setEffort(eff);
		}
		if (detectionsDocument != null) {
			/*
			 *  add the reference document information.
			 *  Within PAMGuard, this will always be 1:1 with a Detections doc.
			 */
			
			ReferencedDocuments refDocs = eff.getReferencedDocuments();
			if (refDocs == null) {
				refDocs = new ReferencedDocuments();
				try {
					Helper.createRequiredElements(refDocs);
				} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
					e.printStackTrace();
				}
				eff.setReferencedDocuments(refDocs);
			}
			Document detectsDoc = new Document();
			detectsDoc.setId(detectionsDocument.getId());
			detectsDoc.setType(Collection.Detections.collectionName());
			detectsDoc.setIndex(BigInteger.ZERO);
			eff.getReferencedDocuments().getDocument().add(detectsDoc);
		}


		String prefix = deployment.nilusObject.getId() + "_" + dataProvider.getDetectionsName() + "_L";
		String fullId = localisationHandler.getLocalisationdocId(prefix);
		currentDocument.setId(fullId);
		//		detections.setDescription(dataProvider.getDescription(deployment, tethysExportParams));
		currentDocument.setDescription(exportParams.getNilusDetectionDescription());
		DataSourceType dataSource = new DataSourceType();
		dataSource.setDeploymentId(deployment.nilusObject.getId());
		//		dataSource.setEnsembleId(""); ToDo
		currentDocument.setDataSource(dataSource);
		AlgorithmType algorithm = currentDocument.getAlgorithm();

		if (dataProvider != null) {
			algorithm = dataProvider.getAlgorithm(Collection.Localizations);
			//			detections.setAlgorithm(algorithm);
		}
		LocalisationAlgorithm locAlgorithm = dataBlock.getLocalisationAlgorithm();
		LocalisationAlgorithmInfo locAlgoinfo = null;
		if (locAlgorithm != null) {
			locAlgoinfo = locAlgorithm.getAlgorithmInfo();
		}
		if (locAlgoinfo != null) {
			algorithm.setMethod(locAlgoinfo.getAlgorithmName());
		}
		else {
			algorithm.setMethod(localisationHandler.getMethodString(dataBlock));
		}
		algorithm.setSoftware(localisationHandler.getSoftwareString(dataBlock));
		algorithm.setVersion(localisationHandler.getVersionString(dataBlock));

		List<SupportSoftware> supSoft = algorithm.getSupportSoftware();
		SupportSoftware supportSoft = new SupportSoftware();
		supportSoft.setSoftware(localisationHandler.getSupportSoftware(dataBlock));
		supportSoft.setVersion(localisationHandler.getSupportSoftwareVersion(dataBlock));
		supSoft.add(supportSoft);
		currentDocument.setAlgorithm(algorithm);

		currentDocument.setUserId("PAMGuard user");
		//		localisations.setEffort(getLocaliserEffort(deployment, dataBlock, exportParams));
		sortLocaliseCoordinates(dataBlock);

		// sort out coordinate system. 

		return currentDocument;
	}
	
	private boolean sortLocaliseCoordinates(PamDataBlock dataBlock) {
		boolean done = false;
		if (localisationCreator != null) {
			done = localisationCreator.sortLocalisationCoordinates(this, dataBlock);
		}
		if (!done) {
			sortStandardCoordinates(dataBlock);
		}
		return done;
	}
	
	/**
	 * Get a default reference frame for the header of a localization document. This is very dependent
	 * on the localization subtype. Also on whether the array is fixed or moving. It it's fixed, then
	 * we also need to add the instrument lat long. 
	 * @param coordinateName
	 * @param subType
	 * @return
	 */
	public ReferenceFrame getDefaultReferenceFrame(CoordinateName coordinateName, LocalizationSubTypes subType) {
		ReferenceFrame referenceFrame = new ReferenceFrame();
		switch (subType) {
		case Derived:
			referenceFrame.setAnchor(Anchor.UTMZone.toString()); // i never use this on. If I do, this will need work !
			break;
		case Engineering:
			referenceFrame.setAnchor(Anchor.instrument.toString()); 
			break;
		case Geographic:
			referenceFrame.setAnchor(Anchor.WGS84.toString()); 
			break;
		default:
			break;
		}
		/*
		 * And see if it's a fixed or moving array. Will just have to look at the 
		 * first streamer here. Hard to copy with multiples !
		 */
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		Streamer streamer = array.getStreamer(0);
		if (streamer != null) {
			OriginSettings origin = streamer.getOriginSettings();
			if (origin instanceof StaticOriginSettings) {
				StaticOriginSettings staticOrigin = (StaticOriginSettings) origin;
				GpsDataUnit ll = staticOrigin.getStaticPosition();
				if (ll != null) {
					GpsData pos = ll.getGpsData();
					if (pos != null) {
						referenceFrame.setLatitude(TethysLatLong.formatLatitude(pos.getLatitude(),TethysLatLong.mmDecimalPlaces));
						referenceFrame.setLongitude(TethysLatLong.formatLongitude(pos.getLongitude(),TethysLatLong.mmDecimalPlaces));
						referenceFrame.setDatum(String.format("Altitude %3.2fm", pos.getHeight()));
					}
				}
			}
		}
		
		return referenceFrame;
	}
	
	public boolean sortStandardCoordinates(PamDataBlock dataBlock) {
		LocalisationInfo locInfo = dataBlock.getLocalisationContents();
		Effort locEffort = currentDocument.getEffort();
		if (locEffort == null) {
			locEffort = new Effort();
			currentDocument.setEffort(locEffort);
		}
		try {
			Helper.createRequiredElements(locEffort);
		} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
			return false;
		}
		locEffort.setTimeReference("relative");
//		List<String> locTypes = locEffort.getLocalizationType();
		boolean ambiguity = locInfo.hasLocContent(LocContents.HAS_AMBIGUITY);
		CoordinateReferenceSystem coordRefs = locEffort.getCoordinateReferenceSystem();		
		ReferenceFrame referenceFrame = null;
		List<String> locTypes = locEffort.getLocalizationType();
		if (locInfo.getLocContent() == 0) {
			return false;
		}
		else if (locInfo.hasLocContent(LocContents.HAS_LATLONG)) {
			coordRefs.setName(CoordinateName.WGS84.toString());
			coordRefs.setSubtype(LocalizationSubTypes.Geographic.toString());
			locTypes.add(LocalizationTypes.Point.toString());
//			locEffort.set
			if (locInfo.hasLocContent(LocContents.HAS_DEPTH)) {
				locEffort.setDimension(3);
			}
			else {
				locEffort.setDimension(2);
			}
			referenceFrame = getDefaultReferenceFrame(CoordinateName.WGS84, LocalizationSubTypes.Geographic);
//			locEffort.set
		}
		else if (locInfo.hasLocContent(LocContents.HAS_XYZ)) {
			coordRefs.setName(CoordinateName.Cartesian.toString());
			coordRefs.setSubtype(LocalizationSubTypes.Engineering.toString());
			locTypes.add(LocalizationTypes.Point.toString());
			locEffort.setDimension(3);
			referenceFrame = getDefaultReferenceFrame(CoordinateName.Cartesian, LocalizationSubTypes.Engineering);
		}
		else if (locInfo.hasLocContent(LocContents.HAS_XY)) {
			coordRefs.setName(CoordinateName.Cartesian.toString());
			coordRefs.setSubtype(LocalizationSubTypes.Engineering.toString());
			locTypes.add(LocalizationTypes.Point.toString());
			locEffort.setDimension(2);
			referenceFrame = getDefaultReferenceFrame(CoordinateName.Cartesian, LocalizationSubTypes.Engineering);
		}
		else if (locInfo.hasLocContent(LocContents.HAS_BEARING)) {
			coordRefs.setName(CoordinateName.Polar.toString());
			coordRefs.setSubtype(LocalizationSubTypes.Engineering.toString());
			locTypes.add(LocalizationTypes.Bearing.toString());
			if (ambiguity) {
				locEffort.setDimension(1);
			}
			else {
				locEffort.setDimension(2);
			}
			referenceFrame = getDefaultReferenceFrame(CoordinateName.Polar, LocalizationSubTypes.Engineering);
		}
		else {
			return false;
		}
		coordRefs.setReferenceFrame(referenceFrame);
		
		return true;
	}
	
	public LocalizationType addLocalization(PamDataUnit dataUnit) {
		LocalizationType newLoc = createLocalization(dataUnit);
		if (newLoc != null) {
			currentDocument.getLocalizations().getLocalization().add(newLoc);
		}
		return newLoc;
	}

	private LocalizationType createLocalization(PamDataUnit dataUnit) {
		if (localisationCreator != null) {
			return localisationCreator.createLocalization(this, dataUnit);
		}
		else {
			return createStandardLocalization(dataUnit);
		}
	}

	public LocalizationType createStandardLocalization(PamDataUnit dataUnit) {
			/*
			 * Get the current type from the document. This should always exist, so
			 * just go for it, then switch on the value to make the localisation. 
			 */
			CoordinateReferenceSystem coordSystem = currentDocument.getEffort().getCoordinateReferenceSystem();
			String name = coordSystem.getName();
			CoordinateName coordName = CoordinateName.valueOf(name);
			if (coordName == null) {
				return null;
			}
			LocalizationType locEl = null;
			switch (coordName) {
			case Cartesian:
				locEl = createCartesianLoc(dataUnit);
				break;
			case Cylindrical:
				locEl = createCylindricalLoc(dataUnit);
				break;
			case PerpendicularRange:
				locEl = createPerpRange(dataUnit);
				break;
			case Polar:
				locEl = createPolarLoc(dataUnit);
				break;
			case Range:
				locEl = createRangeLoc(dataUnit);
				break;
			case Spherical:
				locEl = createSphericalLoc(dataUnit);
				break;
			case UTM:
				break;
			case WGS84:
				locEl = createWGS84Loc(dataUnit);
				break;
			default:
				break;

			}
			return locEl;
		}

		public LocalizationType makeBaseLoc(PamDataUnit dataUnit) {
			LocalizationType locType = new LocalizationType();
			try {
				Helper.createRequiredElements(locType);
			} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
				e.printStackTrace();
			}
			locType.setTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(dataUnit.getTimeMilliseconds()));
			
			locType.setEvent(TethysTimeFuncs.xmlGregCalFromMillis(dataUnit.getTimeMilliseconds()).toString());

			DataBlockSpeciesManager spManager = dataBlock.getDatablockSpeciesManager();
			if (spManager != null) {
				SpeciesMapItem speciesStuff = spManager.getSpeciesItem(dataUnit);
				SpeciesIDType species = new SpeciesIDType();
				if (speciesStuff != null) {
					species.setValue(BigInteger.valueOf(speciesStuff.getItisCode()));
					locType.setSpeciesId(species);
				}
			}
			
			/*
			 * Add references back to PAMGuard data as for Detections. 
			 * Same code as in Detections, but classes are different (though with the same fields). 
			 */
			Parameters params = getParameters(locType);
			String uid = BigInteger.valueOf(dataUnit.getUID()).toString();
			Element el = addUserDefined(params,"PAMGuardUID", uid);
			DataUnitFileInformation fileInf = dataUnit.getDataUnitFileInformation();
			if (fileInf != null) {
				el.setAttribute("BinaryFile", fileInf.getShortFileName(2048));
				el.setAttribute("FileIndex", Long.valueOf(fileInf.getIndexInFile()).toString());
			}
			if (dataUnit.getDatabaseIndex() > 0) {
				// only write the database index if it's > 0, i.e. is used.
				addUserDefined(params, "DatabaseId", String.format("%d", dataUnit.getDatabaseIndex()));
			}

			/*
			 * Not needed. something to do with references to Detection docs. 
			 */
//			References references = locType.getReferences();
//			if (references == null) {
//				references = new References();
//				try {
//					Helper.createRequiredElements(references);
//				} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
//					e.printStackTrace();
//				}
//				locType.setReferences(references);
//			}
//			Reference reference = new Reference();
//			reference.setIndex(BigInteger.valueOf(dataUnit.getUID()));
//			reference.setEventRef("UID");
//			locType.getReferences().getReference().add(reference);

			return locType;
		}

		/**
		 * Get angle in degrees constrained to 0-360
		 * @param radians
		 * @return
		 */
		public double constrainRadianAngle(double radians) {
			double deg = Math.toDegrees(radians);
			if (Math.abs(deg) > 3600) {
				return 359.9;
			}
			deg = PamUtils.constrainedAngle(deg);
			deg = AutoTethysProvider.roundDecimalPlaces(deg, 2);
			return deg;
		}	

		/**
		 * Convert a vertical angle from radians to degrees and round. 
		 * @param radians
		 * @return
		 */
		public double toSlantAngle(double radians) {
			/*
			 * these really need to be constrained to -90 to 90, but I don't see what to do if
			 * they are outside that range.
			 */
			double deg = Math.toDegrees(radians);
			deg= PamUtils.constrainedAngle(deg, 180);
			deg = AutoTethysProvider.roundDecimalPlaces(deg, 2);
			return deg;				
		}

		public LocalizationType createWGS84Loc(PamDataUnit dataUnit) {
			AbstractLocalisation loc = dataUnit.getLocalisation();
			if (loc == null) {
				return null;
			}
			LatLong latlong = loc.getLatLong(0);
			if (latlong == null) {
				return null;
			}
			LocalizationType locType = makeBaseLoc(dataUnit);
			
			
			/**
			 * Export the latlong data.
			 */
			WGS84 wgs84 = new WGS84();
			WGS84CoordinateType coord = new WGS84CoordinateType();
			wgs84.setCoordinate(coord);
			coord.setLongitude(latlong.getLongitude());
			coord.setLatitude(latlong.getLatitude());
			coord.setElevationM(AutoTethysProvider.roundDecimalPlaces(latlong.getHeight(),3));

			PamVector planarVec = loc.getPlanarVector();
			locType.setWGS84(wgs84);
			
//			locType.setParameters(null);
			Parameters params = getParameters(locType);
			TargetMotionAnalysis tma = new TargetMotionAnalysis();
			tma.setStart(TethysTimeFuncs.xmlGregCalFromMillis(dataUnit.getTimeMilliseconds()));
			tma.setEnd(TethysTimeFuncs.xmlGregCalFromMillis(dataUnit.getEndTimeInMilliseconds()));
			params.setTargetMotionAnalysis(tma);
			

			// see if it's possible to get a beam measurement. 
			if (loc instanceof GroupLocalisation) {
				GroupLocalisation groupLoc = (GroupLocalisation) loc;
				GroupLocResult groupLocResult = groupLoc.getGroupLocaResult(0);
				Double perpDist = groupLocResult.getPerpendicularDistance();
				Long beamTime = groupLocResult.getBeamTime();
				if (perpDist != null && beamTime != null) {
					AngularCoordinateType acType = new AngularCoordinateType();
					acType.setAngle1(90);
					acType.setDistanceM(AutoTethysProvider.roundDecimalPlaces(perpDist,1));
					Angular angular = new Angular();
					angular.setCoordinate(acType);
					locType.setAngular(angular);
					locType.setTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(beamTime));
					currentDocument.getEffort().setTimeReference(TimeReference.beam.toString());
				}
				//			groupLoc.getp
			}

			/*
			 * Try to also add a range loc. 
			 */
			//		loc.

			return locType;
		}
		
		/**
		 * Convenience method to get the Parameters object for a localisation and
		 * to automatically create it if it doesn't exist. 
		 * @param localization
		 * @return
		 */
		public Parameters getParameters(LocalizationType localization) {
			Parameters params = localization.getParameters();
			if (params == null) {
				params = new Parameters();
				localization.setParameters(params);
			}
			return params;
		}
		
		public Element addUserDefined(Parameters parameters, String parameterName, String parameterValue) {
			UserDefined userDefined = parameters.getUserDefined();
			if (userDefined == null) {
				userDefined = new UserDefined();
				parameters.setUserDefined(userDefined);
			}
			Element el = null;
			try {
				el = helper.AddAnyElement(userDefined.getAny(), parameterName, parameterValue);
			} catch (JAXBException e) {
				e.printStackTrace();
				return null;
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				return null;
			}
			return el;
		}

		public LocalizationType createSphericalLoc(PamDataUnit dataUnit) {
			// TODO Auto-generated method stub
			return null;
		}

		public LocalizationType createRangeLoc(PamDataUnit dataUnit) {
			// TODO Auto-generated method stub
			return null;
		}

		public LocalizationType createBearingLoc(PamDataUnit dataUnit) {
			AbstractLocalisation loc = dataUnit.getLocalisation();
			if (loc == null) {
				return null;
			}
			LocalizationType locType = makeBaseLoc(dataUnit);
			double[] angles = loc.getAngles();
			if (angles == null || angles.length == 0) {
				return null;
			}
			BearingType angType = new BearingType();
			angType.setAngle1(constrainRadianAngle(angles[0]));
			if (angles.length >= 2) {
				angType.setAngle2(toSlantAngle(angles[1]));
				//			if (angType.getAngle2() > 360) {
				//				angType.setAngle2(Math.toDegrees(angles[1]));
				//			}
			}
			Bearing bearing = new Bearing();
			bearing.setCoordinate(angType);
			locType.setBearing(bearing);

			double[] angErr = loc.getAngleErrors();
			if (angErr != null && angErr.length >= 1) {
				BearingType angErrType = new BearingType();
				angErrType.setAngle1(constrainRadianAngle(angErr[0]));
				if (angErr.length >= 2) {
					angErrType.setAngle2(constrainRadianAngle(angErr[1]));
				}
				bearing.setCoordinateError(angErrType);
			}

			return locType;
		}
		public LocalizationType createPolarLoc(PamDataUnit dataUnit) {
			AbstractLocalisation loc = dataUnit.getLocalisation();
			if (loc == null) {
				return null;
			}
			if (loc.hasLocContent(LocContents.HAS_RANGE) == false) {

				// do the more basic bearing type instead. 
				return createBearingLoc(dataUnit);
			}
			LocalizationType locType = makeBaseLoc(dataUnit);
			double[] angles = loc.getAngles();
			if (angles == null || angles.length == 0) {
				return null;
			}
			AngularCoordinateType angType = new AngularCoordinateType();
			angType.setAngle1(constrainRadianAngle(angles[0]));
			if (angles.length >= 2) {
				angType.setAngle2(constrainRadianAngle(angles[1]));
				if (angType.getAngle2() > 360) {
					angType.setAngle2(toSlantAngle(angles[1]));
				}
			}
			if (loc.hasLocContent(LocContents.HAS_RANGE)) {
				angType.setDistanceM(loc.getRange(0));
			}
			Angular angular = new Angular();
			angular.setCoordinate(angType);
			locType.setAngular(angular);

			double[] angErr = loc.getAngleErrors();
			if (angErr != null && angErr.length >= 1) {
				AngularCoordinateType angErrType = new AngularCoordinateType();
				angErrType.setAngle1(constrainRadianAngle(angErr[0]));
				if (angErr.length >= 2) {
					angErrType.setAngle2(constrainRadianAngle(angErr[1]));
				}
				if (loc.hasLocContent(LocContents.HAS_RANGEERROR)) {
					angErrType.setDistanceM(loc.getRangeError(0));
				}
				angular.setCoordinateError(angErrType);
			}

			return locType;
		}

		public LocalizationType createPerpRange(PamDataUnit dataUnit) {
			// TODO Auto-generated method stub
			return null;
		}

		public LocalizationType createCylindricalLoc(PamDataUnit dataUnit) {
			// TODO Auto-generated method stub
			return null;
		}

		public LocalizationType createCartesianLoc(PamDataUnit dataUnit) {
			// TODO Auto-generated method stub
			return null;
		}


	/**
	 * @return the deployment
	 */
	public PDeployment getDeployment() {
		return deployment;
	}

	/**
	 * @return the detectionsDocument
	 */
	public Detections getDetectionsDocument() {
		return detectionsDocument;
	}

	/**
	 * @return the dataBlock
	 */
	public PamDataBlock getDataBlock() {
		return dataBlock;
	}

	/**
	 * @return the exportParams
	 */
	public StreamExportParams getExportParams() {
		return streamExportParams;
	}

	/**
	 * @return the tethysControl
	 */
	public TethysControl getTethysControl() {
		return tethysControl;
	}

	/**
	 * @return the localisationHandler
	 */
	public LocalizationHandler getLocalisationHandler() {
		return localisationHandler;
	}

	/**
	 * @return the localisationAlgorithm
	 */
	public LocalisationAlgorithm getLocalisationAlgorithm() {
		return localisationAlgorithm;
	}

	/**
	 * @return the currentDocument
	 */
	public Localize getCurrentDocument() {
		return currentDocument;
	}

	public void closeDocument(long endTime) {
		currentDocument.getEffort().setEnd(TethysTimeFuncs.xmlGregCalFromMillis(endTime));
	}

	public boolean checkDocument() {
		// TODO Auto-generated method stub
		return true;
	}



}
