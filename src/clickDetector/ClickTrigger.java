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

package clickDetector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import Layout.PamAxis;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamView.PamColors;
import PamView.dialog.PamLabel;
import PamView.panel.JBufferedPanel;
import PamView.panel.PamPanel;
import PamView.panel.PamProgressBar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamRawDataBlock;
import soundtrap.STClickControl;

/**
 * 
 * @author Doug Gillespie
 *         <p>
 *         Display window for click detector trigger information.
 * 
 */
public class ClickTrigger extends ClickDisplay implements PamObserver {

	LevelMeters levelMeters;

	TriggerPlot triggerPlot;

	public JProgressBar[] progressBars;

	PamAxis xAxis;

	double axisMin = -20;

	double axisMax = 50;

	TriggerHistogram[] triggerHistogram;

	private double scaleMin, scaleMax, xScale, yScale;

	private int nBins;

	private PamDataBlock<TriggerLevelDataUnit> triggerDataBlock;
	private PamRawDataBlock rawDataBlock;

	private Object triggerLock = new Object();

	public ClickTrigger(ClickControl clickControl, ClickDisplayManager clickDisplayManager, 
			ClickDisplayManager.ClickDisplayInfo clickDisplayInfo) {

		super(clickControl, clickDisplayManager, clickDisplayInfo);

		this.clickControl = clickControl;

		setAxisPanel(new TriggerAxis());

		setPlotPanel(triggerPlot = new TriggerPlot());

		setEastPanel(levelMeters = new LevelMeters());

		//		clickControl.GetRawDataBlock().addObserver(this);

	}

	/**
	 * Constructor needed when creating the SoundTrap Click Detector - need to explicitly cast
	 * from STClickControl to ClickControl, or else constructor fails
	 * @param clickControl
	 * @param clickDisplayManager
	 * @param clickDisplayInfo
	 */
	public ClickTrigger(STClickControl clickControl, ClickDisplayManager clickDisplayManager, 
			ClickDisplayManager.ClickDisplayInfo clickDisplayInfo) {
		this((ClickControl) clickControl, clickDisplayManager, clickDisplayInfo);
	}


	public long getRequiredDataHistory(PamObservable o, Object arg) {
		return 0;
	}

	@Override
	public PamObserver getObserverObject() {
		return this;
	}
	
	public void addData(PamObservable obs, PamDataUnit newData) {
		// should get here everytime there is a new raw data unit so update the
		// appropriate bar.
		// check it's a channel we're dealing with
		if (obs == rawDataBlock) {
			RawDataUnit newRawData = (RawDataUnit) newData;
			if ((newRawData.getChannelBitmap() & clickControl.clickParameters.getChannelBitmap()) == 0) return;
			//if ((newRawData.getChannelBitmap() & clickControl.clickParameters.triggerBitmap) == 0) return;
			int chan = PamUtils.getSingleChannel(newRawData.getChannelBitmap());
			chan = PamUtils.getChannelPos(chan, clickControl.clickParameters.getChannelBitmap());
			double maxAmp = 0.;
			double[] rawData =  newRawData.getRawData();
			//			newData.
			for (int i = 0; i < rawData.length; i++) {
				maxAmp = Math.max(Math.abs(rawData[i]), maxAmp);
			}
			try {
				if (maxAmp <= 1E-12) {
					progressBars[chan].setValue(-100);
				} else {
					progressBars[chan].setValue((int) (20. * Math.log10(maxAmp)));
				}
			}
			catch (NullPointerException e) {
				// this really shouldn't be here !!!!
			}
		}
		else if (obs == triggerDataBlock) {
			displayTriggerHistogram(((TriggerLevelDataUnit) newData).getTriggerHistogram());
		}

	}

