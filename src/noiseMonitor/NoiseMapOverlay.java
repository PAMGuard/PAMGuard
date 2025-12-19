package noiseMonitor;

import java.awt.Graphics;
import java.awt.Rectangle;

import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PanelOverlayDraw;
import PamguardMVC.PamDataUnit;
import noiseMonitor.NoiseTabPanel.SpecPlotAxesPanel;

public class NoiseMapOverlay extends PanelOverlayDraw {
	
	NoiseControl noiseControl;
	
	SpecPlotAxesPanel specPlot;

	public NoiseMapOverlay(PamSymbol defaultSymbol,NoiseControl noiseControl) {
		super(defaultSymbol);
		specPlot = noiseControl.getNoiseTabPanel().specAxisPanel;
		
		// TODO Auto-generated constructor stub
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		noiseControl.getNoiseTabPanel();
		return null;
	}

	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector, int keyType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		// TODO Auto-generated method stub
		return null;
	}

}
