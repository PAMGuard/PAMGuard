package targetMotionOld.tethys;

import java.util.List;

import Localiser.LocaliserModel;
import Localiser.algorithms.locErrors.EllipticalError;
import Localiser.algorithms.locErrors.LocaliserError;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.LatLong;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import nilus.AngularCoordinateType;
import nilus.LocalizationType;
import nilus.Localize;
import nilus.WGS84CoordinateType;
import nilus.LocalizationType.Angular;
import nilus.LocalizationType.Parameters;
import nilus.LocalizationType.WGS84;
import nilus.LocalizationType.Parameters.TargetMotionAnalysis;
import nilus.Localize.Effort;
import nilus.Localize.Effort.CoordinateReferenceSystem;
import pamMaths.PamVector;
import targetMotionOld.TargetMotionLocaliser;
import tethys.TethysTimeFuncs;
import tethys.localization.CoordinateName;
import tethys.localization.LocalizationBuilder;
import tethys.localization.LocalizationCreator;
import tethys.localization.LocalizationSubTypes;
import tethys.localization.LocalizationTypes;
import tethys.localization.ReferenceFrame;
import tethys.localization.TimeReference;
import tethys.pamdata.AutoTethysProvider;

public class TMALocalizationCreator implements LocalizationCreator {


	int maxDimension = 2;
	
	public TMALocalizationCreator(TargetMotionLocaliser targetMotionLocaliser) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean sortLocalisationCoordinates(LocalizationBuilder localizationBuilder, PamDataBlock dataBlock) {
		Localize doc = localizationBuilder.getCurrentDocument();
		Effort locEffort = doc.getEffort();
		locEffort.setTimeReference(TimeReference.beam.toString());

		List<String> locTypes = locEffort.getLocalizationType();
		locTypes.add(LocalizationTypes.Point.toString());
		locTypes.add(LocalizationTypes.PerpendicularRange.toString());

		CoordinateReferenceSystem coordRefs = locEffort.getCoordinateReferenceSystem();
		coordRefs.setName(CoordinateName.WGS84.toString());
		coordRefs.setSubtype(LocalizationSubTypes.Geographic.toString());
		
		locEffort.setDimension(2);		
		
		return true;
	}

	@Override
	public LocalizationType createLocalization(LocalizationBuilder localizationBuilder, PamDataUnit dataUnit) {
		/*
		 * Add two types of localisation. A WGS84 and a perpendicular range. 
		 */
		AbstractLocalisation pamLoc = dataUnit.getLocalisation();
		LatLong latLong = pamLoc.getLatLong(0);
		if (pamLoc instanceof GroupLocalisation == false || latLong == null) {
			return localizationBuilder.createStandardLocalization(dataUnit);
		}
		LocalizationType loc = localizationBuilder.makeBaseLoc(dataUnit);
		GroupLocalisation groupLoc = (GroupLocalisation) pamLoc;
		GroupLocResult groupLocResult = groupLoc.getGroupLocaResult(0);
		LocaliserModel tmaModel = groupLocResult.getModel();
		boolean hasDepth = false;
		LocContents locCont = new LocContents(LocContents.HAS_LATLONG);
		if (tmaModel != null) {
			locCont = tmaModel.getLocContents();
			if (locCont.hasLocContent(LocContents.HAS_DEPTH)) {
				hasDepth = true;
			}
		}
		int nDim = hasDepth ? 3 : 2;
		maxDimension = Math.max(maxDimension, nDim);
		/**
		 * Export the latlong data.
		 */
		WGS84 wgs84 = new WGS84();
		WGS84CoordinateType coord = new WGS84CoordinateType();
		wgs84.setCoordinate(coord);
		coord.setLongitude(latLong.getLongitude());
		coord.setLatitude(latLong.getLatitude());
		if (hasDepth) {
			coord.setElevationM(AutoTethysProvider.roundDecimalPlaces(latLong.getHeight(),3));
		}
		PamVector errors = groupLocResult.getErrorVector();
		LocaliserError genError = groupLocResult.getLocError();
		WGS84CoordinateType wgsErr = null; 
		double[] errorVec = null;
		if (errors != null) {
			errorVec = errors.getVector();
		}
		else if (genError instanceof EllipticalError) {
			EllipticalError elliptical = (EllipticalError) genError;
			PamVector dir = genError.getErrorDirection();
			// these are errors perpendicular and parallel to the track
			// so aren't really lat long errors. 
			errorVec = elliptical.getEllipseDim();
		}
		/*
		 * Needs a bit of work to get errors in correct direction (needs import
		 * of track data for this value ?) and conversion to latlong units.  
		 */
//		if (errorVec != null && errorVec.length >= 2) {
//			wgsErr = new WGS84CoordinateType();
//			wgsErr.setLongitude(errorVec[0]);
//			wgsErr.setLatitude(errorVec[1]);
//			if (hasDepth && errorVec.length >= 3) {
//				wgsErr.setElevationM(errorVec[2]);
//			}
//			wgs84.setCoordinateError(wgsErr);
//		}
		
		
		loc.setWGS84(wgs84);
		
		// set the TMA information
		Parameters params = loc.getParameters();
		if (params == null) {
			params = new Parameters();
			loc.setParameters(params);
		}
		TargetMotionAnalysis tma = new TargetMotionAnalysis();
		tma.setStart(TethysTimeFuncs.xmlGregCalFromMillis(dataUnit.getTimeMilliseconds()));
		tma.setEnd(TethysTimeFuncs.xmlGregCalFromMillis(dataUnit.getEndTimeInMilliseconds()));
		params.setTargetMotionAnalysis(tma);
		Long timeAbeam = groupLocResult.getBeamTime();
		if (timeAbeam != null) {
			loc.setTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(timeAbeam));
		}
				
//		 now also output a perpendicular distance.
		Double perp = groupLocResult.getPerpendicularDistance();
		if (perp != null) {
			loc.setPerpendicularRangeM(AutoTethysProvider.roundDecimalPlaces(perp, 2));
		}
		
		
		// con only output one type. 
//		if (perp != null) {
//			AngularCoordinateType acType = new AngularCoordinateType();
//			acType.setAngle1(90);
//			acType.setDistanceM(AutoTethysProvider.roundDecimalPlaces(perp,1));
//			Angular angular = new Angular();
//			angular.setCoordinate(acType);
//			if (errors != null) {
//				AngularCoordinateType angErr = new AngularCoordinateType();
//				angErr.setDistanceM(errors.norm());
//				angular.setCoordinateError(angErr);
//			}
//			loc.setAngular(angular);
//		}

		
		
		return loc;
	}

	@Override
	public boolean checkDocument(LocalizationBuilder localizationBuilder) {
		boolean ok = localizationBuilder.checkDocument();
		Effort locEffort = localizationBuilder.getCurrentDocument().getEffort();
		locEffort.setDimension(maxDimension);
		return ok;
	}

}
