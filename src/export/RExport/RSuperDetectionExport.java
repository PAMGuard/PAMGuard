package export.RExport;

import java.util.ArrayList;
import java.util.List;

import org.renjin.sexp.ListVector;
import org.renjin.sexp.ListVector.NamedBuilder;

import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

public class RSuperDetectionExport extends RDataUnitExport<SuperDetection>{

	private RExportManager rExportManager;

	public RSuperDetectionExport(RExportManager mlDetectionsManager) {
		this.rExportManager=mlDetectionsManager;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public NamedBuilder addDetectionSpecificFields(NamedBuilder rData, SuperDetection dataUnit, int index) {

		//We create a struct within a struct within a struct here...
		SuperDetection superDetection = (SuperDetection) dataUnit;

		ListVector.NamedBuilder dataUnits = new ListVector.NamedBuilder();
		
		//now iterate through the data units and add data units to the struct. 

		//vital to clone this or we mess up all the data units in PAMGuard!
		ArrayList<PamDataUnit> subDataUnits = (ArrayList<PamDataUnit>) superDetection.getSubDetections().clone();

		int n=0; 
		RDataUnitExport exporter; 
		ArrayList<PamDataUnit> savedUnits;
		ListVector.NamedBuilder datasUnitsStruct = null;

		for (int j=0; j<rExportManager.getRDataUnitsExport().size(); j++) {

			exporter = rExportManager.getRDataUnitsExport().get(j);
//					System.out.println("R Try export " + subDataUnits.size());		
			//do we have any data units of that type?
			if  (hasCompatibleUnits(subDataUnits,  exporter)) {

				n=0; //the number of saved units. 
				savedUnits = new ArrayList<PamDataUnit>(); 
				for (int i=0; i<subDataUnits.size(); i++) {
					if (exporter.getUnitClass().isAssignableFrom(subDataUnits.get(i).getClass())) {

						//add to the struct.
						datasUnitsStruct= exporter.detectionToStruct(subDataUnits.get(i), n);
	
						
						System.out.println("Save sub det: " + subDataUnits.get(i).getUID());
						
						//must be named differently
						dataUnits.add((exporter.getName() + "_" + subDataUnits.get(i).getUID()) , datasUnitsStruct); 
						
						savedUnits.add(subDataUnits.get(i));
						
						n++;
					}
				}

				//now we have to remove all the data units that were saved. Otherwise they will be saved again by the more generic 
				//exporters towards the bottom of the list. 
				subDataUnits.removeAll(savedUnits);
//				System.out.println("R Exported " + n+ " of " + superDetection.getSubDetectionsCount() + " data units " + superDetection.getLoadedSubDetectionsCount());		
			}

		}


		rData.add("subdetections", dataUnits);
		rData.add("nsubdet", superDetection.getSubDetectionsCount());

		return rData;
	}


	/**
	 * Check whether there are compatible data units to be exported. 
	 * @param dataUnits - the data unit list
	 * @return true if MATLAB export is possible for the current data units. 
	 */
	@SuppressWarnings("unchecked")
	public static boolean hasCompatibleUnits(List<PamDataUnit> dataUnits, RDataUnitExport mlExporter) {
		//first need to figure out how many data units there are. 
		for (int j=0; j<dataUnits.size(); j++){

			if (mlExporter.getUnitClass().isAssignableFrom(dataUnits.get(j).getClass())){
				return true;
			}

		}
		return false; 
	}

	@Override
	public Class getUnitClass() {
		return SuperDetection.class;
	}

	@Override
	public String getName() {
		return "super_detection";
	}


}
