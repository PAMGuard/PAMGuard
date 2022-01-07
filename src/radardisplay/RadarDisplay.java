package radardisplay;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Timer;

import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.AbstractPamScrollerAWT;
import pamScrollSystem.PamScrollObserver;
import pamScrollSystem.PamScroller;
import pamScrollSystem.RangeSpinner;
import pamScrollSystem.RangeSpinnerListener;
import userDisplay.UserDisplayControl;
import userDisplay.UserFramePlots;
import GPS.GpsDataUnit;
import Layout.PamAxis;
import Layout.PamAxisPanel;
import Map.MapDrawingOptions;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.AbstractLocalisation;
import PamUtils.PamCalendar;
import PamView.PamColors;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.SymbolKeyItem;
import PamView.PamColors.PamColor;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.JPanelWithPamKey;
import PamView.panel.KeyPanel;
import PamView.panel.PamPanel;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.PamSymbolManager;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.dataSelector.DataSelector;

public class RadarDisplay extends UserFramePlots implements PamObserver, PamSettings {

	private RadarParameters radarParameters;
	
	private RadarPlot radarPlot;
	
	private RadarAxis radarAxis;
	
	private PamAxis radialAxis;

	private MouseAdapter popupListener;
	
	private RadarProjector radarProjector;
	
	private ArrayList<PamDataBlock> detectorDataBlocks;

	private long masterMilliseconds;
	
	private long masterSampleNumber;
	
	private Timer timer;
	
	private KeyPanel keyPanel;

	private PamScroller radarScroller;

	private RangeSpinner rangeSpinner;

	protected RadarDisplayComponent radarDisplayComponent;
	
	public RadarDisplay(UserDisplayControl userDisplayControl, RadarParameters radarParameters, RadarDisplayComponent radarDisplayComponent) {
		
		super(userDisplayControl);
		this.radarDisplayComponent = radarDisplayComponent;
		
		if (radarParameters != null) {
			this.radarParameters = radarParameters.clone();
		}
//		else {
			PamSettingManager.getInstance().registerSettings(this);  // always need to register, even if we're using old parameters
//		}
			
		radarProjector = new RadarProjector(this);
		setAxisPanel(radarAxis = new RadarAxis());
		radarPlot = new RadarPlot();
		PamPanel radarOuterPlot = new PamPanel(new BorderLayout());
		radarOuterPlot.add(BorderLayout.CENTER, radarPlot);
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			radarScroller = new PamScroller("Radar", AbstractPamScrollerAWT.HORIZONTAL, 1000, 600000L, true);
			radarScroller.addObserver(new ScrollObserver());
			radarScroller.setRangeMillis(this.radarParameters.scrollMinMillis, this.radarParameters.scrollMaxMillis, false);
			radarScroller.setVisibleMillis((long) (this.radarParameters.viewRangeSeconds * 1000));
			rangeSpinner = new RangeSpinner();
			rangeSpinner.addRangeSpinnerListener(new ScrollRangeListener());
			radarScroller.addControl(rangeSpinner.getComponent());
			radarScroller.addObserver(rangeSpinner);
			rangeSpinner.setSpinnerValue(this.radarParameters.viewRangeSeconds);
			radarOuterPlot.add(BorderLayout.SOUTH, radarScroller.getComponent());
		}
		setPlotPanel(radarOuterPlot);
		radialAxis = new PamAxis(0, 1, 0, 3, 100, 0, true, "dB", "%2.0f");
		radialAxis.setLabelPos(PamAxis.LABEL_NEAR_MAX);
		timer = new Timer(1000, new TimerListener());
		timer.start();

		if (this.radarParameters == null) {
			this.radarParameters = new RadarParameters();
			RadarParameters newParams = RadarParametersDialog.showDialog(RadarDisplay.this,
					userDisplayControl.getPamView().getGuiFrame(), this.radarParameters, radarProjector);
			if (newParams != null) {
				this.radarParameters = newParams.clone();
			}
		}

