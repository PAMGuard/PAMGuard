package soundtrap;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryFooter;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.BinaryOutputStream;
import binaryFileStorage.BinaryStore;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.debug.Debug;
import clickDetector.ClickBinaryDataSource;
import clickDetector.ClickControl;
import clickDetector.ClickDataBlock;
import clickDetector.ClickDetection;
import clickDetector.ClickDetector;
import warnings.PamWarning;
import warnings.WarningSystem;

public class DWVConverter {

	private ArrayList<STGroupInfo> fileGroups;

	private DWVConvertObserver dwvConvertObserver;

	private DWVWorker dwvWorker;

	private boolean keepRunning;

	private STClickControl clickControl;

	private ClickBinaryDataSource clickBinaryDataSource;
	
	private boolean onEffort = false;

	public DWVConverter(ArrayList<STGroupInfo> fileGroups, DWVConvertObserver dwvConvertObserver, STClickControl clickControl) {
		this.fileGroups = fileGroups;
		this.dwvConvertObserver = dwvConvertObserver;
		this.clickControl = clickControl;
		clickBinaryDataSource = clickControl.getClickDetector().getClickBinaryDataSource();
	}

	public void start() {
		dwvWorker = new DWVWorker();		
		keepRunning = true;
		dwvWorker.execute();
	}

	public void stop() {
		keepRunning = false;
	}

	class DWVWorker extends SwingWorker<Integer, DWVConvertInformation> {

		private BinaryOutputStream binaryStream;
		private ClickDataBlock clickDataBlock;
		private ClickDetector clickDetector;
		private long fileStartMicroseconds;
		private BinaryStore binaryStore;

