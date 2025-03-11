package whistleClassifier.tethys;

import java.util.ArrayList;
import java.util.Arrays;

import tethys.species.DataBlockSpeciesCodes;
import whistleClassifier.FragmentClassifierParams;
import whistleClassifier.WhistleClassifierControl;

public class WhistleClassifierSpeciesCodes extends DataBlockSpeciesCodes {

	private WhistleClassifierControl whistleClassifierControl;

	public WhistleClassifierSpeciesCodes(WhistleClassifierControl whistleClassifierControl) {
		super("Unknown");
		this.whistleClassifierControl = whistleClassifierControl;
	}

	@Override
	public ArrayList<String> getSpeciesNames() {
		FragmentClassifierParams params = whistleClassifierControl.getWhistleClassificationParameters().fragmentClassifierParams;
		if (params == null) {
			return null;
		}
		String[] species = params.getSpeciesList();
		if (species == null) {
			return null;
		}
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(species));
		return list;		
	}


}
