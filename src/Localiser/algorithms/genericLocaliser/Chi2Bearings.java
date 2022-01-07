package Localiser.algorithms.genericLocaliser;


import PamUtils.PamUtils;
import pamMaths.PamQuaternion;
import pamMaths.PamVector;

/**
 * Minimisation functions which is based on bearings rather than time delay measurements. <i>e.g.</i> this could be used for 
 * target motion localisation, DIFAR buoy or any system which uses bearings rather than time delay measurements. 
 * <p><p>
 * 
 * @author Jamie Macaulay
 *
 */
public class Chi2Bearings implements MinimisationFunction {

	/**
	 * The location of the sub detection.
	 */
	private PamVector[] subDetectionOrigins;

	/**
	 * The error in the angle of the sub detection
	 */
	private PamVector[] subDetectionAngleErrors;
	
	private double[] scalarAngleErrors;

	/**
	 * The bearing of the sub detection in vector form. 
	 */
	private PamVector[] usedWorldVectors;

	/**
	 * The heading of the array at each loclaisation point. Used if a localisation algortihm to transform stuff. 
	 */
	public PamQuaternion[] rotationVector; 
	
	/**
	 * Conveneince variable. Heading vectors. 
	 */
	public PamVector[] headingVectors; 
	
	/**
	 * Detection angles for simple cases of linear arrays
	 * these are angles relative to the heading vectors. 
	 */
	public double[] detectionAngles;

	/**
	 * Default is 2D solution. 
	 */
	private int nDim=2;

	/**
	 * The start location of the algorithm 
	 */
	double[] start=null;

	/**
	 * The first algorithm step. 
	 */
	double[] firstStep=null;

	/**
	 * Constructor to set up the minimisation function for bearing localisation
	 * @param wordlVectors - the world vectors- relative to the x y of the array
	 * @param subDetectionAngleErrors - the errors in the bearings
	 * @param subDetectionOrigins - the location of each bearing origin in x y z co-ordinate frame
	 */
	public Chi2Bearings(PamVector[] wordlVectors,  PamVector[] subDetectionAngleErrors, PamVector[] subDetectionOrigins, PamQuaternion[] rotationVector){
		this.rotationVector=rotationVector; 
		this.usedWorldVectors=wordlVectors;
		this.subDetectionOrigins=subDetectionOrigins;
		this.subDetectionAngleErrors=subDetectionAngleErrors;
		calcScalarErrors();
		calcHeadings(); 
	}

	/**
	 * Get scalar angle errors. Since some bearings have two errors and others only one, 
	 * but the calculation we're using in the minimisation function only allows for a single
	 * error, just take the sqrt(sumSqr) of the different errors to use as an overall error. 
	 * If for any reason an error wasn't set. Use 1 degree. 
	 */
	private void calcScalarErrors() {
		if (subDetectionAngleErrors ==null) {
			return;
		}
		scalarAngleErrors = new double[subDetectionAngleErrors.length];
//		PamVector ref = new PamVector(0,1,0);
		for (int i = 0; i < scalarAngleErrors.length; i++) {
//			scalarAngleErrors[i] = Math.acos(ref.dotProd(subDetectionAngleErrors[i]));
//			if (scalarAngleErrors[i] == 0) scalarAngleErrors[i] = Math.toRadians(1.);
			if (subDetectionAngleErrors[i] != null) {
				scalarAngleErrors[i] = subDetectionAngleErrors[i].norm();
			}
			else {
				scalarAngleErrors[i] = Math.toRadians(1.);
			}
		}
	}

