package RightWhaleEdgeDetector.graphics;

import PamUtils.Coordinate3d;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.PamDataUnit;
import RightWhaleEdgeDetector.RWEDataBlock;
import RightWhaleEdgeDetector.RWEDataUnit;
import RightWhaleEdgeDetector.RWEProcess;
import RightWhaleEdgeDetector.RWESound;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.projector.TDProjectorFX;
import fftManager.FFTDataBlock;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Polygon;

public class RWEDataPlotinfoFX extends TDDataInfoFX {

	private RWEDataBlock rweDataBlock;
	private TDScaleInfo bearingScaleInfo;
	private TDScaleInfo frequencyInfo;
	private RWEProcess rweProcess;

	public RWEDataPlotinfoFX(RWEDataPlotProviderFX tdDataProvider, RWEProcess rweProcess, TDGraphFX tdGraph, RWEDataBlock rweDataBlock) {
		super(tdDataProvider, tdGraph, rweDataBlock);
		this.rweProcess = rweProcess;
		this.rweDataBlock = rweDataBlock;

		bearingScaleInfo = new TDScaleInfo(0,180, ParameterType.BEARING, ParameterUnits.DEGREES);
		bearingScaleInfo.setReverseAxis(true); //set the axis to be reverse so 0 is at top of graph
		frequencyInfo = new TDScaleInfo(0, 1, ParameterType.FREQUENCY, ParameterUnits.HZ);
		this.getScaleInfos().add(bearingScaleInfo);
		this.getScaleInfos().add(frequencyInfo);

		//set correct frequency range based on nyquist. 
		frequencyInfo.setMaxVal(rweDataBlock.getSampleRate()/2.);
		
	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		if (pamDataUnit.getLocalisation() == null) {
			return null;
		}
		double[] angles = pamDataUnit.getLocalisation().getAngles();
		if (angles != null && angles.length > 0) {
			return Math.toDegrees(angles[0]);
		}
		return null;
	}

	@Override
	public TDScaleInfo getScaleInfo() {

		setNPlotPanes(frequencyInfo, this.getDataBlock(), false); 

		double min = Math.min(bearingScaleInfo.getMinVal(), bearingScaleInfo.getMaxVal());
		double max = Math.max(bearingScaleInfo.getMinVal(), bearingScaleInfo.getMaxVal());

		bearingScaleInfo.setMaxVal(max);
		bearingScaleInfo.setMinVal(min);

		return super.getScaleInfo();
	}
	
	@Override
	public TDSymbolChooserFX getSymbolChooser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Polygon drawDataUnit(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g, double scrollStart, TDProjectorFX tdProjector,int type) {
		// if drawing FFT then need to use slight more complex drawing functions.
		if (getScaleInfoIndex()==1) {
			return drawRWContour(plotNumber, pamDataUnit, g, scrollStart, tdProjector, type);
		}
		else {
			return super.drawDataUnit(plotNumber, pamDataUnit, g, scrollStart, tdProjector ,type);
		}
//		return null;
	}

	private Polygon drawRWContour(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g, double scrollStart,
			TDProjectorFX tdProjector, int type) {
		if (!shouldDraw(plotNumber, pamDataUnit)){
			//System.out.println("Cannot plot whistle");
//			iCol++;
			return null; 
		}
		RWEDataUnit rweDataUnit = (RWEDataUnit) pamDataUnit;
		RWESound rweSound = rweDataUnit.rweSound;
		int[] hf = rweSound.highFreq;
		int[] lf = rweSound.lowFreq;
		int[] pf = rweSound.peakFreq;

		FFTDataBlock dataSource = (FFTDataBlock) rweProcess.getParentDataBlock();
		if (dataSource == null) {
			return null;
		}
		double fs = dataSource.getSampleRate();
		int fftLen = dataSource.getFftLength();
		int fftHop = dataSource.getFftHop();
		
//		Polygon outer = new Poly
		int nOut = hf.length*2;
		double[] outsideX = new double[nOut];
		double[] outsideY = new double[nOut];
		for (int i = 0; i < hf.length; i++) {
			long t = (long) (i*1000*fftHop/fs)+rweDataUnit.getTimeMilliseconds();
			double f  = lf[i]*fs/fftLen;
			double tdPix = tdProjector.getTimePix(t-scrollStart);
			double fPix = tdProjector.getYPix(f);
//			Coordinate3d coord = tdProjector.getCoord3d(i, t, f);
			outsideX[i] =  tdPix;
			outsideY[i] =  fPix;

			f  = hf[i]*fs/fftLen;
//			coord = tdProjector.getCoord3d(i, t, f);
			fPix = tdProjector.getYPix(f);
			outsideX[nOut-1-i] =  tdPix;
			outsideY[nOut-1-i] =  fPix;
			
		}
		g.strokePolygon(outsideX, outsideY, nOut);
		
		
		return null;
	}
}
