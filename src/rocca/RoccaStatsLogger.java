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


/**
 *
 * Facilitates data logging to database
 * copied from ClickLogger.java
 *
 * @author Michael Oswald
 *
 */

package rocca;

import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;

/**
 * Databse logging information for Rocca statistics.
 * 
 * @author Michael Oswald
 *
 */
public class RoccaStatsLogger extends SQLLogging {

	RoccaControl roccaControl;
	
	/**
     * These items are used to form the columns within the Table.  Item names
     * are copied from the enum names in RoccaContourDataBlock for convenience
     * 
     * Mike Oswald 2015/05/17 serialVersionUID = 19
     * Added items from freqPeak to offlineEventID, inclusive.  Somehow these had not been
     * added when the click detector was originally integrated with Rocca
     * 
     * serialVersionUID=24 2016/08/10 added latitude and longitude
     */
	PamTableItem freqMax;
    PamTableItem freqMin;
    PamTableItem duration;
    PamTableItem freqBeg;
    PamTableItem freqEnd;
    PamTableItem freqRange;
    PamTableItem dcMean;
    PamTableItem dcStdDev;
    PamTableItem freqMean;
    PamTableItem freqStdDev;
    PamTableItem freqMedian;
    PamTableItem freqCenter;
    PamTableItem freqRelBW;
    PamTableItem freqMaxMinRatio;
    PamTableItem freqBegEndRatio;
    PamTableItem freqQuarter1;
    PamTableItem freqQuarter2;
    PamTableItem freqQuarter3;
    PamTableItem freqSpread;
    PamTableItem dcQuarter1Mean;
    PamTableItem dcQuarter2Mean;
    PamTableItem dcQuarter3Mean;
    PamTableItem dcQuarter4Mean;
    PamTableItem freqCOFM;
    PamTableItem freqStepUp;
    PamTableItem freqStepDown;
    PamTableItem freqNumSteps;
    PamTableItem freqSlopeMean;
    PamTableItem freqAbsSlopeMean;
    PamTableItem freqPosSlopeMean;
    PamTableItem freqNegSlopeMean;
    PamTableItem freqSlopeRatio;
    PamTableItem freqBegSweep;
    PamTableItem freqBegUp;
    PamTableItem freqBegDwn;
    PamTableItem freqEndSweep;
    PamTableItem freqEndUp;
    PamTableItem freqEndDwn;
    PamTableItem numSweepsUpDwn;
    PamTableItem numSweepsDwnUp;
    PamTableItem numSweepsUpFlat;
    PamTableItem numSweepsDwnFlat;
    PamTableItem numSweepsFlatUp;
    PamTableItem numSweepsFlatDwn;
    PamTableItem freqSweepUpPercent;
    PamTableItem freqSweepDwnPercent;
    PamTableItem freqSweepFlatPercent;
    PamTableItem numInflections;
    PamTableItem inflMaxDelta;
    PamTableItem inflMinDelta;
    PamTableItem inflMaxMinDelta;
    PamTableItem inflMeanDelta;
    PamTableItem inflStdDevDelta;
    PamTableItem inflMedianDelta;
    PamTableItem infldur;
    PamTableItem stepdur;
    PamTableItem clipFile;
    PamTableItem detectionCount;
    PamTableItem sightingNum;
    PamTableItem classifiedAs;
    PamTableItem classifierUsed;
    PamTableItem classifier2Used;
    PamTableItem voteList;
    PamTableItem spList;
    PamTableItem freqPeak;
    PamTableItem bw3db;
    PamTableItem bw3dbLow;
    PamTableItem bw3dbHigh;
    PamTableItem bw10db;
    PamTableItem bw10dbLow;
    PamTableItem bw10dbHigh;
    PamTableItem rmsSignal;
    PamTableItem rmsNoise;
    PamTableItem snr;
    PamTableItem nCrossings;
    PamTableItem sweepRate;
    PamTableItem meanTimeZC;
    PamTableItem medianTimeZC;
    PamTableItem varianceTimeZC;
    PamTableItem whaleTrain;
    PamTableItem clickType;
    PamTableItem ici;
    PamTableItem offlineEventID;
    PamTableItem latitude;
    PamTableItem longitude;

    /**
     * The RoccaLoggingDataBlock to be saved to the database
     */
    public RoccaLoggingDataBlock rldb = null;

