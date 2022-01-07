package difar.display;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import clipgenerator.clipDisplay.ClipDisplayParameters;
import videoRangePanel.VRCursor;
import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamUtils.FrequencyFormat;
import PamUtils.PamCalendar;
import PamView.ColourArray;
import PamView.ColourArray.ColourArrayType;
import PamView.PamColors.PamColor;
import PamView.panel.PamBorder;
import PamView.panel.PamPanel;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamUtils.MatrixOps;
import difar.DIFARMessage;
import difar.DemuxWorkerMessage;
import difar.DifarControl;
import difar.DifarDataUnit;
import difar.DifarParameters;
import difar.DifarParameters.SpeciesParams;
/**
 * Display window for viewing and selecting bearings from difarDataUnits
 * Contains a spectrogram, the DIFARGram (i.e. bearing, frequency, power 
 * ambiguity surface), user controls (e.g. save, delete, etc.) and a 
 * status bar. 
 *
 */
public class DIFARGram implements DIFARDisplayUnit {

	private DifarControl difarControl;

	private JPanel mainPanel;

	private DGAxisPanel axisPanel;
	private DGPlotPanel plotPanel;
	private DGSpecPanel specPanel;

	private PamAxis freqAxis, angleAxis, freqAxis2, timeAxis;

	private JSplitPane splitPane;

	private BufferedImage difarImage;
	private BufferedImage spectrogramImage;

	private Object imageSynchObject = new Object();

	private DIFARUnitControlPanel difarUnitControlPanel;

	private ColourArray specColourArray;
	private ColourArrayType currentcolourType;

	public VRCursor cursor;

	public int spectrogramImageChoice;

	private DIFARGroupPanel difarGroupPanel;

	public DIFARGram(DifarControl difarControl) {
		super();
		this.difarControl = difarControl;
		mainPanel = new JPanel(new BorderLayout());
		axisPanel = new DGAxisPanel();
		difarUnitControlPanel = difarControl.getDifarUnitControlPanel();
		difarGroupPanel = new DIFARGroupPanel(difarControl);
		mainPanel.add(BorderLayout.CENTER, axisPanel);

		// Holds the UnitControlPanel and GroupPanel
		PamPanel controlPanel = new PamPanel(new BorderLayout());
		controlPanel.add(BorderLayout.SOUTH, difarUnitControlPanel.getComponent());
		controlPanel.add(BorderLayout.CENTER, difarGroupPanel.getComponent());
		int width = (int) Math.min(controlPanel.getPreferredSize().getWidth(), 120);
		int height = (int) controlPanel.getPreferredSize().getHeight();
		controlPanel.setPreferredSize(new Dimension(width,height));
		mainPanel.add(BorderLayout.WEST,controlPanel);
	}

	@Override
	public String getName() {
		return "DIFARGram";
	}

	@Override
	public Component getComponent() {
		return mainPanel;
	}
	
	public DIFARGroupPanel getDifarGroupPanel(){
		return difarGroupPanel;
	}

