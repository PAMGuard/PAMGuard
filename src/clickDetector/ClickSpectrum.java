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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import Filters.SmoothingFilter;
import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.FrequencyFormat;
import PamUtils.PamUtils;
import PamView.PamColors;
import PamView.dialog.PamLabel;
import PamView.panel.JBufferedPanel;
import PamView.panel.PamBorderPanel;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.superdet.SuperDetection;
import fftManager.FastFFT;
import soundtrap.STClickControl;

public class ClickSpectrum extends ClickDisplay implements PamObserver , PamSettings {

	private ClickControl clickControl;

	private SpectrumAxis spectrumAxisPanel;

	private SpectrumPlot spectrumPlotPanl;

	private SpectrumInfo spectrumInfo;

	private PamLabel cursorPos;

	private PamAxis frequencyAxis, amplitudeAxis;

	private ClickDetection storedClick;

	private float sampleRate;

	private double[][] storedSpectrum;

	private double[][] eventSpectrum;

	private double[][] eventSpectrumTemplates;

	private double[][] eventSpectrumTemplatesLog;

	private PamDataUnit lastEvent=null;

	private long lastUpdateTime=-1;

	private double[][] logSpectrum;

	private double[][] logEventSpectrum;

	private double[][] logTemplateSpectrum;

	private double maxVal;



	private ClickSpectrumParams clickSpectrumParams = new ClickSpectrumParams();
	private ClickSpectrumTemplateParams clickTemplateParams=new ClickSpectrumTemplateParams();
	private int lastchannelChoice=clickSpectrumParams.channelChoice;

	private Object storedSpectrumLock = new Object();

	protected boolean isViewer;

	private double[][] storedCepstrum;

	private double[][] eventCepstrum;



	//Graphics
	//For the current event lines
	final static float dash1[] = {3.0f};

	final static BasicStroke dashed =
			new BasicStroke(1.0f,
					BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_MITER,
					5.0f, dash1, 0.0f);

	final static BasicStroke dashedtmplate =
			new BasicStroke(2.0f,
					BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_MITER,
					5.0f, dash1, 0.0f);

	final static BasicStroke solid =
			new BasicStroke(1.5f);



	public ClickSpectrum(ClickControl clickControl, ClickDisplayManager clickDisplayManager, ClickDisplayManager.ClickDisplayInfo clickDisplayInfo) {

		super(clickControl, clickDisplayManager, clickDisplayInfo);

		this.clickControl = clickControl;

		sampleRate= clickControl.getClickDataBlock().getSampleRate();

		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;

		setAxisPanel(spectrumAxisPanel = new SpectrumAxis());

		setPlotPanel(spectrumPlotPanl = new SpectrumPlot());

		setNorthPanel(spectrumInfo = new SpectrumInfo());

		//		if (isViewer==true){
		//			setEastPanel(correlationInfo = new CorrelationValuesInfo());
		//			correlationInfo.addCorLabel();
		//		}

		frequencyAxis = new PamAxis(0, 0, 100, 100, 0, 1., 
				false, "Frequency kHz", "%1.0f");

		spectrumAxisPanel.setSouthAxis(frequencyAxis);

		amplitudeAxis = new PamAxis(0, 1, 100, 100, 0, 1., true, "Amplitude", "%3.1f");
		spectrumAxisPanel.setWestAxis(amplitudeAxis);

		clickControl.getClickDataBlock().addObserver(this);

		PamSettingManager.getInstance().registerSettings(this);

		sortWestAxis();

		sortSouthAxis();

	}

	/**
	 * Constructor needed when creating the SoundTrap Click Detector - need to explicitly cast
	 * from STClickControl to ClickControl, or else constructor fails
	 * @param clickControl
	 * @param clickDisplayManager
	 * @param clickDisplayInfo
	 */
	public ClickSpectrum(STClickControl clickControl, ClickDisplayManager clickDisplayManager, ClickDisplayManager.ClickDisplayInfo clickDisplayInfo) {
		this((ClickControl) clickControl, clickDisplayManager, clickDisplayInfo);
	}

	@Override
	public PamObserver getObserverObject() {
		return this;
	}

	private void sortWestAxis() {
		if (clickSpectrumParams.logScale) {
			amplitudeAxis.setRange(-clickSpectrumParams.logRange, -0);
			amplitudeAxis.setLabel("Amplitude (dB)");
			if (clickSpectrumParams.logRange%10 == 0) {
				amplitudeAxis.setInterval(10);
			}
			else {
				amplitudeAxis.setInterval(PamAxis.INTERVAL_AUTO);
			}
		}
		else {
			if (clickSpectrumParams.plotCepstrum) {
				amplitudeAxis.setRange(-1, 1);
			}
			else {
				amplitudeAxis.setRange(0, 1);
			}
			amplitudeAxis.setLabel("Amplitude (Linear)");
			amplitudeAxis.setInterval(PamAxis.INTERVAL_AUTO);
		}
		spectrumAxisPanel.SetBorderMins(10, 20, 10, 20);
		spectrumAxisPanel.repaint();

	}

