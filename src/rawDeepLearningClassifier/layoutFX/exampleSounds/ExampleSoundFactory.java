package rawDeepLearningClassifier.layoutFX.exampleSounds;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors; 

import simulatedAcquisition.sounds.SimSignal;

/**
 * The example sound factory
 * @author Jamie Macaulay 
 *
 */
public class ExampleSoundFactory {
	
	
	
	/**
	 * An example sound type. 
	 * @author Jamie macaulay
	 *
	 */
	public enum ExampleSoundType {
		
		PORPOISE_CLICK("Harbour Porpoise (Phocoena phocoena)", ExampleSoundCategory.ODONTOCETES_CLICKS),

	    SPERM_WHALE("Sperm Whale (Physeter macrocephalus)", ExampleSoundCategory.ODONTOCETES_CLICKS),
	    
	    DOLPHIN("Dolphin (Delphinid)", ExampleSoundCategory.ODONTOCETES_CLICKS),
	    
	    BEAKED_WHALE("Beaked Whale (Ziphiidae)", ExampleSoundCategory.ODONTOCETES_CLICKS),
		
	    BAT_CALL("Bat Call (Myotis daubentonii)", ExampleSoundCategory.BAT),
		
	    RIGHT_WHALE("Southern Right Whale (Eubalaena australis)", ExampleSoundCategory.MYSTICETES),
		
		MINKE_WHALE("Minke Whale (Balaenoptera spp.)", ExampleSoundCategory.MYSTICETES),
				
		HUMPBACK_WHALE("Humpback whale (Megaptera novaeangliae) ", ExampleSoundCategory.MYSTICETES),

		BLUE_WHALE("Blue whale (Balaenoptera musculus) ", ExampleSoundCategory.MYSTICETES);

	    private final String text;
	    
		private final ExampleSoundCategory soundCategory;

	    /**
	     * @param text
	     */
	    ExampleSoundType(final String text, final ExampleSoundCategory soundCategory) {
	        this.text = text;
	        this.soundCategory = soundCategory; 
	    }

	    /* (non-Javadoc)
	     * @see java.lang.Enum#toString()
	     */
	    @Override
	    public String toString() {
	        return text;
	    }
	    
	    /**
	     * Get category of the example sound. 
	     * @return the example sound category. 
	     */
	    public ExampleSoundCategory getCategory() {
	    	return soundCategory; 
	    }
	}
	
	public enum ExampleSoundCategory {MYSTICETES, ODONTOCETES_CLICKS, OCONTOCETES_TONAL, BAT}

	/**
	 * Get all the example sounds for a particular category. 
	 * @param - the category of sund to extract. 
	 * @return the example sounds for a category. 
	 */
	private  List<ExampleSoundType> getAnExampleSoundTypes(ExampleSoundCategory category) {
		
		List<ExampleSoundType> exampleSounds = Arrays.asList(ExampleSoundType.values()); 
		
		List<ExampleSoundType> resultSet = 
				exampleSounds.stream() 
				  .filter(c -> c.getCategory().equals(category))
				  .collect(Collectors.toList());
		
		return resultSet;
		
	}

