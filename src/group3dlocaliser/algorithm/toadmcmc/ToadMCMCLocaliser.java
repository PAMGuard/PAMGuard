package group3dlocaliser.algorithm.toadmcmc;

import java.util.ArrayList;

import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;

import Array.ArrayManager;
import Array.SnapshotGeometry;
import Localiser.LocaliserPane;
import Localiser.algorithms.genericLocaliser.MinimisationFunction;
import Localiser.algorithms.genericLocaliser.MCMC.MCMC;
import Localiser.algorithms.genericLocaliser.MCMC.MCMCResult;
import Localiser.algorithms.locErrors.EllipticalError;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import Localiser.algorithms.genericLocaliser.MCMC.MCMCParams2;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.CPUMonitor;
import PamUtils.LatLong;
import PamUtils.PamArrayUtils;
import PamguardMVC.PamDataUnit;
import generalDatabase.SQLLoggingAddon;
import group3dlocaliser.Group3DLocaliserControl;
import group3dlocaliser.algorithm.Chi2Data;
import group3dlocaliser.algorithm.LocaliserAlgorithmParams;
import group3dlocaliser.algorithm.crossedbearing.CrossedBearingSQLAddon;
import group3dlocaliser.algorithm.hyperbolic.HyperbolicParams;
import group3dlocaliser.algorithm.toadbase.TOADBaseAlgorithm;
import group3dlocaliser.algorithm.toadbase.TOADInformation;
import group3dlocaliser.grouper.DetectionGroupedSet;
import pamMaths.PamVector;



public class ToadMCMCLocaliser extends TOADBaseAlgorithm {
	
	MCMC mcmc = new MCMC();
	
	/**
	 * CPU monitor
	 */
	private CPUMonitor cpuMCMC;

	private MCMCLocaliserPane mcmcSettingsPane; 

	public ToadMCMCLocaliser(Group3DLocaliserControl group3dLocaliser) {
		super(group3dLocaliser);
		cpuMCMC = new CPUMonitor();

	}
	
	
	public DetectionGroupedSet preFilterLoc(DetectionGroupedSet preGroups) {
		
		
		return preGroups; 
	}

	@Override
	public String getName() {
		return "MCMC";
	}

	@Override
	public String getToolTipText() {
		return "Time delay of arrival Markov chain Monte Carlo (MCMC) based localisation Computationaly very slow but less prone to runaway and calculates more accurate error distributions" ;
	}

	@Override
	public LocContents getLocContents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LocaliserPane getAlgorithmSettingsPane() {
		if (mcmcSettingsPane==null) {
			mcmcSettingsPane = new MCMCLocaliserPane(); 
		}
		return mcmcSettingsPane;
	}

	@Override
	public boolean hasParams() {
		return true;
	}

	@Override
	public void notifyModelProgress(double progress) {
		// TODO Auto-generated method stub
	}
	


	@Override
	public AbstractLocalisation processTOADs(PamDataUnit groupDataUnit, SnapshotGeometry geometry,
			TOADInformation toadInformation) {
		
//		System.out.println("Run MCMC: ------ " + groupDataUnit.getUID()); 
//		PamArrayUtils.printArray(toadInformation.getToadSeconds());
		
		/**
		 * This module is a little odd in that it stores paramters for each algorithm in it's own has table without acc
		 */
		MCMCParams2 params = (MCMCParams2) group3dLocaliser.getLocaliserAlgorithmParams(this).getAlgorithmParameters();
		
		cpuMCMC.start();
		PamVector centre = geometry.getGeometricCentre();

		
		MCMCChi2Function chi2Func =  new MCMCChi2Function(geometry, toadInformation); 
		mcmc.setChi2(chi2Func);
		
		//set the parameters. 
		mcmc.setSettings(params);
		
		//these are the *best results. 
		ArrayList<MCMCResult> mcmcResult = mcmc.runMCMCAlgorithm(); 
		
		
		
		GroupLocalisation groupLocalisation = null; 
		for (int i=0; i<mcmcResult.size(); i++) {
			
			double[][] jumps = mcmcResult.get(i).getJumpsd(10); //we don't need all the jumps to calculate an error. 
			
			//System.out.println("Jumps before: " + jumps.length); 

			
			jumps = PamArrayUtils.split(jumps, (int) (0.9*jumps.length), jumps.length);  
			
			//System.out.println("Jumps after: " + jumps.length); 

			
			EllipticalError ellipErr = new MCMCEllipticalError(jumps, mcmcResult.get(i).getMeanLoc()); 
					
					
			LatLong pos = geometry.getReferenceGPS().addDistanceMeters(new PamVector(mcmcResult.get(i).getMeanLoc()).add(centre));
			
			double chi2val =   chi2Func.value(mcmcResult.get(i).getMeanLoc()); 
	
			GroupLocResult glr = new GroupLocResult(pos, i, chi2val);
			glr.setError(ellipErr);
			glr.setPerpendicularDistance(0.);
			glr.setModel(this);
			int nToads = countUsableTOADS(toadInformation);
			int nDF = nToads-3;
			glr.setnDegreesFreedom(nDF);
			glr.setDim(3);
			glr.setBeamLatLong(geometry.getReferenceGPS());
			glr.setBeamTime(groupDataUnit.getTimeMilliseconds());
			glr.setAic(chi2val-6);
			ChiSquaredDistributionImpl chi2Dist = new ChiSquaredDistributionImpl(nDF);
			double p;
			
			if (i==0) {
				 groupLocalisation = new GroupLocalisation(groupDataUnit, glr);
			}
			groupLocalisation.addGroupLocaResult(glr); 
		}
		
		cpuMCMC.stop();
		
		return groupLocalisation;
	}

	@Override
	public SQLLoggingAddon getSQLLoggingAddon(int arrayType) {
		switch (arrayType) {
		case ArrayManager.ARRAY_TYPE_NONE:
		case ArrayManager.ARRAY_TYPE_POINT:
			return null;
		case ArrayManager.ARRAY_TYPE_LINE:
		case ArrayManager.ARRAY_TYPE_PLANE:
			return new CrossedBearingSQLAddon(2);
		case ArrayManager.ARRAY_TYPE_VOLUME:
			return new CrossedBearingSQLAddon(1);
		}
		return null;
	}
	
	
	private class MCMCChi2Function implements MinimisationFunction {

		private SnapshotGeometry geometry;
		private TOADInformation toadInformation;

		/**
		 * Create a chi2 function for MCMC. 
		 * @param toadInformation - the time delay information. 
		 * @param geometry - the array geometry
		 */
		public MCMCChi2Function(SnapshotGeometry geometry, TOADInformation toadInformation) {
			super();
			this.geometry = geometry;
			this.toadInformation = toadInformation;
		}

		@Override
		public double value(double[] position) {
//			lastPosition = position;
			Chi2Data chiData = calcChi2(geometry, toadInformation, position);
			if (chiData == null) {
				return Double.NaN;
			}
			return chiData.getChi2();
		}

		@Override
		public int getDim() {
			return 3;
		}

		@Override
		public double[] getStart() {
			//can be null for MCMC as it handles it's own start locations
			return null;
		}

		@Override
		public double[] getFirstStep() {
			//not used in MCMC
			return null;
		}

	}


	

}

