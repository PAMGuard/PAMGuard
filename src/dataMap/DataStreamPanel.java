package dataMap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import Layout.PamAxis;
import PamController.OfflineDataStore;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.ColourArray;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.dialog.PamLabel;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.JPanelWithPamKey;
import PamView.panel.KeyPanel;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataBlock;
import binaryFileStorage.BinaryOfflineDataMap;
import binaryFileStorage.BinaryStore;
import dataGram.DatagramImageData;
import dataGram.DatagramManager;
import dataGram.DatagramProvider;
import dataGram.DatagramScaleInformation;

/**
 * Panelette to go into the main DataPanel to show the data for a single data
 * stream
 * <p>
 * The outer panel will be a borderlayout with a NORTH and CENTER component.
 * <p>
 * These will be allowed to size to the full size of the container panel, which
 * does not in itself scroll. SCrolling is handled by a custom scroll bar which
 * will send instruction to each component to redraw if it's moved.
 * 
 * @author Doug Gillespie
 *
 */
public class DataStreamPanel extends JPanel implements DataMapObserver {

	// private JPanel panel;

	private DataMapControl dataMapControl;

	private ScrollingDataPanel scrollingDataPanel;

	protected PamDataBlock dataBlock;

	private DataGraph dataGraph;

	private DataName dataName;

	private Color binaryColour = Color.RED;

	private Color haveDataColour = new Color(228, 228, 228);

	private Color databaseColour = Color.blue;

	private double yScaleMin, yScaleMax; // y scale min and max

	private double pixelsPerMilli; // pixels per millisec for horiz scale.

	// private int totalDBDatas, totalBinaryDatas;
	private int totalDatas, maxDatas;

	private volatile boolean graphVisible = true;

	private double[] arrowX = { 0., 0.5, 0.5, 1, 1, 0.5, 0.5, 0. };
	private double[] arrowY = { 0., 0.5, 0.3, 0.3, -0.3, -0.3, -0.5, 0. };

	public Polygon startTimeArrow;

	public Polygon endTimeArrow;

	private boolean hasDatagram;

	private BufferedImage datagramImage;

	private double[] minMaxVal;

	private boolean showDatagram = true;

	private boolean showDataCounts = false;

	private JCheckBoxMenuItem showDatagramMenu, showDataCountsMenu;

	public DataStreamPanel(DataMapControl dataMapControl, ScrollingDataPanel scrollingDataPanel,
			PamDataBlock dataBlock) {
		this.dataMapControl = dataMapControl;
		this.scrollingDataPanel = scrollingDataPanel;
		this.dataBlock = dataBlock;
		hasDatagram = (dataBlock.getDatagramProvider() != null);

		dataGraph = new DataGraph();
		dataName = new DataName();
		// panel = new JPanel(new BorderLayout());
		this.setLayout(new BorderLayout());
		setBackground(Color.RED);
		add(BorderLayout.NORTH, dataName);
		add(BorderLayout.CENTER, dataGraph);
		autoHide();
	}

	public JPanel getPanel() {
		return this;
	}

	/**
	 * @return the dataGraph
	 */
	public DataGraph getDataGraph() {
		return dataGraph;
	}

	/**
	 * @return the dataName
	 */
	public DataName getDataName() {
		return dataName;
	}

	private int getTotalDatas() {

		int nMaps = dataBlock.getNumOfflineDataMaps();
		OfflineDataMap aMap;
		totalDatas = maxDatas = 0;
		for (int i = 0; i < nMaps; i++) {
			aMap = dataBlock.getOfflineDataMap(i);
			totalDatas += aMap.getNumMapPoints();
			maxDatas = Math.max(maxDatas, aMap.getNumMapPoints());
		}
		return totalDatas;
	}

	public void autoHide() {
		getTotalDatas();
		setGraphVisible(totalDatas > 0 || !dataMapControl.isViewer());
	}

