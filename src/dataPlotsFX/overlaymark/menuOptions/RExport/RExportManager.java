package dataPlotsFX.overlaymark.menuOptions.RExport;

import java.util.ArrayList;
import java.util.List;

import org.renjin.sexp.ListVector;
import org.renjin.sexp.PairList;
import com.jmatio.types.MLArray;
import PamguardMVC.PamDataUnit;

/**
 * Handles exporting pam data units into an rdata. 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public class RExportManager {

	/**
	 * 
	 * All the possible RDataUnit export classes. 
	 */
	ArrayList<RDataUnitExport> mlDataUnitsExport = new ArrayList<RDataUnitExport>(); 


	public RExportManager(){
		/***Add more options here to export data units****/
		mlDataUnitsExport.add(new RClickExport()); 
		mlDataUnitsExport.add(new RWhistleExport()); 
		mlDataUnitsExport.add(new RRawExport()); //should be last in case raw data holders have specific exporters

	}
	
	/**
	 * Check whether there are compatible data units to be exported. 
	 * @param dataUnits - the data unit list
	 * @return true if MATLAB export is possible for the current data units. 
	 */
	public boolean hasCompatibleUnits(List<PamDataUnit> dataUnits) {
		for (int i=0; i<mlDataUnitsExport.size(); i++){
			//first need to figure out how many data units there are. 
			for (int j=0; j<dataUnits.size(); j++){
				//check whether the same. ;
//				System.out.println(" dataUnits.get(j).getClass(): " + dataUnits.get(j).getClass());
//				System.out.println(" mlDataUnitsExport.get(i).getUnitClass(): " + mlDataUnitsExport.get(i).getUnitClass());
				if (mlDataUnitsExport.get(i).getUnitClass().isAssignableFrom(dataUnits.get(j).getClass())) {
					
					return true; 
				}
			}
		}
		return false; 
	}
	
	
	/**
	 * Sort a list of data units into lists of the same type of units. Convert to a list of structures. 
	 * @param dataUnits - a list of data units to convert to matlab structures. 
	 * @return list of list of R strucutures ready for saving to .RData file. 
	 */
	public RData dataUnits2R(ArrayList<PamDataUnit> dataUnits){
		
		//if there's a mixed bunch of data units then we want separate arrays of structures. So a structure of arrays of structures.
		//so, need to sort compatible data units.  		
		
		PairList.Builder allData = new PairList.Builder();
		ArrayList<String> dataUnitTypes = new ArrayList<String>(); 
		
		ArrayList<MLArray> list = new ArrayList<MLArray>();
		//iterate through possible export functions. 
		for (int i=0; i<mlDataUnitsExport.size(); i++){
			
			//first need to figure out how many data units there are. 
			int n=0; 
			for (int j=0; j<dataUnits.size(); j++){
				//check whether the same. 
				if (mlDataUnitsExport.get(i).getUnitClass().isAssignableFrom(dataUnits.get(j).getClass())) {
					n++;
				}
			}
			
			
			if (n==0) continue; //no need to do anything else. There are no data units of this type. 
			
			ListVector.NamedBuilder dataListArray = new ListVector.NamedBuilder();
			
			ListVector.NamedBuilder dataList;
			n=0; 
			//allocate the class now. 
			for (int j=0; j<dataUnits.size(); j++){
				//check whether the same. 
				if (mlDataUnitsExport.get(i).getUnitClass().isAssignableFrom(dataUnits.get(j).getClass())) {
					dataList=mlDataUnitsExport.get(i).detectionToStruct(dataUnits.get(j), n); 
					dataListArray.add((mlDataUnitsExport.get(i).getName() + "_" + dataUnits.get(j).getUID()), dataList);	
					n++; 
				}
			}
			
			if (n>1) {
				allData.add(mlDataUnitsExport.get(i).getName(), dataListArray.build());
				dataUnitTypes.add(mlDataUnitsExport.get(i).getName()); 
			}
			
		}
		
		RData rData = new RData(); 
		rData.rData=allData; 
		rData.dataUnitTypes=dataUnitTypes; 

		//now ready to save. 
		return rData; 
	}
	
	/**
	 * Simple class to hold RData and list of the data unit names whihc were saved.
	 * @author jamie
	 *
	 */
	public class RData {
		
		/**
		 * The RData raedy to save
		 */
		public PairList.Builder rData; 
		
		/**
		 * List of the names of the types of data units whihc were saved. 
		 */
		public ArrayList<String> dataUnitTypes; 
	}

	

}
