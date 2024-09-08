package Array;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import Acquisition.AcquisitionControl;
import GPS.GpsData;
import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamController.PamController;
import PamUtils.LatLong;
import PamView.PamColors.PamColor;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.panel.PamBorder;
import pamMaths.PamVector;

/**
 * Panel for the ArrayDialog to show a diagram of the array
 * @author Doug Gillespie
 * @see Array.ArrayDialog
 *
 */
public class HydrophoneDiagram {

	private JPanel hydrophonePanel;
	
	private PlotPanel plotPanel;
	
	private AxisPanel axisPanel;
	
	private PamAxis xAxis, yAxis;
	
	/**
	 * Double vector of x,y,z max and min values.  
	 */
	private double [][] plotLims;
	
	private LatLong refLatLong;
//	private double plotScale = 1;
	
	private ArrayDialog arrayDialog;

private double[][] relPositions;
private double[][] originPositions;

/**
 * Create titled border
 */
private TitledBorder titledBorder;
	
	public HydrophoneDiagram(ArrayDialog arrayDialog) {
		
		this.arrayDialog = arrayDialog;
		
		hydrophonePanel = makeHydrophonePanel();
	}
	
	public JPanel getPlotPanel() {
		return hydrophonePanel;
	}

	JPanel makeHydrophonePanel() {
		JPanel panel = new JPanel();
		panel.setBorder(titledBorder = new TitledBorder("Hydrophone positions"));
		panel.setLayout(new BorderLayout());
//		JPanel b = new JPanel();
//		b.setBorder(BorderFactory.createLoweredBevelBorder());
//		b.setLayout(new BorderLayout());
		panel.add(BorderLayout.CENTER, axisPanel = new AxisPanel());
//		panel.setPreferredSize(new Dimension(390, 110));
//		panel.add(BorderLayout.CENTER, new PamAxisP);
		
		return panel;
	}
	
	void rePaint() {
		setScales();
		titledBorder.setTitle(PamController.getInstance().getGlobalMediumManager().getRecieverString()); 
		axisPanel.repaint();	
		plotPanel.repaint();	
	}
	
