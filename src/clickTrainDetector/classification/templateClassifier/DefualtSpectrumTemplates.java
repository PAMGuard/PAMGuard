package clickTrainDetector.classification.templateClassifier;

import matchedTemplateClassifer.MatchTemplate;

/**
 * Default templates for the template click train classifier. 
 * 
 * @author Jamie Macaulay. 
 *
 */
public class DefualtSpectrumTemplates {

	/**
	 * The default spectrum templates. 
	 * 
	 * @author Jamie Macaulay 
	 */
	public enum SpectrumTemplateType {SPERM_WHALE, DOLPHIN, BEAKED_WHALE, PORPOISE, BOAT}
	
	//TODO-> need real values for these 
	private static double[] spermwhale = new double[] {0.030901961,0.081149445,0.190798778,	0.239713551,	0.304066423	,0.311822261,	0.303934981,	0.314388875,	0.297805671,	0.278023132,	0.280448443,	0.257173567,
			0.240468714,	0.245577016	,0.240430021,	0.231210681,	0.21982088,	0.214370437,	0.207828762,	0.206771018,	0.205857015,	0.206307366,	0.20221785,	0.177000068,	0.16411788,	0.157266143,	0.146108366,	0.129404732,	0.101310049	,0.080081854,
			0.054932378,	0.035486727,	0.021931402,	0.012926236,	0.008340848,	0.005731914,	0.004605861,	0.003710514,	0.003206338,	0.002803686,	0.002494898,	0.002333622,	0.002183665,	0.002011805,	0.00194592,	0.001844656,	0.00178753,	0.001628641,
			0.001598579,	0.001594464,	0.001505428,	0.00150532,	0.001432374,	0.001408597,	0.001437553,	0.001310724,	0.001290281,	0.001306876,	0.001273406,	0.001286941,	0.001338127,	0.001252238,	0.001309146,	0.001252138};
	private static float spermwhaleSR = 96000; 
	
	//broadband dolphin clicks
	private static double[] dolphin = new double[] { 0.0, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}; //each bin is 25kHz (stop at harbour porpoise 125kHz..?)
	private static float dolphinSR = 500000; 
	
	//beaked whale click
	private static double[] beakedwhale = new double[] {0.0207928796815748	,0.0306907634391936,	0.0542618013334441,	0.0927715736291923,	0.160880226335102,	0.296684784810738,	0.597646428735672,	1.30240513409102,	2.89418728104064,	5.90182387336775,	9.56798776063848,
			10.8497298549224,	10.6268357383588,	7.67719642764775,	4.25588468454799,	2.03543953486809,	0.944338665649875,	0.464770071613377,	0.254353569529111,	0.155756953724082,	0.105040575926229,	0.0764551025180798,	0.0590657823674759,	0.0478494061986734,	
			0.0403052031330920,	0.0350966305067761,	0.0314672978023124,	0.0289713012337297,	0.0273407573040125,	0.0264177207215999}; 
	private static float beakedwhaleSR = 192000; 

	//narrow band high frequency click 
	private static double[] porpoise = new double[] {0.102844238281250,	0.00580524415291035,	0.00110545814330225,	0.00184884794594179,	0.000833269857387748,	0.00382978884197165,	0.00390733439195732,	0.00200338485951422,	0.00184446885657806,	0.00208379827858544,
			0.00485698925561781,	0.00607194025341251,	0.00650667768760067,	0.00706435719264119,	0.0145330718168689,	0.0178685400164981,	0.0234054788891583,	0.0289703021081984,	0.0359251221278488,	0.283646175609846,	0.710789323088391,	1.47830030892775,	3.02470284166370,
			3.37076877245164,	3.65735588085117,	2.30754118439244,	0.0581523677331848,	0.374925589991897,	0.347216770982414,	0.254526652681659,	0.0260919968023360,	0.0394119036558762,	0.0168205186824498,	0.0122239160635397,	0.0107193042203834,	0.0156235401750237,	0.0109753178566950,
			0.00407794256237427,	0.00719946536476264,	0.00133881301135877,	0.00472039362015127,	0.00783121545006981};
	private static float porpoiseSR = 500000; 
	
	// boat engine is low frequency 
	private static double[] boat = new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.66, 0.33, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}; //each bin is 25kHz (stop at harbour porpoise 125kHz..?)
	private static float boatSR = 96000; 


	/**
	 * Get a default spectrum template.. 
	 * @param templateType - the template type/
	 * @return the default spectrum tem plate.  
	 */
	public static MatchTemplate getTemplate(SpectrumTemplateType templateType) {
		switch (templateType) {
		case SPERM_WHALE:
			return new MatchTemplate("Sperm Whale", spermwhale, spermwhaleSR);
		case DOLPHIN:
			return new MatchTemplate("Broadband Dolphin", dolphin, dolphinSR);
		case PORPOISE:
			return new MatchTemplate("NBHF", porpoise, porpoiseSR);
		case BEAKED_WHALE:
			return new MatchTemplate("Beaked Whale", beakedwhale, beakedwhaleSR);
		case BOAT:
			return new MatchTemplate("Boat", boat, boatSR);	
		default:
			break;
		}
		return null;
	}

	/**
	 * Spectrum templates. 
	 * @return default templates. 
	 */
	public static MatchTemplate[] getDefaultTemplates() {
		SpectrumTemplateType[] spectrumTemplateTypes = SpectrumTemplateType.values();
		MatchTemplate[] spectrumTemplates = new MatchTemplate[spectrumTemplateTypes.length];
		for (int i=0; i< spectrumTemplates.length; i++) {
			spectrumTemplates[i]=getTemplate(spectrumTemplateTypes[i]);
		}
		return spectrumTemplates;
	}

}