	/**
	 * Get all the example sounds for a set of categories. 
	 * @param - the categories to use. 
	 * @return the example sounds for a category. 
	 */
	public ArrayList<ExampleSoundType> getExampleSoundTypes(ExampleSoundCategory... cateogry) {
		
				
		ArrayList<ExampleSoundType> resultSet = new ArrayList<ExampleSoundType>(); 
		
		for (int i=0; i<cateogry.length; i++) {
				
			resultSet.addAll(getAnExampleSoundTypes(cateogry[i]));
		}
		
		return resultSet;
		
	}
	
	
	/**
	 *Beaked whale click template, sR is 192000; 
	 */
	public static final double[] beakedWhale1 = {2.47E-04,4.17E-04,-2.08E-04,-4.96E-04,2.10E-04,1.77E-04,-1.94E-04,4.12E-04,
			-3.52E-04,-4.06E-04,5.34E-04,1.78E-04,-1.57E-04,-3.78E-04,6.04E-04,2.92E-04,-1.11E-03,-9.88E-05,4.69E-04,4.78E-04,-2.56E-04,
			-6.52E-04,9.52E-04,7.76E-04,-1.24E-03,-2.34E-03,-7.72E-04,5.15E-03,9.90E-03,-7.51E-03,-2.34E-02,1.01E-02,2.53E-02,-1.19E-02,-1.14E-02,
			8.45E-03,4.52E-04,-7.41E-03,-3.62E-03,1.55E-02,7.40E-03,-1.44E-02,-2.38E-04,7.15E-04,-1.20E-02,3.95E-04,1.57E-02,4.98E-03,1.92E-03,
			1.54E-02,-2.55E-02,-5.13E-02,2.06E-02,4.93E-02,4.04E-03,1.07E-02,-4.28E-03,-6.29E-02,-2.66E-02,3.46E-02,3.53E-02,3.77E-02,2.10E-02,
			-5.72E-02,-8.11E-02,-2.50E-03,4.14E-02,7.01E-02,7.57E-02,-4.29E-02,-1.31E-01,-7.97E-02,4.48E-02,1.41E-01,1.17E-01,-3.82E-02,-1.92E-01,
			-1.16E-01,6.49E-02,1.70E-01,1.65E-01,-1.03E-01,-2.66E-01,-4.51E-02,1.66E-01,2.08E-01,9.35E-03,-2.98E-01,-1.26E-01,2.83E-01,1.67E-01,
			-1.97E-01,-2.05E-01,9.55E-02,2.64E-01,-3.56E-02,-2.88E-01,2.30E-02,2.49E-01,-2.65E-02,-1.77E-01,1.71E-02,1.22E-01,6.03E-03,-1.18E-01,
			1.24E-02,1.12E-01,-6.87E-02,-3.49E-02,6.68E-02,-3.83E-02,-1.76E-02,4.04E-02,4.81E-03,-4.35E-02,3.24E-03,5.49E-02,-3.68E-02,-1.93E-02,
			3.83E-02,-2.45E-02,-6.69E-03,3.95E-02,-1.97E-02,-3.61E-02,4.18E-02,3.72E-03,-3.75E-02,3.54E-02,-4.81E-03,-3.30E-02,3.52E-02,2.50E-03,
			-3.08E-02,1.80E-02,1.40E-02,-2.37E-02,4.51E-03,1.71E-02,-1.81E-02,-6.25E-04,1.83E-02,-1.33E-02,-9.61E-03,1.96E-02,-2.14E-03,
			-1.97E-02,1.43E-02,9.88E-03,-1.85E-02,5.62E-03,1.13E-02,-1.41E-02,-6.57E-04,1.33E-02,-6.73E-03,-7.21E-03,1.07E-02,-2.59E-03,
			-1.03E-02,1.08E-02,5.15E-03,-1.25E-02,7.48E-04,9.56E-03,-5.28E-03,-5.46E-03,7.89E-03,1.54E-04,-8.15E-03,5.09E-03,6.07E-03,-7.92E-03,
			-7.59E-04,8.78E-03,-5.57E-03,-5.39E-03,8.47E-03,-1.48E-03,-7.02E-03,5.20E-03,3.33E-03,-6.12E-03,8.16E-04,6.54E-03,-4.25E-03,-3.56E-03,
			4.57E-03,-6.18E-04,-1.87E-03,1.52E-03,3.46E-04};


