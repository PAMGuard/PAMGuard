package Localiser.algorithms.timeDelayLocalisers.hyperbolic;

import java.util.ArrayList;
import java.util.Random;

import javax.vecmath.Point3f;

import Jama.LUDecomposition;
import Jama.Matrix;
import Localiser.algorithms.timeDelayLocalisers.TimeDelayLocaliserModel;
import PamUtils.PamArrayUtils;
import PamUtils.PamUtils;

/**
 * Hyperbolic localisation attempts to localise using the INVERSE problem. i.e. it directly solves a set of simple arithmetic equations
 * to find the position of source from time delays 
 * <p>
 * The hyperbolic localiser also includes an error estimation by sampling time delays from a distribution of errors. As a localisation has to 
 * occur for each sample, this drastically increases the computational time for the hyperbolic localiser. 
 * 
 * @author Jamie Macaulay
 *
 */
public class Hyperbolic implements TimeDelayLocaliserModel {
	
	//Variables needed for localisation
	private ArrayList<ArrayList<Double>> timeDelaysObs;
				
	protected ArrayList<ArrayList<Point3f>> hydrophoneArray;

	private double speedOfSound;

	/**
	 * The result of the latest localisation calculation in x,y,z meters
	 */
	private double[] result;
	
	/**
	 * The error in the latest calculation in meters. Can be null if no error calculation has occured. 
	 */
	private double[] resultError;

	/**
	 * The current sampleRate- only needed to calc error. 
	 */
	private double sampleRate;
	
	/**
	 * The hyerbolic params. 
	 */
	private HyperbolicParams hyperbolicParams;
	
	/**
	 * A random number generator. 
	 */
	private Random r = new Random();

	
	
	/**
	 * Create an instance of a hyperbolic localiser,. 
	 * @param hydrophoneArray- the hydrophone array. Each ArrayList<Point3f> is a synchronised group of hydrophones in cartesian co-ordinates (meters) 
	 * @param timeDelays - time delay values in seconds. Each is a list of time delays using to indexM1 and indexM2 conventions and corresponding to hydrophones in hydrophoneArray list.  
	 * @param timeDelayErrors - time delay error values in seconds. Each is a list of time delay errors using to indexM1 and indexM2 conventions and corresponding to hydrophones in hydrophoneArray list.  
	 * @param speedOfSound - the speed of sound in m/s
	 * @param hyperbolicParams - hyperbolic parameters to use for this instance of the localiser. 
	 */
	public Hyperbolic(ArrayList<ArrayList<Point3f>> hydrophoneArray ,ArrayList<ArrayList<Double>> timeDelays, ArrayList< ArrayList<Double>> timeDelayErrors, float speedOfSound, HyperbolicParams hyperbolicParams){
		this.speedOfSound=speedOfSound;
		this.timeDelaysObs=timeDelays;
		this.hydrophoneArray=hydrophoneArray;
		this.hyperbolicParams=hyperbolicParams; 
	}
	
	/**
	 * Create an instance of a hyperbolic localiser. Uses default settings. 
	 * @param hydrophoneArray- the hydrophone array. Each ArrayList<Point3f> is a synchronised group of hydrophones in cartesian co-ordinates (meters) 
	 * @param timeDelays - time delay values in seconds. Each is a list of time delays using to indexM1 and indexM2 conventions and corresponding to hydrophones in hydrophoneArray list.  
	 * @param timeDelayErrors - time delay error values in seconds. Each is a list of time delay errors using to indexM1 and indexM2 conventions and corresponding to hydrophones in hydrophoneArray list.  
	 * @param speedOfSound - the speed of sound in m/s
	 */
	public Hyperbolic(ArrayList<ArrayList<Point3f>> hydrophoneArray ,ArrayList<ArrayList<Double>> timeDelays, ArrayList< ArrayList<Double>> timeDelayErrors, float speedOfSound){
		this.speedOfSound=speedOfSound;
		this.timeDelaysObs=timeDelays;
		this.hydrophoneArray=hydrophoneArray;
		this.hyperbolicParams=new HyperbolicParams(); 
		
	}
	
	/**
	 * Create an instance of a hyperbolic localiser. Time delays, time delay errors, hydrophone positions and speed of sound must be set before a localisation can occur. 
	 * @param hyperbolicParams - hyperbolic paramaters to use for this instance of the localiser. 
	 */
	public Hyperbolic(HyperbolicParams hyperbolicParams){;
		this.hyperbolicParams=hyperbolicParams; 
	};
	
