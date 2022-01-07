package targetMotionOld;

import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;

import Localiser.LocaliserModel;
import Localiser.algorithms.locErrors.LocaliserError;
import Localiser.algorithms.locErrors.SimpleError;
import Localiser.algorithms.locErrors.json.LocaliserErrorFactory;
import Localiser.algorithms.locErrors.json.ErrorJsonConverter;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLoggingAddon;
import generalDatabase.SQLTypes;

/**
 * Reinstated Target motion add-in as used by the click detector. Hope one day still to replace this
 * with Jamie's new one, but keep this one until Jamie's is working. 
 * @author Doug Gillespie
 *
 */
public class TargetMotionSQLLogging implements SQLLoggingAddon {
	
	private TargetMotionLocaliser targetMotionLocaliser;

	/**
	 * Number of target motion restults to store. 
	 */
	private int numResults;

	private PamTableItem modelName, latitude[], longitude[], side[], chi2[], aic[], prob[], degsFreedom[], 
	perpDist[], perpdistErr[], phones[], comment[], beamLatitude[], beamLongitude[], 
	beamTime[], depth[], depthErr[], locError[];

	private HashSet<PamTableItem[]> hiddenItems = new HashSet<>();
	
	/**
	 * 
	 * @param numResults Number of results to display
	 * @param initials initials for prefix of column names. 
	 */
	public TargetMotionSQLLogging(int numResults) {
		this(numResults, "TM");
	}
	
	/**
	 * 
	 * @param numResults Number of results to display
	 * @param initials initials for prefix of column names. 
	 */
	public TargetMotionSQLLogging(int numResults, String initials) {
		super();
		this.numResults = numResults;

		latitude = new PamTableItem[numResults];
		longitude = new PamTableItem[numResults];
		side = new PamTableItem[numResults];
		chi2 = new PamTableItem[numResults];
		aic = new PamTableItem[numResults];
		prob = new PamTableItem[numResults];
		degsFreedom = new PamTableItem[numResults];
		perpDist = new PamTableItem[numResults];
		perpdistErr = new PamTableItem[numResults];
		phones = new PamTableItem[numResults];
		comment = new PamTableItem[numResults];
		beamLatitude = new PamTableItem[numResults];
		beamLongitude = new PamTableItem[numResults];
		beamTime = new PamTableItem[numResults];
		depth = new PamTableItem[numResults];
		depthErr = new PamTableItem[numResults];
		locError = new PamTableItem[numResults];
		
		modelName = new PamTableItem(initials + "ModelName"+"1", Types.CHAR, 30);
		for (int i = 0; i < numResults; i++) {
			String colSuffix = getColSuffix(i);
			latitude[i] = new PamTableItem(initials + "Latitude"+colSuffix, Types.DOUBLE);
			longitude[i] = new PamTableItem(initials + "Longitude"+colSuffix, Types.DOUBLE);
			beamLatitude[i] = new PamTableItem("BeamLatitude"+colSuffix, Types.DOUBLE);
			beamLongitude[i] = new PamTableItem("BeamLongitude"+colSuffix, Types.DOUBLE);
			beamTime[i] = new PamTableItem("BeamTime"+colSuffix, Types.TIMESTAMP);
			side[i] = new PamTableItem(initials + "Side"+colSuffix, Types.INTEGER);
			chi2[i] = new PamTableItem(initials + "Chi2"+colSuffix, Types.DOUBLE);
			aic[i] = new PamTableItem(initials + "AIC"+colSuffix, Types.DOUBLE);
			prob[i] = new PamTableItem(initials + "Probability"+colSuffix, Types.DOUBLE);
			degsFreedom[i] = new PamTableItem(initials + "DegsFreedom"+colSuffix, Types.INTEGER);
			perpDist[i] = new PamTableItem(initials + "PerpendicularDistance"+colSuffix, Types.DOUBLE);
			perpdistErr[i] = new PamTableItem(initials + "PerpendicularDistanceError"+colSuffix, Types.DOUBLE);
			depth[i] = new PamTableItem(initials + "Depth"+colSuffix, Types.DOUBLE);
			depthErr[i] = new PamTableItem(initials + "DepthError"+colSuffix, Types.DOUBLE);
			phones[i] = new PamTableItem(initials + "Hydrophones"+colSuffix, Types.INTEGER);
			locError[i] = new PamTableItem(initials + "Error"+colSuffix, Types.CHAR, LOCERRORLENGTH);
			comment[i] = new PamTableItem(initials + "Comment"+colSuffix, Types.CHAR, 80);
		}
	}