	protected void sortScales() {
		double pixsPerHour = scrollingDataPanel.getPixelsPerHour();
		pixelsPerMilli = pixsPerHour / 3600 / 1000;

		double highestPoint = 0;
		double aPoint;
		int nMaps = dataBlock.getNumOfflineDataMaps();
		OfflineDataMap aMap, highestMap = null;
		for (int i = 0; i < nMaps; i++) {
			aMap = dataBlock.getOfflineDataMap(i);
			if ((aPoint = aMap.getHighestPoint(OfflineDataMap.SCALE_PERHOUR)) > highestPoint) {
				highestPoint = aPoint;
				highestMap = aMap;
			}
		}

		if (highestMap == null) {
			return;
		}

		yScaleMin = 0;
		yScaleMax = highestMap.getHighestPoint(dataMapControl.dataMapParameters.vScaleChoice);
		if (dataMapControl.dataMapParameters.vLogScale) {
			yScaleMin = highestMap.getLowestNonZeroPoint(dataMapControl.dataMapParameters.vScaleChoice);
			// if (yScaleMin > 0) {
			// yScaleMin = Math.floor(Math.log10(yScaleMin*0.9));
			// yScaleMin = Math.pow(10, yScaleMin);
			// }
			// else {
			// yScaleMin = 0.1;
			// }
			yScaleMin = 0.1;
			if (yScaleMax > 0) {
				yScaleMax = Math.ceil(Math.log10(yScaleMax));
				yScaleMax = Math.pow(10, yScaleMax);
			} else {
				yScaleMax = 1;
			}
		} else {
			if (yScaleMax > 0) {
				yScaleMax *= 1.1;
			} else {
				yScaleMax = 1;
			}
		}
	}

	/**
	 * Convert an x coordinate into a time in milliseconds
	 * 
	 * @param xPos x coordinate in graph
	 * @return milliseconds time
	 */
	private long getTimeFromX(int xPos) {
		return (long) (xPos / pixelsPerMilli) + scrollingDataPanel.getScreenStartMillis();
	}

	public class DataGraph extends JPanelWithPamKey {

		private static final int NCOLOURPOINTS = 100;
		private ColourArray datagramColours;

		private DataGraph() {
			GraphMouse gm = new GraphMouse();
			addMouseMotionListener(gm);
			addMouseListener(gm);
			addMouseWheelListener(gm);

			OfflineDataMap aMap;
			DataMapDrawing dataMapDrawing;
			int nMaps = dataBlock.getNumOfflineDataMaps();
			KeyPanel keyPanel;
			for (int i = 0; i < nMaps; i++) {
				aMap = dataBlock.getOfflineDataMap(i);
				dataMapDrawing = aMap.getSpecialDrawing();
				if (dataMapDrawing != null) {
					keyPanel = dataMapDrawing.getKeyPanel();
					if (keyPanel != null) {
						setKeyPanel(keyPanel);
						setKeyPosition(CornerLayoutContraint.FIRST_LINE_END);
					}
				}
				aMap.addDataMapObserver(DataStreamPanel.this);
			}
			setToolTipText(dataBlock.getDataName());
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			if (getHeight() < 2) {
				return;
			}
			sortScales();
			BespokeDataMapGraphic bsg = dataBlock.getBespokeDataMapGraphic();
			if (bsg != null) {
				bsg.paint(g, this);
			} else {
				if (hasDatagram && showDatagram) {
					datagramPaint(g);
				} else {
					standardPaint(g);
				}
				showLoadPeriod(g);
			}
		}

		private void datagramPaint(Graphics g) {
			if (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW || minMaxVal == null
					|| minMaxVal[0] == Double.MIN_VALUE) {
				minMaxVal = calcMinMax(false);
			}

			if (getScaleType() == DatagramScaleInformation.PLOT_3D) {
				datagramPaint3D(g);
			} else {
				datagramPaint2D(g);
			}
		}

