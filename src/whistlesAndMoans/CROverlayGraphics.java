package whistlesAndMoans;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ListIterator;

import PamController.PamController;
import PamUtils.Coordinate3d;
import PamUtils.PamUtils;
import PamView.GeneralProjector;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.symbol.SymbolData;
import PamguardMVC.DataBlock2D;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class CROverlayGraphics extends PamDetectionOverlayGraphics {

	private WhistleMoanControl whistleControl;

	private ConnectedRegionDataBlock dataBlock;

	public static final Color[] whistleColours = { Color.RED, Color.GREEN, Color.CYAN,
			Color.ORANGE, Color.PINK, Color.MAGENTA };

	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_DIAMOND, 10, 10, true,
			Color.BLUE, Color.BLUE);
//	private int iCol = 0;

	private boolean isViewer;

private DataBlock2D dataBlock2D;

//	public void resetColour() {
//		iCol = 0;
//	}

	public CROverlayGraphics(ConnectedRegionDataBlock dataBlock,
			WhistleMoanControl whistleControl) {
		super(dataBlock, new PamSymbol(defaultSymbol));
		this.dataBlock = dataBlock;
		this.whistleControl = whistleControl;
		isViewer = (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW);
	}

	@Override
	protected Rectangle drawOnSpectrogram(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {
		ConnectedRegionDataUnit crDataUnit = (ConnectedRegionDataUnit) pamDataUnit;
//		setLineColor(Color.RED);
		// use the super class to draw a rectangle. 
		Rectangle r = null;// super.drawOnSpectrogram(g, pamDataUnit, generalProjector);
		drawShape(g, crDataUnit, generalProjector);
		return r;
	}
	
	private double bin2Frequency(double fftBin, int seqChannel) {
		if (dataBlock2D != null) {
			return dataBlock2D.bin2Value(fftBin, seqChannel);
		}
		else {
			float sampleRate = dataBlock.getSampleRate();
			int fftLength = dataBlock.getFftLength();
			return fftBin * sampleRate / fftLength;
		}
	}

	//	int yOff = 0;
	private Rectangle drawShape(Graphics g, ConnectedRegionDataUnit dataUnit,
			GeneralProjector generalProjector) {
		ConnectedRegion cr = dataUnit.getConnectedRegion();
		WhistleToneParameters wmParams = whistleControl.whistleToneParameters;
		//		int lastYStep = 0;
		int maxPeaks = cr.getMaxPeaks();
		int[] lastPeak = null, thisPeak;
		int lastPeakNum;
		int slicePeaks;
		int minX = Integer.MAX_VALUE;
		int minY = minX;
		int maxX = Integer.MIN_VALUE;
		int maxY = maxX;

		int channelNo = PamUtils.getLowestChannel(dataUnit.getChannelBitmap());
		SliceData sliceData, prevSlice = null;
		if (wmParams.shortShowPolicy == WhistleToneParameters.SHORT_HIDEALL && 
				cr.getNumSlices() < wmParams.shortLength) {
			return null;
		}
		ListIterator<SliceData> sliceIterator = cr.getSliceData().listIterator();
		int fftLength = dataBlock.getFftLength();
		int fftHop = dataBlock.getFftHop();
		float sampleRate = dataBlock.getSampleRate();
		boolean isFirst;
		Point pt1 = null, pt2 = null;
		double f1, f2;
		Coordinate3d c3d;

		boolean fullOutline = wmParams.showContourOutline;

		if (wmParams.shortShowPolicy == WhistleToneParameters.SHORT_SHOWGREY && 
				cr.getNumSlices() < wmParams.shortLength) {
			g.setColor(Color.GRAY);
		}
		else {
//			int iCol = dataUnit.getAbsBlockIndex()%whistleColours.length;
//			g.setColor(whistleColours[iCol]);
		}

		((Graphics2D) g).setStroke(new BasicStroke(2));

		/*
		 * find input datablock so that we can do log scale calculations if necessary. 
		 */
		PamDataBlock fftblock = whistleControl.getSpectrogramNoiseProcess().getParentDataBlock();
		if (fftblock instanceof DataBlock2D) {
			dataBlock2D = (DataBlock2D) fftblock;
		}
		else {
			dataBlock2D = null;
		}

		long startMillis = dataUnit.getTimeMilliseconds();
		double binStepMillis = fftHop / sampleRate * 1000;
		isFirst = true;
		double sliceMillis, prevSliceMillis = 0;
		SliceData firstSlice = null;
		boolean timeWrap = false;
		int sliceX, prevSliceX = -100;
		while (sliceIterator.hasNext()) {
			sliceData = sliceIterator.next();
			if (firstSlice == null) {
				firstSlice = sliceData;
			}
			sliceMillis = (sliceData.sliceNumber-firstSlice.sliceNumber) * binStepMillis;
			sliceMillis += startMillis;
			slicePeaks = sliceData.nPeaks;
			c3d = generalProjector.getCoord3d(sliceMillis, 0, 0);
			sliceX = (int) c3d.x;
//			if (sliceX > 1000) {
//				System.out.println("sliceMillis = " + String.valueOf(sliceMillis) + " : Diff = " + String.valueOf(sliceMillis-prevSliceMillis) + " : SliceX = " + String.valueOf(sliceX));
//			}
			if (whistleControl.whistleToneParameters.stretchContours && sliceX <= prevSliceX) {
				sliceX = prevSliceX + 1;
			}
			//			if (slicePeaks > 1) {
			//				System.out.println(slicePeaks + " peaks");
			//			}
			for (int iP = 0; iP < slicePeaks; iP++) {
				thisPeak = sliceData.peakInfo[iP];
				lastPeakNum = thisPeak[3];
				if (lastPeakNum < 0 || prevSlice == null) {
					continue;
				}
				if (lastPeakNum >= prevSlice.peakInfo.length) {
					/*
					 * I don't think this is meant to happen and that this line was
					 * put here to catch a bug. It seems to happen only with branched
					 * regions, which is no longer the default, so noone ever notices
					 * it.
					 */
					lastPeak = prevSlice.peakInfo[prevSlice.peakInfo.length-1];		
				}
				else {
					lastPeak = prevSlice.peakInfo[lastPeakNum];
				}
//				f1 = thisPeak[1] * sampleRate / fftLength;
				f1 = bin2Frequency(thisPeak[1], channelNo);
				//					f2 = lastPeak[1] * sampleRate / fftLength;

				c3d = generalProjector.getCoord3d(sliceMillis, f1, 0);
				pt2 = c3d.getXYPoint();

				double ff = bin2Frequency(lastPeak[1], channelNo);
				c3d = generalProjector.getCoord3d(prevSliceMillis, ff, 0);
				pt1 = c3d.getXYPoint();

				//				if (s1 == prevSlice.getStartSample()) {
				//					System.out.println("Repeat time");
				//				}
				if (pt2.x < pt1.x){
					timeWrap = true;
					break;
				}

				if (pt1 != null) {
					//					g.drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
					g.drawLine(prevSliceX, pt1.y, sliceX, pt2.y);
				}
				if (fullOutline) {
					ff = bin2Frequency(thisPeak[0], channelNo);
					c3d = generalProjector.getCoord3d(sliceMillis, ff, 0);
					pt2 = c3d.getXYPoint();

					ff = bin2Frequency(lastPeak[0], channelNo);
					c3d = generalProjector.getCoord3d(prevSliceMillis, ff, 0);
					pt1 = c3d.getXYPoint();
					g.drawLine(prevSliceX, pt1.y, sliceX, pt2.y);
					minX = Math.min(minX, pt1.x);
					minY = Math.min(minY, pt1.y);
					maxX = Math.max(maxX, pt1.x);
					maxY = Math.max(maxY, pt1.y);
					minX = Math.min(minX, pt2.x);
					minY = Math.min(minY, pt2.y);
					maxX = Math.max(maxX, pt2.x);
					maxY = Math.max(maxY, pt2.y);

					ff = bin2Frequency(thisPeak[2], channelNo);
					c3d = generalProjector.getCoord3d(sliceMillis, ff, 0);
					pt2 = c3d.getXYPoint();

					ff = bin2Frequency(lastPeak[2], channelNo);
					c3d = generalProjector.getCoord3d(prevSliceMillis, ff, 0);
					pt1 = c3d.getXYPoint();
					g.drawLine(prevSliceX, pt1.y, sliceX, pt2.y);

				}
				minX = Math.min(minX, pt1.x);
				minY = Math.min(minY, pt1.y);
				maxX = Math.max(maxX, pt1.x);
				maxY = Math.max(maxY, pt1.y);
				minX = Math.min(minX, pt2.x);
				minY = Math.min(minY, pt2.y);
				maxX = Math.max(maxX, pt2.x);
				maxY = Math.max(maxY, pt2.y);
			}
			if (timeWrap && isViewer) {
				return null;
			}
			prevSlice = sliceData;
			prevSliceMillis = sliceMillis;
			prevSliceX = sliceX;
		}
//		if (generalProjector.isViewer()) {
//		System.out.println("Swing Display drawing data unit UID = " + String.valueOf(dataUnit.getUID()));
			Coordinate3d middle = new Coordinate3d((minX + maxX)/2, (minY + maxY)/2);
			generalProjector.addHoverData(middle, dataUnit);
//		}
		return null;
	}

//	@Override
//	public double getDefaultRange(GeneralProjector projector) {
//		return whistleControl.whistleToneParameters.getMapLineLength();
//	}

	/* (non-Javadoc)
	 * @see PamView.PamDetectionOverlayGraphics#drawOnMap(java.awt.Graphics, PamguardMVC.PamDataUnit, PamView.GeneralProjector)
	 */
	@Override
	protected Rectangle drawOnMap(Graphics g, PamDataUnit pamDetection,
			GeneralProjector generalProjector) {
		return super.drawOnMap(g, pamDetection, generalProjector);
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector,
			PamDataUnit dataUnit, int iSide) {
		String str = dataUnit.getSummaryString();
		return str;
	}


}
