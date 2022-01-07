package rawDeepLearningClassifier.dlClassification.orcaSpot;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import wavFiles.Wav16AudioFormat;
import wavFiles.WavFileWriter;

/**
 * 
 * Executes the Orca Spot deep learning classifier from a python.exe file and
 * python scripts.
 * 
 * Getting this to work is not a simple process.
 * 
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
 * </ul>
 * <p>
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
 * @author Christopher Hauer
 * @author Jamie Macaulay
 */
public class OrcaSpotWorkerExe {

	/**
	 * The raw wave data in linear units and by channel
	 */
	double[][] rawData;

	private OrcaSpotParams currentParams = new OrcaSpotParams();

	/**
	 * Writes wav files.
	 */
	private WavFileWriter wavFileWrite;

	/**
	 * The sample rate in samples per second
	 */
	private float sampleRate = 44100;

	/**
	 * The audio format for the temporarily saved file.
	 */
	private AudioFormat af;
	/**
	 * Use the classifier
	 */
	private boolean useClassifier = true;

	/**
	 * Has a decision been made on whether and Orca or not.
	 */
	private boolean decision = false;

	/**
	 * The location of audio file.
	 */
	private File audioFile;

	/**
	 * Constructor for OrcaSpot classifier
	 */
	public OrcaSpotWorkerExe() {

	}

	// private long simpleSamplesToMilliseconds(long samples) {
	// return (long) (samples * 1000. / SegmenterProcess.getSampleRate()) +
	// PamCalendar.getSessionStartTime();
	// }
	//
	//
	//
	// public double[][] getFFTInputData(long startSam,long durationSam, int
	// Chanmap) throws RawDataUnavailableException{
	// PamRawDataBlock Acq =
	// SegmenterProcess.getParentDataBlock().getRawSourceDataBlock();
	// return Acq.getSamples(startSam,(int) durationSam, Chanmap);
	// }
	//
	// public ComplexArray rfft(double[] x, int n)
	// {
	// ComplexArray fftOutData = null;
	// FastFFT fastFFT = new FastFFT();
	// fftOutData = fastFFT.rfftFull(x, n);
	// return fftOutData;
	// }

	/**
	 * Make an audio file.
	 * 
	 * @return an audio file.
	 */
	private File makeAudioFile() {
		af = new Wav16AudioFormat(getSampleRate(), rawData.length);
		wavFileWrite = new WavFileWriter(currentParams.audio_file.getAbsolutePath(), af);
		wavFileWrite.write(rawData);
		wavFileWrite.close();

		return currentParams.audio_file;
	}

	private float getSampleRate() {
		return this.sampleRate;
	}

	public void runOrcaSporClassifier(double[][] audioData, float sampleRate, OrcaSpotParams orcaSpotParams) {
		// create the audio file
		makeAudioFile();
		// run the classifier and wait for the result
		runClassifier();
	}