	private void sortSouthAxis() {
		if (clickSpectrumParams.plotCepstrum) {
			int specLen = 1024;
			if (storedCepstrum == null || storedCepstrum.length == 0) {
				specLen = 1024;
			}
			else {
				specLen = storedCepstrum[0].length;
			}
			double maxT = specLen / sampleRate * 1000;
			frequencyAxis.setRange(0, maxT);
			frequencyAxis.setLabel("Interval ms");			
		}
		else {
			frequencyAxis.setRange(0, sampleRate / 2 / 1000);
			frequencyAxis.setLabel("Frequency kHz");
		}
	}


	class SpectrumAxis extends PamAxisPanel {

		SpectrumAxis() {
			this.SetBorderMins(10, 20, 10, 20);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

		}

		@Override
		public void setBorder(Border border) {
			super.setBorder(border);
			if (spectrumInfo != null) {
				Insets insets = this.getInsets();
				spectrumInfo.setBorder(new EmptyBorder(new Insets(5, insets.left, 0, 0)));
			}
		}
	}

	/**
	 * Create a closed normalised polygon from a double array. The size of the array is based on the window width. ONLY use for drawing spectrums. 
	 * @param data- datatoDraw
	 * @param maxVal- maximum value of this data
	 * @param xScale- the value of each pixel in x
	 * @param yScale-the value of each pixel in y
	 * @param log- whether thisis a log scale or not
	 * @return
	 **/
	protected static GeneralPath drawPolygon(double[] data, double maxVal, double xScale, double yScale, boolean log, Rectangle r){

		int  x1, y1;
		GeneralPath polygon=null;

		polygon = 
				new GeneralPath(Path2D.WIND_NON_ZERO  ,
						data.length+2);
		x1 = 0;
		if (log){
			y1 = (int) (yScale *(maxVal- data[0]));}
		else{
			y1 = r.height - (int) (yScale * data[0]);}

		polygon.moveTo(0.0,r.height);
		for (int i = 0; i < data.length; i++) {
			//sometimes you get an infinity and that might blue screen your computer!
			x1 = (int) (i * xScale);
			if (data[i]<-Double.MAX_VALUE || data[i]>Double.MAX_VALUE ){
				System.out.println("Click Spectrum: Inf value! ");
				//y1=0; 
				continue; 
			}
			else if (log){
				y1 = (int) (yScale *(maxVal- data[i]));
			}
			else{
				y1 = r.height - (int) (yScale * data[i]);
			}
			//System.out.println("data " + i + " " + data[i] + " yScale: "+ yScale +  " y1 "+ y1);

			polygon.lineTo( x1, y1);
		}

		polygon.lineTo(x1, r.height);
		polygon.closePath();

		return polygon;
	}



	class SpectrumPlot extends JBufferedPanel {


		public SpectrumPlot() {
			super();
			SPMouseListener spml = new SPMouseListener();
			this.addMouseMotionListener(spml);
			this.addMouseListener(spml);
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			addMouseListener(new MouseFuncs());
		}

		class SPMouseListener extends MouseAdapter {

			@Override
			public void mouseExited(MouseEvent e) {
				cursorPos.setText(spectrumInfo.emptyText);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				Rectangle r = getBounds();
				double xNum = frequencyAxis.getDataValue(e.getX());
				double yNum = amplitudeAxis.getDataValue(e.getY());
				String xType, yType;
				String txt;
				yType = clickSpectrumParams.logScale ? "dB": "";
				if (clickSpectrumParams.plotCepstrum) {
					xType = clickSpectrumParams.plotCepstrum ? "ms" : "kHz";
					txt = String.format("Cursor %5.2f %s, %3.1f %s", xNum, xType, yNum, yType);
				}
				else {
					txt = String.format("Cursor %s, %3.1f %s", FrequencyFormat.formatFrequency(xNum*1000, true), yNum, yType);
				}

				//				double xScale = clickControl.clickDetector.getSampleRate() / 2. / (r.width - 1);
				//				double f = e.getX() * xScale;
				//				String txt = String.format("Cursor %s", FrequencyFormat.formatFrequency(f, true));
				//				if (clickSpectrumParams.logScale) {
				//					double yScale = clickSpectrumParams.logRange / r.height;
				//					txt += String.format(", -%3.1f dB", e.getY() * yScale);
				//				}
				cursorPos.setText(txt);
			}

		}

		@Override
		public void paintPanel(Graphics g, Rectangle clipRect) {
			//only allow antialiasing in pamguard viewer mode. Want drawing to be as fast as possible in online mode
			if (isViewer){
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING,
						RenderingHints.VALUE_RENDER_QUALITY);
			}

			if (clickSpectrumParams.logScale) {
				paintLogSpectrum(g, clipRect);
			}
			else {
				paintLinSpectrum(g, clipRect);
			}

		}

		private double maxLogVal;
		private boolean lastCepChoice;