		/**
		 * Draws lines
		 * 
		 * @param g
		 */
		private void datagramPaint2D(Graphics g) {
			long startMillis = scrollingDataPanel.getScreenStartMillis();
			long endMillis = scrollingDataPanel.getScreenEndMillis();
			endMillis = startMillis + (long) (scrollingDataPanel.getScreenSeconds() * 1000.);
			double millisPerPixel = 1. / pixelsPerMilli;
			// find a first datagram point and hope that all the rest are the same.
			BinaryStore binaryStore = (BinaryStore) PamController.getInstance().findOfflineDataStore(BinaryStore.class);
			if (binaryStore == null) {
				return;
			}
			BinaryOfflineDataMap binaryDataMap = (BinaryOfflineDataMap) dataBlock.getOfflineDataMap(binaryStore);
			if (binaryDataMap == null) {
				return;
			}
			DatagramManager datagramManager = binaryStore.getDatagramManager();
			if (datagramManager == null) {
				return;
			}
			DatagramImageData datagramImageData = datagramManager.getImageData(dataBlock, startMillis, endMillis,
					getWidth());
			double[][] imageData = datagramImageData.imageData;
			int x1, x2;
			x1 = (int) ((datagramImageData.imageStartTime - startMillis) * pixelsPerMilli);
			x2 = (int) ((datagramImageData.imageEndTime - startMillis) * pixelsPerMilli);

			int nTimePoints = imageData.length;
			int nAmpPoints = imageData[0].length;

			double[] minMaxValue;
			if (getMinMaxValues(imageData, false) != null) {
				minMaxValue = Arrays.copyOf(minMaxVal, 2);
			} else {
				return;
			}

			lastPlotted2DminVal = minMaxValue[0];
			lastPlotted2DmaxVal = minMaxValue[1];

			double xScale = (double) (x2 - x1) / (double) nTimePoints;
			double yScale = getHeight() / (minMaxValue[1] - minMaxValue[0]);
			int lastX = -9999, lastY = 0, x, y;
			int h = getHeight();
			for (int iy = 0; iy < nAmpPoints; iy++) {
				g.setColor(PamColors.getInstance().getWhaleColor(iy + 1));
				lastX = -9999;
				for (int ix = 0; ix < nTimePoints; ix++) {
					x = (int) (xScale * ix) + x1;
					y = h - (int) (yScale * (imageData[ix][iy] - minMaxValue[0]));
					if (lastX != -9999) {
						g.drawLine(lastX, lastY, x, y);
					}
					lastX = x;
					lastY = y;
				}
			}

		}

		/**
		 * Draws colourmap
		 * 
		 * @param g
		 */
		private void datagramPaint3D(Graphics g) {
			/*
			 * hopefully, there will be datagram data for this block, so do a pretty
			 * coloured map of it all. The data will be spread out amongst all the data map
			 * points, so will need to scroll through a lot of them to find the data we
			 * need.
			 * 
			 */
			long startMillis = scrollingDataPanel.getScreenStartMillis();
			long endMillis = scrollingDataPanel.getScreenEndMillis();
			endMillis = startMillis + (long) (scrollingDataPanel.getScreenSeconds() * 1000.);
			double millisPerPixel = 1. / pixelsPerMilli;

			// find a first datagram point and hope that all the rest are the same.
//			BinaryStore binaryStore = (BinaryStore) PamController.getInstance().findOfflineDataStore(BinaryStore.class);
//			if (binaryStore == null) {
//				return;
//			}

			// try to find a datamap which has a datagram
//			int nMaps = dataBlock.getNumOfflineDataMaps();
//			for (int i = 0; i < nMaps; i++) {
//				OfflineDataMap aMap = dataBlock.getOfflineDataMap(i);
//				if (aMap.)
//			}

			DatagramManager datagramManager = findDatagramManager();
			if (datagramManager == null) {
				return;
			}
			DatagramImageData datagramImageData = datagramManager.getImageData(dataBlock, startMillis, endMillis,
					getWidth());
			double[][] imageData = datagramImageData.imageData;

			if (datagramColours == null) {
				datagramColours = ColourArray.createRainbowArray(NCOLOURPOINTS);
			}
			int nXPoints = imageData.length;
			int nYPoints = imageData[0].length;

			double[] minMaxValue;
			if (getMinMaxValues(imageData, false) != null) {
				minMaxValue = Arrays.copyOf(minMaxVal, 2);
			} else {
				return;
			}

			minMaxValue[1] *= wheelScrollFactor;
			minMaxValue[0] = Math.log(minMaxValue[0]);
			minMaxValue[1] = Math.log(minMaxValue[1]);
			/*
			 * now fudge the scale a little since black is zero and we want anything > 0 to
			 * be significantly away from black.
			 */
			double scaleRange = (minMaxValue[1] - minMaxValue[0]) * 1.2;
			minMaxValue[0] = minMaxValue[1] - scaleRange;

			int iCol, y;
			int x1, x2;
			x1 = (int) ((datagramImageData.imageStartTime - startMillis) * pixelsPerMilli);
			x2 = (int) ((datagramImageData.imageEndTime - startMillis) * pixelsPerMilli);
			if (imageData.length == 0 || imageData[0].length == 0) {
				return;
			}
			/*
			 * Need to scale down the image so that the number of pixels in the image is
			 * less than the number of pixels on the screen so that every pixel gets
			 * plotted. Therefore work out a scaling factor for the data which is the round
			 * up of the datagram height / the panel height. It then adds up data for 1:n
			 * bins and inserts the mean value.
			 */
			int imHeight = imageData[0].length;
			int panelHeight = getHeight();
			if (panelHeight <= 0) {
				return;
			}
			int imageScale = (imHeight + panelHeight - 1) / panelHeight; // clever way to round up!
			if (imageScale <= 0) {
				return; // only if datagram height <= 0;
			}

			int imageHeight = imHeight / imageScale;
			datagramImage = new BufferedImage(imageData.length, imageHeight, BufferedImage.TYPE_INT_BGR);
			for (int i = 0; i < nXPoints; i++) {
				for (int j = 0, k2 = 0; j < imageHeight; j++) {
					double val = 0;
					for (int k = 0; k < imageScale; k++, k2++) {
						val += imageData[i][k2];
					}
					val /= imageScale;
					y = imageHeight - j - 1;
					if (val < 0) {
						datagramImage.setRGB(i, y, Color.LIGHT_GRAY.getRGB());
					} else if (val == 0) {
						datagramImage.setRGB(i, y, 0xFFFFFF);
					} else {
						iCol = (int) (NCOLOURPOINTS * (Math.log(val) - minMaxValue[0]) / scaleRange);
						iCol = Math.min(Math.max(0, iCol), NCOLOURPOINTS - 1);
						datagramImage.setRGB(i, y, datagramColours.getColour(iCol).getRGB());
						// datagramImage.setRGB(i, y, 0x0000FF);
					}
				}
			}
			/*
			 * Now finally paint that into the full window ...
			 * 
			 */
			int imageWidth = getWidth();
			g.drawImage(datagramImage, x1, 0, x2, getHeight(), 0, 0, nXPoints, imageHeight, null);

			if (showDataCounts) {
				OfflineDataMap offlineDataMap = dataBlock.getDatagrammedMap();
				if (offlineDataMap == null) {
					return;
				}
				OfflineDataStore offlineDataStore = offlineDataMap.getOfflineDataSource();
				if (offlineDataStore == null) {
					return;
				}
				drawDataRate(g, offlineDataMap,
						scrollingDataPanel.getDataStreamColour(offlineDataMap.getOfflineDataSource()));
			}

		}

