package group3dlocaliser.tethys;

import java.util.List;

import Localiser.algorithms.locErrors.LocaliserError;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamDetection.AbstractLocalisation;
import PamUtils.LatLong;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import group3dlocaliser.Group3DLocaliserControl;
import nilus.CartesianCoordinateType;
import nilus.LocalizationType;
import nilus.LocalizationType.Cartesian;
import nilus.LocalizationType.WGS84;
import nilus.Localize;
import nilus.WGS84CoordinateType;
import pamMaths.PamVector;
import nilus.Localize.Effort;
import nilus.Localize.Effort.CoordinateReferenceSystem;
import nilus.Localize.Effort.CoordinateReferenceSystem.ReferenceFrame;
import tethys.localization.CoordinateName;
import tethys.localization.LocalizationBuilder;
import tethys.localization.LocalizationCreator;
import tethys.localization.LocalizationSubTypes;
import tethys.localization.LocalizationTypes;
import tethys.localization.TethysLatLong;
import tethys.pamdata.AutoTethysProvider;

public class Group3DLocalizationCreator implements LocalizationCreator {

	private Group3DLocaliserControl group3DControl;
	private boolean hasDepth;
	public Group3DLocalizationCreator(Group3DLocaliserControl group3DControl) {
		this.group3DControl = group3DControl;
	}

	@Override
	public boolean sortLocalisationCoordinates(LocalizationBuilder localizationBuilder, PamDataBlock dataBlock) {
		Localize doc = localizationBuilder.getCurrentDocument();
		Effort locEffort = doc.getEffort();
		List<String> locTypes = locEffort.getLocalizationType();
		
		/**
		 * CoordinateName should be WGS84 or Cartesian. 
		 */
		CoordinateName coordinateName = group3DControl.getGroup3dParams().getExportCoordinateName();
		LocalizationSubTypes coordSubType = coordinateName.getSubType();
		// always a point whether it's cartesian or WGS84
		locTypes.add(LocalizationTypes.Point.toString());
		CoordinateReferenceSystem coordRefs = locEffort.getCoordinateReferenceSystem();
		coordRefs.setName(coordinateName.toString());
		coordRefs.setSubtype(coordinateName.getSubType().toString());
		
		ReferenceFrame refFrame = localizationBuilder.getDefaultReferenceFrame(coordinateName, coordinateName.getSubType());
		if (refFrame != null) {
			coordRefs.setReferenceFrame(refFrame);
		}
		locEffort.setDimension(2);// may get updated at end if hasDepth becomes true. 
		hasDepth = false;
		
		return true;
	}

	@Override
	public LocalizationType createLocalization(LocalizationBuilder localizationBuilder, PamDataUnit dataUnit) {
		CoordinateName coordinateName = group3DControl.getGroup3dParams().getExportCoordinateName();
		LocalizationType locType = localizationBuilder.makeBaseLoc(dataUnit);
		
		switch (coordinateName) {
		case Cartesian:
			Cartesian cartesianValue = createCartesianValue(localizationBuilder, dataUnit);
			if (cartesianValue == null) {
				return null;
			}
			locType.setCartesian(cartesianValue);
			break;
		case WGS84:
			WGS84 wgs84 = createWGS84Localisation(localizationBuilder, dataUnit);
			if (wgs84 == null) {
				return null;
			}
			locType.setWGS84(wgs84);
			break;
		}
		return locType;
	}

	private WGS84 createWGS84Localisation(LocalizationBuilder localizationBuilder, PamDataUnit dataUnit) {
		WGS84 wgs84 = new WGS84();
		AbstractLocalisation loc = dataUnit.getLocalisation();
		if (loc instanceof GroupLocalisation == false) {
			return null;
		}
		GroupLocalisation groupLoc = (GroupLocalisation) loc;
		LatLong ll = loc.getLatLong(0);
		if (ll == null) {
			return null;
		}
		WGS84CoordinateType wgsCoordinate = new WGS84CoordinateType();
		wgsCoordinate.setLatitude(TethysLatLong.formatLatitude(ll.getLatitude(), TethysLatLong.mmDecimalPlaces));
		wgsCoordinate.setLongitude(TethysLatLong.formatLongitude(ll.getLongitude(), TethysLatLong.mmDecimalPlaces));
		if (ll.getNumCoordinates() == 3) {
			hasDepth = true;
			wgsCoordinate.setElevationM(AutoTethysProvider.roundDecimalPlaces(ll.getHeight(), 3));
		}
		wgs84.setCoordinate(wgsCoordinate);
		return wgs84;
	}

	private Cartesian createCartesianValue(LocalizationBuilder localizationBuilder,
			PamDataUnit dataUnit) {
		Cartesian cart = new Cartesian();
		CartesianCoordinateType cartCoordinate = new CartesianCoordinateType();
		AbstractLocalisation loc = dataUnit.getLocalisation();
		if (loc instanceof GroupLocalisation == false) {
			return null;
		}
		GroupLocalisation groupLoc = (GroupLocalisation) loc;
		LatLong ll = loc.getLatLong(0);
		if (ll == null) {
			return null;
		}

		LatLong refLatLong = dataUnit.getOriginLatLong(false);
		if (refLatLong == null) {
			return null;
		}
		cartCoordinate.setXM(AutoTethysProvider.roundDecimalPlaces(refLatLong.distanceToMetresX(ll),3));
		cartCoordinate.setYM(AutoTethysProvider.roundDecimalPlaces(refLatLong.distanceToMetresY(ll),3));
		if (ll.getNumCoordinates() == 3) {
			cartCoordinate.setZM(AutoTethysProvider.roundDecimalPlaces(ll.getHeight()-refLatLong.getHeight(),3));
		}

		LocaliserError locErr = loc.getLocError(0);
		
		cart.setCoordinate(cartCoordinate);
		
		
		return cart;
	}

	@Override
	public boolean checkDocument(LocalizationBuilder localizationBuilder) {
		boolean ok = localizationBuilder.checkDocument();
		Effort locEffort = localizationBuilder.getCurrentDocument().getEffort();
		locEffort.setDimension(hasDepth ? 3 : 2);
		return ok;
	}

}