	private static final int LOCERRORLENGTH = 128;

	public void hideColumns(PamTableItem[] tableItems) {
		hiddenItems.add(tableItems);
	}
	
	public boolean isHidden(PamTableItem[] tableItems) {
		return hiddenItems.contains(tableItems);
	}
	
	public void unHideColumns(PamTableItem[] tableItems) {
		hiddenItems.remove(tableItems);
	}
	
	private String getColSuffix(int resultIndex) {
		return new Integer(resultIndex+1).toString();
	}
	
	@Override
	public void addTableItems(PamTableDefinition pamTableDefinition) {

	
		pamTableDefinition.addTableItem(modelName);
		for (int i = 0; i < numResults; i++) {
			String colSuffix = getColSuffix(i);
			pamTableDefinition.addTableItem(latitude[i]);
			pamTableDefinition.addTableItem(longitude[i]);
			pamTableDefinition.addTableItem(beamLatitude[i]);
			pamTableDefinition.addTableItem(beamLongitude[i]);
			pamTableDefinition.addTableItem(beamTime[i]);
			pamTableDefinition.addTableItem(side[i]);
			pamTableDefinition.addTableItem(chi2[i]);
			pamTableDefinition.addTableItem(aic[i]);
			pamTableDefinition.addTableItem(prob[i]);
			pamTableDefinition.addTableItem(degsFreedom[i]);
			pamTableDefinition.addTableItem(perpDist[i]);
			pamTableDefinition.addTableItem(perpdistErr[i]);
			pamTableDefinition.addTableItem(depth[i]);
			pamTableDefinition.addTableItem(depthErr[i]);
			pamTableDefinition.addTableItem(phones[i]);
			pamTableDefinition.addTableItem(locError[i]);
			pamTableDefinition.addTableItem(comment[i]);
		}
	}

	@Override
	public boolean saveData(SQLTypes sqlTypes, PamTableDefinition pamTableDefinition,
			PamDataUnit pamDataUnit) {
		AbstractLocalisation tmResult = pamDataUnit.getLocalisation();
		clearEverything();
		if (tmResult == null) {
			return false;
		}
		else if (GroupLocalisation.class.isAssignableFrom(tmResult.getClass())) {
			return saveGroupLocalisation(sqlTypes, pamTableDefinition, pamDataUnit, (GroupLocalisation) tmResult);
		}
//		else try {
//			TargetMotionLocalisation tml = (TargetMotionLocalisation) tmResult;
//			GroupLocResult tmr = tml.getTargetMotionResult(0);
//			if (tmr == null) {
//				clearEverything();
//				return false;
//			}
//			LocaliserModel model = tmr.getModel();
//			if (model == null) {
//				modelName.setValue(null);
//			}
//			else {
//				modelName.setValue(tmr.getModel().getName());
//			}
//			LatLong ll = tmr.getLatLong();
//			if (ll != null) {
//				latitude.setValue(ll.getLatitude());
//				longitude.setValue(ll.getLongitude());
//				depth.setValue(-ll.getHeight());
//				Double depthError = tmr.getZError();
//				if (Double.isNaN(depthError)) {
//					depthErr.setValue(null);
//				}
//				else {
//					depthErr.setValue(depthError);
//				}
//			}
//			else {
//				longitude.setValue(null);
//				latitude.setValue(null);
//			}
//			ll = tmr.getBeamLatLong();
//			if (ll != null) {
//				beamLatitude.setValue(ll.getLatitude());
//				beamLongitude.setValue(ll.getLongitude());
//			}
//			else {
//				beamLatitude.setValue(null);
//				beamLongitude.setValue(null);
//			}
//			if (tmr.getBeamTime() != null) {
//				beamTime.setValue(sqlTypes.getTimeStamp(tmr.getBeamTime()));
//			}
//			else {
//				beamTime.setValue(null);
//			}
//			side.setValue(tmr.getSide());
//			chi2.setValue(tmr.getChi2());
//			aic.setValue(tmr.getAic());
//			prob.setValue(tmr.getProbability());
//			degsFreedom.setValue(tmr.getnDegreesFreedom());
//			perpDist.setValue(tmr.getPerpendicularDistance());
//			perpdistErr.setValue(tmr.getPerpendicularDistanceError());
//			phones.setValue(tmr.getReferenceHydrophones());
//			comment.setValue(tmr.getComment());
//		}
//		catch (ClassCastException e) {
//			System.out.println("Localisation is not from target motion analysis in event " + pamDataUnit.getDatabaseIndex());
//		}
		return true;
	}

