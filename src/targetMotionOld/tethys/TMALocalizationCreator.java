package targetMotionOld.tethys;

import java.awt.Window;
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
import nilus.Helper;
import nilus.LocalizationType;
import nilus.Localize;
import nilus.WGS84CoordinateType;
import nilus.LocalizationType.Parameters;
import nilus.LocalizationType.WGS84;
import nilus.LocalizationType.Parameters.TargetMotionAnalysis;
import nilus.Localize.Effort;
import nilus.Localize.Effort.CoordinateReferenceSystem;
import nilus.Localize.Effort.CoordinateReferenceSystem.ReferenceFrame;
import pamMaths.PamVector;
import targetMotionOld.TargetMotionLocaliser;
import targetMotionOld.TargetMotionOptions;
import tethys.TethysTimeFuncs;
import tethys.localization.Anchor;
import tethys.localization.CoordinateName;
import tethys.localization.LocalizationBuilder;
import tethys.localization.LocalizationCreator;
import tethys.localization.LocalizationSubTypes;
import tethys.localization.LocalizationTypes;
import tethys.localization.TimeReference;
import tethys.pamdata.AutoTethysProvider;
import tethys.swing.export.LocalizationOptionsPanel;

public class TMALocalizationCreator implements LocalizationCreator {


	int maxDimension = 2;
	private TargetMotionLocaliser targetMotionLocaliser;

	public TMALocalizationCreator(TargetMotionLocaliser targetMotionLocaliser) {
		this.targetMotionLocaliser = targetMotionLocaliser;
	}

	@Override
	public boolean sortLocalisationCoordinates(LocalizationBuilder localizationBuilder, PamDataBlock dataBlock) {


		Localize doc = localizationBuilder.getCurrentDocument();
		Effort locEffort = doc.getEffort();
		locEffort.setTimeReference(TimeReference.beam.toString());

		List<String> locTypes = locEffort.getLocalizationType();
		locTypes.add(LocalizationTypes.Point.toString());
		locTypes.add(LocalizationTypes.PerpendicularRange.toString());

		/**
		 * Set the coordinate type based on the options. 
		 */
		CoordinateName coordName = getCoordinateName();

		CoordinateReferenceSystem coordRefs = locEffort.getCoordinateReferenceSystem();
		coordRefs.setName(coordName.toString());
		coordRefs.setSubtype(coordName.getSubType().toString());

		ReferenceFrame refFrame = localizationBuilder.getDefaultReferenceFrame(coordName, coordName.getSubType());
		if (refFrame != null) {
			coordRefs.setReferenceFrame(refFrame);
		}

		locEffort.setDimension(2);	

		return true;
	}

	/**
	 * Get the coordinate type, setting to WGS84 if it's undefined. 
	 * @return Coordinate name
	 */
	private CoordinateName getCoordinateName() {
//		TargetMotionOptions tmaOptions = targetMotionLocaliser.getTargetMotionOptions();
//		if (tmaOptions.exportCoordinate == null) {
//			tmaOptions.exportCoordinate = CoordinateName.WGS84;
//		}
//		return tmaOptions.exportCoordinate;
		return CoordinateName.WGS84; // always this I think. I don't see a possibility of options in this one. 
	}

	@Override
	public LocalizationType createLocalization(LocalizationBuilder localizationBuilder, PamDataUnit dataUnit) {
		/*
		 * Add two types of localisation. A WGS84 and a perpendicular range. 
		 */
		CoordinateName coordName = getCoordinateName();
		AbstractLocalisation pamLoc = dataUnit.getLocalisation();
		if (pamLoc == null) {
			return null;
		}
		if (pamLoc instanceof GroupLocalisation == false) {
			return null;
		}
		LocalizationType loc = localizationBuilder.makeBaseLoc(dataUnit);
		boolean locOk = false;
		switch (coordName) {
		case WGS84:
			locOk = makeWGS84Localization(loc, pamLoc);
			break;
		case Cartesian:
			locOk = makeCartesianLocalization(loc, pamLoc);
			break;
		case Cylindrical:
			locOk = makeCylinderLocalization(loc, pamLoc);
			break;
		}
		if (locOk == false) {
			return null;
		}

		GroupLocalisation groupLoc = (GroupLocalisation) pamLoc;
		GroupLocResult groupLocResult = groupLoc.getGroupLocaResult(0);
		LocaliserModel tmaModel = groupLocResult.getModel();
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



		return loc;
	}

	private boolean makeCylinderLocalization(LocalizationType loc, AbstractLocalisation pamLoc) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean makeCartesianLocalization(LocalizationType loc, AbstractLocalisation pamLoc) {
		LatLong latLong = pamLoc.getLatLong(0);
		GroupLocalisation groupLoc = (GroupLocalisation) pamLoc;
		GroupLocResult groupLocResult = groupLoc.getGroupLocaResult(0);
		LocaliserModel tmaModel = groupLocResult.getModel();
		boolean hasDepth = false;
		
		return false;
	}

	private boolean makeWGS84Localization(LocalizationType loc, AbstractLocalisation pamLoc) {
		LatLong latLong = pamLoc.getLatLong(0);
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

		return true;
	}

	@Override
	public boolean checkDocument(LocalizationBuilder localizationBuilder) {
		boolean ok = localizationBuilder.checkDocument();
		Effort locEffort = localizationBuilder.getCurrentDocument().getEffort();
		locEffort.setDimension(maxDimension);
		return ok;
	}


}