	private boolean setScales() {
		relPositions = null;
		PamArray array = arrayDialog.getHydrophoneDialogPanel().getDialogSelectedArray();
		if (array == null) return false;
		// first look and see if any are using static data or gps data and if we can 
		// get some kind of reference position for the streamers.
		int nStreamers = array.getNumStreamers();
		if (nStreamers == 0) {
			return false;
		}
		int nHydrophones = array.getHydrophoneCount();
		if (nHydrophones == 0) {
			return false;
		}
		relPositions = new double[nHydrophones][3];
		originPositions = new double[nHydrophones][3];
//		for (int i = 0; i < nHydrophones; i++) {
//			array.getMasterLocator().getPhoneLatLong(timeMilliseconds, i);
//		}
		GpsData[] sGps = new GpsData[nStreamers];
		GpsData firstGoodGPS = null;
		int nGood = 0;
		for (int i = 0; i < nStreamers; i++) {
			try {
				sGps[i] = array.getStreamer(i).getHydrophoneOrigin().getLastStreamerData().getGpsData();
				if (firstGoodGPS == null) {
					firstGoodGPS = sGps[i];
				}
				nGood = 0;
			}
			catch (Exception e) {
				sGps[i] = null;
			}
		}
		if (nGood == 0) {
			firstGoodGPS = new GpsData();
		}
		for (int i = 0; i < nStreamers; i++) {
			if (sGps[i] == null) {
				sGps[i] = firstGoodGPS;
			}
		}

		plotLims = new double[3][2];
		// now set all origins relative to the first gps data.
		for (int i = 0; i < nHydrophones; i++) {
			relPositions[i] = array.getHydrophone(i).getCoordinates();
			int iStreamer = array.getHydrophone(i).getStreamerId();
			Streamer streamer = array.getStreamer(iStreamer);
			double[] sCoords = streamer.getCoordinates();
			for (int j = 0; j < 3; j++) {
				relPositions[i][j] += sCoords[j];
				originPositions[i][j] += sCoords[j];
			}
//			originPositions[i] = relPositions[i].clone();
			
			relPositions[i][0] += sGps[0].distanceToMetresX(sGps[iStreamer]);
			relPositions[i][1] += sGps[0].distanceToMetresY(sGps[iStreamer]);
			originPositions[i][0] += sGps[0].distanceToMetresX(sGps[iStreamer]);
			originPositions[i][1] += sGps[0].distanceToMetresY(sGps[iStreamer]);
			originPositions[i][2] += sGps[iStreamer].getHeight();
			if (i == 0) {
				for (int j = 0; j < 3; j++) {
					plotLims[j][0] = plotLims[j][1] = relPositions[0][j];
				}
			}
			else {
				for (int j = 0; j < 3; j++) {
					plotLims[j][0] = Math.min(plotLims[j][0], relPositions[i][j]);
					plotLims[j][1] = Math.max(plotLims[j][1], relPositions[i][j]);
				}
			}
		}
		
		
		// look through the hydrophones and set the min and max x and y scales. 

		double[][] arrayLims = plotLims;
		if (arrayLims==null) return false;
		double maxDim = Math.max(arrayLims[0][1]-arrayLims[0][0], arrayLims[1][1]-arrayLims[1][0]);
		// need a border of about 1/4 the max dimension to make the plot look OK
		double border = maxDim/4;
		if (border <= 0) {
			border = 1;
		}
		// and force it to be a power of 10;
		border = (int) Math.ceil(Math.log10(border));
		border = Math.pow(10, border);
		for (int i = 0; i < 3; i++) {
			arrayLims[i][0] -= border;
			arrayLims[i][1] += border;
		}
		
		double[] dimSizes = new double[3];
		double minSize = 1;
		double extraNeeded;
		for (int i = 0; i < 3; i++) {
			dimSizes[i] = arrayLims[i][1]-arrayLims[i][0];
			plotLims[i] = Arrays.copyOf(arrayLims[i],2);
			if (dimSizes[i] < minSize) {
				extraNeeded = minSize-dimSizes[i];
				plotLims[i][0] -= extraNeeded/2;
				plotLims[i][1] += extraNeeded/2;
				dimSizes[i] = minSize;
			}
		}

		Dimension winSize = plotPanel.getSize();
		double xScale = dimSizes[0] / winSize.getWidth();
		double yScale = dimSizes[1] / winSize.getHeight();
		// need to take the largest
		double scale = Math.max(xScale, yScale);
		// now set the upper plot limits according to the scale.
		plotLims[0][1] = plotLims[0][0] + scale * winSize.getWidth();
		plotLims[1][1] = plotLims[1][0] + scale * winSize.getHeight();
//		
//		
//		double largestScale = Math.max(dimSizes[0], dimSizes[1]);
//		double scale = 1;
//		while (scale < largestScale * 3/2) scale *= Math.sqrt(10);
//		scale = 2 * Math.ceil(scale/2);
		
		
		
		xAxis.setRange(plotLims[0][0], plotLims[0][1]);
		yAxis.setRange(plotLims[1][0], plotLims[1][1]);
		
		return true;		
	}
	
	class AxisPanel extends PamAxisPanel {

		public AxisPanel() {
			super();
			
			setInnerPanel(plotPanel = new PlotPanel());
			
			setAutoInsets(true);
			
			xAxis = new PamAxis(0,1,2,3,-5, 5, false, "x (m)", "%3.1f");
			yAxis = new PamAxis(0,1,2,3,-5, 5, true, "y (m)", "%6.1f");
			
			setSouthAxis(xAxis);
			setWestAxis(yAxis);
		}

		@Override
		public PamColor getColorId() {
			return null;
		}
		
	}
	class PlotPanel extends JPanel {

		Dimension lastPlotSize = new Dimension(1,1);

		PamSymbol pamSymbol;
		
		int arrowLength = 6;
		
		public PlotPanel() {
			super();
			setBackground(Color.WHITE);
			setBorder(PamBorder.createInnerBorder());
			pamSymbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 8, 8,
					true, Color.BLUE, Color.BLUE);
			setToolTipText(PamController.getInstance().getGlobalMediumManager().getRecieverString() + " Layout");
		}
		