	/**
	 * Constructor to set up the minimisation function for bearing localisation
	 * @param wordlVectors - the world vectors- relative to the x y of the array
	 * @param subDetectionAngleErrors - the errors in the bearings
	 * @param subDetectionOrigins - the location of each bearing origin in x y z co-ordinate frame
	 * @param nDim - the number of dimensions to solve for. 
	 */
	public Chi2Bearings(PamVector[] wordlVectors,  PamVector[] subDetectionAngleErrors, 
			PamVector[] subDetectionOrigins, PamQuaternion[] rotationVector, int nDim){
		this.rotationVector=rotationVector; 
		this.usedWorldVectors=wordlVectors;
		if (wordlVectors == null) {
			System.out.println("Null world vectors");
		}
		this.subDetectionOrigins=subDetectionOrigins;
		this.subDetectionAngleErrors=subDetectionAngleErrors;
		calcScalarErrors();
		calcHeadings();
		this.nDim=nDim; 
	}
	
	
	/**
	 * Calculation of flat heading vectors for speed purposes. Only used in 3D case. 
	 */
	private void calcHeadings(){
		if (usedWorldVectors == null) {
			return;
		}
		headingVectors= new PamVector[rotationVector.length];
		detectionAngles = new double[usedWorldVectors.length];
		for (int i=0; i<headingVectors.length; i++){
			headingVectors[i]=PamVector.fromHeadAndSlant(Math.toDegrees(rotationVector[i].toHeading()),0); 
			headingVectors[i].normalise(); //normalise to reduce magnitude calculations in dot product. 
			detectionAngles[i] = Math.acos(usedWorldVectors[i].dotProd(headingVectors[i]));
//			//TEMP
//			System.out.println("Heading vector: " +Math.toDegrees(rotationVector[i].toHeading())
//			+" world vector: " + Math.toDegrees(PamVector.vectorToSurfaceBearing(usedWorldVectors[i])));
//			double angleobs =  Math.acos(usedWorldVectors[i].dotProd(headingVectors[i]));
//			System.out.println("Angle obs: " +  Math.toDegrees(angleobs));
//			System.out.println("Subdetection: " +  subDetectionOrigins[i].toString());
//			System.out.println("subDetectionAngleErrors: " +  subDetectionAngleErrors[i].toString());
//			//TEMP
			
		}
	}



	@Override
	public double value(double[] location) {

		int nSubDetections=subDetectionOrigins.length;

		if (nSubDetections < 2) {
			return -1; 
		}
		int nUsed = 0;
		PamVector pointVec;
		PamVector subDetOrigin;
		PamVector angleVec;
		double[] vecData;
		double angle;
		double chiTot = 0.;
		for (int i = 0; i < nSubDetections; i++) {
			if (usedWorldVectors[i] == null) {
				continue;
			}
			nUsed++;
			subDetOrigin = subDetectionOrigins[i];

			///TEMP////////// - bug fixing purposes only
			//subDetOrigin = subDetectionOrigins[i].rotate(-45); //TEMP TEMP TEMP
			//////////////////////////////

			vecData = subDetOrigin.getVector();

			/*
			 * Calculate a vector pointing from the origin of this detection to the 
			 * current location in the optimisation. 
			 */
			if (getDim()==2) 	pointVec = new PamVector(location[0] - vecData[0], location[1] - vecData[1], 0);
			else pointVec = new PamVector(location[0] - vecData[0], location[1] - vecData[1], location[2]-vecData[2]);
			
			if (pointVec.normalise() == 0) {
				/*
				 * we're right on the origin for that point,so the angle has to be zero 
				 * so no need to add anything to the Chi2.
				 */
				continue; 
			}

			/*
			 * Get the vector representing the angle from the origin to the actual source. 
			 */
			angleVec = usedWorldVectors[i];
			angleVec.normalise();

			if (angleVec.isCone() && this.getDim()==3){
				/**
				 * Now that the pointVec vector is in three dimensions we have to use a slightly different
				 * method for calculating chi2. 1) work out the angle between the hydrophone array heading vector and the 
				 * simulated location vector at the current simplex iteration. Work out the angle between the heading vector and 
				 * the observed vector. Now calculate the difference between the two calculated angles for comparison. 
				 * This gets around the fact that the bearings are 3D hyperbolic cones. 
				 */
				angle = Math.acos(pointVec.dotProd(headingVectors[i]));
								
//				double angleobs =  detectionAngles[i];//Math.acos(angleVec.dotProd(headingVectors[i]));
				
				angle=PamUtils.minAngler(angle,detectionAngles[i]);				


			}
			else {
				/**
				 * Calculate the angle between the two vectors; 
				 */
				angle = Math.acos(angleVec.dotProd(pointVec));
			}

			//System.out.println("Chi2Bearing: Angle: "+ Math.toDegrees(angle)); 
			//TODO - got to be a better way of doing this. 
//			angle /= Math.abs(Math.toDegrees(PamVector.vectorToSurfaceBearing(subDetectionAngleErrors[i]))); //TODO- erm...is this right?
			angle /= scalarAngleErrors[i];
			angle *= angle;
			chiTot += angle;
		}
		if (nUsed == 0) {
			return -1; 
		}
		if (Double.isNaN(chiTot)) {
			return -1; 
		}

		/**
		 * Not entirely sure that this is a good idea since calc may be relative
		 * to the array centre so may not work for deep arrays ? In Meygen analysis we've
		 * set the z0 at the centre of the turbine, so are expecting a lot of the localisations
		 * to be + in z. 
		 */
//		if (getDim()>=3){
//			// penalise flying if in 3D!
//			if (location[2] > 0) {
//				chiTot *= (location[2]+1);
//			}
//		}

		return chiTot;
	}

