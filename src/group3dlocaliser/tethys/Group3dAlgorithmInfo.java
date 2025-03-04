package group3dlocaliser.tethys;

import java.awt.Window;
import java.io.Serializable;

import Localiser.LocalisationAlgorithmInfo;
import group3dlocaliser.Group3DLocaliserControl;
import group3dlocaliser.Group3DParams;
import tethys.localization.CoordinateName;
import tethys.localization.LocalizationBuilder;
import tethys.swing.export.CoordinateChoice;
import tethys.swing.export.CoordinateChoicePanel;
import tethys.swing.export.LocalizationOptionsPanel;

public class Group3dAlgorithmInfo implements LocalisationAlgorithmInfo, CoordinateChoice {

	private Group3DLocaliserControl group3DControl;

	private CoordinateName[] possibleCoordinates = {CoordinateName.WGS84, CoordinateName.Cartesian}; 
	
	public Group3dAlgorithmInfo(Group3DLocaliserControl group3DControl) {
		this.group3DControl = group3DControl;
	}

	@Override
	public int getLocalisationContents() {
		return group3DControl.getGroup3dProcess().getGroup3dDataBlock().getLocalisationContents().getLocContent();
	}

	@Override
	public String getAlgorithmName() {
		return group3DControl.getUnitType();
	}

	@Override
	public Serializable getParameters() {
		return group3DControl.getSettingsReference();
	}

	@Override
	public LocalizationOptionsPanel getLocalizationOptionsPanel(Window parent, LocalizationBuilder locBuilder) {
		return new CoordinateChoicePanel(this);
	}

	@Override
	public CoordinateName[] getPossibleCoordinates() {
		return possibleCoordinates;
	}

	@Override
	public CoordinateName getCoordinateName() {
		Group3DParams params = group3DControl.getGrid3dParams();
		return params.getExportCoordinateName();
	}

	@Override
	public void setCoordinateName(CoordinateName coordinateName) {
		Group3DParams params = group3DControl.getGrid3dParams();
		params.setExportCoordinateName(coordinateName);
	}

}