	@Override
	public int difarNotification(DIFARMessage difarMessage) {
		switch(difarMessage.message) {
		case DIFARMessage.DemuxComplete:
		case DIFARMessage.SaveDatagramUnit:
		case DIFARMessage.DeleteDatagramUnit:
		case DIFARMessage.DisplaySettingsChange:
			prepareDifarImage();
			getComponent().repaint();
			break;
		}
		return 0;
	}
	public boolean prepareDifarImage() {
		DifarDataUnit difarDataUnit = difarControl.getCurrentDemuxedUnit();
		if (difarDataUnit == null) {
			difarImage = null;
			spectrogramImage = null;
		}
		else {
			ClipDisplayParameters cdp = difarControl.getClipDisplayParams(difarDataUnit);
			checkColourArray(cdp.getColourMap());
			setupFrequencyAxis(difarDataUnit);
			
			double nSecs = difarDataUnit.getDurationInSeconds();
			timeAxis.setMaxVal(nSecs);
			Color contrastCol = specColourArray.getContrastingColour();
			if (contrastCol != cursor.getCursorColour()) {
				cursor.setCursorColour(specColourArray.getContrastingColour());
				plotPanel.setCursor(cursor.getCursor());
			}
			synchronized (imageSynchObject) {
				double intensityScaleFactor = difarControl.getDifarParameters().findSpeciesParams(difarDataUnit).getDifarGramIntensityScaleFactor(); 
				difarImage=MatrixOps.createImage(difarDataUnit.getSurfaceData(), specColourArray, true, intensityScaleFactor); 
				
				int fftLen = difarControl.getDifarProcess().getDisplayFFTLength(difarDataUnit);
				int fftHop = difarControl.getDifarProcess().getDisplayFFTHop(difarDataUnit);
				spectrogramImage=difarDataUnit.getClipImage(spectrogramImageChoice, 0, fftLen, fftHop, cdp.amlitudeMinVal,
						cdp.amlitudeMinVal+cdp.amplitudeRangeVal, specColourArray.getColours());
			}
		}
		return true;
	}


	private void setupFrequencyAxis(DifarDataUnit difarDataUnit) {
		double[] fRange = getPlotLimits(difarDataUnit);
		freqAxis.setRange(fRange[0], fRange[1]);
		freqAxis2.setRange(fRange[0], fRange[1]);
	}

	private double[] getPlotLimits(DifarDataUnit difarDataUnit) {
		if (difarControl.getDifarParameters().zoomDifarFrequency) {
			double[] fRange = difarDataUnit.getFrequency();
			if (fRange == null) {
				return new double[]{0., difarDataUnit.getDisplaySampleRate()/2.};
			}
			double delt = fRange[1]-fRange[0];
			double max = fRange[1] + delt/4;
			double min = fRange[0] - delt/4;
			max = Math.min(max, difarDataUnit.getDisplaySampleRate()/2.);
			min = Math.max(0., min);
			return new double[]{min, max};
		}
		else {
			return new double[]{0., difarDataUnit.getDisplaySampleRate()/2.};
		}
	}

	public void repaintAll() {
		axisPanel.repaint();
		plotPanel.repaint();
		specPanel.repaint();
	}

	private void checkColourArray(ColourArrayType newType) {
		if (currentcolourType == null || newType != currentcolourType) {
			specColourArray = ColourArray.createStandardColourArray(256, newType);
			currentcolourType = newType;
		}
	}



	class DGAxisPanel extends PamAxisPanel {

		public DGAxisPanel() {
			super();
			JPanel borderPanel = new JPanel(new BorderLayout());
			
			plotPanel = new DGPlotPanel();
			specPanel = new DGSpecPanel();
			
			PanelMouse cancelAutoSave = new PanelMouse();
			plotPanel.addMouseListener(cancelAutoSave);
			specPanel.addMouseListener(cancelAutoSave);
			
			JPanel specBorder = new JPanel(new BorderLayout());
			specBorder.add(BorderLayout.CENTER, specPanel);
			specBorder.setBorder(PamBorder.createInnerBorder());
			JPanel plotBorder = new JPanel(new BorderLayout());
			plotBorder.add(BorderLayout.CENTER, plotPanel);
			plotBorder.setBorder(PamBorder.createInnerBorder());

			splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			splitPane.add(specBorder);
			splitPane.add(plotBorder);
			Integer pos = difarControl.getDifarParameters().difarGramDividerPos;
			if (pos != null) {
				splitPane.setDividerLocation(pos);
			}
			else {
				splitPane.setResizeWeight(0.5);
			}
			borderPanel.add(BorderLayout.CENTER, splitPane);
			splitPane.addPropertyChangeListener(new SplitPaneListener());


			this.setPlotPanel(specPanel);
			this.setInnerPanel(borderPanel);
			freqAxis = new PamAxis(0, 1, 0, 1, 0, 1000, PamAxis.ABOVE_LEFT, "Frequency", PamAxis.LABEL_NEAR_CENTRE, "%3.1f");
			freqAxis2 = new PamAxis(0, 1, 0, 1, 0, 1000, PamAxis.BELOW_RIGHT, "", PamAxis.LABEL_NEAR_CENTRE, "%3.1f");
			angleAxis = new PamAxis(0, 1, 0, 1, 0, 359, PamAxis.BELOW_RIGHT, "Angle", PamAxis.LABEL_NEAR_CENTRE, "%d\u00B0");
			angleAxis.setInterval(30);
			timeAxis = new PamAxis(0, 1, 0, 1, 0, 12, PamAxis.BELOW_RIGHT, "Time (s)", PamAxis.LABEL_NEAR_CENTRE, "%d s");
			setWestAxis(freqAxis);
			setSouthAxis(angleAxis);
			setAutoInsets(true);
			setMinNorth(3);

		}

