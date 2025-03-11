package tethys.pamdata;

import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import Localiser.LocalisationAlgorithm;
import Localiser.LocalisationAlgorithmInfo;
import PamController.PamControlledUnit;
import PamController.PamSettings;
import PamDetection.LocalisationInfo;
import PamUtils.PamUtils;
import PamUtils.XMLUtils;
import PamguardMVC.DataAutomationInfo;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.TFContourData;
import PamguardMVC.TFContourProvider;
import PamguardMVC.superdet.SuperDetection;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import annotation.xml.AnnotationXMLWriter;
import binaryFileStorage.DataUnitFileInformation;
import nilus.AlgorithmType;
import nilus.Deployment;
import nilus.DescriptionType;
import nilus.Detection;
import nilus.Detection.Parameters;
import nilus.Detection.Parameters.UserDefined;
import nilus.DetectionEffortKind;
import nilus.GranularityEnumType;
import nilus.Helper;
import nilus.MarshalXML;
import nilus.SpeciesIDType;
import tethys.Collection;
import tethys.TethysControl;
import tethys.TethysTimeFuncs;
import tethys.detection.DetectionsHandler;
import tethys.localization.TethysLocalisationInfo;
import tethys.niluswraps.PDeployment;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.DataBlockSpeciesMap;
import tethys.species.ITISTypes;
import tethys.species.SpeciesMapItem;
import tethys.swing.export.ExportWizardCard;
import whistleClassifier.WhistleContour;

/**
 * Automatically provides Tethys data for a PAMGuard datablock.
 * Does most of what needs to be done, though individual modules
 * will want to override this, call the base createDetection function and then add a
 * few more bespoke elements.
 * @author dg50
 *
 */
abstract public class AutoTethysProvider implements TethysDataProvider {

	private PamDataBlock pamDataBlock;
	private PamProcess pamProcess;
	private PamControlledUnit pamControlledUnit;
	private TethysControl tethysControl;
	private Helper helper;
	private boolean addFrequencyInfo  = false;

	private MarshalXML marshaller;

	public AutoTethysProvider(TethysControl tethysControl, PamDataBlock pamDataBlock) {
		this.tethysControl = tethysControl;
		this.pamDataBlock = pamDataBlock;
		pamProcess = pamDataBlock.getParentProcess();
		pamControlledUnit = pamProcess.getPamControlledUnit();
		try {
			helper = new Helper();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		try {
			marshaller = new MarshalXML();
		} catch (JAXBException e) {
		}
	}

	@Override
	public DescriptionType getDescription(Deployment deployment, TethysExportParams tethysExportParams) {
		DescriptionType description = new DescriptionType();
		String fullUnitName = pamControlledUnit.getUnitType() + " " + pamControlledUnit.getUnitName();
		description.setAbstract(fullUnitName);
		description.setObjectives(fullUnitName);
		description.setMethod(pamDataBlock.getLongDataName());

		return description;
	}

	@Override
	public AlgorithmType getAlgorithm(Collection collection) {
		/**
		 * Probably need to split this to provide detection algorithm parameters and
		 * localisation algorithm parameters, or pass in the document type as a function
		 * argument.
		 */
		AlgorithmType algorithm = new AlgorithmType();
		try {
			nilus.Helper.createRequiredElements(algorithm);
		} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
		}
		// do the parameters as normal whether it's dets or locs.
		nilus.AlgorithmType.Parameters algoParameters = this.getAlgorithmParameters();
		if (algoParameters != null) {
			algorithm.setParameters(algoParameters);
		}
		if (collection == Collection.Localizations) {
			nilus.AlgorithmType.Parameters locParameters = this.getLocalisationParameters();
			if (locParameters == null) {
				/*
				 * It seems Tethys MUST have parameters, so make an empty one if needed.
				 */
				locParameters = new nilus.AlgorithmType.Parameters();
			}
			if (algoParameters == null) {
				algorithm.setParameters(locParameters);
			}
			else if (locParameters != null) {
				// merge the two sets, putting the localisation information first.
				List<Element> mainList = algoParameters.getAny();
				List<Element> locList = locParameters.getAny();
				if (mainList != null && locList != null) {
					for (int i = 0; i < locList.size(); i++) {
						mainList.add(i, locList.get(i));
					}
				}
			}
		}
		else {
		}

		return algorithm;
	}

