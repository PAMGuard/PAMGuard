package tethys.pamdata;

import java.math.BigInteger;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import PamController.PamControlledUnit;
import PamController.PamSettings;
import PamController.PamguardVersionInfo;
import PamController.settings.output.xml.PamguardXMLWriter;
import PamUtils.XMLUtils;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.TFContourData;
import PamguardMVC.TFContourProvider;
import generalDatabase.DBSchemaWriter;
import generalDatabase.SQLLogging;
import nilus.AlgorithmType;
import nilus.AlgorithmType.Parameters;
import nilus.Deployment;
import nilus.DescriptionType;
import nilus.Detection;
import nilus.SpeciesIDType;
import tethys.TethysControl;
import tethys.TethysTimeFuncs;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.ITISTypes;
import tethys.species.SpeciesMapItem;
import whistleClassifier.WhistleContour;

import javax.xml.bind.JAXBException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;

/**
 * Automatically provides Tethys data based on the SQL database interface 
 * for a data block. 
 * @author dg50
 *
 */
public class AutoTethysProvider implements TethysDataProvider {

	private PamDataBlock pamDataBlock;
	private PamProcess pamProcess;
	private PamControlledUnit pamControlledUnit;

	public AutoTethysProvider(PamDataBlock pamDataBlock) {
		this.pamDataBlock = pamDataBlock;
		pamProcess = pamDataBlock.getParentProcess();
		pamControlledUnit = pamProcess.getPamControlledUnit();
	}

	@Override
	public TethysSchema getSchema() {
		SQLLogging logging = pamDataBlock.getLogging();
		if (logging == null) {
			return null;
		}
		DBSchemaWriter schemaWriter = new DBSchemaWriter();
		Document doc = schemaWriter.generateDatabaseSchema(pamDataBlock, logging, logging.getTableDefinition());
		TethysSchema schema = new TethysSchema(doc);
		return schema;
	}

	@Override
	public TethysDataPoint getDataPoint(PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		return null;
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
	public AlgorithmType getAlgorithm() {
		AlgorithmType algorithm = new AlgorithmType();
		try {
			nilus.Helper.createRequiredElements(algorithm);
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
//		algorithm.setMethod(this.getAlgorithmMethod());
//		algorithm.setSoftware("PAMGuard");
//		algorithm.setVersion(PamguardVersionInfo.version);
		Parameters algoParameters = this.getAlgorithmParameters();
		if (algoParameters != null) {
			algorithm.setParameters(algoParameters);
		}
		
		return algorithm;
	}

	@Override
	public Parameters getAlgorithmParameters() {
		if (pamControlledUnit instanceof PamSettings == false) {
			return null;
		}
		PamSettings pamSettings = (PamSettings) pamControlledUnit;
		Parameters parameters = new Parameters();
		List<Element> paramList = parameters.getAny();
		Object settings = pamSettings.getSettingsReference();
		TethysParameterPacker paramPacker = null;
		try {
			paramPacker = new TethysParameterPacker();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<Element> genList = paramPacker.packParameters(settings);
		if (genList == null || genList.size() == 0) {
			return null;
		}
		paramList.addAll(genList);
		
//		Document doc = XMLUtils.createBlankDoc();
//		PamguardXMLWriter pamXMLWriter = PamguardXMLWriter.getXMLWriter();
//		Element dummyEl = doc.createElement("MODULES");
//		doc.appendChild(dummyEl);
//		PamSettings[] settingsObjs = getSettingsObjects();
//		if (settingsObjs == null) {
//			return null;
//		}
////				pamXMLWriter.setStaticNameSpace(TethysControl.xmlNameSpace);
//		Element settingsEl = pamXMLWriter.writeUnitSettings(doc, dummyEl, pamSettings, settingsObjs);
//		if (settingsEl == null) {
//			return null;
//		}
//
////		settingsEl = addNameSpaceToElements(doc, settingsEl, TethysControl.xmlNameSpace);
//
//
//		dummyEl.appendChild(settingsEl);
//		NodeList childs = settingsEl.getChildNodes();
//		for (int i = 0; i < childs.getLength(); i++) {
//			Node el = childs.item(i);
//			//			System.out.println(el.getNodeName());
//			if (el instanceof Element) {
//				paramList.add((Element) el);
//			}
//		}
//
//		//		Document doc = pamXMLWriter.writeOneModule((PamSettings) pamControlledUnit, System.currentTimeMillis());
//		//		String moduleXML = null;
//		if (doc != null) {
//			// this string should be XML of all the settings for the module controlling this
//			// datablock.
//			//			moduleXML = pamXMLWriter.getAsString(doc, true); // change to false to get smaller xml
//			//			System.out.printf("Module settings for datablock %s are:\n", moduleXML);
//			//			System.out.println(moduleXML);
//			//			Element pamguard = doc.get("PAMGUARD");
//			//			Element modules = (Element) pamguard.getElementsByTagName("MODULES");
//			//			doc.get
//			//			NodeList childs = doc.getChildNodes();
//			//			for (int i = 0; i < childs.getLength(); i++) {
//			//				Node el = childs.item(i);
//			//				System.out.println(el.getNodeName());
//			//				if (el instanceof Element) {
//			//					paramList.add((Element) el);
//			//				}
//			//			}
//			//			String moduleXML = pamXMLWriter.getAsString(doc, true); // change to false to get smaller xml
//			//			System.out.printf("Module settings for datablock %s are:\n%s", this.pamDataBlock.getDataName(), moduleXML);
//		}
//
//		//		// try the old say
//		//		Document doc2 = pamXMLWriter.writeOneModule((PamSettings) pamControlledUnit, System.currentTimeMillis());
//		//		String moduleXML = null;
//		//		if (doc2 != null) {
//		//			// this string should be XML of all the settings for the module controlling this
//		//			// datablock.
//		//			moduleXML = pamXMLWriter.getAsString(doc2, true); // change to false to get smaller xml
//		//			System.out.printf("Module settings for datablock %s are:\n%s", pamDataBlock.getDataName(),moduleXML);
//		//		}
//		//		

		return parameters;
	}

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
		 */
		detection.setChannel(BigInteger.valueOf(dataUnit.getChannelBitmap()));

		nilus.Detection.Parameters detParams = new nilus.Detection.Parameters();
		detection.setParameters(detParams);
		double[] freqs = dataUnit.getFrequency();
		if (freqs != null) {
			detParams.setMinFreqHz(freqs[0]);
			detParams.setMaxFreqHz(freqs[1]);
		}
		double ampli = dataUnit.getAmplitudeDB();
		detParams.setReceivedLevelDB(ampli);
		//		DataUnitBaseData basicData = dataUnit.getBasicData();
		gotTonalContour(dataUnit, detParams);

		return detection;
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
					offsetS.add((double) (tMillis[i]-tMillis[0]) / 1000.);
					hz.add(fHz[i]);
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
					offsetS.add(t[i]-t[0]);
					hz.add(f[i]);
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

}