	private boolean saveGroupLocalisation(SQLTypes sqlTypes, PamTableDefinition pamTableDefinition,
			PamDataUnit pamDataUnit, GroupLocalisation groupLocalisation) {
		
		int nAmbiguities = groupLocalisation.getAmbiguityCount();
		// need to sort the results if there is > 1 so that the one with the
		// lowers AIC or Chi2 comes first. 
		groupLocalisation.sortLocResults();
		
		for (int i = 0; i < numResults; i++) {
			if (i >= nAmbiguities) {
				clearResult(i);
			}
			else {
				GroupLocResult tmResult = groupLocalisation.getGroupLocaResult(i);
				saveGroupLocalisation(sqlTypes, pamTableDefinition, pamDataUnit, i, tmResult);
			}
		}
		
		return true;
	}

	private boolean saveGroupLocalisation(SQLTypes sqlTypes, PamTableDefinition pamTableDefinition,
			PamDataUnit pamDataUnit, int resultIndex, GroupLocResult tmResult) {
		
		
//		System.out.printf("Saving TMR ambiguity %d of %d for event %d\n", resultIndex, numResults, pamDataUnit.getDatabaseIndex());
		
		LocaliserModel model = tmResult.getModel();
		if (model == null) {
			modelName.setValue(null);
		}
		else {
			modelName.setValue(model.getName());
		}
		LatLong latLong = tmResult.getLatLong();
		if (latLong == null) {
			latitude[resultIndex].setValue(null);
			longitude[resultIndex].setValue(null);
			depth[resultIndex].setValue(null);
		}
		else {
			latitude[resultIndex].setValue(latLong.getLatitude());
			longitude[resultIndex].setValue(latLong.getLongitude());
			depth[resultIndex].setValue(-latLong.getHeight());
		}
		latLong = tmResult.getBeamLatLong();
		if (latLong == null) {
			beamLatitude[resultIndex].setValue(null);
			beamLongitude[resultIndex].setValue(null);
		}
		else {
			beamLatitude[resultIndex].setValue(latLong.getLatitude());
			beamLongitude[resultIndex].setValue(latLong.getLongitude());
		}
		if (tmResult.getBeamTime()!=null){
			beamTime[resultIndex].setValue(tmResult.getBeamTime());
		}
		else {
			beamTime[resultIndex].setValue(null);
		}
		side[resultIndex].setValue(resultIndex);
		chi2[resultIndex].setValue(tmResult.getChi2());
		aic[resultIndex].setValue(tmResult.getAic());
		prob[resultIndex].setValue(tmResult.getProbability());
		degsFreedom[resultIndex].setValue(tmResult.getnDegreesFreedom());
		perpDist[resultIndex].setValue(tmResult.getPerpendicularDistance());
		perpdistErr[resultIndex].setValue(tmResult.getPerpendicularDistanceError());
		phones[resultIndex].setValue(tmResult.getReferenceHydrophones());
		comment[resultIndex].setValue(tmResult.getComment());
		if (tmResult.getLocError() != null) {
			locError[resultIndex].setValue(tmResult.getLocError().getJsonErrorString());
		}
			
		return true;
	}

