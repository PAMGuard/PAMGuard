package landMarks;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Window;

import PamController.masterReference.MasterReferencePoint;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PanelOverlayDraw;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;

public class LandmarkGraphics extends PanelOverlayDraw {

	private LandmarkControl landmarkControl;
	
	public static final SymbolData defSymbol = new SymbolData();

	
//	public static final SymbolData defaultSymbol = new SymbolData();
	
	public LandmarkGraphics(LandmarkControl landmarkControl) {
		super(new PamSymbol(defSymbol));
		this.landmarkControl = landmarkControl;
	}

	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		return (parameterTypes[0] == GeneralProjector.ParameterType.LATITUDE && 
				parameterTypes[1] == GeneralProjector.ParameterType.LONGITUDE);
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector,
			int keyType) {
		return null;
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {
		if (generalProjector.getParmeterType(0) == GeneralProjector.ParameterType.LATITUDE && 
				generalProjector.getParmeterType(1) == GeneralProjector.ParameterType.LONGITUDE) {
			return drawOnMap(g, pamDataUnit, generalProjector);
		}
		return null;
	}
	
	public Rectangle drawOnMap(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {
		LandmarkDataUnit landmarkDataUnit = (LandmarkDataUnit) pamDataUnit;
		LandmarkData ld = landmarkDataUnit.getLandmarkData();
		
		PamSymbol symbol = landmarkDataUnit.getLandmarkData().symbol;
		if (symbol == null) {
			symbol = landmarkControl.getDefaultSymbol();
		}
		
		Coordinate3d c3d = generalProjector.getCoord3d(ld.latLong.getLatitude(), ld.latLong.getLongitude(), ld.height);
		symbol.draw(g, c3d.getXYPoint());
		generalProjector.addHoverData(c3d, pamDataUnit);
		
		return null;
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector,
			PamDataUnit dataUnit, int iSide) {
		LandmarkDataUnit landmarkDataUnit = (LandmarkDataUnit) dataUnit;
		LandmarkData ld = landmarkDataUnit.getLandmarkData();
		String str = String.format("<html><p width=\"200px\">%s<br>%s %s<br>Height: %.1fm", 
				ld.name, LatLong.formatLatitude(ld.latLong.getLatitude()), LatLong.formatLongitude(ld.latLong.getLongitude()),
				ld.height);
		LatLong refLatLong = MasterReferencePoint.getLatLong();
		String refName = MasterReferencePoint.getName();
		if (refName != null) {
			str += String.format("<br>Range and bearing from %s<br>%.1fm;   %.1f\u00B0"
					, refName, refLatLong.distanceToMetres(ld.latLong), refLatLong.bearingTo(ld.latLong));
		}
		str += "</html>";
				
		return str;
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