	/**
	 * Run the classifier and output a result
	 */
	private void runClassifier() {
		System.out.println("Run OrcaSpot Classifier!");

		String[] params;
		String[] params2;
		String line;

		Process p = null;
		Process classifier = null;

		// run the deep learning classifier.
		if (currentParams.getNo_cuda()) {
			params = new String[] { currentParams.pythonExeFile, currentParams.getPredict_script(),
					this.audioFile.getAbsolutePath(), "--sequence_len", currentParams.getSeq_len(), "--hop",
					currentParams.getHop_size(), "--threshold", currentParams.getThreshold(), "--model",
					currentParams.getModel(), "--num_workers", currentParams.getNum_workers() };
		} 
		else {
			params = new String[] { currentParams.pythonExeFile, currentParams.getPredict_script(),
					this.audioFile.getAbsolutePath(), "--sequence_len", currentParams.getSeq_len(), "--hop",
					currentParams.getHop_size(), "--threshold", currentParams.getThreshold(), "--no_cuda", "--model",
					currentParams.getModel(), "--num_workers", currentParams.getNum_workers() };
		}
		try {
			for (String aString : params) {
				System.out.println(aString);
			}
			
			long time1 = System.currentTimeMillis();

			p = Runtime.getRuntime().exec(params);
			
			long time2 = System.currentTimeMillis();
			System.out.println("Detector runtime = " + (time2 - time1) / 1000.0 + " seconds");
		} 
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		// while(ForceOrder >= SegmenterProcess.order)
		// { continue;}

		try {
			while ((line = input.readLine()) != null) {
				// make sure we are in Order, should prevent noise from preconsuming. but since
				// the process is already there it should make no problem.

				System.out.println(line);

				if (line.trim().toLowerCase().contains("|time=")) {

					String time = line.trim().split(",")[0].split("\\|")[2].trim();
					String conf = line.trim().split("prob=")[1].trim();

					String classtype = null;
					String classconf = null;

					// attempt a classification
					if (this.is_orca(line.trim())) {

						if (useClassifier) {

							if (currentParams.getNo_cuda()) {
								params2 = new String[] { currentParams.pythonExeFile, currentParams.getClass_script(),
										this.audioFile.getAbsolutePath(), "--sequence_len", currentParams.getSeq_len(),
										"--hop", currentParams.getHop_size(), "--threshold",
										currentParams.getThreshold2(), "--model", currentParams.getclassifierModel(),
										"--num_workers", currentParams.getNum_workers() };
							} 
							else {

								params2 = new String[] { currentParams.pythonExeFile, currentParams.getClass_script(),
										this.audioFile.getAbsolutePath(), "--sequence_len", currentParams.getSeq_len(),
										"--hop", currentParams.getHop_size(), "--threshold",
										currentParams.getThreshold2(), "--model", currentParams.getclassifierModel(),
										"--num_workers", currentParams.getNum_workers(), "--no_cuda", };
							}

							System.out.println("");
							System.out.println("--Classifier Input Params--");
							for (String aString : params) {
								System.out.println(aString);
							}
							System.out.println("----------------");

							long time1 = System.currentTimeMillis();

							try {
								

								classifier = Runtime.getRuntime().exec(params2);

							} 
							catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}

							BufferedReader classifier_input = new BufferedReader(
									new InputStreamReader(classifier.getErrorStream()));
							
							long time2c = System.currentTimeMillis();
							System.out.println("Classifier runtime = " + (time2c - time1) / 1000.0 + " seconds");
							
							try {
								while ((line = classifier_input.readLine()) != null) {
									System.out.println("->>" + line);
									if (line.trim().toLowerCase().contains("|time=")) {
										String time2 = line.trim().split(",")[0].split("\\|")[2].trim();
										classtype = line.trim().split("pred_class=")[1].split(",")[0].trim();
										if (classtype.equals("noise")) {
											classtype = "UOT";
										}
										classconf = line.trim().split("prob_class=")[1].trim();
									} else {
										continue;
									}
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							// } catch (UnsupportedAudioFileException e1) {
							// // TODO Auto-generated catch block
							// e1.printStackTrace();
							// }
							/*
							 * catch (RawDataUnavailableException e1) { // TODO Auto-generated catch block
							 * e1.printStackTrace(); }
							 */
						} 
						else {
							classtype = "Orca";
							classconf = "null";
						}

						String output_orca = "Time=" + time + " -> " + classtype + " detected; Confidence=" + conf
								+ " Classconfidence=" + classconf + "s!\n";
						decision = true;
						System.out.println(output_orca);

						if (classifier != null) {
							try {

								classifier.waitFor();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							classifier.destroy();
						}
					} 
					else {
						String output_noise = "Time=" + time + " -> Noise detected; Confidence=" + conf + "s!\n";
						decision = false;
						System.out.println(output_noise);
					}
				} 
				else {
					continue;
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		p.destroy();

	}

	/**
	 * Check whether the output indicates this is an orca or not.
	 * 
	 * @param prediction_line - the prediciton string from the classifier output/
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
	 */
	public static void main(String args[]) {

		// file location

		// a 1 channel wave file.
		String filePath = "E:\\Google Drive\\PAMGuard_dev\\DeepLearning_OrcaSpot\\example_2s_files\\ch21_call_242_2018_162516065_391647_394869.wav";

		File audioFile = new File(filePath);

		OrcaSpotWorkerExe orcaSpotWorker = new OrcaSpotWorkerExe();

		// true to run on grpahics card
		orcaSpotWorker.currentParams.no_cuda = true;
		// set the current audio file.
		orcaSpotWorker.audioFile = audioFile;

		long time1 = System.currentTimeMillis();

		orcaSpotWorker.runClassifier();

		long time2 = System.currentTimeMillis();

		System.out.println("Time = " + (time2 - time1) / 1000.0 + " seconds");
	}
}