		private DatagramManager findDatagramManager() {
			OfflineDataMap offlineDataMap = dataBlock.getDatagrammedMap();
			if (offlineDataMap == null) {
				return null;
			}
			OfflineDataStore offlineDataStore = offlineDataMap.getOfflineDataSource();
			if (offlineDataStore == null) {
				return null;
			}
			return offlineDataStore.getDatagramManager();

		}

		/**
		 * Get a min and max value for the array of data. if round10 is true, roune down
		 * and up to nearest power of 10
		 * 
		 * @param data    data
		 * @param round10 flag to round min and max to nearest multiple of 10.
		 * @return min and max values.
		 */
		double[] getMinMaxValues(double[][] imageData, boolean round10) {
			if (imageData == null) {
				return null;
			}
			// if minMax is null calculate from binary store. Want to calculate for entire
			// dataset, not just the currently displayed data.
			if (minMaxVal == null) {
				minMaxVal = calcMinMax(round10);
			} else if (Double.isNaN(minMaxVal[0]) || Double.isNaN(minMaxVal[1])) {
				System.out.printf("DataStremPanel %s: min: %3.3f, max %3.3f \n", dataBlock.getDataName(), minMaxVal[0],
						minMaxVal[1]);
				minMaxVal = calcMinMax(round10);
			}
			return minMaxVal;
		}

		/**
		 * Calculate a min and max value for the entire datagram. if round10 is true,
		 * round down and up to nearest power of 10
		 * 
		 * @param round10 flag to round min and max to nearest multiple of 10.
		 * @return min and max values.
		 */
		private double[] calcMinMax(boolean round10) {

			// find a first datagram point and hope that all the rest are the same.
//			BinaryStore binaryStore = (BinaryStore) PamController.getInstance().findOfflineDataStore(BinaryStore.class);
//			if (binaryStore == null) {
//				return null;
//			}
//			OfflineDataStore offlineDataStore = dataBlock.getDatagrammedMap()

			DatagramManager datagramManager = findDatagramManager();
			if (datagramManager == null || dataBlock == null) {
				return null;
			}

			double[] minMaxVal = datagramManager.getMinAndMax(dataBlock, false);

			if (round10) {
				/*
				 * not convinced by utility of this. Datagrams are on log scale but I don't
				 * bother with converting to true dB, so put f=10 in the calculation so that the
				 * rounding is actually to the nearest 10dB. Might be better to say if max/min <
				 * 10 then min = max/10 ?
				 */
				double f = 10;
				if (minMaxVal[1] > 0) {
					minMaxVal[1] = Math.pow(10, Math.ceil(Math.log10(minMaxVal[1]) * f) / f);
				}
				if (minMaxVal[0] > 0) {
					minMaxVal[0] = Math.pow(10, Math.floor(Math.log10(minMaxVal[0]) * f) / f);
				} else {
					minMaxVal[0] = minMaxVal[1] / 10000;
				}

//				minMaxVal[0] = Math.floor(minMaxVal[0]/10.);
//				minMaxVal[0] *= 10;
//				minMaxVal[1] = Math.ceil(minMaxVal[1]/10.);
//				minMaxVal[1] *= 10.;
////				PamUtils.roundNumber(number, step)
//				if (minMaxVal[0] <= 0) {
//					minMaxVal[0] = minMaxVal[1]/100;
//				}
			}

			return minMaxVal;
		}

