package cpod;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
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
import cpod.CPODUtils.CPODFileType;
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


	private File cpFile;
	private long fileStart;

	private CPODControl2 cpodControl;
	float[] tempDataGramData;

	//	/**
	//	 * Flag for a CP1 file. 
	//	 */
	//	public static final int FILE_CP1 = 1;
	//	
	//	/**
	//	 * Flag for a CP3 file. 
	//	 */
	//	public static final int FILE_CP3 = 3; 


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
		//		if (fileType != cpFileType) {
		//			System.err.println("CPOD Mismatched file type " + cpFile.getAbsolutePath());
		//		}
	}


	//	
	//	if (cpFileType == FILE_CP1) {
	//		dataBlock = cpodControl.getCP1DataBlock(); 
	//	}
	//	else {
	//		dataBlock = cpodControl.getCP3DataBlock(); 
	//
	//	}
	
	

	/**
	 * Import the CPOD file. 
	 * @param cpFile2 - the cp1 file
	 * @param dataBlock - the datablock
	 * @param from - the click index to save from. e.g. 100 means that only click 100 + in the file is saved
	 * @param maxNum - the maximum number to import
	 * @return the total number of clicks in  the file. 
	 */
	private int importCPODFile(File cpFile2, CPODClickDataBlock dataBlock, CPODClickTrainDataBlock clickTrainDataBlock, int from, int maxNum) {
		ArrayList<CPODClick> cpodData = null; 
		
		try {
			cpodData = CPODReader.importCPODFile(cpFile, from, maxNum);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} 
		
		
//		fileStart + nMinutes * 60000L; 
		
		
		int nClicks = 0;
		for (int i=0; i<cpodData.size(); i++) {
			//System.out.println("Create a new CPOD click: ");
			CPODClick cpodClick = processCPODClick(cpodData.get(i));
			dataBlock.addPamData(cpodClick);
			nClicks++;
		}
		
		cpodData=null; //trigger garbage collector if needed
		
		return  nClicks;
	
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
		
//		short[] data = new short[9];
//		
//		short nCyc = (short) fpoDdata.Ncyc;
//		short bw = (short) fpoDdata.BW;
//		short kHz =  (short) FPODReader.IPItoKhz(fpoDdata.IPIatMax);
//		short endF =  (short) FPODReader.IPItoKhz(fpoDdata.EndIPI);
//		short spl = (short)  fpoDdata.MaxPkExtnd;
//		short slope = 0;
//		
//		data[3]=nCyc;
//		data[4]=bw;
//		data[5]=kHz;
//		data[6]=endF;
//		data[7]=spl;
//		data[8]=slope;
//
//		//these are the basic data
//		CPODClick cpodClick = new CPODClick(fpoDdata.getTimeMillis(),	
//				fileSamples, nCyc, bw,
//				kHz,  endF,   spl,  slope, data); 
		
//		if (fpoDdata.HasWave) {
//			int[] waveform = FPODReader.makeResampledWaveform(fpoDdata);
//			System.out.println("FPOD click waveform: " + kHz); 
//			PamArrayUtils.printArray(waveform);
//		}
		
//		cpodClick.setDurationInMilliseconds((fpoDdata.duration*5.)/1000.); 
		 
		//does the click have a waveform?
		//TODO
		
		 
		return cpodClick;
	}


	/**
	 * @return the fileStart
	 */
	public long getFileStart() {
		return fileStart;
	}

