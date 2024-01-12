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


package clickDetector;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;

/**
 * Parameters for the IDI Display
 *
 * @author Michael Oswald
 */
public class IDI_DisplayParams implements Serializable, Cloneable, ManagedParameters {

    /** for serialization */
    static public final long serialVersionUID = 1;

    /** Scale of Inter-Detection-Interval (IDI) bins in the high-resolution
     * display, in milliseconds */
    private int highResIdiBinScale = 1;

    /** Scale of Inter-Detection-Interval (IDI) bins in the low-resolution
     * display, in milliseconds */
    private int lowResIdiBinScale = 5;

    /** Size of time bins, in milliseconds (default 20 seconds) */
    private int timeBinScale = 1 * 1000;

    /** maximum IDI bin value in high-resolution display, in milliseconds */
    private int maxHighResBin = 70;

    /** maximum IDI bin value in low-resolution display, in milliseconds */
    private int maxLowResBin = 700;

    /** highest histogram count in the high-resolution colour bar */
    private int maxHighCount = 20;

    /** highest histogram count in the low-resolution colour bar */
    private int maxLowCount = 50;

    /** maximum time bin value, in milliseconds (default 5 minutes) */
    private int maxTimeBin = 1 * 60 * 1000;

    /** output filename */
    private File outputFilename = new File("C:\\IDI_DisplayOutput.csv");

    /** boolean indicating whether or not we're saving the data */
    private boolean saveOutput = false;

    /**
     * Constructor
     */
    public IDI_DisplayParams() {
    }

    /**
     * Returns the scale of the IDI bins (horizontal axis), in
     * milliseconds/bin
     *
     * @return width of IDI bins in milliseconds/bin
     */
    public int getHighResIdiBinScale() {
        return highResIdiBinScale;
    }

    /**
     * Sets the size (along the horizontal axis) of the IDI bins, in
     * milliseconds/bin
     *
     * @param idiBin width of IDI bins in milliseconds/bin
     */
    public void setHighResIdiBinScale(int idiBin) {
        this.highResIdiBinScale = idiBin;
    }

    /**
     * Returns the scale of the IDI bins (horizontal axis), in
     * milliseconds/bin
     *
     * @return width of IDI bins in milliseconds/bin
     */
    public int getLowResIdiBinScale() {
        return lowResIdiBinScale;
    }

    /**
     * Sets the size (along the horizontal axis) of the IDI bins, in
     * milliseconds/bin
     *
     * @param idiBin width of IDI bins in milliseconds/bin
     */
    public void setLowResIdiBinScale(int idiBin) {
        this.lowResIdiBinScale = idiBin;
    }

    /**
     * Returns the highest IDI bin number (horizontal axis) of the high-
     * resolution histogram, in milliseconds
     *
     * @return highest IDI bin number of high-res graph, in milliseconds
     */
    public int getMaxHighResBin() {
        return maxHighResBin;
    }

    /**
     * Sets the highest IDI bin number (horizontal axis) of the high-
     * resolution histogram, in milliseconds

     * @param maxHighResBin highest IDI bin number of high-res graph, in milliseconds
     */
    public void setMaxHighResBin(int maxHighRes) {
        this.maxHighResBin = maxHighRes;
    }

    /**
     * Returns the highest IDI bin number (horizontal axis) of the low-
     * resolution histogram, in milliseconds
     *
     * @return highest IDI bin number of low-res graph, in milliseconds
     */
    public int getMaxLowResBin() {
        return maxLowResBin;
    }

    /**
     * Sets the highest IDI bin number (horizontal axis) of the low-
     * resolution histogram, in milliseconds

     * @param maxLowRes highest IDI bin number of low-res graph, in milliseconds
     */
    public void setMaxLowResBin(int maxLowRes) {
        this.maxLowResBin = maxLowRes;
    }

    /**
     * Returns the size (along the vertical axis) of the time bins, in
     * milliseconds.  This is the value used to set the timer; whenever the
     * timer reaches this value, the histogram data is compiled and displayed
     * as a new line on the chart.
     *
     * @return size of time bins, in milliseconds
     */
    public int getTimeBinScale() {
        return timeBinScale;
    }

    /**
     * Sets the size (along the vertical axis) of the time bins, in
     * milliseconds.  This is the value used to set the timer; whenever the
     * timer reaches this value, the histogram data is compiled and displayed
     * as a new line on the chart.
     *
     * @param timeBin size of the time bins, in milliseconds
     */
    public void setTimeBinScale(int timeBin) {
        this.timeBinScale = timeBin;
    }

    /**
     * Gets the highest histogram count in the high-resolution colour bar
     *
     * @return histogram count corresponding to the red color
     */
    public int getMaxHighCount() {
        return maxHighCount;
    }

    /**
     * Sets the highest histogram count in the high-resolution colour bar
     *
     * @param histogram count corresponding to the red color
     */
    public void setMaxHighCount(int maxHighCount) {
        this.maxHighCount = maxHighCount;
    }

    /**
     * Gets the highest histogram count in the low-resolution colour bar
     *
     * @return histogram count corresponding to the red color
     */
    public int getMaxLowCount() {
        return maxLowCount;
    }

    /**
     * Sets the highest histogram count in the low-resolution colour bar
     *
     * @param histogram count corresponding to the red color
     */
    public void setMaxLowCount(int maxLowCount) {
        this.maxLowCount = maxLowCount;
    }

    /**
     * Gets the highest time bin value in the vertical axis
     *
     * @return the largest time bin to display, in milliseconds
     */
    public int getMaxTimeBin() {
        return maxTimeBin;
    }

    /**
     * Sets the highest time bin value to display in the vertical axis
     *
     * @param maxTimeBin the largest time bin to display on the vertical axis,
     * in milliseconds
     */
    public void setMaxTimeBin(int maxTimeBin) {
        this.maxTimeBin = maxTimeBin;
    }

    public File getOutputFilename() {
        return outputFilename;
    }

    public void setOutputFilename(File outputFilename) {
        this.outputFilename = outputFilename;
    }

    public boolean isOutputSaved() {
        return saveOutput;
    }

    public void setSaveOutput(boolean saveOutput) {
        this.saveOutput = saveOutput;
    }

	@Override
	protected IDI_DisplayParams clone()  {
		try {
			return (IDI_DisplayParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
    
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		try {
			Field field = this.getClass().getDeclaredField("saveOutput");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return saveOutput;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
