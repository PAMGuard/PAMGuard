package UserInput;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import PamUtils.Coordinate3d;
import PamView.BasicKeyItem;
import PamView.GeneralProjector;
import PamView.HoverData;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;


public class UserInputOverlayGraphics extends PamDetectionOverlayGraphics {
	int drawTypes;
	private String name;
	private static int specSymbolSize = 8;
	static PamSymbol specSymbol = new PamSymbol(PamSymbolType.SYMBOL_TRIANGLEU, specSymbolSize, specSymbolSize, true, Color.RED, Color.RED);


	public UserInputOverlayGraphics(PamDataBlock parentDataBlock) {
		super(parentDataBlock, specSymbol);
		this.drawTypes = drawTypes;
		this.name = name;
	}
	
	@Override
	protected boolean canDrawOnSpectrogram() {
		return true;
	}
	
	/**
	 * Draw little triangles at top and bottom of spectrogram - better than 
	 * boxes for things that only have a start timestamps. 
	 */
	@Override
	protected Rectangle drawOnSpectrogram(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {
		// TODO Auto-generated method stub
		//		return super.drawOnSpectrogram(g, pamDataUnit, generalProjector);
		UserInputDataUnit ui = (UserInputDataUnit) pamDataUnit;
		Coordinate3d c3d = generalProjector.getCoord3d(ui.getTimeMilliseconds(), 0, 0);
		Point pt = c3d.getXYPoint();
		PamSymbol specSymbol = getPamSymbol(pamDataUnit, generalProjector);
		pt.y -= specSymbolSize /2; //Top of panel
		specSymbol.setSymbol(PamSymbolType.SYMBOL_TRIANGLEU); specSymbol.setLineColor(Color.RED);
		specSymbol.draw(g, pt);
		generalProjector.addHoverData(c3d, ui);
		specSymbol.setSymbol(PamSymbolType.SYMBOL_TRIANGLED);
		pt.y = specSymbolSize/2; // top of panel
		specSymbol.draw(g, pt);
		c3d.y = specSymbolSize/2;
		generalProjector.addHoverData(c3d, ui);
		return null;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}