		@Override
		protected void paintComponent(Graphics g) {

			super.paintComponent(g);

			Graphics2D g2D = (Graphics2D) g;
			
			if (relPositions == null) {
				return;
			}
			int n = relPositions.length;
			int x, y, ox, oy, oox, ooy;
			String str;
			AcquisitionControl acquisitionControl = arrayDialog.getChannelPanel().getAcquisitionControl();
			for (int i = 0; i < n; i++) {
				x = (int) xAxis.getPosition(relPositions[i][0]);
				y = (int) (yAxis.getPosition(relPositions[i][1]));
				ox = (int) xAxis.getPosition(originPositions[i][0]);
				oy = (int) (yAxis.getPosition(originPositions[i][1]));
				pamSymbol.draw(g, new Point(x,y));

				str = String.format("%d ", i);
				g.setColor(Color.BLACK);
				g.drawLine(x, y, ox, y);
				g.drawLine(ox, y, ox, oy);
				g.setColor(Color.GRAY);
				g.drawLine(ox, oy, ox, 0);

				g.setColor(Color.BLACK);
				g.drawString(str, x+5, y-5);
				if (acquisitionControl != null) {
					int adcChannel = acquisitionControl.findHydrophoneChannel(i);
					if (adcChannel >= 0) {
						Rectangle2D strRect = g.getFontMetrics().getStringBounds(str, g);
						g.setColor(Color.RED);
						g.drawString(String.format("(%d)", adcChannel), (int)(x+5 + strRect.getWidth()), y-5);
					}
				}
			}
			
			// write in the corner what the numbers mean.
			str = new String(PamController.getInstance().getGlobalMediumManager().getRecieverString() + " Id ");
			Rectangle2D strRect = g.getFontMetrics().getStringBounds(str, g);
			g.setColor(Color.BLACK);
			x = 10;
			Dimension newSize = getSize();
			y = (newSize.height - g.getFontMetrics().getAscent() );
			g.drawString(str, x, y);
			x += (int) strRect.getWidth();
			g.setColor(Color.RED);
			g.drawString("(ADC channel)", x, y);
				
			// indicate the direction of the primary axis
			PamVector[] allAxes = ArrayManager.getArrayManager().getArrayDirections();
			if (allAxes!=null) {
				double xVal = allAxes[0].getVector()[0];
				double yVal = allAxes[0].getVector()[1];
				double zVal = allAxes[0].getVector()[2];
				str=null;
				if (yVal==0 && zVal==0) {
					if (xVal==1) {
						str = "Direction of Primary Axis \u2192";
					} else if (xVal==-1) {
						str = "Direction of Primary Axis \u2190";
					}
				}
				else if (xVal==0 && zVal==0) {
					if (yVal==1) {
						str = "Direction of Primary Axis \u2191";
					} else if (yVal==-1) {
						str = "Direction of Primary Axis \u2193";
					}
				}
				else if (xVal==0 && yVal==0) {
					if (zVal==1) {
						str = "Direction of Primary Axis \u21F1 (+z)";
					} else if (zVal==-1) {
						str = "Direction of Primary Axis \u21F2 (-z)";
					}
				}
				if (str!=null) {
					x = 10;
					y = newSize.height - g.getFontMetrics().getAscent() - (int) strRect.getHeight();
					g.setColor(Color.BLACK);
					g.drawString(str, x, y);
				}			
			}
		}
//		@Override
//		protected void paintComponent(Graphics g) {
//
//			super.paintComponent(g);
//
//			Graphics2D g2D = (Graphics2D) g;
//
//			Dimension newSize = getSize();
//			if (newSize.equals(lastPlotSize) == false) {
//				setScales();
//				if (axisPanel != null) axisPanel.repaint();
//				lastPlotSize = newSize;
//			}
//			
//			AcquisitionControl acquisitionControl = arrayDialog.getChannelPanel().getAcquisitionControl();
//
//			PamArray array = arrayDialog.getHydrophoneDialogPanel().getDialogSelectedArray();
//			
//			/**
//			 * Write the type of array up into the top left corner.
//			 * 
//			 */
//			ArrayManager.getArrayManager();
//			int arrayType = ArrayManager.getArrayManager().getArrayShape(array);
//			String arrayStr = ArrayManager.getArrayTypeString(arrayType);
//			FontMetrics fm = g.getFontMetrics();
//			g.drawString(arrayStr, fm.charWidth(' '), fm.getHeight());
//			
////			PamVector[] locVectors = ArrayManager.getArrayManager().getArrayDirections(array);
////			for (int i = 0; i < locVectors.length; i++) {
////				System.out.println(String.format("Loc vector %d = %s", i, locVectors[i].toString()));
////			}
//			
//			double[][] arrayLims = array.getDimensionLimits();
//			if (arrayLims==null) return;
//			double[] yLim = arrayLims[1];
//			// probably it's behind
//			yLim[1] = Math.max(yLim[1], 0);
//			yLim[0] = Math.min(yLim[0], 0);
//			Streamer streamer;
//			double streamerX;
//			int x1, y1, x2, y2;
////			Rectangle2D r = g.getFontMetrics().getStringBounds(course, g);
////			// now draw the rotated text
//			
//			Rectangle2D tR;
//			String strString;
//			
////			double maxY = arrayLims[1][1];
////			double minY = arrayLims[1][0];
//			
//			y1 = (int)yAxis.getPosition(yLim[0]);
//			y2 = (int)yAxis.getPosition(yLim[1]);
////			y1 = newSize.height - (int) ((yLim[0] - minY) * plotScale);
////			y2 = newSize.height - (int) ((yLim[1] - minY) * plotScale);
//			for (int i = 0; i < array.getNumStreamers(); i++) {
//				streamer = array.getStreamer(i);
//				x1 = x2 = (int) xAxis.getPosition(streamer.getX());
//				g.drawLine(x1, y1, x2, y2);
//				strString = String.format("Streamer %d", i);
//				tR = g.getFontMetrics().getStringBounds(strString, g);
//				g2D.rotate(-Math.PI/2.);
//				g.drawString(strString, (int) -tR.getMaxX() - 30, (int) (x1+tR.getCenterY()));
//				g2D.rotate(Math.PI/2.);
//			}
//			
////			PamArray array = ArrayManager.getArrayManager().getCurrentArray();
//			int nPhones = array.getHydrophoneArray().size();
//			Hydrophone phone;
//			int x, y;
//			String str;
//			int adcChannel;
//			Rectangle2D strRect;
//			double phoneX, phoneY;
//			for (int i = 0; i < nPhones; i++) {
//				phone = array.getHydrophone(i);
//				streamer = array.getStreamer(phone.getStreamerId());
//				phoneX = phone.getX();
//				phoneY = phone.getY();
//				if (streamer != null) {
//					phoneX += streamer.getX();
//					phoneY += streamer.getY();
//				}
//				x = (int) xAxis.getPosition(phoneX);
//				y = (int) yAxis.getPosition(phoneY);
////				y = newSize.height - (int) ((phone.getY() - minY) * plotScale);
//				pamSymbol.draw(g, new Point(x,y));
//				g.setColor(Color.BLACK);
//				if (streamer != null) {
//					x2 = (int) xAxis.getPosition(streamer.getX());
//					g.drawLine(x, y, x2, y);
//				}
//				str = String.format("%d ", i);
//				g.drawString(str, x+5, y-5);
//				if (acquisitionControl != null) {
//					adcChannel = acquisitionControl.findHydrophoneChannel(i);
//					if (adcChannel >= 0) {
//						strRect = g.getFontMetrics().getStringBounds(str, g);
//						g.setColor(Color.RED);
//						g.drawString(String.format("(%d)", adcChannel), (int)(x+5 + strRect.getWidth()), y-5);
//					}
//				}
//				
//			}
//			// draw an arrow in the corner
//			g.setColor(Color.BLUE);
//			y = newSize.height /2;
//			x = newSize.width * 9/10;
//			y2 = y - Math.min((newSize.height/2), 100);
//			g.drawLine(x, y, x, y2);
//			g.drawLine(x, y2, x-arrowLength, y2+arrowLength);
//			g.drawLine(x, y2, x+arrowLength, y2+arrowLength);
//			String course = "North / Ship Heading";
////			if (array.getArrayType() == PamArray.ARRAY_TYPE_TOWED) {
////				course = new String("Ships Heading");
////			}
////			else {
////				course = new String("True North");
////			}
//			Rectangle2D r = g.getFontMetrics().getStringBounds(course, g);
//			// now draw the rotated text
//			g2D.rotate(-Math.PI/2.);
//			g.drawString(course, (int) -((y + y2)/2 + r.getCenterX()), (int) (x+r.getCenterY()));
//			g2D.rotate(Math.PI/2.);
//			
//			// and finally write in the corner what the numbers mean.
//			str = new String("Hydrophone numbers ");
//			strRect = g.getFontMetrics().getStringBounds(str, g);
//			g.setColor(Color.BLACK);
//			x = 10;
//			y = (newSize.height - g.getFontMetrics().getAscent() );
//			g.drawString(str, x, y);
//			x += (int) strRect.getWidth();
//			g.setColor(Color.RED);
//			g.drawString("(ADC channel numbers)", x, y);
//		}
//	
		private String stdText = "Hover over a " + PamController.getInstance().getGlobalMediumManager().getRecieverString() + " for more information";

