package export.RExport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.renjin.eval.Context;
import org.renjin.primitives.io.serialization.RDataWriter;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.PairList;
import org.renjin.sexp.PairList.Builder;

import PamUtils.PamArrayUtils;
import PamguardMVC.PamDataUnit;
import export.PamDataUnitExporter;
import export.MLExport.MLDetectionsManager;

/**
 * Handles exporting pam data units into an rdata. 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public class RExportManager implements PamDataUnitExporter {

	/**
	 * 
	 * All the possible RDataUnit export classes. 
	 */
	ArrayList<RDataUnitExport> rDataExport = new ArrayList<RDataUnitExport>();

	private File currentFileName ;


	private Builder allData; 


	public RExportManager(){
		/***Add more options here to export data units****/
		rDataExport.add(new RClickExport()); 
		rDataExport.add(new RWhistleExport()); 
		rDataExport.add(new RRawExport()); //should be last in case raw data holders have specific exporters
	}


	@Override
	public boolean exportData(File fileName, List<PamDataUnit> dataUnits, boolean append) {

		/**
		 * Note - there is no way to save data units to R files wothout loading the file into memory. 
		 * So everything is stored in memory until saved. 
		 */
		// then
		PamDataUnit minByTime = PamArrayUtils.getMinTimeMillis(dataUnits);

		//matlab struct must start with a letter. 
		Date date = new Date(minByTime.getTimeMilliseconds());
		String entryName = "det_" + MLDetectionsManager.dataFormat.format( date);

		//		System.out.println("Save R data! "+ dataUnits.size());

		//		System.out.println("Export R file!!" + dataUnits.size());

		//is there an existing writer? Is that writer writing to the correct file?
		if (allData==null || !fileName.equals(currentFileName)) {

			if (allData!=null) {
				writeRFile();
			}

			allData = new PairList.Builder();
			currentFileName = fileName;
		}

		//convert the data units to R and save to the PairList builder
		dataUnits2R(dataUnits, entryName, allData);

		return true;

	}

	private void writeRFile() {
		Context context = Context.newTopLevelContext();
		try {

			FileOutputStream fos = new FileOutputStream(currentFileName);
			GZIPOutputStream zos = new GZIPOutputStream(fos);
			
			RDataWriter writer = new RDataWriter(context, zos);	

			writer.save(allData.build());
			writer.close();
			zos.close();
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * Check whether there are compatible data units to be exported. 
	 * @param dataUnits - the data unit list
	 * @return true if r export is possible for the current data units. 
	 */
	public boolean hasCompatibleUnits(List<PamDataUnit> dataUnits) {
		//first need to figure out how many data units there are. 
		for (int j=0; j<dataUnits.size(); j++){
			if (hasCompatibleUnits(dataUnits.get(j).getClass())) return true;
		}
		return false; 
	}

	@Override
	public boolean hasCompatibleUnits(Class dataUnitType) {
		for (int i=0; i<rDataExport.size(); i++){
			//check whether the same. ;
			//System.out.println(" dataUnits.get(j).getClass(): " + dataUnits.get(j).getClass());
			//System.out.println(" mlDataUnitsExport.get(i).getUnitClass(): " + mlDataUnitsExport.get(i).getUnitClass());
			if (rDataExport.get(i).getUnitClass().isAssignableFrom(dataUnitType)) {
				//System.out.println("FOUND THE DATA UNIT!");
				return true; 
			}
		}
		return false;
	}

	/**
	 * Sort a list of data units into lists of the same type of units. Convert to a list of structures. 
	 * @param dataUnits - a list of data units to convert to matlab structures. 
	 * @return list of list of R strucutures ready for saving to .RData file. 
	 */
	public RData dataUnits2R(List<PamDataUnit> dataUnits){
		PairList.Builder allData = new PairList.Builder();
		return dataUnits2R(dataUnits,  null, allData);
	}


	/**
	 * Sort a list of data units into lists of the same type of units. Convert to a list of structures. 
	 * @param dataUnits - a list of data units to convert to matlab structures. 
	 * @param - a name for the structure. 
	 * @return list of list of R strucutures ready for saving to .RData file. 
	 */
	public RData dataUnits2R(List<PamDataUnit> dataUnits, String name, 	PairList.Builder allData) {

		//if there's a mixed bunch of data units then we want separate arrays of structures. So a structure of arrays of structures.
		//so, need to sort compatible data units.  		

		ArrayList<String> dataUnitTypes = new ArrayList<String>(); 

		//keep a track of the data units that have been transcribed. This means data units that are multiple types
		//(e.g. a raw data holder and click) are not added to two different list of structures. 
		boolean[] alreadyStruct = new boolean[dataUnits.size()];

		//iterate through possible export functions. 
		for (int i=0; i<rDataExport.size(); i++){

			//first need to figure out how many data units there are. 
			int n=0; 
			for (int j=0; j<dataUnits.size(); j++){
				//check whether the same. 
				if (rDataExport.get(i).getUnitClass().isAssignableFrom(dataUnits.get(j).getClass())) {
					n++;
				}
			}


			if (n==0) continue; //no need to do anything else. There are no data units of this type. 

			ListVector.NamedBuilder dataListArray = new ListVector.NamedBuilder();
			ListVector.NamedBuilder dataList;

			n=0; 
			double sampleRate = 0.;
			//allocate the class now. 
			for (int j=0; j<dataUnits.size(); j++){
				//check whether the same. 
				if (rDataExport.get(i).getUnitClass().isAssignableFrom(dataUnits.get(j).getClass()) && !alreadyStruct[j]) {
					dataList=rDataExport.get(i).detectionToStruct(dataUnits.get(j), n); 
					dataListArray.add((rDataExport.get(i).getName() + "_" + dataUnits.get(j).getUID()), dataList);	

					sampleRate = dataUnits.get(j).getParentDataBlock().getSampleRate(); 
					n++; 
					alreadyStruct[j] = true;
				}
			}

			if (n>0) {

				String dataName; 
				if (name==null) {
					dataName = rDataExport.get(i).getName();
				}
				else dataName = name + "_" + rDataExport.get(i).getName();

				allData.add(dataName, dataListArray.build());
				allData.add(rDataExport.get(i).getName()+"_sR",  new DoubleArrayVector(sampleRate));

				dataUnitTypes.add(rDataExport.get(i).getName()); 
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
		 * List of the names of the types of data units which were saved. 
		 */
		public ArrayList<String> dataUnitTypes; 
	}



	@Override
	public String getFileExtension() {
		return "RData";
	}

	@Override
	public String getIconString() {
		return "file-r";
	}

	@Override
	public String getName() {
		return "R data";
	}


	@Override
	public void close() {
		if (allData!=null) {
			writeRFile();
		}
	}



}
