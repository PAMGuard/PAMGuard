package tethys.pamdata;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;

import org.docx4j.model.listnumbering.NumberFormatLowerLetter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import PamController.PamControlledUnit;
import PamController.PamSettings;
import PamController.settings.output.xml.PamguardXMLWriter;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterData;
import PamModel.parametermanager.PamParameterSet;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import nilus.MarshalXML;
import tethys.TethysControl;
import tethys.output.TethysExportParams;

/**
 * Functions to pack up a PAMGuard parameters object into the correct format
 * for Tethys. This is very similar to functions in PamguardXMLWriter.writeUnitSettings
 * but seems to have enough differences that it needs totally rewriting for Tethys. 
 * @author dg50
 *
 */
public class TethysParameterPacker {

	private MarshalXML marshaller;
	
	private PamguardXMLWriter xmlWriter;

	private TethysControl tethysControl;

	/**
	 * @throws JAXBException 
	 * 
	 */
	public TethysParameterPacker(TethysControl tethysControl) throws JAXBException {
		super();
		this.tethysControl = tethysControl;
		try {
			marshaller = new MarshalXML();
		} catch (JAXBException e) {
		}
		xmlWriter = PamguardXMLWriter.getXMLWriter();
	}

	/**
	 * Get a list of elements of parameters for all modules feeding 
	 * the given datablock. These are given in reverse order. 
	 * @param pamDataBlock output datablock
	 * @param fullChain 
	 * @return parameters of all modules feeding that datablock. 
	 */
	public List<Element> packParameters(PamDataBlock pamDataBlock) {
		PamProcess pamProcess = pamDataBlock.getParentProcess();
		PamControlledUnit pamControlledUnit = pamProcess.getPamControlledUnit();
		if (pamControlledUnit == null || pamControlledUnit instanceof PamSettings == false) {
			return null;
		}

		int paramOption = tethysControl.getTethysExportParams().detectorParameterOutput;
		if (paramOption == TethysExportParams.DETECTORE_PARAMS_NONE) {
			return null;
		}
		
		PamSettings pamSettings = (PamSettings) pamControlledUnit;
		
		List<Element> elList = new ArrayList<Element>();
		Object data = pamSettings.getSettingsReference();

		ArrayList<Object> objectHierarchy = new ArrayList<>();

		PamParameterSet parameterSet;
		if (data instanceof ManagedParameters) {
			parameterSet = ((ManagedParameters) data).getParameterSet();
		}
		else {
			int exclusions = 0;
			//			if (writerSettings.includeConstants == false) {
			exclusions = Modifier.STATIC | Modifier.FINAL;
			//			}
			parameterSet = PamParameterSet.autoGenerate(data, exclusions);
		}
		if (parameterSet == null) {
			return null;
		}
		// get the XML writer ready for a new export ...
		xmlWriter.setExcludeDisplaySettings(true);
		xmlWriter.makeSettingsList();
		
		/**
		 * first do the data filter. I can't see any way of doing this
		 * without creating a doc as was in the helper example. 
		 */
		DataSelector dataSelector = pamDataBlock.getDataSelector(tethysControl.getDataSelectName(), false);
		if (dataSelector != null) {
			DataSelectParams filterParams = dataSelector.getParams();
			if (filterParams != null) {
				int selected = filterParams.getCombinationFlag();
				if (selected != DataSelectParams.DATA_SELECT_DISABLE) {
					QName qnamef = new QName(MarshalXML.schema, "datafilter", "ty");
					JAXBElement<String> jaxelf = new JAXBElement<String>(
							qnamef, String.class, parameterSet.getParentObject().getClass().getCanonicalName());
					Document docf = null;
					try {
						docf = marshaller.marshalToDOM(jaxelf);
					} catch (JAXBException | ParserConfigurationException e1) {
						e1.printStackTrace();
					}  
					Element elf = docf.getDocumentElement();
					elList.add(elf);/**
					 * Is there a data filter ? If so, write it's 
					 * XML parameters out here. 
					 */
					Element pEl = xmlWriter.writeObjectData(docf, elf, filterParams, null);
				}
				//				if (pEl != null) {
////					filterEl.appendChild(pEl);
//					elf.appendChild(filterEl);
//				}
			}
		}
		if (paramOption == TethysExportParams.DETECTOR_DATASELECTOR) {
			return elList;
		}		
		
		QName qname = new QName(MarshalXML.schema, "parameters", "ty");
		JAXBElement<String> jaxel = new JAXBElement<String>(
				qname, String.class, parameterSet.getParentObject().getClass().getCanonicalName());
		Document doc = null;
		try {
			doc = marshaller.marshalToDOM(jaxel);
		} catch (JAXBException | ParserConfigurationException e1) {
			e1.printStackTrace();
		}  
		Element el = doc.getDocumentElement();
		elList.add(el);

		
		
		/**
		 * Now get the chain of PAMGuard modules for the current detector and for 
		 * all upstream modules. 
		 */
		ArrayList<PamControlledUnit> moduleChain = getParentChain(pamDataBlock);
		for (PamControlledUnit pcu : moduleChain) {
			if (pcu instanceof PamSettings == false) {
				continue;
			}
			pamSettings = (PamSettings) pcu;
			Element pgEl = xmlWriter.writeUnitSettings(doc, el, pamSettings);
			if (pgEl != null) {
				el.appendChild(pgEl);
				//			elList.add(pgEl);
			}
			if (paramOption == TethysExportParams.DETECTORE_PARAMS_MODULE) {
				break;
			}
		}
		return elList;
	}
	
