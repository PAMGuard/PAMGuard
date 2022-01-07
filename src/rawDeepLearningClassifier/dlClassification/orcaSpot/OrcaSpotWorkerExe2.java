package rawDeepLearningClassifier.dlClassification.orcaSpot;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.*;  


/**
 * 
 * Executes the Orca Spot deep learning classifier from a python.exe file and
 * python scripts.
 * <p>
 * A temporary wav file is written from current audio data. Python code is then
 * executed which reads the python.exe file and runs scripts which us a deep
 * learning model to detect whether there is an Orca in the audio data. A result
 * is then returned.
 * <p>
 * This a clunky way of doing this however serves as a prototype for a more
 * integrated approach in future.
 * <p>
 * Prerequisites
 * <ul>
 * <li>The OrcaSpot folder with required scripts and models. Script paths are
 * located in OrcaSpotParmas</li>
 * <li>Python and Anaconda or similar installed.</li>
 * <li>Cuda installed from Nvidea.</li>
 * <li>Cuda support for Pytorch installed.</li>
 * <b> To set up the python environment properly</b>
 * <ul>
 * <li>Open command prompt or Anaconda prompt if is using Anaconda.
 * <li>Type python -m venv C:\Your\Enviroment\Path\Here for Example python -m
 * venv C:\Users\Hauec\Desktop\Segmenter\pytorch\my-venv. This creates a Folder
 * called my-venv in the PyTorch Folder inside of the Segmenter.</li>
 * <li>Next you need to activate your Virtual environment. Inside of
 * my-venv\Scripts should see a windows batch called activate.bat. cd to it and
 * run it in CMD. You'll know that it is active via the (my-venv) precommand.
 * </li>
 * <li>Once that is done, run setup_pytorch.bat from the PyTorch folder. It
 * should automatically install Pytorch, PyVision, and all of the required
 * dependencies. With the exception of pywin32 and pypiwin32. Both of them need
 * to installed manually through pip. e.g. pip install pywin32, pypiwin32</li>
 * </ul>
 * 
 * </ul>
 * <p> To get the daemon to run properly you must also install C:\Users\macst>conda install win32pipe
 * e.g. in Anaconda this is:
 * <li>conda install win32pipe, win32file, pywintypes</li>
 * 
 * @author Christopher Hauer
 * @author Jamie Macaulay
 */
public class OrcaSpotWorkerExe2 {


	//	/**
	//	 * Writes wav files.
	//	 */
	//	private WavFileWriter wavFileWrite;
	//
	//	/**
	//	 * The audio format for the temporarily saved file.
	//	 */
	//	private AudioFormat af;

	/**
	 * Daemon to prevent reloading of classifier the whole time. 
	 */
	private Process Daemon;

	/**
	 * The Client for our Named Pipe.
	 */
	private RandomAccessFile RAFClient;
	/**
	 * The Inputstream from our Process
	 */
	private BufferedReader Daemonreader;

	/**
	 * The orca spot parameters. 
	 */
	private OrcaSpotParams2 currentParams;


