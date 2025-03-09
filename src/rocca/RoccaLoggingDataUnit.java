/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package rocca;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Set;

import com.google.protobuf.Duration;

import PamDetection.PamDetection;
import PamguardMVC.PamDataUnit;
import rocca.RoccaContourStats.ParamIndx;
import tethys.pamdata.AutoTethysProvider;


/**
 * Data unit containing the RoccaContourDataBlock to save to the database
 *
 * @author Michael Oswald
 */
//public class RoccaLoggingDataUnit extends PamDetection<PamDetection,PamDetection> {
public class RoccaLoggingDataUnit extends PamDataUnit<PamDataUnit,PamDataUnit> implements PamDetection {

    /** EnumMap linking the contour parameter to their values */
    private RoccaContourStats contourStats;

    /** String containing the name of the source file */
    private String clipFile;

    /** The detection tally */
    private int detectionCount;

    /** The sighting number */
    private String sightingNum;

    /** The classified species */
    private String classifiedAs;

    /** The name of the Stage 1 Classifier used */
    private String classifierUsed;

    /** The name of the Stage 2 Classifier used */
    private String classifier2Used;

    /** A string containing the votes for each species.  Note that the votes
     * are all contained in a single string, and separated by the '-' character.
     * The order of the votes corresponds to the order of the species given
     * by the spList field (below)
     */
    private String voteList;

    /** A string containing the names of the species.  Note that the names
     * are all contained in a single string, and separated by the '-' character.
     */
    private String spList;
    
    /**
     * Latitude when the RoccaContourDataBlock was created
	 * serialVersionUID=24 2016/08/10 added
     */
    private double latitude;
    
    /**
     * Longitude when the RoccaContourDataBlock was created
	 * serialVersionUID=24 2016/08/10 added
	 */
    private double longitude;
    

    /* declare all the statistic variables.  For convenience, we'll use the same
     * names as those declared in RoccaContourDataBlock
     */
//	private double freqMax;
//    private double freqMin;
//    private double duration;
//    private double freqBeg;
//    private double freqEnd;
//    private double freqRange;
//    private double dcMean;
//    private double dcStdDev;
//    private double freqMean;
//    private double freqStdDev;
//    private double freqMedian;
//    private double freqCenter;
//    private double freqRelBW;
//    private double freqMaxMinRatio;
//    private double freqBegEndRatio;
//    private double freqQuarter1;
//    private double freqQuarter2;
//    private double freqQuarter3;
//    private double freqSpread;
//    private double dcQuarter1Mean;
//    private double dcQuarter2Mean;
//    private double dcQuarter3Mean;
//    private double dcQuarter4Mean;
//    private double freqCOFM;
//    private double freqStepUp;
//    private double freqStepDown;
//    private double freqNumSteps;
//    private double freqSlopeMean;
//    private double freqAbsSlopeMean;
//    private double freqPosSlopeMean;
//    private double freqNegSlopeMean;
//    private double freqSlopeRatio;
//    private double freqBegSweep;
//    private double freqBegUp;
//    private double freqBegDwn;
//    private double freqEndSweep;
//    private double freqEndUp;
//    private double freqEndDwn;
//    private double numSweepsUpDwn;
//    private double numSweepsDwnUp;
//    private double numSweepsUpFlat;
//    private double numSweepsDwnFlat;
//    private double numSweepsFlatUp;
//    private double numSweepsFlatDwn;
//    private double freqSweepUpPercent;
//    private double freqSweepDwnPercent;
//    private double freqSweepFlatPercent;
//    private double numInflections;
//    private double inflMaxDelta;
//    private double inflMinDelta;
//    private double inflMaxMinDelta;
//    private double inflMeanDelta;
//    private double inflStdDevDelta;
//    private double inflMedianDelta;


    /**
     * Main constructor
     * @param timeMilliseconds  time recorded in the first data block
     * @param contourStats the RoccaContourStats object containing the
     * calculated/measured parameters from the contour
     */
	public RoccaLoggingDataUnit
            (long timeMilliseconds,
            RoccaContourStats contourStats) {
		super(timeMilliseconds);
        this.contourStats = contourStats;
	}

