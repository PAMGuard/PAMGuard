package Array.sensors.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;
import java.awt.image.WritableRaster;
import java.io.File;
import java.net.URL;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.netlib.lapack.Sbdsdc;

import Array.sensors.ArrayDisplayParameters;
import Array.sensors.ArrayDisplayParamsProvider;
import Array.sensors.ArraySensorDataUnit;
import Array.sensors.ArraySensorFieldType;
import PamController.PamController;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamFileChooser;
import PamUtils.PamUtils;
import PamView.PamColors;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.panel.PamPanel;
import PamguardMVC.debug.Debug;
import videoRangePanel.pamImage.ImageFileFilter;

/**
 * Make a graphics component showing pitch and roll. 
 * @author dg50
 *
 */
public class PitchRollComponent extends ArrayDimComponent {

	private BufferedImage funnyImage;

	private PamSymbol lTriangle = new PamSymbol(PamSymbolType.SYMBOL_TRIANGLEL, 12, 8, true, Color.red, Color.RED);
	
	private PamSymbol circle = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, Color.BLACK, Color.BLACK);

	private ArrayDisplayParamsProvider paramsProvider;

	/**
	 * @param paramsProvider 
	 * @param streamerMap 
	 * 
	 */
	public PitchRollComponent(ArrayDisplayParamsProvider paramsProvider, int streamerMap) {
		super(streamerMap, 150, 120);
		this.paramsProvider = paramsProvider;
		setComponentPopupMenu(getComponentPopupMenu());
		setToolTipText("Streamer Pitch and Roll");
		loadFunnyImage();
	}


	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// some very bespoke painting not using a PAMAxis or anything standard. 
		ArrayDisplayParameters params = paramsProvider.getDisplayParameters();
		int w = getWidth();
		int h = getHeight();
		int yMid = h/2;
		int xMid = w/2;
		Graphics2D g2d = (Graphics2D) g;
		setFont(g);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		FontMetrics fontMetrics = g2d.getFontMetrics();
		int fontHeight = fontMetrics.getHeight();
		double pitchStep = getPitchStep(h, fontHeight);
		double[] pitchRange = params.getPitchRange();
		double pitchMean = (pitchRange[0] + pitchRange[1])/2.;
		String str = String.format("-%2d", (int) pitchRange[1]);
		LineMetrics lineMetric = fontMetrics.getLineMetrics(str, g);
		//		int axWid = lineMetric.get
		int axWid = fontMetrics.charsWidth(str.toCharArray(), 0, str.length());
		int topBorder = fontMetrics.getHeight();
		double yScale = (h-topBorder*2)/(pitchRange[1]-pitchRange[0]);
		double pitch = 0; 
		g.setColor(getForeground());
		BasicStroke dashed = new BasicStroke(.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1, 3}, 0);
		g2d.setStroke(dashed);
		int xRight = w-fontMetrics.stringWidth(".");
		int yMax = 0;
		pitch = PamUtils.roundNumberUp(pitchRange[1], pitchStep);
