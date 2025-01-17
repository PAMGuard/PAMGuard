package tipOfTheDay;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Random;

/**
 * 
 * @author Doug Gillespie
 * Tip of the day manager. Sets up tips and controls their selection
 * and display<p>
 * Tips must currently be typed into the constructor, so that they are
 * always there. The order of tips is randomly set but then remains fixed. 
 * <p>When you start looking at tips, the first one shown is selected at
 * random. 
 *
 */
public class TipOfTheDayManager {

	private ArrayList<PamTip> tips = new ArrayList<PamTip>();
	
	private static TipOfTheDayManager singleInstance;
	
	private Random random = new Random();
	
	private boolean showAtStart = true;
	
	private TipOfTheDayManager() {
		addTipRandom(new PamTip("NMEA Acquisition", "If your computer does not have a serial port, " +
				"use a serial to USB converter", "NMEA Acquisition", null));
//		addTipRandom(new PamTip("PAMGuard Viewer", "A new scroll system was implemented in version 1.10.00 " +
//				"(Novemeber 2010). See the PAMGuard help for details"));
//		addTipRandom(new PamTip("Binary Storage", "A new binary data storage system is now in place. This can " +
//				"be used with or without the database"));
		addTipRandom(new PamTip("Sound Acquisition", "For high sample rates (above 192 kHz) " +
				"use National instruments data acquisition boards"));
		addTipRandom(new PamTip("Sound Acquisition", "Multiple National Instruments cards can use a " +
				"synchronised clock signal enabling you to use multiple cards for high channel numbers and " +
				"sample rates"));
		addTipRandom(new PamTip("Distance Sampling", "Did you know that some of the statistics " +
				"quoted in the Distance software are made up"));
		addTipRandom(new PamTip("Coordinate System", "We live in three dimensions: Space, time and doubt. " +
				"Of the three doubt is the most certain."));
		addTipRandom(new PamTip("Map Comments", "Map Comments are a good way of keeping track of " +
				"recent detections and other activity"));
		addTipRandom(new PamTip("Seismic Veto", "If you want to listen to or analyse sounds " +
				"without airguns, you can use the seismic veto module to cut out the airguns"));
		addTipRandom(new PamTip("Sound Acquisition", "The sound acquisition module works with many " +
				"types of input device and can also be used to analyse archived data from files"));
		addTipRandom(new PamTip("Source Code", "Did you know that all the PAMGuard source code " +
				"is available on GitHub at https://github.com/PAMGuard. If you don't like something, you might be able to modify it yourself"));
		addTipRandom(new PamTip("Feedback", "The development team love feedback. Please send us an email " +
				"when things work as well as when they go wrong"));
		addTipRandom(new PamTip("Support", "The PAMGuard team can help you. \nSee www.pamguard.org " +
				"for details "));
//		addTipRandom(new PamTip("Windows Vista", "PAMGuard may only run properly on Windows Vista " +
//				"if you have logged on with administrator privilidges"));
		addTipRandom(new PamTip("Time zones", "All PAMGuard data are collected in UTC (Greenwich Mean Time)"));
//		addTipRandom(new PamTip("PC Clock updates", "PAMGuard can set your PC clock based on accurate times from a GPS"));
		addTipRandom(new PamTip("AIS", "You can view the positions of other ships in the area using an AIS receiver and "+ 
				"the PAMGuard AIS processing module"));
		addTipRandom(new PamTip("Airgun display", "Used in conjunction with an AIS module, the positions of airguns can " +
				"be displayed even if they are on a different vessel"));
		addTipRandom(new PamTip("Static hydrophone arrays", "PAMGuard can work with static as well as towed array data"));
		addTipRandom(new PamTip("Online help", "All PAMGuard modules in the Core release are documented in the online help (Nearly)"));
		addTipRandom(new PamTip("Home page", "The PAMGuard home page is at www.pamguard.org"));
//		addTipRandom(new PamTip("Click Detector", "The click detector can output files which are compatible with the IFAW " +
//				"RainbowClick software, but only if channels are arranged into groups of exactly two channels per group"));
		addTipRandom(new PamTip("Right Click", "Click with the right mouse button on many PAMGuard displays to bring " +
				"up lists of options"));
		addTipRandom(new PamTip("Multiple Displays", "Right click on a tab on the main display and use the menu options " +
				"to move display components to new windows"));
		addTipRandom(new PamTip("Copying", "You can copy many PAMGuard displays to the system clipboard by right clicking on " +
				"the tab buttons on the main display\n" +
				"You can then paste the graphics straight into your preferred " +
				"word processing software"));
		addTipRandom(new PamTip("Top Tips", "If you can think of any Top Tips to go in here, please " +
				"email them to support@pamguard.org"));
		addTipRandom(new PamTip("Unknown unknowns", "Unknown unknowns are things we don't know that we don't know"));
		addTipRandom(new PamTip("Known unknowns", "Known unknowns are things we know that we don't know"));
		addTipRandom(new PamTip("Map Display", "You can use the scroll wheel on the mouse to zoom the map in and out"));
		addTipRandom(new PamTip("Bug reporting", "We can only fix them if we know about them !\n" +
				"Please email bug reports to support@pamguard.org"));
//		addTipRandom(new PamTip("User email list", "Details of the PAMGuard users email list " +
//				"can be found on the web at www.pamguard.org/contact.php"));
//		addTipRandom(new PamTip("Developer email list", "Details of the PAMGuard developers email list " +
//		"can be found on the web at www.pamguard.org/contact.shtml"));
		addTipRandom(new PamTip("Visual data", "PAMGuard is not just for acoustic processing. " +
				"Check out the Logger forms and the Video Range tracking modules."));
		addTipRandom(new PamTip("We are on Facebook", "Check out the PAMGuard Facebook page at www.facebook.com/pamguard"));
		addTipRandom(new PamTip("We are on blueSky", "PAMGuard posts on BlueSky. Check out https://bsky.app/profile/pamguard.org"));
		addTipRandom(new PamTip("64 bit processing", "PAMGuard now only works with 64 bit processors"));
		addTipRandom(new PamTip("Clip Generator", "Use the clip generator module to create short waveform clips from detection data"));
		addTipRandom(new PamTip("Microsoft Access", "PAMGuard no longer works with Microsoft Access databases. Use sqlite instead"));
		addTipRandom(new PamTip("Plugin Modules", "Several usefual plugin modules are also available at www.pamguard.org to add you your system"));
		addTipRandom(new PamTip("Deep Learning", "PAMGuards Deep Learning classifier allows you to run AI models within PAMGuard"));
	}
	
