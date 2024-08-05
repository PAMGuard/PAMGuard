package tethys.localization;

import java.math.BigInteger;
import java.util.ArrayList;

import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.LatLong;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import nilus.AngularCoordinateType;
import nilus.BearingType;
import nilus.CoordinateType;
import nilus.Helper;
import nilus.LocalizationType;
import nilus.LocalizationType.References;
import nilus.LocalizationType.References.Reference;
import nilus.Localize;
import nilus.SpeciesIDType;
import nilus.Localize.Effort.CoordinateReferenceSystem;
import tethys.Collection;
import tethys.CollectionHandler;
import tethys.TethysControl;
import tethys.TethysTimeFuncs;
import tethys.detection.StreamDetectionsSummary;
import tethys.niluswraps.NilusDataWrapper;
import tethys.niluswraps.PDeployment;
import tethys.output.StreamExportParams;
import tethys.pamdata.AutoTethysProvider;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.SpeciesMapItem;

public class LocalizationHandler extends CollectionHandler {

	public LocalizationHandler(TethysControl tethysControl) {
		super(tethysControl, Collection.Localizations);
	}

//	public LocalizationType getLoc() {
//		LocalizationType lt = new LocalizationType();
//		CylindricalCoordinateType cct = new CylindricalCoordinateType();
////		cct.set
//		CoordinateReferenceSystem cr;
//		return null;
//	}

	/**
	 * Get a list of Localization documents associated with a particular data block for all deployments
	 * documents. Group them by abstract or something
	 * @param dataBlock
	 * @return
	 */
	public StreamDetectionsSummary<NilusDataWrapper<PLocalization>> getStreamLocalizations(PamDataBlock dataBlock) {
		ArrayList<PDeployment> deployments = tethysControl.getDeploymentHandler().getMatchedDeployments();
		return getStreamLocalizations(dataBlock, deployments);
	}
	
	/**
	 * Get a list of Localization documents associated with a particular data block for the list of deployments
	 * documents. Group them by abstract or something
	 * @param dataBlock
	 * @param deployments can be null for all deployments. 
	 * @return
	 */
	public StreamDetectionsSummary<NilusDataWrapper<PLocalization>> getStreamLocalizations(PamDataBlock dataBlock, ArrayList<PDeployment> deployments) {
		// get the basic data for each document including it's Description.

		ArrayList<PLocalization> localizeDocs = new ArrayList<>();
		for (PDeployment aDep : deployments) {
			ArrayList<String> someNames = tethysControl.getDbxmlQueries().getLocalizationDocuments(dataBlock, aDep.getDocumentId());
			if (someNames == null) {
				continue;
			}
//			// no have a list of all the Detections documents of interest for this datablock.
			for (String aDoc : someNames) {
				Localize localize = tethysControl.getDbxmlQueries().getLocalizationDocInfo(aDoc);
				int count = tethysControl.getDbxmlQueries().countLocalizations2(aDoc);
				PLocalization pLocalize = new PLocalization(localize, dataBlock, aDep, count);
				localizeDocs.add(pLocalize);
//				PDetections pDetections = new PDetections(detections, dataBlock, aDep, count);
//				detectionsDocs.add(pDetections);
			}
		}
		return new StreamDetectionsSummary(localizeDocs);
	}

	@Override
	public String getHelpPoint() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Create a Localization element object to add to a Localizations document. 
	 * @param localiseDocument
	 * @param dataBlock 
	 * @param dataUnit
	 * @param streamExportParams
	 * @return
	 */
	public LocalizationType createLocalization(Localize localiseDocument, PamDataBlock dataBlock, PamDataUnit dataUnit,
			StreamExportParams streamExportParams) {
		/*
		 * Get the current type from the document. This should always exist, so
		 * just go for it, then switch on the value to make the localisation. 
		 */
		CoordinateReferenceSystem coordSystem = localiseDocument.getEffort().getCoordinateReferenceSystem();
		String name = coordSystem.getName();
		CoordinateName coordName = CoordinateName.valueOf(name);
		if (coordName == null) {
			return null;
		}
		LocalizationType locEl = null;
		switch (coordName) {
		case Cartesian:
			locEl = createCartesianLoc(localiseDocument, dataBlock, dataUnit, streamExportParams);
			break;
		case Cylindrical:
			locEl = createCylindricalLoc(localiseDocument, dataBlock, dataUnit, streamExportParams);
			break;
		case PerpindicularRange:
			locEl = createPerpRange(localiseDocument, dataBlock, dataUnit, streamExportParams);
			break;
		case Polar:
			locEl = createPolarLoc(localiseDocument, dataBlock, dataUnit, streamExportParams);
			break;
		case Range:
			locEl = createRangeLoc(localiseDocument, dataBlock, dataUnit, streamExportParams);
			break;
		case Spherical:
			locEl = createSphericalLoc(localiseDocument, dataBlock, dataUnit, streamExportParams);
			break;
		case UTM:
			break;
		case WGS84:
			locEl = createWGS84Loc(localiseDocument, dataBlock, dataUnit, streamExportParams);
			break;
		default:
			break;
		
		}
		return locEl;
	}