//		while (pitch <= pitchRange[1]) {
//			int y = (int) (yMid-pitch*yScale);
//			g.drawLine(axWid, y, xRight, y);
//			y += fontMetrics.getAscent()/2;
//			str = String.format("%d", (int) pitch);
//			int x = axWid-fontMetrics.charsWidth(str.toCharArray(), 0, str.length());
//			g.drawString(str+LatLong.deg, x, y);
//			pitch += pitchStep;
//		}
//		pitch = -pitchStep;
		while (pitch >= pitchRange[0]) {
			int y = (int) (yMid-(pitch-pitchMean)*yScale);
			yMax = Math.max(yMax, y);
			g.drawLine(axWid, y, xRight, y);
			y += fontMetrics.getAscent()/2;
			str = String.format("%d", (int) pitch);
			int x = axWid-fontMetrics.charsWidth(str.toCharArray(), 0, str.length());
			g.drawString(str+LatLong.deg, x, y);
			pitch -= pitchStep;
		}
		xMid = (axWid+xRight)/2;
		int len = (xRight-axWid)*4/5;
		int xt = w-axWid;
		int yt = getHeight();
		int nStreamers = getNStreamers();
		int nDrawn = 0;
		for (int i = nStreamers-1; i >=0; i--) {
			boolean drew = drawStreamerThing(g2d, xMid, yMid, axWid, xRight, yScale, len, i);
			if (drew) nDrawn++;
			int iStreamer = getStreamerIndex(i);
			if (nStreamers > 1) {
				String sTxt = String.format("  –S%d", iStreamer);
				xt -= fontMetrics.charsWidth(sTxt.toCharArray(), 0, sTxt.length());
				g2d.drawString(sTxt, xt, yt);
			}
		}
		if (isViewer() && nDrawn > 0) {
			for (int i = 0; i < nStreamers; i++) {
				ArraySensorDataUnit sensorData = getSensorData(i);
				if (sensorData != null) {
					String txt = PamCalendar.formatDateTime(sensorData.getTimeMilliseconds());
					Rectangle2D sb = fontMetrics.getStringBounds(txt, g);
					g.setColor(getForeground());
//					if (getHeight()-yMax >= fontMetrics.getHeight()-10) {
						yMax = getHeight();
//					}
					g2d.drawString(txt, xMid-(int)sb.getCenterX(), yMax-2);
					break;
				}
			}
		}
		

	}

	private boolean drawStreamerThing(Graphics2D g2d, int xMid, int yMid, int axWid, int xRight, double yScale, int len, int iData) {
		double[] pitchRange = paramsProvider.getDisplayParameters().getPitchRange();
		double pitchMean = (pitchRange[0] + pitchRange[1])/2.;
		Color col = getForeground();
		if (getNStreamers() > 1) {
			int iStreamer = getStreamerIndex(iData);
			col = PamColors.getInstance().getChannelColor(iStreamer);
			g2d.setColor(col);
		}
		ArraySensorDataUnit sensData = getSensorData(iData);
		if (sensData == null) {
			return false;
		}
		Double currentPitch = sensData.getField(iData, ArraySensorFieldType.PITCH);
		Double currentRoll = sensData.getField(iData, ArraySensorFieldType.ROLL);
		if (currentPitch == null) {
			currentPitch = 0.;
		}
		if (currentRoll == null) {
			currentRoll = 0.;
		}
		double y0 = yMid-(currentPitch-pitchMean)*yScale;
		if (funnyImage == null) {
			// draw the pitch roll line 
			BasicStroke solid = new BasicStroke(1.5f);
			g2d.setStroke(solid);
			double rollR = Math.toRadians(currentRoll);
			int x1 = (int) (xMid-Math.cos(rollR)*len/2);
			int x2 = (int) (xMid+Math.cos(rollR)*len/2);
			int y1 = (int) Math.round(y0-Math.sin(rollR)*len/2);
			int y2 = (int) Math.round(y0+Math.sin(rollR)*len/2);
			g2d.drawLine(x1, y1, x2, y2);
			int r = 5;
//			g.fillArc(xMid-r, (int)Math.round (y0-r), 2*r, 2*r, 0, 359);
			circle.setFillColor(col);
			circle.setLineColor(col);
			circle.draw(g2d, new Point((x1+x2+1)/2,(y1+y2+1)/2));
//			circle.draw(g, new Point(x2,y2));
			
			int arcLen = 10;
			int[] xArc = new int[2*arcLen+1];
			int[] yArc = new int[2*arcLen+1]; 
			//		g2d.setStroke(new BasicStroke(1f));
			for (int i = 0; i <= 1; i++) {
				double arcAng = currentRoll + 180*i;
				double sig = i == 0 ? -1 : 1;
				for (int j = 0; j < xArc.length; j++) {
					double ang = Math.toRadians(arcAng + j*sig);
					xArc[j] = (int) (xMid + Math.cos(ang)*len/2);
					yArc[j] = (int) (y0 + Math.sin(ang)*len/2);
				}
				g2d.drawPolyline(xArc, yArc, xArc.length);
			}
			double lF = len/4;
			int xe = (int) (xMid + Math.sin(Math.toRadians(currentRoll))*lF);
			int ye = (int) (y0 - Math.cos(Math.toRadians(currentRoll))*lF);
			g2d.drawLine(xMid, (int) y0, xe, ye);
		}
		if (funnyImage != null) {
			AffineTransform currTrans = g2d.getTransform();
			int w = getWidth();
			len = w*3/4;
			int imW = funnyImage.getWidth();
			int imH = funnyImage.getHeight();
			int dH = len*imH/imW;
			g2d.rotate(Math.toRadians(currentRoll), xMid, y0);
			g2d.drawImage(funnyImage, xMid-len/2, (int) (y0-dH/2), xMid+len/2, (int) (y0+dH/2), 0, 0, imW, imH, null);
			g2d.setTransform(currTrans);
			lTriangle.setFillColor(col);
			lTriangle.setLineColor(col);
			lTriangle.draw(g2d, new Point(xRight-lTriangle.getWidth()/2, (int) y0));
		}
		return true;
	}


	private double getPitchStep(int h, int fontHeight) {
		return paramsProvider.getDisplayParameters().getPitchStep();
	}