	/**
	 * Get a list of parent modules of the datablock, including it's own. 
	 * @param dataBlock
	 * @return
	 */
	private ArrayList<PamControlledUnit> getParentChain(PamDataBlock dataBlock) {
		ArrayList<PamControlledUnit> chain = new ArrayList<>();
		while (dataBlock != null) {
			PamProcess parentProcess = dataBlock.getParentProcess();
			if (parentProcess == null) {
				break;
			}
			PamControlledUnit pamControlledUnit = parentProcess.getPamControlledUnit();
			if (pamControlledUnit == null) {
				break;
			}
			chain.add(pamControlledUnit);
			dataBlock = parentProcess.getParentDataBlock();
		}
		return chain;
	}

	private boolean createElement(Document document, Element parentEl, Object paramData, PamParameterData pamParam, ArrayList<Object> objectHierarchy) {
		Class<? extends Object> javaClass = paramData.getClass();
		if (PamguardXMLWriter.isWritableType(javaClass)) {
			String name = pamParam.getFieldName();
			String value = paramData.toString();
			Element el = document.createElement(name);
//			el.setNodeValue(value);
			el.setTextContent(value);
			parentEl.appendChild(el);
			

//			QName qname = new QName(MarshalXML.schema, name, "ty");
//			JAXBElement<String> jaxel = new JAXBElement<String>(
//					qname, String.class, value);
//			
//
//			try {
//				jxbm.marshal(jaxel, dom);
//			} catch (JAXBException e) {
//				e.printStackTrace();
//			}
//			Document doc = null;
//			try {
//				doc = marshaller.marshalToDOM(jaxel);
//			} catch (JAXBException e) {
//				e.printStackTrace();
//			} catch (ParserConfigurationException e) {
//				e.printStackTrace();
//			}  
//			Element el = doc.getDocumentElement();
//			return el;
			return true;
		}
		if (javaClass.isArray()) {
			return writeArray(document, parentEl, paramData, pamParam, objectHierarchy);
			
		}
		/*
		 * 
		if (javaClass.isArray()) {
			return writeArray(doc, el, data, pamParam, objectHierarchy);
		}
		if (List.class.isAssignableFrom(javaClass)){
			return writeList(doc, el, data, pamParam, objectHierarchy);
		}
		if (Map.class.isAssignableFrom(javaClass)){
			return writeMap(doc, el, data, pamParam, objectHierarchy);
		}
		if (File.class.isAssignableFrom(javaClass)) {
			return writeFile(doc, el, data, pamParam);
		}
		
		else {
			Element e = makeElement(doc, pamParam.getFieldName(), data.getClass().getName());
			el.appendChild(e);
			writeObjectData(doc, e, data, objectHierarchy);
			return e;
		}
		 */
		return false;
	}


	private boolean writeArray(Document document, Element parentEl, Object paramData, PamParameterData pamParam,
			ArrayList<Object> objectHierarchy) {
		if (paramData.getClass().isArray() == false) {
			return false;
		}
		String name = pamParam.getFieldName();
		Element el = document.createElement(name);
		parentEl.appendChild(el);
		int n = Array.getLength(paramData);
		boolean ok = true;
		for (int i = 0; i < n; i++) {
			Object arrayEl = Array.get(paramData, i);
			ok &= createElement(document, el, arrayEl, pamParam, objectHierarchy);
		}
		// TODO Auto-generated method stub
		return ok;
	}


//	private Element writeArray(PamParameterData pamParam, ArrayList<Object> objectHierarchy) {
//		QName qname = new QName(MarshalXML.schema, pamParam.getFieldName(), "ty");
//		JAXBElement<String> jaxel = new JAXBElement<String>(
//				qname, String.class, value);
//		
//		Document doc = null;
//		try {
//			doc = marshaller.marshalToDOM(jaxel);
//		} catch (JAXBException e) {
//			e.printStackTrace();
//		} catch (ParserConfigurationException e) {
//			e.printStackTrace();
//		}  
//		return null;
//	}
}