	/**
	 * Create an instance of a hyperbolic localiser. Uses default settings. 
	 * Time delays, time delay errors, hydrophone positions and speed of sound must be set before a localisation can occur. 
	 */
	public Hyperbolic(){;
		this.hyperbolicParams=new HyperbolicParams(); 
	};

//	public Hyperbolic(StaticLocalise staticLocalise){
//		this.staticLocalise = staticLocalise;
//	}
	
	private static Point3f calcRelOrigin(Point3f origin, Point3f input){
		float x=input.x-origin.x;
		float y=input.y-origin.y;
		float z=input.z-origin.z;
		return new Point3f(x,y,z);
	}
	
	/**
	 * Calculate the position of a source from time delays. 
	 * @param timeDelays - time delays using indexM1 and indexM2 convention
	 * @param hydrophonePos - hydrophone positions. 
	 * @param speedOfSound - speed of sound in m/s
	 * @return the source location in cartesian co-ordinates.
	 */
	public synchronized double[] calcSource(ArrayList<Double> timeDelays, ArrayList<Point3f> hydrophonePos, double speedOfSound){
		/**
		 * Function synchronized by DG 18/12/15 since some parameters were being reset in subsequent calls from 
		 * different threads and causing crashes. 
		 */
		ArrayList<Integer> indexM1;
		ArrayList<Integer> indexM2;
			
		ArrayList<double[]> matrixARows;
		ArrayList<double[]> matrixBRows;
		
		Point3f primeHPos;
		int primeHydrophone;
			
		indexM1 = PamUtils.indexM1(hydrophonePos.size());
		indexM2 = PamUtils.indexM2(hydrophonePos.size());
			
		//find the first non null time delay
		primeHydrophone=-1;
		for (int k=0; k<timeDelays.size(); k++){
			if (timeDelays.get(k)!=null && primeHydrophone==-1) primeHydrophone=k; 
		}
		
		//find the position of the hydrophone corresponding to the first non null delay
		primeHPos=hydrophonePos.get(indexM1.get(primeHydrophone));
		
		matrixARows=new ArrayList<double[]>();
		matrixBRows=new ArrayList<double[]>();
			
		for (int j=0; j<timeDelays.size(); j++){			
			if (indexM1.get(j)==indexM1.get(primeHydrophone) && timeDelays.get(j)!=null){
				Point3f hLoc=calcRelOrigin(primeHPos,hydrophonePos.get(indexM2.get(j)));
				switch(hyperbolicParams.currentType){
					case HyperbolicParams.LOC_3D:
						matrixARows.add(createMatrixARow3D(timeDelays.get(j),  hLoc,  speedOfSound));
						matrixBRows.add(createMatrixBrow3D(timeDelays.get(j),  hLoc,  speedOfSound));
						break; 
						
					case HyperbolicParams.LOC_2D_Z:
						matrixARows.add(createMatrixARow2D(timeDelays.get(j),  hLoc,  speedOfSound,HyperbolicParams.LOC_2D_Z));
						matrixBRows.add(createMatrixBrow2D(timeDelays.get(j),  hLoc,  speedOfSound,HyperbolicParams.LOC_2D_Z));
						break;
				}
			}
		}
		
//		for (int k=0; k<matrixARows.size(); k++){
//			staticLocalise.getStaticLocaliserControl().terminalPrintln(matrixARows.get(k)[0]+ "  " +matrixARows.get(k)[1]+"  "+ matrixARows.get(k)[2]+"  "+ matrixARows.get(k)[3]+"  *m== "+matrixBRows.get(k)[0], 1);
//		}
			
		//create the matrices for A and B Am=B.
		Matrix A=new Matrix(array2doubleArray(matrixARows));
		Matrix B=new Matrix(array2doubleArray(matrixBRows));
	
		//solve for m
		Matrix s=null;
		try{
			LUDecomposition luDecomposition=new LUDecomposition(A) ;
			s=luDecomposition.solve(B);
		}
		catch (Exception e){
			System.err.println("Hyperbolic Calculation: LU Decomposition failed: Matrix is Singular");
		}
		//Matrix s=A.solve(B);

		double x = 0;
		double y = 0;
		double z = 0;
		double t; 
		double[] source=new double[3];
		
		switch (hyperbolicParams.currentType) {
			case HyperbolicParams.LOC_3D:
				if (s!=null){
					x=s.get(0,0)+primeHPos.x;
					y=s.get(1,0)+primeHPos.y;
					z=s.get(2,0)+primeHPos.z;
				}
				break;
			case HyperbolicParams.LOC_2D_Z:
				if (s!=null) {
					x=s.get(0,0)+primeHPos.x;
					y=s.get(1,0)+primeHPos.y;
					t=s.get(2,0); // the time it takes to travel from 0,0,0 (reference position for the first hydrophone) to the source location. 
					//now find z by substituting into equation;
					z=calcZ2D(x,y,t,speedOfSound)+primeHPos.z;;
				}
				break; 
		}
		
		source[0]=x;
		source[1]=y;
		source[2]=z;
		
		//staticLocalise.getStaticLocaliserControl().terminalPrintln("Hyperbolic Solution: x: "+x+" y: "+y+" z: "+z, 10);

		return source; 
	}
	
