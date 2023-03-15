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
import whistleClassifier.WhistleContour;

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
		description.setMethod(pamControlledUnit.getUnitType());
		
		return description;
	}

	@Override
	public AlgorithmType getAlgorithm() {
		AlgorithmType algorithm = new AlgorithmType();
		algorithm.setMethod(this.getAlgorithmMethod());
		algorithm.setSoftware("PAMGuard");
		algorithm.setVersion(PamguardVersionInfo.version);
		algorithm.setParameters(this.getAlgorithmParameters());
		
		return algorithm;
	}

	private Parameters getAlgorithmParameters() {
		if (pamControlledUnit instanceof PamSettings == false) {
			return null;
		}
		PamSettings pamSettings = (PamSettings) pamControlledUnit;
		Parameters parameters = new Parameters();
		List<Element> paramList = parameters.getAny();
		Document doc = XMLUtils.createBlankDoc();
		PamguardXMLWriter pamXMLWriter = PamguardXMLWriter.getXMLWriter();
		Element dummyEl = doc.createElement("MODULES");
		doc.appendChild(dummyEl);
		PamSettings[] settingsObjs = getSettingsObjects();
		if (settingsObjs == null) {
			return null;
		}
//		pamXMLWriter.setStaticNameSpace(TethysControl.xmlNameSpace);
		Element settingsEl = pamXMLWriter.writeUnitSettings(doc, dummyEl, pamSettings, settingsObjs);
		if (settingsEl == null) {
			return null;
		}
		pamXMLWriter.addNameSpaceToElements(doc, settingsEl, TethysControl.xmlNameSpace);
		dummyEl.appendChild(settingsEl);
		NodeList childs = settingsEl.getChildNodes();
		for (int i = 0; i < childs.getLength(); i++) {
			Node el = childs.item(i);
//			System.out.println(el.getNodeName());
			if (el instanceof Element) {
				paramList.add((Element) el);
			}
		}

//		Document doc = pamXMLWriter.writeOneModule((PamSettings) pamControlledUnit, System.currentTimeMillis());
//		String moduleXML = null;
		if (doc != null) {
			// this string should be XML of all the settings for the module controlling this
			// datablock.
//			moduleXML = pamXMLWriter.getAsString(doc, true); // change to false to get smaller xml
//			System.out.printf("Module settings for datablock %s are:\n", moduleXML);
//			System.out.println(moduleXML);
//			Element pamguard = doc.get("PAMGUARD");
//			Element modules = (Element) pamguard.getElementsByTagName("MODULES");
//			doc.get
//			NodeList childs = doc.getChildNodes();
//			for (int i = 0; i < childs.getLength(); i++) {
//				Node el = childs.item(i);
//				System.out.println(el.getNodeName());
//				if (el instanceof Element) {
//					paramList.add((Element) el);
//				}
//			}
//			String moduleXML = pamXMLWriter.getAsString(doc, true); // change to false to get smaller xml
//			System.out.printf("Module settings for datablock %s are:\n%s", this.pamDataBlock.getDataName(), moduleXML);
		}
		
//		// try the old say
//		Document doc2 = pamXMLWriter.writeOneModule((PamSettings) pamControlledUnit, System.currentTimeMillis());
//		String moduleXML = null;
//		if (doc2 != null) {
//			// this string should be XML of all the settings for the module controlling this
//			// datablock.
//			moduleXML = pamXMLWriter.getAsString(doc2, true); // change to false to get smaller xml
//			System.out.printf("Module settings for datablock %s are:\n%s", pamDataBlock.getDataName(),moduleXML);
//		}
//		
		
		return parameters;
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
		detection.setSpeciesId(getSpeciesIdType());
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
//		species.s
		return species;
	}

}
