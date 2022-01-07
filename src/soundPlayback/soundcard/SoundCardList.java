package soundPlayback.soundcard;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer.Info;

import PamUtils.PamCalendar;

public class SoundCardList {

	public static void main(String[] args) {

		SoundCardList scl = new SoundCardList();
		scl.run();
	}

	private void run() {
		while (true) {
			sayMixerInfo();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private int sayMixerInfo() {
		long t1 = System.nanoTime();
		Info[] mixerInfos = AudioSystem.getMixerInfo();
		long tt = System.nanoTime()-t1;
		int n = mixerInfos.length;
		System.out.printf("\n\n%d mixers found on system at %s in %3.4f millis\n", n, 
				PamCalendar.formatTime(System.currentTimeMillis()), (double)tt/1.e6);
		for (int i = 0; i < n ; i++) {
			String name = mixerInfos[i].getName();
//			if (name.contains("RX-TX"))
			System.out.printf("Mixer info %d Name \"%s\" Desc \"%s\" Class \"%s\"\n", i, 
					mixerInfos[i].getName(), mixerInfos[i].getDescription(), mixerInfos[i].getClass().getSimpleName());
		}
		return n;
	}

}
