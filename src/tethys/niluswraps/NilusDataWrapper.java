package tethys.niluswraps;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.datatype.XMLGregorianCalendar;

import PamguardMVC.PamDataBlock;
import nilus.DataSourceType;
import nilus.DescriptionType;
import nilus.QualityAssuranceProcessType;

/**
 * Wrapper for Nilus data objects. This means Detections and Localization documents which 
 * should have an associated datablock, a deployment wrapper and a data count. 
 * @author dg50
 *
 * @param <T>
 */
public class NilusDataWrapper<T> extends NilusDocumentWrapper<T> {

	public Integer count;
	
	public PDeployment deployment;

	public PamDataBlock dataBlock;

	public NilusDataWrapper(T nilusObject, PamDataBlock dataBlock, PDeployment deployment, Integer count) {
		super(nilusObject);
		this.dataBlock = dataBlock;
		this.deployment = deployment;
		this.count = count;
	}
	
	public DescriptionType getDescription() {
		return (DescriptionType) getGotObject("getDescription");
	}

	public DataSourceType getDataSource() {
		return (DataSourceType) getGotObject("getDataSource");
	}


	public QualityAssuranceProcessType getQualityAssurance() {
		return (QualityAssuranceProcessType) getGotObject("getQualityAssurance");
	}
	
	public XMLGregorianCalendar getEffortStart() {
		return (XMLGregorianCalendar) getGotObjects("getEffort", "getStart");
	}
	
	public XMLGregorianCalendar getEffortEnd() {
		return (XMLGregorianCalendar) getGotObjects("getEffort", "getEnd");
	}

}