    /** return the RoccaContourStats object */
    public RoccaContourStats getContourStatsObject() {
        return contourStats;
    }

    /** return the EnumMap containing the measured/calculated parameters
     * contained in the RoccaContourStats object
     */
    public EnumMap<RoccaContourStats.ParamIndx, Double> getContourStats() {
        return contourStats.getContour();
    }

    public void setContourStats(RoccaContourStats contourStats) {
        this.contourStats = contourStats;
    }

    public String getClassifiedSpecies() {
        return classifiedAs;
    }

    public void setClassifiedSpecies(String classifiedAs) {
        this.classifiedAs = classifiedAs;
    }

    public String getClassifierUsed() {
        return classifierUsed;
    }

    public void setClassifierUsed(String classifierUsed) {
        this.classifierUsed = classifierUsed;
    }

    public String getClassifier2Used() {
        return classifier2Used;
    }

    public void setClassifier2Used(String classifier2Used) {
        this.classifier2Used = classifier2Used;
    }

    public String getFilename() {
        return clipFile;
    }

    public void setFilename(String clipFile) {
        this.clipFile = clipFile;
    }

    public int getDetectionCount() {
        return detectionCount;
    }

    public void setDetectionCount(int detectionCount) {
        this.detectionCount = detectionCount;
    }

    public String getSightingNum() {
        return sightingNum;
    }

    public void setSightingNum(String sightingNum) {
        this.sightingNum = sightingNum;
    }

    public String getSpList() {
        return spList;
    }

    public void setSpList(String spList) {
        this.spList = spList;
    }

    public String getVoteList() {
        return voteList;
    }

    public void setVoteList(String voteList) {
        this.voteList = voteList;
    }

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	@Override
	public String getSummaryString() {
		String base = super.getSummaryString();
		
//		if (detectionCount != 0) {
//			base += String.format("Detection count: %d<br>", detectionCount);
//		}
		if (classifiedAs != null) {
			base += "Classified as: " + classifiedAs + "<br>";
		}
		if (classifierUsed != null) {
			base += "Classifier used: " + classifierUsed + "<br>";
		}
		if (classifier2Used != null) {
			base += "Second Classifier: " + classifier2Used + "<br>";
		}
		
		
		if (contourStats == null) {
			return base;
		}
		EnumMap<ParamIndx, Double> lst = contourStats.getContour();
		if (lst == null) {
			return base;
		}
		
		int npRow = 3;
		Set<ParamIndx> keys = lst.keySet();
		int i = 0;
		for (ParamIndx aKey : keys) {
			Double data = lst.get(aKey);
			if (data == null) {
				continue;
			}
			data = AutoTethysProvider.roundDecimalPlaces(data, 3);
			base += String.format("%s: %s", aKey.toString(), data.toString());
			if (++i % npRow == 0) {
				base += ",<br>";
			}
			else {
				base += ", ";
			}
		}
		
		return base;
	}

	@Override
	public double[] getFrequency() {
		double[] fr = super.getFrequency();
		if (fr != null && fr.length == 2 && fr[1] > 0) {
			return fr;
		}
		if (contourStats.getContour() == null) {
			return null;
		}
		ParamIndx[] ps = {RoccaContourStats.ParamIndx.FREQMIN, RoccaContourStats.ParamIndx.FREQMAX};
		fr = new double[2];
		for (int i = 0; i < 2; i++) {
			Double f = contourStats.getContour().get(ps[i]);
			if (f == null) {
				return null;
			}
			else {
				fr[i] = f;
			}
		}
		return fr;
	}

	@Override
	public Double getDurationInMilliseconds() {
		if (contourStats.getContour() == null) {
			return null;
		}
		Double dur = contourStats.getContour().get(RoccaContourStats.ParamIndx.DURATION);
		if (dur == null) {
			return null;
		}
		return dur*1000.;
	}
    
    