	private void clearEverything() {
		modelName.setValue(null); 
		for (int i = 0; i < numResults; i++) {
			clearResult(i);
		}
	}
	private void clearResult(int resultIndex){
		latitude[resultIndex].setValue(null);
		longitude[resultIndex].setValue(null);
		beamLatitude[resultIndex].setValue(null);
		beamLongitude[resultIndex].setValue(null);
		side[resultIndex].setValue(null);
		depth[resultIndex].setValue(null);
		depthErr[resultIndex].setValue(null);
		chi2[resultIndex].setValue(null);
		aic[resultIndex].setValue(null);
		prob[resultIndex].setValue(null);
		degsFreedom[resultIndex].setValue(null);
		perpDist[resultIndex].setValue(null);
		perpdistErr[resultIndex].setValue(null);
		phones[resultIndex].setValue(null);
		comment[resultIndex].setValue(null);
		beamLatitude[resultIndex].setValue(null);
		beamLongitude[resultIndex].setValue(null);
		beamTime[resultIndex].setValue(null);
		locError[resultIndex].setValue(null);
	}

	@Override
	public boolean loadData(SQLTypes sqlTypes, PamTableDefinition pamTableDefinition,
			PamDataUnit pamDataUnit) {
		
		GroupLocalisation tml = new GroupLocalisation(pamDataUnit, null);
		int goodResults = 0;
		for (int i = 0; i < numResults; i++) {
			GroupLocResult tmr = loadLocResult(sqlTypes, pamTableDefinition, pamDataUnit, tml, i);
			if (tmr != null) {
				tml.addGroupLocaResult(tmr);
				goodResults++;
			}
		}
		if (goodResults > 0) {
			pamDataUnit.setLocalisation(tml);
		}
		return true;
	}

		private GroupLocResult loadLocResult(SQLTypes sqlTypes, PamTableDefinition pamTableDefinition,
				PamDataUnit pamDataUnit, GroupLocalisation tml, int resultIndex) {
			
		double latVal, longVal;
		Double dVal;
		if (latitude[resultIndex].getValue() == null) {
			return null;
		}
		latVal = latitude[resultIndex].getDoubleValue();
		longVal = longitude[resultIndex].getDoubleValue();
		LatLong ll = new LatLong(latVal, longVal);
		tml.addLocContents(LocContents.HAS_LATLONG);
		tml.addLocContents(LocContents.HAS_BEARING);
		tml.addLocContents(LocContents.HAS_RANGE);
		Double dep = depth[resultIndex].getDoubleValue();
		if (dep != null) {
			ll.setHeight(-dep);
			tml.addLocContents(LocContents.HAS_DEPTH);
		}
		
		
		latVal = beamLatitude[resultIndex].getDoubleValue();
		longVal = beamLongitude[resultIndex].getDoubleValue();
		LatLong bLL = new LatLong(latVal, longVal);
		
		
		Object ts = beamTime[resultIndex].getValue();
		
		int sideVal = side[resultIndex].getIntegerValue();
		double chiVal = chi2[resultIndex].getDoubleValue();
	
		GroupLocResult tmr = new GroupLocResult(ll, sideVal, chiVal);
		tmr.setBeamLatLong(bLL);
		if (ts != null) {
			tmr.setBeamTime(sqlTypes.millisFromTimeStamp(ts));
		}
		tmr.setChi2((Double) chi2[resultIndex].getValue());
		tmr.setAic((Double) aic[resultIndex].getValue());
		tmr.setProbability((Double) prob[resultIndex].getValue());
		tmr.setnDegreesFreedom((Integer) degsFreedom[resultIndex].getValue());
		tmr.setPerpendicularDistance((Double) perpDist[resultIndex].getValue());
		tmr.setError(getXMLError()); //TODO
		tmr.setReferenceHydrophones((Integer) phones[resultIndex].getValue());
		tmr.setComment(comment[resultIndex].getDeblankedStringValue());
		String mName = modelName.getStringValue();
//		tmr.setFirstHeading(firstHeading);
		
		Double perpDistError = perpdistErr[resultIndex].getDoubleValue();
		
		
		//TODO - need to tidy this up in viewer mode. 
		if (mName != null && targetMotionLocaliser != null) {
			TargetMotionModel model = targetMotionLocaliser.findModelByName(mName, true);
			tmr.setModel(model);
		}
		
		LocaliserError localiserError = null;
		String errorString = locError[resultIndex].getDeblankedStringValue();
		if (errorString != null) {
			localiserError = LocaliserErrorFactory.getErrorFromJsonString(errorString);
			tmr.setError(localiserError);
			if (localiserError != null) {
				tml.addLocContents(LocContents.HAS_RANGEERROR);
			}
			// neeed to add to localisatoin object - need to build that correctly first !
//			
			//System.out.printf("Localisation error type %s created\n", localiserError.toString());
		}
		if (localiserError == null && perpDistError != null) {
			// try to work out the angle of the error from the beam lat long. ...
			double angle = Math.PI;
			if (bLL != null && ll != null) {
				angle = bLL.bearingTo(ll) * Math.PI/180.;
			}
			localiserError = new SimpleError(perpDistError, 0., 0., angle);
			tmr.setError(localiserError);
			tml.addLocContents(LocContents.HAS_RANGEERROR);
		}
		
		return tmr;
	}

	