		newSettings();
	}
	
	@Override
	public PamObserver getObserverObject() {
		return this;
	}

	@Override
	public String getName() {
		return "Radar Display";
	}
	
	private class ScrollObserver implements PamScrollObserver {

		@Override
		public void scrollRangeChanged(AbstractPamScroller pamScroller) {
			radarParameters.scrollMinMillis = pamScroller.getMinimumMillis();
			radarParameters.scrollMaxMillis = pamScroller.getMaximumMillis();
			radarScroller.setVisibleMillis((long) (rangeSpinner.getSpinnerValue() * 1000));
			repaintAll();
		}

		@Override
		public void scrollValueChanged(AbstractPamScroller pamScroller) {
			radarParameters.scrollValue = pamScroller.getValueMillis();
			repaintAll();
		}
		
	}
	
	private class ScrollRangeListener implements RangeSpinnerListener {

		@Override
		public void valueChanged(double oldValue, double newValue) {
			radarScroller.setVisibleMillis((long) (newValue * 1000));
			radarParameters.viewRangeSeconds = newValue;
			repaintAll();
		}
		
	}

	public RadarParameters getRadarParameters() {
		radarParameters.boundingRectangle = getFrame().getBounds();
		return radarParameters;
	}

	@Override
	public int getFrameType() {
		return UserFramePlots.FRAME_TYPE_RADAR;
	}

	@Override
	public void notifyModelChanged(int changeType) {
		if (changeType == PamControllerInterface.ADD_CONTROLLEDUNIT ||
				changeType == PamControllerInterface.REMOVE_CONTROLLEDUNIT) {
			subscribeDetectors();
		}
		
	}

	class RadarAxis extends PamAxisPanel {

		public RadarAxis() {
			super();
			// TODO Auto-generated constructor stub
		}
		
	}
	
	class RadarPlot extends JPanelWithPamKey {

		public RadarPlot() {
			super();
			PamColors.getInstance().setColor(this, PamColor.PlOTWINDOW);
			addMouseListener(new PopupListener());
			addMouseListener(radarProjector.getMouseHoverAdapter(this));
			addMouseMotionListener(radarProjector.getMouseHoverAdapter(this));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			radarProjector.clearHoverList();
			Graphics2D g2d = (Graphics2D) g;
			paintGrid(g2d);
			paintDetectorData(g2d);
		}
		
		@Override
		public void repaint() {
			// TODO Auto-generated method stub
			if (isShowing()) {
				super.repaint();
			}
		}

		private void paintDetectorData(Graphics2D g2d) {
			if (detectorDataBlocks == null) return;
			PamDataBlock dataBlock;
			for (int i = 0; i < detectorDataBlocks.size(); i++) {
				dataBlock = detectorDataBlocks.get(i);
				if (dataBlock.canDraw(radarProjector) == false || radarParameters.getRadarDataInfo(dataBlock).select == false) continue;
				RadarDataInfo rdi = radarParameters.getRadarDataInfo(dataBlock);
				paintDetectorData(g2d, dataBlock, rdi.getDetectorLifetime() * 1000, rdi.isFadeDetector());
			}
		}
		
		private void paintDetectorData(Graphics2D g2d, PamDataBlock dataBlock, long lifetime, boolean fade) {
			long startTime = masterMilliseconds - lifetime;
			long endTime = Long.MAX_VALUE;
			long fadeEnd;
			MapDrawingOptions drawingOptions = null;
			if (fade) {
				drawingOptions = new MapDrawingOptions(1);
			}
			radarProjector.setProjectorDrawingOptions(drawingOptions);
			
			if (radarScroller != null) {
				startTime = radarParameters.scrollValue;
				endTime = (long) (startTime + radarParameters.viewRangeSeconds*1000);
				fadeEnd = endTime;
			}
			else {
				fadeEnd = PamCalendar.getTimeInMillis();
			}
			DataSelector dataSelector = dataBlock.getDataSelector(getDataSelectTitle(), false);
			PamDataUnit dataUnit;
			double plotHeading;
			AbstractLocalisation localisation;
			// draw backwards ! Not nice, but works. 
			synchronized(dataBlock) {
				
				// see if the datablock has a symbol manager. If so, set the radar projector to point
				// to the symbol manager's symbol chooser
				PamSymbolManager symbolManager = dataBlock.getPamSymbolManager();
				if (symbolManager != null) {
					PamSymbolChooser symbolChooser = symbolManager.getSymbolChooser(radarDisplayComponent.getUniqueName(), radarProjector);
					radarProjector.setPamSymbolChooser(symbolChooser);
				}
				else {
					radarProjector.setPamSymbolChooser(null);
				}
				
				ListIterator<PamDataUnit> duIterator = dataBlock.getListIterator(-1);
//				dataUnit = dataBlock.getFirstUnitAfter(duItera)
				while (duIterator.hasPrevious()) {
					dataUnit = duIterator.previous();
					if (dataUnit.getTimeMilliseconds() < startTime) {
						break;
					}
					if (dataUnit.getTimeMilliseconds() > endTime) {
						continue;
					}
					if (dataSelector != null && dataSelector.scoreData(dataUnit) == 0) {
						continue;
					}
					plotHeading = 0;
					if (radarParameters.orientation == RadarParameters.NORTH_UP) {
						localisation = dataUnit.getLocalisation();
						if (localisation != null) {
							plotHeading = localisation.getBearingReference();
						}
					}
					radarProjector.setHeadingReference(plotHeading);
					if (drawingOptions != null) {
						drawingOptions.calculateOpacity(fadeEnd, startTime, dataUnit.getLastChangeTime());
					}
					dataBlock.drawDataUnit(g2d, dataUnit, radarProjector);
				}
			}
		}
		
		private void paintGrid(Graphics2D g2d) {	
			Point plotCentre = getCentre();
			int radius = getRadius();

			int angleStart = getAngleStart();
			int angleSweep = getAngleSweep();
			
			g2d.setColor(PamColors.getInstance().getColor(PamColor.AXIS));
			
			int x, y, w, h;
			g2d.drawArc(x=getArcX(plotCentre, radius), y=getArcY(plotCentre, radius), 
					w=getArcWidth(plotCentre, radius), h=getArcHeight(plotCentre, radius), angleStart, angleSweep);
//			g2d.drawLine(x, plotCentre.y, x+w, plotCentre.y);
//			g2d.drawLine(plotCentre.x, y, plotCentre.x, y+h);
			// now the axis
			// draw the main axis lines.
			//w = Math.abs(w);
			for (int a = angleStart; a <= angleStart+angleSweep; a+=90) {
				g2d.drawLine(plotCentre.x, plotCentre.y, 
						(plotCentre.x + radius * (int)Math.cos(a * Math.PI/180.)),
						(plotCentre.y - radius * (int)Math.sin(a * Math.PI/180)));
			}

			switch (radarParameters.sides) {
			case RadarParameters.SIDES_ALL:
				radialAxis.setTickPosition(PamAxis.BELOW_RIGHT);
				radialAxis.drawAxis(g2d, plotCentre.x, plotCentre.y, x+w, plotCentre.y);
				radialAxis.setTickPosition(PamAxis.ABOVE_LEFT);
				radialAxis.drawAxis(g2d, plotCentre.x, plotCentre.y, plotCentre.x, plotCentre.y - radius);
				break;
			case RadarParameters.SIDES_BACKHALF:
				radialAxis.setTickPosition(PamAxis.BELOW_RIGHT);
				radialAxis.drawAxis(g2d, plotCentre.x, plotCentre.y, plotCentre.x, plotCentre.y + radius);
				radialAxis.setTickPosition(PamAxis.ABOVE_LEFT);
				radialAxis.drawAxis(g2d, plotCentre.x, plotCentre.y, x+w, plotCentre.y);
				break;
			case RadarParameters.SIDES_FRONTHALF:
				radialAxis.setTickPosition(PamAxis.ABOVE_LEFT);
				radialAxis.drawAxis(g2d, plotCentre.x, plotCentre.y, plotCentre.x, plotCentre.y - radius);
				radialAxis.setTickPosition(PamAxis.BELOW_RIGHT);
				radialAxis.drawAxis(g2d, plotCentre.x, plotCentre.y, x+w, plotCentre.y);
				break;
			case RadarParameters.SIDES_LEFTHALF:
				radialAxis.setTickPosition(PamAxis.BELOW_RIGHT);
				radialAxis.drawAxis(g2d, plotCentre.x, plotCentre.y, plotCentre.x, plotCentre.y - radius);
				radialAxis.setTickPosition(PamAxis.ABOVE_LEFT);
				radialAxis.drawAxis(g2d, plotCentre.x, plotCentre.y, plotCentre.x-radius, plotCentre.y);
				break;
			case RadarParameters.SIDES_RIGHTHALF:
				radialAxis.setTickPosition(PamAxis.ABOVE_LEFT);
				radialAxis.drawAxis(g2d, plotCentre.x, plotCentre.y, plotCentre.x, plotCentre.y - radius);
				radialAxis.setTickPosition(PamAxis.BELOW_RIGHT);
				radialAxis.drawAxis(g2d, plotCentre.x, plotCentre.y, plotCentre.x+radius, plotCentre.y);
				break;
			}
			// now put in the grid at the points defined by the axis grid
			radialAxis.setPosition(0, 0, radius, 0);
			ArrayList<Point> axisPoints = radialAxis.getAxisPoints(false);
			int r;
			float[] dashes = {2, 4};
			Stroke s = g2d.getStroke();
			g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dashes, 0));
			for (int i = 0; i < axisPoints.size(); i++) {
				r = axisPoints.get(i).x;// - plotCentre.x;
				if (r >= radius) break;
				g2d.drawArc(x=getArcX(plotCentre, r), y=getArcY(plotCentre, r), 
						w=getArcWidth(plotCentre, r), h=getArcHeight(plotCentre, r), angleStart, angleSweep);
				
			}
			if (radarParameters.angleGrid > 0) {
				for (int a = angleStart + radarParameters.angleGrid; 
				a < angleStart + angleSweep; 
				a+= radarParameters.angleGrid) {
					if (a%90 == 0) continue;
					g2d.drawLine(plotCentre.x, plotCentre.y, 
							(int) (plotCentre.x + radius * Math.cos(a * Math.PI/180.)),
							(int) (plotCentre.y - radius * Math.sin(a * Math.PI/180)));
				}
			}
			String rc = "(Radial Coordinate = " + radarParameters.getScaleName() + ")";
			x = plotCentre.x - g2d.getFontMetrics().stringWidth(rc)/2;
			y = plotCentre.y + radius;
