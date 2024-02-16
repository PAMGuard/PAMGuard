package cpod;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FilenameUtils;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.dialog.warn.WarnOnce;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryFooter;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.BinaryOutputStream;
import binaryFileStorage.BinaryStore;
import cpod.CPODReader.CPODHeader;
import cpod.CPODUtils.CPODFileType;
import cpod.FPODReader.FPODHeader;
import cpod.FPODReader.FPODdata;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * Imports FPOD and CPOD data and converts into binary files. 
 * 
 * @author Jamie Macaulay
 *
 */
public class CPODImporter {

	public static final int MAX_SAVE = 2000000; 

	private CPODControl2 cpodControl;

	/**
	 * Handles the queue for importing files tasks 
	 */
	private ExecutorService exec = Executors.newSingleThreadExecutor(r -> {
		Thread t = new Thread(r);
		t.setDaemon(true); // allows app to exit if tasks are running
		return t ;
	});


	public CPODImporter(CPODControl2 cpodControl) {
		this.cpodControl = cpodControl;
	}


	/**
	 * Process a CPOD click. 
	 * @param FPODData - an FPOD data object
	 * @return an CPODClick data unit. 
	 */
	private CPODClick processCPODClick(CPODClick cpodClick) {

		// now a bit of time stretching. Need to get the start time and see how
		// different this is, then it's a linear stretch. 
		long tMillis = cpodControl.stretchClicktime(cpodClick.getTimeMilliseconds());

		cpodClick.setTimeMilliseconds(tMillis);

		return cpodClick;
	}


	/**
	 * Process the click. 
	 * @param FPODData - an FPOD data object
	 * @return an CPODClick data unit. 
	 */
	private CPODClick processFPODClick(FPODdata fpoDdata) {
		//how many samples are we into the clicks
		long fileSamples = (long) (((fpoDdata.minute*60) +  (fpoDdata.FiveMusec*5/1000000.))*CPODClickDataBlock.CPOD_SR);

		// now a bit of time stretching. Need to get the start time and see how
		// different this is, then it's a linear stretch. 
		long tMillis = cpodControl.stretchClicktime(fpoDdata.getTimeMillis());

		CPODClick cpodClick = CPODClick.makeFPODClick(tMillis, fileSamples, fpoDdata);

		return cpodClick;
	}


	/**
	 * Run the import task. 
	 */
	public void runImportTask(ArrayList<CPODFile> files, CPODClickDataBlock clickDataBlock, CPODClickTrainDataBlock clickTrainDataBlock) {
		Task<Integer>  cpodTask = new CPODImportTask(files, clickDataBlock, clickTrainDataBlock); 

		Thread th = new Thread(cpodTask);

		th.setDaemon(true);

		th.start();
	}


	/**
	 * Import the CPOD data from a certain file type. 
	 * @param  files to import - a list of CPOD compatible files (can be a mix but only the files corresponding to type will be processed)
	 * @param type - the type flag of the file e.g. CPODFileType.CP1
	 * @return the CPOD import task. 
	 */
	public Task<Integer> importCPODDataTask(List<CPODFile> files) {

		CPODImportTask cpodTask = new CPODImportTask(files, cpodControl.getCP1DataBlock(), cpodControl.getClickTrainDataBlock()); 

		return cpodTask; 
	}


	/**
	 * Import CPOD and/or FPOD data from a list of files. The files can be CP1/FP1 or CP3/FP3 or a mix of both.
	 * @param files - a list of CPOD or FPOD files
	 * @return the task to import the list. 
	 */
	public Task<Integer> importCPODData(List<File> files) {
		// Need to create a list of cp1 and cp3 files. 
		ArrayList<CPODFile> cpodFiles = new ArrayList<CPODFile>(); 
		
		CPODFile cpodFile;
		while (files.size()>0) {
			cpodFile = new CPODFile();
			
			CPODFileType type = CPODUtils.getFileType(files.get(0));
			
			File currentFile = files.get(0);
			switch (type) {
			case CP1:
			case FP1:
				cpodFile.cp1File= files.get(0);
				break;
			case CP3:
			case FP3:
				cpodFile.cp3File= files.get(0);
				break;
			default:
				break;
			
			}
			files.remove(0);
			
			//now search for the corresponding CP3 or CP1 file
			for (int i=0; i<files.size(); i++) {
				if (isFilePair(currentFile, files.get(i))){
					switch (type) {
					case CP1:
					case FP1:
						cpodFile.cp3File= files.get(i);
						files.remove(files.get(i));
						break;
					case CP3:
					case FP3:
						cpodFile.cp1File= files.get(i);
						files.remove(files.get(i));
						break;
					default:
						break;
					
					}
					break;//break out of the for loop
				}
			}
			cpodFiles.add(cpodFile);
		}	
				
		return new CPODImportTask(cpodFiles, this.cpodControl.getCP1DataBlock(), this.cpodControl.getClickTrainDataBlock());
	} 
	
