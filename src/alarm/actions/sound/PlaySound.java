package alarm.actions.sound;

import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import alarm.AlarmControl;
import alarm.AlarmDataUnit;
import alarm.AlarmParameters;
import alarm.actions.AlarmAction;

public class PlaySound extends AlarmAction implements PamSettings {

	protected PlaySoundParams playSoundParams = new PlaySoundParams();

	private File currentFile;

	private Clip currentClip;

	private Info li;

	private Clip preparedClip;

	public PlaySound(AlarmControl alarmControl) {
		super(alarmControl);
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public String getActionName() {
		return "Play Sound";
	}

	@Override
	public boolean hasSettings() {
		return true;
	}

	@Override
	public boolean setSettings(Window window) {
		PlaySoundParams newParams = PlaySoundDialog.showDialog(window, this);
		if (newParams != null) {
			playSoundParams = newParams.clone();
			return true;
		}
		return false;
	}

	@Override
	public boolean actOnAlarm(AlarmDataUnit alarmDataUnit) {
		if (alarmDataUnit == null) {
			return false;
		}
		int currentState = alarmDataUnit.getCurrentStatus();
		if (currentState <= 0) {
			return false;
		}
		if (currentState > AlarmParameters.COUNT_LEVELS) {
			System.out.println("Illegal alarm state " + currentState + " from " + alarmDataUnit.toString());
		}
		// don't play sounds too often or it runs out of memory. 
		return playSound(playSoundParams.soundFile[currentState-1]);
	}

	protected synchronized boolean playSound(File soundFile) {
		if (soundFile == null || soundFile.exists() == false) {
			return false;
		}
		if (preparedClip != null && preparedClip.isRunning()) {
			// get out if a clip is already running. 
			return true;
		}
		preparedClip = prepareClip(soundFile);
		if (preparedClip == null) {
			return false;
		}
		try {
			preparedClip.setFramePosition(0);
			preparedClip.start();
			li = preparedClip.getLineInfo();
			int fl = preparedClip.getFrameLength();
			float sr = preparedClip.getFormat().getSampleRate();
			double secs = fl/sr*1000;
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
		return true;
	}

	private Clip prepareClip(File soundFile) {
		if (soundFile == currentFile && currentClip != null) {
			return currentClip;
		}
		currentClip = null;
		currentFile = null;
		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
			currentClip = AudioSystem.getClip();
			currentClip.open(audioInputStream);
		} catch (UnsupportedAudioFileException f ) {
			System.out.println(f.getMessage());
			return currentClip = null;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return currentClip = null;
		} catch (LineUnavailableException e) {
			System.out.println(e.getMessage());
			return currentClip = null;
		}
		currentFile = soundFile;		

		return currentClip;
	}

	@Override
	public int canDo() {
		if (playSoundParams.soundFile == null) {
			return ALARM_CANT_DO;
		}
		else {
			return ALARM_CAN_DO;
		}
	}

	@Override
	public String getUnitName() {
		return alarmControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Play Sound Alarm Action";
	}

	@Override
	public Serializable getSettingsReference() {
		return playSoundParams;
	}

	@Override
	public long getSettingsVersion() {
		return PlaySoundParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		playSoundParams = ((PlaySoundParams) pamControlledUnitSettings.getSettings()).clone();
		return (playSoundParams != null);
	}

}
