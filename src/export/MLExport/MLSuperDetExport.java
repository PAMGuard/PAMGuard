package export.MLExport;

import java.util.ArrayList;
import java.util.List;

import org.renjin.sexp.ListVector.NamedBuilder;

import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.Struct;


/**
 * Exports super detections. 
 */
public class MLSuperDetExport extends MLDataUnitExport {

	private MLDetectionsManager mlDetectionsManager;

	public MLSuperDetExport(MLDetectionsManager mlDetectionsManager) {
		this.mlDetectionsManager=mlDetectionsManager;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Struct addDetectionSpecificFields(Struct mlStruct, int index, PamDataUnit dataUnit) {

		//We create a struct within a struct within a struct here...
		SuperDetection superDetection = (SuperDetection) dataUnit;
		
		Struct dataUnits = Mat5.newStruct(); 
		//now iterate through the data units and add data units to the struct. 

		//		We have different data unit types here...within MATLAB, the best way to do this is have a struct called data units which 
		//		Itself has sub structures for each detection. That way we can have data units structures with different fields for detections
		//		 which have different types of data units. For example one super detection may only have clicks and the other clicks and whistles etc. 
		Struct dataUnitStruct = Mat5.newStruct(); 
		
		
		//vital to clone this or we mess up all the data units in PAMGuard!
		ArrayList<PamDataUnit> subDataUnits = (ArrayList<PamDataUnit>) superDetection.getSubDetections().clone();
		
		int n=0; 
		MLDataUnitExport exporter; 
		ArrayList<PamDataUnit> savedUnits;
		Struct dataUnitstruct;
		for (int j=0; j<mlDetectionsManager.getMlDataUnitsExport().size(); j++) {
						
			exporter = mlDetectionsManager.getMlDataUnitsExport().get(j);
//			System.out.println("Try export " + subDataUnits.size());		

			//do we have any data units of that type?
			if  (hasCompatibleUnits(subDataUnits,  exporter)) {

				dataUnitStruct= Mat5.newStruct(1, subDataUnits.size()); 

				n=0; //the number of saved units. 
				savedUnits = new ArrayList<PamDataUnit>(); 
				for (int i=0; i<subDataUnits.size(); i++) {
					if (exporter.getUnitClass().isAssignableFrom(subDataUnits.get(i).getClass())) {
						
						//add to the struct.
						dataUnitstruct= exporter.detectionToStruct(dataUnitStruct, subDataUnits.get(i), n);
						savedUnits.add(subDataUnits.get(i));
						n++;
					}
				}
				
				//now we have to remove all the data units that were saved. Otherwise they will be saved again by the more generic 
				//exporters towards the bottom of the list. 
				subDataUnits.removeAll(savedUnits);
				
				dataUnits.set(exporter.getName(), dataUnitStruct); 
				
//				System.out.println("Exported " + n+ " of " + superDetection.getSubDetectionsCount() + " data units " + superDetection.getLoadedSubDetectionsCount());		
				}

			}
			

		mlStruct.set("subdetections", index, dataUnits);
		mlStruct.set("nsubdet", index, Mat5.newScalar(superDetection.getSubDetectionsCount()));

		return mlStruct;
	}


	/**
	 * Check whether there are compatible data units to be exported. 
	 * @param dataUnits - the data unit list
	 * @return true if MATLAB export is possible for the current data units. 
	 */
	@SuppressWarnings("unchecked")
	public static boolean hasCompatibleUnits(List<PamDataUnit> dataUnits, MLDataUnitExport mlExporter) {
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