		/* (non-Javadoc)
		 * @see javax.swing.JComponent#getToolTipText(java.awt.event.MouseEvent)
		 */
		@Override
		public String getToolTipText(MouseEvent event) {
			int iH = findHydrophone(event.getPoint());
			if (iH < 0) {
				return stdText;
			}
			AcquisitionControl acquisitionControl = arrayDialog.getChannelPanel().getAcquisitionControl();
			PamArray array = arrayDialog.getHydrophoneDialogPanel().getDialogSelectedArray();
			if (array == null) {
				return stdText;
			}
			Hydrophone hydrophone = array.getHydrophone(iH);
			int iS = hydrophone.getStreamerId();
			String str = String.format("<html>" + PamController.getInstance().getGlobalMediumManager().getRecieverString() + " %d<br>Streamer %d", iH, iS);
			if (acquisitionControl != null) {
				int adcChannel = acquisitionControl.findHydrophoneChannel(iH);
				if (adcChannel >= 0) {
					str += String.format("<br>Channel %d", adcChannel);
				}
			}
			
			Streamer s = array.getStreamer(iS);
			str += "<br>Coordinates: " + String.format("[%3.2f,%3.2f,%3.2f]", 
					hydrophone.getCoordinate(0), hydrophone.getCoordinate(1), hydrophone.getCoordinate(2));
			str += "<br>Type: " + hydrophone.getType();
			str += "<br>Origin: " + s.getHydrophoneOrigin().getName();
			str += "<br>Locator: " + s.getHydrophoneLocator().getName();
			str += "<br>Sensitifivy: " + hydrophone.getSensitivity() + " dBre.1&mu;Pa";
			str += "<br>Preamp gain: " + hydrophone.getPreampGain() + " dB";
			
			str += "</html>";
			return str;		
			
		}
		
		private int findHydrophone(Point point) {
			if (relPositions == null) {
				return -1;
			}
			double x, y;
			double closestDist = Integer.MAX_VALUE;
			int closestPhone = -1;
			for (int i = 0; i < relPositions.length; i++) {
				x = Math.abs(xAxis.getPosition(relPositions[i][0])-point.x);
				y = Math.abs(yAxis.getPosition(relPositions[i][1])-point.y);
				if (x+y < closestDist) {
					closestDist = x+y;
					closestPhone = i;
				}
			}
//			System.out.println(String.format("Find hydrophone at x, y %d,%d, got %d dist %d", point.x, point.))
			if (closestDist < 50) {
				return closestPhone;
			}
			else {
				return -1;
			}
		}
	}
	
	
}