	/**
	 * Check whether the two files are a CP1, CP3 pair or FP1 FP3 pair.
	 * @param cpxfile1
	 * @param cpxFile2
	 * @return true if the files are a pair. 
	 */
	private boolean isFilePair(File cpxfile1, File cpxFile2) {
		
		String filePattern = cpxfile1.getName().substring(0, cpxfile1.getName().length()-1); 
		
		if (cpxFile2.getName().contains(filePattern) && !cpxfile1.getName().equals(cpxFile2.getName())) {
			return true;
		}
		else return false;
	}


	/**
	 * Run the tasks
	 * @param task - the tasks. 
	 */
	public void runTasks(Task<Integer> task) {
		this.exec.execute(task);
	}


	/**
	 * Task for importing CPOD data. 
	 * @author Jamie Macaulay
	 *
	 */
	class CPODImportTask extends Task<Integer> {


		/**
		 * List of files, either CP1 or CP3
		 */
		private List<CPODFile> cpxFile;

		/**
		 * Reference to the binary store. 
		 */
		private BinaryStore binaryStore;

		/**
		 * The click data block. 
		 */
		private CPODClickDataBlock cpodDataBlock;

		/**
		 * The binary stream
		 */
		private BinaryOutputStream binaryStream;

		/**
		 * Reference to the CPOD click train datablock. 
		 */
		private CPODClickTrainDataBlock clickTrainDataBlock;

		private ArrayList<CPODClick> cpodTrainList; 

		/**
		 * 
		 * @param cpxfiles - a list of CP1 or CP3 files. 
		 * @param cpodDataBlock - the CPOD data block. 
		 */
		public CPODImportTask(List<CPODFile> cpxfiles, CPODClickDataBlock cpodDataBlock, CPODClickTrainDataBlock clickTrainDataBlock) {
			this.cpxFile = cpxfiles; 
			this.cpodDataBlock=cpodDataBlock; 
			this.clickTrainDataBlock=clickTrainDataBlock;
		}