	/**
	 * Dolphin click template. sR is 500000; 
	 */
	public static final double[] porp1 = {-3.05E-05,-0.002563477,-0.004974365,0.011444092,-0.009521484,-0.025115967,0.043365479,0.006896973,-0.090332031,0.045959473,
			0.112335205,-0.129669189,-0.099395752,0.208465576,0.036865234,-0.25994873,0.040771484,0.273834229,-0.121124268,-0.264709473,0.18447876,0.235626221,-0.241851807,
			-0.194458008,0.281036377,0.129699707,-0.306427002,-0.048278809,0.293182373,-0.043273926,-0.243774414,0.110229492,0.162536621,-0.144958496,-0.078491211,0.136444092,
			0.004547119,-0.105926514,0.037628174,0.058898926,-0.05682373,-0.018371582,0.047973633,-0.018737793,-0.024383545,0.03302002,-0.014404297,-0.030517578,0.045227051,
			0.002532959,-0.063751221,0.027191162,0.057067871,-0.054901123,-0.042449951,0.065246582,0.016448975,-0.06741333,0.002319336,0.055664063,-0.023345947,-0.040496826,
			0.030548096,0.015625,-0.033569336,0.002441406,0.022857666,-0.019042969,-0.013702393,0.020965576,0.001831055,-0.023101807,0.001068115,0.01739502,-0.005401611,
			-0.017944336,0.004394531,0.013427734,-0.008880615,-0.013122559,0.007781982,0.006561279,-0.010650635,-0.004150391,0.005310059};
	/**
	 * Dolphin click template. sR is 192000; 
	 */
	public static final double[] dolphin1 = {3.56E-05,1.13E-03,-1.28E-03,4.39E-05,1.17E-04,4.11E-04,-3.05E-04,1.57E-04,-3.45E-04,5.21E-04,-5.18E-04,
			1.93E-03,-8.44E-04,-1.62E-04,-8.80E-04,2.74E-04,-4.06E-04,1.32E-03,-7.23E-04,5.70E-04,-6.91E-04,6.78E-04,-7.04E-04,-1.36E-03,-4.10E-04,
			1.41E-03,-1.11E-03,1.36E-03,-9.02E-04,-2.69E-04,-1.49E-04,2.05E-04,-1.53E-04,6.97E-05,-7.78E-04,9.78E-04,-1.04E-03,-9.00E-05,-8.66E-04,
			6.78E-05,-1.06E-03,1.39E-03,-6.03E-04,3.49E-04,2.80E-04,9.42E-04,-3.65E-04,-5.98E-04,-1.91E-04,4.00E-04,-1.22E-03,4.52E-04,-1.35E-03,8.43E-04,
			-1.20E-03,5.83E-04,-3.36E-04,1.26E-03,-2.39E-04,1.63E-04,-1.31E-03,1.15E-03,9.60E-04,1.58E-03,-5.09E-04,1.16E-03,-1.15E-03,-2.38E-04,-3.77E-04,
			1.37E-04,-1.57E-03,1.30E-03,-9.01E-04,1.00E-03,-9.13E-06,8.95E-04,-9.26E-04,3.68E-04,-6.05E-05,5.16E-04,-2.56E-04,2.45E-03,-1.46E-03,1.80E-03,
			-5.76E-04,4.27E-03,4.60E-03,1.62E-02,2.77E-02,4.65E-02,-2.96E-02,-1.70E-01,-3.27E-01,1.98E-01,7.30E-01,-1.69E-01,-4.94E-01,3.59E-02,1.08E-01,
			2.70E-02,-9.51E-03,-2.52E-02,-1.19E-02,5.12E-03,3.54E-03,9.35E-03,6.57E-03,5.81E-03,-1.82E-03,-3.81E-03,-1.73E-02,-3.26E-03,1.66E-02,1.47E-02,
			-1.78E-04,-1.35E-02,-5.18E-03,5.96E-03,1.20E-03,-2.03E-03,8.27E-04,3.08E-03,7.34E-03,2.49E-03,-8.53E-03,-3.58E-03,-4.46E-03,2.39E-03,6.70E-03,
			2.08E-03,-1.54E-03,3.96E-05,-3.75E-04,4.47E-05,9.08E-04,1.13E-03,-4.51E-04,-1.16E-03,-2.38E-03,1.33E-03,4.09E-03,-4.09E-05,-2.57E-03,-7.47E-05,
			-4.94E-04,-1.12E-04,-2.10E-04,3.48E-04,-1.33E-04,-8.04E-04,-1.28E-04,2.31E-04,1.63E-03,4.06E-03,-1.90E-03,-4.81E-03,-3.43E-03,2.05E-03,2.64E-03,
			1.05E-03,-1.25E-03,-1.52E-03,-2.15E-04,5.58E-04,1.09E-03,-2.92E-04,-7.16E-05,-1.62E-04,-7.08E-04,1.37E-03,-3.63E-04,-1.01E-04,-9.10E-04,4.76E-04,
			-4.89E-04,6.00E-04,-1.97E-03,-3.72E-04,-1.23E-03,-2.13E-03,1.80E-03,1.72E-03,-4.45E-04,8.07E-04,2.73E-03,1.87E-03,-3.51E-04,-1.79E-03,
			-4.25E-05,-3.60E-04,-6.36E-03,-3.80E-03};
	/**
	 * Sperm whale click that contains P0 , P1 and P2 pulses. sR is 192000/4
	 */
	public static final double[] sperm1 = {6.25E-05,-2.17E-05,4.27E-05,-0.00020994,-0.000214738,4.15E-05,-4.20E-06,-1.19E-05,1.43E-05,-6.63E-06,-0.000131916,7.46E-05,7.74E-05,
			-3.48E-05,0.000112665,7.23E-05,0.000124127,-0.000327633,-0.000504408,-0.0001821,-9.08E-06,-0.000303635,-0.00091038,-0.000737628,-0.002809006,-0.002485561,0.002436103,
			0.001387111,-0.000240529,0.008338125,0.01556416,0.003837149,-0.012601288,-0.010037818,0.001317383,0.006350685,0.003097904,-0.003146625,-0.005176637,-0.005103367,
			-0.003033016,0.000264741,0.000979974,0.001417843,0.001295476,0.000270959,-0.000941692,-0.002497482,-0.001093983,0.001588331,0.001606697,0.000428882,0.000342588,
			0.000576856,0.000175455,0.000351797,0.000535963,6.16E-05,-2.81E-05,3.01E-05,-4.62E-05,6.29E-05,-0.00032481,-0.000627716,0.000541881,0.000930361,6.94E-05,-8.11E-05,
			-0.001084492,-0.001999887,-0.000853651,-0.000455461,-0.0009841,-0.000148444,0.000505996,0.000659125,0.002106154,0.00305439,0.002859567,0.002226094,-0.001580242,
			-0.005047821,-0.003593811,-0.002505516,-0.00239728,0.000993556,0.004712824,0.005116607,0.002759289,-0.000433693,-0.00259594,-0.002059373,6.38E-05,0.00080239,0.001226022,
			0.000524362,-5.97E-05,0.000218345,0.002725451,0.013643442,0.007104888,-0.013153579,-0.000327077,0.008434641,-0.034580464,-0.058165279,0.001699742,0.050940528,0.016095566,
			-0.019505718,-0.014421101,0.00069818,0.012322224,0.015961877,0.019905987,0.012646343,-0.003135543,-0.003774804,-0.002494694,-0.004462543,-2.78E-05,0.006201286,0.007524738,
			-0.000122701,-0.00686273,-0.003151422,0.000472041,-0.004336464,-0.006828523,-0.0036367,-0.004451807,-0.00476565,-0.00059454,0.001060291,0.000210872,0.000292835,0.001312147,
			0.00354525,0.003836739,-0.001563894,-0.003728106,0.000589295,0.002063008,0.005110769,0.008073922,0.002351888,0.000991397,0.004482293,0.001852083,-0.001210519,-0.003916204,
			-0.009643673,-0.011895388,-0.011094342,-0.009496773,0.007035453,0.021768597,0.012407682,0.007770304,0.009816115,-0.004929878,-0.02191959,-0.021845953,-0.009616377,0.003728446,
			0.011398892,0.007160923,-0.001221613,-0.003364027,-0.001979512,-0.00161046,-0.000138627,0.00464312,0.004116845,0.001597662,0.003842689,0.002166315,-0.001378553,-0.005946158,
			-0.011386535,-0.004845226,0.002418087,-0.003915996,-0.001246813,0.028316573,0.039767874,-0.002601435,-0.030996041,-0.012193441,0.000524819,-0.003479641,-0.001961131,0.005014191,
			0.000109726,-0.008264739,-0.003328171,-0.003266307,-0.005688887,0.002147438,0.005314515,0.003113408,0.000906803,-0.00362076,-0.004367909,0.001707293,0.004516978,0.002670098,
			0.005179915,0.005209394,0.000157009,-0.001113109,0.000351461,-0.00070744,-0.002641287,-0.000378381,0.000650114,-0.000699954,0.001493645,0.002380219,0.001847398,0.000957028,
			-0.00020906,-0.001138818,-0.005258351,-0.004390589,-0.000750034,0.001350919,0.005041997,0.004159317,0.00380484,0.006116095,0.004054061,-0.007240427,-0.015931238,-0.008497872,
			-0.004869947,-0.005248341,0.005820257,0.017324379,0.015179912,0.001501706,-0.004758621,-0.004807403,-0.004766533,0.000532342,0.001876597,0.002421249,0.002974527,-0.003363805,
			-0.006394844,-0.004090489,-0.000551194,0.000815621,0.002722833,0.006326427,0.002422239,1.71E-05,0.001718135,0.000155882,-9.98E-06,0.000374249,0.000685689,-0.00060601,
			-0.002945803,-0.001182812,0.000705219,0.000131511,-0.001627181,-0.004276435,-0.004121385,-0.001013222,-0.000580473,-0.001749975,-0.000810329,0.001049795,0.002380673,
			0.00092144,-0.001721435,-0.001959407,0.001039882,0.003527076,0.001077526,0.001321712,0.003990982,0.00159095,-6.99E-05,0.000197319,0.000611932,0.000414778,-0.000977586,
			0.000546997,-0.00015996,-0.003209788,-0.002958488,-0.001050216,0.000691699,-0.002462978,-0.00411392,-0.001070453,-0.002638013};

//	/**
//	 * Generate a click waveform with some added noise.
//	 * @param type - the type of click e.g. ClickTriggerGraph.PORPOISE_CLICK.
//	 * @param noise. 0  to 1. 1 means max noise amplitude will be same as maximum click amplitude. 
//	 * @return click and noise waveform. 
//	 */
//	private double[] generateClickWaveform(ExampleSoundType type, double noise){
//		SimSignal clickSound;
//		int sR;
//		double length;
//		double freq;
//		switch (type){
//		//TODO - add more types of clicks. 
//		case PORPOISE_CLICK:
//			clickSound=new ClickSound("Porpoise", freq=140000, freq2=140000, length=0.00015, WINDOWTYPE.HANN);
//			sR=500000;
//			break;
//		case SPERM_WHALE:
//			clickSound=(new ClickSound("Beaked Whale", 30000, 60000, length = 0.3e-3, WINDOWTYPE.HANN));
//			sR=192000;
//			break;
//		default:
//			clickSound=new ClickSound("Porpoise", freq=140000, freq2=140000, length=0.00015, WINDOWTYPE.HANN);
//			sR=500000;
//			break;
//		}
//	
//		//now need to work out how many noise samples to add. Use the length of the click 
//		int nNoiseSamples=(int) (2*length*sR);
//		double[] waveform=new double[3*nNoiseSamples];
//		int n=0;
//		for (int i=0; i<3*nNoiseSamples; i++){
//			double noiseSample=noise*(Math.random()-0.5);
//			double[] signal=clickSound.getSignal(0,sR,0);
//			if (i>nNoiseSamples && n <signal.length){
//				waveform[i]=signal[n]+noiseSample;
//				n=n+1;
//			}
//			else waveform[i]=noiseSample;
//		}
//		
//		return waveform;
//	}

