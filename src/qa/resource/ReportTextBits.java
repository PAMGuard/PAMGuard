package qa.resource;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import qa.analyser.QATestAnalysis;

/**
 * Class of a load of static text to include in various parts of report. Note
 * that some parts may have format specifiers in which case they must be matched
 * in an appropriate string formatting command.
 * 
 * @author dg50
 *
 */
public class ReportTextBits {

	public static final String INTROTEXT = "The SIDE output provides detection performance as a function of range to estimate "
			+ "effective detection distances while operating under existing noise conditions. "
			+ "The SIDE does not replicate biological signals or the calling activity of any species. "
			+ "If the SIDE indicates that species cluster vocalizations can be detected by the PAM system, "
			+ "a range of biological and physical factors will affect actual PAM operator or detector performance. "
			+ "It is up to the operator to understand, and report, on factors that may be affecting their ability "
			+ "to monitor any regulatory zones; the SIDE output can be used to inform that understanding but should "
			+ "not be used as the only evaluation. \r\n"
			+ "The Quick Tests can be used to evaluate the PAM deployment and operational conditions prior to or "
			+ "periodically during monitoring; while the Random Drills should be initiated at the beginning of a monitoring "
			+ "period and used to evaluate detection performance at the end of the monitoring period. "
			+ "This SIDE report contains two types of evaluations for assessing detection efficacy. "
			+ "First, a detection table is provided that shows the percent "
			+ "detected injected signals or sequences by automated detectors and operators for each signal/sequence injected "
			+ "at various ranges.  " 
			+ "Second, detection "
			+ "regression plots (Figure 1) which summarize all injected signals or sequences by the number detected and "
			+ "missed by the auto detectors and/or operator. ";

	/**
	 * Text for sequences that only consist of single sounds
	 */
	public static final String SINGLESOUNDSEQUNCETXT = "The simulator for %s only produces single sounds.";

	/**
	 * Text for single sounds that are in sequences
	 */
//	public static final String SINGLESOUNDTXT = "The simulator for %s produces sounds in sequences of %d individual sounds. "
//			+ "This is either because individual animals are likely to produce sounds in sequences or because they "
//			+ "are likely to occur in larger groups of animals, meaning that many animals are likely to be vocalising. "
//			+ "For mitigation, the detection efficiency for individual sounds is less important than the detection "
//			+ "efficiency of sound sequences, since it is not necessary to detect every sound in order to know that animals are present.";

	public static final String SOUNDSEQUNCETXT = "The simulator for %s produces sounds in sequences of %d individual sounds. "
			+ "This is either because individual animals are likely to produce sounds in sequences or because they are likely "
			+ "to occur in larger groups of animals, meaning that many animals are likely to be vocalizing. For mitigation, "
			+ "it is often only necessary to detect one or more of the sounds produced in a sequence, so the detection efficiency "
			+ "for sound sequences will always be higher than the detection efficiency for single sounds. ";

	public static String HYDROPHONETXT = "Please note that hydrophone clipping levels will only be accurate if the "
			+ "correct hydrophone sensitivity data has been entered in the array manager and if the correct input range for the "
			+ "sound input system has been entered in the Sound Acquisition control.";

	public static final String getRegressionFailureText(String detType, String detectorName, QATestAnalysis analysis) {
		String st = String.format("Logistic regression failed for %s with %s detector. ", detType, detectorName);
		double hitMean = analysis.getHitMean();
		if (hitMean > 0.8 || hitMean < 0.2) {
			st += String.format(" This may be because detection efficiency averaged %d%% and did not vary sufficiently"
					+ " over the range of distances.", (int) (hitMean * 100.));
		} else {
			st += " It is possible that insufficient data are included for logistic regression analysis.";
		}
		return st;
	}
	
	public static final String weka1 = "Statistical analysis was conducted using the WEKA Data Mining Toolbox, developed by the "
			+ "University of Waikato, New Zealand.";

	public static final String weka2 = "Eibe Frank, Mark A. Hall, and Ian H. Witten (2016). "
	+ "The WEKA Workbench. Online Appendix for \"Data Mining: Practical Machine "
	+ "Learning Tools and Techniques\", Morgan Kaufmann, Fourth Edition, 2016.";

	public static BufferedImage loadImage(String file) {

		String path = ReportTextBits.class.getPackageName();
		path = path.replace(".", "/");
		// tried using File/pathSeparator, but it didn't work in installed version, so switch to /
		String fileName = path + "/" + file;
//		String fileName = file;
		/*
		 * Use classloader to get image out of Jar file.
		 */
		URL url = ClassLoader.getSystemResource(fileName);
		if (url == null) {
			System.out.println("Can't find image " + fileName);
			return null;
		}
		BufferedImage image = null;
		try {
			image = ImageIO.read(url);
		} catch (IOException e) {

		}
		return image;
	}
}