//			y = (y + getHeight()) / 2;
			y += g2d.getFontMetrics().getHeight()*3/2;
			g2d.drawString(rc, x, y);
			
			if (radarScroller != null) {
				long start = radarScroller.getValueMillis();
				long end = (long) (start + rangeSpinner.getSpinnerValue() * 1000);
				x = g2d.getFontMetrics().getMaxAdvance()/2;
				y = getHeight() - g2d.getFontMetrics().getHeight()/2;
//				rc = String.format("%s - %s", PamCalendar.formatDateTime(start), PamCalendar.formatDateTime(end));
				rc = String.format("%s", PamCalendar.formatDateTime(start));
				g2d.drawString(rc, x, y);
				rc = String.format("%s", PamCalendar.formatDateTime(end));
				x = getWidth() - g2d.getFontMetrics().stringWidth(rc + " ");
				if (x < (getWidth()/2 + 10)) {
					rc = String.format("%s", PamCalendar.formatTime(end, true));
					x = getWidth() - g2d.getFontMetrics().stringWidth(rc + " ");
				}
				g2d.drawString(rc, x, y);
			}
			/*
			 * Write heading information in top right hand corner...
			 */
			String headString;
			switch (radarParameters.orientation) {
			case RadarParameters.HEAD_UP:
				headString = "Head Up";
				break;
			case RadarParameters.NORTH_UP:
				headString = "North Up";
				break;
			default:
				headString = "Error - Unknown Orientation";
			}