		/**Paint the line spectrum
		 * 
		 * @param g
		 * @param clipRect
		 */
		private void paintLinSpectrum(Graphics g, Rectangle clipRect) {


			double[][] clickLineData;
			double[][] eventLineData;
			synchronized (storedSpectrumLock) {
				if (storedSpectrum == null || storedSpectrum.length == 0) return;

				if (clickSpectrumParams.plotCepstrum) {
					clickLineData = storedCepstrum;
					eventLineData = eventCepstrum;
				}
				else {
					clickLineData = storedSpectrum;
					eventLineData = eventSpectrum;
				}

				if (!isEventClick(storedClick)) {
					eventLineData = null;
				}

				// work out the scales, mins and max's, etc.
				maxVal = 0;

				for (int iChan = 0; iChan < clickLineData.length; iChan++) {
					for (int i = 0; i < clickLineData[iChan].length; i++){
						maxVal = Math.max(maxVal, clickLineData[iChan][i]);
//						System.out.println(eventLineData == null ? "Event is null" : ("Event length is " + eventLineData.length));
						if (!clickSpectrumParams.plotCepstrum && eventLineData!=null) {
							if (eventLineData[iChan]!=null && isViewer && clickSpectrumParams.showEventInfo ){
								if( i<eventLineData[iChan].length){
									maxVal = Math.max(maxVal, eventLineData[iChan][i]);
								}
							}
						}
					}
				}

				drawSpectrum( g, clipRect, clickLineData, eventLineData);
			}
		}
		/**
		 *Draw the shapes for the line spectrum. Eventually this function should be integrated with drawLogSpectrum().
		 * @param g
		 * @param clipRect
		 * @param eventLineData 
		 * @param clickLineData 
		 */
		private void drawSpectrum(Graphics g, Rectangle clipRect, double[][] clickLineData, double[][] eventLineData){

			double xScale, yScale;
			int x0, y0, x1, y1;
			Color channelColour;
			Rectangle r = getBounds();
			Graphics2D g2=(Graphics2D) g;

			double[] scaledData;

			double scale = 1./(maxVal*1.1);
			if (isViewer ){

				if (eventLineData!=null && clickSpectrumParams.showEventInfo && !clickSpectrumParams.plotCepstrum){	

					g2.setStroke(dashed);

					for (int iChan = 0; iChan < eventLineData.length; iChan++) {
						scaledData = new double[eventLineData[iChan].length];
						for (int i = 0; i < scaledData.length; i++) {
							scaledData[i] = r.getHeight()-amplitudeAxis.getPosition(eventLineData[iChan][i]*scale);
						}
						xScale = (double) r.width / (double) (eventLineData[iChan].length - 1);
						yScale = r.height / (maxVal * 1.1);
						GeneralPath polygon = drawPolygon(scaledData,1,xScale,1,false,getBounds());
						channelColour=PamColors.getInstance().getChannelColor(iChan);
						g2.setPaint(new Color(channelColour.getRed(),channelColour.getGreen(),channelColour.getBlue(),35));
						g2.fill(polygon);
						g2.setPaint(PamColors.getInstance().getChannelColor(iChan));
						g2.draw(polygon);
					}
				}

				if (!clickSpectrumParams.plotCepstrum && 
						clickTemplateParams.clickTemplateArray.size()>0 && 
						eventSpectrumTemplates !=null) {

					g2.setStroke(dashedtmplate);

					for (int i = 0; i < eventSpectrumTemplates.length; i++) {
						if (eventSpectrumTemplates[i]!=null && clickTemplateParams.clickTempVisible.get(i)==true){

							xScale = (double) r.width / (double) (eventSpectrumTemplates[i].length - 1);
							yScale = r.height / (1.1);
							GeneralPath polygon2 = drawPolygon(eventSpectrumTemplates[i],1.1,xScale,yScale,false,getBounds());
							g2.setPaint(clickTemplateParams.clickTemplateArray.get(i).getColour());
							g2.draw(polygon2);
						}
					}
				}

			}

			xScale = (double) r.width / (double) (clickLineData[0].length - 1);
			yScale = r.height / (maxVal * 1.1);

			for (int iChan = 0; iChan < clickLineData.length; iChan++) {
				g2.setStroke(solid);
				g.setColor(PamColors.getInstance().getChannelColor(iChan));
				x0 = 0;
				if (clickLineData[iChan].length == 0) {
					continue;
				}
				y0 = r.height - (int) (yScale * clickLineData[iChan][0]);
				for (int i = 1; i < clickLineData[iChan].length; i++) {
					scaledData = new double[clickLineData.length];
					x1 = (int) (i * xScale);
					//					y1 = r.height - (int) (yScale * clickLineData[iChan][i]);
					y1 = (int) amplitudeAxis.getPosition(clickLineData[iChan][i]*scale);
					g2.drawLine(x0, y0, x1, y1);
					x0 = x1;
					y0 = y1;
				}
			}

		}
		/**
		 * Paint the log spectrum
		 * @param g
		 * @param clipRect
		 */
		private void paintLogSpectrum(Graphics g, Rectangle clipRect) {

			double[][] clickLineData = null;
			double[][] eventLineData = null;
			synchronized (storedSpectrumLock) {
				if (storedSpectrum == null || storedSpectrum.length == 0 || storedSpectrum[0].length == 0) return;

				if (clickSpectrumParams.plotCepstrum) {
					clickLineData = storedCepstrum;
					eventLineData = eventCepstrum;
				}
				else {
					clickLineData = storedSpectrum;
					eventLineData = eventSpectrum;
				}

				if (!isEventClick(storedClick)) {
					eventLineData = null;
				}

				//System.out.println("paintLogSpectrum.eventLineData: 1" + eventLineData[0].length);


				if (logSpectrum == null || clickSpectrumParams.plotCepstrum != lastCepChoice || eventSpectrumTemplatesLog!=null) {
					double temp;
					logSpectrum = new double[clickLineData.length][clickLineData[0].length];
					if (eventLineData!=null && isViewer ){
						logEventSpectrum = new double[eventLineData.length][eventLineData[0].length];
					}
					if (eventSpectrumTemplatesLog!=null && isViewer ){
						logTemplateSpectrum = new double[eventSpectrumTemplatesLog.length][];
					}
					maxLogVal = 10*Math.log10(clickLineData[0][0]);

					for (int iChan = 0; iChan < clickLineData.length; iChan++) {
						//System.out.println("Log Event click size: " + clickLineData[iChan].length);
						for (int i = 0; i < clickLineData[iChan].length; i++){
							if (clickLineData[iChan][i] > 0){
								logSpectrum[iChan][i] = temp = 10 * Math.log10(clickLineData[iChan][i]);
								maxLogVal = Math.max(maxLogVal, temp);
								//System.out.println(" i: " + i + " "+ logEventSpectrum[iChan][i]  );
							}
						}

						//seperated this out and the spectrums can be different lengths. 
						if (eventLineData!=null && isViewer &&clickSpectrumParams.showEventInfo){
							for (int i = 0; i < eventLineData[iChan].length; i++){
								if( i<eventLineData[iChan].length){
									logEventSpectrum[iChan][i] = temp = 10 * Math.log10(eventLineData[iChan][i]);
									maxLogVal = Math.max(maxLogVal,  temp);
								}
							}
						}



					}

					//System.out.println("paintLogSpectrum.eventLineData: 2 " + eventLineData[0].length);


					if (!clickSpectrumParams.plotCepstrum && eventSpectrumTemplatesLog!=null && isViewer ){

						for(int i=0; i<eventSpectrumTemplatesLog.length;i++){
							double[] logTemplate=new double[eventSpectrumTemplatesLog[i].length];
							for (int j = 0; j < eventSpectrumTemplatesLog[i].length; j++){
								logTemplate[j] = temp = eventSpectrumTemplatesLog[i][j];
							}
							logTemplateSpectrum[i]=logTemplate;
						}
					}
				}

				lastCepChoice = clickSpectrumParams.plotCepstrum;
				drawLogSpectrum(g,clipRect,clickLineData, eventLineData);
			}
		}


