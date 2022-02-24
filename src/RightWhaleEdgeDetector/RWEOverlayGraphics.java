package RightWhaleEdgeDetector;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import javax.activation.DataSource;

import PamUtils.Coordinate3d;
import PamView.GeneralProjector;
import PamView.PamColors;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import fftManager.FFTDataBlock;

public class RWEOverlayGraphics extends PamDetectionOverlayGraphics {
	
	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 10, 10, false, Color.magenta, Color.magenta);
	private RWEProcess rweProcess;

	public RWEOverlayGraphics(RWEProcess rweProcess, PamDataBlock parentDataBlock) {
		super(parentDataBlock, new PamSymbol(defaultSymbol));
		this.rweProcess = rweProcess;
	}

	@Override
	protected Rectangle drawOnSpectrogram(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {
		int type = ((RWEDataUnit) pamDataUnit).rweSound.soundType;
		Color col = PamColors.getInstance().getWhaleColor(type);
		g.setColor(col);
		Rectangle r = super.drawOnSpectrogram(g, pamDataUnit, generalProjector);
		if (r != null) {
			g.drawString(String.format("%d", type), r.x+r.width, r.y);
		}
		/*
		 * And also draw the contour if it exists ...
		 */
		RWEDataUnit rwDataUnit = (RWEDataUnit) pamDataUnit;
		RWESound rwSound = rwDataUnit.rweSound;
		if (rwSound == null || rweProcess == null) {
			return r;
		}
		int[] pf = rwSound.peakFreq;
		int[] lowF = rwSound.lowFreq;
		int[] highF = rwSound.highFreq;
		int[] tbin = rwSound.sliceList;
		if (pf == null || tbin == null) {
			return r;
		}
		FFTDataBlock dataSource = (FFTDataBlock) rweProcess.getParentDataBlock();
		float fs = dataSource.getSampleRate();
		int fftLen = dataSource.getFftLength();
		int ffthop = dataSource.getFftHop();
		
		double t0 = rwDataUnit.getTimeMilliseconds();
		Point2D prevPos = null;
		g.setColor(col);
		int[] polyX = new int[pf.length];
		int[] polyY = new int[pf.length];
		int[] edgeX = new int[pf.length*2];
		int[] edgeY = new int[pf.length*2];
		boolean isWrapped = false;
		for (int i = 0, j = pf.length*2-1; i < pf.length; i++, j--) {
			double t = t0 + (double) tbin[i] * ffthop / fs * 1000.;
			double f = pf[i] * fs/fftLen;
			 Point2D pos = generalProjector.getCoord3d(t, f, 0).getPoint2D();
			 polyX[i] = (int) pos.getX();
			 polyY[i] = (int) pos.getY();
			 if (polyX[i] < polyX[0]) {
				 isWrapped = true;
				 polyX[i] += g.getClipBounds().width;
			 }
			 edgeX[i] = edgeX[j] = polyX[i];
			 Point2D pe = generalProjector.getCoord3d(t, highF[i]*fs/fftLen, 0).getPoint2D();
			 edgeY[i] = (int) pe.getY();
			 pe = generalProjector.getCoord3d(t, lowF[i]*fs/fftLen, 0).getPoint2D();
			 edgeY[j] = (int) pe.getY();
//			 if (prevPos != null) {
//				 if (pos.getX() > prevPos.getX()) {
//					 g.drawLine((int) prevPos.getX(),  (int)prevPos.getY(), (int) pos.getX(), (int) pos.getY());
//				 }
//				 
//			 }
////			 prevPos = pos;
//			 g.draw
		}		
//		g.drawPolyline(polyX, polyY, polyX.length);
		g.drawPolygon(edgeX, edgeY, edgeX.length);
		if (isWrapped) {
			int dx = g.getClipBounds().width;
			for (int i = 0; i < edgeX.length; i++) {
				edgeX[i] -= dx;
			}
			g.drawPolygon(edgeX, edgeY, edgeX.length);
		}
		
		return r;
	}

	/* (non-Javadoc)
	 * @see PamView.PamDetectionOverlayGraphics#getHoverText(PamView.GeneralProjector, PamguardMVC.PamDataUnit, int)
	 */
	@Override
	public String getHoverText(GeneralProjector generalProjector,
			PamDataUnit dataUnit, int iSide) {
		RWEDataUnit rweDataUnit = (RWEDataUnit) dataUnit;
		RWESound aSound = rweDataUnit.rweSound;
		
		String txt = super.getHoverText(generalProjector, dataUnit, iSide);
		// remove the </html> from the end
		int ht = txt.indexOf("</html>");
		if (ht > 0) {
			txt = txt.substring(0, ht);
		}
		txt += String.format("<br>Sound Type %d - %s", aSound.soundType, aSound.getTypeString());
		txt += "</html>";
		
		return txt;
	}

}