	/**
	 * Constructor for OrcaSpot classifier
	 * Creates OrcaSpot Daemon
	 */
	public OrcaSpotWorkerExe2(OrcaSpotParams2 orcaSpotParams2) {
		try {

			this.currentParams =  orcaSpotParams2; 
			//setup Deamon
			createDetectorDaemon(currentParams.cuda);

			//Make friends with Daemon
			//!Important Reserved File Path, Server created by Predict.py, do not change!
			RAFClient = new RandomAccessFile("\\\\.\\pipe\\Orcaspot", "rw"); 

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Create our Background Process and establish connection.
	 * @param cuda
	 */
	public void createDetectorDaemon(boolean cuda)
	{
		String[] params;
		String line;
		if (cuda) {
			params = new String[] { currentParams.getPythonExe(), 
					currentParams.getPredict_script(),
					"--seg_path", currentParams.getSegmenter_script(),
					"--class_path", currentParams.getClassifierModel(),
					"--log_dir", currentParams.getLog_Path(),
					"--sequence_len", currentParams.getSeq_len(),
					"--hop", currentParams.getHop_size(),
					"--mode", currentParams.getMode(),
					"--threshold", currentParams.getThreshold(),
					"--num_workers", currentParams.getNum_workers(),
					"--sample_rate", currentParams.getSample_rate()
			};
		} 
		else {
			params = new String[] { currentParams.getPythonExe(), 
					currentParams.getPredict_script(),
					"--seg_path", currentParams.getSegmenter_script(),
					"--class_path", currentParams.getClassifierModel(), 
					"--log_dir", currentParams.getLog_Path(),
					"--sequence_len", currentParams.getSeq_len(), 
					"--hop", currentParams.getHop_size(), 
					"--mode", currentParams.getMode(),
					"--threshold", currentParams.getThreshold(), 
					"--no_cuda", 
					"--num_workers", currentParams.getNum_workers(),
					"--sample_rate", currentParams.getSample_rate()};
		}
		try {
			for (String aString : params) {
				System.out.println(aString);
			}
			Daemon = Runtime.getRuntime().exec(params);
			Daemonreader = new BufferedReader(new InputStreamReader(Daemon.getErrorStream()));
			boolean ready = false;

			while(!ready)
			{
				line = Daemonreader.readLine();

				if (line.trim().toLowerCase().contains("waiting for client...")) 
				{
					ready = true;
					//break;
				}
			}

		} 
		catch (IOException e1) {
			e1.printStackTrace();
		}
	}


	public void closeOrcaSpotWorker()
	{
		try {
			Daemonreader.close();
			RAFClient.close();
			Daemon.destroy();
			if(Daemon.isAlive())
			{
				Daemon.destroyForcibly(); //Kill it
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Transforms Audiodata to doubles
	 * @param audiofile - the audio file to import
	 * @return the raw audio dtaa. 
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 */

	public static double[] transformAudio2Double(File audiofile) throws UnsupportedAudioFileException, IOException {

		// make sure that AudioFormat is 16-bit, 44,100 Hz, little endian
		final AudioInputStream ais = AudioSystem.getAudioInputStream(audiofile);
		AudioFormat audioFormat = ais.getFormat();

		byte[] bytes = null;
		try {
			int bytesToRead = ais.available();
			bytes = new byte[bytesToRead];
			int bytesRead = ais.read(bytes);
			if (bytesToRead != bytesRead) {
				throw new IllegalStateException("read only " + bytesRead + " of " + bytesToRead + " bytes"); 
			}
		}
		catch (IOException ioe) {
			throw new IllegalArgumentException("could not read ''", ioe);
		}

		int n = bytes.length;

		// little endian, mono
		if (audioFormat.getChannels() == 1) {
			double[] data = new double[n/2];
			for (int i = 0; i < n/2; i++) {
				// little endian, mono
				data[i] = ((short) (((bytes[2*i+1] & 0xFF) << 8) | (bytes[2*i] & 0xFF))) / ((double) 32768);
			}
			return data;
		}

		// little endian, stereo
		else if (audioFormat.getChannels() == 2) {
			double[] data = new double[n/4];
			for (int i = 0; i < n/4; i++) {
				double left  = ((short) (((bytes[4*i+1] & 0xFF) << 8) | (bytes[4*i + 0] & 0xFF))) / ((double) 32768);
				double right = ((short) (((bytes[4*i+3] & 0xFF) << 8) | (bytes[4*i + 2] & 0xFF))) / ((double) 32768);
				data[i] = (left + right) / 2.0;
			}
			return data;
		}

		// TODO: handle big endian (or other formats)
		else throw new IllegalStateException("audio format is neither mono or stereo");
	}


	/**
	 * Read Input from DaemonProcess.
	 * @throws IOException
	 */
	public OrcaSpotModelResult readPrediction() throws IOException
	{
		String line;
		boolean finished = false;
		OrcaSpotModelResult orcaSpotModelResult  = null; 

		orcaSpotModelResult = new OrcaSpotModelResult(); 

		int n=0; 
		while (!finished) {
			
			
			line = Daemonreader.readLine(); // is never null because Daemon doesn't die.

			// make sure we are in Order, should prevent noise from preconsuming. but since
			// the process is already there it should make no problem.

		//System.out.println(this.currentParams.mode + " " + n + ": " + line);

			if (line.trim().toLowerCase().contains("|time=")) {
				String time = line.trim().split(",")[0].split("\\|")[2].trim();
				orcaSpotModelResult.timeSeconds = 0.0; 
			}

			if (line.trim().toLowerCase().contains("prob=")) {
				String  conf = line.trim().split("prob=")[1].trim();
				orcaSpotModelResult.detectionConfidence = new float[] {Float.valueOf(conf)}; 
				if (this.currentParams.mode.equals("0") || 
						(this.currentParams.mode.equals("1") && orcaSpotModelResult.detectionConfidence[0]<=Double.valueOf(currentParams.threshold))) {
					finished = true; 
				}
			}


			if (line.trim().toLowerCase().contains("pred_class=")) {
				String  predClass = line.trim().split(",")[1].trim();
				predClass = predClass.substring(11, predClass.length()); 
				orcaSpotModelResult.predictedClass  = predClass; 
				String  predConf = line.trim().split(" prob_class=")[1].trim().substring(0, 6);

				orcaSpotModelResult.calltypeConfidence = Float.valueOf(predConf); 

				finished = true; 
			}
			
//			if (n>10) {
//				//TEMP - 
//				finished = true;
//			}
				
			n=n+1; 
			//			finished = true; 

			//				// attempt a classification
			//				if (this.is_orca(line.trim())) {
			//					classtype = "Orca";
			//
			//					classconf = "null";
			//
			//					String output_orca = "Time=" + time + " -> " + classtype + " detected; Confidence=" + conf
			//							+ " Classconfidence=" + classconf + "s!\n";
			//					System.out.println(output_orca);
			//					finished = true;
			//					//break;
			//				}
			//				else finished = true; 

		}
		return orcaSpotModelResult;
	}
	/**
	 * Run orca spot
	 * @throws IOException
	 * @throws UnsupportedAudioFileException 
	 */

	public OrcaSpotModelResult runOrcaSpot(File audioFile) throws IOException, UnsupportedAudioFileException {
		//Pre process data from file to Doubles
		double[] data = transformAudio2Double(audioFile);

		return runOrcaSpot(data);
	}

	/**
	 * Run OrcaSpot and return a result
	 * @param data - raw acoustic data. 
	 * @return
	 */
	public OrcaSpotModelResult runOrcaSpot(double[] data) throws IOException  {

		//		System.out.println("Data in: ");
		//		for (int i=0; i<20; i++) {
		//			System.out.print("  " + data[i]); 
		//		}
		//		System.out.println("");

		long time0 =  System.currentTimeMillis(); 

		String msg = Arrays.toString(data);

		//Format Data for Predict. It will be translated to a tensor.
		msg = "["+msg+"]R"; //R is our last Sign, therby our end symbol.
		//Probably very inefficient but it works

		//Send Data
		RAFClient.writeBytes(msg);

		//Read from Deamon
		OrcaSpotModelResult orcaSpotResult = readPrediction();

		long time1 =  System.currentTimeMillis(); 

		orcaSpotResult.setAnlaysisTime((time1-time0)/1000.); 

		return orcaSpotResult;
	}



	/**
	 * Check whether the output indicates this is an orca or not.
	 * 
	 * @param prediction_line - the prediction string from the classifier output/
	 * @return true if an orca is classified.
	 */
	public boolean is_orca(String prediction_line) {
		String prediction = prediction_line.split(",")[1].split("=")[1].trim();
		if (prediction.equalsIgnoreCase("1")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Test the algorithm!
	 * 
	 * @param args - input arguments
	 * @throws UnsupportedAudioFileException 
	 * @throws IOException 
	 */
	public static void main(String args[]) throws IOException, UnsupportedAudioFileException {

		// Only 1 Channel Wave Files
		//!IMPORTANT Directory should only contain wave files, no subfolders!
		File Directory = new File("E:\\DeepLearning_OrcaSpot\\example_2s_files");
		String allFiles[] = Directory.list();

		OrcaSpotParams2 orcaSpotParams2= new OrcaSpotParams2(); 
		orcaSpotParams2.mode="1"; 

		//Build Daemon and Client
		//True for Cuda
		orcaSpotParams2.cuda = true; 
		OrcaSpotWorkerExe2 orcaSpotWorker = new OrcaSpotWorkerExe2(orcaSpotParams2);


		//List containing CurrentTimes
		ArrayList<Long> Times = new ArrayList<Long>();
		//First entry.
		Times.add(System.currentTimeMillis());
		//Loop for multiple Files
		OrcaSpotModelResult orcaSpotResult; 
		for(int i=0; i< allFiles.length;i++)
		{
			//Set new Audio File.
			System.out.println("-----Run new file------");

			orcaSpotResult= orcaSpotWorker.runOrcaSpot(new File(Directory.getAbsolutePath()+'/'+allFiles[i]));
			Times.add(System.currentTimeMillis());
			System.out.println(orcaSpotResult.getResultString()); 
		}

		for(int i=0; i<Times.size()-1;i++)
		{
			System.out.println("ProcessTime: " + (Times.get(i+1)-Times.get(i))/1000.0);
		}
		//Clean up daemon.
		orcaSpotWorker.closeOrcaSpotWorker();

	}


	//	/**
	//	 * Make an audio file.
	//	 * 
	//	 * @return an audio file.
	//	 */
	//	private File makeAudioFile(double[][] rawData, float sR) {
	//		af = new Wav16AudioFormat(sR, rawData.length);
	//		wavFileWrite = new WavFileWriter(currentParams.audio_file.getAbsolutePath(), af);
	//		wavFileWrite.write(rawData);
	//		wavFileWrite.close();
	//
	//		return currentParams.audio_file;
	//	}

	/*public void runOrcaSporClassifier(double[][] audioData, float sampleRate, OrcaSpotParams orcaSpotParams) {
		// create the audio file
		makeAudioFile();
		// run the classifier and wait for the result
		runClassifier();
	}*/

	//	/**
	//	 * Currently Unused
	//	 * Run the classifier and output a result
	//	 */
	//	private void runClassifier() {
	//		System.out.println("Run OrcaSpot Classifier!");
	//
	//		String[] params;
	//		String[] params2;
	//		String line;
	//
	//		Process p = null;
	//		Process classifier = null;
	//
	//		// run the deep learning classifier.
	//		if (currentParams.getcuda()) {
	//			params = new String[] { currentParams.pythonExeFile, currentParams.getPredict_script(),
	//					this.audioFile.getAbsolutePath(), "--sequence_len", currentParams.getSeq_len(), "--hop",
	//					currentParams.getHop_size(), "--threshold", currentParams.getThreshold(), "--model",
	//					currentParams.getModel(), "--num_workers", currentParams.getNum_workers() };
	//		} 
	//		else {
	//			params = new String[] { currentParams.pythonExeFile, currentParams.getPredict_script(),
	//					this.audioFile.getAbsolutePath(), "--sequence_len", currentParams.getSeq_len(), "--hop",
	//					currentParams.getHop_size(), "--threshold", currentParams.getThreshold(), "--no_cuda", "--model",
	//					currentParams.getModel(), "--num_workers", currentParams.getNum_workers() };
	//		}
	//		try {
	//			for (String aString : params) {
	//				System.out.println(aString);
	//			}
	//			p = Runtime.getRuntime().exec(params);
	//		} 
	//		catch (IOException e1) {
	//			// TODO Auto-generated catch block
	//			e1.printStackTrace();
	//		}
	//
	//		BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	//		// while(ForceOrder >= SegmenterProcess.order)
	//		// { continue;}
	//
	//		try {
	//			while ((line = input.readLine()) != null) {
	//				// make sure we are in Order, should prevent noise from preconsuming. but since
	//				// the process is already there it should make no problem.
	//
	//				System.out.println(line);
	//
	//				if (line.trim().toLowerCase().contains("|time=")) {
	//
	//					String time = line.trim().split(",")[0].split("\\|")[2].trim();
	//					String conf = line.trim().split("prob=")[1].trim();
	//
	//					String classtype = null;
	//					String classconf = null;
	//
	//					// attempt a classification
	//					if (this.is_orca(line.trim())) {
	//
	//						if (currentParams.useClassifier) {
	//
	//							if (currentParams.getcuda()) {
	//								params2 = new String[] { currentParams.pythonExeFile, currentParams.getClass_script(),
	//										this.audioFile.getAbsolutePath(), "--sequence_len", currentParams.getSeq_len(),
	//										"--hop", currentParams.getHop_size(), "--threshold",
	//										currentParams.getThreshold2(), "--model", currentParams.getclassifierModel(),
	//										"--num_workers", currentParams.getNum_workers() };
	//							} 
	//							else {
	//
	//								params2 = new String[] { currentParams.pythonExeFile, currentParams.getClass_script(),
	//										this.audioFile.getAbsolutePath(), "--sequence_len", currentParams.getSeq_len(),
	//										"--hop", currentParams.getHop_size(), "--threshold",
	//										currentParams.getThreshold2(), "--model", currentParams.getclassifierModel(),
	//										"--num_workers", currentParams.getNum_workers(), "--no_cuda", };
	//							}
	//
	//							System.out.println("");
	//							System.out.println("--Classifier Input Params--");
	//							for (String aString : params) {
	//								System.out.println(aString);
	//							}
	//							System.out.println("----------------");
	//
	//							try {
	//								classifier = Runtime.getRuntime().exec(params2);
	//							} 
	//							catch (IOException e1) {
	//								// TODO Auto-generated catch block
	//								e1.printStackTrace();
	//							}
	//
	//							BufferedReader classifier_input = new BufferedReader(
	//									new InputStreamReader(classifier.getErrorStream()));
	//							try {
	//								while ((line = classifier_input.readLine()) != null) {
	//									if (line.trim().toLowerCase().contains("|time=")) {
	//										String time2 = line.trim().split(",")[0].split("\\|")[2].trim();
	//										classtype = line.trim().split("pred_class=")[1].split(",")[0].trim();
	//										if (classtype.equals("noise")) {
	//											classtype = "UOT";
	//										}
	//										classconf = line.trim().split("prob_class=")[1].trim();
	//									} else {
	//										continue;
	//									}
	//								}
	//							} catch (IOException e) {
	//								// TODO Auto-generated catch block
	//								e.printStackTrace();
	//							}
	//							// } catch (UnsupportedAudioFileException e1) {
	//							// // TODO Auto-generated catch block
	//							// e1.printStackTrace();
	//							// }
	//							/*
	//							 * catch (RawDataUnavailableException e1) { // TODO Auto-generated catch block
	//							 * e1.printStackTrace(); }
	//							 */
	//						} 
	//						else {
	//							classtype = "Orca";
	//							classconf = "null";
	//						}
	//
	//						String output_orca = "Time=" + time + " -> " + classtype + " detected; Confidence=" + conf
	//								+ " Classconfidence=" + classconf + "s!\n";
	//						decision = true;
	//						System.out.println(output_orca);
	//
	//						if (classifier != null) {
	//							try {
	//
	//								classifier.waitFor();
	//							} catch (InterruptedException e) {
	//								// TODO Auto-generated catch block
	//								e.printStackTrace();
	//							}
	//
	//							classifier.destroy();
	//						}
	//					} 
	//					else {
	//						String output_noise = "Time=" + time + " -> Noise detected; Confidence=" + conf + "s!\n";
	//						decision = false;
	//						System.out.println(output_noise);
	//					}
	//				} 
	//				else {
	//					continue;
	//				}
	//			}
	//		} catch (IOException e1) {
	//			// TODO Auto-generated catch block
	//			e1.printStackTrace();
	//		}
	//
	//		try {
	//			p.waitFor();
	//		} catch (InterruptedException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//		p.destroy();
	//
	//	}


}