	/**
	 * Localisation parameters. Some localisers don't actually have any parameters,
	 * but Tethys requires a parameters element, so if there aren't any, set a dummy
	 * @return
	 */
	public nilus.AlgorithmType.Parameters getLocalisationParameters() {
		LocalisationAlgorithm algo = pamDataBlock.getLocalisationAlgorithm();
		if (algo == null) {
			return null;
		}
		LocalisationAlgorithmInfo algoInfo = algo.getAlgorithmInfo();
		if (algoInfo == null) {
			return null;
		}
		Object params = algoInfo.getParameters();
		if (params == null) {
			return null;
		}
		// pack the params object
		TethysParameterPacker paramPacker = null;
		try {
			paramPacker = new TethysParameterPacker(tethysControl);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		Element paramEl = paramPacker.packObject(params, "localizer");
		if (paramEl == null) {
			return null;
		}
		nilus.AlgorithmType.Parameters parameters = new nilus.AlgorithmType.Parameters();
		List<Element> paramList = parameters.getAny();
		paramList.add(paramEl);

		return parameters;
	}

	@Override
	public nilus.AlgorithmType.Parameters getAlgorithmParameters() {
		if (!(pamControlledUnit instanceof PamSettings)) {
			return null;
		}
		PamSettings pamSettings = (PamSettings) pamControlledUnit;
		nilus.AlgorithmType.Parameters parameters = new nilus.AlgorithmType.Parameters();
		List<Element> paramList = parameters.getAny();
		Object settings = pamSettings.getSettingsReference();
		TethysParameterPacker paramPacker = null;
		try {
			paramPacker = new TethysParameterPacker(tethysControl);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		List<Element> genList = paramPacker.packParameters(pamDataBlock);
		if (genList == null || genList.size() == 0) {
			return null;
		}
		paramList.addAll(genList);

		return parameters;
	}

	/**
	 * Not used. Was an attempt to automatically add name spaces to the PAMGuard settings
	 * XML I generate, but we found a better way.
	 * @param doc
	 * @param settingsEl
	 * @param xmlNameSpace
	 * @return
	 */
	private Element addNameSpaceToElements(Document doc, Element settingsEl, String xmlNameSpace) {


		//		String xsltString = "<xsl:stylesheet version=\"1.0\" \r\n"
		//				+ "  xmlns:xsl=http://www.w3.org/1999/XSL/Transform\r\n"
		//				+ "  xmlns:ns0=http://mydata.com/H2H/Automation\r\n"
		//				+ "  exclude-result-prefixes=\"ns0\">\r\n"
		//				+ "  <xsl:output method=\"xml\" version=\"1.0\" encoding=\"UTF-8\" indent=\"yes\"/>\r\n"
		//				+ "  <xsl:strip-space elements=\"*\"/>\r\n"
		//				+ "  \r\n"
		//				+ "  <xsl:template match=\"*\">\r\n"
		//				+ "    <xsl:element name=\"{local-name()}\" namespace=http://tethys.sdsu.edu/schema/1.0>\r\n"
		//				+ "      <xsl:apply-templates/>\r\n"
		//				+ "    </xsl:element>\r\n"
		//				+ "  </xsl:template>\r\n"
		//				+ "  \r\n"
		//				+ "  <xsl:template match=\"/ns0:Document\">\r\n"
		//				+ "    <Document xmlns=http://tethys.sdsu.edu/schema/1.0 xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance>  \r\n"
		//				+ "      <xsl:apply-templates/>\r\n"
		//				+ "    </Document>\r\n"
		//				+ "  </xsl:template>\r\n"
		//				+ "  \r\n"
		//				+ "</xsl:stylesheet>\r\n";
		String xsltString = "<xsl:stylesheet version=\"1.0\" \n"
				+ "  xmlns:xsl=http://www.w3.org/1999/XSL/Transform\n"
				+ "  xmlns:ns0=http://mydata.com/H2H/Automation\n"
				+ "  exclude-result-prefixes=\"ns0\">\n"
				+ "  <xsl:output method=\"xml\" version=\"1.0\" encoding=\"UTF-8\" indent=\"yes\"/>\n"
				+ "  <xsl:strip-space elements=\"*\"/>\n"
				+ "  \n"
				+ "  <xsl:template match=\"*\">\n"
				+ "    <xsl:element name=\"{local-name()}\" namespace=http://tethys.sdsu.edu/schema/1.0>\n"
				+ "      <xsl:apply-templates/>\n"
				+ "    </xsl:element>\n"
				+ "  </xsl:template>\n"
				+ "  \n"
				+ "  <xsl:template match=\"/ns0:Document\">\n"
				+ "    <Document xmlns=http://tethys.sdsu.edu/schema/1.0 xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance>  \n"
				+ "      <xsl:apply-templates/>\n"
				+ "    </Document>\n"
				+ "  </xsl:template>\n"
				+ "  \n"
				+ "</xsl:stylesheet>\n";
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			//			Source xslt = new StreamSource(new File("transform.xslt"));
			StringReader reader = new StringReader(xmlNameSpace);
			Source xslt = new StreamSource(reader);

			Transformer transformer = factory.newTransformer(xslt);

			DOMSource source = new DOMSource(doc);

			//            Result
			//			Source text = new StreamSource(new File("input.xml"));
			DOMResult result = new DOMResult();
			transformer.transform(source, result);

			System.out.println(result.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private PamSettings[] getSettingsObjects() {
		if (pamControlledUnit instanceof PamSettings) {
			PamSettings[] settings = new PamSettings[1];
			settings[0] = (PamSettings) pamControlledUnit;
			return settings;
		}
		return null;
	}

	/**
	 * Algorithm method. Default is the module name. Can change to a paper citation
	 * by overriding this
	 * @return
	 */
	private String getAlgorithmMethod() {
		return pamControlledUnit.getUnitType();
	}

	@Override
	public Detection createDetection(PamDataUnit dataUnit, TethysExportParams tethysExportParams,
			StreamExportParams streamExportParams) {
		Detection detection = new Detection();
		detection.setStart(TethysTimeFuncs.xmlGregCalFromMillis(dataUnit.getTimeMilliseconds()));
		detection.setEnd(TethysTimeFuncs.xmlGregCalFromMillis(dataUnit.getEndTimeInMilliseconds()));
		if (dataUnit.getEndTimeInMilliseconds() < dataUnit.getTimeMilliseconds()) {
			System.out.printf("Error UID %d, end %s before start %s\n", dataUnit.getUID(), detection.getEnd(), detection.getStart());
		}

		DataBlockSpeciesManager speciesManager = pamDataBlock.getDatablockSpeciesManager();
		SpeciesMapItem speciesItem = null;
		if (speciesManager != null) {
			speciesItem = speciesManager.getSpeciesItem(dataUnit);
			//			detection.setSpeciesId(new Species);
			//			detection.setSpeciesId(getSpeciesIdType());
		}
		else {
		}
		SpeciesIDType species = new SpeciesIDType();
		List<String> calls = detection.getCall();
		if (speciesItem != null) {
			species.setValue(BigInteger.valueOf(speciesItem.getItisCode()));
			if (speciesItem.getCallType() != null) {
				calls.add(speciesItem.getCallType());
			}
		}
		else {
			species.setValue(BigInteger.valueOf(ITISTypes.ANTHROPOGENIC));
			calls.add("unknown");
		}
		detection.setSpeciesId(species);
		/*
		 * NOTE: I use channel bitmaps throughout since detections are often made on multiple channels.
		 * However, this doesn't fit with The Tethys Standard, so am going to put the first channel
		 * in here and add the channel map as an additional parameter. 
		 */
		int firstChannel = PamUtils.getLowestChannel(dataUnit.getChannelBitmap());
		detection.setChannel(BigInteger.valueOf(firstChannel));

		nilus.Detection.Parameters detParams = new nilus.Detection.Parameters();
		detection.setParameters(detParams);
		if (addFrequencyInfo) {
			/**
			 * Don't add by default.
			 */
			double[] freqs = dataUnit.getFrequency();
			if (freqs != null && freqs[1] != 0) {
				detParams.setMinFreqHz(freqs[0]);
				detParams.setMaxFreqHz(freqs[1]);
			}
		}
		double ampli = dataUnit.getAmplitudeDB();
		if (ampli != 0) {
			ampli = roundDecimalPlaces(ampli, 1);
			detParams.setReceivedLevelDB(ampli);
		}
		// if there is a super detection, set the EventRef field in the parameters
		SuperDetection superDet = dataUnit.getSuperDetection(0);
		if (superDet != null) {
			List<String> evRefs = detParams.getEventRef();
			String evT = TethysTimeFuncs.xmlGregCalFromMillis(superDet.getTimeMilliseconds()).toString(); 
//			evRefs.add(evT);
			String uidS = String.format("%s;evT;UID:%d", superDet.getParentDataBlock().getLongDataName(), superDet.getUID());
			evRefs.add(uidS);
		}
		
		//		DataUnitBaseData basicData = dataUnit.getBasicData();
		gotTonalContour(dataUnit, detParams);
		
		// if it's a super detection, fill in the count
		if (dataUnit instanceof SuperDetection) {
			superDet = (SuperDetection<?>) dataUnit;
			int n = superDet.getSubDetectionsCount();
			detection.setCount(BigInteger.valueOf(n));
		}

		String uid = BigInteger.valueOf(dataUnit.getUID()).toString();
		Element el = addUserDefined(detParams,"PAMGuardUID", uid);
		DataUnitFileInformation fileInf = dataUnit.getDataUnitFileInformation();
		if (fileInf != null) {
			el.setAttribute("BinaryFile", fileInf.getShortFileName(2048));
			el.setAttribute("FileIndex", Long.valueOf(fileInf.getIndexInFile()).toString());
		}
		if (dataUnit.getDatabaseIndex() > 0) {
			// only write the database index if it's > 0, i.e. is used.
			addUserDefined(detParams, "DatabaseId", String.format("%d", dataUnit.getDatabaseIndex()));
		}
		// output the channel map, making sure it's positive even if last of 
		// the 32 bits is set. 
		long chanMap = dataUnit.getChannelBitmap();
		if (chanMap < 0) chanMap += 65536L;
		Element userEl = addUserDefined(detParams, "ChannelBitmap", String.format("0x%X", chanMap));
		// add annotations if they exist. 
		int nAnnots = dataUnit.getNumDataAnnotations();
		for (int i = 0; i < nAnnots; i++) {
			DataAnnotation annotation = dataUnit.getDataAnnotation(i);
			DataAnnotationType type = annotation.getDataAnnotationType();
			AnnotationXMLWriter xmlWriter = type.getXMLWriter();
			if (xmlWriter != null) {
				// see near line 182 in TethysPArameterPacker
				el = packAnnotation(annotation, dataUnit);
				if (el != null) {
					detParams.getUserDefined().getAny().add(el);
				}
//					try {
//						Helper hhh = new Helper();
//						hhh.AddAnyElement(detParams.getUserDefined().getAny(), "Dummyeleemnt", "summy value");
//					} catch (JAXBException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (ParserConfigurationException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
				
//				el = null;
//				Helper helper = null;
//				try {
//					helper = new Helper();
//				} catch (JAXBException e) {
//					e.printStackTrace();
//				}
//				try {
//					el = helper.AddAnyElement(detParams.getUserDefined().getAny(), "annotation"+i, "dumvalue");
//				} catch (JAXBException e) {
//					e.printStackTrace();
//					return null;
//				} catch (ParserConfigurationException e) {
//					e.printStackTrace();
//					return null;
//				}
//				QName qnamef = new QName(MarshalXML.schema, "datafilter", "ty");
//				JAXBElement<String> jaxelf = new JAXBElement<String>(
//						qnamef, String.class, "annotation"+i);
//				Document docf = null;
//				try {
//					docf = marshaller.marshalToDOM(jaxelf);
//				} catch (JAXBException | ParserConfigurationException e1) {
//					e1.printStackTrace();
//				}  
//				Element elf = docf.getDocumentElement();
				
				
//				Document doc = XMLUtils.createBlankDoc();
//				Element annotEl = xmlWriter.writeAnnotation(doc, dataUnit, annotation);
//				if (annotEl != null) {
//					userEl.appendChild(annotEl);
//				}
			}
			
		}

		return detection;
	}
	public Element packAnnotation(DataAnnotation annotation, PamDataUnit dataUnit) {
		DataAnnotationType type = annotation.getDataAnnotationType();
		if (type == null) {
			return null;
		}
		AnnotationXMLWriter xmlWriter = type.getXMLWriter();
		if (xmlWriter == null) {
			return null;
		}
		/*
		 * Helper has following:
		 * 		QName qname = new QName(MarshalXML.schema, name, "ty");
				JAXBElement<String> jaxel = new JAXBElement<String>(qname, String.class, value);
		
				Document doc = marshaller.marshalToDOM(jaxel);  
				Element el = doc.getDocumentElement();
				ellist.add(el);
		 */
		
		Document doc = null;
		QName qname = new QName(MarshalXML.schema, "annotation", "ty");
		JAXBElement<String> jaxel = new JAXBElement<String>(
				qname, String.class, annotation.getDataAnnotationType().getAnnotationName());
		try {
			doc = marshaller.marshalToDOM(jaxel);
		} catch (JAXBException | ParserConfigurationException e1) {
			e1.printStackTrace();
		}  
	
		Element el = doc.getDocumentElement();
		
//		Element pEl = xmlWriter.writeObjectData(doc, el, data, null);
		Element aEl = xmlWriter.writeAnnotation(doc, dataUnit, annotation);
//		el.appendChild(aEl);
		return el;
	}

	public static Element addUserDefined(Parameters parameters, String parameterName, String parameterValue) {
		UserDefined userDefined = parameters.getUserDefined();
		if (userDefined == null) {
			userDefined = new UserDefined();
			parameters.setUserDefined(userDefined);
		}
		Element el = null;
		Helper helper = null;
		try {
			helper = new Helper();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		try {
			el = helper.AddAnyElement(userDefined.getAny(), parameterName, parameterValue);
		} catch (JAXBException e) {
			e.printStackTrace();
			return null;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
		return el;
	}

	/**
	 * Get tonal sounds contour. Sadly there are two slightly different interfaces in use
	 * in PAMGuard, so try them both.
	 * @param detParams
	 * @return true if a contour was added
	 */
	private boolean gotTonalContour(PamDataUnit dataUnit, nilus.Detection.Parameters detParams) {
		if (dataUnit instanceof TFContourProvider) {
			TFContourProvider tfcp = (TFContourProvider) dataUnit;
			TFContourData cd = tfcp.getTFContourData();
			if (cd != null) {
				long[] tMillis = cd.getContourTimes();
				double[] fHz = cd.getRidgeFrequency();
				nilus.Detection.Parameters.Tonal tonal = new nilus.Detection.Parameters.Tonal();
				List<Double> offsetS = tonal.getOffsetS();
				List<Double> hz = tonal.getHz();
				for (int i = 0; i < tMillis.length; i++) {
					offsetS.add(roundSignificantFigures((tMillis[i]-tMillis[0]) / 1000., 4));
					hz.add(roundSignificantFigures(fHz[i], 4));
				}
				detParams.setTonal(tonal);
				return true;
			}
		}
		if (dataUnit instanceof WhistleContour) {
			WhistleContour wc = (WhistleContour) dataUnit;
			double[] t = wc.getTimesInSeconds();
			double[] f = wc.getFreqsHz();
			if (t != null && f != null) {
				nilus.Detection.Parameters.Tonal tonal = new nilus.Detection.Parameters.Tonal();
				List<Double> offsetS = tonal.getOffsetS();
				List<Double> hz = tonal.getHz();
				for (int i = 0; i < t.length; i++) {
					offsetS.add(roundSignificantFigures(t[i]-t[0],4));
					hz.add(roundSignificantFigures(f[i],4));
				}
				detParams.setTonal(tonal);
				return true;
			}
		}
		return false;
	}

	private SpeciesIDType getSpeciesIdType() {
		SpeciesIDType species = new SpeciesIDType();
		species.setValue(BigInteger.valueOf(180537));
		return species;
	}

	@Override
	public void getEffortKinds(PDeployment pDeployment, List<DetectionEffortKind> effortKinds, StreamExportParams exportParams) {

		DataBlockSpeciesManager speciesManager = pamDataBlock.getDatablockSpeciesManager();
		if (speciesManager == null) {
			return;
		}
		DataBlockSpeciesMap speciesMap = speciesManager.getDatablockSpeciesMap();
		ArrayList<String> speciesCodes = speciesManager.getAllSpeciesCodes();
		if (speciesCodes == null || speciesMap == null) {
			return;
		}
		for (String speciesCode : speciesCodes) {
			
			boolean sel = exportParams.getSpeciesSelection(speciesCode);
			if (sel == false) {
				continue;
			}

			SpeciesMapItem mapItem = speciesMap.getItem(speciesCode);

			DetectionEffortKind kind = new DetectionEffortKind();
			try {
				nilus.Helper.createRequiredElements(kind);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			kind.getSpeciesId().setValue(BigInteger.valueOf(mapItem.getItisCode()));
			kind.getGranularity().setValue(exportParams.granularity);
			//			nilus.DetectionEffortKind.Parameters granularityParams = kind.getParameters();
			switch (exportParams.granularity) {
			case BINNED:
				kind.getGranularity().setBinSizeMin(exportParams.binDurationS/60.);
				long firstBin = DetectionsHandler.roundDownBinStart(pDeployment.getAudioStart(), (long) (exportParams.binDurationS*1000));
				kind.getGranularity().setFirstBinStart(TethysTimeFuncs.xmlGregCalFromMillis(firstBin));
				break;
			case CALL:
				break;
			case ENCOUNTER:
				kind.getGranularity().setEncounterGapMin(exportParams.encounterGapS/60.);
				break;
			case GROUPED:
				break;

			}
			kind.setCall(mapItem.getCallType());


			effortKinds.add(kind);

		}

	}

	@Override
	public String getDetectionsMethod() {
		/*
		 *  could really do with knowing what type of detector we're dealing with, i.e. if it's
		 *  automatic or manual. For most blocks this is fixed, though some may have a mixture of both !
		 */
		DataAutomationInfo dataAutomation = pamDataBlock.getDataAutomationInfo();
		String method;
		PamControlledUnit pcu = pamDataBlock.getParentProcess().getPamControlledUnit();
		if (dataAutomation == null) {
			method = String.format("Processing using the PAMGuard %s", pcu.getUnitType());
		}
		else {
			method = String.format("%s processing using the PAMGuard %s", dataAutomation.getAutomation(), pcu.getUnitType());
		}

		return method;
	}

	@Override
	public GranularityEnumType[] getAllowedGranularities() {
		GranularityEnumType[] allowed = {GranularityEnumType.CALL, GranularityEnumType.BINNED, GranularityEnumType.ENCOUNTER};
		return allowed;
	}

	@Override
	public String getDetectionsName() {
		PamProcess process = pamDataBlock.getParentProcess();
		PamControlledUnit pcu = process.getPamControlledUnit();
		String pcuName = pcu.getUnitName();
		String blockName = pamDataBlock.getDataName();
		String documentName;
		/**
		 * If the datablock name is the same as the unit name, no need to repeat onesself.
		 */
		if (pcuName.equals(blockName)) {
			documentName = new String(pcuName); // copy it, since we're about to modify it!
		}
		else {
			documentName = pcuName + " " + blockName;
		}
		documentName = documentName.replace(' ', '_');
		return documentName;
	}

	public static double roundDecimalPlaces(double value, int decPlaces) {
		double scale = Math.pow(10, decPlaces);
		long longVal = Math.round(value*scale);
		return longVal/scale;
	}

	public static double roundSignificantFigures(double value, int sigFigs) {
		if (value == 0) {
			return 0;
		}
		double sign = Math.signum(value);
		value = Math.abs(value);
		double scale = sigFigs-Math.floor(Math.log10(value));
		scale = Math.pow(10, scale);
		long longVal = Math.round(value*scale);
		return sign*longVal/scale;
	}

	@Override
	public boolean wantExportDialogCard(ExportWizardCard wizPanel) {
		return true;
	}

	/**
	 * @return the tethysControl
	 */
	public TethysControl getTethysControl() {
		return tethysControl;
	}

	@Override
	public boolean hasDetections() {
		return true;
	}


	@Override
	public boolean canExportLocalisations(GranularityEnumType granularityType) {
		LocalisationInfo locCont = pamDataBlock.getLocalisationContents();
		if (locCont == null) {
			return false;
		}
		return (locCont.getLocContent() > 0 & granularityOK(granularityType));
	}

	/**
	 * Granularity is OK for export.
	 * @param granularityType
	 * @return
	 */
	public boolean granularityOK(GranularityEnumType granularityType) {
		return (granularityType == null || granularityType == GranularityEnumType.CALL);
	}

	@Override
	public LocalisationAlgorithm getLocalisationAlgorithm() {
		return pamDataBlock.getLocalisationAlgorithm();
	}

	@Override
	public TethysLocalisationInfo getLocalisationInfo() {
		LocalisationInfo locCont = pamDataBlock.getLocalisationContents();
		if (locCont == null || locCont.getLocContent() == 0) {
			return null;
		}
		else {
			return new TethysLocalisationInfo(pamDataBlock);
		}
	}

	/**
	 * @return the pamDataBlock
	 */
	protected PamDataBlock getPamDataBlock() {
		return pamDataBlock;
	}

	/**
	 * @return the addFrequencyInfo
	 */
	public boolean isAddFrequencyInfo() {
		return addFrequencyInfo;
	}

	/**
	 * @param addFrequencyInfo the addFrequencyInfo to set
	 */
	public void setAddFrequencyInfo(boolean addFrequencyInfo) {
		this.addFrequencyInfo = addFrequencyInfo;
	}



}
