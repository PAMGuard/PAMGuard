package rocca.tethys;

import java.util.ArrayList;
import java.util.Arrays;

import rocca.RoccaClassifier;
import rocca.RoccaControl;
import tethys.species.DataBlockSpeciesCodes;

public class RoccaSpeciesCodes extends DataBlockSpeciesCodes {

	private RoccaControl roccaControl;

	public RoccaSpeciesCodes(RoccaControl roccaControl) {
		super("Ambig");
		this.roccaControl = roccaControl;
	}

	@Override
	public ArrayList<String> getSpeciesNames() {
		RoccaClassifier classifier = roccaControl.getRoccaProcess().getRoccaClassifier();
		if (classifier == null) {
			return null;
		}
		String[] spList = classifier.getClassifierSpList();
		ArrayList<String> list = new ArrayList<String>();
		list.add("Ambig");
		list.addAll(Arrays.asList(spList));
		return list;
	}


}
