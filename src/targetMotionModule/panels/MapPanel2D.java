package targetMotionModule.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import pamMaths.PamVector;
import targetMotionModule.AbstractTargetMotionInformation;
import targetMotionModule.TargetMotionControl;
import targetMotionModule.TargetMotionInformation;
import targetMotionModule.TargetMotionLocaliser;
import targetMotionModule.TargetMotionResult;
import GPS.GPSDataBlock;
import GPS.GpsDataUnit;
import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamController.PamController;
import PamDetection.AbstractLocalisation;
import PamDetection.PamDetection;
import PamUtils.LatLong;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PopupTextField;
import PamView.PamColors.PamColor;
import PamView.panel.PamBorder;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataUnit;

public class MapPanel2D<T extends PamDataUnit> extends MapPanel<T> {


	private JPanel mainPanel;
	private PamAxisPanel axisPanel;
	private JPanel plotPanel;
	private PamAxis xAxis, yAxis;

	private boolean rotated;

	PamSymbol originSymbol, resultSymbol;
	/**
	 * X range in metres
	 */
	private double[] plotXRange = new double[2];
	/**
	 * Y range in metres
	 */
	private double[] plotYRange = new double[2];
	/**
	 * Centre of plot in Cartesian coordinates. 
	 */
	private double[] plotCentre = new double[2];

	private PamVector[] originVectors;

	private PamVector[] arrayHeadingVectors;

	private PamVector[][] worldVectors;

