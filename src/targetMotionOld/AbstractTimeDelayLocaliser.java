package targetMotionOld;

import java.util.ArrayList;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import Array.ArrayManager;
import Array.PamArray;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;
import pamMaths.PamVector;
import targetMotionOld.algorithms.AbstractTargetMotionModel;

/**
 * Reinstated Target motion add-in as used by the click detector. Hope one day still to replace this
 * with Jamie's new one, but keep this one until Jamie's is working. 
 * @author Doug Gillespie
 *
 * @param <T>
 */
 abstract public class AbstractTimeDelayLocaliser<T extends PamDataUnit> extends AbstractTargetMotionModel<T>{
	 
	 
	 	//Event Info
	 
	  	// changed from PamDetection to PamDataUnit (since we've moved the generics there now)
		protected ArrayList<PamDataUnit> currentDetections;
		
		protected PamVector[] detectionOrigins;
		
		protected PamVector[] detectionHeadings;
				
		public EventRotator eventRotator;
		
		protected int nSubDetections;
		


		//Array info
		
		protected ArrayManager arrayManager;
				
		protected PamArray currentArray;
		
		ArrayList<PamVector> arrayError;
		
		protected double avHydrophoneDepth;
		
		protected int nChannels;
		
		protected float sampleRate;
		
		protected double speedOfSound;
				
		
		//info for localiser
		
		protected ArrayList<ArrayList<Double>> timeDelaysAll;
		
		protected ArrayList<ArrayList<Double>> timeDelayErrorsAll;
		
		protected ArrayList<ArrayList<Point3f>> hydrophonePos;
		

		/**
		 * Get info for the event attached to the pamDetection
		 * @param pamDetection
		 */
		public void getEventInfo(SuperDetection pamDetection){
			
			nSubDetections = pamDetection.getSubDetectionsCount();
			currentDetections=new ArrayList<PamDataUnit>();
			for (int i=0; i<nSubDetections; i++){
				currentDetections.add(pamDetection.getSubDetection(i));
			}
			
			//rotate the event 
		  	this.eventRotator=new EventRotator(pamDetection);

		  	//get the rotated angles from event Rotator
			this.nSubDetections = eventRotator.getnSubDetections();
			this.detectionOrigins = eventRotator.getRotatedOrigins();
			this.detectionHeadings = eventRotator.getRotatedHeadings();

			//get Array Info
			arrayManager = ArrayManager.getArrayManager();
		  	this.currentArray = arrayManager.getCurrentArray();
		  	this.nChannels=currentArray.getHydrophoneCount();
			this.speedOfSound = currentArray.getSpeedOfSound();
			
			
			sampleRate=pamDetection.getParentDataBlock().getSampleRate();
		}
		
		
		public void setEventRotator(SuperDetection pamDetection){
			  eventRotator=new EventRotator(pamDetection);
			
		}
	
		
		/**
		 * Calculate the time delays for each pamDetection. 
		 */
		protected void calculateTimeDelays() {
			
			if (currentDetections==null) return;
			
			timeDelaysAll=new ArrayList<ArrayList<Double>>();
			ArrayList<Double> timeDelays;
			double[] tDs;
			for (int i=0; i< currentDetections.size(); i++){
				timeDelays=new ArrayList<Double>();
				tDs=currentDetections.get(i).getLocalisation().getTimeDelays();
				for (int j=0; j<tDs.length; j++){
					timeDelays.add(tDs[j]);
				}
				timeDelaysAll.add(timeDelays);
			}
		}
		
		
		protected void calculateTimeDelayErrors() {
		if (currentDetections==null) return;
			
			timeDelayErrorsAll=new ArrayList<ArrayList<Double>>();
			ArrayList<Double> timeDelayErrors;
			double[] tDErrors;
			for (int i=0; i< currentDetections.size(); i++){
				timeDelayErrors=new ArrayList<Double>();
				tDErrors=currentDetections.get(i).getLocalisation().getTimeDelayErrors();
				for (int j=0; j<tDErrors.length; j++){
					timeDelayErrors.add(tDErrors[j]);
				}
				timeDelayErrorsAll.add(timeDelayErrors);
			}
		}
		
		/**
		 * We need to calculate the position of all hydrophones in a cartesian (x,y,z) co-ordinate system. To do this we need GPS Co-Ordinates, the heading of the array and dimensions of the array. 
		 */
		protected void calcHydrophonePositions(){
			
			hydrophonePos=new ArrayList<ArrayList<Point3f>>();
			//check that the detection origins have been calculated.

			if (detectionOrigins==null) return;
			if (detectionHeadings==null) return;

			/*now loop over every hydrophone in every subdetection to rotate the positions and essentially convert into one large multi element array in cartesian space. 
			 * The number of elements in this array will be nSubDFetections*numberOfHydrophones
			 * */
			double[] hPos;
			double[] hErrors;
			int[] hydrophoneMap;
			ArrayList<Point3f> hydrophonePositions;
			ArrayList<Point3f> hydrophonePositionErrors;
			double totalHx;
			double totalHy;
			double totalHz;
			Point3d arrayOriginPt;
			PamVector relElement;
			
			float x, y, z;
			
			for (int i=0; i<currentDetections.size(); i++  ){
				//TODO- must be sorted in terms of channel grouping. 
				/*
				 * We must calculate the position of the hydrophone for each detection as someone could have imported hydrophone positions which can change for different detections. 
				 */
				hydrophoneMap=PamUtils.getChannelArray(currentDetections.get(i).getChannelBitmap());
				hydrophonePositions=new ArrayList<Point3f>();
				hydrophonePositionErrors=new ArrayList<Point3f>();
				
				//get a point3f array of the hydrophone positions
				for (int n=0; n<hydrophoneMap.length;n++){
					hPos = currentArray.getHydrophoneCoordinates(hydrophoneMap[n], currentDetections.get(i).getTimeMilliseconds());
					hErrors = currentArray.getHydrophoneCoordinateErrors(hydrophoneMap[n], currentDetections.get(i).getTimeMilliseconds());
//					hPos=currentArray.getHydrophone(hydrophoneMap[n]).getCoordinates();
//					hErrors=currentArray.getHydrophone(hydrophoneMap[n]).getCoordinates();
					hydrophonePositions.add(new Point3f((float) hPos[0],(float) hPos[1], (float)hPos[2]));
					hydrophonePositionErrors.add(new Point3f( (float) hErrors[0], (float)  hErrors[1], (float) hErrors[2]));
				}
				
				/*Find the origin point of the array. In the case of a paired towed array this will be halfway between the elements but in the case of more complicated towed array 
				 * this will be the average positions of all the hydrophones
				 */
				//so this needs to be sorted in terms of channnel grouping. 
				totalHx=0;
				totalHy=0;
				totalHz=0;
				for (int p=0; p<hydrophoneMap.length;p++){
					totalHx+=hydrophonePositions.get(p).getX();
					totalHy+=hydrophonePositions.get(p).getY();
					totalHz+=hydrophonePositions.get(p).getZ();
				}
				
				arrayOriginPt=new Point3d(totalHx/hydrophoneMap.length,totalHy/hydrophoneMap.length,totalHz/hydrophoneMap.length);
				
				ArrayList<Point3f> hRotatedPositions=new ArrayList<Point3f>();

				//System.out.println("HeadingAngle: "+Math.toDegrees(HeadingAngle));
				//work out the change in position for each element due to heading angle
				
				Point3f hPos3dNew;
				PamVector heading;
				for (int k=0; k<hydrophoneMap.length; k++){
					
					heading=detectionHeadings[i];
					if (heading==null) heading=new PamVector(0,0,0);
					
					//work out the position of the element of the array origin is assumed to be 0,0,0,
					relElement=new PamVector(hydrophonePositions.get(k).getX()-arrayOriginPt.getX(),hydrophonePositions.get(k).getY()-arrayOriginPt.getY(),hydrophonePositions.get(k).getZ()-arrayOriginPt.getZ() );
					//now rotate around that origin by the heading.
					relElement=relElement.rotate(new PamVector(0,-1,0));
					relElement=relElement.rotate(heading);
					
					x=(float) relElement.getVector()[0];
					y=(float) relElement.getVector()[1];
					z=(float) relElement.getVector()[2];

					//add into the frame of reference with respect to the gps
					hPos3dNew=new Point3f(x +(float) (detectionOrigins[i].getVector()[0]), y+(float) (detectionOrigins[i].getVector()[1]), z+(float) (detectionOrigins[i].getVector()[2]));
					hRotatedPositions.add(hPos3dNew);
					
				}
				
				hydrophonePos.add(hRotatedPositions);
			}
					
		}



}
