package listening;

import java.io.Serializable;
import java.util.Vector;

public class ListeningParameters implements Cloneable, Serializable {

	static public final long serialVersionUID = 1;
	
	Vector<SpeciesItem> speciesList;
	
	Vector<String> effortStati;
	
	int nVolumes;
	
	int hydrophones = 3;

	public ListeningParameters() {
		super();
		nVolumes = 5;
		speciesList = new Vector<SpeciesItem>();
		speciesList.add(new SpeciesItem("Sperm Whale"));
		speciesList.add(new SpeciesItem("Dolphin Clicks"));
		speciesList.add(new SpeciesItem("Dolphin Whistles"));
		speciesList.add(new SpeciesItem("Ship Noise"));
		speciesList.add(new SpeciesItem("Airguns"));
		speciesList.add(new SpeciesItem("Other Noise"));
		
		effortStati = new Vector<String>();
		effortStati.add("On Effort");
		effortStati.add("Off Effort");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected ListeningParameters clone() {
		try {
			// do a full clone so that different instances don't end up
			// sharing the vectors of options. 
			ListeningParameters newParams = (ListeningParameters) super.clone();
			newParams.speciesList = (Vector<SpeciesItem>) this.speciesList.clone();
			newParams.effortStati = (Vector<String>) this.effortStati.clone();
			return newParams;
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}



}