	/**
	 * Calculate the errors in source position. This is achieved by sampling a rando number from the time delay error distributions, loclaising and looking at the distribution 
	 * in position of loclaisation results. 
	 * @param timeDelays - time delays
	 * @param hydrophonePos
	 * @param speedOfSound
	 * @param bootStrapN 
	 * @return
	 */
	public double[] calcErrors(ArrayList<Double> timeDelays, ArrayList<Double> timeDelayErrors, ArrayList<Point3f> hydrophonePos, double speedOfSound, int bootStrapN){
	
		double[][] sourcePositions=new double[bootStrapN][3];
		
		for (int i=0; i<bootStrapN; i++){
			//create new time delays from errors 
			ArrayList<Double> timeDelaysJitter=new ArrayList<Double>();
			double error; 
			for (int j=0; j<timeDelayErrors.size() ; j++){
				//generate random Gaussina 
				error=r.nextGaussian()*timeDelayErrors.get(j); 
				//add cross correlation error
				error=Math.sqrt(Math.pow(error,2)+Math.pow((r.nextGaussian()*this.hyperbolicParams.crossCorrError)/sampleRate,2));
				
				//now add that error to the time delay
				timeDelaysJitter.add(timeDelays.get(j)+error);
			}
			sourcePositions[i]=calcSource(timeDelaysJitter, hydrophonePos, speedOfSound);
		}
		
		double[] errors=PamArrayUtils.std(sourcePositions); 
		
		//now take the standard deviation of all the measurements
		
		return errors; 
	}

	
	/**
	 * Calculate the z co-ordinate from 2D problem assuming that the first hydrophone is at 0,0,0;
	 * @param sx- source x position (meters)
	 * @param sy- source y position (meters)
	 * @param t - time for sound to travel from source to 0,0,0
	 * @param c- the speed of soundin meters -per second
	 * @return the positive z value for the 2D problem.
	 */
	private double calcZ2D(double sx, double sy, double t, double c){
		double sz2=Math.pow(c,2)*Math.pow(t, 2)-Math.pow(sx,2)-Math.pow(sy,2); 
		sz2=Math.sqrt(Math.abs(sz2));
		return sz2; 
	}
	
	
	private static double[][] array2doubleArray(ArrayList<double[]> array){
		
		if (array==null) return null;
		if (array.size()==0) return null; 
		
		double[][] arrayd=new double[array.size()][array.get(0).length];
		
		for (int i=0; i<array.size(); i++){
			arrayd[i]=array.get(i);
		}
				
		return arrayd;
	}
	
	/**
	 * Calculate a row for matrix A of (Am=b) in hyperbolic localisation; 
	 * @param td- time delay in seconds
	 * @param hLoc- position of hydrophone, asusming the primary hydrophone at the origin. 
	 * @param speedofSound- speed of sound in meters per second. 
	 * @return double[] row of Matrix A. 
	 */
	public static double[] createMatrixARow3D(double td, Point3f hLoc, double speedofSound){
		double[] matrixArow=new double[4];
		 
		double xsx=2*hLoc.x;
		double ysy=2*hLoc.y;
		double zsz=2*hLoc.z;
		double ct=2*Math.pow(speedofSound, 2)*Math.pow(td, 1);
		
		matrixArow[0]=xsx;
		matrixArow[1]=ysy;
		matrixArow[2]=zsz;
		matrixArow[3]=ct;
		
		return matrixArow;
	}
	
	/**
	 * Calculate a row for matrix A of (Am=b) in hyperbolic localisation; 
	 * @param td- time delay in seconds
	 * @param hLoc- position of hydrophone, asusming the primary hydrophone at the origin. 
	 * @param speedofSound- speed of sound in meters per second. 
	 * @return double[] row of Matrix A. 
	 */
	public static double[] createMatrixARow2D(double td, Point3f hLoc, double speedofSound, int type){
		double[] matrixArow=new double[3];
		 
		double xsx=2*hLoc.x;
		double ysy=2*hLoc.y;
		double ct=2*Math.pow(speedofSound, 2)*Math.pow(td, 1);
		
		matrixArow[0]=xsx;
		matrixArow[1]=ysy;
		matrixArow[2]=ct;
		
		return matrixArow;
	}
	
