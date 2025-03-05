package clickTrainDetector.tethys;

import java.util.ArrayList;

import PamguardMVC.PamDataUnit;
import clickTrainDetector.CTDataUnit;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.ClickTrainDataBlock;
import clickTrainDetector.classification.CTClassification;
import tethys.species.DataBlockSpeciesCodes;
import tethys.species.DataBlockSpeciesManager;

public class ClickTrainSpeciesManager extends DataBlockSpeciesManager {

	private ClickTrainControl clickTrainControl;
	
	private ClickTrainSpeciesCodes ctSpeciesCodes;

	public ClickTrainSpeciesManager(ClickTrainControl clickTrainControl, ClickTrainDataBlock dataBlock) {
		super(dataBlock);
		this.clickTrainControl = clickTrainControl;
		ctSpeciesCodes = new ClickTrainSpeciesCodes(clickTrainControl);
	}

	@Override
	public DataBlockSpeciesCodes getSpeciesCodes() {
		return ctSpeciesCodes;
	}

	@Override
	public String getSpeciesCode(PamDataUnit dataUnit) {
		CTDataUnit clickTrain = (CTDataUnit) dataUnit;
		ArrayList<String> names = ctSpeciesCodes.getSpeciesNames();
		ArrayList<CTClassification> classifications = clickTrain.getCtClassifications();
		for (int i = 0; i < classifications.size(); i++) {
			CTClassification aClass = classifications.get(i);
			int id = aClass.getSpeciesID();
			if (id >= 0 && id < names.size()) {
				return names.get(id);
			}
		}
		return null;
	}

}