		@Override
		protected Integer doInBackground() {
			try {
				clickDetector = clickControl.getClickDetector();
				clickDataBlock = clickControl.getClickDataBlock();
				BinaryDataSource binarySource = clickDataBlock.getBinaryDataSource();
				binaryStore = (BinaryStore) PamController.getInstance().findControlledUnit(BinaryStore.defUnitType);
				if (binaryStore == null) {
					String msg = "<html>Error: Can't convert dwv files unless you have a Binary Storage module.<br>" + 
							"Please close this dialog and add/configure a binary store first.</html>";
					int ans = WarnOnce.showWarning(null, "Soundtrap Tools",	msg, WarnOnce.OK_OPTION);
					System.out.println("Can't convert dwv files unless you have a binary storage module");
					return null;
				}
				BinaryOutputStream outputStream = new BinaryOutputStream(binaryStore, clickDataBlock);
				binarySource.setBinaryStorageStream(outputStream);
				binaryStream = clickBinaryDataSource.getBinaryStorageStream();

				int iFile = 0;
				int nFile = fileGroups.size();
				for (STGroupInfo fileGroup:fileGroups) {
					processFiles(fileGroup, nFile, ++iFile);
					if (keepRunning == false) {
						break;
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		private void processFiles(STGroupInfo fileGroup, int nFile, int iFile) {
			this.publish(new DWVConvertInformation(fileGroup, nFile, iFile, 0, 0));
			if (fileGroup.hasDWV() == false) {
				/*
				 * Don't do this. In quiet conditions there will be a BCL file with a start and stop time
				 * but if there were no detections there will be no dwv file. We should make the pgdf file
				 * in any case so that there is a full PAMGuard record of effort. 
				 * In other circumstances dwv files don't actually have data, in which case there may still be
				 * a null file, which is not so good. Will have to think about how to report this - perhaps in 
				 * this case it's better not to make a pgdf file to show a genuine gap in the data.  
				 */
//				return;
			}
			BCLReader bclReader = new BCLReader(fileGroup.getBclFile());
			boolean ok = bclReader.open();
			if (ok == false) return;
			int nDWV = 0;
			DWVReader dwvReader = null;
			if (fileGroup.hasDWV()) {
				dwvReader = new DWVReader(fileGroup.getDwvFile(), fileGroup.getDwvInfo());
				dwvReader.openDWV();
				nDWV = dwvReader.getNumDWV();
			}
			int repStep = Math.max(10, nDWV/100);
			int nRead = 0;
			while (true) {
				BCLLine bclLine = bclReader.nextBCLLine();
				if (bclLine == null) {
					
					// if we have finished the bcl file but are still technically 'on effort', close the binary file properly before starting the next
					if (onEffort) {
						effortLine(fileGroup, null);
					}
					
					break;
				}
				// Work out what kind of line it is - it it effort or what ? 
				// ideally we'll; check the effort on and off times against
				// the times from the xml file. 
				switch (bclLine.getReport()) {
				case "E":
					effortLine(fileGroup, bclLine);
					break;
				case "D":
					dataLine(fileGroup, bclLine, dwvReader);
					nRead++;
					if (nRead%repStep == 0) {
						this.publish(new DWVConvertInformation(fileGroup, nFile, iFile, nDWV, nRead));
					}
					break;

				}

				//				try {
				//					Thread.sleep(10);
				//				} catch (InterruptedException e) {
				//					// TODO Auto-generated catch block
				//					e.printStackTrace();
				//				}
			}
			this.publish(new DWVConvertInformation(fileGroup, nFile, iFile, nDWV, nDWV));
		}

		private void dataLine(STGroupInfo fileGroup, BCLLine bclLine, DWVReader dwvReader) {
			double[] dwvData = null;
			if (dwvReader != null) {
				dwvData = dwvReader.readNextClick(null);
			}
			if (dwvData == null) {
				return; // can happen if file didn't flush correclty and some dwv is missing. 
			}
			long micros = bclLine.getMicroSeconds() - fileStartMicroseconds;
			long sampleNumber = micros * fileGroup.getDwvInfo().getFs() / 1000000;
			ClickDetection click = new ClickDetection(1, sampleNumber, dwvData.length, clickDetector, null, 1);
			click.setTimeMilliseconds(bclLine.getMilliseconds());
			double[][] waveData = {dwvData};
			click.setWaveData(waveData);
			clickDataBlock.addPamData(click);
			clickBinaryDataSource.saveData(click);
			
			// now remove the data unit from the datablock in order to clear up memory.  Note that the remove method
			// saves the data unit to the Deleted-Items list, so clear that as well (otherwise we'll just be using
			// up all the memory with that one)
			clickDataBlock.remove(click);
			clickDataBlock.clearDeletedList();
		}

		private void effortLine(STGroupInfo fileGroup, BCLLine bclLine) {
			if (binaryStream == null) {
				return;
			}
			
			// special case - the bcl file is complete but there was no closing 'E' line (i.e. we are still on-effort).  Just
			// use the samplingEndTime from the dwv or wav file (whichever is later) and properly close the binary file
			if (bclLine==null) {
				long dwvFileStop = fileGroup.getDwvInfo().getTimeInfo().samplingStopTimeUTC;
				long wavFileStop = fileGroup.getWavInfo().getTimeInfo().samplingStopTimeUTC;
				long latestTime = Math.max(dwvFileStop, wavFileStop);
				System.out.printf("Warning - no off-effort line in BCL file.  DWV stop %s wav stop %s E stop %s\n", PamCalendar.formatDBDateTime(dwvFileStop),
						PamCalendar.formatDBDateTime(wavFileStop), PamCalendar.formatDBDateTime(latestTime));
				binaryStream.writeModuleFooter();
				binaryStream.writeFooter(latestTime, System.currentTimeMillis(), BinaryFooter.END_UNKNOWN);
				binaryStream.closeFile();
				binaryStream.createIndexFile();
				onEffort = false;
				return;
			}
			
			if (bclLine.getState() == 1) {
				fileStartMicroseconds = bclLine.getMicroSeconds();
				long wavFileStart = fileGroup.getWavInfo().getTimeInfo().samplingStartTimeUTC;
				try {
					if (fileGroup.getDwvInfo() != null) {
						long dwvFileStart = fileGroup.getDwvInfo().getTimeInfo().samplingStartTimeUTC;
						Debug.out.printf("DWV start %s wav start %s bcl start %s\n", PamCalendar.formatDBDateTime(dwvFileStart),
								PamCalendar.formatDBDateTime(wavFileStart), PamCalendar.formatDBDateTime(bclLine.getMilliseconds()));
					}
					else {
						Debug.out.printf("No DWV File !!! wav start %s bcl start %s\n", 
								PamCalendar.formatDBDateTime(wavFileStart), PamCalendar.formatDBDateTime(bclLine.getMilliseconds()));
					}
				}
				catch (Exception e) {
					System.out.println("Exception 1 in DWVConverter.effortLine: " + e.getMessage());
				}
				binaryStream.openOutputFiles(bclLine.getMilliseconds());
				binaryStream.writeHeader(bclLine.getMilliseconds(), System.currentTimeMillis());
				binaryStream.writeModuleHeader();
				onEffort = true;
			}
			if (bclLine.getState() == 0) {
				try {
					long dwvFileStop = fileGroup.getDwvInfo().getTimeInfo().samplingStopTimeUTC;
					long wavFileStop = fileGroup.getWavInfo().getTimeInfo().samplingStopTimeUTC;
					Debug.out.printf("DWV stop %s wav stop %s E stop %s\n", PamCalendar.formatDBDateTime(dwvFileStop),
							PamCalendar.formatDBDateTime(wavFileStop), PamCalendar.formatDBDateTime(bclLine.getMilliseconds()));
				}
				catch (Exception e) {
					System.out.println("Exception 2 in DWVConverter.effortLine: " + e.getMessage());
				}
				binaryStream.writeModuleFooter();
				binaryStream.writeFooter(bclLine.getMilliseconds(), System.currentTimeMillis(), BinaryFooter.END_UNKNOWN);
				binaryStream.closeFile();
				binaryStream.createIndexFile();
				onEffort = false;
			}

		}

		@Override
		protected void process(List<DWVConvertInformation> chunks) {
			if (dwvConvertObserver != null) for (DWVConvertInformation info:chunks) {
				dwvConvertObserver.process(info);
			}
		}

		@Override
		protected void done() {
			String msg = "<html>Import Complete</html>";
			int ans = WarnOnce.showWarning(null, "Soundtrap Tools",	msg, WarnOnce.OK_OPTION);
			if (dwvConvertObserver != null) dwvConvertObserver.done();
			dwvWorker = null;
		}


	}

}