	/**
	 * Calculate a row for matrix A of (Am=b) in hyperbolic localisation -for the 3D problem 
	 * @param td- time delay in seconds
	 * @param hLoc- position of hydrophone, assuming the primary hydrophone at the origin. 
	 * @param speedofSound- speed of sound in meters per second. 
	 * @return double[] row of Matrix A. 
	 */
	public static double[] createMatrixBrow3D(double td, Point3f hLoc ,double speedofSound){
		double[] matrixBrow=new double[1];
		matrixBrow[0]=(hLoc.x*hLoc.x+hLoc.y*hLoc.y+hLoc.z*hLoc.z)-(Math.pow(speedofSound, 2)*td*td); 
		return matrixBrow;
	}
	
	/**
	 * Calculate a row for matrix A of (Am=b) in hyperbolic localisation - this is for the 2D case. 
	 * @param td- time delay in seconds
	 * @param hLoc- position of hydrophone, assuming the primary hydrophone at the origin. 
	 * @param speedofSound- speed of sound in meters per second. 
	 * @return double[] row of Matrix A. 
	 */
	public static double[] createMatrixBrow2D(double td, Point3f hLoc ,double speedofSound, int type){
		double[] matrixBrow=new double[1];
		matrixBrow[0]=(hLoc.x*hLoc.x+hLoc.y*hLoc.y)-(Math.pow(speedofSound, 2)*td*td); 
		return matrixBrow;
	}
	

	@Override
	public void runAlgorithm() {
		
		ArrayList<double[]> resultsAll=new ArrayList<double[]>();
		
		double[] locresult;
		for (int i=0; i<timeDelaysObs.size(); i++){
			locresult=calcSource( timeDelaysObs.get(i), hydrophoneArray.get(i), speedOfSound);
			if (locresult!=null) resultsAll.add(locresult);
		}
		
		double[] meanResult=new double[3]; 
		for (int j=0; j<resultsAll.size(); j++){
			meanResult[0]+=resultsAll.get(j)[0];
			meanResult[1]+=resultsAll.get(j)[1];
			meanResult[2]+=resultsAll.get(j)[2];
		}
		
		meanResult[0]=meanResult[0]/resultsAll.size();
		meanResult[1]=meanResult[1]/resultsAll.size();
		meanResult[2]=meanResult[2]/resultsAll.size();
		
		if (hyperbolicParams.calcErrors){
		
			
		}
		
		this.result=meanResult;
			
	}
	
	/**
	 * Get the latest localisation result 
	 * @return cartesian position of source in meters
	 */
	public double[] getResult(){
		return result;
	}
	
	/**
	 * Get errors for latest loclaisation result 
	 * @return errors of latest localisation result in meters
	 */
	public double[] getErrors(){
		return resultError; 
	}

	@Override
	public Boolean changeSettings() {
		return null;
	}
	
	
	public ArrayList<ArrayList<Double>> getTimeDelaysObs() {
		return timeDelaysObs;
	}

	public ArrayList<ArrayList<Point3f>> getHydrophoneArray() {
		return hydrophoneArray;
	}

	public double getSpeedOfSound() {
		return speedOfSound;
	}

	public void setTimeDelaysObs(ArrayList<ArrayList<Double>> timeDelaysObs) {
		this.timeDelaysObs = timeDelaysObs;
	}

	public void setHydrophoneArray(ArrayList<ArrayList<Point3f>> hydrophoneArray) {
		this.hydrophoneArray = hydrophoneArray;
	}

	public void setSpeedOfSound(double d) {
		this.speedOfSound = d;
	}

	@Override
	public void stop() {
		//too fast to worry about
	}

	/**
	 * Get the type of hyperbolic localiser which is being used e.g. LOC_3D
	 * @param currentType v- type to set. 
	 */
	public int getCurrentType() {
		return hyperbolicParams.currentType;
	}

	/**
	 * Set the type of hyperbolic loclaiser e.g. LOC_3D
	 * @param currentType v- type to set. 
	 */
	public void setCurrentType(int currentType) {
		hyperbolicParams.currentType = currentType;
	}
	
	/**
	 * Get the hyperbolic parameters
	 * @return the hyperbolic parameters. 
	 */
	public HyperbolicParams getHyperbolicParams() {
		return hyperbolicParams;
	}

	/**
	 * Set the hyperbolic parameters
	 * @param hyperbolicParams - the hyperbolic parameters. 
	 */
	public void setHyperbolicParams(HyperbolicParams hyperbolicParams) {
		this.hyperbolicParams = hyperbolicParams;
	}
	

}