//	/**
//	 * Get the data 
//	 * @param type
//	 * @return
//	 */
//	public CPODClickDataBlock getDataBlock(CPODFileType type) {
//		switch (type) {
//		case CP1:
//		case FP1:
//			return this.cpodControl.getCP1DataBlock();
//		case CP3:
//		case FP3:
//			return this.cpodControl.getCP3DataBlock();
//		}
//		return null;
//	}

	/**
	 * Run the import task. 
	 */
	public void runImportTask(ArrayList<File> files, CPODClickDataBlock clickDataBlock, CPODClickTrainDataBlock clickTrainDataBlock) {
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
	public Task<Integer> importCPODDataTask(List<File> files, CPODFileType type) {

		List<File> cpXFIles = new ArrayList<File>(); 

		for (int i=0; i<files.size(); i++) {
			String ext = FilenameUtils.getExtension(files.get(i).getAbsolutePath());
			if (ext.equals(type.getText())) {
				cpXFIles.add(files.get(i));
			}
		}

		CPODImportTask cpodTask = new CPODImportTask(cpXFIles, cpodControl.getCP1DataBlock(), cpodControl.getClickTrainDataBlock()); 

		return cpodTask; 
	}



	/**
	 * Import the CPOD data from a list of CPOD files. 
	 * @param  a list of CPOD compatible files (can be a mix)
	 * @return a list of tasks whihc imports each file type. 
	 */
	public List<Task<Integer>> importCPODData(List<File> files) {
		
		List<Task<Integer>>  tasks  = new ArrayList<Task<Integer>>(); 

		for (int i=0; i<CPODFileType.values().length; i++) {
			Task<Integer> cp1Task = importCPODDataTask(files, CPODFileType.values()[i]); 
			tasks.add(cp1Task); 
		}

		tasks.get(tasks.size()-1).setOnSucceeded((workerState)->{
			PamController.getInstance().updateDataMap();
		});

		//TODO what if a task is cancelled...
		return tasks; 
	}

	/**
	 * Run the tasks
	 * @param tasks - the tasks. 
	 */
	public void runTasks(List<Task<Integer>> tasks) {

		for (int i=0; i<CPODFileType.values().length; i++) {
			this.exec.execute(tasks.get(i));
		}
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
		private List<File> cpxFile;

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

		/**
		 * 
		 * @param cpxfiles - a list of CP1 or CP3 files. 
		 * @param cpodDataBlock - the CPOD data block. 
		 */
		public CPODImportTask(List<File> cpxfiles, CPODClickDataBlock cpodDataBlock, CPODClickTrainDataBlock clickTrainDataBlock) {
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

					if (this.isCancelled()) return -1; 

					final int ii = i;
					this.updateMessage(("Importing CPOD file: " + (ii+1)));

					int nClicks = 0; 
					int totalClicks = Integer.MAX_VALUE;
					int importedClicks = -1;

					this.updateProgress(-1, 1);
					
					CPODClickDataBlock tempCPODDataBlock = new CPODClickDataBlock("Temp CPOD Detections", 
							null, 0);

					while (importedClicks>0 || importedClicks==-1) {

				
						CPODFileType fileType = CPODUtils.getFileType(cpxFile.get(i));
						
						this.updateMessage(("Importing from " + fileType +  " " +  i + " of " + cpxFile.size()));

						//import the CPOD or FPOD data
						importedClicks = importFile(cpxFile.get(i), tempCPODDataBlock, clickTrainDataBlock, nClicks, MAX_SAVE); 
						
						System.out.println("Number of CPOD data units in the data block: " + tempCPODDataBlock.getUnitsCount() + " progress: " +  (i+1) + " " + cpxFile.size() );
						
						ListIterator<CPODClick> iterator = tempCPODDataBlock.getListIterator(0);
						
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
							count++;

							//System.out.println("Saving click: " + 	click.getUID());

							//new binary file every daya; 
							cal.setTimeInMillis(click.getTimeMilliseconds());
							int dayYear = cal.get(Calendar.DAY_OF_YEAR);
							
							if (day!=dayYear) {
								this.updateProgress(i+(count/(double) importedClicks), cpxFile.size());

								if (day>-1) {
									//close current file
									binaryStream.writeModuleFooter();
									binaryStream.writeFooter(click.getTimeMilliseconds(), System.currentTimeMillis(), BinaryFooter.END_UNKNOWN);
									binaryStream.closeFile();
									binaryStream.createIndexFile();
									
								}

								System.out.println("Open new binary file: " + 	PamCalendar.formatDBDateTime(click.getTimeMilliseconds()));
								
								final String timeMillisStr = PamCalendar.formatDBDateTime(click.getTimeMilliseconds());
								Platform.runLater(()->{
									this.updateMessage(("Saving file: " + 	timeMillisStr));
								});
								
								if (fileType.equals(CPODFileType.CP3) || fileType.equals(CPODFileType.FP3)) {
								
								cpodDataBlock.clearAll();
								
								//load the view from the data block for that day.
								long millisDayStart = roundToDay(click.getTimeMilliseconds());
								cpodDataBlock.loadViewerData(millisDayStart, millisDayStart+24*60*60*1000L-1, null);
								
									//now the tricky bit - if we are loading viewer files we want to save the data from the CP3 on top of the 
									//CP1 file but we DO NOT want to add in duplicate clicks. 
									if (cpodDataBlock.getUnitsCount()>0) {
										System.out.println("There are already data units from CP1 or FP1 files " + cpodDataBlock.getUnitsCount());
										//TODO
									}
								
								}

								//write the module head
								binaryStream.openOutputFiles(click.getTimeMilliseconds());
								binaryStream.writeHeader(click.getTimeMilliseconds(), System.currentTimeMillis());
								binaryStream.writeModuleHeader();

								day=dayYear; 
							}

							data =  cpodDataBlock.getBinaryDataSource().getPackedData(click);
							this.binaryStream.storeData(data.getObjectType(), click.getBasicData(), data);
						}
						
						cpodDataBlock.clearAll(); 
						tempCPODDataBlock.clearAll();
						

						
						//update number of clicks. 
						nClicks=nClicks+MAX_SAVE+1; 
					}
	
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			
			this.updateMessage("Finished saving data units: ");
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
		 * Import a FPOD file. 
		 * @param cpFile - the FP1 or FP3 file. 
		 * @param from - the click index to save from. e.g. 100 means that only click 100 + in the file is saved
		 * @param maxNum - the maximum number to import
		 * @return the total number of clicks in  the file. 
		 */
		protected int importFPODFile(File cpFile, CPODClickDataBlock dataBlock, CPODClickTrainDataBlock clickTrainDataBlock, int from, int maxNum) {
			
			ArrayList<FPODdata> fpodData = new ArrayList<FPODdata>();
			
			try {
				FPODReader.importFile(cpFile, fpodData, from, maxNum);
		
//			fileStart + nMinutes * 60000L; 
			
			HashMap<Integer, CPODClickTrainDataUnit> cpodClickTrains = new HashMap<Integer, CPODClickTrainDataUnit>();
			
			int nClicks = 0;
			for (int i=0; i<fpodData.size(); i++) {
				if (i%40000 ==0) {
					System.out.println("Create a new CPOD click: " + i + " of " + fpodData.size() + PamCalendar.formatDateTime(fpodData.get(i).getTimeMillis()));
				}
				CPODClick cpodClick = processFPODClick(fpodData.get(i));
				dataBlock.addPamData(cpodClick);
				
				if (fpodData.get(i).getClassification()!=null) {
					CPODClickTrainDataUnit clickTrain = cpodClickTrains.get(fpodData.get(i).getClassification().clicktrainID);
					if (clickTrain==null) {
						clickTrain= new CPODClickTrainDataUnit(cpodClick.getTimeMilliseconds(), null, fpodData.get(i).getClassification());
						cpodClickTrains.put(fpodData.get(i).getClassification().clicktrainID, clickTrain);
					}
					clickTrain.addSubDetection(cpodClick);				
				}
				nClicks++;
			}
			
			System.out.println("Add click train to datablock: ");

			//add all the click trains with sub detections ot the datablock. 
			int count =0;
			for (Integer key: cpodClickTrains.keySet()) {
				
				if (count%100 == 0) {
					this.updateMessage(("Add click train data to datablock: " + count +  "  of " + cpodClickTrains.keySet().size()));
					this.updateProgress(count, cpodClickTrains.keySet().size());
//					System.out.println("Add click train data to datablock: " + count +  "  " + cpodClickTrains.keySet().size());
				}

				
				clickTrainDataBlock.addPamData(cpodClickTrains.get(key));
				count++;
			}
			
			System.out.println("Finished adding click train to datablock: ");

			
			fpodData=null; //trigger garbage collector if needed
			
			return  nClicks;

			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			} 
		}
		
		/**
		 * Import a CPOD or FPOD file. 
		 * @param cpFile - the CP1/FP1 or CP3/FP3 file. 
		 * @param from - the click index to save from. e.g. 100 means that only click 100 + in the file is saved
		 * @param maxNum - the maximum number to import
		 * @return the total number of clicks in  the file. 
		 */
		protected int importFile(File cpFile, CPODClickDataBlock dataBlock, CPODClickTrainDataBlock clickTrainDataBlock, int from, int maxNum) {
			CPODFileType fileType = CPODUtils.getFileType( cpFile); 
			
			switch (fileType) {
			case CP1:
			case CP3:
				return importCPODFile(cpFile, dataBlock, clickTrainDataBlock, from, maxNum);
			case FP1:
			case FP3:
				return importFPODFile(cpFile, dataBlock, clickTrainDataBlock, from, maxNum);
			}
			
			return 0;
			
		}		
		
		/**
		 * Import a file. 
		 * @param cpFile - the CP1 file. 
		 * @return the number of clicks saved to the datablock
		 */
		protected int importFile(File cpFile, CPODClickDataBlock dataBlock, CPODClickTrainDataBlock clickTrainDataBlock) {
			return	importFile( cpFile, dataBlock, clickTrainDataBlock, -1, Integer.MAX_VALUE); 
		}

		

	}

}