		@Override
		protected Integer call() throws Exception {
			try {

				BinaryDataSource binarySource = cpodDataBlock.getBinaryDataSource();
				binaryStore = (BinaryStore) PamController.getInstance().findControlledUnit(BinaryStore.defUnitType);
				if (binaryStore == null) {
					String msg = "<html>Error: Can't convert CPOD files unless you have a Binary Storage module.<br>" + 
							"Please close this dialog and add/configure a binary store first.</html>";
					WarnOnce.showWarning(null, "CPOD Import",	msg, WarnOnce.OK_OPTION);
					System.out.println("Can't convert CPOD files unless you have a binary storage module");
					return null;
				}

				BinaryOutputStream outputStream = new BinaryOutputStream(binaryStore, cpodDataBlock);
				binarySource.setBinaryStorageStream(outputStream);
				binaryStream = cpodDataBlock.getBinaryDataSource().getBinaryStorageStream();

				for (int i=0; i<cpxFile.size(); i++) {
					int count=0; 

					if (this.isCancelled()) {
						return -1; 
					}

					final int ii = i;

					System.out.println(("Importing CPOD file: " + (ii+1) + "  " +cpxFile.get(i)));
					this.updateMessage(("Importing CPOD file: " + (ii+1) + "  " +cpxFile.get(i).getName()));

					int nClicks = 0; 

					this.updateProgress(-1, 1);
					
					boolean importClicks = true;

					//get the start and end of the file. 
					long[] fileStartEnd = getFileStartEnd(cpxFile.get(i).cp1File !=null ? cpxFile.get(i).cp1File :  cpxFile.get(i).cp3File);

					while (importClicks) {

						System.out.println(("Importing file " + (i+1) + " of " + cpxFile.size() + " from detection " + nClicks));

						this.updateMessage(("Importing file " + (i+1) + " of " + cpxFile.size() + " from detection " + nClicks));

						//import the CPOD or FPOD data
						this.updateProgress(-1, 1);
						int importClicksN = importCPODFile(cpxFile.get(i), cpodDataBlock, clickTrainDataBlock, nClicks, MAX_SAVE); 
						
						//if there are no more clicks imported OR there is only a CP3 or FP3 file then we have finished importing
						//This is because CP3 files are always loaded completely (because they hold a lot less data than CP1)
						if (importClicksN==0 || cpxFile.get(i).cp1File==null) importClicks=false;
						
						this.updateProgress(-1, 1);

						System.out.println("Number of CPOD data units in the data block: " + cpodDataBlock.getUnitsCount() + " progress: " +  (i+1) + " " + cpxFile.size() );

						//need to make a copy of the data incase we clear the cp2 datablock to look for previously
						//loaded detection. Not memory efficient but the easiest way to do and only occurs
						//in the laoding process. 
						ListIterator<CPODClick> iterator = cpodDataBlock.getDataCopy().listIterator();
						cpodDataBlock.clearAll(); 
						//save the click trains to the database
						clickTrainDataBlock.saveViewerData();

						//save the raw clicks to the binary file.s 
						CPODClick  click; 
						double day = -1; 

						Calendar cal = Calendar.getInstance();
						BinaryObjectData data ; 
						while (iterator.hasNext()) {
							if (this.isCancelled()) return -1; 
							click = iterator.next(); 

							//new binary file every daya; 
							cal.setTimeInMillis(click.getTimeMilliseconds());
							int dayYear = cal.get(Calendar.DAY_OF_YEAR);

							if (day!=dayYear) {

								//set the progress
								double progress=(((double) click.getTimeMilliseconds()-fileStartEnd[0]))/(fileStartEnd[1]-fileStartEnd[0]);
								this.updateProgress(i+(progress), cpxFile.size());

								if (day>-1) {
									//close current file
									binaryStream.writeModuleFooter();
									binaryStream.writeFooter(click.getTimeMilliseconds(), System.currentTimeMillis(), BinaryFooter.END_UNKNOWN);
									binaryStream.closeFile();
									binaryStream.createIndexFile();
								}

								System.out.println("Open new binary file: " + 	PamCalendar.formatDBDateTime(click.getTimeMilliseconds()));

								//send an update message
								final String timeMillisStr = PamCalendar.formatDBDateTime(click.getTimeMilliseconds());

								this.updateMessage(("Saving file: " + 	timeMillisStr));

								//write the module head
								binaryStream.openOutputFiles(click.getTimeMilliseconds());
								binaryStream.writeHeader(click.getTimeMilliseconds(), System.currentTimeMillis());
								binaryStream.writeModuleHeader();

								day=dayYear; 
							}

							data =  cpodDataBlock.getBinaryDataSource().getPackedData(click);
							this.binaryStream.storeData(data.getObjectType(), click.getBasicData(), data);
							nClicks++;
						}

						nClicks=nClicks+1; //so we start at the right click. 
						cpodDataBlock.clearAll();

					}

				}
			} 
			catch (Exception e) {
				e.printStackTrace();
			}

			this.updateMessage("Finished saving detections");
			System.out.println("CPOD import thread finished: " + this);

			return 1;
		}



		/**
		 * Rounds millis to start of da=y
		 * @param millis
		 * @return
		 */
		long roundToDay(long millis) {
			Date date = new Date(millis);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			return calendar.getTimeInMillis();
		}