		@Override
		protected void drawSouthAxis(Graphics g, Insets insets) {
			// overrode this function to do bespoke drawing of the two South axis. 
			int panelBottom = getHeight() - insets.bottom;
			Point thisTopLeft = this.getLocationOnScreen();
			Point plotTopLeft = specPanel.getLocationOnScreen();
			int x1 = plotTopLeft.x - thisTopLeft.x;
			int x2 = x1 + specPanel.getWidth();
			timeAxis.drawAxis(g, x1, panelBottom, x2, panelBottom);

			plotTopLeft = plotPanel.getLocationOnScreen();
			x1 = plotTopLeft.x - thisTopLeft.x;
			x2 = x1 + plotPanel.getWidth();
			angleAxis.drawAxis(g, x1, panelBottom, x2, panelBottom);
		}

	}
	class SplitPaneListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			axisPanel.repaint();
			difarControl.getDifarParameters().difarGramDividerPos = splitPane.getDividerLocation();
		}

	}
	class DGPlotPanel extends PamPanel {

		private PamSymbol maxSymbol = new PamSymbol(PamSymbolType.SYMBOL_CROSS2, 12, 12, false, Color.BLACK, Color.BLACK);
		private PamSymbol selectedSymbol = new PamSymbol(PamSymbolType.SYMBOL_CROSS, 6, 6, false, Color.BLACK, Color.BLACK);
		private PamSymbol maxPerFrequencySymbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 6, 6, false, Color.BLACK, Color.BLACK);
		BasicStroke dashedStroke, dottedStroke;
		public DGPlotPanel() {
			super();	
			setToolTipText("DIFARGram Display");
			Dimension d = Toolkit.getDefaultToolkit().getBestCursorSize(31, 31);
			cursor = new VRCursor(d);
			setCursor(cursor.getCursor());
			DataGramMouse dgMouse = new DataGramMouse();
			addMouseMotionListener(dgMouse);
			addMouseListener(dgMouse);
			maxSymbol.setLineThickness(1);
			selectedSymbol.setLineThickness(1);
			maxPerFrequencySymbol.setLineThickness(1);
			float dash1[] = {5.0f, 5.f};
			dashedStroke = new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
					1f, dash1, 0.0f);
			float dot1[] = {2.0f, 4.0f};
			dottedStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
					1f, dot1, 0.0f);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			DifarDataUnit difarDataUnit = difarControl.getCurrentDemuxedUnit();
			synchronized (imageSynchObject) {
				if (difarDataUnit == null || difarImage == null) {
					String txt = "DIFARGram";
					Rectangle2D strBounds = g.getFontMetrics().getStringBounds(txt, g);
					g.drawString(txt, (int) (getWidth()-strBounds.getWidth())/2, (int) (getHeight() + strBounds.getHeight())/2);
					return;
				}
				int[] imageBins = getFreqImageBins(difarDataUnit, difarImage);
				g.drawImage(difarImage, 0, 0, getWidth(), getHeight(), 0, imageBins[0], difarImage.getWidth(), imageBins[1], null);

				Color col = specColourArray.getContrastingColour();
				PamSymbol dataSymbol = difarControl.getSpeciesSymbol(difarDataUnit);
				if (dataSymbol != null)
					col = dataSymbol.getLineColor();

				// draw the line. 
				if (difarControl.getDifarParameters().showDifarGramSummary) {
					double[] difarLine = difarDataUnit.getSurfaceSummary();
					if (difarLine != null) {
						double lMax = 0;
						for (int i = 0; i < difarLine.length; i++) {
							lMax = Math.max(lMax, difarLine[i]);
						}
						/**
						 * Work out the yMax based on the frequency selection of the data unit
						 */
						double[] fRange = difarDataUnit.getFrequency();
						double yMax = freqAxis.getPosition(fRange[1]);
						double yScale = (getHeight()-yMax) / lMax;
						double xScale = (double) getWidth() / (double) (difarLine.length-1);
						int x0, x1, y0, y1;
						double firstAngle, secondAngle;
						g.setColor(specColourArray.getContrastingColour());
						for (int i = 0; i < difarLine.length-1; i++) {
							firstAngle =  difarControl.getDifarProcess().difarGridToDegrees(difarDataUnit, i);
							secondAngle =  difarControl.getDifarProcess().difarGridToDegrees(difarDataUnit, i+1);
							x0 = (int) angleAxis.getPosition(firstAngle);
							x1 = (int) angleAxis.getPosition(secondAngle);
							y0 = (int) (getHeight()-difarLine[i]*yScale);
							y1 = (int) (getHeight()-difarLine[i+1]*yScale);
							g.drawLine(x0, y0, x1, y1);
						}
					}
				}
				
				if (difarControl.getDifarParameters().showDifarGramSummary) {
					double[] maxAngleLine = difarDataUnit.getMaximumAngleSummary();
					if (maxAngleLine != null) {
						/**
						 * Work out the yMax based on the frequency selection of the data unit
						 */
						double[] fRange = difarDataUnit.getFrequency();
						double yMax = freqAxis.getPosition(fRange[1]);
						double yMin = freqAxis.getPosition(fRange[0]);
						double yScale = getHeight()/(double) fRange[1]-fRange[0];
						double xScale = (double) getWidth()/360; 
						g.setColor(specColourArray.getContrastingColour());
						
						double freq;
						int x, y;
						maxPerFrequencySymbol.setLineColor(specColourArray.getContrastingColour());
						for (int i = 0; i < maxAngleLine.length; i++) {
							freq = difarControl.getDifarProcess().difarGridToFrequency(difarDataUnit, i);
							if (freq >= fRange[0] && freq <= fRange[1]){
								y = (int) freqAxis.getPosition(freq);
								x = (int) angleAxis.getPosition(maxAngleLine[i]);

								maxPerFrequencySymbol.draw(g, new Point(x, y));
							}
						}
					}
				}

				// draw the selected point
				Double selAng = difarDataUnit.getSelectedAngle();
				Double selFreq = difarDataUnit.getSelectedFrequency();
				if (selAng != null && selFreq != null) {
					selectedSymbol.setLineColor(specColourArray.getContrastingColour());
					selectedSymbol.draw(g, new Point((int) angleAxis.getPosition(selAng), (int) freqAxis.getPosition(selFreq)));
				}

				// draw the maximum point
				Double maxAng = difarDataUnit.getMaximumAngle();
				Double maxFreq = difarDataUnit.getMaximumFrequency();
				if (maxAng != null && maxFreq != null) {
					maxSymbol.setLineColor(specColourArray.getContrastingColour());
					maxSymbol.draw(g, new Point((int) angleAxis.getPosition(maxAng), (int) freqAxis.getPosition(maxFreq)));
				}

				if (difarControl.getDifarParameters().showDifarGramFreqLimits) {
					// also draw a couple of dotted lines to show the selected frequencies from the data unit.
					double[] fRange = difarDataUnit.getFrequency();
					if (fRange != null && fRange.length == 2) {
						g.setColor(col);
						g2d.setStroke(dashedStroke);
						for (int i = 0; i < 2; i++) {
							int y = (int) freqAxis.getPosition(fRange[i]);
							g.drawLine(0, y, getWidth(), y);
						}
					}
				}
				if (difarControl.getDifarParameters().showDifarGramKey) {
					// draw key by hand in upper left corner. 
					FontMetrics fm = g2d.getFontMetrics();
					int rowHeight = fm.getAscent() + fm.getDescent();
					int lineLength = 30;
					int leftMargin = 3;
					int textX = leftMargin + lineLength + 3;
					int y = 0;
					int yLift = fm.getAscent()/2;
					int yMid;

					y+= rowHeight; // bottom of row
					yMid = y - yLift;
					g2d.setStroke(dashedStroke);
					maxSymbol.draw(g, new Point(leftMargin + lineLength - maxSymbol.getWidth(), yMid));
					g.drawString("Maximum position", textX, y);

					y+= rowHeight; // bottom of row
					yMid = y - yLift;
					g2d.setStroke(dashedStroke);
					selectedSymbol.draw(g, new Point(leftMargin + lineLength - maxSymbol.getWidth(), yMid));
					g.drawString("Selected position", textX, y);
					
					if (difarControl.getDifarParameters().showDifarGramFreqLimits) {
						y+= rowHeight; // bottom of row
						yMid = y - yLift;
						g2d.setStroke(dashedStroke);
						g.setColor(col);
						g.drawLine(leftMargin, yMid, leftMargin+lineLength, yMid);
						g.setColor(specColourArray.getContrastingColour());
						SpeciesParams sp = difarControl.getDifarParameters().findSpeciesParams(difarDataUnit);
						boolean markedLimits = (difarDataUnit.triggerName == null) ? 
								sp.useMarkedBandsForSpectrogramClips : sp.useDetectionLimitsForTriggeredDetections;
						String freqBandType = markedLimits ? "Selected frequency limits" : "Classification frequency limits";	
												
						g.drawString(freqBandType, textX, y);
					}
					if (difarControl.getDifarParameters().showDifarGramSummary) {
						y+= rowHeight; // bottom of row
						yMid = y - yLift;
						g2d.setStroke(new BasicStroke(1));
						g.drawLine(leftMargin, yMid, leftMargin+lineLength, yMid);
						g.drawString("Summary line", textX, y);
					}
				}
			}

		}

		@Override
		public String getToolTipText(MouseEvent event) {
			if (difarControl.getCurrentDemuxedUnit() == null) {
				return null;
			}
			double f = freqAxis.getDataValue(event.getY());
			double a = angleAxis.getDataValue(event.getX());
			String str = String.format("angle=%3.1f\u00B0; freq'=%s", a, FrequencyFormat.formatFrequency(f, true));
			DifarDataUnit difarDataUnit = difarControl.getCurrentDemuxedUnit();
			if (difarDataUnit != null){
				Double gain = difarDataUnit.calculateDifarGain(a, f);
				if (gain != null) {
					str += String.format("; gain=%3.1fdB", 20.*Math.log10(gain));
				}
			}
			return str;
		}

	}
	class DGSpecPanel extends PamPanel {

		private JCheckBoxMenuItem[] sourceChoises = new JCheckBoxMenuItem[4];
		String[] choiceNames = {"Decimated audio data", "Demuxed Omni", "Demuxed EW", "Demuxed NS"};
		private JPopupMenu popupMenu;
		BasicStroke dashedStroke;

		public DGSpecPanel() {
			super();	
			setToolTipText("Spectrogram Display");
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			
			// make a quick popup menu to show different channels
			popupMenu = new JPopupMenu();
			int firstChoice = 1;
			if (difarControl.isViewer()) firstChoice = 1;
			for (int i = firstChoice; i < 4; i++) {
				sourceChoises[i] = new JCheckBoxMenuItem(choiceNames[i]);
				sourceChoises[i].addActionListener(new SpectrogramSourceChoise(i));
				popupMenu.add(sourceChoises[i]);
			}
			setChoice(firstChoice);
			SpectrogramMouse specMouse = new SpectrogramMouse();
			this.addMouseListener(specMouse);
			this.addMouseMotionListener(specMouse);
			float[] dash1 = {5.0f, 5.f};
			dashedStroke = new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
					1f, dash1, 0.0f);
		}
		
		private class SpectrogramMouse extends MouseAdapter {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showSpectrogramMenu(e);
				}
			}


			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showSpectrogramMenu(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showSpectrogramMenu(e);
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				sayMousePos(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				difarControl.getDemuxProgressDisplay().setDataGramCursorInfo(null, null);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				sayMousePos(e);
			}

			private void sayMousePos(MouseEvent e) {
				if (difarControl.getCurrentDemuxedUnit() == null) {
					difarControl.getDemuxProgressDisplay().setDataGramCursorInfo(null, null);
				}
				else {
					double f = freqAxis.getDataValue(e.getY());
					difarControl.getDemuxProgressDisplay().setDataGramCursorInfo(f, null);
				}

			}
			
		}
		
		private void showSpectrogramMenu(MouseEvent e) {
			popupMenu.show(this, e.getX(), e.getY());
		}
		
		private void setChoice(int imageChoice) {
			if (imageChoice != spectrogramImageChoice) {
				spectrogramImageChoice = imageChoice;
				prepareDifarImage();
				repaint();
			}
			for (int i = 0; i < 4; i++) {
				if (sourceChoises[i] == null) {
					continue;
				}
				sourceChoises[i].setSelected(i == imageChoice);
			}
		}

		private class SpectrogramSourceChoise implements ActionListener {
			int specChoice = 0;
			
			public SpectrogramSourceChoise(int iChoice) {
				this.specChoice = iChoice;
			}

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setChoice(specChoice);
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			DifarDataUnit difarDataUnit = difarControl.getCurrentDemuxedUnit();
			synchronized (imageSynchObject) {
				if (spectrogramImage == null || difarDataUnit == null) {
					String txt = "Spectrogram";
					Rectangle2D strBounds = g.getFontMetrics().getStringBounds(txt, g);
					g.drawString(txt, (int) (getWidth()-strBounds.getWidth())/2, (int) (getHeight() + strBounds.getHeight())/2);
					return;
				}
				int[] imageBins = getFreqImageBins(difarDataUnit, spectrogramImage);
				g.drawImage(spectrogramImage, 0, 0, getWidth(), getHeight(), 
						0, imageBins[0], spectrogramImage.getWidth(), imageBins[1], null);
				
				if (difarControl.getDifarParameters().showDifarGramFreqLimits) {
					// also draw a couple of dotted lines to show the selected frequencies from the data unit. 
					Color col = specColourArray.getContrastingColour();
					PamSymbol dataSymbol = difarControl.getSpeciesSymbol(difarDataUnit);
					if (dataSymbol != null)
						col = dataSymbol.getLineColor();
					double[] fRange = difarDataUnit.getFrequency();
					if (fRange != null && fRange.length == 2) {
						g.setColor(col);
						g2d.setStroke(dashedStroke);
						for (int i = 0; i < 2; i++) {
							int y = (int) freqAxis.getPosition(fRange[i]);
							g.drawLine(0, y, getWidth(), y);
						}
					}
				}
			}
		}

		@Override
		public String getToolTipText(MouseEvent event) {
			if (difarControl.getCurrentDemuxedUnit() == null) {
				return null;
			}
					
			double f = freqAxis.getDataValue(event.getY());
			double t = timeAxis.getDataValue(event.getX());
			return String.format("time=%3.1fs; freq'=%s", t, FrequencyFormat.formatFrequency(f, true));
			
		}


	}

	private class PanelMouse extends MouseAdapter {
		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			cancelAutoSave();
		}
		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			cancelAutoSave();
		}
		
		void cancelAutoSave(){
			
			//this is a bit pointless now as action performed doesn't attempt save if timer is stopped
			DifarDataUnit unit = difarControl.getCurrentDemuxedUnit();
			if (unit!=null) {
				unit.cancelAutoSave();
			}else{
				
//				System.out.println("DifarUnit"+unit);
			}
			difarControl.getDifarProcess().cancelAutoSaveTimer();
		}
	}
	
	private class DataGramMouse extends MouseAdapter {

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseDragged(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseDragged(MouseEvent e) {
			sayMousePos(e);
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseMoved(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseMoved(MouseEvent e) {
			sayMousePos(e);
		}

		private void sayMousePos(MouseEvent e) {
			if (difarControl.getCurrentDemuxedUnit() == null) {
				difarControl.getDemuxProgressDisplay().setDataGramCursorInfo(null, null);
			}
			else {
				double f = freqAxis.getDataValue(e.getY());
				double a = angleAxis.getDataValue(e.getX());
				difarControl.getDemuxProgressDisplay().setDataGramCursorInfo(f, a);
			}

		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseExited(MouseEvent e) {
			difarControl.getDemuxProgressDisplay().setDataGramCursorInfo(null, null);
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			if (difarControl.getCurrentDemuxedUnit() == null) {
				return;
			}
			if (e.getButton() == MouseEvent.BUTTON1) {
				setClickedPosition(e);
			}
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}

		private void showPopupMenu(MouseEvent e) {
			JPopupMenu popupMenu = createPopupMenu(e);
			if (popupMenu != null) {
				popupMenu.show(plotPanel, e.getX(), e.getY());
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}

	}
	
	/**
	 * Set angle and frequency information and at teh same time
	 * search for matching angles, etc. 
	 * @param angle new angle
	 * @param frequency new frequency. 
	 */
	private void setAngleAndFrequency(double angle, double frequency) {
		DifarDataUnit currentUnit = difarControl.getCurrentDemuxedUnit();
		if (currentUnit == null) {
			return;
		}
		currentUnit.setSelectedAngle(angle);
		currentUnit.setSelectedFrequency(frequency);
		difarControl.getDifarProcess().getDifarRangeInfo(currentUnit);
		difarControl.getDifarProcess().estimateTrackedGroup(currentUnit);
		difarControl.getDifarProcess().getQueuedDifarData().updatePamData(currentUnit, currentUnit.getTimeMilliseconds());
		difarControl.getDifarUnitControlPanel().enableControls();
	}
	
	/**
	 * Image may be zoomed in frequency so may only plot a subset of 
	 * the available image. 
	 * @param difarDataunit difar data unit
	 * @param difarImage2 image. 
	 * @return bin limits for drawing of image on difargram and spectrgram
	 */
	public int[] getFreqImageBins(DifarDataUnit difarDataUnit,
			BufferedImage difarImage) {
		if (difarDataUnit == null || difarImage == null) {
			return null;
		}
		int[] binLims = new int[]{0, difarImage.getHeight()};
		double fMin = freqAxis.getMinVal();
		double fMax = freqAxis.getMaxVal();
		double niquist = difarDataUnit.getDisplaySampleRate()/2.;
		binLims[1] = difarImage.getHeight()-(int)Math.round(fMin/niquist*difarImage.getHeight()); 
		binLims[0] = difarImage.getHeight()-(int)Math.round(fMax/niquist*difarImage.getHeight()); 
		for (int i = 0; i < 2; i++) {
			binLims[i] = Math.max(0, Math.min(difarImage.getHeight(), binLims[i]));
		}
		return binLims;
	}

	/**
	 * Called when user clicks on the DIFARgram with the left mouse button
	 * @param e mouse data. 
	 */
	public void setClickedPosition(MouseEvent e) {
		if (difarControl.isViewer()) {
			return;
		}
		double f = freqAxis.getDataValue(e.getY());
		double a = angleAxis.getDataValue(e.getX());
		
		setAngleAndFrequency(a, f);
		
		plotPanel.repaint();
//		if (difarControl.getDifarParameters().isSingleClickSave()) {
//			difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.SaveDatagramUnit, difarControl.getCurrentDemuxedUnit()));
//		}
//		else {
			difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.ClickDatagramUnit, difarControl.getCurrentDemuxedUnit()));
//		}		
	}

	public JPopupMenu createPopupMenu(MouseEvent e) {
		DifarDataUnit difarDataUnit = difarControl.getCurrentDemuxedUnit();
		if (difarDataUnit == null) {
			return null;
		}
		DifarParameters difarParams = difarControl.getDifarParameters();
		JPopupMenu popupMenu = new JPopupMenu();

		JMenuItem menuItem;
		menuItem = new JMenuItem("Restore Maximum");
		menuItem.addActionListener(new SelectMaximum());
		menuItem.setEnabled(difarDataUnit.getMaximumAngle() != null && difarDataUnit.getMaximumFrequency() != null);
		popupMenu.add(menuItem);
		
		menuItem = new JMenuItem("Return to queue");
		menuItem.addActionListener(new ReturnToQueue());
		popupMenu.add(menuItem);
		
		popupMenu.addSeparator();
		
		JCheckBoxMenuItem cbMenuItem;
		cbMenuItem = new JCheckBoxMenuItem("Show Key");
		cbMenuItem.setSelected(difarParams.showDifarGramKey);
		cbMenuItem.addActionListener(new ShowKey());
		popupMenu.add(cbMenuItem);

		cbMenuItem = new JCheckBoxMenuItem("Show Summary Line");
		cbMenuItem.setSelected(difarParams.showDifarGramSummary);
		cbMenuItem.addActionListener(new ShowSummary());
		popupMenu.add(cbMenuItem);

		cbMenuItem = new JCheckBoxMenuItem("Show Frequency Selection");
		cbMenuItem.setSelected(difarParams.showDifarGramFreqLimits);
		cbMenuItem.addActionListener(new ShowFrequencies());
		popupMenu.add(cbMenuItem);

		return popupMenu;
	}

	private class ShowKey implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			difarControl.getDifarParameters().showDifarGramKey = !difarControl.getDifarParameters().showDifarGramKey;
			plotPanel.repaint();
		}
	}
	private class ShowSummary implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			difarControl.getDifarParameters().showDifarGramSummary = !difarControl.getDifarParameters().showDifarGramSummary;
			plotPanel.repaint();
		}
	}
	private class ShowFrequencies implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			difarControl.getDifarParameters().showDifarGramFreqLimits = !difarControl.getDifarParameters().showDifarGramFreqLimits;
			plotPanel.repaint();
		}
	}
	private class SelectMaximum implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			restoreDifarMaximum();
		}
	}
	
	private class ReturnToQueue implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.ReturnToQueue, difarControl.getCurrentDemuxedUnit()));
		}
	}


	/**
	 * Restore maximum values 
	 */
	public void restoreDifarMaximum() {
		DifarDataUnit difarDataUnit = difarControl.getCurrentDemuxedUnit();
		if (difarDataUnit != null) {
			setAngleAndFrequency(difarDataUnit.getMaximumAngle(), difarDataUnit.getMaximumFrequency());
			plotPanel.repaint();
		}		
	}

	/**
	 * Called when the zoom state of the control panel changes. 
	 * Remake the axes and repaint the images. 
	 */
	public void zoomFrequency() {
		DifarDataUnit difarDataUnit = difarControl.getCurrentDemuxedUnit();
		if (difarDataUnit == null) {
			return;
		}
		double[] fRange = getPlotLimits(difarDataUnit);
		freqAxis.setRange(fRange[0], fRange[1]);
		freqAxis2.setRange(fRange[0], fRange[1]);
		repaintAll();
	}
}
