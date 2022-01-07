package group3dlocaliser.grids;

import java.io.Serializable;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import pamMaths.PamVector;

/**
 * Simple spherical grid. Points are spaced at even angles vertically. The number 
 * of points generated at each vertical angles is reduced as vertical angle
 * increases in order to keep spacing more or less even. 
 * @author dg50
 *
 */
public class SphericalGrid extends Grid3D {
	
	public static final String gridName = "Spherical Grid";

	private SphericalGridParams sphericalGridParams = new SphericalGridParams();
	
	private double[] ranges;

	private double[] vAngles;
	
	private double[] hAngles;

	private int[] nHAngles;

	private int totalAngles;

	private int iVAngle;

	private int iHAngle;
	
	private int iRange;

	public SphericalGrid(String settingsName) {
		super(settingsName);
		
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public Serializable getSettingsReference() {
		return sphericalGridParams;
	}

	@Override
	public long getSettingsVersion() {
		return SphericalGridParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		this.sphericalGridParams = (SphericalGridParams) pamControlledUnitSettings.getSettings();
		return true;
	}

	@Override
	public String getGridName() {
		return gridName;
	}

	@Override
	public int getTotalPoints() {
		if (ranges == null) {
			return 0;
		}
		return ranges.length*totalAngles;
	}

	@Override
	public void resetGrid() {
		int nR = sphericalGridParams.getnRanges();
		ranges = new double[nR];
		if (sphericalGridParams.isLogRangeScale()) {
			double logMin = Math.log(sphericalGridParams.getRangeRange()[0]);
			double logMax = Math.log(sphericalGridParams.getRangeRange()[1]);
			double logStep = (logMax-logMin)/(nR-1);
			for (int i = 0; i < nR; i++) {
				ranges[i] = Math.exp(logMin+i*logStep);
			}
		}
		else {
			double min = sphericalGridParams.getRangeRange()[0];
			double max = sphericalGridParams.getRangeRange()[1];
			double step = (max-min)/(nR-1);
			for (int i = 0; i < nR; i++) {
				ranges[i] = Math.exp(min+i*step);
			}
		}
		double angStep = sphericalGridParams.getAngleStep();
		int nVangs = (int) Math.floor((180+angStep)/angStep);
		vAngles = new double[nVangs];
		nHAngles = new int[nVangs];
		double eqAngles = 360/angStep; 
		totalAngles = 0;
		for (int i = 0; i < nVangs; i++) {
			vAngles[i] = Math.toRadians(-90.+i*angStep);
			nHAngles[i] = (int) Math.max(Math.round(Math.cos(vAngles[i]) * eqAngles), 1); 
			totalAngles += nHAngles[i];
		}
		iVAngle = 0;
		iHAngle = 0;
		iRange = 0;		
	}

	@Override
	public PamVector getNextPoint() {
		if (ranges == null || iRange >= ranges.length) {
			return null;
		}
		double x, y, z;
		if (iHAngle == 0) {
			hAngles = new double[nHAngles[iVAngle]];
			double angStep = 2*Math.PI / hAngles.length;
			double offs = angStep*Math.random();
			for (int i = 0; i < hAngles.length; i++) {
				hAngles[i] = offs + i * angStep;
			}
		}
		x = Math.cos(vAngles[iVAngle])*Math.cos(hAngles[iHAngle])*ranges[iRange];
		y = Math.cos(vAngles[iVAngle])*Math.sin(hAngles[iHAngle])*ranges[iRange];
		z = Math.sin(vAngles[iVAngle])*ranges[iRange];
		PamVector v = new PamVector(x, y, z);
		if (++iHAngle == hAngles.length) {
			iHAngle = 0;
			if (++iVAngle == vAngles.length) {
				iVAngle = 0;
				if (++iRange == ranges.length) {
					iRange++;
				}
			}
		}
		return v;
	}

}