		/**
		 * Draw the shapes for the log spectrum. This function should eventually be integrated with the drawSpectrum() function.
		 * @param g
		 * @param clipRect
		 * @param eventLineData 
		 * @param clickLineData 
		 */
		public void drawLogSpectrum(Graphics g, Rectangle clipRect, double[][] clickLineData, double[][] eventLineData){

			Rectangle r = getBounds();
			double xScale, yScale, scaleLim;
			int x0, y0, x1, y1;
			Color channelColour;
			Graphics2D g2=(Graphics2D) g;

			//System.out.println("drawLogSpectrum.eventLineData: "+eventLineData[0].length);


			//show an average log spectrum
			if (eventLineData!=null && isViewer && clickSpectrumParams.showEventInfo && !clickSpectrumParams.plotCepstrum){

				xScale = (double) r.width / (double) (eventLineData[0].length - 1);
				scaleLim = Math.abs(clickSpectrumParams.logRange);
				yScale = r.height / Math.abs(scaleLim);

				g2.setStroke(dashed);

				for (int iChan = 0; iChan < logEventSpectrum.length; iChan++) {
					//System.out.println("Draw log spectrum: "+logEventSpectrum[iChan].length);

					GeneralPath polygon=drawPolygon(logEventSpectrum[iChan],maxLogVal,xScale,yScale,true, getBounds());
					channelColour=PamColors.getInstance().getChannelColor(iChan);
					g2.setPaint(new Color(channelColour.getRed(),channelColour.getGreen(),channelColour.getBlue(),35));
					g2.fill(polygon);
					g2.setPaint(PamColors.getInstance().getChannelColor(iChan));
					g2.draw(polygon);
				}
			}

			//show custim templates
			if (clickTemplateParams.clickTemplateArray.size()>0 && logTemplateSpectrum !=null){
				g2.setStroke(dashedtmplate);
				for (int i = 0; i < logTemplateSpectrum.length; i++) {
					if (logTemplateSpectrum[i]!=null && clickTemplateParams.clickTempVisible.get(i)==true){
						xScale = (double) r.width / (double) (logTemplateSpectrum[i].length - 1);
						scaleLim = Math.abs(clickSpectrumParams.logRange);
						yScale = r.height / Math.abs(scaleLim);
						GeneralPath polygon2 = drawPolygon(logTemplateSpectrum[i],0,xScale,yScale,true,r);
						g2.setPaint(clickTemplateParams.clickTemplateArray.get(i).getColour());
						g2.draw(polygon2);
					}
				}
			}


			xScale = (double) r.width / (double) (clickLineData[0].length - 1);
			scaleLim = Math.abs(clickSpectrumParams.logRange);
			yScale = r.height / Math.abs(scaleLim);

			for (int iChan = 0; iChan < logSpectrum.length; iChan++) {
				//System.out.println("Draw click spectrum: "+logSpectrum[iChan].length);
				g2.setStroke(solid);
				g.setColor(PamColors.getInstance().getChannelColor(iChan));
				x0 = 0;
				y0 = (int) (yScale * (maxLogVal-logSpectrum[iChan][0]));
				for (int i = 1; i < logSpectrum[iChan].length; i++) {
					x1 = (int) (i * xScale);
					y1 = (int) (yScale * (maxLogVal-logSpectrum[iChan][i]));
					g.drawLine(x0, y0, x1, y1);
					x0 = x1;
					y0 = y1;
				}
			}

		}

