package group3dlocaliser.algorithm.gridsearch;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;

import Array.ArrayManager;
import Array.SnapshotGeometry;
import Localiser.LocaliserPane;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;
import generalDatabase.SQLLoggingAddon;
import group3dlocaliser.Group3DLocaliserControl;
import group3dlocaliser.algorithm.Chi2Data;
import group3dlocaliser.algorithm.toadbase.TOADBaseAlgorithm;
import group3dlocaliser.algorithm.toadbase.TOADInformation;
import group3dlocaliser.grids.Grid3D;
import group3dlocaliser.grids.SphericalGrid;
import pamMaths.PamVector;

public class TOADGridSearch extends TOADBaseAlgorithm {

	public static String gridSearchName = "Grid Search";
	
	private Grid3D searchGrid;
	
	private TOADGridParams toadGridParams = new TOADGridParams();

	public TOADGridSearch(Group3DLocaliserControl group3dLocaliser) {
		super(group3dLocaliser);
		searchGrid = new SphericalGrid(group3dLocaliser.getUnitName());
	}

	@Override
	public String getName() {
		return gridSearchName ;
	}

	@Override
	public String getToolTipText() {
		return "Grid Search";
	}

	@Override
	public LocContents getLocContents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LocaliserPane<?> getSettingsPane() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasParams() {
		return false;
	}

	@Override
	public void notifyModelProgress(double progress) {
		
	}

	@Override
	public SQLLoggingAddon getSQLLoggingAddon(int arrayType) {
		return null;
	}

	@Override
	public boolean canArrayShape(int arrayShape) {
		return arrayShape == ArrayManager.ARRAY_TYPE_VOLUME;
	}
	
	@Override
	public AbstractLocalisation processTOADs(PamDataUnit groupDataUnit, SnapshotGeometry geometry,
			TOADInformation toadInformation) {
		searchGrid.resetGrid();
		PamVector centre = geometry.getGeometricCentre();
		int nPoint = searchGrid.getTotalPoints();
		Chi2Data bestChi2 = new Chi2Data(Double.MAX_VALUE, 1);
		PamVector bestPoint = null; 
		while(true) {
			PamVector gridPoint = searchGrid.getNextPoint();
			if (gridPoint == null) {
				break;
			}
			gridPoint = gridPoint.add(centre);
			Chi2Data chi2 = calcChi2(geometry, toadInformation, gridPoint.getVector());
			if (chi2 != null && chi2.getChi2() < bestChi2.getChi2()) {
				bestPoint = gridPoint;
				bestChi2 = chi2;
			}
		}
		if (bestPoint == null) {
			return null;
		}

		LatLong pos = geometry.getReferenceGPS().addDistanceMeters(bestPoint);
		GroupLocResult glr = new GroupLocResult(pos, 0, bestChi2.getChi2());
		glr.setError(null);
		glr.setPerpendicularDistance(0.);
		glr.setModel(this);
		int nToads = countUsableTOADS(toadInformation);
		int nDF = nToads-3;
		glr.setnDegreesFreedom(nDF);
		glr.setDim(3);
		glr.setBeamLatLong(geometry.getReferenceGPS());
		glr.setBeamTime(groupDataUnit.getTimeMilliseconds());
		glr.setAic(bestChi2.getChi2()-6);
		ChiSquaredDistributionImpl chi2Dist = new ChiSquaredDistributionImpl(nDF);
		double p;
		try {
			p = chi2Dist.cumulativeProbability(bestChi2.getChi2());
			glr.setProbability(p);
		} catch (MathException e) {
			glr.setProbability(null);
		}
		GroupLocalisation groupLocalisation = new GroupLocalisation(groupDataUnit, glr);
		return groupLocalisation;
	}


}