//			x = getWidth() - g2d.getFontMetrics().stringWidth(headString) - 10;
			x = plotCentre.x;
//			y = g2d.getFontMetrics().getHeight()*3/2;
			y = plotCentre.y - radius - g2d.getFontMetrics().getHeight()/2;
			g2d.drawString(headString, x, y);
		}
	}
	
	int getArcX(Point centre, int radius) {
		switch (radarParameters.sides) {
		case RadarParameters.SIDES_ALL:
			return centre.x - radius;
		case RadarParameters.SIDES_BACKHALF:
			return centre.x - radius;
		case RadarParameters.SIDES_FRONTHALF:
			return centre.x - radius;
		case RadarParameters.SIDES_LEFTHALF:
			return centre.x - radius;
		case RadarParameters.SIDES_RIGHTHALF:
			return centre.x - radius;
		}
		return 0;
	}
	
	int getArcY(Point centre, int radius) {
		switch (radarParameters.sides) {
		case RadarParameters.SIDES_ALL:
			return centre.y - radius;
		case RadarParameters.SIDES_BACKHALF:
			return centre.y - radius;
		case RadarParameters.SIDES_FRONTHALF:
			return centre.y - radius;
		case RadarParameters.SIDES_LEFTHALF:
			return centre.y - radius;
		case RadarParameters.SIDES_RIGHTHALF:
			return centre.y - radius;
		}
		return 0;
	}
	
	int getArcWidth(Point centre, int radius) {
		switch (radarParameters.sides) {
		case RadarParameters.SIDES_ALL:
			return radius * 2;
		case RadarParameters.SIDES_BACKHALF:
			return radius * 2;
		case RadarParameters.SIDES_FRONTHALF:
			return radius * 2;
		case RadarParameters.SIDES_LEFTHALF:
			return radius * 2;
		case RadarParameters.SIDES_RIGHTHALF:
			return radius * 2;
		}
		return 0;
	}
	
	int getArcHeight(Point centre, int radius) {
		switch (radarParameters.sides) {
		case RadarParameters.SIDES_ALL:
			return radius * 2;
		case RadarParameters.SIDES_BACKHALF:
			return radius * 2;
		case RadarParameters.SIDES_FRONTHALF:
			return radius * 2;
		case RadarParameters.SIDES_LEFTHALF:
			return radius * 2;
		case RadarParameters.SIDES_RIGHTHALF:
			return radius * 2;
		}
		return 0;
	}
	
	int getAngleStart() {
		switch (radarParameters.sides) {
		case RadarParameters.SIDES_ALL:
			return 0;
		case RadarParameters.SIDES_BACKHALF:
			return -180;
		case RadarParameters.SIDES_FRONTHALF:
			return 0;
		case RadarParameters.SIDES_LEFTHALF:
			return 90;
		case RadarParameters.SIDES_RIGHTHALF:
			return -90;
		}
		return 0;
	}
	
	int getAngleSweep() {
		switch (radarParameters.sides) {
		case RadarParameters.SIDES_ALL:
			return 360;
		case RadarParameters.SIDES_BACKHALF:
			return 180;
		case RadarParameters.SIDES_FRONTHALF:
			return 180;
		case RadarParameters.SIDES_LEFTHALF:
			return 180;
		case RadarParameters.SIDES_RIGHTHALF:
			return 180;
		}
		return 0;
	}
	
	int getPlotWidth() {
		switch (radarParameters.sides) {
		case RadarParameters.SIDES_ALL:
			return getRadius() * 2;
		case RadarParameters.SIDES_BACKHALF:
			return getRadius() * 2;
		case RadarParameters.SIDES_FRONTHALF:
			return getRadius() * 2;
		case RadarParameters.SIDES_LEFTHALF:
			return getRadius();
		case RadarParameters.SIDES_RIGHTHALF:
			return getRadius();
		}
		return 0;
	}
	
	int getPlotHeight() {
		switch (radarParameters.sides) {
		case RadarParameters.SIDES_ALL:
			return getRadius() * 2;
		case RadarParameters.SIDES_BACKHALF:
			return getRadius();
		case RadarParameters.SIDES_FRONTHALF:
			return getRadius();
		case RadarParameters.SIDES_LEFTHALF:
			return getRadius() * 2;
		case RadarParameters.SIDES_RIGHTHALF:
			return getRadius() * 2;
		}
		return 0;
	}
	
	
	int getWestSpace() {
		switch (radarParameters.sides) {
		case RadarParameters.SIDES_ALL:
			return radialAxis.getExtent(radarPlot.getGraphics());
		case RadarParameters.SIDES_BACKHALF:
			return radialAxis.getExtent(radarPlot.getGraphics());
		case RadarParameters.SIDES_FRONTHALF:
			return radialAxis.getExtent(radarPlot.getGraphics());
		case RadarParameters.SIDES_LEFTHALF:
			return radialAxis.getExtent(radarPlot.getGraphics());
		case RadarParameters.SIDES_RIGHTHALF:
			return radialAxis.getExtent(radarPlot.getGraphics());
		}
		return 0;
	}
	
	int getEastSpace() {
		switch (radarParameters.sides) {
		case RadarParameters.SIDES_ALL:
			return radialAxis.getExtent(radarPlot.getGraphics());
		case RadarParameters.SIDES_BACKHALF:
			return radialAxis.getExtent(radarPlot.getGraphics());
		case RadarParameters.SIDES_FRONTHALF:
			return radialAxis.getExtent(radarPlot.getGraphics());
		case RadarParameters.SIDES_LEFTHALF:
			return radialAxis.getExtent(radarPlot.getGraphics());
		case RadarParameters.SIDES_RIGHTHALF:
			return radialAxis.getExtent(radarPlot.getGraphics());
		}
		return 0;
	}
	
	int getNorthSpace() {
		switch (radarParameters.sides) {
		case RadarParameters.SIDES_ALL:
			return radialAxis.getExtent(radarPlot.getGraphics());
		case RadarParameters.SIDES_BACKHALF:
			return radialAxis.getExtent(radarPlot.getGraphics());
		case RadarParameters.SIDES_FRONTHALF:
			return radialAxis.getExtent(radarPlot.getGraphics());
		case RadarParameters.SIDES_LEFTHALF:
			return radialAxis.getExtent(radarPlot.getGraphics());
		case RadarParameters.SIDES_RIGHTHALF:
			return radialAxis.getExtent(radarPlot.getGraphics());
		}
		return 0;
	}
	
	int getSouthSpace() {
		switch (radarParameters.sides) {
		case RadarParameters.SIDES_ALL:
			return radialAxis.getExtent(radarPlot.getGraphics());
		case RadarParameters.SIDES_BACKHALF:
			return radialAxis.getExtent(radarPlot.getGraphics());
		case RadarParameters.SIDES_FRONTHALF:
			return radialAxis.getExtent(radarPlot.getGraphics());
		case RadarParameters.SIDES_LEFTHALF:
			return radialAxis.getExtent(radarPlot.getGraphics());
		case RadarParameters.SIDES_RIGHTHALF:
			return radialAxis.getExtent(radarPlot.getGraphics());
		}
		return 0;
	}
	
	Point getCentre() {
		switch (radarParameters.sides) {
		case RadarParameters.SIDES_ALL:
			return new Point((getWestSpace() + radarPlot.getWidth() - getEastSpace())/2,
					(getNorthSpace() + radarPlot.getHeight() - getSouthSpace()) / 2);
		case RadarParameters.SIDES_BACKHALF:
			return new Point((getWestSpace() + radarPlot.getWidth() - getEastSpace())/2,
					getNorthSpace());
		case RadarParameters.SIDES_FRONTHALF:
			return new Point((getWestSpace() + radarPlot.getWidth() - getEastSpace())/2,
					getNorthSpace() + getRadius());
		case RadarParameters.SIDES_LEFTHALF:
			return new Point(getWestSpace() + getRadius(),
					(getNorthSpace() + radarPlot.getHeight() - getSouthSpace()) / 2);
		case RadarParameters.SIDES_RIGHTHALF:
			return new Point(getWestSpace(),
					(getNorthSpace() + radarPlot.getHeight() - getSouthSpace()) / 2);
		}
		return new Point((getWestSpace() + radarPlot.getWidth() - getEastSpace())/2,
				(getNorthSpace() + radarPlot.getHeight() - getSouthSpace()) / 2);
	}
	
	int getRadius() {
		switch (radarParameters.sides) {
		case RadarParameters.SIDES_ALL:
			return Math.min(radarPlot.getWidth() - getWestSpace() - getEastSpace(),
					radarPlot.getHeight() - getNorthSpace() - getSouthSpace()) / 2;
		case RadarParameters.SIDES_BACKHALF:
		case RadarParameters.SIDES_FRONTHALF:
			return Math.min((radarPlot.getWidth() - getWestSpace() - getEastSpace()) / 2,
					radarPlot.getHeight() - getNorthSpace() - getSouthSpace());
		case RadarParameters.SIDES_LEFTHALF:
		case RadarParameters.SIDES_RIGHTHALF:
			return Math.min(radarPlot.getWidth() - getWestSpace() - getEastSpace(),
					(radarPlot.getHeight() - getNorthSpace() - getSouthSpace())/2) ;
		}
		return Math.min(radarPlot.getWidth() - getWestSpace() - getEastSpace(),
				radarPlot.getHeight() - getNorthSpace() - getSouthSpace()) / 2;
	}

	class PopupListener extends MouseAdapter {
		
		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}
		
		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				getPlotDetectorMenu()
				.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
	JPopupMenu detectorMenu;
	JPopupMenu getPlotDetectorMenu() {
		if (radarParameters == null)
			return null;
		if (detectorMenu == null) {
			detectorMenu = new JPopupMenu();
			SettingsAction settingsAction = new SettingsAction();
			JMenuItem menuItem;
			menuItem = new JMenuItem("Settings ...");
			menuItem.addActionListener(settingsAction);
			detectorMenu.add(menuItem);
		}
		return detectorMenu;
	}
	class SettingsAction implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			RadarParameters newParams = RadarParametersDialog.showDialog(RadarDisplay.this,
					userDisplayControl.getPamView().getGuiFrame(), radarParameters, radarProjector);
			if (newParams != null) {
				radarParameters = newParams.clone();
				newSettings();
			}
			
		}
		
	}
	public void newSettings() {
		
		radarProjector.setRadarParameters(radarParameters);
		
		switch (radarParameters.radialAxis) {
		case RadarParameters.RADIAL_AMPLITIDE:
			radialAxis.setRange(radarParameters.rangeStartdB, radarParameters.rangeEnddB);
			radialAxis.setLabel("dB");
			radialAxis.setInterval(PamAxis.INTERVAL_AUTO);
			break;
		case RadarParameters.RADIAL_DISTANCE:
			if (radarParameters.rangeEndm > 3000){
				radialAxis.setRange(radarParameters.rangeStartm/1000, radarParameters.rangeEndm/1000);
				radialAxis.setLabel("km");
			}
			else {
				radialAxis.setRange(radarParameters.rangeStartm, radarParameters.rangeEndm);
				radialAxis.setLabel("m");
			}
			radialAxis.setInterval(PamAxis.INTERVAL_AUTO);
			break;
		case RadarParameters.RADIAL_SLANT_ANGLE:
			radialAxis.setRange(90, 0);
			radialAxis.setLabel("deg.");
			radialAxis.setInterval(-45);
		}
		if (getFrame() != null) {
			if (radarParameters.windowName != null && radarParameters.windowName.length() > 0) {
				getFrame().setTitle(radarParameters.windowName);
			}
			else {
				getFrame().setTitle("Radar display");
			}
		}
		
		subscribeDetectors();
		
		createKeyPanel();
		
		repaintAll();
	}
	
	public String getFrameTitle() {
		if (radarParameters.windowName != null) {
			return radarParameters.windowName;
		}
		else if (getFrame() != null) {
			return getFrame().getTitle();
		}
		else {
			return "Radar Display";
		}
	}
	
	public String getDataSelectTitle() {
		return "Radar" + getFrameTitle();
	}
	
	void repaintAll(){
		if (radarPlot.isShowing() == false) return;
		radarAxis.repaint();
		radarPlot.repaint();
	}
	
	class TimerListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
						
			radarPlot.repaint();
		}
	}
	
	private void subscribeDetectors() {
		if (radarScroller != null) {
			radarScroller.removeAllDataBlocks();
		}
		detectorDataBlocks = 
			PamController.getInstance().getDataBlocks(PamDataUnit.class, true);
		if (detectorDataBlocks == null) return;
		
		// check if there is old data that needs to be converted.  If so, convert
		if (radarParameters.isThereOnlyOldData()) {
			radarParameters.convertOldData(detectorDataBlocks);
		}

		PamDataBlock dataBlock;
		for (int i = 0; i < detectorDataBlocks.size(); i++) {
			dataBlock = detectorDataBlocks.get(i);
			dataBlock.deleteObserver(this);
			if (radarParameters.getRadarDataInfo(dataBlock).select == false) continue;
			dataBlock.addObserver(this);
			if (radarScroller != null) {
				radarScroller.addDataBlock(dataBlock);
			}
		}
		

		gpsDataBlock = PamController.getInstance().getDataBlock(GpsDataUnit.class, 0);
		if (gpsDataBlock != null) {
			gpsDataBlock.addObserver(this);
		}
	}
	
	private PamDataBlock<GpsDataUnit> gpsDataBlock;
	private GpsDataUnit lastGpsDataUnit;
	
	private void createKeyPanel() {
		if (keyPanel != null) {
			this.radarPlot.remove(keyPanel.getPanel());
		}

		keyPanel = new KeyPanel("key", PamKeyItem.KEY_VERBOSE);
		PamDataBlock dataBlock;
		for (int i = 0; i < detectorDataBlocks.size(); i++) {
			dataBlock = detectorDataBlocks.get(i);
			if (dataBlock.canDraw(radarProjector) == false || radarParameters.getRadarDataInfo(dataBlock).select == false) continue;
//			keyPanel.add(dataBlock.createKeyItem(radarProjector, keyPanel.getKeyType()));
			
			SymbolData symbolData = null;
			PamSymbolManager symbolManager = dataBlock.getPamSymbolManager();
			if (symbolManager != null) {
				PamSymbolChooser symbolChooser = symbolManager.getSymbolChooser(radarDisplayComponent.getUniqueName(), radarProjector);
				try {
					symbolData = symbolChooser.getSymbolChoice(radarProjector, null);
				}
				catch (Exception e) {
					// will throw when the symbolchooser can't handle a null data unit
				}
			}
			if (symbolData != null) {
				keyPanel.add(new SymbolKeyItem(new PamSymbol(symbolData), dataBlock.getDataName()));
			}
			else {
				keyPanel.add(dataBlock.createKeyItem(radarProjector, keyPanel.getKeyType()));
			}

		}
		radarPlot.setKeyPanel(keyPanel);
		
		int keyPos = CornerLayoutContraint.FIRST_LINE_START;
		switch (radarParameters.sides) {
		case RadarParameters.SIDES_ALL:
			keyPos = CornerLayoutContraint.FIRST_LINE_START;
			break;
		case RadarParameters.SIDES_BACKHALF:
			keyPos = CornerLayoutContraint.LAST_LINE_START;
			break;
		case RadarParameters.SIDES_FRONTHALF:
			keyPos = CornerLayoutContraint.FIRST_LINE_START;
			break;
		case RadarParameters.SIDES_LEFTHALF:
			keyPos = CornerLayoutContraint.FIRST_LINE_START;
			break;
		case RadarParameters.SIDES_RIGHTHALF:
			keyPos = CornerLayoutContraint.FIRST_LINE_END;
			break;
		}
		radarPlot.setKeyPosition(keyPos);
	}

	public String getObserverName() {
		return getName();
	}

	public long getRequiredDataHistory(PamObservable o, Object arg) {
		if (detectorDataBlocks == null) return 0;
		PamDataBlock dataBlock = (PamDataBlock) o;
		if (radarParameters.getRadarDataInfo(dataBlock).select) {
			return radarParameters.getRadarDataInfo(dataBlock).getDetectorLifetime()*1000;
		}
		return 0;
	}

	public void noteNewSettings() {
		// TODO Auto-generated method stub
		newSettings();
		
	}

	public void removeObservable(PamObservable o) {
		// TODO Auto-generated method stub
		
	}

	public void setSampleRate(float sampleRate, boolean notify) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void masterClockUpdate(long milliSeconds, long sampleNumber) {

		this.masterMilliseconds = milliSeconds;
		this.masterSampleNumber = sampleNumber;
		
	}

	public void addData(PamObservable o, PamDataUnit pamDataUnit) {

		if (o == gpsDataBlock) {
			setLastGpsDataUnit((GpsDataUnit) pamDataUnit);
		}
//		if (radarPlot.isShowing() == false) return;
//		
//		PamDataBlock dataBlock = (PamDataBlock) o;
//		dataBlock.drawDataUnit((Graphics2D) radarPlot.getGraphics(), pamDataUnit, radarProjector);
		radarPlot.repaint(100);
		
	}

	@Override
	public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
		radarPlot.repaint(100);
	}

	@Override
	public void receiveSourceNotification(int type, Object object) {
		// don't do anything by default
	}

	public GpsDataUnit getLastGpsDataUnit() {
		return lastGpsDataUnit;
	}

	public void setLastGpsDataUnit(GpsDataUnit lastGpsDataUnit) {
		this.lastGpsDataUnit = lastGpsDataUnit;
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#getUnitName()
	 */
	@Override
	public String getUnitName() {
		return radarDisplayComponent.getUniqueName();
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#getUnitType()
	 */
	@Override
	public String getUnitType() {
		return "Radar Display";
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#getSettingsReference()
	 */
	@Override
	public Serializable getSettingsReference() {
		return radarParameters;
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#getSettingsVersion()
	 */
	@Override
	public long getSettingsVersion() {
		return RadarParameters.serialVersionUID;
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#restoreSettings(PamController.PamControlledUnitSettings)
	 */
	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		this.radarParameters = ((RadarParameters) pamControlledUnitSettings.getSettings()).clone();
		return (radarParameters != null);
	}
	
}