	private LocaliserError getXMLError() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the targetMotionLocaliser
	 */
	public TargetMotionLocaliser getTargetMotionLocaliser() {
		return targetMotionLocaliser;
	}

	/**
	 * @param targetMotionLocaliser the targetMotionLocaliser to set
	 */
	public void setTargetMotionLocaliser(TargetMotionLocaliser targetMotionLocaliser) {
		this.targetMotionLocaliser = targetMotionLocaliser;
	}

	@Override
	public String getName() {
		if (targetMotionLocaliser == null) {
			return "TargetMotionLocaliser";
		}
		else {
			return targetMotionLocaliser.getLocaliserName();
		}
	}

	/**
	 * @return the numResults
	 */
	public int getNumResults() {
		return numResults;
	}

	/**
	 * @return the modelName
	 */
	public PamTableItem getModelName() {
		return modelName;
	}

	/**
	 * @return the latitude
	 */
	public PamTableItem[] getLatitude() {
		return latitude;
	}

	/**
	 * @return the longitude
	 */
	public PamTableItem[] getLongitude() {
		return longitude;
	}

	/**
	 * @return the side
	 */
	public PamTableItem[] getSide() {
		return side;
	}

	/**
	 * @return the chi2
	 */
	public PamTableItem[] getChi2() {
		return chi2;
	}

	/**
	 * @return the aic
	 */
	public PamTableItem[] getAic() {
		return aic;
	}

	/**
	 * @return the prob
	 */
	public PamTableItem[] getProb() {
		return prob;
	}

	/**
	 * @return the degsFreedom
	 */
	public PamTableItem[] getDegsFreedom() {
		return degsFreedom;
	}

	/**
	 * @return the perpDist
	 */
	public PamTableItem[] getPerpDist() {
		return perpDist;
	}

	/**
	 * @return the perpdistErr
	 */
	public PamTableItem[] getPerpdistErr() {
		return perpdistErr;
	}

	/**
	 * @return the phones
	 */
	public PamTableItem[] getPhones() {
		return phones;
	}

	/**
	 * @return the comment
	 */
	public PamTableItem[] getComment() {
		return comment;
	}

	/**
	 * @return the beamLatitude
	 */
	public PamTableItem[] getBeamLatitude() {
		return beamLatitude;
	}

	/**
	 * @return the beamLongitude
	 */
	public PamTableItem[] getBeamLongitude() {
		return beamLongitude;
	}

	/**
	 * @return the beamTime
	 */
	public PamTableItem[] getBeamTime() {
		return beamTime;
	}

	/**
	 * @return the depth
	 */
	public PamTableItem[] getDepth() {
		return depth;
	}

	/**
	 * @return the depthErr
	 */
	public PamTableItem[] getDepthErr() {
		return depthErr;
	}

	/**
	 * @return the locError
	 */
	public PamTableItem[] getLocError() {
		return locError;
	}

	/**
	 * @return the hiddenItems
	 */
	public HashSet<PamTableItem[]> getHiddenItems() {
		return hiddenItems;
	}

	/**
	 * @return the locerrorlength
	 */
	public static int getLocerrorlength() {
		return LOCERRORLENGTH;
	}

}