	private PamSymbol bestHighlight = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 20, 20, false, Color.WHITE, Color.LIGHT_GRAY);
	private double range = 1000;
	private TargetMotionInformation targetMotionInformation;


	/**
	 * @param targetMotionLocaliser
	 * @param targetMotionDialog
	 */
	public MapPanel2D(TargetMotionLocaliser<T> targetMotionLocaliser,
			TargetMotionMainPanel<T> targetMotionDialog) {
		super(targetMotionLocaliser, targetMotionDialog);

		mainPanel = new JPanel();
		JPanel innerPanel = new JPanel(new BorderLayout());
		innerPanel.setBorder(PamBorder.createInnerBorder());
		axisPanel = new MapAxisPanel();
		axisPanel.setMinEast(10);
		axisPanel.setMinWest(10);
		axisPanel.setMinNorth(10);
		axisPanel.setMinSouth(10);
		plotPanel = new MapPlotPanel();
		mainPanel.setLayout(new BorderLayout());
		innerPanel.add(BorderLayout.CENTER, plotPanel);
		axisPanel.setPlotPanel(plotPanel);
		axisPanel.setInnerPanel(innerPanel);

		plotPanel.setPreferredSize(new Dimension(700, 400));

		mainPanel.add(BorderLayout.CENTER, axisPanel);

		xAxis = new PamAxis(0, 0, 1, 1, 0, 1, PamAxis.BELOW_RIGHT, "m", PamAxis.LABEL_NEAR_CENTRE, "%d");
		yAxis = new PamAxis(0, 0, 1, 1, 0, 1, PamAxis.ABOVE_LEFT, "m", PamAxis.LABEL_NEAR_CENTRE, "%d");
		axisPanel.setWestAxis(yAxis);
		axisPanel.setSouthAxis(xAxis);

		bestHighlight.setLineThickness(3);

	}

	@Override
	public JPanel getPanel() {
		return mainPanel;
	}

	private void sortPlotDimensions() {
		
		if (targetMotionInformation == null) {
			return;
		}
		
		for (int i = 0; i < 2; i ++) {
			plotXRange[i] = plotYRange[i] = 0;
		}
		int nSubDetections=targetMotionInformation.getNDetections();
		if (nSubDetections == 0) {
			return;
		}
		PamDataUnit pd = targetMotionInformation.getCurrentDetections().get(0);
		if (pd == null) {
			return;
		}

		originVectors = targetMotionInformation.getOrigins();
		arrayHeadingVectors = AbstractTargetMotionInformation.getHeadingVectors(targetMotionInformation.getEulerAngles());
		worldVectors = targetMotionInformation.getWorldVectors();

		PamVector v;
		double x, y;
		for (int i = 1; i < nSubDetections; i++) {
			v = originVectors[i];
			if (originVectors[i]==null) originVectors[i]=new PamVector(0,0,0);
			x = v.getElement(0);
			y = v.getElement(1);
			
			plotXRange[0] = Math.min(plotXRange[0], x);
			plotXRange[1] = Math.max(plotXRange[1], x);

			plotYRange[0] = Math.min(plotYRange[0], x);
			plotYRange[1] = Math.max(plotYRange[1], x);
		}
		plotCentre[0] = (plotXRange[0] + plotXRange[1]) / 2.;
		plotCentre[1] = (plotYRange[0] + plotYRange[1]) / 2.;
		plotXRange[0] -= getRange();
		plotXRange[1] += getRange();
		plotYRange[0] -= getRange();
		plotYRange[1] += getRange();

		setPlotAxis();
	}

	/**
	 * Make axis for the x and y coordinates based on the ranges and centre of the plot
	 */
	private void setPlotAxis() {
		/**
		 * Need to ensure square axis. 
		 */
		int xPixs = plotPanel.getWidth();
		int yPixs = plotPanel.getHeight();
		double xScale = xPixs / (plotXRange[1]-plotXRange[0]);
		double yScale = yPixs / (plotYRange[1]-plotYRange[0]);
		double scale = Math.min(xScale, yScale);
		double xRange = xPixs / scale;
		double yRange = yPixs / scale;

		xAxis.setRange(plotCentre[0]-xRange/2, plotCentre[0]+xRange/2);
		yAxis.setRange(plotCentre[1]-yRange/2, plotCentre[1]+yRange/2);

		repaintAll();
	}

	/**
	 * Convert a lat long to a point on the plot
	 * @param ll
	 * @return
	 */
	Point latLongToPoint(LatLong ll) {
		if (ll == null) {
			return null;
		}
		PamVector v = targetMotionInformation.latLongToMetres(ll);
		if (xAxis == null || yAxis == null) {
			return null;
		}
		Point pt = new Point();
		pt.x = (int) xAxis.getPosition(v.getElement(0));
		pt.y = (int) yAxis.getPosition(v.getElement(1));
		return pt;
	}

	LatLong pointToLatLong(Point pt) {
		double[] m = pointToMetres(pt);
		PamVector v = new PamVector(m[0], m[1], 0);
		if (targetMotionInformation == null) {
			return null;
		}
		return targetMotionInformation.metresToLatLong(v);
	}

	/**
	 * Convert a point on the plot to a position in metres from the plot origin. 
	 * @param pt point on the plot
	 * @return position in metres (double array [x,y])
	 */
	double[] pointToMetres(Point pt) {
		if (xAxis == null || yAxis == null) {
			return null;
		}
		double m[] = new double[2];
		m[0] = xAxis.getDataValue(pt.x);
		m[1] = yAxis.getDataValue(pt.y);
		return m;
	}


	private void repaintAll() {
		axisPanel.repaint();
		plotPanel.repaint();
	}

	private double getRange() {
		return range;
	}
	
	private void setRange(double range) {
		this.range = range;
	}

	/**
	 * Zoom the plot in or out, centering the zoom about the current point
	 * @param point
	 * @param unitsToScroll
	 */
	public void zoomPlot(Point point, int unitsToScroll) {
		double zoomFactor = unitsToScroll;
		if (zoomFactor > 0) {
			zoomFactor = 0.1; 
		}
		else {
			zoomFactor = -.1;
		}
		double xRange = xAxis.getMaxVal()-xAxis.getMinVal();
		double yRange = yAxis.getMaxVal()-yAxis.getMinVal();
		double xGain = xRange * zoomFactor;
		double yGain = yRange * zoomFactor;

		double xFac = (double) point.x/ (double)plotPanel.getWidth();
		double yFac = 1. - (double) point.y/(double)plotPanel.getHeight();

		double newXmax = xAxis.getMaxVal() + (1-xFac) * xGain;
		double newXmin = xAxis.getMinVal() - (xFac) * xGain;

		double newYmax = yAxis.getMaxVal() + (1-yFac) * yGain;
		double newYmin = yAxis.getMinVal() - (yFac) * yGain;

		xAxis.setRange(newXmin, newXmax);
		yAxis.setRange(newYmin, newYmax);

		repaintAll();
	}

	void panPlot(Point oldPoint, Point newPoint) {
		double[] oldM = pointToMetres(oldPoint);
		double[] newM = pointToMetres(newPoint);
		double dx = newM[0]-oldM[0];
		double dy = newM[1]-oldM[1];
		xAxis.setRange(xAxis.getMinVal()-dx, xAxis.getMaxVal()-dx);
		yAxis.setRange(yAxis.getMinVal()-dy, yAxis.getMaxVal()-dy);
		repaintAll();
	}

	class MapKeyListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			int mods = e.getModifiers();