	private PamTableDefinition tableDefinition;

	public static final int STRING_LENGTH = 128;
	public static final int SHORT_STRING_LENGTH = 20;

    /**
     *
     * @param roccaControl
     * @param pamDataBlock
     */
	public RoccaStatsLogger(RoccaControl roccaControl, PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		setCanView(true);

		this.roccaControl = roccaControl;
        this.rldb = (RoccaLoggingDataBlock) pamDataBlock;

		tableDefinition = new PamTableDefinition(rldb.getDataName(), UPDATE_POLICY_WRITENEW);
		tableDefinition.addTableItem(clipFile = new PamTableItem("Source", Types.CHAR, STRING_LENGTH));
		tableDefinition.addTableItem(detectionCount = new PamTableItem("encounterCount", Types.INTEGER));
		tableDefinition.addTableItem(sightingNum = new PamTableItem("sightingNum", Types.CHAR, SHORT_STRING_LENGTH));
		tableDefinition.addTableItem(classifiedAs = new PamTableItem("classifiedAs", Types.CHAR, SHORT_STRING_LENGTH));
		tableDefinition.addTableItem(classifierUsed = new PamTableItem("classifierUsed", Types.CHAR, STRING_LENGTH));
		tableDefinition.addTableItem(classifier2Used = new PamTableItem("classifier2Used", Types.CHAR, STRING_LENGTH));
		tableDefinition.addTableItem(voteList = new PamTableItem("voteList", Types.CHAR, STRING_LENGTH));
		tableDefinition.addTableItem(spList = new PamTableItem("spList", Types.CHAR, STRING_LENGTH));
		tableDefinition.addTableItem(freqMax = new PamTableItem("freqMax", Types.DOUBLE));
		tableDefinition.addTableItem(freqMin = new PamTableItem("freqMin", Types.DOUBLE));
		tableDefinition.addTableItem(duration = new PamTableItem("duration", Types.DOUBLE));
		tableDefinition.addTableItem(freqBeg = new PamTableItem("freqBeg", Types.DOUBLE));
		tableDefinition.addTableItem(freqEnd = new PamTableItem("freqEnd", Types.DOUBLE));
		tableDefinition.addTableItem(freqRange = new PamTableItem("freqRange", Types.DOUBLE));
		tableDefinition.addTableItem(dcMean = new PamTableItem("dcMean", Types.DOUBLE));
		tableDefinition.addTableItem(dcStdDev = new PamTableItem("dcStdDev", Types.DOUBLE));
		tableDefinition.addTableItem(freqMean = new PamTableItem("freqMean", Types.DOUBLE));
		tableDefinition.addTableItem(freqStdDev = new PamTableItem("freqStdDev", Types.DOUBLE));
		tableDefinition.addTableItem(freqMedian = new PamTableItem("freqMedian", Types.DOUBLE));
		tableDefinition.addTableItem(freqCenter = new PamTableItem("freqCenter", Types.DOUBLE));
		tableDefinition.addTableItem(freqRelBW = new PamTableItem("freqRelBW", Types.DOUBLE));
		tableDefinition.addTableItem(freqMaxMinRatio = new PamTableItem("freqMaxMinRatio", Types.DOUBLE));
		tableDefinition.addTableItem(freqBegEndRatio = new PamTableItem("freqBegEndRatio", Types.DOUBLE));
		tableDefinition.addTableItem(freqQuarter1 = new PamTableItem("freqQuarter1", Types.DOUBLE));
		tableDefinition.addTableItem(freqQuarter2 = new PamTableItem("freqQuarter2", Types.DOUBLE));
		tableDefinition.addTableItem(freqQuarter3 = new PamTableItem("freqQuarter3", Types.DOUBLE));
		tableDefinition.addTableItem(freqSpread = new PamTableItem("freqSpread", Types.DOUBLE));
		tableDefinition.addTableItem(dcQuarter1Mean = new PamTableItem("dcQuarter1Mean", Types.DOUBLE));
		tableDefinition.addTableItem(dcQuarter2Mean = new PamTableItem("dcQuarter2Mean", Types.DOUBLE));
		tableDefinition.addTableItem(dcQuarter3Mean = new PamTableItem("dcQuarter3Mean", Types.DOUBLE));
		tableDefinition.addTableItem(dcQuarter4Mean = new PamTableItem("dcQuarter4Mean", Types.DOUBLE));
		tableDefinition.addTableItem(freqCOFM = new PamTableItem("freqCOFM", Types.DOUBLE));
		tableDefinition.addTableItem(freqStepUp = new PamTableItem("freqStepUp", Types.DOUBLE));
		tableDefinition.addTableItem(freqStepDown = new PamTableItem("freqStepDown", Types.DOUBLE));
		tableDefinition.addTableItem(freqNumSteps = new PamTableItem("freqNumSteps", Types.DOUBLE));
		tableDefinition.addTableItem(freqSlopeMean = new PamTableItem("freqSlopeMean", Types.DOUBLE));
		tableDefinition.addTableItem(freqAbsSlopeMean = new PamTableItem("freqAbsSlopeMean", Types.DOUBLE));
		tableDefinition.addTableItem(freqPosSlopeMean = new PamTableItem("freqPosSlopeMean", Types.DOUBLE));
		tableDefinition.addTableItem(freqNegSlopeMean = new PamTableItem("freqNegSlopeMean", Types.DOUBLE));
		tableDefinition.addTableItem(freqSlopeRatio = new PamTableItem("freqSlopeRatio", Types.DOUBLE));
		tableDefinition.addTableItem(freqBegSweep = new PamTableItem("freqBegSweep", Types.DOUBLE));
		tableDefinition.addTableItem(freqBegUp = new PamTableItem("freqBegUp", Types.DOUBLE));
		tableDefinition.addTableItem(freqBegDwn = new PamTableItem("freqBegDwn", Types.DOUBLE));
		tableDefinition.addTableItem(freqEndSweep = new PamTableItem("freqEndSweep", Types.DOUBLE));
		tableDefinition.addTableItem(freqEndUp = new PamTableItem("freqEndUp", Types.DOUBLE));
		tableDefinition.addTableItem(freqEndDwn = new PamTableItem("freqEndDwn", Types.DOUBLE));
		tableDefinition.addTableItem(numSweepsUpDwn = new PamTableItem("numSweepsUpDwn", Types.DOUBLE));
		tableDefinition.addTableItem(numSweepsDwnUp = new PamTableItem("numSweepsDwnUp", Types.DOUBLE));
		tableDefinition.addTableItem(numSweepsUpFlat = new PamTableItem("numSweepsUpFlat", Types.DOUBLE));
		tableDefinition.addTableItem(numSweepsDwnFlat = new PamTableItem("numSweepsDwnFlat", Types.DOUBLE));
		tableDefinition.addTableItem(numSweepsFlatUp = new PamTableItem("numSweepsFlatUp", Types.DOUBLE));
		tableDefinition.addTableItem(numSweepsFlatDwn = new PamTableItem("numSweepsFlatDwn", Types.DOUBLE));
		tableDefinition.addTableItem(freqSweepUpPercent = new PamTableItem("freqSweepUpPercent", Types.DOUBLE));
		tableDefinition.addTableItem(freqSweepDwnPercent = new PamTableItem("freqSweepDwnPercent", Types.DOUBLE));
		tableDefinition.addTableItem(freqSweepFlatPercent = new PamTableItem("freqSweepFlatPercent", Types.DOUBLE));
		tableDefinition.addTableItem(numInflections = new PamTableItem("numInflections", Types.DOUBLE));
		tableDefinition.addTableItem(inflMaxDelta = new PamTableItem("inflMaxDelta", Types.DOUBLE));
		tableDefinition.addTableItem(inflMinDelta = new PamTableItem("inflMinDelta", Types.DOUBLE));
		tableDefinition.addTableItem(inflMaxMinDelta = new PamTableItem("inflMaxMinDelta", Types.DOUBLE));
		tableDefinition.addTableItem(inflMeanDelta = new PamTableItem("inflMeanDelta", Types.DOUBLE));
		tableDefinition.addTableItem(inflStdDevDelta = new PamTableItem("inflStdDevDelta", Types.DOUBLE));
		tableDefinition.addTableItem(inflMedianDelta = new PamTableItem("inflMedianDelta", Types.DOUBLE));
		tableDefinition.addTableItem(infldur = new PamTableItem("infldur", Types.DOUBLE));
		tableDefinition.addTableItem(stepdur = new PamTableItem("stepdur", Types.DOUBLE));
		tableDefinition.addTableItem(freqPeak = new PamTableItem("freqPeak", Types.DOUBLE));
		tableDefinition.addTableItem(bw3db = new PamTableItem("bw3db", Types.DOUBLE));
		tableDefinition.addTableItem(bw3dbLow = new PamTableItem("bw3dbLow", Types.DOUBLE));
		tableDefinition.addTableItem(bw3dbHigh = new PamTableItem("bw3dbHigh", Types.DOUBLE));
		tableDefinition.addTableItem(bw10db = new PamTableItem("bw10db", Types.DOUBLE));
		tableDefinition.addTableItem(bw10dbLow = new PamTableItem("bw10dbLow", Types.DOUBLE));
		tableDefinition.addTableItem(bw10dbHigh = new PamTableItem("bw10dbHigh", Types.DOUBLE));
		tableDefinition.addTableItem(rmsSignal = new PamTableItem("rmsSignal", Types.DOUBLE));
		tableDefinition.addTableItem(rmsNoise = new PamTableItem("rmsNoise", Types.DOUBLE));
		tableDefinition.addTableItem(snr = new PamTableItem("snr", Types.DOUBLE));
		tableDefinition.addTableItem(nCrossings = new PamTableItem("nCrossings", Types.DOUBLE));
		tableDefinition.addTableItem(sweepRate = new PamTableItem("sweepRate", Types.DOUBLE));
		tableDefinition.addTableItem(meanTimeZC = new PamTableItem("meanTimeZC", Types.DOUBLE));
		tableDefinition.addTableItem(medianTimeZC = new PamTableItem("medianTimeZC", Types.DOUBLE));
		tableDefinition.addTableItem(varianceTimeZC = new PamTableItem("varianceTimeZC", Types.DOUBLE));
		tableDefinition.addTableItem(whaleTrain = new PamTableItem("whaleTrain", Types.DOUBLE));
		tableDefinition.addTableItem(clickType = new PamTableItem("clickType", Types.DOUBLE));
		tableDefinition.addTableItem(ici = new PamTableItem("ici", Types.DOUBLE));
		tableDefinition.addTableItem(offlineEventID = new PamTableItem("offlineEventID", Types.DOUBLE));
		tableDefinition.addTableItem(latitude = new PamTableItem("latitude", Types.DOUBLE));	// serialVersionUID=24 2016/08/10 added
		tableDefinition.addTableItem(longitude = new PamTableItem("longitude", Types.DOUBLE));	// serialVersionUID=24 2016/08/10 added
		
        setTableDefinition(tableDefinition);
	}

