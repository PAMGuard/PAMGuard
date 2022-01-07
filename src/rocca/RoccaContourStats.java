/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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

/**
 * An EnumMap linking the parameter names to the values measured/calculated from
 * the contour.  Parameter names are stored in a static Enum so that they can
 * be accessed by other classes easily, without needing to instantiate a
 * RoccaContourStats object.<p>
 * If new statistics are to be added, the names must first be added to the
 * ParamIndx enum.  After that, the calculation must be added to
 * RoccaContourDataBlock and the parameter must be added to RoccaStatsLogger.
 *
 * @author Michael Oswald
 */
public class RoccaContourStats {

    /** reference to this object */
    private RoccaContourStats rcs = null;

    /** Enum class with the names of the contour parameters <p>
     * Note that these names MUST match the attribute names used to create the
     * classifier (other than the case - by convention, java constants are
     * denoted by uppercase characters).
     */
    public static enum ParamIndx {
        FREQMAX,
        FREQMIN,
        DURATION,
        FREQBEG,
        FREQEND,
        FREQRANGE,
        DCMEAN,
        DCSTDDEV,
        FREQMEAN,
        FREQSTDDEV,
        FREQMEDIAN,
        FREQCENTER,
        FREQRELBW,
        FREQMAXMINRATIO,
        FREQBEGENDRATIO,
        FREQQUARTER1,
        FREQQUARTER2,
        FREQQUARTER3,
        FREQSPREAD,
//        DCQUARTERMEAN,
        DCQUARTER1MEAN,
        DCQUARTER2MEAN,
        DCQUARTER3MEAN,
        DCQUARTER4MEAN,
        FREQCOFM,
        FREQSTEPUP,
        FREQSTEPDOWN,
        FREQNUMSTEPS,
        FREQSLOPEMEAN,
        FREQABSSLOPEMEAN,
        FREQPOSSLOPEMEAN,
        FREQNEGSLOPEMEAN,
        FREQSLOPERATIO,
        FREQBEGSWEEP,
        FREQBEGUP,
        FREQBEGDWN,
        FREQENDSWEEP,
        FREQENDUP,
        FREQENDDWN,
        NUMSWEEPSUPDWN,
        NUMSWEEPSDWNUP,
        NUMSWEEPSUPFLAT,
        NUMSWEEPSDWNFLAT,
        NUMSWEEPSFLATUP,
        NUMSWEEPSFLATDWN,
        FREQSWEEPUPPERCENT,
        FREQSWEEPDWNPERCENT,
        FREQSWEEPFLATPERCENT,
        NUMINFLECTIONS,
        INFLMAXDELTA,
        INFLMINDELTA,
        INFLMAXMINDELTA,
        INFLMEANDELTA,
        INFLSTDDEVDELTA,
        INFLMEDIANDELTA,
        INFLDUR,
        STEPDUR,
        FREQPEAK,
        BW3DB,
        BW3DBLOW,
        BW3DBHIGH,
        BW10DB,
        BW10DBLOW,
        BW10DBHIGH,
        RMSSIGNAL,
        RMSNOISE,
        SNR,
        NCROSSINGS,
        SWEEPRATE,
        MEANTIMEZC,
        MEDIANTIMEZC,
        VARIANCETIMEZC,
        WHALETRAIN,
        CLICKTYPE,
        ICI,
        OFFLINEEVENTID
    }
    
    
    /** EnumMap linking the contour parameter to their values */
    private EnumMap<ParamIndx, Double> contour = new EnumMap<ParamIndx, Double>(ParamIndx.class);

    /** ArrayList containing the EnumMap key names */
    private ArrayList<String> keyNames = new ArrayList<String>(contour.size());

    /**
     * Main Constructor
     *
     */
    public RoccaContourStats() {

        /* save a reference to this object */
        rcs = this;
        initialize(contour);
    }

	/** Initialize all parameters in the map except FREQMIN to 0.0; set FREQMIN
     * to 100000.0.  Also save the key names into an arrayList to make it easier
     * to search when comparing to the classifier's required attributes.
     */
    public void initialize(EnumMap<ParamIndx, Double> contour) {
        for (ParamIndx p : ParamIndx.values()) {
            contour.put(p, 0.0);
            keyNames.add(p.toString());
        }
        contour.put(ParamIndx.FREQMIN, 100000.0);
    }

    public EnumMap<ParamIndx, Double> getContour() {
        return contour;
    }

    public ArrayList<String> getKeyNames() {
        return keyNames;
    }

    public RoccaContourStats getRCS() {
        return rcs;
    }


}
