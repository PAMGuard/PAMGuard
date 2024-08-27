package PamView;

import java.awt.Component;

import PamView.dialog.IconPanel;

public class PanelOverlayKeyItem implements PamKeyItem {

	PamDetectionOverlayGraphics pamDetectionOverlayGraphics;
	
	public PanelOverlayKeyItem(PamDetectionOverlayGraphics pamDetectionOverlayGraphics) {
		super();
		this.pamDetectionOverlayGraphics = pamDetectionOverlayGraphics;
	}

	@Override
	public Component getIcon(int keyType, int nComponent) {
		PamSymbol pamSymbol = pamDetectionOverlayGraphics.getDefaultSymbol();
		if (pamSymbol == null) {
			return null;
		}
		return new IconPanel(pamSymbol);
	}

	@Override
	public int getNumItems(int keyType) {
		return 1;
	}

	@Override
	public String getText(int keyType, int nComponent) {

		return pamDetectionOverlayGraphics.getParentDataBlock().getDataName();
		
	}

}