	private LocalizationType makeBaseLoc(PamDataBlock dataBlock, PamDataUnit dataUnit) {
		LocalizationType locType = new LocalizationType();
		try {
			Helper.createRequiredElements(locType);
		} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
		}
		locType.setTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(dataUnit.getTimeMilliseconds()));
		
		DataBlockSpeciesManager spManager = dataBlock.getDatablockSpeciesManager();
		if (spManager != null) {
			SpeciesMapItem speciesStuff = spManager.getSpeciesItem(dataUnit);
			SpeciesIDType species = new SpeciesIDType();
			if (speciesStuff != null) {
				species.setValue(BigInteger.valueOf(speciesStuff.getItisCode()));
				locType.setSpeciesId(species);
			}
		}
		
		References references = locType.getReferences();
		if (references == null) {
			references = new References();
			try {
				Helper.createRequiredElements(references);
			} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
				e.printStackTrace();
			}
			locType.setReferences(references);
		}
		Reference reference = new Reference();
		reference.setIndex(BigInteger.valueOf(dataUnit.getUID()));
		reference.setEventRef("UID");
		locType.getReferences().getReference().add(reference);
		
		return locType;
	}

	/**
	 * Get angle in degrees constrained to 0-360
	 * @param radians
	 * @return
	 */
	private double constrainRadianAngle(double radians) {
		double deg = Math.toDegrees(radians);
		if (Math.abs(deg) > 3600) {
			return 359.9;
		}
		deg = PamUtils.constrainedAngle(deg);
		deg = AutoTethysProvider.roundDecimalPlaces(deg, 2);
		return deg;
	}

	private LocalizationType createWGS84Loc(Localize localiseDocument, PamDataBlock dataBlock, PamDataUnit dataUnit,
			StreamExportParams streamExportParams) {
		AbstractLocalisation loc = dataUnit.getLocalisation();
		if (loc == null) {
			return null;
		}
		LatLong latlong = loc.getLatLong(0);
		if (latlong == null) {
			return null;
		}
		LocalizationType locType = makeBaseLoc(dataBlock, dataUnit);
		CoordinateType coord = new CoordinateType();
		coord.setX(latlong.getLongitude());
		coord.setY(latlong.getLatitude());
		coord.setZ(latlong.getHeight());
				
		locType.setCoordinate(coord);
		
		return locType;
	}

	private LocalizationType createSphericalLoc(Localize localiseDocument, PamDataBlock dataBlock, PamDataUnit dataUnit,
			StreamExportParams streamExportParams) {
		// TODO Auto-generated method stub
		return null;
	}

	private LocalizationType createRangeLoc(Localize localiseDocument, PamDataBlock dataBlock, PamDataUnit dataUnit,
			StreamExportParams streamExportParams) {
		// TODO Auto-generated method stub
		return null;
	}

	private LocalizationType createBearingLoc(Localize localiseDocument, PamDataBlock dataBlock, PamDataUnit dataUnit,
			StreamExportParams streamExportParams) {
		AbstractLocalisation loc = dataUnit.getLocalisation();
		if (loc == null) {
			return null;
		}
		LocalizationType locType = makeBaseLoc(dataBlock, dataUnit);
		double[] angles = loc.getAngles();
		if (angles == null || angles.length == 0) {
			return null;
		}
		BearingType angType = new BearingType();
		angType.setAngle1(constrainRadianAngle(angles[0]));
		if (angles.length >= 2) {
			angType.setAngle2(constrainRadianAngle(angles[1]));
		}
		locType.setBearing(angType);

		double[] angErr = loc.getAngleErrors();
		if (angErr != null && angErr.length >= 1) {
			BearingType angErrType = new BearingType();
			angErrType.setAngle1(constrainRadianAngle(angErr[0]));
			if (angErr.length >= 2) {
				angErrType.setAngle2(constrainRadianAngle(angErr[1]));
			}
			locType.setBearingError(angErrType);
		}
		
		return locType;
	}
	private LocalizationType createPolarLoc(Localize localiseDocument, PamDataBlock dataBlock, PamDataUnit dataUnit,
			StreamExportParams streamExportParams) {
		AbstractLocalisation loc = dataUnit.getLocalisation();
		if (loc == null) {
			return null;
		}
		if (loc.hasLocContent(LocContents.HAS_RANGE) == false) {
			
			// do the more basic bearing type instead. 
			return createBearingLoc(localiseDocument, dataBlock, dataUnit, streamExportParams);
		}
		LocalizationType locType = makeBaseLoc(dataBlock, dataUnit);
		double[] angles = loc.getAngles();
		if (angles == null || angles.length == 0) {
			return null;
		}
		AngularCoordinateType angType = new AngularCoordinateType();
		angType.setAngle1(constrainRadianAngle(angles[0]));
		if (angles.length >= 2) {
			angType.setAngle2(constrainRadianAngle(angles[1]));
		}
		if (loc.hasLocContent(LocContents.HAS_RANGE)) {
			angType.setDistanceM(loc.getRange(0));
		}
		locType.setAngularCoordinate(angType);

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
			locType.setAngularCoordinateError(angErrType);
		}
		
		return locType;
	}
	
	private LocalizationType createPerpRange(Localize localiseDocument, PamDataBlock dataBlock, PamDataUnit dataUnit,
			StreamExportParams streamExportParams) {
		// TODO Auto-generated method stub
		return null;
	}

	private LocalizationType createCylindricalLoc(Localize localiseDocument, PamDataBlock dataBlock,
			PamDataUnit dataUnit, StreamExportParams streamExportParams) {
		// TODO Auto-generated method stub
		return null;
	}

	private LocalizationType createCartesianLoc(Localize localiseDocument, PamDataBlock dataBlock, PamDataUnit dataUnit,
			StreamExportParams streamExportParams) {
		// TODO Auto-generated method stub
		return null;
	}

}