//			String txt = KeyEvent.getKeyModifiersText(mods);
//			System.out.println(String.format("Key %d mods %d are %s", e.getKeyCode(), mods, txt));
			if (mods != 2) { // check for ctrl key down
				return;
			}
			Point pt = plotPanel.getMousePosition();
			if (pt == null) {
				pt = new Point(plotPanel.getWidth()/2, plotPanel.getHeight()/2);
			}
			switch (e.getKeyCode()) {
			case 38: // zoom in (up arrow)
			case 33: // page up
				zoomPlot(pt, -1);
				break;
			case 40: // zoom out (down arrow)
			case 34: // page down
				zoomPlot(pt, +1);
				break;
			}
		}
		
	}
	class MapAxisPanel extends PamAxisPanel {

		/**
		 * 
		 */
		public MapAxisPanel() {
			super();
		}
		
		private int oldWidth, oldHeight;
		@Override
		public void paintComponent(Graphics g) {
		
			super.paintComponent(g);
			
			

			if (oldWidth != getWidth() || oldHeight != getHeight()) {
				setPlotAxis();
				oldWidth = getWidth();
				oldHeight = getHeight();
				return;
			}
		}

	}

	class MapPlotPanel extends PamPanel {

		int oldWidth, oldHeight;
		private T lastEvent;
		private long lastUpdateTime;
		private TargetMotionInformation lastTargetMotionInformation;
		/**
		 * 
		 */
		public MapPlotPanel() {
			super(PamColor.PlOTWINDOW);
			originSymbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 3, 3, true, Color.BLUE, Color.blue);
			resultSymbol = new PamSymbol(PamSymbolType.SYMBOL_HEXAGRAM, 9, 9, true, Color.BLUE, Color.RED);
			setToolTipText("2D localisation plot");
			setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
			PlotMouse plotMouse = new PlotMouse();
			this.addMouseMotionListener(plotMouse);
			this.addMouseWheelListener(plotMouse);
			this.addMouseListener(plotMouse);
//			this.setEnabled(true);
			this.setFocusable(true);
			this.addKeyListener(new MapKeyListener());
		}


		@Override
		public String getToolTipText(MouseEvent event) {
			if (targetMotionInformation == null) {
				return null;
			}
			Point pt = event.getPoint();
			LatLong ll = pointToLatLong(pt);
			if (ll == null) {
				return "Localiser map";
			}
			double[] m = pointToMetres(pt);
			if (m == null || ll == null) {
				return "2D Localisation plot";
			}
			TargetMotionResult nearResult = findNearResult(pt);
			if (nearResult == null) {
				return String.format("<html>[%3.1f,%3.1f] metres<br>%s %s</html>", 
						m[0], m[1], ll.formatLatitude(), ll.formatLongitude());
			}
			else {
				ll = nearResult.getLatLong();
				PamVector v = targetMotionInformation.latLongToMetres(ll);
				return String.format("<html>%s localisation<br>[%3.1f,%3.1f] metres<br>%s %s</html>", 
						nearResult.getModel().getName(),
						m[0], m[1], ll.formatLatitude(), ll.formatLongitude());
			}
		}

		class PlotMouse extends MouseAdapter {

			//			private boolean mouseDown;

			private Point panPoint;

			@Override
			public void mouseWheelMoved(MouseWheelEvent mwe) {
				zoomPlot(mwe.getPoint(), mwe.getUnitsToScroll());
			}

			@Override
			public void mousePressed(MouseEvent me) {
				if (me.isPopupTrigger()) {
					showPopupMenu(me.getPoint());
					return;
				}
				if (me.getButton() == MouseEvent.BUTTON1) {
					//					mouseDown = true;
					panPoint = me.getPoint();
				}
				requestFocusInWindow();
			}

			@Override
			public void mouseReleased(MouseEvent me) {
				//				if (mouseDown) {
				//					mouseDown = false;
				//					return;
				//				}
				if (me.isPopupTrigger()) {
					showPopupMenu(me.getPoint());
					return;
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {	
				//				System.out.println("Mouse moved: mouse down = " + mouseDown + " point = " + panPoint);
				//				if (mouseDown == false) {
				//					return;
				//				}
				if (panPoint == null) {
					return;
				}
				panPlot(panPoint, e.getPoint());
				panPoint = e.getPoint();
			}


			@Override
			public void mouseEntered(MouseEvent arg0) {
				// get the focus as soon as the mouse enters the plot so 
				// that the Ctrl-?? controls can work. 
				requestFocus();
			}


		}


		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			  g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			          RenderingHints.VALUE_ANTIALIAS_ON);
			        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
			          RenderingHints.VALUE_RENDER_QUALITY);
			super.paintComponent(g2);
			try {

			

//			synchronized (targetMotionLocaliser.getDataSynchObject()) {
				targetMotionInformation= targetMotionLocaliser.getTargetMotionControl().getCurrentTMinfo();
				
				if (targetMotionInformation == null) {
					return;
				}
				
				if (targetMotionInformation!=lastTargetMotionInformation) {
					originVectors=targetMotionInformation.getOrigins();
					arrayHeadingVectors = AbstractTargetMotionInformation.getHeadingVectors(targetMotionInformation.getEulerAngles());
					worldVectors=targetMotionInformation.getWorldVectors();
					sortPlotDimensions();
				}

//				lastUpdateTime = currentEvent.getLastUpdateTime();
//				lastEvent = currentEvent;
				
				lastTargetMotionInformation = targetMotionInformation;
				
				try {			
					
					plotSubDetections(g, targetMotionInformation);
					
					plotGpsTrack(g, targetMotionInformation);
					
					plotResults(g, targetMotionInformation);
					
				}
				catch (Exception e) {
					e.printStackTrace();
				}
//			}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void showPopupMenu(Point point) {
			
			JPopupMenu menu = new JPopupMenu();
			JMenuItem menuItem = new JMenuItem("Reset plot");
			menuItem.addActionListener(new ResetPlot());
			menu.add(menuItem);
			
				
			JCheckBoxMenuItem cItem = new JCheckBoxMenuItem("Rotate plot", rotated);
			cItem.addActionListener(new RotatePlot());
			menu.add(cItem);
			
			menuItem = new JMenuItem("Set line length");
			menuItem.addActionListener(new LineLength());
			menu.add(menuItem);
			
		
			
			menu.show(this, point.x, point.y);
		}
		

		class ResetPlot implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setPlotAxis();
			}
		}
		
		class LineLength implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setLineLength();
			}
		}

		class RotatePlot implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				rotated = !rotated;
				sortPlotDimensions();
				repaintAll();
			}
		}
		
		private void setLineLength() {
			Point pt = getMousePosition();
			SwingUtilities.convertPointToScreen(pt, this);
			Double newVal = PopupTextField.getValue(this, "Length(m)", pt, getRange());
			if (newVal != null) {
				setRange(newVal);
				repaintAll();
			}
		}

		/**
		 * Find the nearest point to Pt, with a maximum distance of 10 pixels. 
		 * @param pt point 
		 * @return nearby result
		 */
		private TargetMotionResult findNearResult(Point point) {
			ArrayList<TargetMotionResult> results = targetMotionLocaliser.getResults();
			if (results == null) {
				return null;
			}
			int n = results.size();
			LatLong ll;
			TargetMotionResult aResult;
			TargetMotionResult nearResult = null;
			Point pt;
			int nearDist = 100;
			int newDist;
			for (int i = 0; i < n; i++) {
				aResult = results.get(i);
				ll = aResult.getLatLong();
				pt = latLongToPoint(ll);
				newDist = (pt.x-point.x)*(pt.x-point.x) + (pt.y-point.y)*(pt.y-point.y);
				if (newDist <= nearDist) {
					nearDist = newDist;
					nearResult = aResult;
				}
			}
			return nearResult;
		}

		private void plotResults(Graphics g, TargetMotionInformation targetMotionInformation) {
			ArrayList<TargetMotionResult> results = targetMotionLocaliser.getResults();
			g.setColor(Color.BLACK);
			if (results == null) {
				g.drawString("No Results", 5, getHeight()-5);
				return;
			}
			g.drawString(String.format("%d available fit results", results.size()), 5, getHeight()-5);
			int bestResult = targetMotionLocaliser.getBestResultIndex();
			int n = results.size();
			LatLong ll;
			TargetMotionResult aResult;
			Point pt, pt2;
			PamSymbol aSymbol;
			for (int i = 0; i < n; i++) {
				aResult = results.get(i);
				aSymbol = aResult.getModel().getPlotSymbol(aResult.getSide());
				if (aSymbol == null) {
					aSymbol = resultSymbol;
				}
				ll = aResult.getLatLong();
				pt = latLongToPoint(ll);
				aSymbol.draw(g, pt);
				ll = aResult.getBeamLatLong();
				if (ll != null) {
					pt2 = latLongToPoint(ll);
					g.setColor(Color.BLACK);
					g.drawLine(pt.x, pt.y, pt2.x, pt2.y);
				}

				if (i == bestResult) {
					bestHighlight.draw(g, pt);
				}
			}
		}


		private void plotGpsTrack(Graphics g, TargetMotionInformation targetMotionInformation) {
			GPSDataBlock gpsDataBlock = (GPSDataBlock) PamController.getInstance().getDataBlock(GpsDataUnit.class, 0);
			if (gpsDataBlock == null) {
				return;
			}
			//			g.setColor(Color.RED);
			//			PamSymbol s = new PamSymbol(PamSymbol.SYMBOL_HEXAGRAM, 10, 10, true, Color.RED, Color.BLUE);
			//			Point p = latLongToPoint(plotOrigin);
			//			s.draw(g, p);

			long startTime = 0;
			if (targetMotionInformation != null) {
				startTime = targetMotionInformation.getCurrentDetections().get(0).getTimeMilliseconds();
			}

			g.setColor(Color.GRAY);
			Point pt1, pt2;
			GpsDataUnit gpsDataUnit;
			LatLong ll;
			//			ListIterator<GpsDataUnit> li = gpsDataBlock.getListIteratorFromStart(startTime, 0, 0, 0);
//			synchronized (gpsDataBlock) {
				ListIterator<GpsDataUnit> li = gpsDataBlock.getListIterator(0);
				if (li.hasNext()) {
					gpsDataUnit = li.next();
					pt1 = latLongToPoint(gpsDataUnit.getGpsData());
				}
				else {
					return;
				}
				while (li.hasNext()) {
					gpsDataUnit = li.next();
					pt2 = latLongToPoint(gpsDataUnit.getGpsData());
					if (pt1 != null && pt2 != null) {
						g.drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
					}
					pt1 = pt2;
					//				if (currentEvent != null && gpsDataUnit.getTimeMilliseconds() > currentEvent.)
				}
//			}

			if (targetMotionInformation != null) {
				ll = targetMotionInformation.getGPSReference();
				if (ll == null) {
					return;
				}
				//				pt1 = latLongToPoint(ll);
				//				double b = eventRotator.getLinFitB();
				//				pt2 = new Point();
				//				pt2.x = pt1.x + 100;
				//				pt2.y = (int) (pt1.y - b * 100.);
				//				g.setColor(Color.MAGENTA);
				//				g.drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
			}
		}


		private void plotSubDetections(Graphics g, TargetMotionInformation targetMotionInformation) {
			AbstractLocalisation localisation;
			double range;
			Point pt = new Point();
			Point pt2 = new Point();
			double referenceBearing;
			int nSubDetections = targetMotionInformation.getNDetections();
			PamVector oVector, hVector;
			PamVector[] wVectors;
			for (int i = 0; i < nSubDetections; i++) {	
				oVector = originVectors[i];
				hVector = arrayHeadingVectors[i];
				wVectors = worldVectors[i];
				/**
				 * Paint origin points for each sub detection
				 */
				pt.x = (int) xAxis.getPosition(oVector.getElement(0));
				pt.y = (int) yAxis.getPosition(oVector.getElement(1));
				originSymbol.draw(g, pt);

				/**
				 * Draw vessel heading lines. 
				 */
				localisation = targetMotionInformation.getCurrentDetections().get(i).getLocalisation();
				if (localisation == null) {
					continue;
				}
				referenceBearing = Math.PI/2. - localisation.getBearingReference();
				//				pt2.x = (int) xAxis.getPosition(oVector.getElement(0) + 10 * hVector.getElement(0));
				//				pt2.y = (int) yAxis.getPosition(oVector.getElement(1) + 10 * hVector.getElement(1));
				pt2.x = (int) (pt.x + 10 * hVector.getElement(0));
				pt2.y = (int) (pt.y - 10 * hVector.getElement(1));
				g.drawLine(pt.x, pt.y, pt2.x, pt2.y);

				/*
				 * Now the bearing lines
				 */
				range = getRange();
				for (int v = 0; v < wVectors.length; v++) {
					switch(v) {
					case 0:
						g.setColor(Color.GREEN);
						break;
					case 1:
						g.setColor(Color.RED);
						break;
					default:
						g.setColor(Color.BLACK);
						break;
					}
					pt2.x = (int) xAxis.getPosition(oVector.getElement(0) + range * wVectors[v].getElement(0));
					pt2.y = (int) yAxis.getPosition(oVector.getElement(1) + range * wVectors[v].getElement(1));
					g.drawLine(pt.x, pt.y, pt2.x, pt2.y);
				}
			}


		}

	}

	@Override
	public boolean canRun() {
		return true;
	}

	@Override
	public void enableControls() {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyNewResults() {
		repaintAll();
	}

	@Override
	public void settings() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showMap(boolean b) {
		if (b) {
			getPanel().repaint();
		}
	}

	@Override
	public void update(int flag) {
		switch(flag) {
		
		case TargetMotionControl.CURRENT_DETECTIONS_CHANGED:
			
			getPanel().repaint();
			
		
		case TargetMotionControl.LOCALISATION_RESULTS_ADDED:
			
			
			break;
			
		case TargetMotionControl.RANGE_CHANGED:
			
			break;
		
	
	}

		
	}
}