		/**
		 * Paint effort and detections for all data maps.
		 */
		public void standardPaint(Graphics g) {
			int nMaps = dataBlock.getNumOfflineDataMaps();
			OfflineDataMap aMap;
			DataMapDrawing dataMapDrawing;

			for (int i = 0; i < nMaps; i++) {
				aMap = dataBlock.getOfflineDataMap(i);
				dataMapDrawing = aMap.getSpecialDrawing();
				if (dataMapDrawing == null) {
					drawEffort(g, aMap, haveDataColour);
					drawDataRate(g, aMap, scrollingDataPanel.getDataStreamColour(aMap.getOfflineDataSource()));
				} else {
					dataMapDrawing.drawEffort(g, this, aMap, haveDataColour);
					dataMapDrawing.drawDataRate(g, this, aMap,
							scrollingDataPanel.getDataStreamColour(aMap.getOfflineDataSource()));
				}
			}
		}

		public void drawEffort(Graphics g, OfflineDataMap offlineDataMap, Color effortColour) {
			OfflineDataMapPoint mapPoint;
			int n;
			int x1, x2, y1, y2;
			long pointStart, pointEnd;
			int h = getHeight();
			g.setColor(effortColour);
			synchronized (offlineDataMap) {
				long startMillis = scrollingDataPanel.getScreenStartMillis();
				long endMillis = scrollingDataPanel.getScreenEndMillis();
				Iterator<OfflineDataMapPoint> iterator = offlineDataMap.getListIterator();
				while (iterator.hasNext()) {
					mapPoint = iterator.next();
					n = mapPoint.getNDatas();
					pointStart = mapPoint.getStartTime();
					pointEnd = mapPoint.getEndTime();
					if (pointEnd < startMillis) {
						continue;
					}
					if (pointStart > endMillis) {
						break;
					}
					x1 = (int) ((pointStart - startMillis) * pixelsPerMilli);
					x2 = (int) ((pointEnd - startMillis) * pixelsPerMilli);
					y2 = h;
					y1 = h - (int) ((n - yScaleMin) / (yScaleMax - yScaleMin) * h);
					// y1 = Math.min(y1, h-1);
					if (x1 == x2) {
						g.drawLine(x1, 0, x2, h);
					} else {
						g.drawRect(x1, 0, x2 - x1 + 1, h);
					}
				}
			}

		}

		public void drawDataRate(Graphics g, OfflineDataMap offlineDataMap, Color dataColour) {
			OfflineDataMapPoint mapPoint;
			int n;
			int x1, x2, y1, y2;
			// boolean logScale = dataMapControl.dataMapParameters.vLogScale;
			long pointStart, pointEnd;
			// int scaleType = dataMapControl.dataMapParameters.vScaleChoice;
			int h = getHeight();
			g.setColor(dataColour);
			synchronized (offlineDataMap) {
				long startMillis = scrollingDataPanel.getScreenStartMillis();
				long endMillis = scrollingDataPanel.getScreenEndMillis();
				Iterator<OfflineDataMapPoint> iterator = offlineDataMap.getListIterator();
				while (iterator.hasNext()) {
					mapPoint = iterator.next();
					n = mapPoint.getNDatas();
					pointStart = mapPoint.getStartTime();
					pointEnd = mapPoint.getEndTime();
					if (pointEnd < startMillis) {
						continue;
					}
					if (pointStart > endMillis) {
						break;
					}
					// x1 = (int) ((pointStart - startMillis) * pixelsPerMilli);
					// x2 = (int) ((pointEnd - startMillis) * pixelsPerMilli);
					x1 = getXCoord(pointStart);
					x2 = getXCoord(pointEnd);
					y2 = h;
					y1 = getYCoord(n, pointEnd - pointStart);
					// y1 = h - (int) ((n - yScaleMin) / (yScaleMax-yScaleMin) * h);
					// y1 = Math.min(y1, h-1);
					if (Math.abs(x1 - x2) > 100) {
//						System.out.println("big");
					}
					if (x1 == x2) {
						g.drawLine(x1, y1, x2, y2);
					} else {
						g.drawRect(x1, y1, x2 - x1, y2 - y1);
					}
				}
			}

		}