	public void addTipRandom(PamTip pamTip) {
//		addTip(pamTip);
		int nTips = tips.size();
		if (nTips == 0) {
			tips.add(pamTip);
		}
		else {
			int iTip = random.nextInt(nTips);
			tips.add(iTip, pamTip);
		}
	}
	
	public void addTip(PamTip pamTip) {
		tips.add(pamTip);
	}
	
	public static TipOfTheDayManager getInstance() {
		if (singleInstance == null) {
			singleInstance = new TipOfTheDayManager();
		}
		return singleInstance;
	}
	
	public PamTip getRandomTip() {
		int nTips = tips.size();
		int iTip = random.nextInt(nTips);
		return tips.get(iTip);
	}
	
	public PamTip getNextTip(PamTip pamTip) {
		int iTip = tips.indexOf(pamTip);
		return getNextTip(iTip);
	}
	
	public PamTip getNextTip(int iTip) {
		iTip++;
		if (iTip < 0 || iTip == tips.size()) {
			iTip = 0;
		}
		return tips.get(iTip);
	}
	
	public PamTip getPrevTip(PamTip pamTip) {
		int iTip = tips.indexOf(pamTip);
		return getPrevTip(iTip);
	}
	
	public PamTip getPrevTip(int iTip) {
		iTip--;
		if (iTip < 0) {
			iTip = tips.size()-1;
		}
		return tips.get(iTip);
	}
	
	public void showTip(Window window, PamTip pamTip) {
		if (pamTip == null) {
			pamTip = getRandomTip();
		}
		PamTipViewer.showTip(this, window, pamTip);
	}

	public boolean isShowAtStart() {
		return showAtStart;
	}

	public void setShowAtStart(boolean showAtStart) {
		this.showAtStart = showAtStart;
	}
}
