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
package Spectrogram;

import PamUtils.Coordinate3d;
import PamUtils.PamCoordinate;
import PamView.GeneralProjector;

/**
 * Spectrogram projector for overlaying data onto the spectrogram bitmap
 * which will always be scaled 1:1 with regard to pixels per frequency or
 * time bin. 
 * @author Doug Gillespie
 * @see DirectDrawProjector
 *
 */
public class SpectrogramProjector extends GeneralProjector<TimeFrequencyPoint> {

	/**
	 * xScale is in pixels per millisecond. 
	 */
	private double xScale;

	private double yScale;

	private int pixOriginX = 0;

	private int pixOriginY = 0;

	private int specWidth = 1;

	private int specHeight = 1;

	private long timeOffsetMillis;
	
	/**
	 * @return the timeOffsetMillis
	 */
	public long getTimeOffsetMillis() {
		return timeOffsetMillis;
	}

	private SpectrogramDisplay spectrogramDisplay;
	
	private int channel;

	public SpectrogramProjector(SpectrogramDisplay spectrogramDisplay) {
		
		this.spectrogramDisplay = spectrogramDisplay;

		setParmeterType(0, GeneralProjector.ParameterType.TIME);

		setParmeterType(1, GeneralProjector.ParameterType.FREQUENCY);

	}

	/**
	 * 
	 * @param xScale pixels per millisecond
	 * @param yScale 2/fftLength
	 * @param specWidth display width
	 * @param specHeight display height
	 */
	public void setScales(double xScale, double yScale, int specWidth,
			int specHeight) {
		this.xScale = xScale;
		this.yScale = yScale;
		this.specWidth = specWidth;
		this.specHeight = specHeight;
	}

	/**
	 * Set the x and the time offset 
	 * @param timeOffsetMillis time offset in Java milliseconds
	 * @param timeOffsetPixs position in pixels
	 */
	public void setOffset(long timeOffsetMillis, int timeOffsetPixs) {
		this.timeOffsetMillis = timeOffsetMillis;
		pixOriginX = timeOffsetPixs;
	}

	@Override
	public Coordinate3d getCoord3d(TimeFrequencyPoint data) {
		return getCoord3d(data.getTimeMilliseconds(), data.getFrequency(), 0);
	}

	@Override
	public Coordinate3d getCoord3d(double d1, double d2, double d3) {
		return new Coordinate3d(dataToX(d1, d2), dataToY(d1, d2));
	}

	@Override
	public TimeFrequencyPoint getDataPosition(PamCoordinate screenPos) {
		double x = screenPos.getCoordinate(0);
		double y = screenPos.getCoordinate(1);
		double timeMillis = (x - pixOriginX)/xScale + timeOffsetMillis;
		double specDuration = this.specWidth/xScale;
		if (timeMillis > timeOffsetMillis + specDuration) {
			timeMillis -= specDuration;
		}
		if (timeMillis < 0) {
			timeMillis += specDuration;
		}
		double[] frequencyLimits = spectrogramDisplay.getSpectrogramParameters().frequencyLimits;
		double frequency = frequencyLimits[1] - y/specHeight*(frequencyLimits[1] - frequencyLimits[0]);
		return new TimeFrequencyPoint((long) timeMillis, frequency);
	}

	public double dataToX(double d1, double d2) {
		double x = ((d1 - timeOffsetMillis) * xScale) + pixOriginX
				- 2;
		while (x < 0) {
			x += specWidth;
		}
		while (x >= specWidth) {
			x -= specWidth;
		}
		return x;
	}

	public double dataToY(double d1, double frequency) {
//		return (int) (specHeight - (int) d2);
		/* 
		 * old (pre 19th Sept 2007) version of this was just using bins (see line above) and was 
		 * not taking any account of set scales on spectrogram. 
		 * Should really be using natural units (i.e. Hz for all input to projectors, so make that change
		 * now.
		 */
		// specHeight is the height in pixels. Get what these mean in terms of frequency from the display
		double[] frequencyLimits = spectrogramDisplay.getSpectrogramParameters().frequencyLimits;
		return specHeight * (frequencyLimits[1] - frequency) / (frequencyLimits[1] - frequencyLimits[0]);
	}

	/**
	 * @return the spectrogramDisplay
	 */
	public SpectrogramDisplay getSpectrogramDisplay() {
		return spectrogramDisplay;
	}

	/**
	 * @param channel the channel to set
	 */
	public void setChannel(int channel) {
		this.channel = channel;
	}

	/**
	 * @return the channel
	 */
	public int getChannel() {
		return channel;
	}

}
