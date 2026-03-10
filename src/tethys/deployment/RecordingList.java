package tethys.deployment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;

import PamUtils.PamCalendar;
import pamMaths.STD;
import tethys.TethysControl;
import tethys.output.TethysExportParams;

/**
 * Information about periods of effort that might come from either the raw data recordings or 
 * an analysis of binary data maps. 
 * @author dg50
 *
 */
public class RecordingList implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private ArrayList<RecordingPeriod> effortPeriods = new ArrayList();
	
	private ArrayList<RecordingPeriod> compoundPeriods;
	
	/**
	 * Name / source of this list. 
	 */
	private String sourceName;

	/**
	 * @param sourceName
	 */
	public RecordingList(String sourceName) {
		this.sourceName = sourceName;
	}

	public RecordingList(String sourceName, ArrayList<RecordingPeriod> selectedDeployments) {
		this.sourceName = sourceName;
		this.effortPeriods = selectedDeployments;
	}

	/**
	 * Get the duration of the recording periods from start to end. 
	 * @return
	 */
	public long duration() {
		return getEnd()-getStart();
	}
	
	/**
	 * Get the start of the first in the list. 
	 * @return
	 */
	public long getStart() {
		if (effortPeriods.size() == 0) {
			return 0;
		}
		return effortPeriods.get(0).getRecordStart();
	}
	
	/**
	 * get the end of the last in the list. 
	 */
	public long getEnd() {
		if (effortPeriods.size() == 0) {
			return 0;
		}
		return effortPeriods.get(effortPeriods.size()-1).getRecordStop();
	}
	
	/**
	 * Sort the list in ascending order. 
	 */
	public void sort() {
		Collections.sort(effortPeriods, new Comparator<RecordingPeriod>() {

			@Override
			public int compare(RecordingPeriod o1, RecordingPeriod o2) {
				return (int) Math.signum(o1.getRecordStart()-o2.getRecordStart());
			}
		});
	}
	
	/**
	 * Get the coverage as a fraction. This is the sum of the individual periods divided 
	 * by the start to end times
	 * @return
	 */
	public double getCoverage() {
		long cov = 0;
		long durTot = 0;
		if (effortPeriods.size() == 0) {
			return 0;
		}
		Iterator<RecordingPeriod> it = effortPeriods.iterator();
		while (it.hasNext()) {
			RecordingPeriod rp = it.next();
			cov += rp.getDuration();
		}
		durTot = getEnd()-getStart();
		return (double) cov / (double) durTot;
	}
	
	/**
	 * Merge recording periods, with a max gap between periods in milliseconds. 
	 * @param maxGap
	 * @return the number of periods removed. 
	 */
	public int mergeRecordingPeriods(long maxGap) {
		if (effortPeriods.size() < 2) {
			return 0;
		}
		Iterator<RecordingPeriod> it = effortPeriods.iterator();
		RecordingPeriod prev = it.next();
		int removed = 0;
		while (it.hasNext()) {
			RecordingPeriod curr = it.next();
			if (curr.getRecordStart() - prev.getRecordStop() <= maxGap) {
				prev.setRecordStop(curr.getRecordStop());
				it.remove();
				removed++;
			}
			else {
				prev = curr;
			}
		}
		return removed;
	}
	
	/**
	 * Work out whether or not the data are evenly duty cycled by testing the
	 * distributions of on and off times.
	 * @param tempPeriods
	 * @return
	 */
	public DutyCycleInfo assessDutyCycle() {
		if (effortPeriods == null) {
			return null;
		}
		int n = effortPeriods.size();
		if (n < 2) {
			return new DutyCycleInfo(false, 0,0,n);
		}
		double[] ons = new double[n-1]; // ignore the last one since it may be artificially shortened which is OK
		double[] gaps = new double[n-1];
		for (int i = 0; i < n-1; i++) {
			ons[i] = effortPeriods.get(i).getDuration()/1000.;
			gaps[i] = (effortPeriods.get(i+1).getRecordStart()-effortPeriods.get(i).getRecordStop())/1000.;
		}
		/* now look at how consistent those values are
		 * But some data gets messed by small gaps, so want to 
		 * remove outliers and concentrate on say 80% of the data. 
		 */
		ons = getDistributionCentre(ons, 80);
		gaps = getDistributionCentre(gaps, 80);
		Arrays.sort(gaps);

		STD std = new STD();
		double onsMean = std.getMean(ons);
		double onsSTD = std.getSTD(ons);
		double gapsMean = std.getMean(gaps);
		double gapsSTD = std.getSTD(gaps);
		boolean dutyCycle = onsSTD/onsMean < .05 && gapsSTD/gapsMean < 0.05;
		DutyCycleInfo cycleInfo = new DutyCycleInfo(dutyCycle, onsMean, gapsMean, effortPeriods.size());
		return cycleInfo;
	}
	/**
	 * Get the central part of a distribution without any outliers so 
	 * that we can get a better assessment of duty cycle. 
	 * @param data unsorted distribution data. 
	 * @param percent percentage to include (half this removed from top and bottom)
	 * @return
	 */
	private double[] getDistributionCentre(double[] data, double percent) {
		if (data == null) {
			return null;
		}
		Arrays.sort(data);
		int nRem = (int) Math.round(data.length * (100-percent)/200);
		int newLen = data.length-nRem*2;
		double[] subdata = Arrays.copyOfRange(data, nRem, data.length-2*nRem);
		if (subdata.length < 2) {
			return data;
		}
		return subdata;
	}

	/**
	 * @return the sourceName
	 */
	public String getSourceName() {
		return sourceName;
	}

	@Override
	public String toString() {
		if (effortPeriods.size() == 0) {
			return "Empty recording list";
		}
		String str = String.format("%s: %s to %s, %3.1f%% coverage", getSourceName(),
				PamCalendar.formatDBDateTime(getStart()), 
				PamCalendar.formatDBDateTime(getEnd()), getCoverage()*100);
		return str;
	}
	
	/**
	 * Get similarity to another recording list. 1 = identical, 0 means not even overlapping.
	 * @param other other recording list. 
	 * @return measure of similarity. 
	 */
	public double getSimilarity(RecordingList other) {
		double sim1 = (double) other.duration() / (double) this.duration();
		if (sim1 > 1) {
			sim1 = 1./sim1;
		}
		long overlap = Math.min(other.getEnd(), this.getEnd()) - Math.max(other.getStart(), this.getStart());
		overlap = Math.max(0,  overlap);
		long longest = Math.max(other.duration(), this.duration());
		double sim2 = (double) overlap / (double) longest;
		
		return Math.min(sim1, sim2);
	}

	/**
	 * Add a recording period to the list. 
	 * @param recordingPeriod
	 */
	public void add(RecordingPeriod recordingPeriod) {
		effortPeriods.add(recordingPeriod);
	}

	/**
	 * Add a recording period to the list. 
	 * @param startTime
	 * @param endTime
	 */
	public void add(long startTime, long endTime) {
		add (new RecordingPeriod(startTime, endTime));
	}

	public int size() {
		return effortPeriods.size();
	}

	/**
	 * @return the effortPeriods
	 */
	public ArrayList<RecordingPeriod> getEffortPeriods() {
		return effortPeriods;
	}

	/**
	 * @return the compoundPeriods
	 */
	public ArrayList<RecordingPeriod> getCompoundPeriods(DeploymentExportOpts exportParams) {
		if (compoundPeriods == null) {
			compoundPeriods = makeCompoundPeriods(exportParams);
		}
		return compoundPeriods;
	}
	/**
	 * @return the compoundPeriods
	 */
	private ArrayList<RecordingPeriod> makeCompoundPeriods(DeploymentExportOpts exportParams) {
		/**
		 * What do we really want here ? 
		 * 
		 */
		
		ArrayList<RecordingPeriod> periods = new ArrayList<>();
		if (effortPeriods == null) {
			return null;
		}
		if (effortPeriods.size() < 2) {
			periods.addAll(effortPeriods);
			return periods;
		}
		// otherwise, work out what duty cycle info we want to use and return it. 
		boolean isOne = false;
		DutyCycleInfo ds = assessDutyCycle();
		switch (exportParams.sepDeployments) {
		case ALWAYSSEPARATE:
			return makeSeparatePeriods(exportParams);
		case ALWAYSSINGLE:
			return makeSinglePeriod(exportParams);
		case AUTOSCHEDULE:
			return makeAutoPeriods(exportParams);
		default:
			break;
		
		}
		
		
		return compoundPeriods;
	}

	/**
	 * Keep largely as it was, but only include items that exceeded the minimum and merge some
	 * very close ones addording to the rules. 
	 * @param exportParams
	 * @return
	 */
	private ArrayList<RecordingPeriod> makeSeparatePeriods(DeploymentExportOpts exportParams) {
		if (effortPeriods == null) {
			return null;
		}
		ArrayList<RecordingPeriod> periods = new ArrayList<>();
		RecordingPeriod last = null;
		for (RecordingPeriod p : effortPeriods) {
			if (last != null && (p.getRecordStart()-last.getRecordStop() < exportParams.maxRecordingGapSeconds * 1000)) {
				last.setRecordStop(p.getRecordStop()); // don't even bother to record that there is a gap. 
			}
			else {
				// move this period across, but clone it in case it get's extended in the next iteration. 
				periods.add(last = p.clone());
			}
		}
		/*
		 * Then remove the short ones - note that this is done AFTER short periods may have been joined together. 
		 */
		ListIterator<RecordingPeriod> it = periods.listIterator();
		while (it.hasNext()) {
			RecordingPeriod p = it.next();
			if (p.getDuration() < exportParams.minRecordingLengthSeconds * 1000) {
				it.remove();
			}			
		}
		
		return periods;
	}

	private ArrayList<RecordingPeriod> makeSinglePeriod(DeploymentExportOpts exportParams) {


		ArrayList<RecordingPeriod> periods = new ArrayList<>();
		if (effortPeriods == null || effortPeriods.size() < 1) {
			return null;
		}
		RecordingPeriod f = effortPeriods.get(0);
		RecordingPeriod l = effortPeriods.get(effortPeriods.size()-1);
		RecordingPeriod single = new RecordingPeriod(f.getRecordStart(), l.getRecordStop());
		periods.add(single);
		// then add the off effort periods to single
		ListIterator<RecordingPeriod> it = effortPeriods.listIterator();
		RecordingPeriod prev = it.next(); // should be ok, becuase there is always >= 1
		while (it.hasNext()) {
			RecordingPeriod p = it.next();
			RecordingPeriod gap = new RecordingPeriod(prev.getRecordStop(), p.getRecordStart());
			single.addRecordingGap(gap);
			prev = p;
		}
		DutyCycleInfo ds = assessDutyCycle();
		if (ds.isDutyCycled) {
//			return makeDutyCycledList(exportParams, ds);
			single.setDutyCycleInfo(ds);
		}
		
		return periods;
	}


	private ArrayList<RecordingPeriod> makeAutoPeriods(DeploymentExportOpts exportParams) {
		DutyCycleInfo ds = assessDutyCycle();
		if (ds.isDutyCycled) {
			return makeDutyCycledList(exportParams, ds);
		}
		else {
			return makeSeparatePeriods(exportParams);
		}
	}

	// know it's a duty cycled list, so get on with it. 
	private ArrayList<RecordingPeriod> makeDutyCycledList(DeploymentExportOpts exportParams, DutyCycleInfo ds) {
		// can just make the single list option, then add the duty cycle information I think. 
		ArrayList<RecordingPeriod> periods = makeSinglePeriod(exportParams);
		if (periods == null || periods.size() == 0) {
			return periods;
		}
		RecordingPeriod f = periods.get(0);
		f.setDutyCycleInfo(ds);
		return periods;
	}
}
