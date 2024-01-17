package tethys;

/**
 * Names of Tethys Collections. These are the plural names, though contain functionality
 * to get the document names, which are generally the singular of the enum
 * @author dg50
 *
 */
public enum Collection {

	Deployments, Detections, Calibrations, Localizations, SpeciesAbbreviations, Ensembles, SourceMaps, ITIS, ITIS_ranks, OTHER;
	
	/**
	 * A list of the main collections in the database, i.e. ones the user will 
	 * possibly want to interract with through the GUI. 
	 * @return list of main collections. 
	 */
	public static Collection[] mainList() {
		Collection[] cs = {Deployments, Detections, Calibrations, Localizations, SpeciesAbbreviations, Ensembles};
		return cs;
	}
	/**
	 * Get the name of a document in this collection, this is generally the singular 
	 * of the collection name. 
	 * @return Document name, e.g. Detection for Detections
	 */
	public String documentName() {
		switch (this) {
		case Calibrations:
			return "Calibration";
		case Deployments:
			return "Deployment";
		case Detections:
			return "Detections"; // this one is plural !
		case Localizations:
			return "Localize";
		case SpeciesAbbreviations:
			return "SpeciesAbbreviation";
		case Ensembles:
			return "Ensemble";
		default:
			break;
		}
		return null;
	}
	
	public String collectionName() {
		return this.toString();
	}
	
	/**
	 * Find a collection for the given name. This does
	 * a bit more than the simple 'valueof' since it also 
	 * allows the user to input a documentname in place, which 
	 * is just the collection name without the plural 's' on the end
	 * @param name Collection name. 
	 * @return Collection or null. 
	 */
	public static Collection fromName(String name) {
		Collection c = Collection.valueOf(name);
		if (c != null) {
			return c;
		}
		/**
		 * Otherwise, may need to do a longer search to see if the user has passed 
		 * the singular document name. 
		 */
		if (name.endsWith("s") == false) {
			c = Collection.valueOf(name+"s");
			if (c != null) {
				return c;
			}
		}
		return null;		
	}
	/**
	 * get Tethys collection name from nilus collection objects
	 * @param className nilus object Class Name
	 * @return name of Tethys collection
	 */
	public static Collection fromClass(Class nilusClass) {
		String className = nilusClass.getName();
		switch(className) {
		case "nilus.Deployment":
			return Deployments;
		case "nilus.Detections":
			return Detections;
		case "nilus.Calibration":
			return Calibrations;
		case "nilus.Ensemble":
			return Ensembles;
		case "nilus.Localization":
			return Localizations;
		case "nilus.SpeciesAbbreviation":
			return SpeciesAbbreviations;
		case "nilus.SourceMap":
			return SourceMaps;
		case "nilus.ITIS":
			return ITIS;
		case "nilus.ranks":
			return ITIS_ranks;
		default:
			return null;
		}
	}
//	/**
//	 * get Tethys collection name from nilus collection objects
//	 * @param className nilus object Class Name
//	 * @return name of Tethys collection
//	 */
//	public static String getCollection(Class nilusClass) {
//		String className = nilusClass.getName();
//		switch(className) {
//		case "nilus.Deployment":
//			return "Deployments";
//		case "nilus.Detections":
//			return "Detections";
//		case "nilus.Calibration":
//			return "Calibrations";
//		case "nilus.Ensemble":
//			return "Ensembles";
//		case "nilus.Localization":
//			return "Localizations";
//		case "nilus.SpeciesAbbreviation":
//			return "SpeciesAbbreviations";
//		case "nilus.SourceMap":
//			return "SourceMaps";
//		case "nilus.ITIS":
//			return "ITIS";
//		case "nilus.ranks":
//			return "ITIS_ranks";
//		default:
//			return "";
//		}
//	}
} 