		/**
		 * Import the CPOD file. 
		 * @param cpFile2 - the cp1 file
		 * @param dataBlock - the datablock
		 * @param from - the click index to save from. e.g. 100 means that only click 100 + in the file is saved
		 * @param maxNum - the maximum number to import
		 * @return the total number of clicks in  the file. 
		 */
		private int importCPODFile(CPODFile cpFile, CPODClickDataBlock dataBlock, CPODClickTrainDataBlock clickTrainDataBlock, int from, int maxNum) {
			ArrayList<CPODClick> cpodCP1Data = null; 
			ArrayList<CPODClick> cpodCP3Data = null; 

			try {

				if (cpFile.isFPOD()) {
					//load a chunk of FP1 data
					cpodCP1Data = FPODReader.importFPODFile(cpFile.cp1File, from, maxNum);
					//load all FP3 data
					cpodCP3Data = FPODReader.importFPODFile(cpFile.cp3File, 0, Integer.MAX_VALUE);
				}
				else {
					//load a chunk of CP1 data
					cpodCP1Data = CPODReader.importCPODFile(cpFile.cp1File, from, maxNum);
					//load all CP3 data
					cpodCP3Data = CPODReader.importCPODFile(cpFile.cp3File, 0, Integer.MAX_VALUE);
				}



			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			} 

			//create an arraylist 
			ArrayList<CPODClick> cpodData = new ArrayList<CPODClick>(); 


			//now we get rid if duplicate clicks
			if (cpodCP1Data != null && cpodCP3Data != null) {
				CPODClickOmparator cpodComparator =  new CPODClickOmparator();
				Collections.sort(cpodCP3Data, cpodComparator);;
				//here we need to replace the CP1 detection in the CP1 file with the CP3 detections in the CP3 file
				for (int j=0; j<cpodCP1Data.size(); j++) {
					int index = Collections.binarySearch(cpodCP3Data, cpodCP1Data.get(j),cpodComparator);
					//replace
					if (index>=0) {
						cpodCP1Data.set(j, cpodCP3Data.get(index));
					}
				}
				cpodData= cpodCP1Data; //cp1 data now contains CP3 clicks. 
			}
			else if (cpodCP1Data != null) cpodData= cpodCP1Data; //only CP1 data imported
			else if (cpodCP3Data != null) cpodData= cpodCP3Data; //only CP3 data imported


			HashMap<Integer, CPODClickTrainDataUnit> cpodClickTrains = new HashMap<Integer, CPODClickTrainDataUnit>();

			//		fileStart + nMinutes * 60000L; 

			int nClicks = 0;
			for (int i=0; i<cpodData.size(); i++) {
				
				//System.out.println("Create a new CPOD click: ");
				CPODClick cpodClick = processCPODClick(cpodData.get(i));
				dataBlock.addPamData(cpodClick);
				
				if (cpodData.get(i).getClassification()!=null) {
					CPODClickTrainDataUnit clickTrain = cpodClickTrains.get(cpodData.get(i).getClassification().clicktrainID);

					//add the click train to the hash map
					if (clickTrain==null) {
						clickTrain= new CPODClickTrainDataUnit(cpodClick.getTimeMilliseconds(), null, cpodData.get(i).getClassification());
						cpodClickTrains.put(cpodData.get(i).getClassification().clicktrainID, clickTrain);
					}

					//add the cpos click as a sub detection
					clickTrain.addSubDetection(cpodClick);				
				}
				nClicks++;
			}
			
//			System.out.println("CPOD CLICK TRAINS: " +cpodClickTrains.size());

			//add all the click trains with sub detections ot the datablock. 
			int count =0;
			for (Integer key: cpodClickTrains.keySet()) {

				if (count%100 ==0) {
					this.updateMessage(("Add click train data to datablock: " + count +  "  of " + cpodClickTrains.keySet().size()));
					this.updateProgress(count, cpodClickTrains.keySet().size());
					//					System.out.println("Add click train data to datablock: " + count +  "  " + cpodClickTrains.keySet().size());
				}

				clickTrainDataBlock.addPamData(cpodClickTrains.get(key));
				count++;
			}

			cpodData=null; //trigger garbage collector if needed

			return  nClicks;

		}

		/**
		 * Import a file. 
		 * @param cpFile - the CP1 file. 
		 * @return the number of clicks saved to the datablock
		 */
		protected int importCPODFile(CPODFile cpFile, CPODClickDataBlock dataBlock, CPODClickTrainDataBlock clickTrainDataBlock) {
			return	importCPODFile( cpFile, dataBlock, clickTrainDataBlock, -1, Integer.MAX_VALUE); 
		}



	}

	/**
	 * Get the start and end of a file from the header data. This opens and closes the file. 
	 * @param file -a CP1, CP3 FP1 or FP3 file;
	 * @return the start and end time in Java millis in a long array (first element start, last element end)
	 */
	private long[] getFileStartEnd(File file) {
		CPODFileType fileType = CPODUtils.getFileType(file);
		long[] timelims = null;
		switch (fileType){
		case CP1:
		case CP3:
			CPODHeader headerC =CPODReader.readHeader(file);
			timelims=new long[] {headerC.fileStart, headerC.fileEnd};
			break;
		case FP1:
		case FP3:
			FPODHeader header =FPODReader.readHeader(file);
			if (header==null) return null; 
			timelims=new long[] {CPODUtils.podTimeToMillis(header.FirstLoggedMin), CPODUtils.podTimeToMillis(header.LastLoggedMin)};
			break;
		default:
			break;
		}
		return timelims;
	}

	// Comparator to sort a list 
	class CPODClickOmparator implements Comparator<CPODClick> { 
		@Override public int compare(CPODClick s1, CPODClick s2) 
		{ 
			if (s1.getTimeMilliseconds() > s2.getTimeMilliseconds()) { 
				return 1; 
			} 
			else if (s1.getTimeMilliseconds() < s2.getTimeMilliseconds()) { 
				return -1; 
			} 
			else if (s1.getTimeMilliseconds() == s2.getTimeMilliseconds()) { 

				if (s1.getStartSample()>s2.getStartSample()) return 1;
				else if (s1.getStartSample()<s2.getStartSample()) return -1;
				else return 0;
			} 
			return -1; 
		} 
	}


}