	@Override
	public int getDim() {
		return nDim;
	}



	@Override
	public double[] getStart() {
		if (this.start!=null &&  this.start.length==nDim ) return this.start; 

		//if not suitable switch to most common default. 
		//special cases for 2D and 3D to keep consistent with old
		//Implementation of same algorithm. 
		double[] start=null; 
		switch (nDim){
		//2D default
		case 2:
			start=new double[2];
			start[0]=100;
			start[1]=0;
			break; 
			//3D default
		case 3:
			start=new double[3];
			start[0]=0;
			start[1]=0;
			start[2]=-100;
			break; 
		default:
			start=new double[nDim];
			for (int i=0; i< nDim; i++){
				start[i]=100*(Math.random()-0.5); 
			}
			break; 
		}
		return start;
	}

	/**
	 * Set the start location for the algortihm 
	 * @param start location. Must be the same length as number of dimensions
	 */
	public void setStart(double[] start){
		this.start=start; 
	}




	@Override
	public double[] getFirstStep() {
		if (this.firstStep!=null &&  this.firstStep.length==nDim ) return this.firstStep; 
		//if not suitable switch to most common default. 
		//special cases for 2D and 3D to keep consistent with old
		//Implementation of same algorithm. 
		double[] firstStep=null; 
		switch (nDim){
		//2D default
		case 2:
			firstStep=new double[2];
			firstStep[0]=10;
			firstStep[1]=100;
			break; 
			//3D default
		case 3:
			firstStep=new double[3];
			firstStep[0]=1;
			firstStep[1]=1;
			firstStep[2]=1;
			break; 
		default:
			firstStep=new double[nDim];
			for (int i=0; i< nDim; i++){
				firstStep[i]=100*(Math.random()-0.5); 
			}
			break; 
		}
		return firstStep;
	}



	/**
	 * Get sub detection origins. This is the origin of the subdection in Cartesian space. 
	 * @return a list of sub detection co-ordinates. 
	 */
	public PamVector[] getSubDetectionOrigins() {
		return subDetectionOrigins;
	}


	/**
	 * Get sub detection angle errors. 
	 * @return the angular errors
	 */
	public PamVector[] getSubDetectionAngleErrors() {
		return subDetectionAngleErrors;
	}

	/**
	 * Get the angle of each sub detection. Note that the this is a non-ambiguous angle. 
	 * @return the angle of each sub detection. 
	 */
	public PamVector[] getUsedWorldVectors() {
		return usedWorldVectors;
	}

	/**
	 * Get the rotation of the hydrophone array for each detection. Used to transofmr co-oridnatres, 
	 * @return the rotation of the hydrophone for each subdetection. 
	 */
	public PamQuaternion[] getRotationVectors() {
		return rotationVector;
	}


}
