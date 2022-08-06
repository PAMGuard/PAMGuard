package clickTrainDetector.clickTrainAlgorithms.mht;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;

import PamUtils.PamArrayUtils;
import PamguardMVC.debug.Debug;

/**
 * Core functions for a multi-hypothesis tracking (MHT) algorithm which groups
 * data based on a minimisation function.
 * <p>
 * The MHT algorithm is passed data and keeps a record of all possible ways to
 * group the data, creating a tree like structure in memory. As the tree becomes
 * larger (and risks taking up too much memory), the branches of the tree are
 * "pruned" based on a minimisation function. For example the minimisation
 * function may favour slowly changing amplitude or bearing.
 * <p>
 * The algorithm can hold multiple groups at the same time. Once a group reaches
 * a pre-defined size then, on completion, it is saved.
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public class MHTKernel<T> {

	public static int verbosity=0; 


	/**
	 * Array which holds a reference to to all data units. 
	 * Although the array only holds a reference any data unit in the array will
	 * not be deleted when removed form the datablock. So be careful here...
	 */
	private ArrayList<T> dataUnits;

	/**
	 * The current probability matrix which holds an array where the row is each sequential click or detection
	 * and the columns represent one click train path. 
	 */
	private ArrayList<TrackBitSet> possibleTracks;


	/**
	 * Synchronisation object for possibleTracks.
	 */
	private Object trackSynchronisation = new Object();

	/**
	 * Function which calculate the chi2 value of each probability branch. 	`
	 */
	private MHTChi2Provider<T> mhtChi2Provider; 

	/**
	 * The reference data unit is the first data object in the possibleTracks 
	 * matrix. This onyl changes when the initial section of the probability 
	 * matrix is cleaned up. 
	 */
	private T referenceDataUnit; 

	/**
	 * Parameters for the algorithm. 
	 */
	private MHTKernelParams mHTParams = new MHTKernelParams();

	/**
	 * The total number of added detections to possibility matrix
	 */
	private int kcount=0; 

	/**
	 * List of the currently confirmed tracks i.e. what the algorithm has decided is a click train. 
	 * This list will grow until cleared by the user so a potential memory leak. 
	 */
	private ArrayList<TrackBitSet> confirmedTracks;

	/**
	 * A list of the active in ascending chi^2 order. Active tracks are newPossibleTracks 
	 * with a pruneback. Usually only used for plotting. 
	 */
	private ArrayList<TrackBitSet> activeTracks;

	//	/**
	//	 * ArrayList of MHT variables which can contribute to the chi^2 value. 
	//	 */
	//	private ArrayList<MHTChi2Var<PamDataUnit>> mhtChi2Vars;

	/**
	 * Record of the time of the chi^2 calculation 
	 */
	private long timeChi2 = 0;

	/**
	 * Record the time of growing the matrix array. 
	 */
	private long timeGrow = 0;

	/**
	 * Used for print statements and debugging. 
	 */
	int debugFlag = 0;

	/**
	 * Constructor for the MHT kernel. This handles the data. 
	 */
	public MHTKernel(MHTChi2Provider<T> mhtChi2) {
		this.mhtChi2Provider=mhtChi2; 
		confirmedTracks=new ArrayList<TrackBitSet>(); 
	}	

	/**
	 * Clear up the kernel if starting a new run. 
	 */
	public void clearKernel() {
		//Debug.out.println("MHTKernel: Clear Kernel Garbage");
		synchronized (trackSynchronisation) {
			possibleTracks=null;
			activeTracks=null;
		}
		referenceDataUnit=null; 
		activeTracks=null;
		confirmedTracks=null;
		dataUnits = null; //garbage collector will delete all data units referenced in list and nowhere else.
		mhtChi2Provider.clear(); 
		kcount=0; 
	}

	/**
	 * Add a new data into the possibility mix. Note that data units should
	 * be sequentially in chronological order. 
	 * @param detection - the detection to add. 
	 */
	public void addDetection(T detection) {

		//instantiate array list if new run
		if (referenceDataUnit==null) {
			referenceDataUnit=detection; 
			dataUnits = new ArrayList<T>();
		}

		dataUnits.add(detection); //keep a reference to the data units. 

		//the total detection count.
		kcount++;

		//update the chi2 provider. 
		this.mhtChi2Provider.addDetection(detection, kcount);

		synchronized (trackSynchronisation) {

			//grow the probability matrix.
			long time1=System.currentTimeMillis();
			growProbMatrix(detection);
			long time2=System.currentTimeMillis();

			if (verbosity>0) {
				Debug.out.println("Possiblity matrix size is " + possibleTracks.size() + " x " +  kcount );
			}
//					for (int i=0; i<possibleTracks.size(); i++) {
//						System.out.println("Pos " + i +  " chi^2 "+ possibleTracks.get(i).chi2Track.getChi2() + 
//								"  " +  MHTKernel.bitSetString(possibleTracks.get(i).trackBitSet,kcount));
//					}

			//prune the probability matrix.
			pruneProbMatrix(false);

			long time3=System.currentTimeMillis();

			if (verbosity>0) {
				Debug.out.println("TimeTaken: growProb: " + (time2-time1) + 
						" (chi2: " + this.timeChi2 + " growMatrix: " + this.timeGrow   + ") pruneProbMatrix: " + (time3-time2)); 
			}

			if (verbosity>1 && this.kcount>this.mHTParams.nPruneBackStart) printMHTKernalData(); 
		}
	}


	/**
	 * Prints the confirmed tracks. 
	 */
	public void printConfirmedTracks() {
		System.out.println("/******MHT Kernal Confired Tracks*******/");
		System.out.println("Number of confirmed tracks is " + confirmedTracks.size());
		if (this.confirmedTracks!=null) {
			for (int i=0; i<this.confirmedTracks.size(); i++) {
				System.out.println("	-----------");
				System.out.println("	CONFIRMED TRACK BITSET: " + 
						MHTKernel.bitSetString(confirmedTracks.get(i).trackBitSet, kcount-this.mHTParams.nPruneback)); 
				System.out.println("	CONFIRMED TRACK Chi2: " +confirmedTracks.get(i).chi2Track); 
			}
		}
		System.out.println("/***************************************/"); 

	}



	/**
	 * Prints the resulting data in the MHT Kernel 
	 */
	public void printMHTKernalData() {
		printMHTKernalData(Integer.MAX_VALUE); 
	}


	/**
	 * Prints the resulting data in the MHT Kernel 
	 * @param maxTracks - the maximum number of tracks to print. 
	 */
	public void printMHTKernalData(int maxTracks) {
		System.out.println("/******MHT Kernal Data*******/");
		System.out.println("Possiblity matrix size after prune " + possibleTracks.size() + " x " +  kcount );
		if (confirmedTracks!=null) {
			System.out.println("Number of confirmed tracks is " + confirmedTracks.size());
		}

		//print active tracks 
		if (this.activeTracks!=null) {
			for (int i=0; i<Math.min(this.activeTracks.size(), maxTracks); i++) {
				System.out.println("	-----------");
				System.out.println("	ACTIVE TRACK BITSET: " + 
						MHTKernel.bitSetString(activeTracks.get(i).trackBitSet, kcount-this.mHTParams.nPruneback)); 
				System.out.println("	ACTIVE TRACK Chi2: " + String.format("%.3f 		", activeTracks.get(i).chi2Track.getChi2())); 
			}
		}

		//print possible tracks after the prune
		if (this.possibleTracks!=null && this.possibleTracks.size()>0) {

			System.out.println("----------------------");
			System.out.println("	POSS TRACKS AFTER PRUNE: Chi2: ");
			System.out.println("	");
			for (int i=0; i<Math.min(this.possibleTracks.size(), maxTracks) ; i++) {
				System.out.println(i + " "+ String.format("%.3f 		", possibleTracks.get(i).chi2Track.getChi2())+"  "  + String.format("%d 	", possibleTracks.get(i).chi2Track.getNCoasts())
				+ MHTKernel.bitSetString(possibleTracks.get(i).trackBitSet, kcount));
			}
		}
	}


	public void printMHTKernalData2(int maxTracks) {
		if (this.possibleTracks!=null && this.possibleTracks.size()>0) {

			System.out.println("----------------------");
			System.out.println("	POSS TRACKS AFTER PRUNE: Chi2: ");
			System.out.println("	");
			for (int i=0; i<Math.min(this.possibleTracks.size(), maxTracks) ; i++) {
				System.out.println(i + " "+ String.format("%.3f 		", possibleTracks.get(i).chi2Track.getChi2())+"  N: " 
						+ MHTKernel.getTrueBitCount(possibleTracks.get(i).trackBitSet, kcount));
			}
		}
	}


	/**
	 * Grow the probability matrix by one data unit. This will over
	 * double the size of the matrix. 
	 * @param confirmAll - true to confirm all tracks up to nhold. Used to complete click train process 
	 * once last data unit has been added.,
	 */
	@SuppressWarnings("unchecked")
	private void growProbMatrix(T detection) {
		//if the tracks are null then initialise the matrix and return. 

		long time1 = System.currentTimeMillis(); 
		ArrayList<TrackBitSet> newPossibilities = new ArrayList<TrackBitSet>();

		if (possibleTracks==null) {
			//the click can either have a possibility or not. 
			TrackBitSet bitSet1 = new TrackBitSet(1, mhtChi2Provider.newMHTChi2(null)); 
			bitSet1.trackBitSet.set(0, true);
			TrackBitSet bitSet2 = new TrackBitSet(1, mhtChi2Provider.newMHTChi2(null)); 
			bitSet2.trackBitSet.set(0, false); 

			newPossibilities.add(bitSet1);
			newPossibilities.add(bitSet2);
		}

		else {
			//now the array has to grow. All possibilities must now be branched to include 
			//and exclude the new data unit. 
			BitSet currentBitSet;
			MHTChi2<T> mhtChi2;
			int index; 
			for (int i=0; i<possibleTracks.size(); i++) {

				currentBitSet=possibleTracks.get(i).trackBitSet;

				//index is the total detection count-1; 
				index=kcount-1; 

				//now add both a true and false for the data unit to be in this possibility. 
				currentBitSet.set(index, true);
				mhtChi2=possibleTracks.get(i).chi2Track.cloneMHTChi2(); 

				newPossibilities.add(new TrackBitSet(currentBitSet, mhtChi2)); 

				//add a coast to the possibility
				currentBitSet=(BitSet) currentBitSet.clone(); 
				currentBitSet.set(index, false);
				//currentBitSet.set(currentBitSet.size(), true);
				//add new chi2 value -  need to clone this time. 
				mhtChi2=possibleTracks.get(i).chi2Track.cloneMHTChi2(); 

				//added the cloned bitset to not mess up references 
				newPossibilities.add(new TrackBitSet(currentBitSet, mhtChi2)); 
			}
		}

		long time2 = System.currentTimeMillis(); 


		//now update all the chi2 values. 
		for (int i=0; i<newPossibilities.size(); i++) {
			newPossibilities.get(i).chi2Track.update(detection, newPossibilities.get(i), this.kcount); 
		}

		long time3 = System.currentTimeMillis(); 

		//keep track of some times
		this.timeGrow = time2-time1; 
		this.timeChi2= time3-time2; 

		//now set new variables 
		this.possibleTracks=newPossibilities; 
	}

	/**
	 * Prune the matrix depending on the chi2 function
	 */
	@SuppressWarnings("unchecked")
	private void pruneProbMatrix(boolean confirmAll) {

		if (this.kcount>this.mHTParams.nPruneBackStart || confirmAll) {

			//long time1=System.currentTimeMillis();
			ArrayList<TrackBitSet> newPossibleTracks=possibleTracks;

			//first sort the tracks by increasing chi2 values. 
			//sort the possible tracks by chi2 values
			//now sort the chi2 values so they correspond to the track list.  
			Collections.sort(newPossibleTracks, (left, right)->{
				//Note- this is definitely in the correct order
				return Double.compare(left.chi2Track.getChi2(), right.chi2Track.getChi2());
			});		

			//			for (int i=0; i<newPossibleTracks.size(); i++) {
			//				System.out.print("Possibility chi2: " + i +  "  " + String.format("%.3f", newPossibleTracks.get(i).chi2Track.getChi2()));
			//				System.out.println("   " + MHTKernel.bitSetString(newPossibleTracks.get(i).trackBitSet, kcount));
			//				System.out.println("Chi2 info: " + newPossibleTracks.get(i).chi2Track.getMHTChi2Info().getInfoString());
			//				System.out.println("********");
			//			}

			//set up an array for pruned tracks. These are the tracks which 
			//have survived the prune. 
			ArrayList<TrackBitSet> possibleTracksPrune= new ArrayList<TrackBitSet>(); 

			//set up an array for confirmed tracks i.e. tracks whihc have been 
			//saved but are no longer in possibility matrix. 
			ArrayList<TrackBitSet> confirmedTracks= new ArrayList<TrackBitSet>(); 

			//set up an array of active tracks. i.e. branches which have not been confirmed 
			//but are active. these will be n pruneback less in length compared to the 
			//new possibilities. Usually used for plotting. 
			ArrayList<TrackBitSet> activeTracks= new ArrayList<TrackBitSet>(); 


			//now find nHold branches after pruning back by nPruneBack. 
			TrackBitSet testBranch; //the branch being tested
			BitSet testBitSet; 
			TrackBitSet currentBranch; //the current branch being kept in the prune back
			BitSet currentBitSet; 

			boolean[] indexConfirm; //list of branches to keep from possibility matrix
			boolean[] indexRemove; //list of branches to get rid of from probability matrix. 

			//define prune back to use. 
			int pruneback=mHTParams.nPruneback; 

			if (confirmAll) pruneback=0; //pruneback is zero because we have to extract the current active branches to last detection

			//long time2=System.currentTimeMillis();

			for (int i=0; i<mHTParams.nHold; i++) {

				//System.out.println("New possible tracks size: " +newPossibleTracks.size() + "  i: "+i);

				//long time1a=System.currentTimeMillis();

				//all possibilities have been removed. 
				if (newPossibleTracks.size()==0) {
					break;
				}

				//the branch to test, starting at lowest chi2 value. 
				currentBranch=newPossibleTracks.get(0); 

				//remove part of test branch which will not be used. 
				currentBitSet=currentBranch.trackBitSet.get(0, kcount-(pruneback));

				//System.out.println("The bitset to test: " + printBitSet(currentBitSet));

				indexConfirm= new boolean[newPossibleTracks.size()]; 
				indexRemove= new boolean[newPossibleTracks.size()]; 

				//test the testBranch against all other branches. 
				for (int j=0; j<newPossibleTracks.size(); j++) {
					//test whether branch is the same 
					testBranch = newPossibleTracks.get(j); 
					testBitSet=testBranch.trackBitSet.get(0, kcount-(pruneback));

					
					//now test whether the current and test branch are the same. 
					if (testBitSet.equals(currentBitSet)) {
						indexConfirm[j]=true; 
					}
					else {
						//if the branch is not equal check whether it has any detections 
						//in it which are the same as the the detections in currentBranch
						if (testBitSet.intersects(currentBitSet)) {
							indexRemove[j]=true; 	
						}						
					}
					
				}

				//long time2a=System.currentTimeMillis();

				//now need to remove confirmed tracks, add still going tracks and remove 
				//any tracks with duplicate values. 

				//check the number of coasts for the new branch. 
				int nCoasts=currentBranch.chi2Track.getNCoasts();


				if (nCoasts>=this.mHTParams.maxCoast || confirmAll || currentBranch.flag==TrackBitSet.JUNK_TRACK) {
					//the branch needs to be confirmed. 

//					System.out.println(i + " DONE: " + (nCoasts >= this.mHTParams.maxCoast) + "  " + confirmAll + "  "
//							+ (currentBranch.flag == TrackBitSet.JUNK_TRACK) + " "
//							+ String.format("%.3f 		", currentBranch.chi2Track.getChi2()) + "  "
//							+ String.format("%d 	", currentBranch.chi2Track.getNCoasts())
//							+ MHTKernel.bitSetString(currentBranch.trackBitSet, kcount));

					/**
					 * 27/02/2020
					 *  The track has only been tested up to it's pruneback. The rest of the clicks may
					 *  be correct or may be junk. Crucially though, they may be part of other tracks. There 
					 *  are two choices here; take the complete track removing all the clicks past the pruneback
					 *  from the possibility mix or only include clicks up to the pruneback. Turns out the latter
					 *  choice works a lot better because you do not remove clicks from other tracks. 
					 */
					/**
					 * 2/06/2021
					 * The above statement is not correct. The chi2 for the track is indeed tested up to it's last chi2. The chi2 though 
					 * included the new possible clicks which may be junk. 
					 */
					//if confirm all then we want to grab the last track as this will not have any effect on limiting other
					//track possibilities. 
					if (!confirmAll) currentBranch.trackBitSet=currentBranch.trackBitSet.get(0, kcount-1);

					//add confirmed track
					confirmedTracks.add(currentBranch); 
					
					////					//if a branch is confirmed then all the tracks which include it's clicks must also be removed. 
					////					//test the testBranch against all other branches. TODO - Is this the most efficient code -could add if statements to above loop?
					for (int j=0; j<newPossibleTracks.size(); j++) {
						//test whether branch is the same 
						testBranch = newPossibleTracks.get(j); 
						testBitSet=testBranch.trackBitSet; 
						if (testBitSet.intersects(currentBranch.trackBitSet)) {
							indexRemove[j]=true; 	
						}					
					}
					
				}
				else {
					//save as an active track. 
					activeTracks.add(new TrackBitSet(currentBitSet,currentBranch.chi2Track.cloneMHTChi2())); 

					//add any possibilities to a new array. 
					for (int j=0; j<indexConfirm.length; j++) {
						if (indexConfirm[j]) {
							possibleTracksPrune.add(newPossibleTracks.get(j)); 
						}
					}
				}

				//long time3a=System.currentTimeMillis();

				//now remove both the confirmed tracks and the tracks which are 
				//need to be removed because they contain the same possibility as
				//the new possible tracks
				ArrayList<TrackBitSet> newNewPossibleTracks=new ArrayList<TrackBitSet>(); 

				for (int j=0; j<newPossibleTracks.size(); j++) {
					if (!indexRemove[j] && !indexConfirm[j]) {
						newNewPossibleTracks.add(newPossibleTracks.get(j)); 
					}
				}
				//long time4a=System.currentTimeMillis();

				//remove all pruned and remove branches from the array 
				//newPossibleTracks.removeAll(toRemove); // <- this is a bit slow. 
				newPossibleTracks = newNewPossibleTracks; 

				//long time5a=System.currentTimeMillis();

				//System.out.println(" --> + time54a: " + (time5a-time4a) + " time43a " +(time4a-time3a) + " time32a: " + (time3a-time2a)+" time21a: " + (time2a-time1a) );

				//now loops back round for another possibility
			}

			//long time3=System.currentTimeMillis();

			//reset possible tracks
			this.possibleTracks=possibleTracksPrune; 
			this.activeTracks=activeTracks; 

			//now for good measure add a track which is ALL coasts. i.e. has no 
			//current detection if one does not exist already. 
			TrackBitSet coasts=new TrackBitSet(kcount, this.mhtChi2Provider.newMHTChi2(null) );
			boolean add=true; 
			@SuppressWarnings("unused")
			int countC=0; 
			for (int j=0; j<possibleTracks.size(); j++) {
				//count backwards for efficiency (because zero tracks are usually at the end)
				if (possibleTracks.get(possibleTracks.size()-j-1).trackBitSet.equals(coasts.trackBitSet)) {
					add=false; 
					countC++; 
					//break; //no need to add. 
				}
			}
			if (add) this.possibleTracks.add(coasts); 

			//add confirmed tracks to array 
			if (this.confirmedTracks==null) this.confirmedTracks= new ArrayList<TrackBitSet>(); 
			this.confirmedTracks.addAll(confirmedTracks); 

			//for debugging. 
			if (this.debugFlag>0) {
				//printMHTKernalData2(Integer.MAX_VALUE); 
				debugFlag--; 
			}

			//long time4 = System.currentTimeMillis();

			//System.out.println("Time32: " +(time3-time2) + " time21: " + (time2-time1)+" time43: " + (time4-time3));

		}
	}


	/**
	 * Get the chi^2 provider. This handles chi^2 calculations for possible tracks. . 
	 * @return the mnhtchi2
	 */
	public MHTChi2Provider<?> getMHTChi2Provider() {
		return mhtChi2Provider;
	}


	/**
	 * Set the chi2 class. 
	 * @param mhtChi2Provider the chi^2 provider to set
	 */
	public void setMnhtchi2(MHTChi2Provider<T> mhtChi2Provider) {
		this.mhtChi2Provider = mhtChi2Provider;
	}

	/**
	 * @return the mHTParams
	 */
	public MHTKernelParams getMHTParams() {
		return mHTParams;
	}


	/**
	 * @param mHTParams the mHTParams to set
	 */
	public void setMHTParams(MHTKernelParams mHTParams) {
		this.mHTParams = mHTParams;
	}


	/**
	 * Print bit set. 
	 * @param bitSet - the bit set.
	 * @return the string builder representation 
	 */
	public static StringBuilder bitSetString(BitSet bitSet, int size) {
		StringBuilder s = new StringBuilder();
		for( int i = 0; i < Math.min(bitSet.size(), size);  i++ )
		{
			s.append( bitSet.get( i ) == true ? 1: 0 );
		}
		return s; 
	}

	/**
	 * Get the  number of confirmed tracks. 
	 * These are tracks which have been designated as done
	 * by the algorithm. 
	 * @return the number of confirmed tracks. 
	 */
	public int getNConfrimedTracks() {
		if (confirmedTracks==null) return 0; 
		return confirmedTracks.size();
	}

	/**
	 * Clear the confirmed tracks from memory. 
	 */
	public void clearConfirmedTracks() {
		if (confirmedTracks==null) return; 
		confirmedTracks.clear(); 
	}

	/**
	 * Get the confirmed track at the specified index.
	 * @param i - the index.
	 * @return the TrackBitSet at index i. 
	 */
	public TrackBitSet getConfirmedTrack(int i) {
		if (confirmedTracks==null || i>=confirmedTracks.size()) {
			Debug.err.println("Confirmed tracks was null??");
			return null; 
		}
		return confirmedTracks.get(i);
	}

	/**
	 * Confirm all remaining tracks and add to confirm list. This is usually
	 * called once the last data unit has been added. 
	 */
	public void confirmRemainingTracks() {
		if (possibleTracks != null && possibleTracks.size()>0) {
			//kcount=kcount-1; 
			this.pruneProbMatrix(true);
		}
	}

	/**
	 * Get a list of the active in ascending chi^2 order. Active tracks are newPossibleTracks 
	 * with a prune back and duplicates removed. Usually only used for plotting. 
	 * @return a list of active tracks. 
	 */
	public ArrayList<TrackBitSet> getActiveTracks() {
		return activeTracks;
	}


	/**
	 * Get the reference data unit. This is unit 0 for all current confirmed tracks and
	 * track possibilities. 
	 * @return the reference data unit. 
	 */
	public T getReferenceUnit() {
		return this.referenceDataUnit;
	}

	/**
	 * Get the kcount. This is the total number of data units which have so far been
	 * added to the kernel. 
	 * @return the kcount. 
	 */
	public int getKCount() {
		return this.kcount;
	}

	/**
	 * Get possible tracks. These are all possible tracks which are in the current 
	 * possibility mix up to the last recieved data unit. 
	 */
	public ArrayList<TrackBitSet> getPossibleTracks() {
		return possibleTracks;
	}

	/**
	 * 
	 * Set a new reference index and junk all data before that index. This can be
	 * useful for long data sets to save memory once all click trains in preceding
	 * data units have been detecteded. Note that the function deletes the currently
	 * confirmed tracks. These should be extracted beforehand;
	 * 
	 * @param newRefIndex
	 *            - the new reference index. This is the index of refUnit
	 * @param newRefUnit
	 *            - the new reference data unit.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void clearKernelGarbage(int newRefIndex) {
		if (newRefIndex==0) return; //nothing to do.

		synchronized (trackSynchronisation) {

			//		//Debug.out.println("MHTKernel: Clear Kernel Garbage");
			//				synchronized (trackSynchronisation) {
			//					possibleTracks=null;
			//					activeTracks=null;
			//				}
			//				referenceDataUnit=null; 
			//				activeTracks=null;
			//				confirmedTracks=null;
			//				dataUnits = null; //garbage collector will delete all data units referenced in list and nowhere else.
			//				mhtChi2Provider.clear(); 
			//				kcount=0; 


			this.confirmedTracks=null; 

			//important to reference from the newest index. 
			this.referenceDataUnit=dataUnits.get(newRefIndex);

			//			System.out.println("Before Trim: MHTKernel: kcount" + kcount + " dataunits: " +  dataUnits.size() +  " ref: " +referenceDataUnit + " first inx: " + this.getFirstDetectionIndex()); 

			//trim the arraylist
			dataUnits = new ArrayList<T>(dataUnits.subList(newRefIndex, kcount));

			//trim all the track possibilities. 
			ArrayList<TrackBitSet> newPossibilities = new ArrayList<TrackBitSet>();

			for (int i=0; i<this.possibleTracks.size(); i++) {
				//trim the track  possibility.
				newPossibilities.add(new TrackBitSet(possibleTracks.get(i).trackBitSet.get(newRefIndex, kcount), possibleTracks.get(i).chi2Track)); 


				//send a call to chi^2 calculators that the starting data unit has changed.
				newPossibilities.get(i).chi2Track.clearKernelGarbage(newRefIndex); 

			}
			this.possibleTracks=newPossibilities; 

			kcount=kcount-newRefIndex; //alter the kcount accordingly. 

			//send a call to the chi^2 provider that the starting data unit has changed. 
			this.mhtChi2Provider.clearKernelGarbage(newRefIndex); 

			//			System.out.println("After Trim: MHTKernel: kcount" + kcount + " dataunits: " +  dataUnits.size()+ " ref: "  + " first inx: " + this.getFirstDetectionIndex()); 

		}

	}


	/**
	 * Get the index from the current data unit which has the first non-zero detection. 
	 * Can be used in conjunction with {@code clearKernelGarbage(newIndex)} to help save 
	 * memory during long processing runs. 
	 * 06/10/2019
	 * 
	 * @return the first non zero detection 
	 */
	public int getFirstDetectionIndex() {
		int[] startZeroIndex=new int[possibleTracks.size()]; 
		for (int i=0; i<this.possibleTracks.size(); i++) {
			startZeroIndex[i]=findFirstNonZeroBit(possibleTracks.get(i).trackBitSet, kcount);
		}
		return PamArrayUtils.min(startZeroIndex); 
	}

	/**
	 * Find the first non zero. 
	 * @param bitset - the bitset to find non zero for. 
	 * @param kcount - number of bits to iterate through.
	 * @return the index of the first non zero. 
	 */
	public static int findFirstNonZeroBit(BitSet bitset, int kcount) {
		for (int i=0; i<kcount; i++) {
			if (bitset.get(i)){
				return i; 
			}
		}
		return kcount; 
	}

	/**
	 * Get the number of bits which are 1. 
	 * @param bitset - the bitset to test. 
	 * @param kcount - number of bits to iterate through.
	 * @return the number of positive bits in the bitset. 
	 */
	@Deprecated
	public static int getTrueBitCount(BitSet bitset, int kcount) {
		return bitset.cardinality(); //faster
		//		int count=0; 
		//		for (int i=0; i<kcount; i++) {
		//			if (bitset.get(i)){
		//				count++; 
		//			}
		//		}
		//		return count; 
	}

	/**
	 * Get the number of bits which are 1. 
	 * @param bitset - the bitset to test. 
	 * @param kcount - number of bits to iterate through.
	 * @return the number of positive bits in the bitset. 
	 */
	public static int getTrueBitCount(BitSet bitset) {
		return bitset.cardinality(); //faster
		//		int count=0; 
		//		for (int i=0; i<kcount; i++) {
		//			if (bitset.get(i)){
		//				count++; 
		//			}
		//		}
		//		return count; 
	}

	/**
	 * Convenience function get prune back value from params. The prune back is 
	 * the number of detections backwards the algorithm prune the branches for
	 * @return the prune back value. 
	 */
	public int getPruneBack() {
		return this.mHTParams.nPruneback;
	}

	/**
	 * Get a list of all data units added to the click train detector. 
	 * @return a list of all data units added to the click train detector. 
	 */
	public ArrayList<T> getDataUnits() {
		return dataUnits;
	}


	/**
	 * Get the number of track possibilities currently in the possibility mix. 
	 * As the algorithm runs this should initially increase and then stabilise. 
	 * @return the possibility matrix count. 
	 */
	public int getPossibilityCount() {
		if (possibleTracks==null) return 0; 
		return possibleTracks.size();
	}

	/**
	 * Get the last data unit added to the matrix. 
	 * @return the last data unit. 
	 */
	public T getLastDataUnit() {
		if (this.dataUnits==null) return null;
		return dataUnits.get(dataUnits.size()-1);
	}


}
