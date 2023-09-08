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
import nilus.MarshalXML;

/**
 * Functions to pack up a PAMGuard parameters object into the correct format
 * for Tethys. This is very similar to functions in PamguardXMLWriter.writeUnitSettings
 * but seems to have enough differences that it needs totally rewriting for Tethys. 
 * @author dg50
 *
 */
public class TethysParameterPacker {

//	/**
//	 * Parameters should look something like below. however, only packing them with a long 
//	schema name seems to work. 
//	 * 
//<Algorithm>
//<Method>Analyst detections</Method>
//<Software>Triton</Software>
//<Version>unknown</Version>
//<Parameters>
//<LTSA_plot_time_h>0.75</LTSA_plot_time_h>
//<LTSA_low_Hz>0.0</LTSA_low_Hz>
//<LTSA_high_Hz>5000.0</LTSA_high_Hz>
//<LTSA_brightness>30.0</LTSA_brightness>
//</Parameters>
//</Algorithm>
	/*
	 * 
//			// this works. Can look at the source to see how it's done. 
//			// may have fun making this work for more complex structures. 
//			try {
//				Helper helper = new Helper();
//				helper.AddAnyElement(paramList, "Threshold", "3.5");
//				
//				 *  and see Matlab code for dbStruct2DOM at C:\Users\dg50\source\repos\TethysMatlab\db
//				 *  for more complex structures
//				 *  This looks like it may be possible to rewrite my functions for 
//				 *  writing structures to XML using the helper.AddAnyElement function as 
//				 *  an example and I should be able to output my complex structures. 
//				 
//			} catch (JAXBException | ParserConfigurationException e) {
//				e.printStackTrace();
//			}
	 */

	private MarshalXML marshaller;
	
	private PamguardXMLWriter xmlWriter;

	/**
	 * @throws JAXBException 
	 * 
	 */
	public TethysParameterPacker() throws JAXBException {
		super();
		try {
			marshaller = new MarshalXML();
		} catch (JAXBException e) {
		}
		xmlWriter = PamguardXMLWriter.getXMLWriter();
	}

	public List<Element> packParameters(PamDataBlock pamDataBlock) {
		PamProcess pamProcess = pamDataBlock.getParentProcess();
		PamControlledUnit pamControlledUnit = pamProcess.getPamControlledUnit();
		if (pamControlledUnit == null || pamControlledUnit instanceof PamSettings == false) {
			return null;
		}
		PamSettings pamSettings = (PamSettings) pamControlledUnit;
//		return null;
//	}
//
//	public List<Element> packParameters(Object data) {
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
//		Document document = null;
//		try {
//			document =  DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
//		} catch (ParserConfigurationException e1) {
//			e1.printStackTrace();
//		}
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

		for (PamParameterData pamParam : parameterSet.getParameterCollection()) {
			try {
				Object paramData = pamParam.getData();
				boolean ok = createElement(doc, el, paramData, pamParam, objectHierarchy);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		elList.add(el);
		xmlWriter.setExcludeDisplaySettings(true);
		xmlWriter.makeSettingsList();
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
