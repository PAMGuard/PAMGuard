package Localiser.algorithms.genericLocaliser;

import java.util.ArrayList;

import PamDetection.AbstractLocalisation;
import PamUtils.CoordUtils;
import PamUtils.PamUtils;

/**
 * Minimisation function for a localisation performed using time delay calculations. This can be used with any PAMGuard minimisation algorithm; 
 * <p><p>
 * The time delay minimisation function is based on a set of observed time delays. A generic structure for
 * observed time delays is used.
 * <p>
 * Each row in the time delay ArrayList contains a group time delays calculated from N synchrnoised hydrophones 
 * in the corresponding ArrayList<ArrayList<Point3f>> row. This allows potential locaisation between large groups of 
 * unsynchronised hydrophones, a situation always encountered during target motions analysis.
 *
 * @author Jamie Macaulay 
 *
 */
public class Chi2TimeDelays implements MinimisationFunction {
	
	//Variables needed for localisation
	
	/**
	 * A set of time delays in seconds. Each row is a set of delays from N synchronised hydrophones. Different rows
	 * can have different numbers of synchronised hydrophones. 
	 */
	private ArrayList<ArrayList<Double>> timeDelaysObs;

	/**
	 * Time delay errors in seconds. Follows the same structure as time delays. 
	 */
	private ArrayList< ArrayList<Double>> timeDelayErrors;
	
	/**
	 * The position of hydrophones. These follow the same rows as the time delays. 
	 */
	private ArrayList<ArrayList<double[]>> hydrophoneArray; 

	/**
	 * The speed of sound in meters per second. 
	 */
	protected double speedOfSound=1500; //default is 1500

	/*
	 *The simulated time delays for a source at a defined location. 
	 */
	ArrayList<ArrayList<Double>> timeDelaysSim;

	/**
	 * Initial start position of algorithm 
	 */
	private double[] start;

	/**
	 * First algorithm step
	 */
	private double[] firstStep;
	
	/**
	 * Chi2 time delays 
	 */
	public Chi2TimeDelays(){

	}
	
	/**
	 * Chi2 time delays. 
	 * @param timeDelaysObs - the observed time delays in seconds
	 * @param timeDelayErrors - the time delay errors in seconds
	 * @param hydrophoneArray - the hydrophone arrays. 
	 */
	public Chi2TimeDelays(ArrayList<ArrayList<Double>> timeDelaysObs, ArrayList< ArrayList<Double>> timeDelayErrors, 
			ArrayList<ArrayList<double[]>> hydrophoneArray){
		this.timeDelaysObs=timeDelaysObs; 
		this.timeDelayErrors=timeDelayErrors;
		this.hydrophoneArray=hydrophoneArray; 
	}
	

	@Override
	public double value(double[] sourceLoc) {
		//location is the potential location of the source

		//first check we have all data we need
		if (timeDelaysObs==null || timeDelayErrors==null ||  hydrophoneArray==null || speedOfSound<0) return -1; 
		
		//calculate the time delays 
		
		timeDelaysSim=calcTimeDelays(sourceLoc, hydrophoneArray,  speedOfSound);
		
		//calculate the chi2 value - compare the true time delays for source at location to real time delays. 
		double chi2=chiSquared(timeDelaysObs,  timeDelaysSim, timeDelayErrors);
		
		return chi2;
	}

	@Override
	public int getDim() {
		return 3;
	}
	
	/**
	 * Calculates the time in seconds between sound travelling from a point in space to a reciver.
	 * @param reciever the reciever position.
	 * @param source the source location . 
	 * @param speedOfSound - the speed of sound in meters per second. 
	 * @return the time in seconds it take for aq sound to travel from the source to the reciever. 
	 */
	private static double timeToHydrophone(double[] reciever, double[] source, double speedOfSound){
		return Math.abs(CoordUtils.dist(reciever, source) / speedOfSound);
		//return Math.sqrt((Math.pow(Hydrophone.getX()-ChainPos.getX(),2)+Math.pow(Hydrophone.getY()-ChainPos.getY(),2)+Math.pow(Hydrophone.getZ()-ChainPos.getZ(),2)))/ SpeedofSound;
	}
	
