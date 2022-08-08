package clickTrainDetector.clickTrainAlgorithms.mht;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;

/**
 * Handles removing garbage tracks from an MHT algorithm's probability mix. 
 * 
 * @author Jamie Macaulay
 *
 */
public class MHTGarbageBot {
	
	/**
	 * The minimum trim count is the minimum number of trailing zeros (i.e. all
	 * tracks done) before a trim of the possibility matrix is performed. A trim
	 * involved a full array copy of time, IDI and data unit measurements and so is
	 * not worth it to save memory to remove just a small portion of the array,
	 */
	private static final int MIN_TRIM_COUNT = 100;
	
	/**
	 * Defines how many data units to leave in between tests to check whether probability
	 * matrix needs trimmed. 
	 */
	private static final int GARBAGE_COUNT_N_TEST=20;
	
	
	
	/**
	 * This is the maximum number of detections long the click train possibility
	 * tree can be. Hopefully within this number of detections the click train will
	 * have reset itself due to a gap in the data/ and or self trimmed.
	 */
	public static int DETECTION_HARD_LIMIT = 10000;

	private MHTClickTrainAlgorithm mhtClickTrainAlgorithm; 
	
	public MHTGarbageBot() {
		
	}
	
	public MHTGarbageBot(MHTClickTrainAlgorithm mhtClickTrainAlgorithm){
		this.mhtClickTrainAlgorithm=mhtClickTrainAlgorithm; 
	}
	
	
	int onceCount = 0; 
	
	
	/**
	 * Checks whether an attempt for a garbage collection of click trains is
	 * necessary. If there are simply too many clicks (DETECTION_HARD_LIMIT) then
	 * the whole algorithm is reset. Every garbCountNTest clicks the algorithm will
	 * check the possibility matrix in the kernel and see of there is empty space at
	 * the start. If there is then the possibility matrix is trimmed to remove this
	 * section. As long as a single click train never reaches DETECTION_HARD_LIMIT
	 * then the possibility matrix will simply keep resizing and click train
	 * detector will run indefinitely.
	 * 
	 * @param dataUnit - the new data unit
	 * @param mhtKernel - the current MHT Kernel. 
	 * @return - true if a garbage collection task was executed. 
	 */
	public boolean checkCTGarbageCollect(PamDataUnit dataUnit, MHTKernel<PamDataUnit> mhtKernel) {
				
		double iciPrev = getLastICI(dataUnit, mhtKernel.getLastDataUnit());
		
		double maxICI = mhtKernel.getMHTParams().maxCoast * mhtKernel.getMHTChi2Provider().getChi2Params().maxICI; 
		
		//check the current set of click train possible ICI's 
		
		//Debug.out.println("MHTGARBAGEBOT: maxICI " + maxICI + "  " + iciPrev); 
		
		//we have reached the hard limit. Save click trains, wipe the detector and start again. 
		if (mhtKernel.getKCount()>mhtKernel.getMHTParams().nPruneBackStart && (iciPrev>maxICI || mhtKernel.getKCount()>DETECTION_HARD_LIMIT)) {
			
			//Debug.out.println("MHTGARBAGEBOT: KERNEL HARD LIMIT"); 
			//check whether the next click has a gap so big that all click trains should be restarted 			
			//grab tracks
			mhtKernel.confirmRemainingTracks();
			if (mhtClickTrainAlgorithm!=null) {
				mhtClickTrainAlgorithm.grabDoneTrains(mhtKernel); 
			}
			//reset the kernel; 
			mhtKernel.clearKernel();
			return true; 
		}
				
		//check if there are a bunch of trailing zeros in the possibility mix that can be deleted. If so, delete. 
		if (mhtKernel.getKCount()!=0 && mhtKernel.getKCount()%GARBAGE_COUNT_N_TEST==0 
				&& mhtKernel.getKCount() > mhtKernel.getMHTParams().nPruneBackStart) {
			//do a full check to make sure we don't have a bunch of dead space in the possibility 
			//matrix. 
			int newRefIndex = mhtKernel.getFirstDetectionIndex();
			
			//System.out.println("New ref index: " + newRefIndex); 
			
			if (newRefIndex==mhtKernel.getKCount()) {
//				Debug.out.println("MHTGarbageBot: CLEAR TRIALLING WHOLE KERNEL: "+ newRefIndex +  " " + PamCalendar.formatDateTime(dataUnit.getTimeMilliseconds())); 
				mhtKernel.clearKernel(); //reset the kernel;
				return true; 
			}
			
			
			//Note the new ref index can be greater than the kcount if all possibilities are zero. 
			//it then counts to max value of an integer. 
			if (newRefIndex>MIN_TRIM_COUNT && newRefIndex<=mhtKernel.getKCount()) {
				
//				Debug.out.println("CLEAR TRIALLING ZEROS: newRefIndex: " + newRefIndex + " Kcount: " 
//						+ mhtKernel.getKCount() + " possibilities: " +mhtKernel.getPossibleTracks().size() + 
//						" " + PamCalendar.formatDateTime(dataUnit.getTimeMilliseconds()) + " DataUnit: UID: " + dataUnit.getUID()) ;
								
//				//must grab any done trains or they will get deleted. 
//				if (mhtClickTrainAlgorithm!=null) {
//					mhtClickTrainAlgorithm.grabDoneTrains(mhtKernel); 
//				}
				mhtKernel.clearKernelGarbage(newRefIndex);
//				mhtKernel.debugFlag=1; 
								
//				Debug.out.println("MHTAlgorithm: Clear trailing zeros: newRefIndex Kcount: " 
//						+ mhtKernel.getKCount() + " possibilities: " +mhtKernel.getPossibleTracks().size());
				return true; 
			}
		}
		
		return false; 
	}
	
	
	/**
	 * Check whether a garbage collection of the hypothesis matrix is required based on current time rather than the current data unit. 
	 * @param currentTimemillis - the current time in millis
	 * @param mhtKernel - the MHT kernel
	 * @return true if a garbage collect is required. 
	 */
	public boolean checkCTGarbageCollect(long currentTimemillis, MHTKernel<PamDataUnit> mhtKernel) {
		
		if (mhtKernel.getLastDataUnit()==null) return false; 
		
		if ((currentTimemillis - mhtKernel.getLastDataUnit().getTimeMilliseconds())
				>mhtKernel.getMHTParams().maxCoast*mhtKernel.getMHTChi2Provider().getChi2Params().maxICI*1000.) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Get the interval between the new unit and the last unit. 
	 * @param dataUnit - get the data units. 
	 * @param mhtAlgorithm  - the MHTAlgorithm
	 * @return the ICI in seconds 
	 */
	private double getLastICI(PamDataUnit dataUnit, PamDataUnit lastDataUnit) {
		if (lastDataUnit!=null) {
			//			System.out.println(dataUnit.getTimeMilliseconds() + " " + 
			//					mhtAlgorithm.lastDataUnit.getTimeMilliseconds());
			return ((double) (dataUnit.getTimeMilliseconds()
					- lastDataUnit.getTimeMilliseconds()))/1000.;
		}
		else return 0;
	}

}