		/**
		 * Draw on the chart the period of data loaded into memory.
		 * 
		 * @param g graphics
		 */
		public void showLoadPeriod(Graphics g) {
			long dataStart = dataBlock.getCurrentViewDataStart();
			long dataEnd = dataBlock.getCurrentViewDataEnd();
			if (dataStart <= 1 || dataEnd < dataStart || dataEnd > Long.MAX_VALUE / 2) {
				return;
			}
			int xStart = getXCoord(dataStart);
			int xEnd = getXCoord(dataEnd);
			if (!hasDatagram) {
				g.setColor(Color.MAGENTA);
			} else {
				g.setColor(Color.DARK_GRAY);
			}
			int yEff = 5;
			g.drawLine(xStart, yEff, xEnd, yEff);
			endTimeArrow = drawArrow(g, xEnd, yEff, 12, 12);
			startTimeArrow = drawArrow(g, xStart, yEff, -12, 12);
		}

		private Polygon drawArrow(Graphics g, int x0, int y0, double xScale, double yScale) {
			int[] x = new int[arrowX.length];
			int[] y = new int[arrowY.length];
			for (int i = 0; i < x.length; i++) {
				x[i] = x0 + (int) (xScale * arrowX[i]);
				y[i] = y0 + (int) (yScale * arrowY[i]);
			}
			Polygon poly = new Polygon(x, y, x.length);
			g.fillPolygon(x, y, x.length);
			return poly;
		}

		/**
		 * Get the item y Coordinate based on the count, the plotHeight,
		 * 
		 * @param count        Number of data entries
		 * @param plotHeight   plot height in pixels
		 * @param scaleType    per second, per minute, etc.
		 * @param itemDuration duration of data map item in milliseconds.
		 * @param logScale     log scale
		 * @return the y coordinate (from the top of the panel)
		 */
		public int getYCoord(double count, long itemDuration) {
			int plotHeight = getHeight();
			boolean logScale = dataMapControl.dataMapParameters.vLogScale;
			int scaleType = dataMapControl.dataMapParameters.vScaleChoice;
			double value = OfflineDataMap.scaleData(count, itemDuration, scaleType);
			if (logScale) {
				if (value <= 0) {
					return plotHeight;
				} else {
					return plotHeight
							- (int) (Math.log(value / yScaleMin) / Math.log(yScaleMax / yScaleMin) * plotHeight);
				}
			} else {
				return plotHeight - (int) ((value - yScaleMin) / (yScaleMax - yScaleMin) * plotHeight);
			}
		}

		/**
		 * Get an x coordinate from a time value.
		 * 
		 * @param value
		 * @return
		 */
		public int getXCoord(long value) {
			return (int) ((value - scrollingDataPanel.getScreenStartMillis()) * pixelsPerMilli);
		}

		@Override
		public String getToolTipText(MouseEvent me) {
			long tm = getTimeFromX(me.getX());
			// find data map points that overlap that time.

			String tipText;
			if (startTimeArrow != null && startTimeArrow.contains(me.getPoint())) {
				tipText = "Data Start: " + PamCalendar.formatDateTime(dataBlock.getCurrentViewDataStart(), false);
			} else if (endTimeArrow != null && endTimeArrow.contains(me.getPoint())) {
				tipText = "Data End: " + PamCalendar.formatDateTime(dataBlock.getCurrentViewDataEnd(), false);
			} else {
				OfflineDataMap dMap = dataBlock.getPrimaryDataMap();
				if (dMap != null) {
					tipText = String.format("%s Data from<p>%s to %s<p>Cursor: %s", dataBlock.getDataName(), 
							PamCalendar.formatDateTime(dMap.getFirstDataTime(), false),
							PamCalendar.formatDateTime(dMap.getLastDataTime(), false),
							PamCalendar.formatDateTime(tm, true));
				}
				else {
					tipText = "Cursor: " + PamCalendar.formatDateTime(tm, true);
				}
			}

//			tipText += "<br>Panel height = " + getHeight();

			DatagramScaleInformation dsi = findDatagramScaleInfo();
			if (dsi != null & yAxis != null) {
				double val = yAxis.getDataValue(me.getY());
				tipText = String.format("%s<br>%3.2f %s", tipText, val, dsi.getUnits());
			}

			// now add information about the displayed datamap point under the mouse.

			int nMaps = dataBlock.getNumOfflineDataMaps();
			OfflineDataMap aMap;
			DataMapDrawing dataMapDrawing;
			for (int i = 0; i < nMaps; i++) {
				aMap = dataBlock.getOfflineDataMap(i);
//				aMap.isInGap(timeMillis)
				OfflineDataMapPoint mapPoint = aMap.findMapPoint(tm);
				if (mapPoint != null) {
					tipText += String.format("<p>%s: %s", aMap.getDataMapName(), mapPoint.toString());
					tipText += String.format("<p>UID %s to %s", mapPoint.getLowestUID(), mapPoint.getHighestUID());
				}
			}

			return "<html>" + tipText + "</html>";
		}

	}

