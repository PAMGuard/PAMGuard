package Array.sensors.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;

import Array.sensors.ArrayDisplayParamsProvider;
import Array.sensors.ArraySensorFieldType;
import PamController.PamController;
import PamController.soundMedium.GlobalMedium.SoundMedium;
import PamController.soundMedium.GlobalMediumManager;
import PamUtils.PamUtils;
import PamView.PamColors;
import PamView.PamSymbol;
import PamView.PamSymbolType;

public class DepthComponent extends ArrayDimComponent {

	PamSymbol rTriangle = new PamSymbol(PamSymbolType.SYMBOL_TRIANGLER, 8, 8, true, Color.red, Color.RED);
	
	private static final double defaultDepthRange = 40;
	
	private double stepSize = 10;

	private ArrayDisplayParamsProvider paramsProvider;
	
	public DepthComponent(ArrayDisplayParamsProvider paramsProvider, int streamerMap) {
		super(streamerMap, 25, 120);
		this.paramsProvider = paramsProvider;
		GlobalMediumManager mm = PamController.getInstance().getGlobalMediumManager();
		setToolTipText("Streamer " + mm.getZString());
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int w = getWidth();
		int h = getHeight();
		int yMid = h/2;
		int xMid = w/2;
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		setFont(g2d);
		FontMetrics fontMetrics = g2d.getFontMetrics();
		int topBorder = fontMetrics.getHeight()/2;
		double[] range = getDisplayRange();
		int txtWid = 0;
		float txtHeight = 0;
		for (int i = 0; i < 2; i++) {
			String st = getHString(range[i]);
			txtWid = Math.max(txtWid, fontMetrics.stringWidth(st));
			Rectangle2D sb = fontMetrics.getStringBounds(st, g);
			LineMetrics lm = fontMetrics.getLineMetrics(st, g);
//			txtHeight = (int) Math.max(txtHeight, sb.getHeight()-sb.getY()*0);
			txtHeight = Math.max(txtHeight, lm.getAscent()-lm.getDescent());
		}
		
		double midHeight = (range[0]+range[1])/2.;
		double yScale = (h-2*topBorder) / (range[1]-range[0]);
		double height = range[1];
		int xt = w-txtWid-2;
		BasicStroke dashed = new BasicStroke(.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1, 2}, 0);
		g2d.setStroke(dashed);
		boolean first = true;
		while (height >= range[0]) {
			int y = (int) (yMid-(height-midHeight)*yScale);
			float yt = (float) (y+txtHeight/2.);
			String txt = getHString(height);
			g2d.drawString(txt, xt, yt);
			g.drawLine(0, (int)y, xt, (int)y);
			if (first) {
				first = false;
				g2d.drawString(" m", xt, yt+txtHeight);
				
			}
			height -= stepSize;
		}
		g2d.setStroke(new BasicStroke(1));
		int nStreamers = getNStreamers();
		for (int i = 0; i < nStreamers; i++) {
			Color col = Color.BLACK;
			int iStreamer = getStreamerIndex(i);
			if (nStreamers > 1) {
				col = PamColors.getInstance().getChannelColor(iStreamer);
			}
			else {
				iStreamer = 0;
			}
			Double sH = getSensorValue(iStreamer, ArraySensorFieldType.HEIGHT);
			if (sH == null) {
				continue;
			}
			rTriangle.setFillColor(col);
			rTriangle.setLineColor(col);
			int y = (int) (yMid-(sH-midHeight)*yScale);
			rTriangle.draw(g2d, new Point(rTriangle.getWidth()/2, y));
		}
//		double meanDepth = getMeanDepth();
//		double depthRange = defaultDepthRange;
//		double minDepth = Math.max
	}
	
	private String getHString(double h) {
		return String.format("%d", (int) Math.abs(h));
	}
	
	public double[] getDisplayRange() {
		stepSize = 5;
		double meanHeight = getMeanHeight();
		double heightRange = 20;//defaultDepthRange;
		double maxHeight, minHeight;
		if (PamController.getInstance().getGlobalMediumManager().getCurrentMedium() == SoundMedium.Water) {
			maxHeight = meanHeight+heightRange/2;
			maxHeight = PamUtils.roundNumberUp(maxHeight, stepSize);
//			maxHeight = Math.min(maxHeight, stepSize);
			minHeight = maxHeight-heightRange;
		}
		else {
			minHeight = meanHeight-heightRange/2;
			maxHeight = PamUtils.roundNumberDown(minHeight, stepSize);
//			minHeight = Math.max(maxHeight, -stepSize);
			maxHeight = minHeight+heightRange;
		}
		double[] range = {minHeight, maxHeight};
		return range;
	}
	
	/**
	 * Get the mean depth 
	 * @return
	 */
	private double getMeanHeight() {
		double totHeight = 0;
		int n = 0;
		int nStreamers = getNStreamers();
		for (int i = 0; i < nStreamers; i++) {
			Double h = getSensorValue(i, ArraySensorFieldType.HEIGHT);
			if (h != null) {
				totHeight += h;
				n++;
			}
		}
		if (n == 0) {
			return 0;
		}
		return totHeight/n;
	}

}
