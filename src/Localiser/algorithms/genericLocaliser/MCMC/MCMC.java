package Localiser.algorithms.genericLocaliser.MCMC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import Localiser.LocaliserPane;
import Localiser.algorithms.genericLocaliser.MinimisationAlgorithm;
import Localiser.algorithms.genericLocaliser.MinimisationFunction;
import Localiser.algorithms.genericLocaliser.MCMC.old.MCMCParams;
import Localiser.algorithms.locErrors.EllipticalError;
import Localiser.algorithms.locErrors.LocaliserError;
import PamUtils.PamArrayUtils;
import PamUtils.CoordUtils;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;


/**
* Markov chain Monte Carlo (MCMC) is a minimisation technique used widely in a variety of field, from finding exo planets, 
* to solving complex intergals. 
* <p>
* This is a highly computationally intensive localisation algorithm based on MCMC methods. For a good description see;  
* The Transit Light Curve (TLC) Project.I. Four Consecutive Transits of the Exoplanet XO-1b Matthew J. Holman1
* <p>
* A chi2 function is required to define the minimisation problem. 
* <p>
* Multiple MCMC chains can and should be run. These are executed on different threads to take advantage of multi-core processing if possible. 
* Even so, a large number of chains or large observation set can result in significant processing times. 
* <p>
* Results are analysed for convergence and final locations packed into an MCMCTDResults class. 
* 
* @author Jamie Macaulay
*
*/
public class MCMC implements MinimisationAlgorithm {
	
	/**
	 * Function for generating random numbers from Gaussian distribution
	 */
	private Random r;
	
	/**
	 * Paramters class for MCMC. 
	 */
	protected MCMCParams2 settings;
	
	/**
	 * The final results. Multiple possible results may be presents. 
	 */
	private ArrayList<ChainResult> chainResults;
	
	/**
	 * List of MCMC results
	 */
	private ArrayList<MCMCResult> mCMCResults;

	/**
	 * The mean loc result
	 */
	private double[][] locResult;

	/**
	 * The mean chi2 results 
	 */
	private double[] chi2Results;
	
	/**
	 * The loclaiser error. 
	 */
	private EllipticalError[] localiserError;

	
	/**
	 * The chi2 function for the MCMC algorithm. Compares simulated data to observed data. Must be set for the 
	 * algorithm to work properly. 
	 */
	private MinimisationFunction chi2;
	
	/**
	 * Constructor for MCMC algorithm. 
	 */
	public MCMC(){
		this.r=new Random();
		this.settings=new MCMCParams2(); 
	}
	
	/**
	 * Constructor for the MCMC algorithm
	 * @param mCMCParams - the MCMC settings to use. 
	 */
	public MCMC(MCMCParams2 mCMCParams) {
		this.r=new Random();
		this.settings=mCMCParams;
	}

	/**
	 * Run MCMC algortihm. This starts a single Markov chain at a random location.  
	 * @param chi2- the chi2 function to compare simulation data to observed data. 
	 * @return the results from the chain. 
	 */
	public ChainResult mCMC(MinimisationFunction chi2){

		//check to make sure we have a correct chi2 function 
		if (chi2==null){
			return null; 
		}

		double[] chainPos=new double[chi2.getDim()]; 
		double[] potentialNewJump=new double[chi2.getDim()]; 
		

		
		double currentChi=Double.MAX_VALUE;
		double newChi;

		ArrayList<Double> successChi=new ArrayList<Double>(settings.numberOfJumps/5);
		ArrayList<float[]> successJump=new ArrayList<float[]>(settings.numberOfJumps/5);

//		System.out.println("Start MCMC milliseconds: "+ System.currentTimeMillis());
		
		//check some stuff isn't set to null - if it is set some default valuess
		if (settings.jumpSize==null) settings.setJumpSize(1, chi2.getDim());
		if (settings.chainStartDispersion==null) settings.setChainDispersion(100, chi2.getDim());
		
		for (int i=0; i<chainPos.length; i++) {
			chainPos[i]=((Math.random()-0.5)*settings.chainStartDispersion[i][1]-settings.chainStartDispersion[i][0])+settings.chainStartDispersion[i][0]; 
		}


		//now run the algorithm 
		for (int i=0; i<settings.numberOfJumps;i++){

			//calculate a new jump. 
			potentialNewJump=getNewJumpPoint(chainPos);

			//calculate new chi2 value for new jump
			newChi=chi2.value(potentialNewJump); 

			if (newChi<currentChi){
				chainPos=potentialNewJump;
				currentChi=newChi;
				successChi.add(newChi);
				successJump.add(PamArrayUtils.double2Float(chainPos));
				//System.out.println(ChainPos);
				//System.out.println(NewChi);
				//System.out.println(ObservedTimeDelays);
				//System.out.println(GetTimeDelays(PotentialNewJump));	 
			}
			else if (Math.random()<Math.exp((-0.5*(-currentChi+newChi)))){
				chainPos=potentialNewJump;
				currentChi=newChi;
				successChi.add(newChi);
				successJump.add(PamArrayUtils.double2Float(chainPos));
				//System.out.println(ChainPos);
				//System.out.println(NewChi);
				//System.out.println(ObservedTimeDelays);
				//System.out.println(GetTimeDelays(PotentialNewJump))
			}
		}


		ChainResult chainResult=new ChainResult(successJump, successChi); 
		chainResult= analyseChain( chainResult); 
		chainResult.nDim=chi2.getDim(); 
//		System.out.println("Chain results is: " + chainResult.mean[0] + " " +  chainResult.mean[1] + " " +  chainResult.mean[2]); 
//		System.out.println("End MCMC millis: "+ System.currentTimeMillis());

		
		return chainResult;
	}
	