	/**
	 * Get the example sound type. 
	 * @param exampleSoundType
	 * @return
	 */
	public ExampleSound getExampleSound(ExampleSoundType exampleSoundType) {
		ExampleSound exampleSound = null; 
		URL path; 
		//File file; 
		switch (exampleSoundType) {
		case BAT_CALL:
			path = getClass().getResource("/Resources/exampleSounds/DUB_20200623_000152_885.wav"); 
			exampleSound = new SimpleExampleSound(path, 2500, 5000); 
			break;
		case RIGHT_WHALE:
			 //file = new File("src/rawDeepLearningClassifier/layoutFX/exampleSounds/southern_right_whale_clip2.wav"); 
			path = getClass().getResource("/Resources/exampleSounds/southern_right_whale_clip2.wav"); 
			exampleSound = new SimpleExampleSound(path); 
			break;
		case MINKE_WHALE:
			 //file = new File("src/rawDeepLearningClassifier/layoutFX/exampleSounds/southern_right_whale_clip2.wav"); 
			path = getClass().getResource("/Resources/exampleSounds/Minke_whale.wav"); 
			exampleSound = new SimpleExampleSound(path); 
			break;
		case HUMPBACK_WHALE:
			 //file = new File("src/rawDeepLearningClassifier/layoutFX/exampleSounds/southern_right_whale_clip2.wav"); 
			path = getClass().getResource("/Resources/exampleSounds/Humpback_whale.wav"); 
			exampleSound = new SimpleExampleSound(path); 
			break;
		case PORPOISE_CLICK:
			exampleSound = new SimpleExampleSound(porp1, 500000); 
			break;
		case SPERM_WHALE:
			exampleSound = new SimpleExampleSound(sperm1, 48000); 
			break;
		case BEAKED_WHALE:
			exampleSound = new SimpleExampleSound(beakedWhale1, 192000); 
			break;
		case DOLPHIN:
			exampleSound = new SimpleExampleSound(dolphin1, 192000); 
			break;
		case BLUE_WHALE:
			path = getClass().getResource("/Resources/exampleSounds/Blue_whale.wav"); 
			exampleSound = new SimpleExampleSound(path); 			
			break;
		default:
			break;


		}
		return exampleSound; 
	}

	/**
	 * Get all the example sounds for a particular category. 
	 * @param - the category of sund to extract. 
	 * @return the example sounds for a category. 
	 */
	public  ExampleSoundType[] getExampleSoundTypes(ExampleSoundCategory category) {
		
		
		List<ExampleSoundType> exampleSounds = Arrays.asList(ExampleSoundType.values()); 
		
		List<ExampleSoundType> resultSet = 
				exampleSounds.stream() 
				  .filter(c -> c.getCategory().equals(category))
				  .collect(Collectors.toList());
		
		return (ExampleSoundType[]) resultSet.toArray();
		
	}
	

}