	class GraphMouse extends MouseAdapter {

		@Override
		public void mouseExited(MouseEvent me) {
			scrollingDataPanel.dataMapPanel.dataGraphMouseTime(null);
		}

		@Override
		public void mouseMoved(MouseEvent me) {
			long tm = getTimeFromX(me.getX());
			scrollingDataPanel.dataMapPanel.dataGraphMouseTime(tm);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (showPopup(e)) {
				showGraphMenu(e);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (showPopup(e)) {
				showGraphMenu(e);
			}
		}

		private boolean showPopup(MouseEvent e) {
			return (e.isPopupTrigger());// && getTotalDatas() > 0);
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent mwe) {
			wheelMoved(mwe);
		}

	}

	private int totalWheelRotation = 0;

	public void wheelMoved(MouseWheelEvent mwe) {
		totalWheelRotation += mwe.getWheelRotation();
		wheelScrollFactor = Math.pow(2.0, totalWheelRotation);
//		System.out.println(String.format("totalRotation = %d, scroll factor = %3.5f", 
//				totalWheelRotation, wheelScrollFactor));
		repaintAll();
	}

	private JPopupMenu graphMenu;
	private long menuMouseTime;

	private PamAxis yAxis;

	private double wheelScrollFactor = 1;

	private double lastPlotted2DmaxVal;

	private double lastPlotted2DminVal;

	public void showGraphMenu(MouseEvent e) {
		// if (graphMenu == null) {
		createGraphMenu();
		// }
		menuMouseTime = getTimeFromX(e.getX());
		graphMenu.setToolTipText(PamCalendar.formatDateTime(menuMouseTime, true));
		graphMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	private void createGraphMenu() {
		graphMenu = new JPopupMenu();

		JMenuItem menuItem;

		menuItem = new JMenuItem("Centre data here");
		menuItem.addActionListener(new CentreDataHere());
		graphMenu.add(menuItem);

		menuItem = new JMenuItem("Start data here");
		menuItem.addActionListener(new StartDataHere());
		graphMenu.add(menuItem);

		graphMenu.addSeparator();

		menuItem = new JMenuItem("Scroll To Data");
		menuItem.addActionListener(new ScrollToData());
		graphMenu.add(menuItem);

		if (hasDatagram) {
			graphMenu.addSeparator();
			showDatagramMenu = new JCheckBoxMenuItem("Show datagram", showDatagram);
			showDatagramMenu.addActionListener(new ShowDatagram());
			graphMenu.add(showDatagramMenu);

			showDataCountsMenu = new JCheckBoxMenuItem("Show data counts", showDataCounts);
			showDataCountsMenu.addActionListener(new ShowDataCounts());
			graphMenu.add(showDataCountsMenu);
		}
	}

	class CentreDataHere implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			dataMapControl.centreDataAt(dataBlock, menuMouseTime);
		}
	}