	/**
	 * Class to hold the results of a single Markov chain. 
	 * @author Jamie Macaulay 
	 *
	 */
	public class ChainResult {
		
		/**
		 * Holds a single MCMC chain results
		 * @param successJump - list of successful jumps
		 * @param successChi - list of successful chi2 values. 
		 */
		public ChainResult(ArrayList<float[]> successJump, ArrayList<Double> successChi) {
			this.successJump=successJump;
			this.successChi=successChi; 
		}

		/**
		 * A list of the chi2 values of successful jumps
		 */
		public ArrayList<Double> successChi; 
		
		/**
		 * A list of points of the successful jumps. 
		 */
		public ArrayList<float[]> successJump;
		
		/**
		 * The number of dimensions. 
		 */
		public int nDim;

		/**
		 * The mean value of the chain after the burn in phase has been removed
		 */
		public double[] mean;

		/**
		 * The standard deviation of the chain after the burn in phase has been removed. 
		 */
		public double[] std;

		/**
		 * The mean chi2 value after the burn in phase has been removed
		 */
		public double meanChi;

		/**
		 * The median chi2 value after the burn in phase has been removed. 
		 */
		public double medianChi;
		
		/**
		 * The analysis method which was used to analyse the chain. 
		 */
		public int analysisMethod;

		/**
		 * The minimum chi2 value for all jumps 
		 */
		public double minChi2;

		/**
		 * The position of the minimum chi2 value. 
		 */
		public double[] minChi2Pos;
		
	}
	
	/**
	 * Calculates a random new Co-Ordinate, 3D Cartesian or Cylindrical space;
	 * @param chainPos - the position of the chain in ndimensional space.
	 * @return the new jump point to tests
	 */
	public double[] getNewJumpPoint(double[] chainPos){
		double[] newChainPos=new double[chainPos.length]; 
		
		//Cylindrical
		if (settings.cylindricalCoOrdinates){
			double r= Math.sqrt(Math.pow(chainPos[0], 2)+Math.pow(chainPos[1], 2));
			double theta=Math.atan(chainPos[1]/chainPos[0]);
			double newTheta=(theta+randomNGenerator()*2*Math.PI);
			double newr=Math.abs(r+(randomNGenerator()*(settings.jumpSize[0]+settings.jumpSize[1])/2));
			newChainPos[0]=(newr*Math.cos(newTheta));
			newChainPos[2]=(chainPos[2]+(randomNGenerator()*settings.jumpSize[2]));
			newChainPos[1]=(newr*Math.sin(newTheta));			
		}
		else {
			for (int i=0; i<chainPos.length; i++){
				//if cylindrical then the jump size needs to be different. 
				newChainPos[i]=chainPos[i] + randomNGenerator()*settings.jumpSize[i]; 
			}
		}
		return newChainPos; 
	}

	
	/**
	 * Run MCMC on a single thread.
	 */
	Runnable MarkovChainSequence = new Runnable() {

		@SuppressWarnings("unchecked")
		public void run() {
			//run the simulations
				ChainResult chainResult=mCMC(chi2);
				chainResults.add(chainResult); 
				//System.out.println("MCMC: chain completed");
		  }
	};

