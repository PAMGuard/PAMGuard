package Acquisition.sud;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingUtilities;

import org.pamguard.x3.sud.ChunkHeader;
import org.pamguard.x3.sud.SudMapListener;

import Acquisition.AcquisitionParameters;
import Acquisition.pamAudio.PamAudioSettingsPane;
import Acquisition.pamAudio.WavAudioFile;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.worker.PamWorkProgressMessage;
import PamUtils.worker.PamWorkWrapper;
import PamUtils.worker.PamWorker;

/**
 * Opens a .sud audio file.
 * <p>
 * Sud files contain X3 compressed audio data. The sud file reader opens files,
 * creating a map of the file and saving the map as a.sudx file so it can be
 * read more rapidly when the file is next accessed.
 * <p>
 * The SudioAudioInput stream fully implements AudioInputStream and so sud files
 * can be accessed using much of the same code as .wav files.
 *
 * @author Jamie Macaulay
 *
 */
public class SudAudioFile extends WavAudioFile implements PamSettings {

	private Object conditionSync = new Object();

	private volatile PamWorker<AudioInputStream> worker;
	
	private volatile SudMapWorker sudMapWorker;

	/**
	 * Settings pane to allow users to set some additional options.  
	 */
	private SudAudioSettingsPane sudAudioSettingsPane;
	
	/**
	 * Parameters for the sud file. TODO Note: PamAudioManager is always a single
	 * instance referenced globally from PAMGuard. Having parameters is therefore
	 * slightly problematic because they will apply across SoundAcquisition modules.
	 * So in the case that someone is using two or more Sound Acquisition modules
	 * then selecting zero and non -zero pad would be impossible
	 */
	private PamSudParams sudParams = new PamSudParams(); 

	public SudAudioFile() {
		super();
		fileExtensions = new ArrayList<String>(Arrays.asList(new String[] { ".sud" }));
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public String getName() {
		return "SUD";
	}

	@Override
	public AudioInputStream getAudioStream(File soundFile) {

		synchronized (conditionSync) {

			// System.out.println("Get SUD getAudioStream : " + soundFile.getName());

			if (soundFile.exists() == false) {
				System.err.println("The sud file does not exist: " + soundFile);
				return null;
			}
			if (soundFile != null) {

				if (new File(soundFile.getAbsolutePath() + "x").exists()) {
//				System.out.println("----NO NEED TO MAP SUD FILE-----"  + soundFile);
					try {
						return new SudAudioFileReader(sudParams.zeroPad).getAudioInputStream(soundFile);
					} catch (UnsupportedAudioFileException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {

//				System.out.println("----MAP SUD FILE ON OTHER THREAD-----" + soundFile);

					/**
					 * We need to map the sud file. But we don't want this o just freeze the current
					 * GUI thread. Therefore add a listener to the mapping process and show a
					 * blocking dialog to indicate that something is happening. The mapping is put
					 * on a separate thread and blocks stuff from happening until the mapping
					 * process has completed.
					 */
					if (sudMapWorker == null || !sudMapWorker.getSudFile().equals(soundFile)) {

						sudMapWorker = new SudMapWorker(soundFile);
						worker = new PamWorker<AudioInputStream>(sudMapWorker,
								PamController.getInstance().getMainFrame(), 1,
								"Mapping sud file: " + soundFile.getName());
//					System.out.println("Sud Audio Stream STARTED: " + soundFile.getName());

						SwingUtilities.invokeLater(() -> {
							worker.start();
						});
						// this should block AWT thread but won't block if called on another thread..
					}

					// this is only ever called if this function is called on another thread other
					// than the event dispatch thread.
					while (sudMapWorker == null || !sudMapWorker.isDone()) {
						// do nothing
//						System.out.println("Waiting for the SUD file map: " + soundFile.getName() + " worker: " + worker);
						try {
//						Thread.sleep(100);
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					AudioInputStream stream = sudMapWorker.getSudAudioStream();

//				sudMapWorker= null;
//				worker = null;

//				System.out.println("----RETURN SUD FILE ON OTHER THREAD-----" + stream);

					return stream;

				}
			}
		}

		return null;
	}

	public class SudMapProgress implements SudMapListener {

		PamWorker<AudioInputStream> sudMapWorker;

		public SudMapProgress(PamWorker<AudioInputStream> sudMapWorker) {
			this.sudMapWorker = sudMapWorker;
		}

		@Override
		public void chunkProcessed(ChunkHeader chunkHeader, int count) {
			// System.out.println("Sud Map Progress: " + count);
			if (count % 500 == 0) {
				// don't update too often or everything just freezes
				sudMapWorker.update(new PamWorkProgressMessage(-1, ("Mapped " + count + " sud file chunks")));
			}
			if (count == -1) {
				sudMapWorker.update(new PamWorkProgressMessage(-1, ("Mapping sud file finished")));
			}
		}

	}

	/**
	 * Opens an sud file on a different thread and adds a listener for a mapping.
	 * This allows a callback to show map progress.
	 * 
	 * @author Jamie Macaulay
	 *
	 */
	public class SudMapWorker implements PamWorkWrapper<AudioInputStream> {

		private File soundFile;

		private SudMapProgress sudMapListener;

		private volatile boolean done = false;

		private AudioInputStream result;

		public SudMapWorker(File soundFile) {
			this.soundFile = soundFile;
		}

		public File getSudFile() {
			return soundFile;
		}

		public AudioInputStream getSudAudioStream() {
			return result;
		}

		@Override
		public AudioInputStream runBackgroundTask(PamWorker<AudioInputStream> pamWorker) {
			AudioInputStream stream;
			try {
//				System.out.println("START OPEN SUD FILE:");

				this.sudMapListener = new SudMapProgress(pamWorker);
				stream = new SudAudioFileReader(sudParams.zeroPad).getAudioInputStream(soundFile, sudMapListener);

//				System.out.println("END SUD FILE:");

				// for some reason - task finished may not be called on other
				// thread so put this here.
				this.result = stream;
				this.done = true;

				return stream;
			} catch (UnsupportedAudioFileException e) {
				System.err.println("UnsupportedAudioFileException: Could not open sud file: not a supported file "
						+ soundFile.getName());
				System.err.println(e.getMessage());
				// e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Could not open sud file: IO Exception: " + soundFile.getName());

				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void taskFinished(AudioInputStream result) {
//			System.out.println("TASK FINSIHED:");
			this.result = result;
			this.done = true;
		}

		public boolean isDone() {
			return done;
		}

	}
	
	@Override
	public PamAudioSettingsPane getSettingsPane() {
		if (sudAudioSettingsPane==null) {
			sudAudioSettingsPane = new SudAudioSettingsPane(this); 
		}
		return sudAudioSettingsPane;
	}

	@Override
	public String getUnitName() {
		return "PamAudioManager";
	}

	@Override
	public String getUnitType() {
		return "sud_files";
	}

	@Override
	public Serializable getSettingsReference() {
		return sudParams;
	}

	@Override
	public long getSettingsVersion() {
		return PamSudParams.serialVersionUID;
	}


	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
			try {
				sudParams = ((PamSudParams) pamControlledUnitSettings.getSettings()).clone();
				
				return true;
			}
			catch (ClassCastException e) {
				e.printStackTrace();
			}
			return false;
	}

	public PamSudParams getSudParams() {
		return this.sudParams;
	}

}