	/**
	 * Calculates the chi^2 value between observed data and theoretical delays calculated for a certain point in space.
	 * @param observedTimeDelays- the observed time delays (seconds). 
	 * @param chainPosTimeDelays- the set of time delays to compare to the observed time delays (seconds).
	 * @param errors- the measurement errors in the observed time delays. 
	 * @return chi squared value for observed time delays compared to chain position time delays. 
	 */
	public static double chiSquared(ArrayList<ArrayList<Double>> observedTimeDelays, ArrayList<ArrayList<Double>> chainPosTimeDelays, ArrayList<ArrayList<Double>> errors){

		double chiResult=0;
		for (int k=0;k<observedTimeDelays.size(); k++){
			for (int m=0; m<observedTimeDelays.get(k).size(); m++){
				//System.out.println("Chi: "+observedTimeDelays.get(k).get(m));
				if (observedTimeDelays.get(k).get(m)!=null && observedTimeDelays.get(k).get(m).isNaN()==false){
					chiResult+=(Math.pow((observedTimeDelays.get(k).get(m)-chainPosTimeDelays.get(k).get(m)),2))/Math.pow(errors.get(k).get(m),2);
				}
			}
		}
		return chiResult;
	}
	
	
	/**
	 * Calculate the theoretical time delays if a source was located at position in space. (Technically space can be any number of dimensions but probably 
	 * useful to stick to 3D)
	 * @param sourceLoc- theoretical position of the source.
	 * @param hydrophonePos- the positions of all the hydrophones within the array in Cartesian coOrdinates.
	 * @param speedOfSound- the speed of sound in meters per second. 
	 * @return The time delays that would result from a source at this location. 
	 */
	public static ArrayList<ArrayList<Double>> calcTimeDelays(double[] sourceLoc, ArrayList<ArrayList<double[]>> hydrophonePos, double speedOfSound){

		ArrayList<Integer> indexM1;
		ArrayList<Integer> indexM2;

		ArrayList<Double> timeDelays;
		ArrayList<Double> sourceTime;

		ArrayList<ArrayList<Double>> timeDelaysAll=new ArrayList<ArrayList<Double>>();
		//ArrayList<ArrayList<Point3f>> hydrophonePos=this.hydrophoneArray;

		double timedelay;

		for (int k=0; k<hydrophonePos.size(); k++){

			sourceTime=new ArrayList<Double>();

			for (int i=0; i<hydrophonePos.get(k).size();i++){
				sourceTime.add(timeToHydrophone(hydrophonePos.get(k).get(i),sourceLoc,speedOfSound));
				//System.out.println("HydrophonePos: "+hydrophonePos.get(k).get(i));
				//System.out.println("Source Time: "+sourceTime);
			}

			indexM1=PamUtils.indexM1(hydrophonePos.get(k).size());
			indexM2=PamUtils.indexM2(hydrophonePos.get(k).size());

			timeDelays=new ArrayList<Double>();

			for (int j=0; j<indexM1.size(); j++){
				timedelay=sourceTime.get(indexM2.get(j))-sourceTime.get(indexM1.get(j));
				//note this is m2-m1 in order to stay with convention
				timeDelays.add(timedelay); //TODO
			}

			timeDelaysAll.add(timeDelays);
		}

		return timeDelaysAll;
	}
	
	/**
	 * Set the time delays. Each row is a set for delays from N synchronised hydrophones. Different rows
	 * can have different numbers of synchronised hydrophones. 
	 * @param timeDelays - a set of time delays in seconds. 
	 */
	public void setTimeDelays(ArrayList<ArrayList<Double>> timeDelays){
		this.timeDelaysObs=timeDelays;
	}
	
	/**
	 * 
	 * @param timeDelayErrors
	 */
	public void setTimeDelaysErrors(ArrayList<ArrayList<Double>> timeDelayErrors) {
		this.timeDelayErrors=timeDelayErrors; 
	}
	
	/**
	 * Set the hydrophone positions 
	 * @param hydrophonePos - the hydrophone positions. 
	 */
	public void setHydrophonePos(ArrayList<ArrayList<double[]>> hydrophonePos){
		this.hydrophoneArray=hydrophonePos;
	}
	
	
	/**Some useful functions for adding errors**/ 
	
	/**
	 * Adds the a constant timing error to the time delays error array e.g. this could be used to add an average cross correlation timing error. 
	 * @param timeDelayErrors - list of time delay errors. 
	 * @param timeError - the timing error to add, in samples
	 * @param sampleRate - the sample rate in samples per second. 
	 * @return an array with the timeError added appropriately to each measurement. 
	 */
	public static ArrayList<ArrayList<Double>> addTimingError(ArrayList<ArrayList<Double>> timeDelayErrors, double timeError, float sampleRate){
		//add cross correlation error to time delay errors
		ArrayList<Double> timeDelayErCC;
		ArrayList<ArrayList<Double>> timeDelayErrorsNew=new ArrayList<ArrayList<Double>>();
		for (int i=0; i<timeDelayErrors.size(); i++){
			timeDelayErCC=new ArrayList<Double>();
			for (int j=0; j<timeDelayErrors.get(i).size(); j++){
				timeDelayErCC.add(Math.sqrt(Math.pow(timeDelayErrors.get(i).get(j),2)+Math.pow((timeError/sampleRate), 2)));
			}
			timeDelayErrorsNew.add(timeDelayErCC);
		}
		return timeDelayErrorsNew;
	}


	@Override
	public double[] getFirstStep() {
		if (this.firstStep!=null &&  this.firstStep.length==this.getDim() ) return this.firstStep; 

		double[] firstStep=new double[getDim()];
		for (int i=0; i< getDim(); i++){
			firstStep[i]=100*(Math.random()-0.5); 
		}
		return firstStep; 
	}
	
	/**
//	 * Get the sound speed.
	 * @return - the sound speed in meters per second
	 */
	public double getSpeedOfSound() {
		return speedOfSound;
	}

	/**
	 * Set the sound speed.
	 * @param speedOfSound - the sound speed in meters per second. 
	 */
	public void setSpeedOfSound(double speedOfSound) {
		this.speedOfSound = speedOfSound;
	}
	
	@Override
	public double[] getStart() {
		if (this.start!=null &&  this.start.length==this.getDim() ) return this.start; 
		
		double[] start=new double[getDim()];
		for (int i=0; i< getDim(); i++){
			start[i]=100*(Math.random()-0.5); 
		}
		return start; 
	}

	/**
	 * Set the start location for the algorithm. If null then a random first step is assigned. 
	 * @param start location. Must be the same length as number of dimensions
	 */
	public void setStart(double[] start){
		this.start=start; 
	}
	
	/**
	 * Set the start location for the algorithm. If null then a random location is selected
	 * @param start location. Must be the same length as number of dimensions
	 */
	public void setFirstStep(double[] firstStep){
		this.firstStep=firstStep;
	}


}