//	/**
//	 * @return the currentPitch
//	 */
//	public double getCurrentPitch() {
//		return currentPitch;
//	}

//	/**
//	 * @param currentPitch the currentPitch to set
//	 * @param currentPitch2 
//	 */
//	public void setCurrentPitch(int iStreamer, double currentPitch) {
//		int dataInd = getDataIndex(iStreamer);
//		if (dataInd >= 0) {
//			this.currentPitch[dataInd] = currentPitch;
//			repaint(20);
//		}
//	}
	


//	/**
//	 * @return the currentRoll
//	 */
//	public double getCurrentRoll() {
//		return currentRoll;
//	}

//	/**
//	 * @param currentRoll the currentRoll to set
//	 * @param currentRoll2 
//	 */
//	public void setCurrentRoll(int iStreamer, double currentRoll) {
//		int dataInd = getDataIndex(iStreamer);
////		Debug.out.printf("S%d ind %d Roll %3.1f\n", iStreamer, dataInd, currentRoll);
//		if (dataInd >= 0) {
//			this.currentRoll[dataInd] = currentRoll;
//			repaint(20);
//		}
//	}


	@Override
	public JPopupMenu getComponentPopupMenu() {
		JPopupMenu popMenu = new JPopupMenu();
//		JMenuItem menuItem = new JMenuItem("Select image");
//		menuItem.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				selectImage(e);
//			}
//		});
//		popMenu.add(menuItem);
//		if (funnyImage != null) {
//			menuItem = new JMenuItem("Clear image");
//			menuItem.addActionListener(new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					loadFunnyImage(null);
//				}
//			});
//			popMenu.add(menuItem);
//		}
		JMenuItem menuItem = new JMenuItem("Display Options");
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				displayOptions();
			}
		});
		popMenu.add(menuItem);
		return popMenu;
	}


	protected void displayOptions() {
		if (paramsProvider.showDisplayParamsDialog(PamController.getMainFrame())) {
			loadFunnyImage();
			repaint();
		}
	
}


	public void loadFunnyImage() {
		funnyImage = null;
		ArrayDisplayParameters params = paramsProvider.getDisplayParameters();
		if (params == null) {
			return;
		}
		String imageName = params.getPitchRollImageFile();
		if (imageName == null) {
			return;
		}
		File imageFile = new File(imageName);
		if (imageFile == null || imageFile.exists() == false) {
			return;
		}
		BufferedImage tempImage = null;
		try {
			//				URL res = ClassLoader.getSystemResource("Resources/falcon.png");
			tempImage = ImageIO.read(imageFile);
		} catch (Exception e) {
//			System.out.println(e.getMessage());
			return;
		}
		//		funnyImage = tempImage;
		if (tempImage != null) {
			WritableRaster tempRaster = tempImage.getRaster();
			int w = tempRaster.getWidth();
			int h = tempRaster.getHeight();
			funnyImage = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
			WritableRaster fRaster = funnyImage.getRaster();
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					int[] pixDat = new int[4];
					tempRaster.getPixel(i, j, pixDat);
					pixDat = Arrays.copyOf(pixDat, 4);
					if (pixDat[0]+pixDat[1]+pixDat[2] < 20) {
						pixDat[3] = 0;
					}
					else {
						pixDat[3] = 255;
					}
					fRaster.setPixel(i, j, pixDat);
				}
			}
		}
	}

}