		private FastFFT cepFFT;
		private double[][] getLineData(double[][] spectrogramData) {
			if (!clickSpectrumParams.plotCepstrum) {
				return spectrogramData;
			}
			else {
				return storedCepstrum;
			}
			//			else {
			//				// calculate a cepstrum of the data instead.
			//				if (spectrogramData == null || spectrogramData.length == 0) {
			//					return null;
			//				}
			//				int fftLen = spectrogramData[0].length;
			//				int lFFTLen = FastFFT.log2(fftLen);
			//				double[][] cepData = new double[spectrogramData.length][fftLen];
			////				double[] lData = new double[spectrogramData[0].length];
			//				Complex[] compData = Complex.allocateComplexArray(fftLen*2);
			//				double specMax = 0;
			//				double cepMax = 0;
			//				if (cepFFT == null) {
			//					cepFFT = new FastFFT();
			//				}
			//				for (int iChan = 0; iChan < spectrogramData.length; iChan++) {
			//					for (int i = 0; i < fftLen; i++) {
			//						specMax = Math.max(specMax, spectrogramData[iChan][i]);
			//						compData[i].real = Math.log(spectrogramData[iChan][i]);
			//						compData[i].imag = 0;
			//						compData[fftLen*2-1-i].assign(compData[i]);
			//					}
			//					cepFFT.ifft(compData, lFFTLen+1);
			//					for (int i = 0; i < cepData[0].length; i++) {
			//						double rVal = compData[i].magsq();
			//						cepData[iChan][i] = rVal;
			//						cepMax = Math.max(cepMax, rVal);
			//					}
			//					// scale the data to be same amplitude as the spectrum data. 
			//					double scale = specMax/cepMax;
			//					for (int i = 0; i < cepData[0].length; i++) {
			//						cepData[iChan][i] *= scale;
			//					}
			//				}
			//				return cepData;
			//			}
		}


	}



	class SpectrumInfo extends PamBorderPanel {

		String emptyText = "Move cursor over plot for frequency information";
		public SpectrumInfo() {
			super();
			setLayout(new BorderLayout());
			add(BorderLayout.CENTER, cursorPos = new PamLabel(emptyText));
			setBorder(new EmptyBorder(new Insets(spectrumAxisPanel.getInsets().left, 2, 2, 2)));
		}

	}

	//	class CorrelationValuesInfo extends PamBorderPanel {
	//
	//		String noVal="-";
	//		int y=0;
	//		GridBagConstraints c;
	//		ArrayList<PamLabel> labels=new ArrayList<PamLabel>();
	//		
	//		public CorrelationValuesInfo() {
	//			super();
	//			setLayout(new GridBagLayout());
	//			 c = new PamGridBagContraints();
	//			 c.gridy = 0;
	//		
	//		}
	//		
	//		public void addCorLabel(){
	//			 c.gridy = y;
	//			PamLabel corValue = new PamLabel();
	//			this.add(corValue = new PamLabel(noVal),c);
	//			this.labels.add(corValue);
	//			this.y=y+2;
	//			System.out.println("y: "+y);
	//		}
	//
	//	}

	class MouseFuncs extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			showMenu(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			showMenu(e);
		}

		private void showMenu(MouseEvent e) {
			if (e.isPopupTrigger()) {
				JPopupMenu menu = new JPopupMenu();
				JMenuItem menuItem = new JMenuItem("Plot options ...");
				menuItem.addActionListener(new PlotOptions());
				menu.add(menuItem);
				JCheckBoxMenuItem logScale = new JCheckBoxMenuItem("Log scale");
				logScale.setSelected(clickSpectrumParams.logScale);
				logScale.addActionListener(new LogScale());

				if (isViewer){
					menu.add(logScale);
					menuItem = new JMenuItem("Manage templates...");
					menuItem.addActionListener(new EditTemplates());
					menu.add(menuItem);
				}


				menuItem = new JMenuItem("Add template...");
				menuItem.addActionListener(new AddTemplate());
				menu.add(menuItem);

				menuItem = new JMenuItem("Clear templates");
				menuItem.addActionListener(new ClearTemplates());
				menu.add(menuItem);

				menu.add(getCopyMenuItem());
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}


	private class LogScale implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			clickSpectrumParams.logScale = !clickSpectrumParams.logScale;
			sortWestAxis();
			repaint(10);
		}
	}


	private class AddTemplate implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			addTemnplateDialog();
		}
	}

	private class ClearTemplates implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			clearTemplates() ;
		}
	}

	private class EditTemplates implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			createTemplateOptions();
		}
	}

	private class PlotOptions implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			plotOptions();
		}
	}

	private void plotOptions() {
		Point pt = spectrumAxisPanel.getLocationOnScreen();
		pt.x += 10;
		pt.y += 20;
		ClickSpectrumParams newParams = ClickSpectrumDialog.showDialog(
				clickControl.getGuiFrame(), pt, this, clickSpectrumParams);
		if (newParams != null) {
			if (newParams.plotCepstrum) {
				newParams.logScale = false;
			}
			clickSpectrumParams = newParams.clone();
			sortWestAxis();
			sortSouthAxis();
			newClick(storedClick); // may have to re-do spectrum data. 		
			getFrame().setTitle(getName());
		}
	}

	private void createTemplateOptions() {
		Point pt = spectrumAxisPanel.getLocationOnScreen();
		pt.x += 10;
		pt.y += 20;
		ClickSpectrumTemplateParams newTempParams = ClickSpectrumTemplateEditDialog.showDialog(
				clickControl.getGuiFrame(), pt, this, clickTemplateParams,clickControl);
		if (newTempParams!=null){
			clickTemplateParams = newTempParams.clone();
			sortWestAxis();
			newClick(storedClick);
		}

	}

	private void addTemnplateDialog() {
		Point pt = spectrumAxisPanel.getLocationOnScreen();
		pt.x += 10;
		pt.y += 20;
		ClickSpectrumTemplateParams newTempParams = ClickSpectrumTemplateDialog.showDialog(
				clickControl.getGuiFrame(), pt, this, clickTemplateParams);
		if (newTempParams!=null){
			clickTemplateParams = newTempParams.clone();
			sortWestAxis();
			newClick(storedClick);
		}

	}

	public void clearTemplates(){
		clickTemplateParams.clickTemplateArray=new ArrayList<ClickTemplate>();
		clickTemplateParams.clickTempVisible=new ArrayList<Boolean>();
		newClick(storedClick);
	}

	@Override
	public String getName() {
		if (clickSpectrumParams.plotCepstrum) {
			return "Click Cepstrum";
		}
		else {
			return "Click Spectrum";
		}
	}

	public void newClick(ClickDetection newClick) {
		showClick(newClick);

	}

	protected void showClick(ClickDetection newClick) {
		if (newClick==null) return; 
		synchronized (storedSpectrumLock) {
			storedClick = newClick;
			logSpectrum = null;
			double[][] tempSpec = new double[newClick.getNChan()][];
			double[][] tempCep = new double[newClick.getNChan()][];
			/**
			 * Muck with FFT lengths a bit to ensure they are all the same length
			 * as dictated by the first channel returned. 
			 */
			int fftLen = 0;
			for (int iChan = 0; iChan < storedClick.getNChan(); iChan++) {
				tempSpec[iChan] = storedClick.getPowerSpectrum(iChan, clickControl.getClickParameters().maxLength);
				//System.out.println("FFT length; "+storedClick.getWaveData()[0].length);
				//System.out.println(String.format("TempSpec click %d is %d points", storedClick.clickNumber, tempSpec[iChan].length));
				fftLen = tempSpec[iChan].length*2;
				tempCep[iChan] = storedClick.getCepstrum(iChan, fftLen);
				//				crossNormalise(tempSpec[iChan], tempCep[iChan]);
			}

			if (clickSpectrumParams.channelChoice == ClickSpectrumParams.CHANNELS_SINGLE) {
				storedSpectrum = tempSpec;
				storedCepstrum = tempCep;
			}
			else {
				storedSpectrum = new double[1][];
				int specLen = tempSpec[0].length;
				storedSpectrum[0] = Arrays.copyOf(tempSpec[0],specLen);
				for (int iChan = 1; iChan < tempSpec.length; iChan++) {
					for (int i = 0; i < specLen; i++) {
						storedSpectrum[0][i] += tempSpec[iChan][i];
					}
				}
				storedCepstrum = new double[1][];
				int cepLen = tempCep[0].length;
				storedCepstrum[0] = Arrays.copyOf(tempCep[0], cepLen);
				for (int iChan = 1; iChan < tempCep.length; iChan++) {
					for (int i = 0; i < cepLen; i++) {
						storedCepstrum[0][i] += tempCep[iChan][i];
					}
				}				
			}
			if (clickSpectrumParams.smoothPlot && clickSpectrumParams.plotSmoothing > 1) {
				for (int i = 0; i < storedSpectrum.length; i++) {
					storedSpectrum[i] = SmoothingFilter.smoothData(storedSpectrum[i], clickSpectrumParams.plotSmoothing);
				}
			}
			//calculating an event spectrum can take a very long time so we don't delete the last event incase a user
			//is clicking between one event and un-annotated clicks so whether to draw an event is handled in the paint functions
			if (isViewer && this.clickSpectrumParams.showEventInfo){
				getEventClick(newClick);
			}
			if (isViewer) getTemplateClick();
			sortSouthAxis();
		}

		spectrumPlotPanl.repaint(100);
		spectrumAxisPanel.repaint(100);
	}

	/**
	 * Does the click belong to an event.
	 */
	private boolean isEventClick(ClickDetection newClick) {
		if (newClick.getSuperDetection(0)==null || !newClick.getSuperDetection(0).equals(lastEvent) ) {
			return false;
		}
		return true;
	}


	/**
	 * Normalis so that the amplitude of the cepstrum is the same as that of the spectrum. 
	 * @param ds
	 * @param ds2
	 */
	private void crossNormalise(double[] spec, double[] cep) {
		double specMax = 0; 
		double cepMax = 0;
		for (int i = 0; i < spec.length; i++) {
			specMax = Math.max(specMax, spec[i]);
		}
		for (int i = 0; i < cep.length; i++) {
			cepMax = Math.max(cepMax, cep[i]);
		}
		double scale = specMax/cepMax;
		for (int i = 0; i < cep.length; i++) {
			cep[i] *= scale;
		}
	}


	public void getEventClick(ClickDetection newClick){

		if (newClick==null){
			return;
		}

		ArrayList<double[]> eventFFTs;
		double[][] meanEvent;
		int cepLength = FastFFT.nextBinaryExp(clickControl.clickParameters.maxLength);
		int fftLength=cepLength;
		int[] hydrophoneMap=PamUtils.getChannelArray(newClick.getChannelBitmap());


		SuperDetection currentEvent;
		//there seems to be an issue here if a click is removed from an event then it returns an error when using the getSuperDetection function
		currentEvent = newClick.getSuperDetection(0);
		if (currentEvent==null){
			return;
		}

		//		if (currentEvent==lastEvent && lastchannelChoice == clickSpectrumParams.channelChoice && lastUpdateTime==newClick.getLastUpdateTime()){
		//			//System.out.println("last event==currentevent: "+currentEvent);
		//			//System.out.println("lastUpdateTime: "+newClick.getLastUpdateTime());
		//			return;
		//		}

		lastEvent=null;
		eventSpectrum=null;
		eventCepstrum = null;
		lastUpdateTime=-1;

		lastchannelChoice = clickSpectrumParams.channelChoice;
		lastEvent=currentEvent;
		lastUpdateTime=newClick.getLastUpdateTime();
		int subDetectionN=currentEvent.getSubDetectionsCount(); 
		int totalCepChannels = 0;
		if (subDetectionN > 1) {
			//			if (clickSpectrumParams.plotCepstrum) {
			eventCepstrum = new double[1][cepLength];
			double[] clickCepstrum;
			for (int i = 0; i < subDetectionN; i++) {
				ClickDetection aClick = (ClickDetection) currentEvent.getSubDetection(i);
				if (aClick == null) continue;
				int nChan = aClick.getNChan();
				for (int c = 0; c < nChan; c++) {
					clickCepstrum = aClick.getCepstrum(c, cepLength);
					totalCepChannels++;
					for (int s = 0; s < cepLength; s++) {
						eventCepstrum[0][s] += clickCepstrum[s];
					}
				}
			}
			for (int s = 0; s < cepLength; s++) {
				eventCepstrum[0][s] /= totalCepChannels;
			}
			//			}
			//			else {
			//System.out.println("new click number of channels: "+newClick.getNChan());
			eventSpectrum=new double[newClick.getNChan()][fftLength];
			eventFFTs=new ArrayList<double[]>();

			double[] clickFFT;
			int n;
			for (int iChan = 0; iChan < newClick.getNChan(); iChan++) {
				n=1;
				for (int i=0; i<subDetectionN;i++){
					ClickDetection clickr=(ClickDetection) currentEvent.getSubDetection(i);
					if (clickr==null) continue; 
					if (containsChannel(clickr,  hydrophoneMap[iChan])){
						//System.out.println("NumberOfChannels: "+clickr.getNChan()+ "  TotalChan: "+newClick.getNChan()  +"chan: "+hydrophoneMap[iChan]);
						//have to be careful here..when get power spectra we refer to the group channel number, not the real channel number	
						clickFFT=clickr.getPowerSpectrum(iChan,fftLength);
						//System.out.println("Click fft: length: " + clickFFT.length);
						eventFFTs.add(clickFFT);	
					}
				}
				eventSpectrum[iChan]=meanSpectrum(eventFFTs);
				//System.out.println("Create event: "+eventSpectrum[iChan].length);
			}

			if (clickSpectrumParams.channelChoice == ClickSpectrumParams.CHANNELS_MEANS) {
				meanEvent=new double[1][eventSpectrum[0].length];
				for (int i = 0; i <eventSpectrum[0].length; i++) {
					for (int iChan=0; iChan<newClick.getNChan();iChan++){
						meanEvent[0][i]+=eventSpectrum[iChan][i];
					}
				}
				eventSpectrum=meanEvent;
			}
			//			}
		}

		else{
			eventSpectrum=null;
			return;
		}		
	}

	private boolean containsChannel(ClickDetection click, int channel){
		boolean containsChan=false; 
		int[] hydrophoneMap=PamUtils.getChannelArray(click.getChannelBitmap());
		for (int i=0; i<hydrophoneMap.length; i++){
			if (hydrophoneMap[i]==channel) containsChan=true;
		} 		
		return containsChan;
	}


	/**
	 * This function converts a ClickTemplate to an fft which corresponds to the correct samplerate used in the current click data.
	 * @param clickTemplate
	 * @return
	 */
	public double[] convertTemplate(ClickTemplate clickTemplate, boolean log){
		sampleRate= clickControl.getClickDataBlock().getSampleRate();
		float sampleRatetempl=clickTemplate.getSampleRate();
		ArrayList<Double> fftData;
		if (log)  fftData=clickTemplate.getSpectrumLog();
		else fftData=clickTemplate.getSpectrum();

		if (fftData==null){
			return null;
		}

		int fftDatalength=fftData.size();

		int numberOfBins=Math.round((sampleRate/sampleRatetempl)*fftDatalength);

		if (numberOfBins>50000){
			return null;
		}


		double[] dfftData=new double[numberOfBins];

		if (fftData.size()>1 ){
			for (int i=0; i<numberOfBins; i++){
				if (fftDatalength>i){
					dfftData[i]=fftData.get(i);
				}
				else{
					dfftData[i]=0;
				}
			}

			return dfftData;

		}

		if (fftData.size()==0 ){
			System.out.println("Error: Click template in wrong format");
			return null;
		}

		return null;
	}


	public void getTemplateClick(){

		ArrayList<ClickTemplate> clickTemplates=clickTemplateParams.clickTemplateArray;
		if (clickTemplates==null)
		{
			eventSpectrumTemplates=null;
			eventSpectrumTemplatesLog=null;
			return;
		}

		if (clickTemplates.size()==0)
		{
			eventSpectrumTemplates=null;
			eventSpectrumTemplatesLog=null;
			return;
		}


		double[][] clickTemplatesd=new double[clickTemplates.size()][];
		double[][] clickTemplatesdLog=new double[clickTemplates.size()][];
		for (int i=0; i<clickTemplates.size(); i++){
			double[] fftd= convertTemplate( clickTemplates.get(i),false);
			double[] fftdLog= convertTemplate( clickTemplates.get(i),true);
			if (fftd!=null){
				clickTemplatesd[i]=normailiseSpectrum(fftd);
				clickTemplatesdLog[i]=fftdLog;

				double[] x=normailiseSpectrum(fftd);
				//			for (int j=0; j<fftd.length;j++){
				//				System.out.print(x[j]+",  h");
				//			}

			}
		}

		eventSpectrumTemplates=clickTemplatesd;
		eventSpectrumTemplatesLog=clickTemplatesdLog;
	}


	public double[] meanSpectrum(ArrayList<double[]> eventSpectrums){

		if (eventSpectrums==null){
			return null;
		}

		if (eventSpectrums.size()==0){
			return null;
		}


		int N=eventSpectrums.size();
		double sum;
		double[] meanSpectrum=new double[eventSpectrums.get(0).length];

		for (int i=0; i<eventSpectrums.get(0).length; i++){
			sum=0;
			for (int j=0; j<eventSpectrums.size(); j++){
				//if (eventSpectrums[j].length<i)	
				sum+=eventSpectrums.get(j)[i];
			}
			meanSpectrum[i]=sum/N;
		}

		return meanSpectrum;
	}


	public double[] normailiseSpectrum(double[] Spectrum){

		if (Spectrum==null){
			return null;
		}

		double max=-Double.MAX_VALUE;
		double val;

		for (int i=0; i<Spectrum.length; i++){
			if (max<Spectrum[i]){
				max=Spectrum[i];	
			}	
		}

		for (int i=0; i<Spectrum.length; i++){
			val=Spectrum[i]/max;
			Spectrum[i]=val;
		}

		return Spectrum;
	}



	public double[][] normailiseSpectrum(double[][] eventSpectrums){

		if (eventSpectrums==null){
			return null;
		}

		double max=0;
		double val;

		for (int i=0; i<eventSpectrums.length; i++){
			for (int j=0; j<eventSpectrums[i].length; j++){

				if (max<eventSpectrums[i][j]){
					max=eventSpectrums[i][j];	
				}
			}	
		}
		for (int i=0; i<eventSpectrums.length; i++){
			for (int j=0; j<eventSpectrums[i].length; j++){
				val=eventSpectrums[i][j]/max;
				eventSpectrums[i][j]=val;
			}	
		}

		return eventSpectrums;
	}

	/**
	 * Take two double arrays, not necessarily the same frequency or same number of bins and correlate;
	 * @return
	 */
	public double correlate(double[] wave1, double[] wave2){

		return lastchannelChoice;

	}



	@Override
	public String getObserverName() {
		return getName();
	}

	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		return 0;
	}

	@Override
	public void noteNewSettings() {

	}

	@Override
	public void removeObservable(PamObservable o) {

	}

	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		this.sampleRate = sampleRate;
		sortSouthAxis();
	}

	@Override
	public void masterClockUpdate(long milliSeconds, long sampleNumber) {
		// TODO Auto-generated method stub

	}


	@Override
	public void addData(PamObservable o, PamDataUnit arg) {

		if (clickDisplayManager.isBAutoScroll() && !isViewer) {
			ClickDetection click = (ClickDetection) arg;
			newClick(click);

		}

	}

	@Override
	public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub

	}

	public ClickDetection getStoredClick(){
		return storedClick;
	}
	@Override
	public void clickedOnClick(ClickDetection click) {
		newClick(click);

	}


	@Override
	public Serializable getSettingsReference() {
		return clickSpectrumParams;
	}


	@Override
	public long getSettingsVersion() {
		return ClickSpectrumParams.serialVersionUID;
	}


	@Override
	public String getUnitName() {
		return clickControl.getUnitName();
	}


	@Override
	public String getUnitType() {
		return "Click Spectrogram Display";
	}

	public ClickSpectrumTemplateParams getClickTemplateParams(){
		return clickTemplateParams;
	}

	public void setClickTemplateParams(ClickSpectrumTemplateParams clickTemplateParams){
		this. clickTemplateParams=clickTemplateParams;
	}



	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		clickSpectrumParams = ((ClickSpectrumParams) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	@Override
	public void receiveSourceNotification(int type, Object object) {
		// don't do anything by default
	}



}