	@Override
	public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
		
	}
	
	public String getObserverName() {
		return "click detector trigger display";
	}

	public void displayTriggerHistogram(TriggerHistogram[] triggerHistogram) {
		// need to work out which trigger histos actually have anything in them.
		synchronized (triggerLock) {
			this.triggerHistogram = triggerHistogram;
			for (int i = 0; i < triggerHistogram.length; i++) {
				if (triggerHistogram[i] == null) {
					continue;
				}
				axisMin = triggerHistogram[i].getMinVal();
				axisMax = triggerHistogram[i].getMaxVal();
				xAxis.setRange(axisMin, axisMax);
				break;
			}
		}
		triggerPlot.repaint();
	}

	public void setSampleRate(float sampleRate, boolean notify) {
		// this.sampleRate = sampleRate;
	}

	@Override
	public void masterClockUpdate(long milliSeconds, long sampleNumber) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveSourceNotification(int type, Object object) {
		// don't do anything by default
	}


	class TriggerAxis extends PamPanel {

		int axisExtent = 0;

		TriggerAxis() {
			super();
			// PamColors.getInstance().registerComponent(this,
			// PamColors.PamColor.BORDER);
			xAxis = new PamAxis(10, 10, 300, 10, axisMin, axisMax, false,
					"Signal / Trigger Level (dB)", "%2.0f");
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			// just draw the axis along the bottom of the plot
			Rectangle r = getBounds();
			Insets insets = getInsets();
			Insets plotInsets = triggerPlot.getInsets();
			int newExtent = xAxis.getExtent(g, "0");
			if (newExtent != axisExtent) {
				setBorder(new EmptyBorder(insets.top, insets.left, newExtent,
						insets.right));
				axisExtent = newExtent;
			}

			xAxis.drawAxis(g, insets.left + plotInsets.left, r.height
					- insets.bottom, r.width - insets.right - plotInsets.right,
					r.height - insets.bottom);
		}

	}

	class TriggerPlot extends JBufferedPanel {

		@Override
		public void paintPanel(Graphics g, Rectangle clipRectangle) {
			//			super.paintComponent(g);
			synchronized (triggerLock) {
				TriggerHistogram anyHistogram = null;
				if (triggerHistogram == null)
					return;
				FontMetrics fontMetrics = g.getFontMetrics();
				double maxVal = 0;
				if (triggerHistogram.length == 0)
					return;
				for (int iChan = 0; iChan < triggerHistogram.length; iChan++) {
					if (triggerHistogram[iChan] == null) continue;
					for (int iVal = 0; iVal < triggerHistogram[iChan].getData().length; iVal++) {
						maxVal = Math.max(maxVal,
								triggerHistogram[iChan].getData()[iVal]);
					}
					anyHistogram = triggerHistogram[iChan];
				}

				if (anyHistogram == null) {
					return;
				}

				PamColors pamColors = PamColors.getInstance();
				Rectangle r = getBounds();
				Insets insets = getInsets();
				scaleMax = Math.log10(maxVal);
				scaleMin = scaleMax - 4;
				scaleMax *= 1.1;
				nBins = anyHistogram.getData().length;
				yScale = r.height / (scaleMax - scaleMin);
				xScale = (double) (r.width - insets.left - insets.right)
				/ (double) (nBins - 1);
				int x1, x2, y1, y2;
				double[] data;

				int chCount = 0;
				for (int iChan = 0; iChan < triggerHistogram.length; iChan++) {
					if (triggerHistogram[iChan] == null) continue;
					if ((1<<iChan & clickControl.clickParameters.triggerBitmap) == 0) continue;
					g.setColor(pamColors.getChannelColor(iChan));
					data = triggerHistogram[iChan].getData();
					x1 = BinToX(0);
					y1 = r.height - ValueToY(data[0]);
					for (int iVal = 1; iVal < data.length; iVal++) {
						x2 = BinToX(iVal);
						y2 = r.height - ValueToY(data[iVal]);
						g.drawLine(x1, y1, x2, y2);
						x1 = x2;
						y1 = y2;
					}
					/*
					 * And draw a wee line in the corner too for each channel ...
					 */
					y1 = 20 + 20 * chCount;
					x1 = 10;
					x2 = 30;
					g.drawLine(x1, y1, x2, y1);

					g.drawString("CH " + iChan,
							x2 + fontMetrics.charWidth('c') / 2, y1
							+ fontMetrics.getAscent() / 2);
					chCount++;
				}
				g.setColor(pamColors.getColor(PamColors.PamColor.PLAIN));
				x1 = BinToX(anyHistogram.getBin(0));
				g.drawLine(x1, r.height, x1, 0);
				// and draw on the trigger threshold line in red
				g.setColor(Color.RED);
				x1 = dBToX(clickControl.clickParameters.dbThreshold);
				g.drawLine(x1, r.height, x1, 0);
			}
		}

		private int dBToX(double dBVal) {
			return (int) ((dBVal - axisMin) / (axisMax - axisMin) * this.getWidth());
		}
	}

	private int BinToX(int bin) {
		return (int) (bin * xScale);
	}


	private int ValueToY(double value) {
		if (value <= 0.)
			return 0;
		return (int) ((Math.log10(value) - scaleMin) * yScale);
	}

	class LevelMeters extends PamPanel {

		LevelMeters() {
			super();
			createMeters();
		}

		protected void newSettings() {
			this.removeAll();
			createMeters();
		}
		private void createMeters() {
			int nChan = PamUtils.getNumChannels(clickControl.clickParameters.getChannelBitmap());
			JPanel barPanel = new PamPanel();
			JPanel subPanel;
			barPanel.setBorder(new EmptyBorder(10, 5, 10, 5));
			int nCol = 1;
			int nRow = 1;
			if (nChan < 4) {
				nRow = 1;
			}
			else if (nChan <= 8) {
				nRow = 2;
			}
			else if (nChan <= 16) {
				nRow = 4;
			}
			else {
				nRow = 4;
			}
			while (nCol * nRow < nChan) {
				nCol++;
			}
			barPanel.setLayout(new GridLayout(nRow, nCol));
//			PamColors.getInstance().registerComponent(barPanel,
//					PamColors.PamColor.BORDER);

			progressBars = new JProgressBar[nChan];

			int channel;
			for (int i = 0; i < nChan; i++) {
				subPanel = new PamPanel();
				subPanel.setLayout(new BorderLayout());
				subPanel.setBorder(new EmptyBorder(0, 2, 0, 2));
//				PamColors.getInstance().registerComponent(subPanel,
//						PamColors.PamColor.BORDER);
				progressBars[i] = new PamProgressBar(SwingConstants.VERTICAL, -60,
						0);
				progressBars[i].setValue(-60);
				channel = PamUtils.getNthChannel(i, clickControl.clickParameters.getChannelBitmap());
				subPanel.add(BorderLayout.NORTH, new PamLabel(Integer.toString(channel), JLabel.CENTER));
				subPanel.add(BorderLayout.CENTER, progressBars[i]);
				barPanel.add(subPanel);
//				PamColors.getInstance().registerComponent(progressBars[i], PamColor.BORDER);
			}

			setLayout(new GridLayout(1, 0));
			add(barPanel);
//			PamColors.getInstance().registerComponent(this,
//					PamColors.PamColor.BORDER);
		}

	}

	@Override
	public String getName() {
		return "Trigger";
	}

	@Override
	public void noteNewSettings() {
		levelMeters.newSettings();
	}

	public void removeObservable(PamObservable o) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pamStart() {
		if (clickControl.clickDetector.getParentDataBlock() == null) {
			return;
		}
		(rawDataBlock = (PamRawDataBlock) clickControl.clickDetector.getParentDataBlock()).addObserver(this);
		(triggerDataBlock = clickControl.clickDetector.getTriggerDataBlock()).addObserver(this);
	}
}