	/**
	 * Run the MCMC algorithm. 
	 */
	public ArrayList<MCMCResult> runMCMCAlgorithm() {
		
//		System.out.println("MCMC: Begin MCMC");
		
		// If there is no data in pamDetection then  don't attempt to localise. 
		if (chi2 == null) {
			System.out.println("MCMC: Chi2 function is null: Cannot initilise MCMC algorithm. ");
			return null;
		}
		
		//wipe any previous results and create a new results class; 
		chainResults=new ArrayList<ChainResult>(); 
		
//		System.out.println("Number of Chains: "+settings.numberOfChains);
		
		/*Run multiple Markov Chains. Each chain is started on a different thread.*/
		Thread[] threads=new Thread[settings.numberOfChains];
		
		for (int i=0; i<settings.numberOfChains; i++){
			threads[i]=new Thread(MarkovChainSequence);
		}

		for (int j=0; j<settings.numberOfChains;j++){
			 threads[j].start();
		}
		
		for (int k=0; k<settings.numberOfChains;k++){
			try {
				threads[k].join();
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//kill threads
		for (int l=0; l<settings.numberOfChains;l++){
			threads[l].interrupt();
			threads[l] = null;
		}
		
		
		ArrayList<MCMCResult> finalResults = clusterAnalysis(chainResults);
		//this.finalResults=results;
		
		return finalResults;
	}
	
	/**
	 * Perform a cluster analysis of results. The clustering algorithm used is determined by the MCMC localiser params.  
	 */
	public ArrayList<MCMCResult> clusterAnalysis(ArrayList<ChainResult> meanArray){
		
		ArrayList<MCMCResult> finalResults=new ArrayList<MCMCResult>();
		
		int clusteringAlgorithm=settings.clusterAnalysis;
		
		switch (clusteringAlgorithm){
			case MCMCParams.NONE:
				finalResults.add(averageChains(meanArray));
			break;
			case MCMCParams.K_MEANS:
				if (settings.nKMeans>1) finalResults=clusterChains(meanArray, kMeansAnalysis(meanArray, settings.kmeanAttempts, settings.kmeansIterations));
				else finalResults.add(averageChains(meanArray));
			break;
		}
		return finalResults; 
	}
	
	
	
	
	//TODO - should make this generic to all localisers
	/**
	 * Sometimes multiple chains will converge to different final distributions. If this is the case the results must be clustered, 
	 * otherwise the final answer will simply be the mean of the two different distributions. Here a 3D k-means clustering algorithm is used in x, y and z . 
	 * First, nk random points are assigned in space based on the start dispersion. Next, all chains are clusters corresponding to their euclidian distance 
	 * from these two random points. The random points then move to the centroid of their corresponding clusters. The process repeats 'int iterations' times 
	 * by which time it is likely convergence will have occurred. Clustering is attempted 'attempts' times from different random positions. 
	 * <p><p>
	 * See: https://en.wikipedia.org/wiki/K-means_clustering
	 * @param chains - an array of single Markov chains with summary results (mean position, standard deviation etc.)
	 * @param attempts - the number of clustering attempts. Each attempt starts at a random location for each cluster. All attempts are averaged. 
	 * @param iterations - the number of iterations for each attempt. Should be high enough for each kmeans cluster to converge to a result. 
	 * @return a list of cluster locations. i.e. where the clusters are centred. This is not a list of average positions of the chains within each cluster. 
	 */
	public synchronized ArrayList<double[]> kMeansAnalysis(ArrayList<ChainResult> mcmcChains, int attempts, int iterations){
		
		if (mcmcChains.size()==0) return null; 
		
		//no point in running algorithm if there is just one chain. 
		if (mcmcChains.size()==1) {
			ArrayList<double[]> results=new ArrayList<double[]>(); 
			results.add(mcmcChains.get(0).mean); 
			return results; 
		}
		
//		for(int i=0; i<mcmcChains.size(); i++ ) {
//			System.out.println("Chain " + i + " mean: " + mcmcChains.get(i).mean[0]
//				+" "	+ mcmcChains.get(i).mean[1]+ " "+ + mcmcChains.get(i).mean[2]);
//		}
		
		//convenient to know the number of dimensions that are being used. 
		int nDim=mcmcChains.get(0).nDim; 
		int nKmeans=Math.min(settings.nKMeans, mcmcChains.size()); 
		
		DoublePoint[] chainPoints = new DoublePoint[mcmcChains.size()];
		for (int i=0; i<mcmcChains.size(); i++ ) {
			chainPoints[i]= new DoublePoint(mcmcChains.get(i).mean); 
		}
		
		//create the clustering algorithm from Apache commons. 
		KMeansPlusPlusClusterer<DoublePoint> kmeanCluster= new KMeansPlusPlusClusterer<DoublePoint>(nKmeans,  settings.kmeanAttempts)	;
		//run the algorithm 
		List<? extends Cluster<DoublePoint>> clusters  = kmeanCluster.cluster(Arrays.asList(chainPoints)); 
		
		//convert result back to standard ArrayList. 
		ArrayList<double[]> clusterArray = new 	ArrayList<double[]>(); 
		for (int i=0; i<clusters.size(); i++) {
			clusterArray.add(this.averagePoints(clusters.get(i).getPoints())); 
		}
		
		/**
		 * Now must cluster the clusters. We have a certain number of kmeans attempts each which have the clustered the chains. 
		 * Ideally all attempts will have produced the same result but often this is not the case. 
		 */
		
		/**
		 * We need to find the number of clusters.  Here we have to make a 'decision', i.e. what we define as a cluster. e.g. a 
		 * cluster might be any results which are separated by more than 5m. This comes from a param in MCMC params- let the user decide. 
		 */
		ArrayList<double[]> clusterSorted=new ArrayList<double[]>();
		// group of corresponding clusters
		ArrayList<double[]> clusternK;
		// remaining clusters
		ArrayList<double[]> newClusterArray;
		// cluster to compare distances to. 
		double[] referenceCluster; 
		double[] finalCluster; 

		
//		for (int k=0; k<clusterArray.size(); k++){
//			System.out.println("clusterArray: "+clusterArray.get(k).getX()+ " "+clusterArray.get(k).getY() + " "+ clusterArray.get(k).getZ());
//		}
		
		/***
		 * There may be three clusters, two corresponding the actual chain 
		 * locations and one which accidently clusters all chains. 
		 */
		while (clusterArray.size()!=0){
			
//			if (clusterArray.size()==0){
//				continue;
//			}
			
			clusternK=	new ArrayList<double[]>(); //the current set of clusters to be averaged
			newClusterArray=new ArrayList<double[]>(); //the remaining clusters
			
			referenceCluster=clusterArray.get(0);
				
			for (int i=0; i<clusterArray.size(); i++){
				/**
				 * Check the distance of the clusters from each other and also check the minimum distance of that cluster from the chains, 
				 * because, there is a chance a cluster will end up between two sets of chains. We want to get rid of these. 
				 */
//				System.out.println("clusterArray: "+clusterArray.get(i).getX()+ " "+clusterArray.get(i).getY() + " "+ clusterArray.get(i).getZ());
				if (CoordUtils.dist(clusterArray.get(i), referenceCluster)<settings.maxClusterSize) clusternK.add(clusterArray.get(i));
				else newClusterArray.add(clusterArray.get(i));
				
			}
			
			if (clusternK.size()==0) continue; 
			
			//find the mean position of the cluster; 
			finalCluster=averagePoints(clusternK);
			
			//add only if cluster is near enough some chains; 
			if (checkClusterDistance(finalCluster, mcmcChains)<Math.pow(settings.maxClusterSize,2)) 	clusterSorted.add(finalCluster);
	
			//the new array of clusters omits the clusters that have already been removed. 
			clusterArray=newClusterArray;
				
		}
		
//		for (int j=0; j<clusterSorted.size(); j++){
//			System.out.println("Final Cluster: "+clusterSorted.get(j).getX() + " "  +clusterSorted.get(j).getY()+ " " +clusterSorted.get(j).getZ());
//		}

		return clusterSorted; 
	}
	
	/**
	 * Calculates the average of the points within a list.
	 * @param points - a list of points. 
	 * @return the average of the points
	 */
	private double[] averagePoints(List<DoublePoint> points){
		
		if (points.size()<1) return null; 
		
		int nDim=points.get(0).getPoint().length; 
		
		double[] finalCluster=new double[points.get(0).getPoint().length]; 

		for (int n=0; n<points.size(); n++){
			for (int j=1; j<nDim; j++){
				finalCluster[j]+=points.get(n).getPoint()[j]; 
			}
		}
		
		for (int j=1; j<nDim; j++){
			finalCluster[j]=finalCluster[j]/points.size();
		}	
		
		return finalCluster;
	}
	
	/**
	 * Calculates the average of the points within a list.
	 * @param points - a list of points. 
	 * @return the average of the points
	 */
	private double[] averagePoints(ArrayList<double[]> points){
		
		if (points.size()<1) return null; 
		
		int nDim=points.get(0).length; 
		
		double[] finalCluster=new double[points.get(0).length]; 

		for (int n=0; n<points.size(); n++){
			for (int j=1; j<nDim; j++){
				finalCluster[j]+=points.get(n)[j]; 
			}
		}
		
		for (int j=1; j<nDim; j++){
			finalCluster[j]=finalCluster[j]/points.size();
		}	
		
		return finalCluster;
	}
	
	
	/**
	 * Finds the distance between a point and the closest chain point.  
	 * @param point - the point to check 
	 * @param chainArray - a list of chains
	 * @return the distance of the closest chain 
	 */
	private double checkClusterDistance(double[] point, ArrayList<ChainResult> chainArray){
		
		double minDistance=Double.MAX_VALUE;
		double distance=-1; 
		
		for (int i=0; i<chainArray.size(); i++){
			distance=CoordUtils.dist(chainArray.get(i).mean, point);
			if (distance<minDistance){
				minDistance=distance; 
			}
		}
		return minDistance;
	}
	
	/**
	 * Take an ArrayList of the Markov chain jumps and chi-squared values of each jump and calculate average values and the standard deviation 
	 * for all dimensions. 
	 * <p>
	 * The main task of this function are to 1) get rid of the burn in phase and 2) calculate the mean and standard deviation of where the chain
	 * has converged for all dimensions.  
	 * @param chainResult: the result from a single Markov chain. The summary results are added to this class and this class is also returned by
	 * the function. 
	 * @return ChainResult with added summary of chain result added.  
	 */
	public ChainResult analyseChain(ChainResult chainResult){
		//TODO - implement other chain analysis methods.
		
		double[] mean=new double[chi2.getDim()]; 
		double[] std=new double[chi2.getDim()]; 
		double meanChi;
		double medianChi;
		double minChi2;
		double[] minChi2Pos; 

		switch (settings.chainAnalysis){
			//TODO implement different analysis types here. 
			default: {
				//default is using the percentage ignore method. 
				for (int i=0; i<chi2.getDim(); i++){
					mean[i]=PamArrayUtils.mean(chainResult.successJump, settings.percentageToIgnore, i);
					std[i]=PamArrayUtils.std(chainResult.successJump, settings.percentageToIgnore, i);
				}
				meanChi=PamArrayUtils.mean(chainResult.successChi, settings.percentageToIgnore);
				medianChi=PamArrayUtils.median(chainResult.successChi, settings.percentageToIgnore);
				
				//find min value 
				int minIndex = chainResult.successChi.indexOf(Collections.min(chainResult.successChi));
				minChi2=chainResult.successChi.get(minIndex); 
				minChi2Pos=PamArrayUtils.float2Double(chainResult.successJump.get(minIndex)); 

			break; 
			}
		}
		
		//add results to chain; 
		chainResult.mean=mean;
		chainResult.std=std;
		chainResult.meanChi=meanChi; 
		chainResult.medianChi=medianChi; 
		
		chainResult.minChi2=minChi2;
		chainResult.minChi2Pos=minChi2Pos;
				
		return chainResult;
	}
	
	
	
	/**
	 * Sorts chains by an array for clusters
	 * @param meanArray - list of individual Markov chain simulation
	 * @param clusterSorted- Array of sorted clusters. It can be any size <=nk, not always nK if there are less clusters than nK.
	 * @return
	 */
	private ArrayList<MCMCResult> clusterChains(ArrayList<ChainResult> meanArray, ArrayList<double[]> clusterSorted) {
		
		ArrayList<MCMCResult> finalResults= new ArrayList<MCMCResult>();
		
		if (clusterSorted.size()==0){
			finalResults.add(averageChains(meanArray));
			return finalResults; 
		}
		
		int iVal;
		double distance;
		double minDistance; 
		int[] minDistances=new int[meanArray.size()]; 
		for (int i=0; i<meanArray.size(); i++){
			iVal=0; 
			minDistance=Double.MAX_VALUE;
			for (int j=0; j<clusterSorted.size(); j++){
				distance=CoordUtils.dist(meanArray.get(i).mean, clusterSorted.get(j));
				if (distance<minDistance){
					minDistance=distance; 
					iVal=j;
				}
			}
			minDistances[i]=iVal; 
		}		
		
		ArrayList<ChainResult> resultsCluster;
		
		for (int i=0; i<clusterSorted.size(); i++){
			resultsCluster=new ArrayList<ChainResult>();
			for (int j=0; j<meanArray.size(); j++){
				if (minDistances[j]==i){
					resultsCluster.add(meanArray.get(j));
				}
			}
			if (resultsCluster.size()!=0) finalResults.add(averageChains(resultsCluster));
		}

		return finalResults;
	}
	
	/**
	 * Take different MCMC chains and average the results. 
	 * @param data - a list of MCMC results from a single Markov chain. 
	 * @return the average of all results packaged in a MCMCResult class. 
	 */
	public MCMCResult averageChains(ArrayList<ChainResult> data){
		
		int n=data.size(); 		
		if (n==0) return null;
		int nDim=data.get(0).nDim; 

		
		double[] meanloc=new double[nDim]; 
		double[] meanstd=new double[nDim];  
		double meanChi=0; 
		double meanMedianChi=0; 
		double minChi=Double.MAX_VALUE; 
		double[] minloc = null; 

		
		//first find the averages and min values
		
		 for (int i=0; i<data.size(); i++){
			 for (int j=0; j<nDim; j++){
				 meanloc[j]+=data.get(i).mean[j]; 
				 meanstd[j]+=data.get(i).std[j]; 
			 }
			 meanChi+=data.get(i).meanChi; 
			 meanMedianChi = data.get(i).medianChi; 
			 if (data.get(i).minChi2<minChi) {
				 minChi=data.get(i).minChi2;
				 minloc=data.get(i).minChi2Pos; 
			 }
		 }
		 
		 //divide by n for mean
		 for (int i=0; i<nDim; i++){
			 meanloc[i]= meanloc[i]/n; 
			 meanstd[i]= meanstd[i]; 
		 }
		 //meanChi=meanChi/n;
		 meanChi=this.chi2.value(meanloc); 
		 meanMedianChi=meanMedianChi/n;
		
		 
		 //create a new MCMC result. 
		 MCMCResult averageChainResult=new MCMCResult();
		 
		 //decide whether to use the average or the minimum. 
		 if (meanChi<minChi) {
			 //use mean value of chains
			 averageChainResult.setLocation(meanloc);
			 averageChainResult.setChi2(meanChi);
		 }
		 else {
			 //use the absolute minimum value of the jumps.
			 averageChainResult.setLocation(minloc);
			 averageChainResult.setChi2(minChi);
		 }

		 //calculate errors and add to result.
		 averageChainResult.setError(meanstd);
		 averageChainResult.setChains(data); // the 
		 EllipticalError locError = getLocError(data);
		 averageChainResult.setLocError(locError); 

		 return averageChainResult;
	}
	
	
	/**
	 * Get the localiser error for an MCMC result. 
	 * @param mCMCMResults - the MCMC results i.e. all execute chains
	 * @return localiser error 
	 */
	private EllipticalError getLocError(ArrayList<ChainResult> data) {
		
		ArrayList<float[]> successJumpAll = new ArrayList<float[]>(); 
		List<float[]> successJump; 
		for (int i=0; i<data.size(); i++) {
			successJump = data.get(i).successJump.subList((int) this.settings.percentageToIgnore*data.get(i).successJump.size(),
					data.get(i).successJump.size()-1);
			successJumpAll.addAll(successJump); 
		}
	
		float[][] results= new float[successJumpAll.size()][3]; 
		results=successJumpAll.toArray(results); 
		
		//Elliptical error
		EllipticalError ellError= new EllipticalError(PamArrayUtils.float2Double(results)); 
		
		return ellError;
	}
	
	
	/**
	 * Used in testing the localiser error. 
	 * @return a set of random points centered on (0,0,0); 
	 */
	public double[][] createErrorData(){
		
		double a=100;
		double b=30;
		double c=15; 
		
		Random fRandom = new Random();
		
		int n=5000;
		double[][] errData=new double[n][3];

		for (int i=0; i<n; i++){
			errData[i]=new double[]{fRandom.nextGaussian() * a, 	fRandom.nextGaussian() * b, fRandom.nextGaussian() * c};
//			errData[i]=rotateZ(results, Math.toRadians(40));
//			errData[i]=rotateY(errData[i],  Math.toRadians(30)) ;
//			errData[i]=rotateX(errData[i],  Math.toRadians(30));
		}
	
		return errData; 
	}
	
	
	/**
	 * Generate a random number from a Gaussian distribution. 
	 * @return a random number from Gaussian distribution with a mean of zero and standard deviation of 1. 
	 */
	public double  randomNGenerator(){
		return r.nextGaussian();
		//return Math.random()-0.5;
	}
	
	/**
	 * Get a random number betwene two limits. So for example a number between 5 and 20 would have standard deviation of 15. 
	 * The random number generated form this standard deviation would then have 5 add to it. 
	 * @param limits - the min std and max std values the number should be between
	 * @return the random number; 
	 */
	public double randNGenerator(double[] limits){
		double rand=r.nextGaussian();
		double magnitude=limits[1]-limits[0];
		rand=magnitude*rand+limits[0];
		return rand; 
	}
	
	
	
	/**
	 * Set a new settings class for MCMC. 
	 * @param settings - new MCMCMParams settings class. 
	 */
	public void setSettings(MCMCParams2 settings) {
		this.settings=settings; 
	}
	
	/**
	 * Get settings class for MCMC. 
	 * @return
	 */
	public MCMCParams2 getSettings() {
		return settings;
	}
	

	/**
	 * Get the chi2 class for this algorithm. 
	 * @return the chi2 class for the algortihm 
	 */
	public MinimisationFunction getChi2Function() {
		return chi2;
	}
	
	/**
	 * Set the chi2 value for the MCMC algortihm. This must be set before the algorithm is run. 
	 * @param chi2 - the chi2 class to set. Compares simulation to obsetrved data. 
	 */
	public void setChi2(MinimisationFunction chi2) {
		this.chi2 = chi2;
	}


	@Override
	public void setMinimisationFunction(MinimisationFunction minFunc) {
		this.chi2=minFunc; 
	}


	@Override
	public boolean runAlgorithm() {
		mCMCResults=runMCMCAlgorithm();
		
		//now summarise results into default format; 
		this.locResult=new double[mCMCResults.size()][this.chi2.getDim()]; 
		this.chi2Results = new double[mCMCResults.size()]; 
		this.localiserError = new EllipticalError[mCMCResults.size()]; 
		
		for (int i=0; i<mCMCResults.size(); i++){
			locResult[i]=mCMCResults.get(i).getMeanLoc();
			chi2Results[i]=mCMCResults.get(i).getChi2();
			localiserError[i]=getLocError(mCMCResults.get(i).getData());
			mCMCResults.get(i).setLocError(localiserError[i]); 
		}
		
		return (mCMCResults != null && mCMCResults.size() > 0);
	}
	
	/**
	 * Get MCMC results. 
	 */
	public ArrayList<MCMCResult> getMCMCResults() {
		return this.mCMCResults; 
	}

	@Override
	public double[][] getResult() {
		return locResult;
	}


	@Override
	public LocaliserError[] getErrors() {
		return localiserError;
	}


	@Override
	public void notifyStatus(int status, double progress) {
		// this is going to be complicated to implement because the algorithm 
		//runs on multiple threads. 
	}


	@Override
	public double[] getChi2() {
		return this.chi2Results;
	}


	@Override
	public boolean hasParams() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public LocaliserPane<?> getSettingsPane() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Clear the results from the localiser. 
	 */
	public void clearResults() {
		this.mCMCResults.clear();
		this.localiserError=null;
		this.locResult=null;
		this.chi2Results=null;
	}
	

}
