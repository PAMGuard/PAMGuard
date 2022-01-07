package clipgenerator;

import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import clipgenerator.ClipProcess.ClipBlockProcess;

public class StandardClipBudgetMaker {

	private ClipBlockProcess clipBlockProcess;
	
	private long budgetPeriod;

	private long budgetPeriodStart, budgetPeriodEnd;
	
	private long nStoredClips;
	
	private long totalStoredSize;
	
	private long budgetSize;
	
	private float sampleRate;
	
	private long averageClipSize = 48000;

	private boolean useBudget;

	/**
	 * @param clipBlockProcess
	 */
	public StandardClipBudgetMaker(ClipBlockProcess clipBlockProcess) {
		super();
		this.clipBlockProcess = clipBlockProcess;
	}
	
	/**
	 * Initialise the budget maker. <br>
	 * This gets called before acquisition starts. <br> 
	 * It may involve a quick whiz through the 
	 * existing data output folder to see how much data has been stored to date
	 * and what the size of currently stored files is. 
	 */
	public void initialise(long timeMillis) {
		sampleRate = clipBlockProcess.clipProcess.getSampleRate();
	}
	
	/**
	 * Should store a data unit. <br>
	 * Decision based on remaining budget, data rate, etc.  
	 * @param dataUnit data unit to consider
	 * @return true if the data unit should be stored, false otherwise. 
	 */
	public boolean shouldStore(PamDataUnit dataUnit) {
		if (dataUnit.getTimeMilliseconds() >= budgetPeriodEnd) {
			initialiseBudgetPeriod(dataUnit.getTimeMilliseconds());
		}
		if (!useBudget) {
			return true;
		}
		
		double p = getStorageProbability(dataUnit);
		boolean s = p > Math.random();
		if (s) {
			nStoredClips ++;
			totalStoredSize += getClipSize(dataUnit);
		}
		return s;
	}
	
	/**
	 * Calculate a probability for storing the next unit. 
	 * @param dataUnit
	 * @return a probability between 0 (don't store) and 1 (definitely store). 
	 */
	public double getStorageProbability(PamDataUnit dataUnit) {
		double usedTimeFraction = (double)(dataUnit.getTimeMilliseconds()-budgetPeriodStart) / (double) budgetPeriod;
		double usedDataFraction = (double) totalStoredSize / budgetSize;
		if (totalStoredSize == 0) {
			return 1;
		}
		if (usedDataFraction >= 1) {
			return 0;
		}
		double prob = usedTimeFraction / usedDataFraction;
		return prob;
	}
	
	private void initialiseBudgetPeriod(long timeMilliseconds) {
		useBudget = clipBlockProcess.clipGenSetting.useDataBudget;
		budgetPeriod = (long) (clipBlockProcess.clipGenSetting.budgetPeriodHours * 3600. * 1000.);
		budgetPeriod = Math.max(budgetPeriod, 1000);
		budgetPeriodStart = timeMilliseconds / budgetPeriod;
		budgetPeriodStart *= budgetPeriod;
		budgetPeriodEnd = budgetPeriodStart + budgetPeriod;
		
		if (nStoredClips > 0) {
			averageClipSize = totalStoredSize / nStoredClips;
		}
		nStoredClips = 0;
		totalStoredSize = 0;
		budgetSize = clipBlockProcess.clipGenSetting.dataBudget * 1024;
	}

	/**
	 * Get the estimated size of a data unit in bytes. 
	 * @param dataUnit
	 * @return estimated size in bytes. 
	 */
	private int getClipSize(PamDataUnit dataUnit) {
		ClipGenSetting cgs = clipBlockProcess.clipGenSetting;
		if (dataUnit.getSampleDuration() == null) {
			return 0;
//			double secs =  (double) dataUnit.getDurationInMilliseconds() / 1000 + (cgs.preSeconds+cgs.postSeconds);
//			return (int) (secs * sampleRate);
		}
		int nSamples = (int) (dataUnit.getSampleDuration() +  sampleRate * (cgs.preSeconds+cgs.postSeconds));
		int nChan = clipBlockProcess.decideChannelMap(dataUnit.getChannelBitmap());
		nChan = PamUtils.getNumChannels(nChan);
		return 44 + 2 * nSamples * nChan; // 44 bytes for a standard wav file header. assume 16 bit data. 
	}
	
}
