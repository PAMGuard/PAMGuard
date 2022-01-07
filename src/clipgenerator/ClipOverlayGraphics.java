package clipgenerator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.MappedByteBuffer;

import javax.imageio.ImageIO;

import pamguard.Pamguard;
import fftManager.FFTDataBlock;
import PamController.PamController;
import PamView.ColourArray;
import PamView.GeneralProjector;
import PamView.PamColors;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PanelOverlayDraw;
import PamView.GeneralProjector.ParameterType;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;
import Spectrogram.DirectDrawProjector;
import Spectrogram.SpectrogramDisplay;
import Spectrogram.SpectrogramParameters;

public class ClipOverlayGraphics extends PamDetectionOverlayGraphics {

	private ClipControl clipControl;

	private boolean isViewer;

	private boolean isNetReceiver;
	
	public static final SymbolData defSymbol = new SymbolData(PamSymbolType.SYMBOL_DIAMOND, 10, 10, false, Color.RED, Color.CYAN);

	/**
	 * @param clipControl
	 */
	public ClipOverlayGraphics(ClipControl clipControl, ClipDataBlock clipDataBlock) {
		super(clipDataBlock, new PamSymbol(defSymbol));
		this.clipControl = clipControl;
		isViewer = (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW);
		isNetReceiver = (PamController.getInstance().getRunMode() == PamController.RUN_NETWORKRECEIVER);
	}

	@Override
	public boolean canDraw(GeneralProjector generalProjector) {
		if (generalProjector.getParmeterType(0) == ParameterType.TIME
				&& generalProjector.getParmeterType(1) == ParameterType.FREQUENCY) {
			return true;
		}
		return super.canDraw(generalProjector);
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector,
			int keyType) {
		return null;
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {		
		if (generalProjector.getParmeterType(0) == ParameterType.TIME
				&& generalProjector.getParmeterType(1) == ParameterType.FREQUENCY) {
			if (isViewer || isNetReceiver) {
				return drawViewerMode(g, pamDataUnit, generalProjector);
			}
			else {
				return drawNormalMode(g, pamDataUnit, generalProjector);
			}
		}
		ClipDataUnit clipDataUnit = (ClipDataUnit) pamDataUnit;
		ClipGenSetting genSet = clipControl.findClipGenSetting(clipDataUnit.triggerName);
		if (genSet != null && genSet.mapLineLength != null) {
			setDefaultRange(genSet.mapLineLength);
		}
		return super.drawDataUnit(g, pamDataUnit, generalProjector);
	}

	private Rectangle drawNormalMode(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {
		// draw a simple rectangle. 
		PamSymbol s = getPamSymbol(pamDataUnit, generalProjector);
		Graphics2D g2d = (Graphics2D) g;
		Stroke oldStroke = g2d.getStroke();
		if (s != null) {
			g.setColor(s.getLineColor());
			g2d.setStroke(new BasicStroke(s.getLineThickness()));
		}
		Rectangle r = super.drawOnSpectrogram(g, pamDataUnit, generalProjector);
		if (s != null) {
			g2d.setStroke(oldStroke);
		}
		return r;
	} 
	
	private Rectangle drawViewerMode(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {
		/*
		 * Will really need to get some spectrogram and FFT parameters out
		 * of the projector if this drawing is going to work !
		 */
		DirectDrawProjector spectrogramProjector;
		SpectrogramDisplay spectrogramDisplay;
		FFTDataBlock fftDataBlock;
		try {
			spectrogramProjector = (DirectDrawProjector) generalProjector;
		}
		catch (ClassCastException e) {
			return null;
		}
		spectrogramDisplay = spectrogramProjector.getSpectrogramDisplay();
		spectrogramDisplay.getSpectrogramParameters();
		Color[] colorLUT = spectrogramDisplay.getColourArray().getColours();
		fftDataBlock = spectrogramDisplay.getSourceFFTDataBlock();
		SpectrogramParameters specParams = spectrogramDisplay.getSpectrogramParameters();
		int panelId = spectrogramProjector.getPanelId();
		int channel = specParams.channelList[panelId];
		if (fftDataBlock == null) {
			return null;
		}
		float sampleRate = clipControl.clipProcess.getSampleRate();
		ClipDataUnit clipDataUnit = (ClipDataUnit) pamDataUnit;
		BufferedImage image = clipDataUnit.getClipImage(channel, fftDataBlock.getFftLength(), fftDataBlock.getFftHop(), 
				specParams.amplitudeLimits[0], specParams.amplitudeLimits[1], colorLUT);
		if (image == null) {
			return null;
		}
		/*
		 * find the top left and bottom right corners of the image. 
		 */
		double lenMillis = (double) image.getWidth() * (double) fftDataBlock.getFftHop() * 1000. / sampleRate;
		long tStart= clipDataUnit.getTimeMilliseconds();
		Point topLeft = generalProjector.getCoord3d(tStart, sampleRate/2, 0).getXYPoint();
		Point botRight = generalProjector.getCoord3d(tStart+lenMillis, 0, 0).getXYPoint();
		if (botRight.x < topLeft.x){
			// display has wrapped around. Do something !!!!
			botRight.x += spectrogramProjector.getDisplayWidth();
		}
		
		
		g.drawImage(image, topLeft.x, topLeft.y, botRight.x-topLeft.x, botRight.y-topLeft.y, null);
		
		return new Rectangle(topLeft.x, topLeft.y, botRight.x-topLeft.x, botRight.y-topLeft.y);
	}

	private ColourArray imageArray = ColourArray.createStandardColourArray(256, ColourArray.ColourArrayType.GREY);
	@Override
	public BufferedImage getHoverImage(GeneralProjector generalProjector,
			PamDataUnit dataUnit, int iSide) {
		
		ClipDataUnit clipDataUnit = (ClipDataUnit) dataUnit;
		return clipDataUnit.getClipImage(0, 512, 128, 30., 120., imageArray.getColours());
		
	}

	@Override
	public boolean hasOptionsDialog(GeneralProjector generalProjector) {
		return false;
	}

	@Override
	public boolean showOptions(Window parentWindow,
			GeneralProjector generalProjector) {
		return false;
	}

}