    /**
     * Load up the class fields with the data to save
     *
     * @param pamDataUnit The RoccaLoggingDataUnit that contains the
     * RoccaContourStats object with all the data to save
     */
	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {

		RoccaLoggingDataUnit rldu = (RoccaLoggingDataUnit) pamDataUnit;

		clipFile.setValue(rldu.getFilename());
		detectionCount.setValue(rldu.getDetectionCount());
		sightingNum.setValue(rldu.getSightingNum());
		classifiedAs.setValue(rldu.getClassifiedSpecies());
		classifierUsed.setValue(rldu.getClassifierUsed());
		classifier2Used.setValue(rldu.getClassifier2Used());
		voteList.setValue(rldu.getVoteList());
		spList.setValue(rldu.getSpList());
        latitude.setValue(rldu.getLatitude());	// serialVersionUID=24 2016/08/10 added
        longitude.setValue(rldu.getLongitude());	// serialVersionUID=24 2016/08/10 added
        freqMax.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQMAX));
        freqMin.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQMIN));
        duration.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.DURATION));
        freqBeg.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQBEG));
        freqEnd.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQEND));
        freqRange.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQRANGE));
        dcMean.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.DCMEAN));
        dcStdDev.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.DCSTDDEV));
        freqMean.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQMEAN));
        freqStdDev.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQSTDDEV));
        freqMedian.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQMEDIAN));
        freqCenter.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQCENTER));
        freqRelBW.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQRELBW));
        freqMaxMinRatio.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQMAXMINRATIO));
        freqBegEndRatio.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQBEGENDRATIO));
        freqQuarter1.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQQUARTER1));
        freqQuarter2.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQQUARTER2));
        freqQuarter3.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQQUARTER3));
        freqSpread.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQSPREAD));
        dcQuarter1Mean.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.DCQUARTER1MEAN));
        dcQuarter2Mean.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.DCQUARTER2MEAN));
        dcQuarter3Mean.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.DCQUARTER3MEAN));
        dcQuarter4Mean.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.DCQUARTER4MEAN));
        freqCOFM.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQCOFM));
        freqStepUp.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQSTEPUP));
        freqStepDown.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQSTEPDOWN));
        freqNumSteps.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQNUMSTEPS));
        freqSlopeMean.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQSLOPEMEAN));
        freqAbsSlopeMean.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQABSSLOPEMEAN));
        freqPosSlopeMean.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQPOSSLOPEMEAN));
        freqNegSlopeMean.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQNEGSLOPEMEAN));
        freqSlopeRatio.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQSLOPERATIO));
        freqBegSweep.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQBEGSWEEP));
        freqBegUp.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQBEGUP));
        freqBegDwn.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQBEGDWN));
        freqEndSweep.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQENDSWEEP));
        freqEndUp.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQENDUP));
        freqEndDwn.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQENDDWN));
        numSweepsUpDwn.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.NUMSWEEPSUPDWN));
        numSweepsDwnUp.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.NUMSWEEPSDWNUP));
        numSweepsUpFlat.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.NUMSWEEPSUPFLAT));
        numSweepsDwnFlat.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.NUMSWEEPSDWNFLAT));
        numSweepsFlatUp.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.NUMSWEEPSFLATUP));
        numSweepsFlatDwn.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.NUMSWEEPSFLATDWN));
        freqSweepUpPercent.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQSWEEPUPPERCENT));
        freqSweepDwnPercent.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQSWEEPDWNPERCENT));
        freqSweepFlatPercent.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQSWEEPFLATPERCENT));
        numInflections.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.NUMINFLECTIONS));
        inflMaxDelta.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.INFLMAXDELTA));
        inflMinDelta.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.INFLMINDELTA));
        inflMaxMinDelta.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.INFLMAXMINDELTA));
        inflMeanDelta.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.INFLMEANDELTA));
        inflStdDevDelta.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.INFLSTDDEVDELTA));
        inflMedianDelta.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.INFLMEDIANDELTA));
        infldur.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.INFLDUR));
        stepdur.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.STEPDUR));
        freqPeak.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.FREQPEAK));
        bw3db.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.BW3DB));
        bw3dbLow.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.BW3DBLOW));
        bw3dbHigh.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.BW3DBHIGH));
        bw10db.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.BW10DB));
        bw10dbLow.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.BW10DBLOW));
        bw10dbHigh.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.BW10DBHIGH));
        rmsSignal.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.RMSSIGNAL));
        rmsNoise.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.RMSNOISE));
        snr.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.SNR));
        nCrossings.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.NCROSSINGS));
        sweepRate.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.SWEEPRATE));
        meanTimeZC.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.MEANTIMEZC));
        medianTimeZC.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.MEDIANTIMEZC));
        varianceTimeZC.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.VARIANCETIMEZC));
        whaleTrain.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.WHALETRAIN));
        clickType.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.CLICKTYPE));
        ici.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.ICI));
        offlineEventID.setValue(rldu.getContourStats().get(RoccaContourStats.ParamIndx.OFFLINEEVENTID));
	}

    /**
     * Create a new RoccaLogginDataUnit and fill it with values from the database
     *
     * @param timeMilliseconds parameter from the database
     * @param databaseIndex database index
     * @return PamDataUnit the new data unit
     */
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {


        /* create new RoccaContourStats and RoccaLoggingDataUnit objects */
        RoccaContourStats rcs = new RoccaContourStats();
        RoccaLoggingDataUnit rldu = new RoccaLoggingDataUnit(timeMilliseconds, rcs);

        /* set the logging database index */
        rldu.setDatabaseIndex(databaseIndex);
        
        /* put the values from the database into the RoccaContourStats object */
		rldu.setFilename(clipFile.getStringValue());
		rldu.setDetectionCount(detectionCount.getIntegerValue());
		rldu.setSightingNum(sightingNum.getStringValue());
		rldu.setClassifiedSpecies(classifiedAs.getStringValue());
		rldu.setClassifierUsed(classifierUsed.getStringValue());
		rldu.setClassifier2Used(classifier2Used.getStringValue());
		rldu.setVoteList(voteList.getStringValue());
		rldu.setSpList(spList.getStringValue());
		rldu.setLatitude(latitude.getDoubleValue());	// serialVersionUID=24 2016/08/10 added
		rldu.setLongitude(longitude.getDoubleValue());	// serialVersionUID=24 2016/08/10 added
        rcs.getContour().put(RoccaContourStats.ParamIndx.FREQMAX, freqMax.getDoubleValue());
        rcs.getContour().put(RoccaContourStats.ParamIndx.FREQMIN, freqMin.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.DURATION, duration.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQBEG, freqBeg.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQEND, freqEnd.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQRANGE, freqRange.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.DCMEAN, dcMean.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.DCSTDDEV, dcStdDev.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQMEAN, freqMean.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQSTDDEV, freqStdDev.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQMEDIAN, freqMedian.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQCENTER, freqCenter.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQRELBW, freqRelBW.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQMAXMINRATIO, freqMaxMinRatio.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQBEGENDRATIO, freqBegEndRatio.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQQUARTER1, freqQuarter1.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQQUARTER2, freqQuarter2.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQQUARTER3, freqQuarter3.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQSPREAD, freqSpread.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.DCQUARTER1MEAN, dcQuarter1Mean.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.DCQUARTER2MEAN, dcQuarter2Mean.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.DCQUARTER3MEAN, dcQuarter3Mean.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.DCQUARTER4MEAN, dcQuarter4Mean.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQCOFM, freqCOFM.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQSTEPUP, freqStepUp.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQSTEPDOWN, freqStepDown.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQNUMSTEPS, freqNumSteps.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQSLOPEMEAN, freqSlopeMean.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQABSSLOPEMEAN, freqAbsSlopeMean.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQPOSSLOPEMEAN, freqPosSlopeMean.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQNEGSLOPEMEAN, freqNegSlopeMean.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQSLOPERATIO, freqSlopeRatio.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQBEGSWEEP, freqBegSweep.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQBEGUP, freqBegUp.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQBEGDWN, freqBegDwn.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQENDSWEEP, freqEndSweep.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQENDUP, freqEndUp.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQENDDWN, freqEndDwn.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.NUMSWEEPSUPDWN, numSweepsUpDwn.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.NUMSWEEPSDWNUP, numSweepsDwnUp.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.NUMSWEEPSUPFLAT, numSweepsUpFlat.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.NUMSWEEPSDWNFLAT, numSweepsDwnFlat.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.NUMSWEEPSFLATUP, numSweepsFlatUp.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.NUMSWEEPSFLATDWN, numSweepsFlatDwn.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQSWEEPUPPERCENT, freqSweepUpPercent.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQSWEEPDWNPERCENT, freqSweepDwnPercent.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQSWEEPFLATPERCENT, freqSweepFlatPercent.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.NUMINFLECTIONS, numInflections.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.INFLMAXDELTA, inflMaxDelta.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.INFLMINDELTA, inflMinDelta.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.INFLMAXMINDELTA, inflMaxMinDelta.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.INFLMEANDELTA, inflMeanDelta.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.INFLSTDDEVDELTA, inflStdDevDelta.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.INFLMEDIANDELTA, inflMedianDelta.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.INFLDUR, infldur.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.STEPDUR, stepdur.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.FREQPEAK, freqPeak.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.BW3DB, bw3db.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.BW3DBLOW, bw3dbLow.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.BW3DBHIGH, bw3dbHigh.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.BW10DB, bw10db.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.BW10DBLOW, bw10dbLow.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.BW10DBHIGH, bw10dbHigh.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.RMSSIGNAL, rmsSignal.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.RMSNOISE, rmsNoise.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.SNR, snr.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.NCROSSINGS, nCrossings.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.SWEEPRATE, sweepRate.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.MEANTIMEZC, meanTimeZC.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.MEDIANTIMEZC, medianTimeZC.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.VARIANCETIMEZC, varianceTimeZC.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.WHALETRAIN, whaleTrain.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.CLICKTYPE, clickType.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.ICI, ici.getDoubleValue());
		rcs.getContour().put(RoccaContourStats.ParamIndx.OFFLINEEVENTID, offlineEventID.getDoubleValue());
		
		
		/* save the RoccaLoggingDataUnit to the RoccaLoggingDataBlock and return */
        rldb.addPamData(rldu);
        return rldu;
    }

}