    //    public double getDcMean() {
//        return dcMean;
//    }
//
//    public void setDcMean(double dcMean) {
//        this.dcMean = dcMean;
//    }
//
//    public double getDcQuarter1Mean() {
//        return dcQuarter1Mean;
//    }
//
//    public void setDcQuarter1Mean(double dcQuarter1Mean) {
//        this.dcQuarter1Mean = dcQuarter1Mean;
//    }
//
//    public double getDcQuarter2Mean() {
//        return dcQuarter2Mean;
//    }
//
//    public void setDcQuarter2Mean(double dcQuarter2Mean) {
//        this.dcQuarter2Mean = dcQuarter2Mean;
//    }
//
//    public double getDcQuarter3Mean() {
//        return dcQuarter3Mean;
//    }
//
//    public void setDcQuarter3Mean(double dcQuarter3Mean) {
//        this.dcQuarter3Mean = dcQuarter3Mean;
//    }
//
//    public double getDcQuarter4Mean() {
//        return dcQuarter4Mean;
//    }
//
//    public void setDcQuarter4Mean(double dcQuarter4Mean) {
//        this.dcQuarter4Mean = dcQuarter4Mean;
//    }
//
//    public double getDcStdDev() {
//        return dcStdDev;
//    }
//
//    public void setDcStdDev(double dcStdDev) {
//        this.dcStdDev = dcStdDev;
//    }
//
//    public double getThisDuration() {
//        return duration;
//    }
//
//    public void setThisDuration(double duration) {
//        this.duration = duration;
//    }
//
//    public double getFreqAbsSlopeMean() {
//        return freqAbsSlopeMean;
//    }
//
//    public void setFreqAbsSlopeMean(double freqAbsSlopeMean) {
//        this.freqAbsSlopeMean = freqAbsSlopeMean;
//    }
//
//    public double getFreqBeg() {
//        return freqBeg;
//    }
//
//    public void setFreqBeg(double freqBeg) {
//        this.freqBeg = freqBeg;
//    }
//
//    public double getFreqBegDwn() {
//        return freqBegDwn;
//    }
//
//    public void setFreqBegDwn(double freqBegDwn) {
//        this.freqBegDwn = freqBegDwn;
//    }
//
//    public double getFreqBegEndRatio() {
//        return freqBegEndRatio;
//    }
//
//    public void setFreqBegEndRatio(double freqBegEndRatio) {
//        this.freqBegEndRatio = freqBegEndRatio;
//    }
//
//    public double getFreqBegSweep() {
//        return freqBegSweep;
//    }
//
//    public void setFreqBegSweep(double freqBegSweep) {
//        this.freqBegSweep = freqBegSweep;
//    }
//
//    public double getFreqBegUp() {
//        return freqBegUp;
//    }
//
//    public void setFreqBegUp(double freqBegUp) {
//        this.freqBegUp = freqBegUp;
//    }
//
//    public double getFreqCOFM() {
//        return freqCOFM;
//    }
//
//    public void setFreqCOFM(double freqCOFM) {
//        this.freqCOFM = freqCOFM;
//    }
//
//    public double getFreqCenter() {
//        return freqCenter;
//    }
//
//    public void setFreqCenter(double freqCenter) {
//        this.freqCenter = freqCenter;
//    }
//
//    public double getFreqEnd() {
//        return freqEnd;
//    }
//
//    public void setFreqEnd(double freqEnd) {
//        this.freqEnd = freqEnd;
//    }
//
//    public double getFreqEndDwn() {
//        return freqEndDwn;
//    }
//
//    public void setFreqEndDwn(double freqEndDwn) {
//        this.freqEndDwn = freqEndDwn;
//    }
//
//    public double getFreqEndSweep() {
//        return freqEndSweep;
//    }
//
//    public void setFreqEndSweep(double freqEndSweep) {
//        this.freqEndSweep = freqEndSweep;
//    }
//
//    public double getFreqEndUp() {
//        return freqEndUp;
//    }
//
//    public void setFreqEndUp(double freqEndUp) {
//        this.freqEndUp = freqEndUp;
//    }
//
//    public double getFreqMax() {
//        return freqMax;
//    }
//
//    public void setFreqMax(double freqMax) {
//        this.freqMax = freqMax;
//    }
//
//    public double getFreqMaxMinRatio() {
//        return freqMaxMinRatio;
//    }
//
//    public void setFreqMaxMinRatio(double freqMaxMinRatio) {
//        this.freqMaxMinRatio = freqMaxMinRatio;
//    }
//
//    public double getFreqMean() {
//        return freqMean;
//    }
//
//    public void setFreqMean(double freqMean) {
//        this.freqMean = freqMean;
//    }
//
//    public double getFreqMedian() {
//        return freqMedian;
//    }
//
//    public void setFreqMedian(double freqMedian) {
//        this.freqMedian = freqMedian;
//    }
//
//    public double getFreqMin() {
//        return freqMin;
//    }
//
//    public void setFreqMin(double freqMin) {
//        this.freqMin = freqMin;
//    }
//
//    public double getFreqNegSlopeMean() {
//        return freqNegSlopeMean;
//    }
//
//    public void setFreqNegSlopeMean(double freqNegSlopeMean) {
//        this.freqNegSlopeMean = freqNegSlopeMean;
//    }
//
//    public double getFreqNumSteps() {
//        return freqNumSteps;
//    }
//
//    public void setFreqNumSteps(double freqNumSteps) {
//        this.freqNumSteps = freqNumSteps;
//    }
//
//    public double getFreqPosSlopeMean() {
//        return freqPosSlopeMean;
//    }
//
//    public void setFreqPosSlopeMean(double freqPosSlopeMean) {
//        this.freqPosSlopeMean = freqPosSlopeMean;
//    }
//
//    public double getFreqQuarter1() {
//        return freqQuarter1;
//    }
//
//    public void setFreqQuarter1(double freqQuarter1) {
//        this.freqQuarter1 = freqQuarter1;
//    }
//
//    public double getFreqQuarter2() {
//        return freqQuarter2;
//    }
//
//    public void setFreqQuarter2(double freqQuarter2) {
//        this.freqQuarter2 = freqQuarter2;
//    }
//
//    public double getFreqQuarter3() {
//        return freqQuarter3;
//    }
//
//    public void setFreqQuarter3(double freqQuarter3) {
//        this.freqQuarter3 = freqQuarter3;
//    }
//
//    public double getFreqRange() {
//        return freqRange;
//    }
//
//    public void setFreqRange(double freqRange) {
//        this.freqRange = freqRange;
//    }
//
//    public double getFreqRelBW() {
//        return freqRelBW;
//    }
//
//    public void setFreqRelBW(double freqRelBW) {
//        this.freqRelBW = freqRelBW;
//    }
//
//    public double getFreqSlopeMean() {
//        return freqSlopeMean;
//    }
//
//    public void setFreqSlopeMean(double freqSlopeMean) {
//        this.freqSlopeMean = freqSlopeMean;
//    }
//
//    public double getFreqSlopeRatio() {
//        return freqSlopeRatio;
//    }
//
//    public void setFreqSlopeRatio(double freqSlopeRatio) {
//        this.freqSlopeRatio = freqSlopeRatio;
//    }
//
//    public double getFreqSpread() {
//        return freqSpread;
//    }
//
//    public void setFreqSpread(double freqSpread) {
//        this.freqSpread = freqSpread;
//    }
//
//    public double getFreqStdDev() {
//        return freqStdDev;
//    }
//
//    public void setFreqStdDev(double freqStdDev) {
//        this.freqStdDev = freqStdDev;
//    }
//
//    public double getFreqStepDown() {
//        return freqStepDown;
//    }
//
//    public void setFreqStepDown(double freqStepDown) {
//        this.freqStepDown = freqStepDown;
//    }
//
//    public double getFreqStepUp() {
//        return freqStepUp;
//    }
//
//    public void setFreqStepUp(double freqStepUp) {
//        this.freqStepUp = freqStepUp;
//    }
//
//    public double getFreqSweepDwnPercent() {
//        return freqSweepDwnPercent;
//    }
//
//    public void setFreqSweepDwnPercent(double freqSweepDwnPercent) {
//        this.freqSweepDwnPercent = freqSweepDwnPercent;
//    }
//
//    public double getFreqSweepFlatPercent() {
//        return freqSweepFlatPercent;
//    }
//
//    public void setFreqSweepFlatPercent(double freqSweepFlatPercent) {
//        this.freqSweepFlatPercent = freqSweepFlatPercent;
//    }
//
//    public double getFreqSweepUpPercent() {
//        return freqSweepUpPercent;
//    }
//
//    public void setFreqSweepUpPercent(double freqSweepUpPercent) {
//        this.freqSweepUpPercent = freqSweepUpPercent;
//    }
//
//    public double getInflMaxDelta() {
//        return inflMaxDelta;
//    }
//
//    public void setInflMaxDelta(double inflMaxDelta) {
//        this.inflMaxDelta = inflMaxDelta;
//    }
//
//    public double getInflMaxMinDelta() {
//        return inflMaxMinDelta;
//    }
//
//    public void setInflMaxMinDelta(double inflMaxMinDelta) {
//        this.inflMaxMinDelta = inflMaxMinDelta;
//    }
//
//    public double getInflMeanDelta() {
//        return inflMeanDelta;
//    }
//
//    public void setInflMeanDelta(double inflMeanDelta) {
//        this.inflMeanDelta = inflMeanDelta;
//    }
//
//    public double getInflMedianDelta() {
//        return inflMedianDelta;
//    }
//
//    public void setInflMedianDelta(double inflMedianDelta) {
//        this.inflMedianDelta = inflMedianDelta;
//    }
//
//    public double getInflMinDelta() {
//        return inflMinDelta;
//    }
//
//    public void setInflMinDelta(double inflMinDelta) {
//        this.inflMinDelta = inflMinDelta;
//    }
//
//    public double getInflStdDevDelta() {
//        return inflStdDevDelta;
//    }
//
//    public void setInflStdDevDelta(double inflStdDevDelta) {
//        this.inflStdDevDelta = inflStdDevDelta;
//    }
//
//    public double getNumInflections() {
//        return numInflections;
//    }
//
//    public void setNumInflections(double numInflections) {
//        this.numInflections = numInflections;
//    }
//
//    public double getNumSweepsDwnFlat() {
//        return numSweepsDwnFlat;
//    }
//
//    public void setNumSweepsDwnFlat(double numSweepsDwnFlat) {
//        this.numSweepsDwnFlat = numSweepsDwnFlat;
//    }
//
//    public double getNumSweepsDwnUp() {
//        return numSweepsDwnUp;
//    }
//
//    public void setNumSweepsDwnUp(double numSweepsDwnUp) {
//        this.numSweepsDwnUp = numSweepsDwnUp;
//    }
//
//    public double getNumSweepsFlatDwn() {
//        return numSweepsFlatDwn;
//    }
//
//    public void setNumSweepsFlatDwn(double numSweepsFlatDwn) {
//        this.numSweepsFlatDwn = numSweepsFlatDwn;
//    }
//
//    public double getNumSweepsFlatUp() {
//        return numSweepsFlatUp;
//    }
//
//    public void setNumSweepsFlatUp(double numSweepsFlatUp) {
//        this.numSweepsFlatUp = numSweepsFlatUp;
//    }
//
//    public double getNumSweepsUpDwn() {
//        return numSweepsUpDwn;
//    }
//
//    public void setNumSweepsUpDwn(double numSweepsUpDwn) {
//        this.numSweepsUpDwn = numSweepsUpDwn;
//    }
//
//    public double getNumSweepsUpFlat() {
//        return numSweepsUpFlat;
//    }
//
//    public void setNumSweepsUpFlat(double numSweepsUpFlat) {
//        this.numSweepsUpFlat = numSweepsUpFlat;
//    }

}