	class StartDataHere implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			dataMapControl.startDataAt(dataBlock, menuMouseTime);
		}
	}

	class ScrollToData implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			dataMapControl.scrollToData(dataBlock);
		}
	}

	class ShowDatagram implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			showDatagram = showDatagramMenu.isSelected();
			if (!showDatagram) {
				showDataCounts = true;
			}
			repaintAll();
		}
	}

	class ShowDataCounts implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			showDataCounts = showDataCountsMenu.isSelected();
			if (!showDataCounts) {
				showDatagram = true;
			}
			repaintAll();
		}
	}

	public boolean isGraphVisible() {
		return graphVisible;
	}

	/**
	 * Border like panel in the top of each display graph on the data map.
	 * 
	 * @author Doug
	 *
	 */
	class DataName extends PamPanel {
		private JLabel title;
		private String dataName;

		private DataName() {
			super(PamColor.BORDER);
			setBorder(BorderFactory.createRaisedSoftBevelBorder());
//			setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
			setLayout(new BorderLayout());
			dataName = String.format("%s - %s", dataBlock.getParentProcess().getPamControlledUnit().getUnitName(),
					dataBlock.getDataName());
			add(BorderLayout.WEST, title = new PamLabel(dataName, SwingConstants.LEFT));
//			Font titleFont = title.getFont();
//			titleFont = titleFont.deriveFont(titleFont.getSize()-2);
//			title.setFont(titleFont);
			addMouseListener(new DataNameMouse());
			setToolTipText("Double click to hide / show data rate graph");
			setTitle();
		}

		private void setTitle() {
			getTotalDatas();
			String tit = new String(dataName);
			if (getTotalDatas() == 0 && PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
				tit += "  (No data)";
			}
			title.setText(tit);
		}
	}

	class DataNameMouse extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent me) {
			if (me.getClickCount() == 2) {
				doubleClick(me);
			}
		}

		private void doubleClick(MouseEvent me) {
			setGraphVisible(!graphVisible);
			if (graphVisible) {
				add(BorderLayout.CENTER, dataGraph);
			} else {
				remove(dataGraph);
			}
			SwingUtilities.invokeLater(new ShowLater());
		}

	}

	private void setGraphVisible(boolean b) {
		graphVisible = b;
		dataGraph.setVisible(b);
	}

	private class ShowLater implements Runnable {
		@Override
		public void run() {
			scrollingDataPanel.showHideGraph();
		}
	}

	public void scrollChanged() {
		repaint();
		dataGraph.repaint();
	}

	/**
	 * @return the logScale
	 */
	public boolean isLogScale() {
		DatagramScaleInformation dsi = findDatagramScaleInfo();
		if (dsi != null) {
			return dsi.isLogScale();
		}
		return dataMapControl.dataMapParameters.vLogScale;
	}

	/**
	 * @return the yScaleMin
	 */
	public double getYScaleMin() {
		DatagramScaleInformation dsi = findDatagramScaleInfo();
		if (dsi != null) {
			double minVal = dsi.getMinValue();
			if (Double.isNaN(minVal)) {
				return lastPlotted2DminVal;
			} else {
				return minVal;
			}
		}
		return yScaleMin;
	}

	/**
	 * @return the yScaleMax
	 */
	public double getYScaleMax() {
		DatagramScaleInformation dsi = findDatagramScaleInfo();
		if (dsi != null) {
			double maxVal = dsi.getMaxValue();
			if (Double.isNaN(maxVal)) {
				return lastPlotted2DmaxVal;
			} else {
				return maxVal;
			}
		}
		return yScaleMax;
	}

	public String getScaleUnits() {
		DatagramScaleInformation dsi = findDatagramScaleInfo();
		if (dsi != null) {
			return dsi.getUnits();
		}
		return "n";
	}

	public int getScaleType() {
		DatagramScaleInformation dsi = findDatagramScaleInfo();
		if (dsi != null) {
			return dsi.getDisplayType();
		}
		return DatagramScaleInformation.PLOT_3D;
	}

	/**
	 * Find the scale information for the datagram - return null if there either
	 * isn't a datagram or if it's not currently showing
	 * 
	 * @return
	 */
	private DatagramScaleInformation findDatagramScaleInfo() {
		if (!hasDatagram) {
			return null;
		}
		DatagramProvider dp = dataBlock.getDatagramProvider();
		if (dp == null) {
			return null;
		}
		if (!showDatagram) {
			return null;
		}
		return dp.getScaleInformation();
	}

	/**
	 * Need to know the axis since the axis can easily calculate a value based on
	 * the coordinate of the cursor for the tooltips.
	 * 
	 * @param yAxis
	 */
	public void setAxis(PamAxis yAxis) {
		this.yAxis = yAxis;
	}

	/**
	 * Repaint everything, including the output border panel
	 */
	public void repaintAll() {
//		repaint();
//		dataGraph.repaint();
		scrollingDataPanel.repaintAll();
	}

	@Override
	public void updateDataMap(OfflineDataMap dataMap, OfflineDataMapPoint dataMapPoint) {
		scrollingDataPanel.updateDataMap(dataMap, dataMapPoint);
	}
}
