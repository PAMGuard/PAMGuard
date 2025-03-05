package clickTrainDetector.tethys;

import java.util.ArrayList;

import javax.swing.JCheckBox;

import PamView.dialog.PamDialog;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.classification.CTClassifierManager;
import tethys.species.DataBlockSpeciesCodes;

public class ClickTrainSpeciesCodes extends DataBlockSpeciesCodes {


	private static int itisDefault = 180404; // Odontocete
	private ClickTrainControl clickTrainControl;


	public ClickTrainSpeciesCodes(ClickTrainControl clickTrainControl) {
		super(itisDefault , "Click train", "Click train");
		this.clickTrainControl = clickTrainControl;
	}


	@Override
	public ArrayList<String> getSpeciesNames() {
		CTClassifierManager ctClassifierManager = clickTrainControl.getClassifierManager();
		ArrayList<String> names = new ArrayList();
		for (int i=0; i<ctClassifierManager.getCurrentClassifiers().size(); i++) {
			names.add(ctClassifierManager.getCurrentClassifiers().get(i).getParams().classifierName);
		}	
		
		return names;
	}

}
